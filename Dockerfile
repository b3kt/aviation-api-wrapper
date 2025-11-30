# Multi-stage build untuk optimasi ukuran image
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml dan download dependencies (untuk caching layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code dan build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user untuk security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy JAR dari builder stage
COPY --from=builder /app/target/aviation-api-*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM options untuk production
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Run aplikasi
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

