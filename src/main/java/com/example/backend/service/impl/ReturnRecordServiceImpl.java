package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.OrderStatus;
import com.example.backend.common.UserContext;
import com.example.backend.dto.ReturnApplyDTO;
import com.example.backend.dto.ReturnAuditDTO;
import com.example.backend.entity.Order;
import com.example.backend.entity.Product;
import com.example.backend.entity.ReturnRecord;
import com.example.backend.entity.User;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.ReturnRecordMapper;
import com.example.backend.service.OrderService;
import com.example.backend.service.OrderItemService;
import com.example.backend.service.PointsLogService;
import com.example.backend.service.ProductService;
import com.example.backend.service.ReturnRecordService;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnRecordServiceImpl extends ServiceImpl<ReturnRecordMapper, ReturnRecord>
        implements ReturnRecordService {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ProductService productService;
    private final UserService userService;
    private final PointsLogService pointsLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyReturn(Long orderId, ReturnApplyDTO dto) {
        Long userId = UserContext.get().userId();
        Order order = orderService.getById(orderId);

        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (!OrderStatus.COMPLETED.getValue().equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不允许退货");
        }
        if (order.getReturnDeadline() == null || LocalDateTime.now().isAfter(order.getReturnDeadline())) {
            throw new BusinessException("已超过退货期限（确认收货后24小时内）");
        }

        ReturnRecord record = new ReturnRecord();
        record.setOrderId(orderId);
        record.setUserId(userId);
        record.setSellerId(order.getSellerId());
        record.setReason(dto.getReason());
        record.setStatus("待审核");
        record.setApplyTime(LocalDateTime.now());
        save(record);

        order.setStatus(OrderStatus.REFUND_PENDING.getValue());
        orderService.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long returnId, ReturnAuditDTO dto) {
        Long sellerId = UserContext.get().userId();
        ReturnRecord record = getById(returnId);

        if (record == null) {
            throw new BusinessException("退货记录不存在");
        }
        if (!record.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权审核此退货申请");
        }
        if (!"待审核".equals(record.getStatus())) {
            throw new BusinessException("该退货申请已处理");
        }

        Order order = orderService.getById(record.getOrderId());

        if ("同意".equals(dto.getResult())) {
            record.setStatus("同意");
            updateById(record);

            List<com.example.backend.entity.OrderItem> items = orderItemService.listByOrderId(order.getId());
            for (com.example.backend.entity.OrderItem item : items) {
                productService.update(new LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, item.getProductId())
                        .setSql("stock = stock + " + item.getQuantity()));
            }

            userService.update(new LambdaUpdateWrapper<User>()
                    .eq(User::getId, order.getUserId())
                    .setSql("wallet = wallet + " + order.getActualPay()));

            int deductPoints = order.getActualPay().intValue();
            userService.update(new LambdaUpdateWrapper<User>()
                    .eq(User::getId, order.getUserId())
                    .setSql("points = GREATEST(points - " + deductPoints + ", 0)"));
            pointsLogService.record(order.getUserId(), -deductPoints, "退货扣减积分");

            order.setStatus(OrderStatus.REFUNDED.getValue());
            orderService.updateById(order);

        } else if ("拒绝".equals(dto.getResult())) {
            record.setStatus("拒绝");
            record.setRejectReason(dto.getRejectReason());
            updateById(record);

            order.setStatus(OrderStatus.REFUND_REJECTED.getValue());
            orderService.updateById(order);

        } else {
            throw new BusinessException("审核结果只能为 同意 或 拒绝");
        }
    }
}
