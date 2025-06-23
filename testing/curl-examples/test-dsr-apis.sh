#!/bin/bash

# Philippine Dynamic Social Registry (DSR) - API Testing with cURL
# Version: 1.0.0
# Author: DSR Development Team
# Purpose: Test DSR APIs using cURL commands

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost"
REGISTRATION_PORT="8080"
DATA_MANAGEMENT_PORT="8081"
ELIGIBILITY_PORT="8082"
INTEROPERABILITY_PORT="8083"
PAYMENT_PORT="8084"
GRIEVANCE_PORT="8085"
ANALYTICS_PORT="8086"

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

# Test function
test_api() {
    local name="$1"
    local url="$2"
    local method="${3:-GET}"
    local data="${4:-}"
    local expected_status="${5:-200}"
    
    log_info "Testing: $name"
    
    local curl_cmd="curl -s -w '%{http_code}' -o /tmp/response.json"
    
    if [[ "$method" == "POST" && -n "$data" ]]; then
        curl_cmd="$curl_cmd -X POST -H 'Content-Type: application/json' -d '$data'"
    fi
    
    curl_cmd="$curl_cmd '$url'"
    
    local status_code=$(eval $curl_cmd)
    
    if [[ "$status_code" == "$expected_status" ]]; then
        log_success "$name - Status: $status_code"
        if [[ -f "/tmp/response.json" ]]; then
            echo "Response: $(cat /tmp/response.json | jq . 2>/dev/null || cat /tmp/response.json)"
        fi
    else
        log_error "$name - Expected: $expected_status, Got: $status_code"
        if [[ -f "/tmp/response.json" ]]; then
            echo "Response: $(cat /tmp/response.json)"
        fi
    fi
    
    echo
}

# Health check tests
test_health_checks() {
    log_info "=== Testing Health Checks ==="
    
    test_api "Registration Service Health" "$BASE_URL:$REGISTRATION_PORT/actuator/health"
    test_api "Data Management Service Health" "$BASE_URL:$DATA_MANAGEMENT_PORT/actuator/health"
    test_api "Eligibility Service Health" "$BASE_URL:$ELIGIBILITY_PORT/actuator/health"
    test_api "Interoperability Service Health" "$BASE_URL:$INTEROPERABILITY_PORT/actuator/health"
    test_api "Payment Service Health" "$BASE_URL:$PAYMENT_PORT/actuator/health"
    test_api "Grievance Service Health" "$BASE_URL:$GRIEVANCE_PORT/actuator/health"
    test_api "Analytics Service Health" "$BASE_URL:$ANALYTICS_PORT/actuator/health"
}

# Service info tests
test_service_info() {
    log_info "=== Testing Service Info ==="
    
    test_api "Registration Service Info" "$BASE_URL:$REGISTRATION_PORT/actuator/info"
    test_api "Data Management Service Info" "$BASE_URL:$DATA_MANAGEMENT_PORT/actuator/info"
    test_api "Eligibility Service Info" "$BASE_URL:$ELIGIBILITY_PORT/actuator/info"
}

# Metrics tests
test_metrics() {
    log_info "=== Testing Metrics ==="
    
    test_api "Registration Service Metrics" "$BASE_URL:$REGISTRATION_PORT/actuator/metrics"
    test_api "Data Management Service Metrics" "$BASE_URL:$DATA_MANAGEMENT_PORT/actuator/metrics"
    test_api "Eligibility Service Metrics" "$BASE_URL:$ELIGIBILITY_PORT/actuator/metrics"
}

# Registration workflow tests
test_registration_workflow() {
    log_info "=== Testing Registration Workflow ==="
    
    # Sample household registration data
    local registration_data='{
        "householdHead": {
            "firstName": "Juan",
            "lastName": "Dela Cruz",
            "middleName": "Santos",
            "birthDate": "1985-06-15",
            "gender": "male",
            "civilStatus": "married",
            "psn": "9999-1234-5678-9012"
        },
        "address": {
            "houseNumber": "123",
            "street": "Test Street",
            "barangay": "Barangay 1",
            "cityMunicipality": "Quezon City",
            "province": "Metro Manila",
            "postalCode": "1100"
        },
        "members": [
            {
                "firstName": "Maria",
                "lastName": "Dela Cruz",
                "birthDate": "1987-03-20",
                "gender": "female",
                "relationshipToHead": "spouse",
                "psn": "9999-1234-5678-9013"
            }
        ]
    }'
    
    # Test registration endpoint (this would normally return 201 for created)
    test_api "Create Household Registration" "$BASE_URL:$REGISTRATION_PORT/api/v1/registrations" "POST" "$registration_data" "201"
    
    # Test getting registration list
    test_api "Get Registrations List" "$BASE_URL:$REGISTRATION_PORT/api/v1/registrations"
}

# Eligibility assessment tests
test_eligibility_workflow() {
    log_info "=== Testing Eligibility Workflow ==="
    
    # Sample eligibility assessment data
    local assessment_data='{
        "householdId": "sample-household-id",
        "programName": "4Ps",
        "assessmentType": "full"
    }'
    
    test_api "Assess Household Eligibility" "$BASE_URL:$ELIGIBILITY_PORT/api/v1/eligibility/assess" "POST" "$assessment_data" "200"
    test_api "Get Program List" "$BASE_URL:$ELIGIBILITY_PORT/api/v1/eligibility/programs"
}

# Payment processing tests
test_payment_workflow() {
    log_info "=== Testing Payment Workflow ==="
    
    # Sample payment request data
    local payment_data='{
        "householdId": "sample-household-id",
        "programName": "4Ps",
        "amount": 1400.00,
        "paymentMethod": "bank_transfer",
        "beneficiaryAccount": {
            "accountNumber": "1234567890",
            "bankCode": "LBP",
            "accountName": "Juan Dela Cruz"
        }
    }'
    
    test_api "Create Payment Request" "$BASE_URL:$PAYMENT_PORT/api/v1/payments" "POST" "$payment_data" "201"
    test_api "Get Payment Methods" "$BASE_URL:$PAYMENT_PORT/api/v1/payments/methods"
}

# Grievance management tests
test_grievance_workflow() {
    log_info "=== Testing Grievance Workflow ==="
    
    # Sample grievance data
    local grievance_data='{
        "householdId": "sample-household-id",
        "grievanceType": "payment_issue",
        "subject": "Payment not received",
        "description": "I have not received my 4Ps payment for this month. Please investigate.",
        "priority": "medium",
        "contactMethod": "email",
        "contactDetails": "juan.delacruz@email.com"
    }'
    
    test_api "Submit Grievance" "$BASE_URL:$GRIEVANCE_PORT/api/v1/grievances" "POST" "$grievance_data" "201"
    test_api "Get Grievance Types" "$BASE_URL:$GRIEVANCE_PORT/api/v1/grievances/types"
}

# Analytics tests
test_analytics_workflow() {
    log_info "=== Testing Analytics Workflow ==="
    
    test_api "Get Dashboard Summary" "$BASE_URL:$ANALYTICS_PORT/api/v1/analytics/dashboard/summary"
    test_api "Get Program Statistics" "$BASE_URL:$ANALYTICS_PORT/api/v1/analytics/programs/statistics?program=4Ps&period=monthly"
    test_api "Get Registration Trends" "$BASE_URL:$ANALYTICS_PORT/api/v1/analytics/registrations/trends?period=weekly"
}

# Infrastructure tests
test_infrastructure_endpoints() {
    log_info "=== Testing Infrastructure Endpoints ==="
    
    # Test database connectivity through services
    test_api "Database Health via Registration Service" "$BASE_URL:$REGISTRATION_PORT/actuator/health/db"
    
    # Test Redis connectivity through services
    test_api "Redis Health via Registration Service" "$BASE_URL:$REGISTRATION_PORT/actuator/health/redis"
    
    # Test Kafka connectivity through services
    test_api "Kafka Health via Registration Service" "$BASE_URL:$REGISTRATION_PORT/actuator/health/kafka"
}

# Main execution
main() {
    log_info "Starting DSR API Tests with cURL..."
    echo
    
    # Check if jq is available for JSON formatting
    if ! command -v jq &> /dev/null; then
        log_warning "jq not found. JSON responses will not be formatted."
    fi
    
    test_health_checks
    test_service_info
    test_metrics
    test_infrastructure_endpoints
    test_registration_workflow
    test_eligibility_workflow
    test_payment_workflow
    test_grievance_workflow
    test_analytics_workflow
    
    log_success "DSR API testing completed!"
    log_info "Note: Some tests may fail if the corresponding API endpoints are not yet implemented."
    log_info "This is normal for a development environment."
}

# Show usage
show_usage() {
    echo "Usage: $0 [test_category]"
    echo
    echo "Test Categories:"
    echo "  health        Test health check endpoints"
    echo "  info          Test service info endpoints"
    echo "  metrics       Test metrics endpoints"
    echo "  registration  Test registration workflow"
    echo "  eligibility   Test eligibility workflow"
    echo "  payment       Test payment workflow"
    echo "  grievance     Test grievance workflow"
    echo "  analytics     Test analytics workflow"
    echo "  all           Run all tests (default)"
    echo
    echo "Examples:"
    echo "  $0                # Run all tests"
    echo "  $0 health         # Test only health endpoints"
    echo "  $0 registration   # Test only registration workflow"
}

# Handle command line arguments
case "${1:-all}" in
    "health")
        test_health_checks
        ;;
    "info")
        test_service_info
        ;;
    "metrics")
        test_metrics
        ;;
    "registration")
        test_registration_workflow
        ;;
    "eligibility")
        test_eligibility_workflow
        ;;
    "payment")
        test_payment_workflow
        ;;
    "grievance")
        test_grievance_workflow
        ;;
    "analytics")
        test_analytics_workflow
        ;;
    "all")
        main
        ;;
    "help"|"-h"|"--help")
        show_usage
        ;;
    *)
        log_error "Unknown test category: $1"
        show_usage
        exit 1
        ;;
esac
