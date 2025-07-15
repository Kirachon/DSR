import { Page, Locator, expect } from '@playwright/test';

/**
 * Component Testing Utilities
 * Specialized utilities for testing DSR enhanced components
 */

export interface ProgressIndicatorTestOptions {
  variant?: 'stepped' | 'circular' | 'linear';
  expectedSteps?: number;
  currentStep?: number;
  validateAccessibility?: boolean;
}

export interface DataTableTestOptions {
  hasSorting?: boolean;
  hasFiltering?: boolean;
  hasPagination?: boolean;
  hasSelection?: boolean;
  expectedColumns?: number;
  expectedRows?: number;
}

export interface NavigationTestOptions {
  userRole?: string;
  expectedSections?: string[];
  testCollapsible?: boolean;
  validateActiveStates?: boolean;
}

export interface TimelineTestOptions {
  expectedEvents?: number;
  hasInteractiveElements?: boolean;
  validateTimestamps?: boolean;
  testStatusUpdates?: boolean;
}

/**
 * Progress Indicator Component Test Utilities
 */
export class ProgressIndicatorTester {
  constructor(private page: Page, private locator: Locator) {}

  /**
   * Test stepped progress indicator
   */
  async testSteppedVariant(options: ProgressIndicatorTestOptions = {}): Promise<void> {
    const { expectedSteps, currentStep, validateAccessibility = true } = options;

    // Verify component is visible
    await expect(this.locator).toBeVisible();

    // Check ARIA attributes
    if (validateAccessibility) {
      await expect(this.locator).toHaveAttribute('role', 'progressbar');
      
      const ariaValueNow = await this.locator.getAttribute('aria-valuenow');
      const ariaValueMin = await this.locator.getAttribute('aria-valuemin');
      const ariaValueMax = await this.locator.getAttribute('aria-valuemax');
      
      expect(ariaValueNow).toBeTruthy();
      expect(ariaValueMin).toBe('0');
      expect(ariaValueMax).toBe('100');
    }

    // Check steps
    const steps = this.locator.locator('.step, [data-testid*="step"]');
    const stepCount = await steps.count();

    if (expectedSteps) {
      expect(stepCount).toBe(expectedSteps);
    }

    // Validate step states
    for (let i = 0; i < stepCount; i++) {
      const step = steps.nth(i);
      await expect(step).toBeVisible();

      // Check step has proper status
      const stepStatus = await step.getAttribute('data-status') || 
                        await step.getAttribute('aria-current') ||
                        'pending';
      
      expect(['pending', 'current', 'completed', 'error']).toContain(stepStatus);

      // If current step is specified, validate it
      if (currentStep !== undefined) {
        if (i < currentStep) {
          expect(['completed']).toContain(stepStatus);
        } else if (i === currentStep) {
          expect(['current']).toContain(stepStatus);
        } else {
          expect(['pending']).toContain(stepStatus);
        }
      }
    }
  }

  /**
   * Test circular progress indicator
   */
  async testCircularVariant(options: ProgressIndicatorTestOptions = {}): Promise<void> {
    const { validateAccessibility = true } = options;

    await expect(this.locator).toBeVisible();

    // Check for SVG or canvas element
    const progressElement = this.locator.locator('svg, canvas, .circular-progress');
    await expect(progressElement).toBeVisible();

    if (validateAccessibility) {
      await expect(this.locator).toHaveAttribute('role', 'progressbar');
      
      const ariaValueNow = await this.locator.getAttribute('aria-valuenow');
      expect(ariaValueNow).toBeTruthy();
      
      const progressValue = parseInt(ariaValueNow || '0');
      expect(progressValue).toBeGreaterThanOrEqual(0);
      expect(progressValue).toBeLessThanOrEqual(100);
    }

    // Check for percentage display
    const percentageText = this.locator.locator('.percentage, [data-testid*="percentage"]');
    if (await percentageText.count() > 0) {
      const percentText = await percentageText.textContent();
      expect(percentText).toMatch(/\d+%/);
    }
  }

  /**
   * Test linear progress indicator
   */
  async testLinearVariant(options: ProgressIndicatorTestOptions = {}): Promise<void> {
    const { validateAccessibility = true } = options;

    await expect(this.locator).toBeVisible();

    // Check for progress bar
    const progressBar = this.locator.locator('.progress-bar, [role="progressbar"]');
    await expect(progressBar).toBeVisible();

    if (validateAccessibility) {
      const ariaValueNow = await progressBar.getAttribute('aria-valuenow');
      expect(ariaValueNow).toBeTruthy();
    }

    // Check progress fill
    const progressFill = this.locator.locator('.progress-fill, .progress-value');
    if (await progressFill.count() > 0) {
      await expect(progressFill.first()).toBeVisible();
    }
  }

  /**
   * Test clickable steps functionality
   */
  async testClickableSteps(): Promise<void> {
    const steps = this.locator.locator('.step[data-clickable="true"], .step.clickable');
    const clickableStepCount = await steps.count();

    for (let i = 0; i < clickableStepCount; i++) {
      const step = steps.nth(i);
      
      // Check step is clickable
      await expect(step).toBeEnabled();
      
      // Test click functionality
      await step.click();
      await this.page.waitForTimeout(100); // Brief wait for any animations
      
      // Verify step received focus or active state
      const isActive = await step.evaluate(el => 
        el.matches(':focus') || 
        el.classList.contains('active') || 
        el.getAttribute('aria-current') === 'step'
      );
      
      expect(isActive).toBeTruthy();
    }
  }
}

/**
 * Data Table Component Test Utilities
 */
export class DataTableTester {
  constructor(private page: Page, private locator: Locator) {}

  /**
   * Test basic table structure
   */
  async testTableStructure(options: DataTableTestOptions = {}): Promise<void> {
    const { expectedColumns, expectedRows } = options;

    await expect(this.locator).toBeVisible();

    // Check table headers
    const headers = this.locator.locator('th, [role="columnheader"]');
    const headerCount = await headers.count();
    
    expect(headerCount).toBeGreaterThan(0);
    
    if (expectedColumns) {
      expect(headerCount).toBe(expectedColumns);
    }

    // Check table rows
    const rows = this.locator.locator('tbody tr, [role="row"]:not([role="columnheader"])');
    const rowCount = await rows.count();
    
    if (expectedRows) {
      expect(rowCount).toBe(expectedRows);
    }

    // Validate each header has text content
    for (let i = 0; i < headerCount; i++) {
      const header = headers.nth(i);
      const headerText = await header.textContent();
      expect(headerText?.trim()).toBeTruthy();
    }
  }

  /**
   * Test sorting functionality
   */
  async testSorting(): Promise<void> {
    const sortableHeaders = this.locator.locator('th[data-sortable="true"], .sortable-header');
    const sortableCount = await sortableHeaders.count();

    if (sortableCount === 0) return;

    for (let i = 0; i < Math.min(sortableCount, 2); i++) {
      const header = sortableHeaders.nth(i);
      
      // Get initial data
      const initialData = await this.getColumnData(i);
      
      // Click to sort ascending
      await header.click();
      await this.page.waitForTimeout(500);
      
      // Verify sort indicator
      const sortIndicator = header.locator('.sort-indicator, [data-testid*="sort"]');
      if (await sortIndicator.count() > 0) {
        await expect(sortIndicator.first()).toBeVisible();
      }
      
      // Click to sort descending
      await header.click();
      await this.page.waitForTimeout(500);
      
      // Get sorted data
      const sortedData = await this.getColumnData(i);
      
      // Verify data changed (basic check)
      expect(sortedData).not.toEqual(initialData);
    }
  }

  /**
   * Test filtering functionality
   */
  async testFiltering(): Promise<void> {
    const filterInputs = this.locator.locator('input[data-filter], .filter-input');
    const filterCount = await filterInputs.count();

    if (filterCount === 0) return;

    const firstFilter = filterInputs.first();
    
    // Get initial row count
    const initialRowCount = await this.getRowCount();
    
    // Apply filter
    await firstFilter.fill('test');
    await this.page.keyboard.press('Enter');
    await this.page.waitForTimeout(500);
    
    // Verify filtering occurred (rows changed or loading state)
    const filteredRowCount = await this.getRowCount();
    
    // Clear filter
    await firstFilter.clear();
    await this.page.keyboard.press('Enter');
    await this.page.waitForTimeout(500);
  }

  /**
   * Test pagination functionality
   */
  async testPagination(): Promise<void> {
    const paginationContainer = this.page.locator('.pagination, [data-testid*="pagination"]');
    
    if (!(await paginationContainer.count() > 0)) return;

    // Test next page
    const nextButton = paginationContainer.locator('.next, [data-testid*="next"]');
    if (await nextButton.count() > 0 && await nextButton.first().isEnabled()) {
      const initialData = await this.getTableData();
      
      await nextButton.first().click();
      await this.page.waitForTimeout(500);
      
      const newData = await this.getTableData();
      expect(newData).not.toEqual(initialData);
    }

    // Test previous page
    const prevButton = paginationContainer.locator('.prev, [data-testid*="prev"]');
    if (await prevButton.count() > 0 && await prevButton.first().isEnabled()) {
      await prevButton.first().click();
      await this.page.waitForTimeout(500);
    }
  }

  /**
   * Test row selection functionality
   */
  async testRowSelection(): Promise<void> {
    const checkboxes = this.locator.locator('input[type="checkbox"]');
    const checkboxCount = await checkboxes.count();

    if (checkboxCount === 0) return;

    // Test individual row selection
    const firstRowCheckbox = checkboxes.first();
    await firstRowCheckbox.check();
    await expect(firstRowCheckbox).toBeChecked();

    // Test select all if present
    const selectAllCheckbox = this.locator.locator('thead input[type="checkbox"]');
    if (await selectAllCheckbox.count() > 0) {
      await selectAllCheckbox.first().check();
      
      // Verify all checkboxes are checked
      const allCheckboxes = this.locator.locator('tbody input[type="checkbox"]');
      const allCount = await allCheckboxes.count();
      
      for (let i = 0; i < allCount; i++) {
        await expect(allCheckboxes.nth(i)).toBeChecked();
      }
    }
  }

  /**
   * Get column data for sorting verification
   */
  private async getColumnData(columnIndex: number): Promise<string[]> {
    const cells = this.locator.locator(`tbody tr td:nth-child(${columnIndex + 1})`);
    const cellCount = await cells.count();
    const data: string[] = [];

    for (let i = 0; i < cellCount; i++) {
      const cellText = await cells.nth(i).textContent();
      data.push(cellText?.trim() || '');
    }

    return data;
  }

  /**
   * Get current row count
   */
  private async getRowCount(): Promise<number> {
    const rows = this.locator.locator('tbody tr');
    return await rows.count();
  }

  /**
   * Get all table data
   */
  private async getTableData(): Promise<string[][]> {
    const rows = this.locator.locator('tbody tr');
    const rowCount = await rows.count();
    const data: string[][] = [];

    for (let i = 0; i < rowCount; i++) {
      const cells = rows.nth(i).locator('td');
      const cellCount = await cells.count();
      const rowData: string[] = [];

      for (let j = 0; j < cellCount; j++) {
        const cellText = await cells.nth(j).textContent();
        rowData.push(cellText?.trim() || '');
      }

      data.push(rowData);
    }

    return data;
  }
}

/**
 * Role-Based Navigation Component Test Utilities
 */
export class NavigationTester {
  constructor(private page: Page, private locator: Locator) {}

  /**
   * Test navigation structure and visibility
   */
  async testNavigationStructure(options: NavigationTestOptions = {}): Promise<void> {
    const { userRole, expectedSections, validateActiveStates = true } = options;

    await expect(this.locator).toBeVisible();

    // Check navigation sections
    const sections = this.locator.locator('.nav-section, [data-testid*="nav-section"]');
    const sectionCount = await sections.count();

    if (expectedSections) {
      expect(sectionCount).toBe(expectedSections.length);
    }

    // Validate each section
    for (let i = 0; i < sectionCount; i++) {
      const section = sections.nth(i);
      await expect(section).toBeVisible();

      // Check section has navigation items
      const navItems = section.locator('a, button, [role="menuitem"]');
      const itemCount = await navItems.count();
      expect(itemCount).toBeGreaterThan(0);
    }

    // Test role-based visibility
    if (userRole) {
      await this.validateRoleBasedVisibility(userRole);
    }

    // Test active states
    if (validateActiveStates) {
      await this.validateActiveStates();
    }
  }

  /**
   * Test collapsible navigation behavior
   */
  async testCollapsibleBehavior(): Promise<void> {
    const toggleButton = this.locator.locator('.nav-toggle, [data-testid*="nav-toggle"]');

    if (await toggleButton.count() === 0) return;

    // Test collapse
    await toggleButton.first().click();
    await this.page.waitForTimeout(300);

    // Check if navigation is collapsed
    const isCollapsed = await this.locator.evaluate(el =>
      el.classList.contains('collapsed') ||
      el.getAttribute('aria-expanded') === 'false'
    );

    expect(isCollapsed).toBeTruthy();

    // Test expand
    await toggleButton.first().click();
    await this.page.waitForTimeout(300);

    const isExpanded = await this.locator.evaluate(el =>
      !el.classList.contains('collapsed') ||
      el.getAttribute('aria-expanded') === 'true'
    );

    expect(isExpanded).toBeTruthy();
  }

  /**
   * Test navigation item interactions
   */
  async testNavigationInteractions(): Promise<void> {
    const navItems = this.locator.locator('a, button, [role="menuitem"]');
    const itemCount = await navItems.count();

    // Test first few navigation items
    for (let i = 0; i < Math.min(itemCount, 3); i++) {
      const item = navItems.nth(i);

      if (await item.isVisible() && await item.isEnabled()) {
        // Test hover state
        await item.hover();
        await this.page.waitForTimeout(100);

        // Test focus state
        await item.focus();
        await this.page.waitForTimeout(100);

        // Verify focus is visible
        const hasFocus = await item.evaluate(el => el.matches(':focus'));
        expect(hasFocus).toBeTruthy();
      }
    }
  }

  /**
   * Validate role-based visibility
   */
  private async validateRoleBasedVisibility(userRole: string): Promise<void> {
    const roleSpecificItems = this.locator.locator('[data-role], [data-user-role]');
    const roleItemCount = await roleSpecificItems.count();

    for (let i = 0; i < roleItemCount; i++) {
      const item = roleSpecificItems.nth(i);
      const itemRole = await item.getAttribute('data-role') ||
                      await item.getAttribute('data-user-role');

      if (itemRole && itemRole !== userRole) {
        await expect(item).not.toBeVisible();
      } else if (itemRole === userRole) {
        await expect(item).toBeVisible();
      }
    }
  }

  /**
   * Validate active states
   */
  private async validateActiveStates(): Promise<void> {
    const activeItems = this.locator.locator('.active, [aria-current="page"]');
    const activeCount = await activeItems.count();

    // Should have at least one active item
    expect(activeCount).toBeGreaterThanOrEqual(0);

    // Validate active items are properly styled
    for (let i = 0; i < activeCount; i++) {
      const activeItem = activeItems.nth(i);
      await expect(activeItem).toBeVisible();

      const ariaCurrent = await activeItem.getAttribute('aria-current');
      const hasActiveClass = await activeItem.evaluate(el => el.classList.contains('active'));

      expect(ariaCurrent === 'page' || hasActiveClass).toBeTruthy();
    }
  }
}

/**
 * Workflow Timeline Component Test Utilities
 */
export class TimelineTester {
  constructor(private page: Page, private locator: Locator) {}

  /**
   * Test timeline structure and events
   */
  async testTimelineStructure(options: TimelineTestOptions = {}): Promise<void> {
    const { expectedEvents, validateTimestamps = true } = options;

    await expect(this.locator).toBeVisible();

    // Check timeline events
    const events = this.locator.locator('.timeline-event, [data-testid*="timeline-event"]');
    const eventCount = await events.count();

    if (expectedEvents) {
      expect(eventCount).toBe(expectedEvents);
    } else {
      expect(eventCount).toBeGreaterThan(0);
    }

    // Validate each event
    for (let i = 0; i < eventCount; i++) {
      const event = events.nth(i);
      await expect(event).toBeVisible();

      // Check event has title
      const title = event.locator('.event-title, [data-testid*="title"]');
      if (await title.count() > 0) {
        await expect(title.first()).toBeVisible();
        const titleText = await title.first().textContent();
        expect(titleText?.trim()).toBeTruthy();
      }

      // Check event has status
      const status = event.locator('.event-status, [data-testid*="status"]');
      if (await status.count() > 0) {
        await expect(status.first()).toBeVisible();
      }

      // Validate timestamps
      if (validateTimestamps) {
        const timestamp = event.locator('.event-timestamp, [data-testid*="timestamp"]');
        if (await timestamp.count() > 0) {
          const timestampText = await timestamp.first().textContent();
          expect(timestampText?.trim()).toBeTruthy();
        }
      }
    }
  }

  /**
   * Test interactive timeline elements
   */
  async testInteractiveElements(): Promise<void> {
    const interactiveEvents = this.locator.locator('.timeline-event[data-interactive="true"], .interactive-event');
    const interactiveCount = await interactiveEvents.count();

    for (let i = 0; i < interactiveCount; i++) {
      const event = interactiveEvents.nth(i);

      // Test click interaction
      await event.click();
      await this.page.waitForTimeout(200);

      // Check if details panel or modal opened
      const detailsPanel = this.page.locator('.event-details, [data-testid*="event-details"]');
      const modal = this.page.locator('.modal, [role="dialog"]');

      if (await detailsPanel.count() > 0) {
        await expect(detailsPanel.first()).toBeVisible();
      } else if (await modal.count() > 0) {
        await expect(modal.first()).toBeVisible();

        // Close modal
        const closeButton = modal.locator('.close, [data-testid*="close"]');
        if (await closeButton.count() > 0) {
          await closeButton.first().click();
        }
      }
    }
  }

  /**
   * Test timeline status progression
   */
  async testStatusProgression(): Promise<void> {
    const events = this.locator.locator('.timeline-event');
    const eventCount = await events.count();

    let hasCompleted = false;
    let hasCurrent = false;
    let foundCurrentIndex = -1;

    for (let i = 0; i < eventCount; i++) {
      const event = events.nth(i);
      const status = await event.getAttribute('data-status') ||
                    await event.evaluate(el => {
                      if (el.classList.contains('completed')) return 'completed';
                      if (el.classList.contains('current')) return 'current';
                      if (el.classList.contains('pending')) return 'pending';
                      return 'unknown';
                    });

      if (status === 'completed') {
        hasCompleted = true;
        // All events before current should be completed
        if (foundCurrentIndex >= 0) {
          expect(i).toBeLessThan(foundCurrentIndex);
        }
      } else if (status === 'current') {
        hasCurrent = true;
        foundCurrentIndex = i;
      } else if (status === 'pending') {
        // All pending events should be after current
        if (foundCurrentIndex >= 0) {
          expect(i).toBeGreaterThan(foundCurrentIndex);
        }
      }
    }

    // Timeline should have logical progression
    if (eventCount > 1) {
      expect(hasCompleted || hasCurrent).toBeTruthy();
    }
  }

  /**
   * Test timeline accessibility
   */
  async testTimelineAccessibility(): Promise<void> {
    // Check timeline has proper ARIA attributes
    const timelineRole = await this.locator.getAttribute('role');
    expect(['list', 'timeline', 'log']).toContain(timelineRole);

    // Check events have proper structure
    const events = this.locator.locator('.timeline-event');
    const eventCount = await events.count();

    for (let i = 0; i < eventCount; i++) {
      const event = events.nth(i);

      // Check event has proper role
      const eventRole = await event.getAttribute('role');
      if (eventRole) {
        expect(['listitem', 'article']).toContain(eventRole);
      }

      // Check event has accessible name
      const ariaLabel = await event.getAttribute('aria-label');
      const ariaLabelledBy = await event.getAttribute('aria-labelledby');

      expect(ariaLabel || ariaLabelledBy).toBeTruthy();
    }
  }
}
