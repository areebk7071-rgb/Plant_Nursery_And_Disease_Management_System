package com.plantmanager.repository;

import com.plantmanager.model.User;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    public List<User> loadAll() throws IOException {
        try {
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

    public Optional<User> findByUsername(String username) throws IOException {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        String sql = "SELECT username, password_hash, display_name FROM users WHERE LOWER(username) = LOWER(?)";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
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

    public void createUser(User user) throws IOException {
        try {
            insertUser(DatabaseManager.getConnection(), user);
        } catch (SQLException e) {
            throw new IOException("Failed to create user " + user.getUsername(), e);
        }
    }

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
        String sql = "INSERT INTO users (username, password_hash, display_name) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getDisplayName());
            stmt.executeUpdate();
        }
    }
}
