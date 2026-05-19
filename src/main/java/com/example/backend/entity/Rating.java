package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("rating")
public class Rating {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long fromUserId;

    private Long toUserId;

    private String targetType;

    private Integer score;

    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
