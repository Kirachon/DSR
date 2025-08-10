# Philippine Dynamic Social Registry (DSR) - Local Deployment Guide

## Overview

This guide provides comprehensive instructions for setting up and running the complete DSR system locally for development and testing purposes. The local environment mirrors the production setup but is optimized for development efficiency.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Quick Start](#quick-start)
3. [Detailed Setup](#detailed-setup)
4. [Service Architecture](#service-architecture)
5. [Testing](#testing)
6. [Troubleshooting](#troubleshooting)
7. [Development Workflow](#development-workflow)

## Prerequisites

### System Requirements

- **Operating System**: Windows 10/11, macOS 10.15+, or Linux
- **Memory**: 8GB RAM minimum, 16GB recommended
- **Storage**: 10GB free space
- **Network**: Internet connection for downloading dependencies

### Required Software

1. **Java 17+**
   ```bash
   # Verify installation
   java -version
   ```

2. **Node.js 18+**
   ```bash
   # Verify installation
   node --version
   npm --version
   ```

3. **Podman 4.8+**
   ```bash
   # Verify installation
   podman --version
   podman-compose --version
   ```

4. **Git**
   ```bash
   # Verify installation
   git --version
   ```

### Optional Tools

- **PostgreSQL Client Tools** (for database management)
- **Redis CLI** (for cache management)
- **curl** (for API testing)
- **jq** (for JSON formatting)

## Quick Start

### 1. Clone and Setup

```bash
# Clone the repository
git clone https://github.com/Kirachon/DSR.git
cd DSR

# Run the automated setup
./scripts/setup-local-env.sh
```

### 2. Start Services

```bash
# Start all DSR services
./scripts/run-local.sh
```

### 3. Verify Installation

```bash
# Run comprehensive tests
./scripts/test-local-deployment.sh

# Check service status
./scripts/run-local.sh status
```

### 4. Access Applications

- **Web Portal**: http://localhost:3000
- **Admin Portal**: http://localhost:3001
- **API Gateway**: http://localhost:8080
- **Grafana Dashboard**: http://localhost:3001 (admin/admin)
- **Prometheus**: http://localhost:9090
- **Jaeger Tracing**: http://localhost:16686

## Detailed Setup

### Step 1: Environment Configuration

1. **Copy Environment Template**
   ```bash
   cp .env.local .env
   ```

2. **Customize Configuration** (optional)
   Edit `.env` to modify ports, database settings, or feature flags.

### Step 2: Infrastructure Services

1. **Start Infrastructure**
   ```bash
   podman-compose up -d
   ```

2. **Verify Infrastructure**
   ```bash
   # Check PostgreSQL
   pg_isready -h localhost -p 5432 -U dsr_user

   # Check Redis
   redis-cli -h localhost -p 6379 ping

   # Check Elasticsearch
   curl http://localhost:9200/_cluster/health

   # Check Kafka
   nc -z localhost 9092
   ```

### Step 3: Database Initialization

```bash
# Initialize database with schema and sample data
PGPASSWORD=dsr_local_password psql -h localhost -p 5432 -U dsr_user -d dsr_local -f database/sample-data/01_init_local_db.sql
PGPASSWORD=dsr_local_password psql -h localhost -p 5432 -U dsr_user -d dsr_local -f database/sample-data/03_load_sample_data.sql
```

### Step 4: Build Services

```bash
# Build all microservices
./scripts/build-all.sh
```

### Step 5: Start DSR Services

```bash
# Start services with dependency management
./scripts/run-local.sh start
```

## Service Architecture

### Infrastructure Services

| Service | Port | Purpose | Admin UI |
|---------|------|---------|----------|
| PostgreSQL | 5432 | Primary database | pgAdmin: http://localhost:5050 |
| Redis | 6379 | Caching & sessions | Redis Commander: http://localhost:8082 |
| Elasticsearch | 9200 | Search & analytics | - |
| Kafka | 9092 | Event streaming | Kafka UI: http://localhost:8081 |
| Prometheus | 9090 | Metrics collection | http://localhost:9090 |
| Grafana | 3001 | Monitoring dashboards | http://localhost:3001 |
| Jaeger | 16686 | Distributed tracing | http://localhost:16686 |

### DSR Microservices

| Service | Port | Purpose | Health Check |
|---------|------|---------|--------------|
| Registration Service | 8080 | Citizen registration | http://localhost:8080/actuator/health |
| Data Management Service | 8081 | Data processing | http://localhost:8081/actuator/health |
| Eligibility Service | 8082 | Program eligibility | http://localhost:8082/actuator/health |
| Interoperability Service | 8083 | Agency integration | http://localhost:8083/actuator/health |
| Payment Service | 8084 | Payment processing | http://localhost:8084/actuator/health |
| Grievance Service | 8085 | Complaint handling | http://localhost:8085/actuator/health |
| Analytics Service | 8086 | Reporting & analytics | http://localhost:8086/actuator/health |

## Testing

### Automated Testing

```bash
# Run all tests
./scripts/test-local-deployment.sh

# Run specific test categories
./testing/curl-examples/test-dsr-apis.sh health
./testing/curl-examples/test-dsr-apis.sh registration
```

### Manual Testing

1. **Import Postman Collection**
   - Import `testing/api-tests/dsr-local-tests.postman_collection.json`
   - Set environment variables for local testing

2. **Test Workflows**
   - Citizen Registration
   - Eligibility Assessment
   - Payment Processing
   - Grievance Submission

### Sample Test Data

The system includes pre-loaded test data:
- **50 sample households** with members and addresses
- **Test user accounts** with different roles
- **Sample program eligibilities** and service deliveries
- **Mock API responses** for external integrations

**Test Credentials:**
- Admin: `admin@dsr.local` / `admin123`
- Caseworker: `caseworker@dsr.local` / `admin123`
- Citizen: `citizen@dsr.local` / `admin123`

## Development Workflow

### Hot Reload Development

1. **Enable Development Mode**
   ```bash
   export SPRING_DEVTOOLS_ENABLED=true
   export SPRING_DEVTOOLS_RESTART_ENABLED=true
   ```

2. **Start Individual Service**
   ```bash
   java -jar build/registration-service.jar --spring.profiles.active=local
   ```

### Debugging

1. **Enable Debug Mode**
   ```bash
   java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar build/registration-service.jar
   ```

2. **Connect IDE Debugger** to port 5005

### Log Management

```bash
# View all service logs
./scripts/run-local.sh logs

# View specific service log
tail -f logs/registration-service.log

# View infrastructure logs
podman-compose logs -f postgresql
```

### Database Management

```bash
# Connect to database
PGPASSWORD=dsr_local_password psql -h localhost -p 5432 -U dsr_user -d dsr_local

# Reset sample data
SELECT dsr_core.reset_local_data();
SELECT dsr_core.generate_comprehensive_sample_data(50, true, true);

# Validate environment
SELECT * FROM dsr_core.validate_local_environment();
```

### Performance Monitoring

- **Grafana Dashboards**: http://localhost:3001
- **Prometheus Metrics**: http://localhost:9090
- **Application Metrics**: http://localhost:8080/actuator/metrics
- **JVM Metrics**: http://localhost:8080/actuator/metrics/jvm.memory.used

## Next Steps

1. **Explore the API Documentation**
   - Visit http://localhost:8080/swagger-ui for interactive API docs

2. **Customize Configuration**
   - Modify `.env` file for your specific needs
   - Adjust service configurations in `services/*/src/main/resources/application-local.yml`

3. **Develop New Features**
   - Use the hot reload setup for rapid development
   - Follow the established patterns and conventions

4. **Run Integration Tests**
   - Execute the test suites regularly
   - Add new tests for your features

## Troubleshooting

### Common Issues

#### Port Conflicts
```bash
# Check what's using a port
lsof -i :8080

# Kill process using port
kill -9 $(lsof -t -i:8080)

# Use alternative ports
export REGISTRATION_SERVICE_PORT=8090
```

#### Database Connection Issues
```bash
# Check PostgreSQL status
podman ps | grep postgresql

# Restart PostgreSQL
podman-compose restart postgresql

# Check logs
podman-compose logs postgresql
```

#### Service Startup Failures
```bash
# Check service logs
tail -f logs/registration-service.log

# Verify dependencies
./scripts/test-local-deployment.sh infrastructure

# Restart with debug
java -Dlogging.level.root=DEBUG -jar build/registration-service.jar
```

#### Memory Issues
```bash
# Increase JVM heap size
export JAVA_OPTS="-Xmx2g -Xms1g"

# Monitor memory usage
./scripts/run-local.sh status
```

For detailed troubleshooting information, see the [Troubleshooting Guide](TROUBLESHOOTING.md).

For production deployment, see the [Production Deployment Guide](deployment/DEPLOYMENT_GUIDE.md).
