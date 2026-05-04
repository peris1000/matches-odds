# Matches Odds

A Spring Boot microservice for managing teams, matches, and matches odds.

This project demonstrates modern Java practices, including caching, rate limiting, and resilient database interactions.

## Core Capabilities

### Team Management
- Full CRUD operations for sports teams.
- Specialized "Full Team View" including all associated matches.
- Categorization by sport type.

### Match & Odds Management
- Comprehensive match scheduling and lifecycle management.
- Dynamic betting odds (Match Odds) synchronization.
- **Advanced Search**: Filter matches by date ranges, specific teams, and status using QueryDSL.
- **Pagination & Sorting**: Robust support for large datasets across all list endpoints.

### Performance & Scalability
- **Multi-Level Caching**:
    - Application-level caching using **Caffeine**.
    - Integrated Cache Administration API to monitor hit rates and evict entries manually.
- **Rate Limiting**: Sliding window rate limiting via **Redis** and Lua scripting to prevent API abuse.

### Security & Reliability
- **Role-Based Access Control (RBAC)**: Secured endpoints using Spring Security (Admin vs. User roles).
- **Resiliency**: Circuit Breaker pattern implementation with **Resilience4j**.
- **Data Integrity**: Database migrations managed by **Flyway**.
- **Observability**: Built-in health checks, metrics, and Prometheus integration via **Spring Boot Actuator**.

---

## Technology Stack

| Category | Technology |
| :--- | :--- |
| **Framework** | Spring Boot 3.x / 4.x |
| **Language** | Java 21 |
| **Database** | PostgreSQL |
| **Caching/NoSQL** | Redis, Caffeine |
| **Data Access** | Spring Data JPA, Hibernate, QueryDSL |
| **Security** | Spring Security (HTTP Basic/RBAC) |
| **Migration** | Flyway |
| **Resiliency** | Resilience4j |
| **Monitoring** | Prometheus, Micrometer, Spring Actuator |
| **Documentation** | SpringDoc OpenAPI (Swagger UI) |
| **Testing** | JUnit 5, Mockito, Testcontainers |
| **Build Tool** | Maven |

---

## Architecture Highlights

### Transaction Management
The application follows a strict transactional model using Spring's `@Transactional`. 
- **Read-Only Optimizations**: Applied to fetch operations to reduce Hibernate overhead.
- **Dirty Checking**: Utilized for seamless entity updates.

### Database Schema
Managed via Flyway migrations (`src/main/resources/db/migration`). 
- **V01_00_01__init_tables.sql**: Initializes Teams, Matches, and MatchOdds tables with appropriate constraints and indexes.

---

## Getting Started

### Prerequisites
- JDK 21
- Docker & Docker Compose
- Maven

### Running the Application
1. **Start Infrastructure**:
   ```bash
   docker-compose up -d
   ```
2. **Build and Run**:
   ```bash
   ./mvnw spring-boot:run
   ```

## GraalVM Native Support

This project has been configured to let you generate either a lightweight container or a native executable.
It is also possible to run your tests in a native image.

### Lightweight Container with Cloud Native Buildpacks

If you're already familiar with Spring Boot container images support, this is the easiest way to get started.
Docker should be installed and configured on your machine prior to creating the image.

To create the image, run the following goal:

```
$ ./mvnw spring-boot:build-image -Pnative
```

Then, you can run the app like any other container:

```
$ docker run --rm -p8082:8082 -p8083:8083 \
  -e DB_URL=jdbc:postgresql://192.168.1.24:25432/matchesodds \
  -e REDIS_HOST=192.168.1.24 \
  -e REDIS_PORT=6379 \
  matches-odds:1.0.0
```
where `192.168.1.24` stands for and can be replaced by your host/infrastructure ip.

### Executable with Native Build Tools

Use this option if you want to explore more options, such as running your tests in a native image.
The GraalVM `native-image` compiler should be installed and configured on your machine.

NOTE: GraalVM 25+ is required.

To create the executable, run the following goal:

```
$ ./mvnw native:compile -Pnative
```

Then, you can run the app as follows:

```
$ target/matches-odds
```

You can also run your existing tests suite in a native image.
This is an efficient way to validate the compatibility of your application.

To run your existing tests in a native image, run the following goal:

```
$ ./mvnw test -PnativeTest
```

### Testcontainers support

This project
uses [Testcontainers at development time](https://docs.spring.io/spring-boot/4.0.6/reference/features/dev-services.html#features.dev-services.testcontainers).

Testcontainers has been configured to use the following components:
- Postgres
- Redis

Please review the tags of the used images and set them to the same as you're running in production.

### Maven Parent overrides

Test coverage report through
```bash
./mvnw clean verify
```

### API Documentation
Once running, explore the API via Swagger UI:
`http://localhost:8082/swagger-ui/index.html`

In addition, a `postman` collection is available at `./postman` folder.
---

## Monitoring
- **Metrics**: `http://localhost:8083/actuator/prometheus`
- **Health**: `http://localhost:8083/actuator/health`
- **Cache Mgmt**: Admin-only endpoints under `/api/admin/cache/...`

The prometheus infrastructure instance is available at `http://localhost:9090`
and preloads a scrape config for the project at `./config/prometheus` folder.

## User Credentials

If security is enabled (default: true) through configuration property `app.security.enabled`
then endpoints require basic authentication credentials to serve results.
```
admin:admin -> for overseeing L1 app cache endpoints (/api/admin/cache)
john:john -> for business domain endpoints (teams, matches)
```
**Actuator** and consequently `prometheus` and/or `metrics` endpoints require no authentication listening at designated port.
