// DSR End-to-End Workflow Test Script
// Tests the complete workflow from registration to benefit disbursement

const axios = require('axios');

const BASE_URL = 'http://localhost:3000';
const API_BASE = 'http://localhost:8080/api/v1';

// Test configuration
const TEST_USER = {
  email: 'admin@dsr.gov.ph',
  password: 'admin123',
  firstName: 'System',
  lastName: 'Administrator'
};

const TEST_CITIZEN = {
  firstName: 'Juan',
  lastName: 'Dela Cruz',
  email: 'juan.delacruz@example.com',
  phoneNumber: '+639123456789',
  dateOfBirth: '1990-01-01',
  address: {
    street: '123 Main Street',
    barangay: 'Barangay 1',
    municipality: 'Quezon City',
    province: 'Metro Manila',
    zipCode: '1100'
  }
};

// Helper function to make API calls
async function apiCall(method, endpoint, data = null, token = null) {
  try {
    const config = {
      method,
      url: `${API_BASE}${endpoint}`,
      headers: {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` })
      },
      ...(data && { data })
    };

    const response = await axios(config);
    return { success: true, data: response.data, status: response.status };
  } catch (error) {
    return { 
      success: false, 
      error: error.response?.data || error.message,
      status: error.response?.status || 500
    };
  }
}

// Test service health
async function testServiceHealth() {
  console.log('\n=== Testing Service Health ===');
  
  const services = [
    { name: 'Registration Service', port: 8080, endpoint: '/health' },
    { name: 'Data Management Service', port: 8081, endpoint: '/actuator/health' },
    { name: 'Eligibility Service', port: 8082, endpoint: '/actuator/health' },
    { name: 'Payment Service', port: 8084, endpoint: '/actuator/health' },
    { name: 'Grievance Service', port: 8085, endpoint: '/actuator/health' },
  ];

  for (const service of services) {
    try {
      const response = await axios.get(`http://localhost:${service.port}${service.endpoint}`, {
        timeout: 5000
      });
      console.log(`✅ ${service.name}: Healthy (${response.status})`);
    } catch (error) {
      console.log(`❌ ${service.name}: Unhealthy (${error.code || 'ERROR'})`);
    }
  }
}

// Test authentication
async function testAuthentication() {
  console.log('\n=== Testing Authentication ===');
  
  const result = await apiCall('POST', '/auth/login', {
    email: TEST_USER.email,
    password: TEST_USER.password
  });

  if (result.success) {
    console.log('✅ Authentication successful');
    return result.data.token;
  } else {
    console.log('❌ Authentication failed:', result.error);
    return null;
  }
}

// Test citizen registration workflow
async function testCitizenRegistration(token) {
  console.log('\n=== Testing Citizen Registration Workflow ===');
  
  // Step 1: Register new citizen
  const registrationResult = await apiCall('POST', '/registration/citizens', TEST_CITIZEN, token);
  
  if (registrationResult.success) {
    console.log('✅ Citizen registration successful');
    const citizenId = registrationResult.data.id;
    
    // Step 2: Get citizen details
    const citizenResult = await apiCall('GET', `/registration/citizens/${citizenId}`, null, token);
    
    if (citizenResult.success) {
      console.log('✅ Citizen details retrieved successfully');
      return citizenId;
    } else {
      console.log('❌ Failed to retrieve citizen details:', citizenResult.error);
      return null;
    }
  } else {
    console.log('❌ Citizen registration failed:', registrationResult.error);
    return null;
  }
}

// Test eligibility assessment
async function testEligibilityAssessment(citizenId, token) {
  console.log('\n=== Testing Eligibility Assessment ===');
  
  const eligibilityData = {
    citizenId,
    householdSize: 4,
    monthlyIncome: 15000,
    hasDisability: false,
    isSeniorCitizen: false,
    isPregnant: false
  };

  const result = await apiCall('POST', '/eligibility/assess', eligibilityData, token);
  
  if (result.success) {
    console.log('✅ Eligibility assessment successful');
    console.log(`   Eligible: ${result.data.eligible}`);
    console.log(`   Benefit Amount: ₱${result.data.benefitAmount || 'N/A'}`);
    return result.data;
  } else {
    console.log('❌ Eligibility assessment failed:', result.error);
    return null;
  }
}

// Test payment processing
async function testPaymentProcessing(citizenId, token) {
  console.log('\n=== Testing Payment Processing ===');
  
  const paymentData = {
    citizenId,
    amount: 5000,
    paymentType: 'CASH_TRANSFER',
    description: 'Monthly benefit payment'
  };

  const result = await apiCall('POST', '/payment/process', paymentData, token);
  
  if (result.success) {
    console.log('✅ Payment processing successful');
    console.log(`   Payment ID: ${result.data.id}`);
    console.log(`   Status: ${result.data.status}`);
    return result.data;
  } else {
    console.log('❌ Payment processing failed:', result.error);
    return null;
  }
}

// Main test execution
async function runWorkflowTests() {
  console.log('🚀 Starting DSR End-to-End Workflow Tests');
  console.log('==========================================');

  try {
    // Test 1: Service Health
    await testServiceHealth();

    // Test 2: Authentication
    const token = await testAuthentication();
    if (!token) {
      console.log('\n❌ Cannot proceed without authentication');
      return;
    }

    // Test 3: Citizen Registration
    const citizenId = await testCitizenRegistration(token);
    if (!citizenId) {
      console.log('\n❌ Cannot proceed without citizen registration');
      return;
    }

    // Test 4: Eligibility Assessment
    const eligibilityResult = await testEligibilityAssessment(citizenId, token);
    
    // Test 5: Payment Processing (only if eligible)
    if (eligibilityResult && eligibilityResult.eligible) {
      await testPaymentProcessing(citizenId, token);
    }

    console.log('\n🎉 End-to-End Workflow Tests Completed');
    console.log('=====================================');

  } catch (error) {
    console.error('\n💥 Test execution failed:', error.message);
  }
}

// Run the tests
runWorkflowTests();
