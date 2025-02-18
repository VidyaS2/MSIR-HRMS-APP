package com.attendance.clockme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private EditText signinEmailInput, signinPasswordInput;
    private Button signInButton;
    private TextView signUpText, forgotPasswordText;
    private boolean isPasswordVisible = false;

    private FirebaseAuth mAuth; // FirebaseAuth instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        signinEmailInput = findViewById(R.id.signin_email_input);
        signinPasswordInput = findViewById(R.id.signin_password_input);
        signInButton = findViewById(R.id.textView8);
        signUpText = findViewById(R.id.signUpText);
        forgotPasswordText = findViewById(R.id.forgot_password);

        // Load saved credentials
        loadSavedCredentials();

        // Check if the admin is already logged in
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isAdminLoggedIn = prefs.getBoolean("isAdminLoggedIn", false);

        if (isAdminLoggedIn) {
            // If admin is logged in, redirect directly to Admin Dashboard
            Intent intent = new Intent(SignInActivity.this, ActivityAdminDashboard.class);
            startActivity(intent);
            finish(); // Close the current activity
            return;
        }

        // Set up Sign In button click listener
        signInButton.setOnClickListener(v -> handleSignIn());

        // Set up Sign Up text click listener
        signUpText.setOnClickListener(v -> {
            // Redirect to Sign Up activity
            Intent intent = new Intent(SignInActivity.this, SignInActivity.class);
            startActivity(intent);
        });

        // Set up Forgot Password text click listener
        forgotPasswordText.setOnClickListener(v -> {
            // Redirect to Forgot Password activity
            Intent intent = new Intent(SignInActivity.this, activity_forgot_password.class);
            startActivity(intent);
        });

        // Set up Password Visibility Toggle
        signinPasswordInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (signinPasswordInput.getRight() - signinPasswordInput.getCompoundDrawables()[2].getBounds().width())) {
                    togglePasswordVisibility();
                    return true;
                }
            }
            return false;
        });
    }

    private void handleSignIn() {
        String email = signinEmailInput.getText().toString().trim();
        String password = signinPasswordInput.getText().toString().trim();

        // Basic validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and Password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Log the email and password for debugging purposes
        Log.d("SignInActivity", "Email entered: " + email + " Password entered: " + password);

        // Check if email and password match admin credentials
        if (email.equals("admin123@gmail.com") && password.equals("admin@123")) {
            // Log that the admin credentials matched
            Log.d("SignInActivity", "Admin credentials matched. Redirecting to Admin Dashboard.");

            // Save admin login state in SharedPreferences
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isAdminLoggedIn", true); // Set admin as logged in
            editor.putBoolean("isLoggedIn", true); // Set logged in state
            editor.putString("loggedInEmail", email); // Save admin email
            editor.apply();

            // Redirect to Admin Dashboard activity
            Toast.makeText(SignInActivity.this, "Admin Sign In Successful!", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(SignInActivity.this, ActivityAdminDashboard.class);
                startActivity(intent);
                finish();  // Finish this activity
            }, 1000);  // Delay by 1 second
        } else {
            // Log that regular user login is initiated
            Log.d("SignInActivity", "Admin credentials did not match. Proceeding with regular user login.");

            // Proceed with regular Firebase Authentication login
            loginUserWithFirebase(email, password);
        }
    }

    // Method to handle Firebase Authentication for regular users
    private void loginUserWithFirebase(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d("SignInActivity", "User signed in: " + user.getEmail());
                        Toast.makeText(SignInActivity.this, "Sign In Successful!", Toast.LENGTH_SHORT).show();

                        // Save user login state in SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("isLoggedIn", true); // Set logged in state
                        editor.putString("loggedInEmail", email); // Save user email
                        editor.apply();

                        // Redirect to regular Dashboard activity
                        try {
                            Intent intent = new Intent(SignInActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            Log.e("SignInActivity", "Error starting DashboardActivity", e);
                            Toast.makeText(SignInActivity.this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // If sign in fails, display a message to the user
                        Log.w("SignInActivity", "signInWithEmail:failure", task.getException());
                        Toast.makeText(SignInActivity.this, "Authentication failed. Please check your email and password.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveCredentials(String email, String password) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("savedEmail", email);
        editor.putString("savedPassword", password);
        editor.apply();
    }

    private void loadSavedCredentials() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String savedEmail = prefs.getString("savedEmail", "");
        String savedPassword = prefs.getString("savedPassword", "");
        signinEmailInput.setText(savedEmail);
        signinPasswordInput.setText(savedPassword);
    }

    private void redirectToAdminDashboard() {
        // Save admin login state in SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isAdminLoggedIn", true); // Set admin as logged in
        editor.apply();

        Toast.makeText(SignInActivity.this, "Admin Sign In Successful!", Toast.LENGTH_SHORT).show();

        // Use postDelayed to delay finish() slightly
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SignInActivity.this, ActivityAdminDashboard.class);
            startActivity(intent);
            finish();  // Finish this activity
        }, 1000);  // Delay by 1 second
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            signinPasswordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
            signinPasswordInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.password, 0); // Hidden icon
        } else {
            signinPasswordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            signinPasswordInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.pass_strike, 0); // Visible icon
        }
        isPasswordVisible = !isPasswordVisible;
        signinPasswordInput.setSelection(signinPasswordInput.getText().length()); // Move cursor to end
    }
}
