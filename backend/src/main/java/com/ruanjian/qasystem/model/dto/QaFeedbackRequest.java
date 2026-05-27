package com.ruanjian.qasystem.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 问答反馈请求。
 */
@Data
public class QaFeedbackRequest {
    @NotBlank(message = "反馈类型不能为空")
    @Pattern(
            regexp = "helpful|inaccurate",
            message = "反馈类型只能是 helpful 或 inaccurate"
    )
    private String feedbackType;

    @Size(max = 500, message = "反馈备注不能超过500个字符")
    private String feedbackRemark;
}