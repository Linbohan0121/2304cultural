package com.ruanjian.qasystem.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruanjian.qasystem.model.entity.QaMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QaMessageMapper extends BaseMapper<QaMessage> {
}
