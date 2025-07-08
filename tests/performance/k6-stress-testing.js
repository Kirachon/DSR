// DSR K6 Stress Testing Framework
// Extreme load testing to identify system breaking points
// Tests system behavior under stress conditions beyond normal capacity

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter, Gauge } from 'k6/metrics';

// Custom metrics for stress testing
const errorRate = new Rate('stress_errors');
const responseTime = new Trend('stress_response_time');
const requestCount = new Counter('stress_requests');
const activeUsers = new Gauge('active_users');

// Test configuration for stress testing
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const API_BASE_URL = __ENV.API_BASE_URL || 'http://localhost:8080';

export const options = {
  stages: [
    // Gradual ramp-up to identify breaking point
    { duration: '2m', target: 500 },    // Normal load
    { duration: '3m', target: 1000 },   // High load
    { duration: '3m', target: 1500 },   // Stress load
    { duration: '3m', target: 2000 },   // Extreme stress
    { duration: '5m', target: 2500 },   // Breaking point test
    { duration: '3m', target: 1000 },   // Recovery test
    { duration: '2m', target: 0 },      // Cool down
  ],
  thresholds: {
    // Relaxed thresholds for stress testing
    http_req_duration: ['p(95)<5000'],  // 95% under 5 seconds (stress conditions)
    http_req_failed: ['rate<0.20'],     // Up to 20% failure rate acceptable in stress test
    stress_errors: ['rate<0.25'],       // 25% error rate threshold for stress
    stress_response_time: ['p(90)<3000'], // 90th percentile under 3 seconds
  }
};

// Service endpoints
const SERVICES = {
  registration: `${API_BASE_URL}/api/v1/registration`,
  dataManagement: `${API_BASE_URL}/api/v1/data-management`,
  eligibility: `${API_BASE_URL}/api/v1/eligibility`,
  payment: `${API_BASE_URL}/api/v1/payments`,
  interoperability: `${API_BASE_URL}/api/v1/interoperability`,
  grievance: `${API_BASE_URL}/api/v1/grievances`,
  analytics: `${API_BASE_URL}/api/v1/analytics`
};

let authToken = '';

// Stress test data generators with higher volume
function generateStressHouseholdData(userId) {
  return {
    householdHead: {
      firstName: `StressTest${userId}`,
      lastName: `User${userId}`,
      birthDate: '1985-01-01',
      philsysId: `PSN-STRESS-${userId.toString().padStart(10, '0')}`,
      relationship: 'HEAD',
      email: `stresstest${userId}@dsr.gov.ph`,
      phoneNumber: `+6391${userId.toString().padStart(8, '0')}`
    },
    address: {
      region: 'NCR',
      province: 'Metro Manila',
      city: 'Manila',
      barangay: `Stress Test Barangay ${userId}`,
      street: `Stress Test Street ${userId}`,
      zipCode: '1000'
    },
    members: generateLargeFamily(userId), // Generate larger families for stress testing
    economicProfile: {
      monthlyIncome: 10000 + (userId % 100000),
      employmentStatus: 'EMPLOYED',
      householdAssets: ['HOUSE', 'VEHICLE'],
      vulnerabilityIndicators: ['NONE']
    }
  };
}

function generateLargeFamily(userId) {
  const familySize = 3 + (userId % 5); // 3-7 family members
  const members = [];
  
  for (let i = 1; i <= familySize; i++) {
    members.push({
      firstName: `Member${i}`,
      lastName: `User${userId}`,
      birthDate: `198${i}-0${i}-0${i}`,
      relationship: i === 1 ? 'SPOUSE' : 'CHILD',
      philsysId: `PSN-MEMBER-${userId}-${i.toString().padStart(3, '0')}`
    });
  }
  
  return members;
}

// Setup for stress testing
export function setup() {
  console.log('🔥 Starting DSR Stress Testing Setup...');
  
  const authResponse = http.post(`${API_BASE_URL}/api/v1/auth/login`, JSON.stringify({
    email: 'dswd.staff@test.ph',
    password: 'TestPassword123!'
  }), {
    headers: { 'Content-Type': 'application/json' },
  });

  if (authResponse.status === 200) {
    const authData = authResponse.json();
    console.log('✅ Stress test authentication successful');
    return { authToken: authData.accessToken };
  } else {
    console.error('❌ Stress test authentication failed:', authResponse.status);
    return { authToken: '' };
  }
}

// Main stress test function
export default function(data) {
  const token = data.authToken || authToken;
  const userId = __VU * 10000 + __ITER; // Higher user ID range for stress testing
  
  activeUsers.add(__VU);
  
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };

  // Aggressive testing pattern - multiple operations per iteration
  const operations = [
    () => stressTestRegistration(headers, userId),
    () => stressTestEligibility(headers, userId),
    () => stressTestPayment(headers, userId),
    () => stressTestGrievance(headers, userId),
    () => stressTestAnalytics(headers, userId)
  ];

  // Execute multiple operations rapidly
  const numOperations = 2 + (userId % 3); // 2-4 operations per user
  for (let i = 0; i < numOperations; i++) {
    const operation = operations[i % operations.length];
    operation();
    
    // Minimal sleep to maintain high load
    sleep(0.1 + Math.random() * 0.2);
  }
}

function stressTestRegistration(headers, userId) {
  const startTime = Date.now();
  
  const householdData = generateStressHouseholdData(userId);
  const response = http.post(`${SERVICES.registration}/households`, 
    JSON.stringify(householdData), { 
      headers,
      timeout: '10s' // Longer timeout for stress conditions
    });
  
  const duration = Date.now() - startTime;
  responseTime.add(duration);
  requestCount.add(1);
  
  const success = check(response, {
    'Stress Registration: not 5xx error': (r) => r.status < 500,
    'Stress Registration: response time < 10s': (r) => duration < 10000,
  });
  
  if (!success) {
    errorRate.add(1);
    console.log(`Stress registration failed for user ${userId}: ${response.status} (${duration}ms)`);
  }
}

function stressTestEligibility(headers, userId) {
  const startTime = Date.now();
  
  const assessmentData = {
    householdId: `HH-STRESS-${userId.toString().padStart(10, '0')}`,
    assessmentType: 'FULL_ASSESSMENT'
  };
  
  const response = http.post(`${SERVICES.eligibility}/assessments`, 
    JSON.stringify(assessmentData), { 
      headers,
      timeout: '10s'
    });
  
  const duration = Date.now() - startTime;
  responseTime.add(duration);
  requestCount.add(1);
  
  const success = check(response, {
    'Stress Eligibility: not 5xx error': (r) => r.status < 500,
    'Stress Eligibility: response time < 10s': (r) => duration < 10000,
  });
  
  if (!success) {
    errorRate.add(1);
  }
}

function stressTestPayment(headers, userId) {
  const startTime = Date.now();
  
  const paymentData = {
    beneficiaryId: `BEN-STRESS-${userId.toString().padStart(10, '0')}`,
    programId: 'PANTAWID',
    amount: 1500 + (userId % 5000),
    paymentMethod: 'BANK_TRANSFER',
    scheduledDate: new Date().toISOString().split('T')[0]
  };
  
  const response = http.post(`${SERVICES.payment}`, 
    JSON.stringify(paymentData), { 
      headers,
      timeout: '10s'
    });
  
  const duration = Date.now() - startTime;
  responseTime.add(duration);
  requestCount.add(1);
  
  const success = check(response, {
    'Stress Payment: not 5xx error': (r) => r.status < 500,
    'Stress Payment: response time < 10s': (r) => duration < 10000,
  });
  
  if (!success) {
    errorRate.add(1);
  }
}

function stressTestGrievance(headers, userId) {
  const startTime = Date.now();
  
  const grievanceData = {
    complainantName: `StressTest${userId} User${userId}`,
    complainantEmail: `stresstest${userId}@dsr.gov.ph`,
    category: 'SYSTEM_ERROR',
    priority: 'HIGH',
    description: `Stress test grievance ${userId} - high load testing complaint`,
    relatedHouseholdId: `HH-STRESS-${userId.toString().padStart(10, '0')}`
  };
  
  const response = http.post(`${SERVICES.grievance}`, 
    JSON.stringify(grievanceData), { 
      headers,
      timeout: '10s'
    });
  
  const duration = Date.now() - startTime;
  responseTime.add(duration);
  requestCount.add(1);
  
  const success = check(response, {
    'Stress Grievance: not 5xx error': (r) => r.status < 500,
    'Stress Grievance: response time < 10s': (r) => duration < 10000,
  });
  
  if (!success) {
    errorRate.add(1);
  }
}

function stressTestAnalytics(headers, userId) {
  const startTime = Date.now();
  
  // Test multiple analytics endpoints rapidly
  const endpoints = [
    '/dashboards/system-overview',
    '/health',
    '/metrics/performance'
  ];
  
  const endpoint = endpoints[userId % endpoints.length];
  const response = http.get(`${SERVICES.analytics}${endpoint}`, { 
    headers,
    timeout: '10s'
  });
  
  const duration = Date.now() - startTime;
  responseTime.add(duration);
  requestCount.add(1);
  
  const success = check(response, {
    'Stress Analytics: not 5xx error': (r) => r.status < 500,
    'Stress Analytics: response time < 10s': (r) => duration < 10000,
  });
  
  if (!success) {
    errorRate.add(1);
  }
}

// Generate stress test report
export function handleSummary(data) {
  const report = {
    'reports/k6-stress-test-report.html': generateStressReport(data),
    'reports/k6-stress-test-summary.txt': generateStressSummary(data),
    'reports/k6-stress-test-results.json': JSON.stringify(data),
  };
  
  return report;
}

function generateStressReport(data) {
  return `
<!DOCTYPE html>
<html>
<head>
    <title>DSR Stress Test Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .metric { margin: 10px 0; padding: 10px; border-left: 4px solid #007cba; }
        .error { border-left-color: #d32f2f; }
        .warning { border-left-color: #f57c00; }
        .success { border-left-color: #388e3c; }
    </style>
</head>
<body>
    <h1>DSR System Stress Test Report</h1>
    <h2>Test Summary</h2>
    <div class="metric">
        <strong>Total Requests:</strong> ${data.metrics.stress_requests?.values?.count || 0}
    </div>
    <div class="metric">
        <strong>Error Rate:</strong> ${((data.metrics.stress_errors?.values?.rate || 0) * 100).toFixed(2)}%
    </div>
    <div class="metric">
        <strong>Average Response Time:</strong> ${(data.metrics.stress_response_time?.values?.avg || 0).toFixed(2)}ms
    </div>
    <div class="metric">
        <strong>95th Percentile Response Time:</strong> ${(data.metrics.stress_response_time?.values?.['p(95)'] || 0).toFixed(2)}ms
    </div>
    <div class="metric">
        <strong>Max Active Users:</strong> ${data.metrics.active_users?.values?.max || 0}
    </div>
    
    <h2>Performance Analysis</h2>
    <div class="metric ${(data.metrics.stress_errors?.values?.rate || 0) < 0.20 ? 'success' : 'error'}">
        <strong>System Stability:</strong> ${(data.metrics.stress_errors?.values?.rate || 0) < 0.20 ? 'PASSED' : 'FAILED'}
        <br>Error rate under stress conditions: ${((data.metrics.stress_errors?.values?.rate || 0) * 100).toFixed(2)}%
    </div>
    
    <div class="metric ${(data.metrics.stress_response_time?.values?.['p(95)'] || 0) < 5000 ? 'success' : 'warning'}">
        <strong>Response Time:</strong> ${(data.metrics.stress_response_time?.values?.['p(95)'] || 0) < 5000 ? 'ACCEPTABLE' : 'DEGRADED'}
        <br>95th percentile: ${(data.metrics.stress_response_time?.values?.['p(95)'] || 0).toFixed(2)}ms
    </div>
    
    <h2>Recommendations</h2>
    <ul>
        <li>Monitor system behavior at ${data.metrics.active_users?.values?.max || 0} concurrent users</li>
        <li>Consider horizontal scaling if error rate exceeds 15%</li>
        <li>Implement circuit breakers for response times > 3 seconds</li>
        <li>Review database connection pooling and caching strategies</li>
    </ul>
</body>
</html>`;
}

function generateStressSummary(data) {
  return `
DSR STRESS TEST SUMMARY
=======================

Test Configuration:
- Maximum Users: ${data.metrics.active_users?.values?.max || 0}
- Total Requests: ${data.metrics.stress_requests?.values?.count || 0}
- Test Duration: ${data.state?.testRunDurationMs ? (data.state.testRunDurationMs / 1000).toFixed(0) : 0}s

Performance Metrics:
- Error Rate: ${((data.metrics.stress_errors?.values?.rate || 0) * 100).toFixed(2)}%
- Avg Response Time: ${(data.metrics.stress_response_time?.values?.avg || 0).toFixed(2)}ms
- 95th Percentile: ${(data.metrics.stress_response_time?.values?.['p(95)'] || 0).toFixed(2)}ms
- Max Response Time: ${(data.metrics.stress_response_time?.values?.max || 0).toFixed(2)}ms

System Status:
- Stability: ${(data.metrics.stress_errors?.values?.rate || 0) < 0.20 ? 'PASSED' : 'FAILED'}
- Performance: ${(data.metrics.stress_response_time?.values?.['p(95)'] || 0) < 5000 ? 'ACCEPTABLE' : 'DEGRADED'}

Breaking Point Analysis:
- System maintained ${((1 - (data.metrics.stress_errors?.values?.rate || 0)) * 100).toFixed(1)}% availability under extreme load
- Response time degradation: ${((data.metrics.stress_response_time?.values?.['p(95)'] || 0) / 2000).toFixed(1)}x normal load
`;
}

export function teardown(data) {
  console.log('🔥 Stress testing completed');
  console.log(`📊 Maximum concurrent users tested: ${data.metrics?.active_users?.values?.max || 0}`);
  console.log(`📈 Total requests processed: ${data.metrics?.stress_requests?.values?.count || 0}`);
  console.log(`⚠️  Error rate under stress: ${((data.metrics?.stress_errors?.values?.rate || 0) * 100).toFixed(2)}%`);
}
