package com.ruanjian.qasystem.service;

import com.ruanjian.qasystem.common.QaIntent;
import org.springframework.stereotype.Service;

/**
 * 简单意图识别器。
 * MVP 阶段先使用关键词规则，后续可替换为大模型或 NLP 分类器。
 */
@Service
public class IntentResolver {

    public QaIntent resolve(String question) {
        if (question == null || question.isBlank()) {
            return QaIntent.UNKNOWN;
        }

        if (containsAny(question, "还有哪些作品", "其他作品", "作品有哪些", "有哪些作品")) {
            return QaIntent.ARTIST_WORKS;
        }

        if (containsAny(question, "生平", "经历")) {
            return QaIntent.ARTIST_BIOGRAPHY;
        }

        if (containsAny(question, "相关文物", "相似文物", "推荐")) {
            return QaIntent.RELATED_ARTIFACTS;
        }

        if (containsAny(question, "收藏了多少件", "有多少件", "多少件文物", "藏品数量", "文物数量")) {
            return QaIntent.MUSEUM_ARTIFACT_COUNT;
        }

        if (containsAny(question, "瓷器", "书画", "雕塑", "青铜器")
                && containsAny(question, "有哪些文物", "有什么文物", "哪些文物", "文物有哪些")) {
            return QaIntent.TYPE_ARTIFACTS;
        }

        if (containsAny(question, "陶瓷", "青铜", "玉", "玉石", "金银", "纸", "纸本", "丝绸")
                && containsAny(question, "有哪些文物", "有什么文物", "哪些文物", "文物有哪些")) {
            return QaIntent.MATERIAL_ARTIFACTS;
        }

        if (containsAny(question, "收藏了哪些", "有哪些中国文物", "有哪些文物", "收藏哪些文物", "藏有哪些文物")
                && containsAny(question, "博物馆", "Museum", "museum")) {
            return QaIntent.MUSEUM_ARTIFACTS;
        }

        if (containsAny(question, "代表性文物", "有哪些文物", "有什么文物", "哪些文物")) {
            return QaIntent.DYNASTY_ARTIFACTS;
        }

        if (containsAny(question, "藏于", "现藏", "收藏于", "收藏在哪里", "收藏在哪", "哪家博物馆", "哪个博物馆", "哪里")) {
            return QaIntent.ARTIFACT_MUSEUM;
        }

        if (containsAny(question, "朝代", "年代", "时期", "什么时候")) {
            return QaIntent.ARTIFACT_DYNASTY;
        }

        if (containsAny(question, "材质", "材料", "制成", "什么做")) {
            return QaIntent.ARTIFACT_MATERIAL;
        }

        if (containsAny(question, "类型", "类别", "器物")) {
            return QaIntent.ARTIFACT_TYPE;
        }

        if (containsAny(question, "作者", "谁画", "谁创作")) {
            return QaIntent.ARTIFACT_ARTIST;
        }

        if (containsAny(question, "尺寸", "规格", "重量", "多大")) {
            return QaIntent.ARTIFACT_SIZE;
        }

        if (containsAny(question, "介绍", "简介", "说明", "讲讲")) {
            return QaIntent.ARTIFACT_DESCRIPTION;
        }

        return QaIntent.UNKNOWN;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
