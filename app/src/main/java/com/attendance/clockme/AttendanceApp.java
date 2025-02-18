package com.attendance.clockme;

import java.util.Random;

public class AttendanceApp {
    public static void main(String[] args) {
        User user = new User("john_doe", "john.doe@example.com");
        String otp = generateOTP(6);
        String welcomeMessage = constructWelcomeMessage(user, otp);
        sendEmail(user.email, "Welcome to Attendance App", welcomeMessage);
    }

    public static class User {
        public String username;
        public String email;

        public User(String username, String email) {
            this.username = username;
            this.email = email;
        }
    }

    public static String generateOTP(int length) {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10)); // Generates a random integer between 0 and 9
        }
        return otp.toString();
    }

    public static String constructWelcomeMessage(User user, String otp) {
        return "Dear " + user.username + ",\n\n" +
                "Welcome to Attendance App!\n\n" +
                "Your OTP is: " + otp + "\n" +
                "Please use this OTP to continue to your dashboard.\n\n" +
                "Thank you for joining us!\n\n" +
                "Best regards,\n" +
                "The Attendance App Team";
    }

    public static void sendEmail(String to, String subject, String body) {
        // Implement your email sending logic here.
        // You can use JavaMail API or any other email service provider API.
        System.out.println("Sending email to: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: \n" + body);
        // Actual email sending code goes here.
    }
}
