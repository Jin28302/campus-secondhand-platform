package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String password;

    private String name;

    private String phone;

    private String email;

    private String city;

    private Integer gender;

    private String bankAccount;

    private String role;

    private String status;

    private String licenseImg;

    private String idCardImg;

    private Integer sellerLevel;

    private Integer points;

    private BigDecimal wallet;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
