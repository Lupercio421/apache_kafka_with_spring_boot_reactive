package com.daniel.wikimedia.producer.producer;

import com.dan.logging.LoggingFormatter;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static com.daniel.wikimedia.producer.logging.CommandConstants.WIKIMEDIA_PRODUCER_SERVICE_NAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class WikiMediaProducer {

    @Value("${spring.kafka.producer.topic-name}")
    private String wikiMediaStreamTopic;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final WikiMediaMessageRepository messageRepository;

    public Mono<Void> sendMessage(String msg) {
        WikiMediaMessage message = new WikiMediaMessage(null, msg, Instant.now());
        return messageRepository.save(message)
            .doOnSuccess(saved -> log.info(LoggingFormatter.WIKIMEDIA_LOGGING_FORMAT_V1 + LoggingFormatter.DEFAULT_COMMA_APPENDER + LoggingFormatter.KV_MONGODB_MESSAGE, WIKIMEDIA_PRODUCER_SERVICE_NAME, "WikiMediaProducer", "sendMessage", "Saved message to MongoDB", msg)) //change KV to KV_MONGODB_MESSAGE
            .doOnError(error -> log.error(LoggingFormatter.WIKIMEDIA_LOGGING_FORMAT_V1, WIKIMEDIA_PRODUCER_SERVICE_NAME, "WikiMediaProducer", "sendMessage", "Failed to save message to MongoDB: " + error.getMessage()))
            .onErrorResume(error -> {
                // Return Mono.empty() for reactive error handling
                return Mono.empty();
            })
            .then(Mono.fromRunnable(() -> {
                try {
                    kafkaTemplate.send(wikiMediaStreamTopic, msg);
                    log.info(LoggingFormatter.WIKIMEDIA_LOGGING_FORMAT_V1 + LoggingFormatter.DEFAULT_COMMA_APPENDER + LoggingFormatter.KV_KAFKA_MESSAGE, WIKIMEDIA_PRODUCER_SERVICE_NAME, "WikiMediaProducer", "sendMessage", "Sent message to Kafka Topic", msg);
                } catch (Exception e) {
                    log.error(LoggingFormatter.WIKIMEDIA_LOGGING_FORMAT_V1, WIKIMEDIA_PRODUCER_SERVICE_NAME, "WikiMediaProducer", "sendMessage", "Failed to send message to Kafka: " + e.getMessage());
                }
            }));
    }

    @PreDestroy
    public void evaluateShutdown() throws InterruptedException {
        for (int i = 0; i <= 9; i++){
            log.info(LoggingFormatter.WIKIMEDIA_LOGGING_FORMAT_V1, WIKIMEDIA_PRODUCER_SERVICE_NAME, "WikiMediaProducer", "evaluateShutdown", "Shutting down WikiMedia Producer Application");
            performCleanup();
        }
    }

    private void performCleanup() throws InterruptedException {
        Thread.sleep(500);
    }
}
