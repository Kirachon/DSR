import { test, expect } from '@playwright/test';
import { LoginPage } from '../../src/pages/auth/login-page';
import { TestDataManager } from '../../src/utils/test-data-manager';

test.describe('Login Functionality', () => {
  let loginPage: LoginPage;

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page);
    await loginPage.goto();
  });

  test.describe('Page Load and UI', () => {
    test('should load login page correctly @smoke', async () => {
      await loginPage.verifyPageLoaded();
      await loginPage.verifyLoginOptions();
    });

    test('should display all required form elements', async () => {
      await loginPage.verifyFormValidation();
    });

    test('should be responsive on different screen sizes', async () => {
      await loginPage.verifyResponsiveDesign();
    });

    test('should support keyboard navigation', async () => {
      await loginPage.testKeyboardNavigation();
    });
  });

  test.describe('Valid Login Scenarios', () => {
    test('should login successfully with valid credentials @smoke', async () => {
      const credentials = TestDataManager.getTestCredentials('user');
      await loginPage.loginAndWaitForDashboard(credentials.email, credentials.password);
      
      // Verify redirect to dashboard
      await expect(loginPage.page).toHaveURL(/.*\/dashboard/);
    });

    test('should login successfully as admin user', async () => {
      const credentials = TestDataManager.getTestCredentials('admin');
      await loginPage.loginAndWaitForDashboard(credentials.email, credentials.password);
      
      // Verify redirect to dashboard
      await expect(loginPage.page).toHaveURL(/.*\/dashboard/);
    });

    test('should login successfully as LGU staff', async () => {
      const credentials = TestDataManager.getTestCredentials('lgu_staff');
      await loginPage.loginAndWaitForDashboard(credentials.email, credentials.password);
      
      // Verify redirect to dashboard
      await expect(loginPage.page).toHaveURL(/.*\/dashboard/);
    });

    test('should remember user when remember me is checked', async () => {
      const credentials = TestDataManager.getTestCredentials('user');
      await loginPage.login(credentials.email, credentials.password, true);
      
      // Verify remember me functionality
      await loginPage.verifyRememberMeFunctionality();
    });

    test('should login with Enter key submission', async () => {
      const credentials = TestDataManager.getTestCredentials('user');
      await loginPage.testEnterKeySubmission(credentials.email, credentials.password);
      
      // Should redirect to dashboard
      await expect(loginPage.page).toHaveURL(/.*\/dashboard/);
    });
  });

  test.describe('Invalid Login Scenarios', () => {
    test('should reject login with invalid email', async () => {
      await loginPage.loginWithInvalidCredentials('invalid@email.com', 'password123');
    });

    test('should reject login with invalid password', async () => {
      const credentials = TestDataManager.getTestCredentials('user');
      await loginPage.loginWithInvalidCredentials(credentials.email, 'wrongpassword');
    });

    test('should reject login with empty fields', async () => {
      await loginPage.loginWithEmptyFields();
    });

    test('should validate email format', async () => {
      const invalidEmails = ['invalid-email', '@domain.com', 'user@', 'user.domain.com'];
      
      for (const email of invalidEmails) {
        await loginPage.testEmailValidation(email);
        await loginPage.clearForm();
      }
    });

    test('should handle SQL injection attempts', async () => {
      const invalidData = TestDataManager.generateInvalidData();
      await loginPage.loginWithInvalidCredentials(invalidData.sqlInjection, invalidData.sqlInjection);
    });

    test('should handle XSS attempts', async () => {
      const invalidData = TestDataManager.generateInvalidData();
      await loginPage.loginWithInvalidCredentials(invalidData.xssPayload, 'password');
    });
  });

  test.describe('Password Reset Functionality', () => {
    test('should open forgot password modal', async () => {
      await loginPage.openForgotPasswordModal();
    });

    test('should submit password reset request', async () => {
      const credentials = TestDataManager.getTestCredentials('user');
      await loginPage.submitPasswordReset(credentials.email);
    });

    test('should close forgot password modal', async () => {
      await loginPage.openForgotPasswordModal();
      await loginPage.closeForgotPasswordModal();
    });

    test('should validate email in password reset', async () => {
      await loginPage.openForgotPasswordModal();
      // Try with invalid email format
      await loginPage.submitPasswordReset('invalid-email');
      await loginPage.verifyErrorMessage();
    });
  });

  test.describe('PhilSys Integration', () => {
    test('should support PhilSys login @philsys', async () => {
      await loginPage.loginWithPhilSys();
      
      // Verify redirect to dashboard
      await expect(loginPage.page).toHaveURL(/.*\/dashboard/);
    });

    test('should display PhilSys login option', async () => {
      await expect(loginPage.page.locator('[data-testid="philsys-login-button"]')).toBeVisible();
    });
  });

  test.describe('Navigation', () => {
    test('should navigate to registration page', async () => {
      await loginPage.goToRegister();
      await expect(loginPage.page).toHaveURL(/.*\/register/);
    });

    test('should redirect authenticated users to dashboard', async ({ page }) => {
      // First login
      const credentials = TestDataManager.getTestCredentials('user');
      await loginPage.loginAndWaitForDashboard(credentials.email, credentials.password);
      
      // Try to access login page again
      await page.goto('/login');
      await expect(page).toHaveURL(/.*\/dashboard/);
    });
  });

  test.describe('Security Features', () => {
    test('should mask password input', async () => {
      await loginPage.verifySecurityFeatures();
    });

    test('should not expose sensitive data in DOM', async () => {
      const credentials = TestDataManager.getTestCredentials('user');
      await loginPage.login(credentials.email, credentials.password);
      
      // Check that password is not visible in page source
      const pageContent = await loginPage.page.content();
      expect(pageContent).not.toContain(credentials.password);
    });

    test('should handle rate limiting gracefully', async () => {
      // Attempt multiple failed logins
      for (let i = 0; i < 5; i++) {
        await loginPage.loginWithInvalidCredentials('test@example.com', 'wrongpassword');
        await loginPage.clearForm();
      }
      
      // Should still show appropriate error message
      await loginPage.verifyErrorMessage();
    });
  });

  test.describe('Accessibility', () => {
    test('should meet accessibility standards', async () => {
      await loginPage.verifyAccessibility();
    });

    test('should support screen readers', async () => {
      // Check for proper ARIA labels and roles
      const emailInput = loginPage.page.locator('[data-testid="email-input"]');
      const passwordInput = loginPage.page.locator('[data-testid="password-input"]');
      
      await expect(emailInput).toHaveAttribute('aria-label');
      await expect(passwordInput).toHaveAttribute('aria-label');
    });
  });

  test.describe('Performance', () => {
    test('should load within acceptable time', async ({ page }) => {
      const startTime = Date.now();
      await page.goto('/login');
      await page.waitForLoadState('networkidle');
      const loadTime = Date.now() - startTime;
      
      // Should load within 3 seconds
      expect(loadTime).toBeLessThan(3000);
    });

    test('should handle slow network conditions', async ({ page }) => {
      // Simulate slow network
      await page.route('**/*', route => {
        setTimeout(() => route.continue(), 1000);
      });
      
      await loginPage.goto();
      await loginPage.verifyPageLoaded();
    });
  });

  test.describe('Error Handling', () => {
    test('should handle network errors gracefully', async ({ page }) => {
      // Simulate network failure
      await page.route('**/api/auth/login', route => {
        route.abort('failed');
      });
      
      const credentials = TestDataManager.getTestCredentials('user');
      await loginPage.login(credentials.email, credentials.password);
      await loginPage.verifyErrorMessage();
    });

    test('should handle server errors gracefully', async ({ page }) => {
      // Simulate server error
      await page.route('**/api/auth/login', route => {
        route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({ error: 'Internal server error' })
        });
      });
      
      const credentials = TestDataManager.getTestCredentials('user');
      await loginPage.login(credentials.email, credentials.password);
      await loginPage.verifyErrorMessage();
    });
  });
});
