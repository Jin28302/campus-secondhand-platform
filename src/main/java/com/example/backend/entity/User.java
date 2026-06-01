package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户实体 - 对应 users 表
 * 包含用户基本信息、角色、状态、钱包余额和积分字段
 * 支持三种角色：user（普通用户）、seller（商家）、admin（管理员）
 * 用户状态：pending（待审核）、normal（正常）、banned（封禁）
 */
@Data
@TableName("users")
public class User {

    /** 用户ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录密码（加密存储） */
    private String password;

    /** 用户姓名 */
    private String name;

    /** 手机号，用于登录和注册，需唯一 */
    private String phone;

    /** 邮箱地址 */
    private String email;

    /** 所在城市 */
    private String city;

    /** 性别：1=男，0=女 */
    private Integer gender;

    /** 银行账号（16位数字） */
    private String bankAccount;

    /** 用户角色：user（普通用户）、seller（商家）、admin（管理员） */
    private String role;

    /** 用户状态：pending（待审核）、normal（正常）、banned（封禁） */
    private String status;

    /** 营业执照图片URL（仅商家，注册时上传） */
    private String licenseImg;

    /** 身份证图片URL（仅商家，注册时上传） */
    private String idCardImg;

    /** 商家等级（1-5级，仅商家有值） */
    private Integer sellerLevel;

    /** 用户积分（购物获得，可用于抵扣金额，100积分=1元） */
    private Integer points;

    /** 钱包余额（用户充值金额） */
    private BigDecimal wallet;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0=未删除，1=已删除 */
    @TableLogic
    private Integer deleted;
}
