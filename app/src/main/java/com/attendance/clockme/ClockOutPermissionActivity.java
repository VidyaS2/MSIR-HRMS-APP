package com.attendance.clockme;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ClockOutPermissionActivity extends AppCompatActivity {

    private TextView tvDate, tvClockOutTime, tvClockOutLocation;
    private EditText etRemark;
    private Button btnSubmit;
    private Spinner spinnerTeam;  // Spinner for team selection
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clock_out_permission); // Replace with your layout name

        // Initialize views
        tvDate = findViewById(R.id.tv_date);
        tvClockOutTime = findViewById(R.id.tv_clock_in_time);
        tvClockOutLocation = findViewById(R.id.tv_clock_in_location); // Location TextView
        etRemark = findViewById(R.id.et_remark);
        btnSubmit = findViewById(R.id.btn_submit);
        spinnerTeam = findViewById(R.id.spinner_team); // Initialize spinner for team selection

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Get the current date
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        tvDate.setText(currentDate);

        // Get the current clock-out time
        String currentClockOutTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        tvClockOutTime.setText(currentClockOutTime);

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Fetch and display the current location
        fetchCurrentLocation();

        // Set up the spinner with team options
        String[] teams = new String[] {"Select Team", "Team 1", "Team 2", "Team 3"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, teams);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTeam.setAdapter(adapter);

        // Submit button logic
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String remark = etRemark.getText().toString().trim();
                if (remark.isEmpty()) {
                    Toast.makeText(ClockOutPermissionActivity.this, "Please enter a remark", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Fetch user details
                String uid = currentUser.getUid();
                String email = currentUser.getEmail();

                // Fetch the selected team from the spinner
                String selectedTeam = spinnerTeam.getSelectedItem().toString();

                // Fetch location from the UI element
                String clockOutLocation = tvClockOutLocation.getText().toString();

                // Generate a unique key based on the current date and time (or timestamp)
                String clockOutKey = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

                // Get user data from Firebase (username, position)
                databaseReference.child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String username = snapshot.child("username").getValue(String.class);
                            String position = snapshot.child("position").getValue(String.class);

                            // Prepare data for ClockOutPermission node
                            HashMap<String, Object> permissionData = new HashMap<>();
                            permissionData.put("date", currentDate);
                            permissionData.put("clockOutTime", currentClockOutTime);
                            permissionData.put("remark", remark);
                            permissionData.put("username", username);
                            permissionData.put("position", position);
                            permissionData.put("email", email);
                            permissionData.put("status", "pending");
                            permissionData.put("uid", uid);
                            permissionData.put("clockOutLocation", clockOutLocation); // Add location to data
                            permissionData.put("team", selectedTeam); // Add selected team to data

                            // Save data to Firebase with the unique clockOutKey
                            databaseReference.child("ClockOutPermission").child(uid).child(clockOutKey)
                                    .setValue(permissionData)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ClockOutPermissionActivity.this, "Permission request submitted", Toast.LENGTH_SHORT).show();
                                            finish(); // Close activity
                                        } else {
                                            Toast.makeText(ClockOutPermissionActivity.this, "Submission failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ClockOutPermissionActivity.this, "Error fetching user details", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // Method to fetch the current location
    private void fetchCurrentLocation() {
        if (androidx.core.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED
                && androidx.core.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<android.location.Location>() {
            @Override
            public void onSuccess(android.location.Location location) {
                if (location != null) {
                    // Geocoder to get the address
                    Geocoder geocoder = new Geocoder(ClockOutPermissionActivity.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            String address = addresses.get(0).getAddressLine(0);
                            tvClockOutLocation.setText(address); // Set the address in the TextView
                        } else {
                            tvClockOutLocation.setText("Unable to fetch location");
                        }
                    } catch (IOException e) {
                        tvClockOutLocation.setText("Error fetching address");
                    }
                } else {
                    tvClockOutLocation.setText("Location not available");
                }
            }
        });
    }
}
