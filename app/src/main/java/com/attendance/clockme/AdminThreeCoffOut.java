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

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AdminThreeCoffOut extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference clockOutPermissionRef;
    private DatabaseReference usersRef;
    private String loggedInEmail;
    private String loggedInTeam;
    private static final String PREFS_NAME = "MyAppPreferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin3_request_approve); // Ensure layout file exists

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        clockOutPermissionRef = FirebaseDatabase.getInstance().getReference("CoffHdOutPermission");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

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
            Intent intent = new Intent(AdminThreeCoffOut.this, DashboardActivity.class);
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
            Intent intent = new Intent(AdminThreeCoffOut.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Optionally, show a message indicating the user has logged out
            Toast.makeText(AdminThreeCoffOut.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        clockIcon.setOnClickListener(v -> {
            Intent clockInOutIntent = new Intent(AdminThreeCoffOut.this, SwipingTimeActivity.class);
            startActivity(clockInOutIntent);
        });

        // Set listener for requestIcon
        requestIcon.setOnClickListener(v -> {
            Intent salaryDetailsIntent = new Intent(AdminThreeCoffOut.this, SalaryDetailsActivity.class);
            startActivity(salaryDetailsIntent);
        });
    }

    private void fetchTeamData(String team) {
        clockOutPermissionRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                                hasPendingRequests = true; // Mark as found pending requests

                                String username = (String) dataMap.get("username");
                                String position = (String) dataMap.get("position");
                                String email = (String) dataMap.get("email");
                                String clockOutLocation = (String) dataMap.get("clockOutLocation");
                                String clockOutTime = (String) dataMap.get("clockOutTime");
                                String date = (String) dataMap.get("date");
                                String remark = (String) dataMap.get("remark");
                                String uid = uidSnapshot.getKey(); // Get user ID
                                String dateKey = dateSnapshot.getKey(); // Get date key

                                // Default fallback for null fields
                                username = username != null ? capitalize(username) : "Not available";
                                position = position != null ? capitalize(position) : "Not available";
                                email = email != null ? email : "Not available";
                                clockOutLocation = clockOutLocation != null ? clockOutLocation : "Not available";
                                clockOutTime = clockOutTime != null ? clockOutTime : "Not available";
                                date = date != null ? date : "Not available";
                                remark = remark != null ? remark : "Not available";

                                // Dynamically create the CardView
                                CardView cardView = new CardView(AdminThreeCoffOut.this);
                                cardView.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                ));
                                cardView.setCardElevation(4);
                                cardView.setRadius(16);
                                cardView.setCardBackgroundColor(getResources().getColor(R.color.white));

                                // Add content to the CardView
                                LinearLayout permissionsLayout = new LinearLayout(AdminThreeCoffOut.this);
                                permissionsLayout.setOrientation(LinearLayout.VERTICAL);
                                permissionsLayout.setPadding(16, 16, 16, 16);

                                permissionsLayout.addView(createTextView("Username: " + username));
                                permissionsLayout.addView(createTextView("Clock Out Time: " + clockOutTime));
                                permissionsLayout.addView(createTextView("Date: " + date));
                                permissionsLayout.addView(createTextView("Remark: " + remark));

                                // Create a horizontal layout for the buttons
                                LinearLayout buttonLayout = new LinearLayout(AdminThreeCoffOut.this);
                                buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
                                buttonLayout.setGravity(Gravity.END); // Align buttons to the end
                                buttonLayout.setPadding(0, 16, 0, 0);

                                // Create Approve and Reject buttons
                                Button approveButton = new Button(AdminThreeCoffOut.this);
                                approveButton.setText("approve");
                                approveButton.setTextColor(getResources().getColor(R.color.white));
                                approveButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3C71B1")));
                                approveButton.setPadding(16, 8, 16, 8); // Adjust padding for the Approve button

                                // Create Reject button
                                Button rejectButton = new Button(AdminThreeCoffOut.this);
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
                                String finalDate = date;
                                String finalClockInTime = clockOutTime;
                                String finalClockInLocation = clockOutLocation;
                                approveButton.setOnClickListener(v -> {
                                    // Update ClockInPermission status to approved
                                    updateClockOutPermissionStatus(uid, dateKey, "approved");

                                    // Update the Users node with ClockInTime and ClockInLocation
                                    updateUserClockOutData(uid, finalDate, finalClockInTime, finalClockInLocation);
                                });

                                // Set OnClickListener for Reject button
                                rejectButton.setOnClickListener(v -> {
                                    // Optionally handle rejection (e.g., update status to rejected or delete entry)
                                    updateClockOutPermissionStatus(uid, dateKey, "rejected");
                                });

                                // Add the layout to the CardView
                                cardView.addView(permissionsLayout);
                                parentLayout.addView(cardView);

                                Log.d("AdminThreeRequests", "CardView added for username: " + username);
                            }
                        }
                    }
                }

                if (!hasPendingRequests) {
                    // Add a "No requests available" message
                    TextView noRequestsTextView = new TextView(AdminThreeCoffOut.this);
                    noRequestsTextView.setText("No requests available.");
                    noRequestsTextView.setTextSize(16);
                    noRequestsTextView.setTextColor(getResources().getColor(R.color.Black));
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



    private void updateClockOutPermissionStatus(String uid, String dateKey, String newStatus) {
        // Update the ClockOutPermission status
        clockOutPermissionRef.child(uid).child(dateKey).child("status").setValue(newStatus);
        Toast.makeText(AdminThreeCoffOut.this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
    }

    private void updateUserClockOutData(String uid, String date, String clockOutTime, String clockOutLocation) {
        // Update the Users node with the clock-out details
        usersRef.child(uid).child("timeRecords").child(date)
                .child("ClockOutTime").setValue(clockOutTime);
        usersRef.child(uid).child("timeRecords").child(date)
                .child("ClockOutLocation").setValue(clockOutLocation);
        Toast.makeText(AdminThreeCoffOut.this, "User data updated with ClockOut details.", Toast.LENGTH_SHORT).show();
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return "Not available";
        }
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
        TextView textView = new TextView(AdminThreeCoffOut.this);
        textView.setText(text);
        textView.setTextSize(14);
        textView.setTextColor(getResources().getColor(R.color.Black));
        return textView;
    }

}
