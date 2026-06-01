package com.example.backend.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

// 积分抵扣和平台费率工具类
public final class PointsDeductUtil {

    // 积分兑换比例：每100积分可抵扣1元
    private static final int POINTS_PER_YUAN = 100;

    // 平台费率映射 - 卖家等级越高，平台手续费率越高
    // 等级1: 0.1%, 等级2: 0.2%, 等级3: 0.5%, 等级4: 0.75%, 等级5: 1%
    private static final Map<Integer, BigDecimal> FEE_RATE_MAP = Map.of(
            1, new BigDecimal("0.001"),
            2, new BigDecimal("0.002"),
            3, new BigDecimal("0.005"),
            4, new BigDecimal("0.0075"),
            5, new BigDecimal("0.01")
    );

    private PointsDeductUtil() {}

    // 计算积分可抵扣金额 - 每100积分抵扣1元，不超过订单金额
    // @param userPoints 用户当前积分
    // @param orderAmount 订单总金额
    // @return 实际可抵扣金额
    public static BigDecimal calcDeduction(int userPoints, BigDecimal orderAmount) {
        // 计算最大可抵扣金额 = 积分 / 100
        BigDecimal maxDeduct = BigDecimal.valueOf(userPoints / POINTS_PER_YUAN);
        // 取可抵扣金额与订单金额的较小值
        return maxDeduct.min(orderAmount);
    }

    // 计算抵扣金额对应的积分数 - 抵扣金额 * 100
    // @param deductAmount 抵扣金额（元）
    // @return 需要扣减的积分数
    public static int deductedPoints(BigDecimal deductAmount) {
        return deductAmount.intValue() * POINTS_PER_YUAN;
    }

    // 计算平台手续费 - 根据卖家等级和订单金额计算，保留2位小数（四舍五入）
    // @param amount 订单实付金额
    // @param sellerLevel 卖家等级（1-5）
    // @return 平台手续费
    public static BigDecimal getPlatformFee(BigDecimal amount, int sellerLevel) {
        // 根据等级取费率，默认等级1（0.1%）
        BigDecimal rate = FEE_RATE_MAP.getOrDefault(sellerLevel, new BigDecimal("0.001"));
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
