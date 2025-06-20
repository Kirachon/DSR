# Philippine Dynamic Social Registry (DSR) - Deployment Guide

## Overview

This guide provides comprehensive instructions for deploying the Philippine Dynamic Social Registry (DSR) system in production environments. The DSR is built using modern cloud-native technologies with Podman containerization and Kubernetes orchestration.

## Prerequisites

### Infrastructure Requirements

**Kubernetes Cluster**
- Kubernetes 1.30+ with RBAC enabled
- Minimum 3 worker nodes (4 vCPU, 16GB RAM each)
- Storage class supporting ReadWriteOnce volumes
- Load balancer support (MetalLB, cloud provider LB)

**Container Registry**
- Private container registry (Harbor, GitLab Registry, or cloud provider)
- Registry credentials configured on all nodes

**External Dependencies**
- PostgreSQL 16+ cluster (primary + replica)
- Redis 7.2+ cluster
- Elasticsearch 8.11+ cluster
- Apache Kafka 3.6+ cluster
- PhilSys API access credentials

### Software Requirements

**Local Development**
- Podman 4.8+
- Kubectl 1.30+
- Helm 3.13+
- Java 17+
- Node.js 18+

**CI/CD Environment**
- GitLab CI/CD or equivalent
- Container scanning tools (Trivy)
- Security scanning tools (SonarQube)

## Quick Start

### 1. Clone Repository

```bash
git clone https://github.com/Kirachon/DSR.git
cd DSR
```

### 2. Configure Environment

```bash
# Copy environment template
cp .env.example .env

# Edit configuration
vim .env
```

### 3. Build and Deploy

```bash
# Build all components
./scripts/build-all.sh

# Deploy to Kubernetes
./scripts/deploy.sh --environment production
```

## Detailed Deployment Steps

### Step 1: Infrastructure Setup

#### 1.1 Create Namespaces

```bash
kubectl create namespace dsr-production
kubectl create namespace dsr-infrastructure
kubectl create namespace dsr-monitoring
kubectl create namespace istio-system
```

#### 1.2 Install Istio Service Mesh

```bash
# Download and install Istio
curl -L https://istio.io/downloadIstio | sh -
cd istio-*
export PATH=$PWD/bin:$PATH

# Install Istio
istioctl install --set values.defaultRevision=default

# Enable sidecar injection
kubectl label namespace dsr-production istio-injection=enabled
```

#### 1.3 Deploy Infrastructure Components

```bash
# Deploy PostgreSQL
helm install postgresql infrastructure/helm/postgresql \
  --namespace dsr-infrastructure \
  --values infrastructure/helm/postgresql/values-production.yaml

# Deploy Redis
helm install redis infrastructure/helm/redis \
  --namespace dsr-infrastructure \
  --values infrastructure/helm/redis/values-production.yaml

# Deploy Elasticsearch
helm install elasticsearch infrastructure/helm/elasticsearch \
  --namespace dsr-infrastructure \
  --values infrastructure/helm/elasticsearch/values-production.yaml

# Deploy Kafka
helm install kafka infrastructure/helm/kafka \
  --namespace dsr-infrastructure \
  --values infrastructure/helm/kafka/values-production.yaml
```

### Step 2: Security Configuration

#### 2.1 Create Service Accounts

```bash
kubectl apply -f security/rbac/service-accounts.yaml
kubectl apply -f security/rbac/cluster-roles.yaml
kubectl apply -f security/rbac/role-bindings.yaml
```

#### 2.2 Configure Network Policies

```bash
kubectl apply -f security/network-policies/
```

#### 2.3 Set up Secrets

```bash
# Database credentials
kubectl create secret generic dsr-database-secret \
  --from-literal=url="jdbc:postgresql://postgresql.dsr-infrastructure:5432/dsr_production" \
  --from-literal=username="dsr_user" \
  --from-literal=password="CHANGE_ME_IN_PRODUCTION" \
  --namespace dsr-production

# Redis credentials
kubectl create secret generic dsr-redis-secret \
  --from-literal=password="CHANGE_ME_IN_PRODUCTION" \
  --namespace dsr-production

# PhilSys API credentials
kubectl create secret generic dsr-philsys-secret \
  --from-literal=api-key="CHANGE_ME_IN_PRODUCTION" \
  --from-literal=api-url="https://api.philsys.gov.ph/v1" \
  --namespace dsr-production

# Container registry credentials
kubectl create secret docker-registry govcloud-registry-secret \
  --docker-server=registry.govcloud.ph \
  --docker-username=dsr-service \
  --docker-password=CHANGE_ME_IN_PRODUCTION \
  --namespace dsr-production
```

### Step 3: Database Setup

#### 3.1 Initialize Database Schema

```bash
# Connect to PostgreSQL
kubectl exec -it postgresql-0 -n dsr-infrastructure -- psql -U postgres

# Create database and user
CREATE DATABASE dsr_production;
CREATE USER dsr_user WITH PASSWORD 'CHANGE_ME_IN_PRODUCTION';
GRANT ALL PRIVILEGES ON DATABASE dsr_production TO dsr_user;

# Run schema migrations
kubectl apply -f database/migrations/
```

#### 3.2 Load Reference Data

```bash
# Load administrative divisions
kubectl exec -it postgresql-0 -n dsr-infrastructure -- \
  psql -U dsr_user -d dsr_production -f /data/reference-data.sql
```

### Step 4: Application Deployment

#### 4.1 Deploy Core Services

```bash
# Deploy Registration Service
kubectl apply -f infrastructure/k8s/services/registration-service.yaml

# Deploy Data Management Service
kubectl apply -f infrastructure/k8s/services/data-management-service.yaml

# Deploy Eligibility Service
kubectl apply -f infrastructure/k8s/services/eligibility-service.yaml

# Deploy Interoperability Service
kubectl apply -f infrastructure/k8s/services/interoperability-service.yaml

# Deploy Payment Service
kubectl apply -f infrastructure/k8s/services/payment-service.yaml

# Deploy Grievance Service
kubectl apply -f infrastructure/k8s/services/grievance-service.yaml

# Deploy Analytics Service
kubectl apply -f infrastructure/k8s/services/analytics-service.yaml
```

#### 4.2 Deploy API Gateway

```bash
# Deploy Kong API Gateway
helm install kong kong/kong \
  --namespace dsr-production \
  --values infrastructure/helm/kong/values-production.yaml
```

#### 4.3 Deploy Frontend Applications

```bash
# Deploy Web Portal
kubectl apply -f infrastructure/k8s/frontend/web-portal.yaml

# Deploy Admin Portal
kubectl apply -f infrastructure/k8s/frontend/admin-portal.yaml
```

### Step 5: Monitoring Setup

#### 5.1 Deploy Prometheus

```bash
kubectl apply -f monitoring/prometheus/dsr-monitoring.yaml
```

#### 5.2 Deploy Grafana

```bash
helm install grafana grafana/grafana \
  --namespace dsr-monitoring \
  --values monitoring/grafana/values-production.yaml
```

#### 5.3 Deploy Jaeger

```bash
kubectl apply -f monitoring/jaeger/jaeger-production.yaml
```

### Step 6: Verification

#### 6.1 Check Pod Status

```bash
# Check all pods are running
kubectl get pods -n dsr-production
kubectl get pods -n dsr-infrastructure
kubectl get pods -n dsr-monitoring

# Check service endpoints
kubectl get svc -n dsr-production
```

#### 6.2 Health Checks

```bash
# Check service health
kubectl exec -it deployment/dsr-registration-service -n dsr-production -- \
  curl http://localhost:8080/actuator/health

# Check database connectivity
kubectl exec -it deployment/dsr-registration-service -n dsr-production -- \
  curl http://localhost:8080/actuator/health/db
```

#### 6.3 API Testing

```bash
# Test API Gateway
curl -k https://api.dsr.gov.ph/health

# Test registration endpoint
curl -k -X POST https://api.dsr.gov.ph/api/v3/registrations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d @test-data/sample-registration.json
```

## Configuration Management

### Environment Variables

Key environment variables for each service:

```yaml
# Registration Service
DATABASE_URL: "jdbc:postgresql://postgresql.dsr-infrastructure:5432/dsr_production"
DATABASE_USERNAME: "dsr_user"
DATABASE_PASSWORD: "CHANGE_ME_IN_PRODUCTION"
REDIS_HOST: "redis.dsr-infrastructure"
REDIS_PORT: "6379"
REDIS_PASSWORD: "CHANGE_ME_IN_PRODUCTION"
KAFKA_BOOTSTRAP_SERVERS: "kafka.dsr-infrastructure:9092"
PHILSYS_API_URL: "https://api.philsys.gov.ph/v1"
PHILSYS_API_KEY: "CHANGE_ME_IN_PRODUCTION"
```

### ConfigMaps

Application configuration is managed through Kubernetes ConfigMaps:

```bash
# Update configuration
kubectl edit configmap dsr-registration-config -n dsr-production

# Restart pods to pick up changes
kubectl rollout restart deployment/dsr-registration-service -n dsr-production
```

## Scaling and Performance

### Horizontal Pod Autoscaling

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: dsr-registration-service-hpa
  namespace: dsr-production
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: dsr-registration-service
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

### Database Scaling

```bash
# Scale PostgreSQL replicas
kubectl scale statefulset postgresql --replicas=3 -n dsr-infrastructure

# Scale Redis cluster
kubectl scale statefulset redis --replicas=6 -n dsr-infrastructure
```

## Backup and Recovery

### Database Backup

```bash
# Create database backup
kubectl exec postgresql-0 -n dsr-infrastructure -- \
  pg_dump -U dsr_user dsr_production > backup-$(date +%Y%m%d).sql

# Restore from backup
kubectl exec -i postgresql-0 -n dsr-infrastructure -- \
  psql -U dsr_user dsr_production < backup-20241220.sql
```

### Configuration Backup

```bash
# Backup all configurations
kubectl get all,configmap,secret -n dsr-production -o yaml > dsr-backup.yaml
```

## Troubleshooting

### Common Issues

**Pod Startup Issues**
```bash
# Check pod logs
kubectl logs -f deployment/dsr-registration-service -n dsr-production

# Check events
kubectl get events -n dsr-production --sort-by='.lastTimestamp'
```

**Database Connection Issues**
```bash
# Test database connectivity
kubectl exec -it postgresql-0 -n dsr-infrastructure -- \
  psql -U dsr_user -d dsr_production -c "SELECT 1;"
```

**Service Discovery Issues**
```bash
# Check service endpoints
kubectl get endpoints -n dsr-production

# Check DNS resolution
kubectl exec -it deployment/dsr-registration-service -n dsr-production -- \
  nslookup postgresql.dsr-infrastructure.svc.cluster.local
```

### Performance Issues

**High Memory Usage**
```bash
# Check memory usage
kubectl top pods -n dsr-production

# Increase memory limits
kubectl patch deployment dsr-registration-service -n dsr-production \
  -p '{"spec":{"template":{"spec":{"containers":[{"name":"registration-service","resources":{"limits":{"memory":"2Gi"}}}]}}}}'
```

**High CPU Usage**
```bash
# Check CPU usage
kubectl top pods -n dsr-production

# Scale horizontally
kubectl scale deployment dsr-registration-service --replicas=5 -n dsr-production
```

## Security Considerations

### Network Security
- All inter-service communication uses mTLS via Istio
- Network policies restrict traffic between namespaces
- External access only through API Gateway

### Data Security
- All data encrypted at rest and in transit
- Database credentials stored in Kubernetes secrets
- Regular security scanning of container images

### Access Control
- RBAC configured for all service accounts
- Pod security policies enforce security standards
- Audit logging enabled for all API access

## Maintenance

### Regular Tasks

**Weekly**
- Review monitoring dashboards
- Check for security updates
- Verify backup integrity

**Monthly**
- Update container images
- Review resource usage
- Conduct security scans

**Quarterly**
- Update Kubernetes cluster
- Review and update documentation
- Conduct disaster recovery tests

### Update Procedures

```bash
# Update application
./scripts/deploy.sh --environment production --version 3.1.0

# Update infrastructure
helm upgrade postgresql infrastructure/helm/postgresql \
  --namespace dsr-infrastructure \
  --values infrastructure/helm/postgresql/values-production.yaml
```

## Support

For technical support and issues:

- **Technical Support**: api-support@dsr.gov.ph
- **Documentation**: docs@dsr.gov.ph
- **Emergency**: emergency@dsr.gov.ph
- **Security Issues**: security@dsr.gov.ph

---

**Note**: This deployment guide assumes a production environment. Adjust configurations and security settings according to your specific requirements and security policies.
