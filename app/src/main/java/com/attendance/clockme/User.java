package com.attendance.clockme;

import java.util.List;

public class User {
    private String username;
    private String email;
    private String password; // New field for password
    private String salary;   // New field for salary
    private String position; // New field for position


    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public User() {
    }

    // Constructor to initialize the User object with username, email, password, salary, and position
    public User(String username, String email, String password, String salary, String position) {
        this.username = username;
        this.email = email;
        this.password = password; // Initialize the password
        this.salary = salary;     // Initialize the salary
        this.position = position; // Initialize the position
    }

    // Getter and setter methods for username
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Getter and setter methods for email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter and setter methods for password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Getter and setter methods for salary
    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    // Getter and setter methods for position
    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
