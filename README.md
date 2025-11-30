# Aviation API Wrapper

Microservice berbasis Spring Boot untuk mengambil informasi bandara menggunakan kode ICAO dari [AviationAPI.com](https://aviationapi.com).

> 🚀 **Quick Start**: Ingin langsung mulai? Lihat [QUICK_START.md](QUICK_START.md) untuk panduan 5 menit!

## 📋 Overview

Proyek ini adalah implementasi production-ready microservice yang menyediakan REST API untuk query informasi bandara berdasarkan kode ICAO 4 karakter. Dibangun dengan fokus pada **scalability**, **resilience**, dan **observability**.

### Key Features

- ✅ REST API endpoint untuk lookup airport by ICAO code
- ✅ Integrasi dengan public aviation API (aviationapi.com)
- ✅ Clean Architecture dengan 4 layer terpisah
- ✅ Command-Executor Pattern untuk extensibility
- ✅ Comprehensive resilience patterns (Circuit Breaker, Retry, Rate Limiter, Timeout)
- ✅ High-performance caching dengan Caffeine
- ✅ Full observability (Logging, Metrics, Health Checks, Tracing)
- ✅ Reactive non-blocking architecture dengan WebFlux
- ✅ OpenAPI/Swagger documentation
- ✅ Comprehensive test coverage

---

## 🚀 Prerequisites

- **Java 21** atau lebih tinggi
- **Maven 3.9+** untuk build management
- **Internet connectivity** untuk akses ke aviationapi.com

---

## 📦 Setup Instructions

### 1. Clone Repository

```bash
git clone <repository-url>
cd aviation-api-wrapper
```

### 2. Build Project

```bash
mvn clean install
```

Build akan:
- Compile semua source code
- Run semua unit & integration tests
- Package aplikasi menjadi executable JAR

**Expected output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX.XXX s
```

### 3. Run Application

**Option A: Using Maven**
```bash
mvn spring-boot:run
```

**Option B: Using Java JAR**
```bash
java -jar target/aviation-api-0.0.1-SNAPSHOT.jar
```

**Option C: Using Docker**
```bash
# Build image
docker build -t aviation-api:latest .

# Run container
docker run -p 8080:8080 --name aviation-api aviation-api:latest
```

**Option D: Using Docker Compose** (with monitoring)
```bash
# Start all services (API + Prometheus + Grafana)
docker-compose up -d

# View logs
docker-compose logs -f aviation-api

# Stop all services
docker-compose down
```

Services yang akan running:
- **Aviation API**: http://localhost:8080
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

Aplikasi akan start di **http://localhost:8080**

---

## 🧪 Running Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=AirportControllerIntegrationTest
```

### Test Coverage

Proyek ini memiliki 3 level testing:

1. **Unit Tests** - `GetAirportByIcaoCommandHandlerTest`
   - Test command handler secara isolated
   - Mock dependencies

2. **Integration Tests** - `AviationApiClientTest`
   - Test WebClient dengan MockWebServer
   - Verify resilience patterns

3. **End-to-End Tests** - `AirportControllerIntegrationTest`
   - Full Spring Boot context
   - Test complete request flow
   - Validate HTTP responses

---

## 🔌 API Endpoints

### Get Airport by ICAO Code

```http
GET /api/v1/airports/{icao}
```

**Path Parameters:**
- `icao` (required): 4-character ICAO code (e.g., KJFK, EGLL, YSSY)

**Example Request:**
```bash
curl http://localhost:8080/api/v1/airports/KJFK
```

**Success Response (200 OK):**
```json
{
  "icaoCode": "KJFK",
  "iataCode": "JFK",
  "name": "John F Kennedy International Airport",
  "city": "New York",
  "country": "United States",
  "coordinates": {
    "latitude": 40.6398,
    "longitude": -73.7789
  },
  "timezone": "America/New_York",
  "elevationFeet": 13
}
```

**Error Responses:**
- `400 Bad Request` - Invalid ICAO code format
- `404 Not Found` - Airport tidak ditemukan
- `429 Too Many Requests` - Rate limit exceeded
- `503 Service Unavailable` - Circuit breaker open (upstream API down)

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### Metrics (Prometheus)

```bash
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/prometheus
```

### API Documentation

Buka browser ke: **http://localhost:8080/swagger-ui.html**

---

## 🏗️ Architecture Decisions

### Clean Architecture

Proyek ini mengimplementasikan **Clean Architecture** dengan 4 layer yang terisolasi:

```
┌─────────────────────────────────────────────┐
│         Presentation Layer                  │
│  (Controllers, Exception Handlers, DTOs)    │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│         Application Layer                   │
│  (Commands, Handlers, Executor, Use Cases)  │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│            Domain Layer                     │
│  (Entities, Ports, Business Logic)          │
└─────────────────────────────────────────────┘
                 ▲
┌────────────────┴────────────────────────────┐
│        Infrastructure Layer                 │
│  (API Clients, Config, External Systems)    │
└─────────────────────────────────────────────┘
```

**Benefits:**
- **Testability**: Business logic dapat ditest tanpa dependencies eksternal
- **Maintainability**: Perubahan di satu layer tidak affect layer lain
- **Provider Independence**: Mudah swap aviation data provider
- **Clear Boundaries**: Setiap layer punya single responsibility

### Command-Executor Pattern

Implementasi menggunakan **Command-Executor Pattern** untuk request handling:

- **Command**: Immutable data object yang merepresentasikan intent
- **CommandHandler**: Mengeksekusi business logic untuk specific command
- **CommandExecutor**: Router yang mengirim command ke handler yang tepat

**Benefits:**
- **Extensibility**: Tambah fitur baru = buat command baru (Open/Closed Principle)
- **Single Responsibility**: Setiap handler fokus pada satu use case
- **Auditability**: Mudah tambah logging/metrics di sekitar command execution
- **Testability**: Commands dan handlers dapat ditest independently

### Technology Choices

| Technology | Purpose | Rationale |
|-----------|---------|-----------|
| **Spring WebFlux** | Reactive web framework | Non-blocking I/O untuk high throughput |
| **Resilience4j** | Resilience patterns | Modern library dengan Spring integration |
| **Caffeine** | Caching | High-performance in-memory cache |
| **Micrometer** | Metrics & Tracing | Industry standard observability |
| **Springdoc OpenAPI** | API Documentation | Auto-generate OpenAPI spec |
| **Lombok** | Boilerplate reduction | Clean, maintainable code |

---

## 🛡️ Error Handling & Resilience

### Resilience Patterns

Aplikasi ini mengimplementasikan multiple layers of resilience:

#### 1. **Circuit Breaker**
```yaml
Configuration:
- Sliding window: 10 calls
- Failure threshold: 50%
- Wait duration: 30 seconds
- Half-open calls: 3
```

**Purpose**: Mencegah cascading failures ketika upstream API down
- Open state: Langsung reject request tanpa call API
- Half-open state: Test jika API sudah recover
- Closed state: Normal operation

#### 2. **Retry Logic**
```yaml
Configuration:
- Max attempts: 3
- Backoff: Exponential (500ms → 1s → 2s)
- Retry on: 500, 503, 504 errors
- Don't retry: 400, 404 errors
```

**Purpose**: Handle transient failures (network glitches, temporary unavailability)

#### 3. **Rate Limiter**
```yaml
Configuration:
- Limit: 100 requests per minute
- Timeout: 5 seconds to acquire permission
```

**Purpose**: Protect upstream API dari overload, comply dengan rate limits

#### 4. **Timeout**
```yaml
Configuration:
- Request timeout: 3 seconds
- Overall timeout: 10 seconds
```

**Purpose**: Prevent indefinite waiting, free up resources quickly

#### 5. **Caching**
```yaml
Configuration:
- Max size: 1000 entries
- TTL: 60 minutes
- Eviction: LRU (Least Recently Used)
```

**Purpose**: Reduce load ke upstream API, improve response time

### Error Response Format

Semua errors dikembalikan dalam format konsisten:

```json
{
  "error": "Error Type",
  "message": "Detailed error message",
  "timestamp": "2025-11-30T10:30:00Z",
  "path": "/api/v1/airports/INVALID"
}
```

### Failure Scenarios Handled

| Scenario | Handling Strategy |
|----------|-------------------|
| Upstream API down | Circuit breaker opens → 503 response |
| Transient network error | Retry dengan exponential backoff |
| Invalid ICAO code | Immediate 400 response (no retry) |
| Airport not found | 404 response (no retry) |
| Timeout | Cancel request after 3s → retry or fail |
| Rate limit hit | Queue request atau 429 response |

---

## 📊 Observability

### Logging

- **Structured logging** dengan SLF4J
- **Trace context** included (traceId, spanId)
- **Log levels**: INFO (default), DEBUG (untuk troubleshooting)

**Sample log:**
```
2025-11-30 10:30:15.123 [http-nio-8080-exec-1] INFO [a1b2c3,d4e5f6] AirportController - Received request for airport with ICAO: KJFK
```

### Metrics

Exposed via Prometheus format di `/actuator/prometheus`:
- Request count & duration
- Circuit breaker state
- Cache hit/miss ratio
- JVM metrics (memory, threads, GC)

### Health Checks

```bash
GET /actuator/health
```

Returns:
- Application status (UP/DOWN)
- Disk space
- Circuit breaker status

### Distributed Tracing

- **W3C Trace Context** propagation
- **Brave** tracer implementation
- Ready untuk export ke Zipkin/Jaeger

---

## 🔧 Configuration

Key configuration properties di `application.yml`:

```yaml
# Server
server.port: 8080

# Aviation API
aviation.api.base-url: https://api.aviationapi.com
aviation.api.timeout-seconds: 3
aviation.api.max-retries: 3

# Cache
spring.cache.caffeine.spec: maximumSize=1000,expireAfterWrite=60m

# Resilience4j
resilience4j.circuitbreaker.instances.aviationApi:
  failureRateThreshold: 50
  waitDurationInOpenState: 30s
```

Untuk custom configuration, override via:
- Environment variables
- External `application.yml`
- Command line arguments: `--server.port=9090`

---

## 🤖 AI-Generated Code Disclosure

Berikut adalah bagian yang dibantu dengan AI tools dan telah direview/divalidasi:

### Fully AI-Generated (dengan review):
- Boilerplate configuration classes (`WebClientConfiguration`, `CacheConfiguration`)
- OpenAPI configuration setup
- Test scaffolding dan mock data setup
- Initial project structure dengan Maven dependencies

### Human-Designed with AI Assistance:
- Architecture decisions (Clean Architecture + Command Pattern)
- Resilience patterns implementation
- Business logic di command handlers
- Error handling strategies
- Domain model design

### Fully Human-Written:
- Core business logic
- Command validation logic
- Integration dengan aviation API
- Test scenarios dan assertions
- Documentation dan README

**Catatan**: Semua code telah dipahami, ditest, dan divalidasi untuk production readiness.

---

## 📁 Project Structure

```
aviation-api-wrapper/
├── src/
│   ├── main/
│   │   ├── java/com/github/b3kt/aviation/
│   │   │   ├── domain/              # Core business logic
│   │   │   │   ├── model/           # Domain entities
│   │   │   │   ├── port/            # Port interfaces
│   │   │   │   └── exception/       # Domain exceptions
│   │   │   ├── application/         # Use cases
│   │   │   │   ├── command/         # Commands & handlers
│   │   │   │   ├── executor/        # Command executor
│   │   │   │   └── dto/             # Application DTOs
│   │   │   ├── infrastructure/      # External integrations
│   │   │   │   ├── client/          # API clients
│   │   │   │   └── config/          # Spring configurations
│   │   │   └── presentation/        # API layer
│   │   │       ├── controller/      # REST controllers
│   │   │       └── exception/       # Exception handlers
│   │   └── resources/
│   │       └── application.yml      # Configuration
│   └── test/                         # Test classes
├── pom.xml                           # Maven dependencies
├── README.md                         # This file
└── assignment.md                     # Original requirements
```

---

## 🎯 Assignment Requirements Coverage

### ✅ Core Requirements
- [x] Accept HTTP requests untuk fetch airport by ICAO
- [x] Query aviation API (https://aviationapi.com)
- [x] Clean response format dengan key airport info
- [x] Handle upstream failures gracefully

### ✅ Scalability
- [x] Clean service layering (4 layers)
- [x] Stateless design
- [x] Ready for horizontal scaling
- [x] Efficient caching

### ✅ Resilience
- [x] Retry logic dengan exponential backoff
- [x] Circuit breaker implementation
- [x] Fallback strategies
- [x] Timeout handling
- [x] Rate limiting

### ✅ Extensibility
- [x] Not tightly coupled to provider
- [x] Port/Adapter pattern
- [x] Command pattern untuk new features

### ✅ Observability
- [x] Structured logging
- [x] Error transparency
- [x] Metrics readiness (Prometheus)
- [x] Health checks

### ✅ Deliverables
- [x] Executable Maven project
- [x] Complete README with instructions
- [x] Integration tests
- [x] Architecture documentation
- [x] Error handling notes
- [x] AI disclosure

---

## 🧑‍💻 Development

### Running in Development Mode

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Enable Debug Logging

```bash
mvn spring-boot:run -Dlogging.level.com.github.b3kt.aviation=DEBUG
```

### Running Tests with Coverage

```bash
mvn clean test jacoco:report
# Report: target/site/jacoco/index.html
```

---

## 📞 Support & Contact

Untuk pertanyaan atau issues:
- Check [assignment.md](assignment.md) untuk requirements
- Review [walkthrough.md](walkthrough.md) untuk implementation details
- Lihat logs di console untuk troubleshooting

---

## 📚 References

- [Spring Cloud](https://spring.io/projects/spring-cloud)
- [AviationAPI Docs](https://docs.aviationapi.com/)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [GitHub Repository](https://github.com/b3kt/aviation-api)

---

Notes:
- assignment.md is a scope for ai assisted development
- walkthrough.md is an initial boilerplate implementation plan generated using Claude Sonnet 4.5 

**Built with ❤️ using Clean Architecture principles**
