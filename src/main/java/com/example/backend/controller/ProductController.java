package com.example.backend.controller;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.common.R;
import com.example.backend.common.RequireRole;
import com.example.backend.dto.ProductDTO;
import com.example.backend.dto.ProductSearchDTO;
import com.example.backend.entity.Product;
import com.example.backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

// 商品控制器 - 处理商品发布、编辑、下架、搜索和详情查看
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 发布商品 - 仅商家和管理员可操作，新商品状态为"待审核"
    @PostMapping
    @RequireRole({"商家", "管理员"})
    public R<Void> publish(@RequestBody @Valid ProductDTO dto) {
        productService.publish(dto);
        return R.ok();
    }

    // 编辑商品 - 更新商品标题、描述、价格、图片等信息
    @PutMapping("/{id}")
    @RequireRole({"商家", "管理员"})
    public R<Void> update(@PathVariable Long id, @RequestBody @Valid ProductDTO dto) {
        productService.update(id, dto);
        return R.ok();
    }

    // 下架商品 - 将商品状态改为已下架，不再展示给买家
    @PutMapping("/{id}/off")
    @RequireRole({"商家", "管理员"})
    public R<Void> offShelf(@PathVariable Long id) {
        productService.offShelf(id);
        return R.ok();
    }

    // 获取当前商家的商品列表 - 商家查看自己发布的商品
    @GetMapping("/seller/list")
    @RequireRole({"商家", "管理员"})
    public R<List<Product>> sellerList() {
        return R.ok(productService.sellerList());
    }

    // 商品详情 - 公开接口，根据商品ID查看详细信息
    @GetMapping("/{id}")
    public R<Product> detail(@PathVariable Long id) {
        return R.ok(productService.detail(id));
    }

    // 商品搜索 - 支持关键词、分类、排序等条件的分页查询
    @GetMapping("/search")
    public R<IPage<Product>> search(ProductSearchDTO dto) {
        return R.ok(productService.search(dto));
    }
}
