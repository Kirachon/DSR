# Philippine Dynamic Social Registry (DSR) - Registration Service
# Multi-stage Docker build for optimized production image

# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy parent POM and common modules first for better layer caching
COPY pom.xml .
COPY common/ common/
COPY services/registration-service/pom.xml services/registration-service/

# Download dependencies
RUN mvn dependency:go-offline -pl services/registration-service -am

# Copy source code
COPY services/registration-service/src/ services/registration-service/src/

# Build the application
RUN mvn clean package -pl services/registration-service -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Create non-root user for security
RUN addgroup -g 1001 -S dsr && \
    adduser -u 1001 -S dsr -G dsr

# Set working directory
WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Copy the built JAR from builder stage
COPY --from=builder /app/services/registration-service/target/registration-service-*.jar app.jar

# Create directories for logs and storage
RUN mkdir -p /app/logs /app/local-storage/documents && \
    chown -R dsr:dsr /app

# Switch to non-root user
USER dsr

# Expose the service port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=local"]
