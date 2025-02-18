package com.attendance.clockme;

import androidx.appcompat.app.AppCompatActivity;

public class OTPVerificationActivity extends AppCompatActivity {
//    private EditText otpInput;
//    private Button verifyButton;
//    private String generatedOtp; // This should be the OTP sent to the user
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_otp_verification);
//
//        // Initialize UI elements
//        otpInput = findViewById(R.id.otp_input);
//        verifyButton = findViewById(R.id.verify_button);
//
//        // Retrieve the generated OTP (passed from the registration activity)
//        generatedOtp = getIntent().getStringExtra("otp");
//
//        // Set up button click listener
//        verifyButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                handleOtpVerification();
//            }
//        });
//    }
//
//    private void handleOtpVerification() {
//        String enteredOtp = otpInput.getText().toString().trim();
//
//        if (enteredOtp.isEmpty()) {
//            Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (enteredOtp.equals(generatedOtp)) {
//            Toast.makeText(this, "OTP Verified Successfully", Toast.LENGTH_SHORT).show();
//            // Proceed with the next step (e.g., navigate to the main app screen)
//            Intent intent = new Intent(OTPVerificationActivity.this, MainActivity.class); // Change to your main activity
//            startActivity(intent);
//            finish();
//        } else {
//            Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
//        }
//    }
}
