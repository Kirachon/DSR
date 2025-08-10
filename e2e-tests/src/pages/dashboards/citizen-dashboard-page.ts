import { Page, Locator, expect } from '@playwright/test';
import { BasePage } from '../base-page';

/**
 * Citizen Dashboard Page Object
 * Handles citizen-specific dashboard interactions and validations
 */
export class CitizenDashboardPage extends BasePage {
  // Citizen-specific locators
  private readonly welcomeMessage: Locator;
  private readonly profileCompleteness: Locator;
  private readonly quickActions: Locator;
  private readonly applicationStatus: Locator;
  private readonly benefitCards: Locator;
  private readonly notificationPanel: Locator;
  private readonly journeyTimeline: Locator;
  private readonly registrationProgress: Locator;
  private readonly eligibilityStatus: Locator;
  private readonly paymentHistory: Locator;

  // Quick action buttons
  private readonly startRegistrationButton: Locator;
  private readonly checkEligibilityButton: Locator;
  private readonly viewPaymentsButton: Locator;
  private readonly submitGrievanceButton: Locator;
  private readonly updateProfileButton: Locator;

  // Dashboard widgets
  private readonly statusWidget: Locator;
  private readonly progressWidget: Locator;
  private readonly notificationWidget: Locator;
  private readonly activityWidget: Locator;

  constructor(page: Page) {
    super(page);
    
    // Initialize citizen dashboard locators
    this.welcomeMessage = page.locator('[data-testid="welcome-message"]');
    this.profileCompleteness = page.locator('[data-testid="profile-completeness"]');
    this.quickActions = page.locator('[data-testid="quick-actions"]');
    this.applicationStatus = page.locator('[data-testid="application-status"]');
    this.benefitCards = page.locator('[data-testid="benefit-cards"]');
    this.notificationPanel = page.locator('[data-testid="notification-panel"]');
    this.journeyTimeline = page.locator('[data-testid="journey-timeline"]');
    this.registrationProgress = page.locator('[data-testid="registration-progress"]');
    this.eligibilityStatus = page.locator('[data-testid="eligibility-status"]');
    this.paymentHistory = page.locator('[data-testid="payment-history"]');

    // Quick action buttons
    this.startRegistrationButton = page.locator('[data-testid="start-registration"]');
    this.checkEligibilityButton = page.locator('[data-testid="check-eligibility"]');
    this.viewPaymentsButton = page.locator('[data-testid="view-payments"]');
    this.submitGrievanceButton = page.locator('[data-testid="submit-grievance"]');
    this.updateProfileButton = page.locator('[data-testid="update-profile"]');

    // Dashboard widgets
    this.statusWidget = page.locator('[data-testid="status-widget"]');
    this.progressWidget = page.locator('[data-testid="progress-widget"]');
    this.notificationWidget = page.locator('[data-testid="notification-widget"]');
    this.activityWidget = page.locator('[data-testid="activity-widget"]');
  }

  /**
   * Navigate to citizen dashboard
   */
  async goto(): Promise<void> {
    await this.page.goto(`${this.baseUrl}/dashboard`);
    await this.waitForPageLoad();
  }

  /**
   * Validate citizen dashboard is loaded correctly
   */
  async validateDashboardLoaded(): Promise<void> {
    await expect(this.welcomeMessage).toBeVisible();
    await expect(this.quickActions).toBeVisible();
    await expect(this.applicationStatus).toBeVisible();
  }

  /**
   * Validate welcome message shows correct user information
   */
  async validateWelcomeMessage(expectedName: string): Promise<void> {
    await expect(this.welcomeMessage).toContainText(expectedName);
  }

  /**
   * Validate profile completeness indicator
   */
  async validateProfileCompleteness(): Promise<number> {
    await expect(this.profileCompleteness).toBeVisible();
    
    const progressText = await this.profileCompleteness.textContent();
    const progressMatch = progressText?.match(/(\d+)%/);
    const progressValue = progressMatch ? parseInt(progressMatch[1]) : 0;
    
    expect(progressValue).toBeGreaterThanOrEqual(0);
    expect(progressValue).toBeLessThanOrEqual(100);
    
    return progressValue;
  }

  /**
   * Validate quick actions are available and functional
   */
  async validateQuickActions(): Promise<void> {
    await expect(this.quickActions).toBeVisible();
    
    // Check all quick action buttons are present
    await expect(this.startRegistrationButton).toBeVisible();
    await expect(this.checkEligibilityButton).toBeVisible();
    await expect(this.viewPaymentsButton).toBeVisible();
    await expect(this.submitGrievanceButton).toBeVisible();
    await expect(this.updateProfileButton).toBeVisible();
  }

  /**
   * Click start registration button
   */
  async clickStartRegistration(): Promise<void> {
    await this.clickButton(this.startRegistrationButton, true);
  }

  /**
   * Click check eligibility button
   */
  async clickCheckEligibility(): Promise<void> {
    await this.clickButton(this.checkEligibilityButton, true);
  }

  /**
   * Click view payments button
   */
  async clickViewPayments(): Promise<void> {
    await this.clickButton(this.viewPaymentsButton, true);
  }

  /**
   * Click submit grievance button
   */
  async clickSubmitGrievance(): Promise<void> {
    await this.clickButton(this.submitGrievanceButton, true);
  }

  /**
   * Click update profile button
   */
  async clickUpdateProfile(): Promise<void> {
    await this.clickButton(this.updateProfileButton, true);
  }

  /**
   * Validate application status section
   */
  async validateApplicationStatus(): Promise<void> {
    await expect(this.applicationStatus).toBeVisible();
    
    // Check for status badges
    const statusBadges = this.applicationStatus.locator('[role="status"]');
    const badgeCount = await statusBadges.count();
    
    if (badgeCount > 0) {
      for (let i = 0; i < badgeCount; i++) {
        const badge = statusBadges.nth(i);
        await expect(badge).toBeVisible();
        
        const badgeText = await badge.textContent();
        expect(badgeText?.trim()).toBeTruthy();
      }
    }
  }

  /**
   * Validate benefit cards section
   */
  async validateBenefitCards(): Promise<void> {
    await expect(this.benefitCards).toBeVisible();
    
    const cards = this.benefitCards.locator('.card, [data-testid*="card"]');
    const cardCount = await cards.count();
    
    for (let i = 0; i < cardCount; i++) {
      const card = cards.nth(i);
      await expect(card).toBeVisible();
    }
  }

  /**
   * Validate notification panel
   */
  async validateNotificationPanel(): Promise<void> {
    if (await this.isVisible(this.notificationPanel)) {
      const notifications = this.notificationPanel.locator('.notification, [data-testid*="notification"]');
      const notificationCount = await notifications.count();
      
      for (let i = 0; i < notificationCount; i++) {
        const notification = notifications.nth(i);
        await expect(notification).toBeVisible();
        
        const notificationText = await notification.textContent();
        expect(notificationText?.trim()).toBeTruthy();
      }
    }
  }

  /**
   * Validate journey timeline
   */
  async validateJourneyTimeline(): Promise<void> {
    if (await this.isVisible(this.journeyTimeline)) {
      await expect(this.journeyTimeline).toBeVisible();
      
      // Check for timeline events
      const timelineEvents = this.journeyTimeline.locator('.timeline-event, [data-testid*="timeline-event"]');
      const eventCount = await timelineEvents.count();
      
      expect(eventCount).toBeGreaterThan(0);
      
      for (let i = 0; i < eventCount; i++) {
        const event = timelineEvents.nth(i);
        await expect(event).toBeVisible();
      }
    }
  }

  /**
   * Validate registration progress
   */
  async validateRegistrationProgress(): Promise<void> {
    if (await this.isVisible(this.registrationProgress)) {
      await expect(this.registrationProgress).toBeVisible();
      
      // Check for progress indicator
      const progressIndicator = this.registrationProgress.locator('[role="progressbar"]');
      if (await progressIndicator.count() > 0) {
        await expect(progressIndicator.first()).toBeVisible();
        
        const ariaValueNow = await progressIndicator.first().getAttribute('aria-valuenow');
        expect(ariaValueNow).toBeTruthy();
      }
    }
  }

  /**
   * Validate eligibility status
   */
  async validateEligibilityStatus(): Promise<void> {
    if (await this.isVisible(this.eligibilityStatus)) {
      await expect(this.eligibilityStatus).toBeVisible();
      
      const statusText = await this.eligibilityStatus.textContent();
      expect(statusText?.trim()).toBeTruthy();
    }
  }

  /**
   * Validate payment history
   */
  async validatePaymentHistory(): Promise<void> {
    if (await this.isVisible(this.paymentHistory)) {
      await expect(this.paymentHistory).toBeVisible();
      
      // Check for payment entries
      const paymentEntries = this.paymentHistory.locator('.payment-entry, [data-testid*="payment"]');
      const entryCount = await paymentEntries.count();
      
      for (let i = 0; i < entryCount; i++) {
        const entry = paymentEntries.nth(i);
        await expect(entry).toBeVisible();
      }
    }
  }

  /**
   * Validate all dashboard widgets
   */
  async validateDashboardWidgets(): Promise<void> {
    const widgets = [
      this.statusWidget,
      this.progressWidget,
      this.notificationWidget,
      this.activityWidget
    ];

    for (const widget of widgets) {
      if (await this.isVisible(widget)) {
        await expect(widget).toBeVisible();
      }
    }
  }

  /**
   * Test responsive design for citizen dashboard
   */
  async testResponsiveDesign(): Promise<void> {
    const viewports = [
      { width: 320, height: 568, name: 'mobile' },
      { width: 768, height: 1024, name: 'tablet' },
      { width: 1920, height: 1080, name: 'desktop' }
    ];

    await super.testResponsiveDesign(viewports);
  }

  /**
   * Validate citizen-specific accessibility features
   */
  async validateAccessibility(): Promise<void> {
    await this.runAccessibilityTests({
      includeTags: ['wcag2a', 'wcag2aa'],
      rules: {
        'color-contrast': { enabled: true },
        'keyboard-navigation': { enabled: true },
        'focus-management': { enabled: true }
      }
    });
  }
}
