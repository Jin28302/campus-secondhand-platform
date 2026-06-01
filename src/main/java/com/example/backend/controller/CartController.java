package com.example.backend.controller;

import com.example.backend.common.R;
import com.example.backend.dto.CartDTO;
import com.example.backend.dto.CartGroupVO;
import com.example.backend.entity.Cart;
import com.example.backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 购物车控制器 - 处理购物车添加、修改数量、查看、删除
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // 添加到购物车 - 若同一商品已存在则增加数量，否则新增记录
    @PostMapping
    public R<Void> add(@RequestBody @Valid CartDTO dto) {
        cartService.add(dto);
        return R.ok();
    }

    // 修改购物车商品数量 - 数量大于0则更新，等于0则自动删除
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestParam Integer quantity) {
        cartService.update(id, quantity);
        return R.ok();
    }

    // 查看购物车 - 返回当前用户购物车中的所有商品列表
    @GetMapping
    public R<List<Cart>> list() {
        return R.ok(cartService.myCart());
    }

    // 按商家分组查看购物车 - 将购物车商品按商家分组，便于展示和结算
    @GetMapping("/grouped")
    public R<List<CartGroupVO>> grouped() {
        return R.ok(cartService.myCartGrouped());
    }

    // 删除购物车商品 - 移除指定记录
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        cartService.remove(id);
        return R.ok();
    }
}
