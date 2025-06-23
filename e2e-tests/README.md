# DSR E2E Test Suite

Comprehensive end-to-end testing suite for the Dynamic Social Registry (DSR) system using Playwright with TypeScript.

## 🎯 Overview

This test suite provides comprehensive coverage of:
- **Account Management**: Registration, login, logout, password reset, profile management
- **Module Testing**: All 7 DSR core services and their workflows
- **Cross-browser Testing**: Chrome, Firefox, Safari, and mobile browsers
- **Security Testing**: Authentication, authorization, input validation
- **Performance Testing**: Load times, responsiveness, user experience
- **Accessibility Testing**: WCAG compliance, screen reader support

## 🏗️ Architecture

```
e2e-tests/
├── src/
│   ├── pages/           # Page Object Model classes
│   │   ├── auth/        # Authentication pages
│   │   ├── modules/     # DSR module pages
│   │   └── base-page.ts # Base page class
│   ├── utils/           # Utilities and helpers
│   │   ├── api-client.ts      # API interaction client
│   │   ├── test-data-manager.ts # Test data generation
│   │   └── auth-helper.ts     # Authentication utilities
│   └── setup/           # Global setup and teardown
├── tests/
│   ├── account/         # Account functionality tests
│   ├── modules/         # Module-specific tests
│   └── integration/     # Cross-module integration tests
├── mock-frontend/       # Mock frontend for testing
└── playwright.config.ts # Playwright configuration
```

## 🚀 Quick Start

### Prerequisites

- Node.js 18+
- DSR backend services running
- Podman or Docker (for containerized testing)

### Installation

```bash
# Clone and navigate to test directory
cd e2e-tests

# Install dependencies
npm install

# Install Playwright browsers
npm run setup

# Copy environment configuration
cp .env.example .env
```

### Running Tests

```bash
# Run all tests
npm test

# Run specific test suites
npm run test:account        # Account functionality
npm run test:modules        # Module testing
npm run test:integration    # Integration tests

# Run on specific browsers
npm run test:chrome         # Chrome only
npm run test:firefox        # Firefox only
npm run test:safari         # Safari only
npm run test:mobile         # Mobile browsers

# Run with UI mode
npm run test:ui

# Run in headed mode (see browser)
npm run test:headed

# Run smoke tests only
npm run test:smoke

# Run regression tests
npm run test:regression
```

## 🧪 Test Categories

### Account Testing

- **Registration**: Valid/invalid data, email validation, password strength
- **Login/Logout**: Credentials validation, session management, remember me
- **Password Reset**: Email validation, reset flow, security
- **Profile Management**: View/edit user details, data validation
- **Authentication State**: Session persistence, timeout handling
- **PhilSys Integration**: QR code scanning, identity verification

### Module Testing

#### 1. Registration Service
- Household registration workflows
- PhilSys integration testing
- Document upload validation
- Multi-channel access testing

#### 2. Data Management Service
- Data ingestion and validation
- De-duplication logic
- Historical data archiving
- Data consistency checks

#### 3. Eligibility Service
- Program eligibility assessment
- Criteria validation
- Appeal processes
- Recommendation engine

#### 4. Interoperability Service
- External system integration
- Data exchange protocols
- API compatibility
- Error handling

#### 5. Payment Service
- Payment processing workflows
- Disbursement tracking
- Bank integration
- Transaction security

#### 6. Grievance Service
- Case filing and routing
- Status tracking
- Resolution workflows
- SLA compliance

#### 7. Analytics Service
- Dashboard functionality
- Report generation
- Data visualization
- Performance metrics

## 🔧 Configuration

### Environment Variables

```bash
# Base URLs
BASE_URL=http://localhost:3000
API_BASE_URL=http://localhost:8080

# Service URLs
REGISTRATION_SERVICE_URL=http://localhost:8080
DATA_MANAGEMENT_SERVICE_URL=http://localhost:8081
# ... (see .env.example for full list)

# Test Credentials
TEST_ADMIN_EMAIL=admin@test.dsr.gov.ph
TEST_ADMIN_PASSWORD=TestAdmin123!
TEST_USER_EMAIL=user@test.dsr.gov.ph
TEST_USER_PASSWORD=TestUser123!

# Test Configuration
HEADLESS=true
TIMEOUT=30000
RETRIES=2
```

### Playwright Configuration

Key configuration options in `playwright.config.ts`:

- **Cross-browser support**: Chrome, Firefox, Safari, Mobile
- **Parallel execution**: Configurable worker count
- **Retry logic**: Automatic retry on failure
- **Screenshots/Videos**: Capture on failure
- **Trace collection**: Detailed debugging information

## 📊 Reporting

### HTML Reports
```bash
npm run report:html
```
Interactive HTML report with test results, screenshots, and traces.

### Allure Reports
```bash
npm run report:allure
```
Comprehensive Allure reports with detailed analytics.

### JUnit Reports
Automatically generated for CI/CD integration.

## 🔄 CI/CD Integration

### GitLab CI/CD

The test suite includes a complete GitLab CI/CD pipeline:

```yaml
# .gitlab-ci.yml stages:
- setup      # Install dependencies, start services
- test       # Run tests across browsers
- report     # Generate and publish reports
- cleanup    # Clean up resources
```

### Running in CI

```bash
# Set environment variables in GitLab CI/CD settings
CI=true
HEADLESS=true
PARALLEL_WORKERS=1
RETRIES=2
```

## 🎭 Page Object Model

The test suite uses the Page Object Model pattern for maintainability:

```typescript
// Example: Login Page Object
export class LoginPage extends BasePage {
  private readonly emailInput = this.page.locator('[data-testid="email-input"]');
  private readonly passwordInput = this.page.locator('[data-testid="password-input"]');
  
  async login(email: string, password: string): Promise<void> {
    await this.fillField(this.emailInput, email);
    await this.fillField(this.passwordInput, password);
    await this.clickButton(this.loginButton, true);
  }
}
```

## 🛡️ Security Testing

- **Input Validation**: SQL injection, XSS prevention
- **Authentication**: Token validation, session security
- **Authorization**: Role-based access control
- **Data Protection**: Sensitive data handling
- **CSRF Protection**: Cross-site request forgery prevention

## ♿ Accessibility Testing

- **WCAG 2.1 AA Compliance**: Automated accessibility checks
- **Screen Reader Support**: ARIA labels and roles
- **Keyboard Navigation**: Tab order and focus management
- **Color Contrast**: Visual accessibility validation

## 📱 Mobile Testing

- **Responsive Design**: Multiple viewport sizes
- **Touch Interactions**: Mobile-specific gestures
- **Performance**: Mobile network conditions
- **Cross-platform**: iOS and Android simulation

## 🔍 Debugging

### Debug Mode
```bash
npm run test:debug
```

### UI Mode
```bash
npm run test:ui
```

### Trace Viewer
```bash
npx playwright show-trace trace.zip
```

### Screenshots and Videos
Automatically captured on test failures and stored in `test-results/`.

## 📈 Performance Testing

- **Load Time Monitoring**: Page load performance
- **Network Simulation**: Slow 3G, offline scenarios
- **Memory Usage**: Browser resource consumption
- **Lighthouse Integration**: Performance audits

## 🤝 Contributing

1. **Follow naming conventions**: `feature.spec.ts` for test files
2. **Use Page Object Model**: Create page objects for new pages
3. **Add proper test tags**: `@smoke`, `@regression`, `@performance`
4. **Include documentation**: Update README for new features
5. **Test data management**: Use TestDataManager for consistent data

### Test Tags

- `@smoke`: Critical functionality tests
- `@regression`: Full regression test suite
- `@performance`: Performance-related tests
- `@accessibility`: Accessibility compliance tests
- `@security`: Security validation tests
- `@mobile`: Mobile-specific tests

## 🆘 Troubleshooting

### Common Issues

1. **Services not running**: Ensure DSR backend services are started
2. **Browser installation**: Run `npm run setup` to install browsers
3. **Port conflicts**: Check if ports 3000, 8080-8086 are available
4. **Network timeouts**: Increase timeout values in configuration
5. **Authentication failures**: Verify test user credentials

### Debug Commands

```bash
# Check service health
curl http://localhost:8080/actuator/health

# Verify mock frontend
curl http://localhost:3000/health

# Run single test with debug
npx playwright test login.spec.ts --debug

# Generate trace
npx playwright test --trace on
```

## 📞 Support

For issues and questions:
- Create an issue in the project repository
- Contact the DSR development team
- Check the troubleshooting guide above

---

**DSR E2E Test Suite** - Ensuring quality and reliability for the Dynamic Social Registry system.
