package com.example.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.dto.AuditDTO;
import com.example.backend.dto.UserSearchDTO;
import com.example.backend.dto.UserUpdateDTO;
import com.example.backend.entity.Product;
import com.example.backend.entity.User;

import java.util.List;

public interface AdminService {

    List<User> pendingUsers();

    void auditUser(Long userId, AuditDTO dto);

    List<Product> pendingProducts();

    void approveProduct(Long productId);

    IPage<User> userList(UserSearchDTO dto);

    void updateUser(Long userId, UserUpdateDTO dto);

    void deleteUser(Long userId);

    void banUser(Long userId);
}
