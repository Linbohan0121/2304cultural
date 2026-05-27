package com.ruanjian.qasystem.service;

import com.ruanjian.qasystem.model.dto.QaAskRequest;
import com.ruanjian.qasystem.model.dto.QaFeedbackRequest;
import com.ruanjian.qasystem.model.entity.QaMessage;
import com.ruanjian.qasystem.model.vo.*;

import java.util.List;

public interface QaService {

    QaAskResponse ask(QaAskRequest request);
    PageResult<QaHistoryItem> listHistory(Long userId, Long page, Long pageSize);
    QaHistoryItem getMessage(Long id);
    List<QaHotQuestion> listHotQuestions(Integer limit);
    Boolean submitFeedback(Long id, QaFeedbackRequest request);
    QaFeedbackStatistics getFeedbackStatistics();
}
