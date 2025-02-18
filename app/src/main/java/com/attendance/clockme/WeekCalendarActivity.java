package com.attendance.clockme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WeekCalendarActivity extends AppCompatActivity {

    private TextView welcomeMessage, pr1, pr2, pr3, pr4, pr5, leave1; // Add pr5 for dates 29-31
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private static final String PREFS_NAME = "your_prefs_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_weekcalendar);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        welcomeMessage = findViewById(R.id.welcomeTextView);
        pr1 = findViewById(R.id.pr1);
        pr2 = findViewById(R.id.pr2);
        pr3 = findViewById(R.id.pr3);
        pr4 = findViewById(R.id.pr4);
        pr5 = findViewById(R.id.pr5);
        leave1 = findViewById(R.id.lv1);

        LinearLayout homeIcon = findViewById(R.id.homelayout);
        LinearLayout clockIcon = findViewById(R.id.clocklayout);
        LinearLayout logoutIcon = findViewById(R.id.signoutlayout);
        LinearLayout requestIcon = findViewById(R.id.requestlayout);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        homeIcon.setOnClickListener(v -> startActivity(new Intent(WeekCalendarActivity.this, DashboardActivity.class)));

        logoutIcon.setOnClickListener(v -> {
            // Clear SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Sign out from Firebase Authentication
            mAuth.signOut();

            // Redirect to the SignInActivity
            Intent intent = new Intent(WeekCalendarActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(WeekCalendarActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        clockIcon.setOnClickListener(v -> startActivity(new Intent(WeekCalendarActivity.this, SwipingTimeActivity.class)));

        // Set listener for requestIcon
        requestIcon.setOnClickListener(v -> startActivity(new Intent(WeekCalendarActivity.this, SalaryDetailsActivity.class)));


        loadUserDataAndDisplay();
        countClockInsForRanges();
        countLeaveFor1To7DaysCurrentMonth();
        countLeaveFor8To14DaysCurrentMonth();
        countLeaveFor15To21DaysCurrentMonth();
        countLeaveFor22To28DaysCurrentMonth();
        countLeaveFor29To31DaysCurrentMonth();
        countHolidayAndSundayFor1To7Days();
        countHolidayAndSundayFor8To14Days();
        countHolidayAndSundayFor15To21Days();
        countHolidayAndSundayFor22To28Days();
        countHolidayAndSundayFor29To31Days();
        countLateFor1To7Days();
        countLateFor8To14Days();
        countLateFor15To21Days();
        countLateFor22To28Days();
        countLateFor29To31Days();
        countOTForSundays1To7();
        countOTForSundays8To14();
        countOTForSundays15To21();
        countOTForSundays22To28();
        countOTForSundays29To31();
        countPermissionsFor1To7DaysCurrentMonth();
        countPermissionsFor8To14DaysCurrentMonth();
        countPermissionsFor15To21DaysCurrentMonth();
        countPermissionsFor22To28DaysCurrentMonth();
        countPermissionsFor29To31DaysCurrentMonth();
        countLateRequestsFor1To7DaysCurrentMonth();
        countLateRequestsFor8To14DaysCurrentMonth();
        countLateRequestsFor15To21DaysCurrentMonth();
        countLateRequestsFor22To28DaysCurrentMonth();
        countLateRequestsFor29To31DaysCurrentMonth();


    }

    // Method to load user data and display welcome message
    private void loadUserDataAndDisplay() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null) {
                DatabaseReference usersRef = mDatabase.child("Users");
                usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String username = userSnapshot.child("username").getValue(String.class);
                                if (username != null) {
                                    username = username.substring(0, 1).toUpperCase() + username.substring(1).toLowerCase();
                                    welcomeMessage.setText("Welcome, " + username + "!");
                                } else {
                                    Toast.makeText(WeekCalendarActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(WeekCalendarActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(WeekCalendarActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(WeekCalendarActivity.this, "User email not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(WeekCalendarActivity.this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to count clock-ins before 9:05 AM and clock-outs after 8:00 PM for date ranges
    private void countClockInsForRanges() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;
            int currentYear = calendar.get(Calendar.YEAR);

            DatabaseReference timeRecordsRef = mDatabase.child("Users").child(uid).child("timeRecords");

            timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    double totalCount1to7 = 0.0;
                    double totalCount8to14 = 0.0;
                    double totalCount15to21 = 0.0;
                    double totalCount22to28 = 0.0;
                    double totalCount29to31 = 0.0;  // New variable for dates 29-31

                    for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                        String dateKey = dateSnapshot.getKey();

                        try {
                            String[] dateParts = dateKey.split("-");
                            int day = Integer.parseInt(dateParts[0]);
                            int month = Integer.parseInt(dateParts[1]);
                            int year = Integer.parseInt(dateParts[2]);

                            if (year == currentYear && month == currentMonth) {
                                boolean incrementClockIn = false;
                                boolean incrementClockOut = false;

                                String clockInTime = dateSnapshot.child("ClockInTime").getValue(String.class);
                                String clockOutTime = dateSnapshot.child("ClockOutTime").getValue(String.class);

                                if (clockInTime != null) {
                                    String[] timeParts = clockInTime.split(":");
                                    int clockInHour = Integer.parseInt(timeParts[0]);
                                    int clockInMinute = Integer.parseInt(timeParts[1]);

                                    if (clockInHour < 9 || (clockInHour == 9 && clockInMinute <= 5)) {
                                        incrementClockIn = true;
                                    }
                                }

                                if (clockOutTime != null) {
                                    String[] timeParts = clockOutTime.split(":");
                                    int clockOutHour = Integer.parseInt(timeParts[0]);
                                    int clockOutMinute = Integer.parseInt(timeParts[1]);

                                    if (clockOutHour > 20 || (clockOutHour == 20 && clockOutMinute >= 0)) {
                                        incrementClockOut = true;
                                    }
                                }

                                if (day >= 1 && day <= 7) {
                                    if (incrementClockIn) {
                                        totalCount1to7 += 0.5;
                                    }
                                    if (incrementClockOut) {
                                        totalCount1to7 += 0.5;
                                    }
                                } else if (day >= 8 && day <= 14) {
                                    if (incrementClockIn) {
                                        totalCount8to14 += 0.5;
                                    }
                                    if (incrementClockOut) {
                                        totalCount8to14 += 0.5;
                                    }
                                } else if (day >= 15 && day <= 21) {
                                    if (incrementClockIn) {
                                        totalCount15to21 += 0.5;
                                    }
                                    if (incrementClockOut) {
                                        totalCount15to21 += 0.5;
                                    }
                                } else if (day >= 22 && day <= 28) {
                                    if (incrementClockIn) {
                                        totalCount22to28 += 0.5;
                                    }
                                    if (incrementClockOut) {
                                        totalCount22to28 += 0.5;
                                    }
                                } else if (day >= 29 && day <= 31) {  // New range for dates 29-31
                                    if (incrementClockIn) {
                                        totalCount29to31 += 0.5;
                                    }
                                    if (incrementClockOut) {
                                        totalCount29to31 += 0.5;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e("DateParsingError", "Error parsing date key: " + dateKey, e);
                        }
                    }

                    pr1.setText(String.format(Locale.getDefault(), "%.1f", totalCount1to7));
                    pr2.setText(String.format(Locale.getDefault(), "%.1f", totalCount8to14));
                    pr3.setText(String.format(Locale.getDefault(), "%.1f", totalCount15to21));
                    pr4.setText(String.format(Locale.getDefault(), "%.1f", totalCount22to28));
                    pr5.setText(String.format(Locale.getDefault(), "%.1f", totalCount29to31));  // Update pr5 TextView

                    Log.d("ClockInTime", "Count for dates 1-7: " + totalCount1to7 +
                            ", Count for dates 8-14: " + totalCount8to14 +
                            ", Count for dates 15-21: " + totalCount15to21 +
                            ", Count for dates 22-28: " + totalCount22to28 +
                            ", Count for dates 29-31: " + totalCount29to31);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countLeaveFor1To7DaysCurrentMonth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month, year, and day of the month
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the user's time records in Firebase
            DatabaseReference timeRecordsRef = mDatabase.child("Users").child(uid).child("timeRecords");

            // Fetch the data for the time records
            timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    double totalLeave = 0.0;

                    // Loop through dates 1 to 7
                    for (int day = 1; day <= 7; day++) {
                        // Skip future dates
                        if (day > currentDay) {
                            continue;
                        }

                        String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, currentMonth, currentYear);
                        double leaveForDay = 0.0;

                        // Exclude Sundays
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.set(currentYear, currentMonth - 1, day); // Set the date for comparison
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                        // Skip if the date is a Sunday (Sunday is 1 in Calendar)
                        if (dayOfWeek == Calendar.SUNDAY) {
                            continue;
                        }

                        // Check if data for the date exists in Firebase
                        if (!dataSnapshot.hasChild(dateKey)) {
                            // If the date is missing in Firebase, count as leave (0.5 for clock-in, 0.5 for clock-out)
                            leaveForDay = 1.0;
                        } else {
                            // Get ClockInTime and ClockOutTime from Firebase
                            String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                            String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                            // Check if ClockInTime is missing or marked as "holiday"
                            if (clockInTime == null) {
                                leaveForDay += 0.5;  // Count 0.5 leave if ClockInTime is missing or marked as holiday
                            }

                            // Check if ClockOutTime is missing or marked as "holiday"
                            if (clockOutTime == null) {
                                leaveForDay += 0.5;  // Count 0.5 leave if ClockOutTime is missing or marked as holiday
                            }

                            // Check if ClockOutTime is available and is before 20:00
                            if (clockOutTime != null && !clockOutTime.equals("holiday")) {
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    Date clockOutDate = sdf.parse(clockOutTime);
                                    Date cutoffDate = sdf.parse("20:00");

                                    if (clockOutDate != null && clockOutDate.before(cutoffDate)) {
                                        leaveForDay += 0.5;  // Count additional 0.5 leave if ClockOutTime is before 20:00
                                    }
                                } catch (ParseException e) {
                                    Log.e("LeaveCount", "Error parsing ClockOutTime", e);
                                }
                            }
                        }

                        // Add the leave count for this day
                        totalLeave += leaveForDay;
                    }

                    // Update TextView for leave count 1-7 of the current month
                    TextView leaveTextView = findViewById(R.id.lv1); // Ensure your TextView ID is correct
                    leaveTextView.setText(String.format(Locale.getDefault(), "%.1f", totalLeave));

                    // Log the result for debugging
                    Log.d("LeaveCount", "Leave count for dates 1-7 of current month: " + totalLeave);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countLeaveFor8To14DaysCurrentMonth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month, year, and day of the month
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1; // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the user's time records in Firebase
            DatabaseReference timeRecordsRef = mDatabase.child("Users").child(uid).child("timeRecords");

            // Fetch the data for the time records
            timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    double totalLeave = 0.0;

                    // Loop through dates 8 to 14
                    for (int day = 8; day <= 14; day++) {
                        // Skip future dates
                        if (day > currentDay) {
                            continue;
                        }

                        String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, currentMonth, currentYear);
                        double leaveForDay = 0.0;

                        // Exclude Sundays
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.set(currentYear, currentMonth - 1, day); // Set the date for comparison
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                        // Skip if the date is a Sunday (Sunday is 1 in Calendar)
                        if (dayOfWeek == Calendar.SUNDAY) {
                            continue;
                        }

                        // Check if data for the date exists in Firebase
                        if (!dataSnapshot.hasChild(dateKey)) {
                            // If the date is missing in Firebase, count as leave (0.5 for clock-in, 0.5 for clock-out)
                            leaveForDay = 1.0;
                        } else {
                            // Get ClockInTime and ClockOutTime from Firebase
                            String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                            String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                            // Check if ClockInTime is missing
                            if (clockInTime == null) {
                                leaveForDay += 0.5;  // Count 0.5 leave if ClockInTime is missing or marked as holiday
                            }

                            // Check if ClockOutTime is missing
                            if (clockOutTime == null) {
                                leaveForDay += 0.5;  // Count 0.5 leave if ClockOutTime is missing or marked as holiday
                            }

                            // Check if ClockOutTime is available and is before 20:00
                            if (clockOutTime != null && !"holiday".equals(clockOutTime)) {
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    Date clockOutDate = sdf.parse(clockOutTime);
                                    Date cutoffDate = sdf.parse("20:00");

                                    if (clockOutDate != null && clockOutDate.before(cutoffDate)) {
                                        leaveForDay += 0.5;  // Count additional 0.5 leave if ClockOutTime is before 20:00
                                    }
                                } catch (ParseException e) {
                                    Log.e("LeaveCount", "Error parsing ClockOutTime", e);
                                }
                            }
                        }

                        // Add the leave count for this day
                        totalLeave += leaveForDay;
                    }

                    // Update TextView for leave count 8-14 of current month
                    TextView leaveTextView = findViewById(R.id.lv2); // Ensure your TextView ID is correct
                    leaveTextView.setText(String.format(Locale.getDefault(), "%.1f", totalLeave));

                    // Log the result for debugging
                    Log.d("LeaveCount", "Leave count for dates 8-14 of current month: " + totalLeave);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countLeaveFor15To21DaysCurrentMonth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month, year, and day of the month
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the user's time records in Firebase
            DatabaseReference timeRecordsRef = mDatabase.child("Users").child(uid).child("timeRecords");

            // Fetch the data for the time records
            timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    double totalLeave = 0.0;

                    // Loop through dates 15 to 21
                    for (int day = 15; day <= 21; day++) {
                        // Skip future dates (greater than today's date)
                        if (day > currentDay) {
                            continue;
                        }

                        String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, currentMonth, currentYear);
                        boolean isLeaveForDay = false;

                        // Exclude Sundays
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.set(currentYear, currentMonth - 1, day); // Set the date for comparison
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                        // Skip if the date is a Sunday
                        if (dayOfWeek == Calendar.SUNDAY) {
                            continue;
                        }

                        // Check if data for the date exists in Firebase
                        if (!dataSnapshot.hasChild(dateKey)) {
                            // If the date is missing in Firebase, count as leave (0.5)
                            isLeaveForDay = true;
                        } else {
                            // Get ClockInTime and ClockOutTime from Firebase
                            String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                            String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                            // Check if either ClockInTime or ClockOutTime is missing or set as 'holiday'
                            if (clockInTime == null) {
                                isLeaveForDay = true;  // Count as leave if ClockInTime is missing or holiday
                            }

                            // Check if ClockOutTime is missing
                            if (clockOutTime == null) {
                                isLeaveForDay = true;  // Count as leave if ClockOutTime is missing or holiday
                            } else {
                                // Check if ClockInTime is missing or ClockOutTime is before 20:00
                                if (clockInTime == null || clockOutTime == null) {
                                    isLeaveForDay = true;  // Count as leave if either is missing
                                } else {
                                    try {
                                        // Parse ClockOutTime to check if it is before 20:00
                                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                        Date clockOutDate = sdf.parse(clockOutTime);
                                        Date cutoffDate = sdf.parse("20:00");

                                        if (clockOutDate != null && clockOutDate.before(cutoffDate)) {
                                            isLeaveForDay = true; // Count as leave if ClockOutTime is before 20:00
                                        }
                                    } catch (ParseException e) {
                                        Log.e("LeaveCount", "Error parsing ClockOutTime", e);
                                    }
                                }
                            }
                        }

                        // If the date qualifies as leave, add 0.5
                        if (isLeaveForDay) {
                            totalLeave += 0.5;
                        }
                    }

                    // Update TextView for leave count 15-21 of current month
                    TextView leaveTextView = findViewById(R.id.lv3); // Ensure your TextView ID is correct
                    leaveTextView.setText(String.format(Locale.getDefault(), "%.1f", totalLeave));

                    // Log the result for debugging
                    Log.d("LeaveCount", "Leave count for dates 15-21 of current month: " + totalLeave);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void countLeaveFor22To28DaysCurrentMonth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the user's time records in Firebase
            DatabaseReference timeRecordsRef = mDatabase.child("Users").child(uid).child("timeRecords");

            // Fetch the data for the time records
            timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    double totalLeave = 0.0;

                    // Loop through dates 22 to 28
                    for (int day = 22; day <= 28; day++) {
                        // Skip future dates
                        if (day > currentDay) {
                            continue;
                        }

                        String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, currentMonth, currentYear);
                        boolean isLeaveForDay = false;

                        // Exclude Sundays
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.set(currentYear, currentMonth - 1, day); // Set the date for comparison
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                        // Skip if the date is a Sunday (Sunday is 1 in Calendar)
                        if (dayOfWeek == Calendar.SUNDAY) {
                            continue;
                        }

                        // Check if data for the date exists in Firebase
                        if (!dataSnapshot.hasChild(dateKey)) {
                            // If the date is missing in Firebase, count as leave (0.5)
                            isLeaveForDay = true;
                        } else {
                            // Get ClockInTime and ClockOutTime from Firebase
                            String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                            String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                            // Check if ClockInTime is missing
                            if (clockInTime == null) {
                                isLeaveForDay = true;  // Count as leave if ClockInTime is missing or holiday
                            }

                            // Check if ClockOutTime is missing
                            if (clockOutTime == null) {
                                isLeaveForDay = true;  // Count as leave if ClockOutTime is missing or holiday
                            }

                            // Check if ClockOutTime is available and is before 20:00
                            if (clockOutTime != null && !"holiday".equals(clockOutTime)) {
                                try {
                                    // Parse the ClockOutTime to compare the time
                                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    Date clockOutDate = sdf.parse(clockOutTime);
                                    Date cutoffDate = sdf.parse("20:00");

                                    if (clockOutDate != null && clockOutDate.before(cutoffDate)) {
                                        isLeaveForDay = true; // Count as leave if ClockOutTime is before 20:00
                                    }
                                } catch (ParseException e) {
                                    Log.e("LeaveCount", "Error parsing ClockOutTime", e);
                                }
                            }
                        }

                        // If the date qualifies as leave, add 0.5
                        if (isLeaveForDay) {
                            totalLeave += 0.5;
                        }
                    }

                    // Update TextView for leave count 22-28 of current month
                    TextView leaveTextView = findViewById(R.id.lv4); // Use the correct TextView ID (lv4)
                    leaveTextView.setText(String.format(Locale.getDefault(), "%.1f", totalLeave));

                    // Log the result for debugging
                    Log.d("LeaveCount", "Leave count for dates 22-28 of current month: " + totalLeave);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countLeaveFor29To31DaysCurrentMonth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the user's time records in Firebase
            DatabaseReference timeRecordsRef = mDatabase.child("Users").child(uid).child("timeRecords");

            // Fetch the data for the time records
            timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    double totalLeave = 0.0;

                    // Loop through dates 29 to 31
                    for (int day = 29; day <= 31; day++) {
                        // Skip future dates
                        if (day > currentDay) {
                            continue;
                        }

                        // Check if the current month has the 31st day
                        if (day == 31 && (currentMonth == 2 || currentMonth == 4 || currentMonth == 6 || currentMonth == 9 || currentMonth == 11)) {
                            // Skip if the month does not have a 31st day
                            break;
                        }

                        String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, currentMonth, currentYear);
                        boolean isLeaveForDay = false;

                        // Exclude Sundays
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.set(currentYear, currentMonth - 1, day); // Set the date for comparison
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                        // Skip if the date is a Sunday (Sunday is 1 in Calendar)
                        if (dayOfWeek == Calendar.SUNDAY) {
                            continue;
                        }

                        // Check if data for the date exists in Firebase
                        if (!dataSnapshot.hasChild(dateKey)) {
                            // If the date is missing in Firebase, count as leave (0.5)
                            isLeaveForDay = true;
                        } else {
                            // Get ClockInTime and ClockOutTime from Firebase
                            String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                            String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                            // Check if ClockInTime is missing
                            if (clockInTime == null) {
                                isLeaveForDay = true;  // Count as leave if ClockInTime is missing or holiday
                            }

                            // Check if ClockOutTime is missing
                            if (clockOutTime == null) {
                                isLeaveForDay = true;  // Count as leave if ClockOutTime is missing or holiday
                            }

                            // Check if ClockOutTime is available and is before 20:00
                            if (clockOutTime != null && !"holiday".equals(clockOutTime)) {
                                try {
                                    // Parse the ClockOutTime to compare the time
                                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    Date clockOutDate = sdf.parse(clockOutTime);
                                    Date cutoffDate = sdf.parse("20:00");

                                    if (clockOutDate != null && clockOutDate.before(cutoffDate)) {
                                        isLeaveForDay = true; // Count as leave if ClockOutTime is before 20:00
                                    }
                                } catch (ParseException e) {
                                    Log.e("LeaveCount", "Error parsing ClockOutTime", e);
                                }
                            }
                        }

                        // If the date qualifies as leave, add 0.5
                        if (isLeaveForDay) {
                            totalLeave += 0.5;
                        }
                    }

                    // Update TextView for leave count 29-31 of current month
                    TextView leaveTextView = findViewById(R.id.lv5); // Use the correct TextView ID (lv5)
                    leaveTextView.setText(String.format(Locale.getDefault(), "%.1f", totalLeave));

                    // Log the result for debugging
                    Log.d("LeaveCount", "Leave count for dates 29-31 of current month: " + totalLeave);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void countHolidayAndSundayFor1To7Days() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the user's time records in Firebase
            DatabaseReference timeRecordsRef = mDatabase.child("Users").child(uid).child("timeRecords");

            // Fetch the data for the time records
            timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    double totalHolidayAndSunday = 0.0; // Variable to store the total count

                    // Loop through dates 1 to 7
                    for (int day = 1; day <= 7; day++) {
                        // Skip future dates (ensure we don't count for days after the current date)
                        if (day > currentDay) {
                            continue;
                        }

                        // Create the date key for the specific day in the current month and year
                        String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, currentMonth, currentYear);

                        // Check if the date is in the current month and year
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.set(currentYear, currentMonth - 1, day); // Set the date for comparison
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                        // Initialize flags for Sunday and holiday
                        boolean isSundayForDay = (dayOfWeek == Calendar.SUNDAY);
                        double holidayCountForDay = 0.0;  // We will calculate the holiday count for each day

                        // Check if data for the date exists in Firebase
                        if (dataSnapshot.hasChild(dateKey)) {
                            // Get ClockInTime and ClockOutTime from Firebase
                            String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                            String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                            // Check if ClockInTime or ClockOutTime is "holiday" and calculate accordingly
                            if ("holiday".equals(clockInTime)) {
                                holidayCountForDay += 0.5;  // Add 0.5 if ClockInTime is "holiday"
                            }
                            if ("holiday".equals(clockOutTime)) {
                                holidayCountForDay += 0.5;  // Add 0.5 if ClockOutTime is "holiday"
                            }
                        }

                        // If it's Sunday, count as 1.0
                        if (isSundayForDay) {
                            totalHolidayAndSunday += 1.0;
                        }

                        // Add the holiday count for the day (if applicable)
                        totalHolidayAndSunday += holidayCountForDay;
                    }

                    // Update TextView for holiday and Sunday count for dates 1 to 7
                    TextView holidayAndSundayTextView = findViewById(R.id.hd1); // Use the correct TextView ID (hd1)
                    holidayAndSundayTextView.setText(String.format(Locale.getDefault(), "%.1f", totalHolidayAndSunday));

                    // Log the result for debugging
                    Log.d("HolidayAndSundayCount", "Holiday and Sunday count for dates 1-7 of current month: " + totalHolidayAndSunday);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countHolidayAndSundayFor8To14Days() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the user's time records in Firebase
            DatabaseReference timeRecordsRef = mDatabase.child("Users").child(uid).child("timeRecords");

            // Fetch the data for the time records
            timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    double totalHolidayAndSunday = 0.0; // Variable to store the total count for days 8 to 14

                    // Loop through dates 8 to 14
                    for (int day = 8; day <= 14; day++) {
                        // Skip future dates (ensure we don't count for days after the current date)
                        if (day > currentDay) {
                            continue;
                        }

                        // Create the date key for the specific day in the current month and year
                        String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, currentMonth, currentYear);

                        // Check if the date is in the current month and year
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.set(currentYear, currentMonth - 1, day); // Set the date for comparison
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                        // Initialize flags for Sunday and holiday
                        boolean isSundayForDay = (dayOfWeek == Calendar.SUNDAY);
                        double holidayCountForDay = 0.0;  // We will calculate the holiday count for each day

                        // Check if data for the date exists in Firebase
                        if (dataSnapshot.hasChild(dateKey)) {
                            // Get ClockInTime and ClockOutTime from Firebase
                            String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                            String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                            // Check if ClockInTime or ClockOutTime is "holiday" and calculate accordingly
                            if ("holiday".equals(clockInTime)) {
                                holidayCountForDay += 0.5;  // Add 0.5 if ClockInTime is "holiday"
                            }
                            if ("holiday".equals(clockOutTime)) {
                                holidayCountForDay += 0.5;  // Add 0.5 if ClockOutTime is "holiday"
                            }
                        }

                        // If it's Sunday, count as 1.0
                        if (isSundayForDay) {
                            totalHolidayAndSunday += 1.0;
                        }

                        // Add the holiday count for the day (if applicable)
                        totalHolidayAndSunday += holidayCountForDay;
                    }

                    // Update TextView for holiday and Sunday count for dates 8 to 14
                    TextView holidayAndSundayTextView = findViewById(R.id.hd2); // Use the correct TextView ID (hd2)
                    holidayAndSundayTextView.setText(String.format(Locale.getDefault(), "%.1f", totalHolidayAndSunday));

                    // Log the result for debugging
                    Log.d("HolidayAndSundayCount", "Holiday and Sunday count for dates 8-14 of current month: " + totalHolidayAndSunday);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countHolidayAndSundayFor15To21Days() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the user's time records in Firebase
            DatabaseReference timeRecordsRef = mDatabase.child("Users").child(uid).child("timeRecords");

            // Fetch the data for the time records
            timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    double totalHolidayAndSunday = 0.0; // Variable to store the total count for days 15 to 21

                    // Loop through dates 15 to 21
                    for (int day = 15; day <= 21; day++) {
                        // Skip future dates (ensure we don't count for days after the current date)
                        if (day > currentDay) {
                            continue;
                        }

                        // Create the date key for the specific day in the current month and year
                        String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, currentMonth, currentYear);

                        // Check if the date is in the current month and year
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.set(currentYear, currentMonth - 1, day); // Set the date for comparison
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                        // Initialize flags for Sunday and holiday
                        boolean isSundayForDay = (dayOfWeek == Calendar.SUNDAY);
                        double holidayCountForDay = 0.0;  // We will calculate the holiday count for each day

                        // Check if data for the date exists in Firebase
                        if (dataSnapshot.hasChild(dateKey)) {
                            // Get ClockInTime and ClockOutTime from Firebase
                            String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                            String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                            // Check if ClockInTime or ClockOutTime is "holiday" and calculate accordingly
                            if ("holiday".equals(clockInTime)) {
                                holidayCountForDay += 0.5;  // Add 0.5 if ClockInTime is "holiday"
                            }
                            if ("holiday".equals(clockOutTime)) {
                                holidayCountForDay += 0.5;  // Add 0.5 if ClockOutTime is "holiday"
                            }
                        }

                        // If it's Sunday, count as 1.0
                        if (isSundayForDay) {
                            totalHolidayAndSunday += 1.0;
                        }

                        // Add the holiday count for the day (if applicable)
                        totalHolidayAndSunday += holidayCountForDay;
                    }

                    // Update TextView for holiday and Sunday count for dates 15 to 21
                    TextView holidayAndSundayTextView = findViewById(R.id.hd3); // Use the correct TextView ID (hd3)
                    holidayAndSundayTextView.setText(String.format(Locale.getDefault(), "%.1f", totalHolidayAndSunday));

                    // Log the result for debugging
                    Log.d("HolidayAndSundayCount", "Holiday and Sunday count for dates 15-21 of current month: " + totalHolidayAndSunday);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countHolidayAndSundayFor22To28Days() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the user's time records in Firebase
            DatabaseReference timeRecordsRef = mDatabase.child("Users").child(uid).child("timeRecords");

            // Fetch the data for the time records
            timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    double totalHolidayAndSunday = 0.0; // Variable to store the total count for days 22 to 28

                    // Loop through dates 22 to 28
                    for (int day = 22; day <= 28; day++) {
                        // Skip future dates (ensure we don't count for days after the current date)
                        if (day > currentDay) {
                            continue;
                        }

                        // Create the date key for the specific day in the current month and year
                        String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, currentMonth, currentYear);

                        // Check if the date is in the current month and year
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.set(currentYear, currentMonth - 1, day); // Set the date for comparison
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                        // Initialize flags for Sunday and holiday
                        boolean isSundayForDay = (dayOfWeek == Calendar.SUNDAY);
                        double holidayCountForDay = 0.0;  // We will calculate the holiday count for each day

                        // Check if data for the date exists in Firebase
                        if (dataSnapshot.hasChild(dateKey)) {
                            // Get ClockInTime and ClockOutTime from Firebase
                            String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                            String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                            // Check if ClockInTime or ClockOutTime is "holiday" and calculate accordingly
                            if ("holiday".equals(clockInTime)) {
                                holidayCountForDay += 0.5;  // Add 0.5 if ClockInTime is "holiday"
                            }
                            if ("holiday".equals(clockOutTime)) {
                                holidayCountForDay += 0.5;  // Add 0.5 if ClockOutTime is "holiday"
                            }
                        }

                        // If it's Sunday, count as 1.0
                        if (isSundayForDay) {
                            totalHolidayAndSunday += 1.0;
                        }

                        // Add the holiday count for the day (if applicable)
                        totalHolidayAndSunday += holidayCountForDay;
                    }

                    // Update TextView for holiday and Sunday count for dates 22 to 28
                    TextView holidayAndSundayTextView = findViewById(R.id.hd4); // Use the correct TextView ID (hd4)
                    holidayAndSundayTextView.setText(String.format(Locale.getDefault(), "%.1f", totalHolidayAndSunday));

                    // Log the result for debugging
                    Log.d("HolidayAndSundayCount", "Holiday and Sunday count for dates 22-28 of current month: " + totalHolidayAndSunday);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countHolidayAndSundayFor29To31Days() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the user's time records in Firebase
            DatabaseReference timeRecordsRef = mDatabase.child("Users").child(uid).child("timeRecords");

            // Fetch the data for the time records
            timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Check if the current month has 31 days
                    boolean has31stDay = false;
                    if (currentMonth == Calendar.JANUARY || currentMonth == Calendar.MARCH || currentMonth == Calendar.MAY || currentMonth == Calendar.JULY ||
                            currentMonth == Calendar.AUGUST || currentMonth == Calendar.OCTOBER || currentMonth == Calendar.DECEMBER) {
                        has31stDay = true;
                    }

                    double totalHolidayAndSunday = 0.0; // Variable to store the total count for days 29 to 31

                    // Loop through dates 29 to 31, checking if the day exists in the current month
                    for (int day = 29; day <= 31; day++) {
                        // Skip future dates or non-existent days in the current month
                        if (day > currentDay || (day == 31 && !has31stDay)) {
                            continue;
                        }

                        // Create the date key for the specific day in the current month and year
                        String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, currentMonth, currentYear);

                        // Check if the date is in the current month and year
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.set(currentYear, currentMonth - 1, day); // Set the date for comparison
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                        // Initialize flags for Sunday and holiday
                        boolean isSundayForDay = (dayOfWeek == Calendar.SUNDAY);
                        double holidayCountForDay = 0.0;  // We will calculate the holiday count for each day

                        // Check if data for the date exists in Firebase
                        if (dataSnapshot.hasChild(dateKey)) {
                            // Get ClockInTime and ClockOutTime from Firebase
                            String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                            String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                            // Check if ClockInTime or ClockOutTime is "holiday" and calculate accordingly
                            if ("holiday".equals(clockInTime)) {
                                holidayCountForDay += 0.5;  // Add 0.5 if ClockInTime is "holiday"
                            }
                            if ("holiday".equals(clockOutTime)) {
                                holidayCountForDay += 0.5;  // Add 0.5 if ClockOutTime is "holiday"
                            }
                        }

                        // If it's Sunday, count as 1.0
                        if (isSundayForDay) {
                            totalHolidayAndSunday += 1.0;
                        }

                        // Add the holiday count for the day (if applicable)
                        totalHolidayAndSunday += holidayCountForDay;
                    }

                    // Update TextView for holiday and Sunday count for dates 29 to 31
                    TextView holidayAndSundayTextView = findViewById(R.id.hd5); // Use the correct TextView ID (hd5)
                    holidayAndSundayTextView.setText(String.format(Locale.getDefault(), "%.1f", totalHolidayAndSunday));

                    // Log the result for debugging
                    Log.d("HolidayAndSundayCount", "Holiday and Sunday count for dates 29-31 of current month: " + totalHolidayAndSunday);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countLateFor1To7Days() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the user's time records in Firebase
            DatabaseReference timeRecordsRef = mDatabase.child("Users").child(uid).child("timeRecords");

            // Fetch the data for the time records
            timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int lateCount = 0; // Variable to store the total late count

                    // Loop through dates 1 to 7
                    for (int day = 1; day <= 7; day++) {
                        // Skip future dates (ensure we don't count for days after the current date)
                        if (day > currentDay) {
                            continue;
                        }

                        // Create the date key for the specific day in the current month and year
                        String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, currentMonth, currentYear);

                        // Check if the date exists in the user's time records
                        if (dataSnapshot.hasChild(dateKey)) {
                            // Get the ClockInTime from Firebase
                            String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);

                            // Check if ClockInTime exists and is after 09:05
                            if (clockInTime != null) {
                                // Assuming ClockInTime is in "HH:mm" format (e.g., "09:10", "09:05", "08:45")
                                try {
                                    // Define the threshold time of 09:05
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    Date clockInDate = timeFormat.parse(clockInTime);
                                    Date thresholdTime = timeFormat.parse("09:05");

                                    // Check if the ClockInTime is after 09:05
                                    if (clockInDate != null && clockInDate.after(thresholdTime)) {
                                        lateCount++; // Increment late count if ClockInTime is after 09:05
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    // Update the TextView for late count
                    TextView lateCountTextView = findViewById(R.id.laps1); // Use the correct TextView ID (laps1)
                    lateCountTextView.setText(String.format(Locale.getDefault(), "%d", lateCount));

                    // Log the result for debugging
                    Log.d("LateCount", "Late count for dates 1-7 of current month: " + lateCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countLateFor8To14Days() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the user's time records in Firebase
            DatabaseReference timeRecordsRef = mDatabase.child("Users").child(uid).child("timeRecords");

            // Fetch the data for the time records
            timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int lateCount = 0; // Variable to store the total late count for dates 8 to 14

                    // Loop through dates 8 to 14
                    for (int day = 8; day <= 14; day++) {
                        // Skip future dates (ensure we don't count for days after the current date)
                        if (day > currentDay) {
                            continue;
                        }

                        // Create the date key for the specific day in the current month and year
                        String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, currentMonth, currentYear);

                        // Check if the date exists in the user's time records
                        if (dataSnapshot.hasChild(dateKey)) {
                            // Get the ClockInTime from Firebase
                            String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);

                            // Check if ClockInTime exists and is after 09:05
                            if (clockInTime != null) {
                                // Assuming ClockInTime is in "HH:mm" format (e.g., "09:10", "09:05", "08:45")
                                try {
                                    // Define the threshold time of 09:05
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    Date clockInDate = timeFormat.parse(clockInTime);
                                    Date thresholdTime = timeFormat.parse("09:05");

                                    // Check if the ClockInTime is after 09:05
                                    if (clockInDate != null && clockInDate.after(thresholdTime)) {
                                        lateCount++; // Increment late count if ClockInTime is after 09:05
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    // Update the TextView for late count for dates 8 to 14
                    TextView lateCountTextView = findViewById(R.id.laps2); // Use the correct TextView ID (laps2)
                    lateCountTextView.setText(String.format(Locale.getDefault(), "%d", lateCount));

                    // Log the result for debugging
                    Log.d("LateCount", "Late count for dates 8-14 of current month: " + lateCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countLateFor15To21Days() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the user's time records in Firebase
            DatabaseReference timeRecordsRef = mDatabase.child("Users").child(uid).child("timeRecords");

            // Fetch the data for the time records
            timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int lateCount = 0; // Variable to store the total late count for dates 15 to 21

                    // Loop through dates 15 to 21
                    for (int day = 15; day <= 21; day++) {
                        // Skip future dates (ensure we don't count for days after the current date)
                        if (day > currentDay) {
                            continue;
                        }

                        // Create the date key for the specific day in the current month and year
                        String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, currentMonth, currentYear);

                        // Check if the date exists in the user's time records
                        if (dataSnapshot.hasChild(dateKey)) {
                            // Get the ClockInTime from Firebase
                            String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);

                            // Check if ClockInTime exists and is after 09:05
                            if (clockInTime != null) {
                                // Assuming ClockInTime is in "HH:mm" format (e.g., "09:10", "09:05", "08:45")
                                try {
                                    // Define the threshold time of 09:05
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    Date clockInDate = timeFormat.parse(clockInTime);
                                    Date thresholdTime = timeFormat.parse("09:05");

                                    // Check if the ClockInTime is after 09:05
                                    if (clockInDate != null && clockInDate.after(thresholdTime)) {
                                        lateCount++; // Increment late count if ClockInTime is after 09:05
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    // Update the TextView for late count for dates 15 to 21
                    TextView lateCountTextView = findViewById(R.id.laps3); // Use the correct TextView ID (laps3)
                    lateCountTextView.setText(String.format(Locale.getDefault(), "%d", lateCount));

                    // Log the result for debugging
                    Log.d("LateCount", "Late count for dates 15-21 of current month: " + lateCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countLateFor22To28Days() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the user's time records in Firebase
            DatabaseReference timeRecordsRef = mDatabase.child("Users").child(uid).child("timeRecords");

            // Fetch the data for the time records
            timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int lateCount = 0; // Variable to store the total late count for dates 22 to 28

                    // Loop through dates 22 to 28
                    for (int day = 22; day <= 28; day++) {
                        // Skip future dates (ensure we don't count for days after the current date)
                        if (day > currentDay) {
                            continue;
                        }

                        // Create the date key for the specific day in the current month and year
                        String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, currentMonth, currentYear);

                        // Check if the date exists in the user's time records
                        if (dataSnapshot.hasChild(dateKey)) {
                            // Get the ClockInTime from Firebase
                            String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);

                            // Check if ClockInTime exists and is after 09:05
                            if (clockInTime != null) {
                                // Assuming ClockInTime is in "HH:mm" format (e.g., "09:10", "09:05", "08:45")
                                try {
                                    // Define the threshold time of 09:05
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    Date clockInDate = timeFormat.parse(clockInTime);
                                    Date thresholdTime = timeFormat.parse("09:05");

                                    // Check if the ClockInTime is after 09:05
                                    if (clockInDate != null && clockInDate.after(thresholdTime)) {
                                        lateCount++; // Increment late count if ClockInTime is after 09:05
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    // Update the TextView for late count for dates 22 to 28
                    TextView lateCountTextView = findViewById(R.id.laps4); // Use the correct TextView ID (laps4)
                    lateCountTextView.setText(String.format(Locale.getDefault(), "%d", lateCount));

                    // Log the result for debugging
                    Log.d("LateCount", "Late count for dates 22-28 of current month: " + lateCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countLateFor29To31Days() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Check if the current month has a 31st
            boolean has31stDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) == 31;

            // Reference to the user's time records in Firebase
            DatabaseReference timeRecordsRef = mDatabase.child("Users").child(uid).child("timeRecords");

            // Fetch the data for the time records
            timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int lateCount = 0; // Variable to store the total late count for dates 29 to 31

                    // Loop through dates 29 to 31, ensuring we don't go beyond the current day
                    for (int day = 29; day <= (has31stDay ? 31 : currentDay); day++) {
                        // Skip future dates (ensure we don't count for days after the current date)
                        if (day > currentDay) {
                            continue;
                        }

                        // Create the date key for the specific day in the current month and year
                        String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, currentMonth, currentYear);

                        // Check if the date exists in the user's time records
                        if (dataSnapshot.hasChild(dateKey)) {
                            // Get the ClockInTime from Firebase
                            String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);

                            // Check if ClockInTime exists and is after 09:05
                            if (clockInTime != null) {
                                // Assuming ClockInTime is in "HH:mm" format (e.g., "09:10", "09:05", "08:45")
                                try {
                                    // Define the threshold time of 09:05
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    Date clockInDate = timeFormat.parse(clockInTime);
                                    Date thresholdTime = timeFormat.parse("09:05");

                                    // Check if the ClockInTime is after 09:05
                                    if (clockInDate != null && clockInDate.after(thresholdTime)) {
                                        lateCount++; // Increment late count if ClockInTime is after 09:05
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    // Update the TextView for late count for dates 29 to 31
                    TextView lateCountTextView = findViewById(R.id.laps5); // Use the correct TextView ID (laps5)
                    lateCountTextView.setText(String.format(Locale.getDefault(), "%d", lateCount));

                    // Log the result for debugging
                    Log.d("LateCount", "Late count for dates 29-31 of current month: " + lateCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countOTForSundays1To7() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the user's time records in Firebase
            DatabaseReference timeRecordsRef = mDatabase.child("Users").child(uid).child("timeRecords");

            // Fetch the data for the time records
            timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    double otCount = 0.0; // Variable to store the total OT count for Sundays

                    // Loop through days 1 to 7
                    for (int day = 1; day <= 7; day++) {
                        // Create the date key for the specific day in the current month and year
                        String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, currentMonth, currentYear);

                        // Check if the date exists in the user's time records
                        if (dataSnapshot.hasChild(dateKey)) {
                            // Get the ClockInTime and ClockOutTime from Firebase
                            String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                            String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                            // Check if the date is a Sunday (day of the week = Sunday)
                            Calendar dateCalendar = Calendar.getInstance();
                            dateCalendar.set(currentYear, currentMonth - 1, day); // Set the date for comparison
                            int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK); // Get day of the week

                            // If it's a Sunday (Sunday is 1 in Calendar class)
                            if (dayOfWeek == Calendar.SUNDAY) {
                                // If ClockInTime exists, count 0.5
                                if (clockInTime != null && !clockInTime.isEmpty()) {
                                    otCount += 0.5;
                                }

                                // If ClockOutTime exists, count 0.5
                                if (clockOutTime != null && !clockOutTime.isEmpty()) {
                                    otCount += 0.5;
                                }
                            }
                        }
                    }

                    // Update the TextView for OT count on Sundays (1-7 range)
                    TextView otCountTextView = findViewById(R.id.coff1); // coff1 is the TextView for 1-7 range
                    otCountTextView.setText(String.format(Locale.getDefault(), "%.1f", otCount));

                    // Log the result for debugging
                    Log.d("OTCount", "OT count for Sundays from 1 to 7: " + otCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countOTForSundays8To14() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the user's time records in Firebase
            DatabaseReference timeRecordsRef = mDatabase.child("Users").child(uid).child("timeRecords");

            // Fetch the data for the time records
            timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    double otCount = 0.0; // Variable to store the total OT count for Sundays

                    // Loop through days 8 to 14
                    for (int day = 8; day <= 14; day++) {
                        // Create the date key for the specific day in the current month and year
                        String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, currentMonth, currentYear);

                        // Check if the date exists in the user's time records
                        if (dataSnapshot.hasChild(dateKey)) {
                            // Get the ClockInTime and ClockOutTime from Firebase
                            String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                            String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                            // Check if the date is a Sunday (day of the week = Sunday)
                            Calendar dateCalendar = Calendar.getInstance();
                            dateCalendar.set(currentYear, currentMonth - 1, day); // Set the date for comparison
                            int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK); // Get day of the week

                            // If it's a Sunday (Sunday is 1 in Calendar class)
                            if (dayOfWeek == Calendar.SUNDAY) {
                                // If ClockInTime exists, count 0.5
                                if (clockInTime != null && !clockInTime.isEmpty()) {
                                    otCount += 0.5;
                                }

                                // If ClockOutTime exists, count 0.5
                                if (clockOutTime != null && !clockOutTime.isEmpty()) {
                                    otCount += 0.5;
                                }
                            }
                        }
                    }

                    // Update the TextView for OT count on Sundays (8-14 range)
                    TextView otCountTextView = findViewById(R.id.coff2); // coff2 is the TextView for 8-14 range
                    otCountTextView.setText(String.format(Locale.getDefault(), "%.1f", otCount));

                    // Log the result for debugging
                    Log.d("OTCount", "OT count for Sundays from 8 to 14: " + otCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countOTForSundays15To21() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the user's time records in Firebase
            DatabaseReference timeRecordsRef = mDatabase.child("Users").child(uid).child("timeRecords");

            // Fetch the data for the time records
            timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    double otCount = 0.0; // Variable to store the total OT count for Sundays

                    // Loop through days 15 to 21
                    for (int day = 15; day <= 21; day++) {
                        // Create the date key for the specific day in the current month and year
                        String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, currentMonth, currentYear);

                        // Check if the date exists in the user's time records
                        if (dataSnapshot.hasChild(dateKey)) {
                            // Get the ClockInTime and ClockOutTime from Firebase
                            String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                            String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                            // Check if the date is a Sunday (day of the week = Sunday)
                            Calendar dateCalendar = Calendar.getInstance();
                            dateCalendar.set(currentYear, currentMonth - 1, day); // Set the date for comparison
                            int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK); // Get day of the week

                            // If it's a Sunday (Sunday is 1 in Calendar class)
                            if (dayOfWeek == Calendar.SUNDAY) {
                                // If ClockInTime exists, count 0.5
                                if (clockInTime != null && !clockInTime.isEmpty()) {
                                    otCount += 0.5;
                                }

                                // If ClockOutTime exists, count 0.5
                                if (clockOutTime != null && !clockOutTime.isEmpty()) {
                                    otCount += 0.5;
                                }
                            }
                        }
                    }

                    // Update the TextView for OT count on Sundays (15-21 range)
                    TextView otCountTextView = findViewById(R.id.coff3); // coff3 is the TextView for 15-21 range
                    otCountTextView.setText(String.format(Locale.getDefault(), "%.1f", otCount));

                    // Log the result for debugging
                    Log.d("OTCount", "OT count for Sundays from 15 to 21: " + otCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countOTForSundays22To28() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the user's time records in Firebase
            DatabaseReference timeRecordsRef = mDatabase.child("Users").child(uid).child("timeRecords");

            // Fetch the data for the time records
            timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    double otCount = 0.0; // Variable to store the total OT count for Sundays

                    // Loop through days 22 to 28
                    for (int day = 22; day <= 28; day++) {
                        // Create the date key for the specific day in the current month and year
                        String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, currentMonth, currentYear);

                        // Check if the date exists in the user's time records
                        if (dataSnapshot.hasChild(dateKey)) {
                            // Get the ClockInTime and ClockOutTime from Firebase
                            String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                            String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                            // Check if the date is a Sunday (day of the week = Sunday)
                            Calendar dateCalendar = Calendar.getInstance();
                            dateCalendar.set(currentYear, currentMonth - 1, day); // Set the date for comparison
                            int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK); // Get day of the week

                            // If it's a Sunday (Sunday is 1 in Calendar class)
                            if (dayOfWeek == Calendar.SUNDAY) {
                                // If ClockInTime exists, count 0.5
                                if (clockInTime != null && !clockInTime.isEmpty()) {
                                    otCount += 0.5;
                                }

                                // If ClockOutTime exists, count 0.5
                                if (clockOutTime != null && !clockOutTime.isEmpty()) {
                                    otCount += 0.5;
                                }
                            }
                        }
                    }

                    // Update the TextView for OT count on Sundays (22-28 range)
                    TextView otCountTextView = findViewById(R.id.coff4); // coff4 is the TextView for 22-28 range
                    otCountTextView.setText(String.format(Locale.getDefault(), "%.1f", otCount));

                    // Log the result for debugging
                    Log.d("OTCount", "OT count for Sundays from 22 to 28: " + otCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countOTForSundays29To31() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the user's time records in Firebase
            DatabaseReference timeRecordsRef = mDatabase.child("Users").child(uid).child("timeRecords");

            // Fetch the data for the time records
            timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    double otCount = 0.0; // Variable to store the total OT count for Sundays

                    // Loop through days 29 to 31
                    for (int day = 29; day <= 31; day++) {
                        // Create the date key for the specific day in the current month and year
                        String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, currentMonth, currentYear);

                        // Check if the date exists in the user's time records
                        if (dataSnapshot.hasChild(dateKey)) {
                            // Get the ClockInTime and ClockOutTime from Firebase
                            String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                            String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                            // Check if the date is a Sunday (day of the week = Sunday)
                            Calendar dateCalendar = Calendar.getInstance();
                            dateCalendar.set(currentYear, currentMonth - 1, day); // Set the date for comparison
                            int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK); // Get day of the week

                            // If it's a Sunday (Sunday is 1 in Calendar class)
                            if (dayOfWeek == Calendar.SUNDAY) {
                                // If ClockInTime exists, count 0.5
                                if (clockInTime != null && !clockInTime.isEmpty()) {
                                    otCount += 0.5;
                                }

                                // If ClockOutTime exists, count 0.5
                                if (clockOutTime != null && !clockOutTime.isEmpty()) {
                                    otCount += 0.5;
                                }
                            }
                        }
                    }

                    // Update the TextView for OT count on Sundays (29-31 range)
                    TextView otCountTextView = findViewById(R.id.coff5); // coff5 is the TextView for 29-31 range
                    otCountTextView.setText(String.format(Locale.getDefault(), "%.1f", otCount));

                    // Log the result for debugging
                    Log.d("OTCount", "OT count for Sundays from 29 to 31: " + otCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countPermissionsFor1To7DaysCurrentMonth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month, year, and day
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the permission requests in Firebase
            DatabaseReference permissionRequestsRef = mDatabase.child("permissionRequests");

            // Fetch the data for the permission requests
            permissionRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int permissionCount = 0;

                    // Loop through the permission requests
                    for (DataSnapshot permissionSnapshot : dataSnapshot.getChildren()) {
                        // Extract the data for each permission request
                        String permissionUid = permissionSnapshot.child("uid").getValue(String.class);
                        String date = permissionSnapshot.child("date").getValue(String.class);

                        // Skip if the uid does not match the current user's UID
                        if (permissionUid == null || !permissionUid.equals(uid)) {
                            continue;
                        }

                        // Ensure the date is properly formatted and split it to extract day, month, and year
                        if (date != null && !date.trim().isEmpty()) {
                            String[] dateParts = date.trim().split("-"); // Trim and split the date
                            if (dateParts.length == 3) {
                                try {
                                    int day = Integer.parseInt(dateParts[0]);
                                    int month = Integer.parseInt(dateParts[1]);
                                    int year = Integer.parseInt(dateParts[2].trim());

                                    // Check if the request is for the current month, and the year and day are valid
                                    if (month == currentMonth && year == currentYear && day >= 1 && day <= 7) {
                                        // Ensure that the day is not in the future
                                        if (day <= currentDay) {
                                            // Increment the permission count for this date
                                            permissionCount++;
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e("PermissionCount", "Error parsing date: " + date);
                                }
                            }
                        }
                    }

                    // Find the TextView for displaying the permission count (pm1)
                    TextView permissionTextView = findViewById(R.id.pm1); // Use the correct TextView ID (pm1)

                    // Update the TextView with the permission count for dates 1-7 of current month
                    permissionTextView.setText(String.valueOf(permissionCount));

                    // Log the result for debugging
                    Log.d("PermissionCount", "Permission count for dates 1-7 of current month: " + permissionCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch permission requests", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countPermissionsFor8To14DaysCurrentMonth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month, year, and day
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the permission requests in Firebase
            DatabaseReference permissionRequestsRef = mDatabase.child("permissionRequests");

            // Fetch the data for the permission requests
            permissionRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int permissionCount = 0;

                    // Loop through the permission requests
                    for (DataSnapshot permissionSnapshot : dataSnapshot.getChildren()) {
                        // Extract the data for each permission request
                        String permissionUid = permissionSnapshot.child("uid").getValue(String.class);
                        String date = permissionSnapshot.child("date").getValue(String.class);

                        // Skip if the uid does not match the current user's UID
                        if (permissionUid == null || !permissionUid.equals(uid)) {
                            continue;
                        }

                        // Ensure the date is properly formatted and split it to extract day, month, and year
                        if (date != null && !date.trim().isEmpty()) {
                            String[] dateParts = date.trim().split("-"); // Trim and split the date
                            if (dateParts.length == 3) {
                                try {
                                    int day = Integer.parseInt(dateParts[0]);
                                    int month = Integer.parseInt(dateParts[1]);
                                    int year = Integer.parseInt(dateParts[2].trim());

                                    // Check if the request is for the current month, the year is correct, and the day is between 8 and 14
                                    if (month == currentMonth && year == currentYear && day >= 8 && day <= 14) {
                                        // Ensure that the day is not in the future
                                        if (day <= currentDay) {
                                            // Increment the permission count for this date
                                            permissionCount++;
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e("PermissionCount", "Error parsing date: " + date);
                                }
                            }
                        }
                    }

                    // Find the TextView for displaying the permission count (pm2)
                    TextView permissionTextView = findViewById(R.id.pm2); // Use the correct TextView ID (pm2)

                    // Update the TextView with the permission count for dates 8-14 of current month
                    permissionTextView.setText(String.valueOf(permissionCount));

                    // Log the result for debugging
                    Log.d("PermissionCount", "Permission count for dates 8-14 of current month: " + permissionCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch permission requests", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countPermissionsFor15To21DaysCurrentMonth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month, year, and day
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the permission requests in Firebase
            DatabaseReference permissionRequestsRef = mDatabase.child("permissionRequests");

            // Fetch the data for the permission requests
            permissionRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int permissionCount = 0;

                    // Loop through the permission requests
                    for (DataSnapshot permissionSnapshot : dataSnapshot.getChildren()) {
                        // Extract the data for each permission request
                        String permissionUid = permissionSnapshot.child("uid").getValue(String.class);
                        String date = permissionSnapshot.child("date").getValue(String.class);

                        // Skip if the uid does not match the current user's UID
                        if (permissionUid == null || !permissionUid.equals(uid)) {
                            continue;
                        }

                        // Ensure the date is properly formatted and split it to extract day, month, and year
                        if (date != null && !date.trim().isEmpty()) {
                            String[] dateParts = date.trim().split("-"); // Trim and split the date
                            if (dateParts.length == 3) {
                                try {
                                    int day = Integer.parseInt(dateParts[0]);
                                    int month = Integer.parseInt(dateParts[1]);
                                    int year = Integer.parseInt(dateParts[2].trim());

                                    // Check if the request is for the current month, the year is correct, and the day is between 15 and 21
                                    if (month == currentMonth && year == currentYear && day >= 15 && day <= 21) {
                                        // Ensure that the day is not in the future
                                        if (day <= currentDay) {
                                            // Increment the permission count for this date
                                            permissionCount++;
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e("PermissionCount", "Error parsing date: " + date);
                                }
                            }
                        }
                    }

                    // Find the TextView for displaying the permission count (pm3)
                    TextView permissionTextView = findViewById(R.id.pm3); // Use the correct TextView ID (pm3)

                    // Update the TextView with the permission count for dates 15-21 of the current month
                    permissionTextView.setText(String.valueOf(permissionCount));

                    // Log the result for debugging
                    Log.d("PermissionCount", "Permission count for dates 15-21 of current month: " + permissionCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch permission requests", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countPermissionsFor22To28DaysCurrentMonth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the permission requests in Firebase
            DatabaseReference permissionRequestsRef = mDatabase.child("permissionRequests");

            // Fetch the data for the permission requests
            permissionRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int permissionCount = 0;

                    // Loop through the permission requests
                    for (DataSnapshot permissionSnapshot : dataSnapshot.getChildren()) {
                        // Extract the data for each permission request
                        String permissionUid = permissionSnapshot.child("uid").getValue(String.class);
                        String date = permissionSnapshot.child("date").getValue(String.class);

                        // Skip if the uid does not match the current user's UID
                        if (permissionUid == null || !permissionUid.equals(uid)) {
                            continue;
                        }

                        // Ensure the date is properly formatted and split it to extract day, month, and year
                        if (date != null && !date.trim().isEmpty()) {
                            String[] dateParts = date.trim().split("-"); // Trim and split the date
                            if (dateParts.length == 3) {
                                try {
                                    int day = Integer.parseInt(dateParts[0]);
                                    int month = Integer.parseInt(dateParts[1]);
                                    int year = Integer.parseInt(dateParts[2].trim());

                                    // Check if the request is for the current month, the year is correct, and the day is between 22 and 28
                                    if (month == currentMonth && year == currentYear && day >= 22 && day <= 28) {
                                        // Ensure that the day is not in the future
                                        if (day <= currentDay) {
                                            // Increment the permission count for this date
                                            permissionCount++;
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e("PermissionCount", "Error parsing date: " + date);
                                }
                            }
                        }
                    }

                    // Find the TextView for displaying the permission count (pm4)
                    TextView permissionTextView = findViewById(R.id.pm4); // Use the correct TextView ID (pm4)

                    // Update the TextView with the permission count for dates 22-28 of current month
                    permissionTextView.setText(String.valueOf(permissionCount));

                    // Log the result for debugging
                    Log.d("PermissionCount", "Permission count for dates 22-28 of current month: " + permissionCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch permission requests", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countPermissionsFor29To31DaysCurrentMonth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month

            // Reference to the permission requests in Firebase
            DatabaseReference permissionRequestsRef = mDatabase.child("permissionRequests");

            // Fetch the data for the permission requests
            permissionRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int permissionCount = 0;

                    // Loop through the permission requests
                    for (DataSnapshot permissionSnapshot : dataSnapshot.getChildren()) {
                        // Extract the data for each permission request
                        String permissionUid = permissionSnapshot.child("uid").getValue(String.class);
                        String date = permissionSnapshot.child("date").getValue(String.class);

                        // Skip if the uid does not match the current user's UID
                        if (permissionUid == null || !permissionUid.equals(uid)) {
                            continue;
                        }

                        // Ensure the date is properly formatted and split it to extract day, month, and year
                        if (date != null && !date.trim().isEmpty()) {
                            String[] dateParts = date.trim().split("-"); // Trim and split the date
                            if (dateParts.length == 3) {
                                try {
                                    int day = Integer.parseInt(dateParts[0]);
                                    int month = Integer.parseInt(dateParts[1]);
                                    int year = Integer.parseInt(dateParts[2].trim());

                                    // Check if the request is for the current month, the year is correct, and the day is between 29 and 31
                                    if (month == currentMonth && year == currentYear && day >= 29 && day <= 31) {
                                        // Ensure that the day is not in the future
                                        if (day <= currentDay) {
                                            // Increment the permission count for this date
                                            permissionCount++;
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e("PermissionCount", "Error parsing date: " + date);
                                }
                            }
                        }
                    }

                    // Find the TextView for displaying the permission count (pm5)
                    TextView permissionTextView = findViewById(R.id.pm5); // Use the correct TextView ID (pm5)

                    // Update the TextView with the permission count for dates 29-31 of current month
                    permissionTextView.setText(String.valueOf(permissionCount));

                    // Log the result for debugging
                    Log.d("PermissionCount", "Permission count for dates 29-31 of current month: " + permissionCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch permission requests", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countLateRequestsFor1To7DaysCurrentMonth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);

            // Reference to the LateRequests in Firebase
            DatabaseReference lateRequestsRef = mDatabase.child("LateRequests");

            // Fetch the data for the LateRequests
            lateRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int lateRequestCount = 0;

                    // Loop through the LateRequests
                    for (DataSnapshot lateRequestSnapshot : dataSnapshot.getChildren()) {
                        // Extract the data for each LateRequest
                        String lateRequestUid = lateRequestSnapshot.child("userId").getValue(String.class);
                        String date = lateRequestSnapshot.child("date").getValue(String.class);
                        String status = lateRequestSnapshot.child("status").getValue(String.class);

                        // Skip if the uid does not match the current user's UID
                        if (lateRequestUid == null || !lateRequestUid.equals(uid)) {
                            continue;
                        }

                        // Ensure the date is properly formatted and split it to extract day, month, and year
                        if (date != null && !date.trim().isEmpty()) {
                            String[] dateParts = date.trim().split("-"); // Trim and split the date
                            if (dateParts.length == 3) {
                                try {
                                    int day = Integer.parseInt(dateParts[0]);
                                    int month = Integer.parseInt(dateParts[1]);
                                    int year = Integer.parseInt(dateParts[2].trim());

                                    // Check if the request is for the current month, current year,
                                    // and if the day is between 1 and 7 and the status is "updated"
                                    if (month == currentMonth && year == currentYear && day >= 1 && day <= 7 && "Updated".equalsIgnoreCase(status)) {
                                        // Increment the late request count
                                        lateRequestCount++;
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e("LateRequestCount", "Error parsing date: " + date);
                                }
                            }
                        }
                    }

                    // Find the TextView for displaying the late request count (sm1)
                    TextView lateRequestTextView = findViewById(R.id.sm1); // Use the correct TextView ID (sm1)

                    // Update the TextView with the late request count for dates 1-7 of current month
                    lateRequestTextView.setText(String.valueOf(lateRequestCount));

                    // Log the result for debugging
                    Log.d("LateRequestCount", "Late request count for dates 1-7 of current month: " + lateRequestCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch late requests", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countLateRequestsFor8To14DaysCurrentMonth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);

            // Reference to the LateRequests in Firebase
            DatabaseReference lateRequestsRef = mDatabase.child("LateRequests");

            // Fetch the data for the LateRequests
            lateRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int lateRequestCount = 0;

                    // Loop through the LateRequests
                    for (DataSnapshot lateRequestSnapshot : dataSnapshot.getChildren()) {
                        // Extract the data for each LateRequest
                        String lateRequestUid = lateRequestSnapshot.child("userId").getValue(String.class);
                        String date = lateRequestSnapshot.child("date").getValue(String.class);
                        String status = lateRequestSnapshot.child("status").getValue(String.class);

                        // Skip if the uid does not match the current user's UID
                        if (lateRequestUid == null || !lateRequestUid.equals(uid)) {
                            continue;
                        }

                        // Ensure the date is properly formatted and split it to extract day, month, and year
                        if (date != null && !date.trim().isEmpty()) {
                            String[] dateParts = date.trim().split("-"); // Trim and split the date
                            if (dateParts.length == 3) {
                                try {
                                    int day = Integer.parseInt(dateParts[0]);
                                    int month = Integer.parseInt(dateParts[1]);
                                    int year = Integer.parseInt(dateParts[2].trim());

                                    // Check if the request is for the current month, current year,
                                    // and if the day is between 8 and 14 and the status is "updated"
                                    if (month == currentMonth && year == currentYear && day >= 8 && day <= 14 && "Updated".equalsIgnoreCase(status)) {
                                        // Increment the late request count
                                        lateRequestCount++;
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e("LateRequestCount", "Error parsing date: " + date);
                                }
                            }
                        }
                    }

                    // Find the TextView for displaying the late request count (sm2)
                    TextView lateRequestTextView = findViewById(R.id.sm2); // Use the correct TextView ID (sm2)

                    // Update the TextView with the late request count for dates 8-14 of current month
                    lateRequestTextView.setText(String.valueOf(lateRequestCount));

                    // Log the result for debugging
                    Log.d("LateRequestCount", "Late request count for dates 8-14 of current month: " + lateRequestCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch late requests", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countLateRequestsFor15To21DaysCurrentMonth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);

            // Reference to the LateRequests in Firebase
            DatabaseReference lateRequestsRef = mDatabase.child("LateRequests");

            // Fetch the data for the LateRequests
            lateRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int lateRequestCount = 0;

                    // Loop through the LateRequests
                    for (DataSnapshot lateRequestSnapshot : dataSnapshot.getChildren()) {
                        // Extract the data for each LateRequest
                        String lateRequestUid = lateRequestSnapshot.child("userId").getValue(String.class);
                        String date = lateRequestSnapshot.child("date").getValue(String.class);
                        String status = lateRequestSnapshot.child("status").getValue(String.class);

                        // Skip if the uid does not match the current user's UID
                        if (lateRequestUid == null || !lateRequestUid.equals(uid)) {
                            continue;
                        }

                        // Ensure the date is properly formatted and split it to extract day, month, and year
                        if (date != null && !date.trim().isEmpty()) {
                            String[] dateParts = date.trim().split("-"); // Trim and split the date
                            if (dateParts.length == 3) {
                                try {
                                    int day = Integer.parseInt(dateParts[0]);
                                    int month = Integer.parseInt(dateParts[1]);
                                    int year = Integer.parseInt(dateParts[2].trim());

                                    // Check if the request is for the current month, current year,
                                    // and if the day is between 15 and 21 and the status is "Updated"
                                    if (month == currentMonth && year == currentYear && day >= 15 && day <= 21 && "Updated".equalsIgnoreCase(status)) {
                                        // Increment the late request count
                                        lateRequestCount++;
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e("LateRequestCount", "Error parsing date: " + date);
                                }
                            }
                        }
                    }

                    // Find the TextView for displaying the late request count (sm3)
                    TextView lateRequestTextView = findViewById(R.id.sm3); // Use the correct TextView ID (sm3)

                    // Update the TextView with the late request count for dates 15-21 of current month
                    lateRequestTextView.setText(String.valueOf(lateRequestCount));

                    // Log the result for debugging
                    Log.d("LateRequestCount", "Late request count for dates 15-21 of current month: " + lateRequestCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch late requests", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countLateRequestsFor22To28DaysCurrentMonth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);

            // Reference to the LateRequests in Firebase
            DatabaseReference lateRequestsRef = mDatabase.child("LateRequests");

            // Fetch the data for the LateRequests
            lateRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int lateRequestCount = 0;

                    // Loop through the LateRequests
                    for (DataSnapshot lateRequestSnapshot : dataSnapshot.getChildren()) {
                        // Extract the data for each LateRequest
                        String lateRequestUid = lateRequestSnapshot.child("userId").getValue(String.class);
                        String date = lateRequestSnapshot.child("date").getValue(String.class);
                        String status = lateRequestSnapshot.child("status").getValue(String.class);

                        // Skip if the uid does not match the current user's UID
                        if (lateRequestUid == null || !lateRequestUid.equals(uid)) {
                            continue;
                        }

                        // Ensure the date is properly formatted and split it to extract day, month, and year
                        if (date != null && !date.trim().isEmpty()) {
                            String[] dateParts = date.trim().split("-"); // Trim and split the date
                            if (dateParts.length == 3) {
                                try {
                                    int day = Integer.parseInt(dateParts[0]);
                                    int month = Integer.parseInt(dateParts[1]);
                                    int year = Integer.parseInt(dateParts[2].trim());

                                    // Check if the request is for the current month, current year,
                                    // and if the day is between 22 and 28 and the status is "Updated"
                                    if (month == currentMonth && year == currentYear && day >= 22 && day <= 28 && "Updated".equalsIgnoreCase(status)) {
                                        // Increment the late request count
                                        lateRequestCount++;
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e("LateRequestCount", "Error parsing date: " + date);
                                }
                            }
                        }
                    }

                    // Find the TextView for displaying the late request count (sm4)
                    TextView lateRequestTextView = findViewById(R.id.sm4); // Use the correct TextView ID (sm4)

                    // Update the TextView with the late request count for dates 22-28 of current month
                    lateRequestTextView.setText(String.valueOf(lateRequestCount));

                    // Log the result for debugging
                    Log.d("LateRequestCount", "Late request count for dates 22-28 of current month: " + lateRequestCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch late requests", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countLateRequestsFor29To31DaysCurrentMonth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the UID of the currently logged-in user

            // Get the current date and extract the current month and year
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Months are 0-based, so add 1
            int currentYear = calendar.get(Calendar.YEAR);

            // Reference to the LateRequests in Firebase
            DatabaseReference lateRequestsRef = mDatabase.child("LateRequests");

            // Fetch the data for the LateRequests
            lateRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int lateRequestCount = 0;

                    // Loop through the LateRequests
                    for (DataSnapshot lateRequestSnapshot : dataSnapshot.getChildren()) {
                        // Extract the data for each LateRequest
                        String lateRequestUid = lateRequestSnapshot.child("userId").getValue(String.class);
                        String date = lateRequestSnapshot.child("date").getValue(String.class);
                        String status = lateRequestSnapshot.child("status").getValue(String.class);

                        // Skip if the uid does not match the current user's UID
                        if (lateRequestUid == null || !lateRequestUid.equals(uid)) {
                            continue;
                        }

                        // Ensure the date is properly formatted and split it to extract day, month, and year
                        if (date != null && !date.trim().isEmpty()) {
                            String[] dateParts = date.trim().split("-"); // Trim and split the date
                            if (dateParts.length == 3) {
                                try {
                                    int day = Integer.parseInt(dateParts[0]);
                                    int month = Integer.parseInt(dateParts[1]);
                                    int year = Integer.parseInt(dateParts[2].trim());

                                    // Check if the request is for the current month, current year,
                                    // and if the day is between 29 and 31 and the status is "Updated"
                                    if (month == currentMonth && year == currentYear && day >= 29 && day <= 31 && "Updated".equalsIgnoreCase(status)) {
                                        // Increment the late request count
                                        lateRequestCount++;
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e("LateRequestCount", "Error parsing date: " + date);
                                }
                            }
                        }
                    }

                    // Find the TextView for displaying the late request count (sm5)
                    TextView lateRequestTextView = findViewById(R.id.sm5); // Use the correct TextView ID (sm5)

                    // Update the TextView with the late request count for dates 29-31 of current month
                    lateRequestTextView.setText(String.valueOf(lateRequestCount));

                    // Log the result for debugging
                    Log.d("LateRequestCount", "Late request count for dates 29-31 of current month: " + lateRequestCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(WeekCalendarActivity.this, "Failed to fetch late requests", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


}