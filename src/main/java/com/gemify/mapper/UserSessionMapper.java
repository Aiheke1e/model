package com.gemify.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gemify.entity.UserSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户会话（Refresh Token）数据访问。
 */
@Mapper
public interface UserSessionMapper extends BaseMapper<UserSession> {

    /** 按 refreshToken 哈希查询会话 */
    UserSession selectByRefreshTokenHash(@Param("refreshTokenHash") String refreshTokenHash);

    /** 删除用户全部会话（强制全部下线） */
    int deleteByUserId(@Param("userId") Long userId);
}
