package com.gemify.service.sender;

import com.gemify.enums.CodePurpose;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockEmailSender implements EmailSender {

    @Override
    public void send(String email, String code, CodePurpose purpose) {
        log.info("[Mock Email] email={}, purpose={}, code={}", email, purpose.getValue(), code);
    }
}
