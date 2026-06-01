package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分记录实体 - 对应 points_log 表
 * 记录用户积分变动明细（每次购物获得积分、积分抵扣消耗等）
 * amount 为正数表示获得积分，为负数表示消耗积分
 */
@Data
@TableName("points_log")
public class PointsLog {

    /** 记录ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 积分变动数量：正数=获得，负数=消耗 */
    private Integer amount;

    /** 积分变动原因描述 */
    private String reason;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
