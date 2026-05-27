package com.ruanjian.qasystem.service.impl;

import com.ruanjian.qasystem.common.BusinessException;
import com.ruanjian.qasystem.common.QaIntent;
import com.ruanjian.qasystem.graph.GraphQueryService;
import com.ruanjian.qasystem.model.dto.QaAskRequest;
import com.ruanjian.qasystem.model.dto.QaFeedbackRequest;
import com.ruanjian.qasystem.model.entity.QaMessage;
import com.ruanjian.qasystem.model.vo.LlmParseResult;
import com.ruanjian.qasystem.model.vo.QaAskResponse;
import com.ruanjian.qasystem.model.vo.QaFact;
import com.ruanjian.qasystem.repository.QaMessageMapper;
import com.ruanjian.qasystem.service.AnswerBuilder;
import com.ruanjian.qasystem.service.EntityExtractor;
import com.ruanjian.qasystem.service.IntentResolver;
import com.ruanjian.qasystem.service.LlmAnswerEnhancer;
import com.ruanjian.qasystem.service.LlmIntentParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QaServiceImplTest {

    private GraphQueryService graphQueryService;
    private QaMessageMapper qaMessageMapper;
    private QaServiceImpl qaService;

    @BeforeEach
    void setUp() {
        graphQueryService = mock(GraphQueryService.class);
        qaMessageMapper = mock(QaMessageMapper.class);
        qaService = new QaServiceImpl(
                new IntentResolver(),
                new EntityExtractor(),
                new AnswerBuilder(),
                graphQueryService,
                qaMessageMapper
        );
    }

    @Test
    void shouldAnswerAndSaveQaMessage() {
        QaAskRequest request = askRequest("青花瓷瓶收藏在哪个博物馆？");
        when(graphQueryService.query(QaIntent.ARTIFACT_MUSEUM, "青花瓷瓶"))
                .thenReturn(new QaFact("大英博物馆", "知识图谱", "https://example.com"));

        QaAskResponse response = qaService.ask(request);

        assertEquals("青花瓷瓶收藏在哪个博物馆？", response.getQuestion());
        assertTrue(response.getAnswer().contains("收藏地"));
        assertTrue(response.getAnswer().contains("大英博物馆"));
        assertEquals(Boolean.TRUE, response.getMatched());
        assertEquals("ARTIFACT_MUSEUM", response.getIntent());
        assertEquals("attribute", response.getQuestionType());
        assertEquals("青花瓷瓶", response.getKeyword());
        assertEquals("rule", response.getParseSource());
        assertEquals("template", response.getAnswerSource());
        assertNotNull(response.getSources());
        assertEquals(1, response.getSources().size());
        assertEquals("success", response.getQaStatus());

        QaMessage saved = captureSavedMessage();
        assertEquals(1L, saved.getUserId());
        assertEquals("青花瓷瓶收藏在哪个博物馆？", saved.getQuestion());
        assertEquals(response.getAnswer(), saved.getAnswer());
        assertEquals("ARTIFACT_MUSEUM", saved.getIntent());
        assertEquals("attribute", saved.getQuestionType());
        assertEquals("青花瓷瓶", saved.getKeywordText());
        assertEquals(Boolean.TRUE, saved.getMatched());
        assertEquals("知识图谱", saved.getSourceName());
        assertEquals("https://example.com", saved.getSourceUrl());
        assertEquals("success", saved.getQaStatus());
    }

    @Test
    void shouldUseLlmAnswerWhenEnhancerReturnsAnswer() {
        LlmAnswerEnhancer enhancer = mock(LlmAnswerEnhancer.class);
        qaService.setLlmAnswerEnhancer(enhancer);

        QaAskRequest request = askRequest("青花瓷瓶收藏在哪里？");
        QaFact fact = new QaFact("大英博物馆", "知识图谱", "https://example.com");
        when(graphQueryService.query(QaIntent.ARTIFACT_MUSEUM, "青花瓷瓶")).thenReturn(fact);
        when(enhancer.enhance(any(), any(), any(), any(), any()))
                .thenReturn(Optional.of("青花瓷瓶现收藏于大英博物馆。这是由 Ollama 根据知识图谱事实润色后的回答。"));

        QaAskResponse response = qaService.ask(request);

        assertEquals("llm", response.getAnswerSource());
        assertTrue(response.getAnswer().contains("Ollama"));
    }

    @Test
    void shouldSaveQaMessageWhenGraphQueryFails() {
        QaAskRequest request = askRequest("青花瓷瓶收藏在哪个博物馆？");
        when(graphQueryService.query(QaIntent.ARTIFACT_MUSEUM, "青花瓷瓶"))
                .thenThrow(new RuntimeException("Neo4j unavailable"));

        QaAskResponse response = qaService.ask(request);

        assertEquals("系统暂时无法查询知识图谱，请稍后再试", response.getAnswer());
        assertEquals(Boolean.FALSE, response.getMatched());
        assertEquals("error", response.getQaStatus());
        assertEquals("ARTIFACT_MUSEUM", response.getIntent());
        assertEquals("attribute", response.getQuestionType());

        QaMessage saved = captureSavedMessage();
        assertEquals("系统暂时无法查询知识图谱，请稍后再试", saved.getAnswer());
        assertEquals(Boolean.FALSE, saved.getMatched());
        assertEquals("error", saved.getQaStatus());
    }

    @Test
    void shouldNotQueryGraphWhenIntentIsUnknown() {
        QaAskRequest request = askRequest("今天天气怎么样？");

        QaAskResponse response = qaService.ask(request);

        assertEquals("暂不支持该类问题", response.getAnswer());
        assertEquals(Boolean.FALSE, response.getMatched());
        assertEquals("no_data", response.getQaStatus());
        assertEquals("UNKNOWN", response.getIntent());
        assertEquals("unknown", response.getQuestionType());
        assertEquals("rule", response.getParseSource());

        verify(graphQueryService, never()).query(any(), any());
    }

    @Test
    void shouldSaveNoDataQaMessageWhenGraphReturnsNull() {
        QaAskRequest request = askRequest("不存在的文物收藏在哪个博物馆？");
        when(graphQueryService.query(QaIntent.ARTIFACT_MUSEUM, "不存在的文物"))
                .thenReturn(null);

        QaAskResponse response = qaService.ask(request);

        assertEquals("暂无相关数据", response.getAnswer());
        assertEquals(Boolean.FALSE, response.getMatched());
        assertEquals("no_data", response.getQaStatus());
        assertEquals("ARTIFACT_MUSEUM", response.getIntent());
        assertEquals("attribute", response.getQuestionType());
        assertEquals("不存在的文物", response.getKeyword());

        QaMessage saved = captureSavedMessage();
        assertEquals("暂无相关数据", saved.getAnswer());
        assertEquals(Boolean.FALSE, saved.getMatched());
        assertEquals("no_data", saved.getQaStatus());
    }

    @Test
    void shouldSubmitFeedback() {
        QaFeedbackRequest request = new QaFeedbackRequest();
        request.setFeedbackType("helpful");
        request.setFeedbackRemark("回答有用");

        QaMessage message = new QaMessage();
        message.setId(1L);
        when(qaMessageMapper.selectById(1L)).thenReturn(message);

        Boolean result = qaService.submitFeedback(1L, request);

        assertEquals(Boolean.TRUE, result);
        assertEquals("helpful", message.getFeedbackType());
        assertEquals("回答有用", message.getFeedbackRemark());
        verify(qaMessageMapper).updateById(message);
    }

    @Test
    void shouldThrowBusinessExceptionWhenFeedbackMessageNotFound() {
        QaFeedbackRequest request = new QaFeedbackRequest();
        request.setFeedbackType("helpful");
        when(qaMessageMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> qaService.submitFeedback(999L, request)
        );

        assertEquals(404, exception.getCode());
        assertEquals("问答记录不存在", exception.getMessage());
    }

    @Test
    void shouldUseLlmParseResultBeforeRuleResolver() {
        LlmIntentParser llmIntentParser = mock(LlmIntentParser.class);
        qaService.setLlmIntentParser(llmIntentParser);

        QaAskRequest request = askRequest("这件瓶子现在在哪里？");
        when(llmIntentParser.parse("这件瓶子现在在哪里？"))
                .thenReturn(Optional.of(new LlmParseResult(QaIntent.ARTIFACT_MUSEUM, "青花瓷瓶")));
        when(graphQueryService.query(QaIntent.ARTIFACT_MUSEUM, "青花瓷瓶"))
                .thenReturn(new QaFact("大英博物馆", "知识图谱", "https://example.com"));

        QaAskResponse response = qaService.ask(request);

        assertEquals("ARTIFACT_MUSEUM", response.getIntent());
        assertEquals("青花瓷瓶", response.getKeyword());
        assertEquals("llm", response.getParseSource());
        assertTrue(response.getAnswer().contains("大英博物馆"));
        verify(graphQueryService).query(QaIntent.ARTIFACT_MUSEUM, "青花瓷瓶");
    }

    @Test
    void shouldFallbackToRuleResolverWhenLlmReturnsEmpty() {
        LlmIntentParser llmIntentParser = mock(LlmIntentParser.class);
        qaService.setLlmIntentParser(llmIntentParser);

        QaAskRequest request = askRequest("青花瓷瓶收藏在哪个博物馆？");
        when(llmIntentParser.parse("青花瓷瓶收藏在哪个博物馆？")).thenReturn(Optional.empty());
        when(graphQueryService.query(QaIntent.ARTIFACT_MUSEUM, "青花瓷瓶"))
                .thenReturn(new QaFact("大英博物馆", "知识图谱", "https://example.com"));

        QaAskResponse response = qaService.ask(request);

        assertEquals("ARTIFACT_MUSEUM", response.getIntent());
        assertEquals("青花瓷瓶", response.getKeyword());
        assertEquals("rule", response.getParseSource());
        assertTrue(response.getAnswer().contains("大英博物馆"));
        verify(graphQueryService).query(QaIntent.ARTIFACT_MUSEUM, "青花瓷瓶");
    }

    private QaAskRequest askRequest(String question) {
        QaAskRequest request = new QaAskRequest();
        request.setUserId(1L);
        request.setQuestion(question);
        return request;
    }

    private QaMessage captureSavedMessage() {
        ArgumentCaptor<QaMessage> captor = ArgumentCaptor.forClass(QaMessage.class);
        verify(qaMessageMapper).insert(captor.capture());
        return captor.getValue();
    }
}
