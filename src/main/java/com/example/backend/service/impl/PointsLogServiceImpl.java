package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.UserContext;
import com.example.backend.entity.PointsLog;
import com.example.backend.mapper.PointsLogMapper;
import com.example.backend.service.PointsLogService;
import org.springframework.stereotype.Service;

@Service
public class PointsLogServiceImpl extends ServiceImpl<PointsLogMapper, PointsLog>
        implements PointsLogService {

    @Override
    public void record(Long userId, int delta, String reason) {
        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setAmount(delta);
        log.setReason(reason);
        save(log);
    }

    @Override
    public IPage<PointsLog> myLogs(int pageNum, int pageSize) {
        Long userId = UserContext.get().userId();
        return page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<PointsLog>()
                        .eq(PointsLog::getUserId, userId)
                        .orderByDesc(PointsLog::getCreatedAt));
    }
}
