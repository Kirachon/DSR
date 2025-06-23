#!/bin/bash

# Philippine Dynamic Social Registry (DSR) - Local Development Runner
# Version: 1.0.0
# Author: DSR Development Team
# Purpose: Start all DSR services locally with proper dependency management

set -euo pipefail

# Script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

# Load environment variables
if [[ -f ".env.local" ]]; then
    set -a
    source .env.local
    set +a
    echo "✓ Loaded local environment configuration"
else
    echo "⚠ Warning: .env.local not found, using defaults"
fi

# Configuration
SERVICES=(
    "registration-service:8080"
    "data-management-service:8081"
    "eligibility-service:8082"
    "interoperability-service:8083"
    "payment-service:8084"
    "grievance-service:8085"
    "analytics-service:8086"
)

INFRASTRUCTURE_SERVICES=(
    "postgresql:5432"
    "redis:6379"
    "elasticsearch:9200"
    "kafka:9092"
)

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

# Error handling
error_exit() {
    log_error "$1"
    cleanup
    exit 1
}

# Cleanup function
cleanup() {
    log_info "Cleaning up..."
    if [[ -f "$PROJECT_ROOT/local-dev.pid" ]]; then
        while IFS= read -r pid; do
            if kill -0 "$pid" 2>/dev/null; then
                log_info "Stopping process $pid"
                kill -TERM "$pid" 2>/dev/null || true
            fi
        done < "$PROJECT_ROOT/local-dev.pid"
        rm -f "$PROJECT_ROOT/local-dev.pid"
    fi
}

# Trap signals for cleanup
trap cleanup EXIT INT TERM

# Check if port is available
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 1
    fi
    return 0
}

# Wait for service to be ready
wait_for_service() {
    local service_name=$1
    local port=$2
    local max_attempts=${3:-30}
    local attempt=1
    
    log_info "Waiting for $service_name on port $port..."
    
    while [[ $attempt -le $max_attempts ]]; do
        if curl -s -f "http://localhost:$port/actuator/health" >/dev/null 2>&1 || \
           curl -s -f "http://localhost:$port" >/dev/null 2>&1; then
            log_success "$service_name is ready on port $port"
            return 0
        fi
        
        if [[ $attempt -eq $max_attempts ]]; then
            log_error "$service_name failed to start on port $port after $max_attempts attempts"
            return 1
        fi
        
        sleep 2
        ((attempt++))
    done
}

# Check infrastructure services
check_infrastructure() {
    log_info "Checking infrastructure services..."
    
    for service_port in "${INFRASTRUCTURE_SERVICES[@]}"; do
        IFS=':' read -r service port <<< "$service_port"
        
        case $service in
            "postgresql")
                if ! pg_isready -h localhost -p $port -U dsr_user >/dev/null 2>&1; then
                    log_error "PostgreSQL is not ready on port $port"
                    log_info "Please start infrastructure with: podman-compose up -d"
                    return 1
                fi
                ;;
            "redis")
                if ! redis-cli -h localhost -p $port ping >/dev/null 2>&1; then
                    log_error "Redis is not ready on port $port"
                    log_info "Please start infrastructure with: podman-compose up -d"
                    return 1
                fi
                ;;
            "elasticsearch")
                if ! curl -s -f "http://localhost:$port/_cluster/health" >/dev/null 2>&1; then
                    log_error "Elasticsearch is not ready on port $port"
                    log_info "Please start infrastructure with: podman-compose up -d"
                    return 1
                fi
                ;;
            "kafka")
                if ! nc -z localhost $port >/dev/null 2>&1; then
                    log_error "Kafka is not ready on port $port"
                    log_info "Please start infrastructure with: podman-compose up -d"
                    return 1
                fi
                ;;
        esac
        
        log_success "$service is ready on port $port"
    done
    
    log_success "All infrastructure services are ready"
}

# Start a single service
start_service() {
    local service_name=$1
    local port=$2
    
    log_info "Starting $service_name on port $port..."
    
    # Check if port is available
    if ! check_port $port; then
        log_warning "Port $port is already in use, skipping $service_name"
        return 0
    fi
    
    # Create logs directory
    mkdir -p logs
    
    # Start the service
    local jar_file="build/$service_name.jar"
    if [[ ! -f "$jar_file" ]]; then
        log_error "JAR file not found: $jar_file"
        log_info "Please build services first with: ./scripts/build-all.sh"
        return 1
    fi
    
    # Start service in background
    java -jar "$jar_file" \
        --spring.profiles.active=local \
        --server.port=$port \
        > "logs/$service_name.log" 2>&1 &
    
    local pid=$!
    echo $pid >> "$PROJECT_ROOT/local-dev.pid"
    
    log_info "$service_name started with PID $pid"
    
    # Wait for service to be ready
    if wait_for_service "$service_name" "$port"; then
        log_success "$service_name is running on http://localhost:$port"
        return 0
    else
        log_error "Failed to start $service_name"
        return 1
    fi
}

# Start all services
start_services() {
    log_info "Starting DSR services..."
    
    # Clear PID file
    rm -f "$PROJECT_ROOT/local-dev.pid"
    
    # Start services in dependency order
    for service_port in "${SERVICES[@]}"; do
        IFS=':' read -r service port <<< "$service_port"
        
        if ! start_service "$service" "$port"; then
            error_exit "Failed to start $service"
        fi
        
        # Small delay between services
        sleep 2
    done
    
    log_success "All DSR services started successfully!"
}

# Show service status
show_status() {
    log_info "DSR Services Status:"
    echo
    
    for service_port in "${SERVICES[@]}"; do
        IFS=':' read -r service port <<< "$service_port"
        
        if curl -s -f "http://localhost:$port/actuator/health" >/dev/null 2>&1; then
            echo -e "  ✓ $service: ${GREEN}RUNNING${NC} on http://localhost:$port"
        else
            echo -e "  ✗ $service: ${RED}NOT RUNNING${NC} on port $port"
        fi
    done
    
    echo
    log_info "Infrastructure Services Status:"
    echo
    
    for service_port in "${INFRASTRUCTURE_SERVICES[@]}"; do
        IFS=':' read -r service port <<< "$service_port"
        
        case $service in
            "postgresql")
                if pg_isready -h localhost -p $port -U dsr_user >/dev/null 2>&1; then
                    echo -e "  ✓ $service: ${GREEN}RUNNING${NC} on port $port"
                else
                    echo -e "  ✗ $service: ${RED}NOT RUNNING${NC} on port $port"
                fi
                ;;
            "redis")
                if redis-cli -h localhost -p $port ping >/dev/null 2>&1; then
                    echo -e "  ✓ $service: ${GREEN}RUNNING${NC} on port $port"
                else
                    echo -e "  ✗ $service: ${RED}NOT RUNNING${NC} on port $port"
                fi
                ;;
            "elasticsearch")
                if curl -s -f "http://localhost:$port/_cluster/health" >/dev/null 2>&1; then
                    echo -e "  ✓ $service: ${GREEN}RUNNING${NC} on port $port"
                else
                    echo -e "  ✗ $service: ${RED}NOT RUNNING${NC} on port $port"
                fi
                ;;
            "kafka")
                if nc -z localhost $port >/dev/null 2>&1; then
                    echo -e "  ✓ $service: ${GREEN}RUNNING${NC} on port $port"
                else
                    echo -e "  ✗ $service: ${RED}NOT RUNNING${NC} on port $port"
                fi
                ;;
        esac
    done
    
    echo
}

# Show usage
show_usage() {
    echo "Usage: $0 [COMMAND]"
    echo
    echo "Commands:"
    echo "  start     Start all DSR services (default)"
    echo "  stop      Stop all running DSR services"
    echo "  restart   Restart all DSR services"
    echo "  status    Show status of all services"
    echo "  logs      Show logs for all services"
    echo "  help      Show this help message"
    echo
    echo "Examples:"
    echo "  $0                # Start all services"
    echo "  $0 start          # Start all services"
    echo "  $0 status         # Check service status"
    echo "  $0 stop           # Stop all services"
}

# Main execution
main() {
    local command=${1:-start}
    
    case $command in
        "start")
            log_info "Starting DSR Local Development Environment..."
            check_infrastructure
            start_services
            show_status
            log_info "DSR is ready for development!"
            log_info "Press Ctrl+C to stop all services"
            
            # Keep script running
            while true; do
                sleep 10
            done
            ;;
        "stop")
            log_info "Stopping DSR services..."
            cleanup
            log_success "All DSR services stopped"
            ;;
        "restart")
            log_info "Restarting DSR services..."
            cleanup
            sleep 2
            check_infrastructure
            start_services
            show_status
            ;;
        "status")
            show_status
            ;;
        "logs")
            log_info "Showing logs for all services..."
            if [[ -d "logs" ]]; then
                tail -f logs/*.log
            else
                log_warning "No log files found"
            fi
            ;;
        "help"|"-h"|"--help")
            show_usage
            ;;
        *)
            log_error "Unknown command: $command"
            show_usage
            exit 1
            ;;
    esac
}

# Run main function
main "$@"
