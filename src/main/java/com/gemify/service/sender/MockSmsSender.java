package com.gemify.service.sender;

import com.gemify.enums.CodePurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockSmsSender implements SmsSender {

    @Override
    public void send(String phone, String code, CodePurpose purpose) {
        log.info("[Mock SMS] phone={}, purpose={}, code={}", phone, purpose.getValue(), code);
    }
}
