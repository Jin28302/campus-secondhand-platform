package com.example.backend.common;

public class UserContext {

    private static final ThreadLocal<UserInfo> HOLDER = new ThreadLocal<>();

    public static void set(UserInfo info) {
        HOLDER.set(info);
    }

    public static UserInfo get() {
        return HOLDER.get();
    }

    public static void remove() {
        HOLDER.remove();
    }

    public record UserInfo(Long userId, String phone, String role) {}
}
