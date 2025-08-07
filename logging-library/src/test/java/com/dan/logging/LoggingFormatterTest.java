package com.dan.logging;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class LoggingFormatterTest {

    @BeforeEach
    void setUp() {
        log.info("Setting up the LoggingFormatterTest class");
        System.setProperty("wikimedia.infrastructure.environment", "test-environment");
    }

    @Test
    void getSpringActiveProfile() {
    }

    @Test
    void getEnvironment() {
    }

    @Test
    void assertLoggingFormat() {
        System.out.println(LoggingFormatter.WIKIMEDIA_LOGGING_FORMAT_V1);
    }
}