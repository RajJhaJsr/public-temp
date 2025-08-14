# Spring Boot Archetype Template

This is a starter template project generated using the custom Maven archetype: `springboot-starter-template`.
Use this template as a starting point for building microservices.


## Quick Start

### Prerequisites
- Java 21
- Maven 3.6+
- Docker & Docker Compose
- PostgreSQL (or use Docker Compose)



## Create new projects


```bash
# Install the archetype locally
cd springboot-starter-template-archetype
mvn install

# Use the archetype to create new projects
mvn archetype:generate  -DarchetypeCatalog=local -DarchetypeGroupId=com.hsbc -DarchetypeArtifactId=springboot-starter-template-archetype -DarchetypeVersion=1.0 -DgroupId=com.hsbc.stm.mvp -DartifactId=stm-fss-capture
```




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



## Features

- Rest API service through Spring WebFlux (Router & Handler-based)
- Generic Event Publisher, Consumer through Spring Cloud Stream (Message Broker: Solace Integration Configs)
- R2DBC with PostgreSQL (Reactive persistence)
- WebClient for reactive external API calls
- Interceptors for web server and client (Security,Auditing, Request-Response Decorator)
- Bean Validation (JSR 380) with custom validators and Annotations
- Internationalization vs. localization (i18n vs l10n) for error messages
- Global Exception Handling with standardized JSON error response
- DTOs with Jackson (custom serialization/deserialization)
- Entity <-> DTO mapping via MapStruct
- Utility classes & constants
- Custom Annotations for Instrumentation
- Aspects (AOP)
- Scheduled tasks
- Docker containerization app(Dockerfile)
- Docker Compose for easy setup (PostgreSQL, PGAdmin, Solace)



## TODO
- Transactional Publishing (PeeGeeQ outbox?)
- Event messaging error/failure handling, RetryQ,DLQ,DMQ
- Event Sourcing and Projection/View (PeeGeeQ Bi-Temporal Store?)
- Metrics Application + Business (Publisher,Consumers,Http) (Through Micrometer instrumentation for Alerts and Mounting Dashboard ex: Prometheus,Grafana)
- Distributed Tracing (Through OpenTelemetry instrumentation ex: Jeager,Elasticsearch )
- Json structured logger with masking including context and trace (For Centralized Logging System ex: EFK)
- Integration tests with mock (http,broker)
- Health check to be upgraded with check all downstream dependency (ex: Rest Services)
- Resiliency setup and Validation through Chaos Testing (Circuit breaker, Timeout, Retry)
- Library to move reusable components (pom module)
- platform.version.catalog - type pom for DependencyManagement