package com.example.backend.dto;

import lombok.Data;

/**
 * 订单查询请求DTO - 接收订单列表查询参数
 * 支持按订单状态筛选和分页
 */
@Data
public class OrderQueryDTO {

    /** 订单状态筛选（可选）：pending/completed/refunded/refund_pending/refund_rejected */
    private String status;

    /** 当前页码，默认第1页 */
    private Integer pageNum = 1;

    /** 每页显示数量，默认10条 */
    private Integer pageSize = 10;
}
