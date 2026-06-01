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

// 订单服务实现 - 处理订单创建、确认收货、预览和查询
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final CartService cartService;
    private final ProductService productService;
    private final UserService userService;
    private final PointsLogService pointsLogService;
    private final OrderItemService orderItemService;

    // 创建订单 - 从事务性：校验库存、计算金额、积分抵扣、余额扣款、扣减库存、清除购物车
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Order> createOrders(OrderCreateDTO dto) {
        Long userId = UserContext.get().userId();
        User user = userService.getById(userId);

        List<Cart> cartItems = cartService.listByIds(dto.getCartIds());
        if (cartItems.isEmpty()) {
            throw new BusinessException("购物车商品不存在");
        }

        // 校验购物车归属，防止操作他人购物车
        for (Cart cart : cartItems) {
            if (!cart.getUserId().equals(userId)) {
                throw new BusinessException("购物车数据异常");
            }
        }

        // 遍历购物车计算每个商品的小计金额
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

            // 计算该商品金额 = 折扣价 * 数量
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

            // 取商品第一张图片作为快照
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

        // 积分抵扣 - 根据积分规则计算可抵扣金额
        BigDecimal pointsDeduction = BigDecimal.ZERO;
        if (dto.isUsePoints() && user.getPoints() != null && user.getPoints() > 0) {
            pointsDeduction = PointsDeductUtil.calcDeduction(user.getPoints(), totalPay);
        }
        BigDecimal actualTotal = totalPay.subtract(pointsDeduction);

        // 钱包扣款 - 使用乐观锁（余额 >= 实付金额）确保不超扣
        if (user.getWallet().compareTo(actualTotal) < 0) {
            throw new BusinessException("钱包余额不足");
        }

        boolean walletUpdated = userService.update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .ge(User::getWallet, actualTotal)  // 乐观锁：当前余额 >= 待扣金额
                .set(User::getWallet, user.getWallet().subtract(actualTotal)));

        if (!walletUpdated) {
            throw new BusinessException("扣款失败，余额不足");
        }

        // 扣减积分 - 使用乐观锁确保积分足够
        if (pointsDeduction.compareTo(BigDecimal.ZERO) > 0) {
            int usedPoints = PointsDeductUtil.deductedPoints(pointsDeduction);
            userService.update(new LambdaUpdateWrapper<User>()
                    .eq(User::getId, userId)
                    .ge(User::getPoints, usedPoints)  // 乐观锁：当前积分 >= 待扣积分
                    .setSql("points = points - " + usedPoints));
            // 记录积分消耗日志
            pointsLogService.record(userId, -usedPoints, "积分抵扣");
        }

        // 乐观锁扣减库存 - 使用库存 >= 购买数量作为条件
        for (Cart cart : cartItems) {
            boolean stockUpdated = productService.update(new LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, cart.getProductId())
                    .ge(Product::getStock, cart.getQuantity())  // 乐观锁：库存 >= 购买数量
                    .setSql("stock = stock - " + cart.getQuantity())
                    .setSql("sales_count = sales_count + " + cart.getQuantity()));

            if (!stockUpdated) {
                throw new BusinessException("库存扣减失败，请重试");
            }
        }

        // 批量保存订单，保存后订单获得ID
        saveBatch(orders);

        // 关联 orderId 到明细并批量保存
        for (int i = 0; i < orders.size(); i++) {
            orderItems.get(i).setOrderId(orders.get(i).getId());
        }
        orderItemService.saveBatch(orderItems);

        // 积分奖励：消费1元获得1积分
        int earnedPoints = totalPay.intValue();
        userService.update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .setSql("points = points + " + earnedPoints));
        pointsLogService.record(userId, earnedPoints, "下单获得积分");

        // 清除已下单的购物车记录
        cartService.removeByIds(dto.getCartIds());

        return orders;
    }

    // 商家发货 - 将订单状态从待发货(pending)变为待收货(shipped)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long orderId) {
        Long sellerId = UserContext.get().userId();
        Order order = getById(orderId);

        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getSellerId().equals(sellerId)) throw new BusinessException("无权操作此订单");
        if (!OrderStatus.PENDING.getValue().equals(order.getStatus())) throw new BusinessException("当前状态不允许发货");

        order.setStatus(OrderStatus.SHIPPED.getValue());
        updateById(order);
    }

    // 确认收货 - 买家确认后订单完成，按商家等级扣除平台费后打款给卖家
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
        // 只有"待收货(shipped)"状态才能确认收货
        if (!OrderStatus.SHIPPED.getValue().equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许确认收货");
        }

        LocalDateTime now = LocalDateTime.now();
        order.setStatus(OrderStatus.COMPLETED.getValue());
        order.setConfirmTime(now);
        // 退货截止时间为确认收货后24小时
        order.setReturnDeadline(now.plusHours(24));
        updateById(order);

        // 货款打入卖家钱包，扣除平台费（平台费比例与卖家等级相关）
        User seller = userService.getById(order.getSellerId());
        int sellerLevel = seller.getSellerLevel() != null ? seller.getSellerLevel() : 1;
        // 根据卖家等级计算平台手续费
        BigDecimal platformFee = PointsDeductUtil.getPlatformFee(order.getActualPay(), sellerLevel);
        // 卖家实际收入 = 买家实付 - 平台费
        BigDecimal sellerIncome = order.getActualPay().subtract(platformFee);
        userService.update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, order.getSellerId())
                .setSql("wallet = wallet + " + sellerIncome));
    }

    // 生成订单号 - 格式：ORD + 年月日时分秒 + 4位随机数
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "ORD" + timestamp + random;
    }

    // 订单预览 - 下单前计算总价、积分抵扣、实付金额，不实际创建订单
    @Override
    public OrderPreviewVO preview(OrderCreateDTO dto) {
        Long userId = UserContext.get().userId();
        User user = userService.getById(userId);

        List<Cart> cartItems = cartService.listByIds(dto.getCartIds());
        if (cartItems.isEmpty()) {
            throw new BusinessException("购物车商品不存在");
        }

        // 计算选中商品的合计金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cart : cartItems) {
            Product product = productService.getById(cart.getProductId());
            if (product == null || !ProductStatus.PUBLISHED.getValue().equals(product.getStatus())) {
                throw new BusinessException("商品[" + cart.getProductId() + "]不可购买");
            }
            totalAmount = totalAmount.add(
                    product.getDiscountPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
        }

        // 计算积分可抵扣金额
        BigDecimal deduction = PointsDeductUtil.calcDeduction(
                user.getPoints() != null ? user.getPoints() : 0, totalAmount);

        OrderPreviewVO vo = new OrderPreviewVO();
        // 仅返回选中的购物车分组数据
        vo.setGroups(cartService.myCartGrouped().stream()
                .filter(g -> g.getItems().stream()
                        .anyMatch(i -> dto.getCartIds().contains(i.getCartId())))
                .toList());
        vo.setTotalAmount(totalAmount);
        vo.setAvailablePoints(user.getPoints() != null ? user.getPoints() : 0);
        vo.setPointsDeduction(deduction);
        // 实付金额 = 合计金额 - 积分抵扣
        vo.setActualPay(totalAmount.subtract(deduction));
        return vo;
    }

    // 我的订单 - 买家分页查看自己的订单，支持按状态筛选
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

    // 商家订单 - 商家分页查看与自己商品相关的订单
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
