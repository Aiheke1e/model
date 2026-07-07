package com.gemify.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "找回密码请求")
@Data
public class ResetPasswordRequest {

    @Schema(description = "标识类型：phone | email", example = "phone")
    @NotBlank(message = "标识类型不能为空")
    private String identityType;

    @Schema(description = "手机号或邮箱", example = "13812345678")
    @NotBlank(message = "标识不能为空")
    private String identifier;

    @Schema(description = "验证码", example = "123456")
    @NotBlank(message = "验证码不能为空")
    private String code;

    @Schema(description = "新密码")
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
