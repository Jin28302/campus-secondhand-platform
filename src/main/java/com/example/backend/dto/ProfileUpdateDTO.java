package com.example.backend.dto;

import lombok.Data;

@Data
public class ProfileUpdateDTO {

    private String name;

    private String phone;

    private String email;

    private String city;

    private Integer gender;

    private String bankAccount;
}
