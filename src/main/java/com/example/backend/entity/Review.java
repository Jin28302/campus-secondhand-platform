package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价实体 - 对应 reviews 表
 * 支持两种评价类型：
 * - seller：商家对商品的评价（也可以是买家对商品的评价）
 * - buyer：商家对买家的评价
 * reviewerId 是评价者（谁评价），targetId 是被评价对象（商品ID 或 买家ID）
 */
@Data
@TableName("reviews")
public class Review {

    /** 评价ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的订单ID */
    private Long orderId;

    /** 评价者用户ID（谁发起的评价） */
    private Long reviewerId;

    /** 被评价对象ID（可能是商品ID 或 买家ID，取决于 type） */
    private Long targetId;

    /** 评价类型：seller（商品评价）、buyer（买家评价） */
    private String type;

    /** 评分（1-5星） */
    private Integer rating;

    /** 评价内容（文本） */
    private String content;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
