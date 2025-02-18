package com.attendance.clockme;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "team_requests_channel";
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize context
        context = this;

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        // Get SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Check if the user is logged in
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false); // Default to false if not found
        String loggedInEmail = prefs.getString("loggedInEmail", ""); // Default to empty string if not found

        SessionManager sessionManager = new SessionManager(this);
        sessionManager.saveEmail(loggedInEmail);

        fetchAndSendNotifications(loggedInEmail);

        if (isLoggedIn) {
            if ("calibration@msirindia.com".equals(loggedInEmail)) {
                // If logged in as admin, redirect to Admin Dashboard
                Intent intent = new Intent(MainActivity.this, ActivityAdminDashboard.class);
                startActivity(intent);
            } else if ("azeem@msirindia.com".equals(loggedInEmail) ||
                    "kamal@msirindia.com".equals(loggedInEmail) ||
                    "karthick@msirindia.com".equals(loggedInEmail)) {
                // If logged in with one of the team emails, redirect to AdminThreeDashboard
                Intent intent = new Intent(MainActivity.this, AdminThreeDashboard.class);
                startActivity(intent);

                // Fetch and display notifications

            } else {
                // If logged in as a regular user, redirect to User Dashboard
                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                startActivity(intent);
            }
        } else {
            // Otherwise, redirect to SignInActivity
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        }

        finish(); // Close MainActivity

        // Notification channel for notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "New Request";
            String description = "Pending Requests";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void fetchAndSendNotifications(String loggedInEmail) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        // Fetch notifications for ClockInPermission
        fetchClockInPermissionRequests(database, loggedInEmail);

        // Fetch notifications for ClockOutPermission
        fetchClockOutPermissionRequests(database, loggedInEmail);

        // Fetch notifications for LateInRequests
        fetchLateInRequests(database, loggedInEmail);

        // Fetch notifications for LateOutRequests
        fetchLateOutRequests(database, loggedInEmail);
    }

    private void fetchClockInPermissionRequests(DatabaseReference database, String loggedInEmail) {
        database.child("ClockInPermission").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot uidSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot dateSnapshot : uidSnapshot.getChildren()) {
                        String status = dateSnapshot.child("status").getValue(String.class);
                        String team = dateSnapshot.child("team").getValue(String.class);
                        String username = dateSnapshot.child("username").getValue(String.class);

                        if ("pending".equals(status)) {
                            sendTeamNotification(loggedInEmail, team, username, "Clock In Request");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                runOnUiThread(() -> Toast.makeText(context, "Failed to fetch Clock In data", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void fetchClockOutPermissionRequests(DatabaseReference database, String loggedInEmail) {
        database.child("ClockOutPermission").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot uidSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot dateSnapshot : uidSnapshot.getChildren()) {
                        String status = dateSnapshot.child("status").getValue(String.class);
                        String team = dateSnapshot.child("team").getValue(String.class);
                        String username = dateSnapshot.child("username").getValue(String.class);

                        if ("pending".equals(status)) {
                            sendTeamNotification(loggedInEmail, team, username, "Clock Out Request");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                runOnUiThread(() -> Toast.makeText(context, "Failed to fetch Clock Out data", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void fetchLateInRequests(DatabaseReference database, String loggedInEmail) {
        database.child("LateInRequests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String status = snapshot.child("status").getValue(String.class);
                    String team = snapshot.child("selectedTeam").getValue(String.class);
                    String username = snapshot.child("username").getValue(String.class);

                    if ("pending".equals(status)) {
                        sendTeamNotification(loggedInEmail, team, username, "Late In Request");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                runOnUiThread(() -> Toast.makeText(context, "Failed to fetch Late In data", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void fetchLateOutRequests(DatabaseReference database, String loggedInEmail) {
        database.child("LateOutRequests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String status = snapshot.child("status").getValue(String.class);
                    String team = snapshot.child("selectedTeam").getValue(String.class);
                    String username = snapshot.child("username").getValue(String.class);

                    if ("pending".equals(status)) {
                        sendTeamNotification(loggedInEmail, team, username, "Late Out Request");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                runOnUiThread(() -> Toast.makeText(context, "Failed to fetch Late Out data", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void sendTeamNotification(String loggedInEmail, String team, String username, String requestType) {
        if ((team.equals("Team 1") && loggedInEmail.equals("azeem@msirindia.com")) ||
                (team.equals("Team 2") && loggedInEmail.equals("kamal@msirindia.com")) ||
                (team.equals("Team 3") && loggedInEmail.equals("karthick@msirindia.com"))) {
            sendNotification(requestType, "New request from " + username);
        }
    }

    private void sendNotification(String title, String message) {
        int notificationId = (message + System.currentTimeMillis()).hashCode();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(new long[]{0, 500, 1000});

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(notificationId, notificationBuilder.build());
        }
    }
}
