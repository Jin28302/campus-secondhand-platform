package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.entity.ProductImage;

import java.util.List;

public interface ProductImageService extends IService<ProductImage> {

    List<ProductImage> listByProductId(Long productId);
}
