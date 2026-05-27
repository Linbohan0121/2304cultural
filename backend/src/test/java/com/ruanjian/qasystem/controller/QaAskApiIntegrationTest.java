package com.ruanjian.qasystem.controller;

import com.ruanjian.qasystem.common.QaIntent;
import com.ruanjian.qasystem.graph.GraphQueryService;
import com.ruanjian.qasystem.model.vo.QaFact;
import com.ruanjian.qasystem.repository.QaMessageMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "qa.llm.enabled=false")
@AutoConfigureMockMvc
class QaAskApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GraphQueryService graphQueryService;

    @MockitoBean
    private QaMessageMapper qaMessageMapper;

    @ParameterizedTest
    @MethodSource("apiCases")
    void shouldReturnExpectedAskResponse(
            String question,
            QaIntent intent,
            String keyword,
            String factValue,
            String questionType,
            String expectedPhrase
    ) throws Exception {
        when(graphQueryService.query(intent, keyword))
                .thenReturn(new QaFact(factValue, "知识图谱", "https://example.com"));

        mockMvc.perform(post("/api/qa/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "question": "%s"
                                }
                                """.formatted(question)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.question").value(question))
                .andExpect(jsonPath("$.data.answer", containsString(expectedPhrase)))
                .andExpect(jsonPath("$.data.answer", containsString(factValue)))
                .andExpect(jsonPath("$.data.matched").value(true))
                .andExpect(jsonPath("$.data.qaStatus").value("success"))
                .andExpect(jsonPath("$.data.intent").value(intent.name()))
                .andExpect(jsonPath("$.data.questionType").value(questionType))
                .andExpect(jsonPath("$.data.keyword").value(keyword))
                .andExpect(jsonPath("$.data.parseSource").value("rule"))
                .andExpect(jsonPath("$.data.answerSource").value("template"))
                .andExpect(jsonPath("$.data.sourceName").value("知识图谱"))
                .andExpect(jsonPath("$.data.sourceUrl").value("https://example.com"))
                .andExpect(jsonPath("$.data.sources[0].type").value("knowledge_graph"))
                .andExpect(jsonPath("$.data.sources[0].name").value("知识图谱"))
                .andExpect(jsonPath("$.data.sources[0].url").value("https://example.com"));
    }

    private static Stream<Object[]> apiCases() {
        return Stream.of(
                new Object[]{"青花瓷瓶收藏在哪个博物馆？", QaIntent.ARTIFACT_MUSEUM, "青花瓷瓶", "大英博物馆", "attribute", "收藏地"},
                new Object[]{"张大千还有哪些作品？", QaIntent.ARTIST_WORKS, "张大千", "张大千山水图、仿黄公望山水图", "relation", "相关的作品包括"},
                new Object[]{"大英博物馆收藏了多少件中国文物？", QaIntent.MUSEUM_ARTIFACT_COUNT, "大英博物馆", "2", "statistic", "数量为 2 件"},
                new Object[]{"瓷器有哪些文物？", QaIntent.TYPE_ARTIFACTS, "瓷器", "青花瓷瓶、青花瓷盘", "relation", "瓷器类文物包括"},
                new Object[]{"陶瓷有哪些文物？", QaIntent.MATERIAL_ARTIFACTS, "陶瓷", "青花瓷瓶、青花瓷盘", "relation", "陶瓷材质文物包括"}
        );
    }
}
