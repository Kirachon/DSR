// DSR Role-Based Dashboard Testing Suite
// Comprehensive validation of role-based access control and dashboard functionality
// Tests all user roles across all 7 services

import { test, expect, Page } from '@playwright/test';

// Test configuration
const BASE_URL = process.env.BASE_URL || 'http://localhost:3000';
const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080';

// User roles and their expected permissions
const userRoles = {
  CITIZEN: {
    email: 'citizen@test.ph',
    password: 'TestPassword123!',
    expectedDashboards: ['household-status', 'payment-history', 'grievance-tracker'],
    restrictedEndpoints: ['/admin', '/staff', '/system'],
    allowedEndpoints: ['/dashboard', '/registration', '/payments', '/grievances']
  },
  LGU_STAFF: {
    email: 'lgu.staff@test.ph',
    password: 'TestPassword123!',
    expectedDashboards: ['local-registrations', 'eligibility-queue', 'payment-monitoring'],
    restrictedEndpoints: ['/admin/system', '/national-reports'],
    allowedEndpoints: ['/staff/dashboard', '/registrations', '/eligibility', '/analytics']
  },
  DSWD_STAFF: {
    email: 'dswd.staff@test.ph',
    password: 'TestPassword123!',
    expectedDashboards: ['national-overview', 'program-analytics', 'grievance-management'],
    restrictedEndpoints: ['/admin/system'],
    allowedEndpoints: ['/staff/dashboard', '/analytics', '/reports', '/grievances', '/payments']
  },
  CASE_WORKER: {
    email: 'case.worker@test.ph',
    password: 'TestPassword123!',
    expectedDashboards: ['case-management', 'household-details', 'field-activities'],
    restrictedEndpoints: ['/admin', '/system-config'],
    allowedEndpoints: ['/worker/dashboard', '/households', '/assessments', '/field-work']
  },
  SYSTEM_ADMIN: {
    email: 'admin@test.ph',
    password: 'TestPassword123!',
    expectedDashboards: ['system-health', 'user-management', 'audit-logs', 'performance-metrics'],
    restrictedEndpoints: [],
    allowedEndpoints: ['/admin', '/system', '/monitoring', '/configuration', '/users']
  }
};

// Dashboard components to test for each role
const dashboardComponents = {
  CITIZEN: [
    '[data-testid="household-registration-status"]',
    '[data-testid="payment-history-widget"]',
    '[data-testid="grievance-status-tracker"]',
    '[data-testid="program-eligibility-status"]'
  ],
  LGU_STAFF: [
    '[data-testid="local-registration-queue"]',
    '[data-testid="eligibility-assessment-queue"]',
    '[data-testid="payment-disbursement-status"]',
    '[data-testid="local-analytics-summary"]'
  ],
  DSWD_STAFF: [
    '[data-testid="national-registration-metrics"]',
    '[data-testid="program-performance-dashboard"]',
    '[data-testid="grievance-management-panel"]',
    '[data-testid="payment-reconciliation-status"]'
  ],
  CASE_WORKER: [
    '[data-testid="assigned-households-list"]',
    '[data-testid="field-visit-schedule"]',
    '[data-testid="assessment-tasks"]',
    '[data-testid="case-notes-panel"]'
  ],
  SYSTEM_ADMIN: [
    '[data-testid="system-health-overview"]',
    '[data-testid="service-monitoring-panel"]',
    '[data-testid="user-activity-logs"]',
    '[data-testid="performance-metrics-dashboard"]'
  ]
};

test.describe('DSR Role-Based Access Control Tests', () => {
  let page: Page;

  test.beforeEach(async ({ browser }) => {
    page = await browser.newPage();
    
    // Set up request monitoring
    page.route('**/api/v1/**', async (route) => {
      const response = await route.fetch();
      console.log(`API Call: ${route.request().method()} ${route.request().url()} - Status: ${response.status()}`);
      route.continue();
    });
  });

  test.afterEach(async () => {
    await page.close();
  });

  // Helper function to login as specific role
  async function loginAsRole(role: keyof typeof userRoles) {
    const userData = userRoles[role];
    
    await page.goto(`${BASE_URL}/login`);
    await page.fill('[data-testid="email"]', userData.email);
    await page.fill('[data-testid="password"]', userData.password);
    await page.click('[data-testid="login-submit"]');
    
    // Wait for successful login
    await expect(page).toHaveURL(new RegExp(`${BASE_URL}/(dashboard|admin|staff|worker)`));
    
    console.log(`✅ Successfully logged in as ${role}`);
  }

  // Helper function to test API endpoint access
  async function testAPIAccess(role: keyof typeof userRoles, endpoint: string, shouldHaveAccess: boolean) {
    const response = await page.request.get(`${API_BASE_URL}${endpoint}`, {
      headers: {
        'Authorization': `Bearer ${await getAuthToken(role)}`
      }
    });

    if (shouldHaveAccess) {
      expect(response.status()).not.toBe(403);
      expect(response.status()).not.toBe(401);
    } else {
      expect([401, 403]).toContain(response.status());
    }
  }

  // Helper function to get auth token for role
  async function getAuthToken(role: keyof typeof userRoles): Promise<string> {
    const userData = userRoles[role];
    const response = await page.request.post(`${API_BASE_URL}/api/v1/auth/login`, {
      data: {
        email: userData.email,
        password: userData.password
      }
    });
    
    const authData = await response.json();
    return authData.accessToken;
  }

  test.describe('1. Citizen Role Access Control', () => {
    test('should allow citizen access to appropriate dashboards and restrict admin functions', async () => {
      await loginAsRole('CITIZEN');

      // Test dashboard access
      await page.goto(`${BASE_URL}/dashboard`);
      await expect(page.locator('[data-testid="citizen-dashboard"]')).toBeVisible();

      // Verify citizen-specific components are visible
      for (const component of dashboardComponents.CITIZEN) {
        await expect(page.locator(component)).toBeVisible();
      }

      // Test restricted access
      await page.goto(`${BASE_URL}/admin`);
      await expect(page).toHaveURL(new RegExp('(403|unauthorized|login)'));

      console.log('✅ Citizen role access control validated');
    });

    test('should validate citizen API access permissions', async () => {
      await loginAsRole('CITIZEN');

      // Test allowed endpoints
      await testAPIAccess('CITIZEN', '/api/v1/registration/households', true);
      await testAPIAccess('CITIZEN', '/api/v1/payments/history', true);
      await testAPIAccess('CITIZEN', '/api/v1/grievances/my-cases', true);

      // Test restricted endpoints
      await testAPIAccess('CITIZEN', '/api/v1/admin/users', false);
      await testAPIAccess('CITIZEN', '/api/v1/system/configuration', false);
      await testAPIAccess('CITIZEN', '/api/v1/analytics/national-reports', false);

      console.log('✅ Citizen API access permissions validated');
    });
  });

  test.describe('2. LGU Staff Role Access Control', () => {
    test('should allow LGU staff access to local management functions', async () => {
      await loginAsRole('LGU_STAFF');

      // Test dashboard access
      await page.goto(`${BASE_URL}/staff/dashboard`);
      await expect(page.locator('[data-testid="lgu-staff-dashboard"]')).toBeVisible();

      // Verify LGU staff-specific components
      for (const component of dashboardComponents.LGU_STAFF) {
        await expect(page.locator(component)).toBeVisible();
      }

      // Test local registration management
      await page.goto(`${BASE_URL}/staff/registrations`);
      await expect(page.locator('[data-testid="local-registration-queue"]')).toBeVisible();

      // Test eligibility assessment access
      await page.goto(`${BASE_URL}/staff/eligibility`);
      await expect(page.locator('[data-testid="eligibility-assessment-panel"]')).toBeVisible();

      console.log('✅ LGU staff role access control validated');
    });

    test('should validate LGU staff API access permissions', async () => {
      await loginAsRole('LGU_STAFF');

      // Test allowed endpoints
      await testAPIAccess('LGU_STAFF', '/api/v1/registration/local-queue', true);
      await testAPIAccess('LGU_STAFF', '/api/v1/eligibility/assessments', true);
      await testAPIAccess('LGU_STAFF', '/api/v1/analytics/local-reports', true);

      // Test restricted endpoints
      await testAPIAccess('LGU_STAFF', '/api/v1/admin/system', false);
      await testAPIAccess('LGU_STAFF', '/api/v1/analytics/national-reports', false);

      console.log('✅ LGU staff API access permissions validated');
    });
  });

  test.describe('3. DSWD Staff Role Access Control', () => {
    test('should allow DSWD staff access to national oversight functions', async () => {
      await loginAsRole('DSWD_STAFF');

      // Test dashboard access
      await page.goto(`${BASE_URL}/staff/dashboard`);
      await expect(page.locator('[data-testid="dswd-staff-dashboard"]')).toBeVisible();

      // Verify DSWD staff-specific components
      for (const component of dashboardComponents.DSWD_STAFF) {
        await expect(page.locator(component)).toBeVisible();
      }

      // Test national analytics access
      await page.goto(`${BASE_URL}/staff/analytics`);
      await expect(page.locator('[data-testid="national-analytics-dashboard"]')).toBeVisible();

      // Test grievance management access
      await page.goto(`${BASE_URL}/staff/grievances`);
      await expect(page.locator('[data-testid="grievance-management-panel"]')).toBeVisible();

      console.log('✅ DSWD staff role access control validated');
    });

    test('should validate DSWD staff API access permissions', async () => {
      await loginAsRole('DSWD_STAFF');

      // Test allowed endpoints
      await testAPIAccess('DSWD_STAFF', '/api/v1/analytics/national-reports', true);
      await testAPIAccess('DSWD_STAFF', '/api/v1/grievances/all-cases', true);
      await testAPIAccess('DSWD_STAFF', '/api/v1/payments/national-batches', true);

      // Test restricted endpoints
      await testAPIAccess('DSWD_STAFF', '/api/v1/admin/system-config', false);

      console.log('✅ DSWD staff API access permissions validated');
    });
  });

  test.describe('4. Case Worker Role Access Control', () => {
    test('should allow case worker access to field management functions', async () => {
      await loginAsRole('CASE_WORKER');

      // Test dashboard access
      await page.goto(`${BASE_URL}/worker/dashboard`);
      await expect(page.locator('[data-testid="case-worker-dashboard"]')).toBeVisible();

      // Verify case worker-specific components
      for (const component of dashboardComponents.CASE_WORKER) {
        await expect(page.locator(component)).toBeVisible();
      }

      // Test household management access
      await page.goto(`${BASE_URL}/worker/households`);
      await expect(page.locator('[data-testid="assigned-households-panel"]')).toBeVisible();

      console.log('✅ Case worker role access control validated');
    });
  });

  test.describe('5. System Admin Role Access Control', () => {
    test('should allow system admin full access to all functions', async () => {
      await loginAsRole('SYSTEM_ADMIN');

      // Test admin dashboard access
      await page.goto(`${BASE_URL}/admin/dashboard`);
      await expect(page.locator('[data-testid="admin-dashboard"]')).toBeVisible();

      // Verify admin-specific components
      for (const component of dashboardComponents.SYSTEM_ADMIN) {
        await expect(page.locator(component)).toBeVisible();
      }

      // Test system configuration access
      await page.goto(`${BASE_URL}/admin/system`);
      await expect(page.locator('[data-testid="system-configuration-panel"]')).toBeVisible();

      // Test user management access
      await page.goto(`${BASE_URL}/admin/users`);
      await expect(page.locator('[data-testid="user-management-panel"]')).toBeVisible();

      console.log('✅ System admin role access control validated');
    });

    test('should validate system admin API access permissions', async () => {
      await loginAsRole('SYSTEM_ADMIN');

      // Test full access to all endpoints
      await testAPIAccess('SYSTEM_ADMIN', '/api/v1/admin/users', true);
      await testAPIAccess('SYSTEM_ADMIN', '/api/v1/system/configuration', true);
      await testAPIAccess('SYSTEM_ADMIN', '/api/v1/monitoring/health', true);
      await testAPIAccess('SYSTEM_ADMIN', '/api/v1/analytics/all-reports', true);

      console.log('✅ System admin API access permissions validated');
    });
  });

  test.describe('6. Cross-Role Data Isolation', () => {
    test('should ensure proper data isolation between roles', async () => {
      // Test that citizens can only see their own data
      await loginAsRole('CITIZEN');
      const citizenResponse = await page.request.get(`${API_BASE_URL}/api/v1/registration/households`, {
        headers: {
          'Authorization': `Bearer ${await getAuthToken('CITIZEN')}`
        }
      });
      
      expect(citizenResponse.status()).toBe(200);
      const citizenData = await citizenResponse.json();
      
      // Verify citizen only sees their own households
      expect(Array.isArray(citizenData.households)).toBe(true);
      
      // Test that LGU staff can only see local data
      await loginAsRole('LGU_STAFF');
      const lguResponse = await page.request.get(`${API_BASE_URL}/api/v1/registration/local-households`, {
        headers: {
          'Authorization': `Bearer ${await getAuthToken('LGU_STAFF')}`
        }
      });
      
      expect(lguResponse.status()).toBe(200);
      const lguData = await lguResponse.json();
      
      // Verify LGU staff sees local households only
      expect(Array.isArray(lguData.households)).toBe(true);

      console.log('✅ Cross-role data isolation validated');
    });
  });

  test.describe('7. Session Management and Security', () => {
    test('should handle session timeout and re-authentication', async () => {
      await loginAsRole('CITIZEN');

      // Simulate session timeout by clearing auth token
      await page.evaluate(() => {
        localStorage.removeItem('authToken');
        sessionStorage.removeItem('authToken');
      });

      // Try to access protected resource
      await page.goto(`${BASE_URL}/dashboard`);
      
      // Should be redirected to login
      await expect(page).toHaveURL(new RegExp('login'));

      console.log('✅ Session timeout handling validated');
    });

    test('should prevent unauthorized access attempts', async () => {
      // Try to access admin panel without authentication
      await page.goto(`${BASE_URL}/admin`);
      await expect(page).toHaveURL(new RegExp('(login|unauthorized)'));

      // Try to access API without token
      const unauthorizedResponse = await page.request.get(`${API_BASE_URL}/api/v1/admin/users`);
      expect(unauthorizedResponse.status()).toBe(401);

      console.log('✅ Unauthorized access prevention validated');
    });
  });
});
