package com.daniel.wikimedia.producer.config;

import com.dan.logging.LoggingFormatter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Slf4j
@Configuration
public class WikimediaTopicConfig {

    @Value("${spring.kafka.producer.topic-name}")
    private String wikiMediaStreamTopic;

    @Bean
    public NewTopic wikiMediaStreamTopic(){
        log.info(LoggingFormatter.WIKIMEDIA_LOGGING_FORMAT_V1, "WikimediaProducerService", "WikimediaTopicConfig", "wikiMediaStreamTopicBuilder", "Creating Kafka Topic for Wikimedia Stream", wikiMediaStreamTopic);
        return TopicBuilder
                .name(wikiMediaStreamTopic)
                .build();
    }
}
