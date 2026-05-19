package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.AuditDTO;
import com.example.backend.dto.UserSearchDTO;
import com.example.backend.dto.UserUpdateDTO;
import com.example.backend.entity.Product;
import com.example.backend.entity.User;
import com.example.backend.common.ProductStatus;
import com.example.backend.common.UserStatus;
import com.example.backend.exception.BusinessException;
import com.example.backend.service.AdminService;
import com.example.backend.service.ProductService;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserService userService;
    private final ProductService productService;

    @Override
    public List<User> pendingUsers() {
        return userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, UserStatus.PENDING.getValue())
                .orderByAsc(User::getCreatedAt));
    }

    @Override
    public void auditUser(Long userId, AuditDTO dto) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!UserStatus.PENDING.getValue().equals(user.getStatus())) {
            throw new BusinessException("该用户不在待审核状态");
        }

        if ("通过".equals(dto.getResult())) {
            user.setStatus(UserStatus.NORMAL.getValue());
            if ("商家".equals(user.getRole()) && user.getSellerLevel() == null) {
                user.setSellerLevel(1);
            }
        } else if ("拒绝".equals(dto.getResult())) {
            user.setStatus(UserStatus.BANNED.getValue());
        } else {
            throw new BusinessException("审核结果只能为 通过 或 拒绝");
        }

        userService.updateById(user);
    }

    @Override
    public List<Product> pendingProducts() {
        return productService.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, ProductStatus.PENDING.getValue())
                .orderByAsc(Product::getCreatedAt));
    }

    @Override
    public void approveProduct(Long productId) {
        Product product = productService.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!ProductStatus.PENDING.getValue().equals(product.getStatus())) {
            throw new BusinessException("该商品不在待审核状态");
        }

        product.setStatus(ProductStatus.PUBLISHED.getValue());
        productService.updateById(product);
    }

    @Override
    public IPage<User> userList(UserSearchDTO dto) {
        Page<User> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
            wrapper.and(w -> w
                    .like(User::getPhone, dto.getKeyword())
                    .or()
                    .like(User::getName, dto.getKeyword())
                    .or()
                    .like(User::getPhone, dto.getKeyword()));
        }

        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            wrapper.eq(User::getRole, dto.getRole());
        }

        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            wrapper.eq(User::getStatus, dto.getStatus());
        }

        wrapper.orderByDesc(User::getCreatedAt);
        return userService.page(page, wrapper);
    }

    @Override
    public void updateUser(Long userId, UserUpdateDTO dto) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getCity() != null) user.setCity(dto.getCity());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getBankAccount() != null) user.setBankAccount(dto.getBankAccount());
        if (dto.getRole() != null) user.setRole(dto.getRole());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());
        if (dto.getSellerLevel() != null) user.setSellerLevel(dto.getSellerLevel());
        if (dto.getPoints() != null) user.setPoints(dto.getPoints());
        if (dto.getWallet() != null) user.setWallet(dto.getWallet());

        userService.updateById(user);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        userService.removeById(userId);
    }

    @Override
    public void banUser(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(UserStatus.BANNED.getValue());
        userService.updateById(user);
    }
}
