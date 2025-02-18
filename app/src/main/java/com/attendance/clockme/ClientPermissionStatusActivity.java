package com.attendance.clockme;

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

public class ClientPermissionStatusActivity extends AppCompatActivity {

    private LinearLayout permissionRequestContainer;
    private TextView welcomeMessage; // To display the username
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference permissionRequestsRef;
    private static final String PREFS_NAME = "MyPrefs";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_permission_status);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        permissionRequestContainer = findViewById(R.id.cardContainer);
        welcomeMessage = findViewById(R.id.welcomeMessage);

        // Firebase reference to PermissionRequests
        permissionRequestsRef = mDatabase.child("ClockInPermission");
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Load user data and permission requests
        loadUserDataAndDisplay();

        LinearLayout homeIcon = findViewById(R.id.homelayout01);
        LinearLayout clockIcon = findViewById(R.id.clocklayout01);
        LinearLayout logoutIcon = findViewById(R.id.signoutlayout01);
        LinearLayout requestIcon = findViewById(R.id.requestlayout01);

        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ClientPermissionStatusActivity.this, DashboardActivity.class);
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
            Intent intent = new Intent(ClientPermissionStatusActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Optionally, show a message indicating the user has logged out
            Toast.makeText(ClientPermissionStatusActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        clockIcon.setOnClickListener(v -> {
            Intent clockInOutIntent = new Intent(ClientPermissionStatusActivity.this, SwipingTimeActivity.class);
            startActivity(clockInOutIntent);
        });

        // Set listener for requestIcon
        requestIcon.setOnClickListener(v -> {
            Intent salaryDetailsIntent = new Intent(ClientPermissionStatusActivity.this, SalaryDetailsActivity.class);
            startActivity(salaryDetailsIntent);
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchPermissionRequests();
    }

    private void fetchPermissionRequests() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Navigate to the correct path in Firebase (ClockInPermission > uid)
            permissionRequestsRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    permissionRequestContainer.removeAllViews();

                    if (!snapshot.exists()) {
                        Toast.makeText(ClientPermissionStatusActivity.this, "No permission in requests found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Iterate through each date node under the UID
                    for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                        String date = dateSnapshot.getKey();

                        // Fetch the required data inside each date node
                        String username = dateSnapshot.child("username").getValue(String.class);
                        String remark = dateSnapshot.child("remark").getValue(String.class);
                        String status = dateSnapshot.child("status").getValue(String.class);

                        if (username != null) {
                            username = capitalizeFirstLetter(username);
                        }

                        // Add the fetched data to the UI, including the date and other details
                        addPermissionRequestCard(username, date, remark, status);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ClientPermissionStatusActivity.this, "Failed to fetch permission requests", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ClientPermissionStatusActivity.this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }
    }


    private void addPermissionRequestCard(String username, String date, String remark, String status) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.permission_status_card, permissionRequestContainer, false);

        TextView usernameTextView = cardView.findViewById(R.id.tvUsername);
        TextView dateTextView = cardView.findViewById(R.id.tvDate);
        TextView remarkTextView = cardView.findViewById(R.id.tvReason);
        TextView statusTextView = cardView.findViewById(R.id.tvStatus);

        usernameTextView.setText(username);
        dateTextView.setText("Date: " + date);
        remarkTextView.setText("Remark: " + (remark != null ? remark : "No remarks"));
        statusTextView.setText("Status: " + (status != null ? status : "Pending"));

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
                                    Toast.makeText(ClientPermissionStatusActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(ClientPermissionStatusActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ClientPermissionStatusActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(ClientPermissionStatusActivity.this, "User email not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ClientPermissionStatusActivity.this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}
