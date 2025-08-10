# DSR Performance Validation Script
# Validates system performance and production readiness
# Tests response times, concurrent connections, and system stability

param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$MaxConcurrentUsers = 100,
    [int]$TestDurationSeconds = 300,
    [string]$ReportPath = "tests/performance/reports"
)

# Ensure reports directory exists
if (!(Test-Path $ReportPath)) {
    New-Item -ItemType Directory -Path $ReportPath -Force | Out-Null
}

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$reportFile = "$ReportPath/performance-validation-$timestamp.json"

Write-Host "=== DSR PERFORMANCE VALIDATION ===" -ForegroundColor Green
Write-Host "Base URL: $BaseUrl" -ForegroundColor Yellow
Write-Host "Max Concurrent Users: $MaxConcurrentUsers" -ForegroundColor Yellow
Write-Host "Test Duration: $TestDurationSeconds seconds" -ForegroundColor Yellow
Write-Host "Report File: $reportFile" -ForegroundColor Yellow
Write-Host ""

# Test endpoints
$endpoints = @(
    @{ Path = "/actuator/health"; Name = "Health Check"; Method = "GET" },
    @{ Path = "/actuator/info"; Name = "Service Info"; Method = "GET" },
    @{ Path = "/actuator/metrics"; Name = "Metrics"; Method = "GET" }
)

# Performance metrics
$results = @{
    StartTime = Get-Date
    EndTime = $null
    TotalRequests = 0
    SuccessfulRequests = 0
    FailedRequests = 0
    ResponseTimes = @()
    ErrorRate = 0
    AverageResponseTime = 0
    P95ResponseTime = 0
    MaxResponseTime = 0
    MinResponseTime = [int]::MaxValue
    ConcurrentUsers = $MaxConcurrentUsers
    TestDuration = $TestDurationSeconds
    ProductionReady = $false
}

# Function to test a single endpoint
function Test-Endpoint {
    param($Url, $EndpointName)
    
    $startTime = Get-Date
    try {
        $response = Invoke-WebRequest -Uri $Url -Method GET -TimeoutSec 10 -ErrorAction Stop
        $endTime = Get-Date
        $responseTime = ($endTime - $startTime).TotalMilliseconds
        
        $results.TotalRequests++
        if ($response.StatusCode -eq 200) {
            $results.SuccessfulRequests++
        } else {
            $results.FailedRequests++
        }
        
        $results.ResponseTimes += $responseTime
        
        if ($responseTime -lt $results.MinResponseTime) {
            $results.MinResponseTime = $responseTime
        }
        if ($responseTime -gt $results.MaxResponseTime) {
            $results.MaxResponseTime = $responseTime
        }
        
        return @{
            Success = $true
            ResponseTime = $responseTime
            StatusCode = $response.StatusCode
        }
    }
    catch {
        $endTime = Get-Date
        $responseTime = ($endTime - $startTime).TotalMilliseconds
        $results.TotalRequests++
        $results.FailedRequests++
        $results.ResponseTimes += $responseTime
        
        return @{
            Success = $false
            ResponseTime = $responseTime
            Error = $_.Exception.Message
        }
    }
}

# Function to run concurrent tests
function Start-ConcurrentTests {
    param($ConcurrentUsers, $DurationSeconds)
    
    Write-Host "Starting concurrent performance test..." -ForegroundColor Green
    Write-Host "Concurrent Users: $ConcurrentUsers" -ForegroundColor Yellow
    Write-Host "Duration: $DurationSeconds seconds" -ForegroundColor Yellow
    
    $jobs = @()
    $endTime = (Get-Date).AddSeconds($DurationSeconds)
    
    # Start concurrent jobs
    for ($i = 1; $i -le $ConcurrentUsers; $i++) {
        $job = Start-Job -ScriptBlock {
            param($BaseUrl, $Endpoints, $EndTime)
            
            $localResults = @()
            while ((Get-Date) -lt $EndTime) {
                foreach ($endpoint in $Endpoints) {
                    $url = "$BaseUrl$($endpoint.Path)"
                    $startTime = Get-Date
                    
                    try {
                        $response = Invoke-WebRequest -Uri $url -Method $endpoint.Method -TimeoutSec 5 -ErrorAction Stop
                        $responseTime = ((Get-Date) - $startTime).TotalMilliseconds
                        
                        $localResults += @{
                            Timestamp = Get-Date
                            Endpoint = $endpoint.Name
                            Success = $true
                            ResponseTime = $responseTime
                            StatusCode = $response.StatusCode
                        }
                    }
                    catch {
                        $responseTime = ((Get-Date) - $startTime).TotalMilliseconds
                        $localResults += @{
                            Timestamp = Get-Date
                            Endpoint = $endpoint.Name
                            Success = $false
                            ResponseTime = $responseTime
                            Error = $_.Exception.Message
                        }
                    }
                    
                    Start-Sleep -Milliseconds (Get-Random -Minimum 100 -Maximum 500)
                }
            }
            
            return $localResults
        } -ArgumentList $BaseUrl, $endpoints, $endTime
        
        $jobs += $job
        Start-Sleep -Milliseconds 50  # Stagger job starts
    }
    
    # Wait for all jobs to complete
    Write-Host "Waiting for test completion..." -ForegroundColor Yellow
    $allResults = @()
    foreach ($job in $jobs) {
        $jobResults = Receive-Job -Job $job -Wait
        $allResults += $jobResults
        Remove-Job -Job $job
    }
    
    return $allResults
}

# Run the performance test
Write-Host "Starting performance validation..." -ForegroundColor Green

# First, test basic connectivity
Write-Host "Testing basic connectivity..." -ForegroundColor Yellow
$connectivityTest = Test-Endpoint "$BaseUrl/actuator/health" "Health Check"
if (-not $connectivityTest.Success) {
    Write-Host "ERROR: Cannot connect to DSR system at $BaseUrl" -ForegroundColor Red
    Write-Host "Please ensure the DSR services are running." -ForegroundColor Red
    exit 1
}

Write-Host "✓ Basic connectivity successful" -ForegroundColor Green

# Run concurrent load test
$testResults = Start-ConcurrentTests -ConcurrentUsers $MaxConcurrentUsers -DurationSeconds $TestDurationSeconds

# Process results
Write-Host "Processing test results..." -ForegroundColor Yellow

$totalRequests = $testResults.Count
$successfulRequests = ($testResults | Where-Object { $_.Success -eq $true }).Count
$failedRequests = $totalRequests - $successfulRequests
$responseTimes = $testResults | ForEach-Object { $_.ResponseTime }

if ($responseTimes.Count -gt 0) {
    $averageResponseTime = ($responseTimes | Measure-Object -Average).Average
    $maxResponseTime = ($responseTimes | Measure-Object -Maximum).Maximum
    $minResponseTime = ($responseTimes | Measure-Object -Minimum).Minimum
    
    # Calculate 95th percentile
    $sortedTimes = $responseTimes | Sort-Object
    $p95Index = [math]::Floor($sortedTimes.Count * 0.95)
    $p95ResponseTime = $sortedTimes[$p95Index]
} else {
    $averageResponseTime = 0
    $maxResponseTime = 0
    $minResponseTime = 0
    $p95ResponseTime = 0
}

$errorRate = if ($totalRequests -gt 0) { ($failedRequests / $totalRequests) * 100 } else { 0 }

# Update results
$results.EndTime = Get-Date
$results.TotalRequests = $totalRequests
$results.SuccessfulRequests = $successfulRequests
$results.FailedRequests = $failedRequests
$results.ResponseTimes = $responseTimes
$results.ErrorRate = $errorRate
$results.AverageResponseTime = $averageResponseTime
$results.P95ResponseTime = $p95ResponseTime
$results.MaxResponseTime = $maxResponseTime
$results.MinResponseTime = $minResponseTime

# Determine production readiness
$results.ProductionReady = ($p95ResponseTime -lt 2000) -and ($errorRate -lt 5) -and ($totalRequests -gt 100)

# Display results
Write-Host ""
Write-Host "=== PERFORMANCE TEST RESULTS ===" -ForegroundColor Green
Write-Host "Total Requests: $totalRequests" -ForegroundColor White
Write-Host "Successful Requests: $successfulRequests" -ForegroundColor Green
Write-Host "Failed Requests: $failedRequests" -ForegroundColor $(if ($failedRequests -gt 0) { "Red" } else { "Green" })
Write-Host "Error Rate: $($errorRate.ToString("F2"))%" -ForegroundColor $(if ($errorRate -gt 5) { "Red" } else { "Green" })
Write-Host ""
Write-Host "Response Time Statistics:" -ForegroundColor Yellow
Write-Host "  Average: $($averageResponseTime.ToString("F2"))ms" -ForegroundColor White
Write-Host "  95th Percentile: $($p95ResponseTime.ToString("F2"))ms" -ForegroundColor $(if ($p95ResponseTime -gt 2000) { "Red" } else { "Green" })
Write-Host "  Maximum: $($maxResponseTime.ToString("F2"))ms" -ForegroundColor White
Write-Host "  Minimum: $($minResponseTime.ToString("F2"))ms" -ForegroundColor White
Write-Host ""
Write-Host "Production Readiness Criteria:" -ForegroundColor Yellow
Write-Host "  Response Time under 2000ms: $(if ($p95ResponseTime -lt 2000) { "PASS" } else { "FAIL" })" -ForegroundColor $(if ($p95ResponseTime -lt 2000) { "Green" } else { "Red" })
Write-Host "  Error Rate under 5%: $(if ($errorRate -lt 5) { "PASS" } else { "FAIL" })" -ForegroundColor $(if ($errorRate -lt 5) { "Green" } else { "Red" })
Write-Host "  Concurrent Users: $MaxConcurrentUsers" -ForegroundColor Green
Write-Host ""
Write-Host "Overall Status: $(if ($results.ProductionReady) { "PRODUCTION READY ✅" } else { "NEEDS OPTIMIZATION ⚠️" })" -ForegroundColor $(if ($results.ProductionReady) { "Green" } else { "Yellow" })

# Save results to JSON
$results | ConvertTo-Json -Depth 3 | Out-File -FilePath $reportFile -Encoding UTF8
Write-Host ""
Write-Host "Results saved to: $reportFile" -ForegroundColor Green

# Return exit code based on production readiness
if ($results.ProductionReady) {
    exit 0
} else {
    exit 1
}
