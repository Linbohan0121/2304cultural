package com.ruanjian.qasystem.controller;

import com.ruanjian.qasystem.common.ApiResponse;
import com.ruanjian.qasystem.common.QaIntent;
import com.ruanjian.qasystem.model.dto.QaAskRequest;
import com.ruanjian.qasystem.model.dto.QaFeedbackRequest;
import com.ruanjian.qasystem.model.vo.*;
import com.ruanjian.qasystem.service.EntityExtractor;
import com.ruanjian.qasystem.service.IntentResolver;
import com.ruanjian.qasystem.service.QaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/qa")
@RequiredArgsConstructor
public class QaController {

    private final QaService qaService;

    @PostMapping("/ask")
    public ApiResponse<QaAskResponse> ask(@Valid @RequestBody QaAskRequest request) {
        return ApiResponse.success(qaService.ask(request));
    }
    @GetMapping("/history")
    public ApiResponse<PageResult<QaHistoryItem>> history(
            @RequestParam @Min(value=1,message = "用户ID不合法") Long userId,
            @RequestParam(defaultValue = "1")@Min(value=1,message = "页码不能小于1")Long page,
            @RequestParam(defaultValue = "20")
            @Min(value=1,message = "每页数量不能小于1")
            @Max(value = 100,message="每页数量不超过100")Long pageSize
    ) {
        return ApiResponse.success(qaService.listHistory(userId,page, pageSize));
    }
    @GetMapping("/messages/{id}")
    public ApiResponse<QaHistoryItem> getMessage(
            @PathVariable
            @Min(value=1,message = "问答记录ID不合法")Long id){
        return ApiResponse.success(qaService.getMessage(id));
    }
    @PatchMapping("/messages/{id}/feedback")
    public ApiResponse<Boolean> submitFeedback(
            @PathVariable @Min(value = 1, message = "问答记录ID不合法") Long id,
            @Valid @RequestBody QaFeedbackRequest request
    ) {
        return ApiResponse.success(qaService.submitFeedback(id, request));
    }
    @GetMapping("/hot")
    public ApiResponse<List<QaHotQuestion>> hot(
            @RequestParam(defaultValue = "10")
            @Min(value=1,message = "每页数量不能小于1")
            @Max(value = 100,message="每页数量不超过100") Integer limit
    ) {
        return ApiResponse.success(qaService.listHotQuestions(limit));
    }

}
