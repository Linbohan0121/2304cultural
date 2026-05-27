package com.ruanjian.qasystem.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruanjian.qasystem.model.entity.QaMessage;
import com.ruanjian.qasystem.model.vo.QaFeedbackStatistics;
import com.ruanjian.qasystem.model.vo.QaHotQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QaMessageMapper extends BaseMapper<QaMessage> {

    @Select("""
            SELECT question, COUNT(*) AS count
            FROM qa_message
            GROUP BY question
            ORDER BY count DESC
            LIMIT #{limit}
            """)
    List<QaHotQuestion> selectHotQuestions(Integer limit);
    @Select("""
        SELECT
            SUM(CASE WHEN feedback_type = 'helpful' THEN 1 ELSE 0 END) AS helpful_count,
            SUM(CASE WHEN feedback_type = 'inaccurate' THEN 1 ELSE 0 END) AS inaccurate_count
        FROM qa_message
        """)
    QaFeedbackStatistics selectFeedbackStatistics();
}