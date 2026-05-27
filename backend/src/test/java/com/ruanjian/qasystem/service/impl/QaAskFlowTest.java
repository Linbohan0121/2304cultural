package com.ruanjian.qasystem.service.impl;

import com.ruanjian.qasystem.common.QaIntent;
import com.ruanjian.qasystem.graph.GraphQueryService;
import com.ruanjian.qasystem.model.dto.QaAskRequest;
import com.ruanjian.qasystem.model.entity.QaMessage;
import com.ruanjian.qasystem.model.vo.QaAskResponse;
import com.ruanjian.qasystem.model.vo.QaFact;
import com.ruanjian.qasystem.repository.QaMessageMapper;
import com.ruanjian.qasystem.service.AnswerBuilder;
import com.ruanjian.qasystem.service.EntityExtractor;
import com.ruanjian.qasystem.service.IntentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QaAskFlowTest {

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

    @ParameterizedTest
    @MethodSource("supportedCases")
    void shouldReturnExpectedAnswerForSupportedQuestion(
            String question,
            QaIntent intent,
            String keyword,
            String factValue,
            String questionType,
            String expectedPhrase
    ) {
        QaAskRequest request = new QaAskRequest();
        request.setUserId(1L);
        request.setQuestion(question);

        when(graphQueryService.query(intent, keyword))
                .thenReturn(new QaFact(factValue, "知识图谱", "https://example.com"));

        QaAskResponse response = qaService.ask(request);

        assertEquals(question, response.getQuestion());
        assertTrue(response.getAnswer().contains(expectedPhrase));
        assertTrue(response.getAnswer().contains(factValue));
        assertEquals(Boolean.TRUE, response.getMatched());
        assertEquals("success", response.getQaStatus());
        assertEquals(intent.name(), response.getIntent());
        assertEquals(questionType, response.getQuestionType());
        assertEquals(keyword, response.getKeyword());
        assertEquals("知识图谱", response.getSourceName());
        assertEquals("https://example.com", response.getSourceUrl());
        assertEquals("rule", response.getParseSource());
        assertEquals("template", response.getAnswerSource());
        assertNotNull(response.getSources());
        assertEquals(1, response.getSources().size());

        ArgumentCaptor<QaMessage> captor = ArgumentCaptor.forClass(QaMessage.class);
        verify(qaMessageMapper).insert(captor.capture());

        QaMessage saved = captor.getValue();
        assertEquals(1L, saved.getUserId());
        assertEquals(question, saved.getQuestion());
        assertEquals(response.getAnswer(), saved.getAnswer());
        assertEquals(intent.name(), saved.getIntent());
        assertEquals(questionType, saved.getQuestionType());
        assertEquals(keyword, saved.getKeywordText());
        assertEquals(Boolean.TRUE, saved.getMatched());
        assertEquals("success", saved.getQaStatus());
    }

    private static Stream<Object[]> supportedCases() {
        return Stream.of(
                new Object[]{"青花瓷瓶收藏在哪个博物馆？", QaIntent.ARTIFACT_MUSEUM, "青花瓷瓶", "大英博物馆", "attribute", "收藏地"},
                new Object[]{"青花瓷瓶属于哪个朝代？", QaIntent.ARTIFACT_DYNASTY, "青花瓷瓶", "明代", "attribute", "朝代信息"},
                new Object[]{"青花瓷瓶是什么材质？", QaIntent.ARTIFACT_MATERIAL, "青花瓷瓶", "陶瓷", "attribute", "材质信息"},
                new Object[]{"青花瓷瓶属于什么类型？", QaIntent.ARTIFACT_TYPE, "青花瓷瓶", "瓷器", "attribute", "类型信息"},
                new Object[]{"请介绍一下青花瓷瓶？", QaIntent.ARTIFACT_DESCRIPTION, "青花瓷瓶", "明代景德镇窑瓷器", "attribute", "基本介绍"},
                new Object[]{"青花瓷瓶的作者是谁？", QaIntent.ARTIFACT_ARTIST, "青花瓷瓶", "佚名", "attribute", "作者信息"},
                new Object[]{"青花瓷瓶尺寸是多少？", QaIntent.ARTIFACT_SIZE, "青花瓷瓶", "高 30cm", "attribute", "尺寸规格"},
                new Object[]{"张大千的生平经历是怎样的？", QaIntent.ARTIST_BIOGRAPHY, "张大千", "张大千是中国近现代画家", "attribute", "生平信息"},
                new Object[]{"张大千还有哪些作品？", QaIntent.ARTIST_WORKS, "张大千", "张大千山水图、仿黄公望山水图", "relation", "相关的作品包括"},
                new Object[]{"唐代有哪些代表性文物？", QaIntent.DYNASTY_ARTIFACTS, "唐代", "唐三彩马", "relation", "代表性文物包括"},
                new Object[]{"推荐一些和青花瓷瓶相关的文物", QaIntent.RELATED_ARTIFACTS, "青花瓷瓶", "青花瓷盘", "relation", "相关的文物包括"},
                new Object[]{"大英博物馆收藏了多少件中国文物？", QaIntent.MUSEUM_ARTIFACT_COUNT, "大英博物馆", "2", "statistic", "数量为 2 件"},
                new Object[]{"大英博物馆收藏了哪些文物？", QaIntent.MUSEUM_ARTIFACTS, "大英博物馆", "青花瓷瓶、青铜鼎", "relation", "中国文物包括"},
                new Object[]{"瓷器有哪些文物？", QaIntent.TYPE_ARTIFACTS, "瓷器", "青花瓷瓶、青花瓷盘", "relation", "瓷器类文物包括"},
                new Object[]{"陶瓷有哪些文物？", QaIntent.MATERIAL_ARTIFACTS, "陶瓷", "青花瓷瓶、青花瓷盘", "relation", "陶瓷材质文物包括"}
        );
    }
}
