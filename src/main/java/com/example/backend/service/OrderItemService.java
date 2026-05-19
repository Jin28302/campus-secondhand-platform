package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.entity.OrderItem;

import java.util.List;

public interface OrderItemService extends IService<OrderItem> {

    List<OrderItem> listByOrderId(Long orderId);
}
