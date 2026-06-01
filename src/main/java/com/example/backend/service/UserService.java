package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.dto.LoginDTO;
import com.example.backend.dto.ProfileUpdateDTO;
import com.example.backend.dto.RegisterDTO;
import com.example.backend.entity.User;

import java.math.BigDecimal;
import java.util.Map;

public interface UserService extends IService<User> {

    void register(RegisterDTO dto);

    Map<String, String> login(LoginDTO dto);

    BigDecimal deductPoints(Long userId, BigDecimal orderAmount);

    void updateProfile(ProfileUpdateDTO dto);
}
