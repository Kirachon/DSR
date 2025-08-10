// DSR Disaster Response Load Testing Scenario
// Simulates extreme load conditions during natural disasters or emergencies
// Tests system resilience under massive registration surges

import { ProductionDataGenerator } from '../data-generators/production-data-generator.js';
import { ProductionLoadScenarios } from './production-load-scenarios.js';
import { check, sleep } from 'k6';
import http from 'k6/http';

// Disaster response configuration based on historical data
// (Typhoon Yolanda affected 16M people, requiring massive DSR registration)
export const DISASTER_SCENARIOS = {
  // Immediate disaster response (first 24-48 hours)
  IMMEDIATE_RESPONSE: {
    duration: '24h',
    users: 5000,
    rampUp: '5m',    // Very fast ramp-up
    rampDown: '2h',  // Gradual ramp-down
    description: 'Immediate disaster response with massive registration surge',
    registrationMultiplier: 10, // 10x normal registration volume
    priorityLevel: 'URGENT'
  },
  
  // Extended disaster response (1-2 weeks)
  EXTENDED_RESPONSE: {
    duration: '14d',
    users: 3000,
    rampUp: '30m',
    rampDown: '4h',
    description: 'Extended disaster response with sustained high load',
    registrationMultiplier: 5, // 5x normal registration volume
    priorityLevel: 'HIGH'
  },
  
  // Recovery phase (1-3 months)
  RECOVERY_PHASE: {
    duration: '90d',
    users: 1500,
    rampUp: '1h',
    rampDown: '6h',
    description: 'Recovery phase with elevated but manageable load',
    registrationMultiplier: 2, // 2x normal registration volume
    priorityLevel: 'MEDIUM'
  }
};

// Disaster-specific user patterns
export const DISASTER_USER_PATTERNS = {
  // Emergency response teams
  EMERGENCY_RESPONDERS: {
    weight: 0.20, // 20% of users during disaster
    operations: [
      { type: 'mass_household_registration', frequency: 0.60 },
      { type: 'emergency_eligibility_assessment', frequency: 0.25 },
      { type: 'urgent_grievance_processing', frequency: 0.10 },
      { type: 'disaster_analytics', frequency: 0.05 }
    ],
    sessionDuration: { min: 480, max: 960 }, // 8-16 hours (emergency shifts)
    thinkTime: { min: 5, max: 15 }, // Minimal think time during emergency
    concurrentOperations: 3 // Multiple operations simultaneously
  },
  
  // Field workers (increased during disaster)
  FIELD_WORKERS: {
    weight: 0.40, // 40% of users during disaster
    operations: [
      { type: 'mobile_household_registration', frequency: 0.50 },
      { type: 'rapid_eligibility_screening', frequency: 0.30 },
      { type: 'emergency_document_upload', frequency: 0.15 },
      { type: 'field_grievance_submission', frequency: 0.05 }
    ],
    sessionDuration: { min: 240, max: 600 }, // 4-10 hours
    thinkTime: { min: 10, max: 30 }, // Field conditions with limited connectivity
    concurrentOperations: 2
  },
  
  // Affected citizens (self-registration surge)
  AFFECTED_CITIZENS: {
    weight: 0.30, // 30% of users during disaster
    operations: [
      { type: 'emergency_self_registration', frequency: 0.70 },
      { type: 'disaster_assistance_application', frequency: 0.20 },
      { type: 'emergency_grievance_submission', frequency: 0.10 }
    ],
    sessionDuration: { min: 10, max: 60 }, // 10 minutes - 1 hour
    thinkTime: { min: 60, max: 300 }, // Longer think time due to stress/unfamiliarity
    concurrentOperations: 1
  },
  
  // Command center operators
  COMMAND_CENTER: {
    weight: 0.10, // 10% of users during disaster
    operations: [
      { type: 'disaster_dashboard_monitoring', frequency: 0.40 },
      { type: 'emergency_analytics', frequency: 0.30 },
      { type: 'resource_allocation_analysis', frequency: 0.20 },
      { type: 'emergency_reporting', frequency: 0.10 }
    ],
    sessionDuration: { min: 360, max: 720 }, // 6-12 hours
    thinkTime: { min: 2, max: 10 }, // Rapid monitoring
    concurrentOperations: 4 // Multiple dashboards/systems
  }
};

export class DisasterResponseScenarios extends ProductionLoadScenarios {
  constructor(baseUrl, apiBaseUrl) {
    super(baseUrl, apiBaseUrl);
    this.disasterGenerator = new ProductionDataGenerator();
    this.disasterStartTime = Date.now();
  }

  // Execute disaster response user session
  executeDisasterSession(userType, authToken, scenario) {
    const pattern = DISASTER_USER_PATTERNS[userType];
    const sessionDuration = this.randomBetween(pattern.sessionDuration.min, pattern.sessionDuration.max);
    const sessionStart = Date.now();
    
    // Simulate concurrent operations during emergency
    const concurrentOps = pattern.concurrentOperations || 1;
    
    while ((Date.now() - sessionStart) < sessionDuration * 1000) {
      // Execute multiple operations concurrently during emergency
      const operations = [];
      for (let i = 0; i < concurrentOps; i++) {
        const operation = this.selectOperation(pattern.operations);
        operations.push(this.executeDisasterOperation(operation, authToken, userType, scenario));
      }
      
      // Wait for all operations to complete
      Promise.all(operations);
      
      // Reduced think time during emergency
      const thinkTime = this.randomBetween(pattern.thinkTime.min, pattern.thinkTime.max);
      sleep(thinkTime);
    }
  }

  // Execute disaster-specific operations
  executeDisasterOperation(operationType, authToken, userType, scenario) {
    const headers = {
      'Authorization': `Bearer ${authToken}`,
      'Content-Type': 'application/json',
      'X-Emergency-Priority': scenario.priorityLevel,
      'X-Disaster-Response': 'true'
    };

    switch (operationType) {
      case 'mass_household_registration':
        return this.performMassHouseholdRegistration(headers, userType, scenario);
      case 'mobile_household_registration':
        return this.performMobileHouseholdRegistration(headers, userType, scenario);
      case 'emergency_self_registration':
        return this.performEmergencySelfRegistration(headers, userType, scenario);
      case 'emergency_eligibility_assessment':
        return this.performEmergencyEligibilityAssessment(headers, userType, scenario);
      case 'rapid_eligibility_screening':
        return this.performRapidEligibilityScreening(headers, userType, scenario);
      case 'disaster_assistance_application':
        return this.performDisasterAssistanceApplication(headers, userType, scenario);
      case 'urgent_grievance_processing':
        return this.performUrgentGrievanceProcessing(headers, userType, scenario);
      case 'field_grievance_submission':
        return this.performFieldGrievanceSubmission(headers, userType, scenario);
      case 'emergency_grievance_submission':
        return this.performEmergencyGrievanceSubmission(headers, userType, scenario);
      case 'emergency_document_upload':
        return this.performEmergencyDocumentUpload(headers, userType, scenario);
      case 'disaster_dashboard_monitoring':
        return this.performDisasterDashboardMonitoring(headers, userType, scenario);
      case 'disaster_analytics':
        return this.performDisasterAnalytics(headers, userType, scenario);
      case 'emergency_analytics':
        return this.performEmergencyAnalytics(headers, userType, scenario);
      case 'resource_allocation_analysis':
        return this.performResourceAllocationAnalysis(headers, userType, scenario);
      case 'emergency_reporting':
        return this.performEmergencyReporting(headers, userType, scenario);
      default:
        console.warn(`Unknown disaster operation type: ${operationType}`);
        return Promise.resolve();
    }
  }

  // Disaster-specific operation implementations
  performMassHouseholdRegistration(headers, userType, scenario) {
    // Generate multiple households in a single request (batch registration)
    const batchSize = this.randomBetween(5, 20); // 5-20 households per batch
    const households = [];
    
    for (let i = 0; i < batchSize; i++) {
      const household = this.generateDisasterAffectedHousehold();
      households.push(household);
    }
    
    const batchData = {
      households: households,
      priority: scenario.priorityLevel,
      disasterType: 'TYPHOON',
      batchId: `DISASTER-BATCH-${Date.now()}-${this.randomBetween(1000, 9999)}`
    };
    
    const response = http.post(`${this.apiBaseUrl}/api/v1/registration/households/batch`, 
      JSON.stringify(batchData), { 
        headers,
        timeout: '30s' // Longer timeout for batch operations
      });
    
    check(response, {
      [`${userType} - Mass Registration: status 200/202`]: (r) => r.status === 200 || r.status === 202,
      [`${userType} - Mass Registration: response time < 10s`]: (r) => r.timings.duration < 10000,
      [`${userType} - Mass Registration: batch processed`]: (r) => r.body.includes('batchId'),
    });
    
    return response;
  }

  performMobileHouseholdRegistration(headers, userType, scenario) {
    const household = this.generateDisasterAffectedHousehold();
    household.registrationSource = 'MOBILE_FIELD_UNIT';
    household.gpsCoordinates = this.generateDisasterAreaCoordinates();
    
    const response = http.post(`${this.apiBaseUrl}/api/v1/registration/households/mobile`, 
      JSON.stringify(household), { 
        headers,
        timeout: '15s' // Account for mobile connectivity issues
      });
    
    check(response, {
      [`${userType} - Mobile Registration: status 200/201`]: (r) => r.status === 200 || r.status === 201,
      [`${userType} - Mobile Registration: response time < 8s`]: (r) => r.timings.duration < 8000,
    });
    
    return response;
  }

  performEmergencySelfRegistration(headers, userType, scenario) {
    const household = this.generateDisasterAffectedHousehold();
    household.registrationSource = 'CITIZEN_SELF_SERVICE';
    household.emergencyContact = this.generateEmergencyContact();
    
    const response = http.post(`${this.apiBaseUrl}/api/v1/registration/households/emergency`, 
      JSON.stringify(household), { headers });
    
    check(response, {
      [`${userType} - Emergency Self Registration: status 200/201`]: (r) => r.status === 200 || r.status === 201,
      [`${userType} - Emergency Self Registration: response time < 5s`]: (r) => r.timings.duration < 5000,
    });
    
    return response;
  }

  performEmergencyEligibilityAssessment(headers, userType, scenario) {
    const assessmentData = {
      householdId: `HH-DISASTER-${this.randomBetween(1000000, 9999999)}`,
      assessmentType: 'EMERGENCY_ASSESSMENT',
      disasterType: 'TYPHOON',
      urgencyLevel: scenario.priorityLevel,
      skipNonEssentialChecks: true
    };
    
    const response = http.post(`${this.apiBaseUrl}/api/v1/eligibility/assessments/emergency`, 
      JSON.stringify(assessmentData), { headers });
    
    check(response, {
      [`${userType} - Emergency Eligibility: status 200/201`]: (r) => r.status === 200 || r.status === 201,
      [`${userType} - Emergency Eligibility: response time < 3s`]: (r) => r.timings.duration < 3000,
    });
    
    return response;
  }

  performDisasterDashboardMonitoring(headers, userType, scenario) {
    const dashboards = [
      '/api/v1/analytics/dashboards/disaster-response',
      '/api/v1/analytics/dashboards/emergency-registrations',
      '/api/v1/analytics/dashboards/resource-allocation',
      '/api/v1/analytics/dashboards/field-operations'
    ];
    
    const promises = dashboards.map(endpoint => {
      return http.get(`${this.apiBaseUrl}${endpoint}`, { headers });
    });
    
    // Simulate real-time dashboard monitoring
    const responses = Promise.all(promises);
    
    responses.forEach((response, index) => {
      check(response, {
        [`${userType} - Disaster Dashboard ${index}: status 200`]: (r) => r.status === 200,
        [`${userType} - Disaster Dashboard ${index}: response time < 2s`]: (r) => r.timings.duration < 2000,
      });
    });
    
    return responses;
  }

  // Generate disaster-affected household data
  generateDisasterAffectedHousehold() {
    const household = this.disasterGenerator.generateHousehold();
    
    // Add disaster-specific attributes
    household.disasterInfo = {
      affectedBy: 'TYPHOON_KRISTINE_2024',
      damageLevel: this.randomChoice(['MINOR', 'MODERATE', 'SEVERE', 'TOTAL']),
      evacuationStatus: this.randomChoice(['EVACUATED', 'IN_PLACE', 'RELOCATED']),
      immediateNeeds: this.randomChoices(['FOOD', 'WATER', 'SHELTER', 'MEDICAL', 'CLOTHING'], 2, 4),
      registrationDate: new Date().toISOString()
    };
    
    // Increase vulnerability indicators for disaster-affected households
    household.economicProfile.vulnerabilityIndicators = [
      ...household.economicProfile.vulnerabilityIndicators,
      'DISASTER_AFFECTED'
    ];
    
    // Reduce income due to disaster impact
    household.economicProfile.monthlyIncome = Math.floor(household.economicProfile.monthlyIncome * 0.3);
    
    return household;
  }

  // Generate GPS coordinates for disaster-affected areas
  generateDisasterAreaCoordinates() {
    // Simulate coordinates for typhoon-affected areas in the Philippines
    const disasterAreas = [
      { lat: 14.5995, lng: 120.9842, name: 'Metro Manila' },
      { lat: 15.4817, lng: 120.5979, name: 'Tarlac' },
      { lat: 16.4023, lng: 120.5960, name: 'Pangasinan' },
      { lat: 11.2421, lng: 124.9975, name: 'Tacloban' }
    ];
    
    const area = this.randomChoice(disasterAreas);
    return {
      latitude: area.lat + (Math.random() - 0.5) * 0.1, // ±0.05 degree variation
      longitude: area.lng + (Math.random() - 0.5) * 0.1,
      accuracy: this.randomBetween(5, 50), // GPS accuracy in meters
      timestamp: new Date().toISOString()
    };
  }

  // Generate emergency contact information
  generateEmergencyContact() {
    return {
      name: `Emergency Contact ${this.randomBetween(1000, 9999)}`,
      relationship: this.randomChoice(['RELATIVE', 'NEIGHBOR', 'FRIEND', 'BARANGAY_OFFICIAL']),
      phoneNumber: `+639${this.randomBetween(100000000, 999999999)}`,
      address: 'Safe Area Outside Disaster Zone'
    };
  }

  // Utility methods for disaster scenarios
  randomChoice(array) {
    return array[Math.floor(Math.random() * array.length)];
  }

  randomChoices(array, min, max) {
    const count = this.randomBetween(min, max);
    const shuffled = [...array].sort(() => 0.5 - Math.random());
    return shuffled.slice(0, count);
  }

  // Additional disaster-specific operations (abbreviated for space)
  performRapidEligibilityScreening(headers, userType, scenario) {
    // Simplified eligibility check for emergency situations
    const screeningData = {
      householdId: `HH-DISASTER-${this.randomBetween(1000000, 9999999)}`,
      screeningType: 'RAPID_DISASTER_SCREENING'
    };
    
    const response = http.post(`${this.apiBaseUrl}/api/v1/eligibility/screening/rapid`, 
      JSON.stringify(screeningData), { headers });
    
    check(response, {
      [`${userType} - Rapid Screening: response time < 1s`]: (r) => r.timings.duration < 1000,
    });
    
    return response;
  }

  performDisasterAssistanceApplication(headers, userType, scenario) {
    const applicationData = {
      householdId: `HH-DISASTER-${this.randomBetween(1000000, 9999999)}`,
      assistanceType: 'EMERGENCY_CASH_TRANSFER',
      urgencyLevel: 'IMMEDIATE',
      requestedAmount: this.randomBetween(5000, 15000)
    };
    
    const response = http.post(`${this.apiBaseUrl}/api/v1/payments/emergency-assistance`, 
      JSON.stringify(applicationData), { headers });
    
    check(response, {
      [`${userType} - Disaster Assistance: response time < 3s`]: (r) => r.timings.duration < 3000,
    });
    
    return response;
  }

  performUrgentGrievanceProcessing(headers, userType, scenario) {
    const response = http.get(`${this.apiBaseUrl}/api/v1/grievances?priority=URGENT&status=OPEN&limit=50`, { headers });
    
    check(response, {
      [`${userType} - Urgent Grievances: response time < 1s`]: (r) => r.timings.duration < 1000,
    });
    
    return response;
  }

  performEmergencyDocumentUpload(headers, userType, scenario) {
    const documentData = {
      householdId: `HH-DISASTER-${this.randomBetween(1000000, 9999999)}`,
      documentType: 'DISASTER_DAMAGE_ASSESSMENT',
      fileName: 'damage_photos.zip',
      fileSize: this.randomBetween(5000000, 20000000), // 5-20MB
      priority: 'URGENT'
    };
    
    const response = http.post(`${this.apiBaseUrl}/api/v1/registration/documents/emergency`, 
      JSON.stringify(documentData), { headers });
    
    check(response, {
      [`${userType} - Emergency Document: response time < 10s`]: (r) => r.timings.duration < 10000,
    });
    
    return response;
  }
}

export { DisasterResponseScenarios };
