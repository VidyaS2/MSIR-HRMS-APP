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

public class AdminLeaveRequest extends AppCompatActivity {
    private LinearLayout leaveLayout; // Container for dynamically added leave request cards
    private LinearLayout homeLayout, createLayout, signoutLayout, requestLayout;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_leave); // Set the content view to the admin leave activity layout

        // Initialize the LinearLayout that will contain the leave request cards
        leaveLayout = findViewById(R.id.leaveLayout);

        // Fetch leave requests from Firebase
        fetchLeaveRequests();

        homeLayout = findViewById(R.id.homeLayout);
        createLayout = findViewById(R.id.createLayout);
        signoutLayout = findViewById(R.id.signoutLayout);
        requestLayout = findViewById(R.id.requestLayout);

        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminLeaveRequest.this, ActivityAdminDashboard.class);
                startActivity(intent);
            }
        });

        // Create icon click - Navigate to EmpRegActivity
        createLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminLeaveRequest.this, EmpRegActivity.class);
                startActivity(intent);
            }
        });

        // Request icon click - Navigate to AdminDailyAttendanceActivity
        requestLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminLeaveRequest.this, AdminDailyAttendanceActivity.class);
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

        Button btnHistory = findViewById(R.id.btnHistory);

        // Set an OnClickListener on the button
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start AdminLeaveHistoryActivity
                Intent intent = new Intent(AdminLeaveRequest.this, AdminLeaveHistoryActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fetchLeaveRequests() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("LeaveRequests");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                leaveLayout.removeAllViews(); // Clear previous cards to avoid duplicates
                if (!snapshot.exists()) {
                    Log.d("AdminLeaveRequest", "No leave requests found");
                    return;
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    LeaveRequest request = dataSnapshot.getValue(LeaveRequest.class);
                    if (request != null) {
                        request.setRequestId(dataSnapshot.getKey()); // Set Firebase key as requestId

                        // Only add the card if the status is "Pending"
                        if ("pending".equals(request.getStatus())) {
                            addLeaveRequestCard(request); // Add card to layout
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
                Toast.makeText(AdminLeaveRequest.this, "Error fetching requests: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addLeaveRequestCard(LeaveRequest request) {
        // Inflate the card layout and set the data
        View cardView = LayoutInflater.from(this).inflate(R.layout.card_leave_activity, leaveLayout, false);

        // Get references to the TextViews and Buttons in the card layout
        TextView username = cardView.findViewById(R.id.tvUsername);
        TextView position = cardView.findViewById(R.id.tvPosition);
        TextView email = cardView.findViewById(R.id.tvEmail);
        TextView fromDate = cardView.findViewById(R.id.tvFromdate);
        TextView toDate = cardView.findViewById(R.id.tvTodate);
        TextView reason = cardView.findViewById(R.id.tvReason);
        TextView status = cardView.findViewById(R.id.tvStatus);
        Button btnApprove = cardView.findViewById(R.id.btnApprove);
        Button btnReject = cardView.findViewById(R.id.btnReject);
        View disappearLayout = cardView.findViewById(R.id.leave_disappear); // Layout that will disappear on approval/rejection

        // Capitalize username and position
        username.setText(capitalizeFirstLetter(request.getUsername()));
        position.setText(capitalizeFirstLetter(request.getPosition()));
        email.setText(request.getEmail());
        fromDate.setText("From Date: " + request.getFromDate());
        toDate.setText("To Date: " + request.getToDate());
        reason.setText("Reason: " + request.getReason());
        status.setText("Status: " + (request.getStatus() != null ? request.getStatus() : "Pending"));

        // Set click listener for the Approve button
        btnApprove.setOnClickListener(v -> {
            DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("LeaveRequests").child(request.getRequestId());
            requestRef.child("status").setValue("Approved").addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AdminLeaveRequest.this, "Approved successfully", Toast.LENGTH_SHORT).show();
                    disappearLayout.setVisibility(View.GONE); // Hide the specific layout
                } else {
                    Toast.makeText(AdminLeaveRequest.this, "Error approving request", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Set click listener for the Reject button
        btnReject.setOnClickListener(v -> {
            DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("LeaveRequests").child(request.getRequestId());
            leaveLayout.removeView(cardView); // Remove the card from the layout
            requestRef.child("status").setValue("Rejected").addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AdminLeaveRequest.this, "Rejected successfully", Toast.LENGTH_SHORT).show();
                    disappearLayout.setVisibility(View.GONE); // Hide the specific layout
                } else {
                    Toast.makeText(AdminLeaveRequest.this, "Error rejecting request", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Add the card to the LinearLayout
        leaveLayout.addView(cardView);
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    // Inner class for LeaveRequest
    public static class LeaveRequest {
        private String requestId;
        private String username;
        private String position;
        private String email;
        private String fromDate;
        private String toDate;
        private String reason;
        private String status;

        public LeaveRequest() {
            // Default constructor required for calls to DataSnapshot.getValue(LeaveRequest.class)
        }

        // Getters and Setters for the LeaveRequest fields
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFromDate() { return fromDate; }
        public void setFromDate(String fromDate) { this.fromDate = fromDate; }
        public String getToDate() { return toDate; }
        public void setToDate(String toDate) { this.toDate = toDate; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
