import { faker } from '@faker-js/faker';

/**
 * Test Data Manager for generating and managing test data
 * Provides consistent test data across all test scenarios
 */
export class TestDataManager {
  
  /**
   * Generate test user data
   */
  static generateUserData(role: 'citizen' | 'admin' | 'lgu_staff' = 'citizen') {
    const firstName = faker.person.firstName();
    const lastName = faker.person.lastName();
    const email = `${firstName.toLowerCase()}.${lastName.toLowerCase()}@test.dsr.gov.ph`;

    return {
      firstName,
      lastName,
      middleName: faker.person.middleName(),
      email,
      password: 'TestPassword123!',
      role,
      phoneNumber: faker.phone.number('+63##########'),
      birthDate: faker.date.birthdate({ min: 18, max: 80, mode: 'age' }).toISOString().split('T')[0],
      gender: faker.person.sexType(),
      civilStatus: faker.helpers.arrayElement(['single', 'married', 'widowed', 'separated']),
      psn: this.generatePSN(),
    };
  }

  /**
   * Generate PhilSys Number (PSN)
   */
  static generatePSN(): string {
    return faker.string.numeric(16);
  }

  /**
   * Generate household data
   */
  static generateHouseholdData() {
    const headOfHousehold = this.generateUserData('citizen');
    const memberCount = faker.number.int({ min: 1, max: 5 });
    const members = [];

    // Add head of household as first member
    members.push({
      ...headOfHousehold,
      relationshipToHead: 'HEAD',
      isHeadOfHousehold: true,
    });

    // Add additional family members
    for (let i = 1; i < memberCount; i++) {
      const member = this.generateUserData('citizen');
      members.push({
        ...member,
        relationshipToHead: faker.helpers.arrayElement(['SPOUSE', 'CHILD', 'PARENT', 'SIBLING', 'OTHER']),
        isHeadOfHousehold: false,
      });
    }

    return {
      headOfHouseholdPsn: headOfHousehold.psn,
      address: this.generateAddressData(),
      members,
      economicProfile: this.generateEconomicProfile(),
      contactInformation: {
        mobileNumber: headOfHousehold.phoneNumber,
        emailAddress: headOfHousehold.email,
        preferredContactMethod: 'mobile',
      },
      consentGiven: true,
      preferredLanguage: 'en',
    };
  }

  /**
   * Generate address data
   */
  static generateAddressData() {
    return {
      region: faker.helpers.arrayElement(['I', 'II', 'III', 'IV-A', 'IV-B', 'V', 'VI', 'VII', 'VIII', 'IX', 'X', 'XI', 'XII', 'XIII', 'NCR', 'CAR', 'BARMM']),
      province: faker.location.state(),
      municipality: faker.location.city(),
      barangay: `Barangay ${faker.location.streetName()}`,
      streetAddress: `${faker.location.buildingNumber()} ${faker.location.street()}`,
      zipCode: faker.location.zipCode(),
      coordinates: {
        latitude: faker.location.latitude({ min: 4.5, max: 21.5 }),
        longitude: faker.location.longitude({ min: 116, max: 127 }),
      },
    };
  }

  /**
   * Generate economic profile data
   */
  static generateEconomicProfile() {
    return {
      monthlyIncome: faker.number.float({ min: 5000, max: 50000, fractionDigits: 2 }),
      employmentStatus: faker.helpers.arrayElement(['employed', 'unemployed', 'self_employed', 'retired', 'student']),
      occupation: faker.person.jobTitle(),
      educationLevel: faker.helpers.arrayElement(['elementary', 'high_school', 'college', 'vocational', 'graduate']),
      householdAssets: {
        hasElectricity: faker.datatype.boolean(),
        hasWaterSupply: faker.datatype.boolean(),
        hasToilet: faker.datatype.boolean(),
        houseMaterial: faker.helpers.arrayElement(['concrete', 'wood', 'bamboo', 'mixed']),
        roofMaterial: faker.helpers.arrayElement(['concrete', 'galvanized_iron', 'nipa', 'mixed']),
      },
    };
  }

  /**
   * Generate payment data
   */
  static generatePaymentData(householdId: string) {
    return {
      householdId,
      programName: faker.helpers.arrayElement(['4Ps', 'DSWD-SLP', 'KALAHI-CIDSS', 'AICS']),
      amount: faker.number.float({ min: 500, max: 5000, fractionDigits: 2 }),
      paymentMethod: faker.helpers.arrayElement(['bank_transfer', 'cash_card', 'mobile_wallet']),
      beneficiaryAccount: {
        accountNumber: faker.finance.accountNumber(10),
        bankCode: faker.helpers.arrayElement(['LBP', 'DBP', 'BPI', 'BDO', 'GCASH', 'PAYMAYA']),
        accountName: faker.person.fullName(),
      },
    };
  }

  /**
   * Generate grievance data
   */
  static generateGrievanceData(householdId: string) {
    return {
      householdId,
      grievanceType: faker.helpers.arrayElement(['payment_issue', 'registration_problem', 'service_complaint', 'data_correction', 'other']),
      subject: faker.lorem.sentence(),
      description: faker.lorem.paragraphs(2),
      priority: faker.helpers.arrayElement(['low', 'medium', 'high', 'urgent']),
      contactMethod: faker.helpers.arrayElement(['email', 'phone', 'sms']),
      contactDetails: faker.internet.email(),
      attachments: [],
    };
  }

  /**
   * Generate eligibility assessment data
   */
  static generateEligibilityData(householdId: string) {
    return {
      householdId,
      programName: faker.helpers.arrayElement(['4Ps', 'DSWD-SLP', 'KALAHI-CIDSS', 'AICS']),
      assessmentType: faker.helpers.arrayElement(['initial', 'reassessment', 'appeal']),
      assessmentDate: new Date().toISOString(),
    };
  }

  /**
   * Generate login credentials for different user types
   * Uses actual demo credentials from the DSR system
   */
  static getTestCredentials(userType: 'admin' | 'user' | 'lgu_staff' = 'user') {
    const credentials = {
      admin: {
        email: process.env.TEST_ADMIN_EMAIL || 'admin@dsr.gov.ph',
        password: process.env.TEST_ADMIN_PASSWORD || 'admin123',
      },
      user: {
        email: process.env.TEST_USER_EMAIL || 'citizen@dsr.gov.ph',
        password: process.env.TEST_USER_PASSWORD || 'citizen123',
      },
      lgu_staff: {
        email: process.env.TEST_LGU_STAFF_EMAIL || 'lgu.staff@dsr.gov.ph',
        password: process.env.TEST_LGU_STAFF_PASSWORD || 'lgustaff123',
      },
    };

    return credentials[userType];
  }

  /**
   * Generate invalid data for negative testing
   */
  static generateInvalidData() {
    return {
      invalidEmail: 'invalid-email',
      shortPassword: '123',
      invalidPSN: '123',
      invalidPhoneNumber: '123',
      emptyString: '',
      nullValue: null,
      undefinedValue: undefined,
      specialCharacters: '!@#$%^&*()',
      sqlInjection: "'; DROP TABLE users; --",
      xssPayload: '<script>alert("xss")</script>',
    };
  }

  /**
   * Clean up test data (for use in teardown)
   */
  static async cleanupTestData(testRunId: string) {
    // This would typically connect to the database and clean up test data
    // For now, we'll just log the cleanup action
    console.log(`Cleaning up test data for test run: ${testRunId}`);
  }

  /**
   * Generate unique test run identifier
   */
  static generateTestRunId(): string {
    return `test-${Date.now()}-${faker.string.alphanumeric(8)}`;
  }
}
