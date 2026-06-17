package com.plantmanager.repository;

import com.plantmanager.model.User;
import com.plantmanager.service.PasswordHasher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Loads and saves user credentials from users.csv.
 */
public class UserRepository {

    public static final String DEFAULT_USERNAME = "admin";
    public static final String DEFAULT_PASSWORD = "admin123";
    public static final String DEFAULT_DISPLAY_NAME = "Administrator";

    private static final Path USERS_FILE = Paths.get("users.csv");

    public void ensureDefaultUsers() throws IOException {
        if (Files.exists(USERS_FILE)) {
            return;
        }
        List<User> defaults = List.of(
                new User(DEFAULT_USERNAME,
                        PasswordHasher.hash(DEFAULT_PASSWORD),
                        DEFAULT_DISPLAY_NAME),
                new User("gardener",
                        PasswordHasher.hash("garden2024"),
                        "Garden Staff")
        );
        saveAll(defaults);
    }

    public List<User> loadAll() throws IOException {
        ensureDefaultUsers();
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(USERS_FILE)) {
            String line = reader.readLine();
            if (line == null) {
                return users;
            }
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                User user = User.fromCsv(CsvUtils.parseLine(line));
                if (user != null) {
                    users.add(user);
                }
            }
        }
        return users;
    }

    public Optional<User> findByUsername(String username) throws IOException {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        String normalized = username.trim().toLowerCase();
        return loadAll().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(normalized))
                .findFirst();
    }

    private void saveAll(List<User> users) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(USERS_FILE)) {
            writer.write(User.csvHeader());
            writer.newLine();
            for (User user : users) {
                writer.write(user.toCsvRow());
                writer.newLine();
            }
        }
    }
}
