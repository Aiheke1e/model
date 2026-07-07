package com.gemify.common;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(0, "ok"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "账号已禁用"),
    USER_FROZEN(1003, "账号已冻结"),
    PASSWORD_NOT_SET(1004, "尚未设置密码"),
    PASSWORD_ALREADY_SET(1005, "密码已设置"),
    PASSWORD_INCORRECT(1006, "密码错误"),
    ACCOUNT_ALREADY_EXISTS(1007, "账号已存在"),
    IDENTITY_ALREADY_BOUND(1008, "该标识已被其他账号绑定"),
    IDENTITY_ALREADY_OWNED(1009, "已绑定该标识"),

    CODE_INVALID(1101, "验证码错误或已失效"),
    CODE_EXPIRED(1102, "验证码已过期"),
    CODE_USED(1103, "验证码已使用"),
    CODE_ATTEMPTS_EXCEEDED(1104, "验证码尝试次数过多"),
    SEND_COOLDOWN(1105, "发送过于频繁，请稍后再试"),
    SEND_DAILY_LIMIT(1106, "今日发送次数已达上限"),

    TOKEN_INVALID(1201, "Token 无效"),
    TOKEN_EXPIRED(1202, "Token 已过期"),
    SESSION_NOT_FOUND(1203, "会话不存在或已失效");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
