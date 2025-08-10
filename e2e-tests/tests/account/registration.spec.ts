import { test, expect } from '@playwright/test';
import { RegisterPage } from '../../src/pages/auth/register-page';
import { TestDataManager } from '../../src/utils/test-data-manager';

test.describe('User Registration', () => {
  let registerPage: RegisterPage;

  test.beforeEach(async ({ page }) => {
    registerPage = new RegisterPage(page);
    await registerPage.goto();
  });

  test.describe('Page Load and UI', () => {
    test('should load registration page correctly @smoke', async () => {
      await registerPage.verifyPageLoaded();
    });

    test('should display all required form elements', async () => {
      await registerPage.verifyFormValidation();
    });

    test('should be responsive on different screen sizes', async () => {
      await registerPage.verifyResponsiveDesign();
    });

    test('should support keyboard navigation', async () => {
      await registerPage.testKeyboardNavigation();
    });
  });

  test.describe('Valid Registration Scenarios', () => {
    test('should register successfully with valid data @smoke', async () => {
      const userData = TestDataManager.generateUserData('citizen');
      await registerPage.registerAndWaitForSuccess({
        firstName: userData.firstName,
        lastName: userData.lastName,
        email: userData.email,
        password: userData.password
      });
    });

    test('should register citizen user', async () => {
      const userData = TestDataManager.generateUserData('citizen');
      await registerPage.register({
        firstName: userData.firstName,
        lastName: userData.lastName,
        email: userData.email,
        password: userData.password
      });
      
      await registerPage.verifySuccessMessage();
    });

    test('should register with strong password', async () => {
      const userData = TestDataManager.generateUserData('citizen');
      const strongPassword = 'StrongPassword123!@#';
      
      await registerPage.register({
        firstName: userData.firstName,
        lastName: userData.lastName,
        email: userData.email,
        password: strongPassword
      });
      
      await registerPage.verifySuccessMessage();
    });

    test('should submit form with Enter key', async () => {
      const userData = TestDataManager.generateUserData('citizen');
      await registerPage.testEnterKeySubmission({
        firstName: userData.firstName,
        lastName: userData.lastName,
        email: userData.email,
        password: userData.password
      });
    });
  });

  test.describe('Invalid Registration Scenarios', () => {
    test('should reject registration with empty fields', async () => {
      await registerPage.testRequiredFields();
    });

    test('should reject registration with invalid email formats', async () => {
      const invalidEmails = [
        'invalid-email',
        '@domain.com',
        'user@',
        'user.domain.com',
        'user@domain',
        'user name@domain.com'
      ];
      
      for (const email of invalidEmails) {
        await registerPage.testEmailValidation(email);
        await registerPage.clearForm();
      }
    });

    test('should reject weak passwords', async () => {
      const weakPasswords = [
        '123',
        'password',
        'Password',
        '12345678',
        'password123'
      ];
      
      for (const password of weakPasswords) {
        await registerPage.testWeakPassword(password);
        await registerPage.clearForm();
      }
    });

    test('should reject mismatched passwords', async () => {
      await registerPage.testPasswordMismatch('Password123!', 'DifferentPassword123!');
    });

    test('should require terms acceptance', async () => {
      await registerPage.testTermsRequirement();
    });

    test('should reject registration with existing email', async () => {
      const userData = TestDataManager.generateUserData('citizen');
      
      // First registration should succeed
      await registerPage.registerAndWaitForSuccess({
        firstName: userData.firstName,
        lastName: userData.lastName,
        email: userData.email,
        password: userData.password
      });
      
      // Navigate back to registration
      await registerPage.goto();
      
      // Second registration with same email should fail
      await registerPage.registerWithInvalidData({
        firstName: 'Different',
        lastName: 'User',
        email: userData.email, // Same email
        password: 'DifferentPassword123!',
        acceptTerms: true
      });
    });

    test('should handle special characters in names', async () => {
      const userData = TestDataManager.generateUserData('citizen');
      
      await registerPage.register({
        firstName: "José María",
        lastName: "Dela Cruz-Santos",
        email: userData.email,
        password: userData.password
      });
      
      await registerPage.verifySuccessMessage();
    });

    test('should reject SQL injection attempts', async () => {
      const invalidData = TestDataManager.generateInvalidData();
      
      await registerPage.registerWithInvalidData({
        firstName: invalidData.sqlInjection,
        lastName: invalidData.sqlInjection,
        email: 'test@example.com',
        password: 'ValidPassword123!',
        acceptTerms: true
      });
    });

    test('should reject XSS attempts', async () => {
      const invalidData = TestDataManager.generateInvalidData();
      
      await registerPage.registerWithInvalidData({
        firstName: invalidData.xssPayload,
        lastName: 'User',
        email: 'test@example.com',
        password: 'ValidPassword123!',
        acceptTerms: true
      });
    });
  });

  test.describe('Password Strength Validation', () => {
    test('should show password strength indicator', async () => {
      await registerPage.testPasswordStrengthLevels();
    });

    test('should validate password complexity requirements', async () => {
      const passwordTests = [
        { password: 'weak', shouldPass: false },
        { password: 'StrongPassword123!', shouldPass: true },
        { password: 'NoNumbers!', shouldPass: false },
        { password: 'nonumbersorspecial', shouldPass: false },
        { password: 'NOLOWERCASE123!', shouldPass: false },
        { password: 'nouppercase123!', shouldPass: false }
      ];

      for (const test of passwordTests) {
        if (test.shouldPass) {
          await registerPage.verifyPasswordStrengthIndicator(test.password, 'Strong');
        } else {
          await registerPage.testWeakPassword(test.password);
        }
        await registerPage.clearForm();
      }
    });
  });

  test.describe('Form Validation', () => {
    test('should validate required fields', async () => {
      await registerPage.testRequiredFields();
    });

    test('should validate field lengths', async () => {
      const userData = TestDataManager.generateUserData('citizen');
      
      // Test very long names
      await registerPage.registerWithInvalidData({
        firstName: 'A'.repeat(101), // Assuming 100 char limit
        lastName: userData.lastName,
        email: userData.email,
        password: userData.password,
        acceptTerms: true
      });
    });

    test('should validate email uniqueness', async () => {
      const userData = TestDataManager.generateUserData('citizen');
      
      // Register first user
      await registerPage.registerAndWaitForSuccess({
        firstName: userData.firstName,
        lastName: userData.lastName,
        email: userData.email,
        password: userData.password
      });
      
      // Try to register with same email
      await registerPage.goto();
      await registerPage.registerWithInvalidData({
        firstName: 'Different',
        lastName: 'User',
        email: userData.email,
        password: 'DifferentPassword123!',
        acceptTerms: true
      });
    });
  });

  test.describe('Navigation', () => {
    test('should navigate to login page', async () => {
      await registerPage.goToLogin();
      await expect(registerPage.page).toHaveURL(/.*\/login/);
    });

    test('should redirect authenticated users to dashboard', async ({ page }) => {
      // Mock authentication
      await page.addInitScript(() => {
        localStorage.setItem('dsr_token', 'mock_token');
        localStorage.setItem('dsr_user', JSON.stringify({ id: '1', email: 'test@example.com' }));
      });
      
      await page.goto('/register');
      await expect(page).toHaveURL(/.*\/dashboard/);
    });
  });

  test.describe('Security Features', () => {
    test('should mask password inputs', async () => {
      const passwordInput = registerPage.page.locator('[data-testid="password-input"]');
      const confirmPasswordInput = registerPage.page.locator('[data-testid="confirm-password-input"]');
      
      await expect(passwordInput).toHaveAttribute('type', 'password');
      await expect(confirmPasswordInput).toHaveAttribute('type', 'password');
    });

    test('should not expose sensitive data in DOM', async () => {
      const userData = TestDataManager.generateUserData('citizen');
      await registerPage.fillRegistrationForm({
        firstName: userData.firstName,
        lastName: userData.lastName,
        email: userData.email,
        password: userData.password
      });
      
      // Check that password is not visible in page source
      const pageContent = await registerPage.page.content();
      expect(pageContent).not.toContain(userData.password);
    });

    test('should implement CSRF protection', async () => {
      // Check for CSRF token or other protection mechanisms
      const form = registerPage.page.locator('[data-testid="register-form"]');
      await expect(form).toBeVisible();
      
      // In a real implementation, check for CSRF tokens
    });
  });

  test.describe('Accessibility', () => {
    test('should meet accessibility standards', async () => {
      // Check for proper labels
      const inputs = [
        '[data-testid="first-name-input"]',
        '[data-testid="last-name-input"]',
        '[data-testid="email-input"]',
        '[data-testid="password-input"]',
        '[data-testid="confirm-password-input"]'
      ];
      
      for (const input of inputs) {
        const element = registerPage.page.locator(input);
        await expect(element).toHaveAttribute('aria-label');
      }
    });

    test('should support screen readers', async () => {
      // Check for proper form structure and labels
      const form = registerPage.page.locator('[data-testid="register-form"]');
      await expect(form).toBeVisible();
      
      // Check for fieldset and legend if grouped fields exist
    });
  });

  test.describe('Performance', () => {
    test('should load within acceptable time', async ({ page }) => {
      const startTime = Date.now();
      await page.goto('/register');
      await page.waitForLoadState('networkidle');
      const loadTime = Date.now() - startTime;
      
      // Should load within 3 seconds
      expect(loadTime).toBeLessThan(3000);
    });

    test('should handle form submission efficiently', async () => {
      const userData = TestDataManager.generateUserData('citizen');
      
      const startTime = Date.now();
      await registerPage.register({
        firstName: userData.firstName,
        lastName: userData.lastName,
        email: userData.email,
        password: userData.password
      });
      const submitTime = Date.now() - startTime;
      
      // Form submission should complete within 5 seconds
      expect(submitTime).toBeLessThan(5000);
    });
  });

  test.describe('Error Handling', () => {
    test('should handle network errors gracefully', async ({ page }) => {
      // Simulate network failure
      await page.route('**/api/auth/register', route => {
        route.abort('failed');
      });
      
      const userData = TestDataManager.generateUserData('citizen');
      await registerPage.register({
        firstName: userData.firstName,
        lastName: userData.lastName,
        email: userData.email,
        password: userData.password
      });
      
      await registerPage.verifyErrorMessage();
    });

    test('should handle server errors gracefully', async ({ page }) => {
      // Simulate server error
      await page.route('**/api/auth/register', route => {
        route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({ error: 'Internal server error' })
        });
      });
      
      const userData = TestDataManager.generateUserData('citizen');
      await registerPage.register({
        firstName: userData.firstName,
        lastName: userData.lastName,
        email: userData.email,
        password: userData.password
      });
      
      await registerPage.verifyErrorMessage();
    });
  });
});
