package com.attendance.clockme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AdminDailyAttendanceActivity extends AppCompatActivity {

    private LinearLayout dynamicRowContainer;
    private DatabaseReference databaseReference;
    private SimpleDateFormat dateFormat;
    private LinearLayout homeLayout, createLayout, signoutLayout, requestLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dailyattendance);

        dynamicRowContainer = findViewById(R.id.dynamicRowContainer);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize date format
        dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        fetchDailyAttendance();


        homeLayout = findViewById(R.id.homeLayout);
        createLayout = findViewById(R.id.createLayout);
        signoutLayout = findViewById(R.id.signoutLayout);
        requestLayout = findViewById(R.id.requestLayout);

        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDailyAttendanceActivity.this, ActivityAdminDashboard.class);
                startActivity(intent);
            }
        });

        // Create icon click - Navigate to EmpRegActivity
        createLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDailyAttendanceActivity.this, EmpRegActivity.class);
                startActivity(intent);
            }
        });

        // Request icon click - Navigate to AdminDailyAttendanceActivity
        requestLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDailyAttendanceActivity.this, AdminDailyAttendanceActivity.class);
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

    private void fetchDailyAttendance() {
        // Get today's date in dd-MM-yyyy format
        String currentDate = dateFormat.format(Calendar.getInstance().getTime());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dynamicRowContainer.removeAllViews(); // Clear previous data

                // Iterate over each user in the database
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String username = userSnapshot.child("username").getValue(String.class);

                    // Capitalize username or set to UNKNOWN if null
                    username = (username != null) ? username.toUpperCase() : "UNKNOWN";

                    // Fetch today's timeRecords for the user
                    DataSnapshot timeRecordsSnapshot = userSnapshot.child("timeRecords").child(currentDate);

                    // Initialize default values
                    String clockInTime = "00:00";
                    String clockInLocation = "Not Found";
                    String clockOutTime = "00:00";
                    String clockOutLocation = "Not Found";

                    // If time records exist for today, fetch the data
                    if (timeRecordsSnapshot.exists()) {
                        clockInTime = timeRecordsSnapshot.child("ClockInTime").getValue(String.class);
                        clockInLocation = timeRecordsSnapshot.child("clockInAddress").getValue(String.class);
                        clockOutTime = timeRecordsSnapshot.child("ClockOutTime").getValue(String.class);
                        clockOutLocation = timeRecordsSnapshot.child("clockOutAddress").getValue(String.class);

                        // Set default values for null data
                        clockInTime = (clockInTime != null) ? clockInTime : "00:00";
                        clockInLocation = (clockInLocation != null) ? clockInLocation : "Not Found";
                        clockOutTime = (clockOutTime != null) ? clockOutTime : "00:00";
                        clockOutLocation = (clockOutLocation != null) ? clockOutLocation : "Not Found";
                    }

                    // Add the row to the layout
                    addRowToLayout(username, clockInTime, clockInLocation, clockOutTime, clockOutLocation);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDailyAttendanceActivity.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void addRowToLayout(String username, String clockInTime, String clockInLocation, String clockOutTime, String clockOutLocation) {
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setPadding(8, 8, 8, 8);

        // Username TextView
        TextView usernameTextView = createTextView(username);

        // ClockInTime TextView with expandable location
        TextView clockInTextView = createTextView(clockInTime + " ...");
        clockInTextView.setOnClickListener(v -> {
            if (clockInTextView.getText().toString().contains("...")) {
                clockInTextView.setText(clockInTime + " (" + clockInLocation + ")");
            } else {
                clockInTextView.setText(clockInTime + " ...");
            }
        });

        // ClockOutTime TextView with expandable location
        TextView clockOutTextView = createTextView(clockOutTime + " ...");
        clockOutTextView.setOnClickListener(v -> {
            if (clockOutTextView.getText().toString().contains("...")) {
                clockOutTextView.setText(clockOutTime + " (" + clockOutLocation + ")");
            } else {
                clockOutTextView.setText(clockOutTime + " ...");
            }
        });

        // Add all views to the row layout
        rowLayout.addView(usernameTextView);
        rowLayout.addView(clockInTextView);
        rowLayout.addView(clockOutTextView);

        // Add row layout to the container
        dynamicRowContainer.addView(rowLayout);
    }


    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        textView.setText(text);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(ContextCompat.getColor(this, android.R.color.black)); // Use ContextCompat for color
        return textView;
    }
}
