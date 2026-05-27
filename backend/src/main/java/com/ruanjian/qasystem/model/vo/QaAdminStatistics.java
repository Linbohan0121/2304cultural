package com.ruanjian.qasystem.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QaAdminStatistics {

    private Long totalCount;

    private Long matchedCount;

    private Long noDataCount;

    private Long errorCount;

    private Long helpfulCount;

    private Long inaccurateCount;
}
