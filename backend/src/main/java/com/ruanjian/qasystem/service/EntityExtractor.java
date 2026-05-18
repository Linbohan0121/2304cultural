package com.ruanjian.qasystem.service;

import com.ruanjian.qasystem.model.vo.ExtractedEntity;
import org.springframework.stereotype.Service;

/**
 * 简单实体抽取器。
 * MVP 阶段通过删除常见问法词提取关键词，后续可替换为分词/NLP/大模型。
 */
@Service
public class EntityExtractor {

    public ExtractedEntity extract(String question) {
        if (question == null || question.isBlank()) {
            return new ExtractedEntity("");
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
}
