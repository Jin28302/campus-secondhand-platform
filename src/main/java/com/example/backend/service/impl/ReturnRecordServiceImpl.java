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

// 退货/退款服务实现 - 处理退货申请和审核
@Service
@RequiredArgsConstructor
public class ReturnRecordServiceImpl extends ServiceImpl<ReturnRecordMapper, ReturnRecord>
        implements ReturnRecordService {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ProductService productService;
    private final UserService userService;
    private final PointsLogService pointsLogService;

    // 申请退货/退款 - 仅已完成的订单可申请，需在24小时退货期限内
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyReturn(Long orderId, ReturnApplyDTO dto) {
        Long userId = UserContext.get().userId();
        Order order = orderService.getById(orderId);

        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        // 校验订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        // 只有"已完成"状态的订单可以申请退货
        if (!OrderStatus.COMPLETED.getValue().equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不允许退货");
        }
        // 检查是否在退货期限内（确认收货后24小时内）
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

        // 订单状态变更为"退款审核中"
        order.setStatus(OrderStatus.REFUND_PENDING.getValue());
        orderService.updateById(order);
    }

    // 审核退货/退款 - 同意则恢复库存、退款给买家、从卖家扣回收入并扣减积分
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long returnId, ReturnAuditDTO dto) {
        Long sellerId = UserContext.get().userId();
        ReturnRecord record = getById(returnId);

        if (record == null) {
            throw new BusinessException("退货记录不存在");
        }
        // 只能由该订单对应的卖家审核
        if (!record.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权审核此退货申请");
        }
        // 只能审核"待审核"状态的记录
        if (!"待审核".equals(record.getStatus())) {
            throw new BusinessException("该退货申请已处理");
        }

        Order order = orderService.getById(record.getOrderId());

        if ("同意".equals(dto.getResult())) {
            record.setStatus("同意");
            updateById(record);

            // 恢复库存：将订单中每个商品的库存加回
            List<com.example.backend.entity.OrderItem> items = orderItemService.listByOrderId(order.getId());
            for (com.example.backend.entity.OrderItem item : items) {
                productService.update(new LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, item.getProductId())
                        .setSql("stock = stock + " + item.getQuantity()));
            }

            // 退款给买家（全额退还实付金额）
            userService.update(new LambdaUpdateWrapper<User>()
                    .eq(User::getId, order.getUserId())
                    .setSql("wallet = wallet + " + order.getActualPay()));

            // 从卖家钱包扣回已到账的收入（实付金额减去平台费）
            User seller = userService.getById(order.getSellerId());
            int sellerLevel = seller.getSellerLevel() != null ? seller.getSellerLevel() : 1;
            // 计算平台手续费
            java.math.BigDecimal platformFee = com.example.backend.common.PointsDeductUtil
                    .getPlatformFee(order.getActualPay(), sellerLevel);
            // 卖家实际已到账金额 = 实付 - 平台费
            java.math.BigDecimal sellerIncome = order.getActualPay().subtract(platformFee);
            // 使用GREATEST确保余额不会扣成负数
            userService.update(new LambdaUpdateWrapper<User>()
                    .eq(User::getId, order.getSellerId())
                    .setSql("wallet = GREATEST(wallet - " + sellerIncome + ", 0)"));

            // 扣减买家下单时获得的积分（消费1元=1积分）
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

    // 按订单ID审核退货 - 先根据orderId查询退货记录，再调用audit逻辑
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditByOrderId(Long orderId, ReturnAuditDTO dto) {
        ReturnRecord record = getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ReturnRecord>()
                .eq(ReturnRecord::getOrderId, orderId)
                .eq(ReturnRecord::getStatus, "待审核"));
        if (record == null) throw new BusinessException("该订单没有待审核的退货记录");
        audit(record.getId(), dto);
    }
}
