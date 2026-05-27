package com.ruanjian.qasystem.service;

import com.ruanjian.qasystem.common.QaIntent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntentResolverTest {

    private final IntentResolver intentResolver = new IntentResolver();

    @ParameterizedTest
    @CsvSource({
            "青花瓷瓶收藏在哪个博物馆？, ARTIFACT_MUSEUM",
            "青花瓷瓶属于哪个朝代？, ARTIFACT_DYNASTY",
            "青花瓷瓶是什么材质？, ARTIFACT_MATERIAL",
            "青花瓷瓶属于什么类型？, ARTIFACT_TYPE",
            "请介绍一下青花瓷瓶？, ARTIFACT_DESCRIPTION",
            "青花瓷瓶的作者是谁？, ARTIFACT_ARTIST",
            "青花瓷瓶尺寸是多少？, ARTIFACT_SIZE",
            "张大千的生平经历是怎样的？, ARTIST_BIOGRAPHY",
            "张大千还有哪些作品？, ARTIST_WORKS",
            "唐代有哪些代表性文物？, DYNASTY_ARTIFACTS",
            "推荐一些和青花瓷瓶相关的文物, RELATED_ARTIFACTS",
            "大英博物馆收藏了多少件中国文物？, MUSEUM_ARTIFACT_COUNT",
            "大英博物馆收藏了哪些文物？, MUSEUM_ARTIFACTS",
            "瓷器有哪些文物？, TYPE_ARTIFACTS",
            "陶瓷有哪些文物？, MATERIAL_ARTIFACTS"
    })
    void shouldResolveSupportedQuestions(String question, QaIntent expectedIntent) {
        QaIntent intent = intentResolver.resolve(question);

        assertEquals(expectedIntent, intent);
    }

    @Test
    void shouldReturnUnknownWhenQuestionIsBlank() {
        QaIntent intent = intentResolver.resolve(" ");

        assertEquals(QaIntent.UNKNOWN, intent);
    }
}
