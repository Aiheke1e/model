package com.gemify.enums;

import lombok.Getter;

/**
 * 登录标识类型：手机号 / 邮箱。
 */
@Getter
public enum IdentityType {

    PHONE("phone"),
    EMAIL("email");

    private final String value;

    IdentityType(String value) {
        this.value = value;
    }

    public static IdentityType fromValue(String value) {
        for (IdentityType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown identity type: " + value);
    }
}
