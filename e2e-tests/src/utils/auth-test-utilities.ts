import { Page, BrowserContext, expect } from '@playwright/test';

/**
 * Authentication Test Utilities
 * Handles role-based authentication for DSR E2E testing
 */

export interface UserCredentials {
  email: string;
  password: string;
  role: 'CITIZEN' | 'LGU_STAFF' | 'DSWD_STAFF' | 'SYSTEM_ADMIN' | 'CASE_WORKER';
  firstName?: string;
  lastName?: string;
}

export interface AuthenticationOptions {
  skipIfAlreadyAuthenticated?: boolean;
  validateRedirect?: boolean;
  expectedRedirectPath?: string;
  timeout?: number;
}

/**
 * Predefined test user credentials for different roles
 */
export const TEST_USERS: Record<string, UserCredentials> = {
  CITIZEN: {
    email: 'citizen@dsr.gov.ph',
    password: 'CitizenTest123!',
    role: 'CITIZEN',
    firstName: 'Juan',
    lastName: 'Dela Cruz'
  },
  LGU_STAFF: {
    email: 'staff@dsr.gov.ph',
    password: 'StaffTest123!',
    role: 'LGU_STAFF',
    firstName: 'Maria',
    lastName: 'Santos'
  },
  DSWD_STAFF: {
    email: 'dswd@dsr.gov.ph',
    password: 'DSWDTest123!',
    role: 'DSWD_STAFF',
    firstName: 'Jose',
    lastName: 'Rizal'
  },
  SYSTEM_ADMIN: {
    email: 'admin@dsr.gov.ph',
    password: 'AdminTest123!',
    role: 'SYSTEM_ADMIN',
    firstName: 'Admin',
    lastName: 'User'
  },
  CASE_WORKER: {
    email: 'caseworker@dsr.gov.ph',
    password: 'CaseWorkerTest123!',
    role: 'CASE_WORKER',
    firstName: 'Ana',
    lastName: 'Garcia'
  }
};

/**
 * Authentication Manager for E2E Tests
 */
export class AuthenticationManager {
  private page: Page;
  private context: BrowserContext;
  private baseUrl: string;

  constructor(page: Page, context?: BrowserContext) {
    this.page = page;
    this.context = context || page.context();
    this.baseUrl = process.env.BASE_URL || 'http://localhost:3000';
  }

  /**
   * Login with specific user credentials
   */
  async loginAs(userType: keyof typeof TEST_USERS, options: AuthenticationOptions = {}): Promise<void> {
    const credentials = TEST_USERS[userType];
    await this.login(credentials, options);
  }

  /**
   * Login with custom credentials
   */
  async login(credentials: UserCredentials, options: AuthenticationOptions = {}): Promise<void> {
    const {
      skipIfAlreadyAuthenticated = true,
      validateRedirect = true,
      expectedRedirectPath,
      timeout = 30000
    } = options;

    // Check if already authenticated
    if (skipIfAlreadyAuthenticated && await this.isAuthenticated()) {
      return;
    }

    // Navigate to login page
    await this.page.goto(`${this.baseUrl}/login`);
    await this.page.waitForLoadState('networkidle');

    // Fill login form
    const emailInput = this.page.locator('input[name="email"], input[type="email"], #email');
    const passwordInput = this.page.locator('input[name="password"], input[type="password"], #password');
    const loginButton = this.page.locator('button[type="submit"], button:has-text("Login"), button:has-text("Sign In")');

    await emailInput.fill(credentials.email);
    await passwordInput.fill(credentials.password);

    // Submit login form
    await Promise.all([
      this.page.waitForResponse(response => 
        response.url().includes('/auth/login') || 
        response.url().includes('/api/auth') ||
        response.url().includes('/login')
      ),
      loginButton.click()
    ]);

    // Wait for authentication to complete
    await this.page.waitForTimeout(2000);

    // Validate successful login
    await this.validateSuccessfulLogin(credentials.role, {
      validateRedirect,
      expectedRedirectPath,
      timeout
    });
  }

  /**
   * Logout current user
   */
  async logout(): Promise<void> {
    // Try multiple logout methods
    const logoutSelectors = [
      '[data-testid="logout-button"]',
      'button:has-text("Logout")',
      'button:has-text("Sign Out")',
      '.logout-button',
      '#logout'
    ];

    let loggedOut = false;

    for (const selector of logoutSelectors) {
      const logoutButton = this.page.locator(selector);
      
      if (await logoutButton.count() > 0 && await logoutButton.isVisible()) {
        // Check if logout button is in a dropdown menu
        const userMenu = this.page.locator('[data-testid="user-menu"], .user-menu');
        if (await userMenu.count() > 0) {
          await userMenu.click();
          await this.page.waitForTimeout(500);
        }

        await logoutButton.click();
        loggedOut = true;
        break;
      }
    }

    if (!loggedOut) {
      // Try clearing storage as fallback
      await this.clearAuthenticationState();
    }

    // Wait for redirect to login page
    await this.page.waitForURL('**/login', { timeout: 10000 });
  }

  /**
   * Check if user is currently authenticated
   */
  async isAuthenticated(): Promise<boolean> {
    try {
      // Check for authentication indicators
      const authIndicators = [
        '[data-testid="user-menu"]',
        '.user-menu',
        '[data-testid="logout-button"]',
        '.logout-button'
      ];

      for (const selector of authIndicators) {
        const element = this.page.locator(selector);
        if (await element.count() > 0 && await element.isVisible()) {
          return true;
        }
      }

      // Check for JWT token in localStorage
      const hasToken = await this.page.evaluate(() => {
        return !!(localStorage.getItem('token') || 
                 localStorage.getItem('authToken') || 
                 localStorage.getItem('accessToken'));
      });

      return hasToken;
    } catch {
      return false;
    }
  }

  /**
   * Get current user information
   */
  async getCurrentUser(): Promise<any> {
    try {
      return await this.page.evaluate(() => {
        const userStr = localStorage.getItem('user') || 
                       localStorage.getItem('currentUser') ||
                       sessionStorage.getItem('user');
        
        return userStr ? JSON.parse(userStr) : null;
      });
    } catch {
      return null;
    }
  }

  /**
   * Validate user has specific role
   */
  async validateUserRole(expectedRole: string): Promise<void> {
    const currentUser = await this.getCurrentUser();
    
    if (!currentUser) {
      throw new Error('No authenticated user found');
    }

    expect(currentUser.role).toBe(expectedRole);
  }

  /**
   * Clear authentication state
   */
  async clearAuthenticationState(): Promise<void> {
    await this.page.evaluate(() => {
      // Clear localStorage
      localStorage.removeItem('token');
      localStorage.removeItem('authToken');
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      localStorage.removeItem('currentUser');
      
      // Clear sessionStorage
      sessionStorage.removeItem('token');
      sessionStorage.removeItem('authToken');
      sessionStorage.removeItem('accessToken');
      sessionStorage.removeItem('user');
      
      // Clear cookies
      document.cookie.split(";").forEach(cookie => {
        const eqPos = cookie.indexOf("=");
        const name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
        document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/";
      });
    });
  }

  /**
   * Set authentication state directly (for testing)
   */
  async setAuthenticationState(user: any, token: string): Promise<void> {
    await this.page.evaluate(({ user, token }) => {
      localStorage.setItem('user', JSON.stringify(user));
      localStorage.setItem('token', token);
      localStorage.setItem('authToken', token);
    }, { user, token });
  }

  /**
   * Validate successful login
   */
  private async validateSuccessfulLogin(
    expectedRole: string, 
    options: { validateRedirect?: boolean; expectedRedirectPath?: string; timeout?: number }
  ): Promise<void> {
    const { validateRedirect = true, expectedRedirectPath, timeout = 30000 } = options;

    // Wait for authentication to be established
    await this.page.waitForFunction(() => {
      return !!(localStorage.getItem('token') || 
               localStorage.getItem('authToken') ||
               document.querySelector('[data-testid="user-menu"]'));
    }, { timeout });

    // Validate user role
    await this.validateUserRole(expectedRole);

    // Validate redirect if specified
    if (validateRedirect) {
      const redirectPath = expectedRedirectPath || this.getExpectedRedirectPath(expectedRole);
      await this.page.waitForURL(`**${redirectPath}`, { timeout });
    }

    // Verify authentication indicators are present
    const isAuth = await this.isAuthenticated();
    expect(isAuth).toBeTruthy();
  }

  /**
   * Get expected redirect path based on user role
   */
  private getExpectedRedirectPath(role: string): string {
    switch (role) {
      case 'CITIZEN':
        return '/dashboard';
      case 'LGU_STAFF':
      case 'DSWD_STAFF':
      case 'CASE_WORKER':
        return '/dashboard';
      case 'SYSTEM_ADMIN':
        return '/admin';
      default:
        return '/dashboard';
    }
  }

  /**
   * Create test user account (for setup)
   */
  async createTestUser(credentials: UserCredentials): Promise<void> {
    // Navigate to registration page
    await this.page.goto(`${this.baseUrl}/register`);
    await this.page.waitForLoadState('networkidle');

    // Fill registration form
    const emailInput = this.page.locator('input[name="email"]');
    const passwordInput = this.page.locator('input[name="password"]');
    const firstNameInput = this.page.locator('input[name="firstName"]');
    const lastNameInput = this.page.locator('input[name="lastName"]');
    const roleSelect = this.page.locator('select[name="role"]');
    const submitButton = this.page.locator('button[type="submit"]');

    await emailInput.fill(credentials.email);
    await passwordInput.fill(credentials.password);
    
    if (credentials.firstName) {
      await firstNameInput.fill(credentials.firstName);
    }
    
    if (credentials.lastName) {
      await lastNameInput.fill(credentials.lastName);
    }

    if (await roleSelect.count() > 0) {
      await roleSelect.selectOption(credentials.role);
    }

    await submitButton.click();
    
    // Wait for registration to complete
    await this.page.waitForTimeout(2000);
  }
}
