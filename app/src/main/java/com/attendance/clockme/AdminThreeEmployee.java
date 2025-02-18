package com.attendance.clockme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminThreeEmployee extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private LinearLayout containerLayout;
    private FirebaseDatabase mDatabase;
    private TextView welcomeTextView;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AttendancePrefs";

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin3_employee);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        welcomeTextView = findViewById(R.id.welcomeMessage);
        containerLayout = findViewById(R.id.containerLayout); // LinearLayout to hold the cards

        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get the current logged-in user
        if (currentUser != null) {
            String loggedInEmail = currentUser.getEmail(); // Get the logged-in email
            String userId = currentUser.getUid(); // Get the user's UID

            // Fetch the current user's username and display it in the welcomeTextView
            fetchUsername(userId);

            // Fetch and display team data
            fetchAndDisplayTeamData(loggedInEmail);

            LinearLayout homeIcon = findViewById(R.id.homelayout);
            LinearLayout clockIcon = findViewById(R.id.clocklayout);
            LinearLayout logoutIcon = findViewById(R.id.signoutlayout);
            LinearLayout requestIcon = findViewById(R.id.requestlayout);
            sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

            homeIcon.setOnClickListener(v -> {
                Intent intent = new Intent(AdminThreeEmployee.this, AdminThreeDashboard.class);
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
                Intent intent = new Intent(AdminThreeEmployee.this, SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                // Optionally, show a message indicating the user has logged out
                Toast.makeText(AdminThreeEmployee.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            });

            clockIcon.setOnClickListener(v -> {
                Intent clockInOutIntent = new Intent(AdminThreeEmployee.this, SwipingTimeActivity.class);
                startActivity(clockInOutIntent);
            });

            // Set listener for requestIcon
            requestIcon.setOnClickListener(v -> {
                Intent salaryDetailsIntent = new Intent(AdminThreeEmployee.this, SalaryDetailsActivity.class);
                startActivity(salaryDetailsIntent);
            });
        }
    }

    private void fetchAndDisplayTeamData(String email) {
        String team = getTeamFromEmail(email); // Get the team from the logged-in email
        if (team != null) {
            // Fetch team data from Firebase using the correct team node (Team 1, Team 2, or Team 3)
            DatabaseReference ref = mDatabase.getReference(team); // Accessing the correct Team node

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Clear previous data
                    containerLayout.removeAllViews();

                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String username = userSnapshot.child("username").getValue(String.class);
                        String position = userSnapshot.child("position").getValue(String.class);
                        String userId = userSnapshot.getKey();  // Get the user ID (key) of the employee

                        if (username != null && position != null && userId != null) {
                            // Capitalize the first letter of username and position
                            username = capitalizeFirstLetter(username);
                            position = capitalizeFirstLetter(position);

                            // Create CardView dynamically
                            CardView cardView = (CardView) getLayoutInflater().inflate(R.layout.cardview_employee_admin3, null);
                            TextView usernameText = cardView.findViewById(R.id.usernameText); // Correct ID
                            TextView positionText = cardView.findViewById(R.id.positionText); // Correct ID
                            Button viewButton = cardView.findViewById(R.id.viewButton); // Correct ID

                            usernameText.setText(username);
                            positionText.setText(position);

                            // Store the userId in the view's tag
                            cardView.setTag(userId); // Store the userId in the CardView tag

                            // Handle "View" button click to navigate to AdminThreeMonthCalendar
                            viewButton.setOnClickListener(v -> {
                                // Retrieve the userId from the CardView tag
                                String selectedUserId = (String) cardView.getTag();

                                // Pass the userId to AdminThreeMonthCalendar
                                Intent intent = new Intent(AdminThreeEmployee.this, AdminThreeMonthCalendar.class);
                                intent.putExtra("USER_ID", selectedUserId); // Pass userId to the next activity
                                startActivity(intent);
                            });

                            // Add the CardView to the container layout with some margin
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.setMargins(30, 0, 30, 16); // 16px margin at the bottom of each card
                            cardView.setLayoutParams(params);
                            containerLayout.addView(cardView);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle cancellation (optional)
                }
            });
        }
    }

    // Helper method to map email to team
    private String getTeamFromEmail(String email) {
        switch (email) {
            case "azeem@msirindia.com":
                return "Team 1";  // Team 1 is the nodwsae name
            case "kamal@msirindia.com":
                return "Team 2";  // Team 2 is the node name
            case "karthick@msirindia.com":
                return "Team 3";  // Team 3 is the node name
            default:
                return null;
        }
    }

    // Helper method to capitalize the first letter of a string
    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    // Fetch the username from Firebase and update the welcomeTextView
    private void fetchUsername(String userId) {
        DatabaseReference userRef = mDatabase.getReference("users").child(userId);

        userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.getValue(String.class);
                if (username != null) {
                    // Capitalize and display the username in the welcome text view
                    username = capitalizeFirstLetter(username);
                    welcomeTextView.setText("Welcome, " + username + "!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AdminThreeEmployee.this, "Failed to fetch username", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
