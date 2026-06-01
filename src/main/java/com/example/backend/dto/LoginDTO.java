package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO - 接收用户登录表单数据
 * 支持手机号/账号 + 密码 + 图形验证码方式登录
 */
@Data
public class LoginDTO {

    /** 登录账号（手机号），不能为空 */
    @NotBlank(message = "账号不能为空")
    private String account;

    /** 登录密码，不能为空 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 验证码输入，不能为空 */
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;

    /** 验证码Key（后端生成，用于匹配验证码），不能为空 */
    @NotBlank(message = "验证码Key不能为空")
    private String captchaKey;
}
