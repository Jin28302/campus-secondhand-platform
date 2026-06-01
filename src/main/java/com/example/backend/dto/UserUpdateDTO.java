package com.example.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户信息更新请求DTO - 管理员修改用户信息
 * 相比 ProfileUpdateDTO，多了角色、状态、商家等级、积分、钱包等管理员专属字段
 * 所有字段均为可选
 */
@Data
public class UserUpdateDTO {

    /** 姓名（可选） */
    private String name;

    /** 手机号（可选） */
    private String phone;

    /** 邮箱（可选） */
    private String email;

    /** 城市（可选） */
    private String city;

    /** 性别（可选）：1=男，0=女 */
    private Integer gender;

    /** 银行账号（可选） */
    private String bankAccount;

    /** 角色（可选）：user/merchant/admin */
    private String role;

    /** 用户状态（可选）：pending/normal/banned */
    private String status;

    /** 商家等级（可选，1-5级） */
    private Integer sellerLevel;

    /** 积分（可选） */
    private Integer points;

    /** 钱包余额（可选） */
    private BigDecimal wallet;
}
