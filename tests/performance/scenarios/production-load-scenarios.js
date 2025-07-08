// DSR Production Load Scenarios
// Realistic load testing scenarios based on actual DSR usage patterns
// Simulates real-world user behavior and system load distribution

import { ProductionDataGenerator } from '../data-generators/production-data-generator.js';
import { sleep, check } from 'k6';
import http from 'k6/http';

const generator = new ProductionDataGenerator();

// Scenario configuration based on real DSR usage patterns
export const LOAD_SCENARIOS = {
  // Normal business hours (8 AM - 5 PM)
  BUSINESS_HOURS: {
    duration: '8h',
    users: 500,
    rampUp: '30m',
    rampDown: '30m',
    description: 'Normal business hours load with steady user activity'
  },
  
  // Peak registration periods (start of month, after calamities)
  PEAK_REGISTRATION: {
    duration: '4h',
    users: 1500,
    rampUp: '15m',
    rampDown: '45m',
    description: 'Peak registration periods with high household registration volume'
  },
  
  // Payment disbursement days (specific dates each month)
  PAYMENT_DISBURSEMENT: {
    duration: '6h',
    users: 2000,
    rampUp: '20m',
    rampDown: '40m',
    description: 'Payment disbursement days with high payment processing load'
  },
  
  // End-of-month reporting (analytics and data export heavy)
  MONTH_END_REPORTING: {
    duration: '3h',
    users: 800,
    rampUp: '30m',
    rampDown: '30m',
    description: 'Month-end reporting with heavy analytics and data export operations'
  },
  
  // Emergency response (disaster/calamity registration surge)
  EMERGENCY_RESPONSE: {
    duration: '12h',
    users: 3000,
    rampUp: '10m',
    rampDown: '60m',
    description: 'Emergency response scenario with massive registration surge'
  }
};

// User behavior patterns based on DSR stakeholder analysis
export const USER_PATTERNS = {
  // DSWD Staff - Heavy system users
  DSWD_STAFF: {
    weight: 0.15, // 15% of users
    operations: [
      { type: 'household_registration', frequency: 0.40 },
      { type: 'eligibility_assessment', frequency: 0.25 },
      { type: 'payment_processing', frequency: 0.20 },
      { type: 'grievance_management', frequency: 0.10 },
      { type: 'analytics_dashboard', frequency: 0.05 }
    ],
    sessionDuration: { min: 120, max: 480 }, // 2-8 hours
    thinkTime: { min: 10, max: 30 } // 10-30 seconds between operations
  },
  
  // LGU Staff - Moderate system users
  LGU_STAFF: {
    weight: 0.25, // 25% of users
    operations: [
      { type: 'household_registration', frequency: 0.50 },
      { type: 'eligibility_assessment', frequency: 0.30 },
      { type: 'grievance_submission', frequency: 0.15 },
      { type: 'analytics_view', frequency: 0.05 }
    ],
    sessionDuration: { min: 60, max: 240 }, // 1-4 hours
    thinkTime: { min: 15, max: 45 } // 15-45 seconds between operations
  },
  
  // Case Workers - Field-based users
  CASE_WORKERS: {
    weight: 0.30, // 30% of users
    operations: [
      { type: 'household_registration', frequency: 0.35 },
      { type: 'eligibility_assessment', frequency: 0.35 },
      { type: 'grievance_submission', frequency: 0.20 },
      { type: 'data_verification', frequency: 0.10 }
    ],
    sessionDuration: { min: 30, max: 180 }, // 30 minutes - 3 hours
    thinkTime: { min: 20, max: 60 } // 20-60 seconds (field conditions)
  },
  
  // Citizens (Self-service portal)
  CITIZENS: {
    weight: 0.20, // 20% of users
    operations: [
      { type: 'application_status_check', frequency: 0.40 },
      { type: 'grievance_submission', frequency: 0.30 },
      { type: 'document_upload', frequency: 0.20 },
      { type: 'profile_update', frequency: 0.10 }
    ],
    sessionDuration: { min: 5, max: 30 }, // 5-30 minutes
    thinkTime: { min: 30, max: 120 } // 30 seconds - 2 minutes
  },
  
  // System Administrators
  SYSTEM_ADMINS: {
    weight: 0.10, // 10% of users
    operations: [
      { type: 'system_monitoring', frequency: 0.30 },
      { type: 'user_management', frequency: 0.25 },
      { type: 'analytics_dashboard', frequency: 0.20 },
      { type: 'data_export', frequency: 0.15 },
      { type: 'system_configuration', frequency: 0.10 }
    ],
    sessionDuration: { min: 60, max: 360 }, // 1-6 hours
    thinkTime: { min: 5, max: 20 } // 5-20 seconds (power users)
  }
};

// Operation implementations for different user types
export class ProductionLoadScenarios {
  constructor(baseUrl, apiBaseUrl) {
    this.baseUrl = baseUrl;
    this.apiBaseUrl = apiBaseUrl;
    this.generator = new ProductionDataGenerator();
  }

  // Execute user session based on user type
  executeUserSession(userType, authToken) {
    const pattern = USER_PATTERNS[userType];
    const sessionDuration = this.randomBetween(pattern.sessionDuration.min, pattern.sessionDuration.max);
    const sessionStart = Date.now();
    
    while ((Date.now() - sessionStart) < sessionDuration * 1000) {
      const operation = this.selectOperation(pattern.operations);
      this.executeOperation(operation, authToken, userType);
      
      // Think time between operations
      const thinkTime = this.randomBetween(pattern.thinkTime.min, pattern.thinkTime.max);
      sleep(thinkTime);
    }
  }

  // Select operation based on frequency weights
  selectOperation(operations) {
    const random = Math.random();
    let cumulative = 0;
    
    for (const operation of operations) {
      cumulative += operation.frequency;
      if (random <= cumulative) {
        return operation.type;
      }
    }
    
    return operations[0].type; // Fallback
  }

  // Execute specific operation
  executeOperation(operationType, authToken, userType) {
    const headers = {
      'Authorization': `Bearer ${authToken}`,
      'Content-Type': 'application/json'
    };

    switch (operationType) {
      case 'household_registration':
        this.performHouseholdRegistration(headers, userType);
        break;
      case 'eligibility_assessment':
        this.performEligibilityAssessment(headers, userType);
        break;
      case 'payment_processing':
        this.performPaymentProcessing(headers, userType);
        break;
      case 'grievance_submission':
        this.performGrievanceSubmission(headers, userType);
        break;
      case 'grievance_management':
        this.performGrievanceManagement(headers, userType);
        break;
      case 'analytics_dashboard':
        this.performAnalyticsDashboard(headers, userType);
        break;
      case 'analytics_view':
        this.performAnalyticsView(headers, userType);
        break;
      case 'application_status_check':
        this.performStatusCheck(headers, userType);
        break;
      case 'document_upload':
        this.performDocumentUpload(headers, userType);
        break;
      case 'profile_update':
        this.performProfileUpdate(headers, userType);
        break;
      case 'system_monitoring':
        this.performSystemMonitoring(headers, userType);
        break;
      case 'user_management':
        this.performUserManagement(headers, userType);
        break;
      case 'data_export':
        this.performDataExport(headers, userType);
        break;
      case 'data_verification':
        this.performDataVerification(headers, userType);
        break;
      case 'system_configuration':
        this.performSystemConfiguration(headers, userType);
        break;
      default:
        console.warn(`Unknown operation type: ${operationType}`);
    }
  }

  // Operation implementations
  performHouseholdRegistration(headers, userType) {
    const household = this.generator.generateHousehold();
    const response = http.post(`${this.apiBaseUrl}/api/v1/registration/households`, 
      JSON.stringify(household), { headers });
    
    check(response, {
      [`${userType} - Household Registration: status 200/201`]: (r) => r.status === 200 || r.status === 201,
      [`${userType} - Household Registration: response time < 3s`]: (r) => r.timings.duration < 3000,
    });
  }

  performEligibilityAssessment(headers, userType) {
    const assessmentData = {
      householdId: `HH-${this.randomBetween(1000000, 9999999)}`,
      assessmentType: 'FULL_ASSESSMENT'
    };
    
    const response = http.post(`${this.apiBaseUrl}/api/v1/eligibility/assessments`, 
      JSON.stringify(assessmentData), { headers });
    
    check(response, {
      [`${userType} - Eligibility Assessment: status 200/201`]: (r) => r.status === 200 || r.status === 201,
      [`${userType} - Eligibility Assessment: response time < 5s`]: (r) => r.timings.duration < 5000,
    });
  }

  performPaymentProcessing(headers, userType) {
    const payment = this.generator.generatePayment();
    const response = http.post(`${this.apiBaseUrl}/api/v1/payments`, 
      JSON.stringify(payment), { headers });
    
    check(response, {
      [`${userType} - Payment Processing: status 200/201`]: (r) => r.status === 200 || r.status === 201,
      [`${userType} - Payment Processing: response time < 3s`]: (r) => r.timings.duration < 3000,
    });
  }

  performGrievanceSubmission(headers, userType) {
    const grievance = this.generator.generateGrievance();
    const response = http.post(`${this.apiBaseUrl}/api/v1/grievances`, 
      JSON.stringify(grievance), { headers });
    
    check(response, {
      [`${userType} - Grievance Submission: status 200/201`]: (r) => r.status === 200 || r.status === 201,
      [`${userType} - Grievance Submission: response time < 2s`]: (r) => r.timings.duration < 2000,
    });
  }

  performGrievanceManagement(headers, userType) {
    const response = http.get(`${this.apiBaseUrl}/api/v1/grievances?status=OPEN&limit=20`, { headers });
    
    check(response, {
      [`${userType} - Grievance Management: status 200`]: (r) => r.status === 200,
      [`${userType} - Grievance Management: response time < 2s`]: (r) => r.timings.duration < 2000,
    });
  }

  performAnalyticsDashboard(headers, userType) {
    const response = http.get(`${this.apiBaseUrl}/api/v1/analytics/dashboards/system-overview`, { headers });
    
    check(response, {
      [`${userType} - Analytics Dashboard: status 200`]: (r) => r.status === 200,
      [`${userType} - Analytics Dashboard: response time < 4s`]: (r) => r.timings.duration < 4000,
    });
  }

  performAnalyticsView(headers, userType) {
    const endpoints = [
      '/api/v1/analytics/reports/registration-summary',
      '/api/v1/analytics/reports/payment-summary',
      '/api/v1/analytics/reports/eligibility-summary'
    ];
    
    const endpoint = endpoints[Math.floor(Math.random() * endpoints.length)];
    const response = http.get(`${this.apiBaseUrl}${endpoint}`, { headers });
    
    check(response, {
      [`${userType} - Analytics View: status 200`]: (r) => r.status === 200,
      [`${userType} - Analytics View: response time < 3s`]: (r) => r.timings.duration < 3000,
    });
  }

  performStatusCheck(headers, userType) {
    const householdId = `HH-${this.randomBetween(1000000, 9999999)}`;
    const response = http.get(`${this.apiBaseUrl}/api/v1/registration/households/${householdId}/status`, { headers });
    
    check(response, {
      [`${userType} - Status Check: response time < 1s`]: (r) => r.timings.duration < 1000,
    });
  }

  performDocumentUpload(headers, userType) {
    // Simulate document upload (mock file data)
    const documentData = {
      householdId: `HH-${this.randomBetween(1000000, 9999999)}`,
      documentType: 'BIRTH_CERTIFICATE',
      fileName: 'birth_cert.pdf',
      fileSize: this.randomBetween(100000, 2000000) // 100KB - 2MB
    };
    
    const response = http.post(`${this.apiBaseUrl}/api/v1/registration/documents`, 
      JSON.stringify(documentData), { headers });
    
    check(response, {
      [`${userType} - Document Upload: response time < 5s`]: (r) => r.timings.duration < 5000,
    });
  }

  performProfileUpdate(headers, userType) {
    const updateData = {
      householdId: `HH-${this.randomBetween(1000000, 9999999)}`,
      updates: {
        phoneNumber: `+639${this.randomBetween(100000000, 999999999)}`,
        email: `updated${this.randomBetween(1000, 9999)}@gmail.com`
      }
    };
    
    const response = http.patch(`${this.apiBaseUrl}/api/v1/registration/households/profile`, 
      JSON.stringify(updateData), { headers });
    
    check(response, {
      [`${userType} - Profile Update: response time < 2s`]: (r) => r.timings.duration < 2000,
    });
  }

  performSystemMonitoring(headers, userType) {
    const endpoints = [
      '/actuator/health',
      '/actuator/metrics',
      '/actuator/info'
    ];
    
    for (const endpoint of endpoints) {
      const response = http.get(`${this.apiBaseUrl}${endpoint}`, { headers });
      check(response, {
        [`${userType} - System Monitoring (${endpoint}): status 200`]: (r) => r.status === 200,
      });
    }
  }

  performUserManagement(headers, userType) {
    const response = http.get(`${this.apiBaseUrl}/api/v1/admin/users?limit=50`, { headers });
    
    check(response, {
      [`${userType} - User Management: status 200`]: (r) => r.status === 200,
      [`${userType} - User Management: response time < 2s`]: (r) => r.timings.duration < 2000,
    });
  }

  performDataExport(headers, userType) {
    const exportData = {
      type: 'HOUSEHOLD_REPORT',
      format: 'CSV',
      dateRange: {
        start: '2024-01-01',
        end: '2024-12-31'
      }
    };
    
    const response = http.post(`${this.apiBaseUrl}/api/v1/data-management/export`, 
      JSON.stringify(exportData), { headers });
    
    check(response, {
      [`${userType} - Data Export: response time < 10s`]: (r) => r.timings.duration < 10000,
    });
  }

  performDataVerification(headers, userType) {
    const householdId = `HH-${this.randomBetween(1000000, 9999999)}`;
    const response = http.get(`${this.apiBaseUrl}/api/v1/data-management/verification/${householdId}`, { headers });
    
    check(response, {
      [`${userType} - Data Verification: response time < 3s`]: (r) => r.timings.duration < 3000,
    });
  }

  performSystemConfiguration(headers, userType) {
    const response = http.get(`${this.apiBaseUrl}/api/v1/admin/configuration`, { headers });
    
    check(response, {
      [`${userType} - System Configuration: status 200`]: (r) => r.status === 200,
      [`${userType} - System Configuration: response time < 1s`]: (r) => r.timings.duration < 1000,
    });
  }

  // Utility functions
  randomBetween(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
  }
}

export { ProductionLoadScenarios };
