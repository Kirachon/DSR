# Payment Service Deployment Guide

## Overview

This guide provides comprehensive instructions for deploying the DSR Payment Service in various environments, from local development to production.

## Prerequisites

### System Requirements
- **Java**: OpenJDK 17 or higher
- **Memory**: Minimum 2GB RAM, Recommended 4GB+
- **Storage**: Minimum 10GB available space
- **Network**: Outbound HTTPS access for FSP integrations

### Dependencies
- **PostgreSQL**: Version 13 or higher
- **Redis**: Version 6 or higher (for caching)
- **Kafka**: Version 2.8 or higher (for event streaming)

### Container Runtime
- **Podman**: Version 4.0 or higher (recommended)
- **Docker**: Version 20.10 or higher (alternative)

## Environment Configuration

### Environment Variables

#### Database Configuration
```bash
# PostgreSQL Database
DATABASE_URL=jdbc:postgresql://localhost:5432/dsr_payment
DATABASE_USERNAME=payment_user
DATABASE_PASSWORD=secure_password
DATABASE_POOL_SIZE=20
DATABASE_TIMEOUT=30000

# Redis Cache
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redis_password
REDIS_DATABASE=0
```

#### Security Configuration
```bash
# JWT Configuration
JWT_SECRET=your_jwt_secret_key_minimum_32_characters
JWT_EXPIRATION=86400
JWT_REFRESH_EXPIRATION=604800

# Encryption
ENCRYPTION_KEY=your_encryption_key_exactly_32_chars
ENCRYPTION_ALGORITHM=AES-256-GCM

# OAuth2
OAUTH_CLIENT_ID=dsr_payment_client
OAUTH_CLIENT_SECRET=oauth_client_secret
OAUTH_REDIRECT_URI=https://your-domain.com/auth/callback
```

#### FSP Configuration
```bash
# FSP Service Settings
FSP_TIMEOUT=30000
FSP_RETRY_ATTEMPTS=3
FSP_RETRY_DELAY=5000
FSP_HEALTH_CHECK_INTERVAL=60000

# Batch Processing
BATCH_MAX_SIZE=1000
BATCH_PROCESSING_TIMEOUT=300000
BATCH_THREAD_POOL_SIZE=10
```

#### Monitoring Configuration
```bash
# Logging
LOG_LEVEL=INFO
LOG_FILE_PATH=/var/log/payment-service/application.log
LOG_MAX_FILE_SIZE=100MB
LOG_MAX_HISTORY=30

# Metrics
METRICS_ENABLED=true
PROMETHEUS_ENABLED=true
HEALTH_CHECK_ENABLED=true
```

## Local Development

### Using Maven
```bash
# Clone the repository
git clone https://github.com/your-org/dsr.git
cd dsr/services/payment-service

# Set environment variables
export DATABASE_URL=jdbc:h2:mem:testdb
export JWT_SECRET=local_development_secret_key

# Run the application
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Using Podman
```bash
# Build the image
podman build -t dsr-payment-service:latest .

# Run with local configuration
podman run -d \
  --name payment-service-local \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=local \
  -e DATABASE_URL=jdbc:h2:mem:testdb \
  -e JWT_SECRET=local_development_secret \
  dsr-payment-service:latest
```

## Staging Deployment

### Database Setup
```sql
-- Create database and user
CREATE DATABASE dsr_payment_staging;
CREATE USER payment_staging WITH PASSWORD 'staging_password';
GRANT ALL PRIVILEGES ON DATABASE dsr_payment_staging TO payment_staging;

-- Create required extensions
\c dsr_payment_staging;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";
```

### Podman Deployment
```bash
# Create network
podman network create dsr-staging

# Deploy PostgreSQL
podman run -d \
  --name postgres-staging \
  --network dsr-staging \
  -e POSTGRES_DB=dsr_payment_staging \
  -e POSTGRES_USER=payment_staging \
  -e POSTGRES_PASSWORD=staging_password \
  -v postgres-staging-data:/var/lib/postgresql/data \
  postgres:13

# Deploy Redis
podman run -d \
  --name redis-staging \
  --network dsr-staging \
  -e REDIS_PASSWORD=staging_redis_password \
  redis:6-alpine redis-server --requirepass staging_redis_password

# Deploy Payment Service
podman run -d \
  --name payment-service-staging \
  --network dsr-staging \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=staging \
  -e DATABASE_URL=jdbc:postgresql://postgres-staging:5432/dsr_payment_staging \
  -e DATABASE_USERNAME=payment_staging \
  -e DATABASE_PASSWORD=staging_password \
  -e REDIS_HOST=redis-staging \
  -e REDIS_PASSWORD=staging_redis_password \
  -e JWT_SECRET=staging_jwt_secret_key_32_chars \
  -e ENCRYPTION_KEY=staging_encryption_key_32_chars \
  dsr-payment-service:latest
```

## Production Deployment

### High Availability Setup

#### Database Cluster
```bash
# Primary PostgreSQL instance
podman run -d \
  --name postgres-primary \
  --network dsr-production \
  -e POSTGRES_DB=dsr_payment \
  -e POSTGRES_USER=payment_user \
  -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD} \
  -e POSTGRES_REPLICATION_USER=replicator \
  -e POSTGRES_REPLICATION_PASSWORD=${REPLICATION_PASSWORD} \
  -v postgres-primary-data:/var/lib/postgresql/data \
  -v ./postgresql.conf:/etc/postgresql/postgresql.conf \
  postgres:13

# Read replica
podman run -d \
  --name postgres-replica \
  --network dsr-production \
  -e PGUSER=replicator \
  -e PGPASSWORD=${REPLICATION_PASSWORD} \
  -e POSTGRES_MASTER_SERVICE=postgres-primary \
  -v postgres-replica-data:/var/lib/postgresql/data \
  postgres:13
```

#### Load Balancer Configuration
```nginx
upstream payment_service {
    server payment-service-1:8080 weight=1 max_fails=3 fail_timeout=30s;
    server payment-service-2:8080 weight=1 max_fails=3 fail_timeout=30s;
    server payment-service-3:8080 weight=1 max_fails=3 fail_timeout=30s;
}

server {
    listen 443 ssl http2;
    server_name api.dsr.gov.ph;
    
    ssl_certificate /etc/ssl/certs/dsr.crt;
    ssl_certificate_key /etc/ssl/private/dsr.key;
    
    location /api/v1/payments {
        proxy_pass http://payment_service;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeouts
        proxy_connect_timeout 30s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # Rate limiting
        limit_req zone=api burst=20 nodelay;
    }
}
```

#### Service Deployment
```bash
# Deploy multiple service instances
for i in {1..3}; do
  podman run -d \
    --name payment-service-${i} \
    --network dsr-production \
    -e SPRING_PROFILES_ACTIVE=production \
    -e DATABASE_URL=jdbc:postgresql://postgres-primary:5432/dsr_payment \
    -e DATABASE_USERNAME=payment_user \
    -e DATABASE_PASSWORD=${POSTGRES_PASSWORD} \
    -e REDIS_HOST=redis-cluster \
    -e REDIS_PASSWORD=${REDIS_PASSWORD} \
    -e JWT_SECRET=${JWT_SECRET} \
    -e ENCRYPTION_KEY=${ENCRYPTION_KEY} \
    -e SERVER_PORT=8080 \
    -e JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC" \
    -v /var/log/payment-service:/var/log/payment-service \
    dsr-payment-service:latest
done
```

## Kubernetes Deployment

### Namespace and ConfigMap
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: dsr-payment

---
apiVersion: v1
kind: ConfigMap
metadata:
  name: payment-service-config
  namespace: dsr-payment
data:
  application.yml: |
    spring:
      profiles:
        active: kubernetes
    server:
      port: 8080
    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus
```

### Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-service
  namespace: dsr-payment
spec:
  replicas: 3
  selector:
    matchLabels:
      app: payment-service
  template:
    metadata:
      labels:
        app: payment-service
    spec:
      containers:
      - name: payment-service
        image: dsr-payment-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: payment-service-secrets
              key: database-url
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: payment-service-secrets
              key: jwt-secret
        resources:
          requests:
            memory: "2Gi"
            cpu: "500m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```

### Service and Ingress
```yaml
apiVersion: v1
kind: Service
metadata:
  name: payment-service
  namespace: dsr-payment
spec:
  selector:
    app: payment-service
  ports:
  - port: 80
    targetPort: 8080
  type: ClusterIP

---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: payment-service-ingress
  namespace: dsr-payment
  annotations:
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/rate-limit: "100"
spec:
  tls:
  - hosts:
    - api.dsr.gov.ph
    secretName: dsr-tls-secret
  rules:
  - host: api.dsr.gov.ph
    http:
      paths:
      - path: /api/v1/payments
        pathType: Prefix
        backend:
          service:
            name: payment-service
            port:
              number: 80
```

## Monitoring and Logging

### Prometheus Configuration
```yaml
global:
  scrape_interval: 15s

scrape_configs:
- job_name: 'payment-service'
  static_configs:
  - targets: ['payment-service:8080']
  metrics_path: '/actuator/prometheus'
  scrape_interval: 30s
```

### Grafana Dashboard
Import the provided Grafana dashboard (`grafana-dashboard.json`) for comprehensive monitoring of:
- Request rates and response times
- Payment processing metrics
- FSP integration health
- Database performance
- JVM metrics

### Log Aggregation
```yaml
# Fluentd configuration for log collection
apiVersion: v1
kind: ConfigMap
metadata:
  name: fluentd-config
data:
  fluent.conf: |
    <source>
      @type tail
      path /var/log/payment-service/*.log
      pos_file /var/log/fluentd-payment-service.log.pos
      tag payment-service
      format json
    </source>
    
    <match payment-service>
      @type elasticsearch
      host elasticsearch.logging.svc.cluster.local
      port 9200
      index_name payment-service
    </match>
```

## Security Hardening

### Container Security
```dockerfile
# Use non-root user
FROM openjdk:17-jre-slim
RUN groupadd -r payment && useradd -r -g payment payment
USER payment

# Security configurations
ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"
EXPOSE 8080
```

### Network Security
- Enable TLS 1.3 for all external communications
- Use mTLS for service-to-service communication
- Implement network policies to restrict traffic
- Regular security scanning of container images

## Backup and Recovery

### Database Backup
```bash
# Daily backup script
#!/bin/bash
BACKUP_DIR="/backup/payment-service"
DATE=$(date +%Y%m%d_%H%M%S)

pg_dump -h postgres-primary -U payment_user dsr_payment | \
  gzip > ${BACKUP_DIR}/payment_db_${DATE}.sql.gz

# Retain backups for 30 days
find ${BACKUP_DIR} -name "*.sql.gz" -mtime +30 -delete
```

### Disaster Recovery
1. **RTO (Recovery Time Objective)**: 4 hours
2. **RPO (Recovery Point Objective)**: 1 hour
3. **Backup Strategy**: Daily full backups, hourly transaction log backups
4. **Failover**: Automated failover to secondary region

## Troubleshooting

### Common Issues

#### Service Won't Start
```bash
# Check logs
podman logs payment-service

# Check configuration
podman exec payment-service env | grep -E "(DATABASE|JWT|REDIS)"

# Verify connectivity
podman exec payment-service nc -zv postgres-primary 5432
```

#### Performance Issues
```bash
# Check JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Monitor database connections
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active

# Check FSP response times
curl http://localhost:8080/actuator/metrics/fsp.response.time
```

## Support

- **Deployment Issues**: deployment-support@dsr.gov.ph
- **Security Concerns**: security@dsr.gov.ph
- **Performance Issues**: performance@dsr.gov.ph
- **Emergency Support**: +63-xxx-xxx-xxxx (24/7)
