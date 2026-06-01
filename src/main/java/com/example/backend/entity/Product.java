package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体 - 对应 product 表
 * 包含商品基本信息、价格、库存、新旧程度、状态等字段
 * 商品状态：pending（待审核）、published（在售）、off_shelf（已下架）
 * 新旧程度：全新、几乎全新、轻微使用、明显使用
 */
@Data
@TableName("product")
public class Product {

    /** 商品ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 卖家（商家）用户ID */
    private Long sellerId;

    /** 店铺名称 */
    private String shopName;

    /** 商品名称 */
    private String name;

    /** 商品分类：书籍教材、电子产品、生活好物、代步出行、学习办公、其他 */
    private String category;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 折扣价（实际售价取折扣价，若无则取原价） */
    private BigDecimal discountPrice;

    /** 商品尺寸（可选） */
    private String size;

    /** 商品图片URL列表（逗号分隔的字符串） */
    private String images;

    /** 商品描述/说明（可选） */
    private String description;

    /** 是否允许议价 */
    private Boolean allowBargain;

    /** 库存数量 */
    private Integer stock;

    /** 新旧程度：全新、几乎全新、轻微使用、明显使用 */
    private String newDegree;

    /** 商品状态：pending（待审核）、published（在售）、off_shelf（已下架） */
    private String status;

    /** 累计销量 */
    private Integer salesCount;

    /** 平均评分（1-5分） */
    private BigDecimal avgRating;

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
