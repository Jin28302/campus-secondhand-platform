package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体 - 对应 order 表
 * 包含订单基本信息、金额、支付方式、状态等字段
 * 订单状态：pending（待收货）、completed（已完成）、refund_pending（退货待审核）、refunded（已退货）、refund_rejected（退货被拒）
 * 表名使用反引号括起，因为 order 是 MySQL 保留字
 */
@Data
@TableName("`order`")
public class Order {

    /** 订单ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号（业务编号，唯一标识） */
    private String orderNo;

    /** 买家用户ID */
    private Long userId;

    /** 卖家（商家）用户ID */
    private Long sellerId;

    /** 商品总金额（未扣除积分） */
    private BigDecimal totalAmount;

    /** 实际支付金额（扣除积分抵扣后） */
    private BigDecimal actualPay;

    /** 使用的积分数量 */
    private Integer pointsUsed;

    /** 订单状态：pending/completed/refund_pending/refunded/refund_rejected */
    private String status;

    /** 买家是否已评价（true=已评价） */
    private Boolean reviewed;

    /** 商家是否已评价买家（true=已评价） */
    private Boolean merchantReviewed;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 确认收货时间 */
    private LocalDateTime confirmTime;

    /** 退货截止日期（确认收货后+N天） */
    private LocalDateTime returnDeadline;

    /** 创建时间（自动填充，即下单时间） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0=未删除，1=已删除 */
    @TableLogic
    private Integer deleted;
}
