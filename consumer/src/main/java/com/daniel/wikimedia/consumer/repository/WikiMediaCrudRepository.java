package com.daniel.wikimedia.consumer.repository;

import com.daniel.wikimedia.consumer.MetaDataObject.WikimediaObject;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface WikiMediaCrudRepository extends ReactiveMongoRepository<WikimediaObject, String> {
}
