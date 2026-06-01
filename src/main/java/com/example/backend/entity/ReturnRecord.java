package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 退货记录实体 - 对应 return_record 表
 * 每个退货申请对应一条记录，记录退货原因、审核结果等信息
 * 退货状态：pending（待审核）、approved（已同意）、rejected（已拒绝）
 */
@Data
@TableName("return_record")
public class ReturnRecord {

    /** 退货记录ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的订单ID */
    private Long orderId;

    /** 申请退货的用户ID（买家） */
    private Long userId;

    /** 商家用户ID */
    private Long sellerId;

    /** 退货原因（用户填写） */
    private String reason;

    /** 拒绝原因（商家拒绝退货时填写） */
    private String rejectReason;

    /** 退货状态：pending（待审核）、approved（已同意）、rejected（已拒绝） */
    private String status;

    /** 退货申请时间 */
    private LocalDateTime applyTime;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
