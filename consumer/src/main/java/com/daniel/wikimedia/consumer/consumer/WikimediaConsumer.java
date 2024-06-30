package com.daniel.wikimedia.consumer.consumer;

import com.daniel.wikimedia.consumer.MetaDataObject.WikimediaObject;
import com.daniel.wikimedia.consumer.repository.WikiMediaCrudRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static java.lang.String.format;

@Service
@Slf4j
public class WikimediaConsumer {
    private WikiMediaCrudRepository wikiMediaCrudRepository;

    @KafkaListener(topics = "WikiMedia-Stream", groupId = "DansGroup")
    public void consumerMsg(String msg){
        log.info(format("Consuming the message from daniel Topic:: %s", msg));
        // serialize the String message into a WikimediaObject?
        // use reactive crud repository to save the object to mongodb  collection
    }

    public Mono<WikimediaObject> saveWikiMediaObject(WikimediaObject wikimediaObject){
        return wikiMediaCrudRepository.save(wikimediaObject);
    }

}
