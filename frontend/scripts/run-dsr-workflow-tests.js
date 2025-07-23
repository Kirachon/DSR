#!/usr/bin/env node

/**
 * DSR Authentication Workflow Test Runner
 * Runs comprehensive end-to-end testing with headed browsers
 */

const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs');

// Configuration
const CONFIG = {
  testFile: 'dsr-authentication-workflow.spec.ts',
  configFile: 'playwright.headed.config.ts',
  browsers: ['chromium-headed', 'firefox-headed', 'webkit-headed'],
  reportDir: 'playwright-report',
  resultsDir: 'test-results'
};

// Colors for console output
const colors = {
  reset: '\x1b[0m',
  bright: '\x1b[1m',
  red: '\x1b[31m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  magenta: '\x1b[35m',
  cyan: '\x1b[36m'
};

function log(message, color = 'reset') {
  console.log(`${colors[color]}${message}${colors.reset}`);
}

function logHeader(message) {
  const border = '='.repeat(60);
  log(border, 'cyan');
  log(`🧪 ${message}`, 'cyan');
  log(border, 'cyan');
}

function logStep(step, message) {
  log(`${step} ${message}`, 'blue');
}

function logSuccess(message) {
  log(`✅ ${message}`, 'green');
}

function logError(message) {
  log(`❌ ${message}`, 'red');
}

function logWarning(message) {
  log(`⚠️  ${message}`, 'yellow');
}

async function checkPrerequisites() {
  logStep('🔍', 'Checking prerequisites...');

  // Check if frontend server is running
  try {
    const http = require('http');

    await new Promise((resolve, reject) => {
      const req = http.get('http://localhost:3000', (res) => {
        if (res.statusCode === 200) {
          logSuccess('Frontend server is running on http://localhost:3000');
          resolve();
        } else {
          reject(new Error('Server not responding correctly'));
        }
      });

      req.on('error', (error) => {
        reject(error);
      });

      req.setTimeout(5000, () => {
        req.destroy();
        reject(new Error('Request timeout'));
      });
    });
  } catch (error) {
    logError('Frontend server is not running on http://localhost:3000');
    logWarning('Please start the frontend server with: npm run dev');
    process.exit(1);
  }
  
  // Check if test files exist
  const testFilePath = path.join(__dirname, '..', 'e2e', CONFIG.testFile);
  if (!fs.existsSync(testFilePath)) {
    logError(`Test file not found: ${CONFIG.testFile}`);
    process.exit(1);
  }
  
  const configFilePath = path.join(__dirname, '..', CONFIG.configFile);
  if (!fs.existsSync(configFilePath)) {
    logError(`Config file not found: ${CONFIG.configFile}`);
    process.exit(1);
  }
  
  logSuccess('All prerequisites met');
}

function runPlaywrightTest(browser = null, testName = null) {
  return new Promise((resolve, reject) => {
    const args = [
      'test',
      '--config', CONFIG.configFile
    ];
    
    if (browser) {
      args.push('--project', browser);
    }
    
    if (testName) {
      args.push('--grep', testName);
    } else {
      args.push(CONFIG.testFile);
    }
    
    // Add headed mode flags
    args.push('--headed');
    
    logStep('🚀', `Running: npx playwright ${args.join(' ')}`);
    
    const process = spawn('npx', ['playwright', ...args], {
      stdio: 'inherit',
      cwd: path.join(__dirname, '..')
    });
    
    process.on('close', (code) => {
      if (code === 0) {
        resolve();
      } else {
        reject(new Error(`Test failed with exit code ${code}`));
      }
    });
    
    process.on('error', (error) => {
      reject(error);
    });
  });
}

async function runTestSuite() {
  logHeader('DSR Authentication Workflow Testing');
  
  try {
    // Check prerequisites
    await checkPrerequisites();
    
    // Run comprehensive test suite
    logStep('🧪', 'Running comprehensive authentication workflow tests...');
    
    // Test 1: Complete Registration Workflow
    logStep('1️⃣', 'Testing Registration Workflow...');
    await runPlaywrightTest('chromium-headed', 'Complete Registration Workflow');
    logSuccess('Registration workflow test completed');
    
    // Test 2: Complete Login Workflow
    logStep('2️⃣', 'Testing Login Workflow...');
    await runPlaywrightTest('chromium-headed', 'Complete Login Workflow');
    logSuccess('Login workflow test completed');
    
    // Test 3: Profile Management
    logStep('3️⃣', 'Testing Profile Management...');
    await runPlaywrightTest('chromium-headed', 'Profile Management Workflow');
    logSuccess('Profile management test completed');
    
    // Test 4: Navigation Testing
    logStep('4️⃣', 'Testing Navigation and Cross-Page Links...');
    await runPlaywrightTest('chromium-headed', 'Navigation and Cross-Page Links');
    logSuccess('Navigation test completed');
    
    // Test 5: Error Handling
    logStep('5️⃣', 'Testing Error Handling and Validation...');
    await runPlaywrightTest('chromium-headed', 'Error Handling and Validation');
    logSuccess('Error handling test completed');
    
    // Test 6: Responsive Design
    logStep('6️⃣', 'Testing Responsive Design...');
    await runPlaywrightTest('chromium-headed', 'Responsive Design and Mobile Testing');
    logSuccess('Responsive design test completed');
    
    // Test 7: Complete End-to-End Journey
    logStep('7️⃣', 'Testing Complete End-to-End User Journey...');
    await runPlaywrightTest('chromium-headed', 'Complete End-to-End User Journey');
    logSuccess('End-to-end journey test completed');
    
    // Test 8: Performance Testing
    logStep('8️⃣', 'Testing Performance and Load...');
    await runPlaywrightTest('chromium-headed', 'Performance and Load Testing');
    logSuccess('Performance test completed');
    
    // Cross-browser testing (optional)
    if (process.argv.includes('--cross-browser')) {
      logStep('🌐', 'Running cross-browser tests...');
      
      for (const browser of ['firefox-headed', 'webkit-headed']) {
        logStep('🔄', `Testing with ${browser}...`);
        await runPlaywrightTest(browser, 'Complete Login Workflow');
        logSuccess(`${browser} test completed`);
      }
    }
    
    // Generate final report
    logStep('📊', 'Generating test report...');
    
    const reportPath = path.join(__dirname, '..', CONFIG.reportDir, 'index.html');
    if (fs.existsSync(reportPath)) {
      logSuccess(`Test report generated: ${reportPath}`);
      logStep('🌐', 'Open the report in your browser to view detailed results');
    }
    
    logHeader('🎉 ALL TESTS COMPLETED SUCCESSFULLY! 🎉');
    
    log('\n📋 Test Summary:', 'bright');
    log('✅ Registration workflow - PASSED', 'green');
    log('✅ Login workflow - PASSED', 'green');
    log('✅ Profile management - PASSED', 'green');
    log('✅ Navigation testing - PASSED', 'green');
    log('✅ Error handling - PASSED', 'green');
    log('✅ Responsive design - PASSED', 'green');
    log('✅ End-to-end journey - PASSED', 'green');
    log('✅ Performance testing - PASSED', 'green');
    
    log('\n🚀 DSR Authentication System is production-ready!', 'bright');
    
  } catch (error) {
    logError(`Test suite failed: ${error.message}`);
    process.exit(1);
  }
}

// Handle command line arguments
if (process.argv.includes('--help') || process.argv.includes('-h')) {
  log('DSR Authentication Workflow Test Runner', 'bright');
  log('');
  log('Usage: node run-dsr-workflow-tests.js [options]', 'blue');
  log('');
  log('Options:', 'bright');
  log('  --cross-browser    Run tests across multiple browsers', 'blue');
  log('  --help, -h         Show this help message', 'blue');
  log('');
  log('Examples:', 'bright');
  log('  node run-dsr-workflow-tests.js', 'blue');
  log('  node run-dsr-workflow-tests.js --cross-browser', 'blue');
  process.exit(0);
}

// Run the test suite
runTestSuite().catch((error) => {
  logError(`Unexpected error: ${error.message}`);
  process.exit(1);
});
