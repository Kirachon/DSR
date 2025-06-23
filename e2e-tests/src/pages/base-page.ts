import { Page, Locator, expect } from '@playwright/test';

/**
 * Base Page Object class providing common functionality for all pages
 * Implements common patterns and utilities used across the DSR application
 */
export abstract class BasePage {
  protected page: Page;
  protected baseUrl: string;

  // Common locators present on most pages
  protected readonly loadingSpinner: Locator;
  protected readonly errorMessage: Locator;
  protected readonly successMessage: Locator;
  protected readonly navigationMenu: Locator;
  protected readonly userMenu: Locator;
  protected readonly logoutButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.baseUrl = process.env.BASE_URL || 'http://localhost:3000';
    
    // Initialize common locators
    this.loadingSpinner = page.locator('[data-testid="loading-spinner"]');
    this.errorMessage = page.locator('[data-testid="error-message"]');
    this.successMessage = page.locator('[data-testid="success-message"]');
    this.navigationMenu = page.locator('[data-testid="navigation-menu"]');
    this.userMenu = page.locator('[data-testid="user-menu"]');
    this.logoutButton = page.locator('[data-testid="logout-button"]');
  }

  /**
   * Navigate to the page
   */
  abstract goto(): Promise<void>;

  /**
   * Wait for the page to be fully loaded
   */
  async waitForPageLoad(): Promise<void> {
    await this.page.waitForLoadState('networkidle');
    await this.waitForLoadingToComplete();
  }

  /**
   * Wait for loading spinner to disappear
   */
  async waitForLoadingToComplete(): Promise<void> {
    try {
      await this.loadingSpinner.waitFor({ state: 'hidden', timeout: 10000 });
    } catch (error) {
      // Loading spinner might not be present, which is fine
    }
  }

  /**
   * Check if user is authenticated by looking for user menu
   */
  async isAuthenticated(): Promise<boolean> {
    try {
      await this.userMenu.waitFor({ state: 'visible', timeout: 5000 });
      return true;
    } catch {
      return false;
    }
  }

  /**
   * Get current page title
   */
  async getPageTitle(): Promise<string> {
    return await this.page.title();
  }

  /**
   * Get current URL
   */
  getCurrentUrl(): string {
    return this.page.url();
  }

  /**
   * Take a screenshot
   */
  async takeScreenshot(name: string): Promise<void> {
    await this.page.screenshot({ 
      path: `test-results/screenshots/${name}-${Date.now()}.png`,
      fullPage: true 
    });
  }

  /**
   * Wait for and verify success message
   */
  async verifySuccessMessage(expectedMessage?: string): Promise<void> {
    await this.successMessage.waitFor({ state: 'visible', timeout: 10000 });
    if (expectedMessage) {
      await expect(this.successMessage).toContainText(expectedMessage);
    }
  }

  /**
   * Wait for and verify error message
   */
  async verifyErrorMessage(expectedMessage?: string): Promise<void> {
    await this.errorMessage.waitFor({ state: 'visible', timeout: 10000 });
    if (expectedMessage) {
      await expect(this.errorMessage).toContainText(expectedMessage);
    }
  }

  /**
   * Fill form field with data validation
   */
  async fillField(locator: Locator, value: string, options?: { 
    clear?: boolean; 
    validate?: boolean;
  }): Promise<void> {
    const { clear = true, validate = true } = options || {};
    
    if (clear) {
      await locator.clear();
    }
    
    await locator.fill(value);
    
    if (validate) {
      await expect(locator).toHaveValue(value);
    }
  }

  /**
   * Select option from dropdown
   */
  async selectOption(locator: Locator, value: string): Promise<void> {
    await locator.selectOption(value);
    await expect(locator).toHaveValue(value);
  }

  /**
   * Click button with loading wait
   */
  async clickButton(locator: Locator, waitForResponse?: boolean): Promise<void> {
    if (waitForResponse) {
      await Promise.all([
        this.page.waitForResponse(response => response.status() < 400),
        locator.click()
      ]);
    } else {
      await locator.click();
    }
    await this.waitForLoadingToComplete();
  }

  /**
   * Navigate using the main navigation menu
   */
  async navigateToSection(sectionName: string): Promise<void> {
    const navLink = this.navigationMenu.locator(`[data-testid="nav-${sectionName}"]`);
    await navLink.click();
    await this.waitForPageLoad();
  }

  /**
   * Logout from the application
   */
  async logout(): Promise<void> {
    await this.userMenu.click();
    await this.logoutButton.click();
    await this.page.waitForURL('**/login');
  }

  /**
   * Check if element is visible
   */
  async isVisible(locator: Locator): Promise<boolean> {
    try {
      await locator.waitFor({ state: 'visible', timeout: 5000 });
      return true;
    } catch {
      return false;
    }
  }

  /**
   * Wait for API response
   */
  async waitForApiResponse(urlPattern: string | RegExp, timeout: number = 30000): Promise<void> {
    await this.page.waitForResponse(
      response => {
        const url = response.url();
        if (typeof urlPattern === 'string') {
          return url.includes(urlPattern);
        }
        return urlPattern.test(url);
      },
      { timeout }
    );
  }

  /**
   * Scroll element into view
   */
  async scrollIntoView(locator: Locator): Promise<void> {
    await locator.scrollIntoViewIfNeeded();
  }

  /**
   * Get text content from element
   */
  async getTextContent(locator: Locator): Promise<string> {
    return await locator.textContent() || '';
  }

  /**
   * Check if page has specific URL pattern
   */
  async hasUrlPattern(pattern: string | RegExp): Promise<boolean> {
    const currentUrl = this.getCurrentUrl();
    if (typeof pattern === 'string') {
      return currentUrl.includes(pattern);
    }
    return pattern.test(currentUrl);
  }
}
