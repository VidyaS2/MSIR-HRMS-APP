package com.attendance.clockme;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {

        private EditText usernameInput, emailInput, passwordInput;
        private Button signUpButton;
        private TextView signUpText;

        private FirebaseAuth mAuth;
        private DatabaseReference mDatabase;
        private boolean isPasswordVisible = false;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_registration);

                // Initialize Firebase Auth
                mAuth = FirebaseAuth.getInstance();
                mDatabase = FirebaseDatabase.getInstance().getReference("Users");

                // Initialize UI elements
                usernameInput = findViewById(R.id.username_input);
                emailInput = findViewById(R.id.email_input);
                passwordInput = findViewById(R.id.password_input);
                signUpButton = findViewById(R.id.textView8);
                signUpText = findViewById(R.id.sign_up_text);

                // Set up Sign Up button click listener
                signUpButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                handleSignUp();
                        }
                });

                // Set up Sign In text click listener
                signUpText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                Intent intent = new Intent(RegistrationActivity.this, SignInActivity.class);
                                startActivity(intent);
                        }
                });

                // Set up Password Visibility Toggle
                passwordInput.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                                if (event.getAction() == MotionEvent.ACTION_UP) {
                                        if (event.getRawX() >= (passwordInput.getRight() - passwordInput.getCompoundDrawables()[2].getBounds().width())) {
                                                togglePasswordVisibility();
                                                return true;
                                        }
                                }
                                return false;
                        }
                });
        }

        private void handleSignUp() {
                String username = usernameInput.getText().toString().trim();
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                // Basic validation
                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                        return;
                }

                // Sign up with Firebase Authentication
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                if (user != null) {
                                                        saveUserToDatabase(username, email);

                                                        Toast.makeText(RegistrationActivity.this, "Successfully Registered", Toast.LENGTH_LONG).show();

                                                        Intent intent = new Intent(RegistrationActivity.this, SignInActivity.class);
                                                        startActivity(intent);
                                                        finish(); // Close the current activity
                                                }
                                        } else {
                                                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Sign Up Failed";
                                                Toast.makeText(RegistrationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                        }
                                }
                        });
        }

        private void saveUserToDatabase(String username, String email) {
                String userId = mAuth.getCurrentUser().getUid();
                User user = new User(username, email);

                mDatabase.child(userId).setValue(user)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                                Toast.makeText(RegistrationActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                        } else {
                                                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Failed to save user data";
                                                Toast.makeText(RegistrationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                        }
                                }
                        });
        }

        private void togglePasswordVisibility() {
                if (isPasswordVisible) {
                        // Hide the password
                        passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        passwordInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.pass_strike, 0); // Hidden icon
                } else {
                        // Show the password
                        passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        passwordInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.password, 0); // Visible icon
                }
                isPasswordVisible = !isPasswordVisible;
                passwordInput.setSelection(passwordInput.getText().length()); // Move cursor to end
        }

        public static class User {
                public String username;
                public String email;

                public User() {}

                public User(String username, String email) {
                        this.username = username;
                        this.email = email;
                }
        }
}
