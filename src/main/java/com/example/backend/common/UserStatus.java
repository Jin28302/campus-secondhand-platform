package com.example.backend.common;

import lombok.Getter;

/**
 * 用户状态枚举
 * 定义了用户在系统中的所有可能状态：
 * - PENDING：待审核（新注册用户等待管理员审核）
 * - NORMAL：正常（审核通过，可正常使用）
 * - BANNED：封禁（管理员封禁，无法登录和使用）
 */
@Getter
public enum UserStatus {

    /** 待审核 */
    PENDING("pending"),
    /** 正常 */
    NORMAL("normal"),
    /** 封禁 */
    BANNED("banned");

    /** 状态的字符串值 */
    private final String value;

    UserStatus(String value) {
        this.value = value;
    }
}
