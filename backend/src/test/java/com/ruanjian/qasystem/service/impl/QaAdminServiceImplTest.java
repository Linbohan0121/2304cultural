package com.ruanjian.qasystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruanjian.qasystem.model.entity.QaMessage;
import com.ruanjian.qasystem.model.vo.PageResult;
import com.ruanjian.qasystem.model.vo.QaAdminStatistics;
import com.ruanjian.qasystem.model.vo.QaFeedbackStatistics;
import com.ruanjian.qasystem.model.vo.QaHistoryItem;
import com.ruanjian.qasystem.repository.QaMessageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.isNull;

class QaAdminServiceImplTest {

    private QaMessageMapper qaMessageMapper;
    private QaAdminServiceImpl qaAdminService;

    @BeforeEach
    void setUp() {
        qaMessageMapper = mock(QaMessageMapper.class);
        qaAdminService = new QaAdminServiceImpl(qaMessageMapper);
    }

    @Test
    void shouldListMessagesWithPagination() {
        QaMessage message = new QaMessage();
        message.setId(1L);
        message.setUserId(1L);
        message.setQuestion("青花瓷瓶收藏在哪个博物馆？");
        message.setAnswer("“青花瓷瓶”现收藏于大英博物馆。");
        message.setIntent("ARTIFACT_MUSEUM");
        message.setQuestionType("attribute");
        message.setKeywordText("青花瓷瓶");
        message.setMatched(true);
        message.setQaStatus("success");
        message.setSourceName("大英博物馆");
        message.setSourceUrl("https://example.com");
        message.setFeedbackType("helpful");
        message.setFeedbackRemark("回答准确");
        message.setCreatedAt(LocalDateTime.now());

        Page<QaMessage> page = new Page<>(1, 20);
        page.setRecords(List.of(message));
        page.setTotal(1);

        when(qaMessageMapper.selectPage(any(), any())).thenReturn(page);

        PageResult<QaHistoryItem> result = qaAdminService.listMessages(1L, 20L, "success", "helpful", "青花瓷瓶");

        assertEquals(1L, result.getTotal());
        assertEquals(1L, result.getPage());
        assertEquals(20L, result.getPageSize());
        assertEquals(1, result.getRecords().size());
        assertEquals("青花瓷瓶", result.getRecords().get(0).getKeyword());
        assertEquals("helpful", result.getRecords().get(0).getFeedbackType());

        ArgumentCaptor<Page<QaMessage>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(qaMessageMapper).selectPage(pageCaptor.capture(), any());
        assertEquals(1L, pageCaptor.getValue().getCurrent());
        assertEquals(20L, pageCaptor.getValue().getSize());
    }

    @Test
    void shouldReturnStatistics() {
        when(qaMessageMapper.selectCount(isNull())).thenReturn(10L);
        when(qaMessageMapper.selectCount(any(Wrapper.class)))
                .thenReturn(7L)
                .thenReturn(2L)
                .thenReturn(1L);
        when(qaMessageMapper.selectFeedbackStatistics())
                .thenReturn(new QaFeedbackStatistics(5L, 3L));

        QaAdminStatistics statistics = qaAdminService.getStatistics();

        assertEquals(10L, statistics.getTotalCount());
        assertEquals(7L, statistics.getMatchedCount());
        assertEquals(2L, statistics.getNoDataCount());
        assertEquals(1L, statistics.getErrorCount());
        assertEquals(5L, statistics.getHelpfulCount());
        assertEquals(3L, statistics.getInaccurateCount());
    }
}
