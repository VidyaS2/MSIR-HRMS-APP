package com.attendance.clockme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminThreeDashboard extends AppCompatActivity {
    private static final String PREFS_NAME = "MyAppPrefs";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private SharedPreferences sharedPreferences;

    private TextView welcomeTextView;
    private TextView countTextView,count2TextView,count3TextView,count4TextView,count5TextView,count6TextView;

    private String loggedInEmail;
    private String loggedInTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin3_dashboard);

        // Initialize Firebase and SharedPreferences
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Bind UI elements
        welcomeTextView = findViewById(R.id.welcomeMessage);
        countTextView = findViewById(R.id.count1);
        count2TextView = findViewById(R.id.count2);
        count3TextView = findViewById(R.id.count3);
        count4TextView = findViewById(R.id.count4);
        count5TextView = findViewById(R.id.count5);
        count6TextView = findViewById(R.id.count6);

        // Get logged-in user's email
        if (mAuth.getCurrentUser() != null) {
            loggedInEmail = mAuth.getCurrentUser().getEmail();
        }

        // Fetch user data and update UI
        if (loggedInEmail != null) {
            fetchUserData(loggedInEmail);
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
        }

        // Set up navigation for various layouts
        setupNavigation(R.id.homelayout, DashboardActivity.class);
        setupNavigation(R.id.clocklayout, SwipingTimeActivity.class);
        setupNavigation(R.id.requestlayout, SalaryDetailsActivity.class);
        setupNavigation(R.id.per_in, AdminThreeRequests.class);
        setupNavigation(R.id.incoff, AdminThreeCoffIn.class);
        setupNavigation(R.id.outcoff, AdminThreeCoffOut.class);
        findViewById(R.id.btn_attendance).setOnClickListener(v -> navigateTo(AdminThreeEmployee.class));
        findViewById(R.id.btn_attendance2).setOnClickListener(v -> navigateTo(TeamAttendanceActivity.class));

        // Navigate to EditInSwipeMissing when inswipemissing ImageView is clicked
        setupSpecificNavigation(R.id.inswipemissing, EditInSwipeMissing.class);

        ImageView outswipeMissing = findViewById(R.id.outswipemissing);
        if (outswipeMissing != null) {
            outswipeMissing.setOnClickListener(v -> navigateTo(EditOutSwipeMissing.class));
        }

        // Navigate to AdminThreePermissionOut when clockOutPermission ImageView is clicked
        setupSpecificNavigation(R.id.clockOutPermission, AdminThreePermissionOut.class);

        // Handle logout functionality
        findViewById(R.id.signoutlayout).setOnClickListener(v -> handleLogout());
    }

    /**
     * Fetch user data from Firebase based on email.
     */
    private void fetchUserData(String email) {
        // First, fetch the username from the "Users" node using the provided email
        mDatabase.child("Users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String username = userSnapshot.child("username").getValue(String.class);

                        // Set the welcome message with the username from the "Users" node
                        welcomeTextView.setText(username != null ? "Welcome, " + username + "!" : "Welcome!");

                        // After fetching the username, determine the user's team based on the email
                        determineUserTeam(email);
                    }
                } else {
                    Toast.makeText(AdminThreeDashboard.this, "User data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminThreeDashboard.this, "Error fetching data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Determine the user's team based on their email.
     */

    private void determineUserTeam(String email) {
        // Determine the team based on the email address
        if (email.equals("azeem@msirindia.com")) {
            loggedInTeam = "Team 1";
        } else if (email.equals("kamal@msirindia.com")) {
            loggedInTeam = "Team 2";
        } else if (email.equals("karthick@msirindia.com")) {
            loggedInTeam = "Team 3";
        } else {
            loggedInTeam = "Unknown"; // Fallback if email is not recognized
        }

        // Fetch the pending requests count for the determined team
        fetchPendingRequestsCount(loggedInTeam);
        fetchClockOutPendingRequestsCount(loggedInTeam);
        fetchLateInRequestsCount(loggedInTeam);
        fetchLateOutRequestsCount(loggedInTeam);
        fetchCoffInPendingRequestsCount(loggedInTeam);
        fetchCoffOutPendingRequestsCount(loggedInTeam);
    }

    /**
     * Fetch and count the total number of ClockInPermission with status "pending" for the logged-in user's team.
     */
    private void fetchPendingRequestsCount(String team) {
        // Query the "ClockInPermission" node for the logged-in team's data
        mDatabase.child("ClockInPermission").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int pendingCount = 0;

                // Loop through each user's data in the team and count pending requests
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot dateSnapshot : userSnapshot.getChildren()) {
                        // Check if the status of the request is "pending"
                        String team1 = dateSnapshot.child("team").getValue(String.class);
                        String status = dateSnapshot.child("status").getValue(String.class);
                        if (team != null && team.equalsIgnoreCase(team1) && "pending".equalsIgnoreCase(status)) {
                            pendingCount++;
                        }
                    }
                }

                // Update the countTextView with the pending request count
                countTextView.setText(String.valueOf(pendingCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminThreeDashboard.this, "Error fetching pending requests: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchClockOutPendingRequestsCount(String team) {
        // Query the "ClockOutPermission" node
        mDatabase.child("ClockOutPermission").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int pendingCount = 0;

                // Loop through each user's data in the team and count pending requests
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot dateSnapshot : userSnapshot.getChildren()) {
                        // Check if the status of the request is "pending"
                        String team1 = dateSnapshot.child("team").getValue(String.class);
                        String status = dateSnapshot.child("status").getValue(String.class);
                        if (team != null && team.equalsIgnoreCase(team1) && "pending".equalsIgnoreCase(status)) {
                            pendingCount++;
                        }
                    }
                }

                // Update the count2TextView with the pending request count
                count2TextView.setText(String.valueOf(pendingCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminThreeDashboard.this, "Error fetching ClockOut pending requests: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchLateInRequestsCount(String team) {
        // Query the "LateInRequests" node
        mDatabase.child("LateInRequests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int pendingCount = 0;

                // Loop through each user's data in the node and count pending requests
                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    // Extract the selectedTeam and status fields
                    String selectedTeam = requestSnapshot.child("selectedTeam").getValue(String.class);
                    String status = requestSnapshot.child("status").getValue(String.class);

                    // Check if the selectedTeam matches the provided team and status is "pending"
                    if (team != null && team.equalsIgnoreCase(selectedTeam) && "pending".equalsIgnoreCase(status)) {
                        pendingCount++;
                    }
                }

                // Update the lateInCountTextView with the pending request count
                count3TextView.setText(String.valueOf(pendingCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminThreeDashboard.this, "Error fetching Late In requests: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchLateOutRequestsCount(String team) {
        // Query the "LateOutRequests" node
        mDatabase.child("LateOutRequests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int pendingCount = 0;

                // Loop through each user's data in the node and count pending requests
                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    // Extract the selectedTeam and status fields
                    String selectedTeam = requestSnapshot.child("selectedTeam").getValue(String.class);
                    String status = requestSnapshot.child("status").getValue(String.class);

                    // Check if the selectedTeam matches the provided team and status is "pending"
                    if (team != null && team.equalsIgnoreCase(selectedTeam) && "pending".equalsIgnoreCase(status)) {
                        pendingCount++;
                    }
                }

                // Update the lateOutCountTextView with the pending request count
                count4TextView.setText(String.valueOf(pendingCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminThreeDashboard.this, "Error fetching Late Out requests: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCoffInPendingRequestsCount(String team) {
        // Query the "ClockOutPermission" node
        mDatabase.child("CoffHdPermission").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int pendingCount = 0;

                // Loop through each user's data in the team and count pending requests
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot dateSnapshot : userSnapshot.getChildren()) {
                        // Check if the status of the request is "pending"
                        String team1 = dateSnapshot.child("team").getValue(String.class);
                        String status = dateSnapshot.child("status").getValue(String.class);
                        if (team != null && team.equalsIgnoreCase(team1) && "pending".equalsIgnoreCase(status)) {
                            pendingCount++;
                        }
                    }
                }

                // Update the count2TextView with the pending request count
                count5TextView.setText(String.valueOf(pendingCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminThreeDashboard.this, "Error fetching ClockOut pending requests: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCoffOutPendingRequestsCount(String team) {
        // Query the "ClockOutPermission" node
        mDatabase.child("CoffHdOutPermission").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int pendingCount = 0;

                // Loop through each user's data in the team and count pending requests
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot dateSnapshot : userSnapshot.getChildren()) {
                        // Check if the status of the request is "pending"
                        String team1 = dateSnapshot.child("team").getValue(String.class);
                        String status = dateSnapshot.child("status").getValue(String.class);
                        if (team != null && team.equalsIgnoreCase(team1) && "pending".equalsIgnoreCase(status)) {
                            pendingCount++;
                        }
                    }
                }

                // Update the count2TextView with the pending request count
                count6TextView.setText(String.valueOf(pendingCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminThreeDashboard.this, "Error fetching ClockOut pending requests: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Set up navigation for layouts using LinearLayout IDs.
     */
    private void setupNavigation(int layoutId, Class<?> targetActivity) {
        View view = findViewById(layoutId);
        if (view != null) {
            view.setOnClickListener(v -> navigateTo(targetActivity));
        }
    }

    /**
     * Set up navigation for specific buttons using ImageView IDs.
     */
    private void setupSpecificNavigation(int viewId, Class<?> targetActivity) {
        ImageView view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> navigateTo(targetActivity));
        }
    }

    /**
     * Navigate to a specific activity.
     */
    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(AdminThreeDashboard.this, targetActivity);
        startActivity(intent);
    }

    /**
     * Handle user logout.
     */
    private void handleLogout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        mAuth.signOut();

        Intent intent = new Intent(AdminThreeDashboard.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        Toast.makeText(AdminThreeDashboard.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}
