package com.daniel.wikimedia.producer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class WikimediaTopicConfig {

    @Value("${spring.kafka.producer.topic-name}")
    private String wikiMediaStreamTopic;

    @Bean
    public NewTopic wikiMediaStreamTopic(){
        return TopicBuilder
                .name(wikiMediaStreamTopic)
                .build();
    }
}
