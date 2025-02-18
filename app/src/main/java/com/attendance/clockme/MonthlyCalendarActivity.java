package com.attendance.clockme;

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


public class MonthlyCalendarActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView welcomeTextView, pr1, pr2, pr3, pr4, pr5, pr6, pr7, pr8, pr9, pr10, pr11, pr12;
    private double totalCount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_calendar);

        sharedPreferences = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        pr1 = findViewById(R.id.pr1);
        pr2 = findViewById(R.id.pr2);
        pr3 = findViewById(R.id.pr3);
        pr4 = findViewById(R.id.pr4);
        pr5 = findViewById(R.id.pr5);
        pr6 = findViewById(R.id.pr6);
        pr7 = findViewById(R.id.pr7);
        pr8 = findViewById(R.id.pr8);
        pr9 = findViewById(R.id.pr9);
        pr10 = findViewById(R.id.pr10);
        pr11 = findViewById(R.id.pr11);
        pr12 = findViewById(R.id.pr12);

        welcomeTextView = findViewById(R.id.welcomeTextView);

        LinearLayout homeIcon = findViewById(R.id.homelayout);
        LinearLayout clockIcon = findViewById(R.id.clocklayout);
        LinearLayout logoutIcon = findViewById(R.id.signoutlayout);
        LinearLayout requestIcon = findViewById(R.id.requestlayout);

        homeIcon.setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));
        logoutIcon.setOnClickListener(v -> logout());
        clockIcon.setOnClickListener(v -> startActivity(new Intent(this, SwipingTimeActivity.class)));
        requestIcon.setOnClickListener(v -> startActivity(new Intent(this, SalaryDetailsActivity.class)));

        fetchAndDisplayUsername();
        calculateAttendanceForJanuary();
        calculateAttendanceForFebruary();
        calculateAttendanceForOctober();
        calculateAttendanceForMarch();
        calculateAttendanceForApril();
        calculateAttendanceForMay();
        calculateAttendanceForJune();
        calculateAttendanceForJuly();
        calculateAttendanceForAugust();
        calculateAttendanceForSeptember();
        calculateAttendanceForNovember();
        calculateAttendanceForDecember();

        calculateLeaveCountForJanuary();
        calculateLeaveCountForFebruary();
        calculateLeaveCountForMarch();
        calculateLeaveCountForApril();
        calculateLeaveCountForMay();
        calculateLeaveCountForJune();
        calculateLeaveCountForJuly();
        calculateLeaveCountForAugust();
        calculateLeaveCountForSeptember();
        calculateLeaveCountForOctober();
        calculateLeaveCountForNovember();
        calculateLeaveCountForDecember();

        calculateHolidayCountForJanuary();
        calculateHolidayCountForFebruary();
        calculateHolidayCountForMarch();
        calculateHolidayCountForApril();
        calculateHolidayCountForMay();
        calculateHolidayCountForJune();
        calculateHolidayCountForJuly();
        calculateHolidayCountForAugust();
        calculateHolidayCountForSeptember();
        calculateHolidayCountForOctober();
        calculateHolidayCountForNovember();
        calculateHolidayCountForDecember();

        calculateLateCountForJanuary();
        calculateLateCountForFebruary();
        calculateLateCountForMarch();
        calculateLateCountForApril();
        calculateLateCountForMay();
        calculateLateCountForJune();
        calculateLateCountForJuly();
        calculateLateCountForAugust();
        calculateLateCountForSeptember();
        calculateLateCountForOctober();
        calculateLateCountForNovember();
        calculateLateCountForDecember();

        countPermissionsForJanuary();
        countPermissionsForFebruary();
        countPermissionsForMarch();
        countPermissionsForApril();
        countPermissionsForMay();
        countPermissionsForJune();
        countPermissionsForJuly();
        countPermissionsForAugust();
        countPermissionsForSeptember();
        countPermissionsForOctober();
        countPermissionsForNovember();
        countPermissionsForDecember();

        countApprovedLateRequestsForJanuary();
        countApprovedLateRequestsForFebruary();
        countLateRequestsForMarch();
        countLateRequestsForApril();
        countLateRequestsForMay();
        countLateRequestsForJune();
        countLateRequestsForJuly();
        countLateRequestsForAugust();
        countLateRequestsForSeptember();
        countApprovedLateRequestsForOctober();
        countApprovedLateRequestsForNovember();
        countApprovedLateRequestsForDecember();
        
        countOTForJanuary();
        countOTForFebruary();
        countOTForMarch();
        countOTForApril();
        countOTForMay();
        countOTForJune();
        countOTForJuly();
        countOTForAugust();
        countOTForSeptember();
        countOTForSundaysInOctober();
        countOTForNovember();
        countOTForDecember();

    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, SignInActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void fetchAndDisplayUsername() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null) {
                mDatabase.child("Users").orderByChild("email").equalTo(email)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                        String username = userSnapshot.child("username").getValue(String.class);
                                        if (username != null) {
                                            username = username.substring(0, 1).toUpperCase() + username.substring(1).toLowerCase();
                                            welcomeTextView.setText("Welcome, " + username + "!");
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }


    // Present calculation //

    private void calculateAttendanceForJanuary() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set target month as January and get the current year
        int januaryMonth = 1; // January is 1
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                double januaryCount = 0.0;

                // Iterate over each day in the user's time records
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String dateKey = dateSnapshot.getKey();

                    try {
                        // Parse date key to check if it matches January of the current year
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.setTime(dateFormat.parse(dateKey));
                        int recordMonth = dateCalendar.get(Calendar.MONTH) + 1;
                        int recordYear = dateCalendar.get(Calendar.YEAR);

                        // Skip Sundays
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                        if (dayOfWeek == Calendar.SUNDAY) {
                            continue; // Skip Sundays
                        }

                        if (recordMonth == januaryMonth && recordYear == currentYear) {
                            // Fetch clock-in and clock-out times
                            String clockInTime = dateSnapshot.child("ClockInTime").getValue(String.class);
                            String clockOutTime = dateSnapshot.child("ClockOutTime").getValue(String.class);

                            double dayCount = 0.0;
                            // Add count for early clock-in (before or at 09:05)
                            if (clockInTime != null && isOnOrBefore(clockInTime, "09:15")) {
                                dayCount += 0.5;
                            }
                            // Add count for late clock-out (after or at 20:00)
                            if (clockOutTime != null && isOnOrAfter(clockOutTime, "17:45")) {
                                dayCount += 0.5;
                            }
                            januaryCount += dayCount;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Once the count is calculated, update the user's monthRecords node
                // The key will be "January-2024" or any other month/year you are processing
                String monthYearKey = "January-" + currentYear;
                DatabaseReference monthRecordsRef = mDatabase.child("Users").child(userId).child("monthPresentRecords").child(monthYearKey);

                // Update the value in Firebase, this will overwrite the existing value
                monthRecordsRef.setValue(januaryCount).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });

                // Optionally, update the UI with the January count
                pr1.setText(String.valueOf(januaryCount)); // Assuming pr1 is where you want to display January's count
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch January attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateAttendanceForFebruary() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set target month as February and get the current year
        int februaryMonth = 2; // February is 2
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                double februaryCount = 0.0;

                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String dateKey = dateSnapshot.getKey();

                    try {
                        // Parse date key to check if it matches February of the current year
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.setTime(dateFormat.parse(dateKey));
                        int recordMonth = dateCalendar.get(Calendar.MONTH) + 1; // Months are 0-indexed, so we add 1
                        int recordYear = dateCalendar.get(Calendar.YEAR);

                        // Skip if the day is Sunday
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                        if (dayOfWeek == Calendar.SUNDAY) {
                            continue; // Skip Sundays
                        }

                        // Check if the date is in February of the current year
                        if (recordMonth == februaryMonth && recordYear == currentYear) {
                            // Fetch clock-in and clock-out times
                            String clockInTime = dateSnapshot.child("ClockInTime").getValue(String.class);
                            String clockOutTime = dateSnapshot.child("ClockOutTime").getValue(String.class);

                            double dayCount = 0.0;
                            // Add 0.5 if clock-in time is on or before 09:05 AM
                            if (clockInTime != null && isOnOrBefore(clockInTime, "09:15")) {
                                dayCount += 0.5;
                            }
                            // Add count for late clock-out (after or at 20:00)
                            if (clockOutTime != null && isOnOrAfter(clockOutTime, "17:45")) {
                                dayCount += 0.5;
                            }
                            februaryCount += dayCount; // Accumulate the day count for February
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // After calculating the count for February, update the Firebase database
                String monthYearKey = "February-" + currentYear; // Example: "February-2024"
                DatabaseReference monthRecordsRef = mDatabase.child("Users").child(userId).child("monthPresentRecords").child(monthYearKey);

                // Update the value in Firebase
                monthRecordsRef.setValue(februaryCount).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });

                // Optionally, update the UI with the calculated February attendance count
                pr2.setText(String.valueOf(februaryCount)); // Assuming pr2 is where you want to display February's count
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch February attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateAttendanceForMarch() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set target month as March and get the current year
        int marchMonth = 3; // March is 3
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                double marchCount = 0.0;

                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String dateKey = dateSnapshot.getKey();

                    try {
                        // Parse date key to check if it matches March of the current year
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.setTime(dateFormat.parse(dateKey));
                        int recordMonth = dateCalendar.get(Calendar.MONTH) + 1; // Get month (1 for March)
                        int recordYear = dateCalendar.get(Calendar.YEAR);

                        // Skip if the day is Sunday
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                        if (dayOfWeek == Calendar.SUNDAY) {
                            continue; // Skip Sundays
                        }

                        // Check if the date is in March of the current year
                        if (recordMonth == marchMonth && recordYear == currentYear) {
                            // Fetch clock-in and clock-out times
                            String clockInTime = dateSnapshot.child("ClockInTime").getValue(String.class);
                            String clockOutTime = dateSnapshot.child("ClockOutTime").getValue(String.class);

                            double dayCount = 0.0;
                            // Add 0.5 if clock-in time is on or before 09:05 AM
                            if (clockInTime != null && isOnOrBefore(clockInTime, "09:15")) {
                                dayCount += 0.5;
                            }
                            // Add count for late clock-out (after or at 20:00)
                            if (clockOutTime != null && isOnOrAfter(clockOutTime, "17:45")) {
                                dayCount += 0.5;
                            }
                            marchCount += dayCount; // Accumulate the day count for March
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // After calculating the count for March, update the Firebase database
                String monthYearKey = "March-" + currentYear; // Example: "March-2024"
                DatabaseReference monthRecordsRef = mDatabase.child("Users").child(userId).child("monthPresentRecords").child(monthYearKey);

                // Update the value in Firebase
                monthRecordsRef.setValue(marchCount).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });

                // Optionally, update the UI with the calculated March attendance count
                pr3.setText(String.valueOf(marchCount)); // Assuming pr3 is where you want to display March's count
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch March attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateAttendanceForApril() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set target month as April and get the current year
        int aprilMonth = 4; // April is 4
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                double aprilCount = 0.0;

                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String dateKey = dateSnapshot.getKey();

                    try {
                        // Parse date key to check if it matches April of the current year
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.setTime(dateFormat.parse(dateKey));
                        int recordMonth = dateCalendar.get(Calendar.MONTH) + 1; // Months are 0-indexed, so add 1
                        int recordYear = dateCalendar.get(Calendar.YEAR);

                        // Skip if the day is Sunday
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                        if (dayOfWeek == Calendar.SUNDAY) {
                            continue; // Skip Sundays
                        }

                        // Check if the date is in April of the current year
                        if (recordMonth == aprilMonth && recordYear == currentYear) {
                            // Fetch clock-in and clock-out times
                            String clockInTime = dateSnapshot.child("ClockInTime").getValue(String.class);
                            String clockOutTime = dateSnapshot.child("ClockOutTime").getValue(String.class);

                            double dayCount = 0.0;
                            // Add 0.5 if clock-in time is on or before 09:05 AM
                            if (clockInTime != null && isOnOrBefore(clockInTime, "09:15")) {
                                dayCount += 0.5;
                            }
                            // Add count for late clock-out (after or at 20:00)
                            if (clockOutTime != null && isOnOrAfter(clockOutTime, "17:45")) {
                                dayCount += 0.5;
                            }
                            aprilCount += dayCount; // Accumulate the day count for April
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // After calculating the count for April, update the Firebase database
                String monthYearKey = "April-" + currentYear; // Example: "April-2024"
                DatabaseReference monthRecordsRef = mDatabase.child("Users").child(userId).child("monthPresentRecords").child(monthYearKey);

                // Update the value in Firebase
                monthRecordsRef.setValue(aprilCount).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });

                // Optionally, update the UI with the calculated April attendance count
                pr4.setText(String.valueOf(aprilCount)); // Assuming pr4 is where you want to display April's count
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch April attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateAttendanceForMay() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set target month as May and get the current year
        int mayMonth = 5; // May is 5
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                double mayCount = 0.0;

                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String dateKey = dateSnapshot.getKey();

                    try {
                        // Parse date key to check if it matches May of the current year
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.setTime(dateFormat.parse(dateKey));
                        int recordMonth = dateCalendar.get(Calendar.MONTH) + 1; // Months are 0-indexed, so add 1
                        int recordYear = dateCalendar.get(Calendar.YEAR);

                        // Skip if the day is Sunday
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                        if (dayOfWeek == Calendar.SUNDAY) {
                            continue; // Skip Sundays
                        }

                        // Check if the date is in May of the current year
                        if (recordMonth == mayMonth && recordYear == currentYear) {
                            // Fetch clock-in and clock-out times
                            String clockInTime = dateSnapshot.child("ClockInTime").getValue(String.class);
                            String clockOutTime = dateSnapshot.child("ClockOutTime").getValue(String.class);

                            double dayCount = 0.0;
                            // Add 0.5 if clock-in time is on or before 09:05 AM
                            if (clockInTime != null && isOnOrBefore(clockInTime, "09:15")) {
                                dayCount += 0.5;
                            }
                            // Add count for late clock-out (after or at 20:00)
                            if (clockOutTime != null && isOnOrAfter(clockOutTime, "17:45")) {
                                dayCount += 0.5;
                            }
                            mayCount += dayCount; // Accumulate the day count for May
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // After calculating the count for May, update the Firebase database
                String monthYearKey = "May-" + currentYear; // Example: "May-2024"
                DatabaseReference monthRecordsRef = mDatabase.child("Users").child(userId).child("monthPresentRecords").child(monthYearKey);

                // Update the value in Firebase
                monthRecordsRef.setValue(mayCount).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });

                // Optionally, update the UI with the calculated May attendance count
                pr5.setText(String.valueOf(mayCount)); // Assuming pr5 is where you want to display May's count
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch May attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateAttendanceForJune() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set target month as June and get the current year
        int juneMonth = 6; // June is 6
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                double juneCount = 0.0;

                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String dateKey = dateSnapshot.getKey();

                    try {
                        // Parse date key to check if it matches June of the current year
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.setTime(dateFormat.parse(dateKey));
                        int recordMonth = dateCalendar.get(Calendar.MONTH) + 1; // Months are 0-indexed, so add 1
                        int recordYear = dateCalendar.get(Calendar.YEAR);

                        // Skip if the day is Sunday
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                        if (dayOfWeek == Calendar.SUNDAY) {
                            continue; // Skip Sundays
                        }

                        // Check if the date is in June of the current year
                        if (recordMonth == juneMonth && recordYear == currentYear) {
                            // Fetch clock-in and clock-out times
                            String clockInTime = dateSnapshot.child("ClockInTime").getValue(String.class);
                            String clockOutTime = dateSnapshot.child("ClockOutTime").getValue(String.class);

                            double dayCount = 0.0;
                            // Add 0.5 if clock-in time is on or before 09:05 AM
                            if (clockInTime != null && isOnOrBefore(clockInTime, "09:15")) {
                                dayCount += 0.5;
                            }
                            // Add count for late clock-out (after or at 20:00)
                            if (clockOutTime != null && isOnOrAfter(clockOutTime, "17:45")) {
                                dayCount += 0.5;
                            }
                            juneCount += dayCount; // Accumulate the day count for June
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // After calculating the count for June, update the Firebase database
                String monthYearKey = "June-" + currentYear; // Example: "June-2024"
                DatabaseReference monthRecordsRef = mDatabase.child("Users").child(userId).child("monthPresentRecords").child(monthYearKey);

                // Update the value in Firebase
                monthRecordsRef.setValue(juneCount).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });

                // Optionally, update the UI with the calculated June attendance count
                pr6.setText(String.valueOf(juneCount)); // Assuming pr6 is where you want to display June's count
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch June attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateAttendanceForJuly() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set target month as July and get the current year
        int julyMonth = 7; // July is 7
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                double julyCount = 0.0;

                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String dateKey = dateSnapshot.getKey();

                    try {
                        // Parse date key to check if it matches July of the current year
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.setTime(dateFormat.parse(dateKey));
                        int recordMonth = dateCalendar.get(Calendar.MONTH) + 1; // Months are 0-indexed, so add 1
                        int recordYear = dateCalendar.get(Calendar.YEAR);

                        // Skip if the day is Sunday
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                        if (dayOfWeek == Calendar.SUNDAY) {
                            continue; // Skip Sundays
                        }

                        // Check if the date is in July of the current year
                        if (recordMonth == julyMonth && recordYear == currentYear) {
                            // Fetch clock-in and clock-out times
                            String clockInTime = dateSnapshot.child("ClockInTime").getValue(String.class);
                            String clockOutTime = dateSnapshot.child("ClockOutTime").getValue(String.class);

                            double dayCount = 0.0;
                            // Add 0.5 if clock-in time is on or before 09:05 AM
                            if (clockInTime != null && isOnOrBefore(clockInTime, "09:15")) {
                                dayCount += 0.5;
                            }
                            // Add count for late clock-out (after or at 20:00)
                            if (clockOutTime != null && isOnOrAfter(clockOutTime, "17:45")) {
                                dayCount += 0.5;
                            }
                            julyCount += dayCount; // Accumulate the day count for July
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // After calculating the count for July, update the Firebase database
                String monthYearKey = "July-" + currentYear; // Example: "July-2024"
                DatabaseReference monthRecordsRef = mDatabase.child("Users").child(userId).child("monthPresentRecords").child(monthYearKey);

                // Update the value in Firebase
                monthRecordsRef.setValue(julyCount).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });

                // Optionally, update the UI with the calculated July attendance count
                pr7.setText(String.valueOf(julyCount)); // Assuming pr7 is where you want to display July's count
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch July attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateAttendanceForAugust() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set target month as August and get the current year
        int augustMonth = 8; // August is 8
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                double augustCount = 0.0;

                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String dateKey = dateSnapshot.getKey();

                    try {
                        // Parse date key to check if it matches August of the current year
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.setTime(dateFormat.parse(dateKey));
                        int recordMonth = dateCalendar.get(Calendar.MONTH) + 1; // Calendar months are 0-indexed
                        int recordYear = dateCalendar.get(Calendar.YEAR);

                        // Skip if the day is Sunday
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                        if (dayOfWeek == Calendar.SUNDAY) {
                            continue; // Skip Sundays
                        }

                        // If the record is from August of the current year
                        if (recordMonth == augustMonth && recordYear == currentYear) {
                            // Fetch clock-in and clock-out times
                            String clockInTime = dateSnapshot.child("ClockInTime").getValue(String.class);
                            String clockOutTime = dateSnapshot.child("ClockOutTime").getValue(String.class);

                            double dayCount = 0.0;
                            // Add 0.5 if clock-in time is on or before 09:05 AM
                            if (clockInTime != null && isOnOrBefore(clockInTime, "09:15")) {
                                dayCount += 0.5;
                            }
                            // Add count for late clock-out (after or at 20:00)
                            if (clockOutTime != null && isOnOrAfter(clockOutTime, "17:45")) {
                                dayCount += 0.5;
                            }
                            augustCount += dayCount; // Accumulate the day count for August
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // After calculating the count for August, update the Firebase database
                String monthYearKey = "August-" + currentYear; // Example: "August-2024"
                DatabaseReference monthRecordsRef = mDatabase.child("Users").child(userId).child("monthPresentRecords").child(monthYearKey);

                // Update the value in Firebase
                monthRecordsRef.setValue(augustCount).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });

                // Optionally, update the UI with the calculated August attendance count
                pr8.setText(String.valueOf(augustCount)); // Assuming pr8 is where you want to display August's count
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch August attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateAttendanceForSeptember() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set target month as September and get the current year
        int septemberMonth = 9; // September is 9
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                double septemberCount = 0.0;

                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String dateKey = dateSnapshot.getKey();

                    try {
                        // Parse date key to check if it matches September of the current year
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.setTime(dateFormat.parse(dateKey));
                        int recordMonth = dateCalendar.get(Calendar.MONTH) + 1; // Calendar months are 0-indexed
                        int recordYear = dateCalendar.get(Calendar.YEAR);

                        // Skip if the day is Sunday
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                        if (dayOfWeek == Calendar.SUNDAY) {
                            continue; // Skip Sundays
                        }

                        if (recordMonth == septemberMonth && recordYear == currentYear) {
                            // Fetch clock in and clock out times
                            String clockInTime = dateSnapshot.child("ClockInTime").getValue(String.class);
                            String clockOutTime = dateSnapshot.child("ClockOutTime").getValue(String.class);

                            double dayCount = 0.0;
                            // Add 0.5 if clock-in time is on or before 09:05 AM
                            if (clockInTime != null && isOnOrBefore(clockInTime, "09:15")) {
                                dayCount += 0.5;
                            }
                            // Add count for late clock-out (after or at 20:00)
                            if (clockOutTime != null && isOnOrAfter(clockOutTime, "17:45")) {
                                dayCount += 0.5;
                            }
                            septemberCount += dayCount; // Accumulate the day count for September
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // After calculating the count for September, update the Firebase database
                String monthYearKey = "September-" + currentYear; // Example: "September-2024"
                DatabaseReference monthRecordsRef = mDatabase.child("Users").child(userId).child("monthPresentRecords").child(monthYearKey);

                // Update the value in Firebase
                monthRecordsRef.setValue(septemberCount).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });

                // Optionally, update the UI with the calculated September attendance count
                pr9.setText(String.valueOf(septemberCount)); // Assuming pr9 is where you want to display September's count
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch September attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateAttendanceForOctober() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set target month as October and get the current year
        int octoberMonth = 10; // October is 10
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                double octoberCount = 0.0;

                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String dateKey = dateSnapshot.getKey();

                    try {
                        // Parse date key to check if it matches October of the current year
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.setTime(dateFormat.parse(dateKey));
                        int recordMonth = dateCalendar.get(Calendar.MONTH) + 1; // Get month (1 for October)
                        int recordYear = dateCalendar.get(Calendar.YEAR);

                        // Skip if the day is Sunday
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                        if (dayOfWeek == Calendar.SUNDAY) {
                            continue; // Skip Sundays
                        }

                        if (recordMonth == octoberMonth && recordYear == currentYear) {
                            // Fetch clock-in and clock-out times
                            String clockInTime = dateSnapshot.child("ClockInTime").getValue(String.class);
                            String clockOutTime = dateSnapshot.child("ClockOutTime").getValue(String.class);

                            double dayCount = 0.0;
                            // Add 0.5 if clock-in is before or at 09:05
                            if (clockInTime != null && isOnOrBefore(clockInTime, "09:15")) {
                                dayCount += 0.5;
                            }
                            // Add count for late clock-out (after or at 20:00)
                            if (clockOutTime != null && isOnOrAfter(clockOutTime, "17:45")) {
                                dayCount += 0.5;
                            }
                            octoberCount += dayCount;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // After calculating the count, insert it into Firebase under the "monthRecords" node
                String monthYearKey = "October-" + currentYear; // Use "October-2024" as the key
                DatabaseReference monthRecordsRef = mDatabase.child("Users").child(userId).child("monthPresentRecords").child(monthYearKey);

                // Update the value of the "October-2024" node with the attendance count
                monthRecordsRef.setValue(octoberCount).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });

                // Optionally, update the UI with the calculated October attendance count
                pr10.setText(String.valueOf(octoberCount)); // Assuming pr10 is where you want to display October's count
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch October attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateAttendanceForNovember() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set target month as November and get the current year
        int novemberMonth = 11; // November is 11
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                double novemberCount = 0.0;

                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String dateKey = dateSnapshot.getKey();

                    try {
                        // Parse date key to check if it matches November of the current year
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.setTime(dateFormat.parse(dateKey));
                        int recordMonth = dateCalendar.get(Calendar.MONTH) + 1; // Calendar months are 0-indexed
                        int recordYear = dateCalendar.get(Calendar.YEAR);

                        // Skip if the day is Sunday
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                        if (dayOfWeek == Calendar.SUNDAY) {
                            continue; // Skip Sundays
                        }

                        if (recordMonth == novemberMonth && recordYear == currentYear) {
                            // Fetch clock-in and clock-out times
                            String clockInTime = dateSnapshot.child("ClockInTime").getValue(String.class);
                            String clockOutTime = dateSnapshot.child("ClockOutTime").getValue(String.class);

                            double dayCount = 0.0;
                            // Add 0.5 if clock-in time is on or before 09:05 AM
                            if (clockInTime != null && isOnOrBefore(clockInTime, "09:15")) {
                                dayCount += 0.5;
                            }
                            // Add count for late clock-out (after or at 20:00)
                            if (clockOutTime != null && isOnOrAfter(clockOutTime, "17:45")) {
                                dayCount += 0.5;
                            }
                            novemberCount += dayCount; // Accumulate the day count for November
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // After calculating the count for November, update the Firebase database
                String monthYearKey = "November-" + currentYear; // Example: "November-2024"
                DatabaseReference monthRecordsRef = mDatabase.child("Users").child(userId).child("monthPresentRecords").child(monthYearKey);

                // Update the value in Firebase
                monthRecordsRef.setValue(novemberCount).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });

                // Optionally, update the UI with the calculated November attendance count
                pr11.setText(String.valueOf(novemberCount)); // Assuming pr11 is where you want to display November's count
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch November attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateAttendanceForDecember() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set target month as December and get the current year
        int decemberMonth = 12; // December is 12
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                double decemberCount = 0.0;

                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String dateKey = dateSnapshot.getKey();

                    try {
                        // Parse date key to check if it matches December of the current year
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.setTime(dateFormat.parse(dateKey));
                        int recordMonth = dateCalendar.get(Calendar.MONTH) + 1; // Calendar months are 0-indexed
                        int recordYear = dateCalendar.get(Calendar.YEAR);

                        // Skip if the day is Sunday
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                        if (dayOfWeek == Calendar.SUNDAY) {
                            continue; // Skip Sundays
                        }

                        if (recordMonth == decemberMonth && recordYear == currentYear) {
                            // Fetch clock-in and clock-out times
                            String clockInTime = dateSnapshot.child("ClockInTime").getValue(String.class);
                            String clockOutTime = dateSnapshot.child("ClockOutTime").getValue(String.class);

                            double dayCount = 0.0;
                            // Add 0.5 if clock-in time is on or before 09:15 AM
                            if (clockInTime != null && isOnOrBefore(clockInTime, "09:15")) {
                                dayCount += 0.5;
                            }
                            // Add count for late clock-out (after or at 17:45)
                            if (clockOutTime != null && isOnOrAfter(clockOutTime, "17:45")) {
                                dayCount += 0.5;
                            }
                            decemberCount += dayCount; // Accumulate the day count for December
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // After calculating the count for December, update the Firebase database
                String monthYearKey = "December-" + currentYear; // Example: "December-2024"
                DatabaseReference monthRecordsRef = mDatabase.child("Users").child(userId).child("monthPresentRecords").child(monthYearKey);

                // Update the value in Firebase
                monthRecordsRef.setValue(decemberCount).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });

                // Optionally, update the UI with the calculated December attendance count
                pr12.setText(String.valueOf(decemberCount)); // Assuming pr12 is where you want to display December's count
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch December attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Present calculation //


    // Absent calculation //

    private void calculateLeaveCountForJanuary() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Get the current year and day
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Set to January (January is 0 in Calendar)
        int januaryMonth = 1; // January is the 1st month (1-based index)
        calendar.set(Calendar.MONTH, Calendar.JANUARY);

        // Get the total days in January
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set to the first day of January
        int totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Initialize total leave count
        final double[] totalLeave = {0.0};

        // Fetch attendance data for each day in January
        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInMonth; day++) {
                    // Skip if the date is in the future
                    if (calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) && day > currentDayOfMonth) {
                        break;
                    }

                    // Format the date key for January
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, januaryMonth, currentYear);
                    double leaveForDay = 0.0;

                    // Exclude Sundays
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, januaryMonth - 1, day); // Set the date for comparison
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek == Calendar.SUNDAY) {
                        continue; // Skip Sundays
                    }

                    // Check if data for the date exists in Firebase
                    if (!dataSnapshot.hasChild(dateKey)) {
                        leaveForDay = 1.0; // Count as 0.5 leave if date is missing
                    } else {
                        // Get ClockInTime and ClockOutTime
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            Date cutoffClockIn = sdf.parse("10:30");
                            Date cutoffClockOut = sdf.parse("17:46");

                            // Check clock-in time
                            if (clockInTime == null || sdf.parse(clockInTime).after(cutoffClockIn)) {
                                leaveForDay += 0.5;
                            }

                            // Check clock-out time
                            if (clockOutTime == null || sdf.parse(clockOutTime).before(cutoffClockOut)) {
                                leaveForDay += 0.5;
                            }
                        } catch (ParseException e) {
                            Log.e("LeaveCount", "Error parsing clock-in or clock-out time", e);
                        }
                    }

                    // Add leave count for the day to the total
                    totalLeave[0] += leaveForDay;
                }

                // Update TextView with total leave count for January
                TextView leaveTextView = findViewById(R.id.lv1); // Make sure this ID matches your layout
                leaveTextView.setText(String.format(Locale.getDefault(), "%.1f", totalLeave[0]));

                // Log the result for debugging
                Log.d("LeaveCount", "Leave count for January (up to current day): " + totalLeave[0]);

                // Now update Firebase with the calculated leave count for January
                String monthYearKey = "January-" + currentYear; // Example: "January-2024"
                DatabaseReference monthLeaveRecordsRef = mDatabase.child("Users").child(userId).child("monthLeaveRecords").child(monthYearKey);

                // Update the value in Firebase
                monthLeaveRecordsRef.setValue(totalLeave[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLeaveCountForFebruary() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Get the current year and day
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Set to October
        int februaryMonth = 2; // October is the 10th month (1-based index)
        calendar.set(Calendar.MONTH, Calendar.FEBRUARY);

        // Get the total days in October
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set to the first day of October
        int totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Initialize total leave count
        final double[] totalLeave = {0.0};

        // Fetch attendance data for each day in October
        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInMonth; day++) {
                    // Skip if the date is in the future
                    if (calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) && day > currentDayOfMonth) {
                        break;
                    }

                    // Format the date key
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, februaryMonth, currentYear);
                    double leaveForDay = 0.0;

                    // Exclude Sundays
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, februaryMonth - 1, day); // Set the date for comparison
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek == Calendar.SUNDAY) {
                        continue; // Skip Sundays
                    }

                    // Check if data for the date exists in Firebase
                    if (!dataSnapshot.hasChild(dateKey)) {
                        leaveForDay = 1.0; // Count as 0.5 leave if date is missing
                    } else {
                        // Get ClockInTime and ClockOutTime
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            Date cutoffClockIn = sdf.parse("10:30");
                            Date cutoffClockOut = sdf.parse("17:46");

                            // Check clock-in time
                            if (clockInTime == null || sdf.parse(clockInTime).after(cutoffClockIn)) {
                                leaveForDay += 0.5;
                            }

                            // Check clock-out time
                            if (clockOutTime == null || sdf.parse(clockOutTime).before(cutoffClockOut)) {
                                leaveForDay += 0.5;
                            }
                        } catch (ParseException e) {
                            Log.e("LeaveCount", "Error parsing clock-in or clock-out time", e);
                        }
                    }

                    // Add leave count for the day to the total
                    totalLeave[0] += leaveForDay;
                }

                // Update TextView with total leave count for October
                TextView leaveTextView = findViewById(R.id.lv2); // Changed to lv10
                leaveTextView.setText(String.format(Locale.getDefault(), "%.1f", totalLeave[0]));

                // Log the result for debugging
                Log.d("LeaveCount", "Leave count for October (up to current day): " + totalLeave[0]);

                String monthYearKey = "February-" + currentYear; // Example: "January-2024"
                DatabaseReference monthLeaveRecordsRef = mDatabase.child("Users").child(userId).child("monthLeaveRecords").child(monthYearKey);

                // Update the value in Firebase
                monthLeaveRecordsRef.setValue(totalLeave[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLeaveCountForMarch() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Get the current year and day
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Set to October
        int marchMonth = 3; // October is the 10th month (1-based index)
        calendar.set(Calendar.MONTH, Calendar.MARCH);

        // Get the total days in October
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set to the first day of October
        int totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Initialize total leave count
        final double[] totalLeave = {0.0};

        // Fetch attendance data for each day in October
        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInMonth; day++) {
                    // Skip if the date is in the future
                    if (calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) && day > currentDayOfMonth) {
                        break;
                    }

                    // Format the date key
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, marchMonth, currentYear);
                    double leaveForDay = 0.0;

                    // Exclude Sundays
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, marchMonth - 1, day); // Set the date for comparison
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek == Calendar.SUNDAY) {
                        continue; // Skip Sundays
                    }

                    // Check if data for the date exists in Firebase
                    if (!dataSnapshot.hasChild(dateKey)) {
                        leaveForDay = 1.0; // Count as 0.5 leave if date is missing
                    } else {
                        // Get ClockInTime and ClockOutTime
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            Date cutoffClockIn = sdf.parse("10:30");
                            Date cutoffClockOut = sdf.parse("17:46");

                            // Check clock-in time
                            if (clockInTime == null || sdf.parse(clockInTime).after(cutoffClockIn)) {
                                leaveForDay += 0.5;
                            }

                            // Check clock-out time
                            if (clockOutTime == null || sdf.parse(clockOutTime).before(cutoffClockOut)) {
                                leaveForDay += 0.5;
                            }
                        } catch (ParseException e) {
                            Log.e("LeaveCount", "Error parsing clock-in or clock-out time", e);
                        }
                    }


                    // Add leave count for the day to the total
                    totalLeave[0] += leaveForDay;
                }

                // Update TextView with total leave count for October
                TextView leaveTextView = findViewById(R.id.lv3); // Changed to lv10
                leaveTextView.setText(String.format(Locale.getDefault(), "%.1f", totalLeave[0]));

                // Log the result for debugging
                Log.d("LeaveCount", "Leave count for October (up to current day): " + totalLeave[0]);

                String monthYearKey = "March-" + currentYear; // Example: "January-2024"
                DatabaseReference monthLeaveRecordsRef = mDatabase.child("Users").child(userId).child("monthLeaveRecords").child(monthYearKey);

                // Update the value in Firebase
                monthLeaveRecordsRef.setValue(totalLeave[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLeaveCountForApril() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Get the current year and day
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Set to October
        int aprilMonth = 4; // October is the 10th month (1-based index)
        calendar.set(Calendar.MONTH, Calendar.APRIL);

        // Get the total days in October
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set to the first day of October
        int totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Initialize total leave count
        final double[] totalLeave = {0.0};

        // Fetch attendance data for each day in October
        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInMonth; day++) {
                    // Skip if the date is in the future
                    if (calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) && day > currentDayOfMonth) {
                        break;
                    }

                    // Format the date key
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, aprilMonth, currentYear);
                    double leaveForDay = 0.0;

                    // Exclude Sundays
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, aprilMonth - 1, day); // Set the date for comparison
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek == Calendar.SUNDAY) {
                        continue; // Skip Sundays
                    }

                    // Check if data for the date exists in Firebase
                    if (!dataSnapshot.hasChild(dateKey)) {
                        leaveForDay = 1.0; // Count as 0.5 leave if date is missing
                    } else {
                        // Get ClockInTime and ClockOutTime
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            Date cutoffClockIn = sdf.parse("10:30");
                            Date cutoffClockOut = sdf.parse("17:46");

                            // Check clock-in time
                            if (clockInTime == null || sdf.parse(clockInTime).after(cutoffClockIn)) {
                                leaveForDay += 0.5;
                            }

                            // Check clock-out time
                            if (clockOutTime == null || sdf.parse(clockOutTime).before(cutoffClockOut)) {
                                leaveForDay += 0.5;
                            }
                        } catch (ParseException e) {
                            Log.e("LeaveCount", "Error parsing clock-in or clock-out time", e);
                        }
                    }


                    // Add leave count for the day to the total
                    totalLeave[0] += leaveForDay;
                }

                // Update TextView with total leave count for October
                TextView leaveTextView = findViewById(R.id.lv4); // Changed to lv10
                leaveTextView.setText(String.format(Locale.getDefault(), "%.1f", totalLeave[0]));

                // Log the result for debugging
                Log.d("LeaveCount", "Leave count for October (up to current day): " + totalLeave[0]);

                String monthYearKey = "April-" + currentYear; // Example: "January-2024"
                DatabaseReference monthLeaveRecordsRef = mDatabase.child("Users").child(userId).child("monthLeaveRecords").child(monthYearKey);

                // Update the value in Firebase
                monthLeaveRecordsRef.setValue(totalLeave[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLeaveCountForMay() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Get the current year and day
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Set to October
        int mayMonth = 5; // October is the 10th month (1-based index)
        calendar.set(Calendar.MONTH, Calendar.OCTOBER);

        // Get the total days in October
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set to the first day of October
        int totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Initialize total leave count
        final double[] totalLeave = {0.0};

        // Fetch attendance data for each day in October
        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInMonth; day++) {
                    // Skip if the date is in the future
                    if (calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) && day > currentDayOfMonth) {
                        break;
                    }

                    // Format the date key
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, mayMonth, currentYear);
                    double leaveForDay = 0.0;

                    // Exclude Sundays
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, mayMonth - 1, day); // Set the date for comparison
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek == Calendar.SUNDAY) {
                        continue; // Skip Sundays
                    }

                    // Check if data for the date exists in Firebase
                    if (!dataSnapshot.hasChild(dateKey)) {
                        leaveForDay = 1.0; // Count as 0.5 leave if date is missing
                    } else {
                        // Get ClockInTime and ClockOutTime
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            Date cutoffClockIn = sdf.parse("10:30");
                            Date cutoffClockOut = sdf.parse("17:46");

                            // Check clock-in time
                            if (clockInTime == null || sdf.parse(clockInTime).after(cutoffClockIn)) {
                                leaveForDay += 0.5;
                            }

                            // Check clock-out time
                            if (clockOutTime == null || sdf.parse(clockOutTime).before(cutoffClockOut)) {
                                leaveForDay += 0.5;
                            }
                        } catch (ParseException e) {
                            Log.e("LeaveCount", "Error parsing clock-in or clock-out time", e);
                        }
                    }


                    // Add leave count for the day to the total
                    totalLeave[0] += leaveForDay;
                }

                // Update TextView with total leave count for October
                TextView leaveTextView = findViewById(R.id.lv5); // Changed to lv10
                leaveTextView.setText(String.format(Locale.getDefault(), "%.1f", totalLeave[0]));

                // Log the result for debugging
                Log.d("LeaveCount", "Leave count for October (up to current day): " + totalLeave[0]);

                String monthYearKey = "May-" + currentYear; // Example: "January-2024"
                DatabaseReference monthLeaveRecordsRef = mDatabase.child("Users").child(userId).child("monthLeaveRecords").child(monthYearKey);

                // Update the value in Firebase
                monthLeaveRecordsRef.setValue(totalLeave[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLeaveCountForJune() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Get the current year and day
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Set to October
        int juneMonth = 6; // October is the 10th month (1-based index)
        calendar.set(Calendar.MONTH, Calendar.OCTOBER);

        // Get the total days in October
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set to the first day of October
        int totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Initialize total leave count
        final double[] totalLeave = {0.0};

        // Fetch attendance data for each day in October
        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInMonth; day++) {
                    // Skip if the date is in the future
                    if (calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) && day > currentDayOfMonth) {
                        break;
                    }

                    // Format the date key
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, juneMonth, currentYear);
                    double leaveForDay = 0.0;

                    // Exclude Sundays
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, juneMonth - 1, day); // Set the date for comparison
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek == Calendar.SUNDAY) {
                        continue; // Skip Sundays
                    }

                    // Check if data for the date exists in Firebase
                    if (!dataSnapshot.hasChild(dateKey)) {
                        leaveForDay = 1.0; // Count as 0.5 leave if date is missing
                    } else {
                        // Get ClockInTime and ClockOutTime
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            Date cutoffClockIn = sdf.parse("10:30");
                            Date cutoffClockOut = sdf.parse("17:46");

                            // Check clock-in time
                            if (clockInTime == null || sdf.parse(clockInTime).after(cutoffClockIn)) {
                                leaveForDay += 0.5;
                            }

                            // Check clock-out time
                            if (clockOutTime == null || sdf.parse(clockOutTime).before(cutoffClockOut)) {
                                leaveForDay += 0.5;
                            }
                        } catch (ParseException e) {
                            Log.e("LeaveCount", "Error parsing clock-in or clock-out time", e);
                        }
                    }

                    // Add leave count for the day to the total
                    totalLeave[0] += leaveForDay;
                }

                // Update TextView with total leave count for October
                TextView leaveTextView = findViewById(R.id.lv6); // Changed to lv10
                leaveTextView.setText(String.format(Locale.getDefault(), "%.1f", totalLeave[0]));

                // Log the result for debugging
                Log.d("LeaveCount", "Leave count for October (up to current day): " + totalLeave[0]);

                String monthYearKey = "June-" + currentYear; // Example: "January-2024"
                DatabaseReference monthLeaveRecordsRef = mDatabase.child("Users").child(userId).child("monthLeaveRecords").child(monthYearKey);

                // Update the value in Firebase
                monthLeaveRecordsRef.setValue(totalLeave[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLeaveCountForJuly() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Get the current year and day
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Set to October
        int julyMonth = 7; // October is the 10th month (1-based index)
        calendar.set(Calendar.MONTH, Calendar.JULY);

        // Get the total days in October
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set to the first day of October
        int totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Initialize total leave count
        final double[] totalLeave = {0.0};

        // Fetch attendance data for each day in October
        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInMonth; day++) {
                    // Skip if the date is in the future
                    if (calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) && day > currentDayOfMonth) {
                        break;
                    }

                    // Format the date key
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, julyMonth, currentYear);
                    double leaveForDay = 0.0;

                    // Exclude Sundays
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, julyMonth - 1, day); // Set the date for comparison
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek == Calendar.SUNDAY) {
                        continue; // Skip Sundays
                    }

                    // Check if data for the date exists in Firebase
                    if (!dataSnapshot.hasChild(dateKey)) {
                        leaveForDay = 1.0; // Count as 0.5 leave if date is missing
                    } else {
                        // Get ClockInTime and ClockOutTime
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            Date cutoffClockIn = sdf.parse("10:30");
                            Date cutoffClockOut = sdf.parse("17:46");

                            // Check clock-in time
                            if (clockInTime == null || sdf.parse(clockInTime).after(cutoffClockIn)) {
                                leaveForDay += 0.5;
                            }

                            // Check clock-out time
                            if (clockOutTime == null || sdf.parse(clockOutTime).before(cutoffClockOut)) {
                                leaveForDay += 0.5;
                            }
                        } catch (ParseException e) {
                            Log.e("LeaveCount", "Error parsing clock-in or clock-out time", e);
                        }
                    }


                    // Add leave count for the day to the total
                    totalLeave[0] += leaveForDay;
                }

                // Update TextView with total leave count for October
                TextView leaveTextView = findViewById(R.id.lv7); // Changed to lv10
                leaveTextView.setText(String.format(Locale.getDefault(), "%.1f", totalLeave[0]));

                // Log the result for debugging
                Log.d("LeaveCount", "Leave count for October (up to current day): " + totalLeave[0]);

                String monthYearKey = "July-" + currentYear; // Example: "January-2024"
                DatabaseReference monthLeaveRecordsRef = mDatabase.child("Users").child(userId).child("monthLeaveRecords").child(monthYearKey);

                // Update the value in Firebase
                monthLeaveRecordsRef.setValue(totalLeave[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLeaveCountForAugust() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Get the current year and day
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Set to October
        int augustMonth = 8; // October is the 10th month (1-based index)
        calendar.set(Calendar.MONTH, Calendar.AUGUST);

        // Get the total days in October
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set to the first day of October
        int totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Initialize total leave count
        final double[] totalLeave = {0.0};

        // Fetch attendance data for each day in October
        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInMonth; day++) {
                    // Skip if the date is in the future
                    if (calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) && day > currentDayOfMonth) {
                        break;
                    }

                    // Format the date key
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, augustMonth, currentYear);
                    double leaveForDay = 0.0;

                    // Exclude Sundays
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, augustMonth - 1, day); // Set the date for comparison
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek == Calendar.SUNDAY) {
                        continue; // Skip Sundays
                    }

                    // Check if data for the date exists in Firebase
                    if (!dataSnapshot.hasChild(dateKey)) {
                        leaveForDay = 1.0; // Count as 0.5 leave if date is missing
                    } else {
                        // Get ClockInTime and ClockOutTime
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            Date cutoffClockIn = sdf.parse("10:30");
                            Date cutoffClockOut = sdf.parse("17:46");

                            // Check clock-in time
                            if (clockInTime == null || sdf.parse(clockInTime).after(cutoffClockIn)) {
                                leaveForDay += 0.5;
                            }

                            // Check clock-out time
                            if (clockOutTime == null || sdf.parse(clockOutTime).before(cutoffClockOut)) {
                                leaveForDay += 0.5;
                            }
                        } catch (ParseException e) {
                            Log.e("LeaveCount", "Error parsing clock-in or clock-out time", e);
                        }
                    }


                    // Add leave count for the day to the total
                    totalLeave[0] += leaveForDay;
                }

                // Update TextView with total leave count for October
                TextView leaveTextView = findViewById(R.id.lv8); // Changed to lv10
                leaveTextView.setText(String.format(Locale.getDefault(), "%.1f", totalLeave[0]));

                // Log the result for debugging
                Log.d("LeaveCount", "Leave count for October (up to current day): " + totalLeave[0]);

                String monthYearKey = "August-" + currentYear; // Example: "January-2024"
                DatabaseReference monthLeaveRecordsRef = mDatabase.child("Users").child(userId).child("monthLeaveRecords").child(monthYearKey);

                // Update the value in Firebase
                monthLeaveRecordsRef.setValue(totalLeave[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLeaveCountForSeptember() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Get the current year and day
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Set to October
        int septemberMonth = 9; // October is the 10th month (1-based index)
        calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);

        // Get the total days in October
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set to the first day of October
        int totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Initialize total leave count
        final double[] totalLeave = {0.0};

        // Fetch attendance data for each day in October
        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInMonth; day++) {
                    // Skip if the date is in the future
                    if (calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) && day > currentDayOfMonth) {
                        break;
                    }

                    // Format the date key
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, septemberMonth, currentYear);
                    double leaveForDay = 0.0;

                    // Exclude Sundays
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, septemberMonth - 1, day); // Set the date for comparison
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek == Calendar.SUNDAY) {
                        continue; // Skip Sundays
                    }

                    // Check if data for the date exists in Firebase
                    if (!dataSnapshot.hasChild(dateKey)) {
                        leaveForDay = 1.0; // Count as 0.5 leave if date is missing
                    } else {
                        // Get ClockInTime and ClockOutTime
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            Date cutoffClockIn = sdf.parse("10:30");
                            Date cutoffClockOut = sdf.parse("17:46");

                            // Check clock-in time
                            if (clockInTime == null || sdf.parse(clockInTime).after(cutoffClockIn)) {
                                leaveForDay += 0.5;
                            }

                            // Check clock-out time
                            if (clockOutTime == null || sdf.parse(clockOutTime).before(cutoffClockOut)) {
                                leaveForDay += 0.5;
                            }
                        } catch (ParseException e) {
                            Log.e("LeaveCount", "Error parsing clock-in or clock-out time", e);
                        }
                    }


                    // Add leave count for the day to the total
                    totalLeave[0] += leaveForDay;
                }

                // Update TextView with total leave count for October
                TextView leaveTextView = findViewById(R.id.lv9); // Changed to lv10
                leaveTextView.setText(String.format(Locale.getDefault(), "%.1f", totalLeave[0]));

                // Log the result for debugging
                Log.d("LeaveCount", "Leave count for October (up to current day): " + totalLeave[0]);

                String monthYearKey = "September-" + currentYear; // Example: "January-2024"
                DatabaseReference monthLeaveRecordsRef = mDatabase.child("Users").child(userId).child("monthLeaveRecords").child(monthYearKey);

                // Update the value in Firebase
                monthLeaveRecordsRef.setValue(totalLeave[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLeaveCountForOctober() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Get the current year and day
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Set to October
        int octoberMonth = 10; // October is the 10th month (1-based index)
        calendar.set(Calendar.MONTH, Calendar.OCTOBER);

        // Get the total days in October
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set to the first day of October
        int totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Initialize total leave count
        final double[] totalLeave = {0.0};

        // Fetch attendance data for each day in October
        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInMonth; day++) {
                    // Skip if the date is in the future
                    if (calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) && day > currentDayOfMonth) {
                        break;
                    }

                    // Format the date key
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, octoberMonth, currentYear);
                    double leaveForDay = 0.0;

                    // Exclude Sundays
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, octoberMonth - 1, day); // Set the date for comparison
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek == Calendar.SUNDAY) {
                        continue; // Skip Sundays
                    }

                    // Check if data for the date exists in Firebase
                    if (!dataSnapshot.hasChild(dateKey)) {
                        leaveForDay = 1.0; // Count as 0.5 leave if date is missing
                    } else {
                        // Get ClockInTime and ClockOutTime
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            Date cutoffClockIn = sdf.parse("10:30");
                            Date cutoffClockOut = sdf.parse("17:46");

                            // Check clock-in time
                            if (clockInTime == null || sdf.parse(clockInTime).after(cutoffClockIn)) {
                                leaveForDay += 0.5;
                            }

                            // Check clock-out time
                            if (clockOutTime == null || sdf.parse(clockOutTime).before(cutoffClockOut)) {
                                leaveForDay += 0.5;
                            }
                        } catch (ParseException e) {
                            Log.e("LeaveCount", "Error parsing clock-in or clock-out time", e);
                        }
                    }

                    // Add leave count for the day to the total
                    totalLeave[0] += leaveForDay;
                }

                // Update TextView with total leave count for October
                TextView leaveTextView = findViewById(R.id.lv10); // Changed to lv10
                leaveTextView.setText(String.format(Locale.getDefault(), "%.1f", totalLeave[0]));

                // Log the result for debugging
                Log.d("LeaveCount", "Leave count for October (up to current day): " + totalLeave[0]);

                String monthYearKey = "October-" + currentYear; // Example: "January-2024"
                DatabaseReference monthLeaveRecordsRef = mDatabase.child("Users").child(userId).child("monthLeaveRecords").child(monthYearKey);

                // Update the value in Firebase
                monthLeaveRecordsRef.setValue(totalLeave[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLeaveCountForNovember() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Get the current year and day
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Set to October
        int novemberMonth = 11;
        calendar.set(Calendar.MONTH, Calendar.NOVEMBER);

        // Get the total days in October
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set to the first day of October
        int totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Initialize total leave count
        final double[] totalLeave = {0.0};

        // Fetch attendance data for each day in October
        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInMonth; day++) {
                    // Skip if the date is in the future
                    if (calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) && day > currentDayOfMonth) {
                        break;
                    }

                    // Format the date key
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, novemberMonth, currentYear);
                    double leaveForDay = 0.0;

                    // Exclude Sundays
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, novemberMonth - 1, day); // Set the date for comparison
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek == Calendar.SUNDAY) {
                        continue; // Skip Sundays
                    }

                    // Check if data for the date exists in Firebase
                    if (!dataSnapshot.hasChild(dateKey)) {
                        leaveForDay = 1.0; // Count as 0.5 leave if date is missing
                    } else {
                        // Get ClockInTime and ClockOutTime
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            Date cutoffClockIn = sdf.parse("10:30");
                            Date cutoffClockOut = sdf.parse("17:46");

                            // Check clock-in time
                            if (clockInTime == null || sdf.parse(clockInTime).after(cutoffClockIn)) {
                                leaveForDay += 0.5;
                            }

                            // Check clock-out time
                            if (clockOutTime == null || sdf.parse(clockOutTime).before(cutoffClockOut)) {
                                leaveForDay += 0.5;
                            }
                        } catch (ParseException e) {
                            Log.e("LeaveCount", "Error parsing clock-in or clock-out time", e);
                        }
                    }


                    // Add leave count for the day to the total
                    totalLeave[0] += leaveForDay;
                }

                // Update TextView with total leave count for October
                TextView leaveTextView = findViewById(R.id.lv11); // Changed to lv10
                leaveTextView.setText(String.format(Locale.getDefault(), "%.1f", totalLeave[0]));

                // Log the result for debugging
                Log.d("LeaveCount", "Leave count for October (up to current day): " + totalLeave[0]);

                String monthYearKey = "November-" + currentYear; // Example: "January-2024"
                DatabaseReference monthLeaveRecordsRef = mDatabase.child("Users").child(userId).child("monthLeaveRecords").child(monthYearKey);

                // Update the value in Firebase
                monthLeaveRecordsRef.setValue(totalLeave[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLeaveCountForDecember() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Get the current year and day
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Set to December
        int decemberMonth = 12;
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);

        // Get the total days in December
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set to the first day of December
        int totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Initialize total leave count
        final double[] totalLeave = {0.0};

        // Fetch attendance data for each day in December
        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInMonth; day++) {
                    // Skip if the date is in the future
                    if (calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) && day > currentDayOfMonth) {
                        continue; // Skip future dates
                    }

                    // Format the date key
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, decemberMonth, currentYear);
                    double leaveForDay = 0.0;

                    // Exclude Sundays
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, decemberMonth - 1, day); // Set the date for comparison
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek == Calendar.SUNDAY) {
                        continue; // Skip Sundays
                    }

                    // Check if data for the date exists in Firebase
                    if (!dataSnapshot.hasChild(dateKey)) {
                        leaveForDay = 1.0; // Count as 0.5 leave if date is missing
                    } else {
                        // Get ClockInTime and ClockOutTime
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            Date cutoffClockIn = sdf.parse("10:30");
                            Date cutoffClockOut = sdf.parse("17:46");

                            // Check clock-in time
                            if (clockInTime == null || sdf.parse(clockInTime).after(cutoffClockIn)) {
                                leaveForDay += 0.5;
                            }

                            // Check clock-out time
                            if (clockOutTime == null || sdf.parse(clockOutTime).before(cutoffClockOut)) {
                                leaveForDay += 0.5;
                            }
                        } catch (ParseException e) {
                            Log.e("LeaveCount", "Error parsing clock-in or clock-out time", e);
                        }
                    }


                    // Add leave count for the day to the total
                    totalLeave[0] += leaveForDay;
                }

                // Update TextView with total leave count for December
                TextView leaveTextView = findViewById(R.id.lv12); // Changed to lv10
                leaveTextView.setText(String.format(Locale.getDefault(), "%.1f", totalLeave[0]));

                // Log the result for debugging
                Log.d("LeaveCount", "Leave count for December (up to current day): " + totalLeave[0]);

                String monthYearKey = "December-" + currentYear; // Example: "January-2024"
                DatabaseReference monthLeaveRecordsRef = mDatabase.child("Users").child(userId).child("monthLeaveRecords").child(monthYearKey);

                // Update the value in Firebase
                monthLeaveRecordsRef.setValue(totalLeave[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Absent calculation //


    // Holiday calculation //

    private void calculateHolidayCountForJanuary() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set the target month to January
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int januaryMonth = 1; // January is the 1st month (1-based index)
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Current day of the month

        // Get the number of days in January for the current year
        calendar.set(currentYear, januaryMonth - 1, 1); // Set to January 1st
        int totalDaysInJanuary = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        final double[] totalHolidays = {0.0}; // Effectively final array to store total holiday count

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInJanuary; day++) {
                    // Use the simple integer format for the date
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, januaryMonth, currentYear);

                    // Get the day of the week
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, januaryMonth - 1, day);
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                    // If it's Sunday, add 1 to the holiday count
                    if (dayOfWeek == Calendar.SUNDAY) {
                        totalHolidays[0] += 1.0;
                        continue; // Skip further checks for Sundays
                    }

                    // Check if attendance data exists for the day in Firebase
                    if (dataSnapshot.hasChild(dateKey)) {
                        // Get ClockInTime and ClockOutTime from Firebase
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        // Check if ClockInTime or ClockOutTime is marked as "holiday"
                        if (clockInTime != null && clockInTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockInTime if it's a holiday
                        }
                        if (clockOutTime != null && clockOutTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockOutTime if it's a holiday
                        }
                    }
                }

                // Update the UI with the total holiday count
                TextView holidayCountTextView = findViewById(R.id.hd1); // Assuming this is the TextView ID
                holidayCountTextView.setText(String.format(Locale.getDefault(), "%.1f", totalHolidays[0]));

                // Update Firebase with the holiday count for January
                String monthYearKey = "January-" + currentYear; // Example: "January-2024"
                DatabaseReference monthHolidayRecordsRef = mDatabase.child("Users").child(userId).child("monthHolidayRecords").child(monthYearKey);

                monthHolidayRecordsRef.setValue(totalHolidays[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateHolidayCountForFebruary() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set the target month to February
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int februaryMonth = 2; // February is the 2nd month (1-based index)
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Current day of the month

        // Get the number of days in February for the current year
        calendar.set(currentYear, februaryMonth - 1, 1); // Set to February 1st
        int totalDaysInFebruary = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        final double[] totalHolidays = {0.0}; // Effectively final array to store total holiday count

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInFebruary; day++) {
                    // Use the simple integer format for the date
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, februaryMonth, currentYear);

                    // Get the day of the week
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, februaryMonth - 1, day);
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                    // If it's Sunday, add 1 to the holiday count
                    if (dayOfWeek == Calendar.SUNDAY) {
                        totalHolidays[0] += 1.0;
                        continue; // Skip further checks for Sundays
                    }

                    // Check if attendance data exists for the day in Firebase
                    if (dataSnapshot.hasChild(dateKey)) {
                        // Get ClockInTime and ClockOutTime from Firebase
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        // Check if ClockInTime or ClockOutTime is marked as "holiday"
                        if (clockInTime != null && clockInTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockInTime if it's a holiday
                        }
                        if (clockOutTime != null && clockOutTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockOutTime if it's a holiday
                        }
                    }
                }

                // Update the UI with the total holiday count
                TextView holidayCountTextView = findViewById(R.id.hd2); // Assuming this is the TextView ID for February
                holidayCountTextView.setText(String.format(Locale.getDefault(), "%.1f", totalHolidays[0]));

                // Update Firebase with the holiday count for February
                String monthYearKey = "February-" + currentYear; // Example: "February-2024"
                DatabaseReference monthHolidayRecordsRef = mDatabase.child("Users").child(userId).child("monthHolidayRecords").child(monthYearKey);

                monthHolidayRecordsRef.setValue(totalHolidays[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateHolidayCountForMarch() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set the target month to March
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Use the current year
        int marchMonth = 3; // March is the 3rd month (1-based index)

        // Get the number of days in March for the current year
        calendar.set(currentYear, marchMonth - 1, 1); // Set to March 1st
        int totalDaysInMarch = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        final double[] totalHolidays = {0.0}; // Use an array to make totalHolidays effectively final

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInMarch; day++) {
                    // Format the date key as "dd-MM-yyyy"
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, marchMonth, currentYear);

                    // Get the day of the week
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, marchMonth - 1, day);
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                    // If it's Sunday, count as a holiday
                    if (dayOfWeek == Calendar.SUNDAY) {
                        totalHolidays[0] += 1.0;
                        continue;
                    }

                    // Check if attendance data exists for the day in Firebase
                    if (dataSnapshot.hasChild(dateKey)) {
                        // Get ClockInTime and ClockOutTime from Firebase
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        // Check if ClockInTime or ClockOutTime is marked as "holiday"
                        if (clockInTime != null && clockInTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockInTime if it's a holiday
                        }
                        if (clockOutTime != null && clockOutTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockOutTime if it's a holiday
                        }
                    }
                }

                // Update the UI with the total holiday count
                TextView holidayCountTextView = findViewById(R.id.hd3); // Assuming this is the TextView ID for March
                holidayCountTextView.setText(String.format(Locale.getDefault(), "%.1f", totalHolidays[0]));

                // Update Firebase with the holiday count for March
                String monthYearKey = "March-" + currentYear; // Example: "March-2024"
                DatabaseReference monthHolidayRecordsRef = mDatabase.child("Users").child(userId).child("monthHolidayRecords").child(monthYearKey);

                monthHolidayRecordsRef.setValue(totalHolidays[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateHolidayCountForApril() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set the target month to April
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int aprilMonth = 4; // April is the 4th month (1-based index)
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Current day of the month

        // Get the number of days in April for the current year
        calendar.set(currentYear, aprilMonth - 1, 1); // Set to April 1st
        int totalDaysInApril = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        final double[] totalHolidays = {0.0}; // Effectively final array to store total holiday count

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInApril; day++) {
                    // Use the simple integer format for the date
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, aprilMonth, currentYear);

                    // Get the day of the week
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, aprilMonth - 1, day);
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                    // If it's Sunday, add 1 to the holiday count
                    if (dayOfWeek == Calendar.SUNDAY) {
                        totalHolidays[0] += 1.0;
                        continue; // Skip further checks for Sundays
                    }

                    // Check if attendance data exists for the day in Firebase
                    if (dataSnapshot.hasChild(dateKey)) {
                        // Get ClockInTime and ClockOutTime from Firebase
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        // Check if ClockInTime or ClockOutTime is marked as "holiday"
                        if (clockInTime != null && clockInTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockInTime if it's a holiday
                        }
                        if (clockOutTime != null && clockOutTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockOutTime if it's a holiday
                        }
                    }
                }

                // Update the UI with the total holiday count
                TextView holidayCountTextView = findViewById(R.id.hd4); // Assuming this is the TextView ID for April
                holidayCountTextView.setText(String.format(Locale.getDefault(), "%.1f", totalHolidays[0]));

                // Update Firebase with the holiday count for April
                String monthYearKey = "April-" + currentYear; // Example: "April-2024"
                DatabaseReference monthHolidayRecordsRef = mDatabase.child("Users").child(userId).child("monthHolidayRecords").child(monthYearKey);

                monthHolidayRecordsRef.setValue(totalHolidays[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateHolidayCountForMay() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set the target month to May
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int mayMonth = 5; // May is the 5th month (1-based index)
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Current day of the month

        // Get the number of days in May for the current year
        calendar.set(currentYear, mayMonth - 1, 1); // Set to May 1st
        int totalDaysInMay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        final double[] totalHolidays = {0.0}; // Effectively final array to store total holiday count

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInMay; day++) {
                    // Use the simple integer format for the date
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, mayMonth, currentYear);

                    // Get the day of the week
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, mayMonth - 1, day);
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                    // If it's Sunday, add 1 to the holiday count
                    if (dayOfWeek == Calendar.SUNDAY) {
                        totalHolidays[0] += 1.0;
                        continue; // Skip further checks for Sundays
                    }

                    // Check if attendance data exists for the day in Firebase
                    if (dataSnapshot.hasChild(dateKey)) {
                        // Get ClockInTime and ClockOutTime from Firebase
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        // Check if ClockInTime or ClockOutTime is marked as "holiday"
                        if (clockInTime != null && clockInTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockInTime if it's a holiday
                        }
                        if (clockOutTime != null && clockOutTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockOutTime if it's a holiday
                        }
                    }
                }

                // Update the UI with the total holiday count
                TextView holidayCountTextView = findViewById(R.id.hd5); // Assuming this is the TextView ID for May
                holidayCountTextView.setText(String.format(Locale.getDefault(), "%.1f", totalHolidays[0]));

                // Update Firebase with the holiday count for May
                String monthYearKey = "May-" + currentYear; // Example: "May-2024"
                DatabaseReference monthHolidayRecordsRef = mDatabase.child("Users").child(userId).child("monthHolidayRecords").child(monthYearKey);

                monthHolidayRecordsRef.setValue(totalHolidays[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateHolidayCountForJune() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set the target month to June
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int juneMonth = 6; // June is the 6th month (1-based index)
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Current day of the month

        // Get the number of days in June for the current year
        calendar.set(currentYear, juneMonth - 1, 1); // Set to June 1st
        int totalDaysInJune = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        final double[] totalHolidays = {0.0}; // Effectively final array to store total holiday count

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInJune; day++) {
                    // Use the simple integer format for the date
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, juneMonth, currentYear);

                    // Get the day of the week
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, juneMonth - 1, day);
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                    // If it's Sunday, add 1 to the holiday count
                    if (dayOfWeek == Calendar.SUNDAY) {
                        totalHolidays[0] += 1.0;
                        continue; // Skip further checks for Sundays
                    }

                    // Check if attendance data exists for the day in Firebase
                    if (dataSnapshot.hasChild(dateKey)) {
                        // Get ClockInTime and ClockOutTime from Firebase
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        // Check if ClockInTime or ClockOutTime is marked as "holiday"
                        if (clockInTime != null && clockInTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockInTime if it's a holiday
                        }
                        if (clockOutTime != null && clockOutTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockOutTime if it's a holiday
                        }
                    }
                }

                // Update the UI with the total holiday count
                TextView holidayCountTextView = findViewById(R.id.hd6); // Assuming this is the TextView ID for June
                holidayCountTextView.setText(String.format(Locale.getDefault(), "%.1f", totalHolidays[0]));

                // Update Firebase with the holiday count for June
                String monthYearKey = "June-" + currentYear; // Example: "June-2024"
                DatabaseReference monthHolidayRecordsRef = mDatabase.child("Users").child(userId).child("monthHolidayRecords").child(monthYearKey);

                monthHolidayRecordsRef.setValue(totalHolidays[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateHolidayCountForJuly() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set the target month to July
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int julyMonth = 7; // July is the 7th month (1-based index)

        // Get the number of days in July for the current year
        calendar.set(currentYear, julyMonth - 1, 1); // Set to July 1st
        int totalDaysInJuly = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        final double[] totalHolidays = {0.0}; // Effectively final array to store total holiday count

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInJuly; day++) {
                    // Use the simple integer format for the date
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, julyMonth, currentYear);

                    // Get the day of the week
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, julyMonth - 1, day);
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                    // If it's Sunday, add 1 to the holiday count
                    if (dayOfWeek == Calendar.SUNDAY) {
                        totalHolidays[0] += 1.0;
                        continue; // Skip further checks for Sundays
                    }

                    // Check if attendance data exists for the day in Firebase
                    if (dataSnapshot.hasChild(dateKey)) {
                        // Get ClockInTime and ClockOutTime from Firebase
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        // Check if ClockInTime or ClockOutTime is marked as "holiday"
                        if (clockInTime != null && clockInTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockInTime if it's a holiday
                        }
                        if (clockOutTime != null && clockOutTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockOutTime if it's a holiday
                        }
                    }
                }

                // Update the UI with the total holiday count
                TextView holidayCountTextView = findViewById(R.id.hd7); // Assuming this is the TextView ID for July
                holidayCountTextView.setText(String.format(Locale.getDefault(), "%.1f", totalHolidays[0]));

                // Update Firebase with the holiday count for July
                String monthYearKey = "July-" + currentYear; // Example: "July-2024"
                DatabaseReference monthHolidayRecordsRef = mDatabase.child("Users").child(userId).child("monthHolidayRecords").child(monthYearKey);

                monthHolidayRecordsRef.setValue(totalHolidays[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateHolidayCountForAugust() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set the target month to August
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int augustMonth = 8; // August is the 8th month (1-based index)

        // Get the number of days in August for the current year
        calendar.set(currentYear, augustMonth - 1, 1); // Set to August 1st
        int totalDaysInAugust = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        final double[] totalHolidays = {0.0}; // Effectively final array to store total holiday count

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInAugust; day++) {
                    // Use the simple integer format for the date
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, augustMonth, currentYear);

                    // Get the day of the week
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, augustMonth - 1, day);
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                    // If it's Sunday, add 1 to the holiday count
                    if (dayOfWeek == Calendar.SUNDAY) {
                        totalHolidays[0] += 1.0;
                        continue; // Skip further checks for Sundays
                    }

                    // Check if attendance data exists for the day in Firebase
                    if (dataSnapshot.hasChild(dateKey)) {
                        // Get ClockInTime and ClockOutTime from Firebase
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        // Check if ClockInTime or ClockOutTime is marked as "holiday"
                        if (clockInTime != null && clockInTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockInTime if it's a holiday
                        }
                        if (clockOutTime != null && clockOutTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockOutTime if it's a holiday
                        }
                    }
                }

                // Update the UI with the total holiday count
                TextView holidayCountTextView = findViewById(R.id.hd8); // Assuming this is the TextView ID for August
                holidayCountTextView.setText(String.format(Locale.getDefault(), "%.1f", totalHolidays[0]));

                // Update Firebase with the holiday count for August
                String monthYearKey = "August-" + currentYear; // Example: "August-2024"
                DatabaseReference monthHolidayRecordsRef = mDatabase.child("Users").child(userId).child("monthHolidayRecords").child(monthYearKey);

                monthHolidayRecordsRef.setValue(totalHolidays[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateHolidayCountForSeptember() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set the target month to September
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int septemberMonth = 9; // September is the 9th month (1-based index)
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Current day of the month

        // Get the number of days in September for the current year
        calendar.set(currentYear, septemberMonth - 1, 1); // Set to September 1st
        int totalDaysInSeptember = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        final double[] totalHolidays = {0.0}; // Effectively final array to store total holiday count

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInSeptember; day++) {
                    // Use the simple integer format for the date
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, septemberMonth, currentYear);

                    // Get the day of the week
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, septemberMonth - 1, day);
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                    // If it's Sunday, add 1 to the holiday count
                    if (dayOfWeek == Calendar.SUNDAY) {
                        totalHolidays[0] += 1.0;
                        continue; // Skip further checks for Sundays
                    }

                    // Check if attendance data exists for the day in Firebase
                    if (dataSnapshot.hasChild(dateKey)) {
                        // Get ClockInTime and ClockOutTime from Firebase
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        // Check if ClockInTime or ClockOutTime is marked as "holiday"
                        if (clockInTime != null && clockInTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockInTime if it's a holiday
                        }
                        if (clockOutTime != null && clockOutTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockOutTime if it's a holiday
                        }
                    }
                }

                // Update the UI with the total holiday count
                TextView holidayCountTextView = findViewById(R.id.hd9); // Assuming this is the TextView ID for September
                holidayCountTextView.setText(String.format(Locale.getDefault(), "%.1f", totalHolidays[0]));

                // Update Firebase with the holiday count for September
                String monthYearKey = "September-" + currentYear; // Example: "September-2024"
                DatabaseReference monthHolidayRecordsRef = mDatabase.child("Users").child(userId).child("monthHolidayRecords").child(monthYearKey);

                monthHolidayRecordsRef.setValue(totalHolidays[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateHolidayCountForOctober() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set the target month to October
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int octoberMonth = 10; // October is the 10th month (1-based index)
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Current day of the month

        // Get the number of days in October for the current year
        calendar.set(currentYear, octoberMonth - 1, 1); // Set to October 1st
        int totalDaysInOctober = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        final double[] totalHolidays = {0.0}; // Effectively final array to store total holiday count

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInOctober; day++) {
                    // Use the simple integer format for the date
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, octoberMonth, currentYear);

                    // Get the day of the week
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, octoberMonth - 1, day);
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                    // If it's Sunday, add 1 to the holiday count
                    if (dayOfWeek == Calendar.SUNDAY) {
                        totalHolidays[0] += 1.0;
                        continue; // Skip further checks for Sundays
                    }

                    // Check if attendance data exists for the day in Firebase
                    if (dataSnapshot.hasChild(dateKey)) {
                        // Get ClockInTime and ClockOutTime from Firebase
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        // Check if ClockInTime or ClockOutTime is marked as "holiday"
                        if (clockInTime != null && clockInTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockInTime if it's a holiday
                        }
                        if (clockOutTime != null && clockOutTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockOutTime if it's a holiday
                        }
                    }
                }

                // Update the UI with the total holiday count
                TextView holidayCountTextView = findViewById(R.id.hd10); // Assuming this is the TextView ID for October
                holidayCountTextView.setText(String.format(Locale.getDefault(), "%.1f", totalHolidays[0]));

                // Update Firebase with the holiday count for October
                String monthYearKey = "October-" + currentYear; // Example: "October-2024"
                DatabaseReference monthHolidayRecordsRef = mDatabase.child("Users").child(userId).child("monthHolidayRecords").child(monthYearKey);

                monthHolidayRecordsRef.setValue(totalHolidays[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateHolidayCountForNovember() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set the target month to November
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int novemberMonth = 11; // November is the 11th month (1-based index)
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Current day of the month

        // Get the number of days in November for the current year
        calendar.set(currentYear, novemberMonth - 1, 1); // Set to November 1st
        int totalDaysInNovember = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        final double[] totalHolidays = {0.0}; // Effectively final array to store total holiday count

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInNovember; day++) {
                    // Use the simple integer format for the date
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, novemberMonth, currentYear);

                    // Get the day of the week
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, novemberMonth - 1, day);
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                    // If it's Sunday, add 1 to the holiday count
                    if (dayOfWeek == Calendar.SUNDAY) {
                        totalHolidays[0] += 1.0;
                        continue; // Skip further checks for Sundays
                    }

                    // Check if attendance data exists for the day in Firebase
                    if (dataSnapshot.hasChild(dateKey)) {
                        // Get ClockInTime and ClockOutTime from Firebase
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        // Check if ClockInTime or ClockOutTime is marked as "holiday"
                        if (clockInTime != null && clockInTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockInTime if it's a holiday
                        }
                        if (clockOutTime != null && clockOutTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockOutTime if it's a holiday
                        }
                    }
                }

                // Update the UI with the total holiday count
                TextView holidayCountTextView = findViewById(R.id.hd11); // Assuming this is the TextView ID for November
                holidayCountTextView.setText(String.format(Locale.getDefault(), "%.1f", totalHolidays[0]));

                // Update Firebase with the holiday count for November
                String monthYearKey = "November-" + currentYear; // Example: "November-2024"
                DatabaseReference monthHolidayRecordsRef = mDatabase.child("Users").child(userId).child("monthHolidayRecords").child(monthYearKey);

                monthHolidayRecordsRef.setValue(totalHolidays[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateHolidayCountForDecember() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set the target month to December
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int decemberMonth = 12; // December is the 12th month (1-based index)
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH); // Current day of the month

        // Get the number of days in December for the current year
        calendar.set(currentYear, decemberMonth - 1, 1); // Set to December 1st
        int totalDaysInDecember = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        final double[] totalHolidays = {0.0}; // Effectively final array to store total holiday count

        timeRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int day = 1; day <= totalDaysInDecember; day++) {
                    // Use the simple integer format for the date
                    String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, decemberMonth, currentYear);

                    // Get the day of the week
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.set(currentYear, decemberMonth - 1, day);
                    int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);

                    // If it's Sunday, add 1 to the holiday count
                    if (dayOfWeek == Calendar.SUNDAY) {
                        totalHolidays[0] += 1.0;
                        continue; // Skip further checks for Sundays
                    }

                    // Check if attendance data exists for the day in Firebase
                    if (dataSnapshot.hasChild(dateKey)) {
                        // Get ClockInTime and ClockOutTime from Firebase
                        String clockInTime = dataSnapshot.child(dateKey).child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child(dateKey).child("ClockOutTime").getValue(String.class);

                        // Check if ClockInTime or ClockOutTime is marked as "holiday"
                        if (clockInTime != null && clockInTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockInTime if it's a holiday
                        }
                        if (clockOutTime != null && clockOutTime.contains("holiday")) {
                            totalHolidays[0] += 0.5; // Add 0.5 for ClockOutTime if it's a holiday
                        }
                    }
                }

                // Update the UI with the total holiday count
                TextView holidayCountTextView = findViewById(R.id.hd12); // Assuming this is the TextView ID for December
                holidayCountTextView.setText(String.format(Locale.getDefault(), "%.1f", totalHolidays[0]));

                // Update Firebase with the holiday count for December
                String monthYearKey = "December-" + currentYear; // Example: "December-2024"
                DatabaseReference monthHolidayRecordsRef = mDatabase.child("Users").child(userId).child("monthHolidayRecords").child(monthYearKey);

                monthHolidayRecordsRef.setValue(totalHolidays[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Holiday calculation //


    // Laps calculation //

    private void calculateLateCountForJanuary() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set the target month to January (January is month 1 in Calendar)
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Use the current year
        String monthKey = "January-" + currentYear; // Key for January month

        // Initialize the total late count
        final double[] totalLates = {0.0}; // Use an array to keep it final for use in nested methods

        // Fetch the late count from monthPermissionRecords
        DatabaseReference permissionRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthKey);
        permissionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double[] permissionLates = {0.0}; // Use an array to make it effectively final
                if (dataSnapshot.exists()) {
                    permissionLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                }

                // Now fetch the late count from monthSMRecords
                DatabaseReference smRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthKey);
                smRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        double[] smLates = {0.0}; // Use an array to make it effectively final
                        if (dataSnapshot.exists()) {
                            smLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                        }

                        // Combine the counts from both monthPermissionRecords and monthSMRecords
                        totalLates[0] = permissionLates[0] + smLates[0];

                        // Update the UI with the total late count
                        TextView lateCountTextView = findViewById(R.id.laps1); // Update TextView with ID laps1
                        lateCountTextView.setText(String.format(Locale.getDefault(), "%.1f", totalLates[0]));

                        // Now store the total late count in Firebase for this month
                        DatabaseReference monthHolidayRecordsRef = mDatabase.child("Users").child(userId).child("monthLapsRecords").child(monthKey);
                        monthHolidayRecordsRef.setValue(totalLates[0]).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                            } else {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch SM records", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLateCountForFebruary() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set the target month to February (February is month 2 in Calendar)
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Use the current year
        String monthKey = "February-" + currentYear;

        // Initialize the total late count
        final double[] totalLates = {0.0}; // Use an array to keep it final for use in nested methods

        // Fetch the late count from monthPermissionRecords
        DatabaseReference permissionRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthKey);
        permissionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double[] permissionLates = {0.0}; // Store as double to handle fractional values
                if (dataSnapshot.exists()) {
                    permissionLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                }

                // Now fetch the late count from monthSMRecords
                DatabaseReference smRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthKey);
                smRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        double[] smLates = {0.0}; // Store as double to handle fractional values
                        if (dataSnapshot.exists()) {
                            smLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                        }

                        // Combine the counts from both monthPermissionRecords and monthSMRecords
                        totalLates[0] = permissionLates[0] + smLates[0];

                        // Update the UI with the total late count
                        TextView lateCountTextView = findViewById(R.id.laps2); // Assuming the February TextView is laps2
                        lateCountTextView.setText(String.format(Locale.getDefault(), "%.1f", totalLates[0]));

                        // Now store the total late count in Firebase for this month
                        DatabaseReference monthLapsRecordsRef = mDatabase.child("Users").child(userId).child("monthLapsRecords").child(monthKey);
                        monthLapsRecordsRef.setValue(totalLates[0]).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                            } else {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch SM records", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLateCountForMarch() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set the target month to March (March is month 3 in Calendar)
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Use the current year
        String monthKey = "March-" + currentYear;  // Key for March data

        // Initialize the total late count
        final int[] totalLates = {0}; // Use an array to keep it final for use in nested methods

        // Fetch the late count from monthPermissionRecords
        DatabaseReference permissionRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthKey);
        permissionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double[] permissionLates = {0.0}; // Use an array to make it effectively final
                if (dataSnapshot.exists()) {
                    permissionLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                }

                // Now fetch the late count from monthSMRecords
                DatabaseReference smRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthKey);
                smRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        double[] smLates = {0.0}; // Use an array to make it effectively final
                        if (dataSnapshot.exists()) {
                            smLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                        }

                        // Combine the counts from both monthPermissionRecords and monthSMRecords
                        totalLates[0] = (int) Math.ceil(permissionLates[0] + smLates[0]);

                        // Update the UI with the total late count
                        TextView lateCountTextView = findViewById(R.id.laps3);  // Assuming this is the correct TextView ID
                        lateCountTextView.setText(String.format(Locale.getDefault(), "%d", totalLates[0]));

                        // Now store the total late count in Firebase for this month
                        DatabaseReference monthHolidayRecordsRef = mDatabase.child("Users").child(userId).child("monthLapsRecords").child(monthKey);
                        monthHolidayRecordsRef.setValue(totalLates[0]).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                            } else {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch SM records", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLateCountForApril() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // Set the target month to April (April is month 4 in Calendar)
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Use the current year
        String monthKey = "April-" + currentYear;

        // Initialize the total late count
        final int[] totalLates = {0}; // Use an array to make it effectively final

        // Fetch the late count from monthPermissionRecords
        DatabaseReference permissionRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthKey);
        permissionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final double[] permissionLates = {0.0}; // Store as double to handle fractional values
                if (dataSnapshot.exists()) {
                    permissionLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                }

                // Round up and convert to integer count
                int roundedPermissionLates = (int) Math.ceil(permissionLates[0]);

                // Now fetch the late count from monthSMRecords
                DatabaseReference smRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthKey);
                smRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final double[] smLates = {0.0}; // Store as double to handle fractional values
                        if (dataSnapshot.exists()) {
                            smLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                        }

                        // Round up and convert to integer count
                        int roundedSmLates = (int) Math.ceil(smLates[0]);

                        // Combine the counts from both monthPermissionRecords and monthSMRecords
                        totalLates[0] = roundedPermissionLates + roundedSmLates;

                        // Update the UI with the total late count
                        TextView lateCountTextView = findViewById(R.id.laps4); // Adjust the TextView ID as needed
                        lateCountTextView.setText(String.format(Locale.getDefault(), "%d", totalLates[0]));

                        // Now store the total late count in Firebase for this month
                        DatabaseReference monthLapsRecordsRef = mDatabase.child("Users").child(userId).child("monthLapsRecords").child(monthKey);
                        monthLapsRecordsRef.setValue(totalLates[0]).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                            } else {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch SM records", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLateCountForMay() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        // Set the target month to May
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Use the current year
        String monthKey = "May-" + currentYear;

        // Initialize the total late count
        final int[] totalLates = {0}; // Use an array to allow modification in nested methods

        // Fetch the late count from monthPermissionRecords
        DatabaseReference permissionRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthKey);
        permissionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final double[] permissionLates = {0.0}; // Store as double to handle fractional values
                if (dataSnapshot.exists()) {
                    permissionLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                }

                int roundedPermissionLates = (int) Math.ceil(permissionLates[0]);

                // Fetch the late count from monthSMRecords
                DatabaseReference smRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthKey);
                smRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final double[] smLates = {0.0}; // Store as double to handle fractional values
                        if (dataSnapshot.exists()) {
                            smLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                        }

                        int roundedSmLates = (int) Math.ceil(smLates[0]);

                        // Combine the counts from both monthPermissionRecords and monthSMRecords
                        totalLates[0] = roundedPermissionLates + roundedSmLates;

                        // Update the UI with the total late count
                        TextView lateCountTextView = findViewById(R.id.laps5); // Replace with your TextView ID for May
                        lateCountTextView.setText(String.format(Locale.getDefault(), "%d", totalLates[0]));

                        // Store the total late count in Firebase for this month
                        DatabaseReference monthLapsRecordsRef = mDatabase.child("Users").child(userId).child("monthLapsRecords").child(monthKey);
                        monthLapsRecordsRef.setValue(totalLates[0]).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                            } else {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch SM records", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLateCountForJune() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set the target month to June (June is month 6 in Calendar)
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Use the current year
        String monthKey = "June-" + currentYear; // Format key as "June-2024"

        // Initialize the total late count
        final int[] totalLates = {0}; // Use an array to make it effectively final

        // Fetch the late count from monthPermissionRecords
        DatabaseReference permissionRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthKey);
        permissionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final double[] permissionLates = {0.0}; // Store as double to handle fractional values
                if (dataSnapshot.exists()) {
                    permissionLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                }

                // Round up and convert to integer count
                int roundedPermissionLates = (int) Math.ceil(permissionLates[0]);

                // Now fetch the late count from monthSMRecords
                DatabaseReference smRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthKey);
                smRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final double[] smLates = {0.0}; // Store as double to handle fractional values
                        if (dataSnapshot.exists()) {
                            smLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                        }

                        // Round up and convert to integer count
                        int roundedSmLates = (int) Math.ceil(smLates[0]);

                        // Combine the counts from both monthPermissionRecords and monthSMRecords
                        totalLates[0] = roundedPermissionLates + roundedSmLates;

                        // Update the UI with the total late count
                        TextView lateCountTextView = findViewById(R.id.laps6); // Adjust the TextView ID as needed
                        lateCountTextView.setText(String.format(Locale.getDefault(), "%d", totalLates[0]));

                        // Now store the total late count in Firebase for this month
                        DatabaseReference monthLapsRecordsRef = mDatabase.child("Users").child(userId).child("monthLapsRecords").child(monthKey);
                        monthLapsRecordsRef.setValue(totalLates[0]).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                            } else {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch SM records for June", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission records for June", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLateCountForJuly() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        // Set the target month to July (July is month 7 in Calendar)
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Use the current year
        String monthKey = "July-" + currentYear; // Use the format "July-YYYY"

        // Initialize the total late count
        final int[] totalLates = {0}; // Use an array to make it effectively final

        // Fetch the late count from monthPermissionRecords
        DatabaseReference permissionRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthKey);
        permissionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final double[] permissionLates = {0.0}; // Store as double to handle fractional values
                if (dataSnapshot.exists()) {
                    permissionLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                }

                // Round up and convert to integer count
                int roundedPermissionLates = (int) Math.ceil(permissionLates[0]);

                // Now fetch the late count from monthSMRecords
                DatabaseReference smRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthKey);
                smRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final double[] smLates = {0.0}; // Store as double to handle fractional values
                        if (dataSnapshot.exists()) {
                            smLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                        }

                        // Round up and convert to integer count
                        int roundedSmLates = (int) Math.ceil(smLates[0]);

                        // Combine the counts from both monthPermissionRecords and monthSMRecords
                        totalLates[0] = roundedPermissionLates + roundedSmLates;

                        // Update the UI with the total late count
                        TextView lateCountTextView = findViewById(R.id.laps7); // Replace with the correct ID for the late count
                        lateCountTextView.setText(String.format(Locale.getDefault(), "%d", totalLates[0]));

                        // Now store the total late count in Firebase for this month
                        DatabaseReference monthLapsRecordsRef = mDatabase.child("Users").child(userId).child("monthLapsRecords").child(monthKey);
                        monthLapsRecordsRef.setValue(totalLates[0]).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                            } else {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch SM records", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLateCountForAugust() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set the target month to August (August is month 8 in Calendar)
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Use the current year
        String monthKey = "August-" + currentYear;

        // Initialize the total late count
        final int[] totalLates = {0}; // Use an array to keep it final for use in nested methods

        // Fetch the late count from monthPermissionRecords
        DatabaseReference permissionRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthKey);
        permissionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final double[] permissionLates = {0.0}; // Store as double to handle fractional values
                if (dataSnapshot.exists()) {
                    permissionLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                }

                // Round up and convert to integer count
                int roundedPermissionLates = (int) Math.ceil(permissionLates[0]);

                // Now fetch the late count from monthSMRecords
                DatabaseReference smRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthKey);
                smRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final double[] smLates = {0.0}; // Store as double to handle fractional values
                        if (dataSnapshot.exists()) {
                            smLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                        }

                        // Round up and convert to integer count
                        int roundedSmLates = (int) Math.ceil(smLates[0]);

                        // Combine the counts from both monthPermissionRecords and monthSMRecords
                        totalLates[0] = roundedPermissionLates + roundedSmLates;

                        // Update the UI with the total late count
                        TextView lateCountTextView = findViewById(R.id.laps8); // Adjust the TextView ID as needed
                        lateCountTextView.setText(String.format(Locale.getDefault(), "%d", totalLates[0]));

                        // Now store the total late count in Firebase for this month
                        DatabaseReference monthLapsRecordsRef = mDatabase.child("Users").child(userId).child("monthLapsRecords").child(monthKey);
                        monthLapsRecordsRef.setValue(totalLates[0]).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                            } else {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch SM records", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLateCountForSeptember() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set the target month to September (September is month 9 in Calendar)
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Use the current year
        String monthKey = "September-" + currentYear;

        // Initialize the total late count
        final int[] totalLates = {0}; // Use an array to keep it effectively final

        // Fetch the late count from monthPermissionRecords
        DatabaseReference permissionRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthKey);
        permissionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final double[] permissionLates = {0.0}; // Use double to handle fractional values
                if (dataSnapshot.exists()) {
                    permissionLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                }

                // Round up and convert to integer count
                int roundedPermissionLates = (int) Math.ceil(permissionLates[0]);

                // Fetch the late count from monthSMRecords
                DatabaseReference smRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthKey);
                smRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final double[] smLates = {0.0}; // Use double to handle fractional values
                        if (dataSnapshot.exists()) {
                            smLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                        }

                        // Round up and convert to integer count
                        int roundedSmLates = (int) Math.ceil(smLates[0]);

                        // Combine the counts from both monthPermissionRecords and monthSMRecords
                        totalLates[0] = roundedPermissionLates + roundedSmLates;

                        // Update the UI with the total late count
                        TextView lateCountTextView = findViewById(R.id.laps9); // Make sure to use the correct TextView ID for September
                        lateCountTextView.setText(String.format(Locale.getDefault(), "%d", totalLates[0]));

                        // Store the total late count in Firebase for this month
                        DatabaseReference monthLapsRecordsRef = mDatabase.child("Users").child(userId).child("monthLapsRecords").child(monthKey);
                        monthLapsRecordsRef.setValue(totalLates[0]).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                            } else {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch SM records", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLateCountForOctober() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set the target month to October (October is month 10 in Calendar)
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Use the current year
        String monthKey = "October-" + currentYear;

        // Initialize the total late count
        final int[] totalLates = {0}; // Use an array to keep it final for use in nested methods

        // Fetch the late count from monthPermissionRecords
        DatabaseReference permissionRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthKey);
        permissionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final double[] permissionLates = {0.0}; // Store as double to handle fractional values
                if (dataSnapshot.exists()) {
                    permissionLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                }

                // Round up and convert to integer count
                int roundedPermissionLates = (int) Math.ceil(permissionLates[0]);

                // Now fetch the late count from monthSMRecords
                DatabaseReference smRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthKey);
                smRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final double[] smLates = {0.0}; // Store as double to handle fractional values
                        if (dataSnapshot.exists()) {
                            smLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                        }

                        // Round up and convert to integer count
                        int roundedSmLates = (int) Math.ceil(smLates[0]);

                        // Combine the counts from both monthPermissionRecords and monthSMRecords
                        totalLates[0] = roundedPermissionLates + roundedSmLates;

                        // Update the UI with the total late count
                        TextView lateCountTextView = findViewById(R.id.laps10); // Adjust the TextView ID as needed
                        lateCountTextView.setText(String.format(Locale.getDefault(), "%d", totalLates[0]));

                        // Now store the total late count in Firebase for this month
                        DatabaseReference monthLapsRecordsRef = mDatabase.child("Users").child(userId).child("monthLapsRecords").child(monthKey);
                        monthLapsRecordsRef.setValue(totalLates[0]).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                            } else {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch SM records", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLateCountForNovember() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set the target month to November (November is month 11 in Calendar)
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Use the current year
        String monthKey = "November-" + currentYear;

        // Initialize the total late count
        final int[] totalLates = {0}; // Use an array to keep it final for use in nested methods

        // Fetch the late count from monthPermissionRecords
        DatabaseReference permissionRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthKey);
        permissionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final double[] permissionLates = {0.0}; // Store as double to handle fractional values
                if (dataSnapshot.exists()) {
                    permissionLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                }

                // Round up and convert to integer count
                int roundedPermissionLates = (int) Math.ceil(permissionLates[0]);

                // Now fetch the late count from monthSMRecords
                DatabaseReference smRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthKey);
                smRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final double[] smLates = {0.0}; // Store as double to handle fractional values
                        if (dataSnapshot.exists()) {
                            smLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                        }

                        // Round up and convert to integer count
                        int roundedSmLates = (int) Math.ceil(smLates[0]);

                        // Combine the counts from both monthPermissionRecords and monthSMRecords
                        totalLates[0] = roundedPermissionLates + roundedSmLates;

                        // Update the UI with the total late count
                        TextView lateCountTextView = findViewById(R.id.laps11); // Adjust the TextView ID as needed
                        lateCountTextView.setText(String.format(Locale.getDefault(), "%d", totalLates[0]));

                        // Now store the total late count in Firebase for this month
                        DatabaseReference monthLapsRecordsRef = mDatabase.child("Users").child(userId).child("monthLapsRecords").child(monthKey);
                        monthLapsRecordsRef.setValue(totalLates[0]).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                            } else {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch SM records", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateLateCountForDecember() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set the target month to December (December is month 12 in Calendar)
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Use the current year
        String monthKey = "December-" + currentYear;

        // Initialize the total late count
        final int[] totalLates = {0}; // Use an array to keep it final for use in nested methods

        // Fetch the late count from monthPermissionRecords
        DatabaseReference permissionRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthKey);
        permissionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final double[] permissionLates = {0.0}; // Store as double to handle fractional values
                if (dataSnapshot.exists()) {
                    permissionLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                }

                // Round up and convert to integer count
                int roundedPermissionLates = (int) Math.ceil(permissionLates[0]);

                // Now fetch the late count from monthSMRecords
                DatabaseReference smRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthKey);
                smRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final double[] smLates = {0.0}; // Store as double to handle fractional values
                        if (dataSnapshot.exists()) {
                            smLates[0] = dataSnapshot.getValue(Double.class); // Assuming it's stored as a Double
                        }

                        // Round up and convert to integer count
                        int roundedSmLates = (int) Math.ceil(smLates[0]);

                        // Combine the counts from both monthPermissionRecords and monthSMRecords
                        totalLates[0] = roundedPermissionLates + roundedSmLates;

                        // Update the UI with the total late count
                        TextView lateCountTextView = findViewById(R.id.laps12); // Adjust the TextView ID as needed
                        lateCountTextView.setText(String.format(Locale.getDefault(), "%d", totalLates[0]));

                        // Now store the total late count in Firebase for this month
                        DatabaseReference monthLapsRecordsRef = mDatabase.child("Users").child(userId).child("monthLapsRecords").child(monthKey);
                        monthLapsRecordsRef.setValue(totalLates[0]).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                            } else {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch SM records", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Laps calculation //



    // Permission calculation //

    private void countPermissionsForJanuary() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference clockInPermissionsRef = mDatabase.child("ClockInPermission");
        DatabaseReference clockOutPermissionsRef = mDatabase.child("ClockOutPermission");

        // Get current year and January month
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int januaryMonth = 1; // January (1-based index)

        final double[] totalPermissionCount = {0.0}; // Keep track of the total count

        ValueEventListener countPermissionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot recordSnapshot : snapshot.getChildren()) {
                        String requestUID = recordSnapshot.child("uid").getValue(String.class);
                        String date = recordSnapshot.child("date").getValue(String.class); // Get the date string (dd-MM-yyyy)
                        String status = recordSnapshot.child("status").getValue(String.class);

                        // Check if the UID matches and status is approved
                        if (userId.equals(requestUID) && "approved".equalsIgnoreCase(status) && date != null) {
                            try {
                                // Extract the month from the date string (dd-MM-yyyy)
                                String[] dateParts = date.split("-"); // Split "01-01-2024" into {"01", "01", "2024"}
                                int requestMonth = Integer.parseInt(dateParts[1]); // Get the month part (1-based index)

                                // Check if the date corresponds to January of the current year
                                if (requestMonth == januaryMonth) {
                                    totalPermissionCount[0] += 0.5; // Increment by 0.5 for approved status
                                }
                            } catch (Exception e) {
                                e.printStackTrace(); // Handle any potential parsing errors
                            }
                        }
                    }
                }

                // After processing this node, check if the total count is complete
                updateJanPermissionCount(totalPermissionCount[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission data", Toast.LENGTH_SHORT).show();
            }
        };

        // Query clock-in permissions
        clockInPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);

        // Query clock-out permissions
        clockOutPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);
    }

    private void updateJanPermissionCount(double totalCount) {
        // Update the UI with the total count for January
        TextView permissionCountTextView = findViewById(R.id.pm1); // Adjust this ID to your TextView
        permissionCountTextView.setText(String.valueOf(totalCount));

        // Save the total count to Firebase for this month
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        String monthYearKey = "January-" + currentYear; // Example: "January-2024"
        DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthYearKey);
        monthPermissionRecordsRef.setValue(totalCount).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

            } else {

            }
        });
    }


    private void countPermissionsForFebruary() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        // Separate references to permission request nodes
        DatabaseReference permissionRequestsRef = mDatabase.child("permissionRequests");

        // Set February date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int februaryMonth = 2; // February (1-based index)

        // Track the count of approved permissions
        final double[] totalPermissionCount = {0.0}; // Using a double to track the count

        // Function to check permissions from a specific reference
        permissionRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot recordSnapshot : snapshot.getChildren()) {
                        String requestUID = recordSnapshot.child("uid").getValue(String.class);
                        String date = recordSnapshot.child("date").getValue(String.class); // Get the date string (dd-MM-yyyy)
                        String status = recordSnapshot.child("status").getValue(String.class); // Get the status

                        // Check if the UID matches and the status is approved
                        if (userId.equals(requestUID) && "approved".equalsIgnoreCase(status) && date != null) {
                            try {
                                // Extract the month from the date string (dd-MM-yyyy)
                                String[] dateParts = date.split("-"); // Split "28-02-2024" into {"28", "02", "2024"}
                                int requestMonth = Integer.parseInt(dateParts[1]); // Get the month part (1-based index)

                                // Check if the date corresponds to February of the current year
                                if (requestMonth == februaryMonth) {
                                    totalPermissionCount[0] += 0.5; // Increment by 0.5 for each approved status
                                }
                            } catch (Exception e) {
                                e.printStackTrace(); // Handle any potential parsing errors
                            }
                        }
                    }
                }

                // After processing this node, check if the total count is complete
                updateFebruaryPermissionCount(totalPermissionCount[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFebruaryPermissionCount(double totalCount) {
        // Update the UI with the total count for February
        TextView permissionCountTextView = findViewById(R.id.pm2); // Adjust this ID to your TextView for February
        permissionCountTextView.setText(String.format(Locale.getDefault(), "%.1f", totalCount)); // Formatting to 1 decimal place

        // Save the total count to Firebase for February
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        // Create a reference path for the user's monthly permission records
        String monthYearKey = "February-" + currentYear; // Example: "February-2024"
        DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthYearKey);

        // Set the total permission count for February in Firebase
        monthPermissionRecordsRef.setValue(totalCount).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Success: Show a confirmation message to the user

            } else {
                // Failure: Show an error message

            }
        });
    }


    private void countPermissionsForMarch() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        // Separate references to clockInPermissions and clockOutPermissions nodes
        DatabaseReference clockInPermissionsRef = mDatabase.child("ClockInPermission");
        DatabaseReference clockOutPermissionsRef = mDatabase.child("ClockOutPermission");

        // Set March date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int marchMonth = 3; // March (1-based month index)

        // Track the count of approved permissions
        final double[] totalPermissionCount = {0.0}; // Keep track of the total count

        // Function to check permissions from a specific reference (clock-in or clock-out)
        ValueEventListener countPermissionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot recordSnapshot : snapshot.getChildren()) {
                        String requestUID = recordSnapshot.child("uid").getValue(String.class);
                        String date = recordSnapshot.child("date").getValue(String.class); // Get the date string (dd-MM-yyyy)
                        String status = recordSnapshot.child("status").getValue(String.class); // Get the status

                        // Check if the UID matches and the status is approved
                        if (userId.equals(requestUID) && "approved".equalsIgnoreCase(status) && date != null) {
                            try {
                                // Extract the month from the date string (dd-MM-yyyy)
                                String[] dateParts = date.split("-"); // Split "28-03-2024" into {"28", "03", "2024"}
                                int requestMonth = Integer.parseInt(dateParts[1]); // Get the month part (1-based index)

                                // Check if the date corresponds to March of the current year
                                if (requestMonth == marchMonth) {
                                    totalPermissionCount[0] += 0.5; // Increment by 0.5 for approved status
                                }
                            } catch (Exception e) {
                                e.printStackTrace(); // Handle any potential parsing errors
                            }
                        }
                    }
                }

                // After processing this node, check if the total count is complete
                updateMarchPermissionCount(totalPermissionCount[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission data", Toast.LENGTH_SHORT).show();
            }
        };

        // Query clock-in permissions for March
        clockInPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);

        // Query clock-out permissions for March
        clockOutPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);
    }

    private void updateMarchPermissionCount(double totalCount) {
        // Update the UI with the total count for March
        TextView permissionCountTextView = findViewById(R.id.pm3); // Adjust this ID to your TextView for March
        permissionCountTextView.setText(String.format(Locale.getDefault(), "%.1f", totalCount)); // Formatting to 1 decimal place

        // Save the total count to Firebase for March
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        // Create a reference path for the user's monthly permission records
        String monthYearKey = "March-" + currentYear; // Example: "March-2024"
        DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthYearKey);

        // Set the total permission count for March in Firebase
        monthPermissionRecordsRef.setValue(totalCount).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

            } else {

            }
        });
    }


    private void countPermissionsForApril() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference clockInPermissionsRef = mDatabase.child("ClockInPermission");
        DatabaseReference clockOutPermissionsRef = mDatabase.child("ClockOutPermission");

        // Get current year and April month
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Get current year
        int aprilMonth = 4; // April (1-based month index)

        final double[] totalPermissionCount = {0.0}; // Keep track of the total count

        ValueEventListener countPermissionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot recordSnapshot : snapshot.getChildren()) {
                        String requestUID = recordSnapshot.child("uid").getValue(String.class);
                        String date = recordSnapshot.child("date").getValue(String.class); // Get the date string (dd-MM-yyyy)
                        String status = recordSnapshot.child("status").getValue(String.class); // Get the status

                        // Check if the UID matches and the status is approved
                        if (userId.equals(requestUID) && "approved".equalsIgnoreCase(status) && date != null) {
                            try {
                                // Extract the month from the date string (dd-MM-yyyy)
                                String[] dateParts = date.split("-"); // Split "28-04-2024" into {"28", "04", "2024"}
                                int requestMonth = Integer.parseInt(dateParts[1]); // Get the month part (1-based index)

                                // Check if the date corresponds to April of the current year
                                if (requestMonth == aprilMonth) {
                                    totalPermissionCount[0] += 0.5; // Increment by 0.5 for approved status
                                }
                            } catch (Exception e) {
                                e.printStackTrace(); // Handle any potential parsing errors
                            }
                        }
                    }
                }

                // After processing this node, check if the total count is complete
                updateAprilPermissionCount(totalPermissionCount[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission data", Toast.LENGTH_SHORT).show();
            }
        };

        // Query clock-in permissions for April
        clockInPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);

        // Query clock-out permissions for April
        clockOutPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);
    }

    private void updateAprilPermissionCount(double totalCount) {
        // Update the UI with the total count for April
        TextView permissionCountTextView = findViewById(R.id.pm4); // Adjust this ID to your TextView for April
        permissionCountTextView.setText(String.format(Locale.getDefault(), "%.1f", totalCount)); // Formatting to 1 decimal place

        // Save the total count to Firebase for April
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        // Create a reference path for the user's monthly permission records
        String monthYearKey = "April-" + currentYear; // Example: "April-2024"
        DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthYearKey);

        // Set the total permission count for April in Firebase
        monthPermissionRecordsRef.setValue(totalCount).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

            } else {
                // Failure: Show an error message
            }
        });
    }


    private void countPermissionsForMay() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference clockInPermissionsRef = mDatabase.child("ClockInPermission");
        DatabaseReference clockOutPermissionsRef = mDatabase.child("ClockOutPermission");

        // Get current year and May month
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int mayMonth = 5; // May (1-based index)

        final double[] totalPermissionCount = {0.0}; // Keep track of the total count

        ValueEventListener countPermissionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot recordSnapshot : snapshot.getChildren()) {
                        String requestUID = recordSnapshot.child("uid").getValue(String.class);
                        String date = recordSnapshot.child("date").getValue(String.class); // Get the date string (dd-MM-yyyy)
                        String status = recordSnapshot.child("status").getValue(String.class);

                        // Check if the UID matches and status is approved
                        if (userId.equals(requestUID) && "approved".equalsIgnoreCase(status) && date != null) {
                            try {
                                // Extract the month from the date string (dd-MM-yyyy)
                                String[] dateParts = date.split("-"); // Split "28-05-2024" into {"28", "05", "2024"}
                                int requestMonth = Integer.parseInt(dateParts[1]); // Get the month part (1-based index)

                                // Check if the date corresponds to May of the current year
                                if (requestMonth == mayMonth) {
                                    totalPermissionCount[0] += 0.5; // Increment by 0.5 for approved status
                                }
                            } catch (Exception e) {
                                e.printStackTrace(); // Handle any potential parsing errors
                            }
                        }
                    }
                }

                // After processing this node, check if the total count is complete
                updateMayPermissionCount(totalPermissionCount[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission data", Toast.LENGTH_SHORT).show();
            }
        };

        // Query clock-in permissions
        clockInPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);

        // Query clock-out permissions
        clockOutPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);
    }

    private void updateMayPermissionCount(double totalCount) {
        // Update the UI with the total count for May
        TextView permissionCountTextView = findViewById(R.id.pm5); // Adjust this ID to your TextView for May
        permissionCountTextView.setText(String.format(Locale.getDefault(), "%.1f", totalCount)); // Formatting to 1 decimal place

        // Save the total count to Firebase for May
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        // Create a reference path for the user's monthly permission records
        String monthYearKey = "May-" + currentYear; // Example: "May-2024"
        DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthYearKey);

        // Set the total permission count for May in Firebase
        monthPermissionRecordsRef.setValue(totalCount).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Success: Show a confirmation message to the user

            } else {
                // Failure: Show an error message
            }
        });
    }


    private void countPermissionsForJune() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference clockInPermissionsRef = mDatabase.child("ClockInPermission");
        DatabaseReference clockOutPermissionsRef = mDatabase.child("ClockOutPermission");

        // Get current year and June month
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int juneMonth = 6; // June (1-based index)

        final double[] totalPermissionCount = {0.0}; // Keep track of the total count

        ValueEventListener countPermissionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot recordSnapshot : snapshot.getChildren()) {
                        String requestUID = recordSnapshot.child("uid").getValue(String.class);
                        String date = recordSnapshot.child("date").getValue(String.class); // Get the date string (dd-MM-yyyy)
                        String status = recordSnapshot.child("status").getValue(String.class);

                        // Check if the UID matches and status is approved
                        if (userId.equals(requestUID) && "approved".equalsIgnoreCase(status) && date != null) {
                            try {
                                // Extract the month from the date string (dd-MM-yyyy)
                                String[] dateParts = date.split("-"); // Split "28-06-2024" into {"28", "06", "2024"}
                                int requestMonth = Integer.parseInt(dateParts[1]); // Get the month part (1-based index)

                                // Check if the date corresponds to June of the current year
                                if (requestMonth == juneMonth) {
                                    totalPermissionCount[0] += 0.5; // Increment by 0.5 for approved status
                                }
                            } catch (Exception e) {
                                e.printStackTrace(); // Handle any potential parsing errors
                            }
                        }
                    }
                }

                // After processing this node, check if the total count is complete
                updateJunePermissionCount(totalPermissionCount[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission data", Toast.LENGTH_SHORT).show();
            }
        };

        // Query clock-in permissions
        clockInPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);

        // Query clock-out permissions
        clockOutPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);
    }

    private void updateJunePermissionCount(double totalCount) {
        // Update the UI with the total count for June
        TextView permissionCountTextView = findViewById(R.id.pm6); // Adjust this ID to your TextView for June
        permissionCountTextView.setText(String.format(Locale.getDefault(), "%.1f", totalCount)); // Formatting to 1 decimal place

        // Save the total count to Firebase for June
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        // Create a reference path for the user's monthly permission records
        String monthYearKey = "June-" + currentYear; // Example: "June-2024"
        DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthYearKey);

        // Set the total permission count for June in Firebase
        monthPermissionRecordsRef.setValue(totalCount).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Success: Show a confirmation message to the user
            } else {

            }
        });
    }


    private void countPermissionsForJuly() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference clockInPermissionsRef = mDatabase.child("ClockInPermission");
        DatabaseReference clockOutPermissionsRef = mDatabase.child("ClockOutPermission");

        // Set current year and July month
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int julyMonth = 7; // July (1-based index)

        final double[] totalPermissionCount = {0.0}; // Initialize the total count

        ValueEventListener countPermissionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot recordSnapshot : snapshot.getChildren()) {
                        String requestUID = recordSnapshot.child("uid").getValue(String.class);
                        String date = recordSnapshot.child("date").getValue(String.class); // Get the date string (dd-MM-yyyy)
                        String status = recordSnapshot.child("status").getValue(String.class);

                        // Check if the UID matches and status is approved
                        if (userId.equals(requestUID) && "approved".equalsIgnoreCase(status) && date != null) {
                            try {
                                // Parse the date string (dd-MM-yyyy)
                                String[] dateParts = date.split("-"); // Split "15-07-2024" into {"15", "07", "2024"}
                                int requestMonth = Integer.parseInt(dateParts[1]); // Extract month (1-based index)
                                int requestYear = Integer.parseInt(dateParts[2]); // Extract year

                                // Check if the date corresponds to July of the current year
                                if (requestYear == currentYear && requestMonth == julyMonth) {
                                    totalPermissionCount[0] += 0.5; // Increment by 0.5 for approved status
                                }
                            } catch (Exception e) {
                                e.printStackTrace(); // Handle any potential parsing errors
                            }
                        }
                    }
                }

                // Update the UI with the total count after processing
                updatePermissionCountForJuly(totalPermissionCount[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission data", Toast.LENGTH_SHORT).show();
            }
        };

        // Query clock-in permissions
        clockInPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);

        // Query clock-out permissions
        clockOutPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);
    }

    private void updatePermissionCountForJuly(double totalCount) {
        // Update the UI with the total count for July
        TextView permissionCountTextView = findViewById(R.id.pm7); // Replace with your TextView ID for July
        permissionCountTextView.setText(String.valueOf(totalCount));

        // Save the total count to Firebase for this month
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        String monthYearKey = "July-" + currentYear; // Example: "July-2024"
        DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthYearKey);
        monthPermissionRecordsRef.setValue(totalCount).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
            } else {
            }
        });
    }


    private void countPermissionsForAugust() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference clockInPermissionsRef = mDatabase.child("ClockInPermission");
        DatabaseReference clockOutPermissionsRef = mDatabase.child("ClockOutPermission");

        // Set current year and August month
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int augustMonth = 8; // August (1-based index)

        final double[] totalPermissionCount = {0.0}; // Initialize the total count

        ValueEventListener countPermissionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot recordSnapshot : snapshot.getChildren()) {
                        String requestUID = recordSnapshot.child("uid").getValue(String.class);
                        String date = recordSnapshot.child("date").getValue(String.class); // Get the date string (dd-MM-yyyy)
                        String status = recordSnapshot.child("status").getValue(String.class);

                        // Check if the UID matches and status is approved
                        if (userId.equals(requestUID) && "approved".equalsIgnoreCase(status) && date != null) {
                            try {
                                // Parse the date string (dd-MM-yyyy)
                                String[] dateParts = date.split("-"); // Split "15-08-2024" into {"15", "08", "2024"}
                                int requestMonth = Integer.parseInt(dateParts[1]); // Extract month (1-based index)
                                int requestYear = Integer.parseInt(dateParts[2]); // Extract year

                                // Check if the date corresponds to August of the current year
                                if (requestYear == currentYear && requestMonth == augustMonth) {
                                    totalPermissionCount[0] += 0.5; // Increment by 0.5 for approved status
                                }
                            } catch (Exception e) {
                                e.printStackTrace(); // Handle any potential parsing errors
                            }
                        }
                    }
                }

                // Update the UI with the total count after processing
                updatePermissionCountForAugust(totalPermissionCount[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission data", Toast.LENGTH_SHORT).show();
            }
        };

        // Query clock-in permissions
        clockInPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);

        // Query clock-out permissions
        clockOutPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);
    }

    private void updatePermissionCountForAugust(double totalCount) {
        // Update the UI with the total count for August
        TextView permissionCountTextView = findViewById(R.id.pm8); // Replace with your TextView ID for August
        permissionCountTextView.setText(String.valueOf(totalCount));

        // Save the total count to Firebase for this month
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        String monthYearKey = "August-" + currentYear; // Example: "August-2024"
        DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthYearKey);
        monthPermissionRecordsRef.setValue(totalCount).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
            } else {
            }
        });
    }


    private void countPermissionsForSeptember() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference clockInPermissionsRef = mDatabase.child("ClockInPermission");
        DatabaseReference clockOutPermissionsRef = mDatabase.child("ClockOutPermission");

        // Get current year and September month
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int septemberMonth = 9; // September (1-based index)

        final double[] totalPermissionCount = {0.0}; // Keep track of the total count

        ValueEventListener countPermissionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot recordSnapshot : snapshot.getChildren()) {
                        String requestUID = recordSnapshot.child("uid").getValue(String.class);
                        String date = recordSnapshot.child("date").getValue(String.class); // Get the date string (dd-MM-yyyy)
                        String status = recordSnapshot.child("status").getValue(String.class);

                        // Check if the UID matches and status is approved
                        if (userId.equals(requestUID) && "approved".equalsIgnoreCase(status) && date != null) {
                            try {
                                // Extract the month from the date string (dd-MM-yyyy)
                                String[] dateParts = date.split("-"); // Split "15-09-2024" into {"15", "09", "2024"}
                                int requestMonth = Integer.parseInt(dateParts[1]); // Get the month part (1-based index)

                                // Check if the date corresponds to September of the current year
                                if (requestMonth == septemberMonth) {
                                    totalPermissionCount[0] += 0.5; // Increment by 0.5 for approved status
                                }
                            } catch (Exception e) {
                                e.printStackTrace(); // Handle any potential parsing errors
                            }
                        }
                    }
                }

                // After processing this node, check if the total count is complete
                updatePermissionCountForSeptember(totalPermissionCount[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission data", Toast.LENGTH_SHORT).show();
            }
        };

        // Query clock-in permissions
        clockInPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);

        // Query clock-out permissions
        clockOutPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);
    }

    private void updatePermissionCountForSeptember(double totalCount) {
        // Update the UI with the total count for September
        TextView permissionCountTextView = findViewById(R.id.pm9); // Adjust this ID to your TextView
        permissionCountTextView.setText(String.valueOf(totalCount));

        // Save the total count to Firebase for this month
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        String monthYearKey = "September-" + currentYear; // Example: "September-2024"
        DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthYearKey);
        monthPermissionRecordsRef.setValue(totalCount).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
            } else {
            }
        });
    }


    private void countPermissionsForOctober() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference clockInPermissionsRef = mDatabase.child("ClockInPermission");
        DatabaseReference clockOutPermissionsRef = mDatabase.child("ClockOutPermission");

        // Get current year and October month
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int octoberMonth = 10; // October (1-based index)

        final double[] totalPermissionCount = {0.0}; // Keep track of the total count

        ValueEventListener countPermissionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot recordSnapshot : snapshot.getChildren()) {
                        String requestUID = recordSnapshot.child("uid").getValue(String.class);
                        String date = recordSnapshot.child("date").getValue(String.class); // Get the date string (dd-MM-yyyy)
                        String status = recordSnapshot.child("status").getValue(String.class);

                        // Check if the UID matches and status is approved
                        if (userId.equals(requestUID) && "approved".equalsIgnoreCase(status) && date != null) {
                            try {
                                // Extract the month from the date string (dd-MM-yyyy)
                                String[] dateParts = date.split("-"); // Split "28-10-2024" into {"28", "10", "2024"}
                                int requestMonth = Integer.parseInt(dateParts[1]); // Get the month part (1-based index)

                                // Check if the date corresponds to October of the current year
                                if (requestMonth == octoberMonth) {
                                    totalPermissionCount[0] += 0.5; // Increment by 0.5 for approved status
                                }
                            } catch (Exception e) {
                                e.printStackTrace(); // Handle any potential parsing errors
                            }
                        }
                    }
                }

                // After processing this node, check if the total count is complete
                updatePermissionCountForOctober(totalPermissionCount[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission data", Toast.LENGTH_SHORT).show();
            }
        };

        // Query clock-in permissions
        clockInPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);

        // Query clock-out permissions
        clockOutPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);
    }

    private void updatePermissionCountForOctober(double totalCount) {
        // Update the UI with the total count for October
        TextView permissionCountTextView = findViewById(R.id.pm10); // Adjust this ID to your TextView
        permissionCountTextView.setText(String.valueOf(totalCount));

        // Save the total count to Firebase for this month
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        String monthYearKey = "October-" + currentYear; // Example: "October-2024"
        DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthYearKey);
        monthPermissionRecordsRef.setValue(totalCount).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
            } else {
            }
        });
    }


    private void countPermissionsForNovember() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference clockInPermissionsRef = mDatabase.child("ClockInPermission");
        DatabaseReference clockOutPermissionsRef = mDatabase.child("ClockOutPermission");

        // Get current year and November month
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int novemberMonth = 11; // November (1-based index)

        final double[] totalPermissionCount = {0.0}; // Keep track of the total count

        ValueEventListener countPermissionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot recordSnapshot : snapshot.getChildren()) {
                        String requestUID = recordSnapshot.child("uid").getValue(String.class);
                        String date = recordSnapshot.child("date").getValue(String.class); // Get the date string (dd-MM-yyyy)
                        String status = recordSnapshot.child("status").getValue(String.class);

                        // Check if the UID matches and status is approved
                        if (userId.equals(requestUID) && "approved".equalsIgnoreCase(status) && date != null) {
                            try {
                                // Extract the month from the date string (dd-MM-yyyy)
                                String[] dateParts = date.split("-"); // Split "28-11-2024" into {"28", "11", "2024"}
                                int requestMonth = Integer.parseInt(dateParts[1]); // Get the month part (1-based index)

                                // Check if the date corresponds to November of the current year
                                if (requestMonth == novemberMonth) {
                                    totalPermissionCount[0] += 0.5; // Increment by 0.5 for approved status
                                }
                            } catch (Exception e) {
                                e.printStackTrace(); // Handle any potential parsing errors
                            }
                        }
                    }
                }

                // After processing this node, check if the total count is complete
                updatePermissionCount(totalPermissionCount[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission data", Toast.LENGTH_SHORT).show();
            }
        };

        // Query clock-in permissions
        clockInPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);

        // Query clock-out permissions
        clockOutPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);
    }

    private void updatePermissionCount(double totalCount) {
        // Update the UI with the total count for November
        TextView permissionCountTextView = findViewById(R.id.pm11); // Adjust this ID to your TextView
        permissionCountTextView.setText(String.valueOf(totalCount));

        // Save the total count to Firebase for this month
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        String monthYearKey = "November-" + currentYear; // Example: "November-2024"
        DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthYearKey);
        monthPermissionRecordsRef.setValue(totalCount).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
            } else {
            }
        });
    }


    private void countPermissionsForDecember() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference clockInPermissionsRef = mDatabase.child("ClockInPermission");
        DatabaseReference clockOutPermissionsRef = mDatabase.child("ClockOutPermission");

        // Set current year and December month
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int decemberMonth = 12; // December (1-based index)

        final double[] totalPermissionCount = {0.0};

        ValueEventListener countPermissionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot recordSnapshot : snapshot.getChildren()) {
                        String requestUID = recordSnapshot.child("uid").getValue(String.class);
                        String date = recordSnapshot.child("date").getValue(String.class);
                        String status = recordSnapshot.child("status").getValue(String.class);

                        if (userId.equals(requestUID) && "approved".equalsIgnoreCase(status) && date != null) {
                            try {
                                String[] dateParts = date.split("-");
                                int requestMonth = Integer.parseInt(dateParts[1]);
                                int requestYear = Integer.parseInt(dateParts[2]);

                                if (requestMonth == decemberMonth && requestYear == currentYear) {
                                    totalPermissionCount[0] += 0.5;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                TextView permissionCountTextView = findViewById(R.id.pm12); // Adjust this ID for December
                permissionCountTextView.setText(String.valueOf(totalPermissionCount[0]));

                savePermissionCountToFirebase(userId, "December-" + currentYear, totalPermissionCount[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch permission data", Toast.LENGTH_SHORT).show();
            }
        };

        clockInPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);
        clockOutPermissionsRef.addListenerForSingleValueEvent(countPermissionListener);
    }

    private void savePermissionCountToFirebase(String userId, String monthYearKey, double totalCount) {
        DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthPermissionRecords").child(monthYearKey);
        monthPermissionRecordsRef.setValue(totalCount).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
            } else {
            }
        });
    }

    // Permission calculation //


    // Skipping calculation //

    private void countApprovedLateRequestsForJanuary() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference lateRequestsRef = mDatabase.child("LateRequests");

        // Set January date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int januaryMonth = 1; // January (1-based index)

        // Track the count of approved late requests
        final int[] approvedLateRequestCount = {0};

        lateRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Check if the userId in this late request matches the current user's UID
                    String requestUserId = snapshot.child("userId").getValue(String.class);
                    String requestStatus = snapshot.child("status").getValue(String.class);

                    // Only process if the request is for the current user and status is "approved"
                    if (userId.equals(requestUserId) && "updated".equalsIgnoreCase(requestStatus)) {
                        // Fetch the date of the late request
                        String date = snapshot.child("date").getValue(String.class);
                        if (date != null) {
                            try {
                                // Parse the date in the format "dd-MM-yyyy"
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                Date requestDate = dateFormat.parse(date.trim());

                                // Create a calendar instance for the request date
                                Calendar requestCalendar = Calendar.getInstance();
                                requestCalendar.setTime(requestDate);

                                int requestYear = requestCalendar.get(Calendar.YEAR);
                                int requestMonth = requestCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based

                                // Check if the request is from January of the current year
                                if (requestYear == currentYear && requestMonth == januaryMonth) {
                                    approvedLateRequestCount[0]++;
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Display the total approved late request count for January
                TextView approvedLateRequestCountTextView = findViewById(R.id.sm1); // Adjust to your TextView ID for January approved late requests
                approvedLateRequestCountTextView.setText(String.valueOf(approvedLateRequestCount[0]));

                String monthYearKey = "January-" + currentYear; // Example: "January-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(approvedLateRequestCount[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch late request data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countApprovedLateRequestsForFebruary() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference lateRequestsRef = mDatabase.child("LateRequests");

        // Set February date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int februaryMonth = 2; // February (1-based index)

        // Track the count of approved late requests
        final int[] approvedLateRequestCount = {0};

        lateRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Check if the userId in this late request matches the current user's UID
                    String requestUserId = snapshot.child("userId").getValue(String.class);
                    String requestStatus = snapshot.child("status").getValue(String.class);

                    // Only process if the request is for the current user and status is "approved"
                    if (userId.equals(requestUserId) && "updated".equalsIgnoreCase(requestStatus)) {
                        // Fetch the date of the late request
                        String date = snapshot.child("date").getValue(String.class);
                        if (date != null) {
                            try {
                                // Parse the date in the format "dd-MM-yyyy"
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                Date requestDate = dateFormat.parse(date.trim());

                                // Create a calendar instance for the request date
                                Calendar requestCalendar = Calendar.getInstance();
                                requestCalendar.setTime(requestDate);

                                int requestYear = requestCalendar.get(Calendar.YEAR);
                                int requestMonth = requestCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based

                                // Check if the request is from February of the current year
                                if (requestYear == currentYear && requestMonth == februaryMonth) {
                                    approvedLateRequestCount[0]++;
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Display the total approved late request count for February
                TextView approvedLateRequestCountTextView = findViewById(R.id.sm2); // Adjust to your TextView ID for February approved late requests
                approvedLateRequestCountTextView.setText(String.valueOf(approvedLateRequestCount[0]));

                String monthYearKey = "February-" + currentYear; // Example: "January-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(approvedLateRequestCount[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch late request data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countLateRequestsForMarch() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference lateRequestsRef = mDatabase.child("LateRequests");

        // Set March date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int marchMonth = 3; // March (1-based index)

        // Track the count of late requests
        final int[] lateRequestCount = {0};

        lateRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Check if the UID in this late request matches the current user's UID
                    String requestUID = snapshot.child("userId").getValue(String.class);
                    if (userId.equals(requestUID)) {
                        // Fetch the date of the late request
                        String date = snapshot.child("date").getValue(String.class);
                        if (date != null) {
                            try {
                                // Parse the date in the format "dd-MM-yyyy"
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                Date requestDate = dateFormat.parse(date);

                                // Create a calendar instance for the request date
                                Calendar requestCalendar = Calendar.getInstance();
                                requestCalendar.setTime(requestDate);

                                int requestYear = requestCalendar.get(Calendar.YEAR);
                                int requestMonth = requestCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based

                                // Check if the request is from March of the current year
                                if (requestYear == currentYear && requestMonth == marchMonth) {
                                    // Check if the status is "approved"
                                    String status = snapshot.child("status").getValue(String.class);
                                    if ("updated".equalsIgnoreCase(status)) {
                                        lateRequestCount[0]++;
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Display the total late request count for March
                TextView lateRequestCountTextView = findViewById(R.id.sm3); // Assuming sm11 is your TextView
                lateRequestCountTextView.setText(String.valueOf(lateRequestCount[0]));

                String monthYearKey = "March-" + currentYear; // Example: "January-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(lateRequestCount[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch late request data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countLateRequestsForApril() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference lateRequestsRef = mDatabase.child("LateRequests");

        // Set April date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int aprilMonth = 4; // April (1-based index)

        // Track the count of late requests
        final int[] lateRequestCount = {0};

        lateRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Check if the UID in this late request matches the current user's UID
                    String requestUID = snapshot.child("userId").getValue(String.class);
                    if (userId.equals(requestUID)) {
                        // Fetch the date of the late request
                        String date = snapshot.child("date").getValue(String.class);
                        if (date != null) {
                            try {
                                // Parse the date in the format "dd-MM-yyyy"
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                Date requestDate = dateFormat.parse(date);

                                // Create a calendar instance for the request date
                                Calendar requestCalendar = Calendar.getInstance();
                                requestCalendar.setTime(requestDate);

                                int requestYear = requestCalendar.get(Calendar.YEAR);
                                int requestMonth = requestCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based

                                // Check if the request is from April of the current year
                                if (requestYear == currentYear && requestMonth == aprilMonth) {
                                    // Check if the status is "approved"
                                    String status = snapshot.child("status").getValue(String.class);
                                    if ("updated".equalsIgnoreCase(status)) {
                                        lateRequestCount[0]++;
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Display the total late request count for April
                TextView lateRequestCountTextView = findViewById(R.id.sm4); // Assuming sm11 is your TextView
                lateRequestCountTextView.setText(String.valueOf(lateRequestCount[0]));

                String monthYearKey = "April-" + currentYear; // Example: "January-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(lateRequestCount[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch late request data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countLateRequestsForMay() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference lateRequestsRef = mDatabase.child("LateRequests");

        // Set May date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int mayMonth = 5; // May (1-based index)

        // Track the count of late requests
        final int[] lateRequestCount = {0};

        lateRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Check if the UID in this late request matches the current user's UID
                    String requestUID = snapshot.child("userId").getValue(String.class);
                    if (userId.equals(requestUID)) {
                        // Fetch the date of the late request
                        String date = snapshot.child("date").getValue(String.class);
                        if (date != null) {
                            try {
                                // Parse the date in the format "dd-MM-yyyy"
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                Date requestDate = dateFormat.parse(date);

                                // Create a calendar instance for the request date
                                Calendar requestCalendar = Calendar.getInstance();
                                requestCalendar.setTime(requestDate);

                                int requestYear = requestCalendar.get(Calendar.YEAR);
                                int requestMonth = requestCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based

                                // Check if the request is from May of the current year
                                if (requestYear == currentYear && requestMonth == mayMonth) {
                                    // Check if the status is "approved"
                                    String status = snapshot.child("status").getValue(String.class);
                                    if ("updated".equalsIgnoreCase(status)) {
                                        lateRequestCount[0]++;
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Display the total late request count for May
                TextView lateRequestCountTextView = findViewById(R.id.sm5); // Assuming sm11 is your TextView
                lateRequestCountTextView.setText(String.valueOf(lateRequestCount[0]));

                String monthYearKey = "May-" + currentYear; // Example: "January-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(lateRequestCount[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch late request data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countLateRequestsForJune() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference lateRequestsRef = mDatabase.child("LateRequests");

        // Set June date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int juneMonth = 6; // June (1-based index)

        // Track the count of late requests
        final int[] lateRequestCount = {0};

        lateRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Check if the UID in this late request matches the current user's UID
                    String requestUID = snapshot.child("userId").getValue(String.class);
                    if (userId.equals(requestUID)) {
                        // Fetch the date of the late request
                        String date = snapshot.child("date").getValue(String.class);
                        if (date != null) {
                            try {
                                // Parse the date in the format "dd-MM-yyyy"
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                Date requestDate = dateFormat.parse(date);

                                // Create a calendar instance for the request date
                                Calendar requestCalendar = Calendar.getInstance();
                                requestCalendar.setTime(requestDate);

                                int requestYear = requestCalendar.get(Calendar.YEAR);
                                int requestMonth = requestCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based

                                // Check if the request is from June of the current year
                                if (requestYear == currentYear && requestMonth == juneMonth) {
                                    // Check if the status is "approved"
                                    String status = snapshot.child("status").getValue(String.class);
                                    if ("updated".equalsIgnoreCase(status)) {
                                        lateRequestCount[0]++;
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Display the total late request count for June
                TextView lateRequestCountTextView = findViewById(R.id.sm6); // Assuming sm11 is your TextView
                lateRequestCountTextView.setText(String.valueOf(lateRequestCount[0]));

                String monthYearKey = "June-" + currentYear; // Example: "January-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(lateRequestCount[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch late request data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countLateRequestsForJuly() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference lateRequestsRef = mDatabase.child("LateRequests");

        // Set July date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int julyMonth = 7; // July (1-based index)

        // Track the count of late requests
        final int[] lateRequestCount = {0};

        lateRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Check if the UID in this late request matches the current user's UID
                    String requestUID = snapshot.child("userId").getValue(String.class);
                    if (userId.equals(requestUID)) {
                        // Fetch the date of the late request
                        String date = snapshot.child("date").getValue(String.class);
                        if (date != null) {
                            try {
                                // Parse the date in the format "dd-MM-yyyy"
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                Date requestDate = dateFormat.parse(date);

                                // Create a calendar instance for the request date
                                Calendar requestCalendar = Calendar.getInstance();
                                requestCalendar.setTime(requestDate);

                                int requestYear = requestCalendar.get(Calendar.YEAR);
                                int requestMonth = requestCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based

                                // Check if the request is from July of the current year
                                if (requestYear == currentYear && requestMonth == julyMonth) {
                                    // Check if the status is "approved"
                                    String status = snapshot.child("status").getValue(String.class);
                                    if ("updated".equalsIgnoreCase(status)) {
                                        lateRequestCount[0]++;
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Display the total late request count for July
                TextView lateRequestCountTextView = findViewById(R.id.sm7); // Assuming sm11 is your TextView
                lateRequestCountTextView.setText(String.valueOf(lateRequestCount[0]));

                String monthYearKey = "July-" + currentYear; // Example: "January-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(lateRequestCount[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch late request data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countLateRequestsForAugust() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference lateRequestsRef = mDatabase.child("LateRequests");

        // Set August date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int augustMonth = 8; // August (1-based index)

        // Track the count of late requests
        final int[] lateRequestCount = {0};

        lateRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Check if the UID in this late request matches the current user's UID
                    String requestUID = snapshot.child("userId").getValue(String.class);
                    if (userId.equals(requestUID)) {
                        // Fetch the date of the late request
                        String date = snapshot.child("date").getValue(String.class);
                        if (date != null) {
                            try {
                                // Parse the date in the format "dd-MM-yyyy"
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                Date requestDate = dateFormat.parse(date);

                                // Create a calendar instance for the request date
                                Calendar requestCalendar = Calendar.getInstance();
                                requestCalendar.setTime(requestDate);

                                int requestYear = requestCalendar.get(Calendar.YEAR);
                                int requestMonth = requestCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based

                                // Check if the request is from August of the current year
                                if (requestYear == currentYear && requestMonth == augustMonth) {
                                    // Check if the status is "approved"
                                    String status = snapshot.child("status").getValue(String.class);
                                    if ("updated".equalsIgnoreCase(status)) {
                                        lateRequestCount[0]++;
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Display the total late request count for August
                TextView lateRequestCountTextView = findViewById(R.id.sm8); // Assuming sm11 is your TextView
                lateRequestCountTextView.setText(String.valueOf(lateRequestCount[0]));

                String monthYearKey = "August-" + currentYear; // Example: "January-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(lateRequestCount[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch late request data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countLateRequestsForSeptember() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference lateRequestsRef = mDatabase.child("LateRequests");

        // Set September date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int septemberMonth = 9; // September (1-based index)

        // Track the count of late requests
        final int[] lateRequestCount = {0};

        lateRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Check if the UID in this late request matches the current user's UID
                    String requestUID = snapshot.child("userId").getValue(String.class);
                    if (userId.equals(requestUID)) {
                        // Fetch the date of the late request
                        String date = snapshot.child("date").getValue(String.class);
                        if (date != null) {
                            try {
                                // Parse the date in the format "dd-MM-yyyy"
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                Date requestDate = dateFormat.parse(date);

                                // Create a calendar instance for the request date
                                Calendar requestCalendar = Calendar.getInstance();
                                requestCalendar.setTime(requestDate);

                                int requestYear = requestCalendar.get(Calendar.YEAR);
                                int requestMonth = requestCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based

                                // Check if the request is from September of the current year
                                if (requestYear == currentYear && requestMonth == septemberMonth) {
                                    // Check if the status is "approved"
                                    String status = snapshot.child("status").getValue(String.class);
                                    if ("updated".equalsIgnoreCase(status)) {
                                        lateRequestCount[0]++;
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Display the total late request count for September
                TextView lateRequestCountTextView = findViewById(R.id.sm9); // Assuming sm11 is your TextView
                lateRequestCountTextView.setText(String.valueOf(lateRequestCount[0]));

                String monthYearKey = "September-" + currentYear; // Example: "January-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(lateRequestCount[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch late request data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countApprovedLateRequestsForOctober() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference lateRequestsRef = mDatabase.child("LateRequests");

        // Set October date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int octoberMonth = 10; // October (1-based index)

        // Track the count of approved late requests
        final int[] approvedLateRequestCount = {0};

        lateRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Check if the userId in this late request matches the current user's UID
                    String requestUserId = snapshot.child("userId").getValue(String.class);
                    String requestStatus = snapshot.child("status").getValue(String.class);

                    // Only process if the request is for the current user and status is "approved"
                    if (userId.equals(requestUserId) && "updated".equalsIgnoreCase(requestStatus)) {
                        // Fetch the date of the late request
                        String date = snapshot.child("date").getValue(String.class);
                        if (date != null) {
                            try {
                                // Parse the date in the format "dd-MM-yyyy"
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                Date requestDate = dateFormat.parse(date.trim());

                                // Create a calendar instance for the request date
                                Calendar requestCalendar = Calendar.getInstance();
                                requestCalendar.setTime(requestDate);

                                int requestYear = requestCalendar.get(Calendar.YEAR);
                                int requestMonth = requestCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based

                                // Check if the request is from October of the current year
                                if (requestYear == currentYear && requestMonth == octoberMonth) {
                                    approvedLateRequestCount[0]++;
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Display the total approved late request count for October
                TextView approvedLateRequestCountTextView = findViewById(R.id.sm10); // Adjust to your TextView ID for October approved late requests
                approvedLateRequestCountTextView.setText(String.valueOf(approvedLateRequestCount[0]));

                String monthYearKey = "October-" + currentYear; // Example: "January-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(approvedLateRequestCount[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch late request data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countApprovedLateRequestsForNovember() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference lateInRequestsRef = mDatabase.child("LateInRequests");
        DatabaseReference lateOutRequestsRef = mDatabase.child("LateOutRequests");

        // Set November date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int novemberMonth = 11; // November (1-based index)

        // Track the total count
        final double[] totalApprovedLateRequests = {0.0};

        ValueEventListener countLateRequestsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Check if the userId in this late request matches the current user's UID
                    String requestUserId = snapshot.child("userId").getValue(String.class);
                    String requestStatus = snapshot.child("status").getValue(String.class);

                    // Only process if the request is for the current user and status is "approved"
                    if (userId.equals(requestUserId) && "approved".equalsIgnoreCase(requestStatus)) {
                        // Fetch the date of the late request
                        String date = snapshot.child("date").getValue(String.class);
                        if (date != null) {
                            try {
                                // Parse the date in the format "dd-MM-yyyy"
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                Date requestDate = dateFormat.parse(date.trim());

                                // Create a calendar instance for the request date
                                Calendar requestCalendar = Calendar.getInstance();
                                requestCalendar.setTime(requestDate);

                                int requestYear = requestCalendar.get(Calendar.YEAR);
                                int requestMonth = requestCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based

                                // Check if the request is from November of the current year
                                if (requestYear == currentYear && requestMonth == novemberMonth) {
                                    totalApprovedLateRequests[0] += 0.5;
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Display the total approved late request count for November
                TextView approvedLateRequestCountTextView = findViewById(R.id.sm11); // Adjust to your TextView ID for November approved late requests
                approvedLateRequestCountTextView.setText(String.valueOf(totalApprovedLateRequests[0]));

                String monthYearKey = "November-" + currentYear; // Example: "November-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(totalApprovedLateRequests[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch late request data", Toast.LENGTH_SHORT).show();
            }
        };

        // Add listeners for both LateInRequests and LateOutRequests
        lateInRequestsRef.addListenerForSingleValueEvent(countLateRequestsListener);
        lateOutRequestsRef.addListenerForSingleValueEvent(countLateRequestsListener);
    }

    private void countApprovedLateRequestsForDecember() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference lateInRequestsRef = mDatabase.child("LateInRequests");
        DatabaseReference lateOutRequestsRef = mDatabase.child("LateOutRequests");

        // Set December date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int decemberMonth = 12; // December (1-based index)

        // Track the total count
        final double[] totalApprovedLateRequests = {0.0};

        ValueEventListener countLateRequestsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Check if the userId in this late request matches the current user's UID
                    String requestUserId = snapshot.child("userId").getValue(String.class);
                    String requestStatus = snapshot.child("status").getValue(String.class);

                    // Only process if the request is for the current user and status is "approved"
                    if (userId.equals(requestUserId) && "approved".equalsIgnoreCase(requestStatus)) {
                        // Fetch the date of the late request
                        String date = snapshot.child("date").getValue(String.class);
                        if (date != null) {
                            try {
                                // Parse the date in the format "dd-MM-yyyy"
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                Date requestDate = dateFormat.parse(date.trim());

                                // Create a calendar instance for the request date
                                Calendar requestCalendar = Calendar.getInstance();
                                requestCalendar.setTime(requestDate);

                                int requestYear = requestCalendar.get(Calendar.YEAR);
                                int requestMonth = requestCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based

                                // Check if the request is from December of the current year
                                if (requestYear == currentYear && requestMonth == decemberMonth) {
                                    totalApprovedLateRequests[0] += 0.5; // Increment by 0.5 for each approved request
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Display the total approved late request count for December
                TextView approvedLateRequestCountTextView = findViewById(R.id.sm12); // Adjust to your TextView ID for December approved late requests
                approvedLateRequestCountTextView.setText(String.valueOf(totalApprovedLateRequests[0]));

                String monthYearKey = "December-" + currentYear; // Example: "December-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthSMRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(totalApprovedLateRequests[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch late request data", Toast.LENGTH_SHORT).show();
            }
        };

        // Add listeners for both LateInRequests and LateOutRequests
        lateInRequestsRef.addListenerForSingleValueEvent(countLateRequestsListener);
        lateOutRequestsRef.addListenerForSingleValueEvent(countLateRequestsListener);
    }


    // Skipping calculation //


    // OT calculation //


    private void countOTForJanuary() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference usersRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set January date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int januaryMonth = Calendar.JANUARY; // January (1-based index)

        // Track the total OT count for Sundays
        final float[] totalOTCount = {0};

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Fetch the date, clock-in, and clock-out time
                    String date = snapshot.child("dd-MM-yyyy").getValue(String.class);
                    String clockInTime = snapshot.child("ClockInTime").getValue(String.class);
                    String clockOutTime = snapshot.child("ClockOutTime").getValue(String.class);

                    if (date != null) {
                        try {
                            // Parse the date in the format "dd-MM-yyyy"
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            Date recordDate = dateFormat.parse(date.trim());

                            // Create a calendar instance for the record date
                            Calendar recordCalendar = Calendar.getInstance();
                            recordCalendar.setTime(recordDate);

                            int recordYear = recordCalendar.get(Calendar.YEAR);
                            int recordMonth = recordCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based
                            int recordDayOfWeek = recordCalendar.get(Calendar.DAY_OF_WEEK); // Get the day of the week

                            // Check if the request is from January of the current year and falls on a Sunday
                            if (recordYear == currentYear && recordMonth == januaryMonth && recordDayOfWeek == Calendar.SUNDAY) {
                                // Count 0.5 for clock-in time if available and is on or before 9:15 AM
                                if (clockInTime != null && !clockInTime.isEmpty()) {
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    Date clockIn = timeFormat.parse(clockInTime);

                                    Date limitClockIn = timeFormat.parse("09:15");

                                    if (clockIn != null && clockIn.before(limitClockIn) || clockIn.equals(limitClockIn)) {
                                        totalOTCount[0] += 0.5f;
                                    }
                                }

                                // Count 0.5 for clock-out time if available and is on or after 17:45
                                if (clockOutTime != null && !clockOutTime.isEmpty()) {
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    Date clockOut = timeFormat.parse(clockOutTime);

                                    Date limitClockOut = timeFormat.parse("17:45");

                                    if (clockOut != null && clockOut.after(limitClockOut) || clockOut.equals(limitClockOut)) {
                                        totalOTCount[0] += 0.5f;
                                    }
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Display the total OT count for Sundays in January
                TextView otCountTextView = findViewById(R.id.coff1); // Adjust to your TextView ID for OT count in January
                otCountTextView.setText(String.valueOf(totalOTCount[0]));

                String monthYearKey = "January-" + currentYear; // Example: "January-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthCOffRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(totalOTCount[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch user time records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countOTForFebruary() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference usersRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set February date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int februaryMonth = 2; // February (1-based index)

        // Track the total OT count for Sundays in February
        final float[] totalOTCount = {0};

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Fetch the date, clock-in, and clock-out time
                    String date = snapshot.child("date").getValue(String.class);
                    String clockInTime = snapshot.child("clockInTime").getValue(String.class);
                    String clockOutTime = snapshot.child("clockOutTime").getValue(String.class);

                    if (date != null) {
                        try {
                            // Parse the date in the format "dd-MM-yyyy"
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            Date recordDate = dateFormat.parse(date.trim());

                            // Create a calendar instance for the record date
                            Calendar recordCalendar = Calendar.getInstance();
                            recordCalendar.setTime(recordDate);

                            int recordYear = recordCalendar.get(Calendar.YEAR);
                            int recordMonth = recordCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based
                            int recordDayOfWeek = recordCalendar.get(Calendar.DAY_OF_WEEK); // Get the day of the week

                            // Check if the request is from February of the current year and falls on a Sunday
                            if (recordYear == currentYear && recordMonth == februaryMonth && recordDayOfWeek == Calendar.SUNDAY) {
                                // Count 0.5 for clock-in time if available and is on or before 9:15 AM
                                if (clockInTime != null && !clockInTime.isEmpty()) {
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    Date clockIn = timeFormat.parse(clockInTime);

                                    Date limitClockIn = timeFormat.parse("09:15");

                                    if (clockIn != null && clockIn.before(limitClockIn) || clockIn.equals(limitClockIn)) {
                                        totalOTCount[0] += 0.5f;
                                    }
                                }

                                // Count 0.5 for clock-out time if available and is on or after 17:45
                                if (clockOutTime != null && !clockOutTime.isEmpty()) {
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    Date clockOut = timeFormat.parse(clockOutTime);

                                    Date limitClockOut = timeFormat.parse("17:45");

                                    if (clockOut != null && clockOut.after(limitClockOut) || clockOut.equals(limitClockOut)) {
                                        totalOTCount[0] += 0.5f;
                                    }
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Display the total OT count for Sundays in February
                TextView otCountTextView = findViewById(R.id.coff1); // Adjust to your TextView ID for OT count in February
                otCountTextView.setText(String.valueOf(totalOTCount[0]));

                String monthYearKey = "February-" + currentYear; // Example: "February-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthCOffRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(totalOTCount[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch user time records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countOTForMarch() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference usersRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set March date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int marchMonth = 3; // March (1-based index)

        // Track the total OT count for Sundays
        final float[] totalOTCount = {0};

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Fetch the date, clock-in, and clock-out time
                    String date = snapshot.child("date").getValue(String.class);
                    String clockInTime = snapshot.child("clockInTime").getValue(String.class);
                    String clockOutTime = snapshot.child("clockOutTime").getValue(String.class);

                    if (date != null) {
                        try {
                            // Parse the date in the format "dd-MM-yyyy"
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            Date recordDate = dateFormat.parse(date.trim());

                            // Create a calendar instance for the record date
                            Calendar recordCalendar = Calendar.getInstance();
                            recordCalendar.setTime(recordDate);

                            int recordYear = recordCalendar.get(Calendar.YEAR);
                            int recordMonth = recordCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based
                            int recordDayOfWeek = recordCalendar.get(Calendar.DAY_OF_WEEK); // Get the day of the week

                            // Check if the request is from March of the current year and falls on a Sunday
                            if (recordYear == currentYear && recordMonth == marchMonth && recordDayOfWeek == Calendar.SUNDAY) {
                                // Count 0.5 for clock-in time if available and is on or before 9:15 AM
                                if (clockInTime != null && !clockInTime.isEmpty()) {
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    Date clockIn = timeFormat.parse(clockInTime);

                                    Date limitClockIn = timeFormat.parse("09:15");

                                    if (clockIn != null && clockIn.before(limitClockIn) || clockIn.equals(limitClockIn)) {
                                        totalOTCount[0] += 0.5f;
                                    }
                                }

                                // Count 0.5 for clock-out time if available and is on or after 17:45
                                if (clockOutTime != null && !clockOutTime.isEmpty()) {
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    Date clockOut = timeFormat.parse(clockOutTime);

                                    Date limitClockOut = timeFormat.parse("17:45");

                                    if (clockOut != null && clockOut.after(limitClockOut) || clockOut.equals(limitClockOut)) {
                                        totalOTCount[0] += 0.5f;
                                    }
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Display the total OT count for Sundays in March
                TextView otCountTextView = findViewById(R.id.coff3); // Adjust to your TextView ID for OT count in March
                otCountTextView.setText(String.valueOf(totalOTCount[0]));

                String monthYearKey = "March-" + currentYear; // Example: "March-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthCOffRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(totalOTCount[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch user time records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countOTForApril() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference usersRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set April date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int aprilMonth = 4; // April (1-based index)

        // Track the total OT count for Sundays
        final float[] totalOTCount = {0};

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Fetch the date, clock-in, and clock-out time
                    String date = snapshot.child("date").getValue(String.class);
                    String clockInTime = snapshot.child("clockInTime").getValue(String.class);
                    String clockOutTime = snapshot.child("clockOutTime").getValue(String.class);

                    if (date != null) {
                        try {
                            // Parse the date in the format "dd-MM-yyyy"
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            Date recordDate = dateFormat.parse(date.trim());

                            // Create a calendar instance for the record date
                            Calendar recordCalendar = Calendar.getInstance();
                            recordCalendar.setTime(recordDate);

                            int recordYear = recordCalendar.get(Calendar.YEAR);
                            int recordMonth = recordCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based
                            int recordDayOfWeek = recordCalendar.get(Calendar.DAY_OF_WEEK); // Get the day of the week

                            // Check if the request is from April of the current year and falls on a Sunday
                            if (recordYear == currentYear && recordMonth == aprilMonth && recordDayOfWeek == Calendar.SUNDAY) {
                                // Count 0.5 for clock-in time if available
                                if (clockInTime != null && !clockInTime.isEmpty()) {
                                    totalOTCount[0] += 0.5f;
                                }

                                // Count 0.5 for clock-out time if available
                                if (clockOutTime != null && !clockOutTime.isEmpty()) {
                                    totalOTCount[0] += 0.5f;
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Display the total OT count for Sundays in April
                TextView otCountTextView = findViewById(R.id.coff4); // Adjust to your TextView ID for OT count in April
                otCountTextView.setText(String.valueOf(totalOTCount[0]));

                String monthYearKey = "April-" + currentYear; // Example: "April-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthCOffRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(totalOTCount[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch user time records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countOTForMay() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference usersRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set May date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int mayMonth = 5; // May (1-based index)

        // Track the total OT count for Sundays
        final float[] totalOTCount = {0};

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Fetch the date, clock-in, and clock-out time
                    String date = snapshot.child("date").getValue(String.class);
                    String clockInTime = snapshot.child("clockInTime").getValue(String.class);
                    String clockOutTime = snapshot.child("clockOutTime").getValue(String.class);

                    if (date != null) {
                        try {
                            // Parse the date in the format "dd-MM-yyyy"
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            Date recordDate = dateFormat.parse(date.trim());

                            // Create a calendar instance for the record date
                            Calendar recordCalendar = Calendar.getInstance();
                            recordCalendar.setTime(recordDate);

                            int recordYear = recordCalendar.get(Calendar.YEAR);
                            int recordMonth = recordCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based
                            int recordDayOfWeek = recordCalendar.get(Calendar.DAY_OF_WEEK); // Get the day of the week

                            // Check if the request is from May of the current year and falls on a Sunday
                            if (recordYear == currentYear && recordMonth == mayMonth && recordDayOfWeek == Calendar.SUNDAY) {
                                // Count 0.5 for clock-in time if available
                                if (clockInTime != null && !clockInTime.isEmpty()) {
                                    totalOTCount[0] += 0.5f;
                                }

                                // Count 0.5 for clock-out time if available
                                if (clockOutTime != null && !clockOutTime.isEmpty()) {
                                    totalOTCount[0] += 0.5f;
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Display the total OT count for Sundays in May
                TextView otCountTextView = findViewById(R.id.coff5); // Adjust to your TextView ID for OT count in May
                otCountTextView.setText(String.valueOf(totalOTCount[0]));

                String monthYearKey = "May-" + currentYear; // Example: "May-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthCOffRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(totalOTCount[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch user time records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countOTForJune() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference usersRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set June date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int juneMonth = 6; // June (1-based index)

        // Track the total OT count for Sundays
        final float[] totalOTCount = {0};

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Fetch the date, clock-in, and clock-out time
                    String date = snapshot.child("date").getValue(String.class);
                    String clockInTime = snapshot.child("clockInTime").getValue(String.class);
                    String clockOutTime = snapshot.child("clockOutTime").getValue(String.class);

                    if (date != null) {
                        try {
                            // Parse the date in the format "dd-MM-yyyy"
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            Date recordDate = dateFormat.parse(date.trim());

                            // Create a calendar instance for the record date
                            Calendar recordCalendar = Calendar.getInstance();
                            recordCalendar.setTime(recordDate);

                            int recordYear = recordCalendar.get(Calendar.YEAR);
                            int recordMonth = recordCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based
                            int recordDayOfWeek = recordCalendar.get(Calendar.DAY_OF_WEEK); // Get the day of the week

                            // Check if the request is from June of the current year and falls on a Sunday
                            if (recordYear == currentYear && recordMonth == juneMonth && recordDayOfWeek == Calendar.SUNDAY) {
                                // Count 0.5 for clock-in time if available
                                if (clockInTime != null && !clockInTime.isEmpty()) {
                                    totalOTCount[0] += 0.5f;  // You can add further conditions for clock-in time if required
                                }

                                // Count 0.5 for clock-out time if available
                                if (clockOutTime != null && !clockOutTime.isEmpty()) {
                                    totalOTCount[0] += 0.5f;  // Similarly, you can add further conditions for clock-out time if required
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Display the total OT count for Sundays in June
                TextView otCountTextView = findViewById(R.id.coff6); // Adjust to your TextView ID for OT count in June
                otCountTextView.setText(String.valueOf(totalOTCount[0]));

                String monthYearKey = "June-" + currentYear; // Example: "June-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthCOffRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(totalOTCount[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch user time records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countOTForJuly() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference usersRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set July date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int julyMonth = 7; // July (1-based index)

        // Track the total OT count for Sundays
        final float[] totalOTCount = {0};

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Fetch the date, clock-in, and clock-out time
                    String date = snapshot.child("date").getValue(String.class);
                    String clockInTime = snapshot.child("clockInTime").getValue(String.class);
                    String clockOutTime = snapshot.child("clockOutTime").getValue(String.class);

                    if (date != null) {
                        try {
                            // Parse the date in the format "dd-MM-yyyy"
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            Date recordDate = dateFormat.parse(date.trim());

                            // Create a calendar instance for the record date
                            Calendar recordCalendar = Calendar.getInstance();
                            recordCalendar.setTime(recordDate);

                            int recordYear = recordCalendar.get(Calendar.YEAR);
                            int recordMonth = recordCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based
                            int recordDayOfWeek = recordCalendar.get(Calendar.DAY_OF_WEEK); // Get the day of the week

                            // Check if the request is from July of the current year and falls on a Sunday
                            if (recordYear == currentYear && recordMonth == julyMonth && recordDayOfWeek == Calendar.SUNDAY) {
                                // Count 0.5 for clock-in time if available and is on or before 9:15 AM
                                if (clockInTime != null && !clockInTime.isEmpty()) {
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    Date clockIn = timeFormat.parse(clockInTime);

                                    Date limitClockIn = timeFormat.parse("09:15");

                                    if (clockIn != null && clockIn.before(limitClockIn) || clockIn.equals(limitClockIn)) {
                                        totalOTCount[0] += 0.5f;
                                    }
                                }

                                // Count 0.5 for clock-out time if available and is on or after 17:45
                                if (clockOutTime != null && !clockOutTime.isEmpty()) {
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    Date clockOut = timeFormat.parse(clockOutTime);

                                    Date limitClockOut = timeFormat.parse("17:45");

                                    if (clockOut != null && clockOut.after(limitClockOut) || clockOut.equals(limitClockOut)) {
                                        totalOTCount[0] += 0.5f;
                                    }
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Display the total OT count for Sundays in July
                TextView otCountTextView = findViewById(R.id.coff7); // Adjust to your TextView ID for OT count in July
                otCountTextView.setText(String.valueOf(totalOTCount[0]));

                String monthYearKey = "July-" + currentYear; // Example: "July-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthCOffRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(totalOTCount[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch user time records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countOTForAugust() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference usersRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set August date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int augustMonth = 8; // August (1-based index)

        // Track the total OT count for Sundays
        final float[] totalOTCount = {0};

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Fetch the date, clock-in, and clock-out time
                    String date = snapshot.child("date").getValue(String.class);
                    String clockInTime = snapshot.child("clockInTime").getValue(String.class);
                    String clockOutTime = snapshot.child("clockOutTime").getValue(String.class);

                    if (date != null) {
                        try {
                            // Parse the date in the format "dd-MM-yyyy"
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            Date recordDate = dateFormat.parse(date.trim());

                            // Create a calendar instance for the record date
                            Calendar recordCalendar = Calendar.getInstance();
                            recordCalendar.setTime(recordDate);

                            int recordYear = recordCalendar.get(Calendar.YEAR);
                            int recordMonth = recordCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based
                            int recordDayOfWeek = recordCalendar.get(Calendar.DAY_OF_WEEK); // Get the day of the week

                            // Check if the request is from August of the current year and falls on a Sunday
                            if (recordYear == currentYear && recordMonth == augustMonth && recordDayOfWeek == Calendar.SUNDAY) {
                                // Count OT for clock-in and clock-out time
                                if (clockInTime != null && !clockInTime.isEmpty()) {
                                    totalOTCount[0] += 0.5f; // 0.5 OT for clock-in on Sunday
                                }

                                if (clockOutTime != null && !clockOutTime.isEmpty()) {
                                    totalOTCount[0] += 0.5f; // 0.5 OT for clock-out on Sunday
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Display the total OT count for Sundays in August
                TextView otCountTextView = findViewById(R.id.coff8); // Adjust to your TextView ID for OT count in August
                otCountTextView.setText(String.valueOf(totalOTCount[0]));

                String monthYearKey = "August-" + currentYear; // Example: "August-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthCOffRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(totalOTCount[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch user time records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countOTForSeptember() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference usersRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set September date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int septemberMonth = 9; // September (1-based index)

        // Track the total OT count for Sundays
        final float[] totalOTCount = {0};

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Fetch the date, clock-in, and clock-out time
                    String date = snapshot.child("date").getValue(String.class);
                    String clockInTime = snapshot.child("clockInTime").getValue(String.class);
                    String clockOutTime = snapshot.child("clockOutTime").getValue(String.class);

                    if (date != null) {
                        try {
                            // Parse the date in the format "dd-MM-yyyy"
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            Date recordDate = dateFormat.parse(date.trim());

                            // Create a calendar instance for the record date
                            Calendar recordCalendar = Calendar.getInstance();
                            recordCalendar.setTime(recordDate);

                            int recordYear = recordCalendar.get(Calendar.YEAR);
                            int recordMonth = recordCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based
                            int recordDayOfWeek = recordCalendar.get(Calendar.DAY_OF_WEEK); // Get the day of the week

                            // Check if the request is from September of the current year and falls on a Sunday
                            if (recordYear == currentYear && recordMonth == septemberMonth && recordDayOfWeek == Calendar.SUNDAY) {
                                // Count 0.5 for clock-in time if available
                                if (clockInTime != null && !clockInTime.isEmpty()) {
                                    totalOTCount[0] += 0.5f;
                                }

                                // Count 0.5 for clock-out time if available
                                if (clockOutTime != null && !clockOutTime.isEmpty()) {
                                    totalOTCount[0] += 0.5f;
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                            // Handle parsing error more gracefully
                        }
                    }
                }

                // Display the total OT count for Sundays in September
                TextView otCountTextView = findViewById(R.id.coff9); // Adjust to your TextView ID for OT count in September
                otCountTextView.setText(String.valueOf(totalOTCount[0]));

                String monthYearKey = "September-" + currentYear; // Example: "September-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthCOffRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(totalOTCount[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch user time records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countOTForSundaysInOctober() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference timeRecordsRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Get the current year (October is month 10)
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int octoberMonth = 10; // October (1-based index)

        // Initialize the OT count
        final float[] totalOTCount = {0}; // Use an array to modify the value inside the listener

        // Loop through all days in October (1 to 31)
        for (int day = 1; day <= 31; day++) {
            // Create the date key in "dd-MM-yyyy" format
            String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%d", day, octoberMonth, currentYear);

            // Fetch the data for the time records for this date
            int finalDay = day;
            timeRecordsRef.child(dateKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Check if data exists for the current day
                    if (dataSnapshot.exists()) {
                        String clockInTime = dataSnapshot.child("ClockInTime").getValue(String.class);
                        String clockOutTime = dataSnapshot.child("ClockOutTime").getValue(String.class);

                        // Create a Calendar instance for the current date to check if it's a Sunday
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.set(currentYear, octoberMonth - 1, finalDay); // Set the date for comparison
                        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK); // Get the day of the week

                        // If it's a Sunday (Sunday is 1 in Calendar class)
                        if (dayOfWeek == Calendar.SUNDAY) {
                            // If ClockInTime exists, count 0.5
                            if (clockInTime != null && !clockInTime.isEmpty()) {
                                totalOTCount[0] += 0.5; // Add 0.5 for ClockInTime on Sunday
                            }

                            // If ClockOutTime exists, count 0.5
                            if (clockOutTime != null && !clockOutTime.isEmpty()) {
                                totalOTCount[0] += 0.5; // Add 0.5 for ClockOutTime on Sunday
                            }
                        }
                    }

                    // After all days are processed, display the total OT count for Sundays
                    TextView otCountTextView = findViewById(R.id.coff10); // Update the TextView ID as per your layout
                    otCountTextView.setText(String.valueOf(totalOTCount[0])); // Display the OT count in coff10 TextView

                    String monthYearKey = "October-" + currentYear; // Example: "October-2024"
                    DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthCOffRecords").child(monthYearKey);

                    // Update the value in Firebase
                    monthPermissionRecordsRef.setValue(totalOTCount[0]).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                        } else {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch user time records", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void countOTForNovember() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference usersRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set November date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int novemberMonth = 11; // November (1-based index)

        // Track the total OT count for Sundays
        final float[] totalOTCount = {0};

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Fetch the date, clock-in, and clock-out time
                    String date = snapshot.child("date").getValue(String.class);
                    String clockInTime = snapshot.child("clockInTime").getValue(String.class);
                    String clockOutTime = snapshot.child("clockOutTime").getValue(String.class);

                    if (date != null) {
                        try {
                            // Parse the date in the format "dd-MM-yyyy"
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            Date recordDate = dateFormat.parse(date.trim());

                            // Create a calendar instance for the record date
                            Calendar recordCalendar = Calendar.getInstance();
                            recordCalendar.setTime(recordDate);

                            int recordYear = recordCalendar.get(Calendar.YEAR);
                            int recordMonth = recordCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based
                            int recordDayOfWeek = recordCalendar.get(Calendar.DAY_OF_WEEK); // Get the day of the week

                            // Check if the record is from November of the current year and falls on a Sunday
                            if (recordYear == currentYear && recordMonth == novemberMonth && recordDayOfWeek == Calendar.SUNDAY) {
                                // Count 0.5 for clock-in time if available
                                if (clockInTime != null && !clockInTime.isEmpty()) {
                                    totalOTCount[0] += 0.5f;
                                }

                                // Count 0.5 for clock-out time if available
                                if (clockOutTime != null && !clockOutTime.isEmpty()) {
                                    totalOTCount[0] += 0.5f;
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Display the total OT count for Sundays in November
                TextView otCountTextView = findViewById(R.id.coff11); // Adjust to your TextView ID for OT count in November
                otCountTextView.setText(String.valueOf(totalOTCount[0]));

                String monthYearKey = "November-" + currentYear; // Example: "November-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthCOffRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(totalOTCount[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch user time records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countOTForDecember() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference usersRef = mDatabase.child("Users").child(userId).child("timeRecords");

        // Set December date range
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); // Current year
        int decemberMonth = 12; // December (1-based index)

        // Track the total OT count for Sundays
        final float[] totalOTCount = {0};

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Fetch the date, clock-in, and clock-out time
                    String date = snapshot.child("date").getValue(String.class);
                    String clockInTime = snapshot.child("clockInTime").getValue(String.class);
                    String clockOutTime = snapshot.child("clockOutTime").getValue(String.class);

                    if (date != null) {
                        try {
                            // Parse the date in the format "dd-MM-yyyy"
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            Date recordDate = dateFormat.parse(date.trim());

                            // Create a calendar instance for the record date
                            Calendar recordCalendar = Calendar.getInstance();
                            recordCalendar.setTime(recordDate);

                            int recordYear = recordCalendar.get(Calendar.YEAR);
                            int recordMonth = recordCalendar.get(Calendar.MONTH) + 1; // Calendar month is 0-based
                            int recordDayOfWeek = recordCalendar.get(Calendar.DAY_OF_WEEK); // Get the day of the week

                            // Check if the request is from December of the current year and falls on a Sunday
                            if (recordYear == currentYear && recordMonth == decemberMonth && recordDayOfWeek == Calendar.SUNDAY) {
                                // Count 0.5 for clock-in time if available and is on or before 9:15 AM
                                if (clockInTime != null && !clockInTime.isEmpty()) {
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    Date clockIn = timeFormat.parse(clockInTime);

                                    Date limitClockIn = timeFormat.parse("09:15");

                                    if (clockIn != null && clockIn.before(limitClockIn) || clockIn.equals(limitClockIn)) {
                                        totalOTCount[0] += 0.5f;
                                    }
                                }

                                // Count 0.5 for clock-out time if available and is on or after 17:45
                                if (clockOutTime != null && !clockOutTime.isEmpty()) {
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    Date clockOut = timeFormat.parse(clockOutTime);

                                    Date limitClockOut = timeFormat.parse("17:45");

                                    if (clockOut != null && clockOut.after(limitClockOut) || clockOut.equals(limitClockOut)) {
                                        totalOTCount[0] += 0.5f;
                                    }
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Display the total OT count for Sundays in December
                TextView otCountTextView = findViewById(R.id.coff12); // Adjust to your TextView ID for OT count in December
                otCountTextView.setText(String.valueOf(totalOTCount[0]));

                String monthYearKey = "December-" + currentYear; // Example: "December-2024"
                DatabaseReference monthPermissionRecordsRef = mDatabase.child("Users").child(userId).child("monthCOffRecords").child(monthYearKey);

                // Update the value in Firebase
                monthPermissionRecordsRef.setValue(totalOTCount[0]).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MonthlyCalendarActivity.this, "Failed to fetch user time records", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // OT calculation //


    private boolean isOnOrBefore(String time, String targetTime) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date timeDate = timeFormat.parse(time);
            Date targetDate = timeFormat.parse(targetTime);
            return timeDate != null && targetDate != null && timeDate.compareTo(targetDate) <= 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isOnOrAfter(String time, String targetTime) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date timeDate = timeFormat.parse(time);
            Date targetDate = timeFormat.parse(targetTime);
            return timeDate != null && targetDate != null && timeDate.compareTo(targetDate) >= 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }


}
