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

@Service
@RequiredArgsConstructor
public class RatingServiceImpl extends ServiceImpl<ReviewMapper, Review> implements RatingService {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ProductService productService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rateSeller(RatingDTO dto) {
        Long userId = UserContext.get().userId();
        Order order = orderService.getById(dto.getOrderId());

        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getUserId().equals(userId)) throw new BusinessException("无权评价此订单");
        if (!"completed".equals(order.getStatus()) && !"refunded".equals(order.getStatus())) {
            throw new BusinessException("订单未完成，不能评价");
        }

        long existing = count(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, dto.getOrderId())
                .eq(Review::getReviewerId, userId)
                .eq(Review::getType, "product"));
        if (existing > 0) throw new BusinessException("已评价过该订单");

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
            updateProductAvgRating(item.getProductId());
        }

        order.setReviewed(true);
        orderService.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rateBuyer(RatingDTO dto) {
        Long sellerId = UserContext.get().userId();
        Order order = orderService.getById(dto.getOrderId());

        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getSellerId().equals(sellerId)) throw new BusinessException("无权评价此订单");
        if (!"completed".equals(order.getStatus()) && !"refunded".equals(order.getStatus())) {
            throw new BusinessException("订单未完成，不能评价");
        }

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

        order.setMerchantReviewed(true);
        orderService.updateById(order);
    }

    @Override
    public List<Review> productRatings(Long productId) {
        return list(new LambdaQueryWrapper<Review>()
                .eq(Review::getTargetId, productId)
                .eq(Review::getType, "product")
                .orderByDesc(Review::getCreatedAt));
    }

    private void updateProductAvgRating(Long productId) {
        List<Review> reviews = productRatings(productId);
        if (reviews.isEmpty()) return;

        long goodCount = reviews.stream().filter(r -> r.getRating() >= 4).count();
        BigDecimal avgRating = BigDecimal.valueOf(goodCount)
                .multiply(BigDecimal.valueOf(5))
                .divide(BigDecimal.valueOf(reviews.size()), 2, RoundingMode.HALF_UP);

        Product product = productService.getById(productId);
        product.setAvgRating(avgRating);
        productService.updateById(product);
    }
}
