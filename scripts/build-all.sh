#!/bin/bash

# Philippine Dynamic Social Registry (DSR) - Build Script
# Version: 3.0.0
# Author: DSR Development Team
# Description: Comprehensive build script for all DSR components

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD_DIR="${PROJECT_ROOT}/build"
REGISTRY="${REGISTRY:-registry.govcloud.ph}"
REGISTRY_GROUP="${REGISTRY_GROUP:-dsr}"
VERSION="${VERSION:-3.0.0}"
SKIP_TESTS="${SKIP_TESTS:-false}"
SKIP_FRONTEND="${SKIP_FRONTEND:-false}"
SKIP_CONTAINERS="${SKIP_CONTAINERS:-false}"

# Services to build
SERVICES=(
    "registration-service"
    "data-management-service"
    "eligibility-service"
    "interoperability-service"
    "payment-service"
    "grievance-service"
    "analytics-service"
)

# Frontend applications
FRONTEND_APPS=(
    "web-portal"
    "admin-portal"
    "mobile-app"
)

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
    exit 1
}

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    # Check Java
    if ! command -v java &> /dev/null; then
        error_exit "Java is not installed or not in PATH"
    fi
    
    local java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [[ "$java_version" -lt 17 ]]; then
        error_exit "Java 17 or higher is required (found: $java_version)"
    fi
    
    # Check Maven
    if ! command -v ./mvnw &> /dev/null; then
        error_exit "Maven wrapper (mvnw) not found"
    fi
    
    # Check Node.js (if building frontend)
    if [[ "$SKIP_FRONTEND" != "true" ]]; then
        if ! command -v node &> /dev/null; then
            error_exit "Node.js is not installed or not in PATH"
        fi
        
        local node_version=$(node --version | cut -d'v' -f2 | cut -d'.' -f1)
        if [[ "$node_version" -lt 18 ]]; then
            error_exit "Node.js 18 or higher is required (found: $node_version)"
        fi
    fi
    
    # Check Podman (if building containers)
    if [[ "$SKIP_CONTAINERS" != "true" ]]; then
        if ! command -v podman &> /dev/null; then
            log_warning "Podman not found, skipping container builds"
            SKIP_CONTAINERS="true"
        fi
    fi
    
    log_success "Prerequisites check completed"
}

# Clean previous builds
clean_build() {
    log_info "Cleaning previous builds..."
    
    # Clean Maven builds
    ./mvnw clean -q
    
    # Clean frontend builds
    if [[ "$SKIP_FRONTEND" != "true" ]]; then
        for app in "${FRONTEND_APPS[@]}"; do
            if [[ -d "frontend/$app" ]]; then
                log_info "Cleaning frontend/$app..."
                cd "frontend/$app"
                if [[ -f "package.json" ]]; then
                    rm -rf node_modules dist build .next
                fi
                cd "$PROJECT_ROOT"
            fi
        done
    fi
    
    # Clean build directory
    rm -rf "$BUILD_DIR"
    mkdir -p "$BUILD_DIR"
    
    log_success "Clean completed"
}

# Build shared libraries
build_shared() {
    log_info "Building shared libraries..."
    
    local shared_modules=("common" "security" "messaging")
    
    for module in "${shared_modules[@]}"; do
        log_info "Building shared/$module..."
        ./mvnw clean install -pl "shared/$module" -am ${SKIP_TESTS:+-DskipTests} -q
    done
    
    log_success "Shared libraries built successfully"
}

# Build backend services
build_backend() {
    log_info "Building backend services..."
    
    for service in "${SERVICES[@]}"; do
        log_info "Building $service..."
        
        # Build the service
        ./mvnw clean package -pl "services/$service" -am ${SKIP_TESTS:+-DskipTests} -q
        
        # Copy JAR to build directory
        local jar_file=$(find "services/$service/target" -name "*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | head -n 1)
        if [[ -f "$jar_file" ]]; then
            cp "$jar_file" "$BUILD_DIR/$service.jar"
            log_success "$service built successfully"
        else
            error_exit "Failed to find JAR file for $service"
        fi
    done
    
    log_success "All backend services built successfully"
}

# Build frontend applications
build_frontend() {
    if [[ "$SKIP_FRONTEND" == "true" ]]; then
        log_info "Skipping frontend builds"
        return
    fi
    
    log_info "Building frontend applications..."
    
    for app in "${FRONTEND_APPS[@]}"; do
        if [[ -d "frontend/$app" ]]; then
            log_info "Building frontend/$app..."
            cd "frontend/$app"
            
            if [[ -f "package.json" ]]; then
                # Install dependencies
                npm ci --silent
                
                # Build application
                npm run build --silent
                
                # Copy build artifacts
                if [[ -d "dist" ]]; then
                    cp -r dist "$BUILD_DIR/$app-dist"
                elif [[ -d "build" ]]; then
                    cp -r build "$BUILD_DIR/$app-build"
                elif [[ -d ".next" ]]; then
                    cp -r .next "$BUILD_DIR/$app-next"
                fi
                
                log_success "frontend/$app built successfully"
            else
                log_warning "No package.json found in frontend/$app, skipping"
            fi
            
            cd "$PROJECT_ROOT"
        else
            log_warning "Frontend app $app not found, skipping"
        fi
    done
    
    log_success "All frontend applications built successfully"
}

# Run tests
run_tests() {
    if [[ "$SKIP_TESTS" == "true" ]]; then
        log_info "Skipping tests"
        return
    fi
    
    log_info "Running tests..."
    
    # Run unit tests
    log_info "Running unit tests..."
    ./mvnw test -q
    
    # Run integration tests
    log_info "Running integration tests..."
    ./mvnw verify -q
    
    # Run frontend tests
    if [[ "$SKIP_FRONTEND" != "true" ]]; then
        for app in "${FRONTEND_APPS[@]}"; do
            if [[ -d "frontend/$app" && -f "frontend/$app/package.json" ]]; then
                log_info "Running tests for frontend/$app..."
                cd "frontend/$app"
                npm test -- --watchAll=false --coverage=false --silent
                cd "$PROJECT_ROOT"
            fi
        done
    fi
    
    log_success "All tests completed successfully"
}

# Build container images
build_containers() {
    if [[ "$SKIP_CONTAINERS" == "true" ]]; then
        log_info "Skipping container builds"
        return
    fi
    
    log_info "Building container images..."
    
    for service in "${SERVICES[@]}"; do
        log_info "Building container for $service..."
        
        local image_name="$REGISTRY/$REGISTRY_GROUP/$service"
        local containerfile="services/$service/Containerfile"
        
        if [[ -f "$containerfile" ]]; then
            # Build container image
            podman build \
                -t "$image_name:$VERSION" \
                -t "$image_name:latest" \
                -f "$containerfile" \
                . \
                --quiet
            
            log_success "Container for $service built successfully"
        else
            log_warning "Containerfile not found for $service, skipping"
        fi
    done
    
    log_success "All container images built successfully"
}

# Generate build report
generate_report() {
    log_info "Generating build report..."
    
    local report_file="$BUILD_DIR/build-report.txt"
    
    cat > "$report_file" << EOF
Philippine Dynamic Social Registry (DSR) - Build Report
======================================================

Build Date: $(date)
Version: $VERSION
Build Directory: $BUILD_DIR

Backend Services:
EOF
    
    for service in "${SERVICES[@]}"; do
        if [[ -f "$BUILD_DIR/$service.jar" ]]; then
            local size=$(du -h "$BUILD_DIR/$service.jar" | cut -f1)
            echo "  ✓ $service ($size)" >> "$report_file"
        else
            echo "  ✗ $service (failed)" >> "$report_file"
        fi
    done
    
    if [[ "$SKIP_FRONTEND" != "true" ]]; then
        echo "" >> "$report_file"
        echo "Frontend Applications:" >> "$report_file"
        
        for app in "${FRONTEND_APPS[@]}"; do
            local build_dir=""
            if [[ -d "$BUILD_DIR/$app-dist" ]]; then
                build_dir="$BUILD_DIR/$app-dist"
            elif [[ -d "$BUILD_DIR/$app-build" ]]; then
                build_dir="$BUILD_DIR/$app-build"
            elif [[ -d "$BUILD_DIR/$app-next" ]]; then
                build_dir="$BUILD_DIR/$app-next"
            fi
            
            if [[ -n "$build_dir" ]]; then
                local size=$(du -sh "$build_dir" | cut -f1)
                echo "  ✓ $app ($size)" >> "$report_file"
            else
                echo "  ✗ $app (failed or skipped)" >> "$report_file"
            fi
        done
    fi
    
    if [[ "$SKIP_CONTAINERS" != "true" ]]; then
        echo "" >> "$report_file"
        echo "Container Images:" >> "$report_file"
        
        for service in "${SERVICES[@]}"; do
            local image_name="$REGISTRY/$REGISTRY_GROUP/$service:$VERSION"
            if podman image exists "$image_name" 2>/dev/null; then
                local size=$(podman image inspect "$image_name" --format "{{.Size}}" | numfmt --to=iec)
                echo "  ✓ $service ($size)" >> "$report_file"
            else
                echo "  ✗ $service (failed or skipped)" >> "$report_file"
            fi
        done
    fi
    
    echo "" >> "$report_file"
    echo "Build completed at: $(date)" >> "$report_file"
    
    log_success "Build report generated: $report_file"
    cat "$report_file"
}

# Main execution
main() {
    log_info "Starting DSR build process..."
    log_info "Project root: $PROJECT_ROOT"
    log_info "Version: $VERSION"
    log_info "Skip tests: $SKIP_TESTS"
    log_info "Skip frontend: $SKIP_FRONTEND"
    log_info "Skip containers: $SKIP_CONTAINERS"
    
    cd "$PROJECT_ROOT"
    
    check_prerequisites
    clean_build
    build_shared
    build_backend
    build_frontend
    run_tests
    build_containers
    generate_report
    
    log_success "DSR build process completed successfully!"
}

# Handle script arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-tests)
            SKIP_TESTS="true"
            shift
            ;;
        --skip-frontend)
            SKIP_FRONTEND="true"
            shift
            ;;
        --skip-containers)
            SKIP_CONTAINERS="true"
            shift
            ;;
        --version)
            VERSION="$2"
            shift 2
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --skip-tests        Skip running tests"
            echo "  --skip-frontend     Skip building frontend applications"
            echo "  --skip-containers   Skip building container images"
            echo "  --version VERSION   Set build version (default: 3.0.0)"
            echo "  --help              Show this help message"
            echo ""
            echo "Environment variables:"
            echo "  REGISTRY            Container registry URL"
            echo "  REGISTRY_GROUP      Container registry group/namespace"
            exit 0
            ;;
        *)
            error_exit "Unknown option: $1"
            ;;
    esac
done

# Execute main function
main
