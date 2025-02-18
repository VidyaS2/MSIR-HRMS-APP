package com.attendance.clockme;

public class LeaveRequest {
    private String fromDate;
    private String toDate;
    private String reason;
    private String username;
    private String email;
    private String position;

    // Default constructor required for calls to DataSnapshot.getValue(LeaveRequest.class)
    public LeaveRequest() {
        // No-argument constructor for Firebase serialization
    }

    // Parametrized constructor
    public LeaveRequest(String fromDate, String toDate, String reason, String username, String email, String position) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.reason = reason;
        this.username = username;
        this.email = email;
        this.position = position;
    }

    // Getters and Setters for all properties
    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    // Optional: Override toString() to return a string representation of the object
    @Override
    public String toString() {
        return "LeaveRequest{" +
                "fromDate='" + fromDate + '\'' +
                ", toDate='" + toDate + '\'' +
                ", reason='" + reason + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", position='" + position + '\'' +
                '}';
    }
}
