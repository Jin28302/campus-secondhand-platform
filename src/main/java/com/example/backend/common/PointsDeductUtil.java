package com.example.backend.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public final class PointsDeductUtil {

    private static final int POINTS_PER_YUAN = 100;

    private static final Map<Integer, BigDecimal> FEE_RATE_MAP = Map.of(
            1, new BigDecimal("0.001"),
            2, new BigDecimal("0.002"),
            3, new BigDecimal("0.005"),
            4, new BigDecimal("0.0075"),
            5, new BigDecimal("0.01")
    );

    private PointsDeductUtil() {}

    /**
     * 计算积分可抵扣金额。每100积分抵扣1元，不超过订单金额。
     * 返回实际可抵扣金额。
     */
    public static BigDecimal calcDeduction(int userPoints, BigDecimal orderAmount) {
        BigDecimal maxDeduct = BigDecimal.valueOf(userPoints / POINTS_PER_YUAN);
        return maxDeduct.min(orderAmount);
    }

    public static int deductedPoints(BigDecimal deductAmount) {
        return deductAmount.intValue() * POINTS_PER_YUAN;
    }

    public static BigDecimal getPlatformFee(BigDecimal amount, int sellerLevel) {
        BigDecimal rate = FEE_RATE_MAP.getOrDefault(sellerLevel, new BigDecimal("0.001"));
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
