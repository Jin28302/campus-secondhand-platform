package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sellerId;

    private String shopName;

    private String name;

    private String category;

    private BigDecimal originalPrice;

    private BigDecimal discountPrice;

    private String size;

    private String images;

    private String description;

    private Boolean allowBargain;

    private Integer stock;

    private String newDegree;

    private String status;

    private Integer salesCount;

    private BigDecimal avgRating;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
