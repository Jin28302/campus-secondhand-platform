package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.PointsDeductUtil;
import com.example.backend.common.OrderStatus;
import com.example.backend.common.ProductStatus;
import com.example.backend.common.UserContext;
import com.example.backend.dto.CartGroupVO;
import com.example.backend.dto.OrderCreateDTO;
import com.example.backend.dto.OrderPreviewVO;
import com.example.backend.dto.OrderQueryDTO;
import com.example.backend.entity.Cart;
import com.example.backend.entity.Order;
import com.example.backend.entity.OrderItem;
import com.example.backend.entity.Product;
import com.example.backend.entity.User;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.OrderMapper;
import com.example.backend.service.CartService;
import com.example.backend.service.OrderItemService;
import com.example.backend.service.OrderService;
import com.example.backend.service.PointsLogService;
import com.example.backend.service.ProductService;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final CartService cartService;
    private final ProductService productService;
    private final UserService userService;
    private final PointsLogService pointsLogService;
    private final OrderItemService orderItemService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Order> createOrders(OrderCreateDTO dto) {
        Long userId = UserContext.get().userId();
        User user = userService.getById(userId);

        List<Cart> cartItems = cartService.listByIds(dto.getCartIds());
        if (cartItems.isEmpty()) {
            throw new BusinessException("购物车商品不存在");
        }

        // 校验购物车归属
        for (Cart cart : cartItems) {
            if (!cart.getUserId().equals(userId)) {
                throw new BusinessException("购物车数据异常");
            }
        }

        // 计算总金额
        BigDecimal totalPay = BigDecimal.ZERO;
        List<Order> orders = new ArrayList<>();
        List<OrderItem> orderItems = new ArrayList<>();

        for (Cart cart : cartItems) {
            Product product = productService.getById(cart.getProductId());
            if (product == null || !ProductStatus.PUBLISHED.getValue().equals(product.getStatus())) {
                throw new BusinessException("商品[" + cart.getProductId() + "]不可购买");
            }

            if (product.getStock() < cart.getQuantity()) {
                throw new BusinessException("商品[" + product.getName() + "]库存不足");
            }

            BigDecimal amount = product.getDiscountPrice()
                    .multiply(BigDecimal.valueOf(cart.getQuantity()));
            totalPay = totalPay.add(amount);

            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setUserId(userId);
            order.setSellerId(product.getSellerId());
            order.setTotalAmount(amount);
            order.setActualPay(amount);
            order.setPointsUsed(0);
            order.setStatus(OrderStatus.PENDING.getValue());
            order.setPayTime(LocalDateTime.now());
            orders.add(order);

            String firstImage = product.getImages() != null && !product.getImages().isBlank()
                    ? product.getImages().split(",")[0] : null;
            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(firstImage);
            item.setUnitPrice(product.getDiscountPrice());
            item.setQuantity(cart.getQuantity());
            item.setSubtotal(amount);
            orderItems.add(item);
        }

        // 钱包扣款
        if (user.getWallet().compareTo(totalPay) < 0) {
            throw new BusinessException("钱包余额不足");
        }

        boolean walletUpdated = userService.update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .ge(User::getWallet, totalPay)
                .set(User::getWallet, user.getWallet().subtract(totalPay)));

        if (!walletUpdated) {
            throw new BusinessException("扣款失败，余额不足");
        }

        // 乐观锁扣减库存
        for (Cart cart : cartItems) {
            boolean stockUpdated = productService.update(new LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, cart.getProductId())
                    .ge(Product::getStock, cart.getQuantity())
                    .setSql("stock = stock - " + cart.getQuantity())
                    .setSql("sales_count = sales_count + " + cart.getQuantity()));

            if (!stockUpdated) {
                throw new BusinessException("库存扣减失败，请重试");
            }
        }

        // 批量保存订单
        saveBatch(orders);

        // 关联 orderId 到明细并批量保存
        for (int i = 0; i < orders.size(); i++) {
            orderItems.get(i).setOrderId(orders.get(i).getId());
        }
        orderItemService.saveBatch(orderItems);

        // 计算积分：消费1元=1积分
        int earnedPoints = totalPay.intValue();
        userService.update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .setSql("points = points + " + earnedPoints));
        pointsLogService.record(userId, earnedPoints, "下单获得积分");

        // 清除已下单的购物车
        cartService.removeByIds(dto.getCartIds());

        return orders;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Long orderId) {
        Long userId = UserContext.get().userId();
        Order order = getById(orderId);

        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (!OrderStatus.PENDING.getValue().equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许确认收货");
        }

        LocalDateTime now = LocalDateTime.now();
        order.setStatus(OrderStatus.COMPLETED.getValue());
        order.setConfirmTime(now);
        order.setReturnDeadline(now.plusHours(24));
        updateById(order);
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "ORD" + timestamp + random;
    }

    @Override
    public OrderPreviewVO preview(OrderCreateDTO dto) {
        Long userId = UserContext.get().userId();
        User user = userService.getById(userId);

        List<Cart> cartItems = cartService.listByIds(dto.getCartIds());
        if (cartItems.isEmpty()) {
            throw new BusinessException("购物车商品不存在");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cart : cartItems) {
            Product product = productService.getById(cart.getProductId());
            if (product == null || !ProductStatus.PUBLISHED.getValue().equals(product.getStatus())) {
                throw new BusinessException("商品[" + cart.getProductId() + "]不可购买");
            }
            totalAmount = totalAmount.add(
                    product.getDiscountPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
        }

        BigDecimal deduction = PointsDeductUtil.calcDeduction(
                user.getPoints() != null ? user.getPoints() : 0, totalAmount);

        OrderPreviewVO vo = new OrderPreviewVO();
        vo.setGroups(cartService.myCartGrouped().stream()
                .filter(g -> g.getItems().stream()
                        .anyMatch(i -> dto.getCartIds().contains(i.getCartId())))
                .toList());
        vo.setTotalAmount(totalAmount);
        vo.setAvailablePoints(user.getPoints() != null ? user.getPoints() : 0);
        vo.setPointsDeduction(deduction);
        vo.setActualPay(totalAmount.subtract(deduction));
        return vo;
    }

    @Override
    public IPage<Order> myOrders(OrderQueryDTO dto) {
        Long userId = UserContext.get().userId();
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreatedAt);
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            wrapper.eq(Order::getStatus, dto.getStatus());
        }
        return page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
    }

    @Override
    public IPage<Order> sellerOrders(OrderQueryDTO dto) {
        Long sellerId = UserContext.get().userId();
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getSellerId, sellerId)
                .orderByDesc(Order::getCreatedAt);
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            wrapper.eq(Order::getStatus, dto.getStatus());
        }
        return page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
    }
}
