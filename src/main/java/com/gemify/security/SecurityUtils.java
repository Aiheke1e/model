package com.gemify.security;

import com.gemify.common.BusinessException;
import com.gemify.common.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static SecurityUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return securityUser;
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }
}
