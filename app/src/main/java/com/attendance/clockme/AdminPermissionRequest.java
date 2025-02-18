package com.attendance.clockme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

public class AdminPermissionRequest extends AppCompatActivity {
    private LinearLayout permissionsLayout;
    private LinearLayout homeLayout, createLayout, signoutLayout, requestLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_permission);

        // LinearLayout that will contain dynamically added CardViews
        permissionsLayout = findViewById(R.id.permissionsLayout);

        // Fetch the permission requests from Firebase
        fetchPermissionRequests();
        // Find the button by its ID
        Button btnHistory = findViewById(R.id.btnHistory);

        // Set an OnClickListener on the button
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the AdminPermissionHistoryActivity
                Intent intent = new Intent(AdminPermissionRequest.this, AdminPermissionHistoryActivity.class);
                startActivity(intent);
            }
        });

        homeLayout = findViewById(R.id.homeLayout);
        createLayout = findViewById(R.id.createLayout);
        signoutLayout = findViewById(R.id.signoutLayout);
        requestLayout = findViewById(R.id.requestLayout);

        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPermissionRequest.this, ActivityAdminDashboard.class);
                startActivity(intent);
            }
        });

        // Create icon click - Navigate to EmpRegActivity
        createLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPermissionRequest.this, EmpRegActivity.class);
                startActivity(intent);
            }
        });

        // Request icon click - Navigate to AdminDailyAttendanceActivity
        requestLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPermissionRequest.this, AdminDailyAttendanceActivity.class);
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
    }

    private void fetchPermissionRequests() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("permissionRequests");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                permissionsLayout.removeAllViews(); // Clear previous data if any
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    PermissionRequest request = dataSnapshot.getValue(PermissionRequest.class);
                    if (request != null) {
                        // Set Firebase key as requestId in PermissionRequest
                        request.setRequestId(dataSnapshot.getKey());

                        // Only add card if status is "Pending"
                        if ("pending".equals(request.getStatus())) {
                            addPermissionRequestCard(request);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
                Toast.makeText(AdminPermissionRequest.this, "Error fetching requests: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addPermissionRequestCard(PermissionRequest request) {
        // Inflate the card layout and set the data
        View cardView = LayoutInflater.from(this).inflate(R.layout.permission_request_card, permissionsLayout, false);

        TextView username = cardView.findViewById(R.id.tvUsername);
        TextView position = cardView.findViewById(R.id.tvPosition);
        TextView email = cardView.findViewById(R.id.tvEmail);
        TextView date = cardView.findViewById(R.id.tvDate);
        TextView hours = cardView.findViewById(R.id.tvHours);
        TextView fromTime = cardView.findViewById(R.id.tvFromTime);
        TextView toTime = cardView.findViewById(R.id.tvToTime);
        TextView reason = cardView.findViewById(R.id.tvReason);
        TextView status = cardView.findViewById(R.id.tvStatus); // Add status TextView
        Button btnApprove = cardView.findViewById(R.id.btnApprove);
        Button btnReject = cardView.findViewById(R.id.btnReject);
        // Get the layout that should disappear
        View disappearLayout = cardView.findViewById(R.id.permission_disappear);

        // Capitalize username and position
        String capitalizedUsername = capitalizeFirstLetter(request.getUsername());
        String capitalizedPosition = capitalizeFirstLetter(request.getPosition());

        // Set the text values from the PermissionRequest object
        username.setText(capitalizedUsername);
        position.setText(capitalizedPosition);
        email.setText(request.getEmail());

        // Set date with "Date: " prefix
        date.setText("Date: " + request.getDate());

        hours.setText("How many hours: " + request.getHours());
        fromTime.setText("From Time: " + request.getFromTime());
        toTime.setText("To Time: " + request.getToTime());
        reason.setText("Reason: " + request.getReason());
        status.setText("Status: " + request.getStatus() != null ? request.getStatus() : "Pending"); // Set status, default to "Pending" if null

        // Set Approve button click listener
        btnApprove.setOnClickListener(v -> {
            DatabaseReference requestRef = FirebaseDatabase.getInstance()
                    .getReference("permissionRequests")
                    .child(request.getRequestId());
            requestRef.child("status").setValue("Approved").addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AdminPermissionRequest.this, "Approved successfully", Toast.LENGTH_SHORT).show();
                    disappearLayout.setVisibility(View.GONE); // Hide the specific layout
                } else {
                    Toast.makeText(AdminPermissionRequest.this, "Error approving request", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Set Reject button click listener
        btnReject.setOnClickListener(v -> {
            DatabaseReference requestRef = FirebaseDatabase.getInstance()
                    .getReference("permissionRequests")
                    .child(request.getRequestId());
            permissionsLayout.removeView(cardView);
            requestRef.child("status").setValue("Rejected").addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AdminPermissionRequest.this, "Rejected successfully", Toast.LENGTH_SHORT).show();
                    disappearLayout.setVisibility(View.GONE); // Hide the specific layout
                } else {
                    Toast.makeText(AdminPermissionRequest.this, "Error rejecting request", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Add the card to the LinearLayout
        permissionsLayout.addView(cardView);
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }


    // Inner class for PermissionRequest
    public static class PermissionRequest {
        private String requestId;
        private String username;
        private String position;
        private String email;
        private String date;
        private String hours;
        private String fromTime;
        private String toTime;
        private String reason;
        private String status; // Add status field

        // Constructor
        public PermissionRequest() {
            // Default constructor required for calls to DataSnapshot.getValue(PermissionRequest.class)
        }

        // Getters and Setters
        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getHours() {
            return hours;
        }

        public void setHours(String hours) {
            this.hours = hours;
        }

        public String getFromTime() {
            return fromTime;
        }

        public void setFromTime(String fromTime) {
            this.fromTime = fromTime;
        }

        public String getToTime() {
            return toTime;
        }

        public void setToTime(String toTime) {
            this.toTime = toTime;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
