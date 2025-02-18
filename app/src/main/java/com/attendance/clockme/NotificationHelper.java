package com.attendance.clockme;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

public class NotificationHelper {

    private static final String CHANNEL_ID = "team_requests_channel";
    private Context context;

    // Constructor
    public NotificationHelper(Context context) {
        this.context = context;
    }

    // Create a notification channel (for Android Oreo and above) - This should be done once in the app
    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Team Requests";
            String description = "Notifications for pending requests from teams";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // Method to fetch and send notifications based on data from Firebase
    public void fetchRequestsAndSendNotifications(String loggedInEmail) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        // Fetch data from different nodes (ClockIn, ClockOut, LateIn, LateOut)
        fetchClockInPermissionRequests(database, loggedInEmail);
        fetchClockOutPermissionRequests(database, loggedInEmail);
        fetchLateInRequests(database, loggedInEmail);
        fetchLateOutRequests(database, loggedInEmail);
    }

    // Fetch ClockInPermission requests and send notifications if pending
    private void fetchClockInPermissionRequests(DatabaseReference database, String loggedInEmail) {
        database.child("ClockInPermission").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot uidSnapshot : dataSnapshot.getChildren()) {
                    // Loop through each "uid" node
                    for (DataSnapshot dateSnapshot : uidSnapshot.getChildren()) {
                        // Accessing the date node which contains "status", "team", "username", etc.
                        String status = dateSnapshot.child("status").getValue(String.class);
                        String team = dateSnapshot.child("team").getValue(String.class);
                        String username = dateSnapshot.child("username").getValue(String.class);
                        String email = dateSnapshot.child("email").getValue(String.class);  // You may use this if you need to check email for notification

                        // Only send notification if status is "pending"
                        if ("pending".equals(status)) {
                            // Check if the logged-in user's email matches the team
                            if ((team.equals("Team 1") && loggedInEmail.equals("azeem@msirindia.com")) ||
                                    (team.equals("Team 2") && loggedInEmail.equals("kamal@msirindia.com")) ||
                                    (team.equals("Team 3") && loggedInEmail.equals("karthick@msirindia.com"))) {

                                // Send the notification with appropriate details
                                sendNotification("New Clock In Request", "New request from " + username + " of " + team);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                runOnUiThread(() -> Toast.makeText(context, "Failed to fetch data", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Fetch ClockOutPermission requests and send notifications if pending
    private void fetchClockOutPermissionRequests(DatabaseReference database, String loggedInEmail) {
        database.child("ClockOutPermission").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot uidSnapshot : dataSnapshot.getChildren()) {
                    // Loop through each "uid" node
                    for (DataSnapshot dateSnapshot : uidSnapshot.getChildren()) {
                        // Accessing the date node which contains "status", "team", "username", etc.
                        String status = dateSnapshot.child("status").getValue(String.class);
                        String team = dateSnapshot.child("team").getValue(String.class);
                        String username = dateSnapshot.child("username").getValue(String.class);
                        String email = dateSnapshot.child("email").getValue(String.class);  // You may use this if you need to check email for notification

                        // Only send notification if status is "pending"
                        if ("pending".equals(status)) {
                            // Check if the logged-in user's email matches the team
                            if ((team.equals("Team 1") && loggedInEmail.equals("azeem@msirindia.com")) ||
                                    (team.equals("Team 2") && loggedInEmail.equals("kamal@msirindia.com")) ||
                                    (team.equals("Team 3") && loggedInEmail.equals("karthick@msirindia.com"))) {

                                // Send the notification with appropriate details
                                sendNotification("New Clock Out Request", "New request from " + username + " of " + team);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                runOnUiThread(() -> Toast.makeText(context, "Failed to fetch data", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Fetch LateInRequests and send notifications if pending
    private void fetchLateInRequests(DatabaseReference database, String loggedInEmail) {
        database.child("LateInRequests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String status = snapshot.child("status").getValue(String.class);
                    String team = snapshot.child("selectedTeam").getValue(String.class);
                    String username = snapshot.child("username").getValue(String.class);

                    // Only send notification if status is "pending"
                    if ("pending".equals(status)) {
                        if (team.equals("Team 1") && loggedInEmail.equals("azeem@msirindia.com")) {
                            sendNotification("New Late In Request", "New request from " + username + " of " + team);
                        } else if (team.equals("Team 2") && loggedInEmail.equals("kamal@msirindia.com")) {
                            sendNotification("New Late In Request", "New request from " + username + " of " + team);
                        } else if (team.equals("Team 3") && loggedInEmail.equals("karthick@msirindia.com")) {
                            sendNotification("New Late In Request", "New request from " + username + " of " + team);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fetch LateOutRequests and send notifications if pending
    private void fetchLateOutRequests(DatabaseReference database, String loggedInEmail) {
        database.child("LateOutRequests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String status = snapshot.child("status").getValue(String.class);
                    String team = snapshot.child("selectedTeam").getValue(String.class);
                    String username = snapshot.child("username").getValue(String.class);

                    // Only send notification if status is "pending"
                    if ("pending".equals(status)) {
                        if (team.equals("Team 1") && loggedInEmail.equals("azeem@msirindia.com")) {
                            sendNotification("New Late Out Request", "New request from " + username + " of " + team);
                        } else if (team.equals("Team 2") && loggedInEmail.equals("kamal@msirindia.com")) {
                            sendNotification("New Late Out Request", "New request from " + username + " of " + team);
                        } else if (team.equals("Team 3") && loggedInEmail.equals("karthick@msirindia.com")) {
                            sendNotification("New Late Out Request", "New request from " + username + " of " + team);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to send a notification with unique IDs and create channel if necessary
    private void sendNotification(String title, String message) {
        createNotificationChannel(); // Ensure channel is created

        // Generate a unique notification ID using the username or a random UUID
        int notificationId = (message + System.currentTimeMillis()).hashCode();  // Unique ID based on message
        // Or use a UUID:
        // int notificationId = UUID.randomUUID().hashCode();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification)  // Ensure this exists
                .setAutoCancel(true)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)  // Optional: Set sound
                .setVibrate(new long[] {0, 500, 1000});  // Optional: Set vibration pattern

        // Send the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(notificationId, notificationBuilder.build());
        }
    }
    // Helper method to run code on UI thread (for Toast or other UI operations)
    private void runOnUiThread(Runnable runnable) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(runnable);
        }
    }
}