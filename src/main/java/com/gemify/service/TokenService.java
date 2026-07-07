package com.gemify.service;

import com.gemify.common.BusinessException;
import com.gemify.common.ErrorCode;
import com.gemify.config.JwtProperties;
import com.gemify.dto.response.TokenResponse;
import com.gemify.entity.User;
import com.gemify.entity.UserSession;
import com.gemify.mapper.UserSessionMapper;
import com.gemify.security.CustomUserDetailsService;
import com.gemify.security.JwtTokenProvider;
import com.gemify.util.AuthUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Token 与会话管理：签发双 Token、刷新、登出。
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final UserSessionMapper userSessionMapper;
    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;

    /**
     * 登录成功后签发 accessToken + refreshToken，并写入 session 表。
     */
    @Transactional
    public TokenResponse issueTokens(User user, boolean newUser, HttpServletRequest request) {
        userDetailsService.validateUserStatus(user);
        userService.touchLastLogin(user.getId());

        String refreshToken = AuthUtils.randomToken();
        UserSession session = new UserSession();
        session.setUserId(user.getId());
        session.setRefreshTokenHash(AuthUtils.sha256(refreshToken));
        session.setDeviceId(request.getHeader("X-Device-Id"));
        session.setDeviceName(request.getHeader("X-Device-Name"));
        session.setUserAgent(request.getHeader("User-Agent"));
        session.setIpAddress(request.getRemoteAddr());
        session.setExpiresAt(LocalDateTime.now().plusDays(jwtProperties.getRefreshTokenExpireDays()));
        session.setLastActiveAt(LocalDateTime.now());
        userSessionMapper.insert(session);

        return buildTokenResponse(user, refreshToken, newUser);
    }

    /**
     * 刷新 Token：校验 refreshToken 后轮换（删旧 session，建新 session）。
     */
    @Transactional
    public TokenResponse refresh(String refreshToken) {
        UserSession session = userSessionMapper.selectByRefreshTokenHash(AuthUtils.sha256(refreshToken));
        if (session == null) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            userSessionMapper.deleteById(session.getId());
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = userService.getById(session.getUserId());
        userDetailsService.validateUserStatus(user);

        userSessionMapper.deleteById(session.getId());

        String newRefreshToken = AuthUtils.randomToken();
        UserSession newSession = new UserSession();
        newSession.setUserId(user.getId());
        newSession.setRefreshTokenHash(AuthUtils.sha256(newRefreshToken));
        newSession.setDeviceId(session.getDeviceId());
        newSession.setDeviceName(session.getDeviceName());
        newSession.setUserAgent(session.getUserAgent());
        newSession.setIpAddress(session.getIpAddress());
        newSession.setExpiresAt(LocalDateTime.now().plusDays(jwtProperties.getRefreshTokenExpireDays()));
        newSession.setLastActiveAt(LocalDateTime.now());
        userSessionMapper.insert(newSession);

        return buildTokenResponse(user, newRefreshToken, false);
    }

    /**
     * 登出当前设备：删除 refreshToken 对应的 session 记录。
     */
    @Transactional
    public void logout(String refreshToken) {
        UserSession session = userSessionMapper.selectByRefreshTokenHash(AuthUtils.sha256(refreshToken));
        if (session != null) {
            userSessionMapper.deleteById(session.getId());
        }
    }

    /**
     * 登出全部设备：删除用户所有 session。
     */
    @Transactional
    public void logoutAll(Long userId) {
        userSessionMapper.deleteByUserId(userId);
    }

    private TokenResponse buildTokenResponse(User user, String refreshToken, boolean newUser) {
        return TokenResponse.builder()
                .accessToken(jwtTokenProvider.createAccessToken(user.getId()))
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(jwtProperties.getAccessTokenExpireMinutes() * ChronoUnit.MINUTES.getDuration().getSeconds())
                .refreshTokenExpiresIn(jwtProperties.getRefreshTokenExpireDays() * ChronoUnit.DAYS.getDuration().getSeconds())
                .hasPassword(userService.hasPassword(user))
                .newUser(newUser)
                .build();
    }
}
