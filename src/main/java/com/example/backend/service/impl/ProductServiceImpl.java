package com.example.backend.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.ProductStatus;
import com.example.backend.common.UserContext;
import com.example.backend.dto.ProductDTO;
import com.example.backend.dto.ProductSearchDTO;
import com.example.backend.entity.Product;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.ProductMapper;
import com.example.backend.service.ProductService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

// 商品服务实现 - 处理商品发布、编辑、下架、搜索和详情
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    // 发布商品 - 创建新商品，状态设为"待审核"，初始销量和评分为0
    @Override
    public void publish(ProductDTO dto) {
        // 从ThreadLocal获取当前登录用户
        UserContext.UserInfo currentUser = UserContext.get();

        // 将图片URL列表用逗号拼接为字符串存储
        String images = dto.getImageUrls() != null ? String.join(",", dto.getImageUrls()) : "";

        Product product = new Product();
        product.setSellerId(currentUser.userId());
        product.setShopName(currentUser.phone());
        product.setName(dto.getName());
        product.setCategory(dto.getCategory());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setDiscountPrice(dto.getDiscountPrice());
        product.setSize(dto.getSize());
        product.setImages(images);
        product.setDescription(dto.getDescription());
        product.setAllowBargain(dto.getAllowBargain());
        product.setStock(dto.getStock());
        product.setNewDegree(dto.getNewDegree());
        // 新发布商品为"待审核"状态，需管理员审核通过后才能上架
        product.setStatus(ProductStatus.PENDING.getValue());
        product.setSalesCount(0);
        product.setAvgRating(BigDecimal.ZERO);

        save(product);
    }

    // 编辑商品 - 仅允许商品所属商家修改，更新非空字段
    @Override
    public void update(Long id, ProductDTO dto) {
        UserContext.UserInfo currentUser = UserContext.get();
        Product product = getById(id);
        if (product == null) throw new BusinessException("商品不存在");
        // 校验归属：只能修改自己的商品
        if (!product.getSellerId().equals(currentUser.userId())) throw new BusinessException("只能修改自己的商品");

        String images = dto.getImageUrls() != null ? String.join(",", dto.getImageUrls()) : product.getImages();
        product.setImages(images);
        // 仅更新DTO中非空的字段，保留未传字段的原有值
        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getCategory() != null) product.setCategory(dto.getCategory());
        if (dto.getOriginalPrice() != null) product.setOriginalPrice(dto.getOriginalPrice());
        if (dto.getDiscountPrice() != null) product.setDiscountPrice(dto.getDiscountPrice());
        if (dto.getSize() != null) product.setSize(dto.getSize());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getAllowBargain() != null) product.setAllowBargain(dto.getAllowBargain());
        if (dto.getStock() != null) product.setStock(dto.getStock());
        if (dto.getNewDegree() != null) product.setNewDegree(dto.getNewDegree());

        updateById(product);
    }

    // 下架商品 - 将商品状态设为"已下架"，校验归属权限
    @Override
    public void offShelf(Long id) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 校验归属：只能下架自己的商品
        UserContext.UserInfo currentUser = UserContext.get();
        if (!product.getSellerId().equals(currentUser.userId())) {
            throw new BusinessException("只能下架自己的商品");
        }

        product.setStatus(ProductStatus.OFF_SHELF.getValue());
        updateById(product);
    }

    // 获取商家自己的商品列表 - 按创建时间倒序排列
    @Override
    public List<Product> sellerList() {
        UserContext.UserInfo currentUser = UserContext.get();
        return list(new LambdaQueryWrapper<Product>()
                .eq(Product::getSellerId, currentUser.userId())
                .orderByDesc(Product::getCreatedAt));
    }

    // 商品详情 - 根据ID查询商品
    @Override
    public Product detail(Long id) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        return product;
    }

    // 商品搜索 - 仅返回"在售"状态的商品，支持关键词、分类、价格范围、排序
    @Override
    public IPage<Product> search(ProductSearchDTO dto) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        // 只搜索已发布的商品
        wrapper.eq("status", ProductStatus.PUBLISHED.getValue());

        // 关键词匹配：按商品名称和描述模糊搜索
        if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
            wrapper.and(w -> w.like("name", dto.getKeyword()).or().like("description", dto.getKeyword()));
        }
        // 按分类筛选
        if (dto.getCategory() != null && !dto.getCategory().isBlank()) {
            wrapper.eq("category", dto.getCategory());
        }
        // 价格区间筛选
        if (dto.getMinPrice() != null) wrapper.ge("discount_price", dto.getMinPrice());
        if (dto.getMaxPrice() != null) wrapper.le("discount_price", dto.getMaxPrice());

        // 排序逻辑：支持按价格、销量、评分升降序，默认按创建时间倒序
        switch (dto.getSort() != null ? dto.getSort() : "") {
            case "price_asc"   -> wrapper.orderByAsc("discount_price");
            case "price_desc"  -> wrapper.orderByDesc("discount_price");
            case "sales_asc"   -> wrapper.orderByAsc("sales_count");
            case "sales_desc"  -> wrapper.orderByDesc("sales_count");
            case "rating_asc"  -> wrapper.orderByAsc("avg_rating");
            case "rating_desc" -> wrapper.orderByDesc("avg_rating");
            default            -> wrapper.orderByDesc("created_at");
        }

        return page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
    }
}
