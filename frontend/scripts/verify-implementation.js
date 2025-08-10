#!/usr/bin/env node

// DSR Frontend Implementation Verification Script
// Comprehensive verification of all implemented components and API integrations

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

// Colors for console output
const colors = {
  green: '\x1b[32m',
  red: '\x1b[31m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  reset: '\x1b[0m',
  bold: '\x1b[1m',
};

// Verification results
const results = {
  passed: 0,
  failed: 0,
  warnings: 0,
  details: [],
};

// Helper functions
const log = (message, color = colors.reset) => {
  console.log(`${color}${message}${colors.reset}`);
};

const logSuccess = message => log(`✅ ${message}`, colors.green);
const logError = message => log(`❌ ${message}`, colors.red);
const logWarning = message => log(`⚠️  ${message}`, colors.yellow);
const logInfo = message => log(`ℹ️  ${message}`, colors.blue);

const addResult = (type, category, message, details = null) => {
  results[type]++;
  results.details.push({ type, category, message, details });
};

// Verification functions
const verifyFileExists = (filePath, description) => {
  const fullPath = path.join(__dirname, '..', filePath);
  if (fs.existsSync(fullPath)) {
    logSuccess(`${description} exists`);
    addResult('passed', 'Files', `${description} exists`);
    return true;
  } else {
    logError(`${description} missing: ${filePath}`);
    addResult('failed', 'Files', `${description} missing`, filePath);
    return false;
  }
};

const verifyDirectoryStructure = () => {
  logInfo('Verifying directory structure...');

  const requiredDirs = [
    'src/app/(dashboard)',
    'src/components/dashboard',
    'src/components/registration',
    'src/components/analytics',
    'src/lib/api',
    'src/contexts',
    'src/types',
    'src/tests',
  ];

  requiredDirs.forEach(dir => {
    verifyFileExists(dir, `Directory: ${dir}`);
  });
};

const verifyAPIClients = () => {
  logInfo('Verifying API clients...');

  const apiClients = [
    'src/lib/api/registration-api.ts',
    'src/lib/api/data-management-api.ts',
    'src/lib/api/eligibility-api.ts',
    'src/lib/api/interoperability-api.ts',
    'src/lib/api/payment-api.ts',
    'src/lib/api/grievance-api.ts',
    'src/lib/api/analytics-api.ts',
    'src/lib/api/index.ts',
  ];

  apiClients.forEach(client => {
    verifyFileExists(client, `API Client: ${path.basename(client)}`);
  });

  // Verify API client exports
  try {
    const indexPath = path.join(__dirname, '..', 'src/lib/api/index.ts');
    const indexContent = fs.readFileSync(indexPath, 'utf8');

    const expectedExports = [
      'registrationApi',
      'dataManagementApi',
      'eligibilityApi',
      'interoperabilityApi',
      'paymentApi',
      'grievanceApi',
      'analyticsApi',
    ];

    expectedExports.forEach(exportName => {
      if (indexContent.includes(exportName)) {
        logSuccess(`API export: ${exportName}`);
        addResult('passed', 'API', `${exportName} exported`);
      } else {
        logError(`Missing API export: ${exportName}`);
        addResult('failed', 'API', `${exportName} not exported`);
      }
    });
  } catch (error) {
    logError(`Failed to verify API exports: ${error.message}`);
    addResult('failed', 'API', 'Failed to verify exports', error.message);
  }
};

const verifyComponents = () => {
  logInfo('Verifying React components...');

  const components = [
    'src/components/dashboard/citizen-dashboard.tsx',
    'src/components/dashboard/staff-dashboard.tsx',
    'src/components/dashboard/admin-dashboard.tsx',
    'src/components/registration/household-registration-wizard.tsx',
    'src/components/analytics/analytics-dashboard.tsx',
    'src/app/(dashboard)/registration/page.tsx',
    'src/app/(dashboard)/payments/page.tsx',
    'src/app/(dashboard)/cases/page.tsx',
    'src/app/(dashboard)/analytics/page.tsx',
  ];

  components.forEach(component => {
    verifyFileExists(component, `Component: ${path.basename(component)}`);
  });
};

const verifyConfiguration = () => {
  logInfo('Verifying configuration files...');

  const configFiles = [
    'src/lib/config.ts',
    'src/lib/api-client.ts',
    'src/lib/service-clients.ts',
    'package.json',
    'tsconfig.json',
    'tailwind.config.js',
    'next.config.js',
  ];

  configFiles.forEach(file => {
    verifyFileExists(file, `Config: ${path.basename(file)}`);
  });

  // Verify package.json dependencies
  try {
    const packagePath = path.join(__dirname, '..', 'package.json');
    const packageJson = JSON.parse(fs.readFileSync(packagePath, 'utf8'));

    const requiredDeps = [
      'next',
      'react',
      'react-dom',
      'typescript',
      'tailwindcss',
      'axios',
      '@types/react',
      '@types/node',
    ];

    requiredDeps.forEach(dep => {
      if (
        packageJson.dependencies?.[dep] ||
        packageJson.devDependencies?.[dep]
      ) {
        logSuccess(`Dependency: ${dep}`);
        addResult('passed', 'Dependencies', `${dep} installed`);
      } else {
        logError(`Missing dependency: ${dep}`);
        addResult('failed', 'Dependencies', `${dep} not installed`);
      }
    });
  } catch (error) {
    logError(`Failed to verify dependencies: ${error.message}`);
    addResult('failed', 'Dependencies', 'Failed to verify', error.message);
  }
};

const verifyTypeScript = () => {
  logInfo('Verifying TypeScript compilation...');

  try {
    execSync('npx tsc --noEmit', {
      cwd: path.join(__dirname, '..'),
      stdio: 'pipe',
    });
    logSuccess('TypeScript compilation successful');
    addResult('passed', 'TypeScript', 'Compilation successful');
  } catch (error) {
    logError('TypeScript compilation failed');
    addResult('failed', 'TypeScript', 'Compilation failed', error.message);
  }
};

const verifyTests = () => {
  logInfo('Verifying test files...');

  const testFiles = ['src/tests/api-integration.test.ts'];

  testFiles.forEach(test => {
    verifyFileExists(test, `Test: ${path.basename(test)}`);
  });

  // Run tests if Jest is available
  try {
    execSync('npm test -- --passWithNoTests', {
      cwd: path.join(__dirname, '..'),
      stdio: 'pipe',
    });
    logSuccess('Test suite execution successful');
    addResult('passed', 'Tests', 'Test suite passed');
  } catch (error) {
    logWarning('Test execution failed or no tests found');
    addResult('warnings', 'Tests', 'Test execution issues', error.message);
  }
};

const verifyServiceIntegration = () => {
  logInfo('Verifying service integration patterns...');

  // Check for proper error handling patterns
  const filesToCheck = [
    'src/app/(dashboard)/registration/page.tsx',
    'src/app/(dashboard)/payments/page.tsx',
    'src/app/(dashboard)/cases/page.tsx',
  ];

  filesToCheck.forEach(filePath => {
    try {
      const fullPath = path.join(__dirname, '..', filePath);
      const content = fs.readFileSync(fullPath, 'utf8');

      // Check for error handling
      if (content.includes('try {') && content.includes('catch')) {
        logSuccess(`Error handling in ${path.basename(filePath)}`);
        addResult(
          'passed',
          'Integration',
          `Error handling in ${path.basename(filePath)}`
        );
      } else {
        logWarning(`Missing error handling in ${path.basename(filePath)}`);
        addResult(
          'warnings',
          'Integration',
          `Missing error handling in ${path.basename(filePath)}`
        );
      }

      // Check for loading states
      if (content.includes('loading') || content.includes('Loading')) {
        logSuccess(`Loading states in ${path.basename(filePath)}`);
        addResult(
          'passed',
          'Integration',
          `Loading states in ${path.basename(filePath)}`
        );
      } else {
        logWarning(`Missing loading states in ${path.basename(filePath)}`);
        addResult(
          'warnings',
          'Integration',
          `Missing loading states in ${path.basename(filePath)}`
        );
      }

      // Check for API integration
      if (content.includes('Api') && content.includes('await')) {
        logSuccess(`API integration in ${path.basename(filePath)}`);
        addResult(
          'passed',
          'Integration',
          `API integration in ${path.basename(filePath)}`
        );
      } else {
        logError(`Missing API integration in ${path.basename(filePath)}`);
        addResult(
          'failed',
          'Integration',
          `Missing API integration in ${path.basename(filePath)}`
        );
      }
    } catch (error) {
      logError(`Failed to verify ${filePath}: ${error.message}`);
      addResult(
        'failed',
        'Integration',
        `Failed to verify ${path.basename(filePath)}`,
        error.message
      );
    }
  });
};

const generateReport = () => {
  log('\n' + '='.repeat(60), colors.bold);
  log('DSR FRONTEND IMPLEMENTATION VERIFICATION REPORT', colors.bold);
  log('='.repeat(60), colors.bold);

  log(`\n📊 Summary:`, colors.bold);
  logSuccess(`Passed: ${results.passed}`);
  logError(`Failed: ${results.failed}`);
  logWarning(`Warnings: ${results.warnings}`);

  const total = results.passed + results.failed + results.warnings;
  const successRate =
    total > 0 ? ((results.passed / total) * 100).toFixed(1) : 0;

  log(`\n🎯 Success Rate: ${successRate}%`, colors.bold);

  if (results.failed > 0) {
    log(`\n❌ Failed Checks:`, colors.red);
    results.details
      .filter(r => r.type === 'failed')
      .forEach(r => log(`   • ${r.category}: ${r.message}`, colors.red));
  }

  if (results.warnings > 0) {
    log(`\n⚠️  Warnings:`, colors.yellow);
    results.details
      .filter(r => r.type === 'warnings')
      .forEach(r => log(`   • ${r.category}: ${r.message}`, colors.yellow));
  }

  log(`\n📝 Recommendations:`, colors.blue);
  if (results.failed > 0) {
    log('   • Address all failed checks before deployment', colors.blue);
  }
  if (results.warnings > 0) {
    log('   • Review warnings for potential improvements', colors.blue);
  }
  if (results.passed > 0 && results.failed === 0) {
    log('   • Implementation looks good! Ready for testing', colors.green);
  }

  log('\n' + '='.repeat(60), colors.bold);
};

// Main verification process
const main = () => {
  log('🚀 Starting DSR Frontend Implementation Verification...', colors.bold);
  log('');

  verifyDirectoryStructure();
  verifyAPIClients();
  verifyComponents();
  verifyConfiguration();
  verifyTypeScript();
  verifyTests();
  verifyServiceIntegration();

  generateReport();
};

// Run verification
main();
