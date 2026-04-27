package com.ruanjian.qasystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruanjian.qasystem.common.QaIntent;
import com.ruanjian.qasystem.graph.GraphQueryService;
import com.ruanjian.qasystem.model.dto.QaAskRequest;
import com.ruanjian.qasystem.model.vo.ExtractedEntity;
import com.ruanjian.qasystem.model.vo.QaAskResponse;
import com.ruanjian.qasystem.model.vo.QaFact;
import com.ruanjian.qasystem.model.vo.QaHistoryItem;
import com.ruanjian.qasystem.service.AnswerBuilder;
import com.ruanjian.qasystem.service.EntityExtractor;
import com.ruanjian.qasystem.service.IntentResolver;
import com.ruanjian.qasystem.service.QaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.ruanjian.qasystem.model.entity.QaMessage;
import com.ruanjian.qasystem.repository.QaMessageMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruanjian.qasystem.model.vo.PageResult;

import java.util.List;


/**
 * 问答主流程。
 * 当前阶段只完成：意图识别 + 实体抽取。
 */
@Service
@RequiredArgsConstructor
public class QaServiceImpl implements QaService {

    private final IntentResolver intentResolver;

    private final EntityExtractor entityExtractor;

    private  final AnswerBuilder answerBuilder;

    private final GraphQueryService graphQueryService;

    private final QaMessageMapper qaMessageMapper;


    @Override
    public QaAskResponse ask(QaAskRequest request) {
        QaIntent intent = intentResolver.resolve(request.getQuestion());
        ExtractedEntity entity = entityExtractor.extract(request.getQuestion());

        QaFact fact=graphQueryService.query(intent,entity.getKeyword());
        String answer=answerBuilder.build(intent,entity.getKeyword(),fact);
        QaAskResponse response =new QaAskResponse(
                answer,
                fact!=null,
                intent.name(),
                entity.getKeyword(),
                fact==null?null:fact.getSourceName(),
                fact==null?null:fact.getSourceUrl()
        );
        saveQaMessage(request,response);
        return response;
    }

    @Override
    public PageResult<QaHistoryItem> listHistory(Long page, Long pageSize) {
        long current = page == null || page < 1 ? 1 : page;
        long size = pageSize == null || pageSize < 1 ? 10 : pageSize;

        Page<QaMessage> pageParam = new Page<>(current, size);

        Page<QaMessage> result = qaMessageMapper.selectPage(
                pageParam,
                new LambdaQueryWrapper<QaMessage>()
                        .orderByDesc(QaMessage::getCreatedAt)
        );

        List<QaHistoryItem> records = result.getRecords()
                .stream()
                .map(message -> new QaHistoryItem(
                        message.getId(),
                        message.getQuestion(),
                        message.getAnswer(),
                        message.getIntent(),
                        message.getKeywordText(),
                        message.getMatched(),
                        message.getSourceName(),
                        message.getSourceUrl(),
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

    private void saveQaMessage(QaAskRequest request, QaAskResponse response) {
        QaMessage message = new QaMessage();
        message.setQuestion(request.getQuestion());
        message.setAnswer(response.getAnswer());
        message.setIntent(response.getIntent());
        message.setKeywordText(response.getKeyword());
        message.setMatched(response.getMatched());
        message.setSourceName(response.getSourceName());
        message.setSourceUrl(response.getSourceUrl());

        qaMessageMapper.insert(message);
    }

}
