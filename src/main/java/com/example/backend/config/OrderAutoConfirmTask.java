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

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAutoConfirmTask {

    private final OrderService orderService;
    private final UserService userService;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void autoConfirmOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(7);

        List<Order> overdueOrders = orderService.list(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, OrderStatus.PENDING.getValue())
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

    private void processAutoConfirm(Order order) {
        LocalDateTime now = LocalDateTime.now();

        // 自动确认收货
        order.setStatus(OrderStatus.COMPLETED.getValue());
        order.setConfirmTime(now);
        order.setReturnDeadline(now.plusHours(24));
        orderService.updateById(order);

        // 获取卖家信息，计算平台费率
        User seller = userService.getById(order.getSellerId());
        int sellerLevel = seller.getSellerLevel() != null ? seller.getSellerLevel() : 1;
        BigDecimal platformFee = PointsDeductUtil.getPlatformFee(order.getActualPay(), sellerLevel);
        BigDecimal sellerIncome = order.getActualPay().subtract(platformFee);

        // 货款打入卖家钱包
        userService.update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, order.getSellerId())
                .setSql("wallet = wallet + " + sellerIncome));

        log.info("订单[{}]自动确认完成，卖家入账{}，平台费{}",
                order.getOrderNo(), sellerIncome, platformFee);
    }
}
