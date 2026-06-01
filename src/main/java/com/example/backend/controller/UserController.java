package com.example.backend.controller;

import com.example.backend.common.R;
import com.example.backend.dto.LoginDTO;
import com.example.backend.dto.ProfileUpdateDTO;
import com.example.backend.dto.RegisterDTO;
import com.example.backend.service.CaptchaService;
import com.example.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

// 用户控制器 - 处理注册、登录、个人资料、验证码
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CaptchaService captchaService;

    // 生成图形验证码 - 返回key和Base64编码的PNG图片
    @GetMapping("/captcha")
    public R<Map<String, String>> captcha() throws Exception {
        // 生成验证码唯一标识key
        String key = captchaService.generateKey();
        // 根据key生成验证码图片
        BufferedImage image = captchaService.generateImage(key);

        // 将BufferedImage转为Base64字符串，前端可直接用于img标签
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        String base64 = Base64.getEncoder().encodeToString(os.toByteArray());

        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", key);
        result.put("image", "data:image/png;base64," + base64);
        return R.ok(result);
    }

    // 用户注册 - 创建新用户，状态默认为"待审核"
    @PostMapping("/register")
    public R<Void> register(@RequestBody @Valid RegisterDTO dto) {
        userService.register(dto);
        return R.ok();
    }

    // 用户登录 - 校验账号密码和验证码，返回JWT token和角色
    @PostMapping("/login")
    public R<Map<String, String>> login(@RequestBody @Valid LoginDTO dto) {
        Map<String, String> result = userService.login(dto);
        return R.ok(result);
    }

    // 修改个人资料 - 更新昵称、手机号等信息
    @PutMapping("/user/profile")
    public R<Void> updateProfile(@RequestBody ProfileUpdateDTO dto) {
        userService.updateProfile(dto);
        return R.ok();
    }
}
