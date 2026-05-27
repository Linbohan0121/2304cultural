package com.ruanjian.qasystem.controller;

import com.ruanjian.qasystem.common.ApiResponse;
import com.ruanjian.qasystem.model.vo.PageResult;
import com.ruanjian.qasystem.model.vo.QaAdminStatistics;
import com.ruanjian.qasystem.model.vo.QaHistoryItem;
import com.ruanjian.qasystem.service.QaAdminService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/qa")
@RequiredArgsConstructor
public class AdminQaController {

    private final QaAdminService qaAdminService;

    @GetMapping("/messages")
    public ApiResponse<PageResult<QaHistoryItem>> listMessages(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码不能小于1") Long page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "每页数量不能小于1")
            @Max(value = 100, message = "每页数量不能超过100") Long pageSize,
            @RequestParam(required = false) String qaStatus,
            @RequestParam(required = false) String feedbackType,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(qaAdminService.listMessages(page, pageSize, qaStatus, feedbackType, keyword));
    }

    @GetMapping("/statistics")
    public ApiResponse<QaAdminStatistics> statistics() {
        return ApiResponse.success(qaAdminService.getStatistics());
    }
}
