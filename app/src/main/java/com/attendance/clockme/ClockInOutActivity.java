package com.attendance.clockme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.core.app.ActivityCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.single.PermissionListener;

public class ClockInOutActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ClockInOutPrefs";
    private static final String CLOCK_IN_TIME = "ClockInTime";
    private static final String CLOCK_OUT_TIME = "ClockOutTime";
    private static final String CLOCK_IN_TIMESTAMP = "ClockInTimestamp";
    private static final String CLOCK_OUT_TIMESTAMP = "ClockOutTimestamp";
    private static final long TWENTY_HOURS_IN_MILLIS = 72000000;

    private TextView currentTimer;
    private TextView currentTimerRight;
    private TextView welcomeTextView;
    private SharedPreferences sharedPreferences;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private WebView mapWebView;
    private ImageView staticMapView;

    @SuppressLint("WrongViewCast")
    FusedLocationProviderClient fusedLocationProviderClient;
    SupportMapFragment supportMapFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock_in_out);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        // Initialize WebView
//        mapWebView = findViewById(R.id.google_map);
//
//        // Enable JavaScript
//        WebSettings webSettings = mapWebView.getSettings();
//        webSettings.setJavaScriptEnabled(true);

        // Set WebViewClient to ensure links open in WebView
//        mapWebView.setWebViewClient(new WebViewClient());
//
//        // Load the Google Maps embed URL (replace with your desired location)
//        String embedUrl = "https://www.google.com/maps/embed/v1/view?key=AIzaSyB575KMfMx6Wz_hFroOLBCi9lwnaFokLA4&center=12.950464387362965,80.20477587484098&zoom=15"; // Update with your Google Maps Embed API key
//        mapWebView.loadUrl(embedUrl);

        // Initialize WebView
        // Initialize views
//        staticMapView = findViewById(R.id.staticMapView);
//        mapWebView = findViewById(R.id.mapWebView);
//
//        // Load static map image using Google Maps Static API
//        String staticMapUrl = "https://maps.googleapis.com/maps/api/staticmap?center=12.950464387362965,80.20477587484098&zoom=15&size=350x200&markers=color:red%7Clabel:A%7C12.950464387362965,80.20477587484098&key=YOUR_API_KEY";
//        Glide.with(this)
//                .load(staticMapUrl)
//                .into(staticMapView);
//
//        // Load the Google Maps URL for interactive view
//        WebSettings webSettings = mapWebView.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//        mapWebView.setWebViewClient(new WebViewClient());
//        mapWebView.loadUrl("https://www.google.com/maps/search/?api=1&query=12.950464387362965,80.20477587484098");



           // setContentView(R.layout.activity_main);

           // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

            supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        fusedLocationProviderClient = (FusedLocationProviderClient) LocationServices.getFusedLocationProviderClient(this);
            //fusedLocationProviderClient = (fusedLocationProviderClient) LocationServices.getFusedLocationProviderClient(this);
            Dexter.withContext(getApplicationContext()).withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                            getCurrentLocation();
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(com.karumi.dexter.listener.PermissionRequest permissionRequest, PermissionToken permissionToken) {

                        }

//                        @Override
//                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
//                            permissionToken.continuePermissionRequest();
//                        }
                    }).check();



            // Initialize views
        LinearLayout homeIcon = findViewById(R.id.homelayout);
        LinearLayout clockIcon = findViewById(R.id.clocklayout);
        LinearLayout logoutIcon = findViewById(R.id.signoutlayout);
        LinearLayout requestIcon = findViewById(R.id.requestlayout);
        ImageView clockInImage = findViewById(R.id.clockInImage);
        ImageView clockOutImage = findViewById(R.id.clockOutImage);
        TextView clockInText = findViewById(R.id.clockInText);
        TextView clockOutText = findViewById(R.id.clockOutText);
        currentTimer = findViewById(R.id.currentTimer);
        currentTimerRight = findViewById(R.id.currenttimerright);
        welcomeTextView = findViewById(R.id.welcomeTextView);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        fetchAndDisplayUsername();
        fetchAndDisplayClockTimes();

        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ClockInOutActivity.this, DashboardActivity.class);
            startActivity(intent);
        });

        logoutIcon.setOnClickListener(v -> {
            // Clear SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Sign out from Firebase Authentication
            FirebaseAuth.getInstance().signOut();

            // Redirect to the SignInActivity
            Intent intent = new Intent(ClockInOutActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Optionally, show a message indicating the user has logged out
            Toast.makeText(ClockInOutActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        clockIcon.setOnClickListener(v -> {
            Intent clockInOutIntent = new Intent(ClockInOutActivity.this, SwipingTimeActivity.class);
            startActivity(clockInOutIntent);
        });

        // Set listener for requestIcon
        requestIcon.setOnClickListener(v -> {
            Intent salaryDetailsIntent = new Intent(ClockInOutActivity.this, SalaryDetailsActivity.class);
            startActivity(salaryDetailsIntent);
        });

        // Set a single click listener for Clock In
        View.OnClickListener clockInListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndUpdateClockInTime();
            }
        };
        clockInImage.setOnClickListener(clockInListener);
        clockInText.setOnClickListener(clockInListener);

        // Set a single click listener for Clock Out
        View.OnClickListener clockOutListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndUpdateClockOutTime();
            }
        };
        clockOutImage.setOnClickListener(clockOutListener);
        clockOutText.setOnClickListener(clockOutListener);

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
                                    welcomeTextView.setText("Welcome, " + username + "!");
                                } else {
                                    Toast.makeText(ClockInOutActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(ClockInOutActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ClockInOutActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(ClockInOutActivity.this, "User email not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ClockInOutActivity.this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchAndDisplayClockTimes() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null) {
                String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

                mDatabase.child("Users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                DataSnapshot timeRecordsSnapshot = userSnapshot.child("timeRecords").child(currentDate);
                                String clockInTime = timeRecordsSnapshot.child(CLOCK_IN_TIME).getValue(String.class);
                                String clockOutTime = timeRecordsSnapshot.child(CLOCK_OUT_TIME).getValue(String.class);

                                // Display times or default to "00:00"
                                currentTimer.setText(clockInTime != null ? clockInTime : "00:00");
                                currentTimerRight.setText(clockOutTime != null ? clockOutTime : "00:00");
                            }
                        } else {
                            currentTimer.setText("00:00");
                            currentTimerRight.setText("00:00");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ClockInOutActivity.this, "Failed to fetch clock times", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(ClockInOutActivity.this, "User email not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ClockInOutActivity.this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAndUpdateClockInTime() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                // Define time ranges
                boolean isEarlyClockIn = (hour == 6 || (hour == 9 && minute <= 15) || (hour > 6 && hour < 9));
                boolean isLateClockIn = (hour == 9 && minute > 15) || (hour == 10 && minute <= 30);
                boolean isAfterLateClockIn = (hour > 10 || (hour == 10 && minute > 30)); // After 10:30 AM

                String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

                mDatabase.child("Users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String userId = userSnapshot.getKey();

                                String clockInTime = userSnapshot.child("timeRecords").child(date).child("ClockInTime").getValue(String.class);
                                if ("holiday".equals(clockInTime)) {
                                    // If it's a holiday, navigate to CoffFormActivity
                                    Intent intent = new Intent(ClockInOutActivity.this, CoffFormActivity.class);
                                    startActivity(intent);
                                    return; // Stop further execution after navigating
                                }

                                // Check if today is Sunday
                                Calendar calendar = Calendar.getInstance();
                                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                                if (dayOfWeek == Calendar.SUNDAY) {
                                    // Redirect to CoffFormActivity if it's Sunday
                                    Intent intent = new Intent(ClockInOutActivity.this, CoffFormActivity.class);
                                    startActivity(intent);
                                    return; // Exit after redirecting
                                }

                                if (isEarlyClockIn) {
                                    // Early clock-in
                                    getCurrentLocationAndStore(userId, date, true);
                                    //Toast.makeText(ClockInOutActivity.this, "Early clock-in recorded", Toast.LENGTH_SHORT).show();
                                } else if (isLateClockIn) {
                                    // Late clock-in: Navigate to permission activity
                                    Intent intent = new Intent(ClockInOutActivity.this, ClockInPermissionActivity.class);
                                    startActivity(intent);
                                } else if (isAfterLateClockIn) {
                                    // Clock-in after 10:30 AM: Insert the clock-in time
                                    getCurrentLocationAndStore(userId, date, true);
                                    //Toast.makeText(ClockInOutActivity.this, "Clock-in after 10:30 AM recorded", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Invalid clock-in time
                                    Toast.makeText(ClockInOutActivity.this, "Clock-in time is not valid", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(ClockInOutActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ClockInOutActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(ClockInOutActivity.this, "User email not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ClockInOutActivity.this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleClockInWithFixedLocation(String userId, String date, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(ClockInOutActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = (addresses != null && addresses.size() > 0) ? addresses.get(0).getAddressLine(0) : "Unknown Location";

            // Get current clock-in time
            String clockInTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

            // Create a map to store data
            Map<String, Object> clockInData = new HashMap<>();
            clockInData.put("ClockInTime", clockInTime);
            clockInData.put("ClockInTimestamp", ServerValue.TIMESTAMP);
            clockInData.put("clockInAddress", address);

            // Update the database
            mDatabase.child("Users").child(userId).child("timeRecords").child(date).updateChildren(clockInData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ClockInOutActivity.this, "Clock-in recorded successfully", Toast.LENGTH_SHORT).show();

                        // Update the `currentTimer` TextView
                        TextView currentTimer = findViewById(R.id.currentTimer);
                        currentTimer.setText(clockInTime);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ClockInOutActivity.this, "Failed to record clock-in", Toast.LENGTH_SHORT).show();
                    });
        } catch (IOException e) {
            Toast.makeText(this, "Failed to get address. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAndUpdateClockOutTime() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null) {
                String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

                // Fetch user data to check for holiday or other conditions
                mDatabase.child("Users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String clockOutTime = userSnapshot.child("timeRecords").child(date).child("ClockOutTime").getValue(String.class);

                                // Check if it's a holiday
                                if ("holiday".equals(clockOutTime)) {
                                    Intent intent = new Intent(ClockInOutActivity.this, CoffHdOutFormActivity.class);
                                    startActivity(intent);
                                    return; // Stop further execution
                                }

                                // Check if today is Sunday
                                Calendar calendar = Calendar.getInstance();
                                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                                if (dayOfWeek == Calendar.SUNDAY) {
                                    Intent intent = new Intent(ClockInOutActivity.this, CoffHdOutFormActivity.class);
                                    startActivity(intent);
                                    return; // Stop further execution
                                }

                                // If not a holiday or Sunday, proceed with clock-out logic
                                int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                                int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);

                                // Check if the time is between 16:01 and 17:45
                                if ((currentHour == 16 && currentMinute >= 1) ||
                                        (currentHour > 16 && currentHour < 17) ||
                                        (currentHour == 17 && currentMinute <= 45)) {
                                    // Time is within the allowed range for clock-out
                                    Intent intent = new Intent(ClockInOutActivity.this, ClockOutPermissionActivity.class);
                                    startActivity(intent);
                                } else {
                                    // Time is outside the range, proceed to record the clock-out time
                                    String userId = userSnapshot.getKey();
                                    getCurrentLocationAndStoreClockOut(userId, date); // Pass true for clock-out
                                }
                            }
                        } else {
                            Toast.makeText(ClockInOutActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ClockInOutActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(ClockInOutActivity.this, "User email not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ClockInOutActivity.this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCurrentLocationAndStoreClockOut(String userId, String date) {
        // Ensure location permission is granted
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve the user's department from the database
        mDatabase.child("Users").child(userId).child("department").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String department = task.getResult().getValue(String.class);

                // Check if the department is "Site Engineer"
                if ("site engineer".equalsIgnoreCase(department)) {
                    // If the department is "Site Engineer", allow clock-out from any location
                    allowClockOutFromAnyLocation(userId, date);
                } else {
                    // For other departments, enforce the fixed location check
                    enforceFixedLocationClockOut(userId, date);
                }
            } else {
                Toast.makeText(this, "Failed to retrieve user department", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method for clocking out at any location (Site Engineer)
    private void allowClockOutFromAnyLocation(String userId, String date) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                Geocoder geocoder = new Geocoder(ClockInOutActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    String address = (addresses != null && addresses.size() > 0) ? addresses.get(0).getAddressLine(0) : "Unknown Location";

                    // Get current clock-out time
                    String clockOutTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                    // Create a map to store data
                    Map<String, Object> clockOutData = new HashMap<>();
                    clockOutData.put("ClockOutTime", clockOutTime);
                    clockOutData.put("ClockOutTimestamp", ServerValue.TIMESTAMP);
                    clockOutData.put("clockOutAddress", address);  // Save the actual address for Site Engineer
                    clockOutData.put("clockOutLatitude", location.getLatitude());  // Save the latitude
                    clockOutData.put("clockOutLongitude", location.getLongitude());  // Save the longitude

                    // Update the database
                    mDatabase.child("Users").child(userId).child("timeRecords").child(date).updateChildren(clockOutData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(ClockInOutActivity.this, "Clock-out recorded successfully", Toast.LENGTH_SHORT).show();

                                // Update the currentTimerRight TextView with clock-out time
                                TextView currentTimerRight = findViewById(R.id.currenttimerright);
                                currentTimerRight.setText(clockOutTime);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ClockInOutActivity.this, "Failed to record clock-out", Toast.LENGTH_SHORT).show();
                            });
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to get address. Please try again.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Location not available. Ensure GPS is turned on", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method for clocking out at the fixed location (non-Site Engineer)
    private void enforceFixedLocationClockOut(String userId, String date) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                Geocoder geocoder = new Geocoder(ClockInOutActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    String address = (addresses != null && addresses.size() > 0) ? addresses.get(0).getAddressLine(0) : "Unknown Location";

                    // Coordinates to match (fixed location for clock-out)
                    double specifiedLatitude = 12.95978437980499;
                    double specifiedLongitude = 80.14750784607222;
                    double tolerance = 0.0001;  // Tolerance level for location matching

                    // Check if the current location matches the specified one
                    if (Math.abs(location.getLatitude() - specifiedLatitude) < tolerance && Math.abs(location.getLongitude() - specifiedLongitude) < tolerance) {
                        processClockOutAtFixedLocation(userId, date, address, location);
                    } else {
                        // If the user is not in the specified location, show an error message
                        Toast.makeText(this, "You are not in the allowed location to clock out.", Toast.LENGTH_SHORT).show();
                    }

                } catch (IOException e) {
                    Toast.makeText(this, "Failed to get address. Please try again.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Location not available. Ensure GPS is turned on", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method for clocking out at the fixed location (non-Site Engineer)
    private void processClockOutAtFixedLocation(String userId, String date, String address, Location location) {
        String clockOutTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        // Create a map to store data
        Map<String, Object> clockOutData = new HashMap<>();
        clockOutData.put("ClockOutTime", clockOutTime);
        clockOutData.put("ClockOutTimestamp", ServerValue.TIMESTAMP);
        clockOutData.put("clockOutAddress", address);  // Save the actual address
        clockOutData.put("clockOutLatitude", location.getLatitude());  // Save the latitude
        clockOutData.put("clockOutLongitude", location.getLongitude());  // Save the longitude

        // Update the database
        mDatabase.child("Users").child(userId).child("timeRecords").child(date).updateChildren(clockOutData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ClockInOutActivity.this, "Clock-out recorded successfully", Toast.LENGTH_SHORT).show();

                    // Update the currentTimerRight TextView with clock-out time
                    TextView currentTimerRight = findViewById(R.id.currenttimerright);
                    currentTimerRight.setText(clockOutTime);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ClockInOutActivity.this, "Failed to record clock-out", Toast.LENGTH_SHORT).show();
                });
    }



    private void getCurrentLocationAndStore(String userId, String date, boolean shouldRedirect) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve the user's department from the database (assuming it's stored in the "Users" node)
        mDatabase.child("Users").child(userId).child("department").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String department = task.getResult().getValue(String.class);

                // Check if the department is "Site Engineer"
                if ("site engineer".equalsIgnoreCase(department)) {
                    // If the department is Site Engineer, allow clock-in from any location
                    clockInAtAnyLocation(userId, date);
                } else {
                    // If the department is not "Site Engineer", enforce the fixed location check
                    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(ClockInOutActivity.this, Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                String address = (addresses != null && addresses.size() > 0) ? addresses.get(0).getAddressLine(0) : "Unknown Location";

                                // Coordinates to match
                                double specifiedLatitude = 12.95978437980499;
                                double specifiedLongitude = 80.14750784607222;
                                double tolerance = 0.0001;  // Tolerance level for location matching

                                // Check if the current location matches the specified one
                                if (Math.abs(location.getLatitude() - specifiedLatitude) < tolerance && Math.abs(location.getLongitude() - specifiedLongitude) < tolerance) {
                                    clockInAtFixedLocation(userId, date, address);
                                } else {
                                    // If the user is not in the specified location, show an error message
                                    Toast.makeText(this, "You are not in the allowed location to clock in.", Toast.LENGTH_SHORT).show();
                                }

                            } catch (IOException e) {
                                Toast.makeText(this, "Failed to get address. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Location not available. Ensure GPS is turned on", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(this, "Failed to retrieve user department", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method for clocking in at any location (Site Engineer)
    private void clockInAtAnyLocation(String userId, String date) {
        // Fetch the current location of the user
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                // Use Geocoder to get the address from the latitude and longitude
                Geocoder geocoder = new Geocoder(ClockInOutActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    String address = (addresses != null && addresses.size() > 0) ? addresses.get(0).getAddressLine(0) : "Unknown Location";

                    // Get current clock-in time
                    String clockInTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                    // Create a map to store data
                    Map<String, Object> clockInData = new HashMap<>();
                    clockInData.put("ClockInTime", clockInTime);
                    clockInData.put("ClockInTimestamp", ServerValue.TIMESTAMP);
                    clockInData.put("clockInAddress", address);  // Save the actual address for the site engineer
                    clockInData.put("clockInLatitude", location.getLatitude());  // Save the latitude
                    clockInData.put("clockInLongitude", location.getLongitude());  // Save the longitude

                    // Update the database
                    mDatabase.child("Users").child(userId).child("timeRecords").child(date).updateChildren(clockInData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(ClockInOutActivity.this, "Clock-in recorded successfully", Toast.LENGTH_SHORT).show();

                                // Update the currentTimer TextView with clock-in time
                                TextView currentTimer = findViewById(R.id.currentTimer);
                                currentTimer.setText(clockInTime);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ClockInOutActivity.this, "Failed to record clock-in", Toast.LENGTH_SHORT).show();
                            });
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to get address. Please try again.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Location not available. Ensure GPS is turned on", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method for clocking in at the fixed location (non-Site Engineer)
    private void clockInAtFixedLocation(String userId, String date, String address) {
        // Get current clock-in time
        String clockInTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        // Create a map to store data
        Map<String, Object> clockInData = new HashMap<>();
        clockInData.put("ClockInTime", clockInTime);
        clockInData.put("ClockInTimestamp", ServerValue.TIMESTAMP);
        clockInData.put("clockInAddress", address);  // Save the actual address

        // Update the database
        mDatabase.child("Users").child(userId).child("timeRecords").child(date).updateChildren(clockInData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ClockInOutActivity.this, "Clock-in recorded successfully", Toast.LENGTH_SHORT).show();

                    // Update the currentTimer TextView
                    TextView currentTimer = findViewById(R.id.currentTimer);
                    currentTimer.setText(clockInTime);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ClockInOutActivity.this, "Failed to record clock-in", Toast.LENGTH_SHORT).show();
                });
    }


    //    public void getCurrentLocation() {
//        // Fixed location coordinates
//        double fixedLatitude = 12.950558490797627;
//        double fixedLongitude = 80.20731860893677;
//
//        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
//            @Override
//            public void onMapReady(@NonNull GoogleMap googleMap) {
//                LatLng latLng = new LatLng(fixedLatitude, fixedLongitude);
//                MarkerOptions markerOptions = new MarkerOptions().position(latLng);
//                googleMap.addMarker(markerOptions);
//                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
//            }
//        });
//    }

    public void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull GoogleMap googleMap) {
                        if (location != null) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng);
                            googleMap.addMarker(markerOptions);
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        } else {
                            Toast.makeText(ClockInOutActivity.this, "Please on your location app permission", Toast.LENGTH_SHORT);
                        }
                    }
                });

            }

        });
    }


    private void updateCurrentTime(TextView textView, String timeKey, String timestampKey, boolean isClockIn) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        textView.setText(currentTime);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null) {
                String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

                mDatabase.child("Users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                // Save both the time and the timestamp
                                mDatabase.child("Users").child(userSnapshot.getKey()).child("timeRecords").child(date).child(timeKey).setValue(currentTime);
                                mDatabase.child("Users").child(userSnapshot.getKey()).child("timeRecords").child(date).child(timestampKey).setValue(System.currentTimeMillis())
                                        .addOnSuccessListener(aVoid -> {
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            if (isClockIn) {
                                                editor.putString(CLOCK_IN_TIME, currentTime);
                                                editor.putLong(CLOCK_IN_TIMESTAMP, System.currentTimeMillis());
                                            } else {
                                                editor.putString(CLOCK_OUT_TIME, currentTime);
                                                editor.putLong(CLOCK_OUT_TIMESTAMP, System.currentTimeMillis());
                                            }
                                            editor.apply();
                                            Toast.makeText(ClockInOutActivity.this, (isClockIn ? "Clocked in" : "Clocked out") + " successfully", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(ClockInOutActivity.this, "Error updating clock time", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Toast.makeText(ClockInOutActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ClockInOutActivity.this, "Failed to update clock time", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(ClockInOutActivity.this, "User email not found", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
