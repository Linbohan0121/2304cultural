package com.ruanjian.qasystem.controller;

import com.ruanjian.qasystem.common.GlobalExceptionHandler;
import com.ruanjian.qasystem.model.vo.PageResult;
import com.ruanjian.qasystem.model.vo.QaAdminStatistics;
import com.ruanjian.qasystem.model.vo.QaHistoryItem;
import com.ruanjian.qasystem.service.QaAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminQaController.class)
@Import(GlobalExceptionHandler.class)
class AdminQaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QaAdminService qaAdminService;

    @Test
    void shouldListMessages() throws Exception {
        QaHistoryItem item = new QaHistoryItem(
                1L,
                "青花瓷瓶收藏在哪个博物馆？",
                "“青花瓷瓶”现收藏于大英博物馆。",
                "ARTIFACT_MUSEUM",
                "attribute",
                "青花瓷瓶",
                true,
                "success",
                "大英博物馆",
                "https://example.com",
                "helpful",
                "回答准确",
                LocalDateTime.now()
        );

        when(qaAdminService.listMessages(1L, 20L, "success", "helpful", "青花瓷瓶"))
                .thenReturn(new PageResult<>(List.of(item), 1L, 1L, 20L));

        mockMvc.perform(get("/api/admin/qa/messages")
                        .param("page", "1")
                        .param("pageSize", "20")
                        .param("qaStatus", "success")
                        .param("feedbackType", "helpful")
                        .param("keyword", "青花瓷瓶"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].question").value("青花瓷瓶收藏在哪个博物馆？"))
                .andExpect(jsonPath("$.data.records[0].qaStatus").value("success"))
                .andExpect(jsonPath("$.data.records[0].feedbackType").value("helpful"));
    }

    @Test
    void shouldReturnStatistics() throws Exception {
        when(qaAdminService.getStatistics())
                .thenReturn(new QaAdminStatistics(10L, 7L, 2L, 1L, 5L, 3L));

        mockMvc.perform(get("/api/admin/qa/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCount").value(10))
                .andExpect(jsonPath("$.data.matchedCount").value(7))
                .andExpect(jsonPath("$.data.noDataCount").value(2))
                .andExpect(jsonPath("$.data.errorCount").value(1))
                .andExpect(jsonPath("$.data.helpfulCount").value(5))
                .andExpect(jsonPath("$.data.inaccurateCount").value(3));
    }
}
