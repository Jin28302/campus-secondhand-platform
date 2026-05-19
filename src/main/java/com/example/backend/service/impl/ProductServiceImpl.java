package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.ProductStatus;
import com.example.backend.common.UserContext;
import com.example.backend.dto.ProductDTO;
import com.example.backend.dto.ProductSearchDTO;
import com.example.backend.entity.Product;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.ProductMapper;
import com.example.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public void publish(ProductDTO dto, MultipartFile[] images) {
        UserContext.UserInfo currentUser = UserContext.get();

        List<String> paths = new ArrayList<>();
        if (images != null) {
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                    File dest = new File(uploadPath + filename);
                    dest.getParentFile().mkdirs();
                    try {
                        file.transferTo(dest);
                    } catch (IOException e) {
                        throw new BusinessException("图片上传失败");
                    }
                    paths.add(filename);
                }
            }
        }

        Product product = new Product();
        product.setSellerId(currentUser.userId());
        product.setShopName(currentUser.phone());
        product.setName(dto.getName());
        product.setCategory(dto.getCategory());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setDiscountPrice(dto.getDiscountPrice());
        product.setSize(dto.getSize());
        product.setImages(String.join(",", paths));
        product.setDescription(dto.getDescription());
        product.setAllowBargain(dto.getAllowBargain());
        product.setStock(dto.getStock());
        product.setNewDegree(dto.getNewDegree());
        product.setStatus(ProductStatus.PENDING.getValue());
        product.setSalesCount(0);
        product.setAvgRating(BigDecimal.ZERO);

        save(product);
    }

    @Override
    public void offShelf(Long id) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        UserContext.UserInfo currentUser = UserContext.get();
        if (!product.getSellerId().equals(currentUser.userId())) {
            throw new BusinessException("只能下架自己的商品");
        }

        product.setStatus(ProductStatus.OFF_SHELF.getValue());
        updateById(product);
    }

    @Override
    public List<Product> sellerList() {
        UserContext.UserInfo currentUser = UserContext.get();
        return list(new LambdaQueryWrapper<Product>()
                .eq(Product::getSellerId, currentUser.userId())
                .orderByDesc(Product::getCreatedAt));
    }

    @Override
    public Product detail(Long id) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        return product;
    }

    @Override
    public List<Product> search(ProductSearchDTO dto) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", ProductStatus.PUBLISHED.getValue());

        if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
            wrapper.and(w -> w
                    .like("name", dto.getKeyword())
                    .or()
                    .like("description", dto.getKeyword()));
        }

        if (dto.getCategory() != null && !dto.getCategory().isBlank()) {
            wrapper.eq("category", dto.getCategory());
        }

        if (dto.getMinPrice() != null) {
            wrapper.ge("discount_price", dto.getMinPrice());
        }

        if (dto.getMaxPrice() != null) {
            wrapper.le("discount_price", dto.getMaxPrice());
        }

        boolean asc = "asc".equalsIgnoreCase(dto.getOrder());

        if ("price_sale".equals(dto.getSortBy())) {
            wrapper.orderBy(true, asc, "discount_price");
        } else if ("score_sale".equals(dto.getSortBy())) {
            wrapper.orderBy(true, false, "avg_rating * sales_count");
        } else {
            wrapper.orderByDesc("create_time");
        }

        return list(wrapper);
    }
}
