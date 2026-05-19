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

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public R<Void> add(@RequestBody @Valid CartDTO dto) {
        cartService.add(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestParam Integer quantity) {
        cartService.update(id, quantity);
        return R.ok();
    }

    @GetMapping
    public R<List<Cart>> list() {
        return R.ok(cartService.myCart());
    }

    @GetMapping("/grouped")
    public R<List<CartGroupVO>> grouped() {
        return R.ok(cartService.myCartGrouped());
    }

    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        cartService.remove(id);
        return R.ok();
    }
}
