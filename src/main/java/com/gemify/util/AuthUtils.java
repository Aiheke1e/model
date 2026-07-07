package com.gemify.util;

import com.gemify.common.BusinessException;
import com.gemify.common.ErrorCode;
import com.gemify.enums.IdentityType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.regex.Pattern;

public final class AuthUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final SecureRandom RANDOM = new SecureRandom();

    private AuthUtils() {
    }

    public static String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "手机号不能为空");
        }
        String normalized = phone.trim().replace(" ", "");
        if (!normalized.startsWith("+")) {
            if (normalized.startsWith("86")) {
                normalized = "+" + normalized;
            } else {
                normalized = "+86" + normalized;
            }
        }
        return normalized;
    }

    public static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "邮箱不能为空");
        }
        String normalized = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "邮箱格式不正确");
        }
        return normalized;
    }

    public static String normalizeIdentifier(IdentityType type, String identifier) {
        return switch (type) {
            case PHONE -> normalizePhone(identifier);
            case EMAIL -> normalizeEmail(identifier);
        };
    }

    public static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static String randomNumericCode(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(RANDOM.nextInt(10));
        }
        return builder.toString();
    }

    public static String randomToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
