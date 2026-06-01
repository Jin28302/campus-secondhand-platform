package com.example.backend.common;

import lombok.Getter;

/**
 * 商品状态枚举
 * 定义了商品在系统中的所有可能状态：
 * - PENDING：待审核（商家发布后等待管理员审核）
 * - PUBLISHED：在售（管理员审核通过，可供购买）
 * - OFF_SHELF：已下架（商家主动下架）
 */
@Getter
public enum ProductStatus {

    /** 待审核 */
    PENDING("pending"),
    /** 在售 */
    PUBLISHED("published"),
    /** 已下架 */
    OFF_SHELF("off_shelf");

    /** 状态的字符串值 */
    private final String value;

    ProductStatus(String value) {
        this.value = value;
    }
}
