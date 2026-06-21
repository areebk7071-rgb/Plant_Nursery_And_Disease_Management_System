package com.plantmanager.repository;

import com.plantmanager.model.User;
import com.plantmanager.service.PasswordHasher;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Performs full CRUD (Create, Read, Update, Delete) for user credentials
 * against the SQLite {@code users} table via JDBC. Passwords are stored
 * as hashes (never plain text) via {@link PasswordHasher}.
 */
public class UserRepository {

    public static final String DEFAULT_USERNAME = "admin";
    public static final String DEFAULT_PASSWORD = "admin123";
    public static final String DEFAULT_DISPLAY_NAME = "Administrator";

    /** CREATE — seeds the default accounts the first time the application runs. */
    public void ensureDefaultUsers() throws IOException {
        try {
            Connection conn = DatabaseManager.getConnection();
            try (Statement check = conn.createStatement();
                 ResultSet rs = check.executeQuery("SELECT COUNT(*) AS cnt FROM users")) {
                if (rs.next() && rs.getInt("cnt") > 0) {
                    return;
                }
            }
            List<User> defaults = List.of(
                    new User(DEFAULT_USERNAME, PasswordHasher.hash(DEFAULT_PASSWORD), DEFAULT_DISPLAY_NAME),
                    new User("gardener", PasswordHasher.hash("garden2024"), "Garden Staff")
            );
            for (User user : defaults) {
                insertUser(conn, user);
            }
        } catch (SQLException e) {
            throw new IOException("Failed to initialize default users", e);
        }
    }

    /** READ — returns every registered user. */
    public List<User> loadAll() throws IOException {
        try {
            ensureDefaultUsers();
            Connection conn = DatabaseManager.getConnection();
            List<User> users = new ArrayList<>();
            String sql = "SELECT username, password_hash, display_name FROM users ORDER BY username";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(new User(
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("display_name")
                    ));
                }
            }
            return users;
        } catch (SQLException e) {
            throw new IOException("Failed to load users from database", e);
        }
    }

    /** READ (single) — looks up one user by username, used during login. */
    public Optional<User> findByUsername(String username) throws IOException {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        String sql = "SELECT username, password_hash, display_name FROM users WHERE LOWER(username) = LOWER(?)";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            ensureDefaultUsers();
            stmt.setString(1, username.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("display_name")
                    ));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IOException("Failed to look up user " + username, e);
        }
    }

    /** CREATE — registers a new user (also usable for future "Register" UI). */
    public void createUser(User user) throws IOException {
        try {
            insertUser(DatabaseManager.getConnection(), user);
        } catch (SQLException e) {
            throw new IOException("Failed to create user " + user.getUsername(), e);
        }
    }

    /** UPDATE — updates an existing user's password hash and/or display name. */
    public void updateUser(User user) throws IOException {
        String sql = "UPDATE users SET password_hash = ?, display_name = ? WHERE username = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, user.getPasswordHash());
            stmt.setString(2, user.getDisplayName());
            stmt.setString(3, user.getUsername());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IOException("Failed to update user " + user.getUsername(), e);
        }
    }

    /** DELETE — removes a user account by username. */
    public void deleteUser(String username) throws IOException {
        String sql = "DELETE FROM users WHERE username = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IOException("Failed to delete user " + username, e);
        }
    }

    private void insertUser(Connection conn, User user) throws SQLException {
        String sql = "INSERT OR IGNORE INTO users (username, password_hash, display_name) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getDisplayName());
            stmt.executeUpdate();
        }
    }
}
