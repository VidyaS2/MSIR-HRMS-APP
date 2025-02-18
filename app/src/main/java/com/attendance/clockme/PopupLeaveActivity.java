package com.attendance.clockme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class PopupLeaveActivity extends AppCompatActivity {

    private EditText dateField, toDateField, reasonField;
    private Button requestButton;
    private ImageView closeIcon;
    private Spinner teamSpinner;  // Spinner for team selection
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private String username, position, email, uid;
    private TextView welcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_leave_request);

        // Initialize Firebase Auth and get current user
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        // Initialize views
        dateField = findViewById(R.id.dateField);
        toDateField = findViewById(R.id.todateField);
        reasonField = findViewById(R.id.reasonField);
        requestButton = findViewById(R.id.requestButton);
        closeIcon = findViewById(R.id.closeIcon);
        welcomeTextView = findViewById(R.id.welcomeMessage);
        teamSpinner = findViewById(R.id.spinner_team);  // Initialize Spinner

        // Initialize the spinner with a hardcoded list of teams
        String[] teams = new String[] {"Team 1", "Team 2", "Team 3"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, teams);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamSpinner.setAdapter(adapter); // Set adapter for the spinner

        // Fetch user details from Firebase
        if (currentUser != null) {
            uid = currentUser.getUid();
            fetchUserDetails(); // Fetch and display the username
        }

        // Close icon click listener to navigate to SalaryDetailsActivity
        closeIcon.setOnClickListener(v -> {
            Intent salaryDetailsIntent = new Intent(PopupLeaveActivity.this, SalaryDetailsActivity.class);
            startActivity(salaryDetailsIntent);
        });

        // Handle Request button click
        requestButton.setOnClickListener(v -> submitLeaveRequest());
    }

    // Method to fetch user details from Firebase
    private void fetchUserDetails() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    username = snapshot.child("username").getValue(String.class);
                    position = snapshot.child("position").getValue(String.class);
                    email = snapshot.child("email").getValue(String.class);

                    // Update the welcome message with the username
                    if (username != null) {
                        welcomeTextView.setText("Welcome, " + username + "!"); // Display username
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PopupLeaveActivity.this, "Failed to fetch user details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to submit leave request
    private void submitLeaveRequest() {
        String fromDate = dateField.getText().toString();
        String toDate = toDateField.getText().toString();
        String reason = reasonField.getText().toString();
        String status = "pending";

        // Get the selected team from the spinner
        String selectedTeam = teamSpinner.getSelectedItem().toString(); // Retrieve the selected team

        // Validate fields
        if (fromDate.isEmpty() || toDate.isEmpty() || reason.isEmpty() || selectedTeam.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate the format of From Date and To Date
        if (!isValidDate(fromDate)) {
            dateField.setError("Invalid date format. Please use dd-MM-yyyy.");
            return;
        }
        if (!isValidDate(toDate)) {
            toDateField.setError("Invalid date format. Please use dd-MM-yyyy.");
            return;
        }

        // Create a unique ID for the leave request
        String requestId = FirebaseDatabase.getInstance().getReference("LeaveRequests").push().getKey();

        // Prepare leave request data
        Map<String, Object> leaveRequest = new HashMap<>();
        leaveRequest.put("username", username);
        leaveRequest.put("position", position);
        leaveRequest.put("email", email);
        leaveRequest.put("fromDate", fromDate);
        leaveRequest.put("toDate", toDate);
        leaveRequest.put("reason", reason);
        leaveRequest.put("team", selectedTeam); // Add selected team to the request
        leaveRequest.put("uid", uid);
        leaveRequest.put("status", status);

        // Save leave request to Firebase
        FirebaseDatabase.getInstance().getReference("LeaveRequests")
                .child(requestId).setValue(leaveRequest)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(PopupLeaveActivity.this, "Leave Request Submitted", Toast.LENGTH_SHORT).show();
                        clearFields();
                        Intent intent = new Intent(PopupLeaveActivity.this, SalaryDetailsActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(PopupLeaveActivity.this, "Submission failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Helper method to validate date format
    private boolean isValidDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateFormat.setLenient(false); // Ensure strict parsing
        try {
            dateFormat.parse(date); // Try parsing the date
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Helper method to clear the fields after submission
    private void clearFields() {
        dateField.setText("");
        toDateField.setText("");
        reasonField.setText("");
    }
}
