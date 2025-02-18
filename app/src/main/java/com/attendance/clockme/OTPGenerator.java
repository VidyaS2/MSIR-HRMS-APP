package com.attendance.clockme;

public class OTPGenerator {
    public static String generateOTP() {
        int otp = (int) (Math.random() * 900000) + 100000; // Generates a 6-digit OTP
        return String.valueOf(otp);
    }
}