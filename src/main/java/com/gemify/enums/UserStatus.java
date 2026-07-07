package com.gemify.enums;

import lombok.Getter;

/**
 * 用户账号状态。
 */
@Getter
public enum UserStatus {

    NORMAL(1),
    DISABLED(2),
    FROZEN(3);

    private final int value;

    UserStatus(int value) {
        this.value = value;
    }

    public static UserStatus fromValue(int value) {
        for (UserStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown user status: " + value);
    }
}
