package com.example.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.common.R;
import com.example.backend.common.RequireRole;
import com.example.backend.dto.AuditDTO;
import com.example.backend.dto.UserSearchDTO;
import com.example.backend.dto.UserUpdateDTO;
import com.example.backend.entity.Product;
import com.example.backend.entity.User;
import com.example.backend.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 管理员控制器 - 处理用户审核、商品审核、用户管理（封禁/删除/编辑）
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 获取待审核用户列表 - 返回注册后状态为"待审核"的用户
    @GetMapping("/users/pending")
    @RequireRole({"管理员"})
    public R<List<User>> pendingUsers() {
        return R.ok(adminService.pendingUsers());
    }

    // 审核用户 - 管理员通过或驳回用户注册申请
    @PutMapping("/user/{id}/audit")
    @RequireRole({"管理员"})
    public R<Void> auditUser(@PathVariable Long id, @RequestBody @Valid AuditDTO dto) {
        adminService.auditUser(id, dto);
        return R.ok();
    }

    // 获取待审核商品列表 - 返回商家发布后状态为"待审核"的商品
    @GetMapping("/products/pending")
    @RequireRole({"管理员"})
    public R<List<Product>> pendingProducts() {
        return R.ok(adminService.pendingProducts());
    }

    // 审核通过商品 - 将商品状态设为"在售"
    @PutMapping("/product/{id}/approve")
    @RequireRole({"管理员"})
    public R<Void> approveProduct(@PathVariable Long id) {
        adminService.approveProduct(id);
        return R.ok();
    }

    // 驳回商品 - 将商品状态设为"已驳回"
    @PutMapping("/product/{id}/reject")
    @RequireRole({"管理员"})
    public R<Void> rejectProduct(@PathVariable Long id) {
        adminService.rejectProduct(id);
        return R.ok();
    }

    // 用户列表 - 支持关键词搜索和分页
    @GetMapping("/users")
    @RequireRole({"管理员"})
    public R<IPage<User>> userList(UserSearchDTO dto) {
        return R.ok(adminService.userList(dto));
    }

    // 编辑用户 - 管理员修改用户信息（手机号、昵称、角色等）
    @PutMapping("/user/{id}")
    @RequireRole({"管理员"})
    public R<Void> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO dto) {
        adminService.updateUser(id, dto);
        return R.ok();
    }

    // 删除用户 - 管理员删除用户及其关联数据
    @DeleteMapping("/user/{id}")
    @RequireRole({"管理员"})
    public R<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return R.ok();
    }

    // 封禁用户 - 将用户状态设为"已封禁"，使其无法登录和操作
    @PutMapping("/user/{id}/ban")
    @RequireRole({"管理员"})
    public R<Void> banUser(@PathVariable Long id) {
        adminService.banUser(id);
        return R.ok();
    }
}
