package com.example.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品搜索请求DTO - 接收商品列表搜索参数
 * 支持关键词搜索、分类筛选、价格区间、多种排序方式和分页
 * 排序方式：price_asc/desc（价格）、sales_asc/desc（销量）、rating_asc/desc（评分）
 */
@Data
public class ProductSearchDTO {

    /** 搜索关键词（可选，模糊匹配商品名称和描述） */
    private String keyword;

    /** 商品分类筛选（可选） */
    private String category;

    /** 最低价格筛选（可选） */
    private BigDecimal minPrice;

    /** 最高价格筛选（可选） */
    private BigDecimal maxPrice;

    /** 排序方式（可选）：price_asc/price_desc/sales_asc/sales_desc/rating_asc/rating_desc */
    private String sort;

    /** 当前页码，默认第1页 */
    private Integer pageNum = 1;

    /** 每页显示数量，默认12条 */
    private Integer pageSize = 12;
}
