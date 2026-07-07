package com.gemify.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "刷新 Token 请求")
@Data
public class RefreshTokenRequest {

    @Schema(description = "登录时返回的 refreshToken")
    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;
}
