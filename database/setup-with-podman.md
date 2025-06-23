# DSR Database Setup with Podman

This guide explains how to set up PostgreSQL for the DSR system using Podman containers.

## Prerequisites

1. **Podman** installed and running
2. **Java 17+** for running the DSR services
3. **Maven** for building the services

## Step 1: Start PostgreSQL with Podman

### Option 1: Quick Start (Recommended for Development)

```bash
# Create a Podman volume for persistent data
podman volume create dsr-postgres-data

# Run PostgreSQL container
podman run -d \
  --name dsr-postgres \
  -e POSTGRES_DB=dsr_local \
  -e POSTGRES_USER=dsr_user \
  -e POSTGRES_PASSWORD=dsr_local_password \
  -e POSTGRES_INITDB_ARGS="--auth-host=scram-sha-256" \
  -v dsr-postgres-data:/var/lib/postgresql/data \
  -p 5432:5432 \
  postgres:15

# Wait for PostgreSQL to start
sleep 10

# Check if PostgreSQL is running
podman exec dsr-postgres pg_isready -U dsr_user -d dsr_local
```

### Option 2: Using Podman Compose (For Production-like Setup)

Create `database/podman-compose.yml`:

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    container_name: dsr-postgres
    environment:
      POSTGRES_DB: dsr_local
      POSTGRES_USER: dsr_user
      POSTGRES_PASSWORD: dsr_local_password
      POSTGRES_INITDB_ARGS: "--auth-host=scram-sha-256"
    volumes:
      - dsr-postgres-data:/var/lib/postgresql/data
      - ./init-scripts:/docker-entrypoint-initdb.d
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U dsr_user -d dsr_local"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  dsr-postgres-data:
```

Then run:
```bash
cd database
podman-compose up -d
```

## Step 2: Initialize Database Schema

### Option 1: Using Podman Exec

```bash
# Copy SQL files to container
podman cp migrations/V1__Initial_Schema.sql dsr-postgres:/tmp/
podman cp migrations/V2__Auth_Tables.sql dsr-postgres:/tmp/
podman cp migrations/V3__Core_Tables.sql dsr-postgres:/tmp/

# Execute SQL files
podman exec -i dsr-postgres psql -U dsr_user -d dsr_local -f /tmp/V1__Initial_Schema.sql
podman exec -i dsr-postgres psql -U dsr_user -d dsr_local -f /tmp/V2__Auth_Tables.sql
podman exec -i dsr-postgres psql -U dsr_user -d dsr_local -f /tmp/V3__Core_Tables.sql
```

### Option 2: Using Maven Flyway Plugin

```bash
# From project root
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/dsr_local -Dflyway.user=dsr_user -Dflyway.password=dsr_local_password
```

## Step 3: Verify Database Setup

```bash
# Connect to PostgreSQL
podman exec -it dsr-postgres psql -U dsr_user -d dsr_local

# Check schemas
\dn

# Check tables
\dt dsr_core.*
\dt dsr_auth.*

# Exit
\q
```

## Step 4: Configure DSR Services for Podman

Update `services/registration-service/src/main/resources/application-local.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dsr_local
    username: dsr_user
    password: dsr_local_password
    driver-class-name: org.postgresql.Driver
```

**Note**: Use `localhost:5432` when running services on host machine. If running services in containers, use the container name or network IP.

## Step 5: Build and Test Registration Service

```bash
# Build the service
cd services/registration-service
mvn clean package -DskipTests

# Start the service (without no-db profile)
java -jar target/registration-service-3.0.0.jar --spring.profiles.active=local

# Test the service
curl http://localhost:8081/api/v1/registrations/health
```

## Networking Considerations for Podman

### Host to Container Communication
- Use `localhost:5432` when services run on host
- Ensure port 5432 is mapped correctly

### Container to Container Communication
If running services in containers:

```bash
# Create a Podman network
podman network create dsr-network

# Run PostgreSQL with network
podman run -d \
  --name dsr-postgres \
  --network dsr-network \
  -e POSTGRES_DB=dsr_local \
  -e POSTGRES_USER=dsr_user \
  -e POSTGRES_PASSWORD=dsr_local_password \
  -v dsr-postgres-data:/var/lib/postgresql/data \
  -p 5432:5432 \
  postgres:15

# Update connection string to use container name
# jdbc:postgresql://dsr-postgres:5432/dsr_local
```

## Troubleshooting

### PostgreSQL Connection Issues

1. **Check if container is running:**
   ```bash
   podman ps | grep dsr-postgres
   ```

2. **Check container logs:**
   ```bash
   podman logs dsr-postgres
   ```

3. **Test connection:**
   ```bash
   podman exec dsr-postgres pg_isready -U dsr_user -d dsr_local
   ```

### Service Connection Issues

1. **Check application logs for database errors**
2. **Verify connection string in application-local.yml**
3. **Ensure PostgreSQL is accepting connections on port 5432**

### Port Conflicts

If port 5432 is already in use:

```bash
# Use a different port
podman run -d \
  --name dsr-postgres \
  -e POSTGRES_DB=dsr_local \
  -e POSTGRES_USER=dsr_user \
  -e POSTGRES_PASSWORD=dsr_local_password \
  -v dsr-postgres-data:/var/lib/postgresql/data \
  -p 5433:5432 \
  postgres:15

# Update connection string
# jdbc:postgresql://localhost:5433/dsr_local
```

## Cleanup

To stop and remove the PostgreSQL container:

```bash
# Stop container
podman stop dsr-postgres

# Remove container
podman rm dsr-postgres

# Remove volume (WARNING: This deletes all data)
podman volume rm dsr-postgres-data
```

## Next Steps

After successful database setup:

1. **Test all Registration Service endpoints**
2. **Verify data persistence**
3. **Check audit logging**
4. **Proceed to Security Framework Implementation**
