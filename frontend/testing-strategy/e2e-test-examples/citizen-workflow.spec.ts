import { test, expect } from '@playwright/test';
import { loginAs } from '../test-utils/auth-helpers';

/**
 * Citizen User Journey E2E Test
 * Tests the complete citizen workflow from registration to application status tracking
 */
test.describe('Citizen User Journey', () => {
  test.beforeEach(async ({ page }) => {
    // Set theme to citizen
    await page.addInitScript(() => {
      localStorage.setItem('dsr-theme-preference', 'citizen');
    });
  });

  test('should complete registration process', async ({ page }) => {
    // Navigate to registration page
    await page.goto('/register');
    
    // Fill out registration form
    await page.fill('[data-testid="full-name"]', 'Juan Dela Cruz');
    await page.fill('[data-testid="email"]', `citizen-${Date.now()}@example.com`);
    await page.fill('[data-testid="password"]', 'Password123!');
    await page.fill('[data-testid="confirm-password"]', 'Password123!');
    
    // Accept terms and submit
    await page.check('[data-testid="terms-checkbox"]');
    await page.click('[data-testid="register-button"]');
    
    // Verify successful registration
    await expect(page.locator('[data-testid="registration-success"]')).toBeVisible();
    await expect(page).toHaveURL(/\/verify-email/);
  });

  test('should login and view dashboard', async ({ page }) => {
    // Login as citizen
    await loginAs(page, 'citizen');
    
    // Verify dashboard elements
    await expect(page.locator('[data-testid="citizen-dashboard"]')).toBeVisible();
    await expect(page.locator('[data-testid="welcome-message"]')).toContainText('Welcome');
    await expect(page.locator('[data-testid="service-cards"]')).toBeVisible();
  });

  test('should apply for educational assistance', async ({ page }) => {
    // Login as citizen
    await loginAs(page, 'citizen');
    
    // Navigate to services
    await page.click('[data-testid="services-nav"]');
    
    // Select educational assistance
    await page.click('[data-testid="educational-assistance-card"]');
    
    // Complete application form (step 1)
    await page.fill('[data-testid="student-name"]', 'Juan Dela Cruz Jr.');
    await page.fill('[data-testid="school-name"]', 'University of the Philippines');
    await page.selectOption('[data-testid="education-level"]', 'college');
    await page.fill('[data-testid="course"]', 'Computer Science');
    await page.click('[data-testid="next-button"]');
    
    // Complete application form (step 2)
    await page.fill('[data-testid="parent-name"]', 'Maria Dela Cruz');
    await page.fill('[data-testid="monthly-income"]', '15000');
    await page.fill('[data-testid="family-members"]', '4');
    await page.click('[data-testid="next-button"]');
    
    // Upload documents (step 3)
    await page.setInputFiles('[data-testid="document-upload"]', 'test-files/sample-document.pdf');
    await expect(page.locator('[data-testid="upload-success"]')).toBeVisible();
    await page.click('[data-testid="next-button"]');
    
    // Review and submit (step 4)
    await expect(page.locator('[data-testid="review-student-name"]')).toContainText('Juan Dela Cruz Jr.');
    await expect(page.locator('[data-testid="review-school-name"]')).toContainText('University of the Philippines');
    await page.click('[data-testid="submit-application"]');
    
    // Verify submission success
    await expect(page.locator('[data-testid="application-success"]')).toBeVisible();
    await expect(page).toHaveURL(/\/application-submitted/);
  });

  test('should track application status', async ({ page }) => {
    // Login as citizen
    await loginAs(page, 'citizen');
    
    // Navigate to applications
    await page.click('[data-testid="applications-nav"]');
    
    // Verify applications list
    await expect(page.locator('[data-testid="applications-list"]')).toBeVisible();
    
    // Click on application
    await page.click('[data-testid="application-item"]:first-child');
    
    // Verify application details
    await expect(page.locator('[data-testid="application-details"]')).toBeVisible();
    await expect(page.locator('[data-testid="application-status"]')).toBeVisible();
    await expect(page.locator('[data-testid="application-timeline"]')).toBeVisible();
  });

  test('should update profile information', async ({ page }) => {
    // Login as citizen
    await loginAs(page, 'citizen');
    
    // Navigate to profile
    await page.click('[data-testid="profile-nav"]');
    
    // Update profile information
    await page.fill('[data-testid="address"]', '123 Main St, Manila');
    await page.fill('[data-testid="phone-number"]', '09123456789');
    await page.click('[data-testid="save-profile"]');
    
    // Verify profile update success
    await expect(page.locator('[data-testid="profile-update-success"]')).toBeVisible();
    
    // Reload page and verify persistence
    await page.reload();
    await expect(page.locator('[data-testid="address"]')).toHaveValue('123 Main St, Manila');
    await expect(page.locator('[data-testid="phone-number"]')).toHaveValue('09123456789');
  });

  test('should view payment history', async ({ page }) => {
    // Login as citizen
    await loginAs(page, 'citizen');
    
    // Navigate to payments
    await page.click('[data-testid="payments-nav"]');
    
    // Verify payment history
    await expect(page.locator('[data-testid="payment-history"]')).toBeVisible();
    await expect(page.locator('[data-testid="payment-item"]')).toHaveCount(3);
    
    // View payment details
    await page.click('[data-testid="payment-item"]:first-child');
    await expect(page.locator('[data-testid="payment-details"]')).toBeVisible();
    await expect(page.locator('[data-testid="payment-receipt"]')).toBeVisible();
  });

  test('should be responsive on mobile devices', async ({ page }) => {
    // Set viewport to mobile size
    await page.setViewportSize({ width: 375, height: 667 });
    
    // Login as citizen
    await loginAs(page, 'citizen');
    
    // Verify mobile navigation
    await expect(page.locator('[data-testid="mobile-menu-button"]')).toBeVisible();
    await page.click('[data-testid="mobile-menu-button"]');
    await expect(page.locator('[data-testid="mobile-nav"]')).toBeVisible();
    
    // Navigate to services
    await page.click('[data-testid="services-nav"]');
    
    // Verify responsive layout
    await expect(page.locator('[data-testid="service-cards"]')).toBeVisible();
    
    // Verify touch-friendly elements
    await expect(page.locator('[data-testid="service-card"]')).toHaveCSS('min-height', '44px');
  });
});
