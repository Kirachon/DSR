import { test, expect } from '@playwright/test';
import { AuthenticationManager } from '../../src/utils/auth-test-utilities';
import { NavigationTester } from '../../src/utils/component-test-utilities';

test.describe('Role-Based Navigation Component Tests', () => {
  let authManager: AuthenticationManager;

  test.beforeEach(async ({ page }) => {
    authManager = new AuthenticationManager(page);
  });

  test.afterEach(async ({ page }) => {
    await authManager.logout();
  });

  test.describe('Citizen Navigation', () => {
    test.beforeEach(async ({ page }) => {
      await authManager.loginAs('CITIZEN');
    });

    test('should display citizen-specific navigation structure', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const navigation = page.locator('nav, [role="navigation"]').first();
      
      if (await navigation.count() > 0) {
        const navTester = new NavigationTester(page, navigation);
        
        await navTester.testNavigationStructure({
          userRole: 'CITIZEN',
          expectedSections: ['Dashboard', 'Registration', 'Eligibility', 'Payments', 'Profile'],
          validateActiveStates: true
        });

        // Take screenshot for visual verification
        await page.screenshot({ 
          path: 'test-results/navigation-citizen.png',
          fullPage: false 
        });
      }
    });

    test('should show only citizen-accessible navigation items', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      // Check for citizen-specific navigation items
      const citizenNavItems = [
        'Dashboard',
        'Registration',
        'Eligibility',
        'Payments',
        'Profile',
        'My Journey',
        'Applications'
      ];

      for (const item of citizenNavItems) {
        const navItem = page.locator(`nav a:has-text("${item}"), nav button:has-text("${item}")`);
        if (await navItem.count() > 0) {
          await expect(navItem.first()).toBeVisible();
        }
      }

      // Check that staff/admin items are not visible
      const restrictedItems = [
        'Admin Panel',
        'Staff Management',
        'System Settings',
        'User Management',
        'Reports',
        'Analytics'
      ];

      for (const item of restrictedItems) {
        const navItem = page.locator(`nav a:has-text("${item}"), nav button:has-text("${item}")`);
        const count = await navItem.count();
        
        for (let i = 0; i < count; i++) {
          await expect(navItem.nth(i)).not.toBeVisible();
        }
      }
    });

    test('should navigate correctly to citizen pages', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const navigationTests = [
        { text: 'Registration', expectedUrl: '/registration' },
        { text: 'Eligibility', expectedUrl: '/eligibility' },
        { text: 'Payments', expectedUrl: '/payments' },
        { text: 'Profile', expectedUrl: '/profile' }
      ];

      for (const { text, expectedUrl } of navigationTests) {
        const navLink = page.locator(`nav a:has-text("${text}")`).first();
        
        if (await navLink.count() > 0 && await navLink.isVisible()) {
          await navLink.click();
          await page.waitForLoadState('networkidle');
          
          // Verify URL contains expected path
          expect(page.url()).toContain(expectedUrl);
          
          // Navigate back to dashboard
          await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
          await page.waitForLoadState('networkidle');
        }
      }
    });

    test('should highlight active navigation item', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      // Check for active dashboard item
      const activeNavItem = page.locator('nav .active, nav [aria-current="page"]');
      
      if (await activeNavItem.count() > 0) {
        await expect(activeNavItem.first()).toBeVisible();
        
        const activeText = await activeNavItem.first().textContent();
        expect(activeText?.toLowerCase()).toContain('dashboard');
      }
    });
  });

  test.describe('LGU Staff Navigation', () => {
    test.beforeEach(async ({ page }) => {
      await authManager.loginAs('LGU_STAFF');
    });

    test('should display LGU staff-specific navigation structure', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const navigation = page.locator('nav, [role="navigation"]').first();
      
      if (await navigation.count() > 0) {
        const navTester = new NavigationTester(page, navigation);
        
        await navTester.testNavigationStructure({
          userRole: 'LGU_STAFF',
          expectedSections: ['Dashboard', 'Citizens', 'Applications', 'Cases', 'Reports'],
          validateActiveStates: true
        });

        // Take screenshot for visual verification
        await page.screenshot({ 
          path: 'test-results/navigation-lgu-staff.png',
          fullPage: false 
        });
      }
    });

    test('should show LGU staff-accessible navigation items', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      // Check for LGU staff-specific navigation items
      const lguStaffNavItems = [
        'Dashboard',
        'Citizens',
        'Applications',
        'Cases',
        'Registrations',
        'Verification'
      ];

      for (const item of lguStaffNavItems) {
        const navItem = page.locator(`nav a:has-text("${item}"), nav button:has-text("${item}")`);
        if (await navItem.count() > 0) {
          await expect(navItem.first()).toBeVisible();
        }
      }

      // Check that DSWD/admin items are not visible
      const restrictedItems = [
        'System Admin',
        'National Reports',
        'DSWD Analytics',
        'User Management'
      ];

      for (const item of restrictedItems) {
        const navItem = page.locator(`nav a:has-text("${item}"), nav button:has-text("${item}")`);
        const count = await navItem.count();
        
        for (let i = 0; i < count; i++) {
          await expect(navItem.nth(i)).not.toBeVisible();
        }
      }
    });

    test('should navigate correctly to LGU staff pages', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const navigationTests = [
        { text: 'Citizens', expectedUrl: '/citizens' },
        { text: 'Applications', expectedUrl: '/applications' },
        { text: 'Cases', expectedUrl: '/cases' }
      ];

      for (const { text, expectedUrl } of navigationTests) {
        const navLink = page.locator(`nav a:has-text("${text}")`).first();
        
        if (await navLink.count() > 0 && await navLink.isVisible()) {
          await navLink.click();
          await page.waitForLoadState('networkidle');
          
          // Verify URL contains expected path
          expect(page.url()).toContain(expectedUrl);
          
          // Navigate back to dashboard
          await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
          await page.waitForLoadState('networkidle');
        }
      }
    });
  });

  test.describe('DSWD Staff Navigation', () => {
    test.beforeEach(async ({ page }) => {
      await authManager.loginAs('DSWD_STAFF');
    });

    test('should display DSWD staff-specific navigation structure', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const navigation = page.locator('nav, [role="navigation"]').first();
      
      if (await navigation.count() > 0) {
        const navTester = new NavigationTester(page, navigation);
        
        await navTester.testNavigationStructure({
          userRole: 'DSWD_STAFF',
          expectedSections: ['Dashboard', 'Reports', 'Analytics', 'Payments', 'Oversight'],
          validateActiveStates: true
        });

        // Take screenshot for visual verification
        await page.screenshot({ 
          path: 'test-results/navigation-dswd-staff.png',
          fullPage: false 
        });
      }
    });

    test('should show DSWD staff-accessible navigation items', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      // Check for DSWD staff-specific navigation items
      const dswdStaffNavItems = [
        'Dashboard',
        'Reports',
        'Analytics',
        'Payments',
        'National Overview'
      ];

      for (const item of dswdStaffNavItems) {
        const navItem = page.locator(`nav a:has-text("${item}"), nav button:has-text("${item}")`);
        if (await navItem.count() > 0) {
          await expect(navItem.first()).toBeVisible();
        }
      }
    });

    test('should navigate correctly to DSWD staff pages', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const navigationTests = [
        { text: 'Reports', expectedUrl: '/reports' },
        { text: 'Analytics', expectedUrl: '/analytics' },
        { text: 'Payments', expectedUrl: '/payments' }
      ];

      for (const { text, expectedUrl } of navigationTests) {
        const navLink = page.locator(`nav a:has-text("${text}")`).first();
        
        if (await navLink.count() > 0 && await navLink.isVisible()) {
          await navLink.click();
          await page.waitForLoadState('networkidle');
          
          // Verify URL contains expected path
          expect(page.url()).toContain(expectedUrl);
          
          // Navigate back to dashboard
          await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
          await page.waitForLoadState('networkidle');
        }
      }
    });
  });

  test.describe('System Admin Navigation', () => {
    test.beforeEach(async ({ page }) => {
      await authManager.loginAs('SYSTEM_ADMIN');
    });

    test('should display admin-specific navigation structure', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/admin`);
      await page.waitForLoadState('networkidle');

      const navigation = page.locator('nav, [role="navigation"]').first();
      
      if (await navigation.count() > 0) {
        const navTester = new NavigationTester(page, navigation);
        
        await navTester.testNavigationStructure({
          userRole: 'SYSTEM_ADMIN',
          validateActiveStates: true
        });

        // Take screenshot for visual verification
        await page.screenshot({ 
          path: 'test-results/navigation-admin.png',
          fullPage: false 
        });
      }
    });

    test('should show all navigation items for admin', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/admin`);
      await page.waitForLoadState('networkidle');

      // Admin should see all navigation items
      const adminNavItems = [
        'Dashboard',
        'Users',
        'System Settings',
        'Reports',
        'Analytics',
        'Audit Logs'
      ];

      for (const item of adminNavItems) {
        const navItem = page.locator(`nav a:has-text("${item}"), nav button:has-text("${item}")`);
        if (await navItem.count() > 0) {
          await expect(navItem.first()).toBeVisible();
        }
      }
    });
  });

  test.describe('Navigation Interaction and Behavior', () => {
    test.beforeEach(async ({ page }) => {
      await authManager.loginAs('CITIZEN');
    });

    test('should support collapsible navigation', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const navigation = page.locator('nav, [role="navigation"]').first();
      
      if (await navigation.count() > 0) {
        const navTester = new NavigationTester(page, navigation);
        await navTester.testCollapsibleBehavior();
      }
    });

    test('should handle navigation item interactions', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const navigation = page.locator('nav, [role="navigation"]').first();
      
      if (await navigation.count() > 0) {
        const navTester = new NavigationTester(page, navigation);
        await navTester.testNavigationInteractions();
      }
    });

    test('should support keyboard navigation', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const navigation = page.locator('nav, [role="navigation"]').first();
      
      if (await navigation.count() > 0) {
        // Focus on navigation
        await navigation.focus();
        
        // Test Tab navigation
        await page.keyboard.press('Tab');
        await page.waitForTimeout(100);
        
        // Check if focus is on a navigation item
        const focusedElement = page.locator(':focus');
        await expect(focusedElement).toBeVisible();
        
        // Test Enter key activation
        await page.keyboard.press('Enter');
        await page.waitForTimeout(500);
      }
    });

    test('should maintain navigation state during page transitions', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      // Navigate to different pages and check navigation consistency
      const pages = ['/registration', '/eligibility', '/payments'];
      
      for (const pagePath of pages) {
        await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}${pagePath}`);
        await page.waitForLoadState('networkidle');
        
        // Check navigation is still present and functional
        const navigation = page.locator('nav, [role="navigation"]').first();
        if (await navigation.count() > 0) {
          await expect(navigation).toBeVisible();
          
          // Check for active state
          const activeItem = navigation.locator('.active, [aria-current="page"]');
          if (await activeItem.count() > 0) {
            await expect(activeItem.first()).toBeVisible();
          }
        }
      }
    });
  });

  test.describe('Navigation Accessibility', () => {
    test.beforeEach(async ({ page }) => {
      await authManager.loginAs('CITIZEN');
    });

    test('should meet WCAG accessibility standards', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      // Run accessibility audit
      const { AxeBuilder } = await import('@axe-core/playwright');
      
      const accessibilityScanResults = await new AxeBuilder({ page })
        .include('nav')
        .withTags(['wcag2a', 'wcag2aa'])
        .analyze();

      expect(accessibilityScanResults.violations).toEqual([]);
    });

    test('should have proper ARIA attributes', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const navigation = page.locator('nav, [role="navigation"]').first();
      
      if (await navigation.count() > 0) {
        // Check navigation has proper role
        const role = await navigation.getAttribute('role');
        expect(role === 'navigation' || await navigation.evaluate(el => el.tagName.toLowerCase()) === 'nav').toBeTruthy();
        
        // Check for aria-label or aria-labelledby
        const ariaLabel = await navigation.getAttribute('aria-label');
        const ariaLabelledBy = await navigation.getAttribute('aria-labelledby');
        
        expect(ariaLabel || ariaLabelledBy).toBeTruthy();
        
        // Check navigation items have proper attributes
        const navItems = navigation.locator('a, button, [role="menuitem"]');
        const itemCount = await navItems.count();
        
        for (let i = 0; i < Math.min(itemCount, 5); i++) {
          const item = navItems.nth(i);
          
          if (await item.isVisible()) {
            // Check for accessible name
            const itemAriaLabel = await item.getAttribute('aria-label');
            const itemText = await item.textContent();
            
            expect(itemAriaLabel || itemText?.trim()).toBeTruthy();
            
            // Check for current state if applicable
            const ariaCurrent = await item.getAttribute('aria-current');
            if (ariaCurrent) {
              expect(['page', 'step', 'location', 'date', 'time', 'true']).toContain(ariaCurrent);
            }
          }
        }
      }
    });

    test('should support screen reader navigation', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const navigation = page.locator('nav, [role="navigation"]').first();
      
      if (await navigation.count() > 0) {
        // Check for landmark navigation
        const landmarkRole = await navigation.getAttribute('role');
        const tagName = await navigation.evaluate(el => el.tagName.toLowerCase());
        
        expect(landmarkRole === 'navigation' || tagName === 'nav').toBeTruthy();
        
        // Check for skip links
        const skipLinks = page.locator('a[href*="#"], .skip-link');
        if (await skipLinks.count() > 0) {
          const skipLink = skipLinks.first();
          const skipText = await skipLink.textContent();
          expect(skipText?.toLowerCase()).toMatch(/skip|main|content/);
        }
      }
    });
  });

  test.describe('Navigation Performance', () => {
    test.beforeEach(async ({ page }) => {
      await authManager.loginAs('CITIZEN');
    });

    test('should load navigation quickly', async ({ page }) => {
      const startTime = Date.now();
      
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');
      
      const navigation = page.locator('nav, [role="navigation"]').first();
      await navigation.waitFor({ state: 'visible', timeout: 2000 });
      
      const endTime = Date.now();
      const loadTime = endTime - startTime;
      
      // Navigation should load within 2 seconds
      expect(loadTime).toBeLessThan(2000);
    });

    test('should handle rapid navigation interactions', async ({ page }) => {
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const navigation = page.locator('nav, [role="navigation"]').first();
      
      if (await navigation.count() > 0) {
        const navItems = navigation.locator('a, button');
        const itemCount = await navItems.count();
        
        // Rapidly hover over navigation items
        const startTime = Date.now();
        
        for (let i = 0; i < Math.min(itemCount, 5); i++) {
          const item = navItems.nth(i);
          if (await item.isVisible()) {
            await item.hover();
            await page.waitForTimeout(50);
          }
        }
        
        const endTime = Date.now();
        const interactionTime = endTime - startTime;
        
        // Interactions should be responsive
        expect(interactionTime).toBeLessThan(1000);
      }
    });
  });
});
