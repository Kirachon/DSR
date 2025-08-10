// Global Setup for DSR Integration Testing
// Prepares test environment and validates all services are ready

import { chromium, FullConfig } from '@playwright/test';

async function globalSetup(config: FullConfig) {
  console.log('🚀 Starting DSR Integration Test Global Setup...');

  const browser = await chromium.launch();
  const context = await browser.newContext();
  const page = await context.newPage();

  try {
    // Configuration
    const BASE_URL = process.env.BASE_URL || 'http://localhost:3000';
    const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080';

    // Service endpoints to validate
    const services = {
      registration: `${API_BASE_URL}/api/v1/registration/health`,
      dataManagement: `${API_BASE_URL}/api/v1/data-management/health`,
      eligibility: `${API_BASE_URL}/api/v1/eligibility/health`,
      payment: `${API_BASE_URL}/api/v1/payments/health`,
      interoperability: `${API_BASE_URL}/api/v1/interoperability/health`,
      grievance: `${API_BASE_URL}/api/v1/grievances/health`,
      analytics: `${API_BASE_URL}/api/v1/analytics/health`
    };

    console.log('📋 Validating service availability...');

    // Check each service health
    const healthChecks = [];
    for (const [serviceName, healthUrl] of Object.entries(services)) {
      try {
        const response = await page.request.get(healthUrl, {
          timeout: 10000
        });
        
        if (response.status() === 200) {
          console.log(`✅ ${serviceName} service is healthy`);
          healthChecks.push({ service: serviceName, status: 'UP' });
        } else {
          console.warn(`⚠️ ${serviceName} service returned status ${response.status()}`);
          healthChecks.push({ service: serviceName, status: 'DOWN' });
        }
      } catch (error) {
        console.error(`❌ ${serviceName} service is not accessible: ${error}`);
        healthChecks.push({ service: serviceName, status: 'ERROR' });
      }
    }

    // Validate all services are healthy
    const unhealthyServices = healthChecks.filter(check => check.status !== 'UP');
    if (unhealthyServices.length > 0) {
      console.error('❌ Some services are not healthy:');
      unhealthyServices.forEach(service => {
        console.error(`   - ${service.service}: ${service.status}`);
      });
      throw new Error(`${unhealthyServices.length} services are not healthy. Cannot proceed with integration tests.`);
    }

    console.log('🔐 Setting up test authentication...');

    // Authenticate test users
    const testUsers = [
      { email: 'citizen@test.ph', password: 'TestPassword123!', role: 'CITIZEN' },
      { email: 'lgu.staff@test.ph', password: 'TestPassword123!', role: 'LGU_STAFF' },
      { email: 'dswd.staff@test.ph', password: 'TestPassword123!', role: 'DSWD_STAFF' },
      { email: 'case.worker@test.ph', password: 'TestPassword123!', role: 'CASE_WORKER' },
      { email: 'admin@test.ph', password: 'TestPassword123!', role: 'SYSTEM_ADMIN' }
    ];

    const authTokens: Record<string, string> = {};

    for (const user of testUsers) {
      try {
        const authResponse = await page.request.post(`${API_BASE_URL}/api/v1/auth/login`, {
          data: {
            email: user.email,
            password: user.password
          },
          timeout: 10000
        });

        if (authResponse.ok()) {
          const authData = await authResponse.json();
          authTokens[user.role] = authData.accessToken;
          console.log(`✅ Authenticated ${user.role}`);
        } else {
          console.warn(`⚠️ Failed to authenticate ${user.role}: ${authResponse.status()}`);
        }
      } catch (error) {
        console.error(`❌ Authentication error for ${user.role}: ${error}`);
      }
    }

    // Store auth tokens for tests
    process.env.TEST_AUTH_TOKENS = JSON.stringify(authTokens);

    console.log('🗄️ Validating database connectivity...');

    // Test database connectivity through services
    try {
      const dbTestResponse = await page.request.get(`${API_BASE_URL}/api/v1/registration/health`, {
        headers: {
          'Authorization': `Bearer ${authTokens.SYSTEM_ADMIN}`
        },
        timeout: 10000
      });

      if (dbTestResponse.ok()) {
        const healthData = await dbTestResponse.json();
        if (healthData.components?.db?.status === 'UP') {
          console.log('✅ Database connectivity validated');
        } else {
          console.warn('⚠️ Database connectivity issues detected');
        }
      }
    } catch (error) {
      console.error(`❌ Database connectivity test failed: ${error}`);
    }

    console.log('🧹 Cleaning up test data...');

    // Clean up any existing test data
    try {
      const cleanupResponse = await page.request.delete(`${API_BASE_URL}/api/v1/test/cleanup`, {
        headers: {
          'Authorization': `Bearer ${authTokens.SYSTEM_ADMIN}`
        },
        timeout: 30000
      });

      if (cleanupResponse.ok()) {
        console.log('✅ Test data cleanup completed');
      } else {
        console.warn('⚠️ Test data cleanup returned non-200 status');
      }
    } catch (error) {
      console.warn(`⚠️ Test data cleanup failed (this may be expected): ${error}`);
    }

    console.log('📊 Generating test data...');

    // Generate baseline test data if needed
    try {
      const testDataResponse = await page.request.post(`${API_BASE_URL}/api/v1/test/generate-baseline`, {
        headers: {
          'Authorization': `Bearer ${authTokens.SYSTEM_ADMIN}`,
          'Content-Type': 'application/json'
        },
        data: {
          householdCount: 10,
          includePayments: true,
          includeGrievances: true
        },
        timeout: 60000
      });

      if (testDataResponse.ok()) {
        console.log('✅ Baseline test data generated');
      } else {
        console.warn('⚠️ Baseline test data generation returned non-200 status');
      }
    } catch (error) {
      console.warn(`⚠️ Baseline test data generation failed (this may be expected): ${error}`);
    }

    console.log('🔍 Validating frontend accessibility...');

    // Validate frontend is accessible
    try {
      await page.goto(BASE_URL, { timeout: 30000 });
      await page.waitForLoadState('networkidle', { timeout: 10000 });
      
      // Check if the page loaded successfully
      const title = await page.title();
      if (title && title.length > 0) {
        console.log(`✅ Frontend is accessible: "${title}"`);
      } else {
        console.warn('⚠️ Frontend loaded but title is empty');
      }
    } catch (error) {
      console.error(`❌ Frontend accessibility test failed: ${error}`);
      throw new Error('Frontend is not accessible. Cannot proceed with integration tests.');
    }

    console.log('⚡ Validating API Gateway...');

    // Test API Gateway routing
    try {
      const gatewayResponse = await page.request.get(`${API_BASE_URL}/api/v1/system/info`, {
        headers: {
          'Authorization': `Bearer ${authTokens.SYSTEM_ADMIN}`
        },
        timeout: 10000
      });

      if (gatewayResponse.ok()) {
        const systemInfo = await gatewayResponse.json();
        console.log(`✅ API Gateway is functional: ${systemInfo.version || 'Unknown version'}`);
      } else {
        console.warn(`⚠️ API Gateway test returned status ${gatewayResponse.status()}`);
      }
    } catch (error) {
      console.error(`❌ API Gateway test failed: ${error}`);
    }

    // Store setup completion timestamp
    process.env.SETUP_COMPLETED_AT = new Date().toISOString();

    console.log('✅ DSR Integration Test Global Setup completed successfully!');
    console.log(`📅 Setup completed at: ${process.env.SETUP_COMPLETED_AT}`);
    console.log('🧪 Ready to run integration tests...\n');

  } catch (error) {
    console.error('❌ Global setup failed:', error);
    throw error;
  } finally {
    await context.close();
    await browser.close();
  }
}

export default globalSetup;
