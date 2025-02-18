package com.attendance.clockme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminLateActivity extends AppCompatActivity {

    private LinearLayout cardContainer;
    private DatabaseReference lateRequestsRef;
    private DatabaseReference usersRef;
    private LinearLayout homeLayout, createLayout, signoutLayout, requestLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_late); // Set your layout file

        cardContainer = findViewById(R.id.cardContainer);
        lateRequestsRef = FirebaseDatabase.getInstance().getReference("LateRequests"); // Update this path based on your Firebase structure
         usersRef=FirebaseDatabase.getInstance().getReference("Users");
        fetchLateRequests();



        homeLayout = findViewById(R.id.homeLayout);
        createLayout = findViewById(R.id.createLayout);
        signoutLayout = findViewById(R.id.signoutLayout);
        requestLayout = findViewById(R.id.requestLayout);

        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminLateActivity.this, ActivityAdminDashboard.class);
                startActivity(intent);
            }
        });

        // Create icon click - Navigate to EmpRegActivity
        createLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminLateActivity.this, EmpRegActivity.class);
                startActivity(intent);
            }
        });

        // Request icon click - Navigate to AdminDailyAttendanceActivity
        requestLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminLateActivity.this, AdminDailyAttendanceActivity.class);
                startActivity(intent);
            }
        });

        // SignOut icon click - Logout and navigate to SignInActivity
        signoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out the user from FirebaseAuth
                FirebaseAuth.getInstance().signOut();

                // Clear SharedPreferences or any stored user session data if needed
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear(); // Clear all data, or use specific keys if only certain data needs to be removed
                editor.apply();

                // Show a toast message indicating the user has signed out
                Toast.makeText(getApplicationContext(), "Signed out", Toast.LENGTH_SHORT).show();

                // Redirect the user to the SignInActivity
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                // Clear the backstack so the user can't go back to the previous activity
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                // Finish the current activity to prevent users from navigating back
                finish();
            }
        });

        Button historyButton = findViewById(R.id.historyButton);

        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminLateActivity.this, AdminLateHistoryActivity.class);
                startActivity(intent);
            }
        });

    }

    private void fetchLateRequests() {
        lateRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cardContainer.removeAllViews(); // Clear existing views

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Get status from the snapshot
                    String status = snapshot.child("status").getValue(String.class);

                    // Check if the status is "approved", if so, skip this iteration
                    if ("updated".equalsIgnoreCase(status)) {
                        continue; // Skip the approved requests
                    }

                    // Retrieve other data only if the status is not approved
                    String username = snapshot.child("username").getValue(String.class);
                    String position = snapshot.child("position").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String date = snapshot.child("date").getValue(String.class);
                    String clockInTime = snapshot.child("clockInTime").getValue(String.class);
                    String clockOutTime = snapshot.child("clockOutTime").getValue(String.class);
                    String remark = snapshot.child("remark").getValue(String.class);
                    String requestId = snapshot.getKey(); // Assuming the key is the unique identifier for the request
                    String userId = snapshot.child("userId").getValue(String.class);

                    // Create CardView dynamically
                    createCardView(username, position, email, date, clockInTime, clockOutTime, remark, requestId, userId, status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Toast.makeText(AdminLateActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder capitalizedText = new StringBuilder();
        for (String word : text.split(" ")) {
            if (!word.isEmpty()) {
                capitalizedText.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return capitalizedText.toString().trim();
    }

    private void createCardView(String username, String position, String email, String date, String clockInTime, String clockOutTime, String remark, String requestId, String userId, String status) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View cardView = inflater.inflate(R.layout.card_late_request, null);

        // Initialize views inside the CardView
        TextView textUsername = cardView.findViewById(R.id.text_username);
        TextView textPosition = cardView.findViewById(R.id.text_position);
        TextView textEmail = cardView.findViewById(R.id.text_email);
        TextView textDate = cardView.findViewById(R.id.text_date);
        TextView textClockIn = cardView.findViewById(R.id.text_clockin);
        TextView textClockOut = cardView.findViewById(R.id.text_clockout);
        TextView textRemark = cardView.findViewById(R.id.text_remark);
        TextView textStatus = cardView.findViewById(R.id.statusField);
        Button buttonEdit = cardView.findViewById(R.id.button_edit);

        // Capitalize username and position
        textUsername.setText(capitalizeWords(username));
        textPosition.setText(capitalizeWords(position));

        // Set text for other fields
        textEmail.setText(email);
        textDate.setText("Date: " + date);
        textClockIn.setText("Clock In Time: " + clockInTime);
        textClockOut.setText("Clock Out Time: " + clockOutTime);
        textRemark.setText("Remark: " + remark);
        textStatus.setText("Status: " + status);

        // Set up the Edit button to open the edit form
        buttonEdit.setOnClickListener(v -> showEditForm(requestId, clockInTime, clockOutTime, date, username, userId, status));

        // Add the CardView to the container layout
        if (cardContainer != null) {
            cardContainer.addView(cardView);
        } else {
            Log.e("CardViewError", "cardContainer is not initialized");
        }
    }


    private void showEditForm(String requestId, String currentClockInTime, String currentClockOutTime, String currentDate, String currentUsername, String userId, String status) {
        // Inflate the layout for the dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.clock_inout_edit, null);

        // Build the dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);
        AlertDialog dialog = dialogBuilder.create();

        // Get references to the form fields
        EditText etClockInTime = dialogView.findViewById(R.id.clockInField);
        EditText etClockOutTime = dialogView.findViewById(R.id.clockOutField);
        EditText etClockDateInput = dialogView.findViewById(R.id.clock_date_input);
        TextView textViewUsername = dialogView.findViewById(R.id.clock_username_input);
        Button saveButton = dialogView.findViewById(R.id.button_update);

        // Set the data in the dialog fields
        etClockInTime.setText(currentClockInTime);
        etClockOutTime.setText(currentClockOutTime);
        etClockDateInput.setText(currentDate);
        textViewUsername.setText(currentUsername);

        // Set the Save button functionality
        saveButton.setOnClickListener(v -> {
            // Capture the entered clock in/out times and date
            String updatedClockInTime = etClockInTime.getText().toString().trim();
            String updatedClockOutTime = etClockOutTime.getText().toString().trim();
            String updatedDate = etClockDateInput.getText().toString().trim();

            // Update the LateRequests data
            DatabaseReference lateRequestRef = lateRequestsRef.child(requestId);

            // Only update if the field is not empty
            if (!updatedClockInTime.isEmpty()) {
                lateRequestRef.child("clockInTime").setValue(updatedClockInTime);
            }
            if (!updatedClockOutTime.isEmpty()) {
                lateRequestRef.child("clockOutTime").setValue(updatedClockOutTime);
            }
            if (!updatedDate.isEmpty()) {
                lateRequestRef.child("date").setValue(updatedDate);
            }
            lateRequestRef.child("status").setValue("Updated");
            // Fetch the email from the late request to match with Users node
            // Assume updatedClockInTime, updatedClockOutTime, updatedDate are already defined and hold the new values

// Retrieve the email for the user
            // Assume updatedClockInTime, updatedClockOutTime, updatedDate are already defined and hold the new values
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            usersRef.child("email").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String email = dataSnapshot.getValue(String.class);

                        // Locate the user's time records using the email and updatedDate
                        DatabaseReference timeRecordsRef = FirebaseDatabase.getInstance().getReference("Users");
                        timeRecordsRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                if (userSnapshot.exists()) {
                                    for (DataSnapshot user : userSnapshot.getChildren()) {
                                        // Reference to the user's time records for the specified date
                                        DatabaseReference timeRecordRef = user.child("timeRecords").child(updatedDate).getRef();

                                        // Retrieve the current clock-in and clock-out times
                                        timeRecordRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot recordSnapshot) {
                                                String currentClockInTime = recordSnapshot.child("ClockInTime").getValue(String.class);
                                                String currentClockOutTime = recordSnapshot.child("ClockOutTime").getValue(String.class);
                                                boolean isUpdated = false;

                                                // Check if the new clock-in time is different from the current one
                                                if (!updatedClockInTime.isEmpty() && !updatedClockInTime.equals(currentClockInTime)) {
                                                    timeRecordRef.child("ClockInTime").setValue(updatedClockInTime);
                                                    isUpdated = true;
                                                }

                                                // Check if the new clock-out time is different from the current one
                                                if (!updatedClockOutTime.isEmpty() && !updatedClockOutTime.equals(currentClockOutTime)) {
                                                    timeRecordRef.child("ClockOutTime").setValue(updatedClockOutTime);
                                                    isUpdated = true;
                                                }

                                                // Show a success message if any updates were made
                                                if (isUpdated) {
                                                    Toast.makeText(AdminLateActivity.this, "User clock-in and clock-out times updated successfully.", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(AdminLateActivity.this, "No changes were made to the clock-in/out times.", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Toast.makeText(AdminLateActivity.this, "Failed to retrieve time records.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                } else {
                                    Toast.makeText(AdminLateActivity.this, "No user found with the given email.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(AdminLateActivity.this, "Failed to retrieve user information.", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        Toast.makeText(AdminLateActivity.this, "User email not found.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(AdminLateActivity.this, "Failed to retrieve email.", Toast.LENGTH_SHORT).show();
                }
            });


            // Show a Toast to indicate success for the late request
            Toast.makeText(AdminLateActivity.this, "Late request updated successfully.", Toast.LENGTH_SHORT).show();

            // Close the dialog after saving
            dialog.dismiss();
        });

        // Show the dialog
        dialog.show();
    }

    private void updateUserDataFromLateRequests(String requestId) {
        // Get the reference to the specific LateRequest node
        DatabaseReference lateRequestRef = FirebaseDatabase.getInstance().getReference("LateRequests").child(requestId);

        // Fetch the clockInTime, clockOutTime, and userId from LateRequests
        lateRequestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve clockInTime, clockOutTime, and userId
                    String clockInTime = dataSnapshot.child("clockInTime").getValue(String.class);
                    String clockOutTime = dataSnapshot.child("clockOutTime").getValue(String.class);
                    String date = dataSnapshot.child("date").getValue(String.class);
                    String userId = dataSnapshot.child("userId").getValue(String.class);
                    String status = dataSnapshot.child("status").getValue(String.class);

                    if (userId != null && clockInTime != null && clockOutTime != null && date != null) {
                        // Log to verify data before updating
                        Log.d("AdminLateActivity", "Fetched Data - userId: " + userId + ", clockInTime: " + clockInTime + ", clockOutTime: " + clockOutTime);

                        // Update the corresponding user node in Users
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                        // Create a map for user data updates
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("clockInTime", clockInTime);
                        updates.put("clockOutTime", clockOutTime);
                        updates.put("date", date);
                        updates.put("status", "Updated"); // Adding status key with updated value

                        // Update the user's clockInTime, clockOutTime, date, and status in the Users node
                        userRef.updateChildren(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(AdminLateActivity.this, "User data updated successfully.", Toast.LENGTH_SHORT).show();
                                    Log.d("AdminLateActivity", "User data updated for userId: " + userId);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AdminLateActivity.this, "Failed to update user data.", Toast.LENGTH_SHORT).show();
                                    Log.e("AdminLateActivity", "Error updating user data", e);
                                });
                    } else {
                        Toast.makeText(AdminLateActivity.this, "Incomplete data in LateRequests.", Toast.LENGTH_SHORT).show();
                        Log.e("AdminLateActivity", "Some data missing in LateRequests for requestId: " + requestId);
                    }
                } else {
                    Toast.makeText(AdminLateActivity.this, "LateRequest not found.", Toast.LENGTH_SHORT).show();
                    Log.e("AdminLateActivity", "LateRequest not found for requestId: " + requestId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AdminLateActivity.this, "Failed to retrieve LateRequest.", Toast.LENGTH_SHORT).show();
                Log.e("AdminLateActivity", "Error fetching LateRequest", databaseError.toException());
            }
        });
    }

    public class TimeRecord {
    public String clockInTime;
    public String clockOutTime;

    public TimeRecord(String clockInTime, String clockOutTime) {
        this.clockInTime = clockInTime;
        this.clockOutTime = clockOutTime;
    }
}



}
