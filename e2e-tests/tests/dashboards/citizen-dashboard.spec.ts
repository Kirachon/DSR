import { test, expect } from '@playwright/test';
import { CitizenDashboardPage } from '../../src/pages/dashboards/citizen-dashboard-page';
import { AuthenticationManager } from '../../src/utils/auth-test-utilities';
import { ProgressIndicatorTester } from '../../src/utils/component-test-utilities';

test.describe('Citizen Dashboard Tests', () => {
  let citizenDashboard: CitizenDashboardPage;
  let authManager: AuthenticationManager;

  test.beforeEach(async ({ page }) => {
    citizenDashboard = new CitizenDashboardPage(page);
    authManager = new AuthenticationManager(page);
    
    // Login as citizen user
    await authManager.loginAs('CITIZEN');
    await citizenDashboard.goto();
  });

  test.afterEach(async ({ page }) => {
    await authManager.logout();
  });

  test.describe('Dashboard Loading and Structure', () => {
    test('should load citizen dashboard correctly', async () => {
      await citizenDashboard.validateDashboardLoaded();
      
      // Take screenshot for visual verification
      await citizenDashboard.takeScreenshot('citizen-dashboard-loaded');
    });

    test('should display welcome message with correct user information', async () => {
      await citizenDashboard.validateWelcomeMessage('Juan Dela Cruz');
    });

    test('should show profile completeness indicator', async () => {
      const completeness = await citizenDashboard.validateProfileCompleteness();
      expect(completeness).toBeGreaterThanOrEqual(0);
      expect(completeness).toBeLessThanOrEqual(100);
    });

    test('should display all required dashboard sections', async () => {
      await citizenDashboard.validateQuickActions();
      await citizenDashboard.validateApplicationStatus();
      await citizenDashboard.validateBenefitCards();
      await citizenDashboard.validateDashboardWidgets();
    });
  });

  test.describe('Quick Actions Functionality', () => {
    test('should have all quick action buttons visible and functional', async () => {
      await citizenDashboard.validateQuickActions();
    });

    test('should navigate to registration when start registration is clicked', async ({ page }) => {
      await citizenDashboard.clickStartRegistration();
      
      // Verify navigation to registration page
      await expect(page).toHaveURL(/.*\/registration/);
    });

    test('should navigate to eligibility check when check eligibility is clicked', async ({ page }) => {
      await citizenDashboard.clickCheckEligibility();
      
      // Verify navigation to eligibility page
      await expect(page).toHaveURL(/.*\/eligibility/);
    });

    test('should navigate to payments when view payments is clicked', async ({ page }) => {
      await citizenDashboard.clickViewPayments();
      
      // Verify navigation to payments page
      await expect(page).toHaveURL(/.*\/payments/);
    });

    test('should navigate to grievance form when submit grievance is clicked', async ({ page }) => {
      await citizenDashboard.clickSubmitGrievance();
      
      // Verify navigation to grievance page
      await expect(page).toHaveURL(/.*\/grievance|.*\/cases/);
    });

    test('should navigate to profile when update profile is clicked', async ({ page }) => {
      await citizenDashboard.clickUpdateProfile();
      
      // Verify navigation to profile page
      await expect(page).toHaveURL(/.*\/profile/);
    });
  });

  test.describe('Application Status and Progress', () => {
    test('should display application status with proper status badges', async () => {
      await citizenDashboard.validateApplicationStatus();
      
      // Validate status badges using component utilities
      await citizenDashboard.validateStatusBadges();
    });

    test('should show registration progress if incomplete', async ({ page }) => {
      await citizenDashboard.validateRegistrationProgress();
      
      // Test progress indicator functionality
      const progressIndicator = page.locator('[data-testid="registration-progress"] [role="progressbar"]');
      if (await progressIndicator.count() > 0) {
        const progressTester = new ProgressIndicatorTester(page, progressIndicator.first());
        await progressTester.testSteppedVariant({
          validateAccessibility: true
        });
      }
    });

    test('should display eligibility status correctly', async () => {
      await citizenDashboard.validateEligibilityStatus();
    });

    test('should show journey timeline if available', async () => {
      await citizenDashboard.validateJourneyTimeline();
    });
  });

  test.describe('Benefit Cards and Information', () => {
    test('should display benefit cards with proper information', async () => {
      await citizenDashboard.validateBenefitCards();
    });

    test('should show payment history if available', async () => {
      await citizenDashboard.validatePaymentHistory();
    });

    test('should display notifications panel', async () => {
      await citizenDashboard.validateNotificationPanel();
    });
  });

  test.describe('Responsive Design', () => {
    test('should display correctly on mobile devices', async ({ page }) => {
      await page.setViewportSize({ width: 320, height: 568 });
      await citizenDashboard.waitForPageLoad();
      
      // Verify dashboard is still functional on mobile
      await citizenDashboard.validateDashboardLoaded();
      await citizenDashboard.validateQuickActions();
      
      // Take mobile screenshot
      await citizenDashboard.takeScreenshot('citizen-dashboard-mobile');
    });

    test('should display correctly on tablet devices', async ({ page }) => {
      await page.setViewportSize({ width: 768, height: 1024 });
      await citizenDashboard.waitForPageLoad();
      
      // Verify dashboard layout on tablet
      await citizenDashboard.validateDashboardLoaded();
      await citizenDashboard.validateDashboardWidgets();
      
      // Take tablet screenshot
      await citizenDashboard.takeScreenshot('citizen-dashboard-tablet');
    });

    test('should display correctly on desktop', async ({ page }) => {
      await page.setViewportSize({ width: 1920, height: 1080 });
      await citizenDashboard.waitForPageLoad();
      
      // Verify full desktop layout
      await citizenDashboard.validateDashboardLoaded();
      await citizenDashboard.validateDashboardWidgets();
      await citizenDashboard.validateBenefitCards();
      
      // Take desktop screenshot
      await citizenDashboard.takeScreenshot('citizen-dashboard-desktop');
    });

    test('should test responsive design across all viewports', async () => {
      await citizenDashboard.testResponsiveDesign();
    });
  });

  test.describe('Performance and Loading', () => {
    test('should load dashboard within performance threshold', async () => {
      await citizenDashboard.validateResponseTime(2000);
    });

    test('should handle loading states gracefully', async ({ page }) => {
      // Reload page and check loading states
      await page.reload();
      
      // Wait for loading to complete
      await citizenDashboard.waitForPageLoad();
      
      // Verify dashboard loaded successfully
      await citizenDashboard.validateDashboardLoaded();
    });

    test('should measure and validate performance metrics', async () => {
      const metrics = await citizenDashboard.measurePerformance();
      
      // Validate performance thresholds
      expect(metrics.loadTime).toBeLessThan(2000);
      expect(metrics.domContentLoaded).toBeLessThan(1500);
      expect(metrics.firstContentfulPaint).toBeLessThan(1000);
    });
  });

  test.describe('Accessibility Compliance', () => {
    test('should meet WCAG AA accessibility standards', async () => {
      await citizenDashboard.validateAccessibility();
    });

    test('should support keyboard navigation', async () => {
      await citizenDashboard.testKeyboardNavigation();
    });

    test('should have proper ARIA labels and roles', async ({ page }) => {
      // Check for proper ARIA attributes on interactive elements
      const interactiveElements = page.locator('button, a, input, select, [role="button"]');
      const elementCount = await interactiveElements.count();
      
      for (let i = 0; i < elementCount; i++) {
        const element = interactiveElements.nth(i);
        
        if (await element.isVisible()) {
          const ariaLabel = await element.getAttribute('aria-label');
          const ariaLabelledBy = await element.getAttribute('aria-labelledby');
          const textContent = await element.textContent();
          
          // Element should have accessible name
          expect(ariaLabel || ariaLabelledBy || textContent?.trim()).toBeTruthy();
        }
      }
    });

    test('should have sufficient color contrast', async ({ page }) => {
      // Run color contrast checks
      await citizenDashboard.runAccessibilityTests({
        rules: {
          'color-contrast': { enabled: true }
        }
      });
    });
  });

  test.describe('Error Handling', () => {
    test('should handle API errors gracefully', async ({ page }) => {
      // Mock API failure
      await page.route('**/api/**', route => {
        route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({ error: 'Internal Server Error' })
        });
      });
      
      // Reload page to trigger API calls
      await page.reload();
      await citizenDashboard.waitForPageLoad();
      
      // Verify error handling
      await citizenDashboard.testErrorHandling();
    });

    test('should display user-friendly error messages', async ({ page }) => {
      // Test error message display
      await citizenDashboard.testErrorHandling();
    });

    test('should provide recovery options for errors', async ({ page }) => {
      // Mock network error
      await page.route('**/api/dashboard/**', route => {
        route.abort('failed');
      });
      
      await page.reload();
      await citizenDashboard.waitForPageLoad();
      
      // Check for retry mechanisms or fallback content
      const retryButton = page.locator('button:has-text("Retry"), button:has-text("Try Again")');
      if (await retryButton.count() > 0) {
        await expect(retryButton.first()).toBeVisible();
      }
    });
  });

  test.describe('Data Integration', () => {
    test('should display real-time data updates', async ({ page }) => {
      // Verify dashboard displays current data
      await citizenDashboard.validateDashboardLoaded();
      
      // Check for data freshness indicators
      const lastUpdated = page.locator('[data-testid*="last-updated"], .last-updated');
      if (await lastUpdated.count() > 0) {
        const updateText = await lastUpdated.first().textContent();
        expect(updateText?.trim()).toBeTruthy();
      }
    });

    test('should handle empty states appropriately', async ({ page }) => {
      // Check for empty state handling
      const emptyStates = page.locator('.empty-state, [data-testid*="empty"]');
      const emptyCount = await emptyStates.count();
      
      for (let i = 0; i < emptyCount; i++) {
        const emptyState = emptyStates.nth(i);
        if (await emptyState.isVisible()) {
          const emptyText = await emptyState.textContent();
          expect(emptyText?.trim()).toBeTruthy();
        }
      }
    });
  });
});
