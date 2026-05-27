package com.ruanjian.qasystem.model.vo;

import com.ruanjian.qasystem.common.QaIntent;

public record LlmParseResult(QaIntent intent, String keyword) {
}
