package com.attendance.clockme;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditOutPermissionForm extends AppCompatActivity {

    // Declare UI elements
    private EditText usernameEditText, dateEditText, clockInEditText, userEmailEditText;
    private Button updateButton;
    private ImageView closeIcon;

    // Firebase reference
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editin_permission_form);

        // Initialize Firebase reference
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize the views
        usernameEditText = findViewById(R.id.clock_username_input);
        dateEditText = findViewById(R.id.clock_date_input);
        clockInEditText = findViewById(R.id.clockInField);
        userEmailEditText = findViewById(R.id.clock_useremail);
        updateButton = findViewById(R.id.button_update);
        closeIcon = findViewById(R.id.close_icon);

        // Get the data passed through the intent
        Intent intent = getIntent();
        String uid = intent.getStringExtra("userId"); // Get the uid passed from the previous activity
        String username = intent.getStringExtra("username");
        String date = intent.getStringExtra("date");
        String clockOutTime = intent.getStringExtra("clockOutTime");
        String userEmail = intent.getStringExtra("email");  // Receive email correctly

        // Pre-populate the EditText fields with received data
        usernameEditText.setText(username);
        dateEditText.setText(date);
        clockInEditText.setText(clockOutTime);

        // Set the email in the EditText (if provided)
        if (userEmail != null && !userEmail.isEmpty()) {
            userEmailEditText.setText(userEmail);  // Set the email in the EditText
        } else {
            Toast.makeText(this, "No email provided", Toast.LENGTH_SHORT).show();
        }

        // Set up close button to close the form
        closeIcon.setOnClickListener(v -> finish());  // Close the form when the icon is clicked

        // Update button functionality
        updateButton.setOnClickListener(v -> {
            // Get the updated values from the EditText fields
            String updatedUsername = usernameEditText.getText().toString().trim();
            String updatedDate = dateEditText.getText().toString().trim();
            String updatedClockIn = clockInEditText.getText().toString().trim();
            String updatedUserEmail = userEmailEditText.getText().toString().trim();

            // Validate the inputs
            if (updatedUsername.isEmpty() || updatedDate.isEmpty() || updatedClockIn.isEmpty() || updatedUserEmail.isEmpty()) {
                Toast.makeText(EditOutPermissionForm.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Update data in Firebase based on the provided uid
                updateClockInTimeForUser(uid, updatedDate, updatedClockIn);
            }
        });


    }

    private void updateClockInTimeForUser(String uid, String date, String clockInTime) {
        // Retrieve the user's data using the uid
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        // Check if the user exists by querying the user's node
        userRef.child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String storedEmail = dataSnapshot.getValue(String.class);
                    Log.d("EditOutPermissionForm", "Stored Email: " + storedEmail);

                    // Locate the user's time records using the email and date
                    userRef.child("timeRecords").child(date).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot timeRecordSnapshot) {
                            if (timeRecordSnapshot.exists()) {
                                // Record exists for the specified date, update the ClockInTime
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("ClockOutTime", clockInTime); // Update the ClockInTime field
                                updates.put("ClockOutTimestamp", System.currentTimeMillis()); // Update timestamp to current time

                                // Update the record in Firebase
                                userRef.child("timeRecords").child(date).updateChildren(updates)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                // Successfully updated the clock-in time
                                                Toast.makeText(EditOutPermissionForm.this, "Clock-out time updated successfully", Toast.LENGTH_SHORT).show();

                                                // Now update the status of the corresponding request in the LateInRequests node
                                                saveUserIdToLateInRequests(uid); // Update the status to 'approved'

                                                finish(); // Close the form
                                            } else {
                                                // Error in updating
                                                Toast.makeText(EditOutPermissionForm.this, "Error updating clock-out time", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // No record found for this date in timeRecords
                                Toast.makeText(EditOutPermissionForm.this, "No record found for this date", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle error
                            Toast.makeText(EditOutPermissionForm.this, "Error accessing database", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // No user record found with this uid
                    Toast.makeText(EditOutPermissionForm.this, "No user found with this UID", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Toast.makeText(EditOutPermissionForm.this, "Error accessing database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserIdToLateInRequests(String userId) {
        // Reference to the "LateInRequests" node
        DatabaseReference lateInRequestsRef = FirebaseDatabase.getInstance().getReference("LateOutRequests");

        // Query to find the correct entry where userId matches
        lateInRequestsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Loop through the matching entries
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Check if the userId matches
                        String uid = snapshot.getKey(); // Get the UID of the matched entry
                        if (snapshot.child("userId").getValue(String.class).equals(userId)) {
                            // Once matched, update the status field to "approved"
                            snapshot.child("status").getRef().setValue("approved")
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Log.d("EditInSwipeMissing", "Status updated to 'approved' for userId: " + userId);
                                        } else {
                                            Log.e("EditInSwipeMissing", "Error updating status to 'approved': " + task.getException());
                                        }
                                    });
                        }
                    }
                } else {
                    Log.e("EditInSwipeMissing", "No record found for the userId: " + userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("EditInSwipeMissing", "Error querying the database: " + databaseError.getMessage());
            }
        });
    }

}
