package com.ruanjian.qasystem.service;

import com.ruanjian.qasystem.common.QaIntent;
import com.ruanjian.qasystem.model.vo.QaFact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnswerBuilderTest {

    private final AnswerBuilder answerBuilder = new AnswerBuilder();

    @Test
    void shouldReturnNoDataWhenFactIsNull() {
        String answer = answerBuilder.build(QaIntent.ARTIFACT_MUSEUM, "青花瓷瓶", null);

        assertEquals("暂无相关数据", answer);
    }

    @ParameterizedTest
    @CsvSource({
            "ARTIFACT_MUSEUM, 青花瓷瓶, 大英博物馆, 收藏地",
            "ARTIFACT_DYNASTY, 青花瓷瓶, 明代, 朝代信息",
            "ARTIFACT_MATERIAL, 青花瓷瓶, 陶瓷, 材质信息",
            "ARTIFACT_TYPE, 青花瓷瓶, 瓷器, 类型信息",
            "ARTIFACT_DESCRIPTION, 青花瓷瓶, 明代景德镇窑瓷器, 基本介绍",
            "ARTIFACT_ARTIST, 张大千山水图, 张大千, 作者信息",
            "ARTIFACT_SIZE, 青花瓷瓶, 高 30cm, 尺寸规格"
    })
    void shouldBuildExpandedArtifactAnswer(QaIntent intent, String keyword, String value, String title) {
        QaFact fact = new QaFact(value, "知识图谱", "https://example.com");

        String answer = answerBuilder.build(intent, keyword, fact);

        assertTrue(answer.contains("关于“" + keyword + "”的" + title));
        assertTrue(answer.contains(value));
        assertTrue(answer.length() > 40);
    }

    @ParameterizedTest
    @CsvSource({
            "ARTIST_BIOGRAPHY, 张大千, 张大千是中国近现代画家, 生平信息",
            "ARTIST_WORKS, 张大千, 张大千山水图、仿黄公望山水图, 相关的作品包括",
            "DYNASTY_ARTIFACTS, 唐代, 唐三彩马, 代表性文物包括",
            "RELATED_ARTIFACTS, 青花瓷瓶, 青花瓷盘, 相关的文物包括",
            "MUSEUM_ARTIFACT_COUNT, 大英博物馆, 2, 数量为 2 件",
            "MUSEUM_ARTIFACTS, 大英博物馆, 青花瓷瓶、青铜鼎, 中国文物包括",
            "TYPE_ARTIFACTS, 瓷器, 青花瓷瓶、青花瓷盘, 瓷器类文物包括",
            "MATERIAL_ARTIFACTS, 陶瓷, 青花瓷瓶、青花瓷盘, 陶瓷材质文物包括"
    })
    void shouldBuildExpandedRelationAnswer(QaIntent intent, String keyword, String value, String expectedPhrase) {
        QaFact fact = new QaFact(value, "知识图谱", "https://example.com");

        String answer = answerBuilder.build(intent, keyword, fact);

        assertTrue(answer.contains(expectedPhrase));
        assertTrue(answer.contains(value));
        assertTrue(answer.length() > 40);
    }
}
