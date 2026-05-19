package com.example.backend.dto;

import lombok.Data;

@Data
public class UserSearchDTO {

    private String keyword;

    private String role;

    private String status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
