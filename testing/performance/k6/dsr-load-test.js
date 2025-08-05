import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const responseTime = new Trend('response_time');
const requestCount = new Counter('requests');

// Test configuration
export const options = {
  stages: [
    // Ramp-up phase
    { duration: '2m', target: 100 },   // Ramp up to 100 users
    { duration: '5m', target: 500 },   // Ramp up to 500 users
    { duration: '10m', target: 1000 }, // Ramp up to 1000 users
    { duration: '15m', target: 1000 }, // Stay at 1000 users
    { duration: '5m', target: 1500 },  // Peak load test
    { duration: '5m', target: 1500 },  // Sustain peak load
    { duration: '5m', target: 0 },     // Ramp down
  ],
  thresholds: {
    // Performance requirements
    http_req_duration: ['p(95)<2000'], // 95% of requests under 2s
    http_req_failed: ['rate<0.01'],    // Error rate under 1%
    errors: ['rate<0.01'],             // Custom error rate under 1%
    response_time: ['p(99)<3000'],     // 99% under 3s
  },
};

// Base URL configuration
const BASE_URL = __ENV.BASE_URL || 'https://api.dsr.gov.ph';

// Authentication tokens (would be generated dynamically in real scenario)
const CITIZEN_TOKEN = __ENV.CITIZEN_TOKEN || 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...';
const STAFF_TOKEN = __ENV.STAFF_TOKEN || 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...';

// Test data
const testCitizens = [
  { id: 'C001', name: 'Juan Dela Cruz', email: 'juan@example.com' },
  { id: 'C002', name: 'Maria Santos', email: 'maria@example.com' },
  { id: 'C003', name: 'Jose Rizal', email: 'jose@example.com' },
];

export default function () {
  const userType = Math.random() < 0.8 ? 'citizen' : 'staff';
  const token = userType === 'citizen' ? CITIZEN_TOKEN : STAFF_TOKEN;
  
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
    'User-Agent': 'DSR-LoadTest/1.0',
  };

  // Simulate different user workflows
  if (userType === 'citizen') {
    citizenWorkflow(headers);
  } else {
    staffWorkflow(headers);
  }
  
  sleep(1); // Think time between requests
}

function citizenWorkflow(headers) {
  const citizen = testCitizens[Math.floor(Math.random() * testCitizens.length)];
  
  // 1. Check registration status
  let response = http.get(`${BASE_URL}/api/v1/registration/status/${citizen.id}`, { headers });
  check(response, {
    'registration status check successful': (r) => r.status === 200,
    'response time < 2s': (r) => r.timings.duration < 2000,
  });
  recordMetrics(response, 'registration_status');
  
  sleep(0.5);
  
  // 2. Get eligibility information
  response = http.get(`${BASE_URL}/api/v1/eligibility/check/${citizen.id}`, { headers });
  check(response, {
    'eligibility check successful': (r) => r.status === 200,
    'response time < 2s': (r) => r.timings.duration < 2000,
  });
  recordMetrics(response, 'eligibility_check');
  
  sleep(0.5);
  
  // 3. Submit application (30% of users)
  if (Math.random() < 0.3) {
    const applicationData = {
      citizenId: citizen.id,
      programType: 'PANTAWID_PAMILYA',
      householdSize: Math.floor(Math.random() * 8) + 1,
      monthlyIncome: Math.floor(Math.random() * 15000),
    };
    
    response = http.post(`${BASE_URL}/api/v1/registration/apply`, 
      JSON.stringify(applicationData), { headers });
    check(response, {
      'application submission successful': (r) => r.status === 201,
      'response time < 3s': (r) => r.timings.duration < 3000,
    });
    recordMetrics(response, 'application_submit');
  }
  
  sleep(0.5);
  
  // 4. Check payment status (if applicable)
  if (Math.random() < 0.4) {
    response = http.get(`${BASE_URL}/api/v1/payments/status/${citizen.id}`, { headers });
    check(response, {
      'payment status check successful': (r) => r.status === 200,
      'response time < 2s': (r) => r.timings.duration < 2000,
    });
    recordMetrics(response, 'payment_status');
  }
}

function staffWorkflow(headers) {
  // 1. Get dashboard data
  let response = http.get(`${BASE_URL}/api/v1/analytics/dashboard`, { headers });
  check(response, {
    'dashboard load successful': (r) => r.status === 200,
    'response time < 3s': (r) => r.timings.duration < 3000,
  });
  recordMetrics(response, 'dashboard_load');
  
  sleep(1);
  
  // 2. Search applications
  const searchParams = {
    status: 'PENDING',
    limit: 20,
    offset: 0,
  };
  
  response = http.get(`${BASE_URL}/api/v1/registration/search?${new URLSearchParams(searchParams)}`, { headers });
  check(response, {
    'application search successful': (r) => r.status === 200,
    'response time < 2s': (r) => r.timings.duration < 2000,
  });
  recordMetrics(response, 'application_search');
  
  sleep(0.5);
  
  // 3. Review application (20% of staff users)
  if (Math.random() < 0.2) {
    const applicationId = `APP${Math.floor(Math.random() * 10000)}`;
    response = http.get(`${BASE_URL}/api/v1/registration/applications/${applicationId}`, { headers });
    check(response, {
      'application review successful': (r) => r.status === 200 || r.status === 404,
      'response time < 2s': (r) => r.timings.duration < 2000,
    });
    recordMetrics(response, 'application_review');
    
    // 4. Update application status (if found)
    if (response.status === 200) {
      const updateData = {
        status: Math.random() < 0.7 ? 'APPROVED' : 'REQUIRES_REVIEW',
        comments: 'Reviewed by automated load test',
      };
      
      response = http.put(`${BASE_URL}/api/v1/registration/applications/${applicationId}/status`, 
        JSON.stringify(updateData), { headers });
      check(response, {
        'status update successful': (r) => r.status === 200,
        'response time < 2s': (r) => r.timings.duration < 2000,
      });
      recordMetrics(response, 'status_update');
    }
  }
  
  sleep(0.5);
  
  // 5. Generate report (10% of staff users)
  if (Math.random() < 0.1) {
    const reportParams = {
      type: 'MONTHLY_SUMMARY',
      startDate: '2025-07-01',
      endDate: '2025-07-31',
    };
    
    response = http.post(`${BASE_URL}/api/v1/analytics/reports`, 
      JSON.stringify(reportParams), { headers });
    check(response, {
      'report generation successful': (r) => r.status === 200 || r.status === 202,
      'response time < 5s': (r) => r.timings.duration < 5000,
    });
    recordMetrics(response, 'report_generation');
  }
}

function recordMetrics(response, operation) {
  requestCount.add(1);
  responseTime.add(response.timings.duration);
  
  if (response.status >= 400) {
    errorRate.add(1);
    console.error(`Error in ${operation}: ${response.status} - ${response.body}`);
  } else {
    errorRate.add(0);
  }
}

// Setup function - runs once before the test
export function setup() {
  console.log('Starting DSR Load Test');
  console.log(`Base URL: ${BASE_URL}`);
  console.log('Test will simulate citizen and staff workflows');
  
  // Verify API is accessible
  const response = http.get(`${BASE_URL}/actuator/health`);
  if (response.status !== 200) {
    throw new Error(`API not accessible: ${response.status}`);
  }
  
  return { startTime: new Date() };
}

// Teardown function - runs once after the test
export function teardown(data) {
  const endTime = new Date();
  const duration = (endTime - data.startTime) / 1000;
  console.log(`Load test completed in ${duration} seconds`);
}
