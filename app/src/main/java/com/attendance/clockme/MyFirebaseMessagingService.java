package com.attendance.clockme;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "team_requests_channel";
    private static final String CHANNEL_NAME = "Team Requests";
    private static final String CHANNEL_DESCRIPTION = "Notifications for pending team requests";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Retrieve the logged-in user's email from the session
        SessionManager sessionManager = new SessionManager(this);
        String loggedInEmail = sessionManager.getEmail();

        // Check if the message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            // Optionally customize the notification for the logged-in user
            if (loggedInEmail != null) {
                body = body + "\nFor user: " + loggedInEmail;
            }

            // Display the notification
            sendNotification(title, body);
        }
    }

    private void sendNotification(String title, String messageBody) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Ensure the NotificationChannel is created (Android O and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager);
        }

        // Build the notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Replace with your app's icon
                .setContentTitle(title != null ? title : "Notification")
                .setContentText(messageBody != null ? messageBody : "New request.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(new long[]{0, 500, 1000});

        // Display the notification
        if (notificationManager != null) {
            int notificationId = (title + messageBody + System.currentTimeMillis()).hashCode(); // Unique ID per notification
            notificationManager.notify(notificationId, notificationBuilder.build());
        }
    }

    private void createNotificationChannel(NotificationManager notificationManager) {
        if (notificationManager != null) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationChannel.setDescription(CHANNEL_DESCRIPTION);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{0, 500, 1000});
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}