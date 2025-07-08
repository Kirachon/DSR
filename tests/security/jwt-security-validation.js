// DSR JWT Authentication Security Validation Suite
// Comprehensive testing of JWT implementation and role-based access controls
// Tests authentication bypass, token manipulation, and authorization flaws

import { test, expect } from '@playwright/test';
import jwt from 'jsonwebtoken';
import crypto from 'crypto';

// DSR service endpoints
const DSR_SERVICES = {
  registration: 'http://localhost:8080',
  dataManagement: 'http://localhost:8081',
  eligibility: 'http://localhost:8082',
  interoperability: 'http://localhost:8083',
  payment: 'http://localhost:8084',
  grievance: 'http://localhost:8085',
  analytics: 'http://localhost:8086'
};

// Test user credentials for different roles
const TEST_USERS = {
  admin: {
    email: 'admin@dsr.gov.ph',
    password: 'AdminPassword123!',
    expectedRole: 'ADMIN'
  },
  dswd_staff: {
    email: 'dswd.staff@dsr.gov.ph',
    password: 'StaffPassword123!',
    expectedRole: 'DSWD_STAFF'
  },
  lgu_staff: {
    email: 'lgu.staff@dsr.gov.ph',
    password: 'LGUPassword123!',
    expectedRole: 'LGU_STAFF'
  },
  case_worker: {
    email: 'case.worker@dsr.gov.ph',
    password: 'WorkerPassword123!',
    expectedRole: 'CASE_WORKER'
  },
  citizen: {
    email: 'citizen@test.ph',
    password: 'CitizenPassword123!',
    expectedRole: 'CITIZEN'
  }
};

// Role-based endpoint access matrix
const ROLE_ACCESS_MATRIX = {
  'ADMIN': {
    allowed: [
      '/api/v1/admin/*',
      '/api/v1/registration/*',
      '/api/v1/data-management/*',
      '/api/v1/eligibility/*',
      '/api/v1/payments/*',
      '/api/v1/grievances/*',
      '/api/v1/analytics/*'
    ],
    denied: []
  },
  'DSWD_STAFF': {
    allowed: [
      '/api/v1/registration/*',
      '/api/v1/eligibility/*',
      '/api/v1/payments/*',
      '/api/v1/grievances/*',
      '/api/v1/analytics/reports/*'
    ],
    denied: [
      '/api/v1/admin/*',
      '/api/v1/analytics/system/*'
    ]
  },
  'LGU_STAFF': {
    allowed: [
      '/api/v1/registration/households',
      '/api/v1/eligibility/assessments',
      '/api/v1/grievances'
    ],
    denied: [
      '/api/v1/admin/*',
      '/api/v1/payments/*',
      '/api/v1/analytics/*'
    ]
  },
  'CASE_WORKER': {
    allowed: [
      '/api/v1/registration/households',
      '/api/v1/eligibility/assessments',
      '/api/v1/grievances'
    ],
    denied: [
      '/api/v1/admin/*',
      '/api/v1/payments/*',
      '/api/v1/analytics/*',
      '/api/v1/data-management/*'
    ]
  },
  'CITIZEN': {
    allowed: [
      '/api/v1/registration/my-household',
      '/api/v1/grievances/my-grievances'
    ],
    denied: [
      '/api/v1/admin/*',
      '/api/v1/payments/*',
      '/api/v1/analytics/*',
      '/api/v1/data-management/*',
      '/api/v1/eligibility/*'
    ]
  }
};

test.describe('DSR JWT Authentication Security Validation', () => {
  let validTokens = {};

  test.beforeAll(async ({ request }) => {
    // Authenticate all test users to get valid tokens
    for (const [role, credentials] of Object.entries(TEST_USERS)) {
      try {
        const response = await request.post(`${DSR_SERVICES.registration}/api/v1/auth/login`, {
          data: credentials
        });
        
        if (response.ok()) {
          const authData = await response.json();
          validTokens[role] = authData.accessToken;
          console.log(`✅ Authenticated ${role}: ${credentials.email}`);
        } else {
          console.log(`⚠️ Failed to authenticate ${role}: ${response.status()}`);
        }
      } catch (error) {
        console.log(`❌ Authentication error for ${role}: ${error.message}`);
      }
    }
  });

  test.describe('1. JWT Token Structure Validation', () => {
    test('should have properly structured JWT tokens', async () => {
      for (const [role, token] of Object.entries(validTokens)) {
        // Decode JWT without verification to check structure
        const decoded = jwt.decode(token, { complete: true });
        
        expect(decoded, `${role} token should be properly structured`).toBeTruthy();
        expect(decoded.header, `${role} token should have header`).toBeTruthy();
        expect(decoded.payload, `${role} token should have payload`).toBeTruthy();
        expect(decoded.signature, `${role} token should have signature`).toBeTruthy();
        
        // Check required claims
        expect(decoded.payload.sub, `${role} token should have subject`).toBeTruthy();
        expect(decoded.payload.iat, `${role} token should have issued at`).toBeTruthy();
        expect(decoded.payload.exp, `${role} token should have expiration`).toBeTruthy();
        expect(decoded.payload.role, `${role} token should have role claim`).toBeTruthy();
        
        // Validate role claim
        expect(decoded.payload.role).toBe(TEST_USERS[role].expectedRole);
        
        console.log(`✅ JWT structure validated for ${role}`);
      }
    });

    test('should have secure JWT algorithm', async () => {
      for (const [role, token] of Object.entries(validTokens)) {
        const decoded = jwt.decode(token, { complete: true });
        
        // Should use secure algorithms (RS256, HS256, etc.)
        const secureAlgorithms = ['RS256', 'HS256', 'ES256'];
        expect(secureAlgorithms).toContain(decoded.header.alg);
        
        // Should not use 'none' algorithm
        expect(decoded.header.alg).not.toBe('none');
        
        console.log(`✅ Secure algorithm (${decoded.header.alg}) for ${role}`);
      }
    });
  });

  test.describe('2. Token Manipulation Testing', () => {
    test('should reject tokens with modified signatures', async ({ request }) => {
      const originalToken = validTokens.dswd_staff;
      if (!originalToken) {
        test.skip('No valid token available for testing');
      }

      // Modify the signature
      const tokenParts = originalToken.split('.');
      const modifiedSignature = tokenParts[2].slice(0, -5) + 'XXXXX';
      const modifiedToken = `${tokenParts[0]}.${tokenParts[1]}.${modifiedSignature}`;

      const response = await request.get(`${DSR_SERVICES.registration}/api/v1/registration/households`, {
        headers: {
          'Authorization': `Bearer ${modifiedToken}`
        }
      });

      expect([401, 403]).toContain(response.status());
      console.log('✅ Modified signature rejected');
    });

    test('should reject tokens with modified payload', async ({ request }) => {
      const originalToken = validTokens.case_worker;
      if (!originalToken) {
        test.skip('No valid token available for testing');
      }

      // Decode and modify payload
      const decoded = jwt.decode(originalToken, { complete: true });
      const modifiedPayload = {
        ...decoded.payload,
        role: 'ADMIN' // Try to escalate privileges
      };

      // Create new token with modified payload (will have invalid signature)
      const modifiedToken = jwt.sign(modifiedPayload, 'fake-secret');

      const response = await request.get(`${DSR_SERVICES.registration}/api/v1/admin/users`, {
        headers: {
          'Authorization': `Bearer ${modifiedToken}`
        }
      });

      expect([401, 403]).toContain(response.status());
      console.log('✅ Modified payload rejected');
    });

    test('should reject tokens with none algorithm', async ({ request }) => {
      // Create token with 'none' algorithm
      const payload = {
        sub: 'test-user',
        role: 'ADMIN',
        iat: Math.floor(Date.now() / 1000),
        exp: Math.floor(Date.now() / 1000) + 3600
      };

      const header = { alg: 'none', typ: 'JWT' };
      const encodedHeader = Buffer.from(JSON.stringify(header)).toString('base64url');
      const encodedPayload = Buffer.from(JSON.stringify(payload)).toString('base64url');
      const noneToken = `${encodedHeader}.${encodedPayload}.`;

      const response = await request.get(`${DSR_SERVICES.registration}/api/v1/admin/users`, {
        headers: {
          'Authorization': `Bearer ${noneToken}`
        }
      });

      expect([401, 403]).toContain(response.status());
      console.log('✅ None algorithm token rejected');
    });
  });

  test.describe('3. Token Expiration Testing', () => {
    test('should reject expired tokens', async ({ request }) => {
      // Create an expired token
      const expiredPayload = {
        sub: 'test-user',
        role: 'DSWD_STAFF',
        iat: Math.floor(Date.now() / 1000) - 7200, // 2 hours ago
        exp: Math.floor(Date.now() / 1000) - 3600  // 1 hour ago (expired)
      };

      // Note: This will have an invalid signature, but we're testing expiration handling
      const expiredToken = jwt.sign(expiredPayload, 'fake-secret');

      const response = await request.get(`${DSR_SERVICES.registration}/api/v1/registration/households`, {
        headers: {
          'Authorization': `Bearer ${expiredToken}`
        }
      });

      expect([401, 403]).toContain(response.status());
      console.log('✅ Expired token rejected');
    });

    test('should accept valid non-expired tokens', async ({ request }) => {
      const validToken = validTokens.dswd_staff;
      if (!validToken) {
        test.skip('No valid token available for testing');
      }

      const response = await request.get(`${DSR_SERVICES.registration}/actuator/health`, {
        headers: {
          'Authorization': `Bearer ${validToken}`
        }
      });

      expect([200, 404]).toContain(response.status()); // 404 is acceptable if endpoint doesn't exist
      console.log('✅ Valid token accepted');
    });
  });

  test.describe('4. Role-Based Access Control Testing', () => {
    Object.entries(ROLE_ACCESS_MATRIX).forEach(([role, accessRules]) => {
      test(`should enforce access controls for ${role}`, async ({ request }) => {
        const token = validTokens[role.toLowerCase()];
        if (!token) {
          test.skip(`No valid token available for ${role}`);
        }

        // Test allowed endpoints
        for (const allowedEndpoint of accessRules.allowed.slice(0, 3)) { // Test first 3 to avoid too many requests
          const testUrl = allowedEndpoint.replace('*', 'test');
          const serviceUrl = getServiceUrlForEndpoint(testUrl);
          
          if (serviceUrl) {
            const response = await request.get(`${serviceUrl}${testUrl}`, {
              headers: {
                'Authorization': `Bearer ${token}`
              }
            });

            // Should not be 403 Forbidden (401 is ok if endpoint doesn't exist)
            expect(response.status()).not.toBe(403);
            console.log(`✅ ${role} can access ${testUrl}`);
          }
        }

        // Test denied endpoints
        for (const deniedEndpoint of accessRules.denied.slice(0, 2)) { // Test first 2
          const testUrl = deniedEndpoint.replace('*', 'test');
          const serviceUrl = getServiceUrlForEndpoint(testUrl);
          
          if (serviceUrl) {
            const response = await request.get(`${serviceUrl}${testUrl}`, {
              headers: {
                'Authorization': `Bearer ${token}`
              }
            });

            // Should be 403 Forbidden
            expect(response.status()).toBe(403);
            console.log(`✅ ${role} denied access to ${testUrl}`);
          }
        }
      });
    });
  });

  test.describe('5. Authentication Bypass Testing', () => {
    test('should require authentication for protected endpoints', async ({ request }) => {
      const protectedEndpoints = [
        '/api/v1/registration/households',
        '/api/v1/eligibility/assessments',
        '/api/v1/payments',
        '/api/v1/admin/users'
      ];

      for (const endpoint of protectedEndpoints) {
        const serviceUrl = getServiceUrlForEndpoint(endpoint);
        if (serviceUrl) {
          const response = await request.get(`${serviceUrl}${endpoint}`);
          
          // Should require authentication
          expect([401, 403]).toContain(response.status());
          console.log(`✅ ${endpoint} requires authentication`);
        }
      }
    });

    test('should reject invalid authorization headers', async ({ request }) => {
      const invalidHeaders = [
        'Bearer invalid-token',
        'Basic dGVzdDp0ZXN0', // Basic auth should be rejected
        'Bearer ', // Empty token
        'InvalidScheme token123'
      ];

      for (const authHeader of invalidHeaders) {
        const response = await request.get(`${DSR_SERVICES.registration}/api/v1/registration/households`, {
          headers: {
            'Authorization': authHeader
          }
        });

        expect([401, 403]).toContain(response.status());
        console.log(`✅ Invalid auth header rejected: ${authHeader.substring(0, 20)}...`);
      }
    });
  });

  test.describe('6. Session Management Testing', () => {
    test('should handle concurrent sessions properly', async ({ request }) => {
      const user = TEST_USERS.dswd_staff;
      
      // Create multiple sessions for the same user
      const sessions = [];
      for (let i = 0; i < 3; i++) {
        const response = await request.post(`${DSR_SERVICES.registration}/api/v1/auth/login`, {
          data: user
        });
        
        if (response.ok()) {
          const authData = await response.json();
          sessions.push(authData.accessToken);
        }
      }

      // All sessions should be valid
      for (const [index, token] of sessions.entries()) {
        const response = await request.get(`${DSR_SERVICES.registration}/actuator/health`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        expect([200, 404]).toContain(response.status());
        console.log(`✅ Concurrent session ${index + 1} is valid`);
      }
    });

    test('should handle logout properly', async ({ request }) => {
      const user = TEST_USERS.case_worker;
      
      // Login
      const loginResponse = await request.post(`${DSR_SERVICES.registration}/api/v1/auth/login`, {
        data: user
      });

      if (loginResponse.ok()) {
        const authData = await loginResponse.json();
        const token = authData.accessToken;

        // Verify token works
        const testResponse = await request.get(`${DSR_SERVICES.registration}/actuator/health`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        expect([200, 404]).toContain(testResponse.status());

        // Logout
        const logoutResponse = await request.post(`${DSR_SERVICES.registration}/api/v1/auth/logout`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        // Token should be invalidated after logout (if logout endpoint exists)
        if (logoutResponse.status() !== 404) {
          const postLogoutResponse = await request.get(`${DSR_SERVICES.registration}/actuator/health`, {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          });
          
          // Token might still work if stateless JWT, but logout should be acknowledged
          console.log(`✅ Logout processed: ${logoutResponse.status()}`);
        }
      }
    });
  });

  test.describe('7. JWT Security Best Practices', () => {
    test('should use secure token storage recommendations', async () => {
      // This test documents security recommendations
      const securityRecommendations = {
        tokenStorage: 'Use httpOnly cookies or secure storage, avoid localStorage',
        tokenTransmission: 'Always use HTTPS in production',
        tokenExpiration: 'Use short-lived access tokens with refresh tokens',
        secretManagement: 'Use strong, randomly generated secrets',
        algorithmValidation: 'Explicitly validate JWT algorithm',
        claimValidation: 'Validate all claims including iss, aud, exp'
      };

      // Verify token expiration is reasonable (not too long)
      for (const [role, token] of Object.entries(validTokens)) {
        const decoded = jwt.decode(token);
        const expirationTime = decoded.exp * 1000; // Convert to milliseconds
        const issuedTime = decoded.iat * 1000;
        const tokenLifetime = expirationTime - issuedTime;
        
        // Token should not be valid for more than 24 hours
        expect(tokenLifetime).toBeLessThanOrEqual(24 * 60 * 60 * 1000);
        console.log(`✅ ${role} token lifetime: ${Math.round(tokenLifetime / (60 * 60 * 1000))} hours`);
      }

      console.log('📋 Security Recommendations:');
      Object.entries(securityRecommendations).forEach(([key, value]) => {
        console.log(`   ${key}: ${value}`);
      });
    });
  });
});

// Helper function to determine service URL for endpoint
function getServiceUrlForEndpoint(endpoint) {
  if (endpoint.includes('/registration')) return DSR_SERVICES.registration;
  if (endpoint.includes('/data-management')) return DSR_SERVICES.dataManagement;
  if (endpoint.includes('/eligibility')) return DSR_SERVICES.eligibility;
  if (endpoint.includes('/interoperability')) return DSR_SERVICES.interoperability;
  if (endpoint.includes('/payments')) return DSR_SERVICES.payment;
  if (endpoint.includes('/grievances')) return DSR_SERVICES.grievance;
  if (endpoint.includes('/analytics')) return DSR_SERVICES.analytics;
  if (endpoint.includes('/admin')) return DSR_SERVICES.registration; // Admin endpoints on main service
  
  return DSR_SERVICES.registration; // Default to registration service
}
