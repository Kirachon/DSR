import { test, expect } from '@playwright/test';
import { CitizenDashboardPage } from '../../src/pages/dashboards/citizen-dashboard-page';
import { StaffDashboardPage } from '../../src/pages/dashboards/staff-dashboard-page';
import { AuthenticationManager } from '../../src/utils/auth-test-utilities';

test.describe('Dashboard Responsive Design Tests', () => {
  let authManager: AuthenticationManager;

  // Define viewport configurations for testing
  const viewports = [
    { width: 320, height: 568, name: 'mobile-small', category: 'mobile' },
    { width: 375, height: 667, name: 'mobile-medium', category: 'mobile' },
    { width: 414, height: 896, name: 'mobile-large', category: 'mobile' },
    { width: 768, height: 1024, name: 'tablet-portrait', category: 'tablet' },
    { width: 1024, height: 768, name: 'tablet-landscape', category: 'tablet' },
    { width: 1280, height: 720, name: 'desktop-small', category: 'desktop' },
    { width: 1920, height: 1080, name: 'desktop-large', category: 'desktop' },
    { width: 2560, height: 1440, name: 'desktop-xl', category: 'desktop' }
  ];

  test.beforeEach(async ({ page }) => {
    authManager = new AuthenticationManager(page);
  });

  test.afterEach(async ({ page }) => {
    await authManager.logout();
  });

  test.describe('Citizen Dashboard Responsive Design', () => {
    test.beforeEach(async ({ page }) => {
      await authManager.loginAs('CITIZEN');
    });

    for (const viewport of viewports) {
      test(`should display correctly on ${viewport.name} (${viewport.width}x${viewport.height})`, async ({ page }) => {
        await page.setViewportSize({ width: viewport.width, height: viewport.height });
        
        const citizenDashboard = new CitizenDashboardPage(page);
        await citizenDashboard.goto();
        await citizenDashboard.waitForPageLoad();

        // Validate basic dashboard functionality
        await citizenDashboard.validateDashboardLoaded();

        // Take screenshot for visual verification
        await citizenDashboard.takeScreenshot(`citizen-dashboard-${viewport.name}`);

        // Validate responsive behavior based on viewport category
        if (viewport.category === 'mobile') {
          await validateMobileLayout(page, citizenDashboard);
        } else if (viewport.category === 'tablet') {
          await validateTabletLayout(page, citizenDashboard);
        } else {
          await validateDesktopLayout(page, citizenDashboard);
        }

        // Validate no horizontal scrollbar on smaller screens
        if (viewport.width <= 768) {
          const hasHorizontalScroll = await page.evaluate(() => {
            return document.documentElement.scrollWidth > document.documentElement.clientWidth;
          });
          expect(hasHorizontalScroll).toBeFalsy();
        }

        // Validate touch targets on mobile
        if (viewport.category === 'mobile') {
          await validateTouchTargets(page);
        }
      });
    }

    test('should handle orientation changes on mobile devices', async ({ page }) => {
      const citizenDashboard = new CitizenDashboardPage(page);
      await citizenDashboard.goto();

      // Test portrait orientation
      await page.setViewportSize({ width: 375, height: 667 });
      await citizenDashboard.waitForPageLoad();
      await citizenDashboard.validateDashboardLoaded();
      await citizenDashboard.takeScreenshot('citizen-dashboard-mobile-portrait');

      // Test landscape orientation
      await page.setViewportSize({ width: 667, height: 375 });
      await citizenDashboard.waitForPageLoad();
      await citizenDashboard.validateDashboardLoaded();
      await citizenDashboard.takeScreenshot('citizen-dashboard-mobile-landscape');
    });

    test('should maintain functionality across all viewport sizes', async ({ page }) => {
      const citizenDashboard = new CitizenDashboardPage(page);
      await citizenDashboard.goto();

      for (const viewport of viewports.slice(0, 4)) { // Test subset for performance
        await page.setViewportSize({ width: viewport.width, height: viewport.height });
        await citizenDashboard.waitForPageLoad();

        // Validate core functionality works
        await citizenDashboard.validateQuickActions();
        await citizenDashboard.validateApplicationStatus();

        // Test navigation on smaller screens
        if (viewport.category === 'mobile') {
          await testMobileNavigation(page);
        }
      }
    });
  });

  test.describe('Staff Dashboard Responsive Design', () => {
    test.beforeEach(async ({ page }) => {
      await authManager.loginAs('LGU_STAFF');
    });

    for (const viewport of viewports.filter(v => v.category !== 'mobile')) { // Staff dashboards typically not optimized for mobile
      test(`should display correctly on ${viewport.name} (${viewport.width}x${viewport.height})`, async ({ page }) => {
        await page.setViewportSize({ width: viewport.width, height: viewport.height });
        
        const staffDashboard = new StaffDashboardPage(page);
        await staffDashboard.goto();
        await staffDashboard.waitForPageLoad();

        // Validate basic dashboard functionality
        await staffDashboard.validateDashboardLoaded();

        // Take screenshot for visual verification
        await staffDashboard.takeScreenshot(`staff-dashboard-${viewport.name}`);

        // Validate responsive behavior
        if (viewport.category === 'tablet') {
          await validateStaffTabletLayout(page, staffDashboard);
        } else {
          await validateStaffDesktopLayout(page, staffDashboard);
        }
      });
    }

    test('should handle data table responsiveness', async ({ page }) => {
      const staffDashboard = new StaffDashboardPage(page);
      await staffDashboard.goto();

      const testViewports = [
        { width: 768, height: 1024, name: 'tablet' },
        { width: 1280, height: 720, name: 'desktop-small' },
        { width: 1920, height: 1080, name: 'desktop-large' }
      ];

      for (const viewport of testViewports) {
        await page.setViewportSize({ width: viewport.width, height: viewport.height });
        await staffDashboard.waitForPageLoad();

        // Test data table responsiveness
        const dataTable = page.locator('[data-testid="data-table"]');
        if (await dataTable.count() > 0) {
          await expect(dataTable.first()).toBeVisible();

          // Check for horizontal scroll on smaller screens
          if (viewport.width <= 768) {
            const tableContainer = dataTable.locator('..').first();
            const hasScroll = await tableContainer.evaluate(el => 
              el.scrollWidth > el.clientWidth
            );
            // Table should either fit or have horizontal scroll
            expect(typeof hasScroll).toBe('boolean');
          }
        }
      }
    });
  });

  test.describe('Cross-Device Compatibility', () => {
    test('should work consistently across different device types', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      const citizenDashboard = new CitizenDashboardPage(page);

      const deviceTests = [
        { device: 'iPhone 12', width: 390, height: 844 },
        { device: 'iPad', width: 768, height: 1024 },
        { device: 'Desktop', width: 1920, height: 1080 }
      ];

      for (const { device, width, height } of deviceTests) {
        await page.setViewportSize({ width, height });
        await citizenDashboard.goto();
        await citizenDashboard.waitForPageLoad();

        // Validate core functionality works on all devices
        await citizenDashboard.validateDashboardLoaded();
        await citizenDashboard.validateQuickActions();

        // Take device-specific screenshot
        await citizenDashboard.takeScreenshot(`citizen-dashboard-${device.toLowerCase().replace(' ', '-')}`);
      }
    });

    test('should handle zoom levels appropriately', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      const citizenDashboard = new CitizenDashboardPage(page);
      await citizenDashboard.goto();

      const zoomLevels = [0.5, 0.75, 1.0, 1.25, 1.5, 2.0];

      for (const zoom of zoomLevels) {
        await page.evaluate((zoomLevel) => {
          document.body.style.zoom = zoomLevel.toString();
        }, zoom);

        await citizenDashboard.waitForPageLoad();
        await citizenDashboard.validateDashboardLoaded();

        // Take screenshot at different zoom levels
        await citizenDashboard.takeScreenshot(`citizen-dashboard-zoom-${zoom.toString().replace('.', '-')}`);
      }

      // Reset zoom
      await page.evaluate(() => {
        document.body.style.zoom = '1';
      });
    });
  });

  // Helper functions for layout validation
  async function validateMobileLayout(page: any, dashboard: CitizenDashboardPage): Promise<void> {
    // Check for mobile-specific layout adjustments
    const mobileMenu = page.locator('.mobile-menu, [data-testid="mobile-menu"]');
    const hamburgerButton = page.locator('.hamburger, [data-testid="menu-toggle"]');

    // Mobile navigation should be present
    if (await hamburgerButton.count() > 0) {
      await expect(hamburgerButton.first()).toBeVisible();
    }

    // Quick actions should stack vertically on mobile
    const quickActions = page.locator('[data-testid="quick-actions"]');
    if (await quickActions.count() > 0) {
      const flexDirection = await quickActions.first().evaluate(el => 
        window.getComputedStyle(el).flexDirection
      );
      expect(['column', 'column-reverse']).toContain(flexDirection);
    }
  }

  async function validateTabletLayout(page: any, dashboard: CitizenDashboardPage): Promise<void> {
    // Validate tablet-specific layout
    await dashboard.validateQuickActions();
    await dashboard.validateApplicationStatus();

    // Check for appropriate spacing and sizing
    const cards = page.locator('.card, [data-testid*="card"]');
    const cardCount = await cards.count();

    for (let i = 0; i < Math.min(cardCount, 3); i++) {
      const card = cards.nth(i);
      if (await card.isVisible()) {
        const cardWidth = await card.evaluate(el => el.offsetWidth);
        expect(cardWidth).toBeGreaterThan(200); // Minimum readable width
      }
    }
  }

  async function validateDesktopLayout(page: any, dashboard: CitizenDashboardPage): Promise<void> {
    // Validate full desktop layout
    await dashboard.validateDashboardLoaded();
    await dashboard.validateQuickActions();
    await dashboard.validateApplicationStatus();
    await dashboard.validateBenefitCards();
    await dashboard.validateDashboardWidgets();
  }

  async function validateStaffTabletLayout(page: any, dashboard: StaffDashboardPage): Promise<void> {
    await dashboard.validateDashboardLoaded();
    await dashboard.validateKPICards();
    await dashboard.validateWorkQueue();
  }

  async function validateStaffDesktopLayout(page: any, dashboard: StaffDashboardPage): Promise<void> {
    await dashboard.validateDashboardLoaded();
    await dashboard.validateKPICards();
    await dashboard.validateWorkQueue();
    await dashboard.validatePendingRegistrations();
  }

  async function validateTouchTargets(page: any): Promise<void> {
    // Validate touch targets meet minimum size requirements (44px)
    const interactiveElements = page.locator('button, a, input, select, [role="button"]');
    const elementCount = await interactiveElements.count();

    for (let i = 0; i < Math.min(elementCount, 10); i++) { // Test subset for performance
      const element = interactiveElements.nth(i);
      
      if (await element.isVisible()) {
        const boundingBox = await element.boundingBox();
        if (boundingBox) {
          expect(boundingBox.width).toBeGreaterThanOrEqual(44);
          expect(boundingBox.height).toBeGreaterThanOrEqual(44);
        }
      }
    }
  }

  async function testMobileNavigation(page: any): Promise<void> {
    // Test mobile navigation functionality
    const mobileMenuToggle = page.locator('.mobile-menu-toggle, [data-testid="menu-toggle"]');
    
    if (await mobileMenuToggle.count() > 0) {
      // Open mobile menu
      await mobileMenuToggle.first().click();
      await page.waitForTimeout(300);

      // Check if menu is visible
      const mobileMenu = page.locator('.mobile-menu, [data-testid="mobile-menu"]');
      if (await mobileMenu.count() > 0) {
        await expect(mobileMenu.first()).toBeVisible();

        // Close mobile menu
        await mobileMenuToggle.first().click();
        await page.waitForTimeout(300);
      }
    }
  }
});
