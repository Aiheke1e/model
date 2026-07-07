package com.gemify.service;

import com.gemify.common.BusinessException;
import com.gemify.common.ErrorCode;
import com.gemify.dto.request.*;
import com.gemify.dto.response.TokenResponse;
import com.gemify.dto.response.UserProfileResponse;
import com.gemify.entity.User;
import com.gemify.enums.CodePurpose;
import com.gemify.enums.IdentityType;
import com.gemify.security.SecurityUtils;
import com.gemify.util.AuthUtils;
import com.gemify.util.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 认证业务编排服务：串联验证码、用户、Token 等子服务。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final VerificationCodeService verificationCodeService;
    private final UserService userService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 发送验证码（手机/邮箱）。
     */
    public void sendCode(SendCodeRequest request, HttpServletRequest httpRequest) {
        IdentityType identityType = IdentityType.fromValue(request.getIdentityType());
        CodePurpose purpose = CodePurpose.fromValue(request.getPurpose());
        String identifier = AuthUtils.normalizeIdentifier(identityType, request.getIdentifier());
        verificationCodeService.sendCode(identityType, identifier, purpose, RequestUtils.getClientIp(httpRequest));
    }

    /**
     * 验证码登录：不存在则自动注册，返回双 Token。
     */
    @Transactional
    public TokenResponse loginByOtp(IdentityType identityType, OtpLoginRequest request, HttpServletRequest httpRequest) {
        String identifier = AuthUtils.normalizeIdentifier(identityType, request.getIdentifier());
        verificationCodeService.verifyCode(identityType, identifier, CodePurpose.LOGIN, request.getCode());

        User existing = userService.findByAccount(identifier);
        boolean newUser = existing == null;
        User user = userService.findOrCreateByIdentity(identityType, identifier);
        return tokenService.issueTokens(user, newUser, httpRequest);
    }

    /**
     * 手机号或邮箱 + 密码登录。
     */
    @Transactional
    public TokenResponse loginByPassword(PasswordLoginRequest request, HttpServletRequest httpRequest) {
        String account = request.getAccount().trim();
        if (account.contains("@")) {
            account = AuthUtils.normalizeEmail(account);
        } else {
            account = AuthUtils.normalizePhone(account);
        }
        User user = userService.findByAccount(account);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!userService.hasPassword(user)) {
            throw new BusinessException(ErrorCode.PASSWORD_NOT_SET);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_INCORRECT);
        }
        return tokenService.issueTokens(user, false, httpRequest);
    }

    /**
     * 首次设置密码（须已登录且尚未设密）。
     */
    @Transactional
    public void setPassword(SetPasswordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);
        if (userService.hasPassword(user)) {
            throw new BusinessException(ErrorCode.PASSWORD_ALREADY_SET);
        }
        userService.updatePassword(userId, passwordEncoder.encode(request.getPassword()));
    }

    /**
     * 修改密码（须已登录且已设密，校验旧密码）。
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);
        if (!userService.hasPassword(user)) {
            throw new BusinessException(ErrorCode.PASSWORD_NOT_SET);
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_INCORRECT);
        }
        userService.updatePassword(userId, passwordEncoder.encode(request.getNewPassword()));
    }

    /**
     * 找回密码：验证码校验通过后重置（仅已设密账号）。
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        IdentityType identityType = IdentityType.fromValue(request.getIdentityType());
        String identifier = AuthUtils.normalizeIdentifier(identityType, request.getIdentifier());
        verificationCodeService.verifyCode(identityType, identifier, CodePurpose.RESET_PASSWORD, request.getCode());

        User user = userService.findByAccount(identifier);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!userService.hasPassword(user)) {
            throw new BusinessException(ErrorCode.PASSWORD_NOT_SET);
        }
        userService.updatePassword(user.getId(), passwordEncoder.encode(request.getNewPassword()));
    }

    /**
     * 绑定手机号（须已登录，验证码 purpose=bind）。
     */
    @Transactional
    public void bindPhone(BindIdentityRequest request) {
        String phone = AuthUtils.normalizePhone(request.getIdentifier());
        verificationCodeService.verifyCode(IdentityType.PHONE, phone, CodePurpose.BIND, request.getCode());
        userService.bindIdentity(SecurityUtils.getCurrentUserId(), IdentityType.PHONE, phone);
    }

    /**
     * 绑定邮箱（须已登录，验证码 purpose=bind）。
     */
    @Transactional
    public void bindEmail(BindIdentityRequest request) {
        String email = AuthUtils.normalizeEmail(request.getIdentifier());
        verificationCodeService.verifyCode(IdentityType.EMAIL, email, CodePurpose.BIND, request.getCode());
        userService.bindIdentity(SecurityUtils.getCurrentUserId(), IdentityType.EMAIL, email);
    }

    /**
     * 使用 refreshToken 刷新访问令牌。
     */
    public TokenResponse refresh(RefreshTokenRequest request) {
        return tokenService.refresh(request.getRefreshToken());
    }

    /**
     * 登出当前设备（删除对应 session）。
     */
    public void logout(LogoutRequest request) {
        tokenService.logout(request.getRefreshToken());
    }

    /**
     * 登出全部设备（删除用户所有 session）。
     */
    public void logoutAll() {
        tokenService.logoutAll(SecurityUtils.getCurrentUserId());
    }

    /**
     * 获取当前登录用户信息（脱敏）。
     */
    public UserProfileResponse currentUser() {
        User user = userService.getById(SecurityUtils.getCurrentUserId());
        return UserProfileResponse.builder()
                .id(user.getId())
                .phone(maskPhone(user.getPhone()))
                .email(maskEmail(user.getEmail()))
                .hasPassword(userService.hasPassword(user))
                .status(user.getStatus())
                .build();
    }

    /** 手机号脱敏，如 +86138****5678 */
    private String maskPhone(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() < 8) {
            return phone;
        }
        return phone.substring(0, phone.length() - 8) + "****" + phone.substring(phone.length() - 4);
    }

    /** 邮箱脱敏，如 u****@example.com */
    private String maskEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return email;
        }
        int at = email.indexOf('@');
        if (at <= 1) {
            return email;
        }
        return email.charAt(0) + "****" + email.substring(at);
    }
}
