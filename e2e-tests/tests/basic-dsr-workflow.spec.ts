import { test, expect } from '@playwright/test';

test.describe('DSR Basic Workflow Tests', () => {
  test('should load DSR frontend and verify basic navigation', async ({ page }) => {
    // Navigate to the DSR frontend
    await page.goto('http://localhost:3000');
    
    // Verify the page loads
    await expect(page).toHaveTitle(/DSR/);
    
    // Check if login page is accessible
    await page.goto('http://localhost:3000/login');
    await expect(page.locator('h1, h2, [data-testid="login-title"]')).toBeVisible();
    
    // Take a screenshot for visual verification
    await page.screenshot({ path: 'test-results/dsr-login-page.png' });
  });

  test('should verify DSR services are accessible', async ({ page }) => {
    // Test Registration Service health endpoint
    const registrationResponse = await page.request.get('http://localhost:8080/actuator/health');
    expect(registrationResponse.status()).toBe(200);
    
    // Test Data Management Service health endpoint  
    const dataResponse = await page.request.get('http://localhost:8081/actuator/health');
    expect(dataResponse.status()).toBe(200);
    
    // Test Eligibility Service health endpoint
    const eligibilityResponse = await page.request.get('http://localhost:8082/actuator/health');
    expect(eligibilityResponse.status()).toBe(200);
    
    // Test Grievance Service health endpoint
    const grievanceResponse = await page.request.get('http://localhost:8085/actuator/health');
    expect(grievanceResponse.status()).toBe(200);
  });

  test('should navigate through DSR frontend pages', async ({ page }) => {
    // Start at the home page
    await page.goto('http://localhost:3000');
    
    // Try to navigate to different sections if they exist
    const possibleLinks = [
      'a[href*="register"]',
      'a[href*="login"]', 
      'a[href*="dashboard"]',
      'a[href*="eligibility"]',
      'a[href*="payment"]'
    ];
    
    for (const linkSelector of possibleLinks) {
      const link = page.locator(linkSelector).first();
      if (await link.isVisible()) {
        await link.click();
        await page.waitForLoadState('networkidle');
        await page.screenshot({ path: `test-results/dsr-page-${linkSelector.replace(/[^a-z]/g, '')}.png` });
        await page.goBack();
      }
    }
  });
});
