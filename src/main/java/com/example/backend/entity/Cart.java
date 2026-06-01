package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车实体 - 对应 cart 表
 * 每个用户对应多条记录，每条记录表示用户添加的一个商品及其数量
 * 同一用户+同一商品只能有一条记录（即数量累加，非新增记录）
 */
@Data
@TableName("cart")
public class Cart {

    /** 购物车记录ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 商品ID */
    private Long productId;

    /** 商品数量 */
    private Integer quantity;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
