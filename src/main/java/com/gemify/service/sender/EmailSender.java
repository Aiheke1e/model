package com.gemify.service.sender;

import com.gemify.enums.CodePurpose;

/**
 * 邮件发送接口。
 * 当前由 {@link MockEmailSender} 实现（控制台输出），后续可替换为 SMTP 或邮件服务。
 */
public interface EmailSender {

    /**
     * 发送邮箱验证码。
     *
     * @param email   规范化邮箱（小写）
     * @param code    验证码明文
     * @param purpose 用途：login / reset_password / bind
     */
    void send(String email, String code, CodePurpose purpose);
}
