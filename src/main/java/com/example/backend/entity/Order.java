package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long sellerId;

    private BigDecimal totalAmount;

    private BigDecimal actualPay;

    private Integer pointsUsed;

    private String status;

    private Boolean reviewed;

    private Boolean merchantReviewed;

    private LocalDateTime payTime;

    private LocalDateTime confirmTime;

    private LocalDateTime returnDeadline;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
