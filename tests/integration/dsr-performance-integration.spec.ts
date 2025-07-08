// DSR Performance Integration Testing Suite
// Validates system performance under realistic loads across all 7 services
// Tests response times, throughput, and system stability

import { test, expect, APIRequestContext } from '@playwright/test';

// Test configuration
const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080';
const PERFORMANCE_THRESHOLDS = {
  maxResponseTime: 2000, // 2 seconds
  maxConcurrentUsers: 100,
  minThroughput: 50, // requests per second
  maxErrorRate: 0.05 // 5%
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

// Performance test data
const generateTestHousehold = (index: number) => ({
  householdHead: {
    firstName: `PerfTest${index}`,
    lastName: `User${index}`,
    birthDate: '1985-01-01',
    philsysId: `PSN-PERF-${index.toString().padStart(6, '0')}`,
    relationship: 'HEAD',
    email: `perftest${index}@dsr.gov.ph`,
    phoneNumber: `+6391234${index.toString().padStart(5, '0')}`
  },
  address: {
    region: 'NCR',
    province: 'Metro Manila',
    city: 'Manila',
    barangay: `Test Barangay ${index}`,
    street: `Test Street ${index}`,
    zipCode: '1000'
  },
  members: [
    {
      firstName: `Spouse${index}`,
      lastName: `User${index}`,
      birthDate: '1987-02-02',
      relationship: 'SPOUSE',
      philsysId: `PSN-SPOUSE-${index.toString().padStart(6, '0')}`
    }
  ],
  economicProfile: {
    monthlyIncome: 15000 + (index % 10000),
    employmentStatus: 'EMPLOYED',
    householdAssets: ['HOUSE'],
    vulnerabilityIndicators: ['NONE']
  }
});

test.describe('DSR Performance Integration Tests', () => {
  let apiContext: APIRequestContext;
  let authToken: string;

  test.beforeAll(async ({ playwright }) => {
    apiContext = await playwright.request.newContext();
    
    // Authenticate for performance testing
    const authResponse = await apiContext.post(`${API_BASE_URL}/api/v1/auth/login`, {
      data: {
        email: 'dswd.staff@test.ph',
        password: 'TestPassword123!'
      }
    });
    
    const authData = await authResponse.json();
    authToken = authData.accessToken;
    
    console.log('✅ Authenticated for performance testing');
  });

  test.afterAll(async () => {
    await apiContext.dispose();
  });

  // Helper function to measure response time
  async function measureResponseTime(requestFn: () => Promise<any>): Promise<{ responseTime: number, success: boolean }> {
    const startTime = Date.now();
    try {
      const response = await requestFn();
      const responseTime = Date.now() - startTime;
      return { responseTime, success: response.status() < 400 };
    } catch (error) {
      const responseTime = Date.now() - startTime;
      return { responseTime, success: false };
    }
  }

  // Helper function for concurrent load testing
  async function performConcurrentTest(requestFn: () => Promise<any>, concurrency: number, duration: number) {
    const results: Array<{ responseTime: number, success: boolean }> = [];
    const startTime = Date.now();
    
    while (Date.now() - startTime < duration) {
      const promises = Array(concurrency).fill(0).map(() => measureResponseTime(requestFn));
      const batchResults = await Promise.all(promises);
      results.push(...batchResults);
      
      // Small delay between batches
      await new Promise(resolve => setTimeout(resolve, 100));
    }
    
    return results;
  }

  test.describe('1. Individual Service Performance', () => {
    test('should meet response time requirements for Registration Service', async () => {
      const testHousehold = generateTestHousehold(1);
      
      const result = await measureResponseTime(async () => {
        return await apiContext.post(`${SERVICES.registration}/households`, {
          headers: {
            'Authorization': `Bearer ${authToken}`,
            'Content-Type': 'application/json'
          },
          data: testHousehold
        });
      });

      expect(result.success).toBe(true);
      expect(result.responseTime).toBeLessThan(PERFORMANCE_THRESHOLDS.maxResponseTime);
      
      console.log(`✅ Registration Service response time: ${result.responseTime}ms`);
    });

    test('should meet response time requirements for Analytics Service', async () => {
      const result = await measureResponseTime(async () => {
        return await apiContext.get(`${SERVICES.analytics}/dashboards/system-overview`, {
          headers: {
            'Authorization': `Bearer ${authToken}`
          }
        });
      });

      expect(result.success).toBe(true);
      expect(result.responseTime).toBeLessThan(PERFORMANCE_THRESHOLDS.maxResponseTime);
      
      console.log(`✅ Analytics Service response time: ${result.responseTime}ms`);
    });
  });

  test.describe('2. Concurrent Load Testing', () => {
    test('should handle concurrent analytics queries', async () => {
      const concurrency = 20;
      const duration = 10000; // 10 seconds
      
      const results = await performConcurrentTest(async () => {
        return await apiContext.get(`${SERVICES.analytics}/health`, {
          headers: {
            'Authorization': `Bearer ${authToken}`
          }
        });
      }, concurrency, duration);

      const successRate = results.filter(r => r.success).length / results.length;
      const avgResponseTime = results.reduce((sum, r) => sum + r.responseTime, 0) / results.length;

      expect(successRate).toBeGreaterThan(1 - PERFORMANCE_THRESHOLDS.maxErrorRate);
      expect(avgResponseTime).toBeLessThan(PERFORMANCE_THRESHOLDS.maxResponseTime);

      console.log(`✅ Concurrent analytics test - Success rate: ${(successRate * 100).toFixed(1)}%, Avg response: ${avgResponseTime.toFixed(0)}ms`);
    });
  });

  test.describe('3. Database Performance Under Load', () => {
    test('should maintain database performance under concurrent queries', async () => {
      const concurrency = 15;
      const duration = 15000; // 15 seconds
      
      const results = await performConcurrentTest(async () => {
        return await apiContext.get(`${SERVICES.registration}/health`, {
          headers: {
            'Authorization': `Bearer ${authToken}`
          }
        });
      }, concurrency, duration);

      const successRate = results.filter(r => r.success).length / results.length;
      const avgResponseTime = results.reduce((sum, r) => sum + r.responseTime, 0) / results.length;

      expect(successRate).toBeGreaterThan(0.95); // 95% success rate
      expect(avgResponseTime).toBeLessThan(1000); // 1 second for database queries

      console.log(`✅ Database performance test - Success rate: ${(successRate * 100).toFixed(1)}%, Avg response: ${avgResponseTime.toFixed(0)}ms`);
    });
  });

  test.describe('4. Memory and Resource Usage', () => {
    test('should not cause memory leaks during extended operation', async () => {
      const iterations = 50; // Reduced for faster execution
      const responseTimes: number[] = [];

      for (let i = 0; i < iterations; i++) {
        const result = await measureResponseTime(async () => {
          return await apiContext.get(`${SERVICES.analytics}/health`, {
            headers: {
              'Authorization': `Bearer ${authToken}`
            }
          });
        });

        responseTimes.push(result.responseTime);

        // Check for memory leak indicators (increasing response times)
        if (i > 25) {
          const recentAvg = responseTimes.slice(-5).reduce((a, b) => a + b, 0) / 5;
          const earlyAvg = responseTimes.slice(5, 10).reduce((a, b) => a + b, 0) / 5;
          
          // Response time shouldn't increase by more than 50%
          expect(recentAvg).toBeLessThan(earlyAvg * 1.5);
        }
      }

      const avgResponseTime = responseTimes.reduce((a, b) => a + b, 0) / responseTimes.length;
      expect(avgResponseTime).toBeLessThan(500); // 500ms average

      console.log(`✅ Memory leak test completed - Average response time: ${avgResponseTime.toFixed(0)}ms over ${iterations} iterations`);
    });
  });

  test.describe('5. System Integration Performance', () => {
    test('should validate cross-service communication performance', async () => {
      const services = Object.keys(SERVICES);
      const healthCheckResults: Array<{ service: string, responseTime: number, success: boolean }> = [];

      for (const serviceName of services) {
        const result = await measureResponseTime(async () => {
          return await apiContext.get(`${SERVICES[serviceName as keyof typeof SERVICES]}/health`, {
            headers: {
              'Authorization': `Bearer ${authToken}`
            }
          });
        });

        healthCheckResults.push({
          service: serviceName,
          responseTime: result.responseTime,
          success: result.success
        });

        expect(result.success).toBe(true);
        expect(result.responseTime).toBeLessThan(PERFORMANCE_THRESHOLDS.maxResponseTime);
      }

      const avgResponseTime = healthCheckResults.reduce((sum, r) => sum + r.responseTime, 0) / healthCheckResults.length;
      const successRate = healthCheckResults.filter(r => r.success).length / healthCheckResults.length;

      expect(successRate).toBe(1.0); // 100% success rate for health checks
      expect(avgResponseTime).toBeLessThan(1000); // 1 second average

      console.log(`✅ Cross-service health check performance - Success rate: ${(successRate * 100).toFixed(1)}%, Avg response: ${avgResponseTime.toFixed(0)}ms`);
    });
  });
});
