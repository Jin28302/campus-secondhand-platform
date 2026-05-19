package com.example.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateDTO {

    @NotEmpty(message = "请选择购物车商品")
    private List<Long> cartIds;
}
