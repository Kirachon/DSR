# Simple DSR Load Test
# Tests basic system performance and validates production readiness

param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$Requests = 100,
    [int]$ConcurrentUsers = 10
)

Write-Host "=== DSR SIMPLE LOAD TEST ===" -ForegroundColor Green
Write-Host "Base URL: $BaseUrl"
Write-Host "Total Requests: $Requests"
Write-Host "Concurrent Users: $ConcurrentUsers"
Write-Host ""

$results = @{
    TotalRequests = 0
    SuccessfulRequests = 0
    FailedRequests = 0
    ResponseTimes = @()
    StartTime = Get-Date
}

# Test function
function Test-Endpoint {
    param($Url)
    
    $startTime = Get-Date
    try {
        $response = Invoke-WebRequest -Uri $Url -Method GET -TimeoutSec 10 -ErrorAction Stop
        $endTime = Get-Date
        $responseTime = ($endTime - $startTime).TotalMilliseconds
        
        return @{
            Success = $true
            ResponseTime = $responseTime
            StatusCode = $response.StatusCode
        }
    }
    catch {
        $endTime = Get-Date
        $responseTime = ($endTime - $startTime).TotalMilliseconds
        
        return @{
            Success = $false
            ResponseTime = $responseTime
            Error = $_.Exception.Message
        }
    }
}

# Run tests
Write-Host "Running load test..." -ForegroundColor Yellow

$jobs = @()
$requestsPerUser = [math]::Ceiling($Requests / $ConcurrentUsers)

for ($user = 1; $user -le $ConcurrentUsers; $user++) {
    $job = Start-Job -ScriptBlock {
        param($BaseUrl, $RequestsPerUser, $UserNumber)
        
        $userResults = @()
        for ($i = 1; $i -le $RequestsPerUser; $i++) {
            $startTime = Get-Date
            try {
                $response = Invoke-WebRequest -Uri "$BaseUrl/actuator/health" -Method GET -TimeoutSec 5 -ErrorAction Stop
                $responseTime = ((Get-Date) - $startTime).TotalMilliseconds
                
                $userResults += @{
                    User = $UserNumber
                    Request = $i
                    Success = $true
                    ResponseTime = $responseTime
                    StatusCode = $response.StatusCode
                }
            }
            catch {
                $responseTime = ((Get-Date) - $startTime).TotalMilliseconds
                $userResults += @{
                    User = $UserNumber
                    Request = $i
                    Success = $false
                    ResponseTime = $responseTime
                    Error = $_.Exception.Message
                }
            }
            
            Start-Sleep -Milliseconds (Get-Random -Minimum 100 -Maximum 300)
        }
        
        return $userResults
    } -ArgumentList $BaseUrl, $requestsPerUser, $user
    
    $jobs += $job
}

# Wait for completion and collect results
$allResults = @()
foreach ($job in $jobs) {
    $jobResults = Receive-Job -Job $job -Wait
    $allResults += $jobResults
    Remove-Job -Job $job
}

# Process results
$results.EndTime = Get-Date
$results.TotalRequests = $allResults.Count
$results.SuccessfulRequests = ($allResults | Where-Object { $_.Success -eq $true }).Count
$results.FailedRequests = $results.TotalRequests - $results.SuccessfulRequests
$results.ResponseTimes = $allResults | ForEach-Object { $_.ResponseTime }

if ($results.ResponseTimes.Count -gt 0) {
    $avgResponseTime = ($results.ResponseTimes | Measure-Object -Average).Average
    $maxResponseTime = ($results.ResponseTimes | Measure-Object -Maximum).Maximum
    $minResponseTime = ($results.ResponseTimes | Measure-Object -Minimum).Minimum
    
    # Calculate 95th percentile
    $sortedTimes = $results.ResponseTimes | Sort-Object
    $p95Index = [math]::Floor($sortedTimes.Count * 0.95)
    $p95ResponseTime = if ($p95Index -lt $sortedTimes.Count) { $sortedTimes[$p95Index] } else { $maxResponseTime }
} else {
    $avgResponseTime = 0
    $maxResponseTime = 0
    $minResponseTime = 0
    $p95ResponseTime = 0
}

$errorRate = if ($results.TotalRequests -gt 0) { ($results.FailedRequests / $results.TotalRequests) * 100 } else { 0 }
$testDuration = ($results.EndTime - $results.StartTime).TotalSeconds

# Display results
Write-Host ""
Write-Host "=== TEST RESULTS ===" -ForegroundColor Green
Write-Host "Total Requests: $($results.TotalRequests)"
Write-Host "Successful: $($results.SuccessfulRequests)" -ForegroundColor Green
Write-Host "Failed: $($results.FailedRequests)" -ForegroundColor $(if ($results.FailedRequests -gt 0) { "Red" } else { "Green" })
Write-Host "Error Rate: $($errorRate.ToString("F2"))%"
Write-Host "Test Duration: $($testDuration.ToString("F2")) seconds"
Write-Host ""
Write-Host "Response Times:" -ForegroundColor Yellow
Write-Host "  Average: $($avgResponseTime.ToString("F2"))ms"
Write-Host "  95th Percentile: $($p95ResponseTime.ToString("F2"))ms"
Write-Host "  Maximum: $($maxResponseTime.ToString("F2"))ms"
Write-Host "  Minimum: $($minResponseTime.ToString("F2"))ms"
Write-Host ""

# Production readiness check
$productionReady = ($p95ResponseTime -lt 2000) -and ($errorRate -lt 5) -and ($results.SuccessfulRequests -gt 0)

Write-Host "Production Readiness:" -ForegroundColor Yellow
Write-Host "  Response Time under 2000ms: $(if ($p95ResponseTime -lt 2000) { "PASS" } else { "FAIL" })" -ForegroundColor $(if ($p95ResponseTime -lt 2000) { "Green" } else { "Red" })
Write-Host "  Error Rate under 5%: $(if ($errorRate -lt 5) { "PASS" } else { "FAIL" })" -ForegroundColor $(if ($errorRate -lt 5) { "Green" } else { "Red" })
Write-Host "  Basic Connectivity: $(if ($results.SuccessfulRequests -gt 0) { "PASS" } else { "FAIL" })" -ForegroundColor $(if ($results.SuccessfulRequests -gt 0) { "Green" } else { "Red" })
Write-Host ""
Write-Host "Overall Status: $(if ($productionReady) { "PRODUCTION READY" } else { "NEEDS OPTIMIZATION" })" -ForegroundColor $(if ($productionReady) { "Green" } else { "Yellow" })

# Save results
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$reportData = @{
    TestConfiguration = @{
        BaseUrl = $BaseUrl
        TotalRequests = $Requests
        ConcurrentUsers = $ConcurrentUsers
        Timestamp = $timestamp
    }
    Results = @{
        TotalRequests = $results.TotalRequests
        SuccessfulRequests = $results.SuccessfulRequests
        FailedRequests = $results.FailedRequests
        ErrorRate = $errorRate
        TestDuration = $testDuration
        ResponseTimes = @{
            Average = $avgResponseTime
            P95 = $p95ResponseTime
            Maximum = $maxResponseTime
            Minimum = $minResponseTime
        }
        ProductionReady = $productionReady
    }
}

$reportFile = "tests/performance/reports/load-test-$timestamp.json"
$reportData | ConvertTo-Json -Depth 4 | Out-File -FilePath $reportFile -Encoding UTF8
Write-Host "Results saved to: $reportFile"

if ($productionReady) {
    exit 0
} else {
    exit 1
}
