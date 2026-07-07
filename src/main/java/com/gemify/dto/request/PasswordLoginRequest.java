package com.gemify.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "密码登录请求")
@Data
public class PasswordLoginRequest {

    @Schema(description = "手机号或邮箱", example = "13812345678")
    @NotBlank(message = "账号不能为空")
    private String account;

    @Schema(description = "登录密码")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "设备 ID（可选）")
    private String deviceId;

    @Schema(description = "设备名称（可选）")
    private String deviceName;
}
