package com.example.backend.common;

import lombok.Getter;

@Getter
public enum UserStatus {

    PENDING("pending"),
    NORMAL("normal"),
    BANNED("banned");

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }
}
