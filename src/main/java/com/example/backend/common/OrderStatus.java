package com.example.backend.common;

import lombok.Getter;

@Getter
public enum OrderStatus {

    PENDING("pending"),
    COMPLETED("completed"),
    REFUND_PENDING("refund_pending"),
    REFUNDED("refunded"),
    REFUND_REJECTED("refund_rejected");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }
}
