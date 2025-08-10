import { test, expect } from '@playwright/test';

test.describe('DSR Authentication Workflow - End-to-End Testing', () => {
  const baseURL = 'http://localhost:3000';
  
  // Test data
  const testUser = {
    firstName: 'Juan',
    lastName: 'Dela Cruz',
    email: 'juan.delacruz@test.com',
    password: 'TestPassword123!',
    phoneNumber: '+63 912 345 6789'
  };

  test.beforeEach(async ({ page }) => {
    // Navigate to homepage before each test
    await page.goto(baseURL);
    await page.waitForLoadState('networkidle');
  });

  test('Complete Registration Workflow', async ({ page }) => {
    console.log('🧪 Testing Complete Registration Workflow');

    // Step 1: Navigate to registration from homepage
    await test.step('Navigate to registration page', async () => {
      await expect(page.locator('h1')).toContainText('Digital Social Registry');
      
      // Click "Get Started" button
      await page.locator('text=Get Started').first().click();
      await page.waitForURL('**/register');
      
      // Verify registration page loaded
      await expect(page.locator('h1')).toContainText('Create Account');
      await expect(page.locator('text=Join the Digital Social Registry')).toBeVisible();
    });

    // Step 2: Fill out registration form
    await test.step('Fill registration form', async () => {
      // Fill personal information
      await page.fill('input[name="firstName"]', testUser.firstName);
      await page.fill('input[name="lastName"]', testUser.lastName);
      await page.fill('input[name="email"]', testUser.email);
      await page.fill('input[name="phoneNumber"]', testUser.phoneNumber);
      
      // Fill password fields
      await page.fill('input[name="password"]', testUser.password);
      await page.fill('input[name="confirmPassword"]', testUser.password);
      
      // Accept terms and conditions
      await page.check('input[name="agreeToTerms"]');
      
      // Verify form is filled correctly
      await expect(page.locator('input[name="firstName"]')).toHaveValue(testUser.firstName);
      await expect(page.locator('input[name="email"]')).toHaveValue(testUser.email);
      await expect(page.locator('input[name="agreeToTerms"]')).toBeChecked();
    });

    // Step 3: Submit registration
    await test.step('Submit registration', async () => {
      // Click create account button
      await page.locator('button:has-text("Create Account")').click();
      
      // Wait for loading state
      await expect(page.locator('button:has-text("Creating Account...")')).toBeVisible();
      
      // Wait for redirect to login page
      await page.waitForURL('**/login**', { timeout: 10000 });
      
      // Verify successful redirect to login
      await expect(page.locator('h1')).toContainText('Welcome Back');
    });

    console.log('✅ Registration workflow completed successfully');
  });

  test('Complete Login Workflow', async ({ page }) => {
    console.log('🧪 Testing Complete Login Workflow');

    // Step 1: Navigate to login page
    await test.step('Navigate to login page', async () => {
      await page.locator('text=Sign In').first().click();
      await page.waitForURL('**/login');
      
      // Verify login page elements
      await expect(page.locator('h1')).toContainText('Welcome Back');
      await expect(page.locator('text=Sign in to your DSR account')).toBeVisible();
      await expect(page.locator('text=Demo Credentials:')).toBeVisible();
    });

    // Step 2: Test form validation
    await test.step('Test form validation', async () => {
      // Try to submit empty form
      await page.locator('button:has-text("Sign In")').click();
      
      // Should show validation message (HTML5 validation)
      const emailInput = page.locator('input[name="email"]');
      await expect(emailInput).toHaveAttribute('required');
    });

    // Step 3: Fill and submit login form
    await test.step('Fill and submit login form', async () => {
      // Fill login credentials
      await page.fill('input[name="email"]', testUser.email);
      await page.fill('input[name="password"]', testUser.password);
      
      // Optional: Check remember me
      await page.check('input[name="rememberMe"]');
      
      // Verify form is filled
      await expect(page.locator('input[name="email"]')).toHaveValue(testUser.email);
      await expect(page.locator('input[name="rememberMe"]')).toBeChecked();
      
      // Submit form
      await page.locator('button:has-text("Sign In")').click();
      
      // Wait for loading state
      await expect(page.locator('button:has-text("Signing In...")')).toBeVisible();
      
      // Wait for redirect to dashboard page
      await page.waitForURL('**/dashboard', { timeout: 10000 });
    });

    // Step 4: Verify successful login
    await test.step('Verify successful login and dashboard page', async () => {
      // Verify dashboard page loaded
      await expect(page.locator('h2')).toContainText('Welcome to DSR Dashboard');
      await expect(page.locator('text=Manage your social protection programs')).toBeVisible();
      
      // Verify dashboard elements
      await expect(page.locator('text=Registration Status')).toBeVisible();
      await expect(page.locator('text=Active')).toBeVisible();
      await expect(page.locator('text=Benefits Received')).toBeVisible();

      // Verify quick actions are available
      await expect(page.locator('text=Update Profile →')).toBeVisible();
      await expect(page.locator('text=Apply Now →')).toBeVisible();
    });

    console.log('✅ Login workflow completed successfully');
  });

  test('Profile Management Workflow', async ({ page }) => {
    console.log('🧪 Testing Profile Management Workflow');

    // First login to access dashboard
    await test.step('Login to access dashboard', async () => {
      await page.goto(`${baseURL}/login`);
      await page.fill('input[name="email"]', testUser.email);
      await page.fill('input[name="password"]', testUser.password);
      await page.locator('button:has-text("Sign In")').click();
      await page.waitForURL('**/dashboard');

      // Navigate to profile from dashboard
      await page.locator('text=Update Profile →').click();
      await page.waitForURL('**/profile');
    });

    // Test profile form functionality
    await test.step('Test profile form interactions', async () => {
      // Verify profile form is editable
      const firstNameInput = page.locator('input[value="Juan"]');
      const lastNameInput = page.locator('input[value="Dela Cruz"]');
      const emailInput = page.locator('input[value="juan.delacruz@email.com"]');
      
      await expect(firstNameInput).toBeVisible();
      await expect(lastNameInput).toBeVisible();
      await expect(emailInput).toBeVisible();
      
      // Test editing profile information
      await firstNameInput.clear();
      await firstNameInput.fill('Juan Carlos');
      await expect(firstNameInput).toHaveValue('Juan Carlos');
      
      // Verify save button is present
      await expect(page.locator('button:has-text("Save Changes")')).toBeVisible();
    });

    // Test navigation from profile
    await test.step('Test profile navigation', async () => {
      // Test quick action links
      await expect(page.locator('text=Test Registration')).toBeVisible();
      await expect(page.locator('text=Back to Login')).toBeVisible();
      await expect(page.locator('text=Back to Home')).toBeVisible();
      
      // Test logout functionality
      await page.locator('button:has-text("Logout")').click();
      await page.waitForURL(baseURL);
      await expect(page.locator('h1')).toContainText('Digital Social Registry');
    });

    console.log('✅ Profile management workflow completed successfully');
  });

  test('Navigation and Cross-Page Links', async ({ page }) => {
    console.log('🧪 Testing Navigation and Cross-Page Links');

    // Test homepage navigation
    await test.step('Test homepage navigation', async () => {
      // Test main navigation buttons
      await expect(page.locator('text=Sign In')).toBeVisible();
      await expect(page.locator('text=Get Started')).toBeVisible();
      await expect(page.locator('text=Register as Citizen')).toBeVisible();
      await expect(page.locator('text=Staff Portal')).toBeVisible();
    });

    // Test login page navigation
    await test.step('Test login page navigation', async () => {
      await page.goto(`${baseURL}/login`);
      
      // Test navigation links
      await expect(page.locator('text=Register here')).toBeVisible();
      await expect(page.locator('text=Forgot password?')).toBeVisible();
      await expect(page.locator('text=← Back to Home')).toBeVisible();
      
      // Test forgot password link
      await page.locator('text=Forgot password?').click();
      await page.waitForURL('**/forgot-password');
      await expect(page.locator('h1')).toContainText('Forgot Password?');
    });

    // Test forgot password workflow
    await test.step('Test forgot password workflow', async () => {
      // Fill email and submit
      await page.fill('input[name="email"]', testUser.email);
      await page.locator('button:has-text("Send Reset Link")').click();
      
      // Wait for success message
      await expect(page.locator('h1')).toContainText('Check Your Email');
      await expect(page.locator(`text=${testUser.email}`)).toBeVisible();
      
      // Test navigation back to login
      await page.locator('text=Back to Login').click();
      await page.waitForURL('**/login');
    });

    // Test register page navigation
    await test.step('Test register page navigation', async () => {
      await page.goto(`${baseURL}/register`);
      
      // Test navigation links
      await expect(page.locator('text=Sign in here')).toBeVisible();
      await expect(page.locator('text=← Back to Home')).toBeVisible();
      
      // Test cross-navigation
      await page.locator('text=Sign in here').click();
      await page.waitForURL('**/login');
      await expect(page.locator('h1')).toContainText('Welcome Back');
    });

    console.log('✅ Navigation testing completed successfully');
  });

  test('Error Handling and Validation', async ({ page }) => {
    console.log('🧪 Testing Error Handling and Validation');

    // Test registration form validation
    await test.step('Test registration validation', async () => {
      await page.goto(`${baseURL}/register`);
      
      // Test password mismatch
      await page.fill('input[name="firstName"]', 'Test');
      await page.fill('input[name="lastName"]', 'User');
      await page.fill('input[name="email"]', 'test@example.com');
      await page.fill('input[name="password"]', 'password123');
      await page.fill('input[name="confirmPassword"]', 'differentpassword');
      
      await page.locator('button:has-text("Create Account")').click();
      
      // Should show password mismatch error
      await expect(page.locator('text=Passwords do not match')).toBeVisible();
      
      // Test terms agreement requirement
      await page.fill('input[name="confirmPassword"]', 'password123');
      await page.locator('button:has-text("Create Account")').click();
      
      // Should show terms agreement error
      await expect(page.locator('text=Please agree to the terms and conditions')).toBeVisible();
    });

    // Test login form validation
    await test.step('Test login validation', async () => {
      await page.goto(`${baseURL}/login`);
      
      // Test empty form submission
      await page.locator('button:has-text("Sign In")').click();
      
      // HTML5 validation should prevent submission
      const emailInput = page.locator('input[name="email"]');
      await expect(emailInput).toHaveAttribute('required');
    });

    console.log('✅ Error handling testing completed successfully');
  });

  test('Responsive Design and Mobile Testing', async ({ page }) => {
    console.log('🧪 Testing Responsive Design and Mobile');

    // Test mobile viewport
    await test.step('Test mobile viewport', async () => {
      await page.setViewportSize({ width: 375, height: 667 }); // iPhone SE
      
      // Test homepage on mobile
      await expect(page.locator('h1')).toContainText('Digital Social Registry');
      
      // Verify mobile-friendly layout
      const heroSection = page.locator('text=Empowering Filipino Families');
      await expect(heroSection).toBeVisible();
      
      // Test navigation buttons are accessible
      await expect(page.locator('text=Get Started')).toBeVisible();
      await expect(page.locator('text=Sign In')).toBeVisible();
    });

    // Test tablet viewport
    await test.step('Test tablet viewport', async () => {
      await page.setViewportSize({ width: 768, height: 1024 }); // iPad
      
      await page.goto(`${baseURL}/login`);
      
      // Verify form layout on tablet
      await expect(page.locator('input[name="email"]')).toBeVisible();
      await expect(page.locator('input[name="password"]')).toBeVisible();
      await expect(page.locator('button:has-text("Sign In")')).toBeVisible();
    });

    // Test desktop viewport
    await test.step('Test desktop viewport', async () => {
      await page.setViewportSize({ width: 1920, height: 1080 }); // Desktop
      
      await page.goto(`${baseURL}/profile`);
      
      // Login first
      await page.goto(`${baseURL}/login`);
      await page.fill('input[name="email"]', testUser.email);
      await page.fill('input[name="password"]', testUser.password);
      await page.locator('button:has-text("Sign In")').click();
      await page.waitForURL('**/profile');
      
      // Verify desktop layout
      await expect(page.locator('text=🎉 Login Successful!')).toBeVisible();
      await expect(page.locator('text=Juan Dela Cruz')).toBeVisible();
    });

    console.log('✅ Responsive design testing completed successfully');
  });

  test('Complete End-to-End User Journey', async ({ page }) => {
    console.log('🧪 Testing Complete End-to-End User Journey');

    // Step 1: Registration
    await test.step('Complete user registration', async () => {
      await page.locator('text=Get Started').first().click();
      await page.waitForURL('**/register');

      await page.fill('input[name="firstName"]', testUser.firstName);
      await page.fill('input[name="lastName"]', testUser.lastName);
      await page.fill('input[name="email"]', testUser.email);
      await page.fill('input[name="phoneNumber"]', testUser.phoneNumber);
      await page.fill('input[name="password"]', testUser.password);
      await page.fill('input[name="confirmPassword"]', testUser.password);
      await page.check('input[name="agreeToTerms"]');

      await page.locator('button:has-text("Create Account")').click();
      await page.waitForURL('**/login**');
    });

    // Step 2: Login
    await test.step('Login with new account', async () => {
      await page.fill('input[name="email"]', testUser.email);
      await page.fill('input[name="password"]', testUser.password);
      await page.locator('button:has-text("Sign In")').click();
      await page.waitForURL('**/dashboard');

      await expect(page.locator('text=Welcome to DSR Dashboard')).toBeVisible();
    });

    // Step 3: Verify Dashboard
    await test.step('Verify dashboard content', async () => {
      await expect(page.locator('h2')).toContainText('Welcome to DSR Dashboard');
      await expect(page.locator('text=Registration Status')).toBeVisible();
      await expect(page.locator('text=Active')).toBeVisible();
    });

    // Step 4: Apply for Benefits
    await test.step('Apply for benefits', async () => {
      await page.locator('text=Apply Now →').click();
      await page.waitForURL('**/applications');

      await expect(page.locator('h2')).toContainText('Apply for Social Programs');

      // Select a program
      await page.locator('text=Pantawid Pamilyang Pilipino Program').click();
      await expect(page.locator('text=✓ Selected')).toBeVisible();

      // Fill application form
      await page.selectOption('select', '3-4 members');
      await page.selectOption('select >> nth=1', 'Below ₱10,000');
      await page.fill('textarea', 'Need assistance for family expenses and children education.');

      // Submit application
      await page.locator('button:has-text("Submit Application")').click();

      // Verify success
      await expect(page.locator('h2')).toContainText('Application Submitted Successfully!');
      await expect(page.locator('text=Application ID:')).toBeVisible();
    });

    // Step 5: Return to Dashboard
    await test.step('Return to dashboard and verify', async () => {
      await page.locator('text=Return to Dashboard').click();
      await page.waitForURL('**/dashboard');

      await expect(page.locator('h2')).toContainText('Welcome to DSR Dashboard');
    });

    // Step 6: Update Profile
    await test.step('Update profile information', async () => {
      await page.locator('text=Update Profile →').click();
      await page.waitForURL('**/profile');

      // Update profile information
      const firstNameInput = page.locator('input[value="Juan"]');
      await firstNameInput.clear();
      await firstNameInput.fill('Juan Carlos');

      await expect(page.locator('button:has-text("Save Changes")')).toBeVisible();
    });

    // Step 7: Logout
    await test.step('Logout and return to homepage', async () => {
      await page.locator('button:has-text("Logout")').click();
      await page.waitForURL(baseURL);

      await expect(page.locator('h1')).toContainText('Digital Social Registry');
      await expect(page.locator('text=Empowering Filipino Families')).toBeVisible();
    });

    console.log('✅ Complete end-to-end user journey completed successfully');
  });

  test('Performance and Load Testing', async ({ page }) => {
    console.log('🧪 Testing Performance and Load');

    await test.step('Measure page load times', async () => {
      const startTime = Date.now();

      await page.goto(baseURL);
      await page.waitForLoadState('networkidle');

      const loadTime = Date.now() - startTime;
      console.log(`Homepage load time: ${loadTime}ms`);

      // Verify load time is reasonable (under 3 seconds)
      expect(loadTime).toBeLessThan(3000);
    });

    await test.step('Test rapid navigation', async () => {
      // Rapidly navigate between pages
      await page.goto(`${baseURL}/login`);
      await page.waitForLoadState('networkidle');

      await page.goto(`${baseURL}/register`);
      await page.waitForLoadState('networkidle');

      await page.goto(`${baseURL}/forgot-password`);
      await page.waitForLoadState('networkidle');

      await page.goto(baseURL);
      await page.waitForLoadState('networkidle');

      // Verify final page loaded correctly
      await expect(page.locator('h1')).toContainText('Digital Social Registry');
    });

    console.log('✅ Performance testing completed successfully');
  });
});
