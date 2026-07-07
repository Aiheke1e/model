package com.gemify.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "登录/刷新 Token 响应")
@Data
@Builder
public class TokenResponse {

    @Schema(description = "访问令牌（JWT），请求头：Authorization: Bearer {accessToken}")
    private String accessToken;

    @Schema(description = "刷新令牌，用于无感续登")
    private String refreshToken;

    @Schema(description = "accessToken 有效期（秒）")
    private long accessTokenExpiresIn;

    @Schema(description = "refreshToken 有效期（秒）")
    private long refreshTokenExpiresIn;

    @Schema(description = "是否已设置密码")
    private boolean hasPassword;

    @Schema(description = "是否为新注册用户（验证码登录时）")
    private boolean newUser;
}
