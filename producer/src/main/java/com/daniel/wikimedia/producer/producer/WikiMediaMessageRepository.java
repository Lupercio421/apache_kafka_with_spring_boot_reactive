package com.daniel.wikimedia.producer.producer;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WikiMediaMessageRepository extends ReactiveMongoRepository<WikiMediaMessage, String> {
}
