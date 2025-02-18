package com.attendance.clockme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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

public class ClientClockInPerm extends AppCompatActivity {

    private TableLayout dynamicRowContainer;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_late_history); // Ensure this matches your layout file

        sharedPreferences = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        dynamicRowContainer = findViewById(R.id.dynamicRowContainer);

        // Add table header before fetching data
        addTableHeader();

        // Fetch data from Firebase
        fetchLateRequests();

        LinearLayout homeIcon = findViewById(R.id.homelayout);
        LinearLayout clockIcon = findViewById(R.id.clocklayout);
        LinearLayout logoutIcon = findViewById(R.id.signoutlayout);
        LinearLayout requestIcon = findViewById(R.id.requestlayout);

        homeIcon.setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));
        logoutIcon.setOnClickListener(v -> logout());
        clockIcon.setOnClickListener(v -> startActivity(new Intent(this, SwipingTimeActivity.class)));
        requestIcon.setOnClickListener(v -> startActivity(new Intent(this, SalaryDetailsActivity.class)));
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, SignInActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
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
    }
}
