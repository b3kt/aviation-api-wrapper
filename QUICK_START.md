# 🚀 Quick Start Guide

Quick guide to running the Aviation API in 5 minutes!

## Prerequisites Checklist

- [ ] Java 21 installed: `java -version`
- [ ] Maven installed: `mvn -version`
- [ ] Internet connection available

## Option 1: Quick Run (Fastest) ⚡

```bash
# Windows
build.bat
mvn spring-boot:run

# Linux/Mac
chmod +x build.sh
./build.sh
mvn spring-boot:run
```

**That's it!** API will be running at http://localhost:8080

## Option 2: Docker (Easiest) 🐳

```bash
# Single command
docker-compose up -d

# Access:
# - API: http://localhost:8080
# - Swagger: http://localhost:8080/swagger-ui.html
# - Prometheus: http://localhost:9090
# - Grafana: http://localhost:3000
```

## Quick Test

```bash
# Test KJFK (JFK Airport)
curl http://localhost:8080/api/v1/airports/KJFK

# Test health
curl http://localhost:8080/actuator/health
```

**Expected response:**
```json
{
  "icaoCode": "KJFK",
  "iataCode": "JFK",
  "name": "John F Kennedy International Airport",
  "city": "New York",
  "country": "United States",
  ...
}
```

## Try Other Airports

| Airport | ICAO Code | Location |
|---------|-----------|----------|
| JFK International | KJFK | New York, USA |
| Heathrow | EGLL | London, UK |
| Sydney | YSSY | Sydney, Australia |
| Changi | WSSS | Singapore |
| Soekarno-Hatta | WIII | Jakarta, Indonesia |

```bash
curl http://localhost:8080/api/v1/airports/WIII
curl http://localhost:8080/api/v1/airports/WSSS
```

## View Documentation

Open browser: http://localhost:8080/swagger-ui.html

## Troubleshooting

### Port 8080 already in use?

```bash
# Change port
mvn spring-boot:run -Dserver.port=9090
```

### Build fails?

```bash
# Clean and retry
mvn clean install -U
```

### API returns error?

Check logs for details:
```bash
# Docker
docker-compose logs -f aviation-api

# Local run
Check console output
```

## Next Steps

- Read full [README.md](README.md) for detailed documentation
- Explore [assignment.md](assignment.md) for requirements
- Check [walkthrough.md](walkthrough.md) for implementation details
- Run tests: `mvn test` or `./run-tests.sh`

---

**Need help?** Check the main README.md for comprehensive documentation.
