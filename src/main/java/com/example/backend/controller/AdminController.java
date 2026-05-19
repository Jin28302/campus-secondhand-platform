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

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users/pending")
    @RequireRole({"管理员"})
    public R<List<User>> pendingUsers() {
        return R.ok(adminService.pendingUsers());
    }

    @PutMapping("/user/{id}/audit")
    @RequireRole({"管理员"})
    public R<Void> auditUser(@PathVariable Long id, @RequestBody @Valid AuditDTO dto) {
        adminService.auditUser(id, dto);
        return R.ok();
    }

    @GetMapping("/products/pending")
    @RequireRole({"管理员"})
    public R<List<Product>> pendingProducts() {
        return R.ok(adminService.pendingProducts());
    }

    @PutMapping("/product/{id}/approve")
    @RequireRole({"管理员"})
    public R<Void> approveProduct(@PathVariable Long id) {
        adminService.approveProduct(id);
        return R.ok();
    }

    @GetMapping("/users")
    @RequireRole({"管理员"})
    public R<IPage<User>> userList(UserSearchDTO dto) {
        return R.ok(adminService.userList(dto));
    }

    @PutMapping("/user/{id}")
    @RequireRole({"管理员"})
    public R<Void> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO dto) {
        adminService.updateUser(id, dto);
        return R.ok();
    }

    @DeleteMapping("/user/{id}")
    @RequireRole({"管理员"})
    public R<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return R.ok();
    }

    @PutMapping("/user/{id}/ban")
    @RequireRole({"管理员"})
    public R<Void> banUser(@PathVariable Long id) {
        adminService.banUser(id);
        return R.ok();
    }
}
