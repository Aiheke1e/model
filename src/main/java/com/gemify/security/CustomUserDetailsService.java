package com.gemify.security;

import com.gemify.common.BusinessException;
import com.gemify.common.ErrorCode;
import com.gemify.entity.User;
import com.gemify.enums.UserStatus;
import com.gemify.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        throw new UnsupportedOperationException("Use loadUserById instead");
    }

    public SecurityUser loadUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        validateUserStatus(user);
        return new SecurityUser(user);
    }

    public void validateUserStatus(User user) {
        if (user.getStatus() == null) {
            return;
        }
        UserStatus status = UserStatus.fromValue(user.getStatus());
        switch (status) {
            case DISABLED -> throw new BusinessException(ErrorCode.USER_DISABLED);
            case FROZEN -> throw new BusinessException(ErrorCode.USER_FROZEN);
            default -> {
            }
        }
    }
}
