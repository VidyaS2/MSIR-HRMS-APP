package com.attendance.clockme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.graphics.Color;
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

public class AdminLateHistoryActivity extends AppCompatActivity {

    private TableLayout dynamicRowContainer;
    private LinearLayout homeLayout, createLayout, signoutLayout, requestLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_late_history); // Ensure this matches your layout file

        dynamicRowContainer = findViewById(R.id.dynamicRowContainer);

        // Add table header before fetching data
        addTableHeader();

        // Fetch data from Firebase
        fetchLateRequests();

        homeLayout = findViewById(R.id.homeLayout);
        createLayout = findViewById(R.id.createLayout);
        signoutLayout = findViewById(R.id.signoutLayout);
        requestLayout = findViewById(R.id.requestLayout); // Corrected ID

        // Home icon click - Navigate to ActivityAdminDashboard
        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminLateHistoryActivity.this, ActivityAdminDashboard.class);
                startActivity(intent);
            }
        });

        // Create icon click - Navigate to EmpRegActivity
        createLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminLateHistoryActivity.this, EmpRegActivity.class);
                startActivity(intent);
            }
        });

        // Request icon click - Navigate to AdminDailyAttendanceActivity
        requestLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminLateHistoryActivity.this, AdminDailyAttendanceActivity.class);
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

    private void addTableHeader() {
        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(Color.parseColor("#3C71B1"));
        headerRow.setPadding(4, 4, 4, 4);

        String[] headers = {"Date", "Name and Position", "Clock In Time", "Clock Out Time", "Remark", "Status"};
        for (String headerText : headers) {
            TextView header = new TextView(this);
            header.setText(headerText);
            header.setPadding(8, 8, 8, 8);
            header.setGravity(Gravity.CENTER);
            header.setTextColor(Color.WHITE);
            header.setTextSize(16);
            header.setTypeface(null, android.graphics.Typeface.BOLD);
            headerRow.addView(header);
        }

        // Add header row to the TableLayout
        dynamicRowContainer.addView(headerRow);
    }

    private void fetchLateRequests() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("LateRequests");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Remove all rows except the header
                dynamicRowContainer.removeViews(1, Math.max(0, dynamicRowContainer.getChildCount() - 1));

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String date = snapshot.child("date").getValue(String.class);
                    String username = capitalizeWords(snapshot.child("username").getValue(String.class));
                    String position = capitalizeWords(snapshot.child("position").getValue(String.class));
                    String clockInTime = snapshot.child("clockInTime").getValue(String.class);
                    String clockOutTime = snapshot.child("clockOutTime").getValue(String.class);
                    String remark = snapshot.child("remark").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);

                    addTableRow(date, username, position, clockInTime, clockOutTime, remark, status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    private void addTableRow(String date, String username, String position, String clockInTime, String clockOutTime, String remark, String status) {
        TableRow tableRow = new TableRow(this);

        TextView dateTextView = createTextView(date);
        TextView userTextView = createTextView(username + " (" + position + ")");
        TextView clockInTextView = createTextView(clockInTime);
        TextView clockOutTextView = createTextView(clockOutTime);
        TextView remarkTextView = createTextView(remark);
        TextView statusTextView = createTextView(status);

        // Add views to the table row
        tableRow.addView(dateTextView);
        tableRow.addView(userTextView);
        tableRow.addView(clockInTextView);
        tableRow.addView(clockOutTextView);
        tableRow.addView(remarkTextView);
        tableRow.addView(statusTextView);

        // Add the table row to the TableLayout
        dynamicRowContainer.addView(tableRow);
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.BLACK);
        return textView;
    }

    // Helper method to capitalize the first letter of each word
    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder capitalizedText = new StringBuilder();
        for (String word : text.split(" ")) {
            if (!word.isEmpty()) {
                capitalizedText.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        // Trim any trailing space
        return capitalizedText.toString().trim();
    }}
