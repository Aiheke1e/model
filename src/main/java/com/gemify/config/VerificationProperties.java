package com.gemify.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "gemify.verification")
public class VerificationProperties {

    private int codeLength = 6;
    private int expireMinutes = 5;
    private int maxAttempts = 5;
    private int sendCooldownSeconds = 60;
    private int dailySendLimit = 10;
}
