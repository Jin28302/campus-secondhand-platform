package com.example.backend.controller;

import com.example.backend.common.R;
import com.example.backend.common.RequireRole;
import com.example.backend.dto.ProductDTO;
import com.example.backend.dto.ProductSearchDTO;
import com.example.backend.entity.Product;
import com.example.backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @RequireRole({"商家"})
    public R<Void> publish(@Valid ProductDTO dto,
                           @RequestParam(value = "images", required = false) MultipartFile[] images) {
        productService.publish(dto, images);
        return R.ok();
    }

    @PutMapping("/{id}/off")
    @RequireRole({"商家"})
    public R<Void> offShelf(@PathVariable Long id) {
        productService.offShelf(id);
        return R.ok();
    }

    @GetMapping("/seller/list")
    @RequireRole({"商家"})
    public R<List<Product>> sellerList() {
        return R.ok(productService.sellerList());
    }

    @GetMapping("/{id}")
    public R<Product> detail(@PathVariable Long id) {
        return R.ok(productService.detail(id));
    }

    @GetMapping("/search")
    public R<List<Product>> search(ProductSearchDTO dto) {
        return R.ok(productService.search(dto));
    }
}
