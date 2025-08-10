import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

/**
 * Accessibility Compliance Tests
 * Ensures WCAG 2.0 AA compliance across all user interfaces
 */
test.describe('Accessibility Compliance', () => {
  test('should have no accessibility violations on homepage', async ({ page }) => {
    await page.goto('/');
    
    const accessibilityScanResults = await new AxeBuilder({ page })
      .withTags(['wcag2a', 'wcag2aa', 'wcag21aa'])
      .analyze();

    expect(accessibilityScanResults.violations).toEqual([]);
  });

  test('should have no accessibility violations on citizen dashboard', async ({ page }) => {
    // Login as citizen
    await page.goto('/login');
    await page.fill('[data-testid="email"]', 'citizen@dsr.test');
    await page.fill('[data-testid="password"]', 'TestPassword123!');
    await page.click('[data-testid="login-button"]');
    
    await page.waitForURL(/\/dashboard/);
    
    const accessibilityScanResults = await new AxeBuilder({ page })
      .withTags(['wcag2a', 'wcag2aa'])
      .analyze();

    expect(accessibilityScanResults.violations).toEqual([]);
  });

  test('should have no accessibility violations on DSWD staff interface', async ({ page }) => {
    // Login as DSWD staff
    await page.goto('/login');
    await page.fill('[data-testid="email"]', 'staff@dsr.gov.ph');
    await page.fill('[data-testid="password"]', 'StaffPassword123!');
    await page.click('[data-testid="login-button"]');
    
    await page.waitForURL(/\/dashboard/);
    
    const accessibilityScanResults = await new AxeBuilder({ page })
      .withTags(['wcag2a', 'wcag2aa'])
      .analyze();

    expect(accessibilityScanResults.violations).toEqual([]);
  });

  test('should have no accessibility violations on LGU staff interface', async ({ page }) => {
    // Login as LGU staff
    await page.goto('/login');
    await page.fill('[data-testid="email"]', 'lgu@local.gov.ph');
    await page.fill('[data-testid="password"]', 'LguPassword123!');
    await page.click('[data-testid="login-button"]');
    
    await page.waitForURL(/\/dashboard/);
    
    const accessibilityScanResults = await new AxeBuilder({ page })
      .withTags(['wcag2a', 'wcag2aa'])
      .analyze();

    expect(accessibilityScanResults.violations).toEqual([]);
  });

  test('should support keyboard navigation', async ({ page }) => {
    await page.goto('/');
    
    // Test tab navigation through main elements
    await page.keyboard.press('Tab');
    let focusedElement = page.locator(':focus');
    await expect(focusedElement).toBeVisible();
    
    // Navigate through header elements
    for (let i = 0; i < 5; i++) {
      await page.keyboard.press('Tab');
      focusedElement = page.locator(':focus');
      await expect(focusedElement).toBeVisible();
    }
    
    // Test Enter key activation
    const currentFocus = await page.locator(':focus').getAttribute('data-testid');
    if (currentFocus && currentFocus.includes('button')) {
      await page.keyboard.press('Enter');
      // Verify action was triggered
    }
    
    // Test Escape key functionality
    await page.keyboard.press('Escape');
    
    // Test arrow key navigation in menus
    await page.keyboard.press('Tab');
    await page.keyboard.press('Enter'); // Open menu
    await page.keyboard.press('ArrowDown');
    await page.keyboard.press('ArrowDown');
    await page.keyboard.press('Enter'); // Select item
  });

  test('should have proper heading hierarchy', async ({ page }) => {
    await page.goto('/dashboard');
    
    // Check for h1 element
    const h1Elements = page.locator('h1');
    await expect(h1Elements).toHaveCount(1);
    
    // Verify heading hierarchy (h1 -> h2 -> h3, etc.)
    const headings = await page.locator('h1, h2, h3, h4, h5, h6').allTextContents();
    
    // Check that headings follow logical order
    const headingLevels = await page.locator('h1, h2, h3, h4, h5, h6').evaluateAll(elements => 
      elements.map(el => parseInt(el.tagName.charAt(1)))
    );
    
    // Verify no heading levels are skipped
    for (let i = 1; i < headingLevels.length; i++) {
      const currentLevel = headingLevels[i];
      const previousLevel = headingLevels[i - 1];
      expect(currentLevel - previousLevel).toBeLessThanOrEqual(1);
    }
  });

  test('should have proper ARIA labels and roles', async ({ page }) => {
    await page.goto('/dashboard');
    
    // Check main navigation
    const mainNav = page.locator('[role="navigation"]').first();
    await expect(mainNav).toHaveAttribute('aria-label');
    
    // Check main content area
    const mainContent = page.locator('[role="main"]');
    await expect(mainContent).toBeVisible();
    
    // Check form labels
    const formInputs = page.locator('input[type="text"], input[type="email"], input[type="password"]');
    const inputCount = await formInputs.count();
    
    for (let i = 0; i < inputCount; i++) {
      const input = formInputs.nth(i);
      const hasLabel = await input.evaluate(el => {
        const id = el.getAttribute('id');
        const ariaLabel = el.getAttribute('aria-label');
        const ariaLabelledBy = el.getAttribute('aria-labelledby');
        const label = id ? document.querySelector(`label[for="${id}"]`) : null;
        
        return !!(ariaLabel || ariaLabelledBy || label);
      });
      
      expect(hasLabel).toBe(true);
    }
    
    // Check button accessibility
    const buttons = page.locator('button');
    const buttonCount = await buttons.count();
    
    for (let i = 0; i < buttonCount; i++) {
      const button = buttons.nth(i);
      const hasAccessibleName = await button.evaluate(el => {
        const textContent = el.textContent?.trim();
        const ariaLabel = el.getAttribute('aria-label');
        const ariaLabelledBy = el.getAttribute('aria-labelledby');
        
        return !!(textContent || ariaLabel || ariaLabelledBy);
      });
      
      expect(hasAccessibleName).toBe(true);
    }
  });

  test('should have sufficient color contrast', async ({ page }) => {
    await page.goto('/');
    
    // Test with axe-core color contrast rules
    const accessibilityScanResults = await new AxeBuilder({ page })
      .withTags(['wcag2aa'])
      .include('[data-testid]')
      .analyze();

    const colorContrastViolations = accessibilityScanResults.violations.filter(
      violation => violation.id === 'color-contrast'
    );
    
    expect(colorContrastViolations).toEqual([]);
  });

  test('should support screen readers', async ({ page }) => {
    await page.goto('/dashboard');
    
    // Check for skip links
    const skipLink = page.locator('[data-testid="skip-to-main"]');
    await expect(skipLink).toBeVisible();
    
    // Test skip link functionality
    await skipLink.click();
    const mainContent = page.locator('[role="main"]');
    await expect(mainContent).toBeFocused();
    
    // Check for landmark regions
    await expect(page.locator('[role="banner"]')).toBeVisible(); // Header
    await expect(page.locator('[role="navigation"]')).toBeVisible(); // Navigation
    await expect(page.locator('[role="main"]')).toBeVisible(); // Main content
    await expect(page.locator('[role="contentinfo"]')).toBeVisible(); // Footer
    
    // Check for live regions
    const liveRegions = page.locator('[aria-live]');
    const liveRegionCount = await liveRegions.count();
    expect(liveRegionCount).toBeGreaterThan(0);
  });

  test('should handle focus management', async ({ page }) => {
    await page.goto('/');
    
    // Test modal focus management
    await page.click('[data-testid="open-modal-button"]');
    const modal = page.locator('[role="dialog"]');
    await expect(modal).toBeVisible();
    
    // Check that focus is trapped in modal
    await page.keyboard.press('Tab');
    const focusedElement = page.locator(':focus');
    const isInsideModal = await focusedElement.evaluate(el => {
      const modal = document.querySelector('[role="dialog"]');
      return modal?.contains(el) || false;
    });
    expect(isInsideModal).toBe(true);
    
    // Test escape key closes modal
    await page.keyboard.press('Escape');
    await expect(modal).not.toBeVisible();
    
    // Check that focus returns to trigger element
    const triggerButton = page.locator('[data-testid="open-modal-button"]');
    await expect(triggerButton).toBeFocused();
  });

  test('should support reduced motion preferences', async ({ page }) => {
    // Set reduced motion preference
    await page.emulateMedia({ reducedMotion: 'reduce' });
    await page.goto('/');
    
    // Check that animations are disabled or reduced
    const animatedElement = page.locator('[data-testid="animated-element"]');
    const animationDuration = await animatedElement.evaluate(el => 
      getComputedStyle(el).animationDuration
    );
    
    // Should be either 0s or very short duration
    expect(['0s', '0.01ms'].some(duration => animationDuration.includes(duration))).toBe(true);
  });

  test('should support high contrast mode', async ({ page }) => {
    // Emulate high contrast mode
    await page.emulateMedia({ colorScheme: 'dark', forcedColors: 'active' });
    await page.goto('/');
    
    // Verify elements are still visible and functional
    await expect(page.locator('[data-testid="main-navigation"]')).toBeVisible();
    await expect(page.locator('[data-testid="primary-content"]')).toBeVisible();
    
    // Test that interactive elements are still accessible
    const buttons = page.locator('button');
    const buttonCount = await buttons.count();
    
    for (let i = 0; i < Math.min(buttonCount, 5); i++) {
      await expect(buttons.nth(i)).toBeVisible();
    }
  });

  test('should have proper form validation and error messages', async ({ page }) => {
    await page.goto('/register');
    
    // Test required field validation
    await page.click('[data-testid="register-button"]');
    
    // Check that error messages are associated with form fields
    const errorMessages = page.locator('[role="alert"], [aria-live="polite"]');
    await expect(errorMessages.first()).toBeVisible();
    
    // Check that form fields have aria-invalid when in error state
    const emailField = page.locator('[data-testid="email"]');
    await expect(emailField).toHaveAttribute('aria-invalid', 'true');
    
    // Check that error messages are properly associated
    const ariaDescribedBy = await emailField.getAttribute('aria-describedby');
    if (ariaDescribedBy) {
      const errorElement = page.locator(`#${ariaDescribedBy}`);
      await expect(errorElement).toBeVisible();
    }
  });

  test('should support zoom up to 200%', async ({ page }) => {
    await page.goto('/dashboard');
    
    // Zoom to 200%
    await page.evaluate(() => {
      document.body.style.zoom = '2';
    });
    
    // Verify content is still accessible and functional
    await expect(page.locator('[data-testid="main-navigation"]')).toBeVisible();
    await expect(page.locator('[data-testid="primary-content"]')).toBeVisible();
    
    // Test that interactive elements are still clickable
    const firstButton = page.locator('button').first();
    await expect(firstButton).toBeVisible();
    await firstButton.click();
    
    // Reset zoom
    await page.evaluate(() => {
      document.body.style.zoom = '1';
    });
  });
});
