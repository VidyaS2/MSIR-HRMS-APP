package com.attendance.clockme;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import java.util.HashMap;
import java.util.Locale;
import java.text.ParseException;
import java.util.Objects;

public class DashboardActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "ClockInOutPrefs";
    static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView welcomeMessage;
    private GridLayout calendarGrid;
    private Calendar calendar = Calendar.getInstance();
    private TextView monthYearText;
    private ImageView imageView1;
    private Button buttonMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        welcomeMessage = findViewById(R.id.welcomeMessage);
        calendarGrid = findViewById(R.id.gridLayout); // Your GridLayout ID

        monthYearText = findViewById(R.id.header);
        ImageView previousButton = findViewById(R.id.calendar_previous);
        ImageView nextButton = findViewById(R.id.calendar_next);

        // Set initial month and year in the header
        updateMonthYearText();

        Intent incomingIntent = getIntent();
        String successMessage = incomingIntent.getStringExtra("success_message");

        if (successMessage != null) {
            Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show();
        }

        // Check for location permissions and services
        checkLocationPermissions();

        setupNavigation();
        loadUserDataAndDisplay();
        initializeCalendar();
        populateCalendar();

        // Set initial month and year in the header
        updateMonthYearText();

        // Set click listener for the previous month button
        previousButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1); // Go to the previous month
            updateMonthYearText(); // Update the displayed month and year
            populateCalendar(); // Refresh the calendar display
        });

        // Set click listener for the next month button
        nextButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1); // Go to the next month
            updateMonthYearText(); // Update the displayed month and year
            populateCalendar(); // Refresh the calendar display
        });
//        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putBoolean("isLoggedIn", false); // reset to false on logout
//        editor.apply();
        // Populate the calendar for the first time


//        populateCalendar();

        // Find the button by ID
        Button buttonWeek = findViewById(R.id.button_week);

        // Set an OnClickListener to handle the button click event
        buttonWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to start WeekCalendarActivity
                Intent intent = new Intent(DashboardActivity.this, WeekCalendarActivity.class);
                startActivity(intent); // Start the activity
            }
        });

        buttonMonth = findViewById(R.id.button_month);  // Get reference to the button

        buttonMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to MonthlyCalendarActivity
                Intent intent = new Intent(DashboardActivity.this, MonthlyCalendarActivity.class);
                startActivity(intent);  // Start the activity
            }
        });
    }
    private void updateMonthYearText() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        monthYearText.setText(sdf.format(calendar.getTime())); // Set the formatted month/year
    }

    private void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            checkLocationEnabled();
        }
    }

    private void checkLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showLocationSettingsAlert();
        }
    }

    private void showLocationSettingsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Location Services Disabled");
        builder.setMessage("Please enable location services to use this app.")
                .setCancelable(false)
                .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationEnabled(); // Check if location is enabled after permission granted
            } else {
                Toast.makeText(this, "Location permission is required for this app", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupNavigation() {
        LinearLayout homeIcon = findViewById(R.id.homelayout1);
        LinearLayout clockIcon = findViewById(R.id.clocklayout1);
        LinearLayout logoutIcon = findViewById(R.id.signoutlayut1);
        LinearLayout requestIcon = findViewById(R.id.requestlayout1);
        Button attendanceButton = findViewById(R.id.attendanceButton);
        ImageView calendarPrevious = findViewById(R.id.calendar_previous);
        ImageView calendarNext = findViewById(R.id.calendar_next);

        homeIcon.setOnClickListener(v -> { /* already on Dashboard */ });

        clockIcon.setOnClickListener(v -> {
            Intent clockInOutIntent = new Intent(DashboardActivity.this, SwipingTimeActivity.class);
            startActivity(clockInOutIntent);
        });

        requestIcon.setOnClickListener(v -> {
            Intent salaryDetailsIntent = new Intent(DashboardActivity.this, SalaryDetailsActivity.class);
            startActivity(salaryDetailsIntent);
        });

        logoutIcon.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(DashboardActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(DashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        attendanceButton.setOnClickListener(v -> {
            Intent attendanceIntent = new Intent(DashboardActivity.this, ClockInOutActivity.class);
            startActivity(attendanceIntent);
        });

        calendarPrevious.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            populateCalendar();
        });

        calendarNext.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            populateCalendar(); // Update the calendar view
        });
    }

    private void initializeCalendar() {
        calendar = Calendar.getInstance(); // Initialize calendar with the current date
    }

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
                                    // Capitalize the first letter
                                    username = username.substring(0, 1).toUpperCase() + username.substring(1).toLowerCase();
                                    welcomeMessage.setText("Welcome, " + username + "!");
                                } else {
                                    Toast.makeText(DashboardActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(DashboardActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(DashboardActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(DashboardActivity.this, "User email not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(DashboardActivity.this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private void populateCalendar() {
        // Clear previous calendar items
        calendarGrid.removeAllViews();

        // Set a fixed column count for the GridLayout
        calendarGrid.setColumnCount(7); // 7 days in a week

        // Get the current month and year
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);

        // Set the calendar to the first day of the current month
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        // Calculate the day of the week for the first day of the month
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // Sunday = 1, Monday = 2, ..., Saturday = 7

        // Adjust to zero-based index where Sunday = 0, Monday = 1, ..., Saturday = 6
        int adjustedFirstDayOfWeek = firstDayOfWeek - 1;

        // Get the number of days in the current month
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Define fixed dimensions for the cells (adjust as necessary)
        int totalScreenWidth = getResources().getDisplayMetrics().widthPixels;

        // Reduce the width by introducing a scaling factor (e.g., 0.9 for 90% of the original width)
        float widthScalingFactor = 1.0f;
        int cellWidth = (int) ((totalScreenWidth / 7) * widthScalingFactor); // Scale down the width

        // Adjust the height relative to the new width
        int cellHeight = (int) (cellWidth * 1.2); // Keep height proportional to width
        // Add empty cells (padding) to align the first day of the month with the correct day of the week
        for (int i = 0; i < adjustedFirstDayOfWeek; i++) {
            LinearLayout emptyLayout = new LinearLayout(this);
            emptyLayout.setLayoutParams(new GridLayout.LayoutParams());
            calendarGrid.addView(emptyLayout); // Add empty view for padding
        }

        // Get today's date for comparison
        Calendar today = Calendar.getInstance();
        int todayDay = today.get(Calendar.DAY_OF_MONTH);
        int todayMonth = today.get(Calendar.MONTH);
        int todayYear = today.get(Calendar.YEAR);

//        // Fetch clockInTime from Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//
        String uid = currentUser.getUid();
//          //  DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("timeRecords");
//        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
//        usersRef.child(uid).child("timeRecords")
//                .addListenerForSingleValueEvent(new ValueEventListener() {
        DatabaseReference timeRecordsRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(uid).child("timeRecords");

        //  DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        //usersRef.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("timeRecords").child("clockInTime")
        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Assume clockInTime is stored as a HashMap with the date as key and time as value
                // HashMap<String, String> clockInTimes = (HashMap<String, String>) dataSnapshot.getValue();
// @SuppressLint("ResourceType")


                // Assume clockInTime is stored as a HashMap with the date as key and time as value
                // HashMap<String, String> clockInTimes = (HashMap<String, String>) dataSnapshot.getValue();
//                            HashMap<String, String> clockInTimes = new HashMap<>();
//                            if (dataSnapshot.exists()) {
//                                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
//                                    String date = dateSnapshot.getKey();
//                                    String clockInTime = dateSnapshot.child("ClockInTime").getValue(String.class);
//
//                                    if (clockInTime != null) {
//                                        clockInTimes.put(date, clockInTime);
//                                    } else {
//                                        clockInTimes.put(date, "00:00");
//                                    }
//                                }
//                            } else {
//                                Log.d("FirebaseData", "No data found under clockInTime");
//                            }

                HashMap<String, String> clockInTimes = new HashMap<>();
                HashMap<String, String> clockOutTimes = new HashMap<>();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                        String date = dateSnapshot.getKey();

                        // Fetch clock-in time
                        String clockInTime = dateSnapshot.child("ClockInTime").getValue(String.class);
                        if (clockInTime != null) {
                            clockInTimes.put(date, clockInTime);
                        } else {
                            clockInTimes.put(date, "00:00");
                        }

                        // Fetch clock-out time
                        String clockOutTime = dateSnapshot.child("ClockOutTime").getValue(String.class);
                        if (clockOutTime != null) {
                            clockOutTimes.put(date, clockOutTime);
                        } else {
                            clockOutTimes.put(date, "00:00");
                        }
                    }
                } else {
                    Log.d("FirebaseData", "No data found under clockInTime");
                }

                // You can now access clockInTimes and clockOutTimes as needed

                // Iterate through each day of the month and add it to the GridLayout
                for (int day = 1; day <= daysInMonth; day++) {
                    // Create a layout for each day
                    LinearLayout dayLayout = new LinearLayout(DashboardActivity.this);
                    dayLayout.setOrientation(LinearLayout.VERTICAL);
                    dayLayout.setGravity(Gravity.CENTER);

                    // Set fixed size for each day layout
                    GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                    layoutParams.width = cellWidth; // Fixed width
                    layoutParams.height = cellHeight; // Fixed height
                    layoutParams.setMargins(0, 0, 0, 0); // Optional: add margin for spacing
                    dayLayout.setLayoutParams(layoutParams);

                    // Set the background to the border drawable
                    dayLayout.setBackgroundResource(R.drawable.border_calendar_day);

                    // Create and add the TextView for the weekday name (Mon, Tue, Wed, etc.)
                    calendar.set(Calendar.DAY_OF_MONTH, day);
                    String weekdayName = getWeekdayName(calendar.get(Calendar.DAY_OF_WEEK)); // Get weekday name
                    TextView weekdayText = new TextView(DashboardActivity.this);
                    weekdayText.setText(weekdayName);
                    weekdayText.setTextSize(12); // Smaller text size for weekday
                    weekdayText.setTextColor(Color.BLACK);
                    weekdayText.setGravity(Gravity.CENTER);
                    dayLayout.addView(weekdayText); // Add weekday name above the day number

                    // Create and add the TextView for the date number
                    TextView dateText = new TextView(DashboardActivity.this);
                    dateText.setText(String.valueOf(day));
                    dateText.setTextSize(16);
                    dateText.setTextColor(Color.parseColor("#3C71B1"));
                    dateText.setGravity(Gravity.CENTER);

                    // Set bold only if it's the current day
                    if (day == todayDay && currentMonth == todayMonth && currentYear == todayYear) {
                        dateText.setTypeface(null, Typeface.BOLD); // Make the date bold
                    }
                    dayLayout.addView(dateText); // Add the date number below the weekday name

                    // Create a horizontal layout for two ImageView indicators (clock-in and clock-out)
                    LinearLayout imageLayout = new LinearLayout(DashboardActivity.this);
                    imageLayout.setOrientation(LinearLayout.HORIZONTAL);
                    imageLayout.setGravity(Gravity.CENTER); // Center align the ImageViews

                    // Increase size and add gap between ImageViews
                    LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(50, 50);
                    imageParams.setMargins(8, 0, 8, 0);

                    // Add the ImageView elements
                    ImageView imageView1 = new ImageView(DashboardActivity.this);
                    imageView1.setLayoutParams(imageParams);
                    ImageView imageView2 = new ImageView(DashboardActivity.this);
                    imageView2.setLayoutParams(imageParams);

                    // Check if there's a clockInTime for the current date
                    String dateKey = String.format("%02d-%02d-%d", day, currentMonth + 1, currentYear); // Format: dd-MM-yyyy

                    // Check if it's Sunday
                    if (isSunday(dateKey)) {
                        if (clockInTimes != null && clockInTimes.containsKey(dateKey)) {
                            String clockInTimeStr = clockInTimes.get(dateKey);
                            if (clockInTimeStr.equalsIgnoreCase("Holiday")) {
                                imageView1.setBackgroundResource(R.drawable.calendar_hd_box); // Holiday for Sunday (clock-in)
                            } else {
                                imageView1.setBackgroundResource(R.drawable.calendar_ot_box); // Blue for Sunday with clock-in time
                            }
                        } else {
                            imageView1.setBackgroundResource(R.drawable.calendar_hd_box); // Holiday for Sunday without clock-in
                        }
                    } else {
                        // For other days, check if clock-in time is available
                        if (clockInTimes != null && clockInTimes.containsKey(dateKey)) {
                            String clockInTimeStr = clockInTimes.get(dateKey); // Get clockInTime as String
                            if (clockInTimeStr.equalsIgnoreCase("Holiday")) {
                                imageView1.setBackgroundResource(R.drawable.calendar_hd_box); // Holiday
                            } else {
                                String[] timeParts = clockInTimeStr.split(":"); // Assuming format is HH:mm
                                if (timeParts.length == 2) {
                                    int clockInHour = Integer.parseInt(timeParts[0]);
                                    int clockInMinute = Integer.parseInt(timeParts[1]);

                                    // Set to green if clockInTime is at or before 9:05 AM
                                    if (clockInHour < 9 || (clockInHour == 9 && clockInMinute <= 15)) {
                                        imageView1.setBackgroundResource(R.drawable.calendar_pr_box);
                                    } else if (clockInHour < 10 || (clockInHour == 10 && clockInMinute <= 30)){
                                        imageView1.setBackgroundResource(R.drawable.calendar_lt_box); // Late color
                                    }else {
                                        imageView1.setBackgroundResource(R.drawable.calendar_ab_box); // Late color
                                    }
                                }
                            }
                        } else {
                            // Default for no clock-in time
                            if ((currentMonth < todayMonth && currentYear <= todayYear) || (day <= todayDay && currentMonth <= todayMonth && currentYear <= todayYear)) {
                                imageView1.setBackgroundResource(R.drawable.calendar_ab_box); // Absent color for past days with no clock-in
                            } else {
                                imageView1.setBackgroundResource(R.drawable.calendar_grey_box); // Grey for future dates
                            }
                        }
                    }

                    // Clock-out checks for Sunday
                    if (isSunday(dateKey)) {
                        if (clockOutTimes != null && clockOutTimes.containsKey(dateKey)) {
                            String clockOutTimeStr = clockOutTimes.get(dateKey);
                            if (clockOutTimeStr.equalsIgnoreCase("Holiday")) {
                                imageView2.setBackgroundResource(R.drawable.calendar_hd_box); // Holiday for Sunday (clock-out)
                            } else {
                                imageView2.setBackgroundResource(R.drawable.calendar_ot_box); // Blue for Sunday with clock-out time
                            }
                        } else {
                            imageView2.setBackgroundResource(R.drawable.calendar_hd_box); // Holiday for Sunday without clock-out
                        }
                    } else {
                        // For other days, check if clock-out time is available
                        if (clockOutTimes != null && clockOutTimes.containsKey(dateKey)) {
                            String clockOutTimeStr = clockOutTimes.get(dateKey); // Get clockOutTime as String
                            if (clockOutTimeStr.equalsIgnoreCase("Holiday")) {
                                imageView2.setBackgroundResource(R.drawable.calendar_hd_box); // Holiday
                            } else {
                                String[] timeParts = clockOutTimeStr.split(":"); // Assuming format is HH:mm
                                if (timeParts.length == 2) {
                                    int clockOutHour = Integer.parseInt(timeParts[0]);
                                    int clockOutMinute = Integer.parseInt(timeParts[1]);

                                    // Set to green if clockOutTime is after or at 8:00 PM
                                    if (clockOutHour >= 16 || (clockOutHour == 17 && clockOutMinute <= 46)) {
                                        imageView2.setBackgroundColor(Color.parseColor("#1C9521"));
                                    } else {
                                        imageView2.setBackgroundResource(R.drawable.calendar_ab_box);
                                    }
                                }
                            }
                        } else {
                            // Default for no clock-out time
                            if ((currentMonth < todayMonth && currentYear <= todayYear) || (day <= todayDay && currentMonth <= todayMonth && currentYear <= todayYear)) {
                                imageView2.setBackgroundResource(R.drawable.calendar_ab_box); // Absent for past days without clock-out
                            } else {
                                imageView2.setBackgroundResource(R.drawable.calendar_grey_box); // Grey for future dates
                            }
                        }
                    }

                    // Always show the second box as 'calendar_pr_box'
                    // imageView2.setBackgroundResource(R.drawable.calendar_grey_box);

                    // Add the ImageViews to the horizontal layout
                    imageLayout.addView(imageView1);
                    imageLayout.addView(imageView2);
                    dayLayout.addView(imageLayout);


                    // Highlight the current day
                    if (day == todayDay && currentMonth == todayMonth && currentYear == todayYear) {
                        dayLayout.setBackgroundColor(Color.LTGRAY); // Light gray for the current day
                        dateText.setTextColor(Color.parseColor("#3C71B1")); // Optionally change text color for the current day
                    }

                    // Add the day layout to the GridLayout
                    calendarGrid.addView(dayLayout);
                }

                // Add an empty row to increase the row count
                LinearLayout emptyRowLayout = new LinearLayout(DashboardActivity.this);
                emptyRowLayout.setLayoutParams(new GridLayout.LayoutParams());
                calendarGrid.addView(emptyRowLayout); // This adds an empty row at the bottom
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });}

    private String getWeekdayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return "Sun";
            case Calendar.MONDAY: return "Mon";
            case Calendar.TUESDAY: return "Tue";
            case Calendar.WEDNESDAY: return "Wed";
            case Calendar.THURSDAY: return "Thu";
            case Calendar.FRIDAY: return "Fri";
            case Calendar.SATURDAY: return "Sat";
            default: return "";
        }
    }

    private void isHoliday(final String dateKey, final HolidayChecker.HolidayCheckCallback callback) {
        DatabaseReference holidaysRef = FirebaseDatabase.getInstance().getReference("holidays");

        // Check for the holiday in the Firebase database
        holidaysRef.child(dateKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // If the holiday exists for the given dateKey, return true
                boolean isHoliday = dataSnapshot.exists();

                // Ensure callback is called on the main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(isHoliday); // Pass the result back to the callback
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors (for example, if Firebase service is down or query fails)
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(false); // If the holiday check fails, assume it's not a holiday
                    }
                });
            }
        });
    }

    // Callback interface to handle holiday check
    public interface HolidayChecker {
        interface HolidayCheckCallback {
            void onResult(boolean isHoliday);
        }
    }

    private boolean isSunday(String dateKey) {
        // Parse the dateKey to determine if it is a Sunday
        // Assuming dateKey is in format "yyyy-MM-dd"
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Date date = sdf.parse(dateKey);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

}
