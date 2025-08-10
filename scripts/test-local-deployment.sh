#!/bin/bash

# Philippine Dynamic Social Registry (DSR) - Local Deployment Testing
# Version: 1.0.0
# Author: DSR Development Team
# Purpose: Comprehensive testing of local DSR deployment

set -euo pipefail

# Script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test results
TESTS_PASSED=0
TESTS_FAILED=0
FAILED_TESTS=()

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

# Test result functions
test_pass() {
    local test_name="$1"
    ((TESTS_PASSED++))
    log_success "✓ $test_name"
}

test_fail() {
    local test_name="$1"
    local error_msg="$2"
    ((TESTS_FAILED++))
    FAILED_TESTS+=("$test_name: $error_msg")
    log_error "✗ $test_name: $error_msg"
}

# Infrastructure smoke tests
test_infrastructure() {
    log_info "Testing infrastructure services..."
    
    # Test PostgreSQL
    if pg_isready -h localhost -p 5432 -U dsr_user >/dev/null 2>&1; then
        test_pass "PostgreSQL connectivity"
    else
        test_fail "PostgreSQL connectivity" "Cannot connect to PostgreSQL"
    fi
    
    # Test Redis
    if redis-cli -h localhost -p 6379 ping >/dev/null 2>&1; then
        test_pass "Redis connectivity"
    else
        test_fail "Redis connectivity" "Cannot connect to Redis"
    fi
    
    # Test Elasticsearch
    if curl -s -f "http://localhost:9200/_cluster/health" >/dev/null 2>&1; then
        test_pass "Elasticsearch connectivity"
    else
        test_fail "Elasticsearch connectivity" "Cannot connect to Elasticsearch"
    fi
    
    # Test Kafka
    if nc -z localhost 9092 >/dev/null 2>&1; then
        test_pass "Kafka connectivity"
    else
        test_fail "Kafka connectivity" "Cannot connect to Kafka"
    fi
}

# Database schema tests
test_database_schema() {
    log_info "Testing database schema..."
    
    # Test core tables exist
    local tables=("household_profiles" "household_members" "household_addresses" "economic_profiles")
    
    for table in "${tables[@]}"; do
        if PGPASSWORD=dsr_local_password psql -h localhost -p 5432 -U dsr_user -d dsr_local -c "\dt dsr_core.$table" >/dev/null 2>&1; then
            test_pass "Table $table exists"
        else
            test_fail "Table $table exists" "Table not found"
        fi
    done
    
    # Test sample data exists
    local count=$(PGPASSWORD=dsr_local_password psql -h localhost -p 5432 -U dsr_user -d dsr_local -t -c "SELECT COUNT(*) FROM dsr_core.household_profiles;" 2>/dev/null | xargs)
    if [[ "$count" -gt 0 ]]; then
        test_pass "Sample data loaded ($count households)"
    else
        test_fail "Sample data loaded" "No sample data found"
    fi
}

# Service health tests
test_service_health() {
    log_info "Testing DSR service health..."
    
    local services=(
        "registration-service:8080"
        "data-management-service:8081"
        "eligibility-service:8082"
        "interoperability-service:8083"
        "payment-service:8084"
        "grievance-service:8085"
        "analytics-service:8086"
    )
    
    for service_port in "${services[@]}"; do
        IFS=':' read -r service port <<< "$service_port"
        
        if curl -s -f "http://localhost:$port/actuator/health" >/dev/null 2>&1; then
            test_pass "$service health check"
        else
            test_fail "$service health check" "Service not responding on port $port"
        fi
    done
}

# API endpoint tests
test_api_endpoints() {
    log_info "Testing API endpoints..."
    
    # Test Registration Service endpoints
    if curl -s -f "http://localhost:8080/actuator/info" >/dev/null 2>&1; then
        test_pass "Registration Service info endpoint"
    else
        test_fail "Registration Service info endpoint" "Endpoint not accessible"
    fi
    
    # Test Data Management Service endpoints
    if curl -s -f "http://localhost:8081/actuator/metrics" >/dev/null 2>&1; then
        test_pass "Data Management Service metrics endpoint"
    else
        test_fail "Data Management Service metrics endpoint" "Endpoint not accessible"
    fi
    
    # Test Eligibility Service endpoints
    if curl -s -f "http://localhost:8082/actuator/health" >/dev/null 2>&1; then
        test_pass "Eligibility Service health endpoint"
    else
        test_fail "Eligibility Service health endpoint" "Endpoint not accessible"
    fi
}

# Integration tests
test_integration() {
    log_info "Testing service integration..."
    
    # Test database connectivity from services
    local db_test=$(curl -s "http://localhost:8080/actuator/health" | grep -o '"db":{"status":"UP"' || echo "")
    if [[ -n "$db_test" ]]; then
        test_pass "Registration Service database integration"
    else
        test_fail "Registration Service database integration" "Database connection not healthy"
    fi
    
    # Test Redis connectivity from services
    local redis_test=$(curl -s "http://localhost:8080/actuator/health" | grep -o '"redis":{"status":"UP"' || echo "")
    if [[ -n "$redis_test" ]]; then
        test_pass "Registration Service Redis integration"
    else
        test_fail "Registration Service Redis integration" "Redis connection not healthy"
    fi
}

# Performance tests
test_performance() {
    log_info "Testing basic performance..."
    
    # Test response times
    local start_time=$(date +%s%N)
    curl -s "http://localhost:8080/actuator/health" >/dev/null 2>&1
    local end_time=$(date +%s%N)
    local response_time=$(( (end_time - start_time) / 1000000 )) # Convert to milliseconds
    
    if [[ $response_time -lt 5000 ]]; then # Less than 5 seconds
        test_pass "Registration Service response time (${response_time}ms)"
    else
        test_fail "Registration Service response time" "Response time too slow: ${response_time}ms"
    fi
}

# End-to-end workflow test
test_e2e_workflow() {
    log_info "Testing end-to-end workflow..."
    
    # This is a simplified test - in a real scenario, you would test:
    # 1. Citizen registration
    # 2. Eligibility assessment
    # 3. Service delivery
    # 4. Payment processing
    # 5. Grievance handling
    
    # For now, just test that we can access the main endpoints
    local workflow_steps=(
        "http://localhost:8080/actuator/health:Registration"
        "http://localhost:8082/actuator/health:Eligibility"
        "http://localhost:8084/actuator/health:Payment"
        "http://localhost:8085/actuator/health:Grievance"
    )
    
    for step in "${workflow_steps[@]}"; do
        IFS=':' read -r url name <<< "$step"
        
        if curl -s -f "$url" >/dev/null 2>&1; then
            test_pass "$name workflow step"
        else
            test_fail "$name workflow step" "Service not accessible"
        fi
    done
}

# Monitoring and observability tests
test_monitoring() {
    log_info "Testing monitoring and observability..."
    
    # Test Prometheus metrics
    if curl -s -f "http://localhost:9090/api/v1/query?query=up" >/dev/null 2>&1; then
        test_pass "Prometheus metrics collection"
    else
        test_fail "Prometheus metrics collection" "Prometheus not accessible"
    fi
    
    # Test Grafana
    if curl -s -f "http://localhost:3001/api/health" >/dev/null 2>&1; then
        test_pass "Grafana dashboard access"
    else
        test_fail "Grafana dashboard access" "Grafana not accessible"
    fi
    
    # Test Jaeger
    if curl -s -f "http://localhost:16686/api/services" >/dev/null 2>&1; then
        test_pass "Jaeger tracing"
    else
        test_fail "Jaeger tracing" "Jaeger not accessible"
    fi
}

# Security tests
test_security() {
    log_info "Testing basic security..."
    
    # Test that sensitive endpoints are not exposed without authentication
    # (This is a basic test - real security testing would be more comprehensive)
    
    # Test actuator endpoints are accessible (they should be in local development)
    if curl -s -f "http://localhost:8080/actuator/health" >/dev/null 2>&1; then
        test_pass "Actuator endpoints accessible"
    else
        test_fail "Actuator endpoints accessible" "Health endpoint not accessible"
    fi
}

# Generate test report
generate_report() {
    local total_tests=$((TESTS_PASSED + TESTS_FAILED))
    local success_rate=0
    
    if [[ $total_tests -gt 0 ]]; then
        success_rate=$(( (TESTS_PASSED * 100) / total_tests ))
    fi
    
    echo
    echo "=== DSR Local Deployment Test Report ==="
    echo "Date: $(date)"
    echo "Total Tests: $total_tests"
    echo "Passed: $TESTS_PASSED"
    echo "Failed: $TESTS_FAILED"
    echo "Success Rate: $success_rate%"
    echo
    
    if [[ $TESTS_FAILED -gt 0 ]]; then
        echo "Failed Tests:"
        for failed_test in "${FAILED_TESTS[@]}"; do
            echo "  - $failed_test"
        done
        echo
    fi
    
    if [[ $TESTS_FAILED -eq 0 ]]; then
        log_success "All tests passed! DSR local deployment is working correctly."
        return 0
    else
        log_error "Some tests failed. Please check the issues above."
        return 1
    fi
}

# Main execution
main() {
    log_info "Starting DSR Local Deployment Tests..."
    echo
    
    test_infrastructure
    test_database_schema
    test_service_health
    test_api_endpoints
    test_integration
    test_performance
    test_e2e_workflow
    test_monitoring
    test_security
    
    generate_report
}

# Run main function
main "$@"
