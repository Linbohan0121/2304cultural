package com.ruanjian.qasystem.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 问答结果返回对象。
 * answer：最终回答
 * matched：是否查到知识图谱数据
 * intent：识别出来的问题类型
 * sourceName：数据来源名称，例如“大英博物馆”
 * sourceUrl：原始详情页链接
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QaAskResponse {

    private String question;

    private String answer;

    private Boolean matched;

    private String qaStatus;

    private String intent;

    private String questionType;

    private String sourceName;

    private String sourceUrl;

    private List<QaSource> sources;
    private String keyword;

    private String parseSource;

    private String answerSource;

    public QaAskResponse(
            String question,
            String answer,
            Boolean matched,
            String qaStatus,
            String intent,
            String questionType,
            String sourceName,
            String sourceUrl,
            List<QaSource> sources,
            String keyword
    ) {
        this.question = question;
        this.answer = answer;
        this.matched = matched;
        this.qaStatus = qaStatus;
        this.intent = intent;
        this.questionType = questionType;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.sources = sources;
        this.keyword = keyword;
        this.parseSource = null;
        this.answerSource = null;
    }

    public QaAskResponse(
            String question,
            String answer,
            Boolean matched,
            String qaStatus,
            String intent,
            String questionType,
            String sourceName,
            String sourceUrl,
            List<QaSource> sources,
            String keyword,
            String parseSource
    ) {
        this.question = question;
        this.answer = answer;
        this.matched = matched;
        this.qaStatus = qaStatus;
        this.intent = intent;
        this.questionType = questionType;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.sources = sources;
        this.keyword = keyword;
        this.parseSource = parseSource;
        this.answerSource = null;
    }

}
