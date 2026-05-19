package com.example.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderPreviewVO {

    private List<CartGroupVO> groups;

    private BigDecimal totalAmount;

    private int availablePoints;

    private BigDecimal pointsDeduction;

    private BigDecimal actualPay;
}
