package com.plantmanager.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hashes passwords for secure storage (no plaintext in users.csv).
 */
public final class PasswordHasher {

    private static final String SALT = "bta-v1:";

    private PasswordHasher() {
    }

    public static String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((SALT + password).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static boolean matches(String password, String storedHash) {
        return hash(password).equals(storedHash);
    }
}
