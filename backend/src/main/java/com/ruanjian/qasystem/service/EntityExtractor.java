package com.ruanjian.qasystem.service;

import com.ruanjian.qasystem.common.QaIntent;
import com.ruanjian.qasystem.model.vo.ExtractedEntity;
import org.springframework.stereotype.Service;

/**
 * 简单实体抽取器。
 * MVP 阶段通过删除常见问法词提取关键词，后续可替换为分词/NLP/大模型。
 */
@Service
public class EntityExtractor {
    public ExtractedEntity extract(String question) {
        return extract(question, QaIntent.UNKNOWN);
    }
    public ExtractedEntity extract(String question, QaIntent intent) {
        if (question == null || question.isBlank()) {
            return new ExtractedEntity("");
        }
        if (intent == QaIntent.MUSEUM_ARTIFACT_COUNT || intent == QaIntent.MUSEUM_ARTIFACTS) {
            return new ExtractedEntity(cleanMuseumKeyword(question));
        }
        if (intent == QaIntent.TYPE_ARTIFACTS) {
            return new ExtractedEntity(cleanTypeKeyword(question));
        }
        if (intent == QaIntent.MATERIAL_ARTIFACTS) {
            return new ExtractedEntity(cleanMaterialKeyword(question));
        }
        if (intent == QaIntent.DYNASTY_ARTIFACTS) {
            String dynasty = extractDynasty(question);
            if (!dynasty.isBlank()) {
                return new ExtractedEntity(dynasty);
            }
        }
        if (intent == QaIntent.ARTIST_BIOGRAPHY || intent == QaIntent.ARTIST_WORKS) {
            return new ExtractedEntity(cleanArtistKeyword(question));
        }
        if (isArtifactIntent(intent)) {
            return new ExtractedEntity(cleanArtifactKeyword(question));
        }

        String keyword = question
                .replace("请问", "")
                .replace("请介绍一下", "")
                .replace("介绍一下", "")
                .replace("讲讲", "")
                .replace("是什么", "")
                .replace("是哪个", "")
                .replace("属于哪个", "")
                .replace("属于什么", "")
                .replace("朝代", "")
                .replace("材质", "")
                .replace("类型", "")
                .replace("类别", "")
                .replace("收藏在哪个博物馆", "")
                .replace("收藏在哪里", "")
                .replace("现藏于哪里", "")
                .replace("现藏在哪里", "")
                .replace("由什么材料制成", "")
                .replace("是什么材质", "")
                .replace("什么材质", "")
                .replace("哪个朝代", "")
                .replace("什么年代", "")
                .replace("什么时候", "")
                .replace("的作者是谁", "")
                .replace("是谁画的", "")
                .replace("有多大", "")
                .replace("尺寸是多少", "")
                .replace("？", "")
                .replace("?", "")
                .trim();

        return new ExtractedEntity(keyword);
    }
    private String extractDynasty(String question) {
        String[] dynasties = {
                "夏代", "商代", "周代", "西周", "东周",
                "春秋", "战国", "秦代", "汉代", "西汉", "东汉",
                "三国", "魏晋", "晋代", "南北朝",
                "隋代", "唐代", "五代", "宋代", "北宋", "南宋",
                "辽代", "金代", "元代", "明代", "清代",
                "民国"
        };

        for (String dynasty : dynasties) {
            if (question.contains(dynasty)) {
                return dynasty;
            }
        }

        return "";
    }
    private String cleanArtistKeyword(String question) {
        return question
                .replace("请问", "")
                .replace("介绍一下", "")
                .replace("介绍", "")
                .replace("的生平经历是怎样的", "")
                .replace("的生平经历", "")
                .replace("生平经历", "")
                .replace("的生平", "")
                .replace("生平", "")
                .replace("经历", "")
                .replace("还有哪些作品", "")
                .replace("还有什么作品", "")
                .replace("其他作品", "")
                .replace("作品有哪些", "")
                .replace("有哪些作品", "")
                .replace("？", "")
                .replace("?", "")
                .trim();
    }
    private boolean isArtifactIntent(QaIntent intent) {
        return intent == QaIntent.ARTIFACT_MUSEUM
                || intent == QaIntent.ARTIFACT_DYNASTY
                || intent == QaIntent.ARTIFACT_MATERIAL
                || intent == QaIntent.ARTIFACT_TYPE
                || intent == QaIntent.ARTIFACT_DESCRIPTION
                || intent == QaIntent.ARTIFACT_ARTIST
                || intent == QaIntent.ARTIFACT_SIZE
                || intent == QaIntent.RELATED_ARTIFACTS;
    }
    private String cleanArtifactKeyword(String question) {
        return question
                .replace("请问", "")
                .replace("请介绍一下", "")
                .replace("介绍一下", "")
                .replace("介绍", "")
                .replace("讲讲", "")
                .replace("是什么", "")
                .replace("是哪一个", "")
                .replace("属于哪个", "")
                .replace("属于什么", "")
                .replace("朝代", "")
                .replace("材质", "")
                .replace("类型", "")
                .replace("类别", "")
                .replace("收藏在哪个博物馆", "")
                .replace("收藏在哪里", "")
                .replace("现藏于哪里", "")
                .replace("现藏在哪里", "")
                .replace("现收藏于哪里", "")
                .replace("由什么材料制成", "")
                .replace("是什么材质", "")
                .replace("什么材质", "")
                .replace("哪个朝代", "")
                .replace("什么年代", "")
                .replace("什么时候", "")
                .replace("的作者是谁", "")
                .replace("是谁画的", "")
                .replace("是谁创作的", "")
                .replace("相关文物有哪些", "")
                .replace("相似文物有哪些", "")
                .replace("推荐一些相似文物", "")
                .replace("推荐", "")
                .replace("有多大", "")
                .replace("尺寸是多少", "")
                .replace("规格是多少", "")
                .replace("？", "")
                .replace("?", "")
                .replace("推荐一些和", "")
                .replace("推荐一些与", "")
                .replace("一些", "")
                .replace("和", "")
                .replace("与", "")
                .replace("相关的文物", "")
                .replace("相似的文物", "")
                .trim();
    }
    private String cleanMuseumKeyword(String question) {
        return normalize(question)
                .replace("收藏了多少件中国文物", "")
                .replace("收藏了多少件文物", "")
                .replace("收藏了哪些中国文物", "")
                .replace("收藏了哪些文物", "")
                .replace("有哪些中国文物", "")
                .replace("有哪些文物", "")
                .replace("收藏哪些文物", "")
                .replace("藏有哪些文物", "")
                .replace("有多少件中国文物", "")
                .replace("有多少件文物", "")
                .replace("多少件中国文物", "")
                .replace("多少件文物", "")
                .replace("藏品数量", "")
                .replace("文物数量", "")
                .trim();
    }
    private String cleanTypeKeyword(String question) {
        return normalize(question)
                .replace("有哪些文物", "")
                .replace("有什么文物", "")
                .replace("哪些文物", "")
                .replace("文物有哪些", "")
                .trim();
    }
    private String cleanMaterialKeyword(String question) {
        return normalize(question)
                .replace("材质", "")
                .replace("材料", "")
                .replace("制成", "")
                .replace("做的", "")
                .replace("有哪些文物", "")
                .replace("有什么文物", "")
                .replace("哪些文物", "")
                .replace("文物有哪些", "")
                .trim();
    }
    private String normalize(String question) {
        return question
                .replace("请问", "")
                .replace("请介绍一下", "")
                .replace("介绍一下", "")
                .replace("介绍", "")
                .replace("讲讲", "")
                .replace("是什么", "")
                .replace("是哪一个", "")
                .replace("？", "")
                .replace("?", "")
                .trim();
    }
}
