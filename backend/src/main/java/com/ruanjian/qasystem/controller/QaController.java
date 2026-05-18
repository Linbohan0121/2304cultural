package com.ruanjian.qasystem.controller;

import com.ruanjian.qasystem.common.ApiResponse;
import com.ruanjian.qasystem.common.QaIntent;
import com.ruanjian.qasystem.model.dto.QaAskRequest;
import com.ruanjian.qasystem.model.vo.ExtractedEntity;
import com.ruanjian.qasystem.model.vo.PageResult;
import com.ruanjian.qasystem.model.vo.QaAskResponse;
import com.ruanjian.qasystem.model.vo.QaHistoryItem;
import com.ruanjian.qasystem.service.EntityExtractor;
import com.ruanjian.qasystem.service.IntentResolver;
import com.ruanjian.qasystem.service.QaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long pageSize
    ) {
        return ApiResponse.success(qaService.listHistory(page, pageSize));
    }


}
