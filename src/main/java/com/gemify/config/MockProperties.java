package com.gemify.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "gemify.mock")
public class MockProperties {

    private String fixedCode = "123456";
}
