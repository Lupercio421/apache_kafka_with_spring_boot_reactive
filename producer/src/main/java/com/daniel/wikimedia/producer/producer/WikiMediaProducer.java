package com.daniel.wikimedia.producer.producer;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
@Slf4j
public class WikiMediaProducer {

    @Value("${spring.kafka.producer.topic-name}")
    private String wikiMediaStreamTopic;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String msg){
        log.info(format("Sending message to WikiMedia-Stream Topic:: %s", msg));
        kafkaTemplate.send(wikiMediaStreamTopic, msg);
    }

    @PreDestroy
    public void evaluateShutdown() throws InterruptedException {
        for (int i = 0; i <= 9; i++){
            log.info("WikiMedia Producer Application evaluateShutdown method, " + "clean-up");
            performCleanup();
        }
    }

    private void performCleanup() throws InterruptedException {
        Thread.sleep(500);
    }
}
