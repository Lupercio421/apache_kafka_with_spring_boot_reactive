package com.daniel.wikimedia.consumer.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Service
@Slf4j
public class WikimediaConsumer {

    @KafkaListener(topics = "WikiMedia-Stream", groupId = "DansGroup")
    public void consumerMsg(String msg){
        log.info(format("Consuming the message from daniel Topic:: %s", msg));
    }

}
