package com.gemify.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "绑定手机/邮箱请求")
@Data
public class BindIdentityRequest {

    @Schema(description = "待绑定的手机号或邮箱", example = "user@example.com")
    @NotBlank(message = "标识不能为空")
    private String identifier;

    @Schema(description = "验证码（purpose=bind）", example = "123456")
    @NotBlank(message = "验证码不能为空")
    private String code;
}
