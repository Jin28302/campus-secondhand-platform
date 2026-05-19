package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.JwtUtil;
import com.example.backend.common.PasswordUtil;
import com.example.backend.common.PointsDeductUtil;
import com.example.backend.common.UserContext;
import com.example.backend.common.UserStatus;
import com.example.backend.dto.LoginDTO;
import com.example.backend.dto.ProfileUpdateDTO;
import com.example.backend.dto.RegisterDTO;
import com.example.backend.entity.User;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.UserMapper;
import com.example.backend.service.CaptchaService;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final CaptchaService captchaService;

    @Override
    public void register(RegisterDTO dto) {
        if (!captchaService.validate(dto.getCaptchaKey(), dto.getCaptchaCode())) {
            throw new BusinessException("验证码错误或已过期");
        }

        long count = count(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
        if (count > 0) {
            throw new BusinessException("手机号已注册");
        }

        if ("商家".equals(dto.getRole())) {
            if (dto.getLicenseImg() == null || dto.getLicenseImg().isBlank()) {
                throw new BusinessException("商家注册必须上传营业执照");
            }
            if (dto.getIdCardImg() == null || dto.getIdCardImg().isBlank()) {
                throw new BusinessException("商家注册必须上传身份证照片");
            }
        }

        User user = new User();
        user.setPassword(PasswordUtil.encode(dto.getPassword()));
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setCity(dto.getCity());
        user.setGender(dto.getGender());
        user.setBankAccount(dto.getBankAccount());
        user.setRole(dto.getRole());
        user.setStatus(UserStatus.PENDING.getValue());
        user.setLicenseImg(dto.getLicenseImg());
        user.setIdCardImg(dto.getIdCardImg());
        user.setSellerLevel("商家".equals(dto.getRole()) ? 1 : null);
        user.setPoints(0);
        user.setWallet(BigDecimal.ZERO);

        save(user);
    }

    @Override
    public String login(LoginDTO dto) {
        if (!captchaService.validate(dto.getCaptchaKey(), dto.getCaptchaCode())) {
            throw new BusinessException("验证码错误或已过期");
        }

        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getAccount()));
        if (user == null) {
            throw new BusinessException("手机号或密码错误");
        }

        if (!PasswordUtil.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("手机号或密码错误");
        }

        if (UserStatus.BANNED.getValue().equals(user.getStatus())) {
            throw new BusinessException("账号已被封禁");
        }

        if (UserStatus.PENDING.getValue().equals(user.getStatus())) {
            throw new BusinessException("账号待审核，请等待管理员审批");
        }

        return JwtUtil.generate(user.getId(), user.getPhone(), user.getRole());
    }

    @Override
    public BigDecimal deductPoints(Long userId, BigDecimal orderAmount) {
        User user = getById(userId);
        if (user == null || user.getPoints() == null || user.getPoints() <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal deduction = PointsDeductUtil.calcDeduction(user.getPoints(), orderAmount);
        if (deduction.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        int pointsToDeduct = PointsDeductUtil.deductedPoints(deduction);
        update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .ge(User::getPoints, pointsToDeduct)
                .setSql("points = points - " + pointsToDeduct));

        return deduction;
    }

    @Override
    public void updateProfile(ProfileUpdateDTO dto) {
        Long userId = UserContext.get().userId();
        User user = getById(userId);

        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getCity() != null) user.setCity(dto.getCity());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getBankAccount() != null) user.setBankAccount(dto.getBankAccount());

        updateById(user);
    }
}
