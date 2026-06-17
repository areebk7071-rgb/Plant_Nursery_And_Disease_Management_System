package com.plantmanager.model;

import com.plantmanager.repository.CsvUtils;

/**
 * Application user for login authentication.
 */
public class User {

    private final String username;
    private final String passwordHash;
    private final String displayName;

    public User(String username, String passwordHash, String displayName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static String csvHeader() {
        return "username,passwordHash,displayName";
    }

    public String toCsvRow() {
        return CsvUtils.quote(username) + ","
                + CsvUtils.quote(passwordHash) + ","
                + CsvUtils.quote(displayName);
    }

    public static User fromCsv(String[] parts) {
        if (parts.length < 3) {
            return null;
        }
        return new User(parts[0].trim(), parts[1].trim(), parts[2].trim());
    }
}
