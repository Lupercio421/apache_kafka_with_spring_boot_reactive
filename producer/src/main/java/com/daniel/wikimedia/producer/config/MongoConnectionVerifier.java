package com.daniel.wikimedia.producer.config;

import com.dan.logging.LoggingFormatter;
import com.daniel.wikimedia.producer.producer.WikiMediaMessageRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.daniel.wikimedia.producer.logging.CommandConstants.WIKIMEDIA_PRODUCER_SERVICE_NAME;

@Component
@RequiredArgsConstructor
@Slf4j
public class MongoConnectionVerifier {
    private final WikiMediaMessageRepository messageRepository;

    @PostConstruct
    public void verifyConnection() {
        messageRepository.count()
            .doOnSuccess(count -> log.info(LoggingFormatter.WIKIMEDIA_LOGGING_FORMAT_V1, WIKIMEDIA_PRODUCER_SERVICE_NAME, "MongoConnectionVerifier", "verifyConnection", "Successfully connected to MongoDB. Message count: " + count))
            .doOnError(error -> log.error(LoggingFormatter.WIKIMEDIA_LOGGING_FORMAT_V1, WIKIMEDIA_PRODUCER_SERVICE_NAME, "MongoConnectionVerifier", "verifyConnection", "Failed to connect to MongoDB: " + error.getMessage()))
            .subscribe();
    }
}

