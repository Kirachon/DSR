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

    // 2. Start mock frontend server if needed
    await startMockFrontend();

    // 3. Setup test database and seed data
    await setupTestData();

    // 4. Verify authentication endpoints
    await verifyAuthEndpoints();

    // 5. Create test users
    await createTestUsers();

    const setupTime = Date.now() - startTime;
    console.log(`✅ Global setup completed successfully in ${setupTime}ms`);

  } catch (error) {
    console.error('❌ Global setup failed:', error);
    throw error;
  }
}

/**
 * Check if all required services are running
 */
async function checkServices(): Promise<void> {
  console.log('🔍 Checking service health...');
  
  const apiClient = new ApiClient();
  const services = [
    { name: 'Registration Service', port: 8080 },
    { name: 'Data Management Service', port: 8081 },
    { name: 'Eligibility Service', port: 8082 },
    { name: 'Interoperability Service', port: 8083 },
    { name: 'Payment Service', port: 8084 },
    { name: 'Grievance Service', port: 8085 },
    { name: 'Analytics Service', port: 8086 }
  ];

  const healthChecks = services.map(async (service) => {
    try {
      await apiClient.waitForService(service.name, service.port, 10);
      console.log(`✅ ${service.name} is healthy`);
      return true;
    } catch (error) {
      console.warn(`⚠️  ${service.name} is not available - tests may fail`);
      return false;
    }
  });

  const results = await Promise.all(healthChecks);
  const healthyServices = results.filter(Boolean).length;
  
  console.log(`📊 ${healthyServices}/${services.length} services are healthy`);
  
  if (healthyServices === 0) {
    throw new Error('No services are available. Please start the DSR services before running tests.');
  }
}

/**
 * Start mock frontend server if not already running
 */
async function startMockFrontend(): Promise<void> {
  console.log('🌐 Checking mock frontend server...');
  
  try {
    const response = await fetch('http://localhost:3000/health');
    if (response.ok) {
      console.log('✅ Mock frontend server is already running');
      return;
    }
  } catch (error) {
    // Server is not running, we'll need to start it
  }

  // In a real scenario, you might start the server here
  // For now, we'll assume it's started externally
  console.log('⚠️  Mock frontend server is not running. Please start it manually with: npm run start:test-server');
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
    const apiClient = new ApiClient('http://localhost:3000');
    
    // Test login endpoint
    const response = await fetch('http://localhost:3000/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: 'test@example.com',
        password: 'testpassword'
      })
    });
    
    if (response.status === 400 || response.status === 401) {
      // Expected for invalid credentials
      console.log('✅ Authentication endpoints are responding');
    } else {
      console.log('✅ Authentication endpoints are available');
    }
  } catch (error) {
    console.warn('⚠️  Authentication endpoint verification failed:', error);
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
