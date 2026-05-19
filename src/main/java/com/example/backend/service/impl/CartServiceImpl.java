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

@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Lazy
    private final ProductService productService;

    @Override
    public void add(CartDTO dto) {
        Long userId = UserContext.get().userId();

        Cart existing = getOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, dto.getProductId()));

        if (existing != null) {
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

    @Override
    public List<Cart> myCart() {
        Long userId = UserContext.get().userId();
        return list(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCreatedAt));
    }

    @Override
    public List<CartGroupVO> myCartGrouped() {
        List<Cart> carts = myCart();
        if (carts.isEmpty()) return List.of();

        Map<Long, CartGroupVO> groupMap = new LinkedHashMap<>();
        for (Cart cart : carts) {
            Product product = productService.getById(cart.getProductId());
            if (product == null) continue;

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

    @Override
    public void remove(Long cartId) {
        Cart cart = getById(cartId);
        if (cart == null) return;
        Long userId = UserContext.get().userId();
        if (!cart.getUserId().equals(userId)) {
            throw new com.example.backend.exception.BusinessException("无权删除此购物车项");
        }
        removeById(cartId);
    }
}
