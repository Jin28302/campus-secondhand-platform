package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.UserContext;
import com.example.backend.dto.RatingDTO;
import com.example.backend.entity.Order;
import com.example.backend.entity.OrderItem;
import com.example.backend.entity.Product;
import com.example.backend.entity.Review;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.ReviewMapper;
import com.example.backend.service.OrderItemService;
import com.example.backend.service.OrderService;
import com.example.backend.service.ProductService;
import com.example.backend.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

// 评价服务实现 - 处理买家评价卖家、卖家评价买家、商品评价查询
@Service
@RequiredArgsConstructor
public class RatingServiceImpl extends ServiceImpl<ReviewMapper, Review> implements RatingService {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ProductService productService;

    // 买家评价卖家 - 对订单中每个商品创建评价，并更新商品均分
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rateSeller(RatingDTO dto) {
        Long userId = UserContext.get().userId();
        Order order = orderService.getById(dto.getOrderId());

        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getUserId().equals(userId)) throw new BusinessException("无权评价此订单");
        // 只有已完成或已退款的订单可以评价
        if (!"completed".equals(order.getStatus()) && !"refunded".equals(order.getStatus())) {
            throw new BusinessException("订单未完成，不能评价");
        }

        // 防止重复评价：同一订单同一用户只能评价一次
        long existing = count(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, dto.getOrderId())
                .eq(Review::getReviewerId, userId)
                .eq(Review::getType, "product"));
        if (existing > 0) throw new BusinessException("已评价过该订单");

        // 为订单中的每个商品创建一条评价
        List<OrderItem> items = orderItemService.listByOrderId(order.getId());
        for (OrderItem item : items) {
            Review review = new Review();
            review.setOrderId(dto.getOrderId());
            review.setReviewerId(userId);
            review.setTargetId(item.getProductId());
            review.setType("product");
            review.setRating(dto.getScore());
            review.setContent(dto.getContent());
            save(review);
            // 更新商品平均评分
            updateProductAvgRating(item.getProductId());
        }

        // 标记订单已被买家评价
        order.setReviewed(true);
        orderService.updateById(order);
    }

    // 卖家评价买家 - 商家对买家进行评价
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rateBuyer(RatingDTO dto) {
        Long sellerId = UserContext.get().userId();
        Order order = orderService.getById(dto.getOrderId());

        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getSellerId().equals(sellerId)) throw new BusinessException("无权评价此订单");
        // 只有已完成或已退款的订单可以评价
        if (!"completed".equals(order.getStatus()) && !"refunded".equals(order.getStatus())) {
            throw new BusinessException("订单未完成，不能评价");
        }

        // 防止重复评价：同一订单同一商家只能评价一次
        long existing = count(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, dto.getOrderId())
                .eq(Review::getReviewerId, sellerId)
                .eq(Review::getType, "buyer"));
        if (existing > 0) throw new BusinessException("已评价过该买家");

        Review review = new Review();
        review.setOrderId(dto.getOrderId());
        review.setReviewerId(sellerId);
        review.setTargetId(order.getUserId());
        review.setType("buyer");
        review.setRating(dto.getScore());
        review.setContent(dto.getContent());
        save(review);

        // 标记订单已被商家评价
        order.setMerchantReviewed(true);
        orderService.updateById(order);
    }

    // 商品评价列表 - 查询指定商品的所有评价，按创建时间倒序
    @Override
    public List<Review> productRatings(Long productId) {
        return list(new LambdaQueryWrapper<Review>()
                .eq(Review::getTargetId, productId)
                .eq(Review::getType, "product")
                .orderByDesc(Review::getCreatedAt));
    }

    // 更新商品平均评分 - 统计该商品4分及以上的好评占比，乘以5得到均分
    private void updateProductAvgRating(Long productId) {
        List<Review> reviews = productRatings(productId);
        if (reviews.isEmpty()) return;

        // 好评数：评分>=4的评价数量
        long goodCount = reviews.stream().filter(r -> r.getRating() >= 4).count();
        // 均分 = (好评数 / 总评价数) * 5，保留2位小数，四舍五入
        BigDecimal avgRating = BigDecimal.valueOf(goodCount)
                .multiply(BigDecimal.valueOf(5))
                .divide(BigDecimal.valueOf(reviews.size()), 2, RoundingMode.HALF_UP);

        Product product = productService.getById(productId);
        product.setAvgRating(avgRating);
        productService.updateById(product);
    }
}
