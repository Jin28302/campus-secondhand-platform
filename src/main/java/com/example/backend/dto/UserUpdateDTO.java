package com.example.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserUpdateDTO {

    private String name;

    private String phone;

    private String email;

    private String city;

    private Integer gender;

    private String bankAccount;

    private String role;

    private String status;

    private Integer sellerLevel;

    private Integer points;

    private BigDecimal wallet;
}
