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

// 管理员服务实现 - 处理用户审核、商品审核、用户管理
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserService userService;
    private final ProductService productService;

    // 获取待审核用户列表 - 按注册时间升序，最早注册的优先审核
    @Override
    public List<User> pendingUsers() {
        return userService.list(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, UserStatus.PENDING.getValue())
                .orderByAsc(User::getCreatedAt));
    }

    // 审核用户 - 通过则设为"正常"，商家默认等级为1；拒绝则设为"已封禁"
    @Override
    public void auditUser(Long userId, AuditDTO dto) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 只能审核待审核状态的用户
        if (!UserStatus.PENDING.getValue().equals(user.getStatus())) {
            throw new BusinessException("该用户不在待审核状态");
        }

        if ("通过".equals(dto.getResult())) {
            user.setStatus(UserStatus.NORMAL.getValue());
            // 商家审核通过时初始化等级为1
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

    // 获取待审核商品列表 - 按创建时间升序排列
    @Override
    public List<Product> pendingProducts() {
        return productService.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, ProductStatus.PENDING.getValue())
                .orderByAsc(Product::getCreatedAt));
    }

    // 审核通过商品 - 将商品状态从"待审核"改为"在售"
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

    // 驳回商品 - 将商品状态从"待审核"改为"已下架"
    @Override
    public void rejectProduct(Long productId) {
        Product product = productService.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!ProductStatus.PENDING.getValue().equals(product.getStatus())) {
            throw new BusinessException("该商品不在待审核状态");
        }
        product.setStatus(ProductStatus.OFF_SHELF.getValue());
        productService.updateById(product);
    }

    // 用户列表 - 支持关键词搜索（手机号/昵称）、按角色和状态筛选，分页展示
    @Override
    public IPage<User> userList(UserSearchDTO dto) {
        Page<User> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索：匹配手机号或昵称
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

    // 编辑用户 - 管理员修改用户的各项信息（昵称、手机号、角色、状态、等级、积分、余额等）
    @Override
    public void updateUser(Long userId, UserUpdateDTO dto) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 仅更新DTO中非空的字段
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

    // 删除用户 - 管理员删除用户及其数据
    @Override
    public void deleteUser(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        userService.removeById(userId);
    }

    // 封禁用户 - 将用户状态设为"已封禁"
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
