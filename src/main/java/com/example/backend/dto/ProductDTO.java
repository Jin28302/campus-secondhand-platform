package com.example.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {

    @NotBlank(message = "商品名称不能为空")
    private String name;

    @NotBlank(message = "分类不能为空")
    private String category;

    @NotNull(message = "原价不能为空")
    @DecimalMin(value = "0.01", message = "原价必须大于0")
    private BigDecimal originalPrice;

    @NotNull(message = "折扣价不能为空")
    @DecimalMin(value = "0.01", message = "折扣价必须大于0")
    private BigDecimal discountPrice;

    private String size;

    private String description;

    private Boolean allowBargain;

    @NotNull(message = "库存不能为空")
    @Min(value = 1, message = "库存至少为1")
    private Integer stock;

    @NotBlank(message = "新旧程度不能为空")
    private String newDegree;
}
