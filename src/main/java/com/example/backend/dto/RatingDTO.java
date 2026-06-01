package com.example.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 评分/评价请求DTO - 接收评价表单数据
 * 用于商品评价和买家评价两种场景
 */
@Data
public class RatingDTO {

    /** 订单ID，不能为空 */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 评分（1-5星），不能为空，最低1分最高5分 */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1分")
    @Max(value = 5, message = "评分最高5分")
    private Integer score;

    /** 评价内容（可选） */
    private String content;
}
