package com.gemify.service;

import com.gemify.common.BusinessException;
import com.gemify.common.ErrorCode;
import com.gemify.entity.User;
import com.gemify.enums.IdentityType;
import com.gemify.enums.UserStatus;
import com.gemify.mapper.UserMapper;
import com.gemify.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 用户领域服务：查询、注册、绑定、密码更新。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final CustomUserDetailsService userDetailsService;

    /** 按 ID 查询用户，不存在则抛异常 */
    public User getById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    /** 按手机号或邮箱查找用户（用于密码登录） */
    public User findByAccount(String account) {
        User user = userMapper.selectByPhone(account);
        if (user == null) {
            user = userMapper.selectByEmail(account);
        }
        return user;
    }

    /**
     * 验证码登录时：用户存在则返回，不存在则自动注册并绑定标识。
     */
    @Transactional
    public User findOrCreateByIdentity(IdentityType identityType, String identifier) {
        User existing = findByIdentity(identityType, identifier);
        if (existing != null) {
            userDetailsService.validateUserStatus(existing);
            return existing;
        }

        User user = new User();
        user.setStatus(UserStatus.NORMAL.getValue());
        if (identityType == IdentityType.PHONE) {
            user.setPhone(identifier);
        } else {
            user.setEmail(identifier);
        }
        userMapper.insert(user);
        return user;
    }

    /** 为当前用户绑定手机号或邮箱 */
    @Transactional
    public void bindIdentity(Long userId, IdentityType identityType, String identifier) {
        User user = getById(userId);
        if (identityType == IdentityType.PHONE) {
            if (identifier.equals(user.getPhone())) {
                throw new BusinessException(ErrorCode.IDENTITY_ALREADY_OWNED);
            }
            if (userMapper.selectByPhone(identifier) != null) {
                throw new BusinessException(ErrorCode.IDENTITY_ALREADY_BOUND);
            }
            User update = new User();
            update.setId(userId);
            update.setPhone(identifier);
            userMapper.updateById(update);
            return;
        }

        if (identifier.equals(user.getEmail())) {
            throw new BusinessException(ErrorCode.IDENTITY_ALREADY_OWNED);
        }
        if (userMapper.selectByEmail(identifier) != null) {
            throw new BusinessException(ErrorCode.IDENTITY_ALREADY_BOUND);
        }
        User update = new User();
        update.setId(userId);
        update.setEmail(identifier);
        userMapper.updateById(update);
    }

    /** 更新密码哈希 */
    @Transactional
    public void updatePassword(Long userId, String passwordHash) {
        User update = new User();
        update.setId(userId);
        update.setPasswordHash(passwordHash);
        update.setPasswordAlgo("bcrypt");
        userMapper.updateById(update);
    }

    /** 更新最近登录时间 */
    @Transactional
    public void touchLastLogin(Long userId) {
        User update = new User();
        update.setId(userId);
        update.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(update);
    }

    /** 是否已设置密码 */
    public boolean hasPassword(User user) {
        return StringUtils.hasText(user.getPasswordHash());
    }

    private User findByIdentity(IdentityType identityType, String identifier) {
        return switch (identityType) {
            case PHONE -> userMapper.selectByPhone(identifier);
            case EMAIL -> userMapper.selectByEmail(identifier);
        };
    }
}
