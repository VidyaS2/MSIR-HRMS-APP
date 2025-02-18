package com.attendance.clockme;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.atomic.AtomicInteger;

public class ActivityAdminDashboard extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private LinearLayout employeeContainer;
    private LinearLayout homeLayout, createLayout, signoutLayout, requestLayout;
    private Button addEmployeeButton;
    private int totalApprovedCount = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize the container where employee cards will be added
        employeeContainer = findViewById(R.id.employeeContainer);

        // Set up the "Add Employee" button
        addEmployeeButton = findViewById(R.id.button_add_employee);
        addEmployeeButton.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityAdminDashboard.this, AdminRequestActivity.class);
            startActivity(intent);
        });

        // Set up the Holiday button
        Button holidayButton = findViewById(R.id.button_holiday);
        holidayButton.setOnClickListener(v -> {
            // Start AdminHolidayDeclaration Activity
            Intent intent = new Intent(ActivityAdminDashboard.this, AdminHolidayDeclaration.class);
            startActivity(intent);
        });

        // Fetch and display employees
        fetchAndDisplayEmployees();

        // Fetch total approved request count
        fetchTotalApprovedCount();

        // Initialize the footer layout buttons
        homeLayout = findViewById(R.id.homeLayout);
        createLayout = findViewById(R.id.createLayout);
        signoutLayout = findViewById(R.id.signoutLayout);
        requestLayout = findViewById(R.id.requestLayout);

        createLayout.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityAdminDashboard.this, EmpRegActivity.class);
            startActivity(intent);
        });

        // Request icon click - Navigate to AdminDailyAttendanceActivity
        requestLayout.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityAdminDashboard.this, AdminDailyAttendanceActivity.class);
            startActivity(intent);
        });

        // SignOut icon click - Logout and navigate to SignInActivity
        signoutLayout.setOnClickListener(v -> {
            // Sign out the user from FirebaseAuth
            FirebaseAuth.getInstance().signOut();

            // Clear SharedPreferences or any stored user session data if needed
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            // Show a toast message indicating the user has signed out
            Toast.makeText(getApplicationContext(), "Signed out", Toast.LENGTH_SHORT).show();

            // Redirect the user to the SignInActivity
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void fetchAndDisplayEmployees() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                employeeContainer.removeAllViews(); // Clear previous views
                Log.d("FetchEmployees", "Total users fetched: " + dataSnapshot.getChildrenCount());

                for (DataSnapshot employeeSnapshot : dataSnapshot.getChildren()) {
                    String uid = employeeSnapshot.getKey();
                    String username = employeeSnapshot.child("username").getValue(String.class);
                    String position = employeeSnapshot.child("position").getValue(String.class);
                    String empId = employeeSnapshot.child("empId").getValue(String.class);
                    String department = employeeSnapshot.child("department").getValue(String.class);

                    if (username != null && position != null && empId != null && department != null) {
                        addEmployeeCard(username, position, empId, department, uid);
                    } else {
                        Log.w("FetchEmployees", "Missing data for UID: " + uid);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FetchEmployees", "Error fetching data: " + databaseError.getMessage());
            }
        });
    }

    // Fetch total approved request count
    private void fetchTotalApprovedCount() {
        String[] requestTypes = {"LateRequests", "permissionRequests", "LeaveRequests", "RelieveRequest"};
        totalApprovedCount = 0;
        final int[] pendingCounts = new int[requestTypes.length]; // Array to store counts for each type
        final AtomicInteger completedRequests = new AtomicInteger(0); // Track completed listeners

        for (int i = 0; i < requestTypes.length; i++) {
            final int index = i; // Final variable for inner class reference
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(requestTypes[i]);

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int count = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String status = snapshot.child("status").getValue(String.class);
                        if ("pending".equalsIgnoreCase(status)) {
                            count++;
                        }
                    }
                    pendingCounts[index] = count; // Store count for this request type

                    // Check if all listeners have finished
                    if (completedRequests.incrementAndGet() == requestTypes.length) {
                        totalApprovedCount = 0;
                        for (int pending : pendingCounts) {
                            totalApprovedCount += pending;
                        }
                        updateButtonCount();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FetchApprovedCount", "Error fetching data: " + databaseError.getMessage());
                }
            });
        }
    }

    private void updateButtonCount() {
        addEmployeeButton.setText("Requests(" + totalApprovedCount + ")");
    }

    // Add employee card dynamically
    private void addEmployeeCard(String username, String position, String email, String department, String uid) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.card_employee, employeeContainer, false);

        TextView employeeNameTextView = cardView.findViewById(R.id.employeename);
        TextView positionTextView = cardView.findViewById(R.id.position);
        Button viewButton = cardView.findViewById(R.id.textView8);

        // Capitalize the first letter of username and position
        employeeNameTextView.setText(capitalizeFirstLetter(username));
        positionTextView.setText(capitalizeFirstLetter(position));

        // Add click listener for the "View" button
        viewButton.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityAdminDashboard.this, ActivityAdminClockview.class);
            intent.putExtra("UID", uid);
            startActivity(intent);
        });

        // Add card to the container layout
        employeeContainer.addView(cardView);
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str; // Return the original string if null or empty
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}
