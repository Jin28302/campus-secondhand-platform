package com.example.backend.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.common.OrderStatus;
import com.example.backend.common.PointsDeductUtil;
import com.example.backend.entity.Order;
import com.example.backend.entity.User;
import com.example.backend.service.OrderService;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// 订单自动确认收货定时任务 - 每天凌晨2点自动确认超7天未确认的订单
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAutoConfirmTask {

    private final OrderService orderService;
    private final UserService userService;

    // 每天凌晨2点执行，自动确认超过7天未确认收货的订单
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void autoConfirmOrders() {
        // 截止时间为7天前
        LocalDateTime deadline = LocalDateTime.now().minusDays(7);

        // 查询待确认且付款时间超过7天的订单
        List<Order> overdueOrders = orderService.list(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, OrderStatus.SHIPPED.getValue())
                .lt(Order::getPayTime, deadline)
                .isNull(Order::getConfirmTime));

        log.info("自动确认收货任务：发现 {} 笔超时订单", overdueOrders.size());

        for (Order order : overdueOrders) {
            try {
                processAutoConfirm(order);
            } catch (Exception e) {
                log.error("自动确认订单[{}]失败: {}", order.getOrderNo(), e.getMessage());
            }
        }
    }

    // 处理单笔订单的自动确认 - 更新订单状态、计算平台费并打款给卖家
    private void processAutoConfirm(Order order) {
        LocalDateTime now = LocalDateTime.now();

        // 自动确认收货，状态变更为"已完成"
        order.setStatus(OrderStatus.COMPLETED.getValue());
        order.setConfirmTime(now);
        // 退货截止时间为确认后24小时
        order.setReturnDeadline(now.plusHours(24));
        orderService.updateById(order);

        // 获取卖家信息，根据卖家等级计算平台费率
        User seller = userService.getById(order.getSellerId());
        int sellerLevel = seller.getSellerLevel() != null ? seller.getSellerLevel() : 1;
        // 计算平台手续费
        BigDecimal platformFee = PointsDeductUtil.getPlatformFee(order.getActualPay(), sellerLevel);
        // 卖家实际收入 = 实付金额 - 平台费
        BigDecimal sellerIncome = order.getActualPay().subtract(platformFee);

        // 货款打入卖家钱包
        userService.update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, order.getSellerId())
                .setSql("wallet = wallet + " + sellerIncome));

        log.info("订单[{}]自动确认完成，卖家入账{}，平台费{}",
                order.getOrderNo(), sellerIncome, platformFee);
    }
}
