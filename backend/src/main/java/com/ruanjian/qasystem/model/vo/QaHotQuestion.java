package com.ruanjian.qasystem.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热门问题统计项。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QaHotQuestion {

    private String question;

    private Long count;
}