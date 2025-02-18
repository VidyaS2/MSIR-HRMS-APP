package com.attendance.clockme;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.UUID;

public class PopupRelieveActivity extends AppCompatActivity {

    private EditText enterDateField, reasonField;
    private Button requestButton;
    private ImageView closeIcon;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private DatabaseReference requestsRef;
    private FirebaseUser currentUser;
    private TextView welcomeTextView;
    private String uid, username, email, position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_relieve_request); // Replace with your layout file name
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        enterDateField = findViewById(R.id.enterdateField);
        reasonField = findViewById(R.id.reasonField);
        requestButton = findViewById(R.id.requestButton);
        closeIcon = findViewById(R.id.closeIcon);
        welcomeTextView = findViewById(R.id.welcomeMessage); // Add missing semicolon here

        // Firebase references
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        requestsRef = FirebaseDatabase.getInstance().getReference("RelieveRequest");

        // Fetch user data
        fetchUserDetails(); // Call the method to fetch user details

        // Handle close icon click
        closeIcon.setOnClickListener(view -> {
            navigateToSalaryDetails();
        });

        // Handle request button click
        requestButton.setOnClickListener(view -> {
            submitRequest();
        });
    }

    private void fetchUserDetails() {
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = usersRef.child(currentUserUid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    username = snapshot.child("username").getValue(String.class);
                    position = snapshot.child("position").getValue(String.class);
                    email = snapshot.child("email").getValue(String.class);
                    uid = snapshot.child("uid").getValue(String.class);

                    // Update the welcome message with the username
                    if (username != null) {
                        welcomeTextView.setText("Welcome, " + username + "!"); // Display username
                    }
                } else {
                    Toast.makeText(PopupRelieveActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PopupRelieveActivity.this, "Error fetching user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitRequest() {
        String date = enterDateField.getText().toString().trim();
        String reason = reasonField.getText().toString().trim();
        String status = "pending";
        if (date.isEmpty() || reason.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a unique request ID
        String requestId = UUID.randomUUID().toString();
        HashMap<String, Object> requestData = new HashMap<>();
        requestData.put("uid", uid);
        requestData.put("username", username);
        requestData.put("email", email);
        requestData.put("position", position);
        requestData.put("date", date);
        requestData.put("reason", reason);
        requestData.put("status", status);
        requestsRef.child(requestId).setValue(requestData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(PopupRelieveActivity.this, "Request submitted successfully!", Toast.LENGTH_SHORT).show();
                        navigateToSalaryDetails(); // Navigate after successful submission
                    } else {
                        Toast.makeText(PopupRelieveActivity.this, "Failed to submit request: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToSalaryDetails() {
        Intent intent = new Intent(PopupRelieveActivity.this, SalaryDetailsActivity.class);
        startActivity(intent);
        finish(); // Optional: Finish this activity to remove it from the back stack
    }
}