package com.gemify.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "当前用户信息")
@Data
@Builder
public class UserProfileResponse {

    @Schema(description = "用户 ID")
    private Long id;

    @Schema(description = "脱敏手机号")
    private String phone;

    @Schema(description = "脱敏邮箱")
    private String email;

    @Schema(description = "是否已设置密码")
    private boolean hasPassword;

    @Schema(description = "状态：1=正常 2=禁用 3=冻结")
    private Integer status;
}
