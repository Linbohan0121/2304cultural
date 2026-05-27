package com.ruanjian.qasystem.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 问答反馈统计。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QaFeedbackStatistics {

    private Long helpfulCount;

    private Long inaccurateCount;
}