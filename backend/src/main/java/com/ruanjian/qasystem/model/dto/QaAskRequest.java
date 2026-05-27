package com.ruanjian.qasystem.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 问答请求参数。
 */
@Data
public class QaAskRequest {
    @Min(value=1,message = "用户ID不合法")
    private  Long userId;
    @NotBlank(message = "问题不能为空")
    @Size(max=500,message = "问题长度不能超过500个字符")
    private String question;
}