package com.ruanjian.qasystem.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private String answer;

    private Boolean matched;

    private String intent;

    private String sourceName;

    private String sourceUrl;

    private String keyword;
}
