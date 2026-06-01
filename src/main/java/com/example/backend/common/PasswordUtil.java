package com.example.backend.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

// 密码工具类 - 使用随机盐 + SHA-256哈希进行密码加密和校验
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {}

    // 密码加密 - 生成16字节随机盐，拼接明文后SHA-256哈希，格式：salt:hash
    public static String encode(String raw) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String saltStr = Base64.getEncoder().encodeToString(salt);
        // 哈希 = SHA-256(盐 + 明文)
        String hashed = sha256(saltStr + raw);
        return saltStr + ":" + hashed;
    }

    // 密码校验 - 从存储的密文中提取盐，用同样的算法计算哈希并比较
    public static boolean matches(String raw, String encoded) {
        String[] parts = encoded.split(":");
        if (parts.length != 2) return false;
        // 用存储的盐对输入明文做相同哈希，比较结果
        String hashed = sha256(parts[0] + raw);
        return hashed.equals(parts[1]);
    }

    // SHA-256哈希计算 - 输入字符串，返回16进制小写哈希字符串
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
