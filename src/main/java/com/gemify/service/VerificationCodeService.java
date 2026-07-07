package com.gemify.service;

import com.gemify.common.BusinessException;
import com.gemify.common.ErrorCode;
import com.gemify.config.MockProperties;
import com.gemify.config.VerificationProperties;
import com.gemify.entity.User;
import com.gemify.entity.VerificationCode;
import com.gemify.enums.CodePurpose;
import com.gemify.enums.IdentityType;
import com.gemify.mapper.UserMapper;
import com.gemify.mapper.VerificationCodeMapper;
import com.gemify.security.CustomUserDetailsService;
import com.gemify.security.SecurityUtils;
import com.gemify.service.sender.EmailSender;
import com.gemify.service.sender.SmsSender;
import com.gemify.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 验证码服务：发送、校验、过期与防重复使用。
 * 发码冷却与每日限额由 Redis 控制。
 */
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final VerificationCodeMapper verificationCodeMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final VerificationProperties verificationProperties;
    private final MockProperties mockProperties;
    private final SmsSender smsSender;
    private final EmailSender emailSender;
    private final CustomUserDetailsService userDetailsService;

    /**
     * 生成验证码并发送（Mock 环境打印到日志）。
     * 同时写入 verification_codes 表，并设置 Redis 冷却。
     */
    @Transactional
    public void sendCode(IdentityType identityType, String identifier, CodePurpose purpose, String ipAddress) {
        validateSendRequest(identityType, identifier, purpose);

        String cooldownKey = cooldownKey(identityType, identifier);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(cooldownKey))) {
            throw new BusinessException(ErrorCode.SEND_COOLDOWN);
        }

        String dailyKey = dailyKey(identityType, identifier);
        Long dailyCount = stringRedisTemplate.opsForValue().increment(dailyKey);
        if (dailyCount == null) {
            dailyCount = 1L;
        }
        if (dailyCount == 1) {
            stringRedisTemplate.expire(dailyKey, Duration.ofDays(1));
        }
        if (dailyCount > verificationProperties.getDailySendLimit()) {
            throw new BusinessException(ErrorCode.SEND_DAILY_LIMIT);
        }

        String code = mockProperties.getFixedCode();
        if (!StringUtils.hasText(code)) {
            code = AuthUtils.randomNumericCode(verificationProperties.getCodeLength());
        }

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setIdentityType(identityType.getValue());
        verificationCode.setIdentifier(identifier);
        verificationCode.setPurpose(purpose.getValue());
        verificationCode.setCodeHash(AuthUtils.sha256(code));
        verificationCode.setAttemptCount(0);
        verificationCode.setMaxAttempts(verificationProperties.getMaxAttempts());
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(verificationProperties.getExpireMinutes()));
        verificationCode.setIpAddress(ipAddress);
        verificationCodeMapper.insert(verificationCode);

        stringRedisTemplate.opsForValue().set(
                cooldownKey,
                "1",
                Duration.ofSeconds(verificationProperties.getSendCooldownSeconds()));

        if (identityType == IdentityType.PHONE) {
            smsSender.send(identifier, code, purpose);
        } else {
            emailSender.send(identifier, code, purpose);
        }
    }

    /**
     * 校验验证码：检查过期、已使用、尝试次数，通过后标记 used_at。
     */
    @Transactional
    public void verifyCode(IdentityType identityType, String identifier, CodePurpose purpose, String code) {
        VerificationCode record = verificationCodeMapper.selectLatestValid(
                identityType.getValue(), identifier, purpose.getValue());
        if (record == null) {
            throw new BusinessException(ErrorCode.CODE_INVALID);
        }
        if (record.getUsedAt() != null) {
            throw new BusinessException(ErrorCode.CODE_USED);
        }
        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.CODE_EXPIRED);
        }
        if (record.getAttemptCount() >= record.getMaxAttempts()) {
            throw new BusinessException(ErrorCode.CODE_ATTEMPTS_EXCEEDED);
        }

        if (!AuthUtils.sha256(code).equals(record.getCodeHash())) {
            VerificationCode update = new VerificationCode();
            update.setId(record.getId());
            update.setAttemptCount(record.getAttemptCount() + 1);
            verificationCodeMapper.updateById(update);
            throw new BusinessException(ErrorCode.CODE_INVALID);
        }

        VerificationCode used = new VerificationCode();
        used.setId(record.getId());
        used.setUsedAt(LocalDateTime.now());
        verificationCodeMapper.updateById(used);
    }

    /** 按用途校验是否允许发码 */
    private void validateSendRequest(IdentityType identityType, String identifier, CodePurpose purpose) {
        User existing = findUserByIdentity(identityType, identifier);
        switch (purpose) {
            case LOGIN -> {
            }
            case RESET_PASSWORD -> {
                if (existing == null) {
                    throw new BusinessException(ErrorCode.USER_NOT_FOUND);
                }
                userDetailsService.validateUserStatus(existing);
                if (!StringUtils.hasText(existing.getPasswordHash())) {
                    throw new BusinessException(ErrorCode.PASSWORD_NOT_SET);
                }
            }
            case BIND -> {
                SecurityUtils.getCurrentUser();
                if (existing != null) {
                    throw new BusinessException(ErrorCode.IDENTITY_ALREADY_BOUND);
                }
            }
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }

    private User findUserByIdentity(IdentityType identityType, String identifier) {
        return switch (identityType) {
            case PHONE -> userMapper.selectByPhone(identifier);
            case EMAIL -> userMapper.selectByEmail(identifier);
        };
    }

    private String cooldownKey(IdentityType identityType, String identifier) {
        return "auth:cooldown:" + identityType.getValue() + ":" + identifier;
    }

    private String dailyKey(IdentityType identityType, String identifier) {
        return "auth:daily:" + identityType.getValue() + ":" + identifier + ":" + LocalDate.now();
    }
}
