package com.example.backend.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// JWT工具类 - 生成和解析JWT token
public final class JwtUtil {

    // 签名密钥（至少32字符）
    private static final String SECRET = "your-secret-key-must-be-at-least-32-chars!";
    // token有效期：24小时
    private static final long EXPIRE_MS = 24 * 60 * 60 * 1000;
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private JwtUtil() {}

    // 生成JWT token - 携带用户ID(subject)、手机号、角色，有效期24小时
    public static String generate(Long userId, String phone, String role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("phone", phone)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_MS))
                .signWith(KEY)
                .compact();
    }

    // 解析JWT token - 返回Claims，包含userId(subject)、phone、role
    public static Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
