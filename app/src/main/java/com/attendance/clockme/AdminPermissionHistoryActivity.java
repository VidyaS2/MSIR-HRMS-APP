package com.attendance.clockme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
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

public class AdminPermissionHistoryActivity extends AppCompatActivity {
    private DatabaseReference permissionRequestsRef;
    private LinearLayout homeLayout, createLayout, signoutLayout, requestLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_permission_history);

        // Initialize Firebase reference
        permissionRequestsRef = FirebaseDatabase.getInstance().getReference("permissionRequests");

        // Fetch permission request data
        fetchPermissionRequests();

        homeLayout = findViewById(R.id.homeLayout);
        createLayout = findViewById(R.id.createLayout);
        signoutLayout = findViewById(R.id.signoutLayout);
        requestLayout = findViewById(R.id.requestLayout);

        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPermissionHistoryActivity.this, ActivityAdminDashboard.class);
                startActivity(intent);
            }
        });

        // Create icon click - Navigate to EmpRegActivity
        createLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPermissionHistoryActivity.this, EmpRegActivity.class);
                startActivity(intent);
            }
        });

        // Request icon click - Navigate to AdminDailyAttendanceActivity
        requestLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPermissionHistoryActivity.this, AdminDailyAttendanceActivity.class);
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
        permissionRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear previous data
                clearPreviousData();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Fetch data from snapshot
                    String date = snapshot.child("date").getValue(String.class);
                    String username = snapshot.child("username").getValue(String.class);
                    String position = snapshot.child("position").getValue(String.class);
                    String totalHours = snapshot.child("hours").getValue(String.class);
                    String fromTime = snapshot.child("fromTime").getValue(String.class);
                    String toTime = snapshot.child("toTime").getValue(String.class);
                    String reason = snapshot.child("reason").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class); // Assuming you have a status field

                    // Check if the status is not approved
                    if (!"approved".equalsIgnoreCase(status)) {
                        // Capitalize first letters of username and position
                        username = capitalizeFirstLetter(username);
                        position = capitalizeFirstLetter(position);

                        // Create a new TableRow or equivalent UI element for each request
                        addRowToTable(date, username, position, totalHours, fromTime, toTime, reason, status);
                    }
                }}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Toast.makeText(AdminPermissionHistoryActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearPreviousData() {
        // Clear any previous data from your UI before populating new data
    }

    private void addRowToTable(String date, String username, String position, String totalHours, String fromTime, String toTime, String reason, String status) {
        // Implement this method to add data to your UI, e.g., add to a TableLayout or RecyclerView
        // Example implementation (if using TableLayout):
        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        // Create TextViews for each piece of data
        TextView dateTextView = createTextView(date);
        TextView userPositionTextView = createTextView(username + " - " + position);
        TextView totalHoursTextView = createTextView(totalHours);
        TextView fromTimeTextView = createTextView(fromTime);
        TextView toTimeTextView = createTextView(toTime);
        TextView reasonTextView = createTextView(reason);
        TextView statusTextView = createTextView(status);

        // Add TextViews to TableRow
        tableRow.addView(dateTextView);
        tableRow.addView(userPositionTextView);
        tableRow.addView(totalHoursTextView);
        tableRow.addView(fromTimeTextView);
        tableRow.addView(toTimeTextView);
        tableRow.addView(reasonTextView);
        tableRow.addView(statusTextView);

        // Assuming you have a TableLayout with the ID `dynamicRowContainer`
        TableLayout dynamicRowContainer = findViewById(R.id.dynamicRowContainer);
        dynamicRowContainer.addView(tableRow);
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));
        return textView;
    }
    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String[] words = input.split(" ");
        StringBuilder capitalized = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                capitalized.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1)).append(" ");
            }
        }
        return capitalized.toString().trim();
    }

}
