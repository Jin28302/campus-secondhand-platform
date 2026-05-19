package com.example.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.common.R;
import com.example.backend.common.RequireRole;
import com.example.backend.dto.OrderCreateDTO;
import com.example.backend.dto.OrderPreviewVO;
import com.example.backend.dto.OrderQueryDTO;
import com.example.backend.entity.Order;
import com.example.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/preview")
    public R<OrderPreviewVO> preview(@RequestBody @Valid OrderCreateDTO dto) {
        return R.ok(orderService.preview(dto));
    }

    @PostMapping("/create")
    public R<List<Order>> create(@RequestBody @Valid OrderCreateDTO dto) {
        return R.ok(orderService.createOrders(dto));
    }

    @PutMapping("/{id}/confirm")
    public R<Void> confirm(@PathVariable Long id) {
        orderService.confirmReceive(id);
        return R.ok();
    }

    @GetMapping("/list")
    public R<IPage<Order>> myOrders(OrderQueryDTO dto) {
        return R.ok(orderService.myOrders(dto));
    }

    @GetMapping("/seller/list")
    @RequireRole({"商家"})
    public R<IPage<Order>> sellerOrders(OrderQueryDTO dto) {
        return R.ok(orderService.sellerOrders(dto));
    }
}
