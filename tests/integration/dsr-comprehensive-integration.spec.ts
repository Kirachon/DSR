// DSR Comprehensive Integration Test Suite
// End-to-end validation of all 7 production-ready services
// Validates complete citizen journey from registration to payment disbursement

import { test, expect, Page, APIRequestContext } from '@playwright/test';

// Test configuration
const BASE_URL = process.env.BASE_URL || 'http://localhost:3000';
const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080';

// Service endpoints
const SERVICE_ENDPOINTS = {
  registration: `${API_BASE_URL}/api/v1/registration`,
  dataManagement: `${API_BASE_URL}/api/v1/data-management`,
  eligibility: `${API_BASE_URL}/api/v1/eligibility`,
  payment: `${API_BASE_URL}/api/v1/payments`,
  interoperability: `${API_BASE_URL}/api/v1/interoperability`,
  grievance: `${API_BASE_URL}/api/v1/grievances`,
  analytics: `${API_BASE_URL}/api/v1/analytics`
};

// Test data for comprehensive workflow
const testHousehold = {
  householdHead: {
    firstName: 'Maria',
    lastName: 'Santos',
    birthDate: '1985-05-15',
    philsysId: 'PSN-1234-5678-9012',
    relationship: 'HEAD',
    email: 'maria.santos@test.ph',
    phoneNumber: '+639123456789'
  },
  address: {
    region: 'NCR',
    province: 'Metro Manila',
    city: 'Quezon City',
    barangay: 'Barangay 123',
    street: '123 Test Street',
    zipCode: '1100'
  },
  members: [
    {
      firstName: 'Jose',
      lastName: 'Santos',
      birthDate: '1980-03-20',
      relationship: 'SPOUSE',
      philsysId: 'PSN-2345-6789-0123'
    },
    {
      firstName: 'Ana',
      lastName: 'Santos',
      birthDate: '2010-08-10',
      relationship: 'CHILD',
      philsysId: 'PSN-3456-7890-1234'
    }
  ],
  economicProfile: {
    monthlyIncome: 15000,
    employmentStatus: 'EMPLOYED',
    householdAssets: ['HOUSE', 'MOTORCYCLE'],
    vulnerabilityIndicators: ['SINGLE_PARENT', 'ELDERLY_MEMBER']
  }
};

// User roles for testing
const testUsers = {
  citizen: {
    email: 'citizen@test.ph',
    password: 'TestPassword123!',
    role: 'CITIZEN'
  },
  lguStaff: {
    email: 'lgu.staff@test.ph',
    password: 'TestPassword123!',
    role: 'LGU_STAFF'
  },
  dswdStaff: {
    email: 'dswd.staff@test.ph',
    password: 'TestPassword123!',
    role: 'DSWD_STAFF'
  },
  systemAdmin: {
    email: 'admin@test.ph',
    password: 'TestPassword123!',
    role: 'SYSTEM_ADMIN'
  }
};

test.describe('DSR Comprehensive Integration Tests', () => {
  let page: Page;
  let apiContext: APIRequestContext;
  let authTokens: Record<string, string> = {};
  let householdId: string;
  let registrationId: string;

  test.beforeAll(async ({ browser, playwright }) => {
    page = await browser.newPage();
    apiContext = await playwright.request.newContext();
    
    // Set up API monitoring
    page.route('**/api/v1/**', async (route) => {
      const response = await route.fetch();
      console.log(`API Call: ${route.request().method()} ${route.request().url()} - Status: ${response.status()}`);
      route.continue();
    });

    // Authenticate all user types
    for (const [userType, userData] of Object.entries(testUsers)) {
      const authResponse = await apiContext.post(`${API_BASE_URL}/api/v1/auth/login`, {
        data: {
          email: userData.email,
          password: userData.password
        }
      });
      
      if (authResponse.ok()) {
        const authData = await authResponse.json();
        authTokens[userType] = authData.accessToken;
        console.log(`✅ Authenticated ${userType}: ${userData.role}`);
      } else {
        console.warn(`⚠️ Failed to authenticate ${userType}`);
      }
    }
  });

  test.afterAll(async () => {
    await page.close();
    await apiContext.dispose();
  });

  test.describe('1. Service Health and Connectivity', () => {
    test('should verify all 7 services are healthy and accessible', async () => {
      const healthChecks = [];

      for (const [serviceName, endpoint] of Object.entries(SERVICE_ENDPOINTS)) {
        const healthResponse = await apiContext.get(`${endpoint}/health`, {
          headers: {
            'Authorization': `Bearer ${authTokens.systemAdmin}`
          }
        });

        expect(healthResponse.status()).toBe(200);
        const healthData = await healthResponse.json();
        expect(healthData.status).toBe('UP');
        
        healthChecks.push({
          service: serviceName,
          status: healthData.status,
          endpoint: endpoint
        });

        console.log(`✅ ${serviceName} service health check passed`);
      }

      // Verify all services are healthy
      expect(healthChecks.every(check => check.status === 'UP')).toBe(true);
    });

    test('should verify service discovery and inter-service communication', async () => {
      // Test service-to-service communication through API gateway
      const serviceDiscoveryResponse = await apiContext.get(`${API_BASE_URL}/api/v1/system/services`, {
        headers: {
          'Authorization': `Bearer ${authTokens.systemAdmin}`
        }
      });

      expect(serviceDiscoveryResponse.status()).toBe(200);
      const services = await serviceDiscoveryResponse.json();
      
      // Verify all 7 services are registered
      const expectedServices = ['registration', 'data-management', 'eligibility', 'payment', 'interoperability', 'grievance', 'analytics'];
      for (const serviceName of expectedServices) {
        expect(services.some((s: any) => s.name.includes(serviceName))).toBe(true);
      }

      console.log('✅ Service discovery validation passed');
    });
  });

  test.describe('2. End-to-End Citizen Registration Workflow', () => {
    test('should complete full household registration workflow', async () => {
      // Step 1: Submit household registration
      const registrationResponse = await apiContext.post(`${SERVICE_ENDPOINTS.registration}/households`, {
        headers: {
          'Authorization': `Bearer ${authTokens.citizen}`,
          'Content-Type': 'application/json'
        },
        data: testHousehold
      });

      expect(registrationResponse.status()).toBe(201);
      const registrationData = await registrationResponse.json();
      registrationId = registrationData.id;
      householdId = registrationData.householdId;

      expect(registrationData.status).toBe('SUBMITTED');
      expect(registrationData.householdHead.firstName).toBe(testHousehold.householdHead.firstName);

      console.log(`✅ Household registration submitted: ${householdId}`);

      // Step 2: Verify data management processing
      await page.waitForTimeout(2000); // Allow processing time

      const dataValidationResponse = await apiContext.get(`${SERVICE_ENDPOINTS.dataManagement}/households/${householdId}/validation`, {
        headers: {
          'Authorization': `Bearer ${authTokens.dswdStaff}`
        }
      });

      expect(dataValidationResponse.status()).toBe(200);
      const validationData = await dataValidationResponse.json();
      expect(validationData.validationStatus).toMatch(/PENDING|VALIDATED|IN_PROGRESS/);

      console.log('✅ Data management validation initiated');

      // Step 3: PhilSys integration validation
      const philsysResponse = await apiContext.post(`${SERVICE_ENDPOINTS.dataManagement}/philsys/verify`, {
        headers: {
          'Authorization': `Bearer ${authTokens.dswdStaff}`,
          'Content-Type': 'application/json'
        },
        data: {
          philsysId: testHousehold.householdHead.philsysId,
          firstName: testHousehold.householdHead.firstName,
          lastName: testHousehold.householdHead.lastName,
          birthDate: testHousehold.householdHead.birthDate
        }
      });

      expect(philsysResponse.status()).toBe(200);
      const philsysData = await philsysResponse.json();
      expect(philsysData.verified).toBe(true);

      console.log('✅ PhilSys integration validation passed');
    });

    test('should process eligibility assessment workflow', async () => {
      // Step 1: Trigger eligibility assessment
      const eligibilityResponse = await apiContext.post(`${SERVICE_ENDPOINTS.eligibility}/assessments`, {
        headers: {
          'Authorization': `Bearer ${authTokens.dswdStaff}`,
          'Content-Type': 'application/json'
        },
        data: {
          householdId: householdId,
          programCodes: ['4PS', 'PANTAWID'],
          assessmentType: 'COMPREHENSIVE'
        }
      });

      expect(eligibilityResponse.status()).toBe(201);
      const assessmentData = await eligibilityResponse.json();
      expect(assessmentData.householdId).toBe(householdId);
      expect(assessmentData.status).toMatch(/PENDING|IN_PROGRESS|COMPLETED/);

      console.log('✅ Eligibility assessment initiated');

      // Step 2: Verify PMT calculation
      const pmtResponse = await apiContext.get(`${SERVICE_ENDPOINTS.eligibility}/assessments/${assessmentData.id}/pmt`, {
        headers: {
          'Authorization': `Bearer ${authTokens.dswdStaff}`
        }
      });

      expect(pmtResponse.status()).toBe(200);
      const pmtData = await pmtResponse.json();
      expect(pmtData.pmtScore).toBeGreaterThan(0);
      expect(pmtData.eligibilityStatus).toMatch(/ELIGIBLE|INELIGIBLE|PENDING/);

      console.log(`✅ PMT calculation completed: Score ${pmtData.pmtScore}`);

      // Step 3: Verify program eligibility determination
      const programEligibilityResponse = await apiContext.get(`${SERVICE_ENDPOINTS.eligibility}/households/${householdId}/programs`, {
        headers: {
          'Authorization': `Bearer ${authTokens.dswdStaff}`
        }
      });

      expect(programEligibilityResponse.status()).toBe(200);
      const programData = await programEligibilityResponse.json();
      expect(Array.isArray(programData.eligiblePrograms)).toBe(true);

      console.log('✅ Program eligibility determination completed');
    });
  });

  test.describe('3. Payment Processing Integration', () => {
    test('should complete payment workflow integration', async () => {
      // Step 1: Create payment batch
      const batchResponse = await apiContext.post(`${SERVICE_ENDPOINTS.payment}/batches`, {
        headers: {
          'Authorization': `Bearer ${authTokens.dswdStaff}`,
          'Content-Type': 'application/json'
        },
        data: {
          programCode: '4PS',
          paymentPeriod: '2025-01',
          description: 'January 2025 4Ps Payment',
          households: [householdId]
        }
      });

      expect(batchResponse.status()).toBe(201);
      const batchData = await batchResponse.json();
      expect(batchData.status).toBe('CREATED');

      console.log(`✅ Payment batch created: ${batchData.batchId}`);

      // Step 2: Verify interoperability service integration
      const fspResponse = await apiContext.get(`${SERVICE_ENDPOINTS.interoperability}/fsp/services`, {
        headers: {
          'Authorization': `Bearer ${authTokens.dswdStaff}`
        }
      });

      expect(fspResponse.status()).toBe(200);
      const fspData = await fspResponse.json();
      expect(Array.isArray(fspData.availableServices)).toBe(true);
      expect(fspData.availableServices.length).toBeGreaterThan(0);

      console.log('✅ FSP services integration verified');

      // Step 3: Process payment batch
      const processResponse = await apiContext.post(`${SERVICE_ENDPOINTS.payment}/batches/${batchData.batchId}/process`, {
        headers: {
          'Authorization': `Bearer ${authTokens.dswdStaff}`
        }
      });

      expect(processResponse.status()).toBe(200);
      const processData = await processResponse.json();
      expect(processData.status).toMatch(/PROCESSING|COMPLETED|PENDING/);

      console.log('✅ Payment batch processing initiated');
    });
  });

  test.describe('4. Analytics and Reporting Integration', () => {
    test('should verify analytics data aggregation across services', async () => {
      // Step 1: Verify analytics dashboard data
      const dashboardResponse = await apiContext.get(`${SERVICE_ENDPOINTS.analytics}/dashboards/system-overview`, {
        headers: {
          'Authorization': `Bearer ${authTokens.dswdStaff}`
        }
      });

      expect(dashboardResponse.status()).toBe(200);
      const dashboardData = await dashboardResponse.json();
      expect(dashboardData.metrics).toBeDefined();
      expect(dashboardData.metrics.totalHouseholds).toBeGreaterThanOrEqual(1);

      console.log('✅ Analytics dashboard data verified');

      // Step 2: Verify cross-service KPI calculation
      const kpiResponse = await apiContext.get(`${SERVICE_ENDPOINTS.analytics}/kpis/registration-to-payment`, {
        headers: {
          'Authorization': `Bearer ${authTokens.dswdStaff}`
        }
      });

      expect(kpiResponse.status()).toBe(200);
      const kpiData = await kpiResponse.json();
      expect(kpiData.registrationCompletionRate).toBeDefined();
      expect(kpiData.paymentSuccessRate).toBeDefined();

      console.log('✅ Cross-service KPI calculation verified');
    });
  });

  test.describe('5. Grievance Management Integration', () => {
    test('should verify grievance workflow integration', async () => {
      // Step 1: Submit grievance case
      const grievanceResponse = await apiContext.post(`${SERVICE_ENDPOINTS.grievance}/cases`, {
        headers: {
          'Authorization': `Bearer ${authTokens.citizen}`,
          'Content-Type': 'application/json'
        },
        data: {
          complainantPsn: testHousehold.householdHead.philsysId,
          subject: 'Payment Delay Issue',
          description: 'My payment has been delayed for this month',
          category: 'PAYMENT_ISSUE',
          priority: 'MEDIUM',
          channel: 'WEB_PORTAL'
        }
      });

      expect(grievanceResponse.status()).toBe(201);
      const grievanceData = await grievanceResponse.json();
      expect(grievanceData.status).toBe('SUBMITTED');
      expect(grievanceData.caseNumber).toBeDefined();

      console.log(`✅ Grievance case submitted: ${grievanceData.caseNumber}`);

      // Step 2: Verify intelligent case routing
      await page.waitForTimeout(1000); // Allow routing processing

      const caseDetailsResponse = await apiContext.get(`${SERVICE_ENDPOINTS.grievance}/cases/${grievanceData.id}`, {
        headers: {
          'Authorization': `Bearer ${authTokens.dswdStaff}`
        }
      });

      expect(caseDetailsResponse.status()).toBe(200);
      const caseData = await caseDetailsResponse.json();
      expect(caseData.assignedTo).toBeDefined();
      expect(caseData.status).toMatch(/SUBMITTED|UNDER_REVIEW|ASSIGNED/);

      console.log('✅ Intelligent case routing verified');
    });
  });
});
