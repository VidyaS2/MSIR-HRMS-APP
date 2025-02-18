package com.attendance.clockme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

public class AdminLeaveHistoryActivity extends AppCompatActivity {
    private DatabaseReference leaveRequestsRef;
    private TableLayout dynamicRowContainer;
    private LinearLayout homeLayout, createLayout, signoutLayout, requestLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_leave_history);

        // Initialize Firebase reference for LeaveRequests table
        leaveRequestsRef = FirebaseDatabase.getInstance().getReference("LeaveRequests");

        // Initialize TableLayout
        dynamicRowContainer = findViewById(R.id.dynamicRowContainer);

        // Fetch leave request data
        fetchLeaveRequests();

        homeLayout = findViewById(R.id.homeLayout);
        createLayout = findViewById(R.id.createLayout);
        signoutLayout = findViewById(R.id.signoutLayout);
        requestLayout = findViewById(R.id.requestLayout);

        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminLeaveHistoryActivity.this, ActivityAdminDashboard.class);
                startActivity(intent);
            }
        });

        // Create icon click - Navigate to EmpRegActivity
        createLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminLeaveHistoryActivity.this, EmpRegActivity.class);
                startActivity(intent);
            }
        });

        // Request icon click - Navigate to AdminDailyAttendanceActivity
        requestLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminLeaveHistoryActivity.this, AdminDailyAttendanceActivity.class);
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

    private void fetchLeaveRequests() {
        leaveRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear previous data
                clearPreviousData();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    String username = snapshot.child("username").getValue(String.class);
                    String position = snapshot.child("position").getValue(String.class);
                    String fromDate = snapshot.child("fromDate").getValue(String.class);
                    String toDate = snapshot.child("toDate").getValue(String.class);
                    String reason = snapshot.child("reason").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);

                    // Add the row to the dynamic table
                    addRowToTable( username, position, fromDate, toDate, reason, status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Toast.makeText(AdminLeaveHistoryActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearPreviousData() {
        // Remove all dynamic rows before adding new ones
        dynamicRowContainer.removeViews(1, dynamicRowContainer.getChildCount() - 1); // Keep the header
    }

    private void addRowToTable( String username, String position, String fromDate, String toDate, String reason, String status) {
        // Create a new TableRow
        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        // Create TextViews for each piece of data
        TextView namePositionTextView = createTextView(capitalizeFirstLetter(username) + " - " + capitalizeFirstLetter(position));
        TextView fromDateTextView = createTextView(fromDate);
        TextView toDateTextView = createTextView(toDate);
        TextView reasonTextView = createTextView(reason);
        TextView statusTextView = createTextView(status);

        // Add TextViews to TableRow
        tableRow.addView(namePositionTextView);
        tableRow.addView(fromDateTextView);
        tableRow.addView(toDateTextView);
        tableRow.addView(reasonTextView);
        tableRow.addView(statusTextView);

        // Add TableRow to the dynamic TableLayout
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
