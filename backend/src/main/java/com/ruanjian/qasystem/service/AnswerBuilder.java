package com.ruanjian.qasystem.service;

import com.ruanjian.qasystem.common.QaIntent;
import com.ruanjian.qasystem.model.vo.QaFact;
import org.springframework.stereotype.Service;

/**
 * 根据知识图谱事实生成可直接展示的基础回答。
 */
@Service
public class AnswerBuilder {

    public String build(QaIntent intent, String keyword, QaFact fact) {
        if (fact == null || fact.getValue() == null || fact.getValue().isBlank()) {
            return "暂无相关数据";
        }

        String value = fact.getValue();
        return switch (intent) {
            case ARTIFACT_MUSEUM -> artifactAnswer(keyword, "收藏地", "现收藏于" + value,
                    "如果你还想继续了解，可以追问它的朝代、材质、类型或相关文物。");
            case ARTIFACT_DYNASTY -> artifactAnswer(keyword, "朝代信息", "属于" + value,
                    "这类信息适合和作者、材质、收藏地一起查看，能帮助你建立更完整的文物背景。");
            case ARTIFACT_MATERIAL -> artifactAnswer(keyword, "材质信息", "材质为" + value,
                    "材质信息通常能帮助判断文物工艺和类型，后续可以继续追问同材质文物。");
            case ARTIFACT_TYPE -> artifactAnswer(keyword, "类型信息", "属于" + value + "类文物",
                    "你也可以继续查询这一类型下还有哪些文物，方便做横向比较。");
            case ARTIFACT_DESCRIPTION -> artifactAnswer(keyword, "基本介绍", value,
                    "当前回答基于知识图谱中的简介字段，适合作为进一步了解该文物的入口。");
            case ARTIFACT_ARTIST -> artifactAnswer(keyword, "作者信息", "作者是" + value,
                    "如果需要扩展，可以继续查询该作者的其他作品或生平信息。");
            case ARTIFACT_SIZE -> artifactAnswer(keyword, "尺寸规格", "尺寸或规格为：" + value,
                    "尺寸信息可以和材质、类型一起使用，用于辅助识别文物形制。");
            case ARTIST_BIOGRAPHY -> "关于“" + keyword + "”，知识图谱记录的生平信息为：" + value
                    + "。这部分内容可以帮助理解其作品风格和创作背景。你还可以继续追问“" + keyword + "有哪些作品”。";
            case ARTIST_WORKS -> "与“" + keyword + "”相关的作品包括：" + value
                    + "。这些作品来自知识图谱中的作者到作品关系。你可以继续选择其中某件作品，查询它的朝代、材质、收藏地或相关文物。";
            case DYNASTY_ARTIFACTS -> keyword + "的代表性文物包括：" + value
                    + "。这些结果来自知识图谱中文物和朝代的关联关系。你可以继续追问某件文物的收藏地、材质或详细介绍。";
            case RELATED_ARTIFACTS -> "与“" + keyword + "”相关的文物包括：" + value
                    + "。这些结果适合用于推荐和拓展浏览。你可以继续查看其中任意一件文物的朝代、作者、材质或收藏地。";
            case MUSEUM_ARTIFACT_COUNT -> keyword + "当前在知识图谱中记录的中国文物数量为 " + value
                    + " 件。这个统计只反映当前图谱已录入的数据，不代表该馆真实完整馆藏数量。你可以继续追问该博物馆收藏了哪些文物。";
            case MUSEUM_ARTIFACTS -> keyword + "当前在知识图谱中记录的中国文物包括：" + value
                    + "。这些文物来自博物馆收藏关系查询。你可以继续选择某件文物，查看它的朝代、材质、作者或介绍。";
            case TYPE_ARTIFACTS -> "当前在知识图谱中记录的" + keyword + "类文物包括：" + value
                    + "。这类问题适合按文物类型做聚合浏览。你可以继续追问其中某件文物的收藏地或朝代。";
            case MATERIAL_ARTIFACTS -> "当前在知识图谱中记录的" + keyword + "材质文物包括：" + value
                    + "。材质维度可以帮助你从工艺角度浏览文物。你可以继续追问其中某件文物的类型、朝代或收藏地。";
            default -> "暂不支持该类问题";
        };
    }

    private String artifactAnswer(String keyword, String title, String factText, String nextStep) {
        return "关于“" + keyword + "”的" + title + "，知识图谱记录显示：" + factText + "。"
                + nextStep;
    }
}
