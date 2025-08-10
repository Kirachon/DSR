import { test, expect } from '@playwright/test';
import { AuthenticationManager } from '../../src/utils/auth-test-utilities';
import { TimelineTester } from '../../src/utils/component-test-utilities';

test.describe('Workflow Timeline Component Tests', () => {
  let authManager: AuthenticationManager;

  test.beforeEach(async ({ page }) => {
    authManager = new AuthenticationManager(page);
  });

  test.afterEach(async ({ page }) => {
    await authManager.logout();
  });

  test.describe('Timeline Structure and Display', () => {
    test('should display timeline with proper structure for citizen journey', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const timeline = page.locator('.workflow-timeline, [data-testid*="timeline"]').first();
      
      if (await timeline.count() > 0) {
        const timelineTester = new TimelineTester(page, timeline);
        
        await timelineTester.testTimelineStructure({
          expectedEvents: undefined, // Will be determined dynamically
          validateTimestamps: true
        });

        // Take screenshot for visual verification
        await page.screenshot({ 
          path: 'test-results/workflow-timeline-citizen.png',
          fullPage: false 
        });
      }
    });

    test('should display timeline events with proper information', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const timeline = page.locator('.workflow-timeline, [data-testid*="timeline"]').first();
      
      if (await timeline.count() > 0) {
        const events = timeline.locator('.timeline-event, [data-testid*="timeline-event"]');
        const eventCount = await events.count();

        expect(eventCount).toBeGreaterThan(0);

        // Validate each event
        for (let i = 0; i < Math.min(eventCount, 5); i++) {
          const event = events.nth(i);
          await expect(event).toBeVisible();

          // Check event has title
          const title = event.locator('.event-title, [data-testid*="title"]');
          if (await title.count() > 0) {
            const titleText = await title.first().textContent();
            expect(titleText?.trim()).toBeTruthy();
          }

          // Check event has status
          const status = event.locator('.event-status, [data-testid*="status"]');
          if (await status.count() > 0) {
            await expect(status.first()).toBeVisible();
            
            const statusText = await status.first().textContent();
            expect(statusText?.trim()).toBeTruthy();
          }

          // Check event has timestamp
          const timestamp = event.locator('.event-timestamp, [data-testid*="timestamp"]');
          if (await timestamp.count() > 0) {
            const timestampText = await timestamp.first().textContent();
            expect(timestampText?.trim()).toBeTruthy();
          }

          // Check event has description
          const description = event.locator('.event-description, [data-testid*="description"]');
          if (await description.count() > 0) {
            const descText = await description.first().textContent();
            expect(descText?.trim()).toBeTruthy();
          }
        }
      }
    });

    test('should show timeline for staff case management', async ({ page }) => {
      await authManager.loginAs('LGU_STAFF');
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/cases`);
      await page.waitForLoadState('networkidle');

      // Look for case timeline
      const caseTimeline = page.locator('.case-timeline, [data-testid*="case-timeline"]').first();
      
      if (await caseTimeline.count() > 0) {
        const timelineTester = new TimelineTester(page, caseTimeline);
        
        await timelineTester.testTimelineStructure({
          validateTimestamps: true
        });

        // Take screenshot for visual verification
        await page.screenshot({ 
          path: 'test-results/workflow-timeline-staff.png',
          fullPage: false 
        });
      }
    });

    test('should display timeline with different event types', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const timeline = page.locator('.workflow-timeline, [data-testid*="timeline"]').first();
      
      if (await timeline.count() > 0) {
        const events = timeline.locator('.timeline-event');
        const eventCount = await events.count();

        // Check for different event types
        const eventTypes = new Set();
        
        for (let i = 0; i < eventCount; i++) {
          const event = events.nth(i);
          const eventType = await event.getAttribute('data-event-type') || 
                           await event.getAttribute('data-type') ||
                           'default';
          
          eventTypes.add(eventType);
        }

        // Should have at least one event type
        expect(eventTypes.size).toBeGreaterThan(0);
      }
    });
  });

  test.describe('Timeline Status and Progression', () => {
    test('should show logical status progression', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const timeline = page.locator('.workflow-timeline, [data-testid*="timeline"]').first();
      
      if (await timeline.count() > 0) {
        const timelineTester = new TimelineTester(page, timeline);
        await timelineTester.testStatusProgression();
      }
    });

    test('should display current step prominently', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const timeline = page.locator('.workflow-timeline, [data-testid*="timeline"]').first();
      
      if (await timeline.count() > 0) {
        const currentEvents = timeline.locator('.timeline-event[data-status="current"], .current-event');
        const currentCount = await currentEvents.count();

        if (currentCount > 0) {
          // Should have at most one current event
          expect(currentCount).toBeLessThanOrEqual(1);
          
          const currentEvent = currentEvents.first();
          await expect(currentEvent).toBeVisible();
          
          // Current event should be visually distinct
          const hasCurrentClass = await currentEvent.evaluate(el => 
            el.classList.contains('current') || 
            el.classList.contains('active') ||
            el.getAttribute('data-status') === 'current'
          );
          
          expect(hasCurrentClass).toBeTruthy();
        }
      }
    });

    test('should show completed steps correctly', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const timeline = page.locator('.workflow-timeline, [data-testid*="timeline"]').first();
      
      if (await timeline.count() > 0) {
        const completedEvents = timeline.locator('.timeline-event[data-status="completed"], .completed-event');
        const completedCount = await completedEvents.count();

        for (let i = 0; i < completedCount; i++) {
          const completedEvent = completedEvents.nth(i);
          await expect(completedEvent).toBeVisible();
          
          // Check for completion indicator
          const completionIcon = completedEvent.locator('.completion-icon, .check-icon, [data-testid*="completed"]');
          if (await completionIcon.count() > 0) {
            await expect(completionIcon.first()).toBeVisible();
          }
        }
      }
    });

    test('should handle error states in timeline', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const timeline = page.locator('.workflow-timeline, [data-testid*="timeline"]').first();
      
      if (await timeline.count() > 0) {
        const errorEvents = timeline.locator('.timeline-event[data-status="error"], .error-event');
        const errorCount = await errorEvents.count();

        for (let i = 0; i < errorCount; i++) {
          const errorEvent = errorEvents.nth(i);
          await expect(errorEvent).toBeVisible();
          
          // Check for error indicator
          const errorIcon = errorEvent.locator('.error-icon, .warning-icon, [data-testid*="error"]');
          if (await errorIcon.count() > 0) {
            await expect(errorIcon.first()).toBeVisible();
          }
          
          // Check for error message
          const errorMessage = errorEvent.locator('.error-message, [data-testid*="error-message"]');
          if (await errorMessage.count() > 0) {
            const errorText = await errorMessage.first().textContent();
            expect(errorText?.trim()).toBeTruthy();
          }
        }
      }
    });
  });

  test.describe('Interactive Timeline Elements', () => {
    test('should handle clickable timeline events', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const timeline = page.locator('.workflow-timeline, [data-testid*="timeline"]').first();
      
      if (await timeline.count() > 0) {
        const timelineTester = new TimelineTester(page, timeline);
        await timelineTester.testInteractiveElements();
      }
    });

    test('should expand event details when clicked', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const timeline = page.locator('.workflow-timeline, [data-testid*="timeline"]').first();
      
      if (await timeline.count() > 0) {
        const expandableEvents = timeline.locator('.timeline-event[data-expandable="true"], .expandable-event');
        const expandableCount = await expandableEvents.count();

        if (expandableCount > 0) {
          const firstExpandable = expandableEvents.first();
          
          // Click to expand
          await firstExpandable.click();
          await page.waitForTimeout(300);
          
          // Check for expanded content
          const expandedContent = firstExpandable.locator('.expanded-content, [data-testid*="expanded"]');
          if (await expandedContent.count() > 0) {
            await expect(expandedContent.first()).toBeVisible();
          }
          
          // Click again to collapse
          await firstExpandable.click();
          await page.waitForTimeout(300);
        }
      }
    });

    test('should show tooltips on hover', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const timeline = page.locator('.workflow-timeline, [data-testid*="timeline"]').first();
      
      if (await timeline.count() > 0) {
        const events = timeline.locator('.timeline-event');
        const eventCount = await events.count();

        if (eventCount > 0) {
          const firstEvent = events.first();
          
          // Hover over event
          await firstEvent.hover();
          await page.waitForTimeout(500);
          
          // Check for tooltip
          const tooltip = page.locator('.tooltip, [role="tooltip"], [data-testid*="tooltip"]');
          if (await tooltip.count() > 0) {
            await expect(tooltip.first()).toBeVisible();
            
            const tooltipText = await tooltip.first().textContent();
            expect(tooltipText?.trim()).toBeTruthy();
          }
        }
      }
    });

    test('should support keyboard navigation', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const timeline = page.locator('.workflow-timeline, [data-testid*="timeline"]').first();
      
      if (await timeline.count() > 0) {
        // Focus on timeline
        await timeline.focus();
        
        // Test arrow key navigation
        await page.keyboard.press('ArrowDown');
        await page.waitForTimeout(100);
        
        // Check if focus moved to an event
        const focusedElement = page.locator(':focus');
        await expect(focusedElement).toBeVisible();
        
        // Test Enter key activation
        await page.keyboard.press('Enter');
        await page.waitForTimeout(200);
      }
    });
  });

  test.describe('Timeline Data and Updates', () => {
    test('should display real-time status updates', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const timeline = page.locator('.workflow-timeline, [data-testid*="timeline"]').first();
      
      if (await timeline.count() > 0) {
        // Get initial timeline state
        const initialEvents = await timeline.locator('.timeline-event').count();
        
        // Trigger refresh if available
        const refreshButton = page.locator('button:has-text("Refresh"), [data-testid*="refresh"]');
        if (await refreshButton.count() > 0) {
          await refreshButton.first().click();
          await page.waitForTimeout(1000);
          
          // Timeline should still be present
          await expect(timeline).toBeVisible();
        }
      }
    });

    test('should handle timeline data loading states', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      
      // Mock slow API response
      await page.route('**/api/timeline**', async route => {
        await page.waitForTimeout(1000);
        route.continue();
      });
      
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      
      // Check for loading state
      const loadingIndicator = page.locator('.timeline-loading, [data-testid*="timeline-loading"]');
      if (await loadingIndicator.count() > 0) {
        await expect(loadingIndicator.first()).toBeVisible();
      }
      
      await page.waitForLoadState('networkidle');
      
      // Timeline should load after loading state
      const timeline = page.locator('.workflow-timeline, [data-testid*="timeline"]').first();
      if (await timeline.count() > 0) {
        await expect(timeline).toBeVisible();
      }
    });

    test('should handle empty timeline state', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      
      // Mock empty timeline response
      await page.route('**/api/timeline**', route => {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ events: [] })
        });
      });
      
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');
      
      // Check for empty state message
      const emptyState = page.locator('.timeline-empty, [data-testid*="timeline-empty"]');
      if (await emptyState.count() > 0) {
        await expect(emptyState.first()).toBeVisible();
        
        const emptyText = await emptyState.first().textContent();
        expect(emptyText?.trim()).toBeTruthy();
      }
    });
  });

  test.describe('Timeline Accessibility', () => {
    test('should meet WCAG accessibility standards', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const timeline = page.locator('.workflow-timeline, [data-testid*="timeline"]').first();
      
      if (await timeline.count() > 0) {
        const timelineTester = new TimelineTester(page, timeline);
        await timelineTester.testTimelineAccessibility();
      }
    });

    test('should have proper ARIA attributes', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const timeline = page.locator('.workflow-timeline, [data-testid*="timeline"]').first();
      
      if (await timeline.count() > 0) {
        // Check timeline has proper role
        const timelineRole = await timeline.getAttribute('role');
        expect(['list', 'timeline', 'log']).toContain(timelineRole);
        
        // Check for aria-label
        const ariaLabel = await timeline.getAttribute('aria-label');
        const ariaLabelledBy = await timeline.getAttribute('aria-labelledby');
        
        expect(ariaLabel || ariaLabelledBy).toBeTruthy();
        
        // Check events have proper structure
        const events = timeline.locator('.timeline-event');
        const eventCount = await events.count();
        
        for (let i = 0; i < Math.min(eventCount, 3); i++) {
          const event = events.nth(i);
          
          // Check event has proper role
          const eventRole = await event.getAttribute('role');
          if (eventRole) {
            expect(['listitem', 'article']).toContain(eventRole);
          }
          
          // Check event has accessible name
          const eventAriaLabel = await event.getAttribute('aria-label');
          const eventAriaLabelledBy = await event.getAttribute('aria-labelledby');
          
          expect(eventAriaLabel || eventAriaLabelledBy).toBeTruthy();
        }
      }
    });

    test('should support screen readers', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');

      const timeline = page.locator('.workflow-timeline, [data-testid*="timeline"]').first();
      
      if (await timeline.count() > 0) {
        // Check for live region updates
        const ariaLive = await timeline.getAttribute('aria-live');
        if (ariaLive) {
          expect(['polite', 'assertive']).toContain(ariaLive);
        }
        
        // Check events have descriptive text
        const events = timeline.locator('.timeline-event');
        const eventCount = await events.count();
        
        for (let i = 0; i < Math.min(eventCount, 3); i++) {
          const event = events.nth(i);
          
          const eventText = await event.textContent();
          expect(eventText?.trim()).toBeTruthy();
          
          // Check for status announcement
          const statusText = event.locator('.sr-only, .visually-hidden');
          if (await statusText.count() > 0) {
            const srText = await statusText.first().textContent();
            expect(srText?.trim()).toBeTruthy();
          }
        }
      }
    });
  });

  test.describe('Timeline Performance', () => {
    test('should render timeline efficiently', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      
      const startTime = Date.now();
      
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');
      
      const timeline = page.locator('.workflow-timeline, [data-testid*="timeline"]').first();
      if (await timeline.count() > 0) {
        await timeline.waitFor({ state: 'visible', timeout: 2000 });
      }
      
      const endTime = Date.now();
      const loadTime = endTime - startTime;
      
      // Timeline should load within 2 seconds
      expect(loadTime).toBeLessThan(2000);
    });

    test('should handle large timelines efficiently', async ({ page }) => {
      await authManager.loginAs('CITIZEN');
      
      // Mock large timeline data
      await page.route('**/api/timeline**', route => {
        const largeTimeline = Array.from({ length: 50 }, (_, i) => ({
          id: i + 1,
          title: `Event ${i + 1}`,
          status: i % 3 === 0 ? 'completed' : i % 3 === 1 ? 'current' : 'pending',
          timestamp: new Date(Date.now() - i * 24 * 60 * 60 * 1000).toISOString()
        }));
        
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ events: largeTimeline })
        });
      });
      
      const startTime = Date.now();
      
      await page.goto(`${process.env.BASE_URL || 'http://localhost:3000'}/dashboard`);
      await page.waitForLoadState('networkidle');
      
      const endTime = Date.now();
      const loadTime = endTime - startTime;
      
      // Should handle large timeline within reasonable time
      expect(loadTime).toBeLessThan(3000);
      
      const timeline = page.locator('.workflow-timeline, [data-testid*="timeline"]').first();
      if (await timeline.count() > 0) {
        await expect(timeline).toBeVisible();
      }
    });
  });
});
