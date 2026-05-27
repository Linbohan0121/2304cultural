package com.ruanjian.qasystem.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 问答历史记录展示对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QaHistoryItem {

    private Long id;

    private String question;

    private String answer;

    private String intent;

    private String questionType;

    private String keyword;

    private Boolean matched;

    private String qaStatus;

    private String sourceName;

    private String sourceUrl;

    private String feedbackType;

    private String feedbackRemark;

    private LocalDateTime createdAt;
}
