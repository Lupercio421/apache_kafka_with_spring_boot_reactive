# Quick Reference: Critical Actions
**Project:** Wikimedia Real-time Event Processing System  
**Generated:** February 28, 2026  
**Purpose:** One-page summary for developers - print and post!

---

## 🔴 CRITICAL: Do These First

### Week 1 Priority Actions

```
[ ] 1. UPGRADE KAFKA
    mvn versions:use-latest-versions -Dincludes=org.springframework.kafka:*
    Verify: mvn dependency:tree | grep kafka-clients  (should be 3.9.1+)

[ ] 2. SECURE CREDENTIALS
    - Update producer/application.yml:
      uri: ${MONGODB_URI}  (not hardcoded)
    - Rotate MongoDB password
    - Add to .gitignore and .env

[ ] 3. ADD HEALTH CHECKS
    - Add spring-boot-starter-actuator to pom.xml
    - Update application.yml with /actuator/health endpoints
    - Test: curl http://localhost:8080/actuator/health

[ ] 4. FIX KAFKA TOPIC NAMING
    - Consumer config: create "wikimedia-stream" (lowercase)
    - Listener: use "wikimedia-stream" (lowercase)
    - Producer config: use "wikimedia-stream"
```

---

## 🟡 HIGH PRIORITY: This Sprint

```
[ ] Implement retry logic in producer stream
    .retry(3)
    .retryWhen(Retry.backoff(...))

[ ] Configure ObjectMapper as @Bean
    - Create JacksonConfig class
    - Inject into WikimediaConsumer

[ ] Standardize configuration
    - Use @Value for hardcoded strings
    - Read from application.yml
    - Support environment variables

[ ] Fix shutdown handler
    - Remove old @PreDestroy methods
    - Create GracefulShutdownManager
    - Properly flush Kafka messages

[ ] Write unit tests
    - Add test for valid JSON parsing
    - Add test for invalid JSON handling
    - Add test for MongoDB failure scenarios
```

---

## 📋 Configuration Checklist

```
Consumer (application.yml):
[ ] spring.kafka.bootstrap-servers = ${KAFKA_BROKERS}
[ ] spring.kafka.consumer.group-id = wikimedia-consumer-group
[ ] spring.kafka.consumer.enable-auto-commit = false
[ ] spring.data.mongodb.uri = ${MONGODB_URI}
[ ] management.endpoints.web.exposure.include = health,metrics

Producer (application.yml):
[ ] spring.kafka.bootstrap-servers = ${KAFKA_BROKERS}
[ ] spring.kafka.producer.topic-name = wikimedia-stream
[ ] wikimedia.base-url = ${WIKIMEDIA_BASE_URL}
[ ] wikimedia.uri = ${WIKIMEDIA_URI}
[ ] spring.data.mongodb.uri = ${MONGODB_URI}
[ ] management.endpoints.web.exposure.include = health,metrics
[ ] server.port = 8081

Both:
[ ] Remove hardcoded values
[ ] No passwords in code
[ ] Use environment variables for secrets
```

---

## 🧪 Testing Commands

```bash
# Compile
mvn clean compile -DskipTests

# Run tests
mvn test

# Check dependencies
mvn dependency:tree | grep -E "kafka|mongodb|log4j"

# Build
mvn clean package

# Start with Docker
docker-compose up -d

# Start applications
java -jar consumer/target/consumer-0.0.1-SNAPSHOT.jar
java -jar producer/target/producer-0.0.1-SNAPSHOT.jar

# Health check
curl http://localhost:8080/actuator/health

# View logs
docker logs kafka
docker logs mongodb
```

---

## 🔐 Security Checklist

```
[ ] Remove all credentials from source code
[ ] Rotate MongoDB passwords
[ ] Use environment variables for secrets
[ ] Add credentials to .gitignore
[ ] Remove commits with exposed credentials:
    git filter-branch --tree-filter 'git rm --cached application.yml' HEAD

[ ] Kafka security
    - Verify kafka-clients version 3.9.1+
    - No ConfigProvider use
    - Set: -Dorg.apache.kafka.automatic.config.providers=none

[ ] Application security
    - HTTPS for external APIs
    - TLS for MongoDB
    - Authentication tokens in .env, not code
```

---

## 📁 File Changes Summary

| File | Change | Priority |
|------|--------|----------|
| `consumer/pom.xml` | Upgrade spring-kafka, add actuator, add testcontainers | CRITICAL |
| `producer/pom.xml` | Upgrade spring-kafka, add actuator | CRITICAL |
| `producer/application.yml` | Move credentials to ENV, add health config | CRITICAL |
| `consumer/application.yml` | Add Kafka config, add health config | CRITICAL |
| `WikimediaConsumer.java` | Inject ObjectMapper, externalize topic name | HIGH |
| `WikiMediaStreamConsumer.java` | Add retry logic, externalize URLs | HIGH |
| `WikiMediaProducer.java` | Update sendMessage return type | MEDIUM |
| (NEW) `JacksonConfig.java` | Create ObjectMapper bean | MEDIUM |
| (NEW) `GracefulShutdownManager.java` | Proper shutdown handling | MEDIUM |
| (NEW) `WikimediaConsumerTests.java` | Unit tests | HIGH |
| (NEW) `docker-compose.yml` | Local dev environment | MEDIUM |

---

## 📞 Common Issues & Solutions

### Issue: Kafka messages going to wrong topic
**Solution:** Ensure all topic names are lowercase: "wikimedia-stream"

### Issue: MongoDB credentials in git history
**Solution:** 
```bash
git filter-branch --tree-filter 'git rm --cached application.yml' HEAD
git push --force-with-lease
```

### Issue: Health endpoint returning DOWN
**Solution:** Check logs, ensure Kafka/MongoDB are running, restart actuator

### Issue: Slow test execution
**Solution:** Add `@DynamicPropertySource` to use Testcontainers

### Issue: High memory usage
**Solution:** Update JVM args: `java -Xmx512m -jar app.jar`

---

## 🚀 Deployment Readiness

```
Pre-Deployment Checklist:
[ ] All tests passing
[ ] No SNAPSHOT dependencies in production
[ ] Environment variables configured
[ ] Health endpoints responding
[ ] Graceful shutdown tested
[ ] Docker images built and scanned
[ ] Kubernetes manifests prepared
[ ] Monitoring configured
[ ] Log aggregation ready
```

---

## 📊 Key Metrics to Monitor

```
After Deployment:
- Kafka consumer lag (target: <1000 messages behind)
- Message processing latency (target: <100ms)
- Error rate (target: <0.1%)
- MongoDB connection pool usage (target: <50%)
- Memory usage (target: <60% of allocated)
- API response time (target: <500ms)
```

---

## 🔗 Documentation References

- **Full Report:** See `CONSULTING_REPORT_2026_02_28.md`
- **Implementation Guide:** See `IMPLEMENTATION_GUIDE.md`
- **Architecture:** See `ARCHITECTURE.md` in consumer/docs/
- **API Documentation:** See `API_DOCUMENTATION.md` in consumer/docs/

---

## 👥 Support & Questions

**For questions about:**
- **Architecture changes:** See CONSULTING_REPORT_2026_02_28.md § Architecture Analysis
- **Security fixes:** See CONSULTING_REPORT_2026_02_28.md § Security Vulnerabilities
- **Implementation steps:** See IMPLEMENTATION_GUIDE.md
- **Testing:** See IMPLEMENTATION_GUIDE.md § Testing Implementation

---

**Status:** Review and Action Required  
**Target Completion:** 2 weeks  
**Estimated Effort:** 26-35 hours  
**Team:** Full development team

