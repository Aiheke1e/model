package com.gemify.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "发送验证码请求")
@Data
public class SendCodeRequest {

    @Schema(description = "标识类型：phone | email", example = "phone")
    @NotBlank(message = "标识类型不能为空")
    private String identityType;

    @Schema(description = "手机号或邮箱", example = "13812345678")
    @NotBlank(message = "标识不能为空")
    private String identifier;

    @Schema(description = "用途：login | reset_password | bind", example = "login")
    @NotBlank(message = "用途不能为空")
    private String purpose;
}
