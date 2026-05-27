package com.ruanjian.qasystem.service;

import com.ruanjian.qasystem.model.vo.LlmParseResult;

import java.util.Optional;

public interface LlmIntentParser {

    Optional<LlmParseResult> parse(String question);
}
