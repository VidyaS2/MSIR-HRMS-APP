package com.attendance.clockme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SalaryDetailsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private TextView welcomeTextView;
    private TextView sundayCountTextView; // OT TextView
    private TextView lateCountTextView; // Late TextView
    private TextView leaveCountTextView; // Leave TextView
    private Button leaveStatusButton, permissionStatusButton, relieveStatusButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private String lateThresholdTime = "09:00"; // Example threshold for being late
    private CardView permissionCard, leaveCard, relieveCard;
    private ImageView salaryPreviousIcon;
    private ImageView salaryNextIcon;
    private TextView monthYearTextView;
    private int currentMonth;
    private int currentYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary_details);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        LinearLayout homeIcon = findViewById(R.id.homelayout2);
        LinearLayout clockIcon = findViewById(R.id.clocklayout2);
        LinearLayout logoutIcon = findViewById(R.id.signoutlayout2);
        LinearLayout requestIcon = findViewById(R.id.requestlayout2);
        welcomeTextView = findViewById(R.id.welcomeMessage); // Only initialize once

        monthYearTextView = findViewById(R.id.textView2); // Assuming this is the TextView for month-year display
        salaryPreviousIcon = findViewById(R.id.salaryPreviousIcon);
        salaryNextIcon = findViewById(R.id.salaryNextIcon);

        // Get current month and year
        Calendar calendar = Calendar.getInstance();
        currentMonth = calendar.get(Calendar.MONTH); // 0-based
        currentYear = calendar.get(Calendar.YEAR);

        // Set the default view as current month and year
        updateMonthYearTextView();
        fetchLeaveCount(currentMonth, currentYear); // Fetch leave count for the previous month
        fetchCOffCount(currentMonth, currentYear); // Fetch C Off count for the previous month
        fetchPresentCount(currentMonth, currentYear);
        fetchQuarterLeave();

        salaryPreviousIcon.setOnClickListener(v -> {
            // Move to the previous month
            calendar.add(Calendar.MONTH, -1);
            currentMonth = calendar.get(Calendar.MONTH);
            currentYear = calendar.get(Calendar.YEAR);
            updateMonthYearTextView(); // Update the displayed month and year
            fetchLeaveCount(currentMonth, currentYear); // Fetch leave count for the previous month
            fetchCOffCount(currentMonth, currentYear); // Fetch C Off count for the previous month
            fetchPresentCount(currentMonth, currentYear);
            fetchQuarterLeave();
        });

        salaryNextIcon.setOnClickListener(v -> {
            // Move to the next month
            calendar.add(Calendar.MONTH, 1);
            currentMonth = calendar.get(Calendar.MONTH);
            currentYear = calendar.get(Calendar.YEAR);
            updateMonthYearTextView(); // Update the displayed month and year
            fetchLeaveCount(currentMonth, currentYear); // Fetch leave count for the next month
            fetchCOffCount(currentMonth, currentYear); // Fetch C Off count for the next month
            fetchPresentCount(currentMonth, currentYear);
            fetchQuarterLeave();
        });


//        findViewById(R.id.permissionLayout).setOnClickListener(v -> showPermissionPopup());
//        findViewById(R.id.leaveLayout).setOnClickListener(v -> showLeaveRequestPopup());
//        findViewById(R.id.relieveLayout).setOnClickListener(v -> showRelieveRequestPopup());

        // Icon click listeners...
        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(SalaryDetailsActivity.this, DashboardActivity.class);
            startActivity(intent);
        });

        requestIcon.setOnClickListener(v -> {
            Intent salaryDetailsIntent = new Intent(SalaryDetailsActivity.this, SalaryDetailsActivity.class);
            startActivity(salaryDetailsIntent);
        });

        logoutIcon.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(SalaryDetailsActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            Toast.makeText(SalaryDetailsActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        clockIcon.setOnClickListener(v -> {
            Intent clockInOutIntent = new Intent(SalaryDetailsActivity.this, SwipingTimeActivity.class);
            startActivity(clockInOutIntent);
        });

        // Fetch user data
        fetchAndDisplayUsername();


        Button btnClockInRequest = findViewById(R.id.btnClockInRequest);
        Button btnClockOutRequest = findViewById(R.id.btnClockOutRequest);
        Button btnSkipMissing = findViewById(R.id.btnSkipMissing);
        Button btnLeaveRequest = findViewById(R.id.btnLeaveRequest);

        // Set the OnClickListener
        btnClockInRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ClientPermissionStatusActivity
                Intent intent = new Intent(SalaryDetailsActivity.this, ClientPermissionStatusActivity.class);
                startActivity(intent);
            }
        });
        btnClockOutRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ClientPermissionStatusActivity
                Intent intent = new Intent(SalaryDetailsActivity.this, ClientLeaveStatusActivity.class);
                startActivity(intent);
            }
        });
        btnSkipMissing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ClientPermissionStatusActivity
                Intent intent = new Intent(SalaryDetailsActivity.this, ClientRelieveStatusActivity.class);
                startActivity(intent);
            }
        });

        btnLeaveRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ClientPermissionStatusActivity
                Intent intent = new Intent(SalaryDetailsActivity.this, ClientRelieveStatusActivity.class);
                startActivity(intent);
            }
        });
//        // Initialize CardViews
//        permissionCard = findViewById(R.id.permissioncard);
//        leaveCard = findViewById(R.id.leavecard);
//        relieveCard = findViewById(R.id.relievecard);
//
//        // Set click listener for Permission Card
//      //  permissionCard.setOnClickListener(v -> showPopup(R.layout.popup_request));
//
//        // Set click listener for Leave Card
//        leaveCard.setOnClickListener(v -> {
////            Intent intent = new Intent(SalaryDetailsActivity.this, PopupLeaveActivity.class);
//            Intent intent = new Intent(SalaryDetailsActivity.this, SalaryDetailsActivity.class);
//            startActivity(intent);
//        });
//
//        // Set click listener for Relieve Card
//       // relieveCard.setOnClickListener(v -> showPopup(R.layout.popup_relieve_request));
//
//        leaveStatusButton = findViewById(R.id.leaveStatusButton);
//        permissionStatusButton = findViewById(R.id.permissionStatusButton);
//        relieveStatusButton = findViewById(R.id.relieveStatusButton);
//
//        leaveStatusButton.setOnClickListener(v -> {
//            // Start ClientLeaveStatusActivity
//           // Intent intent = new Intent(SalaryDetailsActivity.this, ClientLeaveStatusActivity.class);
//            Intent intent = new Intent(SalaryDetailsActivity.this, SalaryDetailsActivity.class);
//            startActivity(intent);
//        });
//
//        permissionStatusButton.setOnClickListener(v -> {
//            // Start ClientLeaveStatusActivity
//          //  Intent intent = new Intent(SalaryDetailsActivity.this, ClientPermissionStatusActivity.class);
//            Intent intent = new Intent(SalaryDetailsActivity.this, ClientClockInPerm.class);
//            startActivity(intent);
//        });
//
//        relieveStatusButton.setOnClickListener(v -> {
//            // Start ClientLeaveStatusActivity
//          //  Intent intent = new Intent(SalaryDetailsActivity.this, ClientRelieveStatusActivity.class);
//            Intent intent = new Intent(SalaryDetailsActivity.this, SalaryDetailsActivity.class);
//            startActivity(intent);
//        });
    }

    private void fetchLeaveCount(int month, int year) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null) {
                // Format the month-year string (e.g., "January-2024")
                String monthYear = new SimpleDateFormat("MMMM-yyyy", Locale.getDefault()).format(getCalendarForMonth(year, month).getTime());
                mDatabase.child("Users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                // Fetch the leave count for the month
                                Double leaveCount = userSnapshot.child("monthLeaveRecords").child(monthYear).getValue(Double.class);
                                if (leaveCount != null) {
                                    // Update the leave TextView with the fetched leave count
                                    TextView leaveTextView = findViewById(R.id.leave);
                                    leaveTextView.setText("Leave: " + leaveCount);
                                } else {
                                    // If no leave data found for the month, set it to 0
                                    TextView leaveTextView = findViewById(R.id.leave);
                                    leaveTextView.setText("Leave: 0");
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(SalaryDetailsActivity.this, "Failed to fetch leave data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private Calendar getCalendarForMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        return calendar;
    }

    private void fetchCOffCount(int month, int year) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null) {
                // Format the month-year string (e.g., "January-2024")
                String monthYear = new SimpleDateFormat("MMMM-yyyy", Locale.getDefault()).format(getCalendarForMonth(year, month).getTime());

                // Query Firebase to get the C Off count for the specific month and user
                mDatabase.child("Users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Iterate through users to find the one matching the email
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                // Fetch the C Off count for the given month
                                Double cOffCount = userSnapshot.child("monthCOffRecords").child(monthYear).getValue(Double.class);

                                // Find the TextView for C Off
                                TextView coffTextView = findViewById(R.id.coff);

                                if (cOffCount != null) {
                                    // If data is found, display the value
                                    coffTextView.setText("C Off: " + cOffCount);
                                } else {
                                    // If no data is found, display 0
                                    coffTextView.setText("C Off: 0");
                                }
                            }
                        } else {
                            // Handle case where user data is not found in the snapshot
                            TextView coffTextView = findViewById(R.id.coff);
                            coffTextView.setText("C Off: 0");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                        Toast.makeText(SalaryDetailsActivity.this, "Failed to fetch C Off data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void fetchPresentCount(int month, int year) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null) {
                // Format the month-year string (e.g., "January-2024")
                String monthYear = new SimpleDateFormat("MMMM-yyyy", Locale.getDefault()).format(getCalendarForMonth(year, month).getTime());

                mDatabase.child("Users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                // Fetch data from the four nodes
                                Double presentCount = userSnapshot.child("monthPresentRecords").child(monthYear).getValue(Double.class);
                                Double permissionCount = userSnapshot.child("monthPermissionRecords").child(monthYear).getValue(Double.class);
                                Double smCount = userSnapshot.child("monthSMRecords").child(monthYear).getValue(Double.class);
                                Double holidayCount = userSnapshot.child("monthHolidayRecords").child(monthYear).getValue(Double.class);

                                // Handle null values (default to 0 if no data is found)
                                presentCount = (presentCount != null) ? presentCount : 0;
                                permissionCount = (permissionCount != null) ? permissionCount : 0;
                                smCount = (smCount != null) ? smCount : 0;
                                holidayCount = (holidayCount != null) ? holidayCount : 0;

                                // Calculate the total present count (adjust logic as needed)
                                double totalPresentCount = presentCount + permissionCount + smCount + holidayCount;

                                // Update the Present TextView
                                TextView presentTextView = findViewById(R.id.present);
                                presentTextView.setText("Present: " + totalPresentCount);

                                // Update the Salary TextView
                                TextView salaryTextView = findViewById(R.id.salary);
                                salaryTextView.setText("Salary: " + totalPresentCount); // Adjust salary logic as required
                            }
                        } else {
                            // Handle case where no data is found for the user
                            TextView presentTextView = findViewById(R.id.present);
                            presentTextView.setText("Present: 0");

                            TextView salaryTextView = findViewById(R.id.salary);
                            salaryTextView.setText("Salary: 0");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle Firebase errors
                        Toast.makeText(SalaryDetailsActivity.this, "Failed to fetch Present data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void fetchQuarterLeave() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null) {
                // Query Firebase to get the quarterLeave value for the specific user
                mDatabase.child("Users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Iterate through users to find the one matching the email
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                // Fetch the quarterLeave value for the user
                                String quarterLeave = userSnapshot.child("quarterLeave").getValue(String.class);

                                // Find the TextView for displaying quarterLeave
                                TextView quarterLeaveTextView = findViewById(R.id.quaterleave);

                                if (quarterLeave != null) {
                                    // If data is found, display the value
                                    quarterLeaveTextView.setText("Quarter Leave: " + quarterLeave);
                                } else {
                                    // If no data is found, display 0
                                    quarterLeaveTextView.setText("Quarter Leave: 0");
                                }
                            }
                        } else {
                            // Handle case where user data is not found in the snapshot
                            TextView quarterLeaveTextView = findViewById(R.id.ql);
                            quarterLeaveTextView.setText("Quarter Leave: 0");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                        Toast.makeText(SalaryDetailsActivity.this, "Failed to fetch Quarter Leave", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void updateMonthYearTextView() {
        // Format and update the TextView with the current month and year
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.set(currentYear, currentMonth, 1);
        String monthYear = monthYearFormat.format(calendar.getTime());
        monthYearTextView.setText(monthYear);
    }

    private void fetchAndDisplayUsername() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null) {
                mDatabase.child("Users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String username = userSnapshot.child("username").getValue(String.class);
                                if (username != null) {
                                    username = username.substring(0, 1).toUpperCase() + username.substring(1).toLowerCase();
                                    welcomeTextView.setText("Welcome, " + username + "!");
                                } else {
                                    Toast.makeText(SalaryDetailsActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(SalaryDetailsActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(SalaryDetailsActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(SalaryDetailsActivity.this, "User email not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(SalaryDetailsActivity.this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPermissionPopup() {
      // Intent intent = new Intent(SalaryDetailsActivity.this, PopupPermissionActivity.class);
        Intent intent = new Intent(SalaryDetailsActivity.this, ClientClockInPerm.class);
        startActivity(intent);
    }

    private void showLeaveRequestPopup() {
       // Intent intent = new Intent(SalaryDetailsActivity.this, PopupLeaveActivity.class);
        Intent intent = new Intent(SalaryDetailsActivity.this, SalaryDetailsActivity.class);
        startActivity(intent);
    }

    private void showRelieveRequestPopup() {
        //Intent intent = new Intent(SalaryDetailsActivity.this, PopupRelieveActivity.class);
        Intent intent = new Intent(SalaryDetailsActivity.this, SalaryDetailsActivity.class);
        startActivity(intent);
    }

//    private void showPopup(int layoutResId) {
//        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//        View popupView = inflater.inflate(layoutResId, null);
//
//        // Set up the PopupWindow
//        PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        popupWindow.setFocusable(true);
//
//        // Show popup in the center
//        popupWindow.showAtLocation(findViewById(R.id.colorGrid), Gravity.CENTER, 0, 0);
//
//        // Close icon click listener
//        ImageView closeIcon = popupView.findViewById(R.id.closeIcon);
//        closeIcon.setOnClickListener(v -> popupWindow.dismiss());
//    }

}
