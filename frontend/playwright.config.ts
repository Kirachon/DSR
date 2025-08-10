import { defineConfig, devices } from '@playwright/test';

/**
 * DSR Frontend E2E Testing Configuration
 * Comprehensive testing across browsers, devices, and user roles
 */
export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ['html', { open: 'never' }],
    ['junit', { outputFile: 'test-results/junit.xml' }],
    ['list']
  ],
  
  // Global setup to run before all tests
  globalSetup: './e2e/global-setup.ts',
  
  // Global teardown to run after all tests
  globalTeardown: './e2e/global-teardown.ts',
  
  // Shared settings for all projects
  use: {
    // Base URL to use in actions like `await page.goto('/')`
    baseURL: process.env.PLAYWRIGHT_TEST_BASE_URL || 'http://localhost:3000',
    
    // Collect trace when retrying the failed test
    trace: 'on-first-retry',
    
    // Capture screenshot on failure
    screenshot: 'only-on-failure',
    
    // Record video on failure
    video: 'on-first-retry',
    
    // Viewport size
    viewport: { width: 1280, height: 720 },
    
    // Automatically wait for elements
    actionTimeout: 10000,
    
    // Automatically wait for navigation
    navigationTimeout: 30000,
    
    // Ignore HTTPS errors
    ignoreHTTPSErrors: true,
    
    // Capture console logs
    acceptDownloads: true,
  },
  
  // Configure projects for different browsers and devices
  projects: [
    // Desktop browsers
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },
    
    // Mobile browsers
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] },
    },
    {
      name: 'Mobile Safari',
      use: { ...devices['iPhone 12'] },
    },
    
    // Tablet browsers
    {
      name: 'iPad',
      use: { ...devices['iPad (gen 7)'] },
    },
    
    // User role-specific testing
    {
      name: 'Citizen Role',
      use: { 
        ...devices['Desktop Chrome'],
        storageState: './e2e/fixtures/storage-states/citizen.json',
      },
    },
    {
      name: 'DSWD Staff Role',
      use: { 
        ...devices['Desktop Chrome'],
        storageState: './e2e/fixtures/storage-states/dswd-staff.json',
      },
    },
    {
      name: 'LGU Staff Role',
      use: { 
        ...devices['Desktop Chrome'],
        storageState: './e2e/fixtures/storage-states/lgu-staff.json',
      },
    },
    
    // Accessibility testing
    {
      name: 'Accessibility',
      use: { 
        ...devices['Desktop Chrome'],
        // Reduced motion for accessibility testing
        contextOptions: {
          reducedMotion: 'reduce',
          forcedColors: 'active',
        },
      },
    },
    
    // Performance testing
    {
      name: 'Performance',
      use: { 
        ...devices['Desktop Chrome'],
        // Throttle CPU and network for performance testing
        contextOptions: {
          reducedMotion: 'reduce',
        },
        launchOptions: {
          args: [
            '--disable-gpu',
            '--disable-dev-shm-usage',
            '--disable-setuid-sandbox',
            '--no-sandbox',
          ],
        },
      },
    },
  ],
  
  // Web server to start before running tests
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI,
    stdout: 'pipe',
    stderr: 'pipe',
    timeout: 60000,
  },
  
  // Timeout for each test
  timeout: 60000,
  
  // Expect timeout
  expect: {
    timeout: 10000,
    toHaveScreenshot: {
      maxDiffPixelRatio: 0.05,
    },
    toMatchSnapshot: {
      maxDiffPixelRatio: 0.05,
    },
  },
});
