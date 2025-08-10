import { test, expect } from '@playwright/test';

/**
 * LGU Staff User Journey E2E Test
 * Tests the complete LGU staff workflow for local government integration
 */
test.describe('LGU Staff User Journey', () => {
  test.use({ storageState: './e2e/fixtures/storage-states/lgu-staff.json' });

  test.beforeEach(async ({ page }) => {
    // Verify LGU staff theme is applied
    await page.goto('/dashboard');
    await expect(page.locator('[data-theme="lgu-staff"]')).toBeVisible();
  });

  test('should access LGU staff dashboard', async ({ page }) => {
    await page.goto('/dashboard');
    
    // Verify LGU staff specific elements
    await expect(page.locator('[data-testid="lgu-staff-dashboard"]')).toBeVisible();
    await expect(page.locator('[data-testid="local-programs-section"]')).toBeVisible();
    await expect(page.locator('[data-testid="beneficiary-management-section"]')).toBeVisible();
    await expect(page.locator('[data-testid="reporting-section"]')).toBeVisible();
    
    // Verify LGU theme colors (secondary color as primary)
    const primaryButton = page.locator('[data-testid="primary-action-button"]').first();
    await expect(primaryButton).toHaveCSS('background-color', /rgb\(220, 38, 127\)/); // Philippine government secondary
  });

  test('should manage local beneficiary registration', async ({ page }) => {
    await page.goto('/lgu/beneficiaries');
    
    // Register new beneficiary
    await page.click('[data-testid="register-beneficiary-button"]');
    
    // Fill beneficiary information
    await page.fill('[data-testid="beneficiary-name"]', 'Maria Santos');
    await page.fill('[data-testid="beneficiary-address"]', '123 Barangay Street, Manila');
    await page.fill('[data-testid="beneficiary-phone"]', '09123456789');
    await page.selectOption('[data-testid="barangay-select"]', 'Barangay 1');
    
    // Add household members
    await page.click('[data-testid="add-household-member"]');
    await page.fill('[data-testid="member-name-0"]', 'Juan Santos');
    await page.selectOption('[data-testid="member-relationship-0"]', 'spouse');
    await page.fill('[data-testid="member-age-0"]', '35');
    
    // Submit registration
    await page.click('[data-testid="submit-registration"]');
    await expect(page.locator('[data-testid="registration-success"]')).toBeVisible();
    
    // Verify beneficiary appears in list
    await page.goto('/lgu/beneficiaries');
    await expect(page.locator('[data-testid="beneficiary-row"]')).toContainText('Maria Santos');
  });

  test('should validate beneficiary eligibility', async ({ page }) => {
    await page.goto('/lgu/eligibility');
    
    // Search for beneficiary
    await page.fill('[data-testid="beneficiary-search"]', 'Maria Santos');
    await page.click('[data-testid="search-button"]');
    
    // Select beneficiary for eligibility check
    await page.click('[data-testid="beneficiary-result"]:first-child');
    await expect(page.locator('[data-testid="beneficiary-details"]')).toBeVisible();
    
    // Run eligibility assessment
    await page.click('[data-testid="run-eligibility-check"]');
    await expect(page.locator('[data-testid="eligibility-processing"]')).toBeVisible();
    
    // Wait for results
    await expect(page.locator('[data-testid="eligibility-results"]')).toBeVisible({ timeout: 30000 });
    
    // Verify eligibility status
    await expect(page.locator('[data-testid="eligibility-status"]')).toContainText('Eligible');
    await expect(page.locator('[data-testid="eligible-programs"]')).toBeVisible();
    
    // Approve eligibility
    await page.click('[data-testid="approve-eligibility"]');
    await page.fill('[data-testid="approval-notes"]', 'Meets all local requirements');
    await page.click('[data-testid="confirm-approval"]');
    
    await expect(page.locator('[data-testid="eligibility-approved"]')).toBeVisible();
  });

  test('should coordinate with DSWD programs', async ({ page }) => {
    await page.goto('/lgu/coordination');
    
    // View DSWD program integration
    await expect(page.locator('[data-testid="dswd-programs-list"]')).toBeVisible();
    
    // Submit local beneficiary for DSWD program
    await page.click('[data-testid="submit-to-dswd-button"]');
    await page.selectOption('[data-testid="dswd-program-select"]', 'educational-assistance');
    
    // Select beneficiaries
    await page.check('[data-testid="beneficiary-checkbox"]:nth-child(1)');
    await page.check('[data-testid="beneficiary-checkbox"]:nth-child(2)');
    
    // Add local endorsement
    await page.fill('[data-testid="endorsement-letter"]', 'The LGU endorses these beneficiaries for the educational assistance program.');
    
    // Submit to DSWD
    await page.click('[data-testid="submit-to-dswd"]');
    await expect(page.locator('[data-testid="dswd-submission-success"]')).toBeVisible();
    
    // Track submission status
    await page.goto('/lgu/coordination/tracking');
    await expect(page.locator('[data-testid="submission-status"]')).toContainText('Submitted to DSWD');
  });

  test('should generate local reports', async ({ page }) => {
    await page.goto('/lgu/reports');
    
    // Generate beneficiary summary report
    await page.click('[data-testid="generate-beneficiary-report"]');
    await page.selectOption('[data-testid="report-period"]', 'monthly');
    await page.selectOption('[data-testid="barangay-filter"]', 'all');
    
    await page.click('[data-testid="generate-report"]');
    await expect(page.locator('[data-testid="report-generating"]')).toBeVisible();
    
    // Wait for report completion
    await expect(page.locator('[data-testid="report-ready"]')).toBeVisible({ timeout: 30000 });
    
    // Verify report contents
    await expect(page.locator('[data-testid="total-beneficiaries"]')).toBeVisible();
    await expect(page.locator('[data-testid="eligible-beneficiaries"]')).toBeVisible();
    await expect(page.locator('[data-testid="program-distribution"]')).toBeVisible();
    
    // Export report
    const downloadPromise = page.waitForEvent('download');
    await page.click('[data-testid="export-pdf"]');
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toContain('beneficiary-report');
  });

  test('should manage local program implementation', async ({ page }) => {
    await page.goto('/lgu/programs');
    
    // Create local supplementary program
    await page.click('[data-testid="create-program-button"]');
    await page.fill('[data-testid="program-name"]', 'Local Educational Support');
    await page.fill('[data-testid="program-description"]', 'Additional support for local students');
    await page.fill('[data-testid="program-budget"]', '500000');
    
    // Set eligibility criteria
    await page.click('[data-testid="add-criteria-button"]');
    await page.selectOption('[data-testid="criteria-type"]', 'income');
    await page.fill('[data-testid="criteria-value"]', '20000');
    
    // Save program
    await page.click('[data-testid="save-program"]');
    await expect(page.locator('[data-testid="program-created-success"]')).toBeVisible();
    
    // Activate program
    await page.click('[data-testid="activate-program"]');
    await expect(page.locator('[data-testid="program-activated"]')).toBeVisible();
  });

  test('should handle interoperability with other systems', async ({ page }) => {
    await page.goto('/lgu/interoperability');
    
    // Test PhilSys integration
    await page.click('[data-testid="philsys-sync-button"]');
    await expect(page.locator('[data-testid="philsys-syncing"]')).toBeVisible();
    await expect(page.locator('[data-testid="philsys-sync-complete"]')).toBeVisible({ timeout: 30000 });
    
    // Test CBMS integration
    await page.click('[data-testid="cbms-import-button"]');
    await page.setInputFiles('[data-testid="cbms-file-input"]', 'test-files/cbms-data.csv');
    await page.click('[data-testid="import-cbms-data"]');
    
    await expect(page.locator('[data-testid="cbms-import-success"]')).toBeVisible();
    await expect(page.locator('[data-testid="imported-records-count"]')).toContainText('150 records imported');
  });

  test('should be responsive on tablet devices', async ({ page }) => {
    // Set tablet viewport
    await page.setViewportSize({ width: 768, height: 1024 });
    await page.goto('/dashboard');
    
    // Verify tablet layout
    await expect(page.locator('[data-testid="tablet-layout"]')).toBeVisible();
    
    // Test tablet-specific navigation
    await page.click('[data-testid="programs-nav"]');
    await expect(page.locator('[data-testid="programs-tablet-view"]')).toBeVisible();
    
    // Verify touch-friendly elements
    const menuItems = page.locator('[data-testid="nav-item"]');
    await expect(menuItems.first()).toHaveCSS('min-height', '44px');
  });

  test('should maintain data security and privacy', async ({ page }) => {
    await page.goto('/lgu/beneficiaries');
    
    // Verify sensitive data is masked
    await expect(page.locator('[data-testid="beneficiary-id"]')).toContainText('***');
    
    // Test access control
    await page.goto('/admin/system-settings');
    await expect(page.locator('[data-testid="access-denied"]')).toBeVisible();
    
    // Verify audit logging
    await page.goto('/lgu/audit-log');
    await expect(page.locator('[data-testid="audit-entries"]')).toBeVisible();
    await expect(page.locator('[data-testid="audit-entry"]').first()).toContainText('User action logged');
  });

  test('should handle offline scenarios', async ({ page }) => {
    await page.goto('/lgu/beneficiaries');
    
    // Simulate offline mode
    await page.context().setOffline(true);
    
    // Verify offline indicator
    await expect(page.locator('[data-testid="offline-indicator"]')).toBeVisible();
    
    // Test offline functionality
    await page.click('[data-testid="register-beneficiary-button"]');
    await expect(page.locator('[data-testid="offline-form"]')).toBeVisible();
    
    // Fill form offline
    await page.fill('[data-testid="beneficiary-name"]', 'Offline Beneficiary');
    await page.click('[data-testid="save-offline"]');
    
    await expect(page.locator('[data-testid="saved-offline"]')).toBeVisible();
    
    // Go back online
    await page.context().setOffline(false);
    await expect(page.locator('[data-testid="sync-pending"]')).toBeVisible();
    
    // Sync offline data
    await page.click('[data-testid="sync-now"]');
    await expect(page.locator('[data-testid="sync-complete"]')).toBeVisible();
  });
});
