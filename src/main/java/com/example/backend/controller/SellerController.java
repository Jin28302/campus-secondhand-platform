package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.R;
import com.example.backend.dto.SellerQueryDTO;
import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 商家列表控制器 - 提供商家搜索和分页展示
@RestController
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerController {

    private final UserService userService;

    // 商家列表 - 查询状态为"正常"的商家，支持关键字搜索，按商家等级降序排列
    @GetMapping("/list")
    public R<IPage<User>> list(SellerQueryDTO dto) {
        // 构建查询条件：角色为"商家"且状态为"正常"
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getRole, "商家")
                .eq(User::getStatus, "正常")
                .orderByDesc(User::getSellerLevel);

        // 关键字搜索：支持按手机号或昵称模糊匹配
        if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
            wrapper.and(w -> w
                    .like(User::getPhone, dto.getKeyword())
                    .or()
                    .like(User::getName, dto.getKeyword()));
        }

        return R.ok(userService.page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper));
    }
}
