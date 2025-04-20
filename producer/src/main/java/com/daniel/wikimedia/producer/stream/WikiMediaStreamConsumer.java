package com.daniel.wikimedia.producer.stream;

import com.daniel.wikimedia.producer.producer.WikiMediaProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
@Configuration
public class WikiMediaStreamConsumer {
    // The WebClient is used to consume the Wikimedia stream

    //@Value("${project.base-url}")
    private String baseUrl = "https://stream.wikimedia.org/v2";
    //@Value("${project.uri}")
    private String uri = "/stream/recentchange";

    private final WebClient webClient;
    private final WikiMediaProducer wikiMediaProducer;

    public WikiMediaStreamConsumer(WebClient.Builder webClientBuilder, WikiMediaProducer wikiMediaProducer) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
        this.wikiMediaProducer = wikiMediaProducer;
    }

    public void consumeStreamAndPublish(){
        webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(String.class)
                .subscribe(wikiMediaProducer::sendMessage);
    }
}
