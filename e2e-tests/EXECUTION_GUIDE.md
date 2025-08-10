# DSR E2E Test Execution Guide

This guide provides step-by-step instructions for executing the DSR E2E test suite in different environments and scenarios.

## 🚀 Quick Start

### 1. Prerequisites Check

Before running tests, ensure you have:

```bash
# Check Node.js version (18+ required)
node --version

# Check if DSR services are running
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
# ... check all services (8080-8086)

# Verify Podman/Docker is available
podman --version
# or
docker --version
```

### 2. Initial Setup

```bash
# Navigate to test directory
cd e2e-tests

# Run setup script
./scripts/run-tests.sh setup

# Or manual setup:
npm install
npx playwright install --with-deps
cp .env.example .env
```

### 3. Start Mock Frontend

```bash
# Start mock frontend server
./scripts/run-tests.sh start-frontend

# Or manually:
cd mock-frontend
npm start
```

### 4. Run Tests

```bash
# Run all tests
./scripts/run-tests.sh test

# Run specific test suites
./scripts/run-tests.sh smoke
./scripts/run-tests.sh account
./scripts/run-tests.sh modules
```

## 🧪 Test Execution Scenarios

### Smoke Tests (Quick Validation)

```bash
# Run critical functionality tests
npm run test:smoke

# Or using script
./scripts/run-tests.sh smoke --browser chromium
```

**Expected Duration**: 5-10 minutes  
**Coverage**: Login, registration, basic navigation, service health

### Account Functionality Tests

```bash
# Run all account-related tests
npm run test:account

# Specific account test files
npx playwright test tests/account/login.spec.ts
npx playwright test tests/account/registration.spec.ts
npx playwright test tests/account/profile.spec.ts
npx playwright test tests/account/password-reset.spec.ts
```

**Expected Duration**: 15-20 minutes  
**Coverage**: Registration, login/logout, password reset, profile management, session handling

### Module Testing

```bash
# Run all module tests
npm run test:modules

# Individual module tests
npx playwright test tests/modules/registration-service.spec.ts
npx playwright test tests/modules/eligibility-service.spec.ts
npx playwright test tests/modules/payment-service.spec.ts
npx playwright test tests/modules/grievance-service.spec.ts
```

**Expected Duration**: 30-45 minutes  
**Coverage**: All 7 DSR services, CRUD operations, workflows, integrations

### Cross-Browser Testing

```bash
# Chrome (default)
npm run test:chrome

# Firefox
npm run test:firefox

# Safari (WebKit)
npm run test:safari

# Mobile browsers
npm run test:mobile
```

### Integration Tests

```bash
# Cross-module integration tests
npm run test:integration

# End-to-end workflows
npx playwright test tests/integration/complete-workflow.spec.ts
```

**Expected Duration**: 20-30 minutes  
**Coverage**: Cross-service workflows, data consistency, user journeys

### Regression Testing

```bash
# Full regression suite
npm run test:regression

# Or using script with specific browser
./scripts/run-tests.sh regression --browser firefox --workers 2
```

**Expected Duration**: 60-90 minutes  
**Coverage**: All functionality, edge cases, security tests, performance tests

## 🔧 Advanced Execution Options

### Debug Mode

```bash
# Debug specific test
npx playwright test login.spec.ts --debug

# Debug with UI
npm run test:ui

# Debug with headed browser
npm run test:headed
```

### Parallel Execution

```bash
# Run with multiple workers
npx playwright test --workers=4

# Disable parallel execution
npx playwright test --workers=1
```

### Custom Configuration

```bash
# Run with custom timeout
npx playwright test --timeout=60000

# Run with retries
npx playwright test --retries=3

# Run specific test pattern
npx playwright test --grep="@smoke"
```

### Environment-Specific Testing

```bash
# Local environment (default)
npm test

# Staging environment
BASE_URL=https://staging.dsr.gov.ph npm test

# Production environment (read-only tests)
BASE_URL=https://dsr.gov.ph npm run test:smoke
```

## 📊 Test Results and Reporting

### HTML Reports

```bash
# Generate and view HTML report
npm run report:html

# Or manually
npx playwright show-report
```

### Allure Reports

```bash
# Generate Allure report
npm run report:allure

# Or manually
allure generate allure-results --clean
allure open
```

### CI/CD Reports

```bash
# Generate JUnit XML for CI
npm run test:ci

# View JUnit results
cat test-results/junit-report.xml
```

## 🐛 Troubleshooting

### Common Issues and Solutions

#### 1. Services Not Running

```bash
# Check service health
./scripts/run-tests.sh health-check

# Start DSR services
cd ..
./scripts/run-local.sh

# Verify services are up
curl http://localhost:8080/actuator/health
```

#### 2. Mock Frontend Issues

```bash
# Check if frontend is running
curl http://localhost:3000/health

# Restart frontend
./scripts/run-tests.sh stop-frontend
./scripts/run-tests.sh start-frontend

# Check logs
cd mock-frontend
tail -f frontend.log
```

#### 3. Browser Installation Issues

```bash
# Reinstall browsers
npx playwright install --force

# Install system dependencies
npx playwright install-deps
```

#### 4. Test Failures

```bash
# Run with verbose output
npx playwright test --reporter=list

# Capture traces for debugging
npx playwright test --trace=on

# Run single test for debugging
npx playwright test login.spec.ts --headed --debug
```

#### 5. Network/Timeout Issues

```bash
# Increase timeout
export TIMEOUT=60000
npm test

# Check network connectivity
ping localhost
curl -I http://localhost:3000
```

### Debug Commands

```bash
# Check test configuration
npx playwright test --list

# Validate test files
npx playwright test --dry-run

# Show test info
npx playwright test --reporter=json > test-info.json

# Check browser versions
npx playwright --version
```

## 🔄 CI/CD Execution

### GitLab CI/CD

The test suite includes automated CI/CD pipeline execution:

```yaml
# Triggered on:
- Merge requests
- Main branch commits
- Scheduled runs

# Pipeline stages:
1. Setup (install dependencies, start services)
2. Test (run tests across browsers)
3. Report (generate reports, send notifications)
4. Cleanup (stop services, archive results)
```

### Local CI Simulation

```bash
# Simulate CI environment
CI=true HEADLESS=true npm test

# Run with CI configuration
npm run test:ci
```

## 📈 Performance Monitoring

### Test Execution Performance

```bash
# Monitor test execution time
time npm test

# Profile test performance
npx playwright test --reporter=json | jq '.stats'
```

### Application Performance Testing

```bash
# Run performance tests
npx playwright test --grep="@performance"

# Monitor page load times
npx playwright test --trace=on
```

## 🔒 Security Testing

### Security Test Execution

```bash
# Run security-focused tests
npx playwright test --grep="@security"

# Test authentication flows
npx playwright test tests/account/

# Test input validation
npx playwright test tests/security/
```

## ♿ Accessibility Testing

### Accessibility Test Execution

```bash
# Run accessibility tests
npx playwright test --grep="@accessibility"

# Test with screen reader simulation
npx playwright test --project=accessibility
```

## 📱 Mobile Testing

### Mobile Test Execution

```bash
# Run mobile tests
npm run test:mobile

# Test specific mobile devices
npx playwright test --project=mobile-chrome
npx playwright test --project=mobile-safari
```

## 🎯 Test Coverage Analysis

### Coverage Reports

```bash
# Generate coverage report
npm run test:coverage

# View coverage in browser
open coverage/index.html
```

## 📞 Support and Escalation

### When Tests Fail

1. **Check service health**: `./scripts/run-tests.sh health-check`
2. **Review test logs**: Check `test-results/` directory
3. **Run in debug mode**: `npm run test:debug`
4. **Check recent changes**: Review git commits
5. **Contact team**: Create issue with logs and screenshots

### Emergency Procedures

For critical test failures in production:

1. **Immediate**: Stop deployment pipeline
2. **Investigate**: Run smoke tests to identify scope
3. **Isolate**: Test individual components
4. **Report**: Create incident report with details
5. **Fix**: Address root cause and re-test

---

**Remember**: Always run smoke tests before major deployments and full regression tests before releases.
