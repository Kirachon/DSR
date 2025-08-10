#!/bin/bash

# DSR Production Deployment Script
# Deploys all DSR services to production environment with health checks and rollback capability

set -euo pipefail

# Configuration
NAMESPACE="dsr-production"
KUBECTL_TIMEOUT="300s"
HEALTH_CHECK_RETRIES=30
HEALTH_CHECK_INTERVAL=10

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
        log_error "kubectl is not installed or not in PATH"
        exit 1
    fi
    
    # Check if helm is installed
    if ! command -v helm &> /dev/null; then
        log_error "helm is not installed or not in PATH"
        exit 1
    fi
    
    # Check if we can connect to the cluster
    if ! kubectl cluster-info &> /dev/null; then
        log_error "Cannot connect to Kubernetes cluster"
        exit 1
    fi
    
    log_success "Prerequisites check passed"
}

# Create namespace if it doesn't exist
create_namespace() {
    log_info "Creating namespace if it doesn't exist..."
    
    if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
        kubectl create namespace "$NAMESPACE"
        log_success "Created namespace: $NAMESPACE"
    else
        log_info "Namespace $NAMESPACE already exists"
    fi
}

# Deploy infrastructure components
deploy_infrastructure() {
    log_info "Deploying infrastructure components..."
    
    # Deploy PostgreSQL
    log_info "Deploying PostgreSQL..."
    kubectl apply -f infrastructure/k8s/infrastructure/postgresql.yaml -n "$NAMESPACE"
    
    # Deploy Redis
    log_info "Deploying Redis..."
    kubectl apply -f infrastructure/k8s/infrastructure/redis.yaml -n "$NAMESPACE"
    
    # Deploy Kafka
    log_info "Deploying Kafka..."
    kubectl apply -f infrastructure/k8s/infrastructure/kafka.yaml -n "$NAMESPACE"
    
    # Wait for infrastructure to be ready
    log_info "Waiting for infrastructure to be ready..."
    kubectl wait --for=condition=ready pod -l app=postgresql -n "$NAMESPACE" --timeout="$KUBECTL_TIMEOUT"
    kubectl wait --for=condition=ready pod -l app=redis -n "$NAMESPACE" --timeout="$KUBECTL_TIMEOUT"
    kubectl wait --for=condition=ready pod -l app=kafka -n "$NAMESPACE" --timeout="$KUBECTL_TIMEOUT"
    
    log_success "Infrastructure deployment completed"
}

# Deploy DSR services
deploy_services() {
    log_info "Deploying DSR services..."
    
    local services=(
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
        kubectl apply -f "infrastructure/k8s/services/${service}.yaml" -n "$NAMESPACE"
        
        # Wait for deployment to be ready
        kubectl rollout status deployment/"dsr-${service}" -n "$NAMESPACE" --timeout="$KUBECTL_TIMEOUT"
        
        log_success "$service deployed successfully"
    done
}

# Deploy frontend
deploy_frontend() {
    log_info "Deploying frontend application..."
    
    kubectl apply -f infrastructure/k8s/frontend/frontend.yaml -n "$NAMESPACE"
    kubectl rollout status deployment/dsr-frontend -n "$NAMESPACE" --timeout="$KUBECTL_TIMEOUT"
    
    log_success "Frontend deployed successfully"
}

# Health check function
health_check() {
    local service=$1
    local port=$2
    local endpoint=${3:-"/actuator/health"}
    
    log_info "Performing health check for $service..."
    
    # Get service IP
    local service_ip
    service_ip=$(kubectl get service "dsr-${service}" -n "$NAMESPACE" -o jsonpath='{.spec.clusterIP}')
    
    for ((i=1; i<=HEALTH_CHECK_RETRIES; i++)); do
        if kubectl run health-check-"$service" --rm -i --restart=Never --image=curlimages/curl -- \
           curl -f "http://${service_ip}:${port}${endpoint}" &> /dev/null; then
            log_success "$service health check passed"
            return 0
        fi
        
        log_warning "$service health check failed (attempt $i/$HEALTH_CHECK_RETRIES)"
        sleep $HEALTH_CHECK_INTERVAL
    done
    
    log_error "$service health check failed after $HEALTH_CHECK_RETRIES attempts"
    return 1
}

# Perform health checks for all services
perform_health_checks() {
    log_info "Performing health checks for all services..."
    
    local services=(
        "registration-service:8080"
        "data-management-service:8081"
        "eligibility-service:8082"
        "payment-service:8083"
        "interoperability-service:8084"
        "grievance-service:8085"
        "analytics-service:8086"
    )
    
    local failed_services=()
    
    for service_port in "${services[@]}"; do
        IFS=':' read -r service port <<< "$service_port"
        if ! health_check "$service" "$port"; then
            failed_services+=("$service")
        fi
    done
    
    if [ ${#failed_services[@]} -eq 0 ]; then
        log_success "All services passed health checks"
        return 0
    else
        log_error "The following services failed health checks: ${failed_services[*]}"
        return 1
    fi
}

# Deploy monitoring stack
deploy_monitoring() {
    log_info "Deploying monitoring stack..."
    
    # Deploy Prometheus
    kubectl apply -f infrastructure/k8s/monitoring/prometheus.yaml -n "$NAMESPACE"
    
    # Deploy Grafana
    kubectl apply -f infrastructure/k8s/monitoring/grafana.yaml -n "$NAMESPACE"
    
    # Wait for monitoring to be ready
    kubectl wait --for=condition=ready pod -l app=prometheus -n "$NAMESPACE" --timeout="$KUBECTL_TIMEOUT"
    kubectl wait --for=condition=ready pod -l app=grafana -n "$NAMESPACE" --timeout="$KUBECTL_TIMEOUT"
    
    log_success "Monitoring stack deployed successfully"
}

# Rollback function
rollback() {
    log_warning "Initiating rollback..."
    
    # Rollback all deployments
    kubectl rollout undo deployment --all -n "$NAMESPACE"
    
    log_success "Rollback completed"
}

# Main deployment function
main() {
    log_info "Starting DSR production deployment..."
    
    # Trap to handle errors and perform rollback if needed
    trap 'log_error "Deployment failed. Consider running rollback."; exit 1' ERR
    
    check_prerequisites
    create_namespace
    deploy_infrastructure
    deploy_services
    deploy_frontend
    deploy_monitoring
    
    if perform_health_checks; then
        log_success "DSR production deployment completed successfully!"
        log_info "Access the application at: https://dsr.gov.ph"
        log_info "Access monitoring at: https://monitoring.dsr.gov.ph"
    else
        log_error "Deployment completed but some services failed health checks"
        log_warning "Consider investigating the failed services or performing a rollback"
        exit 1
    fi
}

# Handle command line arguments
case "${1:-deploy}" in
    "deploy")
        main
        ;;
    "rollback")
        rollback
        ;;
    "health-check")
        perform_health_checks
        ;;
    *)
        echo "Usage: $0 [deploy|rollback|health-check]"
        echo "  deploy      - Deploy all DSR services to production"
        echo "  rollback    - Rollback all deployments"
        echo "  health-check - Perform health checks on all services"
        exit 1
        ;;
esac
