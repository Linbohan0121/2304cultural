package com.ruanjian.qasystem.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 从知识图谱或数据库查到的事实数据。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QaFact {

    private String value;

    private String sourceName;

    private String sourceUrl;
}
