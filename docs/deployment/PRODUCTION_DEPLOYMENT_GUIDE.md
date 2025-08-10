# DSR Production Deployment Guide

**Version:** 1.0  
**Date:** June 24, 2025  
**Status:** Production Ready  

## 📋 Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Infrastructure Setup](#infrastructure-setup)
4. [Service Deployment](#service-deployment)
5. [Security Configuration](#security-configuration)
6. [Monitoring Setup](#monitoring-setup)
7. [Verification Procedures](#verification-procedures)
8. [Operational Guidelines](#operational-guidelines)
9. [Troubleshooting](#troubleshooting)
10. [Rollback Procedures](#rollback-procedures)

## 🎯 Overview

The DSR (Dynamic Social Registry) system is a comprehensive microservices-based platform consisting of:
- **7 Backend Services**: Registration, Data Management, Eligibility, Payment, Interoperability, Grievance, Analytics
- **1 Frontend Application**: Next.js 14+ with TypeScript
- **Infrastructure Components**: PostgreSQL, Redis, Kafka, NGINX Load Balancer
- **Monitoring Stack**: Prometheus, Grafana, Alertmanager

## ✅ Prerequisites

### System Requirements
- **Kubernetes Cluster**: v1.25+ with minimum 16 CPU cores, 32GB RAM
- **Storage**: 500GB SSD for databases and logs
- **Network**: Load balancer with SSL termination capability
- **DNS**: Configured domains (dsr.gov.ph, api.dsr.gov.ph)

### Required Tools
```bash
# Install required tools
kubectl version --client
helm version
podman --version
git --version
```

### Environment Variables
```bash
# Production environment configuration
export ENVIRONMENT=production
export NAMESPACE=dsr-production
export DOMAIN=dsr.gov.ph
export API_DOMAIN=api.dsr.gov.ph
export DATABASE_PASSWORD="SECURE_PRODUCTION_PASSWORD"
export JWT_SECRET="SECURE_JWT_SECRET_KEY"
export PHILSYS_API_KEY="PRODUCTION_PHILSYS_KEY"
```

## 🏗️ Infrastructure Setup

### 1. Create Production Namespace
```bash
kubectl create namespace dsr-production
kubectl label namespace dsr-production name=dsr-production
```

### 2. Deploy Infrastructure Components
```bash
# Deploy PostgreSQL
kubectl apply -f infrastructure/k8s/infrastructure/postgresql.yaml -n dsr-production

# Deploy Redis
kubectl apply -f infrastructure/k8s/infrastructure/redis.yaml -n dsr-production

# Deploy Kafka
kubectl apply -f infrastructure/k8s/infrastructure/kafka.yaml -n dsr-production

# Wait for infrastructure to be ready
kubectl wait --for=condition=ready pod -l app=postgresql -n dsr-production --timeout=300s
kubectl wait --for=condition=ready pod -l app=redis -n dsr-production --timeout=300s
kubectl wait --for=condition=ready pod -l app=kafka -n dsr-production --timeout=300s
```

### 3. Configure Security Policies
```bash
# Apply security policies
kubectl apply -f infrastructure/k8s/security/security-policies.yaml

# Verify security policies
kubectl get networkpolicy -n dsr-production
kubectl get podsecuritypolicy -n dsr-production
```

## 🚀 Service Deployment

### 1. Deploy Backend Services
```bash
# Deploy all 7 microservices
services=(
    "registration-service"
    "data-management-service"
    "eligibility-service"
    "payment-service"
    "interoperability-service"
    "grievance-service"
    "analytics-service"
)

for service in "${services[@]}"; do
    echo "Deploying $service..."
    kubectl apply -f "infrastructure/k8s/services/${service}.yaml" -n dsr-production
    kubectl rollout status deployment/"dsr-${service}" -n dsr-production --timeout=300s
    echo "✅ $service deployed successfully"
done
```

### 2. Deploy Frontend Application
```bash
# Deploy frontend
kubectl apply -f infrastructure/k8s/frontend/frontend.yaml -n dsr-production
kubectl rollout status deployment/dsr-frontend -n dsr-production --timeout=300s
```

### 3. Configure Load Balancer
```bash
# Deploy NGINX Ingress Controller
kubectl apply -f infrastructure/k8s/load-balancer/nginx-ingress.yaml

# Apply ingress rules
kubectl apply -f infrastructure/k8s/security/security-policies.yaml -n dsr-production
```

## 🔒 Security Configuration

### 1. SSL/TLS Certificates
```bash
# Install cert-manager for automatic certificate management
kubectl apply -f https://github.com/jetstack/cert-manager/releases/download/v1.12.0/cert-manager.yaml

# Create certificate issuer
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

### 2. Update Production Secrets
```bash
# Create production secrets
kubectl create secret generic dsr-production-secrets \
  --from-literal=DATABASE_PASSWORD="$DATABASE_PASSWORD" \
  --from-literal=JWT_SECRET="$JWT_SECRET" \
  --from-literal=PHILSYS_API_KEY="$PHILSYS_API_KEY" \
  -n dsr-production

# Verify secrets
kubectl get secrets -n dsr-production
```

### 3. Configure RBAC
```bash
# Apply role-based access control
kubectl apply -f infrastructure/k8s/security/rbac.yaml -n dsr-production
```

## 📊 Monitoring Setup

### 1. Deploy Prometheus Stack
```bash
# Deploy Prometheus
kubectl apply -f infrastructure/k8s/monitoring/prometheus.yaml -n dsr-monitoring

# Deploy Grafana
kubectl apply -f infrastructure/k8s/monitoring/grafana.yaml -n dsr-monitoring

# Deploy Alertmanager
kubectl apply -f infrastructure/k8s/monitoring/alertmanager.yaml -n dsr-monitoring
```

### 2. Configure Dashboards
```bash
# Import DSR-specific dashboards
kubectl apply -f infrastructure/k8s/monitoring/dashboards/ -n dsr-monitoring
```

### 3. Set Up Alerting
```bash
# Configure alert rules
kubectl apply -f infrastructure/k8s/monitoring/alert-rules.yaml -n dsr-monitoring
```

## ✅ Verification Procedures

### 1. Health Checks
```bash
# Run comprehensive health checks
./scripts/deploy-production.sh health-check

# Verify all services
services=(8080 8081 8082 8083 8084 8085 8086)
for port in "${services[@]}"; do
    curl -f "https://api.dsr.gov.ph/actuator/health" || echo "❌ Service on port $port failed"
done
```

### 2. End-to-End Testing
```bash
# Run integration tests against production
export BASE_URL="https://dsr.gov.ph"
export API_BASE_URL="https://api.dsr.gov.ph"
./scripts/run-integration-tests.sh
```

### 3. Performance Testing
```bash
# Load testing with k6
k6 run --vus 100 --duration 5m tests/performance/load-test.js
```

## 🔧 Operational Guidelines

### Daily Operations
1. **Monitor Dashboards**: Check Grafana dashboards for system health
2. **Review Logs**: Check application and infrastructure logs
3. **Backup Verification**: Ensure database backups are successful
4. **Security Monitoring**: Review security alerts and access logs

### Weekly Operations
1. **Performance Review**: Analyze system performance metrics
2. **Capacity Planning**: Review resource utilization trends
3. **Security Updates**: Apply security patches if available
4. **Backup Testing**: Test backup restoration procedures

### Monthly Operations
1. **Security Audit**: Conduct comprehensive security review
2. **Disaster Recovery Testing**: Test full system recovery
3. **Performance Optimization**: Optimize based on usage patterns
4. **Documentation Updates**: Update operational documentation

## 🚨 Troubleshooting

### Common Issues

#### Service Not Starting
```bash
# Check pod status
kubectl get pods -n dsr-production

# Check logs
kubectl logs -f deployment/dsr-registration-service -n dsr-production

# Check events
kubectl get events -n dsr-production --sort-by='.lastTimestamp'
```

#### Database Connection Issues
```bash
# Check PostgreSQL status
kubectl exec -it postgresql-0 -n dsr-production -- pg_isready

# Test connection from service
kubectl exec -it deployment/dsr-registration-service -n dsr-production -- \
  curl -f http://postgresql.dsr-production:5432
```

#### Performance Issues
```bash
# Check resource usage
kubectl top pods -n dsr-production
kubectl top nodes

# Scale services if needed
kubectl scale deployment dsr-registration-service --replicas=5 -n dsr-production
```

## 🔄 Rollback Procedures

### Quick Rollback
```bash
# Rollback specific service
kubectl rollout undo deployment/dsr-registration-service -n dsr-production

# Rollback all services
./scripts/deploy-production.sh rollback
```

### Full System Rollback
```bash
# 1. Stop traffic to new version
kubectl patch ingress dsr-ingress -n dsr-production -p '{"spec":{"rules":[]}}'

# 2. Rollback database if needed
kubectl exec -it postgresql-0 -n dsr-production -- \
  psql -U dsr_user -d dsr_production -c "SELECT pg_restore('/backup/latest.sql')"

# 3. Rollback all services
kubectl rollout undo deployment --all -n dsr-production

# 4. Restore traffic
kubectl patch ingress dsr-ingress -n dsr-production --type=merge -p "$(cat infrastructure/k8s/security/security-policies.yaml | grep -A 50 'kind: Ingress')"
```

## 📞 Support Contacts

- **Technical Lead**: tech-lead@dsr.gov.ph
- **DevOps Team**: devops@dsr.gov.ph
- **Security Team**: security@dsr.gov.ph
- **Emergency Hotline**: +63-XXX-XXX-XXXX

## 📋 Deployment Checklist

### Pre-Deployment
- [ ] Infrastructure prerequisites verified
- [ ] Security certificates obtained
- [ ] Production secrets configured
- [ ] Database backup completed
- [ ] Monitoring stack operational

### Deployment
- [ ] Infrastructure components deployed
- [ ] Backend services deployed and healthy
- [ ] Frontend application deployed
- [ ] Load balancer configured
- [ ] SSL/TLS certificates applied

### Post-Deployment
- [ ] Health checks passed
- [ ] Integration tests successful
- [ ] Performance tests completed
- [ ] Monitoring alerts configured
- [ ] Documentation updated

### Go-Live
- [ ] DNS records updated
- [ ] Traffic routing verified
- [ ] User acceptance testing completed
- [ ] Support team notified
- [ ] Rollback plan confirmed

## 📚 Additional Resources

- [API Documentation](../api/README.md)
- [Security Guidelines](../security/SECURITY.md)
- [Monitoring Runbook](../monitoring/RUNBOOK.md)
- [Backup Procedures](../backup/BACKUP_GUIDE.md)
- [Operational Runbook](OPERATIONAL_RUNBOOK.md)

---

**Document Status:** ✅ Production Ready
**Last Updated:** June 24, 2025
**Next Review:** July 24, 2025
