package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.ProductImage;
import com.example.backend.mapper.ProductImageMapper;
import com.example.backend.service.ProductImageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductImageServiceImpl extends ServiceImpl<ProductImageMapper, ProductImage>
        implements ProductImageService {

    @Override
    public List<ProductImage> listByProductId(Long productId) {
        return list(new LambdaQueryWrapper<ProductImage>()
                .eq(ProductImage::getProductId, productId)
                .orderByAsc(ProductImage::getSort));
    }
}
