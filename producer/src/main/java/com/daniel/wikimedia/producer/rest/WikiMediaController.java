package com.daniel.wikimedia.producer.rest;

import com.daniel.wikimedia.producer.stream.WikiMediaStreamConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wikimedia")
@RequiredArgsConstructor
public class WikiMediaController {
    private final WikiMediaStreamConsumer streamConsumer;

    @GetMapping
    public void startPublishing(){
        streamConsumer.consumeStreamAndPublish();
    }
}
