package com.attendance.clockme;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmpRegActivity extends AppCompatActivity {
    private EditText usernameInput, emailInput, passwordInput, salaryInput, positionInput, empIdInput;
    private LinearLayout homeLayout, createLayout, signoutLayout, requestLayout;
    private Spinner teamSpinner;
    private Button registerButton;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String selectedTeam;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emp_reg_form);

        // Initialize views
        usernameInput = findViewById(R.id.username_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        salaryInput = findViewById(R.id.salary);
        positionInput = findViewById(R.id.position);
        empIdInput = findViewById(R.id.empid_input);
        teamSpinner = findViewById(R.id.spinner_team);
        registerButton = findViewById(R.id.textView8);

        // Initialize Firebase references
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // Populate Spinner with team options
        populateTeamSpinner();

        // Handle team selection from Spinner
        teamSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTeam = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTeam = null;
            }
        });

        // Set click listener for the register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        homeLayout = findViewById(R.id.homeLayout);
        createLayout = findViewById(R.id.createLayout);
        signoutLayout = findViewById(R.id.signoutLayout);
        requestLayout = findViewById(R.id.requestLayout);

        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmpRegActivity.this, ActivityAdminDashboard.class);
                startActivity(intent);
            }
        });

        // Create icon click - Navigate to EmpRegActivity
        createLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmpRegActivity.this, EmpRegActivity.class);
                startActivity(intent);
            }
        });

        // Request icon click - Navigate to AdminDailyAttendanceActivity
        requestLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmpRegActivity.this, AdminDailyAttendanceActivity.class);
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

    private void populateTeamSpinner() {
        // List of teams (you can fetch this from Firebase or an API if needed)
        List<String> teamList = new ArrayList<>(Arrays.asList("Select Team", "Team 1", "Team 2", "Team 3"));

        // Create an ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                teamList
        );

        // Specify layout for dropdown items
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set adapter to Spinner
        teamSpinner.setAdapter(adapter);
    }

    private void registerUser() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String department = salaryInput.getText().toString().trim();
        String position = positionInput.getText().toString().trim();
        String empId = empIdInput.getText().toString().trim();

        // Validate input fields
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || department.isEmpty() || position.isEmpty() ||empId.isEmpty() || selectedTeam == null || selectedTeam.equals("Select Team")) {
            Toast.makeText(this, "All fields are required, including team selection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Register with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            saveUserToDatabase(username, email, department, position, empId, selectedTeam);
                        } else {
                            Toast.makeText(EmpRegActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserToDatabase(String username, String email, String department, String position, String empId, String team) {
        String userId = mAuth.getCurrentUser().getUid(); // Get unique user ID
        User newUser = new User(username, email, department, position, empId, team); // Create User object

        // Save under "Users" node
        databaseReference.child("Users").child(userId).setValue(newUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Save under the specific team node
                        databaseReference.child(team).child(userId).setValue(newUser)
                                .addOnCompleteListener(teamTask -> {
                                    if (teamTask.isSuccessful()) {
                                        sendPasswordResetEmail(email); // Send password reset email
                                    } else {
                                        Toast.makeText(EmpRegActivity.this, "Failed to save to " + team + ": " + teamTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(EmpRegActivity.this, "Failed to save user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EmpRegActivity.this, "Registration successful. A password reset email has been sent.", Toast.LENGTH_LONG).show();
                        clearFields();
                    } else {
                        Toast.makeText(EmpRegActivity.this, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearFields() {
        usernameInput.setText("");
        emailInput.setText("");
        passwordInput.setText("");
        salaryInput.setText("");
        positionInput.setText("");
        teamSpinner.setSelection(0);
    }

    // User model class
    public static class User {
        public String username;
        public String empId;
        public String email;
        public String department;
        public String position;
        public String team;

        public User() {} // Default constructor for Firebase

        public User(String username, String email, String department, String position, String empId, String team) {
            this.username = username;
            this.email = email;
            this.department = department;
            this.position = position;
            this.empId = empId;
            this.team = team;
        }


    }
}
