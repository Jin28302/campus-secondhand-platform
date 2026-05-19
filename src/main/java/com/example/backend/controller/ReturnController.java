package com.example.backend.controller;

import com.example.backend.common.R;
import com.example.backend.common.RequireRole;
import com.example.backend.dto.ReturnApplyDTO;
import com.example.backend.dto.ReturnAuditDTO;
import com.example.backend.service.ReturnRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnRecordService returnRecordService;

    @PutMapping("/order/{id}/return")
    public R<Void> applyReturn(@PathVariable Long id, @RequestBody @Valid ReturnApplyDTO dto) {
        returnRecordService.applyReturn(id, dto);
        return R.ok();
    }

    @PutMapping("/return/{id}/audit")
    @RequireRole({"商家"})
    public R<Void> audit(@PathVariable Long id, @RequestBody @Valid ReturnAuditDTO dto) {
        returnRecordService.audit(id, dto);
        return R.ok();
    }
}
