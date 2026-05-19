package com.example.backend.common;

import lombok.Getter;

@Getter
public enum ProductStatus {

    PENDING("pending"),
    PUBLISHED("published"),
    OFF_SHELF("off_shelf");

    private final String value;

    ProductStatus(String value) {
        this.value = value;
    }
}
