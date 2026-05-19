package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDTO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20位")
    private String password;

    private String name;

    private String email;

    private String city;

    private Integer gender;

    @Pattern(regexp = "^\\d{16}$", message = "银行账号必须为16位数字")
    private String bankAccount;

    @NotBlank(message = "角色不能为空")
    private String role;

    private String licenseImg;

    private String idCardImg;

    @NotBlank(message = "验证码不能为空")
    private String captchaCode;

    @NotBlank(message = "验证码Key不能为空")
    private String captchaKey;
}
