package com.ruanjian.qasystem.controller;

import com.ruanjian.qasystem.model.vo.QaAskResponse;
import com.ruanjian.qasystem.model.vo.QaSource;
import com.ruanjian.qasystem.service.QaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import com.ruanjian.qasystem.common.GlobalExceptionHandler;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QaController.class)
@Import(GlobalExceptionHandler.class)
class QaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QaService qaService;

    @Test
    void shouldAskQuestion() throws Exception {
        QaAskResponse response = new QaAskResponse(
                "青花瓷瓶收藏在哪个博物馆？",
                "“青花瓷瓶”现收藏于大英博物馆。",
                true,
                "success",
                "ARTIFACT_MUSEUM",
                "attribute",
                "大英博物馆",
                "https://example.com",
                List.of(new QaSource("knowledge_graph", null, "大英博物馆", "https://example.com")),
                "青花瓷瓶"
        );

        when(qaService.ask(any())).thenReturn(response);

        mockMvc.perform(post("/api/qa/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "question": "青花瓷瓶收藏在哪个博物馆？"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.answer").value("“青花瓷瓶”现收藏于大英博物馆。"))
                .andExpect(jsonPath("$.data.questionType").value("attribute"))
                .andExpect(jsonPath("$.data.qaStatus").value("success"));
    }

    @Test
    void shouldRejectBlankQuestion() throws Exception {
        mockMvc.perform(post("/api/qa/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "question": " "
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("问题不能为空"));
    }

    @Test
    void shouldRejectInvalidHistoryUserId() throws Exception {
        mockMvc.perform(get("/api/qa/history")
                        .param("userId", "0")
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户ID不合法"));
    }

    @Test
    void shouldRejectInvalidMessageId() throws Exception {
        mockMvc.perform(get("/api/qa/messages/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("问答记录ID不合法"));
    }
}