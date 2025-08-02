package com.daniel.wikimedia.consumer.repository;

import com.daniel.wikimedia.consumer.metadataobject.WikimediaObject;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Component;

@Component
public interface WikiMediaCrudRepository extends ReactiveMongoRepository<WikimediaObject, String> {
}
