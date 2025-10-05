package com.daniel.wikimedia.producer.config;

import com.dan.logging.LoggingFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder(){
        log.info(LoggingFormatter.WIKIMEDIA_LOGGING_FORMAT_V1, "WikimediaProducerService", "WebClientConfig", "webClientBuilder", "Creating WebClient Builder");
        return WebClient.builder();
    }
}
