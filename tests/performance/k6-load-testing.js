// DSR K6 Load Testing Framework
// Comprehensive production-level load testing for all 7 DSR services
// Supports 1000+ concurrent users with realistic production scenarios

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
import { htmlReport } from 'https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.1/index.js';

// Custom metrics
const errorRate = new Rate('errors');
const responseTime = new Trend('response_time');
const requestCount = new Counter('requests');

// Test configuration
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const API_BASE_URL = __ENV.API_BASE_URL || 'http://localhost:8080';

// Performance thresholds for production readiness
export const options = {
  stages: [
    // Ramp-up phase
    { duration: '2m', target: 100 },   // Ramp up to 100 users
    { duration: '3m', target: 500 },   // Ramp up to 500 users
    { duration: '5m', target: 1000 },  // Ramp up to 1000 users
    { duration: '10m', target: 1000 }, // Stay at 1000 users for 10 minutes
    { duration: '3m', target: 500 },   // Ramp down to 500 users
    { duration: '2m', target: 0 },     // Ramp down to 0 users
  ],
  thresholds: {
    // Performance requirements for production readiness
    http_req_duration: ['p(95)<2000'], // 95% of requests must complete within 2 seconds
    http_req_failed: ['rate<0.05'],    // Error rate must be less than 5%
    errors: ['rate<0.05'],             // Custom error rate threshold
    response_time: ['p(95)<2000'],     // 95th percentile response time
    requests: ['count>10000'],         // Minimum number of requests
  },
  ext: {
    loadimpact: {
      projectID: 3595341,
      name: 'DSR Production Load Test'
    }
  }
};

// Service endpoints configuration
const SERVICES = {
  registration: `${API_BASE_URL}/api/v1/registration`,
  dataManagement: `${API_BASE_URL}/api/v1/data-management`,
  eligibility: `${API_BASE_URL}/api/v1/eligibility`,
  payment: `${API_BASE_URL}/api/v1/payments`,
  interoperability: `${API_BASE_URL}/api/v1/interoperability`,
  grievance: `${API_BASE_URL}/api/v1/grievances`,
  analytics: `${API_BASE_URL}/api/v1/analytics`
};

// Authentication token (will be obtained during setup)
let authToken = '';

// Test data generators
function generateHouseholdData(userId) {
  return {
    householdHead: {
      firstName: `LoadTest${userId}`,
      lastName: `User${userId}`,
      birthDate: '1985-01-01',
      philsysId: `PSN-LOAD-${userId.toString().padStart(8, '0')}`,
      relationship: 'HEAD',
      email: `loadtest${userId}@dsr.gov.ph`,
      phoneNumber: `+6391${userId.toString().padStart(8, '0')}`
    },
    address: {
      region: 'NCR',
      province: 'Metro Manila',
      city: 'Manila',
      barangay: `Load Test Barangay ${userId}`,
      street: `Load Test Street ${userId}`,
      zipCode: '1000'
    },
    members: [
      {
        firstName: `Spouse${userId}`,
        lastName: `User${userId}`,
        birthDate: '1987-02-02',
        relationship: 'SPOUSE',
        philsysId: `PSN-SPOUSE-${userId.toString().padStart(8, '0')}`
      }
    ],
    economicProfile: {
      monthlyIncome: 15000 + (userId % 50000),
      employmentStatus: 'EMPLOYED',
      householdAssets: ['HOUSE'],
      vulnerabilityIndicators: ['NONE']
    }
  };
}

function generatePaymentData(userId) {
  return {
    beneficiaryId: `BEN-${userId.toString().padStart(8, '0')}`,
    programId: 'PANTAWID',
    amount: 1500 + (userId % 1000),
    paymentMethod: 'BANK_TRANSFER',
    scheduledDate: new Date().toISOString().split('T')[0]
  };
}

function generateGrievanceData(userId) {
  return {
    complainantName: `LoadTest${userId} User${userId}`,
    complainantEmail: `loadtest${userId}@dsr.gov.ph`,
    category: 'ELIGIBILITY_DISPUTE',
    priority: 'MEDIUM',
    description: `Load test grievance ${userId} - automated testing complaint`,
    relatedHouseholdId: `HH-${userId.toString().padStart(8, '0')}`
  };
}

// Setup function - authenticate and prepare test environment
export function setup() {
  console.log('🚀 Starting DSR Load Testing Setup...');
  
  // Authenticate to get access token
  const authResponse = http.post(`${API_BASE_URL}/api/v1/auth/login`, JSON.stringify({
    email: 'dswd.staff@test.ph',
    password: 'TestPassword123!'
  }), {
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (authResponse.status === 200) {
    const authData = authResponse.json();
    authToken = authData.accessToken;
    console.log('✅ Authentication successful');
    return { authToken: authToken };
  } else {
    console.error('❌ Authentication failed:', authResponse.status);
    return { authToken: '' };
  }
}

// Main test function - executed by each virtual user
export default function(data) {
  const token = data.authToken || authToken;
  const userId = __VU * 1000 + __ITER; // Unique user ID
  
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };

  // Test scenario selection based on virtual user ID
  const scenario = userId % 7; // Distribute load across 7 different scenarios
  
  switch (scenario) {
    case 0:
      testRegistrationService(headers, userId);
      break;
    case 1:
      testDataManagementService(headers, userId);
      break;
    case 2:
      testEligibilityService(headers, userId);
      break;
    case 3:
      testPaymentService(headers, userId);
      break;
    case 4:
      testInteroperabilityService(headers, userId);
      break;
    case 5:
      testGrievanceService(headers, userId);
      break;
    case 6:
      testAnalyticsService(headers, userId);
      break;
  }

  // Random sleep between 1-3 seconds to simulate user think time
  sleep(Math.random() * 2 + 1);
}

// Individual service test functions
function testRegistrationService(headers, userId) {
  const startTime = Date.now();
  
  // Test household registration
  const householdData = generateHouseholdData(userId);
  const response = http.post(`${SERVICES.registration}/households`, 
    JSON.stringify(householdData), { headers });
  
  const duration = Date.now() - startTime;
  responseTime.add(duration);
  requestCount.add(1);
  
  const success = check(response, {
    'Registration: status is 200 or 201': (r) => r.status === 200 || r.status === 201,
    'Registration: response time < 2s': (r) => duration < 2000,
    'Registration: has response body': (r) => r.body.length > 0,
  });
  
  if (!success) {
    errorRate.add(1);
    console.error(`Registration failed for user ${userId}: ${response.status}`);
  }
}

function testDataManagementService(headers, userId) {
  const startTime = Date.now();
  
  // Test data ingestion health check
  const response = http.get(`${SERVICES.dataManagement}/health`, { headers });
  
  const duration = Date.now() - startTime;
  responseTime.add(duration);
  requestCount.add(1);
  
  const success = check(response, {
    'Data Management: status is 200': (r) => r.status === 200,
    'Data Management: response time < 2s': (r) => duration < 2000,
  });
  
  if (!success) {
    errorRate.add(1);
  }
}

function testEligibilityService(headers, userId) {
  const startTime = Date.now();
  
  // Test eligibility assessment
  const assessmentData = {
    householdId: `HH-${userId.toString().padStart(8, '0')}`,
    assessmentType: 'FULL_ASSESSMENT'
  };
  
  const response = http.post(`${SERVICES.eligibility}/assessments`, 
    JSON.stringify(assessmentData), { headers });
  
  const duration = Date.now() - startTime;
  responseTime.add(duration);
  requestCount.add(1);
  
  const success = check(response, {
    'Eligibility: status is 200 or 201': (r) => r.status === 200 || r.status === 201,
    'Eligibility: response time < 2s': (r) => duration < 2000,
  });
  
  if (!success) {
    errorRate.add(1);
  }
}

function testPaymentService(headers, userId) {
  const startTime = Date.now();
  
  // Test payment creation
  const paymentData = generatePaymentData(userId);
  const response = http.post(`${SERVICES.payment}`, 
    JSON.stringify(paymentData), { headers });
  
  const duration = Date.now() - startTime;
  responseTime.add(duration);
  requestCount.add(1);
  
  const success = check(response, {
    'Payment: status is 200 or 201': (r) => r.status === 200 || r.status === 201,
    'Payment: response time < 2s': (r) => duration < 2000,
  });
  
  if (!success) {
    errorRate.add(1);
  }
}

function testInteroperabilityService(headers, userId) {
  const startTime = Date.now();
  
  // Test service health
  const response = http.get(`${SERVICES.interoperability}/health`, { headers });
  
  const duration = Date.now() - startTime;
  responseTime.add(duration);
  requestCount.add(1);
  
  const success = check(response, {
    'Interoperability: status is 200': (r) => r.status === 200,
    'Interoperability: response time < 2s': (r) => duration < 2000,
  });
  
  if (!success) {
    errorRate.add(1);
  }
}

function testGrievanceService(headers, userId) {
  const startTime = Date.now();
  
  // Test grievance submission
  const grievanceData = generateGrievanceData(userId);
  const response = http.post(`${SERVICES.grievance}`, 
    JSON.stringify(grievanceData), { headers });
  
  const duration = Date.now() - startTime;
  responseTime.add(duration);
  requestCount.add(1);
  
  const success = check(response, {
    'Grievance: status is 200 or 201': (r) => r.status === 200 || r.status === 201,
    'Grievance: response time < 2s': (r) => duration < 2000,
  });
  
  if (!success) {
    errorRate.add(1);
  }
}

function testAnalyticsService(headers, userId) {
  const startTime = Date.now();
  
  // Test analytics dashboard
  const response = http.get(`${SERVICES.analytics}/dashboards/system-overview`, { headers });
  
  const duration = Date.now() - startTime;
  responseTime.add(duration);
  requestCount.add(1);
  
  const success = check(response, {
    'Analytics: status is 200': (r) => r.status === 200,
    'Analytics: response time < 2s': (r) => duration < 2000,
  });
  
  if (!success) {
    errorRate.add(1);
  }
}

// Generate comprehensive test report
export function handleSummary(data) {
  return {
    'reports/k6-load-test-report.html': htmlReport(data),
    'reports/k6-load-test-summary.txt': textSummary(data, { indent: ' ', enableColors: true }),
    'reports/k6-load-test-results.json': JSON.stringify(data),
  };
}

// Teardown function - cleanup after testing
export function teardown(data) {
  console.log('🧹 Load testing completed. Cleaning up...');
  console.log('📊 Check reports/ directory for detailed results');
}
