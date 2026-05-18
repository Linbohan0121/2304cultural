package com.ruanjian.qasystem.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 问答请求参数。
 */
@Data
public class QaAskRequest {

    @NotBlank(message = "问题不能为空")
    private String question;
}