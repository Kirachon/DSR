#!/bin/bash

# Philippine Dynamic Social Registry (DSR) - Local Environment Setup
# Version: 1.0.0
# Author: DSR Development Team
# Purpose: Set up complete local development environment

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
    
    local missing_tools=()
    
    # Check Java
    if ! command -v java &> /dev/null; then
        missing_tools+=("Java 17+")
    else
        local java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [[ "$java_version" -lt 17 ]]; then
            missing_tools+=("Java 17+ (found: $java_version)")
        fi
    fi
    
    # Check Node.js
    if ! command -v node &> /dev/null; then
        missing_tools+=("Node.js 18+")
    else
        local node_version=$(node --version | cut -d'v' -f2 | cut -d'.' -f1)
        if [[ "$node_version" -lt 18 ]]; then
            missing_tools+=("Node.js 18+ (found: $node_version)")
        fi
    fi
    
    # Check Podman
    if ! command -v podman &> /dev/null; then
        missing_tools+=("Podman 4.8+")
    fi
    
    # Check Maven
    if ! command -v mvn &> /dev/null && ! [[ -f "./mvnw" ]]; then
        missing_tools+=("Maven or Maven Wrapper")
    fi
    
    # Check curl
    if ! command -v curl &> /dev/null; then
        missing_tools+=("curl")
    fi
    
    # Check PostgreSQL client tools
    if ! command -v pg_isready &> /dev/null; then
        log_warning "PostgreSQL client tools not found (optional for local development)"
    fi
    
    # Check Redis client tools
    if ! command -v redis-cli &> /dev/null; then
        log_warning "Redis client tools not found (optional for local development)"
    fi
    
    if [[ ${#missing_tools[@]} -gt 0 ]]; then
        log_error "Missing required tools:"
        for tool in "${missing_tools[@]}"; do
            echo "  - $tool"
        done
        echo
        log_info "Please install the missing tools and run this script again."
        exit 1
    fi
    
    log_success "All prerequisites are satisfied"
}

# Setup environment configuration
setup_environment() {
    log_info "Setting up environment configuration..."
    
    if [[ ! -f ".env.local" ]]; then
        if [[ -f ".env.local.example" ]]; then
            cp .env.local.example .env.local
            log_success "Created .env.local from template"
        else
            log_warning ".env.local template not found, using defaults"
        fi
    else
        log_info ".env.local already exists, skipping"
    fi
    
    # Create necessary directories
    mkdir -p logs
    mkdir -p build
    mkdir -p local-storage/documents
    mkdir -p local-storage/rosters
    
    log_success "Environment configuration completed"
}

# Start infrastructure services
start_infrastructure() {
    log_info "Starting infrastructure services..."
    
    if ! command -v podman-compose &> /dev/null; then
        log_error "podman-compose not found. Please install podman-compose."
        exit 1
    fi
    
    # Start infrastructure with Podman Compose
    podman-compose up -d
    
    log_info "Waiting for infrastructure services to be ready..."
    
    # Wait for PostgreSQL
    local attempts=0
    while [[ $attempts -lt 30 ]]; do
        if pg_isready -h localhost -p 5432 -U dsr_user >/dev/null 2>&1; then
            break
        fi
        sleep 2
        ((attempts++))
    done
    
    if [[ $attempts -eq 30 ]]; then
        log_error "PostgreSQL failed to start"
        exit 1
    fi
    
    # Wait for Redis
    attempts=0
    while [[ $attempts -lt 30 ]]; do
        if redis-cli -h localhost -p 6379 ping >/dev/null 2>&1; then
            break
        fi
        sleep 2
        ((attempts++))
    done
    
    if [[ $attempts -eq 30 ]]; then
        log_error "Redis failed to start"
        exit 1
    fi
    
    # Wait for Elasticsearch
    attempts=0
    while [[ $attempts -lt 60 ]]; do
        if curl -s -f "http://localhost:9200/_cluster/health" >/dev/null 2>&1; then
            break
        fi
        sleep 2
        ((attempts++))
    done
    
    if [[ $attempts -eq 60 ]]; then
        log_error "Elasticsearch failed to start"
        exit 1
    fi
    
    # Wait for Kafka
    attempts=0
    while [[ $attempts -lt 60 ]]; do
        if nc -z localhost 9092 >/dev/null 2>&1; then
            break
        fi
        sleep 2
        ((attempts++))
    done
    
    if [[ $attempts -eq 60 ]]; then
        log_error "Kafka failed to start"
        exit 1
    fi
    
    log_success "All infrastructure services are ready"
}

# Build services
build_services() {
    log_info "Building DSR services..."
    
    if [[ -f "./scripts/build-all.sh" ]]; then
        ./scripts/build-all.sh
    else
        log_error "build-all.sh script not found"
        exit 1
    fi
    
    log_success "All services built successfully"
}

# Initialize database
initialize_database() {
    log_info "Initializing database with sample data..."
    
    # Run database initialization scripts
    if [[ -f "database/sample-data/01_init_local_db.sql" ]]; then
        PGPASSWORD=dsr_local_password psql -h localhost -p 5432 -U dsr_user -d dsr_local -f database/sample-data/01_init_local_db.sql
        log_success "Database initialized"
    else
        log_warning "Database initialization script not found"
    fi
    
    # Load sample data
    if [[ -f "database/sample-data/03_load_sample_data.sql" ]]; then
        PGPASSWORD=dsr_local_password psql -h localhost -p 5432 -U dsr_user -d dsr_local -f database/sample-data/03_load_sample_data.sql
        log_success "Sample data loaded"
    else
        log_warning "Sample data script not found"
    fi
}

# Show completion summary
show_summary() {
    log_success "DSR Local Development Environment Setup Complete!"
    echo
    echo "=== Environment Summary ==="
    echo "Infrastructure Services:"
    echo "  - PostgreSQL: http://localhost:5432"
    echo "  - Redis: http://localhost:6379"
    echo "  - Elasticsearch: http://localhost:9200"
    echo "  - Kafka: http://localhost:9092"
    echo "  - pgAdmin: http://localhost:5050 (admin@dsr.local / admin)"
    echo "  - Kafka UI: http://localhost:8081"
    echo "  - Redis Commander: http://localhost:8082"
    echo "  - Prometheus: http://localhost:9090"
    echo "  - Grafana: http://localhost:3001 (admin / admin)"
    echo "  - Jaeger: http://localhost:16686"
    echo
    echo "DSR Services (when started):"
    echo "  - Registration Service: http://localhost:8080"
    echo "  - Data Management Service: http://localhost:8081"
    echo "  - Eligibility Service: http://localhost:8082"
    echo "  - Interoperability Service: http://localhost:8083"
    echo "  - Payment Service: http://localhost:8084"
    echo "  - Grievance Service: http://localhost:8085"
    echo "  - Analytics Service: http://localhost:8086"
    echo
    echo "Next Steps:"
    echo "  1. Start DSR services: ./scripts/run-local.sh"
    echo "  2. Check service status: ./scripts/run-local.sh status"
    echo "  3. View logs: ./scripts/run-local.sh logs"
    echo "  4. Stop services: ./scripts/run-local.sh stop"
    echo
    echo "Test Credentials:"
    echo "  - Admin: admin@dsr.local / admin123"
    echo "  - Caseworker: caseworker@dsr.local / admin123"
    echo "  - Citizen: citizen@dsr.local / admin123"
    echo
    log_info "Happy coding! 🚀"
}

# Main execution
main() {
    log_info "Setting up DSR Local Development Environment..."
    echo
    
    check_prerequisites
    setup_environment
    start_infrastructure
    build_services
    initialize_database
    show_summary
}

# Run main function
main "$@"
