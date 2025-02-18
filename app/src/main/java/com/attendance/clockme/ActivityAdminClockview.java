package com.attendance.clockme;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import androidx.appcompat.app.AlertDialog;

public class ActivityAdminClockview extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private TextView usernameTextView, positionTextView, emailTextView, salaryTextView;
    private TextView tvDateRange; // Add TextView for month and year
    private LinearLayout cardContainer; // Declare the card container

    // Initialize counters for OT, late, and leave
    private int otCount = 0;
    private int lateCount = 0;
    private double leaveCount = 0.0;
    private TextView noRecordsTextView;
    private Calendar calendar; // Add calendar instance to manage current date
    private FirebaseAuth mAuth;

    private LinearLayout homeLayout, createLayout, signoutLayout, requestLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_clockview); // Your layout for this activity

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        // Get the passed UID from intent
        String uid = getIntent().getStringExtra("UID");
        mAuth = FirebaseAuth.getInstance();
        //mDatabase = FirebaseDatabase.getInstance().getReference();
        // Initialize TextViews
        usernameTextView = findViewById(R.id.username_text_view);
        positionTextView = findViewById(R.id.position_text_view);
        emailTextView = findViewById(R.id.email_text_view);
        salaryTextView = findViewById(R.id.salary_text_view);
        tvDateRange = findViewById(R.id.tv_date_range); // Initialize the month/year TextView
        cardContainer = findViewById(R.id.cardContainer); // Initialize your card container

        // Initialize Calendar to current date
        calendar = Calendar.getInstance();
        updateDateRange(); // Update the date range display
        AtomicInteger currentMonth = new AtomicInteger(calendar.get(Calendar.MONTH)); // Current month
        AtomicInteger currentYear = new AtomicInteger(calendar.get(Calendar.YEAR));

        // Update date range and fetch data
        updateDateRange();
        fetchUserData(uid);
        fetchPresentCount(uid, currentMonth.get(), currentYear.get());
        fetchLeaveCount(uid, currentMonth.get(), currentYear.get());
        fetchCOffCount(uid, currentMonth.get(), currentYear.get());
        fetchQuarterLeave(uid);
        // Get current month and year
//        AtomicInteger currentMonth = new AtomicInteger(calendar.get(Calendar.MONTH));  // Current month
//        AtomicInteger currentYear = new AtomicInteger(calendar.get(Calendar.YEAR));    // Current year

        // Set up previous and next button functionality
        ImageView previousAdminView = findViewById(R.id.previousadminView);
        ImageView nextAdminView = findViewById(R.id.nextadminView);

        previousAdminView.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1); // Move to previous month
            currentMonth.set(calendar.get(Calendar.MONTH)); // Get updated month
            currentYear.set(calendar.get(Calendar.YEAR));   // Get updated year
            updateDateRange(); // Update display
            fetchUserData(uid); // Refresh user data for the new month
            fetchPresentCount(uid, currentMonth.get(), currentYear.get()); // Fetch present count for the previous month
            fetchLeaveCount(uid, currentMonth.get(), currentYear.get());
            fetchCOffCount(uid, currentMonth.get(), currentYear.get());
            fetchQuarterLeave(uid);
        });

        nextAdminView.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1); // Move to next month
            currentMonth.set(calendar.get(Calendar.MONTH)); // Get updated month
            currentYear.set(calendar.get(Calendar.YEAR));   // Get updated year
            updateDateRange(); // Update display
            fetchUserData(uid); // Refresh user data for the new month
            fetchPresentCount(uid, currentMonth.get(), currentYear.get()); // Fetch present count for the next month
            fetchLeaveCount(uid, currentMonth.get(), currentYear.get());
            fetchCOffCount(uid, currentMonth.get(), currentYear.get());
            fetchQuarterLeave(uid);
        });
        Button quarterLeaveButton = findViewById(R.id.qlbtn);

        // Set a click listener on the Quarter Leave button
        quarterLeaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the quarter leave dialog when the button is clicked
                showQuarterLeaveDialog(uid);
            }
        });
        // Fetch and display user details
        fetchAndDisplayUserDetails(uid);
        noRecordsTextView = findViewById(R.id.noRecordsTextView);

        // Find the remove button in your layout
        Button removeButton = findViewById(R.id.remove_button);

        // Set the button click listener
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRemoveConfirmationDialog(); // Call method to show dialog
            }
        });

        Button monthButton = findViewById(R.id.monthcalendar);

        // Set click listener
        monthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to SuperAdminCalendar and send UID
                Intent intent = new Intent(ActivityAdminClockview.this, SuperAdminCalendar.class);
                intent.putExtra("UID", uid); // Pass the UID
                startActivity(intent);
            }
        });

        homeLayout = findViewById(R.id.homeLayout);
        createLayout = findViewById(R.id.createLayout);
        signoutLayout = findViewById(R.id.signoutLayout);
        requestLayout = findViewById(R.id.requestLayout); // Corrected ID

        // Home icon click - Navigate to ActivityAdminDashboard
        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityAdminClockview.this, ActivityAdminDashboard.class);
                startActivity(intent);
            }
        });

        // Create icon click - Navigate to EmpRegActivity
        createLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityAdminClockview.this, EmpRegActivity.class);
                startActivity(intent);
            }
        });

        // Request icon click - Navigate to AdminDailyAttendanceActivity
        requestLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityAdminClockview.this, AdminDailyAttendanceActivity.class);
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

    private void showRemoveConfirmationDialog() {
        // Create a dialog
        final Dialog dialog = new Dialog(this);
        // Inflate your custom layout
        dialog.setContentView(R.layout.remove_confirmation);

        // Find the close, yes, and no buttons from the layout
        ImageButton closeButton = dialog.findViewById(R.id.close_button);
        Button buttonYes = dialog.findViewById(R.id.button_yes);
        Button buttonNo = dialog.findViewById(R.id.button_no);

        // Close button listener to dismiss the dialog
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Close the dialog
            }
        });

        // No button listener to dismiss the dialog
        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Close the dialog
            }
        });

        // Yes button listener to perform the remove action
        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove the user with the specified UID
                String uid = getIntent().getStringExtra("UID"); // Fetch the UID again
                removeUser(uid);
                dialog.dismiss(); // Close the dialog
            }
        });

        // Show the dialog
        dialog.show();
    }

    private void removeUser(String uid) {
        mDatabase.child(uid).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Successfully removed the user
                    Log.d("RemoveUser", "User with UID: " + uid + " has been removed.");
                    // You can also show a Toast or update the UI accordingly
                    Toast.makeText(ActivityAdminClockview.this, "User removed successfully.", Toast.LENGTH_SHORT).show();
                    finish(); // Optionally finish the activity or update the UI to reflect the changes
                })
                .addOnFailureListener(e -> {
                    // Failed to remove the user
                    Log.e("RemoveUser", "Failed to remove user: " + e.getMessage());
                    Toast.makeText(ActivityAdminClockview.this, "Failed to remove user.", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateDateRange() {
        // Format the month and year and update the TextView
        String monthYear = String.format(Locale.getDefault(), "%tB %tY", calendar, calendar);
        tvDateRange.setText(monthYear);
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input; // Return input as is if it's null or empty
        }

        StringBuilder capitalized = new StringBuilder();
        String[] words = input.split(" "); // Split the input into words

        for (String word : words) {
            if (word.length() > 0) {
                capitalized.append(Character.toUpperCase(word.charAt(0))) // Capitalize the first letter
                        .append(word.substring(1).toLowerCase()) // Append the rest in lowercase
                        .append(" "); // Add space after each word
            }
        }

        return capitalized.toString().trim(); // Return the final string
    }

    private void fetchAndDisplayUserDetails(String uid) {
        mDatabase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.child("username").getValue(String.class);
                    String position = dataSnapshot.child("position").getValue(String.class);
                    String empId = dataSnapshot.child("empId").getValue(String.class);
                    String department = dataSnapshot.child("department").getValue(String.class);

                    // Capitalize the first letter of each word for username and position
                    username = capitalizeFirstLetter(username);
                    position = capitalizeFirstLetter(position);

                    // Set the text in TextViews
                    usernameTextView.setText(username);
                    positionTextView.setText(position);
                    emailTextView.setText(empId);
                    salaryTextView.setText(department); // Add Indian Rupee symbol before the salary amount
                } else {
                    Log.w("FetchUserDetails", "User not found for UID: " + uid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FetchUserDetails", "Error fetching data: " + databaseError.getMessage());
            }
        });

        // Fetch user attendance data
        fetchUserData(uid); // Call the function to fetch user data
    }

    private void fetchUserData(String userId) {
        String selectedMonthYear = new SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(calendar.getTime());

        // Fetch attendance data for the specific month and year
        mDatabase.child(userId).child("timeRecords")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Clear previous data
                        cardContainer.removeAllViews();

                        boolean hasRecords = false; // Flag to check if records exist

                        // Loop through each child in the snapshot
                        for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                            String date = dateSnapshot.getKey();

                            // Check if the date is in the selected month
                            if (isDateInMonth(date, selectedMonthYear)) {
                                hasRecords = true; // At least one record found

                                // Fetch clock-in and clock-out times
                                String clockInTime = dateSnapshot.child("ClockInTime").getValue(String.class);
                                String clockOutTime = dateSnapshot.child("ClockOutTime").getValue(String.class);

                                // Skip Sundays for leave counting
                                if (isSunday(date)) {
                                    if (clockInTime != null) {
                                        otCount++; // Count OT if clocked in on Sunday
                                    }
                                    continue; // Skip leave calculation for Sundays
                                }

                                // Skip holidays (both clockInTime and clockOutTime are null)
                                if ((clockInTime == null && clockOutTime == null)) {
                                    continue; // Skip holidays
                                }

                                String lateHours = "On Time"; // Initialize lateHours variable

                                // Calculate late hours if clockInTime is after 9:05 AM
                                lateHours = calculateLateHours(clockInTime);
                                if (lateHours.startsWith("Late")) {
                                    lateCount++; // Increment late count
                                }

                                // Check for leave if clockInTime is null or partial clock-out (before 8 PM)
                                if (clockInTime == null && clockOutTime == null) {
                                    leaveCount += 1.0; // Full day of leave
                                } else if (clockInTime != null && clockOutTime == null) {
                                    leaveCount += 0.5; // Half-day leave if no clock-out time
                                } else if (clockOutTime != null && isBefore8PM(clockOutTime)) {
                                    leaveCount += 0.5; // Half-day leave if clock-out time is before 8 PM
                                }

                                // Create a new CardView for each entry
                                addCardView(date, clockInTime, clockOutTime, lateHours);
                            }
                        }

                        // Check if records were found and update UI accordingly
                        if (hasRecords) {
                            cardContainer.setVisibility(View.VISIBLE); // Show card container
                            noRecordsTextView.setVisibility(View.GONE); // Hide no records message
                        } else {
                            cardContainer.setVisibility(View.GONE); // Hide card container
                            noRecordsTextView.setVisibility(View.VISIBLE); // Show no records message
                            noRecordsTextView.setText("No Records Found"); // Set the message

                            // Ensure attendance counts are displayed as zero
                            otCount = 0;
                            lateCount = 0;
                            leaveCount = 0.0; // Ensure leaveCount is reset
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FetchUserData", "Error fetching user data: " + error.getMessage());
                    }
                });
    }

    private boolean isDateInMonth(String dateStr, String monthYear) {
        // Define the date format expected
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            Date date = sdf.parse(dateStr); // Parse the date
            SimpleDateFormat monthYearFormat = new SimpleDateFormat("MM-yyyy", Locale.getDefault());
            String recordMonthYear = monthYearFormat.format(date);
            return recordMonthYear.equals(monthYear); // Check if the month-year matches
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false; // Return false if there's an error
    }

    private boolean isSunday(String dateStr) {
        // Define the date format expected (change according to your date string format)
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            Date date = sdf.parse(dateStr); // Parse the date
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY; // Check if it's Sunday
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false; // Return false if there's an error
    }

    private String calculateLateHours(String clockInTime) {
        // Parse the clockInTime and check if it's late
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date clockInDate = sdf.parse(clockInTime);
            Date lateThreshold = sdf.parse("09:15");

            if (clockInDate != null && lateThreshold != null) {
                if (clockInDate.after(lateThreshold)) {
                    long lateMillis = clockInDate.getTime() - lateThreshold.getTime();

                    // Calculate hours, minutes, and seconds
                    long hours = TimeUnit.MILLISECONDS.toHours(lateMillis);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(lateMillis) % 60;

                    // Return formatted late time as Late: HH:mm
                    return String.format(Locale.getDefault(), "Late: %02d:%02d", hours, minutes);
                } else {
                    return "On Time"; // No late hours
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ""; // Default to on time in case of error

    }

    private void addCardView(String date, String clockInTime, String clockOutTime, String lateHours) {

        // Inflate the card layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.admin_clockview, cardContainer, false);

        // Set data to TextViews
        TextView dateTextView = cardView.findViewById(R.id.dateTextView);
        TextView dayTextView = cardView.findViewById(R.id.dayOfWeekTextView);
        TextView inTextView = cardView.findViewById(R.id.overlayBox);
        TextView outTextView = cardView.findViewById(R.id.overlayBoxOut);
        TextView lateTextView = cardView.findViewById(R.id.lateTextView);

        // Display the date and clock-in/out times
        dateTextView.setText(date);
        inTextView.setText(clockInTime != null ? clockInTime : "00:00");
        outTextView.setText(clockOutTime != null ? clockOutTime : "00:00");
        lateTextView.setText(lateHours != null ? lateHours : "No Data");

        // Calculate and set the day name based on the date
        if (date != null && !date.isEmpty()) {
            try {
                // Parse the date string
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                Date parsedDate = dateFormat.parse(date);

                if (parsedDate != null) {
                    // Format to get the day name
                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
                    String dayName = dayFormat.format(parsedDate);
                    dayTextView.setText(dayName); // Display the day name
                } else {
                    dayTextView.setText("Invalid Date");
                }
            } catch (ParseException e) {
                e.printStackTrace();
                dayTextView.setText("Invalid Date");
            }
        } else {
            dayTextView.setText("No Date Provided");
        }

        // Add the card to the container
        cardContainer.addView(cardView);
    }

    private boolean isBefore8PM(String clockOutTime) {
        // Define the time format expected for clock-out time
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date clockOutDate = sdf.parse(clockOutTime);
            Date eightPM = sdf.parse("17:45");

            if (clockOutDate != null && eightPM != null) {
                return clockOutDate.before(eightPM); // Check if the clock-out time is before 8 PM
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false; // Return false if parsing fails
    }

    private void fetchPresentCount(String uid, int month, int year) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        TextView presentTextView = findViewById(R.id.present);

        // Format the month-year key (e.g., "January-2024")
        String monthYear = new SimpleDateFormat("MMMM-yyyy", Locale.getDefault())
                .format(getCalendarForMonth(year, month).getTime());

        // Fetch data from the nodes
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot userSnapshot) {
                if (userSnapshot.exists()) {
                    // Fetch present count from the monthPresentRecords node
                    Double presentCount = userSnapshot.child("monthPresentRecords").child(monthYear).getValue(Double.class);
                    // Fetch permission count from the monthPermissionRecords node
                    Double permissionCount = userSnapshot.child("monthPermissionRecords").child(monthYear).getValue(Double.class);
                    // Fetch sm count from the monthSMRecords node
                    Double smCount = userSnapshot.child("monthSMRecords").child(monthYear).getValue(Double.class);
                    // Fetch holiday count from the monthHolidayRecords node
                    Double holidayCount = userSnapshot.child("monthHolidayRecords").child(monthYear).getValue(Double.class);

                    // Handle null values (default to 0 if no data is found)
                    presentCount = (presentCount != null) ? presentCount : 0;
                    permissionCount = (permissionCount != null) ? permissionCount : 0;
                    smCount = (smCount != null) ? smCount : 0;
                    holidayCount = (holidayCount != null) ? holidayCount : 0;

                    // Calculate the total present count (sum of all four categories)
                    double totalPresentCount = presentCount + permissionCount + smCount + holidayCount;

                    // Update the Present TextView
                    presentTextView.setText("Present: " + totalPresentCount);

                    // Update the Salary TextView (assuming salary depends on the total present count)
                    TextView salaryTextView = findViewById(R.id.salary);
                    salaryTextView.setText("Salary: " + totalPresentCount); // Assuming salary is tied to present count
                } else {
                    // Handle case where no data is found for the user
                    presentTextView.setText("Present: 0");

                    TextView salaryTextView = findViewById(R.id.salary);
                    salaryTextView.setText("Salary: 0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors here, e.g., logging or showing a message to the user
                Log.e("FetchPresentCount", "Error fetching data: " + databaseError.getMessage());
                Toast.makeText(ActivityAdminClockview.this, "Failed to fetch present count", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Calendar getCalendarForMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);  // Sets the calendar to the 1st day of the given month and year
        return calendar;
    }

    private void fetchLeaveCount(String uid, int month, int year) {
        // Reference to the user's data in Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        TextView leaveTextView = findViewById(R.id.leave);

        // Format the month-year key (e.g., "January-2024")
        String monthYear = new SimpleDateFormat("MMMM-yyyy", Locale.getDefault())
                .format(getCalendarForMonth(year, month).getTime());

        // Fetch data from Firebase
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot userSnapshot) {
                if (userSnapshot.exists()) {
                    // Fetch leave count from the monthLeaveRecords node
                    Double leaveCount = userSnapshot.child("monthLeaveRecords").child(monthYear).getValue(Double.class);

                    // Handle null values (default to 0 if no data is found)
                    leaveCount = (leaveCount != null) ? leaveCount : 0;

                    // Update the Leave TextView with the fetched leave count
                    leaveTextView.setText("Leave: " + leaveCount);
                } else {
                    // Handle case where no data is found for the user
                    leaveTextView.setText("Leave: 0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors here, e.g., logging or showing a message to the user
                Log.e("FetchLeaveCount", "Error fetching data: " + databaseError.getMessage());
                Toast.makeText(ActivityAdminClockview.this, "Failed to fetch leave count", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCOffCount(String uid, int month, int year) {
        // Reference to the user's data in Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        TextView cOffTextView = findViewById(R.id.coff);

        // Format the month-year key (e.g., "January-2024")
        String monthYear = new SimpleDateFormat("MMMM-yyyy", Locale.getDefault())
                .format(getCalendarForMonth(year, month).getTime());

        // Fetch data from Firebase
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot userSnapshot) {
                if (userSnapshot.exists()) {
                    // Fetch C Off count from the monthCOffRecords node
                    Double cOffCount = userSnapshot.child("monthCOffRecords").child(monthYear).getValue(Double.class);

                    // Handle null values (default to 0 if no data is found)
                    cOffCount = (cOffCount != null) ? cOffCount : 0;

                    // Update the C Off TextView with the fetched C Off count
                    cOffTextView.setText("C Off: " + cOffCount);
                } else {
                    // Handle case where no data is found for the user
                    cOffTextView.setText("C Off: 0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors here, e.g., logging or showing a message to the user
                Log.e("FetchCOffCount", "Error fetching data: " + databaseError.getMessage());
                Toast.makeText(ActivityAdminClockview.this, "Failed to fetch C Off count", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showQuarterLeaveDialog(String uid) {
        // Create a dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the custom layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quarter_leave, null);
        builder.setView(dialogView);

        // Get the reference to the EditText field in the custom layout
        EditText quarterLeaveInput = dialogView.findViewById(R.id.editTextQuarterLeave);

        // Set the dialog properties
        builder.setCancelable(true)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String quarterLeave = quarterLeaveInput.getText().toString().trim();

                    if (!quarterLeave.isEmpty()) {
                        // Reference to the specific user's node in Firebase using the dynamic uid
                        DatabaseReference userRef = mDatabase.child("Users").child(uid);

                        // Insert the quarter leave data into the Firebase database
                        userRef.child("quarterLeave").setValue(quarterLeave)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ActivityAdminClockview.this, "Quarter Leave Updated", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ActivityAdminClockview.this, "Failed to update Quarter Leave", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(ActivityAdminClockview.this, "Please enter a value", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Create and show the dialog
        builder.create().show();
    }

    private void fetchQuarterLeave(String uid) {
        // Reference to the user's data in Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        TextView quarterLeaveTextView = findViewById(R.id.ql); // Assuming you have a TextView to display this value.

        // Fetch quarterLeave value from Firebase
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot userSnapshot) {
                if (userSnapshot.exists()) {
                    // Fetch the quarterLeave value (assuming it is stored as a string or number)
                    String quarterLeave = userSnapshot.child("quarterLeave").getValue(String.class);

                    // Handle null values (if there's no data for quarterLeave)
                    if (quarterLeave != null) {
                        // Update the UI with the fetched quarterLeave value
                        quarterLeaveTextView.setText("Quarter Leave: " + quarterLeave);
                    } else {
                        quarterLeaveTextView.setText("Quarter Leave: 0"); // Default to 0 if no data
                    }
                } else {
                    // Handle case where user data doesn't exist
                    quarterLeaveTextView.setText("Quarter Leave: 0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error fetching data
                Log.e("FetchQuarterLeave", "Error fetching quarterLeave: " + databaseError.getMessage());
                Toast.makeText(ActivityAdminClockview.this, "Failed to fetch Quarter Leave", Toast.LENGTH_SHORT).show();
            }
        });
    }


}

