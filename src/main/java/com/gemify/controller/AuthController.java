package com.gemify.controller;

import com.gemify.common.Result;
import com.gemify.dto.request.*;
import com.gemify.dto.response.TokenResponse;
import com.gemify.dto.response.UserProfileResponse;
import com.gemify.enums.IdentityType;
import com.gemify.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证相关 HTTP 接口。
 * <p>
 * 接口文档注解说明：
 * <ul>
 *   <li>{@link Tag} — 接口分组（Swagger 左侧菜单）</li>
 *   <li>{@link Operation} — 单个接口的标题与说明</li>
 *   <li>{@link SecurityRequirement} — 标注需要登录（Bearer Token）</li>
 * </ul>
 */
@Tag(name = "认证", description = "登录、注册、验证码、密码、绑定、Token 管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "发送验证码", description = "向手机或邮箱发送验证码。purpose=login 无需登录；purpose=bind 需登录；purpose=reset_password 用于找回密码")
    @PostMapping("/code/send")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeRequest request, HttpServletRequest httpRequest) {
        authService.sendCode(request, httpRequest);
        return Result.ok();
    }

    @Operation(summary = "手机验证码登录", description = "验证码正确则登录；用户不存在时自动注册。未设密码时下次仍需验证码登录")
    @PostMapping("/login/otp/phone")
    public Result<TokenResponse> loginByPhoneOtp(@Valid @RequestBody OtpLoginRequest request,
                                                 HttpServletRequest httpRequest) {
        return Result.ok(authService.loginByOtp(IdentityType.PHONE, request, httpRequest));
    }

    @Operation(summary = "邮箱验证码登录", description = "逻辑同手机验证码登录")
    @PostMapping("/login/otp/email")
    public Result<TokenResponse> loginByEmailOtp(@Valid @RequestBody OtpLoginRequest request,
                                                 HttpServletRequest httpRequest) {
        return Result.ok(authService.loginByOtp(IdentityType.EMAIL, request, httpRequest));
    }

    @Operation(summary = "密码登录", description = "使用手机号或邮箱 + 密码登录（账号须已设置密码）")
    @PostMapping("/login/password")
    public Result<TokenResponse> loginByPassword(@Valid @RequestBody PasswordLoginRequest request,
                                                 HttpServletRequest httpRequest) {
        return Result.ok(authService.loginByPassword(request, httpRequest));
    }

    @Operation(summary = "首次设置密码", description = "登录后调用，仅当用户尚未设置密码时可用")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/password/set")
    public Result<Void> setPassword(@Valid @RequestBody SetPasswordRequest request) {
        authService.setPassword(request);
        return Result.ok();
    }

    @Operation(summary = "修改密码", description = "登录后调用，需提供旧密码和新密码")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/password/change")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return Result.ok();
    }

    @Operation(summary = "找回密码", description = "通过验证码重置密码，仅适用于已设置过密码的账号")
    @PostMapping("/password/reset")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return Result.ok();
    }

    @Operation(summary = "绑定手机号", description = "登录后调用，需先向目标手机号发送 purpose=bind 的验证码")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/bind/phone")
    public Result<Void> bindPhone(@Valid @RequestBody BindIdentityRequest request) {
        authService.bindPhone(request);
        return Result.ok();
    }

    @Operation(summary = "绑定邮箱", description = "登录后调用，需先向目标邮箱发送 purpose=bind 的验证码")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/bind/email")
    public Result<Void> bindEmail(@Valid @RequestBody BindIdentityRequest request) {
        authService.bindEmail(request);
        return Result.ok();
    }

    @Operation(summary = "刷新 Token", description = "使用 refreshToken 换取新的 accessToken 和 refreshToken（轮换）")
    @PostMapping("/token/refresh")
    public Result<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.ok(authService.refresh(request));
    }

    @Operation(summary = "登出当前设备", description = "传入 refreshToken，删除对应会话")
    @PostMapping("/logout")
    public Result<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return Result.ok();
    }

    @Operation(summary = "登出全部设备", description = "删除当前用户所有会话，强制全部下线")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout/all")
    public Result<Void> logoutAll() {
        authService.logoutAll();
        return Result.ok();
    }

    @Operation(summary = "获取当前用户信息", description = "返回脱敏后的手机/邮箱及是否已设密码")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public Result<UserProfileResponse> me() {
        return Result.ok(authService.currentUser());
    }
}
