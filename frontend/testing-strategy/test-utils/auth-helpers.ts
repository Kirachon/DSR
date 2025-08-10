import { Page } from '@playwright/test';

/**
 * Authentication helper utilities for E2E tests
 */

export interface TestUser {
  email: string;
  password: string;
  role: 'CITIZEN' | 'DSWD_STAFF' | 'LGU_STAFF' | 'ADMIN';
  theme: 'citizen' | 'dswd-staff' | 'lgu-staff';
}

export const testUsers: Record<string, TestUser> = {
  citizen: {
    email: 'citizen@dsr.test',
    password: 'TestPassword123!',
    role: 'CITIZEN',
    theme: 'citizen',
  },
  dswdStaff: {
    email: 'staff@dsr.gov.ph',
    password: 'StaffPassword123!',
    role: 'DSWD_STAFF',
    theme: 'dswd-staff',
  },
  lguStaff: {
    email: 'lgu@local.gov.ph',
    password: 'LguPassword123!',
    role: 'LGU_STAFF',
    theme: 'lgu-staff',
  },
  admin: {
    email: 'admin@dsr.gov.ph',
    password: 'AdminPassword123!',
    role: 'ADMIN',
    theme: 'dswd-staff',
  },
};

/**
 * Login as a specific user type
 */
export async function loginAs(page: Page, userType: keyof typeof testUsers): Promise<void> {
  const user = testUsers[userType];
  
  // Set theme preference before login
  await page.addInitScript((theme) => {
    localStorage.setItem('dsr-theme-preference', theme);
  }, user.theme);
  
  // Navigate to login page
  await page.goto('/login');
  
  // Fill login form
  await page.fill('[data-testid="email"]', user.email);
  await page.fill('[data-testid="password"]', user.password);
  
  // Submit login
  await page.click('[data-testid="login-button"]');
  
  // Wait for successful login (redirect to dashboard)
  await page.waitForURL(/\/dashboard/);
  
  // Verify theme is applied
  await page.waitForSelector(`[data-theme="${user.theme}"]`);
}

/**
 * Logout current user
 */
export async function logout(page: Page): Promise<void> {
  // Click user menu
  await page.click('[data-testid="user-menu"]');
  
  // Click logout
  await page.click('[data-testid="logout-button"]');
  
  // Wait for redirect to login page
  await page.waitForURL(/\/login/);
}

/**
 * Setup authenticated session without UI interaction
 */
export async function setupAuthenticatedSession(
  page: Page, 
  userType: keyof typeof testUsers
): Promise<void> {
  const user = testUsers[userType];
  
  // Mock authentication state
  await page.addInitScript((userData) => {
    // Set authentication tokens
    localStorage.setItem('dsr-access-token', 'mock-access-token');
    localStorage.setItem('dsr-refresh-token', 'mock-refresh-token');
    localStorage.setItem('dsr-user', JSON.stringify(userData));
    localStorage.setItem('dsr-theme-preference', userData.theme);
  }, user);
  
  // Set authentication cookies
  await page.context().addCookies([
    {
      name: 'dsr-session',
      value: 'mock-session-id',
      domain: 'localhost',
      path: '/',
    },
  ]);
}

/**
 * Verify user is authenticated
 */
export async function verifyAuthenticated(page: Page, userType: keyof typeof testUsers): Promise<void> {
  const user = testUsers[userType];
  
  // Check for authenticated elements
  await page.waitForSelector('[data-testid="user-menu"]');
  
  // Verify theme is applied
  await page.waitForSelector(`[data-theme="${user.theme}"]`);
  
  // Verify role-specific navigation
  switch (user.role) {
    case 'CITIZEN':
      await page.waitForSelector('[data-testid="citizen-nav"]');
      break;
    case 'DSWD_STAFF':
      await page.waitForSelector('[data-testid="dswd-staff-nav"]');
      break;
    case 'LGU_STAFF':
      await page.waitForSelector('[data-testid="lgu-staff-nav"]');
      break;
    case 'ADMIN':
      await page.waitForSelector('[data-testid="admin-nav"]');
      break;
  }
}

/**
 * Switch user role (for admin testing)
 */
export async function switchUserRole(
  page: Page, 
  newRole: TestUser['role']
): Promise<void> {
  // Only admins can switch roles
  await page.click('[data-testid="admin-menu"]');
  await page.click('[data-testid="switch-role"]');
  await page.selectOption('[data-testid="role-selector"]', newRole);
  await page.click('[data-testid="confirm-role-switch"]');
  
  // Wait for role switch to complete
  await page.waitForSelector(`[data-role="${newRole}"]`);
}

/**
 * Create test user account
 */
export async function createTestUser(
  page: Page,
  userData: Partial<TestUser> & { email: string; password: string }
): Promise<void> {
  await page.goto('/register');
  
  await page.fill('[data-testid="email"]', userData.email);
  await page.fill('[data-testid="password"]', userData.password);
  await page.fill('[data-testid="confirm-password"]', userData.password);
  await page.fill('[data-testid="full-name"]', 'Test User');
  
  await page.check('[data-testid="terms-checkbox"]');
  await page.click('[data-testid="register-button"]');
  
  // Wait for registration success
  await page.waitForSelector('[data-testid="registration-success"]');
}

/**
 * Reset user password
 */
export async function resetPassword(page: Page, email: string): Promise<void> {
  await page.goto('/forgot-password');
  await page.fill('[data-testid="email"]', email);
  await page.click('[data-testid="reset-password-button"]');
  
  // Wait for reset email sent confirmation
  await page.waitForSelector('[data-testid="reset-email-sent"]');
}

/**
 * Verify user permissions
 */
export async function verifyUserPermissions(
  page: Page,
  expectedPermissions: string[]
): Promise<void> {
  // Check that user has access to expected features
  for (const permission of expectedPermissions) {
    await page.waitForSelector(`[data-permission="${permission}"]`);
  }
}

/**
 * Mock API responses for authentication
 */
export async function mockAuthenticationAPI(page: Page): Promise<void> {
  await page.route('/api/v1/auth/login', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        accessToken: 'mock-access-token',
        refreshToken: 'mock-refresh-token',
        user: {
          id: '1',
          email: 'test@example.com',
          role: 'CITIZEN',
          permissions: ['read:profile', 'write:applications'],
        },
      }),
    });
  });

  await page.route('/api/v1/auth/refresh', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        accessToken: 'new-mock-access-token',
      }),
    });
  });

  await page.route('/api/v1/auth/logout', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ success: true }),
    });
  });
}
