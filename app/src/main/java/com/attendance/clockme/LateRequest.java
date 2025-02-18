package com.attendance.clockme;

public class LateRequest {
    private String username;
    private String position;
    private String email;
    private String date;
    private String clockInTime;
    private String clockOutTime;
    private String remark;
    private String selectedTeam;
    private String userId;
    private String status;  // Add status field
    private String selectedAction;  // Replace clock with selectedAction (Clock In / Clock Out)

    // Constructor with status defaulting to "pending" and selectedAction for clock action
    public LateRequest(String username, String position, String email, String date,
                       String clockInTime, String clockOutTime, String remark,
                       String selectedTeam, String userId, String selectedAction) {
        this.username = username;
        this.position = position;
        this.email = email;
        this.date = date;
        this.clockInTime = clockInTime;
        this.clockOutTime = clockOutTime;
        this.remark = remark;
        this.selectedTeam = selectedTeam;
        this.userId = userId;
        this.status = "pending";  // Set default value to "pending"
        this.selectedAction = selectedAction; // Initialize the new selectedAction field
    }

    // Getters and Setters for all fields, including the new selectedAction field
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getClockInTime() {
        return clockInTime;
    }

    public void setClockInTime(String clockInTime) {
        this.clockInTime = clockInTime;
    }

    public String getClockOutTime() {
        return clockOutTime;
    }

    public void setClockOutTime(String clockOutTime) {
        this.clockOutTime = clockOutTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getSelectedTeam() {
        return selectedTeam;
    }

    public void setSelectedTeam(String selectedTeam) {
        this.selectedTeam = selectedTeam;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSelectedAction() {
        return selectedAction;
    }

    public void setSelectedAction(String selectedAction) {
        this.selectedAction = selectedAction;
    }
}
