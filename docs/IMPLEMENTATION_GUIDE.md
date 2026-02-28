# Implementation Guide: Consulting Report Recommendations
**For:** Wikimedia Real-time Event Processing System  
**Date:** February 28, 2026  
**Purpose:** Step-by-step guide to implement consulting report recommendations

---

## Table of Contents
1. [Priority 1: Critical Security Fixes](#priority-1-critical-security-fixes)
2. [Priority 2: High-Impact Changes](#priority-2-high-impact-changes)
3. [Priority 3: Code Quality](#priority-3-code-quality)
4. [Testing Implementation](#testing-implementation)
5. [Operational Readiness](#operational-readiness)

---

## Priority 1: Critical Security Fixes

### 1.1 CVE Remediation: Upgrade Apache Kafka Client

**Issue:** CVE-2024-31141 and CVE-2025-27817 in kafka-clients 3.6.2

**Action 1: Update Consumer pom.xml**

Find the `spring-kafka` dependency and update the version:

```xml
<!-- BEFORE -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
    <version>3.3.10</version>
</dependency>

<!-- AFTER -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
    <version>3.3.13</version>  <!-- Updated to get kafka-clients 3.9.1+ -->
</dependency>
```

**Action 2: Update Producer pom.xml**

Apply the same change to `producer/pom.xml`

**Action 3: Verify the Fix**

```bash
cd consumer
mvn clean dependency:tree | grep kafka-clients
# Should show: org.apache.kafka:kafka-clients:jar:3.9.1 (or newer)

cd ../producer
mvn clean dependency:tree | grep kafka-clients
# Should show: org.apache.kafka:kafka-clients:jar:3.9.1 (or newer)
```

**Action 4: Test Compilation**

```bash
mvn clean compile -DskipTests
# Should complete successfully
```

---

### 1.2 Remove Exposed Credentials from Source Code

**Issue:** MongoDB credentials visible in `producer/src/main/resources/application.yml`

**Step 1: Update Producer application.yml**

```yaml
# BEFORE (DON'T COMMIT THIS)
spring:
  data:
    mongodb:
      uri: mongodb+srv://danielllupercio:ReactiveKafka1025@wikimedia-cluster.g7k5q91.mongodb.net/?retryWrites=true&w=majority&appName=wikimedia-cluster
      database: wikimedia-cluster

# AFTER
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}  # Read from environment variable
      database: ${MONGODB_DATABASE:wikimedia-cluster}
```

**Step 2: Update Consumer application.yml (uncomment and secure)**

```yaml
# BEFORE (commented out)
# spring:
#   data:
#     mongodb:
#       uri: mongodb+srv://danielllupercio

# AFTER (environment variable)
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
      database: ${MONGODB_DATABASE:wikimedia-cluster}
```

**Step 3: Rotate MongoDB Credentials**

1. Go to MongoDB Atlas console
2. Create new database user with strong password
3. Copy new connection string
4. Remove old credentials from all environments

**Step 4: Store Credentials Securely**

**For Local Development:**
Create `.env` file (add to `.gitignore`):
```bash
MONGODB_URI=mongodb+srv://newuser:newpassword@cluster.mongodb.net/?retryWrites=true&w=majority
MONGODB_DATABASE=wikimedia-cluster
KAFKA_BROKERS=localhost:9092
WIKIMEDIA_BASE_URL=https://stream.wikimedia.org/v2
```

**For Docker:**
Create `.env.docker`:
```bash
MONGODB_URI=mongodb://mongodb:27017
```

**For Kubernetes:**
Create Secret:
```bash
kubectl create secret generic wikimedia-secrets \
  --from-literal=mongodb-uri='mongodb+srv://user:pass@...'
```

**Step 5: Update Git History (if credentials were committed)**

```bash
# Option 1: Using filter-branch (older repos)
git filter-branch --tree-filter 'git rm --cached -r application.yml' HEAD

# Option 2: Using BFG Repo-Cleaner (recommended for large repos)
bfg --delete-files application.yml
```

---

### 1.3 Externalize Configuration from Code

**File: `producer/src/main/java/com/daniel/wikimedia/producer/stream/WikiMediaStreamConsumer.java`**

**BEFORE:**
```java
@Service
@Slf4j
@Configuration
public class WikiMediaStreamConsumer {
    
    private String baseUrl = "https://stream.wikimedia.org/v2";  // ❌ Hardcoded
    private String uri = "/stream/recentchange";  // ❌ Hardcoded
    // ... rest of class
}
```

**AFTER:**
```java
@Service
@Slf4j
@Configuration
public class WikiMediaStreamConsumer {
    
    @Value("${wikimedia.base-url:https://stream.wikimedia.org/v2}")
    private String baseUrl;
    
    @Value("${wikimedia.uri:/stream/recentchange}")
    private String uri;
    
    // ... rest of class (no changes to methods)
}
```

**Add to `producer/src/main/resources/application.yml`:**
```yaml
wikimedia:
  base-url: ${WIKIMEDIA_BASE_URL:https://stream.wikimedia.org/v2}
  uri: ${WIKIMEDIA_URI:/stream/recentchange}
```

---

### 1.4 Add Health Check Endpoint

**Step 1: Add Actuator Dependency**

**File: `consumer/pom.xml` and `producer/pom.xml`**

Add after the existing test dependencies:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**Step 2: Configure Actuator in application.yml**

**File: `consumer/src/main/resources/application.yml`**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  health:
    livenessState:
      enabled: true
    readinessState:
      enabled: true
```

**File: `producer/src/main/resources/application.yml`**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
      base-path: /actuator
  endpoint:
    health:
      show-details: always
  health:
    livenessState:
      enabled: true
    readinessState:
      enabled: true
  server:
    port: 8081  # Same as application port
```

**Step 3: Test Health Endpoints**

```bash
# After starting the application
curl http://localhost:8080/actuator/health  # Consumer
curl http://localhost:8081/actuator/health  # Producer

# Expected response:
{
  "status": "UP",
  "components": {
    "kafkaProducer": {"status": "UP"},
    "mongoDb": {"status": "UP"}
  }
}
```

---

## Priority 2: High-Impact Changes

### 2.1 Standardize Kafka Topic Naming

**Issue:** Consumer config creates topic "WikiMedia-Stream" (mixed case) but listens to "wikimedia-stream" (lowercase)

**File: `consumer/src/main/java/com/daniel/wikimedia/consumer/config/WikimediaTopicConfig.java`**

**BEFORE:**
```java
@Configuration
public class WikimediaTopicConfig {
    @Bean
    public NewTopic wikiMediaStreamTopic(){
        return TopicBuilder
                .name("WikiMedia-Stream")  // ❌ Wrong case
                .build();
    }
}
```

**AFTER:**
```java
@Configuration
public class WikimediaTopicConfig {
    @Bean
    public NewTopic wikiMediaStreamTopic(){
        return TopicBuilder
                .name("wikimedia-stream")  // ✅ Lowercase, matches listener
                .partitions(3)
                .replicationFactor((short) 1)
                .build();
    }
}
```

**File: `producer/src/main/resources/application.yml`**

**BEFORE:**
```yaml
spring:
  kafka:
    producer:
      topic-name: wikimedia-stream
```

**AFTER:** (add explicit configuration)
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BROKERS:localhost:9092}
    producer:
      bootstrap-servers: ${KAFKA_BROKERS:localhost:9092}
      topic-name: ${KAFKA_TOPIC:wikimedia-stream}
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all
      retries: 3
      linger-ms: 10
```

**File: `consumer/src/main/resources/application.yml`**

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BROKERS:localhost:9092}
    consumer:
      bootstrap-servers: ${KAFKA_BROKERS:localhost:9092}
      group-id: ${KAFKA_CONSUMER_GROUP:wikimedia-consumer-group}
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      max-poll-records: 500
      enable-auto-commit: false
      isolation-level: read_committed
```

---

### 2.2 Implement Retry Logic with Exponential Backoff

**File: `producer/src/main/java/com/daniel/wikimedia/producer/stream/WikiMediaStreamConsumer.java`**

**BEFORE:**
```java
public void consumeStreamAndPublish(){
    webClient.get()
        .uri(uri)
        .retrieve()
        .bodyToFlux(String.class)
        .subscribe(wikiMediaProducer::sendMessage);
}
```

**AFTER:**
```java
public void consumeStreamAndPublish(){
    webClient.get()
        .uri(uri)
        .retrieve()
        .bodyToFlux(String.class)
        .flatMap(msg -> wikiMediaProducer.sendMessage(msg)
            .retry(3)  // Retry up to 3 times on failure
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))  // Exponential backoff
                .maxBackoff(Duration.ofSeconds(10))
                .doBeforeRetry(signal -> 
                    log.warn("Retrying message send, attempt {}", 
                        signal.totalRetries() + 1)
                )
            )
        )
        .doOnError(error -> 
            log.error("Fatal error processing Wikimedia message after retries", error)
        )
        .subscribe(
            unused -> {},
            error -> log.error("Stream subscription terminated with error", error),
            () -> log.info("Stream subscription completed normally")
        );
}
```

**Dependencies to add to `producer/pom.xml`:**
```xml
<!-- Retry backoff logic is in spring-retry -->
<dependency>
    <groupId>org.springframework.retry</groupId>
    <artifactId>spring-retry</artifactId>
</dependency>
```

---

### 2.3 Configure Jackson ObjectMapper as Spring Bean

**Create new file: `consumer/src/main/java/com/daniel/wikimedia/consumer/config/JacksonConfig.java`**

```java
package com.daniel.wikimedia.consumer.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Deserialization settings
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        
        // Serialization settings
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        return mapper;
    }
}
```

**Update: `consumer/src/main/java/com/daniel/wikimedia/consumer/consumer/WikimediaConsumer.java`**

**BEFORE:**
```java
@Service
@Slf4j
public class WikimediaConsumer {
    private final WikiMediaCrudRepository wikiMediaCrudRepository;
    ObjectMapper mapper = new ObjectMapper();  // ❌ Created fresh each time
    
    public WikimediaConsumer(WikiMediaCrudRepository wikiMediaCrudRepository){
        this.wikiMediaCrudRepository=wikiMediaCrudRepository;
    }
}
```

**AFTER:**
```java
@Service
@Slf4j
public class WikimediaConsumer {
    private final WikiMediaCrudRepository wikiMediaCrudRepository;
    private final ObjectMapper mapper;  // ✅ Injected dependency
    
    public WikimediaConsumer(WikiMediaCrudRepository wikiMediaCrudRepository, 
                           ObjectMapper mapper){
        this.wikiMediaCrudRepository = wikiMediaCrudRepository;
        this.mapper = mapper;
    }
    
    // Rest of class remains the same
}
```

---

### 2.4 Fix Consumer Group ID Configuration

**File: `consumer/src/main/java/com/daniel/wikimedia/consumer/consumer/WikimediaConsumer.java`**

**BEFORE:**
```java
@KafkaListener(topics = "wikimedia-stream", groupId = "DansGroup")
public Mono<WikimediaObject> consumerMsg(String msg){
```

**AFTER:**
```java
@KafkaListener(
    topics = "${kafka.topic.wikimedia:wikimedia-stream}",
    groupId = "${kafka.consumer.group:wikimedia-consumer-group}"
)
public Mono<WikimediaObject> consumerMsg(String msg){
```

---

### 2.5 Implement Graceful Shutdown Manager

**Create new file: `consumer/src/main/java/com/daniel/wikimedia/consumer/config/GracefulShutdownManager.java`**

```java
package com.daniel.wikimedia.consumer.config;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.context.ApplicationContext;

@Component
@RequiredArgsConstructor
@Slf4j
public class GracefulShutdownManager {
    
    private final ApplicationContext context;
    
    @PreDestroy
    public void gracefulShutdown() {
        log.info("Initiating graceful shutdown of Wikimedia Consumer...");
        
        try {
            // Stop all Kafka listeners to avoid pulling new messages
            context.getBeansOfType(AbstractMessageListenerContainer.class)
                .values()
                .forEach(container -> {
                    log.info("Stopping Kafka listener container");
                    container.stop();
                });
            
            // Allow time for in-flight messages to be processed
            Thread.sleep(5000);
            
            log.info("Graceful shutdown completed successfully");
        } catch (InterruptedException e) {
            log.warn("Interrupted during graceful shutdown", e);
            Thread.currentThread().interrupt();
        }
    }
}
```

**Remove the old @PreDestroy method from WikimediaConsumer:**
```java
// DELETE THIS:
@PreDestroy
public void evaluateShutdown() throws InterruptedException {
    // Old implementation
}
```

**Create equivalent for Producer: `producer/src/main/java/com/daniel/wikimedia/producer/config/GracefulShutdownManager.java`**

```java
package com.daniel.wikimedia.producer.config;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GracefulShutdownManager {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    @PreDestroy
    public void gracefulShutdown() {
        log.info("Initiating graceful shutdown of Wikimedia Producer...");
        
        try {
            // Wait for all pending messages to be sent to Kafka
            kafkaTemplate.flush();
            log.info("All pending Kafka messages flushed successfully");
            
            Thread.sleep(2000);  // Allow final cleanup
            
            log.info("Graceful shutdown completed successfully");
        } catch (InterruptedException e) {
            log.warn("Interrupted during graceful shutdown", e);
            Thread.currentThread().interrupt();
        }
    }
}
```

---

## Priority 3: Code Quality

### 3.1 Add Configuration Constants Class

**Create: `consumer/src/main/java/com/daniel/wikimedia/consumer/config/KafkaConstants.java`**

```java
package com.daniel.wikimedia.consumer.config;

public final class KafkaConstants {
    
    // Topic names
    public static final String WIKIMEDIA_TOPIC = "wikimedia-stream";
    public static final String DEAD_LETTER_TOPIC = "wikimedia-stream-dlq";
    
    // Consumer group
    public static final String CONSUMER_GROUP = "wikimedia-consumer-group";
    
    // Polling configuration
    public static final int MAX_POLL_RECORDS = 500;
    public static final long SESSION_TIMEOUT_MS = 45000;
    public static final long HEARTBEAT_INTERVAL_MS = 15000;
    
    private KafkaConstants() {
        throw new IllegalStateException("Constants class. Not meant to be instantiated.");
    }
}
```

---

## Testing Implementation

### 3.1 Add Unit Tests for WikimediaConsumer

**Create: `consumer/src/test/java/com/daniel/wikimedia/consumer/consumer/WikimediaConsumerTests.java`**

```java
package com.daniel.wikimedia.consumer.consumer;

import com.daniel.wikimedia.consumer.metadataobject.WikimediaObject;
import com.daniel.wikimedia.consumer.repository.WikiMediaCrudRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WikimediaConsumerTests {
    
    @Mock
    private WikiMediaCrudRepository repository;
    
    private WikimediaConsumer consumer;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        consumer = new WikimediaConsumer(repository, objectMapper);
    }
    
    @Test
    void testValidJsonDeserialization() {
        // Arrange
        String validJson = "{\"title\":\"Test Article\",\"user\":\"TestEditor\",\"comment\":\"Test edit\"}";
        WikimediaObject expected = new WikimediaObject();
        expected.setTitle("Test Article");
        expected.setUser("TestEditor");
        expected.setComment("Test edit");
        
        when(repository.save(any(WikimediaObject.class)))
            .thenReturn(Mono.just(expected));
        
        // Act
        Mono<WikimediaObject> result = consumer.consumerMsg(validJson);
        
        // Assert
        StepVerifier.create(result)
            .expectNext(expected)
            .verifyComplete();
    }
    
    @Test
    void testInvalidJsonHandling() {
        // Arrange
        String invalidJson = "{invalid json}";
        
        // Act
        Mono<WikimediaObject> result = consumer.consumerMsg(invalidJson);
        
        // Assert
        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();
    }
    
    @Test
    void testMongoDBFailureHandling() {
        // Arrange
        String validJson = "{\"title\":\"Test\",\"user\":\"Editor\"}";
        
        when(repository.save(any(WikimediaObject.class)))
            .thenReturn(Mono.error(new RuntimeException("MongoDB connection failed")));
        
        // Act
        Mono<WikimediaObject> result = consumer.consumerMsg(validJson);
        
        // Assert
        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();
    }
}
```

### 3.2 Add Integration Tests with Testcontainers

**Add dependency to `consumer/pom.xml`:**
```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.19.3</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mongodb</artifactId>
    <version>1.19.3</version>
    <scope>test</scope>
</dependency>
```

**Create: `consumer/src/test/java/com/daniel/wikimedia/consumer/integration/WikimediaConsumerIntegrationTest.java`**

```java
package com.daniel.wikimedia.consumer.integration;

import com.daniel.wikimedia.consumer.metadataobject.WikimediaObject;
import com.daniel.wikimedia.consumer.repository.WikiMediaCrudRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

@SpringBootTest
@Testcontainers
class WikimediaConsumerIntegrationTest {
    
    @Container
    static MongoDBContainer mongoDb = new MongoDBContainer("mongo:7.0");
    
    @Autowired
    private WikiMediaCrudRepository repository;
    
    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDb::getReplicaSetUrl);
    }
    
    @Test
    void testSaveAndRetrieveWikimediaObject() {
        // Arrange
        WikimediaObject obj = new WikimediaObject();
        obj.setTitle("Integration Test Article");
        obj.setUser("TestUser");
        obj.setComment("Integration test");
        
        // Act & Assert
        repository.save(obj)
            .flatMapMany(saved -> repository.findAll())
            .as(StepVerifier::create)
            .expectNextMatches(retrieved -> 
                "Integration Test Article".equals(retrieved.getTitle()))
            .verifyComplete();
    }
}
```

---

## Operational Readiness

### 4.1 Create Docker Compose for Local Development

**Create: `docker-compose.yml` (in project root)**

```yaml
version: '3.8'

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"

  mongodb:
    image: mongo:7.0
    ports:
      - "27017:27017"
    environment:
      MONGO_INITDB_DATABASE: wikimedia-cluster
    volumes:
      - mongodb_data:/data/db

  mongo-express:
    image: mongo-express:latest
    ports:
      - "8081:8081"
    depends_on:
      - mongodb
    environment:
      ME_CONFIG_MONGODB_URL: mongodb://mongodb:27017

volumes:
  mongodb_data:
```

**To use:**
```bash
docker-compose up -d
# Kafka will be available at localhost:9092
# MongoDB will be available at localhost:27017
# Mongo Express UI will be available at http://localhost:8081
```

---

### 4.2 Create Dockerfile for Containerization

**Create: `consumer/Dockerfile`**

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-17 as builder
WORKDIR /build
COPY consumer/pom.xml .
RUN mvn dependency:download-plugins dependency:resolve
COPY consumer/src src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/consumer-*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health

EXPOSE 8080
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
```

**Create: `producer/Dockerfile`**

```dockerfile
FROM maven:3.9-eclipse-temurin-17 as builder
WORKDIR /build
COPY producer/pom.xml .
RUN mvn dependency:download-plugins dependency:resolve
COPY producer/src src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/producer-*.jar app.jar

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health

EXPOSE 8081
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
```

---

## Verification Checklist

After implementing all changes:

```bash
# Step 1: Compile all modules
cd consumer && mvn clean compile -DskipTests
cd ../producer && mvn clean compile -DskipTests
cd ../logging-library && mvn clean compile -DskipTests

# Step 2: Verify dependencies
mvn dependency:tree | grep kafka-clients  # Should be 3.9.1+

# Step 3: Run existing tests
mvn test

# Step 4: Start with docker-compose
docker-compose up -d

# Step 5: Start applications
java -jar consumer/target/consumer-0.0.1-SNAPSHOT.jar
java -jar producer/target/producer-0.0.1-SNAPSHOT.jar

# Step 6: Verify health endpoints
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health

# Step 7: Check logs for errors
# All should indicate successful startup and configuration
```

---

## Timeline Estimation

| Phase | Actions | Effort | Timeline |
|-------|---------|--------|----------|
| Phase 1 (Critical) | CVE fix, credentials rotation, health checks | 4-5 hours | Day 1-2 |
| Phase 2 (High Priority) | Topic naming, retry logic, ObjectMapper, configs | 8-10 hours | Day 3-4 |
| Phase 3 (Quality) | Unit tests, integration tests | 8-12 hours | Day 5-7 |
| Phase 4 (Operational) | Docker, monitoring, documentation | 6-8 hours | Day 8-9 |
| **Total** | **Complete Modernization** | **26-35 hours** | **2 weeks** |

---

**Last Updated:** February 28, 2026

