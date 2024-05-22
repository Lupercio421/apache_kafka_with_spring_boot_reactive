### Docker instructions
Before starting the Spring Boot application, you will need to run these commands in a terminal.

```bash
docker-compose up -d
```

#### Run this only once
```bash
docker pull apache/kafka:3.7.0
```

#### Run every time you need to start the application
```bash
docker run -p 9092:9092 apache/kafka:3.7.0
```
This was taken from the apache kafka quickstart section with [using docker image](https://kafka.apache.org/quickstart) of step 2. All information above is to configure and run apache kafka locally through their downloads.

You can go ahead and leave this terminal running. This makes me think that the explicit docker-compose.yml file I created is not being used. I will need to learn the details of this.