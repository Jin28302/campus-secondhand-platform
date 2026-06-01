package com.example.backend.dto;

import lombok.Data;

/**
 * 商家查询请求DTO - 接收管理员查询商家列表的参数
 * 支持按关键词搜索和分页
 */
@Data
public class SellerQueryDTO {

    /** 搜索关键词（可选，模糊匹配商家名称/手机号/邮箱） */
    private String keyword;

    /** 当前页码，默认第1页 */
    private Integer pageNum = 1;

    /** 每页显示数量，默认10条 */
    private Integer pageSize = 10;
}
