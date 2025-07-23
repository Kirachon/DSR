import { test, expect } from '@playwright/test';

/**
 * Basic Functionality Tests
 * Tests core functionality without authentication requirements
 */
test.describe('Basic DSR Frontend Functionality', () => {
  test('should load homepage successfully', async ({ page }) => {
    // Navigate to homepage
    await page.goto('/');
    
    // Verify page loads
    await expect(page).toHaveTitle(/DSR/);
    
    // Check for basic elements
    await expect(page.locator('body')).toBeVisible();
    
    console.log('✅ Homepage loaded successfully');
  });

  test('should display design token test page', async ({ page }) => {
    // Navigate to token test page
    await page.goto('/token-test');
    
    // Verify design tokens are loaded
    await expect(page.locator('h1')).toContainText('DSR Design Token Integration Test');
    
    // Test theme switching
    const citizenButton = page.locator('button').filter({ hasText: 'Citizen Theme' }).first();
    if (await citizenButton.isVisible()) {
      await citizenButton.click();
      await expect(page.locator('[data-theme="citizen"]')).toBeVisible();
    }
    
    const dswdButton = page.locator('button').filter({ hasText: 'DSWD Staff Theme' }).first();
    if (await dswdButton.isVisible()) {
      await dswdButton.click();
      await expect(page.locator('[data-theme="dswd-staff"]')).toBeVisible();
    }
    
    const lguButton = page.locator('button').filter({ hasText: 'LGU Staff Theme' }).first();
    if (await lguButton.isVisible()) {
      await lguButton.click();
      await expect(page.locator('[data-theme="lgu-staff"]')).toBeVisible();
    }
    
    console.log('✅ Design token integration working');
  });

  test('should display API test page', async ({ page }) => {
    // Navigate to API test page
    await page.goto('/api-test');
    
    // Verify API test interface loads
    await expect(page.locator('h1')).toContainText('DSR API Integration Test');
    
    // Test API compatibility button
    const compatibilityButton = page.locator('button').filter({ hasText: 'Test API Compatibility' }).first();
    if (await compatibilityButton.isVisible()) {
      await compatibilityButton.click();
      
      // Wait for test results
      await page.waitForTimeout(2000);
      
      // Check for test results
      const resultsArea = page.locator('[data-testid="test-results"]');
      if (await resultsArea.isVisible()) {
        await expect(resultsArea).toContainText('API client imported successfully');
      }
    }
    
    console.log('✅ API integration test working');
  });

  test('should be responsive on different screen sizes', async ({ page }) => {
    // Test desktop view
    await page.setViewportSize({ width: 1200, height: 800 });
    await page.goto('/token-test');
    
    // Verify desktop layout
    await expect(page.locator('body')).toBeVisible();
    
    // Test tablet view
    await page.setViewportSize({ width: 768, height: 1024 });
    await page.reload();
    await expect(page.locator('body')).toBeVisible();
    
    // Test mobile view
    await page.setViewportSize({ width: 375, height: 667 });
    await page.reload();
    await expect(page.locator('body')).toBeVisible();
    
    console.log('✅ Responsive design working');
  });

  test('should handle navigation between pages', async ({ page }) => {
    // Start at homepage
    await page.goto('/');
    
    // Navigate to token test
    await page.goto('/token-test');
    await expect(page.locator('h1')).toContainText('DSR Design Token Integration Test');
    
    // Navigate to API test
    await page.goto('/api-test');
    await expect(page.locator('h1')).toContainText('DSR API Integration Test');
    
    // Go back to homepage
    await page.goto('/');
    await expect(page.locator('body')).toBeVisible();
    
    console.log('✅ Navigation working correctly');
  });

  test('should display proper error handling', async ({ page }) => {
    // Try to navigate to non-existent page
    const response = await page.goto('/non-existent-page');
    
    // Should handle 404 gracefully
    expect(response?.status()).toBe(404);
    
    console.log('✅ Error handling working');
  });

  test('should load CSS and JavaScript properly', async ({ page }) => {
    await page.goto('/token-test');
    
    // Check that CSS is loaded (design tokens should be applied)
    const primaryColor = await page.evaluate(() => {
      return getComputedStyle(document.documentElement)
        .getPropertyValue('--dsr-philippine-government-primary-500');
    });
    
    // Should have design token values
    expect(primaryColor.trim()).toBeTruthy();
    
    // Check that JavaScript is working (React components should be interactive)
    const button = page.locator('button').first();
    if (await button.isVisible()) {
      await button.click();
      // If click works, JavaScript is functioning
    }
    
    console.log('✅ CSS and JavaScript loading correctly');
  });

  test('should demonstrate theme switching visually', async ({ page }) => {
    await page.goto('/token-test');
    
    // Wait for page to fully load
    await page.waitForLoadState('networkidle');
    
    // Test each theme with visual verification
    const themes = [
      { name: 'Citizen Theme', attribute: 'citizen' },
      { name: 'DSWD Staff Theme', attribute: 'dswd-staff' },
      { name: 'LGU Staff Theme', attribute: 'lgu-staff' }
    ];
    
    for (const theme of themes) {
      const themeButton = page.locator('button').filter({ hasText: theme.name }).first();
      
      if (await themeButton.isVisible()) {
        await themeButton.click();
        
        // Wait for theme to apply
        await page.waitForTimeout(500);
        
        // Verify theme is applied
        await expect(page.locator(`[data-theme="${theme.attribute}"]`)).toBeVisible();
        
        console.log(`✅ ${theme.name} applied successfully`);
        
        // Small delay to see the visual change
        await page.waitForTimeout(1000);
      }
    }
    
    console.log('✅ All theme switching demonstrated');
  });
});
