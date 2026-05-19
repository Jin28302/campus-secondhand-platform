package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.dto.ReturnApplyDTO;
import com.example.backend.dto.ReturnAuditDTO;
import com.example.backend.entity.ReturnRecord;

public interface ReturnRecordService extends IService<ReturnRecord> {

    void applyReturn(Long orderId, ReturnApplyDTO dto);

    void audit(Long returnId, ReturnAuditDTO dto);
}
