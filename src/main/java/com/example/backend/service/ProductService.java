package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.dto.ProductDTO;
import com.example.backend.dto.ProductSearchDTO;
import com.example.backend.entity.Product;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService extends IService<Product> {

    void publish(ProductDTO dto, MultipartFile[] images);

    void offShelf(Long id);

    List<Product> sellerList();

    Product detail(Long id);

    List<Product> search(ProductSearchDTO dto);
}
