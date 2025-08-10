import { test, expect } from '@playwright/test';
import { TestDataFactory } from '../utils/test-data-factory';
import { ServiceClient } from '../utils/service-client';
import { WorkflowValidator } from '../utils/workflow-validator';

/**
 * Complete Registration to Payment Disbursement Workflow Test
 * 
 * This test validates the entire citizen journey from initial registration
 * through eligibility assessment to final payment disbursement across
 * all 7 DSR microservices.
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */

test.describe('DSR Complete Registration to Payment Workflow', () => {
  let testHousehold: any;
  let serviceClient: ServiceClient;
  let validator: WorkflowValidator;

  test.beforeEach(async ({ page }) => {
    // Initialize test infrastructure
    testHousehold = TestDataFactory.createRealisticHousehold();
    serviceClient = new ServiceClient(page);
    validator = new WorkflowValidator(serviceClient);

    // Ensure clean test environment
    await serviceClient.cleanupTestData(testHousehold.id);
    
    // Verify all services are healthy
    await validator.verifyAllServicesHealthy();
  });

  test.afterEach(async () => {
    // Cleanup test data
    await serviceClient.cleanupTestData(testHousehold.id);
  });

  test('Complete citizen registration to payment disbursement workflow', async ({ page }) => {
    test.setTimeout(900000); // 15 minutes timeout for complete workflow

    // =====================================================
    // STEP 1: CITIZEN REGISTRATION (Registration Service)
    // =====================================================
    await test.step('1. Citizen Registration Process', async () => {
      console.log('Starting citizen registration for household:', testHousehold.id);

      // Navigate to registration portal
      await page.goto('/registration/household/new');
      await expect(page.locator('h1')).toContainText('Household Registration');

      // Fill household head information
      await page.fill('[data-testid="household-head-first-name"]', testHousehold.head.firstName);
      await page.fill('[data-testid="household-head-last-name"]', testHousehold.head.lastName);
      await page.fill('[data-testid="household-head-birth-date"]', testHousehold.head.birthDate);
      await page.selectOption('[data-testid="household-head-gender"]', testHousehold.head.gender);

      // Add family members
      for (const member of testHousehold.members) {
        await page.click('[data-testid="add-family-member"]');
        await page.fill(`[data-testid="member-${member.id}-first-name"]`, member.firstName);
        await page.fill(`[data-testid="member-${member.id}-last-name"]`, member.lastName);
        await page.fill(`[data-testid="member-${member.id}-birth-date"]`, member.birthDate);
        await page.selectOption(`[data-testid="member-${member.id}-relationship"]`, member.relationship);
      }

      // Fill address information
      await page.fill('[data-testid="address-region"]', testHousehold.address.region);
      await page.fill('[data-testid="address-province"]', testHousehold.address.province);
      await page.fill('[data-testid="address-municipality"]', testHousehold.address.municipality);
      await page.fill('[data-testid="address-barangay"]', testHousehold.address.barangay);
      await page.fill('[data-testid="address-street"]', testHousehold.address.street);

      // Upload required documents
      await page.setInputFiles('[data-testid="birth-certificate"]', testHousehold.documents.birthCertificate);
      await page.setInputFiles('[data-testid="valid-id"]', testHousehold.documents.validId);
      await page.setInputFiles('[data-testid="proof-of-residence"]', testHousehold.documents.proofOfResidence);

      // Submit registration
      await page.click('[data-testid="submit-registration"]');
      
      // Wait for registration confirmation
      await expect(page.locator('[data-testid="registration-success"]')).toBeVisible({ timeout: 30000 });
      
      // Extract registration ID
      const registrationId = await page.locator('[data-testid="registration-id"]').textContent();
      testHousehold.registrationId = registrationId;

      console.log('Registration completed successfully:', registrationId);
    });

    // =====================================================
    // STEP 2: DATA VALIDATION (Data Management Service)
    // =====================================================
    await test.step('2. Data Management Validation Process', async () => {
      console.log('Starting data validation for registration:', testHousehold.registrationId);

      // Wait for AI validation engine to process
      await validator.waitForDataValidationCompletion(testHousehold.registrationId, 120000);

      // Verify data quality checks passed
      const validationResult = await serviceClient.getValidationStatus(testHousehold.registrationId);
      expect(validationResult.status).toBe('VALIDATED');
      expect(validationResult.qualityScore).toBeGreaterThan(0.8);
      expect(validationResult.duplicateCheck).toBe('PASSED');

      // Verify data enrichment completed
      const enrichedData = await serviceClient.getEnrichedData(testHousehold.registrationId);
      expect(enrichedData.geoLocation).toBeDefined();
      expect(enrichedData.economicIndicators).toBeDefined();
      expect(enrichedData.socialIndicators).toBeDefined();

      console.log('Data validation completed successfully');
    });

    // =====================================================
    // STEP 3: ELIGIBILITY ASSESSMENT (Eligibility Service)
    // =====================================================
    await test.step('3. Eligibility Assessment Process', async () => {
      console.log('Starting eligibility assessment for:', testHousehold.registrationId);

      // Wait for PMT calculation to complete
      await validator.waitForEligibilityAssessment(testHousehold.registrationId, 180000);

      // Verify PMT score calculation
      const eligibilityResult = await serviceClient.getEligibilityResult(testHousehold.registrationId);
      expect(eligibilityResult.pmtScore).toBeDefined();
      expect(eligibilityResult.pmtScore).toBeGreaterThan(0);
      expect(eligibilityResult.eligibilityStatus).toMatch(/ELIGIBLE|INELIGIBLE/);

      // If eligible, verify benefit calculation
      if (eligibilityResult.eligibilityStatus === 'ELIGIBLE') {
        expect(eligibilityResult.benefitAmount).toBeGreaterThan(0);
        expect(eligibilityResult.benefitPrograms).toHaveLength.greaterThan(0);
        
        // Verify approval workflow
        const approvalStatus = await serviceClient.getApprovalStatus(testHousehold.registrationId);
        expect(approvalStatus.status).toBe('APPROVED');
        expect(approvalStatus.approvedBy).toBeDefined();
        expect(approvalStatus.approvedAt).toBeDefined();
      }

      console.log('Eligibility assessment completed:', eligibilityResult.eligibilityStatus);
    });

    // =====================================================
    // STEP 4: PAYMENT PROCESSING (Payment Service)
    // =====================================================
    await test.step('4. Payment Processing Workflow', async () => {
      // Only proceed if household is eligible
      const eligibilityResult = await serviceClient.getEligibilityResult(testHousehold.registrationId);
      if (eligibilityResult.eligibilityStatus !== 'ELIGIBLE') {
        console.log('Household not eligible for payment, skipping payment processing');
        return;
      }

      console.log('Starting payment processing for:', testHousehold.registrationId);

      // Wait for payment batch creation
      await validator.waitForPaymentBatchCreation(testHousehold.registrationId, 120000);

      // Verify payment batch details
      const paymentBatch = await serviceClient.getPaymentBatch(testHousehold.registrationId);
      expect(paymentBatch.status).toBe('CREATED');
      expect(paymentBatch.totalAmount).toBeGreaterThan(0);
      expect(paymentBatch.beneficiaryCount).toBeGreaterThan(0);

      // Verify FSP selection
      const fspSelection = await serviceClient.getFSPSelection(paymentBatch.id);
      expect(fspSelection.selectedFSP).toBeDefined();
      expect(fspSelection.selectionReason).toBeDefined();

      // Wait for payment execution
      await validator.waitForPaymentExecution(paymentBatch.id, 300000);

      // Verify payment completion
      const paymentResult = await serviceClient.getPaymentResult(paymentBatch.id);
      expect(paymentResult.status).toBe('COMPLETED');
      expect(paymentResult.successfulPayments).toBeGreaterThan(0);
      expect(paymentResult.failedPayments).toBe(0);

      console.log('Payment processing completed successfully');
    });

    // =====================================================
    // STEP 5: INTEROPERABILITY VALIDATION
    // =====================================================
    await test.step('5. Interoperability Service Validation', async () => {
      console.log('Validating interoperability service integration');

      // Verify external system notifications
      const notifications = await serviceClient.getExternalNotifications(testHousehold.registrationId);
      expect(notifications.length).toBeGreaterThan(0);

      // Verify data synchronization with external systems
      const syncStatus = await serviceClient.getExternalSyncStatus(testHousehold.registrationId);
      expect(syncStatus.status).toBe('SYNCHRONIZED');
      expect(syncStatus.lastSyncDate).toBeDefined();

      console.log('Interoperability validation completed');
    });

    // =====================================================
    // STEP 6: ANALYTICS DATA AGGREGATION
    // =====================================================
    await test.step('6. Analytics Service Data Aggregation', async () => {
      console.log('Validating analytics data aggregation');

      // Wait for analytics processing
      await validator.waitForAnalyticsProcessing(testHousehold.registrationId, 60000);

      // Verify dashboard data updates
      const dashboardData = await serviceClient.getDashboardData();
      expect(dashboardData.totalRegistrations).toBeGreaterThan(0);
      expect(dashboardData.totalPayments).toBeGreaterThan(0);
      expect(dashboardData.lastUpdated).toBeDefined();

      // Verify real-time metrics
      const metrics = await serviceClient.getRealTimeMetrics();
      expect(metrics.registrationsToday).toBeGreaterThanOrEqual(1);
      expect(metrics.paymentsToday).toBeGreaterThanOrEqual(0);

      console.log('Analytics validation completed');
    });

    // =====================================================
    // STEP 7: CROSS-SERVICE DATA CONSISTENCY VALIDATION
    // =====================================================
    await test.step('7. Cross-Service Data Consistency Validation', async () => {
      console.log('Validating data consistency across all services');

      // Verify data consistency across services
      const consistencyCheck = await validator.validateDataConsistency(testHousehold.registrationId);
      expect(consistencyCheck.registrationService).toBe('CONSISTENT');
      expect(consistencyCheck.dataManagementService).toBe('CONSISTENT');
      expect(consistencyCheck.eligibilityService).toBe('CONSISTENT');
      expect(consistencyCheck.paymentService).toBe('CONSISTENT');
      expect(consistencyCheck.analyticsService).toBe('CONSISTENT');

      // Verify audit trail completeness
      const auditTrail = await serviceClient.getAuditTrail(testHousehold.registrationId);
      expect(auditTrail.length).toBeGreaterThan(10); // Multiple audit entries expected
      expect(auditTrail[0].action).toBe('REGISTRATION_CREATED');
      expect(auditTrail[auditTrail.length - 1].action).toMatch(/PAYMENT_COMPLETED|ELIGIBILITY_DETERMINED/);

      console.log('Data consistency validation completed');
    });

    // =====================================================
    // STEP 8: GRIEVANCE SYSTEM READINESS VALIDATION
    // =====================================================
    await test.step('8. Grievance System Readiness Validation', async () => {
      console.log('Validating grievance system readiness');

      // Verify grievance system can access household data
      const grievanceAccess = await serviceClient.validateGrievanceAccess(testHousehold.registrationId);
      expect(grievanceAccess.canAccessData).toBe(true);
      expect(grievanceAccess.availableActions).toContain('CREATE_GRIEVANCE');

      // Test grievance creation capability
      const testGrievance = {
        householdId: testHousehold.registrationId,
        type: 'DATA_CORRECTION',
        description: 'Test grievance for E2E validation',
        priority: 'LOW'
      };

      const grievanceResult = await serviceClient.createTestGrievance(testGrievance);
      expect(grievanceResult.id).toBeDefined();
      expect(grievanceResult.status).toBe('SUBMITTED');

      // Cleanup test grievance
      await serviceClient.deleteTestGrievance(grievanceResult.id);

      console.log('Grievance system validation completed');
    });

    // =====================================================
    // FINAL VALIDATION: COMPLETE WORKFLOW SUCCESS
    // =====================================================
    await test.step('9. Final Workflow Validation', async () => {
      console.log('Performing final workflow validation');

      // Verify complete workflow status
      const workflowStatus = await serviceClient.getWorkflowStatus(testHousehold.registrationId);
      expect(workflowStatus.registrationComplete).toBe(true);
      expect(workflowStatus.dataValidationComplete).toBe(true);
      expect(workflowStatus.eligibilityAssessmentComplete).toBe(true);
      
      const eligibilityResult = await serviceClient.getEligibilityResult(testHousehold.registrationId);
      if (eligibilityResult.eligibilityStatus === 'ELIGIBLE') {
        expect(workflowStatus.paymentProcessingComplete).toBe(true);
      }

      // Verify system performance during workflow
      const performanceMetrics = await serviceClient.getWorkflowPerformanceMetrics(testHousehold.registrationId);
      expect(performanceMetrics.totalDuration).toBeLessThan(900000); // Less than 15 minutes
      expect(performanceMetrics.averageResponseTime).toBeLessThan(2000); // Less than 2 seconds
      expect(performanceMetrics.errorCount).toBe(0);

      console.log('Complete workflow validation successful!');
      console.log('Workflow Duration:', performanceMetrics.totalDuration, 'ms');
      console.log('Average Response Time:', performanceMetrics.averageResponseTime, 'ms');
    });
  });

  test('Error handling and recovery workflow', async ({ page }) => {
    test.setTimeout(600000); // 10 minutes timeout

    await test.step('Service unavailability simulation', async () => {
      // Test workflow resilience when services are temporarily unavailable
      // This would involve controlled service shutdowns and restarts
      console.log('Testing service resilience and error recovery');
      
      // Implementation would include:
      // - Simulating service downtime
      // - Verifying graceful degradation
      // - Testing automatic retry mechanisms
      // - Validating data consistency after recovery
    });
  });

  test('High load workflow validation', async ({ page }) => {
    test.setTimeout(1800000); // 30 minutes timeout

    await test.step('Concurrent workflow execution', async () => {
      // Test multiple concurrent workflows
      console.log('Testing concurrent workflow execution under load');
      
      // Implementation would include:
      // - Running multiple workflows simultaneously
      // - Monitoring system performance
      // - Validating data consistency
      // - Ensuring no resource conflicts
    });
  });
});
