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

// 钱包控制器 - 处理余额/积分查询、用户自助充值、管理员充值
@RestController
@RequiredArgsConstructor
public class WalletController {

    private final UserService userService;

    // 查询钱包 - 返回当前用户的余额、积分和个人信息
    @GetMapping("/wallet")
    public R<Map<String, Object>> wallet() {
        // 从ThreadLocal中获取当前登录用户ID
        Long userId = UserContext.get().userId();
        User user = userService.getById(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("wallet", user.getWallet());
        result.put("points", user.getPoints());
        // 同步返回用户个人信息，供个人中心初始化表单
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", user.getName());
        userInfo.put("phone", user.getPhone());
        userInfo.put("email", user.getEmail());
        userInfo.put("city", user.getCity());
        userInfo.put("gender", user.getGender());
        userInfo.put("bankAccount", user.getBankAccount());
        result.put("user", userInfo);
        return R.ok(result);
    }

    // 用户自助充值 - 通过前端传入的金额增加余额
    @PostMapping("/wallet/recharge")
    public R<Void> selfRecharge(@RequestBody Map<String, Object> body) {
        // 从ThreadLocal中获取当前登录用户ID
        Long userId = UserContext.get().userId();
        Object amountObj = body.get("amount");
        if (amountObj == null) throw new BusinessException("请输入充值金额");
        java.math.BigDecimal amount = new java.math.BigDecimal(amountObj.toString());
        if (amount.compareTo(java.math.BigDecimal.ZERO) <= 0) throw new BusinessException("金额必须大于0");
        // 使用setSql执行原子加法，避免并发时余额覆盖问题
        userService.update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .setSql("wallet = wallet + " + amount));
        return R.ok();
    }

    // 管理员充值 - 管理员为指定用户充值余额
    @PostMapping("/admin/wallet/recharge")
    @RequireRole({"管理员"})
    public R<Void> recharge(@RequestBody @Valid RechargeDTO dto) {
        User user = userService.getById(dto.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 使用setSql执行原子加法，避免并发时余额覆盖问题
        userService.update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, dto.getUserId())
                .setSql("wallet = wallet + " + dto.getAmount()));

        return R.ok();
    }
}
