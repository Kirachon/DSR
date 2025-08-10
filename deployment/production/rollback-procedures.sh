#!/bin/bash

# DSR Production Rollback Procedures Script
# Comprehensive rollback capabilities for production deployment
# Includes service rollback, database rollback, and configuration rollback

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
NAMESPACE="dsr-production"
BACKUP_NAMESPACE="dsr-backup"
ROLLBACK_REASON="${ROLLBACK_REASON:-Manual rollback requested}"

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

# DSR Services
DSR_SERVICES=("registration-service" "data-management-service" "eligibility-service" 
              "interoperability-service" "payment-service" "grievance-service" "analytics-service")

# Check prerequisites for rollback
check_rollback_prerequisites() {
    log_step "Checking rollback prerequisites..."
    
    # Check kubectl connection
    if ! kubectl cluster-info &> /dev/null; then
        log_error "Cannot connect to Kubernetes cluster"
        exit 1
    fi
    
    # Check production namespace exists
    if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
        log_error "Production namespace $NAMESPACE does not exist"
        exit 1
    fi
    
    # Check if backup namespace exists
    if ! kubectl get namespace "$BACKUP_NAMESPACE" &> /dev/null; then
        log_warning "Backup namespace $BACKUP_NAMESPACE does not exist"
        log_info "Creating backup namespace for rollback operations..."
        kubectl create namespace "$BACKUP_NAMESPACE"
    fi
    
    log_success "Rollback prerequisites check completed"
}

# Create backup of current state before rollback
create_pre_rollback_backup() {
    log_step "Creating pre-rollback backup..."
    
    local backup_timestamp=$(date +%Y%m%d_%H%M%S)
    local backup_dir="$PROJECT_ROOT/backups/pre-rollback-$backup_timestamp"
    
    mkdir -p "$backup_dir"
    
    # Backup deployments
    for service in "${DSR_SERVICES[@]}"; do
        if kubectl get deployment "$service" -n "$NAMESPACE" &> /dev/null; then
            kubectl get deployment "$service" -n "$NAMESPACE" -o yaml > "$backup_dir/$service-deployment.yaml"
            log_info "Backed up deployment: $service"
        fi
    done
    
    # Backup services
    kubectl get services -n "$NAMESPACE" -l app=dsr -o yaml > "$backup_dir/services.yaml"
    
    # Backup configmaps
    kubectl get configmaps -n "$NAMESPACE" -o yaml > "$backup_dir/configmaps.yaml"
    
    # Backup ingress
    kubectl get ingress -n "$NAMESPACE" -o yaml > "$backup_dir/ingress.yaml"
    
    # Create backup metadata
    cat > "$backup_dir/backup-metadata.json" << EOF
{
  "timestamp": "$backup_timestamp",
  "namespace": "$NAMESPACE",
  "reason": "Pre-rollback backup",
  "services": [$(printf '"%s",' "${DSR_SERVICES[@]}" | sed 's/,$//')]
}
EOF
    
    log_success "Pre-rollback backup created: $backup_dir"
    echo "$backup_dir" > /tmp/rollback_backup_path
}

# Get previous deployment version
get_previous_version() {
    local service="$1"
    
    # Try to get previous version from deployment annotations
    local previous_version=$(kubectl get deployment "$service" -n "$NAMESPACE" \
        -o jsonpath='{.metadata.annotations.deployment\.kubernetes\.io/revision}' 2>/dev/null)
    
    if [[ -n "$previous_version" && "$previous_version" -gt 1 ]]; then
        echo $((previous_version - 1))
    else
        echo "1"
    fi
}

# Rollback service deployment
rollback_service_deployment() {
    local service="$1"
    local target_revision="${2:-}"
    
    log_info "Rolling back $service deployment..."
    
    # Check if deployment exists
    if ! kubectl get deployment "$service" -n "$NAMESPACE" &> /dev/null; then
        log_error "Deployment $service not found in namespace $NAMESPACE"
        return 1
    fi
    
    # Get current revision
    local current_revision=$(kubectl get deployment "$service" -n "$NAMESPACE" \
        -o jsonpath='{.metadata.annotations.deployment\.kubernetes\.io/revision}' 2>/dev/null)
    
    # If no target revision specified, rollback to previous
    if [[ -z "$target_revision" ]]; then
        target_revision=$(get_previous_version "$service")
    fi
    
    log_info "Rolling back $service from revision $current_revision to revision $target_revision"
    
    # Perform rollback
    if kubectl rollout undo deployment/"$service" -n "$NAMESPACE" --to-revision="$target_revision"; then
        log_info "Rollback initiated for $service"
        
        # Wait for rollback to complete
        if kubectl rollout status deployment/"$service" -n "$NAMESPACE" --timeout=300s; then
            log_success "Rollback completed for $service"
            return 0
        else
            log_error "Rollback failed for $service - timeout waiting for rollout"
            return 1
        fi
    else
        log_error "Failed to initiate rollback for $service"
        return 1
    fi
}

# Rollback all services
rollback_all_services() {
    log_step "Rolling back all DSR services..."
    
    local failed_rollbacks=()
    local successful_rollbacks=()
    
    # Rollback services in reverse dependency order
    local rollback_order=("analytics-service" "grievance-service" "payment-service" 
                         "interoperability-service" "eligibility-service" 
                         "data-management-service" "registration-service")
    
    for service in "${rollback_order[@]}"; do
        if rollback_service_deployment "$service"; then
            successful_rollbacks+=("$service")
        else
            failed_rollbacks+=("$service")
        fi
        
        # Wait between rollbacks to avoid overwhelming the cluster
        sleep 10
    done
    
    # Report results
    if [[ ${#failed_rollbacks[@]} -eq 0 ]]; then
        log_success "All services rolled back successfully: ${successful_rollbacks[*]}"
    else
        log_error "Some services failed to rollback: ${failed_rollbacks[*]}"
        log_info "Successfully rolled back: ${successful_rollbacks[*]}"
        return 1
    fi
}

# Rollback database changes
rollback_database() {
    log_step "Rolling back database changes..."
    
    local backup_file="${1:-}"
    
    if [[ -z "$backup_file" ]]; then
        log_warning "No database backup file specified"
        log_info "Skipping database rollback"
        return 0
    fi
    
    if [[ ! -f "$backup_file" ]]; then
        log_error "Database backup file not found: $backup_file"
        return 1
    fi
    
    log_info "Restoring database from backup: $backup_file"
    
    # Create a temporary pod for database operations
    cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Pod
metadata:
  name: db-rollback-pod
  namespace: $NAMESPACE
spec:
  containers:
  - name: postgres-client
    image: postgres:13
    command: ['sleep', '3600']
    env:
    - name: PGPASSWORD
      valueFrom:
        secretKeyRef:
          name: postgres-secret
          key: postgres-password
  restartPolicy: Never
EOF
    
    # Wait for pod to be ready
    kubectl wait --for=condition=ready pod/db-rollback-pod -n "$NAMESPACE" --timeout=60s
    
    # Copy backup file to pod
    kubectl cp "$backup_file" "$NAMESPACE/db-rollback-pod:/tmp/backup.sql"
    
    # Restore database
    if kubectl exec -n "$NAMESPACE" db-rollback-pod -- \
        psql -h postgresql -U postgres -d dsr_db -f /tmp/backup.sql; then
        log_success "Database rollback completed"
    else
        log_error "Database rollback failed"
        kubectl delete pod db-rollback-pod -n "$NAMESPACE" --ignore-not-found
        return 1
    fi
    
    # Cleanup
    kubectl delete pod db-rollback-pod -n "$NAMESPACE" --ignore-not-found
}

# Rollback configuration
rollback_configuration() {
    log_step "Rolling back configuration..."
    
    local config_backup_dir="${1:-}"
    
    if [[ -z "$config_backup_dir" || ! -d "$config_backup_dir" ]]; then
        log_warning "No configuration backup directory specified or found"
        log_info "Skipping configuration rollback"
        return 0
    fi
    
    # Rollback ConfigMaps
    if [[ -f "$config_backup_dir/configmaps.yaml" ]]; then
        log_info "Rolling back ConfigMaps..."
        kubectl apply -f "$config_backup_dir/configmaps.yaml"
    fi
    
    # Rollback Ingress
    if [[ -f "$config_backup_dir/ingress.yaml" ]]; then
        log_info "Rolling back Ingress configuration..."
        kubectl apply -f "$config_backup_dir/ingress.yaml"
    fi
    
    # Rollback Services (if needed)
    if [[ -f "$config_backup_dir/services.yaml" ]]; then
        log_info "Rolling back Services configuration..."
        kubectl apply -f "$config_backup_dir/services.yaml"
    fi
    
    log_success "Configuration rollback completed"
}

# Verify rollback success
verify_rollback() {
    log_step "Verifying rollback success..."
    
    local verification_failed=false
    
    # Check all services are running
    for service in "${DSR_SERVICES[@]}"; do
        local ready_replicas=$(kubectl get deployment "$service" -n "$NAMESPACE" \
            -o jsonpath='{.status.readyReplicas}' 2>/dev/null)
        local desired_replicas=$(kubectl get deployment "$service" -n "$NAMESPACE" \
            -o jsonpath='{.spec.replicas}' 2>/dev/null)
        
        if [[ "$ready_replicas" == "$desired_replicas" && "$ready_replicas" -gt 0 ]]; then
            log_success "$service: $ready_replicas/$desired_replicas replicas ready"
        else
            log_error "$service: $ready_replicas/$desired_replicas replicas ready"
            verification_failed=true
        fi
    done
    
    # Test health endpoints
    log_info "Testing service health endpoints..."
    
    for service in "${DSR_SERVICES[@]}"; do
        # Port forward and test health endpoint
        kubectl port-forward -n "$NAMESPACE" "svc/$service" 8080:80 &
        local pf_pid=$!
        
        sleep 3
        
        if curl -f -s "http://localhost:8080/actuator/health" > /dev/null 2>&1; then
            log_success "$service health check passed"
        else
            log_error "$service health check failed"
            verification_failed=true
        fi
        
        kill $pf_pid 2>/dev/null || true
        sleep 1
    done
    
    if [[ "$verification_failed" == "true" ]]; then
        log_error "Rollback verification failed"
        return 1
    else
        log_success "Rollback verification passed"
        return 0
    fi
}

# Generate rollback report
generate_rollback_report() {
    log_step "Generating rollback report..."
    
    local rollback_timestamp=$(date +%Y%m%d_%H%M%S)
    local report_file="$PROJECT_ROOT/reports/rollback-report-$rollback_timestamp.md"
    
    cat > "$report_file" << EOF
# DSR Production Rollback Report

**Generated:** $(date)  
**Rollback Timestamp:** $rollback_timestamp  
**Namespace:** $NAMESPACE  
**Reason:** $ROLLBACK_REASON  

## Rollback Summary

This report documents the rollback of the DSR production deployment.

### Services Rolled Back

| Service | Status | Details |
|---------|--------|---------|
EOF

    for service in "${DSR_SERVICES[@]}"; do
        local current_revision=$(kubectl get deployment "$service" -n "$NAMESPACE" \
            -o jsonpath='{.metadata.annotations.deployment\.kubernetes\.io/revision}' 2>/dev/null)
        local ready_replicas=$(kubectl get deployment "$service" -n "$NAMESPACE" \
            -o jsonpath='{.status.readyReplicas}' 2>/dev/null)
        local desired_replicas=$(kubectl get deployment "$service" -n "$NAMESPACE" \
            -o jsonpath='{.spec.replicas}' 2>/dev/null)
        
        if [[ "$ready_replicas" == "$desired_replicas" ]]; then
            echo "| $service | ✅ Success | Revision $current_revision, $ready_replicas/$desired_replicas replicas |" >> "$report_file"
        else
            echo "| $service | ❌ Failed | Revision $current_revision, $ready_replicas/$desired_replicas replicas |" >> "$report_file"
        fi
    done
    
    cat >> "$report_file" << EOF

### Rollback Timeline

1. **Pre-rollback backup created:** $(date)
2. **Service rollback initiated:** $(date)
3. **Configuration rollback completed:** $(date)
4. **Verification completed:** $(date)

### Post-Rollback Status

- **All services running:** $(if verify_rollback &>/dev/null; then echo "✅ Yes"; else echo "❌ No"; fi)
- **Health checks passing:** $(if verify_rollback &>/dev/null; then echo "✅ Yes"; else echo "❌ No"; fi)
- **System operational:** $(if verify_rollback &>/dev/null; then echo "✅ Yes"; else echo "❌ No"; fi)

### Next Steps

1. **Investigate root cause** of the issue that triggered the rollback
2. **Fix identified issues** in the development environment
3. **Test thoroughly** before attempting another deployment
4. **Update deployment procedures** if necessary
5. **Schedule next deployment** when issues are resolved

### Backup Information

- **Pre-rollback backup:** $(cat /tmp/rollback_backup_path 2>/dev/null || echo "Not available")
- **Database backup:** Available if specified during rollback
- **Configuration backup:** Included in pre-rollback backup

---

**Report Generated:** $(date)  
**Rollback Duration:** $SECONDS seconds  
**Status:** $(if verify_rollback &>/dev/null; then echo "SUCCESSFUL"; else echo "REQUIRES ATTENTION"; fi)
EOF

    log_success "Rollback report generated: $report_file"
}

# Emergency rollback (fast rollback without extensive verification)
emergency_rollback() {
    log_step "Executing emergency rollback..."
    
    log_warning "Emergency rollback initiated - minimal verification"
    
    # Quick rollback of critical services only
    local critical_services=("registration-service" "payment-service" "eligibility-service")
    
    for service in "${critical_services[@]}"; do
        log_info "Emergency rollback: $service"
        kubectl rollout undo deployment/"$service" -n "$NAMESPACE" --to-revision=1
    done
    
    # Quick health check
    sleep 30
    
    for service in "${critical_services[@]}"; do
        local ready_replicas=$(kubectl get deployment "$service" -n "$NAMESPACE" \
            -o jsonpath='{.status.readyReplicas}' 2>/dev/null)
        
        if [[ "$ready_replicas" -gt 0 ]]; then
            log_success "Emergency rollback: $service is running"
        else
            log_error "Emergency rollback: $service failed to start"
        fi
    done
    
    log_warning "Emergency rollback completed - full verification recommended"
}

# Main rollback function
main() {
    local rollback_type="${1:-full}"
    local database_backup="${2:-}"
    local config_backup="${3:-}"
    
    log_info "🔄 Starting DSR Production Rollback..."
    log_info "📋 Rollback Type: $rollback_type"
    log_info "🎯 Namespace: $NAMESPACE"
    log_info "📝 Reason: $ROLLBACK_REASON"
    echo
    
    case "$rollback_type" in
        "emergency")
            emergency_rollback
            ;;
        "services")
            check_rollback_prerequisites
            create_pre_rollback_backup
            rollback_all_services
            verify_rollback
            generate_rollback_report
            ;;
        "database")
            check_rollback_prerequisites
            rollback_database "$database_backup"
            ;;
        "config")
            check_rollback_prerequisites
            rollback_configuration "$config_backup"
            ;;
        "full")
            check_rollback_prerequisites
            create_pre_rollback_backup
            rollback_all_services
            rollback_configuration "$config_backup"
            if [[ -n "$database_backup" ]]; then
                rollback_database "$database_backup"
            fi
            verify_rollback
            generate_rollback_report
            ;;
        *)
            log_error "Unknown rollback type: $rollback_type"
            exit 1
            ;;
    esac
    
    echo
    if verify_rollback &>/dev/null; then
        log_success "🎉 Rollback completed successfully!"
        log_info "✅ System is operational"
    else
        log_error "❌ Rollback completed with issues"
        log_info "🚨 Manual intervention may be required"
    fi
}

# Handle command line arguments
case "${1:-help}" in
    "full")
        main "full" "$2" "$3"
        ;;
    "services")
        main "services"
        ;;
    "database")
        main "database" "$2"
        ;;
    "config")
        main "config" "$2"
        ;;
    "emergency")
        main "emergency"
        ;;
    "verify")
        verify_rollback
        ;;
    "help"|"-h"|"--help")
        echo "Usage: $0 [full|services|database|config|emergency|verify] [options]"
        echo ""
        echo "Rollback Types:"
        echo "  full      - Complete rollback (services + config + database)"
        echo "  services  - Rollback services only"
        echo "  database  - Rollback database only (requires backup file)"
        echo "  config    - Rollback configuration only (requires backup dir)"
        echo "  emergency - Fast rollback of critical services"
        echo "  verify    - Verify current system status"
        echo ""
        echo "Options:"
        echo "  database backup file - Path to database backup file"
        echo "  config backup dir   - Path to configuration backup directory"
        echo ""
        echo "Environment Variables:"
        echo "  ROLLBACK_REASON - Reason for rollback (default: Manual rollback requested)"
        echo ""
        echo "Examples:"
        echo "  $0 full /path/to/db_backup.sql /path/to/config_backup/"
        echo "  $0 services"
        echo "  $0 emergency"
        exit 0
        ;;
    *)
        log_error "Unknown command: $1"
        echo "Use '$0 help' for usage information"
        exit 1
        ;;
esac
