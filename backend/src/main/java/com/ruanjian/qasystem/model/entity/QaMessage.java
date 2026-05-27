package com.ruanjian.qasystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问答记录实体。
 */
@Data
@TableName("qa_message")
public class QaMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String question;

    private String answer;

    private String intent;

    private String questionType;

    private String keywordText;

    private Boolean matched;

    private String qaStatus;

    private String sourceName;

    private String sourceUrl;

    private String feedbackType;

    private String feedbackRemark;

    private LocalDateTime createdAt;
}
