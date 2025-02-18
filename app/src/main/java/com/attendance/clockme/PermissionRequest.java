package com.attendance.clockme;

public class PermissionRequest {
    private String username;
    private String position;
    private String email;
    private String date;
    private String hours;
    private String fromTime;
    private String toTime;
    private String reason;
    private String team;

    // Constructor
    public PermissionRequest(String username, String position, String email, String date, String hours, String fromTime, String toTime, String reason, String team) {
        this.username = username;
        this.position = position;
        this.email = email;
        this.date = date;
        this.hours = hours;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.reason = reason;
        this.team = team;
    }

    // Getters (no setters to keep the object immutable after creation)
    public String getUsername() {
        return username;
    }

    public String getPosition() {
        return position;
    }

    public String getEmail() {
        return email;
    }

    public String getDate() {
        return date;
    }

    public String getHours() {
        return hours;
    }

    public String getFromTime() {
        return fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public String getReason() {
        return reason;
    }
}
