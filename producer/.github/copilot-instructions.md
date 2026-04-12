# Overview
This project is a Spring Boot application that produces messages to a localhost bootstrap server Apache Kafka topic. It demonstrates how to set up a Kafka producer using Spring Boot and the Spring for Apache Kafka library.
It makes use of logging-library pom dependency to log messages to the console..

# Prerequisites
- Java 17 or higher
- Maven
- Docker and Docker Compose
- Apache Kafka (running locally via Docker)
- Spring Boot 3.0.6
- Spring for Apache Kafka 3.0.6


# Task
1. The baseline of this project has been set up. This project is able to produce messages to a Kafka topic named `"my-topic"`. 
2. The following task is to enhance the producer to store these messages to a MongoDB database.
3. You will need to create a MongoDB database and a collection to store the messages.
4. You will need to create a MongoDB repository to interact with the database. Feel free to use Spring Data MongoDB for this purpose. You can modify the pom.xml file to include only the necessary dependencies relating to MongoDB.
5. You will need to modify the Kafka producer to save the messages to the MongoDB database before they are sent to the Kafka topic.
6. You will need to test the application to ensure that messages are being produced to the Kafka topic and stored in the MongoDB database.
7. The application should be able to handle errors gracefully, such as when the Kafka broker or MongoDB database is not available. Use 'try-catch' blocks to handle exceptions and log appropriate error messages. Make sure you are using the logging-library pom dependency to log messages to the console. 'Mono.just(error)' can be used to handle errors in a reactive way and should be returned in case of an error.
8. Evaluate if you need to create a database connection pool to manage database connections efficiently using the factory pattern. If available, set the ttl (time to live) for the connections in the pool to ensure that idle connections are closed after a certain period of time. And if there is a setting to time a document can be stored in the database, set that as well. Ideally, set that to 1 minute for testing purposes.
9. Connection details to the MongoDB cluster is found on the [mongo_db_connection.md](mongo_db_connection.md)

# Success Criteria
- The application should be able to produce messages to the Kafka topic named `"my-topic"`.
- The application should be able to store the messages in a MongoDB database. Make use of the logging-library pom dependency to log messages to the console.
- The application should be able to handle errors gracefully, such as when the Kafka broker or MongoDB database is not available.
- The application should be well-documented, with clear instructions on how to set up and run the application.
- The code should be clean and follow best practices for Spring Boot and Kafka development. Make use of modular methods to enhance readability and maintainability of the code.