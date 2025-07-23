import { test, expect, devices } from '@playwright/test';

/**
 * Cross-Browser Compatibility Tests
 * Ensures consistent functionality across Chrome, Firefox, and Safari
 */
test.describe('Cross-Browser Compatibility', () => {
  const browsers = ['chromium', 'firefox', 'webkit'];
  
  browsers.forEach(browserName => {
    test.describe(`${browserName} Browser Tests`, () => {
      test.use({ 
        ...devices[browserName === 'webkit' ? 'Desktop Safari' : 
                  browserName === 'firefox' ? 'Desktop Firefox' : 'Desktop Chrome']
      });

      test('should render design system components consistently', async ({ page }) => {
        await page.goto('/token-test');
        
        // Test button rendering
        const primaryButton = page.locator('[data-testid="primary-button"]').first();
        await expect(primaryButton).toBeVisible();
        
        // Verify design token application
        const computedStyle = await primaryButton.evaluate(el => {
          const styles = getComputedStyle(el);
          return {
            backgroundColor: styles.backgroundColor,
            borderRadius: styles.borderRadius,
            padding: styles.padding,
          };
        });
        
        // Verify consistent styling across browsers
        expect(computedStyle.backgroundColor).toMatch(/rgb\(59, 130, 246\)/); // Primary blue
        expect(computedStyle.borderRadius).toBe('6px');
        
        // Test theme switching
        await page.click('[data-testid="theme-switch-dswd"]');
        await expect(page.locator('[data-theme="dswd-staff"]')).toBeVisible();
        
        await page.click('[data-testid="theme-switch-lgu"]');
        await expect(page.locator('[data-theme="lgu-staff"]')).toBeVisible();
      });

      test('should handle form interactions consistently', async ({ page }) => {
        await page.goto('/register');
        
        // Test form field interactions
        await page.fill('[data-testid="full-name"]', 'Test User');
        await page.fill('[data-testid="email"]', 'test@example.com');
        await page.fill('[data-testid="password"]', 'Password123!');
        
        // Test form validation
        await page.fill('[data-testid="confirm-password"]', 'DifferentPassword');
        await page.click('[data-testid="register-button"]');
        
        await expect(page.locator('[data-testid="password-mismatch-error"]')).toBeVisible();
        
        // Fix validation error
        await page.fill('[data-testid="confirm-password"]', 'Password123!');
        await page.check('[data-testid="terms-checkbox"]');
        
        // Test successful form submission
        await page.click('[data-testid="register-button"]');
        await expect(page.locator('[data-testid="registration-success"]')).toBeVisible();
      });

      test('should support keyboard navigation', async ({ page }) => {
        await page.goto('/dashboard');
        
        // Test tab navigation
        await page.keyboard.press('Tab');
        let focusedElement = await page.locator(':focus').getAttribute('data-testid');
        expect(focusedElement).toBeTruthy();
        
        // Navigate through multiple elements
        for (let i = 0; i < 5; i++) {
          await page.keyboard.press('Tab');
        }
        
        // Test Enter key activation
        await page.keyboard.press('Enter');
        
        // Verify keyboard navigation works consistently
        const navigationItems = page.locator('[data-testid^="nav-"]');
        const count = await navigationItems.count();
        expect(count).toBeGreaterThan(0);
      });

      test('should handle JavaScript events consistently', async ({ page }) => {
        await page.goto('/');
        
        // Test click events
        await page.click('[data-testid="services-link"]');
        await expect(page).toHaveURL(/\/services/);
        
        // Test hover events
        await page.hover('[data-testid="user-menu"]');
        await expect(page.locator('[data-testid="user-dropdown"]')).toBeVisible();
        
        // Test focus events
        await page.focus('[data-testid="search-input"]');
        await expect(page.locator('[data-testid="search-suggestions"]')).toBeVisible();
        
        // Test scroll events
        await page.evaluate(() => window.scrollTo(0, 500));
        await expect(page.locator('[data-testid="scroll-to-top"]')).toBeVisible();
      });

      test('should render CSS animations and transitions', async ({ page }) => {
        await page.goto('/');
        
        // Test fade-in animation
        const fadeElement = page.locator('[data-testid="fade-in-element"]');
        await expect(fadeElement).toHaveCSS('opacity', '1');
        
        // Test hover transitions
        const hoverElement = page.locator('[data-testid="hover-element"]');
        await hoverElement.hover();
        
        // Wait for transition to complete
        await page.waitForTimeout(300);
        
        // Verify transition occurred
        const transform = await hoverElement.evaluate(el => 
          getComputedStyle(el).transform
        );
        expect(transform).not.toBe('none');
      });

      test('should handle media queries and responsive design', async ({ page }) => {
        // Test desktop layout
        await page.setViewportSize({ width: 1200, height: 800 });
        await page.goto('/dashboard');
        
        await expect(page.locator('[data-testid="desktop-layout"]')).toBeVisible();
        await expect(page.locator('[data-testid="mobile-menu"]')).not.toBeVisible();
        
        // Test tablet layout
        await page.setViewportSize({ width: 768, height: 1024 });
        await expect(page.locator('[data-testid="tablet-layout"]')).toBeVisible();
        
        // Test mobile layout
        await page.setViewportSize({ width: 375, height: 667 });
        await expect(page.locator('[data-testid="mobile-layout"]')).toBeVisible();
        await expect(page.locator('[data-testid="mobile-menu-button"]')).toBeVisible();
      });

      test('should handle local storage consistently', async ({ page }) => {
        await page.goto('/');
        
        // Set local storage values
        await page.evaluate(() => {
          localStorage.setItem('test-key', 'test-value');
          localStorage.setItem('dsr-theme-preference', 'citizen');
        });
        
        // Reload page and verify persistence
        await page.reload();
        
        const storedValue = await page.evaluate(() => 
          localStorage.getItem('test-key')
        );
        expect(storedValue).toBe('test-value');
        
        // Verify theme persistence
        await expect(page.locator('[data-theme="citizen"]')).toBeVisible();
      });

      test('should handle API requests consistently', async ({ page }) => {
        // Mock API responses
        await page.route('/api/v1/health', route => {
          route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({ status: 'UP' }),
          });
        });
        
        await page.goto('/api-test');
        
        // Test API integration
        await page.click('[data-testid="test-api-compatibility"]');
        await expect(page.locator('[data-testid="api-test-success"]')).toBeVisible();
        
        // Test error handling
        await page.route('/api/v1/health', route => {
          route.fulfill({ status: 500 });
        });
        
        await page.click('[data-testid="test-service-health"]');
        await expect(page.locator('[data-testid="api-error-message"]')).toBeVisible();
      });

      test('should support modern web features', async ({ page }) => {
        await page.goto('/');
        
        // Test CSS Grid support
        const gridContainer = page.locator('[data-testid="grid-container"]');
        const gridDisplay = await gridContainer.evaluate(el => 
          getComputedStyle(el).display
        );
        expect(gridDisplay).toBe('grid');
        
        // Test Flexbox support
        const flexContainer = page.locator('[data-testid="flex-container"]');
        const flexDisplay = await flexContainer.evaluate(el => 
          getComputedStyle(el).display
        );
        expect(flexDisplay).toBe('flex');
        
        // Test CSS Custom Properties
        const customProperty = await page.evaluate(() => 
          getComputedStyle(document.documentElement)
            .getPropertyValue('--dsr-philippine-government-primary-500')
        );
        expect(customProperty.trim()).toBeTruthy();
      });

      test('should handle print styles', async ({ page }) => {
        await page.goto('/dashboard');
        
        // Emulate print media
        await page.emulateMedia({ media: 'print' });
        
        // Verify print-specific styles
        const printHidden = page.locator('[data-testid="print-hidden"]');
        const display = await printHidden.evaluate(el => 
          getComputedStyle(el).display
        );
        expect(display).toBe('none');
        
        // Verify print-visible elements
        const printVisible = page.locator('[data-testid="print-visible"]');
        await expect(printVisible).toBeVisible();
      });
    });
  });

  test('should maintain consistent performance across browsers', async ({ page }) => {
    await page.goto('/');
    
    // Measure page load performance
    const performanceMetrics = await page.evaluate(() => {
      const navigation = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming;
      return {
        domContentLoaded: navigation.domContentLoadedEventEnd - navigation.domContentLoadedEventStart,
        loadComplete: navigation.loadEventEnd - navigation.loadEventStart,
        firstPaint: performance.getEntriesByName('first-paint')[0]?.startTime || 0,
      };
    });
    
    // Verify performance benchmarks
    expect(performanceMetrics.domContentLoaded).toBeLessThan(2000); // < 2 seconds
    expect(performanceMetrics.loadComplete).toBeLessThan(3000); // < 3 seconds
    expect(performanceMetrics.firstPaint).toBeLessThan(1500); // < 1.5 seconds
  });
});
