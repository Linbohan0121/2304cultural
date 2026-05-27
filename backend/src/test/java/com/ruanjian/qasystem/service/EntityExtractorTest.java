package com.ruanjian.qasystem.service;

import com.ruanjian.qasystem.common.QaIntent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntityExtractorTest {

    private final EntityExtractor entityExtractor = new EntityExtractor();

    @ParameterizedTest
    @CsvSource({
            "青花瓷瓶收藏在哪个博物馆？, ARTIFACT_MUSEUM, 青花瓷瓶",
            "青花瓷瓶属于哪个朝代？, ARTIFACT_DYNASTY, 青花瓷瓶",
            "青花瓷瓶是什么材质？, ARTIFACT_MATERIAL, 青花瓷瓶",
            "青花瓷瓶属于什么类型？, ARTIFACT_TYPE, 青花瓷瓶",
            "请介绍一下青花瓷瓶？, ARTIFACT_DESCRIPTION, 青花瓷瓶",
            "青花瓷瓶的作者是谁？, ARTIFACT_ARTIST, 青花瓷瓶",
            "青花瓷瓶尺寸是多少？, ARTIFACT_SIZE, 青花瓷瓶",
            "张大千的生平经历是怎样的？, ARTIST_BIOGRAPHY, 张大千",
            "张大千还有哪些作品？, ARTIST_WORKS, 张大千",
            "唐代有哪些代表性文物？, DYNASTY_ARTIFACTS, 唐代",
            "推荐一些和青花瓷瓶相关的文物, RELATED_ARTIFACTS, 青花瓷瓶",
            "大英博物馆收藏了多少件中国文物？, MUSEUM_ARTIFACT_COUNT, 大英博物馆",
            "大英博物馆收藏了哪些文物？, MUSEUM_ARTIFACTS, 大英博物馆",
            "瓷器有哪些文物？, TYPE_ARTIFACTS, 瓷器",
            "陶瓷有哪些文物？, MATERIAL_ARTIFACTS, 陶瓷"
    })
    void shouldExtractKeywordByIntent(String question, QaIntent intent, String expectedKeyword) {
        String keyword = entityExtractor.extract(question, intent).getKeyword();

        assertEquals(expectedKeyword, keyword);
    }

    @Test
    void shouldNotExtractDynastyForArtifactMuseumQuestion() {
        String keyword = entityExtractor
                .extract("唐代青花瓷瓶收藏在哪个博物馆？", QaIntent.ARTIFACT_MUSEUM)
                .getKeyword();

        assertEquals("唐代青花瓷瓶", keyword);
    }

    @Test
    void shouldReturnEmptyKeywordWhenQuestionIsBlank() {
        String keyword = entityExtractor
                .extract(" ", QaIntent.ARTIFACT_MUSEUM)
                .getKeyword();

        assertEquals("", keyword);
    }
}
