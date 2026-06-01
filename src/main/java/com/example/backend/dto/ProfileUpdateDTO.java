package com.example.backend.dto;

import lombok.Data;

/**
 * 个人资料更新请求DTO - 接收用户修改个人资料的数据
 * 用户可在个人中心修改姓名、手机号、邮箱、城市、性别、银行账号
 * 所有字段均为可选（不传的字段保持不变）
 */
@Data
public class ProfileUpdateDTO {

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
}
