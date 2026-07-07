package com.gemify.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "首次设置密码请求")
@Data
public class SetPasswordRequest {

    @Schema(description = "新密码", example = "abc123456")
    @NotBlank(message = "密码不能为空")
    private String password;
}
