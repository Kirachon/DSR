# DSR Services Health Check Script
# Tests all 7 microservices for health status and basic API functionality

Write-Host "=== DSR Services Health Check ===" -ForegroundColor Green
Write-Host "Testing all 7 microservices..." -ForegroundColor Yellow
Write-Host ""

# Define services with their ports and names
$services = @(
    @{Name="Registration Service"; Port=8080; Path="/api/v1/health"},
    @{Name="Data Management Service"; Port=8081; Path="/actuator/health"},
    @{Name="Eligibility Service"; Port=8082; Path="/actuator/health"},
    @{Name="Interoperability Service"; Port=8083; Path="/actuator/health"},
    @{Name="Payment Service"; Port=8084; Path="/actuator/health"},
    @{Name="Grievance Service"; Port=8085; Path="/actuator/health"},
    @{Name="Analytics Service"; Port=8086; Path="/actuator/health"}
)

$allHealthy = $true
$results = @()

foreach ($service in $services) {
    $url = "http://localhost:$($service.Port)$($service.Path)"
    Write-Host "Testing $($service.Name) on port $($service.Port)..." -NoNewline
    
    try {
        $response = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 10
        if ($response.status -eq "UP") {
            Write-Host " ✓ HEALTHY" -ForegroundColor Green
            $results += @{
                Service = $service.Name
                Port = $service.Port
                Status = "HEALTHY"
                Response = $response
            }
        } else {
            Write-Host " ✗ UNHEALTHY" -ForegroundColor Red
            $allHealthy = $false
            $results += @{
                Service = $service.Name
                Port = $service.Port
                Status = "UNHEALTHY"
                Response = $response
            }
        }
    }
    catch {
        Write-Host " ✗ FAILED" -ForegroundColor Red
        Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red
        $allHealthy = $false
        $results += @{
            Service = $service.Name
            Port = $service.Port
            Status = "FAILED"
            Error = $_.Exception.Message
        }
    }
}

Write-Host ""
Write-Host "=== Health Check Summary ===" -ForegroundColor Green

foreach ($result in $results) {
    $status = $result.Status
    $color = if ($status -eq "HEALTHY") { "Green" } else { "Red" }
    Write-Host "$($result.Service) (Port $($result.Port)): $status" -ForegroundColor $color
}

Write-Host ""
if ($allHealthy) {
    Write-Host "✓ ALL SERVICES ARE HEALTHY!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "✗ SOME SERVICES ARE NOT HEALTHY!" -ForegroundColor Red
    exit 1
}
