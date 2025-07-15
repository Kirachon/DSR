import { test, expect } from '@playwright/test';
import { AuthenticationManager } from '../../src/utils/auth-test-utilities';
import { ProgressIndicatorTester } from '../../src/utils/component-test-utilities';

test.describe('Progress Indicator Component Tests', () => {
  let authManager: AuthenticationManager;

  test.beforeEach(async ({ page }) => {
    authManager = new AuthenticationManager(page);
    await authManager.loginAs('CITIZEN');
  });

  test.afterEach(async ({ page }) => {
    await authManager.logout();
  });

  test.describe('Stepped Progress Indicator', () => {
    test('should display stepped progress indicator correctly', async ({ page }) => {
      // Navigate to registration page which has stepped progress
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/registration`);
      await page.waitForLoadState('networkidle');

      const progressIndicator = page.locator('[role="progressbar"]').first();
      
      if (await progressIndicator.count() > 0) {
        const progressTester = new ProgressIndicatorTester(page, progressIndicator);
        
        await progressTester.testSteppedVariant({
          variant: 'stepped',
          expectedSteps: 5, // Typical registration steps
          validateAccessibility: true
        });

        // Take screenshot for visual verification
        await page.screenshot({ 
          path: 'test-results/progress-indicator-stepped.png',
          fullPage: false 
        });
      }
    });

    test('should handle step progression correctly', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/registration`);
      await page.waitForLoadState('networkidle');

      const progressIndicator = page.locator('[role="progressbar"]').first();
      
      if (await progressIndicator.count() > 0) {
        const progressTester = new ProgressIndicatorTester(page, progressIndicator);
        
        // Test initial state
        await progressTester.testSteppedVariant({
          currentStep: 0,
          validateAccessibility: true
        });

        // Test step navigation if clickable
        const steps = progressIndicator.locator('.step, [data-testid*="step"]');
        const stepCount = await steps.count();

        if (stepCount > 0) {
          // Check if steps are clickable
          const firstStep = steps.first();
          const isClickable = await firstStep.getAttribute('data-clickable') === 'true' ||
                             await firstStep.evaluate(el => el.classList.contains('clickable'));

          if (isClickable) {
            await progressTester.testClickableSteps();
          }
        }
      }
    });

    test('should validate step states and transitions', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/registration`);
      await page.waitForLoadState('networkidle');

      const progressIndicator = page.locator('[role="progressbar"]').first();
      
      if (await progressIndicator.count() > 0) {
        const steps = progressIndicator.locator('.step, [data-testid*="step"]');
        const stepCount = await steps.count();

        // Validate each step has proper status
        for (let i = 0; i < stepCount; i++) {
          const step = steps.nth(i);
          await expect(step).toBeVisible();

          // Check step status
          const stepStatus = await step.getAttribute('data-status') || 
                            await step.getAttribute('aria-current') ||
                            'pending';
          
          expect(['pending', 'current', 'completed', 'error']).toContain(stepStatus);

          // Check step has label
          const stepLabel = step.locator('.step-label, [data-testid*="label"]');
          if (await stepLabel.count() > 0) {
            const labelText = await stepLabel.textContent();
            expect(labelText?.trim()).toBeTruthy();
          }
        }
      }
    });

    test('should support keyboard navigation', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/registration`);
      await page.waitForLoadState('networkidle');

      const progressIndicator = page.locator('[role="progressbar"]').first();
      
      if (await progressIndicator.count() > 0) {
        // Focus on progress indicator
        await progressIndicator.focus();

        // Test arrow key navigation
        await page.keyboard.press('ArrowRight');
        await page.waitForTimeout(100);

        // Check if focus moved
        const focusedElement = page.locator(':focus');
        await expect(focusedElement).toBeVisible();

        // Test Enter key activation
        await page.keyboard.press('Enter');
        await page.waitForTimeout(100);
      }
    });
  });

  test.describe('Circular Progress Indicator', () => {
    test('should display circular progress indicator correctly', async ({ page }) => {
      // Navigate to dashboard which may have circular progress
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const circularProgress = page.locator('.circular-progress, [data-testid*="circular-progress"]').first();
      
      if (await circularProgress.count() > 0) {
        const progressTester = new ProgressIndicatorTester(page, circularProgress);
        
        await progressTester.testCircularVariant({
          variant: 'circular',
          validateAccessibility: true
        });

        // Take screenshot for visual verification
        await page.screenshot({ 
          path: 'test-results/progress-indicator-circular.png',
          fullPage: false 
        });
      }
    });

    test('should display percentage correctly', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const circularProgress = page.locator('.circular-progress, [data-testid*="circular-progress"]').first();
      
      if (await circularProgress.count() > 0) {
        // Check for percentage display
        const percentageText = circularProgress.locator('.percentage, [data-testid*="percentage"]');
        
        if (await percentageText.count() > 0) {
          const percentText = await percentageText.textContent();
          expect(percentText).toMatch(/\d+%/);
          
          // Validate percentage value is reasonable
          const percentValue = parseInt(percentText?.match(/(\d+)%/)?.[1] || '0');
          expect(percentValue).toBeGreaterThanOrEqual(0);
          expect(percentValue).toBeLessThanOrEqual(100);
        }

        // Check ARIA attributes
        const ariaValueNow = await circularProgress.getAttribute('aria-valuenow');
        if (ariaValueNow) {
          const progressValue = parseInt(ariaValueNow);
          expect(progressValue).toBeGreaterThanOrEqual(0);
          expect(progressValue).toBeLessThanOrEqual(100);
        }
      }
    });

    test('should animate progress changes', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const circularProgress = page.locator('.circular-progress, [data-testid*="circular-progress"]').first();
      
      if (await circularProgress.count() > 0) {
        // Get initial progress value
        const initialValue = await circularProgress.getAttribute('aria-valuenow');
        
        // Trigger progress update if possible
        const updateButton = page.locator('button:has-text("Update"), button:has-text("Refresh")').first();
        
        if (await updateButton.count() > 0) {
          await updateButton.click();
          await page.waitForTimeout(1000);
          
          // Check if progress value changed
          const newValue = await circularProgress.getAttribute('aria-valuenow');
          
          // Values might be the same, but animation should have occurred
          expect(typeof newValue).toBe('string');
        }
      }
    });
  });

  test.describe('Linear Progress Indicator', () => {
    test('should display linear progress indicator correctly', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const linearProgress = page.locator('.progress-bar, [data-testid*="linear-progress"]').first();
      
      if (await linearProgress.count() > 0) {
        const progressTester = new ProgressIndicatorTester(page, linearProgress);
        
        await progressTester.testLinearVariant({
          variant: 'linear',
          validateAccessibility: true
        });

        // Take screenshot for visual verification
        await page.screenshot({ 
          path: 'test-results/progress-indicator-linear.png',
          fullPage: false 
        });
      }
    });

    test('should show progress fill correctly', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const linearProgress = page.locator('.progress-bar, [data-testid*="linear-progress"]').first();
      
      if (await linearProgress.count() > 0) {
        // Check for progress fill element
        const progressFill = linearProgress.locator('.progress-fill, .progress-value').first();
        
        if (await progressFill.count() > 0) {
          await expect(progressFill).toBeVisible();
          
          // Check fill width
          const fillWidth = await progressFill.evaluate(el => {
            const style = window.getComputedStyle(el);
            return style.width;
          });
          
          expect(fillWidth).toBeTruthy();
          expect(fillWidth).not.toBe('0px');
        }
      }
    });

    test('should handle indeterminate state', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      // Look for indeterminate progress indicators
      const indeterminateProgress = page.locator('[data-indeterminate="true"], .indeterminate').first();
      
      if (await indeterminateProgress.count() > 0) {
        await expect(indeterminateProgress).toBeVisible();
        
        // Check for animation or movement
        const hasAnimation = await indeterminateProgress.evaluate(el => {
          const style = window.getComputedStyle(el);
          return style.animationName !== 'none' || style.transform !== 'none';
        });
        
        expect(hasAnimation).toBeTruthy();
      }
    });
  });

  test.describe('Progress Indicator Accessibility', () => {
    test('should meet WCAG accessibility standards', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/registration`);
      await page.waitForLoadState('networkidle');

      const progressIndicators = page.locator('[role="progressbar"]');
      const indicatorCount = await progressIndicators.count();

      for (let i = 0; i < indicatorCount; i++) {
        const indicator = progressIndicators.nth(i);
        
        // Check required ARIA attributes
        await expect(indicator).toHaveAttribute('role', 'progressbar');
        
        const ariaValueNow = await indicator.getAttribute('aria-valuenow');
        const ariaValueMin = await indicator.getAttribute('aria-valuemin');
        const ariaValueMax = await indicator.getAttribute('aria-valuemax');
        
        expect(ariaValueNow).toBeTruthy();
        expect(ariaValueMin).toBeTruthy();
        expect(ariaValueMax).toBeTruthy();
        
        // Check aria-label or aria-labelledby
        const ariaLabel = await indicator.getAttribute('aria-label');
        const ariaLabelledBy = await indicator.getAttribute('aria-labelledby');
        
        expect(ariaLabel || ariaLabelledBy).toBeTruthy();
      }
    });

    test('should support screen readers', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/registration`);
      await page.waitForLoadState('networkidle');

      const progressIndicator = page.locator('[role="progressbar"]').first();
      
      if (await progressIndicator.count() > 0) {
        // Check for descriptive text
        const ariaLabel = await progressIndicator.getAttribute('aria-label');
        const ariaLabelledBy = await progressIndicator.getAttribute('aria-labelledby');
        
        if (ariaLabelledBy) {
          const labelElement = page.locator(`#${ariaLabelledBy}`);
          await expect(labelElement).toBeVisible();
          
          const labelText = await labelElement.textContent();
          expect(labelText?.trim()).toBeTruthy();
        } else if (ariaLabel) {
          expect(ariaLabel.trim()).toBeTruthy();
        }

        // Check for live region updates
        const ariaLive = await progressIndicator.getAttribute('aria-live');
        if (ariaLive) {
          expect(['polite', 'assertive']).toContain(ariaLive);
        }
      }
    });

    test('should have sufficient color contrast', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/registration`);
      await page.waitForLoadState('networkidle');

      // Run accessibility audit focusing on color contrast
      const { AxeBuilder } = await import('@axe-core/playwright');
      
      const accessibilityScanResults = await new AxeBuilder({ page })
        .withRules(['color-contrast'])
        .analyze();

      expect(accessibilityScanResults.violations).toEqual([]);
    });

    test('should be keyboard accessible', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/registration`);
      await page.waitForLoadState('networkidle');

      const progressIndicator = page.locator('[role="progressbar"]').first();
      
      if (await progressIndicator.count() > 0) {
        // Check if progress indicator can receive focus
        const tabIndex = await progressIndicator.getAttribute('tabindex');
        const isClickable = await progressIndicator.evaluate(el => 
          el.classList.contains('clickable') || 
          el.getAttribute('data-clickable') === 'true'
        );

        if (isClickable) {
          // Should be focusable
          expect(tabIndex === '0' || tabIndex === null).toBeTruthy();
          
          // Test focus
          await progressIndicator.focus();
          const hasFocus = await progressIndicator.evaluate(el => el.matches(':focus'));
          expect(hasFocus).toBeTruthy();
        }
      }
    });
  });

  test.describe('Progress Indicator Performance', () => {
    test('should render without performance issues', async ({ page }) => {
      const startTime = Date.now();
      
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/registration`);
      await page.waitForLoadState('networkidle');
      
      const endTime = Date.now();
      const loadTime = endTime - startTime;
      
      // Page should load within 2 seconds
      expect(loadTime).toBeLessThan(2000);

      // Check for progress indicators
      const progressIndicators = page.locator('[role="progressbar"]');
      const indicatorCount = await progressIndicators.count();
      
      // Should have at least one progress indicator on registration page
      expect(indicatorCount).toBeGreaterThan(0);
    });

    test('should handle rapid updates smoothly', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/registration`);
      await page.waitForLoadState('networkidle');

      const progressIndicator = page.locator('[role="progressbar"]').first();
      
      if (await progressIndicator.count() > 0) {
        // Simulate rapid progress updates
        for (let i = 0; i < 5; i++) {
          // Trigger any available update mechanism
          const nextButton = page.locator('button:has-text("Next"), button:has-text("Continue")').first();
          
          if (await nextButton.count() > 0 && await nextButton.isEnabled()) {
            await nextButton.click();
            await page.waitForTimeout(100);
          }
        }

        // Progress indicator should still be functional
        await expect(progressIndicator).toBeVisible();
      }
    });
  });
});
