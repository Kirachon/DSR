import { defineConfig, devices } from '@playwright/test';
import * as dotenv from 'dotenv';

// Load environment variables
dotenv.config();

/**
 * Playwright configuration for DSR E2E tests
 * Supports cross-browser testing, mobile emulation, and CI/CD integration
 */
export default defineConfig({
  // Test directory
  testDir: './tests',
  
  // Global test timeout
  timeout: 60000,
  
  // Expect timeout for assertions
  expect: {
    timeout: 10000,
  },
  
  // Fail the build on CI if you accidentally left test.only in the source code
  forbidOnly: !!process.env.CI,
  
  // Retry on CI only
  retries: process.env.CI ? 2 : 0,
  
  // Opt out of parallel tests on CI
  workers: process.env.CI ? 1 : undefined,
  
  // Reporter configuration
  reporter: [
    ['html', { outputFolder: 'test-results/html-report' }],
    ['junit', { outputFile: 'test-results/junit-report.xml' }],
    ['allure-playwright', { outputFolder: 'allure-results' }],
    ['list'],
  ],
  
  // Global setup and teardown
  globalSetup: require.resolve('./src/setup/global-setup.ts'),
  globalTeardown: require.resolve('./src/setup/global-teardown.ts'),
  
  // Shared settings for all projects
  use: {
    // Base URL for tests
    baseURL: process.env.BASE_URL || 'http://localhost:3000',
    
    // Global test timeout
    actionTimeout: 15000,
    
    // Capture screenshot on failure
    screenshot: 'only-on-failure',
    
    // Capture video on failure
    video: 'retain-on-failure',
    
    // Capture trace on failure
    trace: 'retain-on-failure',
    
    // Ignore HTTPS errors
    ignoreHTTPSErrors: true,
    
    // Extra HTTP headers
    extraHTTPHeaders: {
      'Accept-Language': 'en-US,en;q=0.9',
    },
  },

  // Configure projects for major browsers
  projects: [
    // Desktop browsers
    {
      name: 'chromium',
      use: { 
        ...devices['Desktop Chrome'],
        viewport: { width: 1920, height: 1080 },
      },
    },
    {
      name: 'firefox',
      use: { 
        ...devices['Desktop Firefox'],
        viewport: { width: 1920, height: 1080 },
      },
    },
    {
      name: 'webkit',
      use: { 
        ...devices['Desktop Safari'],
        viewport: { width: 1920, height: 1080 },
      },
    },
    
    // Mobile browsers
    {
      name: 'mobile-chrome',
      use: { ...devices['Pixel 5'] },
    },
    {
      name: 'mobile-safari',
      use: { ...devices['iPhone 12'] },
    },
    
    // Tablet
    {
      name: 'tablet',
      use: { ...devices['iPad Pro'] },
    },
  ],

  // Run your local dev server before starting the tests
  webServer: process.env.CI ? undefined : {
    command: 'npm run start:test-server',
    port: 3000,
    reuseExistingServer: !process.env.CI,
    timeout: 120000,
  },
  
  // Output directory for test artifacts
  outputDir: 'test-results/artifacts',
  
  // Test match patterns
  testMatch: [
    '**/*.spec.ts',
    '**/*.test.ts',
  ],
  
  // Test ignore patterns
  testIgnore: [
    '**/node_modules/**',
    '**/dist/**',
    '**/build/**',
  ],
});
