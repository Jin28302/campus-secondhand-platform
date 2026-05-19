package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product_images")
public class ProductImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private String url;

    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
