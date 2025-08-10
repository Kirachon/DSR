import { Page, Locator, expect } from '@playwright/test';
import { BasePage } from '../base-page';

/**
 * Staff Dashboard Page Object
 * Handles LGU and DSWD staff dashboard interactions and validations
 */
export class StaffDashboardPage extends BasePage {
  // Staff-specific locators
  private readonly dashboardHeader: Locator;
  private readonly kpiCards: Locator;
  private readonly workQueue: Locator;
  private readonly pendingRegistrations: Locator;
  private readonly assignedCases: Locator;
  private readonly recentPayments: Locator;
  private readonly analyticsSection: Locator;
  private readonly actionButtons: Locator;
  private readonly searchFilters: Locator;
  private readonly dataTable: Locator;
  private readonly bulkActions: Locator;

  // Staff action buttons
  private readonly reviewApplicationButton: Locator;
  private readonly approveRegistrationButton: Locator;
  private readonly assignCaseButton: Locator;
  private readonly generateReportButton: Locator;
  private readonly exportDataButton: Locator;
  private readonly viewAnalyticsButton: Locator;

  // Data management elements
  private readonly sortControls: Locator;
  private readonly filterControls: Locator;
  private readonly paginationControls: Locator;
  private readonly searchInput: Locator;
  private readonly statusFilter: Locator;
  private readonly dateRangeFilter: Locator;

  // Modal and dialog elements
  private readonly reviewModal: Locator;
  private readonly confirmationDialog: Locator;
  private readonly detailsPanel: Locator;

  constructor(page: Page) {
    super(page);
    
    // Initialize staff dashboard locators
    this.dashboardHeader = page.locator('[data-testid="dashboard-header"]');
    this.kpiCards = page.locator('[data-testid="kpi-cards"]');
    this.workQueue = page.locator('[data-testid="work-queue"]');
    this.pendingRegistrations = page.locator('[data-testid="pending-registrations"]');
    this.assignedCases = page.locator('[data-testid="assigned-cases"]');
    this.recentPayments = page.locator('[data-testid="recent-payments"]');
    this.analyticsSection = page.locator('[data-testid="analytics-section"]');
    this.actionButtons = page.locator('[data-testid="action-buttons"]');
    this.searchFilters = page.locator('[data-testid="search-filters"]');
    this.dataTable = page.locator('[data-testid="data-table"]');
    this.bulkActions = page.locator('[data-testid="bulk-actions"]');

    // Staff action buttons
    this.reviewApplicationButton = page.locator('[data-testid="review-application"]');
    this.approveRegistrationButton = page.locator('[data-testid="approve-registration"]');
    this.assignCaseButton = page.locator('[data-testid="assign-case"]');
    this.generateReportButton = page.locator('[data-testid="generate-report"]');
    this.exportDataButton = page.locator('[data-testid="export-data"]');
    this.viewAnalyticsButton = page.locator('[data-testid="view-analytics"]');

    // Data management elements
    this.sortControls = page.locator('[data-testid="sort-controls"]');
    this.filterControls = page.locator('[data-testid="filter-controls"]');
    this.paginationControls = page.locator('[data-testid="pagination-controls"]');
    this.searchInput = page.locator('[data-testid="search-input"]');
    this.statusFilter = page.locator('[data-testid="status-filter"]');
    this.dateRangeFilter = page.locator('[data-testid="date-range-filter"]');

    // Modal and dialog elements
    this.reviewModal = page.locator('[data-testid="review-modal"]');
    this.confirmationDialog = page.locator('[data-testid="confirmation-dialog"]');
    this.detailsPanel = page.locator('[data-testid="details-panel"]');
  }

  /**
   * Navigate to staff dashboard
   */
  async goto(): Promise<void> {
    await this.page.goto(`${this.baseUrl}/dashboard`);
    await this.waitForPageLoad();
  }

  /**
   * Validate staff dashboard is loaded correctly
   */
  async validateDashboardLoaded(): Promise<void> {
    await expect(this.dashboardHeader).toBeVisible();
    await expect(this.kpiCards).toBeVisible();
    await expect(this.workQueue).toBeVisible();
  }

  /**
   * Validate KPI cards display correct information
   */
  async validateKPICards(): Promise<void> {
    await expect(this.kpiCards).toBeVisible();
    
    const cards = this.kpiCards.locator('.kpi-card, [data-testid*="kpi"]');
    const cardCount = await cards.count();
    
    expect(cardCount).toBeGreaterThan(0);
    
    for (let i = 0; i < cardCount; i++) {
      const card = cards.nth(i);
      await expect(card).toBeVisible();
      
      // Check card has title and value
      const title = card.locator('.kpi-title, [data-testid*="title"]');
      const value = card.locator('.kpi-value, [data-testid*="value"]');
      
      await expect(title).toBeVisible();
      await expect(value).toBeVisible();
      
      const valueText = await value.textContent();
      expect(valueText?.trim()).toBeTruthy();
    }
  }

  /**
   * Validate work queue functionality
   */
  async validateWorkQueue(): Promise<void> {
    await expect(this.workQueue).toBeVisible();
    
    // Check for pending items
    const queueItems = this.workQueue.locator('.queue-item, [data-testid*="queue-item"]');
    const itemCount = await queueItems.count();
    
    for (let i = 0; i < itemCount; i++) {
      const item = queueItems.nth(i);
      await expect(item).toBeVisible();
      
      // Check item has status badge
      const statusBadge = item.locator('[role="status"]');
      if (await statusBadge.count() > 0) {
        await expect(statusBadge.first()).toBeVisible();
      }
    }
  }

  /**
   * Validate pending registrations section
   */
  async validatePendingRegistrations(): Promise<void> {
    await expect(this.pendingRegistrations).toBeVisible();
    
    const registrations = this.pendingRegistrations.locator('.registration-item, [data-testid*="registration"]');
    const regCount = await registrations.count();
    
    for (let i = 0; i < regCount; i++) {
      const registration = registrations.nth(i);
      await expect(registration).toBeVisible();
    }
  }

  /**
   * Validate assigned cases section
   */
  async validateAssignedCases(): Promise<void> {
    if (await this.isVisible(this.assignedCases)) {
      await expect(this.assignedCases).toBeVisible();
      
      const cases = this.assignedCases.locator('.case-item, [data-testid*="case"]');
      const caseCount = await cases.count();
      
      for (let i = 0; i < caseCount; i++) {
        const caseItem = cases.nth(i);
        await expect(caseItem).toBeVisible();
      }
    }
  }

  /**
   * Validate recent payments section
   */
  async validateRecentPayments(): Promise<void> {
    if (await this.isVisible(this.recentPayments)) {
      await expect(this.recentPayments).toBeVisible();
      
      const payments = this.recentPayments.locator('.payment-item, [data-testid*="payment"]');
      const paymentCount = await payments.count();
      
      for (let i = 0; i < paymentCount; i++) {
        const payment = payments.nth(i);
        await expect(payment).toBeVisible();
      }
    }
  }

  /**
   * Test data table functionality
   */
  async testDataTableFunctionality(): Promise<void> {
    if (await this.isVisible(this.dataTable)) {
      await expect(this.dataTable).toBeVisible();
      
      // Test sorting
      const sortableHeaders = this.dataTable.locator('th[data-sortable="true"], .sortable-header');
      const headerCount = await sortableHeaders.count();
      
      if (headerCount > 0) {
        const firstHeader = sortableHeaders.first();
        await firstHeader.click();
        await this.waitForLoadingToComplete();
        
        // Click again to test reverse sort
        await firstHeader.click();
        await this.waitForLoadingToComplete();
      }
      
      // Test pagination if present
      if (await this.isVisible(this.paginationControls)) {
        const nextButton = this.paginationControls.locator('[data-testid="next-page"], .next-page');
        if (await nextButton.count() > 0 && await nextButton.first().isEnabled()) {
          await nextButton.first().click();
          await this.waitForLoadingToComplete();
        }
      }
    }
  }

  /**
   * Test search and filter functionality
   */
  async testSearchAndFilters(): Promise<void> {
    // Test search input
    if (await this.isVisible(this.searchInput)) {
      await this.fillField(this.searchInput, 'test search');
      await this.page.keyboard.press('Enter');
      await this.waitForLoadingToComplete();
      
      // Clear search
      await this.searchInput.clear();
      await this.page.keyboard.press('Enter');
      await this.waitForLoadingToComplete();
    }
    
    // Test status filter
    if (await this.isVisible(this.statusFilter)) {
      const options = this.statusFilter.locator('option');
      const optionCount = await options.count();
      
      if (optionCount > 1) {
        await this.selectOption(this.statusFilter, await options.nth(1).getAttribute('value') || '');
        await this.waitForLoadingToComplete();
      }
    }
  }

  /**
   * Test bulk actions functionality
   */
  async testBulkActions(): Promise<void> {
    if (await this.isVisible(this.bulkActions)) {
      // Select multiple items
      const checkboxes = this.dataTable.locator('input[type="checkbox"]');
      const checkboxCount = await checkboxes.count();
      
      if (checkboxCount > 1) {
        // Select first two checkboxes
        await checkboxes.nth(0).check();
        await checkboxes.nth(1).check();
        
        // Check if bulk actions are enabled
        await expect(this.bulkActions).toBeVisible();
        
        const bulkActionButtons = this.bulkActions.locator('button');
        const buttonCount = await bulkActionButtons.count();
        
        if (buttonCount > 0) {
          const firstButton = bulkActionButtons.first();
          if (await firstButton.isEnabled()) {
            await firstButton.click();
            
            // Handle confirmation dialog if present
            if (await this.isVisible(this.confirmationDialog)) {
              const confirmButton = this.confirmationDialog.locator('[data-testid="confirm"], .confirm-button');
              if (await confirmButton.count() > 0) {
                await confirmButton.first().click();
              }
            }
            
            await this.waitForLoadingToComplete();
          }
        }
      }
    }
  }

  /**
   * Click review application button
   */
  async clickReviewApplication(): Promise<void> {
    await this.clickButton(this.reviewApplicationButton, true);
  }

  /**
   * Click approve registration button
   */
  async clickApproveRegistration(): Promise<void> {
    await this.clickButton(this.approveRegistrationButton, true);
  }

  /**
   * Click assign case button
   */
  async clickAssignCase(): Promise<void> {
    await this.clickButton(this.assignCaseButton, true);
  }

  /**
   * Click generate report button
   */
  async clickGenerateReport(): Promise<void> {
    await this.clickButton(this.generateReportButton, true);
  }

  /**
   * Click export data button
   */
  async clickExportData(): Promise<void> {
    await this.clickButton(this.exportDataButton, true);
  }

  /**
   * Click view analytics button
   */
  async clickViewAnalytics(): Promise<void> {
    await this.clickButton(this.viewAnalyticsButton, true);
  }

  /**
   * Validate role-based permissions for staff
   */
  async validateStaffPermissions(userRole: 'LGU_STAFF' | 'DSWD_STAFF'): Promise<void> {
    await this.validateRoleBasedContent(userRole);
    
    // Check role-specific action buttons
    if (userRole === 'LGU_STAFF') {
      // LGU staff should see local registration and case management
      await expect(this.reviewApplicationButton).toBeVisible();
      await expect(this.assignCaseButton).toBeVisible();
    } else if (userRole === 'DSWD_STAFF') {
      // DSWD staff should see payment and analytics functions
      await expect(this.generateReportButton).toBeVisible();
      await expect(this.viewAnalyticsButton).toBeVisible();
    }
  }

  /**
   * Test responsive design for staff dashboard
   */
  async testResponsiveDesign(): Promise<void> {
    const viewports = [
      { width: 768, height: 1024, name: 'tablet' },
      { width: 1920, height: 1080, name: 'desktop' },
      { width: 2560, height: 1440, name: 'large-desktop' }
    ];

    await super.testResponsiveDesign(viewports);
  }

  /**
   * Validate staff-specific accessibility features
   */
  async validateAccessibility(): Promise<void> {
    await this.runAccessibilityTests({
      includeTags: ['wcag2a', 'wcag2aa'],
      rules: {
        'color-contrast': { enabled: true },
        'keyboard-navigation': { enabled: true },
        'focus-management': { enabled: true },
        'table-headers': { enabled: true }
      }
    });
  }
}
