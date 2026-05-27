package com.ruanjian.qasystem.service;

import com.ruanjian.qasystem.common.QaIntent;
import com.ruanjian.qasystem.model.vo.QaFact;

import java.util.Optional;

public interface LlmAnswerEnhancer {

    Optional<String> enhance(String question, QaIntent intent, String keyword, QaFact fact, String baseAnswer);
}
