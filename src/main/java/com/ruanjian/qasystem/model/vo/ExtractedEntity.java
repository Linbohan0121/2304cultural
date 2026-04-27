package com.ruanjian.qasystem.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 从用户问题中抽取出的查询实体。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedEntity {

    private String keyword;
}
