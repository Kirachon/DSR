#!/bin/bash

# DSR Production Infrastructure Setup Script
# Comprehensive production environment configuration for Kubernetes deployment
# Includes infrastructure, security, monitoring, and deployment pipeline setup

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
NAMESPACE="dsr-production"
DOMAIN="${DOMAIN:-dsr.gov.ph}"
API_DOMAIN="${API_DOMAIN:-api.dsr.gov.ph}"
ENVIRONMENT="production"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${PURPLE}[STEP]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    log_step "Checking prerequisites..."
    
    # Check required tools
    local required_tools=("kubectl" "helm" "podman" "git" "openssl")
    for tool in "${required_tools[@]}"; do
        if ! command -v "$tool" &> /dev/null; then
            log_error "$tool is required but not installed"
            exit 1
        fi
    done
    
    # Check Kubernetes connection
    if ! kubectl cluster-info &> /dev/null; then
        log_error "Cannot connect to Kubernetes cluster"
        exit 1
    fi
    
    # Check Helm repositories
    helm repo add bitnami https://charts.bitnami.com/bitnami 2>/dev/null || true
    helm repo add prometheus-community https://prometheus-community.github.io/helm-charts 2>/dev/null || true
    helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx 2>/dev/null || true
    helm repo update
    
    log_success "Prerequisites check completed"
}

# Create namespace and basic resources
setup_namespace() {
    log_step "Setting up Kubernetes namespace..."
    
    # Create namespace
    kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -
    
    # Label namespace
    kubectl label namespace "$NAMESPACE" name="$NAMESPACE" --overwrite
    kubectl label namespace "$NAMESPACE" environment="$ENVIRONMENT" --overwrite
    
    # Create resource quotas
    cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ResourceQuota
metadata:
  name: dsr-resource-quota
  namespace: $NAMESPACE
spec:
  hard:
    requests.cpu: "20"
    requests.memory: 40Gi
    limits.cpu: "40"
    limits.memory: 80Gi
    persistentvolumeclaims: "10"
    services: "20"
    secrets: "50"
    configmaps: "50"
EOF

    # Create network policies
    cat <<EOF | kubectl apply -f -
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: dsr-network-policy
  namespace: $NAMESPACE
spec:
  podSelector: {}
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: $NAMESPACE
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
  egress:
  - {}
EOF

    log_success "Namespace setup completed"
}

# Setup secrets and configuration
setup_secrets() {
    log_step "Setting up secrets and configuration..."
    
    # Generate JWT secret if not exists
    if ! kubectl get secret jwt-secret -n "$NAMESPACE" &> /dev/null; then
        JWT_SECRET=$(openssl rand -base64 32)
        kubectl create secret generic jwt-secret \
            --from-literal=secret="$JWT_SECRET" \
            -n "$NAMESPACE"
    fi
    
    # Generate database passwords
    if ! kubectl get secret postgres-secret -n "$NAMESPACE" &> /dev/null; then
        DB_PASSWORD=$(openssl rand -base64 16)
        kubectl create secret generic postgres-secret \
            --from-literal=postgres-password="$DB_PASSWORD" \
            --from-literal=password="$DB_PASSWORD" \
            -n "$NAMESPACE"
    fi
    
    # Create TLS certificates (self-signed for demo, use real certs in production)
    if ! kubectl get secret tls-secret -n "$NAMESPACE" &> /dev/null; then
        openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
            -keyout tls.key -out tls.crt \
            -subj "/CN=$DOMAIN/O=DSR/C=PH"
        
        kubectl create secret tls tls-secret \
            --key tls.key --cert tls.crt \
            -n "$NAMESPACE"
        
        rm tls.key tls.crt
    fi
    
    # Create application configuration
    cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ConfigMap
metadata:
  name: dsr-config
  namespace: $NAMESPACE
data:
  ENVIRONMENT: "$ENVIRONMENT"
  DOMAIN: "$DOMAIN"
  API_DOMAIN: "$API_DOMAIN"
  LOG_LEVEL: "INFO"
  METRICS_ENABLED: "true"
  TRACING_ENABLED: "true"
EOF

    log_success "Secrets and configuration setup completed"
}

# Deploy PostgreSQL database
deploy_database() {
    log_step "Deploying PostgreSQL database..."
    
    # Create persistent volume claims
    cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: postgres-pvc
  namespace: $NAMESPACE
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 100Gi
  storageClassName: fast-ssd
EOF

    # Deploy PostgreSQL using Helm
    helm upgrade --install postgresql bitnami/postgresql \
        --namespace "$NAMESPACE" \
        --set auth.existingSecret=postgres-secret \
        --set auth.secretKeys.adminPasswordKey=postgres-password \
        --set auth.database=dsr_db \
        --set primary.persistence.existingClaim=postgres-pvc \
        --set primary.persistence.size=100Gi \
        --set primary.resources.requests.memory=4Gi \
        --set primary.resources.requests.cpu=2 \
        --set primary.resources.limits.memory=8Gi \
        --set primary.resources.limits.cpu=4 \
        --set metrics.enabled=true \
        --set metrics.serviceMonitor.enabled=true \
        --wait --timeout=10m
    
    log_success "PostgreSQL database deployed"
}

# Deploy Redis cache
deploy_redis() {
    log_step "Deploying Redis cache..."
    
    helm upgrade --install redis bitnami/redis \
        --namespace "$NAMESPACE" \
        --set auth.enabled=false \
        --set master.persistence.size=10Gi \
        --set master.resources.requests.memory=1Gi \
        --set master.resources.requests.cpu=500m \
        --set master.resources.limits.memory=2Gi \
        --set master.resources.limits.cpu=1 \
        --set metrics.enabled=true \
        --set metrics.serviceMonitor.enabled=true \
        --wait --timeout=5m
    
    log_success "Redis cache deployed"
}

# Deploy monitoring stack
deploy_monitoring() {
    log_step "Deploying monitoring stack..."
    
    # Create monitoring namespace
    kubectl create namespace monitoring --dry-run=client -o yaml | kubectl apply -f -
    
    # Deploy Prometheus stack
    helm upgrade --install prometheus prometheus-community/kube-prometheus-stack \
        --namespace monitoring \
        --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false \
        --set prometheus.prometheusSpec.retention=30d \
        --set prometheus.prometheusSpec.storageSpec.volumeClaimTemplate.spec.resources.requests.storage=50Gi \
        --set grafana.adminPassword=admin123 \
        --set grafana.persistence.enabled=true \
        --set grafana.persistence.size=10Gi \
        --set alertmanager.alertmanagerSpec.storage.volumeClaimTemplate.spec.resources.requests.storage=10Gi \
        --wait --timeout=10m
    
    # Create ServiceMonitor for DSR services
    cat <<EOF | kubectl apply -f -
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: dsr-services
  namespace: monitoring
  labels:
    app: dsr
spec:
  selector:
    matchLabels:
      app: dsr
  endpoints:
  - port: metrics
    path: /actuator/prometheus
    interval: 30s
  namespaceSelector:
    matchNames:
    - $NAMESPACE
EOF

    log_success "Monitoring stack deployed"
}

# Deploy ingress controller
deploy_ingress() {
    log_step "Deploying ingress controller..."
    
    # Create ingress-nginx namespace
    kubectl create namespace ingress-nginx --dry-run=client -o yaml | kubectl apply -f -
    
    # Deploy NGINX Ingress Controller
    helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx \
        --namespace ingress-nginx \
        --set controller.replicaCount=2 \
        --set controller.nodeSelector."kubernetes\.io/os"=linux \
        --set controller.service.type=LoadBalancer \
        --set controller.service.externalTrafficPolicy=Local \
        --set controller.metrics.enabled=true \
        --set controller.metrics.serviceMonitor.enabled=true \
        --wait --timeout=10m
    
    log_success "Ingress controller deployed"
}

# Create deployment configurations
create_deployment_configs() {
    log_step "Creating deployment configurations..."
    
    # Create deployment directory
    mkdir -p "$PROJECT_ROOT/deployment/production/configs"
    
    # Create database initialization script
    cat <<EOF > "$PROJECT_ROOT/deployment/production/configs/init-db.sql"
-- DSR Database Initialization Script
CREATE DATABASE IF NOT EXISTS dsr_db;
CREATE USER IF NOT EXISTS 'dsr_user'@'%' IDENTIFIED BY 'dsr_password';
GRANT ALL PRIVILEGES ON dsr_db.* TO 'dsr_user'@'%';
FLUSH PRIVILEGES;

-- Create schemas for each service
USE dsr_db;
CREATE SCHEMA IF NOT EXISTS registration;
CREATE SCHEMA IF NOT EXISTS data_management;
CREATE SCHEMA IF NOT EXISTS eligibility;
CREATE SCHEMA IF NOT EXISTS payment;
CREATE SCHEMA IF NOT EXISTS interoperability;
CREATE SCHEMA IF NOT EXISTS grievance;
CREATE SCHEMA IF NOT EXISTS analytics;
EOF

    # Create service deployment template
    cat <<'EOF' > "$PROJECT_ROOT/deployment/production/configs/service-deployment-template.yaml"
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ${SERVICE_NAME}
  namespace: ${NAMESPACE}
  labels:
    app: dsr
    service: ${SERVICE_NAME}
    version: ${VERSION}
spec:
  replicas: ${REPLICAS}
  selector:
    matchLabels:
      app: dsr
      service: ${SERVICE_NAME}
  template:
    metadata:
      labels:
        app: dsr
        service: ${SERVICE_NAME}
        version: ${VERSION}
    spec:
      containers:
      - name: ${SERVICE_NAME}
        image: ${IMAGE}
        ports:
        - containerPort: 8080
          name: http
        - containerPort: 8081
          name: metrics
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DATABASE_URL
          value: "jdbc:postgresql://postgresql:5432/dsr_db"
        - name: DATABASE_USERNAME
          value: "postgres"
        - name: DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: postgres-secret
              key: postgres-password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: secret
        - name: REDIS_URL
          value: "redis://redis-master:6379"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
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
        securityContext:
          runAsNonRoot: true
          runAsUser: 1000
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
          capabilities:
            drop:
            - ALL
---
apiVersion: v1
kind: Service
metadata:
  name: ${SERVICE_NAME}
  namespace: ${NAMESPACE}
  labels:
    app: dsr
    service: ${SERVICE_NAME}
spec:
  selector:
    app: dsr
    service: ${SERVICE_NAME}
  ports:
  - name: http
    port: 80
    targetPort: 8080
  - name: metrics
    port: 8081
    targetPort: 8081
EOF

    # Create ingress configuration
    cat <<EOF > "$PROJECT_ROOT/deployment/production/configs/ingress.yaml"
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: dsr-ingress
  namespace: $NAMESPACE
  annotations:
    kubernetes.io/ingress.class: nginx
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/use-regex: "true"
    nginx.ingress.kubernetes.io/rewrite-target: /\$2
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  tls:
  - hosts:
    - $DOMAIN
    - $API_DOMAIN
    secretName: tls-secret
  rules:
  - host: $DOMAIN
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: dsr-frontend
            port:
              number: 80
  - host: $API_DOMAIN
    http:
      paths:
      - path: /api/v1/registration(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: registration-service
            port:
              number: 80
      - path: /api/v1/data-management(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: data-management-service
            port:
              number: 80
      - path: /api/v1/eligibility(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: eligibility-service
            port:
              number: 80
      - path: /api/v1/payment(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: payment-service
            port:
              number: 80
      - path: /api/v1/interoperability(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: interoperability-service
            port:
              number: 80
      - path: /api/v1/grievance(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: grievance-service
            port:
              number: 80
      - path: /api/v1/analytics(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: analytics-service
            port:
              number: 80
EOF

    log_success "Deployment configurations created"
}

# Create CI/CD pipeline configuration
create_cicd_pipeline() {
    log_step "Creating CI/CD pipeline configuration..."
    
    # Create GitHub Actions workflow
    mkdir -p "$PROJECT_ROOT/.github/workflows"
    
    cat <<EOF > "$PROJECT_ROOT/.github/workflows/production-deployment.yml"
name: DSR Production Deployment

on:
  push:
    branches: [main]
    tags: ['v*']
  workflow_dispatch:

env:
  REGISTRY: ghcr.io
  NAMESPACE: dsr-production

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Set up Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'
        cache: 'npm'
        cache-dependency-path: frontend/package-lock.json
    
    - name: Log in to Container Registry
      uses: docker/login-action@v2
      with:
        registry: \${{ env.REGISTRY }}
        username: \${{ github.actor }}
        password: \${{ secrets.GITHUB_TOKEN }}
    
    - name: Build and push backend services
      run: |
        services=("registration" "data-management" "eligibility" "payment" "interoperability" "grievance" "analytics")
        for service in "\${services[@]}"; do
          echo "Building \$service service..."
          cd "\$service-service"
          ./mvnw clean package -DskipTests
          
          # Build and push container image
          podman build -t \${{ env.REGISTRY }}/\${{ github.repository }}/\$service-service:\${{ github.sha }} .
          podman push \${{ env.REGISTRY }}/\${{ github.repository }}/\$service-service:\${{ github.sha }}
          
          cd ..
        done
    
    - name: Build and push frontend
      run: |
        cd frontend
        npm ci
        npm run build
        
        # Build and push container image
        podman build -t \${{ env.REGISTRY }}/\${{ github.repository }}/dsr-frontend:\${{ github.sha }} .
        podman push \${{ env.REGISTRY }}/\${{ github.repository }}/dsr-frontend:\${{ github.sha }}
    
    - name: Deploy to production
      run: |
        # Install kubectl
        curl -LO "https://dl.k8s.io/release/\$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
        chmod +x kubectl
        sudo mv kubectl /usr/local/bin/
        
        # Configure kubectl
        echo "\${{ secrets.KUBECONFIG }}" | base64 -d > kubeconfig
        export KUBECONFIG=kubeconfig
        
        # Deploy services
        ./deployment/production/deploy-services.sh \${{ github.sha }}
    
    - name: Run health checks
      run: |
        ./deployment/production/health-check.sh
    
    - name: Notify deployment status
      if: always()
      run: |
        if [ "\${{ job.status }}" == "success" ]; then
          echo "✅ Production deployment successful"
        else
          echo "❌ Production deployment failed"
        fi
EOF

    log_success "CI/CD pipeline configuration created"
}

# Main execution function
main() {
    log_info "🚀 Starting DSR Production Infrastructure Setup..."
    echo
    
    check_prerequisites
    setup_namespace
    setup_secrets
    deploy_database
    deploy_redis
    deploy_monitoring
    deploy_ingress
    create_deployment_configs
    create_cicd_pipeline
    
    echo
    log_success "🎉 DSR Production Infrastructure Setup Completed!"
    echo
    log_info "📋 Next Steps:"
    echo "   1. Build and push container images"
    echo "   2. Deploy DSR services using: ./deploy-services.sh"
    echo "   3. Configure DNS to point to the load balancer"
    echo "   4. Run health checks and validation"
    echo "   5. Monitor system performance and logs"
    echo
    log_info "📊 Access Information:"
    echo "   - Application: https://$DOMAIN"
    echo "   - API: https://$API_DOMAIN"
    echo "   - Grafana: kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80"
    echo "   - Prometheus: kubectl port-forward -n monitoring svc/prometheus-kube-prometheus-prometheus 9090:9090"
    echo
    log_info "🔐 Default Credentials:"
    echo "   - Grafana: admin / admin123"
    echo "   - Database: postgres / [check postgres-secret]"
}

# Handle command line arguments
case "${1:-setup}" in
    "setup")
        main
        ;;
    "namespace")
        check_prerequisites
        setup_namespace
        ;;
    "secrets")
        check_prerequisites
        setup_secrets
        ;;
    "database")
        check_prerequisites
        deploy_database
        ;;
    "redis")
        check_prerequisites
        deploy_redis
        ;;
    "monitoring")
        check_prerequisites
        deploy_monitoring
        ;;
    "ingress")
        check_prerequisites
        deploy_ingress
        ;;
    "configs")
        create_deployment_configs
        ;;
    "cicd")
        create_cicd_pipeline
        ;;
    "help"|"-h"|"--help")
        echo "Usage: $0 [setup|namespace|secrets|database|redis|monitoring|ingress|configs|cicd]"
        echo "  setup      - Run complete infrastructure setup (default)"
        echo "  namespace  - Setup Kubernetes namespace and policies"
        echo "  secrets    - Setup secrets and configuration"
        echo "  database   - Deploy PostgreSQL database"
        echo "  redis      - Deploy Redis cache"
        echo "  monitoring - Deploy monitoring stack"
        echo "  ingress    - Deploy ingress controller"
        echo "  configs    - Create deployment configurations"
        echo "  cicd       - Create CI/CD pipeline configuration"
        exit 0
        ;;
    *)
        log_error "Unknown command: $1"
        echo "Use '$0 help' for usage information"
        exit 1
        ;;
esac
