package com.ruanjian.qasystem.graph;

import com.ruanjian.qasystem.common.QaIntent;
import com.ruanjian.qasystem.model.vo.QaFact;

public interface GraphQueryService {

    QaFact query(QaIntent intent, String keyword);
}
