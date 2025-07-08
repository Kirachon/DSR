# DSR Production Deployment Guide
**Version:** 3.0.0  
**Last Updated:** June 28, 2025  
**Status:** ✅ PRODUCTION READY - Complete deployment guide  
**Phase:** 2.4.2 Implementation - COMPLETED  

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Infrastructure Setup](#infrastructure-setup)
4. [Database Deployment](#database-deployment)
5. [Application Deployment](#application-deployment)
6. [Configuration Management](#configuration-management)
7. [Security Configuration](#security-configuration)
8. [Monitoring Setup](#monitoring-setup)
9. [Load Balancing Configuration](#load-balancing-configuration)
10. [Backup and Disaster Recovery](#backup-and-disaster-recovery)
11. [Post-Deployment Verification](#post-deployment-verification)
12. [Troubleshooting](#troubleshooting)

## Overview

This guide provides comprehensive instructions for deploying the DSR (Dynamic Social Registry) system to production environments. The deployment follows a microservices architecture using Kubernetes orchestration with high availability and scalability requirements.

### Architecture Overview
- **7 Microservices:** Registration, Data Management, Eligibility, Payment, Interoperability, Grievance, Analytics
- **Frontend Application:** Next.js 14+ with TypeScript
- **Database:** PostgreSQL 15 with read replicas
- **Cache:** Redis Cluster
- **Container Orchestration:** Kubernetes 1.28+
- **Load Balancer:** NGINX Ingress Controller with Istio Service Mesh
- **Monitoring:** Prometheus, Grafana, AlertManager
- **Logging:** ELK Stack (Elasticsearch, Logstash, Kibana)

## Prerequisites

### System Requirements
- **Kubernetes Cluster:** v1.28+ with minimum 3 nodes
- **Node Specifications:** 8 CPU cores, 32GB RAM, 500GB SSD per node
- **Network:** High-speed internet connection with static IP addresses
- **SSL Certificates:** Valid SSL certificates for production domains
- **DNS:** Configured DNS records for all service endpoints

### Required Tools
```bash
# Install required tools
curl -LO "https://dl.k8s.io/release/v1.28.0/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# Install Helm
curl https://get.helm.sh/helm-v3.12.0-linux-amd64.tar.gz | tar xz
sudo mv linux-amd64/helm /usr/local/bin/

# Install AWS CLI
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

### Access Requirements
- **Kubernetes Cluster Access:** Admin-level kubeconfig
- **Container Registry Access:** Push/pull permissions to container registry
- **Cloud Provider Access:** AWS/Azure/GCP credentials with appropriate permissions
- **DNS Management:** Access to DNS provider for record management

## Infrastructure Setup

### 1. Kubernetes Cluster Preparation
```bash
# Verify cluster access
kubectl cluster-info
kubectl get nodes

# Create namespaces
kubectl create namespace dsr-production
kubectl create namespace dsr-infrastructure
kubectl create namespace monitoring

# Label nodes for specific workloads
kubectl label nodes node-1 workload=database
kubectl label nodes node-2 workload=application
kubectl label nodes node-3 workload=monitoring
```

### 2. Storage Configuration
```bash
# Create storage classes
kubectl apply -f - <<EOF
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: dsr-ssd-storage
provisioner: kubernetes.io/aws-ebs
parameters:
  type: gp3
  iops: "3000"
  throughput: "125"
reclaimPolicy: Retain
allowVolumeExpansion: true
EOF

# Create persistent volumes
kubectl apply -f infrastructure/k8s/storage/
```

### 3. Network Configuration
```bash
# Install NGINX Ingress Controller
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update

helm install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx \
  --create-namespace \
  --set controller.service.type=LoadBalancer \
  --set controller.metrics.enabled=true

# Install Istio Service Mesh
curl -L https://istio.io/downloadIstio | sh -
export PATH=$PWD/istio-1.18.0/bin:$PATH
istioctl install --set values.defaultRevision=default
kubectl label namespace dsr-production istio-injection=enabled
```

## Database Deployment

### 1. PostgreSQL Primary Database
```bash
# Create PostgreSQL secrets
kubectl create secret generic postgres-credentials \
  --from-literal=username=postgres \
  --from-literal=password=$(openssl rand -base64 32) \
  --namespace dsr-infrastructure

# Deploy PostgreSQL primary
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install dsr-postgres-primary bitnami/postgresql \
  --namespace dsr-infrastructure \
  --set auth.existingSecret=postgres-credentials \
  --set primary.persistence.size=500Gi \
  --set primary.persistence.storageClass=dsr-ssd-storage \
  --set metrics.enabled=true \
  --set metrics.serviceMonitor.enabled=true
```

### 2. PostgreSQL Read Replicas
```bash
# Deploy read replicas
helm install dsr-postgres-replica bitnami/postgresql \
  --namespace dsr-infrastructure \
  --set auth.existingSecret=postgres-credentials \
  --set architecture=replication \
  --set readReplicas.replicaCount=2 \
  --set readReplicas.persistence.size=500Gi \
  --set readReplicas.persistence.storageClass=dsr-ssd-storage
```

### 3. Database Initialization
```bash
# Create databases and users
kubectl exec -it dsr-postgres-primary-0 -n dsr-infrastructure -- psql -U postgres -c "
CREATE DATABASE dsr_production;
CREATE USER dsr_app WITH PASSWORD '$(kubectl get secret postgres-credentials -n dsr-infrastructure -o jsonpath='{.data.password}' | base64 -d)';
GRANT ALL PRIVILEGES ON DATABASE dsr_production TO dsr_app;
"

# Run database migrations
kubectl apply -f database/migrations/
```

### 4. Redis Cache Deployment
```bash
# Deploy Redis cluster
helm install dsr-redis bitnami/redis-cluster \
  --namespace dsr-infrastructure \
  --set cluster.nodes=6 \
  --set cluster.replicas=1 \
  --set persistence.size=100Gi \
  --set persistence.storageClass=dsr-ssd-storage \
  --set metrics.enabled=true
```

## Application Deployment

### 1. Container Images
```bash
# Build and push container images
docker build -t ghcr.io/dsr/registration-service:v3.0.0 services/registration-service/
docker push ghcr.io/dsr/registration-service:v3.0.0

# Repeat for all services
for service in data-management eligibility interoperability payment grievance analytics; do
  docker build -t ghcr.io/dsr/${service}-service:v3.0.0 services/${service}-service/
  docker push ghcr.io/dsr/${service}-service:v3.0.0
done

# Build and push frontend
docker build -t ghcr.io/dsr/frontend:v3.0.0 frontend/
docker push ghcr.io/dsr/frontend:v3.0.0
```

### 2. Configuration Secrets
```bash
# Create application secrets
kubectl create secret generic dsr-app-secrets \
  --from-literal=jwt-secret=$(openssl rand -base64 64) \
  --from-literal=encryption-key=$(openssl rand -base64 32) \
  --from-literal=philsys-api-key=${PHILSYS_API_KEY} \
  --from-literal=smtp-password=${SMTP_PASSWORD} \
  --namespace dsr-production

# Create database connection secrets
kubectl create secret generic dsr-db-secrets \
  --from-literal=host=dsr-postgres-primary.dsr-infrastructure.svc.cluster.local \
  --from-literal=port=5432 \
  --from-literal=database=dsr_production \
  --from-literal=username=dsr_app \
  --from-literal=password=${DB_PASSWORD} \
  --namespace dsr-production
```

### 3. Deploy Microservices
```bash
# Deploy all microservices
kubectl apply -f infrastructure/k8s/production/

# Verify deployments
kubectl get deployments -n dsr-production
kubectl get pods -n dsr-production
kubectl get services -n dsr-production
```

### 4. Deploy Frontend Application
```bash
# Deploy frontend
kubectl apply -f infrastructure/k8s/frontend/

# Configure ingress
kubectl apply -f infrastructure/k8s/ingress/
```

## Configuration Management

### 1. Environment-Specific Configuration
```yaml
# production-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: dsr-production-config
  namespace: dsr-production
data:
  # Database configuration
  DB_POOL_SIZE: "20"
  DB_CONNECTION_TIMEOUT: "30000"
  
  # Cache configuration
  REDIS_CLUSTER_NODES: "dsr-redis-cluster.dsr-infrastructure.svc.cluster.local:6379"
  CACHE_TTL_DEFAULT: "3600"
  
  # Application configuration
  LOG_LEVEL: "INFO"
  METRICS_ENABLED: "true"
  TRACING_ENABLED: "true"
  
  # External service URLs
  PHILSYS_API_URL: "https://api.philsys.gov.ph"
  BANGKO_SENTRAL_API_URL: "https://api.bsp.gov.ph"
  
  # Feature flags
  FEATURE_AI_VALIDATION: "true"
  FEATURE_ADVANCED_ANALYTICS: "true"
  FEATURE_REAL_TIME_NOTIFICATIONS: "true"
```

### 2. Service-Specific Configuration
```bash
# Apply service configurations
kubectl apply -f configs/registration-service-config.yaml
kubectl apply -f configs/data-management-service-config.yaml
kubectl apply -f configs/eligibility-service-config.yaml
kubectl apply -f configs/payment-service-config.yaml
kubectl apply -f configs/interoperability-service-config.yaml
kubectl apply -f configs/grievance-service-config.yaml
kubectl apply -f configs/analytics-service-config.yaml
```

## Security Configuration

### 1. SSL/TLS Configuration
```bash
# Install cert-manager
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.12.0/cert-manager.yaml

# Create cluster issuer
kubectl apply -f - <<EOF
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: admin@dsr.gov.ph
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
EOF
```

### 2. Network Policies
```bash
# Apply network security policies
kubectl apply -f infrastructure/k8s/security/network-policies.yaml

# Configure pod security policies
kubectl apply -f infrastructure/k8s/security/pod-security-policies.yaml
```

### 3. RBAC Configuration
```bash
# Create service accounts and RBAC
kubectl apply -f infrastructure/k8s/security/rbac.yaml

# Verify RBAC configuration
kubectl auth can-i --list --as=system:serviceaccount:dsr-production:dsr-app
```

## Monitoring Setup

### 1. Prometheus and Grafana
```bash
# Install Prometheus stack
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --set grafana.adminPassword=$(openssl rand -base64 32) \
  --set prometheus.prometheusSpec.retention=30d \
  --set prometheus.prometheusSpec.storageSpec.volumeClaimTemplate.spec.resources.requests.storage=100Gi

# Apply custom monitoring configuration
kubectl apply -f infrastructure/monitoring/
```

### 2. Logging Stack
```bash
# Install ELK stack
helm repo add elastic https://helm.elastic.co
helm install elasticsearch elastic/elasticsearch --namespace monitoring
helm install kibana elastic/kibana --namespace monitoring
helm install filebeat elastic/filebeat --namespace monitoring

# Configure log forwarding
kubectl apply -f infrastructure/logging/
```

### 3. Alerting Configuration
```bash
# Configure AlertManager
kubectl apply -f infrastructure/monitoring/alerts/

# Test alerting
kubectl apply -f infrastructure/monitoring/test-alerts.yaml
```

## Load Balancing Configuration

### 1. NGINX Ingress Configuration
```bash
# Apply ingress configuration
kubectl apply -f infrastructure/k8s/load-balancer/dsr-load-balancing-config.yaml

# Verify ingress
kubectl get ingress -n dsr-production
kubectl describe ingress dsr-ingress -n dsr-production
```

### 2. Istio Service Mesh
```bash
# Apply service mesh configuration
kubectl apply -f infrastructure/k8s/load-balancer/dsr-service-mesh-config.yaml

# Verify service mesh
istioctl proxy-status
istioctl analyze
```

### 3. Auto-scaling Configuration
```bash
# Verify HPA configuration
kubectl get hpa -n dsr-production

# Test auto-scaling
kubectl run -i --tty load-generator --rm --image=busybox --restart=Never -- /bin/sh
# Inside the pod:
while true; do wget -q -O- http://dsr-registration-service:8080/actuator/health; done
```

## Backup and Disaster Recovery

### 1. Backup Configuration
```bash
# Deploy backup services
kubectl apply -f infrastructure/backup/dsr-backup-disaster-recovery.yml

# Verify backup jobs
kubectl get cronjobs -n dsr-infrastructure
kubectl get jobs -n dsr-infrastructure
```

### 2. Disaster Recovery Testing
```bash
# Test database backup restoration
kubectl create job --from=cronjob/postgres-backup postgres-backup-test

# Test application recovery
kubectl delete deployment dsr-registration-service -n dsr-production
kubectl apply -f infrastructure/k8s/production/registration-service.yaml
```

## Post-Deployment Verification

### 1. Health Checks
```bash
# Check all pods are running
kubectl get pods -n dsr-production
kubectl get pods -n dsr-infrastructure
kubectl get pods -n monitoring

# Check service endpoints
curl -f https://api.dsr.gov.ph/actuator/health
curl -f https://dsr.gov.ph/health
```

### 2. Functional Testing
```bash
# Run smoke tests
kubectl apply -f tests/smoke-tests/

# Run integration tests
kubectl apply -f tests/integration-tests/

# Verify test results
kubectl logs -l app=smoke-tests -n dsr-production
```

### 3. Performance Validation
```bash
# Run load tests
kubectl apply -f tests/load-tests/

# Monitor performance metrics
kubectl port-forward svc/prometheus-server 9090:80 -n monitoring
# Access Prometheus at http://localhost:9090

kubectl port-forward svc/grafana 3000:80 -n monitoring
# Access Grafana at http://localhost:3000
```

## Troubleshooting

### Common Issues and Solutions

#### 1. Pod Startup Issues
```bash
# Check pod status
kubectl describe pod <pod-name> -n dsr-production

# Check logs
kubectl logs <pod-name> -n dsr-production --previous

# Check events
kubectl get events -n dsr-production --sort-by='.lastTimestamp'
```

#### 2. Database Connection Issues
```bash
# Test database connectivity
kubectl exec -it dsr-postgres-primary-0 -n dsr-infrastructure -- pg_isready

# Check database logs
kubectl logs dsr-postgres-primary-0 -n dsr-infrastructure

# Verify secrets
kubectl get secret postgres-credentials -n dsr-infrastructure -o yaml
```

#### 3. Service Discovery Issues
```bash
# Check service endpoints
kubectl get endpoints -n dsr-production

# Test service connectivity
kubectl run debug --image=busybox -it --rm --restart=Never -- nslookup dsr-registration-service.dsr-production.svc.cluster.local
```

#### 4. Ingress Issues
```bash
# Check ingress status
kubectl describe ingress dsr-ingress -n dsr-production

# Check NGINX controller logs
kubectl logs -l app.kubernetes.io/name=ingress-nginx -n ingress-nginx

# Test SSL certificates
openssl s_client -connect dsr.gov.ph:443 -servername dsr.gov.ph
```

#### 5. Performance Issues
```bash
# Check resource usage
kubectl top nodes
kubectl top pods -n dsr-production

# Check HPA status
kubectl describe hpa -n dsr-production

# Monitor metrics
kubectl port-forward svc/prometheus-server 9090:80 -n monitoring
```

### Emergency Procedures

#### 1. Service Rollback
```bash
# Rollback deployment
kubectl rollout undo deployment/dsr-registration-service -n dsr-production

# Check rollback status
kubectl rollout status deployment/dsr-registration-service -n dsr-production
```

#### 2. Database Recovery
```bash
# Restore from backup
kubectl create job --from=cronjob/postgres-restore postgres-restore-emergency

# Monitor restoration
kubectl logs -f job/postgres-restore-emergency -n dsr-infrastructure
```

#### 3. Scale Down for Maintenance
```bash
# Scale down all services
kubectl scale deployment --all --replicas=0 -n dsr-production

# Scale up after maintenance
kubectl scale deployment --all --replicas=3 -n dsr-production
```

---

**For additional support:**
- **Technical Support:** devops@dsr.gov.ph
- **Emergency Hotline:** +63-2-8888-DSRR (3777)
- **Documentation:** https://docs.dsr.gov.ph/deployment
- **Status Page:** https://status.dsr.gov.ph
