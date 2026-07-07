package com.gemify.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "验证码登录请求")
@Data
public class OtpLoginRequest {

    @Schema(description = "手机号或邮箱", example = "13812345678")
    @NotBlank(message = "标识不能为空")
    private String identifier;

    @Schema(description = "验证码", example = "123456")
    @NotBlank(message = "验证码不能为空")
    private String code;

    @Schema(description = "设备 ID（可选）")
    private String deviceId;

    @Schema(description = "设备名称（可选）")
    private String deviceName;
}
