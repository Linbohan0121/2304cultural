package com.ruanjian.qasystem.service;

import com.ruanjian.qasystem.model.dto.QaAskRequest;
import com.ruanjian.qasystem.model.vo.PageResult;
import com.ruanjian.qasystem.model.vo.QaAskResponse;
import com.ruanjian.qasystem.model.vo.QaHistoryItem;

import java.util.List;

public interface QaService {

    QaAskResponse ask(QaAskRequest request);
    PageResult<QaHistoryItem> listHistory(Long page, Long pageSize);

}
