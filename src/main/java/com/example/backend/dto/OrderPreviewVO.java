package com.example.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单预览视图对象 - 结算前展示订单汇总信息
 * 包含按商家分组的商品详情、总金额、可用积分、积分抵扣和最终支付金额
 * 兑换规则：100积分 = 1元
 */
@Data
public class OrderPreviewVO {

    /** 按商家分组的购物车商品列表 */
    private List<CartGroupVO> groups;

    /** 商品总金额（不含积分抵扣） */
    private BigDecimal totalAmount;

    /** 用户可用积分数量 */
    private int availablePoints;

    /** 积分可抵扣金额（availablePoints / 100） */
    private BigDecimal pointsDeduction;

    /** 最终应付金额（totalAmount - pointsDeduction） */
    private BigDecimal actualPay;
}
