package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 退货申请请求DTO - 接收用户退货申请数据
 */
@Data
public class ReturnApplyDTO {

    /** 退货原因，不能为空 */
    @NotBlank(message = "退货原因不能为空")
    private String reason;
}
