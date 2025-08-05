#!/bin/bash

# DSR Enhanced System Deployment Script
# Deploys the complete DSR system with API Gateway, Service Discovery, and all microservices

set -e

# Configuration
NAMESPACE="dsr-system"
CHART_VERSION="3.0.0"
TIMEOUT="600s"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    # Check if kubectl is installed
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed"
        exit 1
    fi
    
    # Check if helm is installed
    if ! command -v helm &> /dev/null; then
        log_error "helm is not installed"
        exit 1
    fi
    
    # Check if cluster is accessible
    if ! kubectl cluster-info &> /dev/null; then
        log_error "Cannot access Kubernetes cluster"
        exit 1
    fi
    
    log_success "Prerequisites check passed"
}

# Create namespace
create_namespace() {
    log_info "Creating namespace: $NAMESPACE"
    
    if kubectl get namespace $NAMESPACE &> /dev/null; then
        log_warning "Namespace $NAMESPACE already exists"
    else
        kubectl create namespace $NAMESPACE
        kubectl label namespace $NAMESPACE app.kubernetes.io/name=dsr
        log_success "Namespace $NAMESPACE created"
    fi
}

# Deploy infrastructure components
deploy_infrastructure() {
    log_info "Deploying infrastructure components..."
    
    # Deploy PostgreSQL
    log_info "Deploying PostgreSQL..."
    helm upgrade --install dsr-postgresql bitnami/postgresql \
        --namespace $NAMESPACE \
        --version 12.1.9 \
        --set auth.postgresPassword=dsrpassword123 \
        --set auth.database=dsr_db \
        --set primary.persistence.size=20Gi \
        --set readReplicas.replicaCount=2 \
        --timeout $TIMEOUT
    
    # Deploy Redis
    log_info "Deploying Redis..."
    helm upgrade --install dsr-redis bitnami/redis \
        --namespace $NAMESPACE \
        --version 17.4.3 \
        --set auth.enabled=false \
        --set master.persistence.size=8Gi \
        --set replica.replicaCount=2 \
        --timeout $TIMEOUT
    
    log_success "Infrastructure components deployed"
}

# Deploy core services
deploy_core_services() {
    log_info "Deploying core services..."
    
    # Deploy Eureka Server
    log_info "Deploying Eureka Server..."
    kubectl apply -f ../k8s/dsr-complete-deployment.yaml
    
    # Wait for Eureka Server to be ready
    log_info "Waiting for Eureka Server to be ready..."
    kubectl wait --for=condition=available --timeout=300s deployment/dsr-eureka-server -n $NAMESPACE
    
    # Deploy API Gateway
    log_info "Deploying API Gateway..."
    kubectl wait --for=condition=available --timeout=300s deployment/dsr-api-gateway -n $NAMESPACE
    
    log_success "Core services deployed"
}

# Deploy microservices
deploy_microservices() {
    log_info "Deploying DSR microservices..."
    
    # List of services to deploy
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
        log_info "Deploying $service..."
        
        # Apply existing Kubernetes manifests
        if [ -f "../k8s/$service-deployment.yaml" ]; then
            kubectl apply -f "../k8s/$service-deployment.yaml" -n $NAMESPACE
        else
            log_warning "Deployment file for $service not found, skipping..."
        fi
    done
    
    log_success "Microservices deployment initiated"
}

# Deploy monitoring
deploy_monitoring() {
    log_info "Deploying monitoring stack..."
    
    # Deploy Prometheus
    helm upgrade --install dsr-prometheus prometheus-community/kube-prometheus-stack \
        --namespace $NAMESPACE \
        --create-namespace \
        --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false \
        --set grafana.adminPassword=dsradmin123 \
        --timeout $TIMEOUT
    
    # Apply monitoring configurations
    if [ -f "../k8s/monitoring/production-monitoring.yaml" ]; then
        kubectl apply -f "../k8s/monitoring/production-monitoring.yaml" -n $NAMESPACE
    fi
    
    log_success "Monitoring stack deployed"
}

# Verify deployment
verify_deployment() {
    log_info "Verifying deployment..."
    
    # Check all pods are running
    log_info "Checking pod status..."
    kubectl get pods -n $NAMESPACE
    
    # Check services
    log_info "Checking services..."
    kubectl get services -n $NAMESPACE
    
    # Check ingress
    log_info "Checking ingress..."
    kubectl get ingress -n $NAMESPACE
    
    # Test API Gateway health
    log_info "Testing API Gateway health..."
    if kubectl get service dsr-api-gateway -n $NAMESPACE &> /dev/null; then
        kubectl port-forward service/dsr-api-gateway 8080:8080 -n $NAMESPACE &
        sleep 5
        if curl -f http://localhost:8080/actuator/health &> /dev/null; then
            log_success "API Gateway is healthy"
        else
            log_warning "API Gateway health check failed"
        fi
        pkill -f "kubectl port-forward"
    fi
    
    log_success "Deployment verification completed"
}

# Main deployment function
main() {
    log_info "Starting DSR Enhanced System Deployment"
    log_info "Namespace: $NAMESPACE"
    log_info "Chart Version: $CHART_VERSION"
    
    check_prerequisites
    create_namespace
    deploy_infrastructure
    deploy_core_services
    deploy_microservices
    deploy_monitoring
    verify_deployment
    
    log_success "DSR Enhanced System deployment completed successfully!"
    log_info "Access points:"
    log_info "- API Gateway: https://api.dsr.gov.ph"
    log_info "- Eureka Server: https://eureka.dsr.gov.ph"
    log_info "- Grafana: http://localhost:3000 (admin/dsradmin123)"
    log_info "- Prometheus: http://localhost:9090"
}

# Run main function
main "$@"
