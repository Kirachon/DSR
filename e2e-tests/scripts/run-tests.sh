#!/bin/bash

# DSR E2E Test Runner Script
# Provides convenient commands for running different test scenarios

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
MOCK_FRONTEND_DIR="$PROJECT_ROOT/mock-frontend"

# Default values
BROWSER="chromium"
HEADLESS="true"
WORKERS="1"
RETRIES="2"
TIMEOUT="30000"
ENVIRONMENT="local"

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

# Help function
show_help() {
    cat << EOF
DSR E2E Test Runner

Usage: $0 [COMMAND] [OPTIONS]

COMMANDS:
    setup           Install dependencies and browsers
    start-frontend  Start mock frontend server
    stop-frontend   Stop mock frontend server
    health-check    Check if services are running
    test            Run tests (default)
    smoke           Run smoke tests only
    regression      Run full regression suite
    account         Run account functionality tests
    modules         Run module tests
    integration     Run integration tests
    clean           Clean up test artifacts
    report          Generate and open test reports

OPTIONS:
    --browser BROWSER       Browser to use (chromium, firefox, webkit, mobile-chrome)
    --headed               Run tests in headed mode
    --workers NUM          Number of parallel workers
    --retries NUM          Number of retries on failure
    --timeout MS           Test timeout in milliseconds
    --env ENVIRONMENT      Environment (local, staging, production)
    --debug                Run in debug mode
    --ui                   Run in UI mode
    --help                 Show this help message

EXAMPLES:
    $0 setup                           # Install dependencies
    $0 test --browser firefox --headed # Run tests in Firefox with UI
    $0 smoke --workers 2               # Run smoke tests with 2 workers
    $0 account --debug                 # Debug account tests
    $0 modules --env staging           # Run module tests against staging

ENVIRONMENT VARIABLES:
    BASE_URL                Base URL for the application
    API_BASE_URL           Base URL for API services
    HEADLESS               Run in headless mode (true/false)
    CI                     Running in CI environment (true/false)

EOF
}

# Parse command line arguments
parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            --browser)
                BROWSER="$2"
                shift 2
                ;;
            --headed)
                HEADLESS="false"
                shift
                ;;
            --workers)
                WORKERS="$2"
                shift 2
                ;;
            --retries)
                RETRIES="$2"
                shift 2
                ;;
            --timeout)
                TIMEOUT="$2"
                shift 2
                ;;
            --env)
                ENVIRONMENT="$2"
                shift 2
                ;;
            --debug)
                DEBUG="true"
                shift
                ;;
            --ui)
                UI_MODE="true"
                shift
                ;;
            --help)
                show_help
                exit 0
                ;;
            *)
                break
                ;;
        esac
    done
}

# Setup function
setup() {
    log_info "Setting up DSR E2E Test Suite..."
    
    cd "$PROJECT_ROOT"
    
    # Install Node.js dependencies
    log_info "Installing Node.js dependencies..."
    npm ci
    
    # Install Playwright browsers
    log_info "Installing Playwright browsers..."
    npx playwright install --with-deps
    
    # Setup mock frontend
    log_info "Setting up mock frontend..."
    cd "$MOCK_FRONTEND_DIR"
    npm ci
    
    # Create environment file if it doesn't exist
    if [[ ! -f "$PROJECT_ROOT/.env" ]]; then
        log_info "Creating environment file..."
        cp "$PROJECT_ROOT/.env.example" "$PROJECT_ROOT/.env"
    fi
    
    log_success "Setup completed successfully!"
}

# Start mock frontend server
start_frontend() {
    log_info "Starting mock frontend server..."
    
    cd "$MOCK_FRONTEND_DIR"
    
    # Check if server is already running
    if curl -s http://localhost:3000/health > /dev/null 2>&1; then
        log_warning "Mock frontend server is already running"
        return 0
    fi
    
    # Start server in background
    npm start > frontend.log 2>&1 &
    FRONTEND_PID=$!
    
    # Save PID for later cleanup
    echo $FRONTEND_PID > frontend.pid
    
    # Wait for server to start
    log_info "Waiting for frontend server to start..."
    for i in {1..30}; do
        if curl -s http://localhost:3000/health > /dev/null 2>&1; then
            log_success "Mock frontend server started successfully (PID: $FRONTEND_PID)"
            return 0
        fi
        sleep 1
    done
    
    log_error "Failed to start mock frontend server"
    return 1
}

# Stop mock frontend server
stop_frontend() {
    log_info "Stopping mock frontend server..."
    
    cd "$MOCK_FRONTEND_DIR"
    
    if [[ -f frontend.pid ]]; then
        PID=$(cat frontend.pid)
        if kill -0 $PID 2>/dev/null; then
            kill $PID
            rm frontend.pid
            log_success "Mock frontend server stopped"
        else
            log_warning "Frontend server was not running"
            rm -f frontend.pid
        fi
    else
        log_warning "No frontend PID file found"
    fi
}

# Health check function
health_check() {
    log_info "Checking service health..."
    
    local services=(
        "Mock Frontend:http://localhost:3000/health"
        "Registration Service:http://localhost:8080/actuator/health"
        "Data Management Service:http://localhost:8081/actuator/health"
        "Eligibility Service:http://localhost:8082/actuator/health"
        "Interoperability Service:http://localhost:8083/actuator/health"
        "Payment Service:http://localhost:8084/actuator/health"
        "Grievance Service:http://localhost:8085/actuator/health"
        "Analytics Service:http://localhost:8086/actuator/health"
    )
    
    local healthy=0
    local total=${#services[@]}
    
    for service in "${services[@]}"; do
        local name="${service%%:*}"
        local url="${service##*:}"
        
        if curl -s "$url" > /dev/null 2>&1; then
            log_success "$name is healthy"
            ((healthy++))
        else
            log_error "$name is not responding"
        fi
    done
    
    log_info "Health check complete: $healthy/$total services healthy"
    
    if [[ $healthy -eq 0 ]]; then
        log_error "No services are available. Please start the DSR services."
        return 1
    fi
}

# Run tests function
run_tests() {
    local test_type="$1"
    shift
    
    cd "$PROJECT_ROOT"
    
    # Set environment variables
    export HEADLESS="$HEADLESS"
    export CI="${CI:-false}"
    
    # Build Playwright command
    local cmd="npx playwright test"
    
    # Add test-specific options
    case "$test_type" in
        "smoke")
            cmd="$cmd --grep @smoke"
            ;;
        "regression")
            cmd="$cmd --grep @regression"
            ;;
        "account")
            cmd="$cmd tests/account/"
            ;;
        "modules")
            cmd="$cmd tests/modules/"
            ;;
        "integration")
            cmd="$cmd tests/integration/"
            ;;
        "test"|*)
            # Default test run
            ;;
    esac
    
    # Add browser option
    if [[ "$BROWSER" != "chromium" ]]; then
        cmd="$cmd --project=$BROWSER"
    fi
    
    # Add workers option
    cmd="$cmd --workers=$WORKERS"
    
    # Add retries option
    cmd="$cmd --retries=$RETRIES"
    
    # Add debug mode
    if [[ "${DEBUG:-false}" == "true" ]]; then
        cmd="$cmd --debug"
    fi
    
    # Add UI mode
    if [[ "${UI_MODE:-false}" == "true" ]]; then
        cmd="$cmd --ui"
    fi
    
    # Add headed mode
    if [[ "$HEADLESS" == "false" ]]; then
        cmd="$cmd --headed"
    fi
    
    log_info "Running command: $cmd"
    
    # Run the tests
    if eval "$cmd"; then
        log_success "Tests completed successfully!"
        return 0
    else
        log_error "Tests failed!"
        return 1
    fi
}

# Clean up function
clean() {
    log_info "Cleaning up test artifacts..."
    
    cd "$PROJECT_ROOT"
    
    # Remove test results
    rm -rf test-results/
    rm -rf allure-results/
    rm -rf screenshots/
    rm -rf test-archives/
    
    # Remove temporary files
    rm -f .auth
    
    log_success "Cleanup completed!"
}

# Generate and open reports
report() {
    log_info "Generating test reports..."
    
    cd "$PROJECT_ROOT"
    
    # Generate HTML report
    if [[ -d "test-results" ]]; then
        log_info "Opening HTML report..."
        npx playwright show-report
    fi
    
    # Generate Allure report if results exist
    if [[ -d "allure-results" ]] && command -v allure &> /dev/null; then
        log_info "Generating Allure report..."
        allure generate allure-results --clean -o allure-report
        allure open allure-report
    fi
}

# Main function
main() {
    local command="${1:-test}"
    shift || true
    
    parse_args "$@"
    
    case "$command" in
        "setup")
            setup
            ;;
        "start-frontend")
            start_frontend
            ;;
        "stop-frontend")
            stop_frontend
            ;;
        "health-check")
            health_check
            ;;
        "test"|"smoke"|"regression"|"account"|"modules"|"integration")
            health_check || exit 1
            run_tests "$command"
            ;;
        "clean")
            clean
            ;;
        "report")
            report
            ;;
        "help"|"--help")
            show_help
            ;;
        *)
            log_error "Unknown command: $command"
            show_help
            exit 1
            ;;
    esac
}

# Trap to cleanup on exit
trap 'stop_frontend' EXIT

# Run main function
main "$@"
