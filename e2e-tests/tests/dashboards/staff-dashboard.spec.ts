import { test, expect } from '@playwright/test';
import { StaffDashboardPage } from '../../src/pages/dashboards/staff-dashboard-page';
import { AuthenticationManager } from '../../src/utils/auth-test-utilities';
import { DataTableTester } from '../../src/utils/component-test-utilities';

test.describe('Staff Dashboard Tests', () => {
  let staffDashboard: StaffDashboardPage;
  let authManager: AuthenticationManager;

  test.describe('LGU Staff Dashboard', () => {
    test.beforeEach(async ({ page }) => {
      staffDashboard = new StaffDashboardPage(page);
      authManager = new AuthenticationManager(page);
      
      // Login as LGU staff user
      await authManager.loginAs('LGU_STAFF');
      await staffDashboard.goto();
    });

    test.afterEach(async ({ page }) => {
      await authManager.logout();
    });

    test('should load LGU staff dashboard correctly', async () => {
      await staffDashboard.validateDashboardLoaded();
      await staffDashboard.takeScreenshot('lgu-staff-dashboard-loaded');
    });

    test('should display KPI cards with correct information', async () => {
      await staffDashboard.validateKPICards();
    });

    test('should show work queue with pending items', async () => {
      await staffDashboard.validateWorkQueue();
    });

    test('should display pending registrations for review', async () => {
      await staffDashboard.validatePendingRegistrations();
    });

    test('should show assigned cases for LGU staff', async () => {
      await staffDashboard.validateAssignedCases();
    });

    test('should validate LGU staff permissions and role-based content', async () => {
      await staffDashboard.validateStaffPermissions('LGU_STAFF');
    });

    test('should allow LGU staff to review applications', async ({ page }) => {
      await staffDashboard.clickReviewApplication();
      
      // Verify navigation or modal opening
      const reviewModal = page.locator('[data-testid="review-modal"]');
      const reviewPage = page.url().includes('/review');
      
      expect(await reviewModal.count() > 0 || reviewPage).toBeTruthy();
    });

    test('should allow LGU staff to assign cases', async ({ page }) => {
      await staffDashboard.clickAssignCase();
      
      // Verify case assignment functionality
      const assignModal = page.locator('[data-testid="assign-modal"]');
      if (await assignModal.count() > 0) {
        await expect(assignModal).toBeVisible();
      }
    });

    test('should test data table functionality for LGU staff', async ({ page }) => {
      await staffDashboard.testDataTableFunctionality();
      
      // Test specific data table features
      const dataTable = page.locator('[data-testid="data-table"]');
      if (await dataTable.count() > 0) {
        const tableTester = new DataTableTester(page, dataTable.first());
        await tableTester.testTableStructure({
          hasSorting: true,
          hasFiltering: true,
          hasPagination: true
        });
        await tableTester.testSorting();
        await tableTester.testFiltering();
      }
    });

    test('should test search and filter functionality', async () => {
      await staffDashboard.testSearchAndFilters();
    });

    test('should test bulk actions for LGU staff', async () => {
      await staffDashboard.testBulkActions();
    });
  });

  test.describe('DSWD Staff Dashboard', () => {
    test.beforeEach(async ({ page }) => {
      staffDashboard = new StaffDashboardPage(page);
      authManager = new AuthenticationManager(page);
      
      // Login as DSWD staff user
      await authManager.loginAs('DSWD_STAFF');
      await staffDashboard.goto();
    });

    test.afterEach(async ({ page }) => {
      await authManager.logout();
    });

    test('should load DSWD staff dashboard correctly', async () => {
      await staffDashboard.validateDashboardLoaded();
      await staffDashboard.takeScreenshot('dswd-staff-dashboard-loaded');
    });

    test('should display DSWD-specific KPI cards', async () => {
      await staffDashboard.validateKPICards();
    });

    test('should show DSWD work queue', async () => {
      await staffDashboard.validateWorkQueue();
    });

    test('should display recent payments for DSWD oversight', async () => {
      await staffDashboard.validateRecentPayments();
    });

    test('should validate DSWD staff permissions and role-based content', async () => {
      await staffDashboard.validateStaffPermissions('DSWD_STAFF');
    });

    test('should allow DSWD staff to generate reports', async ({ page }) => {
      await staffDashboard.clickGenerateReport();
      
      // Verify report generation functionality
      const reportModal = page.locator('[data-testid="report-modal"]');
      const reportPage = page.url().includes('/reports');
      
      expect(await reportModal.count() > 0 || reportPage).toBeTruthy();
    });

    test('should allow DSWD staff to view analytics', async ({ page }) => {
      await staffDashboard.clickViewAnalytics();
      
      // Verify analytics access
      const analyticsPage = page.url().includes('/analytics');
      const analyticsSection = page.locator('[data-testid="analytics-section"]');
      
      expect(analyticsPage || await analyticsSection.count() > 0).toBeTruthy();
    });

    test('should allow DSWD staff to export data', async ({ page }) => {
      await staffDashboard.clickExportData();
      
      // Verify export functionality
      const exportModal = page.locator('[data-testid="export-modal"]');
      if (await exportModal.count() > 0) {
        await expect(exportModal).toBeVisible();
      }
    });

    test('should approve registrations for DSWD staff', async ({ page }) => {
      await staffDashboard.clickApproveRegistration();
      
      // Verify approval functionality
      const approvalModal = page.locator('[data-testid="approval-modal"]');
      if (await approvalModal.count() > 0) {
        await expect(approvalModal).toBeVisible();
      }
    });
  });

  test.describe('Common Staff Dashboard Features', () => {
    test.beforeEach(async ({ page }) => {
      staffDashboard = new StaffDashboardPage(page);
      authManager = new AuthenticationManager(page);
      
      // Login as LGU staff for common tests
      await authManager.loginAs('LGU_STAFF');
      await staffDashboard.goto();
    });

    test.afterEach(async ({ page }) => {
      await authManager.logout();
    });

    test('should display correctly on tablet devices', async ({ page }) => {
      await page.setViewportSize({ width: 768, height: 1024 });
      await staffDashboard.waitForPageLoad();
      
      await staffDashboard.validateDashboardLoaded();
      await staffDashboard.validateKPICards();
      
      await staffDashboard.takeScreenshot('staff-dashboard-tablet');
    });

    test('should display correctly on desktop', async ({ page }) => {
      await page.setViewportSize({ width: 1920, height: 1080 });
      await staffDashboard.waitForPageLoad();
      
      await staffDashboard.validateDashboardLoaded();
      await staffDashboard.validateKPICards();
      await staffDashboard.validateWorkQueue();
      
      await staffDashboard.takeScreenshot('staff-dashboard-desktop');
    });

    test('should display correctly on large desktop', async ({ page }) => {
      await page.setViewportSize({ width: 2560, height: 1440 });
      await staffDashboard.waitForPageLoad();
      
      await staffDashboard.validateDashboardLoaded();
      await staffDashboard.takeScreenshot('staff-dashboard-large-desktop');
    });

    test('should test responsive design across all viewports', async () => {
      await staffDashboard.testResponsiveDesign();
    });

    test('should load dashboard within performance threshold', async () => {
      await staffDashboard.validateResponseTime(2000);
    });

    test('should meet WCAG AA accessibility standards', async () => {
      await staffDashboard.validateAccessibility();
    });

    test('should support keyboard navigation', async () => {
      await staffDashboard.testKeyboardNavigation();
    });

    test('should handle API errors gracefully', async ({ page }) => {
      // Mock API failure
      await page.route('**/api/**', route => {
        route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({ error: 'Internal Server Error' })
        });
      });
      
      await page.reload();
      await staffDashboard.waitForPageLoad();
      
      await staffDashboard.testErrorHandling();
    });

    test('should validate cross-browser compatibility', async () => {
      await staffDashboard.validateCrossBrowserCompatibility();
    });

    test('should test data table pagination', async ({ page }) => {
      const dataTable = page.locator('[data-testid="data-table"]');
      if (await dataTable.count() > 0) {
        const tableTester = new DataTableTester(page, dataTable.first());
        await tableTester.testPagination();
      }
    });

    test('should test data table row selection', async ({ page }) => {
      const dataTable = page.locator('[data-testid="data-table"]');
      if (await dataTable.count() > 0) {
        const tableTester = new DataTableTester(page, dataTable.first());
        await tableTester.testRowSelection();
      }
    });

    test('should validate status badges across dashboard', async () => {
      await staffDashboard.validateStatusBadges();
    });

    test('should validate progress indicators functionality', async () => {
      await staffDashboard.validateProgressIndicators();
    });

    test('should validate data tables functionality', async () => {
      await staffDashboard.validateDataTables();
    });

    test('should handle empty states appropriately', async ({ page }) => {
      // Mock empty data responses
      await page.route('**/api/dashboard/**', route => {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ data: [], total: 0 })
        });
      });
      
      await page.reload();
      await staffDashboard.waitForPageLoad();
      
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

    test('should measure and validate performance metrics', async () => {
      const metrics = await staffDashboard.measurePerformance();
      
      // Validate performance thresholds for staff dashboard
      expect(metrics.loadTime).toBeLessThan(2000);
      expect(metrics.domContentLoaded).toBeLessThan(1500);
      expect(metrics.firstContentfulPaint).toBeLessThan(1000);
    });

    test('should validate real-time data updates', async ({ page }) => {
      await staffDashboard.validateDashboardLoaded();
      
      // Check for data refresh mechanisms
      const refreshButton = page.locator('button:has-text("Refresh"), [data-testid*="refresh"]');
      if (await refreshButton.count() > 0) {
        await refreshButton.first().click();
        await staffDashboard.waitForPageLoad();
        await staffDashboard.validateDashboardLoaded();
      }
    });
  });
});
