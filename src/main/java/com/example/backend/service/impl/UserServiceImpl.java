package com.example.backend.service.impl;

import java.util.Map;

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

// 用户服务实现 - 处理注册、登录、积分抵扣、个人资料修改
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final CaptchaService captchaService;

    // 用户注册 - 校验验证码、手机号唯一性，商家需提交营业执照和身份证
    @Override
    public void register(RegisterDTO dto) {
        // 校验图形验证码
        if (!captchaService.validate(dto.getCaptchaKey(), dto.getCaptchaCode())) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 检查手机号是否已注册
        long count = count(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
        if (count > 0) {
            throw new BusinessException("手机号已注册");
        }

        // 商家注册时，营业执照和身份证为必填项
        if ("商家".equals(dto.getRole())) {
            if (dto.getLicenseImg() == null || dto.getLicenseImg().isBlank()) {
                throw new BusinessException("商家注册必须上传营业执照");
            }
            if (dto.getIdCardImg() == null || dto.getIdCardImg().isBlank()) {
                throw new BusinessException("商家注册必须上传身份证照片");
            }
        }

        User user = new User();
        // 密码使用BCrypt加密存储
        user.setPassword(PasswordUtil.encode(dto.getPassword()));
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setCity(dto.getCity());
        user.setGender(dto.getGender());
        user.setBankAccount(dto.getBankAccount());
        user.setRole(dto.getRole());
        // 新注册用户均为待审核状态
        user.setStatus(UserStatus.PENDING.getValue());
        user.setLicenseImg(dto.getLicenseImg());
        user.setIdCardImg(dto.getIdCardImg());
        // 商家默认等级为1
        user.setSellerLevel("商家".equals(dto.getRole()) ? 1 : null);
        // 初始积分为0，余额为0
        user.setPoints(0);
        user.setWallet(BigDecimal.ZERO);

        save(user);
    }

    // 用户登录 - 校验验证码、账号密码，检查账号状态，生成JWT token
    @Override
    public Map<String, String> login(LoginDTO dto) {
        // 校验图形验证码
        if (!captchaService.validate(dto.getCaptchaKey(), dto.getCaptchaCode())) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 根据手机号查询用户
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getAccount()));
        if (user == null) {
            throw new BusinessException("手机号或密码错误");
        }

        // 使用BCrypt校验密码
        if (!PasswordUtil.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("手机号或密码错误");
        }

        // 检查账号状态：已封禁不能登录
        if (UserStatus.BANNED.getValue().equals(user.getStatus())) {
            throw new BusinessException("账号已被封禁");
        }

        // 检查账号状态：待审核不能登录
        if (UserStatus.PENDING.getValue().equals(user.getStatus())) {
            throw new BusinessException("账号待审核，请等待管理员审批");
        }

        // 生成JWT token，携带用户ID、手机号、角色信息
        String token = JwtUtil.generate(user.getId(), user.getPhone(), user.getRole());
        Map<String, String> result = new java.util.HashMap<>();
        result.put("token", token);
        result.put("role", user.getRole());
        return result;
    }

    // 积分抵扣 - 根据用户积分计算可抵扣金额并扣减积分
    // @param userId 用户ID
    // @param orderAmount 订单金额
    // @return 实际抵扣金额
    @Override
    public BigDecimal deductPoints(Long userId, BigDecimal orderAmount) {
        User user = getById(userId);
        if (user == null || user.getPoints() == null || user.getPoints() <= 0) {
            return BigDecimal.ZERO;
        }

        // 根据积分规则计算可抵扣金额
        BigDecimal deduction = PointsDeductUtil.calcDeduction(user.getPoints(), orderAmount);
        if (deduction.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 计算需要扣减的积分数，使用乐观锁确保积分足够才扣减
        int pointsToDeduct = PointsDeductUtil.deductedPoints(deduction);
        update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .ge(User::getPoints, pointsToDeduct)  // 乐观锁条件：积分 >= 要扣减的积分
                .setSql("points = points - " + pointsToDeduct));

        return deduction;
    }

    // 修改个人资料 - 更新当前登录用户的昵称、手机号、邮箱等信息
    @Override
    public void updateProfile(ProfileUpdateDTO dto) {
        // 从ThreadLocal中获取当前登录用户ID
        Long userId = UserContext.get().userId();
        User user = getById(userId);

        // 仅更新DTO中非空的字段
        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getCity() != null) user.setCity(dto.getCity());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getBankAccount() != null) user.setBankAccount(dto.getBankAccount());

        updateById(user);
    }
}
