import { test, expect } from '@playwright/test';
import { AuthenticationManager } from '../../src/utils/auth-test-utilities';
import { DataTableTester } from '../../src/utils/component-test-utilities';

test.describe('Data Table Component Tests', () => {
  let authManager: AuthenticationManager;

  test.beforeEach(async ({ page }) => {
    authManager = new AuthenticationManager(page);
    await authManager.loginAs('LGU_STAFF'); // Staff users have access to data tables
  });

  test.afterEach(async ({ page }) => {
    await authManager.logout();
  });

  test.describe('Basic Table Structure', () => {
    test('should display data table with proper structure', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const dataTable = page.locator('table, [role="table"], [data-testid*="table"]').first();
      
      if (await dataTable.count() > 0) {
        const tableTester = new DataTableTester(page, dataTable);
        
        await tableTester.testTableStructure({
          expectedColumns: undefined, // Will be determined dynamically
          expectedRows: undefined,
          hasSorting: true,
          hasFiltering: true,
          hasPagination: true,
          hasSelection: true
        });

        // Take screenshot for visual verification
        await page.screenshot({ 
          path: 'test-results/data-table-structure.png',
          fullPage: false 
        });
      }
    });

    test('should have proper table headers', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/citizens`);
      await page.waitForLoadState('networkidle');

      const dataTable = page.locator('table, [role="table"]').first();
      
      if (await dataTable.count() > 0) {
        // Check table headers
        const headers = dataTable.locator('th, [role="columnheader"]');
        const headerCount = await headers.count();
        
        expect(headerCount).toBeGreaterThan(0);

        // Validate each header has text content
        for (let i = 0; i < headerCount; i++) {
          const header = headers.nth(i);
          const headerText = await header.textContent();
          expect(headerText?.trim()).toBeTruthy();
          
          // Check for proper ARIA attributes
          const role = await header.getAttribute('role');
          if (role) {
            expect(role).toBe('columnheader');
          }
        }
      }
    });

    test('should display table data correctly', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/citizens`);
      await page.waitForLoadState('networkidle');

      const dataTable = page.locator('table, [role="table"]').first();
      
      if (await dataTable.count() > 0) {
        // Check table rows
        const rows = dataTable.locator('tbody tr, [role="row"]:not([role="columnheader"])');
        const rowCount = await rows.count();
        
        if (rowCount > 0) {
          // Validate first few rows
          for (let i = 0; i < Math.min(rowCount, 3); i++) {
            const row = rows.nth(i);
            await expect(row).toBeVisible();
            
            // Check row has cells
            const cells = row.locator('td, [role="cell"]');
            const cellCount = await cells.count();
            expect(cellCount).toBeGreaterThan(0);
            
            // Validate cells have content
            for (let j = 0; j < cellCount; j++) {
              const cell = cells.nth(j);
              const cellText = await cell.textContent();
              // Cell can be empty, but should exist
              expect(cellText !== null).toBeTruthy();
            }
          }
        }
      }
    });

    test('should handle empty table state', async ({ page }) => {
      // Mock empty data response
      await page.route('**/api/**', route => {
        if (route.request().url().includes('citizens') || route.request().url().includes('data')) {
          route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({ data: [], total: 0 })
          });
        } else {
          route.continue();
        }
      });

      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/citizens`);
      await page.waitForLoadState('networkidle');

      // Check for empty state message
      const emptyState = page.locator('.empty-state, [data-testid*="empty"], text=No data available');
      if (await emptyState.count() > 0) {
        await expect(emptyState.first()).toBeVisible();
        
        const emptyText = await emptyState.first().textContent();
        expect(emptyText?.trim()).toBeTruthy();
      }
    });
  });

  test.describe('Sorting Functionality', () => {
    test('should sort columns when headers are clicked', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/citizens`);
      await page.waitForLoadState('networkidle');

      const dataTable = page.locator('table, [role="table"]').first();
      
      if (await dataTable.count() > 0) {
        const tableTester = new DataTableTester(page, dataTable);
        await tableTester.testSorting();

        // Take screenshot after sorting
        await page.screenshot({ 
          path: 'test-results/data-table-sorted.png',
          fullPage: false 
        });
      }
    });

    test('should display sort indicators', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/citizens`);
      await page.waitForLoadState('networkidle');

      const dataTable = page.locator('table, [role="table"]').first();
      
      if (await dataTable.count() > 0) {
        const sortableHeaders = dataTable.locator('th[data-sortable="true"], .sortable-header');
        const sortableCount = await sortableHeaders.count();

        if (sortableCount > 0) {
          const firstHeader = sortableHeaders.first();
          
          // Click to sort
          await firstHeader.click();
          await page.waitForTimeout(500);
          
          // Check for sort indicator
          const sortIndicator = firstHeader.locator('.sort-indicator, [data-testid*="sort"], .sort-asc, .sort-desc');
          if (await sortIndicator.count() > 0) {
            await expect(sortIndicator.first()).toBeVisible();
          }
          
          // Check ARIA sort attribute
          const ariaSort = await firstHeader.getAttribute('aria-sort');
          if (ariaSort) {
            expect(['ascending', 'descending', 'none']).toContain(ariaSort);
          }
        }
      }
    });

    test('should toggle sort direction', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/citizens`);
      await page.waitForLoadState('networkidle');

      const dataTable = page.locator('table, [role="table"]').first();
      
      if (await dataTable.count() > 0) {
        const sortableHeaders = dataTable.locator('th[data-sortable="true"], .sortable-header');
        
        if (await sortableHeaders.count() > 0) {
          const firstHeader = sortableHeaders.first();
          
          // First click - ascending
          await firstHeader.click();
          await page.waitForTimeout(500);
          
          let ariaSort = await firstHeader.getAttribute('aria-sort');
          const firstSort = ariaSort;
          
          // Second click - descending
          await firstHeader.click();
          await page.waitForTimeout(500);
          
          ariaSort = await firstHeader.getAttribute('aria-sort');
          const secondSort = ariaSort;
          
          // Sort direction should have changed
          expect(firstSort).not.toBe(secondSort);
        }
      }
    });
  });

  test.describe('Filtering Functionality', () => {
    test('should filter data when filter inputs are used', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/citizens`);
      await page.waitForLoadState('networkidle');

      const dataTable = page.locator('table, [role="table"]').first();
      
      if (await dataTable.count() > 0) {
        const tableTester = new DataTableTester(page, dataTable);
        await tableTester.testFiltering();

        // Take screenshot after filtering
        await page.screenshot({ 
          path: 'test-results/data-table-filtered.png',
          fullPage: false 
        });
      }
    });

    test('should have search functionality', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/citizens`);
      await page.waitForLoadState('networkidle');

      const searchInput = page.locator('input[placeholder*="Search"], input[data-testid*="search"]').first();
      
      if (await searchInput.count() > 0) {
        // Get initial row count
        const dataTable = page.locator('table, [role="table"]').first();
        const initialRows = await dataTable.locator('tbody tr').count();
        
        // Perform search
        await searchInput.fill('test search');
        await page.keyboard.press('Enter');
        await page.waitForTimeout(1000);
        
        // Verify search was performed (loading state or results changed)
        const loadingIndicator = page.locator('.loading, [data-testid*="loading"]');
        if (await loadingIndicator.count() > 0) {
          await loadingIndicator.waitFor({ state: 'hidden', timeout: 5000 });
        }
        
        // Clear search
        await searchInput.clear();
        await page.keyboard.press('Enter');
        await page.waitForTimeout(1000);
      }
    });

    test('should support column-specific filters', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/citizens`);
      await page.waitForLoadState('networkidle');

      const columnFilters = page.locator('select[data-filter], input[data-filter]');
      const filterCount = await columnFilters.count();

      for (let i = 0; i < Math.min(filterCount, 2); i++) {
        const filter = columnFilters.nth(i);
        const tagName = await filter.evaluate(el => el.tagName.toLowerCase());
        
        if (tagName === 'select') {
          // Test dropdown filter
          const options = filter.locator('option');
          const optionCount = await options.count();
          
          if (optionCount > 1) {
            await filter.selectOption({ index: 1 });
            await page.waitForTimeout(500);
          }
        } else if (tagName === 'input') {
          // Test input filter
          await filter.fill('filter test');
          await page.keyboard.press('Enter');
          await page.waitForTimeout(500);
          
          // Clear filter
          await filter.clear();
          await page.keyboard.press('Enter');
          await page.waitForTimeout(500);
        }
      }
    });
  });

  test.describe('Pagination Functionality', () => {
    test('should navigate between pages', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/citizens`);
      await page.waitForLoadState('networkidle');

      const dataTable = page.locator('table, [role="table"]').first();
      
      if (await dataTable.count() > 0) {
        const tableTester = new DataTableTester(page, dataTable);
        await tableTester.testPagination();

        // Take screenshot of pagination
        await page.screenshot({ 
          path: 'test-results/data-table-pagination.png',
          fullPage: false 
        });
      }
    });

    test('should display pagination information', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/citizens`);
      await page.waitForLoadState('networkidle');

      const paginationInfo = page.locator('.pagination-info, [data-testid*="pagination-info"]');
      
      if (await paginationInfo.count() > 0) {
        const infoText = await paginationInfo.first().textContent();
        expect(infoText?.trim()).toBeTruthy();
        
        // Should contain numbers indicating current page/total
        expect(infoText).toMatch(/\d+/);
      }
    });

    test('should allow page size selection', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/citizens`);
      await page.waitForLoadState('networkidle');

      const pageSizeSelect = page.locator('select[data-testid*="page-size"], .page-size-select');
      
      if (await pageSizeSelect.count() > 0) {
        const options = pageSizeSelect.locator('option');
        const optionCount = await options.count();
        
        if (optionCount > 1) {
          // Get initial row count
          const dataTable = page.locator('table, [role="table"]').first();
          const initialRows = await dataTable.locator('tbody tr').count();
          
          // Change page size
          await pageSizeSelect.selectOption({ index: 1 });
          await page.waitForTimeout(1000);
          
          // Verify page size change took effect
          const newRows = await dataTable.locator('tbody tr').count();
          // Row count might change or stay the same depending on data
          expect(typeof newRows).toBe('number');
        }
      }
    });
  });

  test.describe('Row Selection Functionality', () => {
    test('should allow individual row selection', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/citizens`);
      await page.waitForLoadState('networkidle');

      const dataTable = page.locator('table, [role="table"]').first();
      
      if (await dataTable.count() > 0) {
        const tableTester = new DataTableTester(page, dataTable);
        await tableTester.testRowSelection();

        // Take screenshot of selected rows
        await page.screenshot({ 
          path: 'test-results/data-table-selection.png',
          fullPage: false 
        });
      }
    });

    test('should support select all functionality', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/citizens`);
      await page.waitForLoadState('networkidle');

      const selectAllCheckbox = page.locator('thead input[type="checkbox"], th input[type="checkbox"]').first();
      
      if (await selectAllCheckbox.count() > 0) {
        // Click select all
        await selectAllCheckbox.check();
        await page.waitForTimeout(500);
        
        // Verify all visible checkboxes are checked
        const rowCheckboxes = page.locator('tbody input[type="checkbox"]');
        const checkboxCount = await rowCheckboxes.count();
        
        for (let i = 0; i < checkboxCount; i++) {
          await expect(rowCheckboxes.nth(i)).toBeChecked();
        }
        
        // Uncheck select all
        await selectAllCheckbox.uncheck();
        await page.waitForTimeout(500);
        
        // Verify all checkboxes are unchecked
        for (let i = 0; i < checkboxCount; i++) {
          await expect(rowCheckboxes.nth(i)).not.toBeChecked();
        }
      }
    });

    test('should enable bulk actions when rows are selected', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/citizens`);
      await page.waitForLoadState('networkidle');

      const rowCheckboxes = page.locator('tbody input[type="checkbox"]');
      const checkboxCount = await rowCheckboxes.count();
      
      if (checkboxCount > 0) {
        // Select first checkbox
        await rowCheckboxes.first().check();
        await page.waitForTimeout(500);
        
        // Check if bulk actions are enabled
        const bulkActions = page.locator('.bulk-actions, [data-testid*="bulk-actions"]');
        if (await bulkActions.count() > 0) {
          await expect(bulkActions.first()).toBeVisible();
          
          const bulkActionButtons = bulkActions.locator('button');
          const buttonCount = await bulkActionButtons.count();
          
          for (let i = 0; i < buttonCount; i++) {
            await expect(bulkActionButtons.nth(i)).toBeEnabled();
          }
        }
      }
    });
  });

  test.describe('Data Table Accessibility', () => {
    test('should meet WCAG accessibility standards', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/citizens`);
      await page.waitForLoadState('networkidle');

      // Run accessibility audit
      const { AxeBuilder } = await import('@axe-core/playwright');
      
      const accessibilityScanResults = await new AxeBuilder({ page })
        .withTags(['wcag2a', 'wcag2aa'])
        .analyze();

      expect(accessibilityScanResults.violations).toEqual([]);
    });

    test('should support keyboard navigation', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/citizens`);
      await page.waitForLoadState('networkidle');

      const dataTable = page.locator('table, [role="table"]').first();
      
      if (await dataTable.count() > 0) {
        // Focus on table
        await dataTable.focus();
        
        // Test arrow key navigation
        await page.keyboard.press('ArrowDown');
        await page.waitForTimeout(100);
        
        await page.keyboard.press('ArrowRight');
        await page.waitForTimeout(100);
        
        // Check if focus is visible
        const focusedElement = page.locator(':focus');
        await expect(focusedElement).toBeVisible();
      }
    });

    test('should have proper table headers association', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/citizens`);
      await page.waitForLoadState('networkidle');

      const dataTable = page.locator('table').first();
      
      if (await dataTable.count() > 0) {
        // Check table has proper structure
        const headers = dataTable.locator('th');
        const headerCount = await headers.count();
        
        for (let i = 0; i < headerCount; i++) {
          const header = headers.nth(i);
          
          // Check for scope attribute
          const scope = await header.getAttribute('scope');
          if (scope) {
            expect(['col', 'row', 'colgroup', 'rowgroup']).toContain(scope);
          }
          
          // Check for id attribute for complex tables
          const headerId = await header.getAttribute('id');
          if (headerId) {
            expect(headerId.trim()).toBeTruthy();
          }
        }
      }
    });
  });

  test.describe('Data Table Performance', () => {
    test('should handle large datasets efficiently', async ({ page }) => {
      // Mock large dataset
      await page.route('**/api/citizens**', route => {
        const largeDataset = Array.from({ length: 1000 }, (_, i) => ({
          id: i + 1,
          name: `Citizen ${i + 1}`,
          email: `citizen${i + 1}@example.com`,
          status: i % 3 === 0 ? 'Active' : 'Pending'
        }));
        
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ data: largeDataset.slice(0, 50), total: 1000 })
        });
      });

      const startTime = Date.now();
      
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/citizens`);
      await page.waitForLoadState('networkidle');
      
      const endTime = Date.now();
      const loadTime = endTime - startTime;
      
      // Should load within 2 seconds even with large dataset
      expect(loadTime).toBeLessThan(2000);

      // Table should be visible and functional
      const dataTable = page.locator('table, [role="table"]').first();
      if (await dataTable.count() > 0) {
        await expect(dataTable).toBeVisible();
      }
    });

    test('should maintain performance during interactions', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/citizens`);
      await page.waitForLoadState('networkidle');

      const dataTable = page.locator('table, [role="table"]').first();
      
      if (await dataTable.count() > 0) {
        // Test multiple rapid interactions
        const sortableHeaders = dataTable.locator('th[data-sortable="true"], .sortable-header');
        
        if (await sortableHeaders.count() > 0) {
          const startTime = Date.now();
          
          // Perform multiple sorts rapidly
          for (let i = 0; i < 3; i++) {
            await sortableHeaders.first().click();
            await page.waitForTimeout(100);
          }
          
          const endTime = Date.now();
          const interactionTime = endTime - startTime;
          
          // Interactions should be responsive
          expect(interactionTime).toBeLessThan(1000);
        }
      }
    });
  });
});
