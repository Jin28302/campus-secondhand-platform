package com.example.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.common.R;
import com.example.backend.entity.PointsLog;
import com.example.backend.service.PointsLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 积分控制器 - 查询积分变动记录
@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsLogService pointsLogService;

    // 积分明细 - 分页查询当前用户的积分获取和使用记录
    @GetMapping("/log")
    public R<IPage<PointsLog>> log(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(pointsLogService.myLogs(pageNum, pageSize));
    }
}
