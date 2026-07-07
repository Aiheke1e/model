package com.gemify.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "gemify.jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenExpireMinutes = 15;
    private long refreshTokenExpireDays = 7;
}
