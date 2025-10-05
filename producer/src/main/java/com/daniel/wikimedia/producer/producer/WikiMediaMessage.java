package com.daniel.wikimedia.producer.producer;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wikimedia_messages")
public class WikiMediaMessage {
    @Id
    private String id;
    private String content;
    @Indexed(name = "expireAfterSeconds", expireAfterSeconds = 60)
    private Instant createdAt;
}
