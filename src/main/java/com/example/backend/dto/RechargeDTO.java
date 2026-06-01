package com.example.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 充值请求DTO - 接收用户钱包充值数据
 * 管理端可为指定用户充值
 */
@Data
public class RechargeDTO {

    /** 用户ID，不能为空 */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 充值金额，不能为空，必须大于0 */
    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    private BigDecimal amount;
}
