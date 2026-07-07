package com.gemify.service.sender;

import com.gemify.enums.CodePurpose;

/**
 * 短信发送接口。
 * 当前由 {@link MockSmsSender} 实现（控制台输出），后续可替换为阿里云/腾讯云。
 */
public interface SmsSender {

    /**
     * 发送短信验证码。
     *
     * @param phone   规范化手机号（E.164）
     * @param code    验证码明文（仅用于发送，不落库）
     * @param purpose 用途：login / reset_password / bind
     */
    void send(String phone, String code, CodePurpose purpose);
}
