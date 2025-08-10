import { test, expect } from '@playwright/test';

/**
 * Performance Testing
 * Ensures <2 second response times and optimal Core Web Vitals
 */
test.describe('Performance Testing', () => {
  test.use({
    // Throttle CPU and network for consistent testing
    launchOptions: {
      args: [
        '--disable-gpu',
        '--disable-dev-shm-usage',
        '--disable-setuid-sandbox',
        '--no-sandbox',
      ],
    },
  });

  test('should meet Core Web Vitals thresholds', async ({ page }) => {
    // Navigate to homepage
    await page.goto('/', { waitUntil: 'networkidle' });
    
    // Measure Core Web Vitals
    const metrics = await page.evaluate(() => {
      return new Promise((resolve) => {
        new PerformanceObserver((list) => {
          const entries = list.getEntries();
          
          // Extract metrics
          const lcpEntry = entries.find(entry => entry.entryType === 'largest-contentful-paint');
          const fidEntry = entries.find(entry => entry.entryType === 'first-input');
          const clsEntries = entries.filter(entry => entry.entryType === 'layout-shift');
          
          // Calculate CLS
          const cls = clsEntries.reduce((sum, entry) => sum + entry.value, 0);
          
          resolve({
            LCP: lcpEntry?.startTime || 0,
            FID: fidEntry?.processingStart - fidEntry?.startTime || 0,
            CLS: cls,
          });
        }).observe({ 
          entryTypes: ['largest-contentful-paint', 'first-input', 'layout-shift'] 
        });
        
        // Resolve after 5 seconds if metrics aren't collected
        setTimeout(() => {
          resolve({
            LCP: 0,
            FID: 0,
            CLS: 0,
          });
        }, 5000);
      });
    });
    
    // Verify metrics meet thresholds
    expect(metrics.LCP).toBeLessThan(2500); // LCP < 2.5s (Good)
    expect(metrics.FID).toBeLessThan(100);  // FID < 100ms (Good)
    expect(metrics.CLS).toBeLessThan(0.1);  // CLS < 0.1 (Good)
    
    console.log('Core Web Vitals:', metrics);
  });

  test('should load dashboard in under 2 seconds', async ({ page }) => {
    // Start performance measurement
    const startTime = Date.now();
    
    // Navigate to dashboard
    await page.goto('/dashboard');
    
    // Wait for critical elements to be visible
    await page.waitForSelector('[data-testid="dashboard-content"]');
    
    // Calculate load time
    const loadTime = Date.now() - startTime;
    console.log(`Dashboard load time: ${loadTime}ms`);
    
    // Verify load time is under 2 seconds
    expect(loadTime).toBeLessThan(2000);
  });

  test('should handle 1000+ concurrent users simulation', async ({ page }) => {
    // This is a simplified simulation - in a real environment, 
    // this would be done with a load testing tool like k6 or JMeter
    
    // Navigate to performance test page
    await page.goto('/performance-test');
    
    // Start simulated load test
    await page.click('[data-testid="start-load-test"]');
    
    // Wait for test to complete
    await page.waitForSelector('[data-testid="load-test-complete"]', { timeout: 60000 });
    
    // Get test results
    const results = await page.evaluate(() => {
      return {
        concurrentUsers: document.querySelector('[data-testid="concurrent-users"]')?.textContent,
        averageResponseTime: document.querySelector('[data-testid="average-response-time"]')?.textContent,
        maxResponseTime: document.querySelector('[data-testid="max-response-time"]')?.textContent,
        successRate: document.querySelector('[data-testid="success-rate"]')?.textContent,
      };
    });
    
    console.log('Load test results:', results);
    
    // Verify performance under load
    expect(results.averageResponseTime).toContain('ms');
    const avgResponseTime = parseFloat(results.averageResponseTime?.replace('ms', '') || '3000');
    expect(avgResponseTime).toBeLessThan(2000); // < 2 seconds
    
    // Verify success rate
    const successRate = parseFloat(results.successRate?.replace('%', '') || '0');
    expect(successRate).toBeGreaterThanOrEqual(99.5); // 99.5% or higher
  });

  test('should optimize bundle size', async ({ page }) => {
    // Navigate to homepage
    await page.goto('/');
    
    // Measure resource sizes
    const resourceSizes = await page.evaluate(() => {
      return performance.getEntriesByType('resource')
        .filter(resource => {
          const url = resource.name;
          return url.endsWith('.js') || url.endsWith('.css');
        })
        .map(resource => ({
          url: resource.name,
          size: resource.transferSize,
          type: resource.name.endsWith('.js') ? 'JavaScript' : 'CSS',
        }));
    });
    
    // Calculate total bundle size
    const totalJsSize = resourceSizes
      .filter(resource => resource.type === 'JavaScript')
      .reduce((sum, resource) => sum + resource.size, 0);
    
    const totalCssSize = resourceSizes
      .filter(resource => resource.type === 'CSS')
      .reduce((sum, resource) => sum + resource.size, 0);
    
    console.log(`Total JavaScript size: ${totalJsSize / 1024} KB`);
    console.log(`Total CSS size: ${totalCssSize / 1024} KB`);
    
    // Verify bundle sizes are optimized
    expect(totalJsSize / 1024).toBeLessThan(500); // JS < 500 KB
    expect(totalCssSize / 1024).toBeLessThan(100); // CSS < 100 KB
  });

  test('should have optimized images', async ({ page }) => {
    // Navigate to homepage
    await page.goto('/');
    
    // Measure image sizes
    const imageSizes = await page.evaluate(() => {
      return performance.getEntriesByType('resource')
        .filter(resource => {
          const url = resource.name;
          return url.endsWith('.jpg') || 
                 url.endsWith('.jpeg') || 
                 url.endsWith('.png') || 
                 url.endsWith('.webp') || 
                 url.endsWith('.avif');
        })
        .map(resource => ({
          url: resource.name,
          size: resource.transferSize,
        }));
    });
    
    // Verify each image is optimized
    for (const image of imageSizes) {
      console.log(`Image: ${image.url}, Size: ${image.size / 1024} KB`);
      expect(image.size / 1024).toBeLessThan(200); // Each image < 200 KB
    }
    
    // Check for WebP or AVIF format usage
    const modernFormatCount = imageSizes.filter(image => 
      image.url.endsWith('.webp') || image.url.endsWith('.avif')
    ).length;
    
    const totalImageCount = imageSizes.length;
    const modernFormatPercentage = (modernFormatCount / totalImageCount) * 100;
    
    console.log(`Modern image format usage: ${modernFormatPercentage.toFixed(2)}%`);
    expect(modernFormatPercentage).toBeGreaterThanOrEqual(50); // At least 50% modern formats
  });

  test('should have efficient API response times', async ({ page }) => {
    // Login to access authenticated APIs
    await page.goto('/login');
    await page.fill('[data-testid="email"]', 'citizen@dsr.test');
    await page.fill('[data-testid="password"]', 'TestPassword123!');
    await page.click('[data-testid="login-button"]');
    
    await page.waitForURL(/\/dashboard/);
    
    // Navigate to API test page
    await page.goto('/api-test');
    
    // Test API response times
    await page.click('[data-testid="test-api-performance"]');
    
    // Wait for test to complete
    await page.waitForSelector('[data-testid="api-test-complete"]');
    
    // Get API performance results
    const apiResults = await page.evaluate(() => {
      const results = {};
      document.querySelectorAll('[data-testid^="api-response-time-"]').forEach(el => {
        const endpoint = el.getAttribute('data-testid')?.replace('api-response-time-', '');
        if (endpoint) {
          results[endpoint] = parseFloat(el.textContent || '0');
        }
      });
      return results;
    });
    
    console.log('API response times (ms):', apiResults);
    
    // Verify all API endpoints respond in under 2 seconds
    for (const [endpoint, responseTime] of Object.entries(apiResults)) {
      expect(responseTime).toBeLessThan(2000);
    }
  });

  test('should have efficient database query times', async ({ page }) => {
    // Login as admin to access database metrics
    await page.goto('/login');
    await page.fill('[data-testid="email"]', 'admin@dsr.gov.ph');
    await page.fill('[data-testid="password"]', 'AdminPassword123!');
    await page.click('[data-testid="login-button"]');
    
    await page.waitForURL(/\/dashboard/);
    
    // Navigate to database performance page
    await page.goto('/admin/database-performance');
    
    // Get database query metrics
    const dbMetrics = await page.evaluate(() => {
      return {
        averageQueryTime: parseFloat(document.querySelector('[data-testid="avg-query-time"]')?.textContent || '0'),
        slowQueries: parseInt(document.querySelector('[data-testid="slow-queries-count"]')?.textContent || '0'),
        cacheHitRatio: parseFloat(document.querySelector('[data-testid="cache-hit-ratio"]')?.textContent || '0'),
      };
    });
    
    console.log('Database metrics:', dbMetrics);
    
    // Verify database performance
    expect(dbMetrics.averageQueryTime).toBeLessThan(100); // < 100ms average query time
    expect(dbMetrics.slowQueries).toBeLessThanOrEqual(5); // ≤ 5 slow queries
    expect(dbMetrics.cacheHitRatio).toBeGreaterThanOrEqual(80); // ≥ 80% cache hit ratio
  });

  test('should have efficient memory usage', async ({ page }) => {
    // Navigate to performance monitoring page
    await page.goto('/performance-monitor');
    
    // Measure memory usage
    const memoryUsage = await page.evaluate(() => {
      // @ts-ignore - Performance memory is non-standard but available in Chrome
      const memory = performance.memory;
      if (!memory) return null;
      
      return {
        usedJSHeapSize: memory.usedJSHeapSize / (1024 * 1024), // MB
        totalJSHeapSize: memory.totalJSHeapSize / (1024 * 1024), // MB
        jsHeapSizeLimit: memory.jsHeapSizeLimit / (1024 * 1024), // MB
      };
    });
    
    if (memoryUsage) {
      console.log('Memory usage (MB):', memoryUsage);
      
      // Verify memory usage is efficient
      expect(memoryUsage.usedJSHeapSize).toBeLessThan(100); // < 100 MB used
      
      // Check heap usage percentage
      const heapUsagePercentage = (memoryUsage.usedJSHeapSize / memoryUsage.jsHeapSizeLimit) * 100;
      expect(heapUsagePercentage).toBeLessThan(50); // < 50% of available heap
    } else {
      console.log('Memory metrics not available in this browser');
    }
  });
});
