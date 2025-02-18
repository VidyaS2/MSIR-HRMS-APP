package com.attendance.clockme;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView; // Import TextView
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ClockInOutEditActivity extends AppCompatActivity {

    private EditText etClockInTime, etClockOutTime, etClockDateInput, etUsernameInput;
    private TextView textViewUsername; // Add this line for the TextView
    private Button saveButton;
    private ImageView closeIcon;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clock_inout_edit);

        etClockInTime = findViewById(R.id.clockInField);
        etClockOutTime = findViewById(R.id.clockOutField);
        etClockDateInput = findViewById(R.id.clock_date_input);
        etUsernameInput = findViewById(R.id.clock_username_input);
        saveButton = findViewById(R.id.button_update);
        closeIcon = findViewById(R.id.close_icon);
        // Assuming this is a dialog creation code
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.clock_inout_edit); // Your layout

        ImageView closeIcon = dialog.findViewById(R.id.close_icon);
        closeIcon.setOnClickListener(v -> {
            dialog.dismiss(); // Close the dialog
        });

// Show the dialog
        dialog.show();
        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Get the intent extras
        Intent intent = getIntent();
        String clockInTime = intent.getStringExtra("clockInTime");
        String clockOutTime = intent.getStringExtra("clockOutTime");
        String date = intent.getStringExtra("date");
        String username = intent.getStringExtra("username");  // Retrieve the username

        // Set the form fields with the received data
        etClockInTime.setText(clockInTime);
        etClockOutTime.setText(clockOutTime);
        etClockDateInput.setText(date);
        textViewUsername.setText(username); // Display username in the TextView

        // Handle Save button click
        saveButton.setOnClickListener(v -> {
            // Capture updated values
            String updatedClockInTime = etClockInTime.getText().toString();
            String updatedClockOutTime = etClockOutTime.getText().toString();
            String updatedDate = etClockDateInput.getText().toString();
            String updatedUsername = etUsernameInput.getText().toString();

            // TODO: Implement Firebase update logic here with updated values

            // Finish the activity after updating
            finish();
        });

        closeIcon.setOnClickListener(v -> {
            // Close the app
            finishAffinity();
        });
    }
}
