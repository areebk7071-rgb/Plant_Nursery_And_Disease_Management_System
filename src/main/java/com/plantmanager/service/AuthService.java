package com.plantmanager.service;

import com.plantmanager.model.User;
import com.plantmanager.repository.UserRepository;

import java.io.IOException;
import java.util.Optional;

/**
 * Handles login sessions and credential verification.
 */
public class AuthService {

    private final UserRepository userRepository;
    private User currentUser;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public Optional<User> authenticate(String username, String password) throws IOException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Optional.empty();
        }
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        if (!PasswordHasher.matches(password, user.get().getPasswordHash())) {
            return Optional.empty();
        }
        currentUser = user.get();
        return Optional.of(currentUser);
    }

    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public void logout() {
        currentUser = null;
    }
}
