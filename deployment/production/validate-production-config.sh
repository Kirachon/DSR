#!/bin/bash

# DSR Production Configuration Validation Script
# Comprehensive validation of production configuration files, environment variables,
# secrets management, and security settings across all 7 services

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
NAMESPACE="dsr-production"
VALIDATION_REPORT="$PROJECT_ROOT/reports/production-config-validation-$(date +%Y%m%d_%H%M%S).md"

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

# Validation results tracking
declare -A VALIDATION_RESULTS
TOTAL_CHECKS=0
PASSED_CHECKS=0
FAILED_CHECKS=0
WARNING_CHECKS=0

# Record validation result
record_result() {
    local check_name="$1"
    local status="$2"
    local message="$3"
    
    VALIDATION_RESULTS["$check_name"]="$status:$message"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    case "$status" in
        "PASS")
            PASSED_CHECKS=$((PASSED_CHECKS + 1))
            log_success "$check_name: $message"
            ;;
        "FAIL")
            FAILED_CHECKS=$((FAILED_CHECKS + 1))
            log_error "$check_name: $message"
            ;;
        "WARNING")
            WARNING_CHECKS=$((WARNING_CHECKS + 1))
            log_warning "$check_name: $message"
            ;;
    esac
}

# Initialize validation report
init_report() {
    cat > "$VALIDATION_REPORT" << EOF
# DSR Production Configuration Validation Report

**Generated:** $(date)  
**Environment:** Production  
**Namespace:** $NAMESPACE  
**Validation Script:** $0  

## Executive Summary

This report documents the validation of all production configuration files, environment variables, secrets management, and security settings for the DSR system deployment.

## Validation Results

EOF
}

# Validate Kubernetes cluster connectivity
validate_cluster_connectivity() {
    log_step "Validating Kubernetes cluster connectivity..."
    
    if kubectl cluster-info &> /dev/null; then
        local cluster_info=$(kubectl cluster-info | head -1)
        record_result "Cluster Connectivity" "PASS" "Successfully connected to Kubernetes cluster: $cluster_info"
    else
        record_result "Cluster Connectivity" "FAIL" "Cannot connect to Kubernetes cluster"
        return 1
    fi
    
    # Check cluster version
    local k8s_version=$(kubectl version --short 2>/dev/null | grep "Server Version" | cut -d' ' -f3)
    if [[ -n "$k8s_version" ]]; then
        record_result "Kubernetes Version" "PASS" "Kubernetes version: $k8s_version"
    else
        record_result "Kubernetes Version" "WARNING" "Could not determine Kubernetes version"
    fi
}

# Validate namespace and resources
validate_namespace() {
    log_step "Validating namespace and basic resources..."
    
    # Check namespace exists
    if kubectl get namespace "$NAMESPACE" &> /dev/null; then
        record_result "Namespace Existence" "PASS" "Namespace $NAMESPACE exists"
    else
        record_result "Namespace Existence" "FAIL" "Namespace $NAMESPACE does not exist"
        return 1
    fi
    
    # Check resource quotas
    if kubectl get resourcequota -n "$NAMESPACE" &> /dev/null; then
        local quota_info=$(kubectl get resourcequota -n "$NAMESPACE" -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
        if [[ -n "$quota_info" ]]; then
            record_result "Resource Quotas" "PASS" "Resource quotas configured: $quota_info"
        else
            record_result "Resource Quotas" "WARNING" "No resource quotas found"
        fi
    else
        record_result "Resource Quotas" "WARNING" "Could not check resource quotas"
    fi
    
    # Check network policies
    if kubectl get networkpolicy -n "$NAMESPACE" &> /dev/null; then
        local netpol_count=$(kubectl get networkpolicy -n "$NAMESPACE" --no-headers 2>/dev/null | wc -l)
        if [[ "$netpol_count" -gt 0 ]]; then
            record_result "Network Policies" "PASS" "Network policies configured: $netpol_count policies"
        else
            record_result "Network Policies" "WARNING" "No network policies found"
        fi
    else
        record_result "Network Policies" "WARNING" "Could not check network policies"
    fi
}

# Validate secrets and configuration
validate_secrets() {
    log_step "Validating secrets and configuration..."
    
    # Required secrets
    local required_secrets=("postgres-secret" "jwt-secret" "tls-secret")
    
    for secret in "${required_secrets[@]}"; do
        if kubectl get secret "$secret" -n "$NAMESPACE" &> /dev/null; then
            # Check secret has required keys
            local keys=$(kubectl get secret "$secret" -n "$NAMESPACE" -o jsonpath='{.data}' | jq -r 'keys[]' 2>/dev/null)
            if [[ -n "$keys" ]]; then
                record_result "Secret: $secret" "PASS" "Secret exists with keys: $(echo $keys | tr '\n' ' ')"
            else
                record_result "Secret: $secret" "WARNING" "Secret exists but could not verify keys"
            fi
        else
            record_result "Secret: $secret" "FAIL" "Required secret $secret not found"
        fi
    done
    
    # Check ConfigMaps
    if kubectl get configmap dsr-config -n "$NAMESPACE" &> /dev/null; then
        local config_keys=$(kubectl get configmap dsr-config -n "$NAMESPACE" -o jsonpath='{.data}' | jq -r 'keys[]' 2>/dev/null)
        if [[ -n "$config_keys" ]]; then
            record_result "ConfigMap: dsr-config" "PASS" "ConfigMap exists with keys: $(echo $config_keys | tr '\n' ' ')"
        else
            record_result "ConfigMap: dsr-config" "WARNING" "ConfigMap exists but could not verify keys"
        fi
    else
        record_result "ConfigMap: dsr-config" "FAIL" "Required ConfigMap dsr-config not found"
    fi
}

# Validate database configuration
validate_database() {
    log_step "Validating database configuration..."
    
    # Check PostgreSQL deployment
    if kubectl get deployment postgresql -n "$NAMESPACE" &> /dev/null; then
        local replicas=$(kubectl get deployment postgresql -n "$NAMESPACE" -o jsonpath='{.status.readyReplicas}' 2>/dev/null)
        if [[ "$replicas" -gt 0 ]]; then
            record_result "PostgreSQL Deployment" "PASS" "PostgreSQL deployment ready with $replicas replicas"
        else
            record_result "PostgreSQL Deployment" "FAIL" "PostgreSQL deployment not ready"
        fi
    else
        record_result "PostgreSQL Deployment" "FAIL" "PostgreSQL deployment not found"
    fi
    
    # Check PostgreSQL service
    if kubectl get service postgresql -n "$NAMESPACE" &> /dev/null; then
        local service_type=$(kubectl get service postgresql -n "$NAMESPACE" -o jsonpath='{.spec.type}' 2>/dev/null)
        record_result "PostgreSQL Service" "PASS" "PostgreSQL service exists (type: $service_type)"
    else
        record_result "PostgreSQL Service" "FAIL" "PostgreSQL service not found"
    fi
    
    # Check persistent volume claims
    if kubectl get pvc postgres-pvc -n "$NAMESPACE" &> /dev/null; then
        local pvc_status=$(kubectl get pvc postgres-pvc -n "$NAMESPACE" -o jsonpath='{.status.phase}' 2>/dev/null)
        if [[ "$pvc_status" == "Bound" ]]; then
            record_result "PostgreSQL PVC" "PASS" "PostgreSQL PVC is bound"
        else
            record_result "PostgreSQL PVC" "WARNING" "PostgreSQL PVC status: $pvc_status"
        fi
    else
        record_result "PostgreSQL PVC" "FAIL" "PostgreSQL PVC not found"
    fi
}

# Validate Redis configuration
validate_redis() {
    log_step "Validating Redis configuration..."
    
    # Check Redis deployment
    if kubectl get deployment redis-master -n "$NAMESPACE" &> /dev/null; then
        local replicas=$(kubectl get deployment redis-master -n "$NAMESPACE" -o jsonpath='{.status.readyReplicas}' 2>/dev/null)
        if [[ "$replicas" -gt 0 ]]; then
            record_result "Redis Deployment" "PASS" "Redis deployment ready with $replicas replicas"
        else
            record_result "Redis Deployment" "FAIL" "Redis deployment not ready"
        fi
    else
        record_result "Redis Deployment" "FAIL" "Redis deployment not found"
    fi
    
    # Check Redis service
    if kubectl get service redis-master -n "$NAMESPACE" &> /dev/null; then
        record_result "Redis Service" "PASS" "Redis service exists"
    else
        record_result "Redis Service" "FAIL" "Redis service not found"
    fi
}

# Validate service configurations
validate_service_configs() {
    log_step "Validating DSR service configurations..."
    
    local services=("registration-service" "data-management-service" "eligibility-service" 
                   "interoperability-service" "payment-service" "grievance-service" "analytics-service")
    
    for service in "${services[@]}"; do
        # Check deployment exists
        if kubectl get deployment "$service" -n "$NAMESPACE" &> /dev/null; then
            local replicas=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.status.readyReplicas}' 2>/dev/null)
            local desired=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.spec.replicas}' 2>/dev/null)
            
            if [[ "$replicas" == "$desired" ]] && [[ "$replicas" -gt 0 ]]; then
                record_result "Service: $service" "PASS" "Deployment ready ($replicas/$desired replicas)"
            else
                record_result "Service: $service" "WARNING" "Deployment not fully ready ($replicas/$desired replicas)"
            fi
        else
            record_result "Service: $service" "FAIL" "Deployment not found"
        fi
        
        # Check service exists
        if kubectl get service "$service" -n "$NAMESPACE" &> /dev/null; then
            local service_type=$(kubectl get service "$service" -n "$NAMESPACE" -o jsonpath='{.spec.type}' 2>/dev/null)
            record_result "Service: $service (svc)" "PASS" "Service exists (type: $service_type)"
        else
            record_result "Service: $service (svc)" "FAIL" "Service not found"
        fi
    done
}

# Validate environment variables
validate_environment_variables() {
    log_step "Validating environment variables..."
    
    local services=("registration-service" "data-management-service" "eligibility-service" 
                   "interoperability-service" "payment-service" "grievance-service" "analytics-service")
    
    for service in "${services[@]}"; do
        if kubectl get deployment "$service" -n "$NAMESPACE" &> /dev/null; then
            # Check required environment variables
            local env_vars=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.spec.template.spec.containers[0].env[*].name}' 2>/dev/null)
            
            local required_vars=("SPRING_PROFILES_ACTIVE" "DATABASE_URL" "DATABASE_USERNAME" "DATABASE_PASSWORD" "JWT_SECRET")
            local missing_vars=()
            
            for var in "${required_vars[@]}"; do
                if [[ ! "$env_vars" =~ $var ]]; then
                    missing_vars+=("$var")
                fi
            done
            
            if [[ ${#missing_vars[@]} -eq 0 ]]; then
                record_result "EnvVars: $service" "PASS" "All required environment variables configured"
            else
                record_result "EnvVars: $service" "FAIL" "Missing environment variables: ${missing_vars[*]}"
            fi
        fi
    done
}

# Validate resource limits and requests
validate_resources() {
    log_step "Validating resource limits and requests..."
    
    local services=("registration-service" "data-management-service" "eligibility-service" 
                   "interoperability-service" "payment-service" "grievance-service" "analytics-service")
    
    for service in "${services[@]}"; do
        if kubectl get deployment "$service" -n "$NAMESPACE" &> /dev/null; then
            # Check resource requests
            local cpu_request=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.spec.template.spec.containers[0].resources.requests.cpu}' 2>/dev/null)
            local memory_request=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.spec.template.spec.containers[0].resources.requests.memory}' 2>/dev/null)
            
            # Check resource limits
            local cpu_limit=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.spec.template.spec.containers[0].resources.limits.cpu}' 2>/dev/null)
            local memory_limit=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.spec.template.spec.containers[0].resources.limits.memory}' 2>/dev/null)
            
            if [[ -n "$cpu_request" && -n "$memory_request" && -n "$cpu_limit" && -n "$memory_limit" ]]; then
                record_result "Resources: $service" "PASS" "Resources configured (CPU: $cpu_request-$cpu_limit, Memory: $memory_request-$memory_limit)"
            else
                record_result "Resources: $service" "WARNING" "Incomplete resource configuration"
            fi
        fi
    done
}

# Validate health checks
validate_health_checks() {
    log_step "Validating health checks..."
    
    local services=("registration-service" "data-management-service" "eligibility-service" 
                   "interoperability-service" "payment-service" "grievance-service" "analytics-service")
    
    for service in "${services[@]}"; do
        if kubectl get deployment "$service" -n "$NAMESPACE" &> /dev/null; then
            # Check liveness probe
            local liveness_path=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.spec.template.spec.containers[0].livenessProbe.httpGet.path}' 2>/dev/null)
            
            # Check readiness probe
            local readiness_path=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.spec.template.spec.containers[0].readinessProbe.httpGet.path}' 2>/dev/null)
            
            if [[ -n "$liveness_path" && -n "$readiness_path" ]]; then
                record_result "HealthChecks: $service" "PASS" "Health checks configured (liveness: $liveness_path, readiness: $readiness_path)"
            else
                record_result "HealthChecks: $service" "FAIL" "Health checks not properly configured"
            fi
        fi
    done
}

# Validate security configurations
validate_security() {
    log_step "Validating security configurations..."
    
    local services=("registration-service" "data-management-service" "eligibility-service" 
                   "interoperability-service" "payment-service" "grievance-service" "analytics-service")
    
    for service in "${services[@]}"; do
        if kubectl get deployment "$service" -n "$NAMESPACE" &> /dev/null; then
            # Check security context
            local run_as_non_root=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.spec.template.spec.securityContext.runAsNonRoot}' 2>/dev/null)
            local run_as_user=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.spec.template.spec.securityContext.runAsUser}' 2>/dev/null)
            
            # Check container security context
            local allow_privilege_escalation=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.spec.template.spec.containers[0].securityContext.allowPrivilegeEscalation}' 2>/dev/null)
            local read_only_root_fs=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.spec.template.spec.containers[0].securityContext.readOnlyRootFilesystem}' 2>/dev/null)
            
            local security_score=0
            local security_details=()
            
            if [[ "$run_as_non_root" == "true" ]]; then
                security_score=$((security_score + 1))
                security_details+=("runAsNonRoot")
            fi
            
            if [[ -n "$run_as_user" && "$run_as_user" != "0" ]]; then
                security_score=$((security_score + 1))
                security_details+=("runAsUser:$run_as_user")
            fi
            
            if [[ "$allow_privilege_escalation" == "false" ]]; then
                security_score=$((security_score + 1))
                security_details+=("noPrivilegeEscalation")
            fi
            
            if [[ "$read_only_root_fs" == "true" ]]; then
                security_score=$((security_score + 1))
                security_details+=("readOnlyRootFS")
            fi
            
            if [[ $security_score -ge 3 ]]; then
                record_result "Security: $service" "PASS" "Good security configuration: ${security_details[*]}"
            elif [[ $security_score -ge 2 ]]; then
                record_result "Security: $service" "WARNING" "Partial security configuration: ${security_details[*]}"
            else
                record_result "Security: $service" "FAIL" "Insufficient security configuration"
            fi
        fi
    done
}

# Validate ingress configuration
validate_ingress() {
    log_step "Validating ingress configuration..."
    
    # Check ingress exists
    if kubectl get ingress dsr-ingress -n "$NAMESPACE" &> /dev/null; then
        # Check TLS configuration
        local tls_hosts=$(kubectl get ingress dsr-ingress -n "$NAMESPACE" -o jsonpath='{.spec.tls[*].hosts[*]}' 2>/dev/null)
        if [[ -n "$tls_hosts" ]]; then
            record_result "Ingress TLS" "PASS" "TLS configured for hosts: $tls_hosts"
        else
            record_result "Ingress TLS" "WARNING" "TLS not configured"
        fi
        
        # Check rules
        local rules_count=$(kubectl get ingress dsr-ingress -n "$NAMESPACE" -o jsonpath='{.spec.rules}' 2>/dev/null | jq length 2>/dev/null)
        if [[ "$rules_count" -gt 0 ]]; then
            record_result "Ingress Rules" "PASS" "Ingress rules configured: $rules_count rules"
        else
            record_result "Ingress Rules" "FAIL" "No ingress rules configured"
        fi
    else
        record_result "Ingress Configuration" "FAIL" "Ingress not found"
    fi
    
    # Check ingress controller
    if kubectl get pods -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx &> /dev/null; then
        local controller_pods=$(kubectl get pods -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx --no-headers 2>/dev/null | wc -l)
        if [[ "$controller_pods" -gt 0 ]]; then
            record_result "Ingress Controller" "PASS" "Ingress controller running: $controller_pods pods"
        else
            record_result "Ingress Controller" "FAIL" "Ingress controller not running"
        fi
    else
        record_result "Ingress Controller" "FAIL" "Ingress controller not found"
    fi
}

# Validate monitoring configuration
validate_monitoring() {
    log_step "Validating monitoring configuration..."
    
    # Check Prometheus
    if kubectl get pods -n monitoring -l app.kubernetes.io/name=prometheus &> /dev/null; then
        local prometheus_pods=$(kubectl get pods -n monitoring -l app.kubernetes.io/name=prometheus --no-headers 2>/dev/null | wc -l)
        if [[ "$prometheus_pods" -gt 0 ]]; then
            record_result "Prometheus" "PASS" "Prometheus running: $prometheus_pods pods"
        else
            record_result "Prometheus" "FAIL" "Prometheus not running"
        fi
    else
        record_result "Prometheus" "WARNING" "Prometheus not found"
    fi
    
    # Check Grafana
    if kubectl get pods -n monitoring -l app.kubernetes.io/name=grafana &> /dev/null; then
        local grafana_pods=$(kubectl get pods -n monitoring -l app.kubernetes.io/name=grafana --no-headers 2>/dev/null | wc -l)
        if [[ "$grafana_pods" -gt 0 ]]; then
            record_result "Grafana" "PASS" "Grafana running: $grafana_pods pods"
        else
            record_result "Grafana" "FAIL" "Grafana not running"
        fi
    else
        record_result "Grafana" "WARNING" "Grafana not found"
    fi
    
    # Check ServiceMonitors
    local servicemonitor_count=$(kubectl get servicemonitor -n "$NAMESPACE" --no-headers 2>/dev/null | wc -l)
    if [[ "$servicemonitor_count" -gt 0 ]]; then
        record_result "ServiceMonitors" "PASS" "ServiceMonitors configured: $servicemonitor_count"
    else
        record_result "ServiceMonitors" "WARNING" "No ServiceMonitors found"
    fi
}

# Generate validation report
generate_report() {
    log_step "Generating validation report..."
    
    cat >> "$VALIDATION_REPORT" << EOF

### Summary Statistics

- **Total Checks:** $TOTAL_CHECKS
- **Passed:** $PASSED_CHECKS ($(( PASSED_CHECKS * 100 / TOTAL_CHECKS ))%)
- **Failed:** $FAILED_CHECKS ($(( FAILED_CHECKS * 100 / TOTAL_CHECKS ))%)
- **Warnings:** $WARNING_CHECKS ($(( WARNING_CHECKS * 100 / TOTAL_CHECKS ))%)

### Overall Status

EOF

    if [[ $FAILED_CHECKS -eq 0 ]]; then
        echo "✅ **PRODUCTION READY** - All critical checks passed" >> "$VALIDATION_REPORT"
    elif [[ $FAILED_CHECKS -le 2 ]]; then
        echo "⚠️ **CONDITIONAL READY** - Minor issues need attention" >> "$VALIDATION_REPORT"
    else
        echo "❌ **NOT READY** - Critical issues must be resolved" >> "$VALIDATION_REPORT"
    fi
    
    cat >> "$VALIDATION_REPORT" << EOF

### Detailed Results

| Check | Status | Details |
|-------|--------|---------|
EOF

    for check in "${!VALIDATION_RESULTS[@]}"; do
        local status_msg="${VALIDATION_RESULTS[$check]}"
        local status="${status_msg%%:*}"
        local message="${status_msg#*:}"
        
        local status_icon
        case "$status" in
            "PASS") status_icon="✅" ;;
            "FAIL") status_icon="❌" ;;
            "WARNING") status_icon="⚠️" ;;
        esac
        
        echo "| $check | $status_icon $status | $message |" >> "$VALIDATION_REPORT"
    done
    
    cat >> "$VALIDATION_REPORT" << EOF

### Recommendations

EOF

    if [[ $FAILED_CHECKS -gt 0 ]]; then
        echo "#### Critical Issues to Resolve:" >> "$VALIDATION_REPORT"
        for check in "${!VALIDATION_RESULTS[@]}"; do
            local status_msg="${VALIDATION_RESULTS[$check]}"
            local status="${status_msg%%:*}"
            if [[ "$status" == "FAIL" ]]; then
                echo "- **$check:** ${status_msg#*:}" >> "$VALIDATION_REPORT"
            fi
        done
        echo "" >> "$VALIDATION_REPORT"
    fi
    
    if [[ $WARNING_CHECKS -gt 0 ]]; then
        echo "#### Warnings to Address:" >> "$VALIDATION_REPORT"
        for check in "${!VALIDATION_RESULTS[@]}"; do
            local status_msg="${VALIDATION_RESULTS[$check]}"
            local status="${status_msg%%:*}"
            if [[ "$status" == "WARNING" ]]; then
                echo "- **$check:** ${status_msg#*:}" >> "$VALIDATION_REPORT"
            fi
        done
        echo "" >> "$VALIDATION_REPORT"
    fi
    
    cat >> "$VALIDATION_REPORT" << EOF

### Next Steps

1. **Address Critical Issues:** Resolve all failed checks before production deployment
2. **Review Warnings:** Consider addressing warnings for optimal configuration
3. **Run Health Checks:** Execute comprehensive health checks after fixes
4. **Update Documentation:** Update deployment documentation with any changes
5. **Schedule Deployment:** Proceed with production deployment once all issues are resolved

---

**Report Generated:** $(date)  
**Validation Script:** $0  
**Total Runtime:** $SECONDS seconds
EOF

    log_success "Validation report generated: $VALIDATION_REPORT"
}

# Main validation function
main() {
    log_info "🔍 Starting DSR Production Configuration Validation..."
    log_info "📊 Namespace: $NAMESPACE"
    log_info "📄 Report: $VALIDATION_REPORT"
    echo
    
    init_report
    
    validate_cluster_connectivity
    validate_namespace
    validate_secrets
    validate_database
    validate_redis
    validate_service_configs
    validate_environment_variables
    validate_resources
    validate_health_checks
    validate_security
    validate_ingress
    validate_monitoring
    
    generate_report
    
    echo
    log_info "📊 Validation Summary:"
    log_info "   Total Checks: $TOTAL_CHECKS"
    log_success "   Passed: $PASSED_CHECKS"
    log_error "   Failed: $FAILED_CHECKS"
    log_warning "   Warnings: $WARNING_CHECKS"
    echo
    
    if [[ $FAILED_CHECKS -eq 0 ]]; then
        log_success "🎉 Production configuration validation PASSED!"
        log_info "✅ System is ready for production deployment"
    elif [[ $FAILED_CHECKS -le 2 ]]; then
        log_warning "⚠️ Production configuration validation CONDITIONAL"
        log_info "🔧 Address minor issues before deployment"
    else
        log_error "❌ Production configuration validation FAILED"
        log_info "🚨 Critical issues must be resolved before deployment"
        exit 1
    fi
    
    echo
    log_info "📄 Detailed report available at: $VALIDATION_REPORT"
}

# Handle command line arguments
case "${1:-validate}" in
    "validate")
        main
        ;;
    "cluster")
        init_report
        validate_cluster_connectivity
        generate_report
        ;;
    "secrets")
        init_report
        validate_secrets
        generate_report
        ;;
    "services")
        init_report
        validate_service_configs
        generate_report
        ;;
    "security")
        init_report
        validate_security
        generate_report
        ;;
    "help"|"-h"|"--help")
        echo "Usage: $0 [validate|cluster|secrets|services|security]"
        echo "  validate  - Run complete configuration validation (default)"
        echo "  cluster   - Validate cluster connectivity only"
        echo "  secrets   - Validate secrets and configuration only"
        echo "  services  - Validate service configurations only"
        echo "  security  - Validate security configurations only"
        exit 0
        ;;
    *)
        log_error "Unknown command: $1"
        echo "Use '$0 help' for usage information"
        exit 1
        ;;
esac
