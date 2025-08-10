// DSR Data Flow Integrity Validation Suite
// Validates data consistency and integrity across all 7 services
// Ensures data flows correctly through the entire system

import { test, expect, APIRequestContext } from '@playwright/test';

// Test configuration
const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080';

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

// Test data for data flow validation
const testHousehold = {
  householdHead: {
    firstName: 'DataFlow',
    lastName: 'TestUser',
    birthDate: '1985-01-01',
    philsysId: 'PSN-TEST-DATA-FLOW',
    relationship: 'HEAD',
    email: 'dataflow.test@dsr.gov.ph',
    phoneNumber: '+639999999999'
  },
  address: {
    region: 'NCR',
    province: 'Metro Manila',
    city: 'Manila',
    barangay: 'Test Barangay',
    street: 'Test Street',
    zipCode: '1000'
  },
  members: [
    {
      firstName: 'TestSpouse',
      lastName: 'TestUser',
      birthDate: '1987-02-02',
      relationship: 'SPOUSE',
      philsysId: 'PSN-TEST-SPOUSE-FLOW'
    }
  ],
  economicProfile: {
    monthlyIncome: 20000,
    employmentStatus: 'EMPLOYED',
    householdAssets: ['HOUSE'],
    vulnerabilityIndicators: ['NONE']
  }
};

test.describe('DSR Data Flow Integrity Validation', () => {
  let apiContext: APIRequestContext;
  let authToken: string;
  let householdId: string;
  let registrationId: string;
  let assessmentId: string;
  let paymentBatchId: string;
  let grievanceCaseId: string;

  test.beforeAll(async ({ playwright }) => {
    apiContext = await playwright.request.newContext();
    
    // Authenticate as DSWD staff for full access
    const authResponse = await apiContext.post(`${API_BASE_URL}/api/v1/auth/login`, {
      data: {
        email: 'dswd.staff@test.ph',
        password: 'TestPassword123!'
      }
    });
    
    const authData = await authResponse.json();
    authToken = authData.accessToken;
    
    console.log('✅ Authenticated for data flow validation');
  });

  test.afterAll(async () => {
    await apiContext.dispose();
  });

  test.describe('1. Registration to Data Management Flow', () => {
    test('should maintain data consistency from registration through data management', async () => {
      // Step 1: Submit household registration
      const registrationResponse = await apiContext.post(`${SERVICES.registration}/households`, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        },
        data: testHousehold
      });

      expect(registrationResponse.status()).toBe(201);
      const registrationData = await registrationResponse.json();
      registrationId = registrationData.id;
      householdId = registrationData.householdId;

      console.log(`✅ Registration created: ${householdId}`);

      // Step 2: Verify data appears in data management service
      await new Promise(resolve => setTimeout(resolve, 2000)); // Allow processing time

      const dataManagementResponse = await apiContext.get(`${SERVICES.dataManagement}/households/${householdId}`, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });

      expect(dataManagementResponse.status()).toBe(200);
      const dmData = await dataManagementResponse.json();

      // Validate data consistency
      expect(dmData.householdHead.firstName).toBe(testHousehold.householdHead.firstName);
      expect(dmData.householdHead.lastName).toBe(testHousehold.householdHead.lastName);
      expect(dmData.householdHead.philsysId).toBe(testHousehold.householdHead.philsysId);
      expect(dmData.address.region).toBe(testHousehold.address.region);
      expect(dmData.members.length).toBe(testHousehold.members.length);

      console.log('✅ Data consistency validated: Registration → Data Management');

      // Step 3: Verify PhilSys validation data flow
      const philsysValidationResponse = await apiContext.post(`${SERVICES.dataManagement}/philsys/validate-household`, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        },
        data: {
          householdId: householdId
        }
      });

      expect(philsysValidationResponse.status()).toBe(200);
      const philsysData = await philsysValidationResponse.json();
      expect(philsysData.householdId).toBe(householdId);
      expect(philsysData.validationResults).toBeDefined();

      console.log('✅ PhilSys validation data flow verified');
    });
  });

  test.describe('2. Data Management to Eligibility Flow', () => {
    test('should maintain data integrity through eligibility assessment', async () => {
      // Step 1: Trigger eligibility assessment
      const eligibilityResponse = await apiContext.post(`${SERVICES.eligibility}/assessments`, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
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
      assessmentId = assessmentData.id;

      console.log(`✅ Eligibility assessment created: ${assessmentId}`);

      // Step 2: Verify household data consistency in eligibility service
      const householdEligibilityResponse = await apiContext.get(`${SERVICES.eligibility}/households/${householdId}`, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });

      expect(householdEligibilityResponse.status()).toBe(200);
      const eligibilityHouseholdData = await householdEligibilityResponse.json();

      // Validate data consistency
      expect(eligibilityHouseholdData.householdHead.firstName).toBe(testHousehold.householdHead.firstName);
      expect(eligibilityHouseholdData.economicProfile.monthlyIncome).toBe(testHousehold.economicProfile.monthlyIncome);

      console.log('✅ Data consistency validated: Data Management → Eligibility');

      // Step 3: Verify PMT calculation uses correct data
      const pmtResponse = await apiContext.get(`${SERVICES.eligibility}/assessments/${assessmentId}/pmt`, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });

      expect(pmtResponse.status()).toBe(200);
      const pmtData = await pmtResponse.json();
      expect(pmtData.householdId).toBe(householdId);
      expect(pmtData.pmtScore).toBeGreaterThan(0);
      expect(pmtData.inputData.monthlyIncome).toBe(testHousehold.economicProfile.monthlyIncome);

      console.log('✅ PMT calculation data integrity verified');
    });
  });

  test.describe('3. Eligibility to Payment Flow', () => {
    test('should maintain data consistency through payment processing', async () => {
      // Step 1: Get eligibility results
      const eligibilityResultsResponse = await apiContext.get(`${SERVICES.eligibility}/households/${householdId}/programs`, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });

      expect(eligibilityResultsResponse.status()).toBe(200);
      const eligibilityResults = await eligibilityResultsResponse.json();

      console.log('✅ Eligibility results retrieved');

      // Step 2: Create payment batch based on eligibility
      const paymentBatchResponse = await apiContext.post(`${SERVICES.payment}/batches`, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        },
        data: {
          programCode: '4PS',
          paymentPeriod: '2025-01',
          description: 'Data Flow Test Payment',
          households: [householdId],
          eligibilityReference: assessmentId
        }
      });

      expect(paymentBatchResponse.status()).toBe(201);
      const batchData = await paymentBatchResponse.json();
      paymentBatchId = batchData.batchId;

      console.log(`✅ Payment batch created: ${paymentBatchId}`);

      // Step 3: Verify payment data consistency
      const paymentDetailsResponse = await apiContext.get(`${SERVICES.payment}/batches/${paymentBatchId}/details`, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });

      expect(paymentDetailsResponse.status()).toBe(200);
      const paymentDetails = await paymentDetailsResponse.json();

      // Validate payment data references correct household
      expect(paymentDetails.households).toContain(householdId);
      expect(paymentDetails.eligibilityReference).toBe(assessmentId);

      console.log('✅ Data consistency validated: Eligibility → Payment');
    });
  });

  test.describe('4. Payment to Interoperability Flow', () => {
    test('should maintain data integrity through FSP integration', async () => {
      // Step 1: Process payment batch
      const processResponse = await apiContext.post(`${SERVICES.payment}/batches/${paymentBatchId}/process`, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });

      expect(processResponse.status()).toBe(200);
      const processData = await processResponse.json();

      console.log('✅ Payment batch processing initiated');

      // Step 2: Verify interoperability service receives correct data
      const fspTransactionResponse = await apiContext.get(`${SERVICES.interoperability}/transactions/batch/${paymentBatchId}`, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });

      expect(fspTransactionResponse.status()).toBe(200);
      const fspData = await fspTransactionResponse.json();

      // Validate FSP transaction data
      expect(fspData.batchId).toBe(paymentBatchId);
      expect(fspData.transactions.length).toBeGreaterThan(0);

      const transaction = fspData.transactions[0];
      expect(transaction.householdId).toBe(householdId);

      console.log('✅ Data consistency validated: Payment → Interoperability');

      // Step 3: Verify service delivery tracking
      const deliveryTrackingResponse = await apiContext.get(`${SERVICES.interoperability}/delivery/household/${householdId}`, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });

      expect(deliveryTrackingResponse.status()).toBe(200);
      const deliveryData = await deliveryTrackingResponse.json();
      expect(deliveryData.householdId).toBe(householdId);

      console.log('✅ Service delivery tracking data integrity verified');
    });
  });

  test.describe('5. Cross-Service Analytics Data Flow', () => {
    test('should aggregate data correctly across all services', async () => {
      // Allow time for analytics data aggregation
      await new Promise(resolve => setTimeout(resolve, 3000));

      // Step 1: Verify household appears in analytics
      const analyticsHouseholdResponse = await apiContext.get(`${SERVICES.analytics}/households/${householdId}/summary`, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });

      expect(analyticsHouseholdResponse.status()).toBe(200);
      const analyticsData = await analyticsHouseholdResponse.json();

      // Validate analytics data aggregation
      expect(analyticsData.householdId).toBe(householdId);
      expect(analyticsData.registrationStatus).toBeDefined();
      expect(analyticsData.eligibilityStatus).toBeDefined();
      expect(analyticsData.paymentHistory).toBeDefined();

      console.log('✅ Analytics data aggregation verified');

      // Step 2: Verify KPI calculations include this household
      const kpiResponse = await apiContext.get(`${SERVICES.analytics}/kpis/registration-to-payment`, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });

      expect(kpiResponse.status()).toBe(200);
      const kpiData = await kpiResponse.json();
      expect(kpiData.totalHouseholds).toBeGreaterThan(0);

      console.log('✅ KPI calculation data integrity verified');
    });
  });

  test.describe('6. Grievance Service Data Integration', () => {
    test('should maintain data consistency in grievance management', async () => {
      // Step 1: Submit grievance related to the household
      const grievanceResponse = await apiContext.post(`${SERVICES.grievance}/cases`, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        },
        data: {
          complainantPsn: testHousehold.householdHead.philsysId,
          subject: 'Data Flow Test Grievance',
          description: 'Testing data flow integrity in grievance system',
          category: 'SYSTEM_ERROR',
          priority: 'MEDIUM',
          channel: 'API_TEST',
          relatedHouseholdId: householdId
        }
      });

      expect(grievanceResponse.status()).toBe(201);
      const grievanceData = await grievanceResponse.json();
      grievanceCaseId = grievanceData.id;

      console.log(`✅ Grievance case created: ${grievanceData.caseNumber}`);

      // Step 2: Verify grievance service can access household data
      const grievanceHouseholdResponse = await apiContext.get(`${SERVICES.grievance}/cases/${grievanceCaseId}/household-context`, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });

      expect(grievanceHouseholdResponse.status()).toBe(200);
      const householdContext = await grievanceHouseholdResponse.json();

      // Validate household context data
      expect(householdContext.householdId).toBe(householdId);
      expect(householdContext.householdHead.firstName).toBe(testHousehold.householdHead.firstName);
      expect(householdContext.registrationStatus).toBeDefined();
      expect(householdContext.eligibilityStatus).toBeDefined();

      console.log('✅ Grievance service data integration verified');
    });
  });

  test.describe('7. Data Consistency Validation', () => {
    test('should verify data consistency across all services', async () => {
      // Collect household data from all services
      const serviceData = {};

      // Registration service
      const regResponse = await apiContext.get(`${SERVICES.registration}/households/${householdId}`, {
        headers: { 'Authorization': `Bearer ${authToken}` }
      });
      serviceData['registration'] = await regResponse.json();

      // Data Management service
      const dmResponse = await apiContext.get(`${SERVICES.dataManagement}/households/${householdId}`, {
        headers: { 'Authorization': `Bearer ${authToken}` }
      });
      serviceData['dataManagement'] = await dmResponse.json();

      // Eligibility service
      const eligResponse = await apiContext.get(`${SERVICES.eligibility}/households/${householdId}`, {
        headers: { 'Authorization': `Bearer ${authToken}` }
      });
      serviceData['eligibility'] = await eligResponse.json();

      // Analytics service
      const analyticsResponse = await apiContext.get(`${SERVICES.analytics}/households/${householdId}/summary`, {
        headers: { 'Authorization': `Bearer ${authToken}` }
      });
      serviceData['analytics'] = await analyticsResponse.json();

      // Validate core data consistency across services
      const firstName = testHousehold.householdHead.firstName;
      const lastName = testHousehold.householdHead.lastName;
      const philsysId = testHousehold.householdHead.philsysId;

      expect(serviceData['registration'].householdHead.firstName).toBe(firstName);
      expect(serviceData['dataManagement'].householdHead.firstName).toBe(firstName);
      expect(serviceData['eligibility'].householdHead.firstName).toBe(firstName);

      expect(serviceData['registration'].householdHead.lastName).toBe(lastName);
      expect(serviceData['dataManagement'].householdHead.lastName).toBe(lastName);
      expect(serviceData['eligibility'].householdHead.lastName).toBe(lastName);

      expect(serviceData['registration'].householdHead.philsysId).toBe(philsysId);
      expect(serviceData['dataManagement'].householdHead.philsysId).toBe(philsysId);
      expect(serviceData['eligibility'].householdHead.philsysId).toBe(philsysId);

      console.log('✅ Data consistency validated across all services');
    });

    test('should verify referential integrity across services', async () => {
      // Verify all services reference the same household ID
      const responses = await Promise.all([
        apiContext.get(`${SERVICES.registration}/households/${householdId}`, {
          headers: { 'Authorization': `Bearer ${authToken}` }
        }),
        apiContext.get(`${SERVICES.eligibility}/assessments?householdId=${householdId}`, {
          headers: { 'Authorization': `Bearer ${authToken}` }
        }),
        apiContext.get(`${SERVICES.payment}/households/${householdId}/payments`, {
          headers: { 'Authorization': `Bearer ${authToken}` }
        }),
        apiContext.get(`${SERVICES.grievance}/cases?householdId=${householdId}`, {
          headers: { 'Authorization': `Bearer ${authToken}` }
        })
      ]);

      // All responses should be successful
      responses.forEach((response, index) => {
        expect(response.status()).toBe(200);
      });

      const [regData, eligData, paymentData, grievanceData] = await Promise.all(
        responses.map(r => r.json())
      );

      // Verify referential integrity
      expect(regData.householdId || regData.id).toBe(householdId);
      expect(eligData.assessments[0].householdId).toBe(householdId);
      expect(paymentData.householdId).toBe(householdId);
      expect(grievanceData.cases[0].relatedHouseholdId).toBe(householdId);

      console.log('✅ Referential integrity validated across all services');
    });
  });
});
