package com.dan.logging;

public class CommandConstants {
    protected static final String LOGGING_VERSION_ONE = "1.0";
    protected static final String WIKIMEDIA_INFRASTRUCTURE_ENVIRONMENT_CONSTANT = "wikimedia.infrastructure.environment";
    protected static final String SPRING_ACTIVE_PROFILE_CONSTANT = "spring.profiles.active";

    private CommandConstants() {
        throw new IllegalStateException("Utility class. Not meant to be instantiated.");
    }
}
