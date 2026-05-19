package com.example.backend.dto;

import lombok.Data;

@Data
public class SellerQueryDTO {

    private String keyword;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
