package com.budgetpro.quality;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingTest {
    private static final Logger log = LoggerFactory.getLogger(LoggingTest.class);

    public void testViolation(String userPassword, String apiToken) {
        // ruleid: budgetpro.quality.logging-standards
        log.info("User details: {}", userPassword);

        // ruleid: budgetpro.quality.logging-standards
        log.error("Failed to authenticate with token: " + apiToken);

        // ruleid: budgetpro.quality.logging-standards
        log.debug("Secret info: {}", getSecret());
    }

    public void testOk(String username, Long userId) {
        // ok: budgetpro.quality.logging-standards
        log.info("User {} logged in", username);

        // ok: budgetpro.quality.logging-standards
        log.debug("Found user with ID: {}", userId);
    }

    private String getSecret() {
        return "top_secret";
    }
}
