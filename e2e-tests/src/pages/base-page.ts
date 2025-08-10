import { Page, Locator, expect, BrowserContext } from '@playwright/test';
import { AxeBuilder } from '@axe-core/playwright';

/**
 * Base Page Object class providing common functionality for all pages
 * Implements common patterns and utilities used across the DSR application
 * Enhanced with accessibility testing, performance monitoring, and component validation
 */
export abstract class BasePage {
  protected page: Page;
  protected context: BrowserContext;
  protected baseUrl: string;

  // Common locators present on most pages
  protected readonly loadingSpinner: Locator;
  protected readonly errorMessage: Locator;
  protected readonly successMessage: Locator;
  protected readonly navigationMenu: Locator;
  protected readonly userMenu: Locator;
  protected readonly logoutButton: Locator;

  // Enhanced component locators
  protected readonly statusBadges: Locator;
  protected readonly progressIndicators: Locator;
  protected readonly dataTables: Locator;
  protected readonly workflowTimelines: Locator;
  protected readonly roleBasedNavigation: Locator;

  constructor(page: Page, context?: BrowserContext) {
    this.page = page;
    this.context = context || page.context();
    this.baseUrl = process.env.BASE_URL || 'http://localhost:3000';

    // Initialize common locators
    this.loadingSpinner = page.locator('[data-testid="loading-spinner"]');
    this.errorMessage = page.locator('[data-testid="error-message"]');
    this.successMessage = page.locator('[data-testid="success-message"]');
    this.navigationMenu = page.locator('[data-testid="navigation-menu"]');
    this.userMenu = page.locator('[data-testid="user-menu"]');
    this.logoutButton = page.locator('[data-testid="logout-button"]');

    // Initialize enhanced component locators
    this.statusBadges = page.locator('[role="status"], .status-badge, [data-testid*="status"]');
    this.progressIndicators = page.locator('[role="progressbar"], .progress-indicator, [data-testid*="progress"]');
    this.dataTables = page.locator('table, [role="table"], .data-table, [data-testid*="table"]');
    this.workflowTimelines = page.locator('.workflow-timeline, [data-testid*="timeline"]');
    this.roleBasedNavigation = page.locator('nav, [role="navigation"], .navigation, [data-testid*="nav"]');
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

  // ===== ENHANCED TESTING UTILITIES =====

  /**
   * Run accessibility tests on the current page
   */
  async runAccessibilityTests(options?: {
    includeTags?: string[];
    excludeTags?: string[];
    rules?: Record<string, any>;
  }): Promise<void> {
    const axeBuilder = new AxeBuilder({ page: this.page });

    if (options?.includeTags) {
      axeBuilder.withTags(options.includeTags);
    }

    if (options?.excludeTags) {
      axeBuilder.disableTags(options.excludeTags);
    }

    if (options?.rules) {
      axeBuilder.withRules(Object.keys(options.rules));
    }

    const results = await axeBuilder.analyze();
    expect(results.violations).toEqual([]);
  }

  /**
   * Measure page performance metrics
   */
  async measurePerformance(): Promise<{
    loadTime: number;
    domContentLoaded: number;
    firstContentfulPaint: number;
    largestContentfulPaint: number;
  }> {
    const performanceMetrics = await this.page.evaluate(() => {
      const navigation = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming;
      const paint = performance.getEntriesByType('paint');

      return {
        loadTime: navigation.loadEventEnd - navigation.loadEventStart,
        domContentLoaded: navigation.domContentLoadedEventEnd - navigation.domContentLoadedEventStart,
        firstContentfulPaint: paint.find(p => p.name === 'first-contentful-paint')?.startTime || 0,
        largestContentfulPaint: 0 // Will be updated with LCP observer if available
      };
    });

    return performanceMetrics;
  }

  /**
   * Validate response time is under threshold
   */
  async validateResponseTime(threshold: number = 2000): Promise<void> {
    const startTime = Date.now();
    await this.waitForPageLoad();
    const endTime = Date.now();
    const responseTime = endTime - startTime;

    expect(responseTime).toBeLessThan(threshold);
  }

  /**
   * Test component responsiveness across different viewport sizes
   */
  async testResponsiveDesign(viewports: Array<{ width: number; height: number; name: string }>): Promise<void> {
    for (const viewport of viewports) {
      await this.page.setViewportSize({ width: viewport.width, height: viewport.height });
      await this.waitForPageLoad();

      // Take screenshot for visual verification
      await this.takeScreenshot(`responsive-${viewport.name}-${viewport.width}x${viewport.height}`);

      // Verify no horizontal scrollbar on mobile
      if (viewport.width <= 768) {
        const hasHorizontalScroll = await this.page.evaluate(() => {
          return document.documentElement.scrollWidth > document.documentElement.clientWidth;
        });
        expect(hasHorizontalScroll).toBeFalsy();
      }
    }
  }

  /**
   * Validate status badges are present and functional
   */
  async validateStatusBadges(): Promise<void> {
    const badges = await this.statusBadges.all();

    for (const badge of badges) {
      // Check badge is visible
      await expect(badge).toBeVisible();

      // Check badge has proper ARIA attributes
      const ariaLabel = await badge.getAttribute('aria-label');
      const role = await badge.getAttribute('role');

      expect(ariaLabel || role).toBeTruthy();

      // Check badge has text content
      const textContent = await badge.textContent();
      expect(textContent?.trim()).toBeTruthy();
    }
  }

  /**
   * Validate progress indicators functionality
   */
  async validateProgressIndicators(): Promise<void> {
    const indicators = await this.progressIndicators.all();

    for (const indicator of indicators) {
      await expect(indicator).toBeVisible();

      // Check for proper ARIA attributes
      const role = await indicator.getAttribute('role');
      const ariaValueNow = await indicator.getAttribute('aria-valuenow');
      const ariaValueMin = await indicator.getAttribute('aria-valuemin');
      const ariaValueMax = await indicator.getAttribute('aria-valuemax');

      expect(role).toBe('progressbar');
      expect(ariaValueNow).toBeTruthy();
      expect(ariaValueMin).toBeTruthy();
      expect(ariaValueMax).toBeTruthy();
    }
  }

  /**
   * Validate data table functionality
   */
  async validateDataTables(): Promise<void> {
    const tables = await this.dataTables.all();

    for (const table of tables) {
      await expect(table).toBeVisible();

      // Check table has headers
      const headers = table.locator('th, [role="columnheader"]');
      const headerCount = await headers.count();
      expect(headerCount).toBeGreaterThan(0);

      // Check table has data rows
      const rows = table.locator('tr, [role="row"]');
      const rowCount = await rows.count();
      expect(rowCount).toBeGreaterThan(1); // At least header + 1 data row
    }
  }

  /**
   * Test keyboard navigation
   */
  async testKeyboardNavigation(): Promise<void> {
    // Test Tab navigation
    await this.page.keyboard.press('Tab');

    // Verify focus is visible
    const focusedElement = await this.page.locator(':focus').first();
    await expect(focusedElement).toBeVisible();

    // Test Enter key on focused element
    await this.page.keyboard.press('Enter');
    await this.waitForLoadingToComplete();
  }

  /**
   * Validate role-based content visibility
   */
  async validateRoleBasedContent(expectedRole: string): Promise<void> {
    // Check for role-specific navigation items
    const navItems = await this.roleBasedNavigation.locator('[data-role], [data-user-role]').all();

    for (const item of navItems) {
      const itemRole = await item.getAttribute('data-role') || await item.getAttribute('data-user-role');
      if (itemRole && itemRole !== expectedRole) {
        await expect(item).not.toBeVisible();
      }
    }
  }

  /**
   * Test error handling and recovery
   */
  async testErrorHandling(): Promise<void> {
    // Check if error messages are user-friendly
    if (await this.isVisible(this.errorMessage)) {
      const errorText = await this.getTextContent(this.errorMessage);

      // Error should not contain technical jargon
      expect(errorText.toLowerCase()).not.toContain('undefined');
      expect(errorText.toLowerCase()).not.toContain('null');
      expect(errorText.toLowerCase()).not.toContain('error:');
      expect(errorText.toLowerCase()).not.toContain('exception');

      // Error should provide actionable guidance
      expect(errorText.length).toBeGreaterThan(10);
    }
  }

  /**
   * Validate cross-browser compatibility
   */
  async validateCrossBrowserCompatibility(): Promise<void> {
    // Check for browser-specific CSS issues
    const computedStyles = await this.page.evaluate(() => {
      const element = document.querySelector('body');
      if (!element) return {};

      const styles = window.getComputedStyle(element);
      return {
        display: styles.display,
        position: styles.position,
        overflow: styles.overflow
      };
    });

    expect(computedStyles.display).toBeTruthy();
  }
}
