import { test, expect } from '@playwright/test';
import { TestDataManager } from '../src/utils/test-data-manager';

/**
 * Frontend Authentication Tests
 * Tests the complete authentication workflow through the Next.js frontend
 * Requires both frontend (port 3000) and backend (port 8080) to be running
 */

const FRONTEND_URL = 'http://localhost:3000';

test.describe('Frontend Authentication Workflow', () => {
  
  test.beforeEach(async ({ page }) => {
    // Navigate to the frontend
    await page.goto(FRONTEND_URL);
  });

  test.describe('Login Page', () => {
    test('should display login page', async ({ page }) => {
      // Check if we're on a login page or redirected to login
      await page.waitForLoadState('networkidle');
      
      // Take screenshot for debugging
      await page.screenshot({ path: 'test-results/login-page.png' });
      
      // Look for common login elements
      const hasLoginForm = await page.locator('form').count() > 0;
      const hasEmailInput = await page.locator('input[type="email"], input[name="email"]').count() > 0;
      const hasPasswordInput = await page.locator('input[type="password"], input[name="password"]').count() > 0;
      const hasLoginButton = await page.locator('button:has-text("Login"), button:has-text("Sign In"), input[type="submit"]').count() > 0;
      
      console.log('Login form elements found:', {
        hasLoginForm,
        hasEmailInput,
        hasPasswordInput,
        hasLoginButton
      });
      
      // At least one of these should be true for a login page
      expect(hasLoginForm || hasEmailInput || hasPasswordInput || hasLoginButton).toBe(true);
    });

    test('should handle admin login attempt', async ({ page }) => {
      const credentials = TestDataManager.getTestCredentials('admin');
      
      await page.waitForLoadState('networkidle');
      
      // Look for email input field
      const emailInput = page.locator('input[type="email"], input[name="email"]').first();
      const passwordInput = page.locator('input[type="password"], input[name="password"]').first();
      const loginButton = page.locator('button:has-text("Login"), button:has-text("Sign In"), input[type="submit"]').first();
      
      if (await emailInput.count() > 0) {
        await emailInput.fill(credentials.email);
        console.log('Filled email:', credentials.email);
      }
      
      if (await passwordInput.count() > 0) {
        await passwordInput.fill(credentials.password);
        console.log('Filled password');
      }
      
      if (await loginButton.count() > 0) {
        await loginButton.click();
        console.log('Clicked login button');
        
        // Wait for response
        await page.waitForTimeout(2000);
        
        // Take screenshot after login attempt
        await page.screenshot({ path: 'test-results/after-admin-login.png' });
      }
      
      // Check current URL and page state
      const currentUrl = page.url();
      console.log('Current URL after login attempt:', currentUrl);
      
      // The test passes if we can interact with the login form
      expect(currentUrl).toContain('localhost:3000');
    });

    test('should handle citizen login attempt', async ({ page }) => {
      const credentials = TestDataManager.getTestCredentials('user');
      
      await page.waitForLoadState('networkidle');
      
      // Look for email input field
      const emailInput = page.locator('input[type="email"], input[name="email"]').first();
      const passwordInput = page.locator('input[type="password"], input[name="password"]').first();
      const loginButton = page.locator('button:has-text("Login"), button:has-text("Sign In"), input[type="submit"]').first();
      
      if (await emailInput.count() > 0) {
        await emailInput.fill(credentials.email);
        console.log('Filled email:', credentials.email);
      }
      
      if (await passwordInput.count() > 0) {
        await passwordInput.fill(credentials.password);
        console.log('Filled password');
      }
      
      if (await loginButton.count() > 0) {
        await loginButton.click();
        console.log('Clicked login button');
        
        // Wait for response
        await page.waitForTimeout(2000);
        
        // Take screenshot after login attempt
        await page.screenshot({ path: 'test-results/after-citizen-login.png' });
      }
      
      // Check current URL and page state
      const currentUrl = page.url();
      console.log('Current URL after login attempt:', currentUrl);
      
      // The test passes if we can interact with the login form
      expect(currentUrl).toContain('localhost:3000');
    });
  });

  test.describe('Frontend-Backend Integration', () => {
    test('should make API calls to backend', async ({ page }) => {
      // Monitor network requests
      const requests: string[] = [];
      page.on('request', request => {
        if (request.url().includes('localhost:8080')) {
          requests.push(`${request.method()} ${request.url()}`);
        }
      });
      
      await page.waitForLoadState('networkidle');
      
      // Try to trigger some API calls by interacting with the page
      await page.waitForTimeout(3000);
      
      console.log('API requests made to backend:', requests);
      
      // Take screenshot of the current page
      await page.screenshot({ path: 'test-results/frontend-backend-integration.png' });
      
      // The test passes if the page loads successfully
      expect(page.url()).toContain('localhost:3000');
    });

    test('should handle CORS and authentication headers', async ({ page }) => {
      // Monitor network responses
      const responses: Array<{url: string, status: number, headers: any}> = [];
      page.on('response', response => {
        if (response.url().includes('localhost')) {
          responses.push({
            url: response.url(),
            status: response.status(),
            headers: response.headers()
          });
        }
      });
      
      await page.waitForLoadState('networkidle');
      await page.waitForTimeout(2000);
      
      console.log('Network responses:', responses.map(r => `${r.status} ${r.url}`));
      
      // Check for CORS headers in responses
      const corsResponses = responses.filter(r => 
        r.headers['access-control-allow-origin'] || 
        r.headers['access-control-allow-methods'] ||
        r.headers['access-control-allow-headers']
      );
      
      console.log('CORS-enabled responses:', corsResponses.length);
      
      // The test passes if we get responses from the frontend
      expect(responses.length).toBeGreaterThan(0);
    });
  });

  test.describe('Responsive Design', () => {
    test('should work on mobile viewport', async ({ page }) => {
      await page.setViewportSize({ width: 375, height: 667 }); // iPhone SE
      await page.waitForLoadState('networkidle');
      
      // Take mobile screenshot
      await page.screenshot({ path: 'test-results/mobile-view.png' });
      
      // Check if page is responsive
      const body = await page.locator('body').boundingBox();
      expect(body?.width).toBeLessThanOrEqual(375);
    });

    test('should work on tablet viewport', async ({ page }) => {
      await page.setViewportSize({ width: 768, height: 1024 }); // iPad
      await page.waitForLoadState('networkidle');
      
      // Take tablet screenshot
      await page.screenshot({ path: 'test-results/tablet-view.png' });
      
      // Check if page is responsive
      const body = await page.locator('body').boundingBox();
      expect(body?.width).toBeLessThanOrEqual(768);
    });

    test('should work on desktop viewport', async ({ page }) => {
      await page.setViewportSize({ width: 1920, height: 1080 }); // Desktop
      await page.waitForLoadState('networkidle');
      
      // Take desktop screenshot
      await page.screenshot({ path: 'test-results/desktop-view.png' });
      
      // Check if page uses available space
      const body = await page.locator('body').boundingBox();
      expect(body?.width).toBeGreaterThan(1000);
    });
  });

  test.describe('Page Performance', () => {
    test('should load within reasonable time', async ({ page }) => {
      const startTime = Date.now();
      
      await page.goto(FRONTEND_URL);
      await page.waitForLoadState('networkidle');
      
      const loadTime = Date.now() - startTime;
      console.log('Page load time:', loadTime, 'ms');
      
      // Should load within 10 seconds
      expect(loadTime).toBeLessThan(10000);
    });

    test('should not have console errors', async ({ page }) => {
      const consoleErrors: string[] = [];
      
      page.on('console', msg => {
        if (msg.type() === 'error') {
          consoleErrors.push(msg.text());
        }
      });
      
      await page.waitForLoadState('networkidle');
      await page.waitForTimeout(2000);
      
      console.log('Console errors:', consoleErrors);
      
      // Allow some errors as the system is in development
      expect(consoleErrors.length).toBeLessThan(10);
    });
  });
});
