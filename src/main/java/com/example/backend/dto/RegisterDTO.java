package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求DTO - 接收用户注册表单数据
 * 支持普通用户和商家两种角色的注册，商家需额外提供营业执照和身份证图片
 */
@Data
public class RegisterDTO {

    /** 手机号，不能为空，格式必须符合1[3-9]xxxxxxxxx */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 密码，不能为空，长度6-20位 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20位")
    private String password;

    /** 姓名（可选） */
    private String name;

    /** 邮箱（可选） */
    private String email;

    /** 城市（可选） */
    private String city;

    /** 性别：1=男，0=女（可选） */
    private Integer gender;

    /** 银行账号，16位数字（可选） */
    @Pattern(regexp = "^\\d{16}$", message = "银行账号必须为16位数字")
    private String bankAccount;

    /** 角色，不能为空：用户/商家 */
    @NotBlank(message = "角色不能为空")
    private String role;

    /** 营业执照图片URL（仅商家注册时需要） */
    private String licenseImg;

    /** 身份证图片URL（仅商家注册时需要） */
    private String idCardImg;

    /** 验证码输入，不能为空 */
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;

    /** 验证码Key（后端生成，用于匹配验证码），不能为空 */
    @NotBlank(message = "验证码Key不能为空")
    private String captchaKey;
}
