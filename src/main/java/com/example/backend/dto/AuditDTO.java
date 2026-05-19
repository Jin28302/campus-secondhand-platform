package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuditDTO {

    @NotBlank(message = "审核结果不能为空")
    private String result;
}
