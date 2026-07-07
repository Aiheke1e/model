package com.gemify.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "登出请求")
@Data
public class LogoutRequest {

    @Schema(description = "当前设备的 refreshToken")
    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;
}
