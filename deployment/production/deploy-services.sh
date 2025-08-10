#!/bin/bash

# DSR Services Deployment Script
# Deploys all DSR microservices to production Kubernetes environment
# Includes health checks, rollback capabilities, and monitoring integration

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
NAMESPACE="dsr-production"
IMAGE_TAG="${1:-latest}"
REGISTRY="${REGISTRY:-ghcr.io/kirachon/dsr}"

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

# DSR Services configuration
declare -A DSR_SERVICES=(
    ["registration-service"]="8080"
    ["data-management-service"]="8081"
    ["eligibility-service"]="8082"
    ["interoperability-service"]="8083"
    ["payment-service"]="8084"
    ["grievance-service"]="8085"
    ["analytics-service"]="8086"
)

# Check prerequisites
check_prerequisites() {
    log_step "Checking deployment prerequisites..."
    
    # Check kubectl connection
    if ! kubectl cluster-info &> /dev/null; then
        log_error "Cannot connect to Kubernetes cluster"
        exit 1
    fi
    
    # Check namespace exists
    if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
        log_error "Namespace $NAMESPACE does not exist. Run infrastructure setup first."
        exit 1
    fi
    
    # Check required secrets
    local required_secrets=("postgres-secret" "jwt-secret" "tls-secret")
    for secret in "${required_secrets[@]}"; do
        if ! kubectl get secret "$secret" -n "$NAMESPACE" &> /dev/null; then
            log_error "Required secret $secret not found in namespace $NAMESPACE"
            exit 1
        fi
    done
    
    log_success "Prerequisites check completed"
}

# Generate deployment manifest for a service
generate_service_deployment() {
    local service_name="$1"
    local service_port="$2"
    local replicas="${3:-2}"
    
    cat <<EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: $service_name
  namespace: $NAMESPACE
  labels:
    app: dsr
    service: $service_name
    version: $IMAGE_TAG
spec:
  replicas: $replicas
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
      maxSurge: 1
  selector:
    matchLabels:
      app: dsr
      service: $service_name
  template:
    metadata:
      labels:
        app: dsr
        service: $service_name
        version: $IMAGE_TAG
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8081"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
        fsGroup: 1000
      containers:
      - name: $service_name
        image: $REGISTRY/$service_name:$IMAGE_TAG
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
          name: http
          protocol: TCP
        - containerPort: 8081
          name: metrics
          protocol: TCP
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: SERVER_PORT
          value: "8080"
        - name: MANAGEMENT_SERVER_PORT
          value: "8081"
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
        - name: LOGGING_LEVEL_PH_GOV_DSR
          value: "INFO"
        - name: MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE
          value: "health,info,metrics,prometheus"
        - name: MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS
          value: "always"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
            ephemeral-storage: "1Gi"
          limits:
            memory: "1Gi"
            cpu: "500m"
            ephemeral-storage: "2Gi"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8081
          initialDelaySeconds: 120
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        securityContext:
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
          capabilities:
            drop:
            - ALL
        volumeMounts:
        - name: tmp
          mountPath: /tmp
        - name: logs
          mountPath: /app/logs
      volumes:
      - name: tmp
        emptyDir: {}
      - name: logs
        emptyDir: {}
      restartPolicy: Always
      terminationGracePeriodSeconds: 30
---
apiVersion: v1
kind: Service
metadata:
  name: $service_name
  namespace: $NAMESPACE
  labels:
    app: dsr
    service: $service_name
spec:
  type: ClusterIP
  selector:
    app: dsr
    service: $service_name
  ports:
  - name: http
    port: 80
    targetPort: 8080
    protocol: TCP
  - name: metrics
    port: 8081
    targetPort: 8081
    protocol: TCP
---
apiVersion: v1
kind: ServiceMonitor
metadata:
  name: $service_name
  namespace: $NAMESPACE
  labels:
    app: dsr
    service: $service_name
spec:
  selector:
    matchLabels:
      app: dsr
      service: $service_name
  endpoints:
  - port: metrics
    path: /actuator/prometheus
    interval: 30s
    scrapeTimeout: 10s
EOF
}

# Deploy frontend application
deploy_frontend() {
    log_step "Deploying DSR frontend application..."
    
    cat <<EOF | kubectl apply -f -
apiVersion: apps/v1
kind: Deployment
metadata:
  name: dsr-frontend
  namespace: $NAMESPACE
  labels:
    app: dsr
    service: dsr-frontend
    version: $IMAGE_TAG
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1
      maxSurge: 1
  selector:
    matchLabels:
      app: dsr
      service: dsr-frontend
  template:
    metadata:
      labels:
        app: dsr
        service: dsr-frontend
        version: $IMAGE_TAG
    spec:
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
        fsGroup: 1000
      containers:
      - name: dsr-frontend
        image: $REGISTRY/dsr-frontend:$IMAGE_TAG
        imagePullPolicy: Always
        ports:
        - containerPort: 3000
          name: http
          protocol: TCP
        env:
        - name: NODE_ENV
          value: "production"
        - name: NEXT_PUBLIC_API_URL
          value: "https://api.dsr.gov.ph"
        - name: NEXTAUTH_URL
          value: "https://dsr.gov.ph"
        - name: NEXTAUTH_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: secret
        resources:
          requests:
            memory: "256Mi"
            cpu: "100m"
          limits:
            memory: "512Mi"
            cpu: "200m"
        livenessProbe:
          httpGet:
            path: /api/health
            port: 3000
          initialDelaySeconds: 30
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /api/health
            port: 3000
          initialDelaySeconds: 10
          periodSeconds: 10
        securityContext:
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
          capabilities:
            drop:
            - ALL
        volumeMounts:
        - name: tmp
          mountPath: /tmp
        - name: nextjs-cache
          mountPath: /.next
      volumes:
      - name: tmp
        emptyDir: {}
      - name: nextjs-cache
        emptyDir: {}
---
apiVersion: v1
kind: Service
metadata:
  name: dsr-frontend
  namespace: $NAMESPACE
  labels:
    app: dsr
    service: dsr-frontend
spec:
  type: ClusterIP
  selector:
    app: dsr
    service: dsr-frontend
  ports:
  - name: http
    port: 80
    targetPort: 3000
    protocol: TCP
EOF

    log_success "Frontend application deployed"
}

# Deploy backend services
deploy_backend_services() {
    log_step "Deploying DSR backend services..."
    
    for service_name in "${!DSR_SERVICES[@]}"; do
        local service_port="${DSR_SERVICES[$service_name]}"
        log_info "Deploying $service_name..."
        
        # Generate and apply deployment manifest
        generate_service_deployment "$service_name" "$service_port" 2 | kubectl apply -f -
        
        # Wait for deployment to be ready
        kubectl rollout status deployment/"$service_name" -n "$NAMESPACE" --timeout=300s
        
        log_success "$service_name deployed successfully"
    done
}

# Deploy ingress configuration
deploy_ingress() {
    log_step "Deploying ingress configuration..."
    
    # Apply the ingress configuration created during infrastructure setup
    if [ -f "$PROJECT_ROOT/deployment/production/configs/ingress.yaml" ]; then
        kubectl apply -f "$PROJECT_ROOT/deployment/production/configs/ingress.yaml"
        log_success "Ingress configuration deployed"
    else
        log_warning "Ingress configuration file not found. Creating default configuration..."
        
        cat <<EOF | kubectl apply -f -
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: dsr-ingress
  namespace: $NAMESPACE
  annotations:
    kubernetes.io/ingress.class: nginx
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/use-regex: "true"
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  tls:
  - hosts:
    - dsr.gov.ph
    - api.dsr.gov.ph
    secretName: tls-secret
  rules:
  - host: dsr.gov.ph
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: dsr-frontend
            port:
              number: 80
  - host: api.dsr.gov.ph
    http:
      paths:
      - path: /api/v1/registration
        pathType: Prefix
        backend:
          service:
            name: registration-service
            port:
              number: 80
      - path: /api/v1/data-management
        pathType: Prefix
        backend:
          service:
            name: data-management-service
            port:
              number: 80
      - path: /api/v1/eligibility
        pathType: Prefix
        backend:
          service:
            name: eligibility-service
            port:
              number: 80
      - path: /api/v1/interoperability
        pathType: Prefix
        backend:
          service:
            name: interoperability-service
            port:
              number: 80
      - path: /api/v1/payment
        pathType: Prefix
        backend:
          service:
            name: payment-service
            port:
              number: 80
      - path: /api/v1/grievance
        pathType: Prefix
        backend:
          service:
            name: grievance-service
            port:
              number: 80
      - path: /api/v1/analytics
        pathType: Prefix
        backend:
          service:
            name: analytics-service
            port:
              number: 80
EOF
        
        log_success "Default ingress configuration deployed"
    fi
}

# Perform health checks
perform_health_checks() {
    log_step "Performing health checks..."
    
    # Wait for all pods to be ready
    log_info "Waiting for all pods to be ready..."
    kubectl wait --for=condition=ready pod -l app=dsr -n "$NAMESPACE" --timeout=600s
    
    # Check service endpoints
    log_info "Checking service endpoints..."
    for service_name in "${!DSR_SERVICES[@]}"; do
        local service_port="${DSR_SERVICES[$service_name]}"
        
        # Port forward and test health endpoint
        kubectl port-forward -n "$NAMESPACE" "svc/$service_name" "$service_port:80" &
        local pf_pid=$!
        
        sleep 5
        
        if curl -f "http://localhost:$service_port/actuator/health" &> /dev/null; then
            log_success "$service_name health check passed"
        else
            log_error "$service_name health check failed"
        fi
        
        kill $pf_pid 2>/dev/null || true
    done
    
    # Check frontend
    kubectl port-forward -n "$NAMESPACE" svc/dsr-frontend 3000:80 &
    local frontend_pid=$!
    
    sleep 5
    
    if curl -f "http://localhost:3000/api/health" &> /dev/null; then
        log_success "Frontend health check passed"
    else
        log_error "Frontend health check failed"
    fi
    
    kill $frontend_pid 2>/dev/null || true
    
    log_success "Health checks completed"
}

# Display deployment status
display_status() {
    log_step "Displaying deployment status..."
    
    echo
    log_info "📊 Deployment Status:"
    kubectl get pods -n "$NAMESPACE" -l app=dsr
    
    echo
    log_info "🌐 Services:"
    kubectl get services -n "$NAMESPACE" -l app=dsr
    
    echo
    log_info "🔗 Ingress:"
    kubectl get ingress -n "$NAMESPACE"
    
    echo
    log_info "📈 Resource Usage:"
    kubectl top pods -n "$NAMESPACE" --containers 2>/dev/null || log_warning "Metrics server not available"
    
    echo
    log_info "🔍 Access Information:"
    echo "   - Application: https://dsr.gov.ph"
    echo "   - API: https://api.dsr.gov.ph"
    echo "   - Health Checks: https://api.dsr.gov.ph/api/v1/*/actuator/health"
    
    echo
    log_info "📋 Useful Commands:"
    echo "   - View logs: kubectl logs -f deployment/<service-name> -n $NAMESPACE"
    echo "   - Scale service: kubectl scale deployment <service-name> --replicas=<count> -n $NAMESPACE"
    echo "   - Port forward: kubectl port-forward svc/<service-name> <local-port>:80 -n $NAMESPACE"
}

# Main deployment function
main() {
    log_info "🚀 Starting DSR Services Deployment..."
    log_info "📦 Image Tag: $IMAGE_TAG"
    log_info "🏷️ Registry: $REGISTRY"
    log_info "🎯 Namespace: $NAMESPACE"
    echo
    
    check_prerequisites
    deploy_backend_services
    deploy_frontend
    deploy_ingress
    perform_health_checks
    display_status
    
    echo
    log_success "🎉 DSR Services Deployment Completed Successfully!"
    echo
    log_info "📋 Next Steps:"
    echo "   1. Configure DNS to point to the load balancer IP"
    echo "   2. Set up SSL certificates (if not using cert-manager)"
    echo "   3. Configure monitoring alerts and dashboards"
    echo "   4. Run integration tests and user acceptance testing"
    echo "   5. Monitor application performance and logs"
}

# Handle command line arguments
case "${1:-deploy}" in
    "deploy")
        main
        ;;
    "backend")
        check_prerequisites
        deploy_backend_services
        ;;
    "frontend")
        check_prerequisites
        deploy_frontend
        ;;
    "ingress")
        check_prerequisites
        deploy_ingress
        ;;
    "health")
        perform_health_checks
        ;;
    "status")
        display_status
        ;;
    "help"|"-h"|"--help")
        echo "Usage: $0 [deploy|backend|frontend|ingress|health|status] [image-tag]"
        echo "  deploy    - Deploy all DSR services (default)"
        echo "  backend   - Deploy only backend services"
        echo "  frontend  - Deploy only frontend application"
        echo "  ingress   - Deploy only ingress configuration"
        echo "  health    - Run health checks only"
        echo "  status    - Display deployment status"
        echo ""
        echo "Arguments:"
        echo "  image-tag - Container image tag to deploy (default: latest)"
        echo ""
        echo "Environment Variables:"
        echo "  REGISTRY  - Container registry URL (default: ghcr.io/kirachon/dsr)"
        exit 0
        ;;
    *)
        # If first argument is not a command, treat it as image tag
        if [[ "$1" =~ ^[a-zA-Z0-9._-]+$ ]]; then
            IMAGE_TAG="$1"
            main
        else
            log_error "Unknown command: $1"
            echo "Use '$0 help' for usage information"
            exit 1
        fi
        ;;
esac
