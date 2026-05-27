package com.ruanjian.qasystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruanjian.qasystem.common.QaIntent;
import com.ruanjian.qasystem.graph.GraphQueryService;
import com.ruanjian.qasystem.model.dto.QaAskRequest;
import com.ruanjian.qasystem.model.dto.QaFeedbackRequest;
import com.ruanjian.qasystem.model.vo.*;
import com.ruanjian.qasystem.service.AnswerBuilder;
import com.ruanjian.qasystem.service.EntityExtractor;
import com.ruanjian.qasystem.service.LlmAnswerEnhancer;
import com.ruanjian.qasystem.service.IntentResolver;
import com.ruanjian.qasystem.service.LlmIntentParser;
import com.ruanjian.qasystem.service.QaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.builder.BuilderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruanjian.qasystem.model.entity.QaMessage;
import com.ruanjian.qasystem.repository.QaMessageMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.RequestParam;
import com.ruanjian.qasystem.common.BusinessException;


import java.util.List;
import java.util.Optional;


/**
 * 问答主流程。
 * 当前阶段只完成：意图识别 + 实体抽取。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QaServiceImpl implements QaService {

    private final IntentResolver intentResolver;

    private final EntityExtractor entityExtractor;

    private  final AnswerBuilder answerBuilder;

    private final GraphQueryService graphQueryService;

    private final QaMessageMapper qaMessageMapper;

    private LlmIntentParser llmIntentParser;

    private LlmAnswerEnhancer llmAnswerEnhancer;

    @Autowired(required = false)
    public void setLlmIntentParser(LlmIntentParser llmIntentParser) {
        this.llmIntentParser = llmIntentParser;
    }

    @Autowired(required = false)
    public void setLlmAnswerEnhancer(LlmAnswerEnhancer llmAnswerEnhancer) {
        this.llmAnswerEnhancer = llmAnswerEnhancer;
    }

    @Override
    public QaAskResponse ask(QaAskRequest request) {
        Optional<LlmParseResult> llmResult = parseWithLlm(request.getQuestion());
        QaIntent intent = llmResult
                .map(LlmParseResult::intent)
                .orElseGet(() -> intentResolver.resolve(request.getQuestion()));
        ExtractedEntity entity = llmResult
                .map(result -> new ExtractedEntity(result.keyword()))
                .orElseGet(() -> entityExtractor.extract(request.getQuestion(), intent));
        String parseSource = llmResult.isPresent() ? "llm" : "rule";

        if(intent==QaIntent.UNKNOWN){
            QaAskResponse response=new QaAskResponse(
                    request.getQuestion(),
                    "暂不支持该类问题",
                    false,
                    "no_data",
                    intent.name(),
                    toQuestionType(intent),
                    null,
                    null,
                    List.of(),
                    entity.getKeyword(),
                    parseSource
            );
            saveQaMessage(request,response);
            return  response;
        }
        QaFact fact=null;

        String answer;
        String qaStatus;
        String answerSource = "template";
        try {
            fact = graphQueryService.query(intent,entity.getKeyword());
            String baseAnswer = answerBuilder.build(intent,entity.getKeyword(),fact);
            Optional<String> enhancedAnswer = enhanceAnswer(request.getQuestion(), intent, entity.getKeyword(), fact, baseAnswer);
            answer = enhancedAnswer.orElse(baseAnswer);
            answerSource = enhancedAnswer.isPresent() ? "llm" : "template";
            qaStatus = fact ==null ?"no_data":"success";
        }catch(Exception e){
            log.error("知识图谱查询失败，question={}, intent={}, keyword={}",
                    request.getQuestion(), intent, entity.getKeyword(), e);
            answer ="系统暂时无法查询知识图谱，请稍后再试";
            qaStatus="error";
        }
        boolean matched = fact != null;
        QaAskResponse response =new QaAskResponse(
                request.getQuestion(),
                answer,
                matched,
                qaStatus,
                intent.name(),
                toQuestionType(intent),
                fact==null?null:fact.getSourceName(),
                fact==null?null:fact.getSourceUrl(),
                buildSources(fact),
                entity.getKeyword(),
                parseSource,
                answerSource
        );
        saveQaMessage(request,response);
        return response;
    }

    @Override
    public PageResult<QaHistoryItem> listHistory(Long userId,Long page,Long  pageSize) {
        long current = page == null || page < 1 ? 1 : page;
        long size = pageSize == null || pageSize < 1 ? 20 : pageSize;

        Page<QaMessage> pageParam = new Page<>(current, size);

        Page<QaMessage> result = qaMessageMapper.selectPage(
                pageParam,
                new LambdaQueryWrapper<QaMessage>()
                        .eq(QaMessage::getUserId,userId)
                        .orderByDesc(QaMessage::getCreatedAt)
        );

        List<QaHistoryItem> records = result.getRecords()
                .stream()
                .map(message -> new QaHistoryItem(
                        message.getId(),
                        message.getQuestion(),
                        message.getAnswer(),
                        message.getIntent(),
                        message.getQuestionType(),
                        message.getKeywordText(),
                        message.getMatched(),
                        message.getQaStatus(),
                        message.getSourceName(),
                        message.getSourceUrl(),
                        message.getFeedbackType(),
                        message.getFeedbackRemark(),
                        message.getCreatedAt()
                ))
                .toList();

        return new PageResult<>(
                records,
                result.getTotal(),
                current,
                size
        );
    }

    @Override
    public QaHistoryItem getMessage(Long id){
        QaMessage message=qaMessageMapper.selectById(id);
        if(message==null){
            throw new BusinessException(404,"问答记录不存在");
        }
        return new QaHistoryItem(
                message.getId(),
                message.getQuestion(),
                message.getAnswer(),
                message.getIntent(),
                message.getQuestionType(),
                message.getKeywordText(),
                message.getMatched(),
                message.getQaStatus(),
                message.getSourceName(),
                message.getSourceUrl(),
                message.getFeedbackType(),
                message.getFeedbackRemark(),
                message.getCreatedAt()
        );
    }

    @Override
    public List<QaHotQuestion> listHotQuestions(Integer limit) {
        int size = limit == null || limit < 1 ? 10 : limit;
        return qaMessageMapper.selectHotQuestions(size);
    }
    private QaFact mockFact(QaIntent intent){
        if(intent==QaIntent.UNKNOWN){
            return null;
        }
        return new QaFact(
                "大英博物馆",
                "模拟数据源",
                "https://example.com"
        );
    }
    private List<QaSource> buildSources(QaFact fact) {
        if (fact == null || fact.getSourceName() == null || fact.getSourceName().isBlank()) {
            return List.of();
        }

        return List.of(new QaSource(
                "knowledge_graph",
                null,
                fact.getSourceName(),
                fact.getSourceUrl()
        ));
    }
    private Optional<LlmParseResult> parseWithLlm(String question) {
        if (llmIntentParser == null) {
            return Optional.empty();
        }
        return llmIntentParser.parse(question);
    }
    private Optional<String> enhanceAnswer(String question, QaIntent intent, String keyword, QaFact fact, String baseAnswer) {
        if (llmAnswerEnhancer == null || fact == null) {
            return Optional.empty();
        }
        return llmAnswerEnhancer.enhance(question, intent, keyword, fact, baseAnswer);
    }
    private String toQuestionType(QaIntent intent) {
        return switch (intent) {
            case ARTIFACT_MUSEUM,
                 ARTIFACT_DYNASTY,
                 ARTIFACT_MATERIAL,
                 ARTIFACT_TYPE,
                 ARTIFACT_DESCRIPTION,
                 ARTIFACT_ARTIST,
                 ARTIST_BIOGRAPHY,
                 ARTIFACT_SIZE -> "attribute";
            case ARTIST_WORKS,
                 DYNASTY_ARTIFACTS,
                 RELATED_ARTIFACTS,
                 MUSEUM_ARTIFACTS,
                 TYPE_ARTIFACTS,
                 MATERIAL_ARTIFACTS -> "relation";
            case MUSEUM_ARTIFACT_COUNT -> "statistic";
            default -> "unknown";
        };
    }
    private void saveQaMessage(QaAskRequest request, QaAskResponse response) {
        QaMessage message = new QaMessage();
        message.setUserId(request.getUserId());
        message.setQuestion(request.getQuestion());
        message.setAnswer(response.getAnswer());
        message.setIntent(response.getIntent());
        message.setQuestionType(response.getQuestionType());
        message.setKeywordText(response.getKeyword());
        message.setMatched(response.getMatched());
        message.setQaStatus(response.getQaStatus());
        message.setSourceName(response.getSourceName());
        message.setSourceUrl(response.getSourceUrl());

        qaMessageMapper.insert(message);
    }
    @Override
    public Boolean submitFeedback(Long id, QaFeedbackRequest request) {
        QaMessage message = qaMessageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException(404, "问答记录不存在");
        }

        message.setFeedbackType(request.getFeedbackType());
        message.setFeedbackRemark(request.getFeedbackRemark());

        qaMessageMapper.updateById(message);
        return true;
    }
    @Override
    public QaFeedbackStatistics getFeedbackStatistics() {
        QaFeedbackStatistics statistics = qaMessageMapper.selectFeedbackStatistics();
        if (statistics == null) {
            return new QaFeedbackStatistics(0L, 0L);
        }

        Long helpfulCount = statistics.getHelpfulCount() == null ? 0L : statistics.getHelpfulCount();
        Long inaccurateCount = statistics.getInaccurateCount() == null ? 0L : statistics.getInaccurateCount();
        return new QaFeedbackStatistics(helpfulCount, inaccurateCount);
    }

}
