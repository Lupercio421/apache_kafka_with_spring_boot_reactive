package com.daniel.wikimedia.producer.producer;

import com.dan.logging.LoggingFormatter;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static com.daniel.wikimedia.producer.logging.CommandConstants.WIKIMEDIA_PRODUCER_SERVICE_NAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class WikiMediaProducer {

    @Value("${spring.kafka.producer.topic-name}")
    private String wikiMediaStreamTopic;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String msg){
        log.info(LoggingFormatter.WIKIMEDIA_LOGGING_FORMAT_V1 + LoggingFormatter.DEFAULT_COMMA_APPENDER + LoggingFormatter.KV_KAFKA_MESSAGE,
        WIKIMEDIA_PRODUCER_SERVICE_NAME, "WikiMediaProducer", "sendMessage", "Sending message to Kafka Topic", msg);
        kafkaTemplate.send(wikiMediaStreamTopic, msg);
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
