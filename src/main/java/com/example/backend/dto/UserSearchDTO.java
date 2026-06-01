package com.example.backend.dto;

import lombok.Data;

/**
 * 用户搜索请求DTO - 接收管理员搜索用户的参数
 * 支持按关键词、角色、状态筛选和分页
 */
@Data
public class UserSearchDTO {

    /** 搜索关键词（可选，模糊匹配姓名/手机号/邮箱） */
    private String keyword;

    /** 角色筛选（可选）：user/merchant/admin */
    private String role;

    /** 状态筛选（可选）：pending/normal/banned */
    private String status;

    /** 当前页码，默认第1页 */
    private Integer pageNum = 1;

    /** 每页显示数量，默认10条 */
    private Integer pageSize = 10;
}
