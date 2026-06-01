package com.example.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品发布/编辑请求DTO - 接收商品表单数据
 * 用于商家发布商品和管理员编辑商品
 * 必填字段：name（商品名称）、category（分类）、originalPrice（原价）、
 *          discountPrice（折扣价）、stock（库存）、newDegree（新旧程度）
 */
@Data
public class ProductDTO {

    /** 商品名称，不能为空 */
    @NotBlank(message = "商品名称不能为空")
    private String name;

    /** 商品分类，不能为空 */
    @NotBlank(message = "分类不能为空")
    private String category;

    /** 原价，不能为空，必须大于0 */
    @NotNull(message = "原价不能为空")
    @DecimalMin(value = "0.01", message = "原价必须大于0")
    private BigDecimal originalPrice;

    /** 折扣价，不能为空，必须大于0 */
    @NotNull(message = "折扣价不能为空")
    @DecimalMin(value = "0.01", message = "折扣价必须大于0")
    private BigDecimal discountPrice;

    /** 商品尺寸（可选） */
    private String size;

    /** 商品描述/说明（可选） */
    private String description;

    /** 是否允许议价（可选，默认false） */
    private Boolean allowBargain;

    /** 库存数量，不能为空，至少为1 */
    @NotNull(message = "库存不能为空")
    @Min(value = 1, message = "库存至少为1")
    private Integer stock;

    /** 新旧程度，不能为空：全新/几乎全新/轻微使用/明显使用 */
    @NotBlank(message = "新旧程度不能为空")
    private String newDegree;

    /** 商品图片URL列表（可选） */
    private List<String> imageUrls;
}
