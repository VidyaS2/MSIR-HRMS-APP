package com.attendance.clockme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActivityClockInOutRemark extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;

    private TextView welcomeTextView;
    private TextView tvDate, tvClockInTime, tvClockOutTime;
    private EditText remarkEditText;
    private Button submitButton;
    private Spinner spinnerTeam, spinnerClockInOut;
    private List<String> teamList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clockinout_remark);  // Replace with your layout name

        // Initialize Firebase Auth and User
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        SharedPreferences sharedPreferences = getSharedPreferences("YourPrefsName", MODE_PRIVATE);

        // Initialize Views
        welcomeTextView = findViewById(R.id.welcomeMessage);
        tvDate = findViewById(R.id.tv_date);
        tvClockInTime = findViewById(R.id.tv_clock_in_time);
        tvClockOutTime = findViewById(R.id.tv_clock_out_time);
        remarkEditText = findViewById(R.id.et_remark);
        submitButton = findViewById(R.id.btn_submit);
        spinnerTeam = findViewById(R.id.spinner_team);  // Spinner for team selection
        spinnerClockInOut = findViewById(R.id.spinner_clockinout);  // Spinner for clock in and clock out

        LinearLayout homeIcon = findViewById(R.id.homelayout5);
        LinearLayout clockIcon = findViewById(R.id.clocklayout5);
        LinearLayout logoutIcon = findViewById(R.id.signoutlayout5);
        LinearLayout requestIcon = findViewById(R.id.requestlayout5);

        // Check if views are null (optional but good for safety)
        if (homeIcon == null || clockIcon == null || logoutIcon == null || requestIcon == null) {
            Toast.makeText(this, "Error loading layout", Toast.LENGTH_SHORT).show();
            return; // Prevent further execution if views are null
        }

        // Set click listeners
        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityClockInOutRemark.this, DashboardActivity.class);
            startActivity(intent);
        });

        logoutIcon.setOnClickListener(v -> {
            // Clear SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Sign out from Firebase Authentication
            FirebaseAuth.getInstance().signOut();

            // Redirect to the SignInActivity
            Intent intent = new Intent(ActivityClockInOutRemark.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Optionally, show a message indicating the user has logged out
            Toast.makeText(ActivityClockInOutRemark.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        clockIcon.setOnClickListener(v -> {
            Intent clockInOutIntent = new Intent(ActivityClockInOutRemark.this, SwipingTimeActivity.class);
            startActivity(clockInOutIntent);
        });

        requestIcon.setOnClickListener(v -> {
            Intent salaryDetailsIntent = new Intent(ActivityClockInOutRemark.this, SalaryDetailsActivity.class);
            startActivity(salaryDetailsIntent);
        });

        // Fetch and display username
        fetchAndDisplayUsername();

        // Retrieve date from previous activity
        String date = getIntent().getStringExtra("date");
        if (date != null) {
            tvDate.setText(date);  // Display the date in TextView
            fetchClockTimes(date);  // Fetch and display clock-in/out times
        }

        // Set predefined teams for the spinner
        String[] predefinedTeams = new String[] {"Select Team", "Team 1", "Team 2", "Team 3"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, predefinedTeams);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTeam.setAdapter(adapter);

        // Set predefined clock in/out options for the spinner
        String[] clockInOutOptions = new String[] {"Select In/Out", "Clock In", "Clock Out"};
        ArrayAdapter<String> clockAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clockInOutOptions);
        clockAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClockInOut.setAdapter(clockAdapter);

        // Set submit button click listener
        submitButton.setOnClickListener(v -> submitRemark());
    }

    private void submitRemark() {
        String remark = remarkEditText.getText().toString().trim();
        String date = tvDate.getText().toString();
        String selectedTeam = spinnerTeam.getSelectedItem().toString(); // Get selected team from spinner
        String selectedAction = spinnerClockInOut.getSelectedItem().toString(); // Get selected action ("Clock In" or "Clock Out")

        // Validate input fields
        if (remark.isEmpty()) {
            Toast.makeText(this, "Please enter a remark", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("Select In/Out".equals(selectedAction)) {
            Toast.makeText(this, "Please select a clock-in/out option", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = currentUser.getUid(); // Get the current user's UID
        DatabaseReference userRef = mDatabase.child("Users").child(currentUserId);

        // Retrieve user details and clock-in/out times
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue(String.class);
                String position = dataSnapshot.child("position").getValue(String.class);
                String email = dataSnapshot.child("email").getValue(String.class);  // Fetch email from Users node

                // Retrieve clock-in/out times or set default values if not available
                String clockInTime = dataSnapshot.child("timeRecords").child(date).child("ClockInTime").getValue(String.class);
                String clockOutTime = dataSnapshot.child("timeRecords").child(date).child("ClockOutTime").getValue(String.class);

                // If clock-in or clock-out time is missing, set to default "00:00"
                if (clockInTime == null) {
                    clockInTime = "00:00";
                }
                if (clockOutTime == null) {
                    clockOutTime = "00:00";
                }

                // Determine the node to insert based on selectedAction
                DatabaseReference targetNode;
                if ("Clock In".equals(selectedAction)) {
                    targetNode = mDatabase.child("LateInRequests").push(); // LateInRequests node
                } else {
                    targetNode = mDatabase.child("LateOutRequests").push(); // LateOutRequests node
                }

                // Prepare the data object for submission
                LateRequest lateRequest = new LateRequest(username, position, email, date, clockInTime, clockOutTime, remark, selectedTeam, currentUserId, selectedAction);

                // Save data to the target node in Firebase
                targetNode.setValue(lateRequest).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Request submitted successfully", Toast.LENGTH_SHORT).show();
                        remarkEditText.setText("");  // Clear remark field
                        finish();  // Close the current activity
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to submit request", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Method to fetch clock-in and clock-out times for the selected date
    private void fetchClockTimes(String date) {
        String currentUserId = currentUser.getUid();
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        // Make sure the date format is correct: dd-MM-yyyy
        String formattedDate = formatDate(date); // Use this method to format the date properly

        mDatabaseRef.child("Users").child(currentUserId).child("timeRecords").child(date)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       // Log.d("FirebaseResponse", "Data for date " + formattedDate + ": " + dataSnapshot.getValue());

                        if (dataSnapshot.exists()) {
                            String clockInTime = dataSnapshot.child("ClockInTime").getValue(String.class);
                            String clockOutTime = dataSnapshot.child("ClockOutTime").getValue(String.class);

                            // Log retrieved times
                            Log.d("FirebaseResponse", "Clock In Time: " + clockInTime + ", Clock Out Time: " + clockOutTime);

                            // Set default time if not found
                            if (clockInTime == null) {
                                clockInTime = "00:00";
                            }
                            if (clockOutTime == null) {
                                clockOutTime = "00:00";
                            }

                            // Update the UI
                            tvClockInTime.setText(clockInTime);
                            tvClockOutTime.setText(clockOutTime);
                        } else {
                            Log.d("FirebaseResponse", "No data found for the date: " + formattedDate);
                            Toast.makeText(ActivityClockInOutRemark.this, "No data found for this date", Toast.LENGTH_SHORT).show();
                            tvClockInTime.setText("00:00");
                            tvClockOutTime.setText("00:00");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("FirebaseError", "Error fetching data: " + databaseError.getMessage());
                        Toast.makeText(ActivityClockInOutRemark.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Helper method to format the date to match the database format
    private String formatDate(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date parsedDate = inputFormat.parse(date);
            return outputFormat.format(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return date;  // return the original date if parsing fails
        }
    }

    // Method to fetch and display username in welcome text
    private void fetchAndDisplayUsername() {
        DatabaseReference userRef = mDatabase.child("Users").child(currentUser.getUid()).child("username");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.getValue(String.class);
                    welcomeTextView.setText("Welcome, " + username + "!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ActivityClockInOutRemark.this, "Failed to fetch username", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
