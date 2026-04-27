package com.ruanjian.qasystem.service;

import com.ruanjian.qasystem.common.QaIntent;
import com.ruanjian.qasystem.model.vo.QaFact;
import org.springframework.stereotype.Service;

/**
 * 根据查询事实生成自然语言答案。
 * MVP 阶段使用模板，避免大模型生成不可靠内容。
 */
@Service
public class AnswerBuilder {

    public String build(QaIntent intent, String keyword, QaFact fact) {
        if (fact == null || fact.getValue() == null || fact.getValue().isBlank()) {
            return "暂无相关数据";
        }

        return switch (intent) {
            case ARTIFACT_MUSEUM -> "“" + keyword + "”现收藏于" + fact.getValue() + "。";
            case ARTIFACT_DYNASTY -> "“" + keyword + "”属于" + fact.getValue() + "。";
            case ARTIFACT_MATERIAL -> "“" + keyword + "”的材质为" + fact.getValue() + "。";
            case ARTIFACT_TYPE -> "“" + keyword + "”属于" + fact.getValue() + "类文物。";
            case ARTIFACT_DESCRIPTION -> "“" + keyword + "”的介绍：" + fact.getValue();
            case ARTIFACT_ARTIST -> "“" + keyword + "”的作者是" + fact.getValue() + "。";
            case ARTIST_BIOGRAPHY -> fact.getValue();
            case ARTIST_WORKS -> "与“" + keyword + "”相关的作品包括：" + fact.getValue() + "。";
            case DYNASTY_ARTIFACTS -> fact.getValue() + "的代表性文物包括：" + keyword + "。";
            case ARTIFACT_SIZE -> "“" + keyword + "”的尺寸或规格为：" + fact.getValue() + "。";
            case RELATED_ARTIFACTS -> "与“" + keyword + "”相关的文物包括：" + fact.getValue() + "。";
            default -> "暂不支持该类问题";
        };
    }
}
