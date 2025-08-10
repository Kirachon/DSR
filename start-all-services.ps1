# DSR Production Services Startup Script
# Starts all 7 microservices with production database configuration

# Load environment variables from production-env.properties
Get-Content "production-env.properties" | ForEach-Object {
    if ($_ -match "^([^#][^=]+)=(.*)$") {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2], "Process")
    }
}

Write-Host "Starting DSR Production Services..." -ForegroundColor Green

# Start Registration Service (Port 8080)
Write-Host "Starting Registration Service on port 8080..." -ForegroundColor Yellow
Start-Process -FilePath "java" -ArgumentList @(
    "-jar", "services/registration-service/target/registration-service-3.0.0.jar",
    "--spring.profiles.active=local",
    "--server.port=8080"
) -WindowStyle Hidden

Start-Sleep -Seconds 5

# Start Data Management Service (Port 8081)
Write-Host "Starting Data Management Service on port 8081..." -ForegroundColor Yellow
Start-Process -FilePath "java" -ArgumentList @(
    "-jar", "services/data-management-service/target/data-management-service-3.0.0.jar",
    "--spring.profiles.active=local",
    "--server.port=8081"
) -WindowStyle Hidden

Start-Sleep -Seconds 5

# Start Eligibility Service (Port 8082)
Write-Host "Starting Eligibility Service on port 8082..." -ForegroundColor Yellow
Start-Process -FilePath "java" -ArgumentList @(
    "-jar", "services/eligibility-service/target/eligibility-service-3.0.0.jar",
    "--spring.profiles.active=local",
    "--server.port=8082"
) -WindowStyle Hidden

Start-Sleep -Seconds 5

# Start Interoperability Service (Port 8083)
Write-Host "Starting Interoperability Service on port 8083..." -ForegroundColor Yellow
Start-Process -FilePath "java" -ArgumentList @(
    "-jar", "services/interoperability-service/target/interoperability-service-3.0.0.jar",
    "--spring.profiles.active=local",
    "--server.port=8083"
) -WindowStyle Hidden

Start-Sleep -Seconds 5

# Start Payment Service (Port 8084)
Write-Host "Starting Payment Service on port 8084..." -ForegroundColor Yellow
Start-Process -FilePath "java" -ArgumentList @(
    "-jar", "services/payment-service/target/payment-service-3.0.0.jar",
    "--spring.profiles.active=local",
    "--server.port=8084"
) -WindowStyle Hidden

Start-Sleep -Seconds 5

# Start Grievance Service (Port 8085)
Write-Host "Starting Grievance Service on port 8085..." -ForegroundColor Yellow
Start-Process -FilePath "java" -ArgumentList @(
    "-jar", "services/grievance-service/target/grievance-service-3.0.0.jar",
    "--spring.profiles.active=local",
    "--server.port=8085"
) -WindowStyle Hidden

Start-Sleep -Seconds 5

# Start Analytics Service (Port 8086)
Write-Host "Starting Analytics Service on port 8086..." -ForegroundColor Yellow
Start-Process -FilePath "java" -ArgumentList @(
    "-jar", "services/analytics-service/target/analytics-service-3.0.0.jar",
    "--spring.profiles.active=local",
    "--server.port=8086"
) -WindowStyle Hidden

Write-Host "All services started! Waiting for initialization..." -ForegroundColor Green
Start-Sleep -Seconds 30

Write-Host "Checking service health..." -ForegroundColor Cyan

# Check each service health
$services = @(
    @{Name="Registration"; Port=8080},
    @{Name="Data Management"; Port=8081},
    @{Name="Eligibility"; Port=8082},
    @{Name="Interoperability"; Port=8083},
    @{Name="Payment"; Port=8084},
    @{Name="Grievance"; Port=8085},
    @{Name="Analytics"; Port=8086}
)

foreach ($service in $services) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$($service.Port)/actuator/health" -TimeoutSec 5 -UseBasicParsing
        if ($response.StatusCode -eq 200) {
            Write-Host "✓ $($service.Name) Service (Port $($service.Port)): HEALTHY" -ForegroundColor Green
        } else {
            Write-Host "✗ $($service.Name) Service (Port $($service.Port)): UNHEALTHY" -ForegroundColor Red
        }
    } catch {
        Write-Host "✗ $($service.Name) Service (Port $($service.Port)): NOT RESPONDING" -ForegroundColor Red
    }
}

Write-Host "`nDSR Production Services Status Check Complete!" -ForegroundColor Green
Write-Host "Frontend URL: http://localhost:3000" -ForegroundColor Cyan
Write-Host "Services running on ports 8080-8086" -ForegroundColor Cyan
