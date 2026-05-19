package com.example.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductSearchDTO {

    private String keyword;

    private String category;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private String sortBy;

    private String order;
}
