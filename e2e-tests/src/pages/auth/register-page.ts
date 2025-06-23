import { Page, Locator, expect } from '@playwright/test';
import { BasePage } from '../base-page';

/**
 * Page Object for the Registration page
 * Handles all registration-related interactions and validations
 */
export class RegisterPage extends BasePage {
  // Form elements
  private readonly firstNameInput: Locator;
  private readonly lastNameInput: Locator;
  private readonly emailInput: Locator;
  private readonly passwordInput: Locator;
  private readonly confirmPasswordInput: Locator;
  private readonly termsCheckbox: Locator;
  private readonly registerButton: Locator;
  private readonly loginLink: Locator;

  // Password strength indicator
  private readonly passwordStrengthIndicator: Locator;
  private readonly passwordFeedback: Locator;

  constructor(page: Page) {
    super(page);
    
    // Initialize locators
    this.firstNameInput = page.locator('[data-testid="first-name-input"]');
    this.lastNameInput = page.locator('[data-testid="last-name-input"]');
    this.emailInput = page.locator('[data-testid="email-input"]');
    this.passwordInput = page.locator('[data-testid="password-input"]');
    this.confirmPasswordInput = page.locator('[data-testid="confirm-password-input"]');
    this.termsCheckbox = page.locator('[data-testid="terms-checkbox"]');
    this.registerButton = page.locator('[data-testid="register-submit-button"]');
    this.loginLink = page.locator('[data-testid="login-link"]');
    
    // Password strength elements
    this.passwordStrengthIndicator = page.locator('[data-testid="password-strength"]');
    this.passwordFeedback = page.locator('[data-testid="password-feedback"]');
  }

  /**
   * Navigate to the registration page
   */
  async goto(): Promise<void> {
    await this.page.goto('/register');
    await this.waitForPageLoad();
  }

  /**
   * Verify the registration page is loaded correctly
   */
  async verifyPageLoaded(): Promise<void> {
    await expect(this.page).toHaveTitle(/Register.*DSR/);
    await expect(this.firstNameInput).toBeVisible();
    await expect(this.lastNameInput).toBeVisible();
    await expect(this.emailInput).toBeVisible();
    await expect(this.passwordInput).toBeVisible();
    await expect(this.confirmPasswordInput).toBeVisible();
    await expect(this.registerButton).toBeVisible();
  }

  /**
   * Fill registration form with user data
   */
  async fillRegistrationForm(userData: {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
    confirmPassword?: string;
    acceptTerms?: boolean;
  }): Promise<void> {
    await this.fillField(this.firstNameInput, userData.firstName);
    await this.fillField(this.lastNameInput, userData.lastName);
    await this.fillField(this.emailInput, userData.email);
    await this.fillField(this.passwordInput, userData.password);
    await this.fillField(this.confirmPasswordInput, userData.confirmPassword || userData.password);
    
    if (userData.acceptTerms !== false) {
      await this.termsCheckbox.check();
    }
  }

  /**
   * Submit registration form
   */
  async submitRegistration(): Promise<void> {
    await this.clickButton(this.registerButton, true);
  }

  /**
   * Complete registration process
   */
  async register(userData: {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
    confirmPassword?: string;
    acceptTerms?: boolean;
  }): Promise<void> {
    await this.fillRegistrationForm(userData);
    await this.submitRegistration();
  }

  /**
   * Register and wait for success
   */
  async registerAndWaitForSuccess(userData: {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
  }): Promise<void> {
    await this.register(userData);
    await this.verifySuccessMessage('Registration successful');
  }

  /**
   * Test registration with invalid data
   */
  async registerWithInvalidData(userData: {
    firstName?: string;
    lastName?: string;
    email?: string;
    password?: string;
    confirmPassword?: string;
    acceptTerms?: boolean;
  }): Promise<void> {
    if (userData.firstName !== undefined) {
      await this.fillField(this.firstNameInput, userData.firstName);
    }
    if (userData.lastName !== undefined) {
      await this.fillField(this.lastNameInput, userData.lastName);
    }
    if (userData.email !== undefined) {
      await this.fillField(this.emailInput, userData.email);
    }
    if (userData.password !== undefined) {
      await this.fillField(this.passwordInput, userData.password);
    }
    if (userData.confirmPassword !== undefined) {
      await this.fillField(this.confirmPasswordInput, userData.confirmPassword);
    }
    if (userData.acceptTerms === true) {
      await this.termsCheckbox.check();
    }
    
    await this.submitRegistration();
    await this.verifyErrorMessage();
  }

  /**
   * Test password mismatch validation
   */
  async testPasswordMismatch(password: string, confirmPassword: string): Promise<void> {
    await this.fillField(this.passwordInput, password);
    await this.fillField(this.confirmPasswordInput, confirmPassword);
    await this.submitRegistration();
    await this.verifyErrorMessage('Passwords do not match');
  }

  /**
   * Test weak password validation
   */
  async testWeakPassword(weakPassword: string): Promise<void> {
    await this.fillField(this.passwordInput, weakPassword);
    await this.submitRegistration();
    await this.verifyErrorMessage();
  }

  /**
   * Test email validation
   */
  async testEmailValidation(invalidEmail: string): Promise<void> {
    await this.fillField(this.emailInput, invalidEmail);
    await this.submitRegistration();
    await this.verifyErrorMessage('Please enter a valid email address');
  }

  /**
   * Test terms and conditions requirement
   */
  async testTermsRequirement(): Promise<void> {
    await this.fillRegistrationForm({
      firstName: 'Test',
      lastName: 'User',
      email: 'test@example.com',
      password: 'TestPassword123!',
      acceptTerms: false
    });
    await this.submitRegistration();
    await this.verifyErrorMessage('Please accept the terms and conditions');
  }

  /**
   * Test required fields validation
   */
  async testRequiredFields(): Promise<void> {
    await this.submitRegistration();
    
    // Check that required attributes are present
    await expect(this.firstNameInput).toHaveAttribute('required');
    await expect(this.lastNameInput).toHaveAttribute('required');
    await expect(this.emailInput).toHaveAttribute('required');
    await expect(this.passwordInput).toHaveAttribute('required');
    await expect(this.confirmPasswordInput).toHaveAttribute('required');
  }

  /**
   * Verify password strength indicator
   */
  async verifyPasswordStrengthIndicator(password: string, expectedStrength: string): Promise<void> {
    await this.fillField(this.passwordInput, password);
    
    // Wait for password strength to update
    await this.page.waitForTimeout(500);
    
    if (await this.isVisible(this.passwordStrengthIndicator)) {
      await expect(this.passwordStrengthIndicator).toContainText(expectedStrength);
    }
  }

  /**
   * Test password strength with various passwords
   */
  async testPasswordStrengthLevels(): Promise<void> {
    const passwordTests = [
      { password: '123', expectedStrength: 'Very Weak' },
      { password: 'password', expectedStrength: 'Weak' },
      { password: 'Password1', expectedStrength: 'Fair' },
      { password: 'Password1!', expectedStrength: 'Good' },
      { password: 'StrongPassword123!', expectedStrength: 'Strong' }
    ];

    for (const test of passwordTests) {
      await this.verifyPasswordStrengthIndicator(test.password, test.expectedStrength);
    }
  }

  /**
   * Navigate to login page
   */
  async goToLogin(): Promise<void> {
    await this.loginLink.click();
    await this.page.waitForURL('**/login');
  }

  /**
   * Verify form validation
   */
  async verifyFormValidation(): Promise<void> {
    // Test input types
    await expect(this.emailInput).toHaveAttribute('type', 'email');
    await expect(this.passwordInput).toHaveAttribute('type', 'password');
    await expect(this.confirmPasswordInput).toHaveAttribute('type', 'password');
    
    // Test required fields
    await expect(this.firstNameInput).toHaveAttribute('required');
    await expect(this.lastNameInput).toHaveAttribute('required');
    await expect(this.emailInput).toHaveAttribute('required');
    await expect(this.passwordInput).toHaveAttribute('required');
    await expect(this.confirmPasswordInput).toHaveAttribute('required');
  }

  /**
   * Test keyboard navigation
   */
  async testKeyboardNavigation(): Promise<void> {
    // Tab through form elements
    await this.firstNameInput.focus();
    await this.page.keyboard.press('Tab');
    await expect(this.lastNameInput).toBeFocused();
    
    await this.page.keyboard.press('Tab');
    await expect(this.emailInput).toBeFocused();
    
    await this.page.keyboard.press('Tab');
    await expect(this.passwordInput).toBeFocused();
    
    await this.page.keyboard.press('Tab');
    await expect(this.confirmPasswordInput).toBeFocused();
    
    await this.page.keyboard.press('Tab');
    await expect(this.termsCheckbox).toBeFocused();
    
    await this.page.keyboard.press('Tab');
    await expect(this.registerButton).toBeFocused();
  }

  /**
   * Test form submission with Enter key
   */
  async testEnterKeySubmission(userData: {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
  }): Promise<void> {
    await this.fillRegistrationForm(userData);
    await this.confirmPasswordInput.press('Enter');
    
    // Should trigger form submission
    await this.waitForLoadingToComplete();
  }

  /**
   * Verify responsive design
   */
  async verifyResponsiveDesign(): Promise<void> {
    // Test mobile viewport
    await this.page.setViewportSize({ width: 375, height: 667 });
    await expect(this.firstNameInput).toBeVisible();
    await expect(this.registerButton).toBeVisible();
    
    // Test tablet viewport
    await this.page.setViewportSize({ width: 768, height: 1024 });
    await expect(this.firstNameInput).toBeVisible();
    await expect(this.registerButton).toBeVisible();
    
    // Reset to desktop
    await this.page.setViewportSize({ width: 1920, height: 1080 });
  }

  /**
   * Get current form values
   */
  async getFormValues(): Promise<{
    firstName: string;
    lastName: string;
    email: string;
    password: string;
    confirmPassword: string;
    termsAccepted: boolean;
  }> {
    return {
      firstName: await this.firstNameInput.inputValue(),
      lastName: await this.lastNameInput.inputValue(),
      email: await this.emailInput.inputValue(),
      password: await this.passwordInput.inputValue(),
      confirmPassword: await this.confirmPasswordInput.inputValue(),
      termsAccepted: await this.termsCheckbox.isChecked(),
    };
  }

  /**
   * Clear all form fields
   */
  async clearForm(): Promise<void> {
    await this.firstNameInput.clear();
    await this.lastNameInput.clear();
    await this.emailInput.clear();
    await this.passwordInput.clear();
    await this.confirmPasswordInput.clear();
    if (await this.termsCheckbox.isChecked()) {
      await this.termsCheckbox.uncheck();
    }
  }
}
