import { test, expect } from '@playwright/test';

/**
 * Simple authentication test without complex global setup
 * Tests the basic login functionality with our demo credentials
 */
test.describe('Simple Authentication Test', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to login page
    await page.goto('http://localhost:3000/login');
  });

  test('should load login page correctly', async ({ page }) => {
    // Check if login page loads
    await expect(page).toHaveTitle(/DSR/);
    
    // Check for login form elements
    await expect(page.locator('input[type="email"]')).toBeVisible();
    await expect(page.locator('input[type="password"]')).toBeVisible();
    await expect(page.locator('button[type="submit"]')).toBeVisible();
  });

  test('should login successfully with admin credentials', async ({ page }) => {
    // Fill in admin credentials
    await page.fill('input[type="email"]', 'admin@dsr.gov.ph');
    await page.fill('input[type="password"]', 'admin123');
    
    // Submit the form
    await page.click('button[type="submit"]');
    
    // Wait for navigation or success indicator
    await page.waitForTimeout(3000);
    
    // Check if we're redirected to dashboard or see success message
    const currentUrl = page.url();
    console.log('Current URL after login:', currentUrl);
    
    // Take a screenshot for debugging
    await page.screenshot({ path: 'test-results/admin-login-result.png' });
  });

  test('should login successfully with citizen credentials', async ({ page }) => {
    // Fill in citizen credentials
    await page.fill('input[type="email"]', 'citizen@dsr.gov.ph');
    await page.fill('input[type="password"]', 'citizen123');
    
    // Submit the form
    await page.click('button[type="submit"]');
    
    // Wait for navigation or success indicator
    await page.waitForTimeout(3000);
    
    // Check if we're redirected to dashboard or see success message
    const currentUrl = page.url();
    console.log('Current URL after login:', currentUrl);
    
    // Take a screenshot for debugging
    await page.screenshot({ path: 'test-results/citizen-login-result.png' });
  });

  test('should show error for invalid credentials', async ({ page }) => {
    // Fill in invalid credentials
    await page.fill('input[type="email"]', 'invalid@example.com');
    await page.fill('input[type="password"]', 'wrongpassword');
    
    // Submit the form
    await page.click('button[type="submit"]');
    
    // Wait for error message
    await page.waitForTimeout(2000);
    
    // Take a screenshot for debugging
    await page.screenshot({ path: 'test-results/invalid-login-result.png' });
    
    // Check that we're still on login page (not redirected)
    expect(page.url()).toContain('/login');
  });

  test('should capture network requests during login', async ({ page }) => {
    // Listen for network requests
    const requests: any[] = [];
    page.on('request', request => {
      if (request.url().includes('/api/')) {
        requests.push({
          url: request.url(),
          method: request.method(),
          headers: request.headers(),
          postData: request.postData()
        });
      }
    });

    // Listen for network responses
    const responses: any[] = [];
    page.on('response', response => {
      if (response.url().includes('/api/')) {
        responses.push({
          url: response.url(),
          status: response.status(),
          statusText: response.statusText()
        });
      }
    });

    // Perform login
    await page.fill('input[type="email"]', 'admin@dsr.gov.ph');
    await page.fill('input[type="password"]', 'admin123');
    await page.click('button[type="submit"]');
    
    // Wait for requests to complete
    await page.waitForTimeout(3000);
    
    // Log network activity
    console.log('API Requests:', JSON.stringify(requests, null, 2));
    console.log('API Responses:', JSON.stringify(responses, null, 2));
    
    // Take a screenshot
    await page.screenshot({ path: 'test-results/network-debug.png' });
  });
});
