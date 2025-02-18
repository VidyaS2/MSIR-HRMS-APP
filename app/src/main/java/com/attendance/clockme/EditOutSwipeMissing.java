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
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditOutSwipeMissing extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference clockOutPermissionRef;
    private DatabaseReference usersRef;
    private String loggedInEmail;
    private String loggedInTeam;
    private static final String PREFS_NAME = "MyAppPreferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editin_sm); // Ensure layout file exists

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        clockOutPermissionRef = FirebaseDatabase.getInstance().getReference("LateOutRequests");
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
            Intent intent = new Intent(EditOutSwipeMissing.this, DashboardActivity.class);
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
            Intent intent = new Intent(EditOutSwipeMissing.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Optionally, show a message indicating the user has logged out
            Toast.makeText(EditOutSwipeMissing.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        clockIcon.setOnClickListener(v -> {
            Intent clockInOutIntent = new Intent(EditOutSwipeMissing.this, SwipingTimeActivity.class);
            startActivity(clockInOutIntent);
        });

        // Set listener for requestIcon
        requestIcon.setOnClickListener(v -> {
            Intent salaryDetailsIntent = new Intent(EditOutSwipeMissing.this, SalaryDetailsActivity.class);
            startActivity(salaryDetailsIntent);
        });
    }

    private void fetchTeamData(String team) {
        // Query the LateInRequests to fetch only the requests with the selectedTeam as the logged-in team and the status as "pending"
        clockOutPermissionRef.orderByChild("selectedTeam").equalTo(team).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                LinearLayout parentLayout = findViewById(R.id.parentLayout); // Ensure this layout exists
                if (parentLayout == null) {
                    Log.e("EditOutSwipeMissing", "Parent layout is null.");
                    return;
                }

                parentLayout.removeAllViews(); // Clear existing views
                boolean hasPendingRequests = false; // Track if there are any pending requests

                // Iterate over the requests
                for (DataSnapshot uidSnapshot : snapshot.getChildren()) {
                    Map<String, Object> dataMap = (Map<String, Object>) uidSnapshot.getValue();
                    if (dataMap != null) {
                        String status = (String) dataMap.get("status");

                        // Check if the status is "pending"
                        if ("pending".equalsIgnoreCase(status)) {
                            hasPendingRequests = true; // Mark as found pending requests

                            // Fetch the necessary details from the request
                            String username = (String) dataMap.get("username");
                            String position = (String) dataMap.get("position");
                            String email = (String) dataMap.get("email");
                            String clockOutTime = (String) dataMap.get("clockOutTime");
                            String team = (String) dataMap.get("selectedTeam");
                            String date = (String) dataMap.get("date");
                            String remark = (String) dataMap.get("remark");
                            String uid = uidSnapshot.getKey(); // Request ID
                            String userId = (String) dataMap.get("userId"); // Fetch the userId

                            // Default fallback for null fields
                            username = username != null ? capitalize(username) : "Not available";
                            position = position != null ? capitalize(position) : "Not available";
                            email = email != null ? email : "Not available";
                            clockOutTime = clockOutTime != null ? clockOutTime : "Not available";
                            date = date != null ? date : "Not available";
                            remark = remark != null ? remark : "Not available";
                            userId = userId != null ? userId : "Not available"; // Default fallback for userId

                            // Dynamically create the CardView
                            CardView cardView = new CardView(EditOutSwipeMissing.this);
                            cardView.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            ));
                            cardView.setCardElevation(4);
                            cardView.setRadius(0); // Slightly reduce the radius
                            cardView.setCardBackgroundColor(ContextCompat.getColor(EditOutSwipeMissing.this, R.color.white));
                            cardView.setPadding(0, 0, 0, 0); // Reduce padding to minimize height

                            // Add content to the CardView
                            LinearLayout permissionsLayout = new LinearLayout(EditOutSwipeMissing.this);
                            permissionsLayout.setOrientation(LinearLayout.VERTICAL);
                            permissionsLayout.setPadding(0, 0, 0, 0); // Reduce padding for inner layout

                            permissionsLayout.addView(createTextView("Username: " + username));
                            permissionsLayout.addView(createTextView("Clock Out Time: " + clockOutTime));
                            permissionsLayout.addView(createTextView("Date: " + date));
                            permissionsLayout.addView(createTextView("Remark: " + remark));

                            // Create a horizontal layout for the buttons
                            LinearLayout buttonLayout = new LinearLayout(EditOutSwipeMissing.this);
                            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
                            buttonLayout.setGravity(Gravity.END); // Align buttons to the right
                            buttonLayout.setPadding(0, 0, 0, 0); // Add some top margin

                            // Create Edit and Reject buttons
                            Button editButton = new Button(EditOutSwipeMissing.this);
                            editButton.setText("approve");
                            editButton.setTextColor(ContextCompat.getColor(EditOutSwipeMissing.this, R.color.white));
                            editButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3C71B1")));
                            editButton.setPadding(0, 0, 0, 0); // Add some padding for better appearance
                            String finalUserId = userId;
                            editButton.setOnClickListener(v -> {
                                // Ensure the user is logged in
                                if (loggedInEmail != null) {
                                    String userid = finalUserId; // userId from the snapshot
                                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                                    // Fetch the date from the LateInRequests node
                                    DatabaseReference lateInRequestsRef = FirebaseDatabase.getInstance().getReference("LateOutRequests");
                                    lateInRequestsRef.child(uid).get().addOnCompleteListener(task -> {
                                        if (task.isSuccessful() && task.getResult() != null) {
                                            // Get the date from the LateInRequests (assumed to be in the format yyyy-MM-dd)
                                            String requestDate = task.getResult().child("date").getValue(String.class);

                                            if (requestDate != null) {
                                                // Now, navigate to the correct timeRecords node for the user
                                                DatabaseReference timeRecordsRef = userRef.child("timeRecords").child(requestDate);

                                                // Update the ClockInTime for the specific date
                                                timeRecordsRef.child("ClockOutTime").setValue("17:50").addOnCompleteListener(updateTask -> {
                                                    if (updateTask.isSuccessful()) {
                                                        // Successfully updated the ClockInTime
                                                        Toast.makeText(EditOutSwipeMissing.this, "Clock-In Time updated successfully!", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        // Failed to update ClockInTime
                                                        Toast.makeText(EditOutSwipeMissing.this, "Failed to update Clock-In Time.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                                // Update the status to "approved" in the LateInRequests node
                                                lateInRequestsRef.child(uid).child("status").setValue("approved").addOnCompleteListener(requestTask -> {
                                                    if (requestTask.isSuccessful()) {
                                                        // Successfully updated the status to approved
                                                        Toast.makeText(EditOutSwipeMissing.this, "Request approved!", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        // Failed to update the status
                                                        Toast.makeText(EditOutSwipeMissing.this, "Failed to approve the request.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
                                                // Date not found in LateInRequests
                                                Toast.makeText(EditOutSwipeMissing.this, "Request date not found.", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            // Failed to fetch LateInRequests data
                                            Toast.makeText(EditOutSwipeMissing.this, "Failed to fetch LateInRequests data.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(EditOutSwipeMissing.this, "User not logged in.", Toast.LENGTH_SHORT).show();
                                }
                            });

                            Button rejectButton = new Button(EditOutSwipeMissing.this);
                            rejectButton.setText("reject");
                            rejectButton.setTextColor(ContextCompat.getColor(EditOutSwipeMissing.this, R.color.white));
                            rejectButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D32F2F")));
                            rejectButton.setPadding(0, 0, 0, 0); // Add some padding for better appearance
                            rejectButton.setOnClickListener(v -> {
                                // Update the status to "rejected" in the LateInRequests node
                                DatabaseReference lateInRequestsRef = FirebaseDatabase.getInstance().getReference("LateOutRequests");
                                lateInRequestsRef.child(uid).child("status").setValue("rejected").addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Successfully updated the status to rejected
                                        Toast.makeText(EditOutSwipeMissing.this, "Request rejected!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Failed to update the status
                                        Toast.makeText(EditOutSwipeMissing.this, "Failed to reject the request.", Toast.LENGTH_SHORT).show();
                                        Log.e("RejectButton", "Failed to update status to rejected: " + task.getException());
                                    }
                                }).addOnFailureListener(e -> {
                                    // Handle any exceptions that occur
                                    Toast.makeText(EditOutSwipeMissing.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("RejectButton", "Error while rejecting request: " + e.getMessage());
                                });
                            });
                            // Add buttons to the horizontal layout
                            buttonLayout.addView(editButton);
                            buttonLayout.addView(rejectButton);

                            // Add the button layout to the permissionsLayout
                            permissionsLayout.addView(buttonLayout);

                            // Add the layout to the CardView
                            cardView.addView(permissionsLayout);
                            parentLayout.addView(cardView);
                        }
                    }
                }

                // If no requests were found, show a message
                if (!hasPendingRequests) {
                    TextView noRequestsText = new TextView(EditOutSwipeMissing.this);
                    noRequestsText.setText("No requests available.");
                    noRequestsText.setTextSize(18);
                    noRequestsText.setGravity(Gravity.CENTER);
                    parentLayout.addView(noRequestsText);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("EditInSwipeMissing", "Error fetching data: " + error.getMessage());
            }
        });
    }

    private void updateClockOutPermissionStatus(String uid, String newStatus) {
        // Update the request's status to the new status (e.g., rejected)
        clockOutPermissionRef.child(uid).child("status").setValue(newStatus);
    }

    private void saveUserIdToLateInRequests(String userId) {
        // Reference to the "LateInRequests" node
        DatabaseReference lateInRequestsRef = FirebaseDatabase.getInstance().getReference("LateInRequests");

    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(EditOutSwipeMissing.this);
        textView.setTextColor(ContextCompat.getColor(EditOutSwipeMissing.this, R.color.Black));
        textView.setText(text);
        textView.setTextSize(16);
        textView.setPadding(0, 8, 0, 8);
        return textView;
    }

    private String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}
