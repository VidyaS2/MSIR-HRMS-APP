package com.attendance.clockme;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class PopupPermissionActivity extends AppCompatActivity {

    private EditText dateField, reasonField, hoursField, fromTimeField, toTimeField;
    private Spinner teamSpinner; // Declare the spinner for teams
    private Button requestButton;
    private ImageView closeIcon;
    private TextView welcomeTextView;
    private FirebaseUser currentUser;

    // Variables to hold user data
    private String username;
    private String position;
    private String email;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_request); // Set your layout
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize your views
        dateField = findViewById(R.id.dateField);
        reasonField = findViewById(R.id.reasonField);
        hoursField = findViewById(R.id.hoursField);
        fromTimeField = findViewById(R.id.fromTimeField);
        toTimeField = findViewById(R.id.toTimeField);
        teamSpinner = findViewById(R.id.spinner_team); // Initialize the team spinner
        requestButton = findViewById(R.id.requestButton);
        closeIcon = findViewById(R.id.closeIcon);
        welcomeTextView = findViewById(R.id.welcomeMessage);

        // Set up the spinner with team options
        String[] teams = new String[] {"Select Team", "Team 1", "Team 2", "Team 3"}; // Static list of teams
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, teams);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamSpinner.setAdapter(adapter);

        // Set up the request button click listener
        requestButton.setOnClickListener(v -> submitPermissionRequest());

        // Set up the close icon click listener
        closeIcon.setOnClickListener(v -> navigateToSalaryDetails());

        // Get the current user UID
        uid = currentUser.getUid();

        // Fetch user details
        fetchUserDetails();
    }

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
                Toast.makeText(PopupPermissionActivity.this, "Failed to fetch user details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitPermissionRequest() {
        // Validate fields (existing validation can be added here)

        // Get the selected team from the spinner
        String selectedTeam = teamSpinner.getSelectedItem().toString();

        // Ensure user details are fetched
        if (username != null && position != null && email != null) {
            // Insert the permission request into the database with user details
            insertPermissionRequest(uid, username, position, email, selectedTeam);
        } else {
            Toast.makeText(this, "Failed to fetch user details. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void insertPermissionRequest(String uid, String username, String position, String email, String selectedTeam) {
        // Get the input fields' values
        String date = dateField.getText().toString();
        String reason = reasonField.getText().toString();
        String hours = hoursField.getText().toString();
        String fromTime = fromTimeField.getText().toString();
        String toTime = toTimeField.getText().toString();
        String status = "pending"; // Default status

        // Create a unique key for the request
        DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference().child("permissionRequests");
        String requestId = requestsRef.push().getKey();

        // Create a map for the request data
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("uid", uid);
        requestData.put("username", username);
        requestData.put("position", position);
        requestData.put("email", email);
        requestData.put("date", date);
        requestData.put("reason", reason);
        requestData.put("hours", hours);
        requestData.put("fromTime", fromTime);
        requestData.put("toTime", toTime);
        requestData.put("status", status); // Add status to request data
        requestData.put("team", selectedTeam); // Add the selected team to request data

        // Insert data into permissionRequests node
        requestsRef.child(requestId).setValue(requestData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(PopupPermissionActivity.this, "Permission request submitted successfully!", Toast.LENGTH_SHORT).show();
                        navigateToSalaryDetails(); // Navigate to SalaryDetailsActivity after submission
                    } else {
                        Toast.makeText(PopupPermissionActivity.this, "Failed to submit request: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to navigate to SalaryDetailsActivity
    private void navigateToSalaryDetails() {
        Intent intent = new Intent(PopupPermissionActivity.this, SalaryDetailsActivity.class);
        startActivity(intent);
        finish(); // Optionally finish the current activity
    }
}
