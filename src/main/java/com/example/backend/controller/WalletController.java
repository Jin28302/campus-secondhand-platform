package com.example.backend.controller;

import com.example.backend.common.R;
import com.example.backend.common.RequireRole;
import com.example.backend.common.UserContext;
import com.example.backend.dto.RechargeDTO;
import com.example.backend.entity.User;
import com.example.backend.exception.BusinessException;
import com.example.backend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class WalletController {

    private final UserService userService;

    @GetMapping("/wallet")
    public R<Map<String, Object>> wallet() {
        Long userId = UserContext.get().userId();
        User user = userService.getById(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("wallet", user.getWallet());
        result.put("points", user.getPoints());
        return R.ok(result);
    }

    @PostMapping("/admin/wallet/recharge")
    @RequireRole({"管理员"})
    public R<Void> recharge(@RequestBody @Valid RechargeDTO dto) {
        User user = userService.getById(dto.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        userService.update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, dto.getUserId())
                .setSql("wallet = wallet + " + dto.getAmount()));

        return R.ok();
    }
}
