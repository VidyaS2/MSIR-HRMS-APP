package com.attendance.clockme;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminRequestActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_request);

        // Find the TextView where the count will be displayed
        TextView lateRequestCountTextView = findViewById(R.id.count1);
        TextView permissionRequestCountTextView = findViewById(R.id.count2);
        TextView leaveRequestCountTextView = findViewById(R.id.count3);
        TextView relieveRequestCountTextView = findViewById(R.id.count4);

        // Initialize Firebase database reference
        DatabaseReference lateRequestsRef = FirebaseDatabase.getInstance().getReference("LateRequests");
        DatabaseReference permissionRequestsRef = FirebaseDatabase.getInstance().getReference("permissionRequests");
        DatabaseReference leaveRequestsRef = FirebaseDatabase.getInstance().getReference("LeaveRequests");
        DatabaseReference relieveRequestsRef = FirebaseDatabase.getInstance().getReference("RelieveRequest");

        // Fetch counts for each request type where status is "Pending"
        fetchPendingCount(lateRequestsRef, lateRequestCountTextView);
        fetchPendingCount(permissionRequestsRef, permissionRequestCountTextView);
        fetchPendingCount(leaveRequestsRef, leaveRequestCountTextView);
        fetchPendingCount(relieveRequestsRef, relieveRequestCountTextView);

        // Initialize the footer layout buttons
        LinearLayout homeLayout = findViewById(R.id.homeLayout);
        LinearLayout createLayout = findViewById(R.id.createLayout);
        LinearLayout signoutLayout = findViewById(R.id.signoutLayout);
        LinearLayout requestLayout = findViewById(R.id.requestLayout);

        // Home icon click - Navigate to ActivityAdminDashboard
        homeLayout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminRequestActivity.this, ActivityAdminDashboard.class);
            startActivity(intent);
        });

        // Request icon click - Navigate to AdminDailyAttendanceActivity
        requestLayout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminRequestActivity.this, AdminDailyAttendanceActivity.class);
            startActivity(intent);
        });

        // Create icon click - Navigate to EmpRegActivity
        createLayout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminRequestActivity.this, EmpRegActivity.class);
            startActivity(intent);
        });

        // SignOut icon click - Logout and navigate to SignInActivity
        signoutLayout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(AdminRequestActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminRequestActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Find the ImageViews
        ImageView lateInImageView = findViewById(R.id.late_in);
        ImageView permissionInImageView = findViewById(R.id.permission_in);
        ImageView leaveInImageView = findViewById(R.id.leave_in);
        ImageView relieveInImageView = findViewById(R.id.relieve_in);

        // Set click listeners for each ImageView
        lateInImageView.setOnClickListener(view -> {
            Intent intent = new Intent(AdminRequestActivity.this, AdminLateActivity.class);
            startActivity(intent);
        });
        permissionInImageView.setOnClickListener(view -> {
            Intent intent = new Intent(AdminRequestActivity.this, AdminPermissionRequest.class);
            startActivity(intent);
        });
        leaveInImageView.setOnClickListener(view -> {
            Intent intent = new Intent(AdminRequestActivity.this, AdminLeaveRequest.class);
            startActivity(intent);
        });
        relieveInImageView.setOnClickListener(view -> {
            Intent intent = new Intent(AdminRequestActivity.this, AdminRelieveActivity.class);
            startActivity(intent);
        });
    }


    private void fetchPendingCount(DatabaseReference reference, TextView textView) {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if ("Pending".equalsIgnoreCase(status)) {
                        count++;
                    }
                }
                textView.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminRequestActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
