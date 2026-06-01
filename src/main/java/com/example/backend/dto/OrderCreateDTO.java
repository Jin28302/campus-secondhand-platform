package com.example.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 创建订单请求DTO - 接收结算提交数据
 * 从购物车选中项创建订单
 */
@Data
public class OrderCreateDTO {

    /** 购物车项ID列表，不能为空（用户选中的要结算的商品） */
    @NotEmpty(message = "请选择购物车商品")
    private List<Long> cartIds;

    /** 是否使用积分抵扣（默认false） */
    private boolean usePoints;
}
