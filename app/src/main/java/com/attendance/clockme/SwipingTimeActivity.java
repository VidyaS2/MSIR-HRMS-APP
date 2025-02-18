package com.attendance.clockme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SwipingTimeActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private LinearLayout cardContainer;
    private SharedPreferences sharedPreferences;
    private TextView welcomeTextView;
    private int currentMonth;
    private int currentYear;
    private static final String PREFS_NAME = "UserPrefs";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swiping_time);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        cardContainer = findViewById(R.id.cardContainer);

        // Initialize views for navigation and welcome message
        LinearLayout homeIcon = findViewById(R.id.homelayout3);
        LinearLayout clockIcon = findViewById(R.id.clocklayout3);
        LinearLayout logoutIcon = findViewById(R.id.signoutlayout3);
        LinearLayout requestIcon = findViewById(R.id.requestlayout3);
        welcomeTextView = findViewById(R.id.welcomeMessage);

        // Initialize current month and year
        Calendar calendar = Calendar.getInstance();
        currentMonth = calendar.get(Calendar.MONTH);
        currentYear = calendar.get(Calendar.YEAR);

        // Display current month and year
        displayCurrentMonthYear();

        // Fetch and display the username
        fetchAndDisplayUsername();

        // Fetch and display clock-in/out times for the current month
        fetchAndDisplayTimeRecords();

        // Get Intent data (if passed)
        Intent intent = getIntent();
        String date = intent.getStringExtra("DATE");
        String clockInTime = intent.getStringExtra("CLOCK_IN_TIME");
        String clockOutTime = intent.getStringExtra("CLOCK_OUT_TIME");

        // Optional: Display this data somewhere in your layout, such as in a Toast or in a dynamically created card.
        if (date != null && clockInTime != null && clockOutTime != null) {
            // You could create a Toast to display the details for debugging or use them for dynamic cards
            Toast.makeText(this, "Date: " + date + ", Clock-In: " + clockInTime + ", Clock-Out: " + clockOutTime, Toast.LENGTH_LONG).show();
        }

        // Set up icon click listeners
        homeIcon.setOnClickListener(v -> {
            Intent dashboardIntent = new Intent(SwipingTimeActivity.this, DashboardActivity.class);
            startActivity(dashboardIntent);
        });

        requestIcon.setOnClickListener(v -> { // Navigate to SalaryDetailsActivity
            Intent salaryDetailsIntent = new Intent(SwipingTimeActivity.this, SalaryDetailsActivity.class);
            startActivity(salaryDetailsIntent);
        });

        logoutIcon.setOnClickListener(v -> {
            // Clear SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Sign out from Firebase Authentication
            FirebaseAuth.getInstance().signOut();

            // Redirect to the SignInActivity
            Intent intentLogout = new Intent(SwipingTimeActivity.this, SignInActivity.class);
            intentLogout.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intentLogout);

            Toast.makeText(SwipingTimeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        clockIcon.setOnClickListener(v -> {
            // You can handle clock-related functionality here
        });

        // Set up month navigation
        findViewById(R.id.imageView).setOnClickListener(v -> showPreviousMonth());
        findViewById(R.id.imageView4).setOnClickListener(v -> showNextMonth());
    }



    private void fetchAndDisplayUsername() {
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null) {
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child("Users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String username = userSnapshot.child("username").getValue(String.class);
                                if (username != null) {
                                    username = username.substring(0,1).toUpperCase() + username.substring(1).toLowerCase();
                                    welcomeTextView.setText("Welcome, " + username + "!");
                                } else {
                                    Toast.makeText(SwipingTimeActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(SwipingTimeActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(SwipingTimeActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(SwipingTimeActivity.this, "User email not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(SwipingTimeActivity.this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchAndDisplayTimeRecords() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("timeRecords");

            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    cardContainer.removeAllViews(); // Clear previous views
                    if (dataSnapshot.exists()) {
                        boolean hasRecords = false;
                        for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                            String date = dateSnapshot.getKey();
                            if (isDateInCurrentMonth(date)) {
                                hasRecords = true;
                                String clockInTime = dateSnapshot.child("ClockInTime").getValue(String.class);
                                String clockOutTime = dateSnapshot.child("ClockOutTime").getValue(String.class);
                                createCardView(date, clockInTime, clockOutTime);
                            }
                        }
                        if (!hasRecords) {
                            showNoRecordsFound();
                        }
                    } else {
                        showNoRecordsFound();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(SwipingTimeActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(SwipingTimeActivity.this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNoRecordsFound() {
        TextView noRecordsTextView = new TextView(this);
        noRecordsTextView.setText("No records found");
        noRecordsTextView.setTextColor(Color.LTGRAY); // Light color for visibility
        noRecordsTextView.setTextSize(16); // Set text size
        noRecordsTextView.setPadding(16, 16, 16, 16); // Padding for better appearance
        cardContainer.addView(noRecordsTextView);
    }

    private boolean isDateInCurrentMonth(String date) {
        try {
            Date parsedDate = dateFormat.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsedDate);
            return calendar.get(Calendar.MONTH) == currentMonth && calendar.get(Calendar.YEAR) == currentYear;
        } catch (Exception e) {
            return false;
        }
    }

    private void showPreviousMonth() {
        if (currentMonth == 0) { // January
            currentMonth = 11; // December
            currentYear--;
        } else {
            currentMonth--;
        }
        displayCurrentMonthYear();
        fetchAndDisplayTimeRecords();
    }

    private void showNextMonth() {
        if (currentMonth == 11) { // December
            currentMonth = 0; // January
            currentYear++;
        } else {
            currentMonth++;
        }
        displayCurrentMonthYear();
        fetchAndDisplayTimeRecords();
    }

    private void displayCurrentMonthYear() {
        String monthName = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date(currentYear - 1900, currentMonth, 1));
        TextView currentMonthTextView = findViewById(R.id.tv_date_range);
        currentMonthTextView.setText(monthName);
    }

    private void createCardView(String date, String clockInTime, String clockOutTime) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.card_view_layout, null); // Inflate the card view layout

        // Get references to the TextViews in the card view layout
        TextView dateTextView = cardView.findViewById(R.id.dateTextView);
        TextView dayOfWeekTextView = cardView.findViewById(R.id.dayOfWeekTextView);
        TextView overlayBox = cardView.findViewById(R.id.overlayBox); // For clock-in time
        TextView overlayBoxOut = cardView.findViewById(R.id.overlayBoxOut); // For clock-out time
        TextView lateTextView = cardView.findViewById(R.id.lateTextView); // For showing lateness

        dateTextView.setText(date);

        // Convert the date string into a Date object and format it to display the day of the week
        try {
            Date parsedDate = dateFormat.parse(date); // Assuming you have a dateFormat instance
            String dayOfWeek = dayFormat.format(parsedDate); // Assuming you have a dayFormat instance
            dayOfWeekTextView.setText(dayOfWeek);
        } catch (Exception e) {
            dayOfWeekTextView.setText("Unknown");
        }

        // Set clock-in and clock-out times, defaulting to "00:00" if they are null
        overlayBox.setText(clockInTime != null ? clockInTime : "00:00");
        overlayBoxOut.setText(clockOutTime != null ? clockOutTime : "00:00");

        // Check if the user is late (after 09:05 AM)
        if (clockInTime != null) {
            try {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date referenceTime = timeFormat.parse("09:05"); // 9:05 AM as reference
                Date actualClockInTime = timeFormat.parse(clockInTime);

                if (actualClockInTime != null && actualClockInTime.after(referenceTime)) {
                    // Calculate the difference in milliseconds
                    long differenceInMilliseconds = actualClockInTime.getTime() - referenceTime.getTime();

                    // Convert milliseconds into hours, minutes, and seconds
                    long hours = (differenceInMilliseconds / (1000 * 60 * 60)) % 24;
                    long minutes = (differenceInMilliseconds / (1000 * 60)) % 60;
                    long seconds = (differenceInMilliseconds / 1000) % 60;

                    // Display the lateness in HH:mm:ss format
                    String lateness = String.format(Locale.getDefault(), "Late: %02d:%02d", hours, minutes);
                    lateTextView.setText(lateness);
                    lateTextView.setTextColor(Color.parseColor("#FF0000")); // Highlight in red
                } else {
                    // On time
                    lateTextView.setText("On Time");
                    lateTextView.setTextColor(Color.parseColor("#06830B")); // Default color (green)
                }
            } catch (Exception e) {
                lateTextView.setText("");
                lateTextView.setTextColor(Color.parseColor("#FF0000")); // Set to red in case of an error
            }
        } else {
            lateTextView.setText("Late:");
            lateTextView.setTextColor(Color.parseColor("#EF5B50")); // Default color
        }

        // Check if the date is within the active range (date + next two days)
        try {
            // Parse the date from the card
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date cardDate = dateFormat.parse(date);
            Calendar calendar = Calendar.getInstance();
            Date currentDate = calendar.getTime();

            calendar.setTime(cardDate);
            calendar.add(Calendar.DAY_OF_YEAR, 2); // Add two days to the card date
            Date lastActiveDate = calendar.getTime();

            // Dynamically fetch the current user's email from Firebase Authentication
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                // Handle case when the user is not logged in
                return; // Exit if no user is logged in
            }
            String currentUserEmail = currentUser.getEmail(); // Get the email of the current user

            // Reference to Firebase
            DatabaseReference lateInRef = FirebaseDatabase.getInstance().getReference("LateInRequests");
            DatabaseReference lateOutRef = FirebaseDatabase.getInstance().getReference("LateOutRequests");

            // Query Firebase for the user's late in requests with the matching email
            lateInRef.orderByChild("email").equalTo(currentUserEmail).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean isLateInLinkDisabled = false;

                    // If requests exist, check their statuses for LateInRequests
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String status = snapshot.child("status").getValue(String.class);
                            String requestDate = snapshot.child("date").getValue(String.class);

                            // If the date and status match, disable the link for LateIn
                            if (requestDate.equals(date) && ("approved".equalsIgnoreCase(status) || "rejected".equalsIgnoreCase(status))) {
                                isLateInLinkDisabled = true;
                                break;
                            }
                        }
                    }

                    // Check if the date is within the active range and the link should be enabled or disabled for LateIn
                    if (!isLateInLinkDisabled) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(cardDate);
                        calendar.add(Calendar.DAY_OF_MONTH, 1); // Add 1 day to cardDate
                        Date nextDay = calendar.getTime();

                        if (currentDate.compareTo(cardDate) >= 0 && currentDate.compareTo(nextDay) <= 0) {
                            // Within two days range from cardDate
                            overlayBox.setOnClickListener(v -> {
                                Intent intent = new Intent(SwipingTimeActivity.this, ActivityClockInOutRemark.class);
                                intent.putExtra("date", date); // Pass date to the next activity if needed
                                startActivity(intent);
                            });
                            overlayBox.setEnabled(true);
                            overlayBox.setTextColor(Color.BLACK); // Ensure the text is enabled color
                        }
                    } else {
                        // Disable LateIn link if status is approved/rejected
                        overlayBox.setOnClickListener(null);
                        overlayBox.setEnabled(false);
                        overlayBox.setTextColor(Color.GRAY);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any error during the Firebase query
                    overlayBox.setOnClickListener(null);
                    overlayBox.setEnabled(false);
                    overlayBox.setTextColor(Color.GRAY);
                }
            });

            // Query Firebase for the user's late out requests with the matching email
            lateOutRef.orderByChild("email").equalTo(currentUserEmail).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean isLateOutLinkDisabled = false;

                    // If requests exist, check their statuses for LateOutRequests
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String status = snapshot.child("status").getValue(String.class);
                            String requestDate = snapshot.child("date").getValue(String.class);

                            // If the date and status match, disable the link for LateOut
                            if (requestDate.equals(date) && ("approved".equalsIgnoreCase(status) || "rejected".equalsIgnoreCase(status))) {
                                isLateOutLinkDisabled = true;
                                break;
                            }
                        }
                    }

                    // Check if the date is within the active range and the link should be enabled or disabled for LateOut
                    if (!isLateOutLinkDisabled) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(cardDate);
                        calendar.add(Calendar.DAY_OF_MONTH, 1); // Add 1 day to cardDate
                        Date nextDay = calendar.getTime();

                        if (currentDate.compareTo(cardDate) >= 0 && currentDate.compareTo(nextDay) <= 0) {
                            // Within two days range from cardDate
                            overlayBoxOut.setOnClickListener(v -> {
                                Intent intent = new Intent(SwipingTimeActivity.this, ActivityClockInOutRemark.class);
                                intent.putExtra("date", date); // Pass date to the next activity if needed
                                startActivity(intent);
                            });
                            overlayBoxOut.setEnabled(true);
                            overlayBoxOut.setTextColor(Color.BLACK); // Ensure the text is enabled color
                        }
                    } else {
                        // Disable LateOut link if status is approved/rejected
                        overlayBoxOut.setOnClickListener(null);
                        overlayBoxOut.setEnabled(false);
                        overlayBoxOut.setTextColor(Color.GRAY);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any error during the Firebase query
                    overlayBoxOut.setOnClickListener(null);
                    overlayBoxOut.setEnabled(false);
                    overlayBoxOut.setTextColor(Color.GRAY);
                }
            });

        } catch (Exception e) {
            // Handle parsing exceptions
            overlayBox.setOnClickListener(null);
            overlayBox.setEnabled(false);
            overlayBox.setTextColor(Color.GRAY);

            overlayBoxOut.setOnClickListener(null);
            overlayBoxOut.setEnabled(false);
            overlayBoxOut.setTextColor(Color.GRAY);
        }


        // Add the dynamically created CardView to the container (LinearLayout)
        cardContainer.addView(cardView);
    }



    private void disableClick(TextView overlayBox, TextView overlayBoxOut) {
        overlayBox.setOnClickListener(null);
        overlayBox.setEnabled(false);
        overlayBox.setTextColor(Color.GRAY);

        overlayBoxOut.setOnClickListener(null);
        overlayBoxOut.setEnabled(false);
        overlayBoxOut.setTextColor(Color.GRAY);
    }


}
