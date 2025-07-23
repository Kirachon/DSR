import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright Configuration for DSR Authentication Workflow Testing
 * Configured for HEADED mode (visible browser) for comprehensive E2E testing
 */
export default defineConfig({
  testDir: './e2e',
  
  /* Run tests in files in parallel */
  fullyParallel: false, // Set to false for better visibility during headed testing
  
  /* Fail the build on CI if you accidentally left test.only in the source code. */
  forbidOnly: !!process.env.CI,
  
  /* Retry on CI only */
  retries: process.env.CI ? 2 : 0,
  
  /* Opt out of parallel tests on CI. */
  workers: process.env.CI ? 1 : 1, // Single worker for headed mode
  
  /* Reporter to use. See https://playwright.dev/docs/test-reporters */
  reporter: [
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
    ['list'],
    ['json', { outputFile: 'test-results/results.json' }]
  ],
  
  /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
  use: {
    /* Base URL to use in actions like `await page.goto('/')`. */
    baseURL: 'http://localhost:3000',
    
    /* Collect trace when retrying the failed test. See https://playwright.dev/docs/trace-viewer */
    trace: 'on-first-retry',
    
    /* Take screenshot on failure */
    screenshot: 'only-on-failure',
    
    /* Record video on failure */
    video: 'retain-on-failure',
    
    /* Headed mode - browser will be visible */
    headless: false,
    
    /* Slow down operations for better visibility */
    slowMo: 500, // 500ms delay between actions
    
    /* Browser viewport */
    viewport: { width: 1280, height: 720 },
    
    /* Ignore HTTPS errors */
    ignoreHTTPSErrors: true,
    
    /* Timeout for each action */
    actionTimeout: 10000,
    
    /* Timeout for navigation */
    navigationTimeout: 30000,
  },

  /* Configure projects for major browsers */
  projects: [
    {
      name: 'chromium-headed',
      use: { 
        ...devices['Desktop Chrome'],
        headless: false,
        slowMo: 500,
        launchOptions: {
          args: [
            '--start-maximized',
            '--disable-web-security',
            '--disable-features=VizDisplayCompositor'
          ]
        }
      },
    },

    {
      name: 'firefox-headed',
      use: { 
        ...devices['Desktop Firefox'],
        headless: false,
        slowMo: 500,
        launchOptions: {
          firefoxUserPrefs: {
            'security.tls.insecure_fallback_hosts': 'localhost'
          }
        }
      },
    },

    {
      name: 'webkit-headed',
      use: { 
        ...devices['Desktop Safari'],
        headless: false,
        slowMo: 500,
      },
    },

    /* Test against mobile viewports. */
    {
      name: 'Mobile Chrome',
      use: { 
        ...devices['Pixel 5'],
        headless: false,
        slowMo: 750, // Slower for mobile testing
      },
    },
    {
      name: 'Mobile Safari',
      use: { 
        ...devices['iPhone 12'],
        headless: false,
        slowMo: 750,
      },
    },

    /* Test against branded browsers. */
    {
      name: 'Microsoft Edge',
      use: { 
        ...devices['Desktop Edge'], 
        channel: 'msedge',
        headless: false,
        slowMo: 500,
      },
    },
    {
      name: 'Google Chrome',
      use: { 
        ...devices['Desktop Chrome'], 
        channel: 'chrome',
        headless: false,
        slowMo: 500,
      },
    },
  ],

  /* Run your local dev server before starting the tests */
  // webServer: {
  //   command: 'npm run dev',
  //   url: 'http://localhost:3000',
  //   reuseExistingServer: true, // Always reuse existing server
  //   timeout: 120000, // 2 minutes to start the server
  //   stdout: 'pipe',
  //   stderr: 'pipe',
  // },

  /* Global setup and teardown - disabled for simple auth workflow tests */
  // globalSetup: require.resolve('./e2e/global-setup.ts'),
  // globalTeardown: require.resolve('./e2e/global-teardown.ts'),

  /* Test timeout */
  timeout: 60000, // 1 minute per test

  /* Expect timeout */
  expect: {
    timeout: 10000, // 10 seconds for assertions
  },

  /* Output directory for test artifacts */
  outputDir: 'test-results/',

  /* Test match patterns */
  testMatch: [
    '**/dsr-authentication-workflow.spec.ts',
    '**/basic-functionality.spec.ts'
  ],
});
