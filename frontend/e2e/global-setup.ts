import { chromium, FullConfig } from '@playwright/test';

// Test users configuration
const testUsers = {
  citizen: {
    email: 'citizen@dsr.test',
    password: 'TestPassword123!',
    role: 'CITIZEN',
    theme: 'citizen',
  },
  dswdStaff: {
    email: 'staff@dsr.gov.ph',
    password: 'StaffPassword123!',
    role: 'DSWD_STAFF',
    theme: 'dswd-staff',
  },
  lguStaff: {
    email: 'lgu@local.gov.ph',
    password: 'LguPassword123!',
    role: 'LGU_STAFF',
    theme: 'lgu-staff',
  },
  admin: {
    email: 'admin@dsr.gov.ph',
    password: 'AdminPassword123!',
    role: 'ADMIN',
    theme: 'dswd-staff',
  },
};

/**
 * Global setup for Playwright tests
 * Prepares authentication states and test environment
 */
async function globalSetup(config: FullConfig) {
  console.log('🚀 Starting DSR E2E test setup...');
  
  const browser = await chromium.launch();
  
  try {
    // Create authenticated storage states for each user role
    await createAuthenticatedStates(browser);
    
    // Verify test environment
    await verifyTestEnvironment(browser);
    
    console.log('✅ DSR E2E test setup completed successfully');
  } catch (error) {
    console.error('❌ DSR E2E test setup failed:', error);
    throw error;
  } finally {
    await browser.close();
  }
}

async function createAuthenticatedStates(browser: any) {
  console.log('🔐 Creating authenticated storage states...');
  
  for (const [userType, userData] of Object.entries(testUsers)) {
    const context = await browser.newContext();
    const page = await context.newPage();
    
    try {
      // Navigate to login page
      await page.goto('http://localhost:3000/login');
      
      // Set theme preference
      await page.addInitScript((theme) => {
        localStorage.setItem('dsr-theme-preference', theme);
      }, userData.theme);
      
      // Perform login
      await page.fill('[data-testid="email"]', userData.email);
      await page.fill('[data-testid="password"]', userData.password);
      await page.click('[data-testid="login-button"]');
      
      // Wait for successful login
      await page.waitForURL(/\/dashboard/, { timeout: 30000 });
      
      // Verify authentication
      await page.waitForSelector('[data-testid="user-menu"]', { timeout: 10000 });
      
      // Save storage state
      await context.storageState({ 
        path: `./e2e/fixtures/storage-states/${userType}.json` 
      });
      
      console.log(`✅ Created storage state for ${userType}`);
      
    } catch (error) {
      console.error(`❌ Failed to create storage state for ${userType}:`, error);
      // Continue with other users even if one fails
    } finally {
      await context.close();
    }
  }
}

async function verifyTestEnvironment(browser: any) {
  console.log('🔍 Verifying test environment...');
  
  const context = await browser.newContext();
  const page = await context.newPage();
  
  try {
    // Check if the application is running
    await page.goto('http://localhost:3000', { timeout: 30000 });
    
    // Verify essential elements are present
    await page.waitForSelector('body', { timeout: 10000 });
    
    // Check if design tokens are loaded
    const hasDesignTokens = await page.evaluate(() => {
      const styles = getComputedStyle(document.documentElement);
      return styles.getPropertyValue('--dsr-philippine-government-primary-500').trim() !== '';
    });
    
    if (!hasDesignTokens) {
      console.warn('⚠️ Design tokens may not be loaded properly');
    }
    
    // Verify API endpoints are accessible
    const healthChecks = await Promise.allSettled([
      page.request.get('http://localhost:8080/api/v1/health'),
      page.request.get('http://localhost:8082/api/v1/health'),
      page.request.get('http://localhost:8085/api/v1/health'),
    ]);
    
    const healthyServices = healthChecks.filter(result => 
      result.status === 'fulfilled' && result.value.ok()
    ).length;
    
    console.log(`📊 ${healthyServices}/${healthChecks.length} microservices are healthy`);
    
    console.log('✅ Test environment verification completed');
    
  } catch (error) {
    console.error('❌ Test environment verification failed:', error);
    throw error;
  } finally {
    await context.close();
  }
}

export default globalSetup;
