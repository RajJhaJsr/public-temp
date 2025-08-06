# Spring Boot Archetype Template

This is a starter template project generated using the custom Maven archetype: `springboot-starter-template`.
Use this template as a starting point for building microservices.


## Quick Start

### Prerequisites
- Java 21
- Maven 3.6+
- Docker & Docker Compose
- PostgreSQL (or use Docker Compose)

### Running Locally with Docker Compose
```bash
# Start PostgreSQL and the application
docker-compose up -d

# View logs
docker-compose logs -f app

# Run the application
./mvnw spring-boot:run
```



### Testing the SmokeTest Endpoint


### GET:

http://localhost:8080/api/smoketest/active

### POST: 

curl -sSi -L -X POST http://localhost:8080/api/smoketest \
  -H  "Content-Type: application/json" \
  -H  "X-API-Version: v1" \
  -d  '{
    "name": "Test Smoke",
    "description": "Testing all components"
   }'



### I18n with Localization Support for Error Message
#### Through Header
curl -sSi -L -X POST http://localhost:8080/api/smoketest \
-H  "Content-Type: application/json" \
-H  "X-API-Version: v1" \
-H "Accept-Language: fr" \
-d  '{
"name": "Test Smoke",
"description": "Testing all components"
}'

#### Through Query Param
curl -sSi http://localhost:8080/api/smoketest/active?lang=de


### API Documentation
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

### Health Check
- Health: http://localhost:8080/actuator/health


## Create new projects


```bash
# Install the archetype locally
cd springboot-starter-template-archetype
mvn install

# Use the archetype to create new projects
mvn archetype:generate  -DarchetypeCatalog=local -DarchetypeGroupId=com.hsbc -DarchetypeArtifactId=springboot-starter-template-archetype -DarchetypeVersion=1.0 -DgroupId=com.hsbc.stm -DartifactId=stm-mvp-capture
```

# TODO

- Template for publisher, consumer, events, interceptors, configs (Solace Integration)
- docker-compose upgrade
- Json structured logger with masking
- Metrics Application+Business (Publisher,Consumers,Http) (Micrometer)
- Distributed Tracing - Message header (Opentelemetry)
- Integration tests with mock (http,broker)
- Library to move reusable components
- Health check to upgrade with  check all dependency (rest,broker)
- Circuit breaker, Timeout, Retry
- platform.version.catalog - type pom for DependencyManagement