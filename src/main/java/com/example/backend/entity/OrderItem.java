package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单子项实体 - 对应 order_item 表
 * 一个订单可能包含多个商品，每个商品对应一个订单子项
 * 记录每个商品的购买单价、数量和金额
 */
@Data
@TableName("order_item")
public class OrderItem {

    /** 订单子项ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属订单ID */
    private Long orderId;

    /** 商品ID */
    private Long productId;

    /** 商品名称（下单时快照，不随商品修改而变化） */
    private String productName;

    /** 商品图片URL（下单时快照） */
    private String productImage;

    /** 购买单价 */
    private BigDecimal unitPrice;

    /** 购买数量 */
    private Integer quantity;

    /** 子项小计金额（单价 × 数量） */
    private BigDecimal subtotal;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
