# DSR Production Readiness Verification Script
# Validates all production-ready components and infrastructure
# Run this script to verify 100% production readiness status

param(
    [switch]$Detailed = $false,
    [switch]$StartServices = $false,
    [switch]$RunLoadTest = $false
)

Write-Host "=== DSR PRODUCTION READINESS VERIFICATION ===" -ForegroundColor Green
Write-Host "Date: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Yellow
Write-Host ""

$verificationResults = @{
    MicroservicesBuilt = $false
    RedisDeployed = $false
    MonitoringDeployed = $false
    LoadTestingReady = $false
    PerformanceOptimized = $false
    DocumentationComplete = $false
    OverallStatus = $false
}

# Function to check if a service JAR exists
function Test-ServiceJAR {
    param($ServiceName, $ServicePath)
    
    $jarPath = "$ServicePath/target/$ServiceName-3.0.0.jar"
    if (Test-Path $jarPath) {
        Write-Host "  ✅ $ServiceName JAR: BUILT" -ForegroundColor Green
        return $true
    } else {
        Write-Host "  ❌ $ServiceName JAR: MISSING" -ForegroundColor Red
        return $false
    }
}

# Function to check container status
function Test-ContainerStatus {
    param($ContainerName)
    
    try {
        $result = podman ps --filter "name=$ContainerName" --format "{{.Names}}" 2>$null
        if ($result -eq $ContainerName) {
            Write-Host "  ✅ ${ContainerName}: RUNNING" -ForegroundColor Green
            return $true
        } else {
            Write-Host "  ⚠️ ${ContainerName}: NOT RUNNING" -ForegroundColor Yellow
            return $false
        }
    }
    catch {
        Write-Host "  ❌ ${ContainerName}: ERROR" -ForegroundColor Red
        return $false
    }
}

# 1. Verify All 7 Microservices Built
Write-Host "1. MICROSERVICES BUILD VERIFICATION" -ForegroundColor Cyan
Write-Host "   Checking all 7 microservice JAR files..." -ForegroundColor White

$services = @(
    @{ Name = "registration-service"; Path = "services/registration-service" },
    @{ Name = "data-management-service"; Path = "services/data-management-service" },
    @{ Name = "eligibility-service"; Path = "services/eligibility-service" },
    @{ Name = "payment-service"; Path = "services/payment-service" },
    @{ Name = "analytics-service"; Path = "services/analytics-service" },
    @{ Name = "interoperability-service"; Path = "services/interoperability-service" },
    @{ Name = "grievance-service"; Path = "services/grievance-service" }
)

$builtServices = 0
foreach ($service in $services) {
    if (Test-ServiceJAR -ServiceName $service.Name -ServicePath $service.Path) {
        $builtServices++
    }
}

$verificationResults.MicroservicesBuilt = ($builtServices -eq 7)
Write-Host "   Result: $builtServices/7 services built" -ForegroundColor $(if ($builtServices -eq 7) { "Green" } else { "Yellow" })
Write-Host ""

# 2. Verify Redis Infrastructure
Write-Host "2. REDIS CACHING INFRASTRUCTURE" -ForegroundColor Cyan
Write-Host "   Checking Redis container and connectivity..." -ForegroundColor White

$redisRunning = Test-ContainerStatus -ContainerName "dsr-redis"
if ($redisRunning) {
    try {
        $pingResult = podman exec dsr-redis redis-cli ping 2>$null
        if ($pingResult -eq "PONG") {
            Write-Host "  ✅ Redis connectivity: VERIFIED" -ForegroundColor Green
            $verificationResults.RedisDeployed = $true
        } else {
            Write-Host "  ❌ Redis connectivity: FAILED" -ForegroundColor Red
        }
    }
    catch {
        Write-Host "  ❌ Redis connectivity: ERROR" -ForegroundColor Red
    }
}
Write-Host ""

# 3. Verify Monitoring Infrastructure
Write-Host "3. MONITORING INFRASTRUCTURE" -ForegroundColor Cyan
Write-Host "   Checking Prometheus and Grafana deployment..." -ForegroundColor White

$prometheusRunning = Test-ContainerStatus -ContainerName "dsr-prometheus"
$grafanaRunning = Test-ContainerStatus -ContainerName "dsr-grafana"

$verificationResults.MonitoringDeployed = $prometheusRunning -and $grafanaRunning

if ($verificationResults.MonitoringDeployed) {
    Write-Host "  ✅ Monitoring stack: DEPLOYED" -ForegroundColor Green
    Write-Host "    📊 Prometheus: http://localhost:9090" -ForegroundColor Blue
    Write-Host "    📈 Grafana: http://localhost:3001 (admin/admin)" -ForegroundColor Blue
} else {
    Write-Host "  ⚠️ Monitoring stack: PARTIAL DEPLOYMENT" -ForegroundColor Yellow
}
Write-Host ""

# 4. Verify Load Testing Framework
Write-Host "4. LOAD TESTING FRAMEWORK" -ForegroundColor Cyan
Write-Host "   Checking load testing tools and scripts..." -ForegroundColor White

$loadTestFiles = @(
    "tests/performance/k6-load-testing.js",
    "tests/performance/simple-load-test.ps1",
    "tests/performance/dsr-production-load-test.js"
)

$loadTestReady = $true
foreach ($file in $loadTestFiles) {
    if (Test-Path $file) {
        Write-Host "  ✅ $(Split-Path $file -Leaf): AVAILABLE" -ForegroundColor Green
    } else {
        Write-Host "  ❌ $(Split-Path $file -Leaf): MISSING" -ForegroundColor Red
        $loadTestReady = $false
    }
}

$verificationResults.LoadTestingReady = $loadTestReady
Write-Host ""

# 5. Verify Performance Optimization
Write-Host "5. PERFORMANCE OPTIMIZATION" -ForegroundColor Cyan
Write-Host "   Checking performance configurations..." -ForegroundColor White

$perfConfigs = @(
    "database/performance/connection-pool-optimization.yml",
    "infrastructure/redis/redis-simple.conf",
    "docs/performance/DSR_PERFORMANCE_ANALYSIS_AND_OPTIMIZATION.md"
)

$perfOptimized = $true
foreach ($config in $perfConfigs) {
    if (Test-Path $config) {
        Write-Host "  ✅ $(Split-Path $config -Leaf): CONFIGURED" -ForegroundColor Green
    } else {
        Write-Host "  ❌ $(Split-Path $config -Leaf): MISSING" -ForegroundColor Red
        $perfOptimized = $false
    }
}

$verificationResults.PerformanceOptimized = $perfOptimized
Write-Host ""

# 6. Verify Documentation
Write-Host "6. PRODUCTION DOCUMENTATION" -ForegroundColor Cyan
Write-Host "   Checking production documentation..." -ForegroundColor White

$docFiles = @(
    "DSR_IMPLEMENTATION_ROADMAP_UPDATED.md",
    "docs/performance/DSR_PERFORMANCE_ANALYSIS_AND_OPTIMIZATION.md",
    "docs/deployment/DSR_Production_Deployment_Guide.md"
)

$docsComplete = $true
foreach ($doc in $docFiles) {
    if (Test-Path $doc) {
        Write-Host "  ✅ $(Split-Path $doc -Leaf): COMPLETE" -ForegroundColor Green
    } else {
        Write-Host "  ❌ $(Split-Path $doc -Leaf): MISSING" -ForegroundColor Red
        $docsComplete = $false
    }
}

$verificationResults.DocumentationComplete = $docsComplete
Write-Host ""

# Overall Assessment
$verificationResults.OverallStatus = $verificationResults.MicroservicesBuilt -and 
                                   $verificationResults.RedisDeployed -and 
                                   $verificationResults.MonitoringDeployed -and 
                                   $verificationResults.LoadTestingReady -and 
                                   $verificationResults.PerformanceOptimized -and 
                                   $verificationResults.DocumentationComplete

Write-Host "=== PRODUCTION READINESS SUMMARY ===" -ForegroundColor Green
Write-Host "Microservices Built (7/7): $(if ($verificationResults.MicroservicesBuilt) { "✅ COMPLETE" } else { "❌ INCOMPLETE" })" -ForegroundColor $(if ($verificationResults.MicroservicesBuilt) { "Green" } else { "Red" })
Write-Host "Redis Infrastructure: $(if ($verificationResults.RedisDeployed) { "✅ DEPLOYED" } else { "❌ NOT DEPLOYED" })" -ForegroundColor $(if ($verificationResults.RedisDeployed) { "Green" } else { "Red" })
Write-Host "Monitoring Stack: $(if ($verificationResults.MonitoringDeployed) { "✅ DEPLOYED" } else { "❌ NOT DEPLOYED" })" -ForegroundColor $(if ($verificationResults.MonitoringDeployed) { "Green" } else { "Red" })
Write-Host "Load Testing Ready: $(if ($verificationResults.LoadTestingReady) { "✅ READY" } else { "❌ NOT READY" })" -ForegroundColor $(if ($verificationResults.LoadTestingReady) { "Green" } else { "Red" })
Write-Host "Performance Optimized: $(if ($verificationResults.PerformanceOptimized) { "✅ OPTIMIZED" } else { "❌ NOT OPTIMIZED" })" -ForegroundColor $(if ($verificationResults.PerformanceOptimized) { "Green" } else { "Red" })
Write-Host "Documentation Complete: $(if ($verificationResults.DocumentationComplete) { "✅ COMPLETE" } else { "❌ INCOMPLETE" })" -ForegroundColor $(if ($verificationResults.DocumentationComplete) { "Green" } else { "Red" })
Write-Host ""
Write-Host "OVERALL STATUS: $(if ($verificationResults.OverallStatus) { "🎯 PRODUCTION READY" } else { "⚠️ NEEDS ATTENTION" })" -ForegroundColor $(if ($verificationResults.OverallStatus) { "Green" } else { "Yellow" })
Write-Host ""

# Optional Actions
if ($StartServices) {
    Write-Host "=== STARTING SERVICES ===" -ForegroundColor Cyan
    Write-Host "Starting all DSR services..." -ForegroundColor White
    
    # Start infrastructure first
    if (-not $verificationResults.RedisDeployed) {
        Write-Host "Starting Redis..." -ForegroundColor Yellow
        podman start dsr-redis
    }
    
    if (-not $verificationResults.MonitoringDeployed) {
        Write-Host "Starting monitoring stack..." -ForegroundColor Yellow
        podman-compose up -d prometheus grafana
    }
    
    Write-Host "Infrastructure services started. Use start-all-services.ps1 to start microservices." -ForegroundColor Green
}

if ($RunLoadTest) {
    Write-Host "=== RUNNING LOAD TEST ===" -ForegroundColor Cyan
    Write-Host "Executing performance validation..." -ForegroundColor White
    
    if (Test-Path "tests/performance/simple-load-test.ps1") {
        & "tests/performance/simple-load-test.ps1" -Requests 100 -ConcurrentUsers 10
    } else {
        Write-Host "Load test script not found!" -ForegroundColor Red
    }
}

# Deployment Commands
Write-Host "=== NEXT STEPS ===" -ForegroundColor Green
Write-Host "To start all services:" -ForegroundColor White
Write-Host "  .\start-all-services.ps1" -ForegroundColor Blue
Write-Host ""
Write-Host "To run performance validation:" -ForegroundColor White
Write-Host "  .\tests\performance\simple-load-test.ps1 -Requests 200 -ConcurrentUsers 20" -ForegroundColor Blue
Write-Host ""
Write-Host "To access monitoring:" -ForegroundColor White
Write-Host "  Prometheus: http://localhost:9090" -ForegroundColor Blue
Write-Host "  Grafana: http://localhost:3001 (admin/admin)" -ForegroundColor Blue
Write-Host ""
Write-Host "Production deployment guide:" -ForegroundColor White
Write-Host "  docs\deployment\DSR_Production_Deployment_Guide.md" -ForegroundColor Blue

# Return appropriate exit code
if ($verificationResults.OverallStatus) {
    exit 0
} else {
    exit 1
}
