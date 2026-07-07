package com.gemify.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gemify.entity.VerificationCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 验证码表数据访问。
 */
@Mapper
public interface VerificationCodeMapper extends BaseMapper<VerificationCode> {

    /**
     * 查询最新一条未使用且未过期的验证码。
     */
    VerificationCode selectLatestValid(@Param("identityType") String identityType,
                                       @Param("identifier") String identifier,
                                       @Param("purpose") String purpose);
}
