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

// 订单控制器 - 处理订单创建、确认收货、订单查询
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 订单预览 - 下单前预览，计算总价、积分抵扣等，不实际创建订单
    @PostMapping("/preview")
    public R<OrderPreviewVO> preview(@RequestBody @Valid OrderCreateDTO dto) {
        return R.ok(orderService.preview(dto));
    }

    // 创建订单 - 从购物车生成订单，扣除余额和积分，扣减库存
    @PostMapping("/create")
    public R<List<Order>> create(@RequestBody @Valid OrderCreateDTO dto) {
        return R.ok(orderService.createOrders(dto));
    }

    // 商家发货 - 商家确认发货后订单进入待收货状态
    @PutMapping("/{id}/ship")
    @RequireRole({"商家", "管理员"})
    public R<Void> ship(@PathVariable Long id) {
        orderService.shipOrder(id);
        return R.ok();
    }

    // 确认收货 - 买家确认收到商品后订单进入已完成状态
    @PutMapping("/{id}/confirm")
    public R<Void> confirm(@PathVariable Long id) {
        orderService.confirmReceive(id);
        return R.ok();
    }

    // 我的订单 - 买家查看自己的订单列表，支持分页和状态筛选
    @GetMapping("/list")
    public R<IPage<Order>> myOrders(OrderQueryDTO dto) {
        return R.ok(orderService.myOrders(dto));
    }

    // 商家订单 - 商家或管理员查看其商品相关的订单
    @GetMapping("/seller/list")
    @RequireRole({"商家", "管理员"})
    public R<IPage<Order>> sellerOrders(OrderQueryDTO dto) {
        return R.ok(orderService.sellerOrders(dto));
    }
}
