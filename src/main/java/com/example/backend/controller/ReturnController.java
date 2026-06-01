package com.example.backend.controller;

import com.example.backend.common.R;
import com.example.backend.common.RequireRole;
import com.example.backend.dto.ReturnApplyDTO;
import com.example.backend.dto.ReturnAuditDTO;
import com.example.backend.service.ReturnRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

// 退货/退款控制器 - 处理退货申请和审核
@RestController
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnRecordService returnRecordService;

    // 申请退货/退款 - 买家对已完成的订单申请退货退款
    @PutMapping("/order/{id}/return")
    public R<Void> applyReturn(@PathVariable Long id, @RequestBody @Valid ReturnApplyDTO dto) {
        returnRecordService.applyReturn(id, dto);
        return R.ok();
    }

    // 审核退货/退款 - 商家或管理员审核买家的退货申请（按退货记录ID）
    @PutMapping("/return/{id}/audit")
    @RequireRole({"商家", "管理员"})
    public R<Void> audit(@PathVariable Long id, @RequestBody @Valid ReturnAuditDTO dto) {
        returnRecordService.audit(id, dto);
        return R.ok();
    }

    // 审核退货/退款 - 按订单ID审核（前端传订单ID更方便）
    @PutMapping("/return/order/{orderId}/audit")
    @RequireRole({"商家", "管理员"})
    public R<Void> auditByOrderId(@PathVariable Long orderId, @RequestBody @Valid ReturnAuditDTO dto) {
        returnRecordService.auditByOrderId(orderId, dto);
        return R.ok();
    }
}
