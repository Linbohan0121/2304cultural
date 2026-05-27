package com.ruanjian.qasystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruanjian.qasystem.model.entity.QaMessage;
import com.ruanjian.qasystem.model.vo.PageResult;
import com.ruanjian.qasystem.model.vo.QaAdminStatistics;
import com.ruanjian.qasystem.model.vo.QaFeedbackStatistics;
import com.ruanjian.qasystem.model.vo.QaHistoryItem;
import com.ruanjian.qasystem.repository.QaMessageMapper;
import com.ruanjian.qasystem.service.QaAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QaAdminServiceImpl implements QaAdminService {

    private final QaMessageMapper qaMessageMapper;

    @Override
    public PageResult<QaHistoryItem> listMessages(Long page, Long pageSize, String qaStatus, String feedbackType, String keyword) {
        long current = page == null || page < 1 ? 1 : page;
        long size = pageSize == null || pageSize < 1 ? 20 : pageSize;

        LambdaQueryWrapper<QaMessage> wrapper = buildFilter(qaStatus, feedbackType, keyword)
                .orderByDesc(QaMessage::getCreatedAt);

        Page<QaMessage> result = qaMessageMapper.selectPage(new Page<>(current, size), wrapper);

        List<QaHistoryItem> records = result.getRecords()
                .stream()
                .map(this::toHistoryItem)
                .toList();

        return new PageResult<>(records, result.getTotal(), current, size);
    }

    @Override
    public QaAdminStatistics getStatistics() {
        Long totalCount = qaMessageMapper.selectCount(null);
        Long matchedCount = qaMessageMapper.selectCount(new LambdaQueryWrapper<QaMessage>()
                .eq(QaMessage::getMatched, true));
        Long noDataCount = qaMessageMapper.selectCount(new LambdaQueryWrapper<QaMessage>()
                .eq(QaMessage::getQaStatus, "no_data"));
        Long errorCount = qaMessageMapper.selectCount(new LambdaQueryWrapper<QaMessage>()
                .eq(QaMessage::getQaStatus, "error"));

        QaFeedbackStatistics feedbackStatistics = qaMessageMapper.selectFeedbackStatistics();
        Long helpfulCount = feedbackStatistics == null || feedbackStatistics.getHelpfulCount() == null
                ? 0L
                : feedbackStatistics.getHelpfulCount();
        Long inaccurateCount = feedbackStatistics == null || feedbackStatistics.getInaccurateCount() == null
                ? 0L
                : feedbackStatistics.getInaccurateCount();

        return new QaAdminStatistics(
                totalCount == null ? 0L : totalCount,
                matchedCount == null ? 0L : matchedCount,
                noDataCount == null ? 0L : noDataCount,
                errorCount == null ? 0L : errorCount,
                helpfulCount,
                inaccurateCount
        );
    }

    private LambdaQueryWrapper<QaMessage> buildFilter(String qaStatus, String feedbackType, String keyword) {
        LambdaQueryWrapper<QaMessage> wrapper = new LambdaQueryWrapper<>();

        if (qaStatus != null && !qaStatus.isBlank()) {
            wrapper.eq(QaMessage::getQaStatus, qaStatus);
        }
        if (feedbackType != null && !feedbackType.isBlank()) {
            wrapper.eq(QaMessage::getFeedbackType, feedbackType);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(query -> query
                    .like(QaMessage::getQuestion, keyword)
                    .or()
                    .like(QaMessage::getAnswer, keyword)
                    .or()
                    .like(QaMessage::getKeywordText, keyword));
        }

        return wrapper;
    }

    private QaHistoryItem toHistoryItem(QaMessage message) {
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
}
