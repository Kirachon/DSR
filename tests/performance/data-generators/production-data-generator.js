// DSR Production Data Volume Generator
// Generates realistic test data that simulates actual Philippine household demographics
// Based on PSA (Philippine Statistics Authority) data patterns and DSR requirements

import { faker } from '@faker-js/faker';

// Configure faker for Philippine locale
faker.locale = 'en_PH';

// Philippine-specific data constants
const PHILIPPINE_REGIONS = [
  'NCR', 'CAR', 'Region I', 'Region II', 'Region III', 'Region IV-A', 
  'Region IV-B', 'Region V', 'Region VI', 'Region VII', 'Region VIII',
  'Region IX', 'Region X', 'Region XI', 'Region XII', 'Region XIII', 'ARMM'
];

const PHILIPPINE_PROVINCES = {
  'NCR': ['Metro Manila'],
  'CAR': ['Abra', 'Apayao', 'Benguet', 'Ifugao', 'Kalinga', 'Mountain Province'],
  'Region I': ['Ilocos Norte', 'Ilocos Sur', 'La Union', 'Pangasinan'],
  'Region III': ['Aurora', 'Bataan', 'Bulacan', 'Nueva Ecija', 'Pampanga', 'Tarlac', 'Zambales'],
  'Region IV-A': ['Batangas', 'Cavite', 'Laguna', 'Quezon', 'Rizal']
};

const PHILIPPINE_CITIES = {
  'Metro Manila': ['Manila', 'Quezon City', 'Makati', 'Pasig', 'Taguig', 'Marikina', 'Pasay', 'Caloocan'],
  'Bulacan': ['Malolos', 'Meycauayan', 'San Jose del Monte', 'Marilao'],
  'Cavite': ['Bacoor', 'Imus', 'Dasmariñas', 'General Trias']
};

const COMMON_FILIPINO_SURNAMES = [
  'Santos', 'Reyes', 'Cruz', 'Bautista', 'Ocampo', 'Garcia', 'Mendoza', 'Torres',
  'Gonzales', 'Lopez', 'Hernandez', 'Perez', 'Sanchez', 'Ramirez', 'Flores',
  'Rivera', 'Gomez', 'Diaz', 'Morales', 'Jimenez', 'Herrera', 'Medina'
];

const COMMON_FILIPINO_FIRST_NAMES = {
  male: ['Juan', 'Jose', 'Antonio', 'Francisco', 'Manuel', 'Pedro', 'Luis', 'Carlos', 'Miguel', 'Rafael'],
  female: ['Maria', 'Ana', 'Carmen', 'Josefa', 'Rosa', 'Teresa', 'Angela', 'Francisca', 'Isabel', 'Esperanza']
};

const EMPLOYMENT_STATUS_DISTRIBUTION = {
  'EMPLOYED': 0.45,
  'UNEMPLOYED': 0.15,
  'SELF_EMPLOYED': 0.25,
  'STUDENT': 0.08,
  'RETIRED': 0.05,
  'DISABLED': 0.02
};

const VULNERABILITY_INDICATORS = [
  'INDIGENOUS_PEOPLE', 'PERSON_WITH_DISABILITY', 'SOLO_PARENT', 'SENIOR_CITIZEN',
  'PREGNANT_LACTATING_WOMEN', 'CHILD_LABOR', 'STREET_CHILDREN', 'CONFLICT_AFFECTED',
  'DISASTER_AFFECTED', 'NONE'
];

const HOUSEHOLD_ASSETS = [
  'HOUSE', 'LOT', 'VEHICLE', 'MOTORCYCLE', 'BICYCLE', 'APPLIANCES', 
  'LIVESTOCK', 'FARM_EQUIPMENT', 'BUSINESS_EQUIPMENT', 'NONE'
];

// Production data volume configuration
const PRODUCTION_VOLUMES = {
  // Based on DSWD target beneficiaries (approximately 4.4 million households)
  TOTAL_HOUSEHOLDS: 4400000,
  DAILY_REGISTRATIONS: 5000,
  PEAK_DAILY_REGISTRATIONS: 15000,
  MONTHLY_ELIGIBILITY_ASSESSMENTS: 500000,
  MONTHLY_PAYMENTS: 1200000,
  MONTHLY_GRIEVANCES: 25000,
  CONCURRENT_USERS_NORMAL: 500,
  CONCURRENT_USERS_PEAK: 2000
};

// Data generation utilities
class ProductionDataGenerator {
  constructor() {
    this.householdCounter = 1;
    this.memberCounter = 1;
    this.paymentCounter = 1;
    this.grievanceCounter = 1;
  }

  // Generate realistic Philippine address
  generatePhilippineAddress() {
    const region = faker.helpers.arrayElement(PHILIPPINE_REGIONS);
    const provinces = PHILIPPINE_PROVINCES[region] || ['Generic Province'];
    const province = faker.helpers.arrayElement(provinces);
    const cities = PHILIPPINE_CITIES[province] || ['Generic City'];
    const city = faker.helpers.arrayElement(cities);
    
    return {
      region: region,
      province: province,
      city: city,
      barangay: `Barangay ${faker.helpers.arrayElement(['San Jose', 'Santa Maria', 'San Antonio', 'Poblacion', 'Santo Niño'])}`,
      street: `${faker.datatype.number({ min: 1, max: 999 })} ${faker.helpers.arrayElement(['Rizal', 'Bonifacio', 'Mabini', 'Luna', 'Del Pilar'])} Street`,
      zipCode: faker.datatype.number({ min: 1000, max: 9999 }).toString()
    };
  }

  // Generate realistic Filipino name
  generateFilipinoName(gender = null) {
    const selectedGender = gender || faker.helpers.arrayElement(['male', 'female']);
    const firstName = faker.helpers.arrayElement(COMMON_FILIPINO_FIRST_NAMES[selectedGender]);
    const lastName = faker.helpers.arrayElement(COMMON_FILIPINO_SURNAMES);
    
    return { firstName, lastName, gender: selectedGender };
  }

  // Generate realistic PhilSys ID
  generatePhilSysId(type = 'CITIZEN') {
    const prefix = type === 'CITIZEN' ? 'PSN' : 'PSN-TEMP';
    const timestamp = Date.now().toString().slice(-8);
    const random = faker.datatype.number({ min: 1000, max: 9999 });
    return `${prefix}-${timestamp}-${random}`;
  }

  // Generate realistic economic profile based on Philippine poverty statistics
  generateEconomicProfile() {
    // Philippine poverty line is approximately ₱12,030 per month per person
    const povertyLine = 12030;
    const householdSize = faker.datatype.number({ min: 2, max: 8 });
    
    // 70% of households are below or near poverty line for DSR targeting
    const isPoor = faker.datatype.float() < 0.7;
    
    let monthlyIncome;
    if (isPoor) {
      // Below or near poverty line
      monthlyIncome = faker.datatype.number({ 
        min: 5000, 
        max: povertyLine * householdSize * 1.2 
      });
    } else {
      // Above poverty line but still eligible for some programs
      monthlyIncome = faker.datatype.number({ 
        min: povertyLine * householdSize * 1.2, 
        max: 50000 
      });
    }

    const employmentStatus = faker.helpers.weightedArrayElement(
      Object.entries(EMPLOYMENT_STATUS_DISTRIBUTION).map(([status, weight]) => ({
        value: status,
        weight: weight * 100
      }))
    );

    const numAssets = faker.datatype.number({ min: 0, max: 4 });
    const householdAssets = faker.helpers.arrayElements(HOUSEHOLD_ASSETS, numAssets);

    const numVulnerabilities = faker.datatype.number({ min: 0, max: 3 });
    const vulnerabilityIndicators = faker.helpers.arrayElements(VULNERABILITY_INDICATORS, numVulnerabilities);

    return {
      monthlyIncome,
      employmentStatus,
      householdAssets: householdAssets.length > 0 ? householdAssets : ['NONE'],
      vulnerabilityIndicators: vulnerabilityIndicators.length > 0 ? vulnerabilityIndicators : ['NONE']
    };
  }

  // Generate realistic household with members
  generateHousehold(householdId = null) {
    const id = householdId || `HH-${this.householdCounter.toString().padStart(10, '0')}`;
    this.householdCounter++;

    const address = this.generatePhilippineAddress();
    const economicProfile = this.generateEconomicProfile();
    
    // Generate household head
    const headName = this.generateFilipinoName();
    const householdHead = {
      firstName: headName.firstName,
      lastName: headName.lastName,
      birthDate: faker.date.between('1950-01-01', '1990-12-31').toISOString().split('T')[0],
      philsysId: this.generatePhilSysId(),
      relationship: 'HEAD',
      email: `${headName.firstName.toLowerCase()}.${headName.lastName.toLowerCase()}${faker.datatype.number({ min: 1, max: 999 })}@gmail.com`,
      phoneNumber: `+639${faker.datatype.number({ min: 100000000, max: 999999999 })}`
    };

    // Generate family members (realistic family size distribution)
    const familySize = faker.datatype.number({ min: 1, max: 6 }); // 1-6 additional members
    const members = [];

    for (let i = 0; i < familySize; i++) {
      const memberName = this.generateFilipinoName();
      const relationship = i === 0 ? 'SPOUSE' : 'CHILD';
      const birthYear = relationship === 'SPOUSE' ? 
        faker.datatype.number({ min: 1950, max: 1995 }) :
        faker.datatype.number({ min: 2000, max: 2020 });

      members.push({
        firstName: memberName.firstName,
        lastName: headName.lastName, // Same surname as head
        birthDate: `${birthYear}-${faker.datatype.number({ min: 1, max: 12 }).toString().padStart(2, '0')}-${faker.datatype.number({ min: 1, max: 28 }).toString().padStart(2, '0')}`,
        relationship: relationship,
        philsysId: this.generatePhilSysId()
      });
    }

    return {
      id: id,
      householdHead: householdHead,
      address: address,
      members: members,
      economicProfile: economicProfile,
      registrationDate: faker.date.recent(365).toISOString(),
      status: faker.helpers.arrayElement(['PENDING', 'APPROVED', 'REJECTED', 'UNDER_REVIEW'])
    };
  }

  // Generate realistic payment data
  generatePayment(beneficiaryId = null) {
    const id = `PAY-${this.paymentCounter.toString().padStart(10, '0')}`;
    this.paymentCounter++;

    const programs = [
      { name: 'PANTAWID', amount: [1500, 3000, 4500] },
      { name: 'TUPAD', amount: [3900, 7800] },
      { name: 'KALAHI_CIDSS', amount: [5000, 10000, 15000] },
      { name: 'SUSTAINABLE_LIVELIHOOD', amount: [10000, 25000, 50000] }
    ];

    const selectedProgram = faker.helpers.arrayElement(programs);
    const amount = faker.helpers.arrayElement(selectedProgram.amount);

    return {
      id: id,
      beneficiaryId: beneficiaryId || `BEN-${faker.datatype.number({ min: 1000000, max: 9999999 })}`,
      programId: selectedProgram.name,
      amount: amount,
      paymentMethod: faker.helpers.arrayElement(['BANK_TRANSFER', 'CASH_CARD', 'MOBILE_MONEY', 'OVER_THE_COUNTER']),
      scheduledDate: faker.date.future(0.5).toISOString().split('T')[0],
      status: faker.helpers.arrayElement(['PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED']),
      createdDate: faker.date.recent(30).toISOString()
    };
  }

  // Generate realistic grievance data
  generateGrievance(relatedHouseholdId = null) {
    const id = `GRV-${this.grievanceCounter.toString().padStart(10, '0')}`;
    this.grievanceCounter++;

    const categories = [
      'ELIGIBILITY_DISPUTE', 'PAYMENT_DELAY', 'INCORRECT_INFORMATION', 
      'SERVICE_QUALITY', 'SYSTEM_ERROR', 'FRAUD_REPORT', 'GENERAL_INQUIRY'
    ];

    const priorities = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];
    const statuses = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'ESCALATED'];

    const complainantName = this.generateFilipinoName();

    return {
      id: id,
      complainantName: `${complainantName.firstName} ${complainantName.lastName}`,
      complainantEmail: `${complainantName.firstName.toLowerCase()}.${complainantName.lastName.toLowerCase()}${faker.datatype.number({ min: 1, max: 999 })}@gmail.com`,
      complainantPhone: `+639${faker.datatype.number({ min: 100000000, max: 999999999 })}`,
      category: faker.helpers.arrayElement(categories),
      priority: faker.helpers.arrayElement(priorities),
      status: faker.helpers.arrayElement(statuses),
      description: this.generateGrievanceDescription(),
      relatedHouseholdId: relatedHouseholdId || `HH-${faker.datatype.number({ min: 1000000, max: 9999999 })}`,
      submissionDate: faker.date.recent(90).toISOString(),
      expectedResolutionDate: faker.date.future(0.25).toISOString()
    };
  }

  // Generate realistic grievance descriptions
  generateGrievanceDescription() {
    const templates = [
      "Hindi pa natatanggap ang payment para sa buwan ng {month}. Naghihintay na kami ng mahigit {weeks} linggo.",
      "May mali sa eligibility assessment namin. Dapat qualified kami sa {program} program.",
      "Ang sistema ay hindi gumagana kapag nag-uupload ng mga dokumento.",
      "Nawawala ang aming household record sa database. Kailangan namin ng tulong.",
      "May nakita kaming suspicious activity sa aming account. Baka may fraud.",
      "Ang case worker namin ay hindi responsive sa mga tanong namin.",
      "Gusto namin mag-update ng aming information pero hindi kami makapag-login."
    ];

    const template = faker.helpers.arrayElement(templates);
    return template
      .replace('{month}', faker.helpers.arrayElement(['Enero', 'Pebrero', 'Marso', 'Abril', 'Mayo', 'Hunyo']))
      .replace('{weeks}', faker.datatype.number({ min: 2, max: 8 }))
      .replace('{program}', faker.helpers.arrayElement(['Pantawid', 'TUPAD', 'Kalahi-CIDSS']));
  }

  // Generate batch of households for load testing
  generateHouseholdBatch(count) {
    const households = [];
    for (let i = 0; i < count; i++) {
      households.push(this.generateHousehold());
    }
    return households;
  }

  // Generate batch of payments for load testing
  generatePaymentBatch(count) {
    const payments = [];
    for (let i = 0; i < count; i++) {
      payments.push(this.generatePayment());
    }
    return payments;
  }

  // Generate batch of grievances for load testing
  generateGrievanceBatch(count) {
    const grievances = [];
    for (let i = 0; i < count; i++) {
      grievances.push(this.generateGrievance());
    }
    return grievances;
  }

  // Generate production-volume test scenarios
  generateProductionScenarios() {
    return {
      // Daily registration scenario
      dailyRegistrations: {
        normal: this.generateHouseholdBatch(PRODUCTION_VOLUMES.DAILY_REGISTRATIONS),
        peak: this.generateHouseholdBatch(PRODUCTION_VOLUMES.PEAK_DAILY_REGISTRATIONS)
      },
      
      // Monthly processing scenarios
      monthlyEligibilityAssessments: PRODUCTION_VOLUMES.MONTHLY_ELIGIBILITY_ASSESSMENTS,
      monthlyPayments: this.generatePaymentBatch(Math.min(PRODUCTION_VOLUMES.MONTHLY_PAYMENTS, 10000)), // Limited for testing
      monthlyGrievances: this.generateGrievanceBatch(Math.min(PRODUCTION_VOLUMES.MONTHLY_GRIEVANCES, 1000)), // Limited for testing
      
      // Concurrent user scenarios
      concurrentUsers: {
        normal: PRODUCTION_VOLUMES.CONCURRENT_USERS_NORMAL,
        peak: PRODUCTION_VOLUMES.CONCURRENT_USERS_PEAK
      }
    };
  }
}

// Export for use in K6 and other testing frameworks
export { ProductionDataGenerator, PRODUCTION_VOLUMES, PHILIPPINE_REGIONS };

// For Node.js environments
if (typeof module !== 'undefined' && module.exports) {
  module.exports = { ProductionDataGenerator, PRODUCTION_VOLUMES, PHILIPPINE_REGIONS };
}

// Usage examples for testing frameworks
const examples = {
  // K6 load testing usage
  k6Usage: `
    import { ProductionDataGenerator } from './data-generators/production-data-generator.js';

    const generator = new ProductionDataGenerator();
    const household = generator.generateHousehold();
    const payment = generator.generatePayment(household.id);
  `,

  // Playwright testing usage
  playwrightUsage: `
    const { ProductionDataGenerator } = require('./data-generators/production-data-generator.js');

    const generator = new ProductionDataGenerator();
    const testData = generator.generateProductionScenarios();
  `,

  // Batch generation for stress testing
  batchGeneration: `
    const generator = new ProductionDataGenerator();
    const households = generator.generateHouseholdBatch(1000);
    const payments = generator.generatePaymentBatch(5000);
    const grievances = generator.generateGrievanceBatch(500);
  `
};

export { examples };
