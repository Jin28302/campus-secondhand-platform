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

@RestController
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerController {

    private final UserService userService;

    @GetMapping("/list")
    public R<IPage<User>> list(SellerQueryDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getRole, "商家")
                .eq(User::getStatus, "正常")
                .orderByDesc(User::getSellerLevel);

        if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
            wrapper.and(w -> w
                    .like(User::getPhone, dto.getKeyword())
                    .or()
                    .like(User::getName, dto.getKeyword()));
        }

        return R.ok(userService.page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper));
    }
}
