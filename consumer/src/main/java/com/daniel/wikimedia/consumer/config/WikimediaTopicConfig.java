package com.daniel.wikimedia.consumer.config;

import com.dan.logging.LoggingFormatter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import static com.daniel.wikimedia.consumer.logging.CommandConstants.WIKIMEDIA_CONSUMER_SERVICE_NAME;

@Slf4j
@Configuration
public class WikimediaTopicConfig {
    @Bean
    public NewTopic wikiMediaStreamTopic(){
        log.info(LoggingFormatter.WIKIMEDIA_LOGGING_FORMAT_V1, WIKIMEDIA_CONSUMER_SERVICE_NAME, "WikimediaTopicConfig", "wikiMediaStreamTopicBuilder", "Creating Kafka Topic for Wikimedia Stream");
        return TopicBuilder
                .name("WikiMedia-Stream")
                .build();
    }
}
