package com.example.backend.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {}

    public static String encode(String raw) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String saltStr = Base64.getEncoder().encodeToString(salt);
        String hashed = sha256(saltStr + raw);
        return saltStr + ":" + hashed;
    }

    public static boolean matches(String raw, String encoded) {
        String[] parts = encoded.split(":");
        if (parts.length != 2) return false;
        String hashed = sha256(parts[0] + raw);
        return hashed.equals(parts[1]);
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
