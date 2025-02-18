package com.attendance.clockme;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.widget.Button;
import android.widget.Toast;

public class AdminRelieveActivity extends AppCompatActivity {
    private LinearLayout relieveLayout; // Container for dynamically added leave request cards
    private LinearLayout homeLayout, createLayout, signoutLayout, requestLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_relieve); // Set the content view to the admin leave activity layout

        relieveLayout = findViewById(R.id.relieveLayout); // Initialize the LinearLayout

        fetchLeaveRequests();

        homeLayout = findViewById(R.id.homeLayout);
        createLayout = findViewById(R.id.createLayout);
        signoutLayout = findViewById(R.id.signoutLayout);
        requestLayout = findViewById(R.id.requestLayout);

        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminRelieveActivity.this, ActivityAdminDashboard.class);
                startActivity(intent);
            }
        });

        // Create icon click - Navigate to EmpRegActivity
        createLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminRelieveActivity.this, EmpRegActivity.class);
                startActivity(intent);
            }
        });

        // Request icon click - Navigate to AdminDailyAttendanceActivity
        requestLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminRelieveActivity.this, AdminDailyAttendanceActivity.class);
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
        // Find the button by its ID
        Button btnHistory = findViewById(R.id.btnHistory);

        // Set an OnClickListener on the button
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start AdminRelieveHistoryActivity
                Intent intent = new Intent(AdminRelieveActivity.this, AdminRelieveHistoryActivity.class);
                startActivity(intent); // Start the new activity
            }
        });
    }

    private void fetchLeaveRequests() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("RelieveRequest");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                relieveLayout.removeAllViews(); // Clear previous cards to avoid duplicates

                // Check if there are no leave requests
                if (!snapshot.exists()) {
                    Log.d("AdminRelieveActivity", "No leave requests found");
                    return;
                }

                // Loop through each child in the snapshot
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    RelieveRequest request = dataSnapshot.getValue(RelieveRequest.class);
                    if (request != null) {
                        request.setRequestId(dataSnapshot.getKey()); // Set Firebase key as requestId
                        Log.d("AdminRelieveActivity", "Fetched request: " + request.getUsername());

                        // Only add requests that are pending
                        if ("Pending".equals(request.getStatus())) {
                            addLeaveRequestCard(request); // Add card to layout
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminRelieveActivity.this, "Error fetching requests: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addLeaveRequestCard(RelieveRequest request) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.relieve_request_card, relieveLayout, false);

        // Initialize the card view elements
        TextView username = cardView.findViewById(R.id.tvUsername);
        TextView position = cardView.findViewById(R.id.tvPosition);
        TextView email = cardView.findViewById(R.id.tvEmail);
        TextView date = cardView.findViewById(R.id.tvdate);
        TextView reason = cardView.findViewById(R.id.tvReason);
        TextView status = cardView.findViewById(R.id.tvStatus);
        Button btnApprove = cardView.findViewById(R.id.btnApprove);
        Button btnReject = cardView.findViewById(R.id.btnReject);
        View disappearLayout = cardView.findViewById(R.id.relieve_disappear);

        // Set text for each view
        username.setText(capitalizeFirstLetter(request.getUsername()));
        position.setText(capitalizeFirstLetter(request.getPosition()));
        email.setText(request.getEmail());
        date.setText("Date: " + request.getDate());
        reason.setText("Reason: " + request.getReason());
        status.setText("Status: " + (request.getStatus() != null ? request.getStatus() : "Pending"));

        // Set click listeners for approve and reject buttons
        btnApprove.setOnClickListener(v -> updateLeaveRequestStatus(request, "Approved", disappearLayout, cardView));
        btnReject.setOnClickListener(v -> updateLeaveRequestStatus(request, "Rejected", disappearLayout, cardView));

        // Add the card view to the layout
        relieveLayout.addView(cardView);
    }

    private void updateLeaveRequestStatus(RelieveRequest request, String newStatus, View disappearLayout, View cardView) {
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("RelieveRequest").child(request.getRequestId());
        requestRef.child("status").setValue(newStatus).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AdminRelieveActivity.this, newStatus + " successfully", Toast.LENGTH_SHORT).show();
                disappearLayout.setVisibility(View.GONE); // Hide the specific layout
                relieveLayout.removeView(cardView); // Remove the card from layout after updating
            } else {
                Toast.makeText(AdminRelieveActivity.this, "Error updating request status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    public static class RelieveRequest {
        private String requestId;
        private String username;
        private String position;
        private String email;
        private String date;
        private String reason;
        private String status;

        // Default constructor for Firebase
        public RelieveRequest() {}

        // Getters
        public String getRequestId() { return requestId; }
        public String getUsername() { return username; }
        public String getPosition() { return position; }
        public String getEmail() { return email; }
        public String getDate() { return date; }
        public String getReason() { return reason; }
        public String getStatus() { return status; }

        // Setters
        public void setRequestId(String requestId) { this.requestId = requestId; }
        public void setUsername(String username) { this.username = username; }
        public void setPosition(String position) { this.position = position; }
        public void setEmail(String email) { this.email = email; }
        public void setDate(String date) { this.date = date; }
        public void setReason(String reason) { this.reason = reason; }
        public void setStatus(String status) { this.status = status; }
    }

}

