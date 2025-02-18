package com.attendance.clockme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ClientSwipeOut extends AppCompatActivity {

    private LinearLayout permissionRequestContainer;
    private TextView welcomeMessage; // To display the username
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference permissionRequestsRef;
    private static final String PREFS_NAME = "MyPrefs";
    private SharedPreferences sharedPreferences;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_lateout_status);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        permissionRequestContainer = findViewById(R.id.cardContainer);
        welcomeMessage = findViewById(R.id.welcomeMessage);

        // Firebase reference to PermissionRequests
        permissionRequestsRef = mDatabase.child("ClockOutPermission");
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Load user data and permission requests
        loadUserDataAndDisplay();

        LinearLayout homeIcon = findViewById(R.id.homelayout01);
        LinearLayout clockIcon = findViewById(R.id.clocklayout01);
        LinearLayout logoutIcon = findViewById(R.id.signoutlayout01);
        LinearLayout requestIcon = findViewById(R.id.requestlayout01);

        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ClientSwipeOut.this, DashboardActivity.class);
            startActivity(intent);
        });

        logoutIcon.setOnClickListener(v -> {
            // Clear SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Sign out from Firebase Authentication
            FirebaseAuth.getInstance().signOut();

            // Redirect to the SignInActivity
            Intent intent = new Intent(ClientSwipeOut.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Optionally, show a message indicating the user has logged out
            Toast.makeText(ClientSwipeOut.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        clockIcon.setOnClickListener(v -> {
            Intent clockInOutIntent = new Intent(ClientSwipeOut.this, SwipingTimeActivity.class);
            startActivity(clockInOutIntent);
        });

        // Set listener for requestIcon
        requestIcon.setOnClickListener(v -> {
            Intent salaryDetailsIntent = new Intent(ClientSwipeOut.this, SalaryDetailsActivity.class);
            startActivity(salaryDetailsIntent);
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchLateOutRequests();
    }

    private void fetchLateOutRequests() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Navigate to the correct path in Firebase (LateOutRequests > uid)
            DatabaseReference lateOutRequestsRef = FirebaseDatabase.getInstance().getReference("LateOutRequests");
            lateOutRequestsRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    permissionRequestContainer.removeAllViews(); // Clear previous views if needed

                    if (!snapshot.exists()) {
                        Toast.makeText(ClientSwipeOut.this, "No swipe out requests found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Iterate through each request node under the UID
                    for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                        // Fetch the required data from the snapshot
                        String username = requestSnapshot.child("username").getValue(String.class);
                        String date = requestSnapshot.child("date").getValue(String.class);
                        String remark = requestSnapshot.child("remark").getValue(String.class);
                        String status = requestSnapshot.child("status").getValue(String.class);

                        // Capitalize first letter of username if not null
                        if (username != null) {
                            username = capitalizeFirstLetter(username);
                        }

                        // Add the fetched data to the UI by calling the addPermissionRequestCard method
                        addLateOutRequestCard(username, date, remark, status);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ClientSwipeOut.this, "Failed to fetch late-out requests", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ClientSwipeOut.this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private void addLateOutRequestCard(String username, String date, String remark, String status) {
        // Inflate a new card layout for each late-out request
        View cardView = LayoutInflater.from(this).inflate(R.layout.permission_status_card, permissionRequestContainer, false);

        // Set data to the views in the card
        TextView usernameTextView = cardView.findViewById(R.id.tvUsername);
        TextView dateTextView = cardView.findViewById(R.id.tvDate);
        TextView remarkTextView = cardView.findViewById(R.id.tvReason);
        TextView statusTextView = cardView.findViewById(R.id.tvStatus);

        usernameTextView.setText(username);
        dateTextView.setText("Date: " + date);
        remarkTextView.setText("Remark: " + (remark != null ? remark : "No remarks"));
        statusTextView.setText("Status: " + (status != null ? status : "Pending"));

        // Add the card to the container
        permissionRequestContainer.addView(cardView);
    }

    private void loadUserDataAndDisplay() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null) {
                DatabaseReference usersRef = mDatabase.child("Users");
                usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String username = userSnapshot.child("username").getValue(String.class);
                                if (username != null) {
                                    welcomeMessage.setText("Welcome, " + capitalizeFirstLetter(username) + "!");
                                } else {
                                    Toast.makeText(ClientSwipeOut.this, "Username not found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(ClientSwipeOut.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ClientSwipeOut.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(ClientSwipeOut.this, "User email not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ClientSwipeOut.this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

}
