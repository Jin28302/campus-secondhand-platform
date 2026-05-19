package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("buyer_rating")
public class BuyerRating {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long buyerId;

    private Long sellerId;

    private Integer score;

    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
