package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.UserContext;
import com.example.backend.dto.CartDTO;
import com.example.backend.dto.CartGroupVO;
import com.example.backend.entity.Cart;
import com.example.backend.entity.Product;
import com.example.backend.mapper.CartMapper;
import com.example.backend.service.CartService;
import com.example.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// 购物车服务实现 - 处理购物车添加、修改、分组查询、删除
@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    // 使用@Lazy延迟注入，解决与ProductService的循环依赖
    @Lazy
    private final ProductService productService;

    // 添加到购物车 - 若同一商品已存在则累加数量，否则新增记录
    @Override
    public void add(CartDTO dto) {
        Long userId = UserContext.get().userId();

        // 检查购物车中是否已有同一商品
        Cart existing = getOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, dto.getProductId()));

        if (existing != null) {
            // 已存在则累加数量
            existing.setQuantity(existing.getQuantity() + dto.getQuantity());
            updateById(existing);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(dto.getProductId());
            cart.setQuantity(dto.getQuantity());
            save(cart);
        }
    }

    // 修改购物车商品数量 - 数量<=0时自动删除记录
    @Override
    public void update(Long cartId, Integer quantity) {
        Cart cart = getById(cartId);
        if (cart == null) return;
        if (quantity <= 0) {
            removeById(cartId);
        } else {
            cart.setQuantity(quantity);
            updateById(cart);
        }
    }

    // 查看我的购物车 - 返回当前用户的所有购物车记录，按创建时间倒序
    @Override
    public List<Cart> myCart() {
        Long userId = UserContext.get().userId();
        return list(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCreatedAt));
    }

    // 按商家分组查看购物车 - 将购物车商品按卖家ID分组，便于前端按店铺展示
    @Override
    public List<CartGroupVO> myCartGrouped() {
        List<Cart> carts = myCart();
        if (carts.isEmpty()) return List.of();

        // 使用LinkedHashMap保持分组插入顺序
        Map<Long, CartGroupVO> groupMap = new LinkedHashMap<>();
        for (Cart cart : carts) {
            Product product = productService.getById(cart.getProductId());
            if (product == null) continue;

            // computeIfAbsent：若分组不存在则创建新分组
            CartGroupVO group = groupMap.computeIfAbsent(product.getSellerId(), k -> {
                CartGroupVO vo = new CartGroupVO();
                vo.setSellerId(product.getSellerId());
                vo.setShopName(product.getShopName());
                vo.setItems(new ArrayList<>());
                return vo;
            });
            group.getItems().add(CartGroupVO.CartItemVO.of(cart, product));
        }
        return new ArrayList<>(groupMap.values());
    }

    // 删除购物车商品 - 校验归属权限后才能删除
    @Override
    public void remove(Long cartId) {
        Cart cart = getById(cartId);
        if (cart == null) return;
        // 校验归属：只能删除自己的购物车记录
        Long userId = UserContext.get().userId();
        if (!cart.getUserId().equals(userId)) {
            throw new com.example.backend.exception.BusinessException("无权删除此购物车项");
        }
        removeById(cartId);
    }
}
