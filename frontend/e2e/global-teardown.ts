import { FullConfig } from '@playwright/test';
import fs from 'fs';
import path from 'path';

/**
 * Global teardown for Playwright tests
 * Cleanup test artifacts and generate reports
 */
async function globalTeardown(config: FullConfig) {
  console.log('🧹 Starting DSR E2E test teardown...');
  
  try {
    // Clean up storage states
    await cleanupStorageStates();
    
    // Generate test summary
    await generateTestSummary();
    
    console.log('✅ DSR E2E test teardown completed successfully');
  } catch (error) {
    console.error('❌ DSR E2E test teardown failed:', error);
  }
}

async function cleanupStorageStates() {
  console.log('🗑️ Cleaning up storage states...');
  
  const storageStatesDir = './e2e/fixtures/storage-states';
  
  if (fs.existsSync(storageStatesDir)) {
    const files = fs.readdirSync(storageStatesDir);
    
    for (const file of files) {
      if (file.endsWith('.json')) {
        const filePath = path.join(storageStatesDir, file);
        try {
          fs.unlinkSync(filePath);
          console.log(`🗑️ Removed storage state: ${file}`);
        } catch (error) {
          console.warn(`⚠️ Failed to remove storage state ${file}:`, error);
        }
      }
    }
  }
}

async function generateTestSummary() {
  console.log('📊 Generating test summary...');
  
  const testResultsDir = './test-results';
  const summaryFile = path.join(testResultsDir, 'test-summary.json');
  
  // Ensure test results directory exists
  if (!fs.existsSync(testResultsDir)) {
    fs.mkdirSync(testResultsDir, { recursive: true });
  }
  
  const summary = {
    timestamp: new Date().toISOString(),
    environment: {
      nodeVersion: process.version,
      platform: process.platform,
      ci: !!process.env.CI,
    },
    configuration: {
      baseURL: process.env.PLAYWRIGHT_TEST_BASE_URL || 'http://localhost:3000',
      browsers: ['chromium', 'firefox', 'webkit'],
      devices: ['Desktop', 'Mobile', 'Tablet'],
      userRoles: ['citizen', 'dswd-staff', 'lgu-staff'],
    },
    coverage: {
      target: '80%',
      components: '90%',
      e2e: '100%',
    },
    testTypes: [
      'User Journey Testing',
      'Cross-Browser Compatibility',
      'Mobile Responsiveness',
      'Accessibility Compliance',
      'Performance Validation',
      'API Integration',
      'Visual Regression',
    ],
  };
  
  try {
    fs.writeFileSync(summaryFile, JSON.stringify(summary, null, 2));
    console.log(`📄 Test summary generated: ${summaryFile}`);
  } catch (error) {
    console.warn('⚠️ Failed to generate test summary:', error);
  }
}

export default globalTeardown;
