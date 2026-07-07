package com.gemify.enums;

import lombok.Getter;

/**
 * 验证码用途枚举。
 */
@Getter
public enum CodePurpose {

    LOGIN("login"),
    RESET_PASSWORD("reset_password"),
    BIND("bind");

    private final String value;

    CodePurpose(String value) {
        this.value = value;
    }

    public static CodePurpose fromValue(String value) {
        for (CodePurpose purpose : values()) {
            if (purpose.value.equals(value)) {
                return purpose;
            }
        }
        throw new IllegalArgumentException("Unknown code purpose: " + value);
    }
}
