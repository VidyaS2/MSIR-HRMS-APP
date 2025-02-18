package com.attendance.clockme;

// LeaveRequestActivity.java

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class LeaveRequestActivity extends AppCompatActivity {

    private EditText dateFromEditText, dateToEditText, reasonEditText;
    private Button requestButton;
    private DatabaseReference leaveRequestReference;
    private DatabaseReference usersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_leave_request); // Change to your layout file name

        // Initialize views
        dateFromEditText = findViewById(R.id.dateField);
        dateToEditText = findViewById(R.id.todateField);
        reasonEditText = findViewById(R.id.reasonField);
        requestButton = findViewById(R.id.requestButton);

        // Initialize Firebase Database Reference
        leaveRequestReference = FirebaseDatabase.getInstance().getReference("LeaveRequest");
        usersReference = FirebaseDatabase.getInstance().getReference("Users"); // Reference to Users table

        // Set up button click listener
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchUserDetailsAndStoreLeaveRequest();
            }
        });
    }

    private void fetchUserDetailsAndStoreLeaveRequest() {
        String fromDate = dateFromEditText.getText().toString().trim();
        String toDate = dateToEditText.getText().toString().trim();
        String reason = reasonEditText.getText().toString().trim();

        if (fromDate.isEmpty() || toDate.isEmpty() || reason.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid(); // User ID

            // Fetch user details from Users table
            usersReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String username = dataSnapshot.child("username").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);
                        String position = dataSnapshot.child("position").getValue(String.class);

                        // Create LeaveRequest object
                        LeaveRequest leaveRequest = new LeaveRequest(fromDate, toDate, reason, username, email, position);

                        // Store Leave Request in the database
                        leaveRequestReference.child(userId).setValue(leaveRequest).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(LeaveRequestActivity.this, "Leave request submitted successfully", Toast.LENGTH_SHORT).show();
                                clearFields();
                            } else {
                                Toast.makeText(LeaveRequestActivity.this, "Failed to submit request", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(LeaveRequestActivity.this, "User details not found", Toast.LENGTH_SHORT).show();
                    }
                }


                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(LeaveRequestActivity.this, "Error fetching user details", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No user signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        dateFromEditText.setText("");
        dateToEditText.setText("");
        reasonEditText.setText("");
    }
}
