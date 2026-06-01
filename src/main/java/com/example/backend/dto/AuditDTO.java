package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 通用审核请求DTO - 用于管理员审核用户/商品
 * 审核结果：通过/拒绝
 */
@Data
public class AuditDTO {

    /** 审核结果，不能为空：通过/拒绝 */
    @NotBlank(message = "审核结果不能为空")
    private String result;
}
