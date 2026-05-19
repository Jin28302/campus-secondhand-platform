package com.example.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.entity.PointsLog;

public interface PointsLogService extends IService<PointsLog> {

    void record(Long userId, int delta, String reason);

    IPage<PointsLog> myLogs(int pageNum, int pageSize);
}
