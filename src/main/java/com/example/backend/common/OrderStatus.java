package com.example.backend.common;

import lombok.Getter;

/**
 * 订单状态枚举
 * - PENDING：待发货（买家已付款，等待商家发货）
 * - SHIPPED：待收货（商家已发货，等待买家确认收货）
 * - COMPLETED：已完成（买家已确认收货）
 * - REFUND_PENDING：退货待审核
 * - REFUNDED：已退货
 * - REFUND_REJECTED：退货被拒
 */
@Getter
public enum OrderStatus {

    /** 待发货 */
    PENDING("pending"),
    /** 待收货 */
    SHIPPED("shipped"),
    /** 已完成 */
    COMPLETED("completed"),
    /** 退货待审核 */
    REFUND_PENDING("refund_pending"),
    /** 已退货 */
    REFUNDED("refunded"),
    /** 退货被拒 */
    REFUND_REJECTED("refund_rejected");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }
}
