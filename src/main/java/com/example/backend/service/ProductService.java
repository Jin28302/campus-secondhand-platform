package com.example.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.dto.ProductDTO;
import com.example.backend.dto.ProductSearchDTO;
import com.example.backend.entity.Product;

import java.util.List;

public interface ProductService extends IService<Product> {

    void publish(ProductDTO dto);

    void update(Long id, ProductDTO dto);

    void offShelf(Long id);

    List<Product> sellerList();

    Product detail(Long id);

    IPage<Product> search(ProductSearchDTO dto);
}
