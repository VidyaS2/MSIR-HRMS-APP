package com.attendance.clockme;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminHolidayDeclaration extends AppCompatActivity {

    private EditText enterDateField, reasonField;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_holiday_declaration);

        enterDateField = findViewById(R.id.enterdateField);
        reasonField = findViewById(R.id.reasonField);
        Button requestButton = findViewById(R.id.requestButton);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                declareHoliday();
            }
        });

        LinearLayout homeLayout = findViewById(R.id.homeLayout);
        LinearLayout createLayout = findViewById(R.id.createLayout);
        LinearLayout signoutLayout = findViewById(R.id.signoutLayout);
        LinearLayout requestLayout = findViewById(R.id.requestLayout);

        // Home icon click - Navigate to ActivityAdminDashboard
        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHolidayDeclaration.this, ActivityAdminDashboard.class);
                startActivity(intent);
            }
        });

        requestLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHolidayDeclaration.this, AdminDailyAttendanceActivity.class);
                startActivity(intent);
            }
        });

        // Create icon click - Navigate to EmpRegActivity
        createLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHolidayDeclaration.this, EmpRegActivity.class);
                startActivity(intent);
            }
        });

        // SignOut icon click - Logout and navigate to SignInActivity
        signoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(AdminHolidayDeclaration.this, "Signed out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AdminHolidayDeclaration.this, SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void declareHoliday() {
        String inputDate = enterDateField.getText().toString().trim();
        String reason = reasonField.getText().toString().trim();

        if (inputDate.isEmpty()) {
            Toast.makeText(this, "Please enter a date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reason.isEmpty()) {
            Toast.makeText(this, "Please enter a reason", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert the entered date to dd-MM-yyyy format
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()); // Adjust input format if needed
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate;
        try {
            Date parsedDate = inputFormat.parse(inputDate); // Parse the input date
            formattedDate = outputFormat.format(parsedDate); // Reformat the date
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format. Please use dd-MM-yyyy.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Define the holiday structure with "holiday" text for clock-in and clock-out, and the reason
        Holiday holiday = new Holiday("holiday", "holiday", reason);

        // Fetch all users from the database and update their timeRecords
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Iterate through all users (UIDs) in the Users node
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String userId = userSnapshot.getKey(); // User UID
                        if (userId != null) {
                            // Insert the holiday for each user under their timeRecords node
                            databaseReference.child(userId).child("timeRecords").child(formattedDate).setValue(holiday);
                        }
                    }

                    // Clear the form after all users have been updated
                    enterDateField.setText("");
                    reasonField.setText("");

                    // Confirm that the holiday has been declared for all users
                    Toast.makeText(AdminHolidayDeclaration.this, "Holiday declared for all users", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminHolidayDeclaration.this, "No users found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminHolidayDeclaration.this, "Failed to fetch users: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class Holiday {
        public String ClockInTime;
        public String ClockOutTime;
        public String Reason;

        public Holiday() {
            // Default constructor required for Firebase
        }

        public Holiday(String ClockInTime, String ClockOutTime, String Reason) {
            this.ClockInTime = ClockInTime;
            this.ClockOutTime = ClockOutTime;
            this.Reason = Reason;
        }
    }
}
