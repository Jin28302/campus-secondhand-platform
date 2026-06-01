package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 退货审核请求DTO - 接收商家审核退货申请的数据
 * 审核结果：同意/拒绝，拒绝时需填写拒绝原因
 */
@Data
public class ReturnAuditDTO {

    /** 审核结果，不能为空：同意/拒绝 */
    @NotBlank(message = "审核结果不能为空")
    private String result;

    /** 拒绝原因（仅在审核结果为"拒绝"时需要填写） */
    private String rejectReason;
}
