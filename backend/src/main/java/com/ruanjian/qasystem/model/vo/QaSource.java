package com.ruanjian.qasystem.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 问答答案来源。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QaSource {

    private String type;

    private Long id;

    private String name;

    private String url;
}