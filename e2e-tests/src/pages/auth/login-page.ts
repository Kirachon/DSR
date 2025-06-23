import { Page, Locator, expect } from '@playwright/test';
import { BasePage } from '../base-page';

/**
 * Page Object for the Login page
 * Handles all login-related interactions and validations
 */
export class LoginPage extends BasePage {
  // Form elements
  private readonly emailInput: Locator;
  private readonly passwordInput: Locator;
  private readonly rememberMeCheckbox: Locator;
  private readonly loginButton: Locator;
  private readonly forgotPasswordLink: Locator;
  private readonly registerLink: Locator;
  private readonly philsysLoginButton: Locator;

  // Modal elements
  private readonly forgotPasswordModal: Locator;
  private readonly resetEmailInput: Locator;
  private readonly resetPasswordButton: Locator;
  private readonly modalCloseButton: Locator;

  constructor(page: Page) {
    super(page);
    
    // Initialize locators
    this.emailInput = page.locator('[data-testid="email-input"]');
    this.passwordInput = page.locator('[data-testid="password-input"]');
    this.rememberMeCheckbox = page.locator('[data-testid="remember-me-checkbox"]');
    this.loginButton = page.locator('[data-testid="login-submit-button"]');
    this.forgotPasswordLink = page.locator('[data-testid="forgot-password-link"]');
    this.registerLink = page.locator('[data-testid="register-link"]');
    this.philsysLoginButton = page.locator('[data-testid="philsys-login-button"]');
    
    // Modal elements
    this.forgotPasswordModal = page.locator('[data-testid="forgot-password-modal"]');
    this.resetEmailInput = page.locator('[data-testid="reset-email-input"]');
    this.resetPasswordButton = page.locator('[data-testid="reset-password-button"]');
    this.modalCloseButton = page.locator('[data-testid="modal-close-button"]');
  }

  /**
   * Navigate to the login page
   */
  async goto(): Promise<void> {
    await this.page.goto('/login');
    await this.waitForPageLoad();
  }

  /**
   * Verify the login page is loaded correctly
   */
  async verifyPageLoaded(): Promise<void> {
    await expect(this.page).toHaveTitle(/Login.*DSR/);
    await expect(this.emailInput).toBeVisible();
    await expect(this.passwordInput).toBeVisible();
    await expect(this.loginButton).toBeVisible();
  }

  /**
   * Perform login with email and password
   */
  async login(email: string, password: string, rememberMe: boolean = false): Promise<void> {
    await this.fillField(this.emailInput, email);
    await this.fillField(this.passwordInput, password);
    
    if (rememberMe) {
      await this.rememberMeCheckbox.check();
    }
    
    await this.clickButton(this.loginButton, true);
  }

  /**
   * Perform login and wait for redirect to dashboard
   */
  async loginAndWaitForDashboard(email: string, password: string): Promise<void> {
    await this.login(email, password);
    await this.page.waitForURL('**/dashboard');
  }

  /**
   * Attempt login with invalid credentials
   */
  async loginWithInvalidCredentials(email: string, password: string): Promise<void> {
    await this.login(email, password);
    await this.verifyErrorMessage();
  }

  /**
   * Test login with empty fields
   */
  async loginWithEmptyFields(): Promise<void> {
    await this.clickButton(this.loginButton);
    // Browser validation should prevent form submission
    await expect(this.emailInput).toHaveAttribute('required');
    await expect(this.passwordInput).toHaveAttribute('required');
  }

  /**
   * Test email validation
   */
  async testEmailValidation(invalidEmail: string): Promise<void> {
    await this.fillField(this.emailInput, invalidEmail);
    await this.fillField(this.passwordInput, 'somepassword');
    await this.clickButton(this.loginButton);
    await this.verifyErrorMessage('Please enter a valid email address');
  }

  /**
   * Click forgot password link and open modal
   */
  async openForgotPasswordModal(): Promise<void> {
    await this.forgotPasswordLink.click();
    await expect(this.forgotPasswordModal).toBeVisible();
  }

  /**
   * Submit password reset request
   */
  async submitPasswordReset(email: string): Promise<void> {
    await this.openForgotPasswordModal();
    await this.fillField(this.resetEmailInput, email);
    await this.clickButton(this.resetPasswordButton, true);
    await this.verifySuccessMessage('Password reset email sent');
  }

  /**
   * Close forgot password modal
   */
  async closeForgotPasswordModal(): Promise<void> {
    await this.modalCloseButton.click();
    await expect(this.forgotPasswordModal).not.toBeVisible();
  }

  /**
   * Click register link
   */
  async goToRegister(): Promise<void> {
    await this.registerLink.click();
    await this.page.waitForURL('**/register');
  }

  /**
   * Perform PhilSys login
   */
  async loginWithPhilSys(): Promise<void> {
    await this.clickButton(this.philsysLoginButton, true);
    await this.verifySuccessMessage('PhilSys authentication successful');
    await this.page.waitForURL('**/dashboard');
  }

  /**
   * Verify login form validation
   */
  async verifyFormValidation(): Promise<void> {
    // Test required fields
    await expect(this.emailInput).toHaveAttribute('required');
    await expect(this.passwordInput).toHaveAttribute('required');
    
    // Test email type
    await expect(this.emailInput).toHaveAttribute('type', 'email');
    
    // Test password type
    await expect(this.passwordInput).toHaveAttribute('type', 'password');
  }

  /**
   * Verify remember me functionality
   */
  async verifyRememberMeFunctionality(): Promise<void> {
    await expect(this.rememberMeCheckbox).toBeVisible();
    await this.rememberMeCheckbox.check();
    await expect(this.rememberMeCheckbox).toBeChecked();
    await this.rememberMeCheckbox.uncheck();
    await expect(this.rememberMeCheckbox).not.toBeChecked();
  }

  /**
   * Verify all login options are available
   */
  async verifyLoginOptions(): Promise<void> {
    await expect(this.loginButton).toBeVisible();
    await expect(this.philsysLoginButton).toBeVisible();
    await expect(this.forgotPasswordLink).toBeVisible();
    await expect(this.registerLink).toBeVisible();
  }

  /**
   * Test login form accessibility
   */
  async verifyAccessibility(): Promise<void> {
    // Check for proper labels
    await expect(this.emailInput).toHaveAttribute('aria-label');
    await expect(this.passwordInput).toHaveAttribute('aria-label');
    
    // Check for proper form structure
    const form = this.page.locator('[data-testid="login-form"]');
    await expect(form).toBeVisible();
  }

  /**
   * Verify login page security features
   */
  async verifySecurityFeatures(): Promise<void> {
    // Password field should be masked
    await expect(this.passwordInput).toHaveAttribute('type', 'password');
    
    // Form should use HTTPS in production (mock doesn't enforce this)
    // Check for CSRF protection headers if implemented
  }

  /**
   * Test keyboard navigation
   */
  async testKeyboardNavigation(): Promise<void> {
    // Tab through form elements
    await this.emailInput.focus();
    await this.page.keyboard.press('Tab');
    await expect(this.passwordInput).toBeFocused();
    
    await this.page.keyboard.press('Tab');
    await expect(this.rememberMeCheckbox).toBeFocused();
    
    await this.page.keyboard.press('Tab');
    await expect(this.loginButton).toBeFocused();
  }

  /**
   * Test form submission with Enter key
   */
  async testEnterKeySubmission(email: string, password: string): Promise<void> {
    await this.fillField(this.emailInput, email);
    await this.fillField(this.passwordInput, password);
    await this.passwordInput.press('Enter');
    
    // Should trigger form submission
    await this.waitForLoadingToComplete();
  }

  /**
   * Verify responsive design elements
   */
  async verifyResponsiveDesign(): Promise<void> {
    // Test mobile viewport
    await this.page.setViewportSize({ width: 375, height: 667 });
    await expect(this.emailInput).toBeVisible();
    await expect(this.passwordInput).toBeVisible();
    await expect(this.loginButton).toBeVisible();
    
    // Test tablet viewport
    await this.page.setViewportSize({ width: 768, height: 1024 });
    await expect(this.emailInput).toBeVisible();
    await expect(this.passwordInput).toBeVisible();
    
    // Reset to desktop
    await this.page.setViewportSize({ width: 1920, height: 1080 });
  }

  /**
   * Get current form values
   */
  async getFormValues(): Promise<{ email: string; password: string; rememberMe: boolean }> {
    return {
      email: await this.emailInput.inputValue(),
      password: await this.passwordInput.inputValue(),
      rememberMe: await this.rememberMeCheckbox.isChecked(),
    };
  }

  /**
   * Clear all form fields
   */
  async clearForm(): Promise<void> {
    await this.emailInput.clear();
    await this.passwordInput.clear();
    if (await this.rememberMeCheckbox.isChecked()) {
      await this.rememberMeCheckbox.uncheck();
    }
  }
}
