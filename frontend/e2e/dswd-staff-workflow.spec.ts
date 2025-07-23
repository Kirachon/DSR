import { test, expect } from '@playwright/test';

/**
 * DSWD Staff User Journey E2E Test
 * Tests the complete DSWD staff workflow for case management and administration
 */
test.describe('DSWD Staff User Journey', () => {
  test.use({ storageState: './e2e/fixtures/storage-states/dswd-staff.json' });

  test.beforeEach(async ({ page }) => {
    // Verify DSWD staff theme is applied
    await page.goto('/dashboard');
    await expect(page.locator('[data-theme="dswd-staff"]')).toBeVisible();
  });

  test('should access DSWD staff dashboard', async ({ page }) => {
    await page.goto('/dashboard');
    
    // Verify DSWD staff specific elements
    await expect(page.locator('[data-testid="dswd-staff-dashboard"]')).toBeVisible();
    await expect(page.locator('[data-testid="case-management-section"]')).toBeVisible();
    await expect(page.locator('[data-testid="analytics-section"]')).toBeVisible();
    await expect(page.locator('[data-testid="admin-tools-section"]')).toBeVisible();
    
    // Verify theme colors
    const primaryButton = page.locator('[data-testid="primary-action-button"]').first();
    await expect(primaryButton).toHaveCSS('background-color', /rgb\(59, 130, 246\)/); // Philippine government primary
  });

  test('should manage citizen applications', async ({ page }) => {
    await page.goto('/staff/applications');
    
    // Verify applications list
    await expect(page.locator('[data-testid="applications-table"]')).toBeVisible();
    await expect(page.locator('[data-testid="application-row"]')).toHaveCount(5);
    
    // Filter applications
    await page.selectOption('[data-testid="status-filter"]', 'pending');
    await expect(page.locator('[data-testid="application-row"]')).toHaveCount(3);
    
    // View application details
    await page.click('[data-testid="application-row"]:first-child [data-testid="view-button"]');
    await expect(page.locator('[data-testid="application-details"]')).toBeVisible();
    
    // Update application status
    await page.selectOption('[data-testid="status-select"]', 'approved');
    await page.fill('[data-testid="review-notes"]', 'Application meets all requirements');
    await page.click('[data-testid="update-status-button"]');
    
    // Verify success message
    await expect(page.locator('[data-testid="status-update-success"]')).toBeVisible();
  });

  test('should process payment batches', async ({ page }) => {
    await page.goto('/staff/payments');
    
    // Create new payment batch
    await page.click('[data-testid="create-batch-button"]');
    await page.fill('[data-testid="batch-name"]', 'Educational Assistance Batch - January 2024');
    await page.selectOption('[data-testid="program-select"]', 'educational-assistance');
    
    // Add beneficiaries to batch
    await page.click('[data-testid="add-beneficiaries-button"]');
    await page.check('[data-testid="beneficiary-checkbox"]:nth-child(1)');
    await page.check('[data-testid="beneficiary-checkbox"]:nth-child(2)');
    await page.check('[data-testid="beneficiary-checkbox"]:nth-child(3)');
    await page.click('[data-testid="add-selected-button"]');
    
    // Review and submit batch
    await expect(page.locator('[data-testid="batch-summary"]')).toContainText('3 beneficiaries');
    await expect(page.locator('[data-testid="total-amount"]')).toContainText('₱15,000.00');
    
    await page.click('[data-testid="submit-batch-button"]');
    await expect(page.locator('[data-testid="batch-submitted-success"]')).toBeVisible();
  });

  test('should generate analytics reports', async ({ page }) => {
    await page.goto('/staff/analytics');
    
    // Verify dashboard metrics
    await expect(page.locator('[data-testid="total-applications-metric"]')).toBeVisible();
    await expect(page.locator('[data-testid="approved-applications-metric"]')).toBeVisible();
    await expect(page.locator('[data-testid="total-disbursements-metric"]')).toBeVisible();
    
    // Generate custom report
    await page.click('[data-testid="generate-report-button"]');
    await page.selectOption('[data-testid="report-type"]', 'monthly-summary');
    await page.fill('[data-testid="date-from"]', '2024-01-01');
    await page.fill('[data-testid="date-to"]', '2024-01-31');
    await page.click('[data-testid="generate-button"]');
    
    // Verify report generation
    await expect(page.locator('[data-testid="report-generating"]')).toBeVisible();
    await expect(page.locator('[data-testid="report-ready"]')).toBeVisible({ timeout: 30000 });
    
    // Download report
    const downloadPromise = page.waitForEvent('download');
    await page.click('[data-testid="download-report-button"]');
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toContain('monthly-summary');
  });

  test('should manage user accounts', async ({ page }) => {
    await page.goto('/staff/users');
    
    // Verify user management interface
    await expect(page.locator('[data-testid="users-table"]')).toBeVisible();
    
    // Create new user account
    await page.click('[data-testid="create-user-button"]');
    await page.fill('[data-testid="user-email"]', 'newstaff@dsr.gov.ph');
    await page.fill('[data-testid="user-name"]', 'New Staff Member');
    await page.selectOption('[data-testid="user-role"]', 'DSWD_STAFF');
    await page.selectOption('[data-testid="user-region"]', 'NCR');
    
    await page.click('[data-testid="create-user-submit"]');
    await expect(page.locator('[data-testid="user-created-success"]')).toBeVisible();
    
    // Verify new user appears in list
    await expect(page.locator('[data-testid="user-row"]')).toContainText('newstaff@dsr.gov.ph');
  });

  test('should handle grievance cases', async ({ page }) => {
    await page.goto('/staff/grievances');
    
    // Verify grievance management interface
    await expect(page.locator('[data-testid="grievances-table"]')).toBeVisible();
    
    // View grievance details
    await page.click('[data-testid="grievance-row"]:first-child [data-testid="view-button"]');
    await expect(page.locator('[data-testid="grievance-details"]')).toBeVisible();
    
    // Add response to grievance
    await page.fill('[data-testid="response-text"]', 'We have reviewed your concern and will process your application within 5 business days.');
    await page.selectOption('[data-testid="response-status"]', 'in-progress');
    await page.click('[data-testid="submit-response-button"]');
    
    // Verify response added
    await expect(page.locator('[data-testid="response-added-success"]')).toBeVisible();
    await expect(page.locator('[data-testid="grievance-timeline"]')).toContainText('Response added');
  });

  test('should be responsive on mobile devices', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    await page.goto('/dashboard');
    
    // Verify mobile navigation
    await expect(page.locator('[data-testid="mobile-menu-button"]')).toBeVisible();
    await page.click('[data-testid="mobile-menu-button"]');
    await expect(page.locator('[data-testid="mobile-nav"]')).toBeVisible();
    
    // Test mobile-specific DSWD staff features
    await page.click('[data-testid="applications-nav-mobile"]');
    await expect(page.locator('[data-testid="applications-mobile-view"]')).toBeVisible();
    
    // Verify touch-friendly elements
    const actionButtons = page.locator('[data-testid="action-button"]');
    await expect(actionButtons.first()).toHaveCSS('min-height', '44px');
  });

  test('should maintain accessibility standards', async ({ page }) => {
    await page.goto('/dashboard');
    
    // Test keyboard navigation
    await page.keyboard.press('Tab');
    await expect(page.locator(':focus')).toBeVisible();
    
    // Test screen reader compatibility
    const mainContent = page.locator('main');
    await expect(mainContent).toHaveAttribute('role', 'main');
    
    // Verify ARIA labels
    const navigationMenu = page.locator('[data-testid="main-nav"]');
    await expect(navigationMenu).toHaveAttribute('aria-label');
    
    // Test high contrast mode
    await page.emulateMedia({ colorScheme: 'dark', reducedMotion: 'reduce' });
    await expect(page.locator('[data-testid="dswd-staff-dashboard"]')).toBeVisible();
  });

  test('should handle error scenarios gracefully', async ({ page }) => {
    // Test network error handling
    await page.route('/api/v1/applications', route => route.abort());
    await page.goto('/staff/applications');
    
    await expect(page.locator('[data-testid="error-message"]')).toBeVisible();
    await expect(page.locator('[data-testid="retry-button"]')).toBeVisible();
    
    // Test retry functionality
    await page.unroute('/api/v1/applications');
    await page.click('[data-testid="retry-button"]');
    await expect(page.locator('[data-testid="applications-table"]')).toBeVisible();
  });
});
