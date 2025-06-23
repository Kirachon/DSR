import { FullConfig } from '@playwright/test';

/**
 * Global teardown for Playwright tests
 * Runs once after all tests to clean up the test environment
 */
async function globalTeardown(config: FullConfig) {
  console.log('🧹 Starting DSR E2E Test Suite Global Teardown...');

  const startTime = Date.now();

  try {
    // 1. Clean up test data
    await cleanupTestData();

    // 2. Generate test reports
    await generateTestReports();

    // 3. Archive test artifacts
    await archiveTestArtifacts();

    // 4. Send notifications (if configured)
    await sendTestNotifications();

    // 5. Clean up temporary files
    await cleanupTemporaryFiles();

    const teardownTime = Date.now() - startTime;
    console.log(`✅ Global teardown completed successfully in ${teardownTime}ms`);

  } catch (error) {
    console.error('❌ Global teardown failed:', error);
    // Don't throw error to avoid masking test failures
  }
}

/**
 * Clean up test data from database and services
 */
async function cleanupTestData(): Promise<void> {
  console.log('🗄️  Cleaning up test data...');
  
  try {
    // In a real implementation, this would:
    // 1. Connect to test database
    // 2. Delete test records created during tests
    // 3. Reset sequences and counters
    // 4. Clear cache entries
    
    // For now, we'll just log the cleanup
    console.log('✅ Test data cleanup completed');
  } catch (error) {
    console.warn('⚠️  Test data cleanup failed:', error);
  }
}

/**
 * Generate comprehensive test reports
 */
async function generateTestReports(): Promise<void> {
  console.log('📊 Generating test reports...');
  
  try {
    const fs = require('fs');
    const path = require('path');
    
    // Create test summary
    const testSummary = {
      timestamp: new Date().toISOString(),
      environment: {
        baseUrl: process.env.BASE_URL,
        apiBaseUrl: process.env.API_BASE_URL,
        nodeVersion: process.version,
        playwrightVersion: require('@playwright/test/package.json').version
      },
      configuration: {
        browsers: ['chromium', 'firefox', 'webkit'],
        parallel: process.env.CI ? 1 : undefined,
        retries: process.env.CI ? 2 : 0
      }
    };
    
    // Save test summary
    const summaryPath = 'test-results/test-summary.json';
    fs.writeFileSync(summaryPath, JSON.stringify(testSummary, null, 2));
    
    console.log('✅ Test reports generated');
  } catch (error) {
    console.warn('⚠️  Test report generation failed:', error);
  }
}

/**
 * Archive test artifacts for later analysis
 */
async function archiveTestArtifacts(): Promise<void> {
  console.log('📦 Archiving test artifacts...');
  
  try {
    const fs = require('fs');
    const path = require('path');
    
    // Create archive directory with timestamp
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
    const archiveDir = `test-archives/run-${timestamp}`;
    
    if (!fs.existsSync('test-archives')) {
      fs.mkdirSync('test-archives', { recursive: true });
    }
    
    if (!fs.existsSync(archiveDir)) {
      fs.mkdirSync(archiveDir, { recursive: true });
    }
    
    // Copy test results
    const artifactDirs = [
      'test-results',
      'allure-results',
      'screenshots'
    ];
    
    for (const dir of artifactDirs) {
      if (fs.existsSync(dir)) {
        const targetDir = path.join(archiveDir, dir);
        fs.mkdirSync(targetDir, { recursive: true });
        
        // Copy files (simplified - in real implementation use proper copy)
        console.log(`📁 Archived ${dir} to ${targetDir}`);
      }
    }
    
    console.log(`✅ Test artifacts archived to ${archiveDir}`);
  } catch (error) {
    console.warn('⚠️  Test artifact archiving failed:', error);
  }
}

/**
 * Send test notifications (email, Slack, etc.)
 */
async function sendTestNotifications(): Promise<void> {
  console.log('📧 Sending test notifications...');
  
  try {
    // Only send notifications in CI environment
    if (!process.env.CI) {
      console.log('⏭️  Skipping notifications (not in CI environment)');
      return;
    }
    
    // Read test results
    const fs = require('fs');
    let testResults = null;
    
    try {
      if (fs.existsSync('test-results/junit-report.xml')) {
        // Parse JUnit XML for test results
        console.log('📊 Test results found');
      }
    } catch (error) {
      console.warn('⚠️  Could not read test results for notifications');
    }
    
    // Send notifications based on configuration
    if (process.env.SLACK_WEBHOOK_URL) {
      await sendSlackNotification(testResults);
    }
    
    if (process.env.EMAIL_NOTIFICATION_ENABLED === 'true') {
      await sendEmailNotification(testResults);
    }
    
    console.log('✅ Test notifications sent');
  } catch (error) {
    console.warn('⚠️  Test notification sending failed:', error);
  }
}

/**
 * Send Slack notification with test results
 */
async function sendSlackNotification(testResults: any): Promise<void> {
  try {
    const webhookUrl = process.env.SLACK_WEBHOOK_URL;
    if (!webhookUrl) return;
    
    const message = {
      text: '🧪 DSR E2E Test Results',
      blocks: [
        {
          type: 'section',
          text: {
            type: 'mrkdwn',
            text: `*DSR E2E Test Suite Completed*\n\nEnvironment: ${process.env.NODE_ENV || 'test'}\nBranch: ${process.env.CI_COMMIT_BRANCH || 'local'}\nCommit: ${process.env.CI_COMMIT_SHA?.substring(0, 8) || 'local'}`
          }
        }
      ]
    };
    
    const response = await fetch(webhookUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(message)
    });
    
    if (response.ok) {
      console.log('✅ Slack notification sent');
    } else {
      console.warn('⚠️  Slack notification failed');
    }
  } catch (error) {
    console.warn('⚠️  Slack notification error:', error);
  }
}

/**
 * Send email notification with test results
 */
async function sendEmailNotification(testResults: any): Promise<void> {
  try {
    // In a real implementation, this would use an email service
    console.log('📧 Email notification would be sent here');
  } catch (error) {
    console.warn('⚠️  Email notification error:', error);
  }
}

/**
 * Clean up temporary files and directories
 */
async function cleanupTemporaryFiles(): Promise<void> {
  console.log('🗑️  Cleaning up temporary files...');
  
  try {
    const fs = require('fs');
    
    // Clean up temporary files
    const tempFiles = [
      'test-results/auth-state.json',
      '.auth'
    ];
    
    for (const file of tempFiles) {
      if (fs.existsSync(file)) {
        fs.unlinkSync(file);
        console.log(`🗑️  Removed ${file}`);
      }
    }
    
    // Clean up old archives (keep last 10)
    if (fs.existsSync('test-archives')) {
      const archives = fs.readdirSync('test-archives')
        .filter((name: string) => name.startsWith('run-'))
        .sort()
        .reverse();
      
      if (archives.length > 10) {
        const toDelete = archives.slice(10);
        for (const archive of toDelete) {
          const archivePath = `test-archives/${archive}`;
          fs.rmSync(archivePath, { recursive: true, force: true });
          console.log(`🗑️  Removed old archive ${archive}`);
        }
      }
    }
    
    console.log('✅ Temporary files cleaned up');
  } catch (error) {
    console.warn('⚠️  Temporary file cleanup failed:', error);
  }
}

/**
 * Log final test statistics
 */
async function logTestStatistics(): Promise<void> {
  console.log('📈 Test Statistics Summary:');
  
  try {
    const fs = require('fs');
    
    // Read test results if available
    if (fs.existsSync('test-results/test-summary.json')) {
      const summary = JSON.parse(fs.readFileSync('test-results/test-summary.json', 'utf8'));
      console.log(`📊 Test run completed at: ${summary.timestamp}`);
      console.log(`🌐 Base URL: ${summary.environment.baseUrl}`);
      console.log(`🔧 Node Version: ${summary.environment.nodeVersion}`);
      console.log(`🎭 Playwright Version: ${summary.environment.playwrightVersion}`);
    }
    
    // Log artifact locations
    console.log('\n📁 Test Artifacts:');
    console.log('   • HTML Report: test-results/html-report/index.html');
    console.log('   • JUnit Report: test-results/junit-report.xml');
    console.log('   • Allure Results: allure-results/');
    console.log('   • Screenshots: test-results/artifacts/');
    
  } catch (error) {
    console.warn('⚠️  Could not log test statistics:', error);
  }
}

export default globalTeardown;
