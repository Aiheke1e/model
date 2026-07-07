package com.gemify.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("verification_codes")
public class VerificationCode {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String identityType;
    private String identifier;
    private String purpose;
    private String codeHash;
    private Integer attemptCount;
    private Integer maxAttempts;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private String ipAddress;
    private LocalDateTime createdAt;
}
