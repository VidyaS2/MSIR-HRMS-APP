package com.attendance.clockme;

public class CalendarDate {
    private String date;
    private String status; // Can be "leave", "present", "half_day", "holiday"

    // Constructor
    public CalendarDate(String date, String status) {
        this.date = date;
        this.status = status;
    }

    // Getters and setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
