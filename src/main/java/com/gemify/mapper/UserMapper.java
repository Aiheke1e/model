package com.gemify.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gemify.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户表数据访问。
 * 复杂查询见 resources/mapper/UserMapper.xml。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /** 按手机号查询 */
    User selectByPhone(@Param("phone") String phone);

    /** 按邮箱查询 */
    User selectByEmail(@Param("email") String email);
}
