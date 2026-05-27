package com.ruanjian.qasystem.service;

import com.ruanjian.qasystem.model.vo.PageResult;
import com.ruanjian.qasystem.model.vo.QaAdminStatistics;
import com.ruanjian.qasystem.model.vo.QaHistoryItem;

public interface QaAdminService {

    PageResult<QaHistoryItem> listMessages(Long page, Long pageSize, String qaStatus, String feedbackType, String keyword);

    QaAdminStatistics getStatistics();
}
