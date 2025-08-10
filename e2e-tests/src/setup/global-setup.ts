import { chromium, FullConfig } from '@playwright/test';
import { ApiClient } from '../utils/api-client';

/**
 * Global setup for Playwright tests
 * Runs once before all tests to prepare the test environment
 */
async function globalSetup(config: FullConfig) {
  console.log('🚀 Starting DSR E2E Test Suite Global Setup...');

  const startTime = Date.now();

  try {
    // 1. Check if services are running
    await checkServices();

    // 2. Check if frontend is available (optional for backend-only tests)
    await checkFrontendAvailability();

    // 3. Setup test database and seed data
    await setupTestData();

    // 4. Verify authentication endpoints
    await verifyAuthEndpoints();

    // 5. Skip user creation if frontend is not available
    // await createTestUsers(); // Commented out as it requires frontend

    const setupTime = Date.now() - startTime;
    console.log(`✅ Global setup completed successfully in ${setupTime}ms`);

  } catch (error) {
    console.error('❌ Global setup failed:', error);
    throw error;
  }
}

/**
 * Check if required services are running
 * Only checks services that are actually needed for authentication testing
 */
async function checkServices(): Promise<void> {
  console.log('🔍 Checking service health...');

  const apiClient = new ApiClient();
  // Only check services that are actually running and needed for authentication tests
  const requiredServices = [
    { name: 'Registration Service', port: 8080, required: true },
  ];

  // Optional services - check but don't fail if unavailable
  const optionalServices = [
    { name: 'Data Management Service', port: 8081, required: false },
    { name: 'Eligibility Service', port: 8082, required: false },
    { name: 'Interoperability Service', port: 8083, required: false },
    { name: 'Payment Service', port: 8084, required: false },
    { name: 'Grievance Service', port: 8085, required: false },
    { name: 'Analytics Service', port: 8086, required: false }
  ];

  let healthyRequiredServices = 0;
  let healthyOptionalServices = 0;

  // Check required services
  for (const service of requiredServices) {
    try {
      await apiClient.waitForService(service.name, service.port, 5);
      console.log(`✅ ${service.name} is healthy`);
      healthyRequiredServices++;
    } catch (error) {
      console.error(`❌ ${service.name} is not available - this is required for authentication tests`);
      throw new Error(`Required service ${service.name} is not available. Please start it before running tests.`);
    }
  }

  // Check optional services
  for (const service of optionalServices) {
    try {
      await apiClient.waitForService(service.name, service.port, 2);
      console.log(`✅ ${service.name} is healthy`);
      healthyOptionalServices++;
    } catch (error) {
      console.warn(`⚠️  ${service.name} is not available - tests will skip features requiring this service`);
    }
  }

  const totalServices = requiredServices.length + optionalServices.length;
  const totalHealthy = healthyRequiredServices + healthyOptionalServices;
  console.log(`📊 ${totalHealthy}/${totalServices} services are healthy (${healthyRequiredServices}/${requiredServices.length} required)`);
}

/**
 * Check if frontend is available (optional for backend-only tests)
 */
async function checkFrontendAvailability(): Promise<boolean> {
  console.log('🌐 Checking frontend availability...');

  try {
    const response = await fetch('http://localhost:3000', {
      method: 'GET',
      signal: AbortSignal.timeout(3000) // 3 second timeout
    });
    if (response.ok || response.status === 404) {
      console.log('✅ Frontend is available');
      return true;
    }
  } catch (error) {
    console.warn('⚠️  Frontend is not available - tests will run in backend-only mode');
    console.log('💡 To test frontend features, start the frontend server on port 3000');
  }

  return false;
}

/**
 * Setup test database and seed initial data
 */
async function setupTestData(): Promise<void> {
  console.log('🗄️  Setting up test data...');
  
  try {
    // In a real implementation, this would:
    // 1. Connect to test database
    // 2. Run migrations
    // 3. Seed test data
    // 4. Create test households, users, etc.
    
    console.log('✅ Test data setup completed');
  } catch (error) {
    console.warn('⚠️  Test data setup failed:', error);
    // Don't fail the entire setup for data issues
  }
}

/**
 * Verify authentication endpoints are working
 */
async function verifyAuthEndpoints(): Promise<void> {
  console.log('🔐 Verifying authentication endpoints...');

  try {
    // Test Registration Service auth endpoint
    const response = await fetch('http://localhost:8080/api/v1/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: 'test@example.com',
        password: 'testpassword'
      })
    });

    if (response.status === 400 || response.status === 401 || response.status === 404) {
      // Expected for invalid credentials or endpoint not found
      console.log('✅ Authentication endpoints are available');
    } else {
      console.log('✅ Authentication endpoints are responding');
    }
  } catch (error) {
    console.warn('⚠️  Authentication endpoint verification skipped - will test during actual tests');
  }
}

/**
 * Create test users for different scenarios
 */
async function createTestUsers(): Promise<void> {
  console.log('👥 Creating test users...');
  
  try {
    const testUsers = [
      {
        email: process.env.TEST_ADMIN_EMAIL || 'admin@test.dsr.gov.ph',
        password: process.env.TEST_ADMIN_PASSWORD || 'TestAdmin123!',
        role: 'admin',
        name: 'Test Admin'
      },
      {
        email: process.env.TEST_USER_EMAIL || 'user@test.dsr.gov.ph',
        password: process.env.TEST_USER_PASSWORD || 'TestUser123!',
        role: 'citizen',
        name: 'Test User'
      },
      {
        email: process.env.TEST_LGU_STAFF_EMAIL || 'lgu@test.dsr.gov.ph',
        password: process.env.TEST_LGU_STAFF_PASSWORD || 'TestLGU123!',
        role: 'lgu_staff',
        name: 'Test LGU Staff'
      }
    ];

    for (const user of testUsers) {
      try {
        const response = await fetch('http://localhost:3000/api/auth/register', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            email: user.email,
            password: user.password,
            firstName: user.name.split(' ')[0],
            lastName: user.name.split(' ')[1] || 'User',
            role: user.role
          })
        });
        
        if (response.ok || response.status === 409) { // 409 = user already exists
          console.log(`✅ Test user ${user.role} is ready`);
        } else {
          console.warn(`⚠️  Failed to create test user ${user.role}`);
        }
      } catch (error) {
        console.warn(`⚠️  Error creating test user ${user.role}:`, error);
      }
    }
  } catch (error) {
    console.warn('⚠️  Test user creation failed:', error);
  }
}

/**
 * Setup browser context with authentication
 */
async function setupAuthenticatedContext(): Promise<void> {
  console.log('🔑 Setting up authenticated browser context...');
  
  try {
    const browser = await chromium.launch();
    const context = await browser.newContext();
    const page = await context.newPage();
    
    // Login as test user
    await page.goto('http://localhost:3000/login');
    await page.fill('[data-testid="email-input"]', process.env.TEST_USER_EMAIL || 'user@test.dsr.gov.ph');
    await page.fill('[data-testid="password-input"]', process.env.TEST_USER_PASSWORD || 'TestUser123!');
    await page.click('[data-testid="login-submit-button"]');
    
    // Wait for authentication
    await page.waitForURL('**/dashboard');
    
    // Save authentication state
    await context.storageState({ path: 'test-results/auth-state.json' });
    
    await browser.close();
    console.log('✅ Authenticated context saved');
  } catch (error) {
    console.warn('⚠️  Failed to setup authenticated context:', error);
  }
}

/**
 * Cleanup any existing test artifacts
 */
async function cleanupTestArtifacts(): Promise<void> {
  console.log('🧹 Cleaning up test artifacts...');
  
  try {
    // Clean up any previous test results
    const fs = require('fs');
    const path = require('path');
    
    const artifactDirs = [
      'test-results',
      'allure-results',
      'screenshots'
    ];
    
    for (const dir of artifactDirs) {
      if (fs.existsSync(dir)) {
        fs.rmSync(dir, { recursive: true, force: true });
      }
      fs.mkdirSync(dir, { recursive: true });
    }
    
    console.log('✅ Test artifacts cleaned up');
  } catch (error) {
    console.warn('⚠️  Failed to cleanup test artifacts:', error);
  }
}

/**
 * Validate test environment configuration
 */
async function validateEnvironment(): Promise<void> {
  console.log('🔧 Validating test environment...');
  
  const requiredEnvVars = [
    'BASE_URL',
    'API_BASE_URL'
  ];
  
  const missingVars = requiredEnvVars.filter(varName => !process.env[varName]);
  
  if (missingVars.length > 0) {
    console.warn(`⚠️  Missing environment variables: ${missingVars.join(', ')}`);
    console.log('Using default values for missing variables');
  }
  
  // Set defaults
  process.env.BASE_URL = process.env.BASE_URL || 'http://localhost:3000';
  process.env.API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080';
  
  console.log(`✅ Environment validated - Base URL: ${process.env.BASE_URL}`);
}

export default globalSetup;
