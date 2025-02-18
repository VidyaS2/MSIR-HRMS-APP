package com.attendance.clockme;

import android.annotation.SuppressLint;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CoffFormActivity extends AppCompatActivity {

    private TextView tvDate, tvClockInTime, tvClockInLocation;
    private EditText etRemark;
    private Button btnSubmit;
    private Spinner spinnerTeam;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coff_form);

        // Initialize views
        tvDate = findViewById(R.id.tv_date);
        tvClockInTime = findViewById(R.id.tv_clock_in_time);
        tvClockInLocation = findViewById(R.id.tv_clock_in_location);
        etRemark = findViewById(R.id.et_remark);
        btnSubmit = findViewById(R.id.btn_submit);
        spinnerTeam = findViewById(R.id.spinner_team);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Set current date and time
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String currentClockInTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        tvDate.setText(currentDate);
        tvClockInTime.setText(currentClockInTime);

        // Initialize location provider
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchCurrentLocation();

        // Set up spinner
        String[] teams = new String[]{"Select Team", "Team 1", "Team 2", "Team 3"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, teams);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTeam.setAdapter(adapter);

        // Handle submit button
        btnSubmit.setOnClickListener(v -> {
            String remark = etRemark.getText().toString().trim();
            if (remark.isEmpty()) {
                Toast.makeText(CoffFormActivity.this, "Please enter a remark", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = currentUser.getUid();
            String email = currentUser.getEmail();
            String selectedTeam = spinnerTeam.getSelectedItem().toString();
            String clockInLocation = tvClockInLocation.getText().toString();
            String clockInKey = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

            if (selectedTeam.equals("Select Team")) {
                Toast.makeText(CoffFormActivity.this, "Please select a valid team", Toast.LENGTH_SHORT).show();
                return;
            }

            databaseReference.child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String username = snapshot.child("username").getValue(String.class);
                        String position = snapshot.child("position").getValue(String.class);

                        HashMap<String, Object> permissionData = new HashMap<>();
                        permissionData.put("date", currentDate);
                        permissionData.put("clockInTime", currentClockInTime);
                        permissionData.put("remark", remark);
                        permissionData.put("username", username);
                        permissionData.put("position", position);
                        permissionData.put("email", email);
                        permissionData.put("status", "pending");
                        permissionData.put("uid", uid);
                        permissionData.put("clockInLocation", clockInLocation);
                        permissionData.put("team", selectedTeam);

                        // Ensure this points to CoffHdPermission
                        databaseReference.child("CoffHdPermission").child(uid).child(clockInKey)
                                .setValue(permissionData)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(CoffFormActivity.this, "Permission request submitted", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(CoffFormActivity.this, "Submission failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(CoffFormActivity.this, "Error fetching user details", Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    private void fetchCurrentLocation() {
        if (androidx.core.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED
                && androidx.core.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                Geocoder geocoder = new Geocoder(CoffFormActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        tvClockInLocation.setText(addresses.get(0).getAddressLine(0));
                    } else {
                        tvClockInLocation.setText("Unable to fetch location");
                    }
                } catch (IOException e) {
                    tvClockInLocation.setText("Error fetching address");
                }
            } else {
                tvClockInLocation.setText("Location not available");
            }
        });
    }
}
