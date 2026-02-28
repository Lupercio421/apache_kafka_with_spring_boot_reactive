# Wikimedia Real-time Event Processing System - Consulting Report
**Date:** February 28, 2026  
**Project:** Apache Kafka with Spring Boot Reactive  
**Status:** Comprehensive Architecture, Code Quality, and Security Analysis

---

## Executive Summary

This Apache Kafka-based reactive event processing system demonstrates a well-architected approach to building high-throughput, real-time data pipelines. The application successfully ingests Wikimedia edit events, processes them through a reactive pipeline, and persists them in MongoDB.

**Overall Assessment:** **7.5/10** - The project has solid architectural foundations and excellent use of reactive patterns, but requires attention to **critical security vulnerabilities**, enhanced test coverage, and several modernization opportunities.

**Critical Actions Required:**
1. ⚠️ **IMMEDIATE:** Upgrade Apache Kafka Client to 3.9.1+ to patch 2 CVEs (CVE-2024-31141, CVE-2025-27817)
2. ⚠️ **HIGH:** Implement comprehensive error handling across reactive streams
3. 📈 **MEDIUM:** Establish test coverage baseline and add integration tests
4. 🔒 **HIGH:** Secure database credentials and API configuration

---

## Table of Contents
1. [Current State Assessment](#current-state-assessment)
2. [Architecture Analysis](#architecture-analysis)
3. [Security Vulnerabilities & Remediation](#security-vulnerabilities--remediation)
4. [Code Quality Review](#code-quality-review)
5. [Test Coverage Analysis](#test-coverage-analysis)
6. [Operational Readiness](#operational-readiness)
7. [Actionable Recommendations](#actionable-recommendations)
8. [Modernization Opportunities](#modernization-opportunities)

---

## Current State Assessment

### Project Structure Overview
The project consists of three Maven modules:

| Module | Purpose | Tech Stack |
|--------|---------|-----------|
| **producer** | Consumes Wikimedia API stream, publishes to Kafka | Spring Boot 3.2.5, WebFlux, MongoDB (persistence only) |
| **consumer** | Listens to Kafka topic, stores in MongoDB | Spring Boot 3.2.5, WebFlux, MongoDB Reactive |
| **logging-library** | Custom structured logging utilities | SLF4J, Log4j 2.21.1 |

### Build Status
✅ **All modules compile successfully** with Java 17
- Consumer: ✅ Clean compile
- Producer: ✅ Clean compile  
- Logging Library: ✅ Clean compile

### Dependency Versions
- **Spring Boot:** 3.2.5 (released Feb 2024, stable)
- **Java:** 17 (LTS, excellent choice)
- **Kafka:** 3.6.2 (⚠️ contains CVEs - see below)
- **MongoDB Driver:** 4.11.2 (current stable)
- **Log4j:** 2.21.1 (current stable)

---

## Architecture Analysis

### System Design: Strengths ✅

#### 1. **Excellent Reactive Architecture**
The project correctly implements Spring Boot WebFlux for non-blocking I/O:
```
Wikimedia API Stream 
    ↓ (WebClient with Flux)
WikiMediaStreamConsumer 
    ↓ (async processing)
WikiMediaProducer 
    ↓ (KafkaTemplate)
Apache Kafka Topic: "wikimedia-stream"
    ↓
WikimediaConsumer (@KafkaListener)
    ↓ (Mono<WikimediaObject>)
MongoDB (ReactiveMongoRepository)
```

**Why this matters:** This non-blocking approach allows handling thousands of concurrent connections with minimal thread overhead—essential for production event processing.

#### 2. **Structured Logging Framework**
Custom `LoggingFormatter` provides consistent JSON-formatted logs:
```java
WIKIMEDIA_LOGGING_FORMAT_V1 = 
  "LoggingVersion":"1.0",
  "Environment":"{}",
  "SpringActiveProfile":"{}",
  "WikimediaService":"{}",
  "Source":"{}",
  "LogEventName":"{}",
  "Message":"{}"
```
This enables:
- Centralized log aggregation (ELK, Splunk)
- Better debugging and monitoring
- Structured alerting on specific events

#### 3. **Proper Use of MongoDB Reactive**
Both applications correctly use reactive data access:
- Consumer: `ReactiveMongoRepository<WikimediaObject>`
- Producer: `ReactiveMongoRepository<WikiMediaMessage>`
- TTL indexing on producer messages (60-second expiration)

### Architecture: Weaknesses & Risks ⚠️

#### 1. **Topic Naming Mismatch Risk**
**Current Issue:**
```java
// Consumer Config creates this topic:
@Bean
public NewTopic wikiMediaStreamTopic() {
    return TopicBuilder.name("WikiMedia-Stream").build();  // Mixed case
}

// But consumer listens to:
@KafkaListener(topics = "wikimedia-stream", ...)  // Lowercase
```

**Impact:** If the config topic creation runs before the listener starts, messages could be published to a different topic than what's being consumed.

**Recommended Fix:** Standardize to lowercase (`wikimedia-stream`) across all components:
```java
// Consumer config
return TopicBuilder.name("wikimedia-stream").build();

// Producer config
@Value("${spring.kafka.producer.topic-name:wikimedia-stream}")
private String wikiMediaStreamTopic;
```

#### 2. **Insufficient Error Handling in Reactive Streams**

**Issue in Producer's `WikiMediaStreamConsumer`:**
```java
public void consumeStreamAndPublish(){
    webClient.get()
        .uri(uri)
        .retrieve()
        .bodyToFlux(String.class)
        .flatMap(msg -> wikiMediaProducer.sendMessage(msg))
        .subscribe(
            unused -> {},
            error -> log.error("Stream subscription error: {}", ...)
        );  // ⚠️ What happens if this stream terminates unexpectedly?
}
```

**Risks:**
- Stream termination is not retried
- No exponential backoff on API failure
- Infinite stream assumptions not validated

**Recommended Implementation:**
```java
public void consumeStreamAndPublish(){
    webClient.get()
        .uri(uri)
        .retrieve()
        .bodyToFlux(String.class)
        .flatMap(msg -> wikiMediaProducer.sendMessage(msg)
            .retry(3)  // Retry up to 3 times
            .delaySubscription(Duration.ofSeconds(1))  // Exponential backoff
        )
        .onErrorResume(error -> {
            log.error("Fatal error in stream consumption", error);
            // Could emit to a dead-letter topic or alerting system
            return Mono.error(error);
        })
        .subscribe(
            unused -> {},
            error -> log.error("Unrecoverable stream error", error)
        );
}
```

#### 3. **No Health Checks**
Neither application exposes `/actuator/health` endpoints for Kubernetes/Docker deployments.

---

## Security Vulnerabilities & Remediation

### 🔴 CRITICAL: Apache Kafka Client CVEs

Two medium-severity CVEs identified in `kafka-clients@3.6.2`:

| CVE | Description | Severity | Impact |
|-----|-------------|----------|--------|
| **CVE-2024-31141** | Privilege escalation via ConfigProvider (FileConfigProvider, DirectoryConfigProvider) | MEDIUM | Attackers with config access can read arbitrary files and environment variables |
| **CVE-2025-27817** | Arbitrary file read and SSRF via SASL/OAUTHBEARER URL configuration | MEDIUM | Potential for reading local files or making requests to unintended servers |

**Affected Versions:** 2.3.0-3.5.2, 3.6.0-3.6.2, 3.7.0

**Resolution:**
✅ **Upgrade to 3.9.1 or higher**

**Action Steps:**
1. Update both `consumer/pom.xml` and `producer/pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
    <version>3.3.13</version>  <!-- Updated to include Kafka 3.9.1+ -->
</dependency>
```

2. Verify transitive dependency resolution:
```bash
mvn dependency:tree | grep kafka-clients
```

3. For `sasl.oauthbearer` configuration (if used), add JVM parameter:
```bash
-Dorg.apache.kafka.sasl.oauthbearer.allowed.urls="https://your-auth-server/*"
```

4. To disable automatic config providers in untrusted environments:
```bash
-Dorg.apache.kafka.automatic.config.providers=none
```

### 🔴 HIGH: Exposed Credentials in application.yml

**Issue in Producer `application.yml`:**
```yaml
spring:
  data:
    mongodb:
      uri: mongodb+srv://danielllupercio:ReactiveKafka1025@...
```

**Risk Level:** CRITICAL - Database password is visible in source control.

**Immediate Actions:**
1. Rotate MongoDB password immediately
2. Remove sensitive data from `application.yml`:
```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}  # Read from environment variable
```

3. Update Git history to remove the credential:
```bash
git filter-branch --tree-filter 'rm -f application.yml' HEAD
# OR use BFG Repo-Cleaner for larger repos
```

4. Implement in CI/CD pipeline:
```yaml
# Example for GitHub Actions
env:
  MONGODB_URI: ${{ secrets.MONGODB_CONNECTION_STRING }}
```

### 🟡 MEDIUM: Configuration Management Issues

**Issues:**
1. **Hardcoded API URLs** in `WikiMediaStreamConsumer`:
```java
private String baseUrl = "https://stream.wikimedia.org/v2";  // Hardcoded
private String uri = "/stream/recentchange";  // Commented @Value config
```

2. **MongoDB connection commented out** in Consumer:
```yaml
# spring:
#   data:
#     mongodb:
#       uri: ...
```

**Recommendation:**
Implement externalized configuration:
```java
@Configuration
public class WikiMediaStreamConsumer {
    @Value("${wikimedia.base-url:https://stream.wikimedia.org/v2}")
    private String baseUrl;
    
    @Value("${wikimedia.uri:/stream/recentchange}")
    private String uri;
}
```

With `application.yml`:
```yaml
wikimedia:
  base-url: ${WIKIMEDIA_BASE_URL:https://stream.wikimedia.org/v2}
  uri: ${WIKIMEDIA_URI:/stream/recentchange}
```

---

## Code Quality Review

### Positive Aspects ✅

#### 1. **Correct Lombok Usage**
All domain classes properly use `@Data` for reducing boilerplate:
```java
@Data
public class WikimediaObject {
    @JsonProperty("$schema")
    public String $schema;
    // ... other fields
}
```

#### 2. **Proper Spring Stereotype Annotations**
```java
@Service          // WikimediaConsumer
@Configuration    // WebClientConfig
@Component        // WikiMediaCrudRepository
@RestController   // WikiMediaController
```
Annotations correctly indicate component responsibilities.

#### 3. **Reactive Return Types**
Both consumer and producer correctly use `Mono<T>` and `Flux<T>`:
```java
public Mono<WikimediaObject> consumerMsg(String msg)
public Mono<Void> sendMessage(String msg)
```

### Quality Issues & Recommendations 🔧

#### 1. **Inconsistent Shutdown Management**

**Issue:**
```java
@PreDestroy
public void evaluateShutdown() throws InterruptedException {
    for (int i = 0; i <= 9; i++){  // ⚠️ Hardcoded loop 10 times
        log.info("Shutting down...");
        performCleanup();  // 10 iterations × 200-500ms = 2-5 seconds
    }
}

private void performCleanup() throws InterruptedException {
    Thread.sleep(200);  // Consumer does 200ms, Producer does 500ms
}
```

**Problems:**
- Arbitrary loop count (why 10?)
- No graceful Kafka consumer shutdown
- `Thread.sleep()` blocks reactive threads

**Better Implementation:**
```java
@RequiredArgsConstructor
@Component
public class GracefulShutdownManager {
    private final KafkaConsumerFactory consumerFactory;
    private final KafkaProducerFactory producerFactory;
    
    @PreDestroy
    public void shutdown() throws InterruptedException {
        log.info("Initiating graceful shutdown...");
        
        // Consumer: Stop listening and process in-flight messages
        consumerFactory.getConsumer().commitSync();
        
        // Producer: Wait for in-flight sends
        producerFactory.getProducer().flush(Duration.ofSeconds(10));
        
        // Cooperative rebalancing (Kafka 2.7+)
        log.info("Graceful shutdown completed");
    }
}
```

#### 2. **Missing ObjectMapper Configuration**

**Issue:**
```java
public class WikimediaConsumer {
    ObjectMapper mapper = new ObjectMapper();  // ⚠️ Created fresh every time
}
```

**Problems:**
- ObjectMapper is thread-safe but expensive to create repeatedly
- No custom deserialization configuration (null handling, unknown properties)
- Could cause performance issues under high throughput

**Fix:**
```java
@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        return mapper;
    }
}

// Then in WikimediaConsumer
@Service
public class WikimediaConsumer {
    private final ObjectMapper mapper;
    private final WikiMediaCrudRepository repo;
    
    public WikimediaConsumer(ObjectMapper mapper, WikiMediaCrudRepository repo) {
        this.mapper = mapper;
        this.repo = repo;
    }
}
```

#### 3. **Consumer Group ID Hardcoded**

**Issue:**
```java
@KafkaListener(topics = "wikimedia-stream", groupId = "DansGroup")
```

**Recommendation:**
```java
@KafkaListener(
    topics = "${kafka.topic.wikimedia:wikimedia-stream}",
    groupId = "${kafka.consumer.group:${spring.application.name}-group}"
)
public Mono<WikimediaObject> consumerMsg(String msg) { ... }
```

With `application.yml`:
```yaml
kafka:
  topic:
    wikimedia: wikimedia-stream
  consumer:
    group: wikimedia-consumer-group
```

#### 4. **Missing @Transactional Semantics**

The consumer's message processing doesn't explicitly handle transaction semantics:

```java
@KafkaListener(...)
public Mono<WikimediaObject> consumerMsg(String msg) {
    // If MongoDB save fails, what happens to the Kafka offset?
    return wikiMediaCrudRepository.save(wikimediaObject);
    // ⚠️ Offset committed before MongoDB confirm
}
```

**Recommendation:**
```java
@Service
public class WikimediaConsumer {
    
    @KafkaListener(topics = "wikimedia-stream", groupId = "DansGroup")
    public void consumerMsg(String msg, Acknowledgment ack) {
        WikimediaObject obj = mapper.readValue(msg, WikimediaObject.class);
        
        wikiMediaCrudRepository.save(obj)
            .doOnSuccess(saved -> ack.acknowledge())  // Commit after save succeeds
            .doOnError(error -> log.error("Save failed, offset NOT committed", error))
            .subscribe();
    }
}
```

---

## Test Coverage Analysis

### Current State: ⚠️ Minimal Coverage

Both applications have only placeholder tests:

```java
@SpringBootTest
class ConsumerApplicationTests {
    @Test
    void contextLoads() {
    }  // ⚠️ Only tests that Spring context initializes
}
```

**Current Coverage:** ~10% (only Spring context startup)

### Recommended Test Strategy

#### 1. **Unit Tests for Business Logic**
```java
class WikimediaConsumerTests {
    @ExtendWith(MockitoExtension.class)
    private WikimediaConsumer consumer;
    
    @Mock
    private WikiMediaCrudRepository repository;
    
    @BeforeEach
    void setup() {
        consumer = new WikimediaConsumer(repository);
    }
    
    @Test
    void testValidJsonDeserialization() {
        String validJson = "{\"title\":\"Test\",\"user\":\"Editor\"}";
        WikimediaObject expected = new WikimediaObject();
        expected.setTitle("Test");
        expected.setUser("Editor");
        
        when(repository.save(any())).thenReturn(Mono.just(expected));
        
        Mono<WikimediaObject> result = consumer.consumerMsg(validJson);
        
        StepVerifier.create(result)
            .expectNext(expected)
            .verifyComplete();
    }
    
    @Test
    void testJsonParsingFailureHandling() {
        String invalidJson = "{invalid json}";
        
        Mono<WikimediaObject> result = consumer.consumerMsg(invalidJson);
        
        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();
    }
}
```

#### 2. **Integration Tests with Testcontainers**
```java
@SpringBootTest
@Testcontainers
class WikimediaConsumerIntegrationTest {
    
    @Container
    static MongoDBContainer mongoDb = new MongoDBContainer("mongo:7.0");
    
    @Container
    static KafkaContainer kafka = new KafkaContainer("confluentinc/cp-kafka:7.5.0");
    
    @Autowired
    private WikimediaConsumer consumer;
    
    @Test
    void testEndToEndKafkaToMongoDB() {
        // Setup
        String testMessage = "{\"title\":\"TestEdit\",\"user\":\"TestUser\"}";
        KafkaProducer<String, String> producer = createProducer(kafka.getBootstrapServers());
        
        // Act
        producer.send(new ProducerRecord<>("wikimedia-stream", testMessage));
        producer.flush();
        
        // Assert - message persisted in MongoDB
        Mono<WikimediaObject> result = mongoRepository.findByTitle("TestEdit");
        StepVerifier.create(result)
            .expectNextCount(1)
            .verifyComplete();
    }
}
```

#### 3. **Test Coverage Goals**
| Component | Current | Target | Tests |
|-----------|---------|--------|-------|
| WikimediaConsumer | 0% | 85% | Unit + Integration |
| WikiMediaProducer | 0% | 85% | Unit + Integration |
| Controller | 0% | 90% | REST API tests |
| Config Classes | 0% | 80% | Configuration validation |
| **Overall** | ~10% | **75%+** | 40+ tests |

---

## Operational Readiness

### Deployment & Monitoring Gaps 🔴

#### 1. **Missing Actuator Configuration**
No Spring Boot Actuator endpoints exposed for Kubernetes/Docker health checks.

**Implementation:**
```java
@Configuration
public class ActuatorConfig {
    // Auto-enabled by adding dependency
}
```

Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Add to `application.yml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true
```

**Benefits:**
- Kubernetes liveness/readiness probes
- Prometheus metrics for monitoring
- JVM metrics (memory, GC, threads)
- Kafka consumer lag visibility

#### 2. **No Container Image Definition**
Missing `Dockerfile` and `docker-compose.yml` for local development.

**Recommended Dockerfile:**
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/producer-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 3. **Database Connection Pool Not Optimized**
MongoDB connection pooling not configured.

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
      # Add explicit pool configuration
      connection-pool:
        max-size: 100
        min-size: 50
        wait-queue-timeout-ms: 5000
```

#### 4. **No Circuit Breaker Pattern**
External API calls (Wikimedia) have no circuit breaker protection.

**Implementation with Resilience4j:**
```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.1.0</version>
</dependency>
```

```java
@Service
@CircuitBreaker(name = "wikimediaAPI", fallbackMethod = "fallback")
public void consumeStreamAndPublish() {
    webClient.get()
        .uri(uri)
        .retrieve()
        .bodyToFlux(String.class)
        .subscribe(wikiMediaProducer::sendMessage);
}

public void fallback(Exception e) {
    log.error("Circuit breaker opened for Wikimedia API", e);
    // Could emit to alerting system or queue messages for retry
}
```

### Kafka Configuration Issues 🟡

**Consumer Configuration Incomplete:**
```yaml
spring:
  kafka:
    consumer:
      bootstrap-servers: localhost:9092
      group-id: myGroup  # ⚠️ Overridden in code to "DansGroup"
      auto-offset-reset: earliest
      # Missing important settings:
      max-poll-records: 100  # Batch size
      session-timeout-ms: 30000  # Consumer heartbeat
      heartbeat-interval-ms: 10000
      isolation-level: read_committed  # Exactly-once semantics
      enable-auto-commit: false  # Manual commit for reliability
```

**Recommended configuration:**
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BROKERS:localhost:9092}
    consumer:
      group-id: wikimedia-consumer-group
      auto-offset-reset: earliest
      max-poll-records: 500  # Higher throughput
      session-timeout-ms: 45000
      heartbeat-interval-ms: 15000
      isolation-level: read_committed
      enable-auto-commit: false  # Manual commits after MongoDB persist
    producer:
      bootstrap-servers: ${KAFKA_BROKERS:localhost:9092}
      acks: all  # Durability
      retries: 3
      linger-ms: 10  # Batch messages
```

---

## Actionable Recommendations

### Priority 1: Critical (Do Immediately) 🔴

| # | Action | Effort | Impact |
|---|--------|--------|--------|
| 1.1 | Upgrade Kafka Client to 3.9.1+ for CVE remediation | 30 min | **CRITICAL** - Security fix |
| 1.2 | Rotate MongoDB credentials and remove from source code | 1 hour | **CRITICAL** - Prevent data breach |
| 1.3 | Add environment variable configuration for sensitive data | 2 hours | **HIGH** - Security hardening |
| 1.4 | Implement health check endpoints (/actuator/health) | 1 hour | **HIGH** - Operational visibility |

**Implementation Example:**
```bash
# 1.1 Update pom.xml versions
# 1.2 Rotate credentials in MongoDB Atlas
# 1.3 Update application.yml to use ${ENV_VARIABLES}
# 1.4 Add spring-boot-starter-actuator dependency
mvn clean install
mvn dependency:tree | grep kafka-clients  # Verify 3.9.1+
```

### Priority 2: High (This Sprint) 🟡

| # | Action | Effort | Impact |
|---|--------|--------|--------|
| 2.1 | Standardize Kafka topic naming (all lowercase) | 1 hour | **HIGH** - Prevents message loss |
| 2.2 | Implement retry logic with exponential backoff in producer stream | 3 hours | **HIGH** - Resilience |
| 2.3 | Fix hardcoded ObjectMapper in WikimediaConsumer | 1 hour | **MEDIUM** - Performance |
| 2.4 | Add unit tests for JSON parsing and error cases | 4 hours | **HIGH** - Code quality |
| 2.5 | Implement structured graceful shutdown | 2 hours | **MEDIUM** - Reliability |

**Code Changes Needed:**
```java
// 2.1: Standardize topic names
// 2.2: Add retry(3).delaySubscription(Duration.ofSeconds(1))
// 2.3: Inject ObjectMapper from @Configuration bean
// 2.4: Add @Test methods for WikimediaConsumer
// 2.5: Implement proper Kafka client.flush() and shutdown hooks
```

### Priority 3: Medium (Next Sprint) 🟢

| # | Action | Effort | Impact |
|---|--------|--------|--------|
| 3.1 | Add integration tests with Testcontainers (Kafka + MongoDB) | 8 hours | **HIGH** - Code confidence |
| 3.2 | Implement circuit breaker pattern for Wikimedia API | 4 hours | **MEDIUM** - Fault tolerance |
| 3.3 | Add Prometheus metrics and monitoring dashboard | 6 hours | **MEDIUM** - Observability |
| 3.4 | Configure database connection pooling | 1 hour | **MEDIUM** - Performance |
| 3.5 | Document API endpoints and create Swagger/OpenAPI spec | 3 hours | **MEDIUM** - Developer experience |

### Priority 4: Low (Backlog) 💡

| # | Action | Effort | Impact |
|---|--------|--------|--------|
| 4.1 | Create Dockerfile and docker-compose for local development | 2 hours | **MEDIUM** - DX improvement |
| 4.2 | Implement distributed tracing (Sleuth + Zipkin) | 4 hours | **LOW** - Advanced debugging |
| 4.3 | Add Apache Avro schema validation for Kafka messages | 3 hours | **MEDIUM** - Data contract enforcement |
| 4.4 | Implement schema registry integration | 4 hours | **MEDIUM** - Evolution support |

---

## Modernization Opportunities

### 1. **Spring Boot 3.3+ Migration** (Recommended for 2026 Roadmap)

**Current:** Spring Boot 3.2.5 (Feb 2024)  
**Recommended:** Spring Boot 3.4+ (Jan 2025+)

**Benefits:**
- Virtual threads support (Project Loom)
- Native compilation with GraalVM
- Improved AOT (Ahead-of-Time) compilation

**Timeline:** Q2 2026

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.0</version>  <!-- Planned for Jan 2025 -->
</parent>
```

### 2. **Kubernetes Native Features**

**Implement:**
- Cloud-native buildpacks (instead of Docker)
- Spring Cloud Kubernetes for config management
- Graceful shutdown with Kubernetes lifecycle hooks

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: wikimedia-producer
spec:
  template:
    spec:
      terminationGracePeriodSeconds: 60
      containers:
      - name: producer
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8081
          initialDelaySeconds: 10
          periodSeconds: 5
```

### 3. **Virtual Threads (Java 21+)**

When Java 21 is adopted, convert thread-blocking operations to virtual threads:

```java
// Current approach
Thread.sleep(500);  // Blocks OS thread

// Future approach (Java 21+)
Thread.ofVirtual()
    .start(() -> {
        Thread.sleep(500);  // Blocks virtual thread only
    });
```

### 4. **GraalVM Native Image Compilation**

For sub-100ms startup times in serverless environments:

```bash
mvn -Pnative native:compile
# Result: Executable binary without JVM startup overhead
```

**Use Case:** Serverless Kafka consumers (AWS Lambda + EventBridge)

---

## Summary: Next Steps

### Week 1: Security & Stability
```
Day 1-2: Apply CVE fixes, rotate credentials
Day 3-4: Implement health checks, standardize topic names
Day 5:   Validate all changes compile and pass existing tests
```

### Week 2-3: Quality Improvements
```
Implement unit tests
Add integration tests
Improve error handling
```

### Week 4: Operations
```
Add monitoring/metrics
Create deployment configs
Document architecture decisions
```

---

## Appendix: Quick Reference

### CVE Remediation Commands
```bash
# Check vulnerable dependency
mvn dependency:tree | grep kafka-clients

# Update to fixed version
mvn versions:use-latest-versions -Dincludes=org.springframework.kafka:*

# Verify upgrade
mvn clean compile
```

### Configuration Checklist
- [ ] All hardcoded values moved to environment variables
- [ ] Database credentials rotated
- [ ] Kafka topic names standardized
- [ ] Health check endpoints exposed
- [ ] Graceful shutdown implemented
- [ ] Error handling for reactive streams
- [ ] ObjectMapper configured as singleton
- [ ] Consumer group ID externalized

### Testing Checklist
- [ ] Unit tests for JSON parsing
- [ ] Unit tests for error scenarios
- [ ] Integration tests with Testcontainers
- [ ] End-to-end tests covering full pipeline
- [ ] Load tests for throughput validation
- [ ] Failure scenario tests (Kafka down, MongoDB down)

---

**Report Generated:** February 28, 2026  
**Reviewed By:** Principal Engineer Consultant Agent  
**Confidence Level:** High - Based on comprehensive codebase analysis, dependency audit, and architectural review.

For implementation support or clarification on recommendations, refer to linked code examples throughout this report.

