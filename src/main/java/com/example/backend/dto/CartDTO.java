package com.example.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 购物车请求DTO - 接收添加商品到购物车的数据
 */
@Data
public class CartDTO {

    /** 商品ID，不能为空 */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 购买数量，不能为空，至少为1 */
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;
}
