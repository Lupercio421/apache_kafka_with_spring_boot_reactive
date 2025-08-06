package com.dan.logging;

import static com.dan.logging.CommandConstants.SPRING_ACTIVE_PROFILE_CONSTANT;
import static com.dan.logging.CommandConstants.WIKIMEDIA_INFRASTRUCTURE_ENVIRONMENT_CONSTANT;

public class LoggingFormatter {

    private LoggingFormatter(){
        throw new IllegalStateException("Utility class. Not meant to be instantiated.");
    }

    public static final String KV_WIKIMEDIA_SERVICE_NAME = "\"WikimediaService\":\"{}\"";
    public static final String KV_WIKIMEDIA_LOGGING_VERSION = "\"LoggingVersion\":\"" + CommandConstants.LOGGING_VERSION_ONE + "\"";
    public static final String KV_WIKIMEDIA_LOGGING_ENVIRONMENT = "\"Environment\":\"" + getEnvironment() + "\"";
    public static final String KV_WIKIMEDIA_ACTIVE_PROFILE = "\"SpringActiveProfile\":\"" + getSpringActiveProfile() + "\"";
    public static final String KV_WIKIMEDIA_MESSAGE_VALUE = "\"Message\":\"{}\"";
    public static final String KV_WIKIMEDIA_SOURCE = "\"Source\":\"{}\"";
    public static final String KV_WIKIMEDIA_LOG_EVENT_NAME = "\"LogEventName\":\"{}\"";
    public static final String DEFAULT_COMMA_APPENDER = ",";
    public static final String DOUBLE_COLON_APPENDER = " :: ";

    /**
     * Defines the logging format version 1 for Wikimedia logs.
     * <p>
     * This format includes the logging version, environment, Spring active profile,
     * service name, source, log event name, and message, each as a key-value pair
     * separated by commas. Use this format to ensure consistent log structure across
     * Wikimedia services. Keys with {} placeholders are intended to be replaced by the developer.
     * - "LoggingVersion":"1.0","Environment":"test-environment","SpringActiveProfile":"default","WikimediaService":"{}","Source":"{}","LogEventName":"{}","Message":"{}"
     * </p>
     */
    public static final String KV_WIKIMEDIA_LOGGING_FORMAT_V1 = KV_WIKIMEDIA_LOGGING_VERSION + DEFAULT_COMMA_APPENDER + KV_WIKIMEDIA_LOGGING_ENVIRONMENT + DEFAULT_COMMA_APPENDER + KV_WIKIMEDIA_ACTIVE_PROFILE + DEFAULT_COMMA_APPENDER + KV_WIKIMEDIA_SERVICE_NAME + DEFAULT_COMMA_APPENDER + KV_WIKIMEDIA_SOURCE + DEFAULT_COMMA_APPENDER + KV_WIKIMEDIA_LOG_EVENT_NAME + DEFAULT_COMMA_APPENDER + KV_WIKIMEDIA_MESSAGE_VALUE ;

    //write a method that returns the machines Spring Active Profile
    public static String getSpringActiveProfile() {
        return System.getProperty(SPRING_ACTIVE_PROFILE_CONSTANT, "default");
    }

    //write a method that returns the machines environment
    public static String getEnvironment() {
        String env = "";
        if (System.getProperty(WIKIMEDIA_INFRASTRUCTURE_ENVIRONMENT_CONSTANT) != null) {
            env = System.getProperty(WIKIMEDIA_INFRASTRUCTURE_ENVIRONMENT_CONSTANT);
        }
        return env;
    }

    // write a helper method that accepts a Throwable data type and a String helperMessage. The return value should be a String that contains the Throwable message and the Throwable.getCause().getMessage() if it is not null. Add the helperMessage to the String if either of the Throwable message or the Throwable.getCause().getMessage() is not null.
    public static String getThrowableMessage(Throwable throwable, String helperMessage) {
        StringBuilder messageBuilder = new StringBuilder();
        if (throwable != null) {
            if (throwable.getMessage() != null) {
                messageBuilder.append(throwable.getMessage());
            }
            if (throwable.getCause() != null && throwable.getCause().getMessage() != null) {
                messageBuilder.append(" Cause: ").append(throwable.getCause().getMessage());
            }
        }
        if (helperMessage != null && !helperMessage.isEmpty()) {
            messageBuilder.append(" Helper Message: ").append(helperMessage);
        }
        return messageBuilder.toString();
    }
}
