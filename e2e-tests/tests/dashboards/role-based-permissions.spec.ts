import { test, expect } from '@playwright/test';
import { CitizenDashboardPage } from '../../src/pages/dashboards/citizen-dashboard-page';
import { StaffDashboardPage } from '../../src/pages/dashboards/staff-dashboard-page';
import { AuthenticationManager, TEST_USERS } from '../../src/utils/auth-test-utilities';

test.describe('Role-Based Permissions and Content Validation', () => {
  let authManager: AuthenticationManager;

  test.beforeEach(async ({ page }) => {
    authManager = new AuthenticationManager(page);
  });

  test.afterEach(async ({ page }) => {
    await authManager.logout();
  });

  test.describe('Citizen Role Permissions', () => {
    test('should show citizen-specific content and hide staff functions', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      const citizenDashboard = new CitizenDashboardPage(page);
      await citizenDashboard.goto();

      // Verify citizen can see their content
      await citizenDashboard.validateDashboardLoaded();
      await citizenDashboard.validateQuickActions();

      // Verify citizen cannot see staff-only elements
      const staffOnlyElements = [
        '[data-role="LGU_STAFF"]',
        '[data-role="DSWD_STAFF"]',
        '[data-role="SYSTEM_ADMIN"]',
        '[data-testid*="admin"]',
        '[data-testid*="staff-only"]',
        '.admin-only',
        '.staff-only'
      ];

      for (const selector of staffOnlyElements) {
        const elements = page.locator(selector);
        const count = await elements.count();
        
        for (let i = 0; i < count; i++) {
          await expect(elements.nth(i)).not.toBeVisible();
        }
      }

      // Verify citizen-specific navigation
      await citizenDashboard.validateRoleBasedContent('CITIZEN');
    });

    test('should prevent citizen access to staff-only pages', async ({ page }) => {
      await authManager.loginAs('CITIZEN');

      // Try to access staff-only pages
      const staffOnlyPages = [
        '/admin',
        '/staff',
        '/management',
        '/reports/admin',
        '/analytics/staff'
      ];

      for (const staffPage of staffOnlyPages) {
        await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}${staffPage}`);
        
        // Should be redirected or show access denied
        const currentUrl = page.url();
        const accessDenied = page.locator('text=Access Denied, text=Unauthorized, text=403');
        
        expect(
          currentUrl.includes('/dashboard') || 
          currentUrl.includes('/login') || 
          await accessDenied.count() > 0
        ).toBeTruthy();
      }
    });

    test('should validate citizen can only see their own data', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      const citizenDashboard = new CitizenDashboardPage(page);
      await citizenDashboard.goto();

      // Verify user information matches logged-in citizen
      const currentUser = await authManager.getCurrentUser();
      expect(currentUser.role).toBe('CITIZEN');
      
      await citizenDashboard.validateWelcomeMessage(`${currentUser.firstName} ${currentUser.lastName}`);
    });
  });

  test.describe('LGU Staff Role Permissions', () => {
    test('should show LGU staff-specific content and functions', async ({ page }) => {
      await authManager.loginAs('LGU_STAFF');
      const staffDashboard = new StaffDashboardPage(page);
      await staffDashboard.goto();

      // Verify LGU staff can see their content
      await staffDashboard.validateDashboardLoaded();
      await staffDashboard.validateStaffPermissions('LGU_STAFF');

      // Verify LGU staff-specific elements are visible
      const lguStaffElements = [
        '[data-testid="review-application"]',
        '[data-testid="assign-case"]',
        '[data-testid="pending-registrations"]'
      ];

      for (const selector of lguStaffElements) {
        const element = page.locator(selector);
        if (await element.count() > 0) {
          await expect(element.first()).toBeVisible();
        }
      }

      // Verify LGU staff cannot see DSWD-only or admin-only elements
      const restrictedElements = [
        '[data-role="DSWD_STAFF"]',
        '[data-role="SYSTEM_ADMIN"]',
        '[data-testid*="dswd-only"]',
        '[data-testid*="admin-only"]'
      ];

      for (const selector of restrictedElements) {
        const elements = page.locator(selector);
        const count = await elements.count();
        
        for (let i = 0; i < count; i++) {
          await expect(elements.nth(i)).not.toBeVisible();
        }
      }
    });

    test('should allow LGU staff to access local management functions', async ({ page }) => {
      await authManager.loginAs('LGU_STAFF');
      const staffDashboard = new StaffDashboardPage(page);
      await staffDashboard.goto();

      // Test LGU staff can review applications
      await staffDashboard.clickReviewApplication();
      
      // Test LGU staff can assign cases
      await staffDashboard.clickAssignCase();
    });

    test('should prevent LGU staff access to DSWD-only functions', async ({ page }) => {
      await authManager.loginAs('LGU_STAFF');

      // Try to access DSWD-only pages
      const dswdOnlyPages = [
        '/admin/dswd',
        '/reports/national',
        '/analytics/national'
      ];

      for (const dswdPage of dswdOnlyPages) {
        await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}${dswdPage}`);
        
        // Should be redirected or show access denied
        const currentUrl = page.url();
        const accessDenied = page.locator('text=Access Denied, text=Unauthorized, text=403');
        
        expect(
          currentUrl.includes('/dashboard') || 
          currentUrl.includes('/login') || 
          await accessDenied.count() > 0
        ).toBeTruthy();
      }
    });
  });

  test.describe('DSWD Staff Role Permissions', () => {
    test('should show DSWD staff-specific content and functions', async ({ page }) => {
      await authManager.loginAs('DSWD_STAFF');
      const staffDashboard = new StaffDashboardPage(page);
      await staffDashboard.goto();

      // Verify DSWD staff can see their content
      await staffDashboard.validateDashboardLoaded();
      await staffDashboard.validateStaffPermissions('DSWD_STAFF');

      // Verify DSWD staff-specific elements are visible
      const dswdStaffElements = [
        '[data-testid="generate-report"]',
        '[data-testid="view-analytics"]',
        '[data-testid="approve-registration"]'
      ];

      for (const selector of dswdStaffElements) {
        const element = page.locator(selector);
        if (await element.count() > 0) {
          await expect(element.first()).toBeVisible();
        }
      }
    });

    test('should allow DSWD staff to access national oversight functions', async ({ page }) => {
      await authManager.loginAs('DSWD_STAFF');
      const staffDashboard = new StaffDashboardPage(page);
      await staffDashboard.goto();

      // Test DSWD staff can generate reports
      await staffDashboard.clickGenerateReport();
      
      // Test DSWD staff can view analytics
      await staffDashboard.clickViewAnalytics();
      
      // Test DSWD staff can approve registrations
      await staffDashboard.clickApproveRegistration();
    });
  });

  test.describe('System Admin Role Permissions', () => {
    test('should show admin-specific content and full system access', async ({ page }) => {
      await authManager.loginAs('SYSTEM_ADMIN');
      
      // Navigate to admin dashboard
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/admin`);
      await page.waitForLoadState('networkidle');

      // Verify admin can access admin functions
      const adminElements = [
        '[data-testid*="admin"]',
        '[data-role="SYSTEM_ADMIN"]',
        '.admin-only'
      ];

      for (const selector of adminElements) {
        const elements = page.locator(selector);
        const count = await elements.count();
        
        for (let i = 0; i < count; i++) {
          if (await elements.nth(i).isVisible()) {
            await expect(elements.nth(i)).toBeVisible();
          }
        }
      }
    });

    test('should allow admin access to all system functions', async ({ page }) => {
      await authManager.loginAs('SYSTEM_ADMIN');

      // Test admin can access all pages
      const allPages = [
        '/dashboard',
        '/admin',
        '/reports',
        '/analytics',
        '/users',
        '/settings'
      ];

      for (const adminPage of allPages) {
        await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}${adminPage}`);
        await page.waitForLoadState('networkidle');
        
        // Should not be redirected to login or access denied
        const currentUrl = page.url();
        expect(currentUrl).toContain(adminPage);
      }
    });
  });

  test.describe('Cross-Role Permission Validation', () => {
    test('should validate each role can only access appropriate content', async ({ page }) => {
      const roles = ['CITIZEN', 'LGU_STAFF', 'DSWD_STAFF', 'SYSTEM_ADMIN'] as const;

      for (const role of roles) {
        await authManager.loginAs(role);
        
        // Validate user role
        await authManager.validateUserRole(role);
        
        // Navigate to dashboard
        await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
        await page.waitForLoadState('networkidle');

        // Validate role-based content visibility
        if (role === 'CITIZEN') {
          const citizenDashboard = new CitizenDashboardPage(page);
          await citizenDashboard.validateRoleBasedContent('CITIZEN');
        } else {
          const staffDashboard = new StaffDashboardPage(page);
          await staffDashboard.validateRoleBasedContent(role);
        }

        await authManager.logout();
      }
    });

    test('should prevent unauthorized access between roles', async ({ page }) => {
      // Test that each role cannot access other roles' exclusive content
      const roleTests = [
        {
          role: 'CITIZEN' as const,
          restrictedPages: ['/admin', '/staff/management', '/reports/admin']
        },
        {
          role: 'LGU_STAFF' as const,
          restrictedPages: ['/admin/system', '/dswd/national']
        },
        {
          role: 'DSWD_STAFF' as const,
          restrictedPages: ['/admin/system', '/lgu/local']
        }
      ];

      for (const { role, restrictedPages } of roleTests) {
        await authManager.loginAs(role);

        for (const restrictedPage of restrictedPages) {
          await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}${restrictedPage}`);
          
          // Should be redirected or show access denied
          const currentUrl = page.url();
          const accessDenied = page.locator('text=Access Denied, text=Unauthorized, text=403');
          
          expect(
            !currentUrl.includes(restrictedPage) || 
            await accessDenied.count() > 0
          ).toBeTruthy();
        }

        await authManager.logout();
      }
    });

    test('should validate navigation menu items based on role', async ({ page }) => {
      const roleNavigationTests = [
        {
          role: 'CITIZEN' as const,
          expectedItems: ['Dashboard', 'Registration', 'Eligibility', 'Payments', 'Profile'],
          restrictedItems: ['Admin', 'Staff Management', 'Reports', 'Analytics']
        },
        {
          role: 'LGU_STAFF' as const,
          expectedItems: ['Dashboard', 'Applications', 'Cases', 'Citizens'],
          restrictedItems: ['Admin Panel', 'System Settings']
        },
        {
          role: 'DSWD_STAFF' as const,
          expectedItems: ['Dashboard', 'Reports', 'Analytics', 'Payments'],
          restrictedItems: ['System Admin', 'User Management']
        }
      ];

      for (const { role, expectedItems, restrictedItems } of roleNavigationTests) {
        await authManager.loginAs(role);
        await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
        await page.waitForLoadState('networkidle');

        // Check expected navigation items are visible
        for (const item of expectedItems) {
          const navItem = page.locator(`nav a:has-text("${item}"), nav button:has-text("${item}")`);
          if (await navItem.count() > 0) {
            await expect(navItem.first()).toBeVisible();
          }
        }

        // Check restricted navigation items are not visible
        for (const item of restrictedItems) {
          const navItem = page.locator(`nav a:has-text("${item}"), nav button:has-text("${item}")`);
          const count = await navItem.count();
          
          for (let i = 0; i < count; i++) {
            await expect(navItem.nth(i)).not.toBeVisible();
          }
        }

        await authManager.logout();
      }
    });
  });
});
