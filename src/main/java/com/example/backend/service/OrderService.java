package com.example.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.dto.OrderCreateDTO;
import com.example.backend.dto.OrderPreviewVO;
import com.example.backend.dto.OrderQueryDTO;
import com.example.backend.entity.Order;

import java.util.List;

public interface OrderService extends IService<Order> {

    List<Order> createOrders(OrderCreateDTO dto);

    void shipOrder(Long orderId);

    void confirmReceive(Long orderId);

    OrderPreviewVO preview(OrderCreateDTO dto);

    IPage<Order> myOrders(OrderQueryDTO dto);

    IPage<Order> sellerOrders(OrderQueryDTO dto);
}
