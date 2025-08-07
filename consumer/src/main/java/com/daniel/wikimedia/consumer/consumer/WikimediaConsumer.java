package com.daniel.wikimedia.consumer.consumer;

import com.dan.logging.LoggingFormatter;
import com.daniel.wikimedia.consumer.metadataobject.WikimediaObject;
import com.daniel.wikimedia.consumer.repository.WikiMediaCrudRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.dan.logging.LoggingFormatter.*;
import static com.daniel.wikimedia.consumer.logging.CommandConstants.*;
import static java.lang.String.format;

@Service
@Slf4j
public class WikimediaConsumer {
    private final WikiMediaCrudRepository wikiMediaCrudRepository;
    ObjectMapper mapper = new ObjectMapper();
    String topicName = "wikimedia-stream";

    public WikimediaConsumer(WikiMediaCrudRepository wikiMediaCrudRepository){
        this.wikiMediaCrudRepository=wikiMediaCrudRepository;
    }

    @KafkaListener(topics = "wikimedia-stream", groupId = "DansGroup")
    public Mono<WikimediaObject> consumerMsg(String msg){
        log.info(WIKIMEDIA_LOGGING_FORMAT_V1 + DEFAULT_COMMA_APPENDER + KV_KAFKA_MESSAGE, WIKIMEDIA_CONSUMER_SERVICE_NAME, WIKIMEDIA_CONSUMER_SOURCE, WIKIMEDIA_CONSUMER_LOG_EVENT_NAME, "Consuming the message from daniel Topic", msg);

        // serialize the String message into a WikimediaObject?
        WikimediaObject wikimediaObject = new WikimediaObject();
        try{
            wikimediaObject = mapper.readValue(msg, WikimediaObject.class);;
            log.info(WIKIMEDIA_LOGGING_FORMAT_V1 + DEFAULT_COMMA_APPENDER + KV_WIKIMEDIA_OBJECT, WIKIMEDIA_CONSUMER_SERVICE_NAME, WIKIMEDIA_CONSUMER_SOURCE, WIKIMEDIA_CONSUMER_LOG_EVENT_NAME, "WikimediaObject created from message", wikimediaObject.toString());
        }
        catch (JsonProcessingException e){
            String concatenatedErrorMessage = LoggingFormatter.getThrowableMessage(e, "Error processing JSON message");
            log.error(WIKIMEDIA_LOGGING_FORMAT_V1_WITH_ERROR + DEFAULT_COMMA_APPENDER + KV_KAFKA_MESSAGE, WIKIMEDIA_CONSUMER_SERVICE_NAME, WIKIMEDIA_CONSUMER_SOURCE, "consumeMsg", "Error processing JSON message", concatenatedErrorMessage, msg);
            return Mono.error(new RuntimeException(format("Error processing JSON message: %s", concatenatedErrorMessage)));
        }
        // use reactive crud repository to save the object to mongodb collection
        return saveWikiMediaObject(wikimediaObject);
    }

    public Mono<WikimediaObject> saveWikiMediaObject(WikimediaObject wikimediaObject){
        log.info(WIKIMEDIA_LOGGING_FORMAT_V1 + DEFAULT_COMMA_APPENDER + KV_WIKIMEDIA_OBJECT, WIKIMEDIA_CONSUMER_SERVICE_NAME, WIKIMEDIA_CONSUMER_SOURCE, "saveWikiMediaObject", "Saving WikimediaObject to MongoDB", wikimediaObject.toString());
        return wikiMediaCrudRepository.save(wikimediaObject);
    }

    @PreDestroy
    public void evaluateShutdown() throws InterruptedException {
        for (int i = 0; i <= 9; i++){
            log.info(WIKIMEDIA_LOGGING_FORMAT_V1, WIKIMEDIA_CONSUMER_SERVICE_NAME, WIKIMEDIA_CONSUMER_SOURCE, "evaluateShutdown", "Shutting down Wikimedia Consumer Application");
            performCleanup();
        }
    }

    private void performCleanup() throws InterruptedException {
        Thread.sleep(200);
    }
}
