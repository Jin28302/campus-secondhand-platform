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

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CaptchaService captchaService;

    @GetMapping("/captcha")
    public R<Map<String, String>> captcha() throws Exception {
        String key = captchaService.generateKey();
        BufferedImage image = captchaService.generateImage(key);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        String base64 = Base64.getEncoder().encodeToString(os.toByteArray());

        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", key);
        result.put("image", "data:image/png;base64," + base64);
        return R.ok(result);
    }

    @PostMapping("/register")
    public R<Void> register(@RequestBody @Valid RegisterDTO dto) {
        userService.register(dto);
        return R.ok();
    }

    @PostMapping("/login")
    public R<Map<String, String>> login(@RequestBody @Valid LoginDTO dto) {
        String token = userService.login(dto);
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        return R.ok(result);
    }

    @PutMapping("/user/profile")
    public R<Void> updateProfile(@RequestBody ProfileUpdateDTO dto) {
        userService.updateProfile(dto);
        return R.ok();
    }
}
