package com.attendance.clockme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
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
import java.util.HashMap;
import java.util.Map;

public class AdminThreeRequests extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private DatabaseReference usersReference; // Reference for Users node
    private String loggedInEmail;
    private String loggedInTeam;
    private static final String PREFS_NAME = "MyAppPreferences";
    private TextView welcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin3_request_approve); // Ensure layout file exists

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("ClockInPermission");
        usersReference = FirebaseDatabase.getInstance().getReference("Users"); // Reference for Users node

        // Get the logged-in user's email
        if (mAuth.getCurrentUser() != null) {
            loggedInEmail = mAuth.getCurrentUser().getEmail();
        }

        if (loggedInEmail != null) {
            // Map email to team dynamically
            Map<String, String> emailToTeamMap = new HashMap<>();
            emailToTeamMap.put("azeem@msirindia.com", "Team 1");
            emailToTeamMap.put("kamal@msirindia.com", "Team 2");
            emailToTeamMap.put("karthick@msirindia.com", "Team 3");

            loggedInTeam = emailToTeamMap.getOrDefault(loggedInEmail, null);

            if (loggedInTeam != null) {
                Log.d("AdminThreeRequests", "Logged in as: " + loggedInEmail + " Team: " + loggedInTeam);
                fetchTeamData(loggedInTeam); // Fetch data for the team
            } else {
                Log.e("AdminThreeRequests", "Unknown email: " + loggedInEmail);
            }
        } else {
            Log.e("AdminThreeRequests", "User email is null.");
        }

        LinearLayout homeIcon = findViewById(R.id.homelayout);
        LinearLayout clockIcon = findViewById(R.id.clocklayout);
        LinearLayout logoutIcon = findViewById(R.id.signoutlayout);
        LinearLayout requestIcon = findViewById(R.id.requestlayout);
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(AdminThreeRequests.this, DashboardActivity.class);
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
            Intent intent = new Intent(AdminThreeRequests.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Optionally, show a message indicating the user has logged out
            Toast.makeText(AdminThreeRequests.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        clockIcon.setOnClickListener(v -> {
            Intent clockInOutIntent = new Intent(AdminThreeRequests.this, SwipingTimeActivity.class);
            startActivity(clockInOutIntent);
        });

        // Set listener for requestIcon
        requestIcon.setOnClickListener(v -> {
            Intent salaryDetailsIntent = new Intent(AdminThreeRequests.this, SalaryDetailsActivity.class);
            startActivity(salaryDetailsIntent);
        });

    }


    private void fetchTeamData(String team) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                LinearLayout parentLayout = findViewById(R.id.parentLayout); // Ensure this layout exists
                if (parentLayout == null) {
                    Log.e("AdminThreeRequests", "Parent layout is null.");
                    return;
                }

                parentLayout.removeAllViews(); // Clear existing views

                boolean hasPendingRequests = false; // Track if there are any pending requests

                for (DataSnapshot uidSnapshot : snapshot.getChildren()) { // Iterate over user IDs
                    for (DataSnapshot dateSnapshot : uidSnapshot.getChildren()) { // Iterate over dates
                        Map<String, Object> dataMap = (Map<String, Object>) dateSnapshot.getValue();
                        if (dataMap != null) {
                            String dataTeam = (String) dataMap.get("team");
                            String status = (String) dataMap.get("status");

                            if (team.equals(dataTeam) && "pending".equalsIgnoreCase(status)) {
                                hasPendingRequests = true; // Mark as having pending requests

                                String username = (String) dataMap.get("username");
                                String position = (String) dataMap.get("position");
                                String email = (String) dataMap.get("email");
                                String clockInLocation = (String) dataMap.get("clockInLocation");
                                String clockInTime = (String) dataMap.get("clockInTime");
                                String date = (String) dataMap.get("date");
                                String remark = (String) dataMap.get("remark");
                                String uid = uidSnapshot.getKey(); // Get user ID
                                String dateKey = dateSnapshot.getKey(); // Get date key

                                // Default fallback for null fields
                                username = username != null ? capitalize(username) : "Not available";
                                position = position != null ? capitalize(position) : "Not available";
                                email = email != null ? email : "Not available";
                                clockInLocation = clockInLocation != null ? clockInLocation : "Not available";
                                clockInTime = clockInTime != null ? clockInTime : "Not available";
                                date = date != null ? date : "Not available";
                                remark = remark != null ? remark : "Not available";

                                // Dynamically create the CardView
                                CardView cardView = new CardView(AdminThreeRequests.this);
                                cardView.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                ));
                                cardView.setCardElevation(4);
                                cardView.setRadius(16);
                                cardView.setCardBackgroundColor(getResources().getColor(R.color.white));

                                // Add content to the CardView
                                LinearLayout permissionsLayout = new LinearLayout(AdminThreeRequests.this);
                                permissionsLayout.setOrientation(LinearLayout.VERTICAL);
                                permissionsLayout.setPadding(16, 16, 16, 16);

                                permissionsLayout.addView(createTextView("Username: " + username));
                                permissionsLayout.addView(createTextView("Clock In Time: " + clockInTime));
                                permissionsLayout.addView(createTextView("Date: " + date));
                                permissionsLayout.addView(createTextView("Remark: " + remark));

                                // Create a horizontal LinearLayout for the buttons
                                LinearLayout buttonLayout = new LinearLayout(AdminThreeRequests.this);
                                buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
                                buttonLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                ));
                                buttonLayout.setGravity(Gravity.END); // Align buttons to the end (optional)

                                // Create Approve button
                                Button approveButton = new Button(AdminThreeRequests.this);
                                approveButton.setText("approve");
                                approveButton.setTextColor(getResources().getColor(R.color.white));
                                approveButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3C71B1")));
                                approveButton.setPadding(16, 8, 16, 8); // Adjust padding for the Approve button

                                // Create Reject button
                                Button rejectButton = new Button(AdminThreeRequests.this);
                                rejectButton.setText("reject");
                                rejectButton.setTextColor(getResources().getColor(R.color.white));
                                rejectButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D32F2F")));
                                rejectButton.setPadding(16, 8, 16, 8); // Adjust padding for the Reject button

                                // Add buttons to the horizontal layout
                                buttonLayout.addView(approveButton);
                                buttonLayout.addView(rejectButton);

                                // Add some spacing between the buttons
                                LinearLayout.LayoutParams buttonParams = (LinearLayout.LayoutParams) rejectButton.getLayoutParams();
                                buttonParams.setMargins(16, 0, 0, 0); // Add left margin to the Reject button
                                rejectButton.setLayoutParams(buttonParams);

                                // Add the button layout to the permissions layout
                                permissionsLayout.addView(buttonLayout);

                                // Set OnClickListener for Approve button
                                String finalDate = date;
                                String finalClockInTime = clockInTime;
                                String finalClockInLocation = clockInLocation;
                                approveButton.setOnClickListener(v -> {
                                    // Update ClockInPermission status to approved
                                    updateClockInPermissionStatus(uid, dateKey, "approved");

                                    // Update the Users node with ClockInTime and ClockInLocation
                                    updateUserClockInData(uid, finalDate, finalClockInTime, finalClockInLocation);
                                });

                                // Set OnClickListener for Reject button
                                rejectButton.setOnClickListener(v -> {
                                    // Optionally handle rejection (e.g., update status to rejected or delete entry)
                                    updateClockInPermissionStatus(uid, dateKey, "rejected");
                                });

                                // Add the layout to the CardView
                                cardView.addView(permissionsLayout);
                                parentLayout.addView(cardView);

                                Log.d("AdminThreeRequests", "CardView added for username: " + username);
                            }
                        }
                    }
                }

                // Show "No requests available" message if no pending requests are found
                if (!hasPendingRequests) {
                    TextView noRequestsTextView = new TextView(AdminThreeRequests.this);
                    noRequestsTextView.setText("No requests available.");
                    noRequestsTextView.setTextSize(16);
                    noRequestsTextView.setTextColor(Color.BLACK);
                    noRequestsTextView.setPadding(16, 16, 16, 16);
                    noRequestsTextView.setGravity(Gravity.CENTER);

                    parentLayout.addView(noRequestsTextView);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("AdminThreeRequests", "Database error: " + error.getMessage());
            }
        });
    }



    private void updateClockInPermissionStatus(String uid, String dateKey, String newStatus) {
        // Update the ClockInPermission status
        databaseReference.child(uid).child(dateKey).child("status").setValue(newStatus);
        Toast.makeText(AdminThreeRequests.this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
    }

    private void updateUserClockInData(String uid, String date, String clockInTime, String clockInLocation) {
        // Update the Users node with the clock-in details
        usersReference.child(uid).child("timeRecords").child(date)
                .child("ClockInTime").setValue(clockInTime);
        usersReference.child(uid).child("timeRecords").child(date)
                .child("clockInAddress").setValue(clockInLocation);
        Toast.makeText(AdminThreeRequests.this, "User data updated with ClockIn details.", Toast.LENGTH_SHORT).show();
    }

    private String capitalize(String text) {
        String[] words = text.split("\\s+");
        StringBuilder capitalized = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                capitalized.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase()).append(" ");
            }
        }
        return capitalized.toString().trim();
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(AdminThreeRequests.this);
        textView.setText(text);
        textView.setTextColor(getResources().getColor(R.color.Black));
        textView.setTextSize(16);
        return textView;
    }

}
