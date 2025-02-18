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

public class ClientRelieveStatusActivity extends AppCompatActivity {

    private LinearLayout relieveRequestContainer;
    private TextView welcomeMessage; // To display the username
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference relieveRequestsRef;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "MyPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_relieve_status);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        relieveRequestContainer = findViewById(R.id.cardContainer); // Ensure this exists in your XML
        welcomeMessage = findViewById(R.id.welcomeMessage); // Ensure this exists in your XML

        // Firebase reference to RelieveRequests
        relieveRequestsRef = mDatabase.child("LateInRequests");
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Set up navigation
        setupNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadUserDataAndFetchRequests(); // Load user data and fetch relieve requests when the activity starts
    }

    private void setupNavigation() {
        LinearLayout homeIcon = findViewById(R.id.homelayout01);
        LinearLayout clockIcon = findViewById(R.id.clocklayout01);
        LinearLayout logoutIcon = findViewById(R.id.signoutlayout01);
        LinearLayout requestIcon = findViewById(R.id.requestlayout01);

        homeIcon.setOnClickListener(v -> startActivity(new Intent(ClientRelieveStatusActivity.this, DashboardActivity.class)));

        logoutIcon.setOnClickListener(v -> {
            // Clear SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear().apply();

            // Sign out from Firebase Authentication
            mAuth.signOut();

            // Redirect to the SignInActivity
            Intent intent = new Intent(ClientRelieveStatusActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(ClientRelieveStatusActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        clockIcon.setOnClickListener(v -> startActivity(new Intent(ClientRelieveStatusActivity.this, SwipingTimeActivity.class)));

        requestIcon.setOnClickListener(v -> startActivity(new Intent(ClientRelieveStatusActivity.this, SalaryDetailsActivity.class)));
    }

    private void loadUserDataAndFetchRequests() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null) {
                // Fetch user data from the database
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
                                    Toast.makeText(ClientRelieveStatusActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(ClientRelieveStatusActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                        // After loading user data, fetch relieve requests
                        fetchLateInRequests(currentUser.getEmail());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ClientRelieveStatusActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(ClientRelieveStatusActivity.this, "User email not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ClientRelieveStatusActivity.this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchLateInRequests(String email) {
        // Query the LateInRequests node to match the user's email
        DatabaseReference lateInRequestsRef = FirebaseDatabase.getInstance().getReference("LateInRequests");
        lateInRequestsRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                relieveRequestContainer.removeAllViews(); // Clear previous views if needed

                if (!snapshot.exists()) {
                    Toast.makeText(ClientRelieveStatusActivity.this, "No swipe in requests found", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Iterate through each late-in request for the given email
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Fetch the required data for each request
                    String username = dataSnapshot.child("username").getValue(String.class);
                    String date = dataSnapshot.child("date").getValue(String.class);
                    String remark = dataSnapshot.child("remark").getValue(String.class);
                    String status = dataSnapshot.child("status").getValue(String.class);

                    // Capitalize first letters of username and position if not null
                    if (username != null) {
                        username = capitalizeFirstLetter(username);
                    }

                    // Add the fetched data to the UI, including the username, date, remark, and status
                    addLateInRequestCard(username, date, remark, status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ClientRelieveStatusActivity.this, "Failed to fetch late-in requests", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addLateInRequestCard(String username, String date, String remark, String status) {
        // Inflate a new card layout for displaying late-in request details
        View cardView = LayoutInflater.from(this).inflate(R.layout.relieve_status_card, relieveRequestContainer, false);

        // Set data to the views in the card
        TextView usernameTextView = cardView.findViewById(R.id.tvUsername);
        TextView dateTextView = cardView.findViewById(R.id.tvDate);
        TextView remarkTextView = cardView.findViewById(R.id.tvReason);
        TextView statusTextView = cardView.findViewById(R.id.tvStatus);

        usernameTextView.setText(username);
        dateTextView.setText("Date: " + date);
        remarkTextView.setText("Remark: " + (remark != null ? remark : "No remarks"));
        statusTextView.setText("Status: " + (status != null ? status : "Pending")); // Default to "Pending" if status is null

        // Add the card to the container
        relieveRequestContainer.addView(cardView);
    }

    // Helper method to capitalize the first letter of a string
    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}
