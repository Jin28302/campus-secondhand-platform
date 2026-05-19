package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.dto.CartDTO;
import com.example.backend.dto.CartGroupVO;
import com.example.backend.entity.Cart;

import java.util.List;

public interface CartService extends IService<Cart> {

    void add(CartDTO dto);

    void update(Long cartId, Integer quantity);

    List<Cart> myCart();

    List<CartGroupVO> myCartGrouped();

    void remove(Long cartId);
}
