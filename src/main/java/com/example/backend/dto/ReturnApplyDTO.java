package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReturnApplyDTO {

    @NotBlank(message = "退货原因不能为空")
    private String reason;
}
