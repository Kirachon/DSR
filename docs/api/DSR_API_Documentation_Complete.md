# DSR API Documentation - Complete Reference
**Version:** 3.0.0  
**Last Updated:** June 28, 2025  
**Status:** ✅ PRODUCTION READY - All API documentation finalized  
**Phase:** 2.4.1 Implementation - COMPLETED  

## Table of Contents
1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Registration Service API](#registration-service-api)
4. [Data Management Service API](#data-management-service-api)
5. [Eligibility Service API](#eligibility-service-api)
6. [Payment Service API](#payment-service-api)
7. [Interoperability Service API](#interoperability-service-api)
8. [Grievance Service API](#grievance-service-api)
9. [Analytics Service API](#analytics-service-api)
10. [Error Handling](#error-handling)
11. [Rate Limiting](#rate-limiting)
12. [Integration Examples](#integration-examples)

## Overview

The DSR (Dynamic Social Registry) API provides comprehensive access to all social protection services through RESTful endpoints. All APIs follow OpenAPI 3.0 specification and support JSON request/response formats.

### Base URLs
- **Production:** `https://api.dsr.gov.ph`
- **Staging:** `https://staging-api.dsr.gov.ph`
- **Development:** `http://localhost:8080`

### API Versioning
All APIs are versioned using URL path versioning: `/api/v1/`

### Content Types
- **Request:** `application/json`
- **Response:** `application/json`
- **File Upload:** `multipart/form-data`

## Authentication

### JWT Bearer Token Authentication
All API endpoints require authentication using JWT Bearer tokens.

#### Login Endpoint
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@dsr.gov.ph",
  "password": "SecurePassword123!"
}
```

#### Response
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "uuid",
    "email": "user@dsr.gov.ph",
    "role": "LGU_STAFF",
    "permissions": ["READ_REGISTRATIONS", "WRITE_REGISTRATIONS"]
  }
}
```

#### Using the Token
Include the token in the Authorization header:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Token Refresh
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

## Registration Service API

### Base URL: `/api/v1/registration`

#### Create Household Registration
```http
POST /api/v1/registration/households
Authorization: Bearer {token}
Content-Type: application/json

{
  "householdNumber": "HH-2025-001234",
  "headOfHousehold": {
    "firstName": "Juan",
    "lastName": "Dela Cruz",
    "middleName": "Santos",
    "birthDate": "1985-05-15",
    "gender": "MALE",
    "civilStatus": "MARRIED",
    "phoneNumber": "+639171234567",
    "email": "juan.delacruz@email.com",
    "philsysNumber": "1234-5678-9012"
  },
  "address": {
    "houseNumber": "123",
    "street": "Rizal Street",
    "barangay": "Poblacion",
    "municipality": "Quezon City",
    "province": "Metro Manila",
    "region": "NCR",
    "zipCode": "1100"
  },
  "economicProfile": {
    "monthlyIncome": 15000,
    "employmentStatus": "EMPLOYED",
    "householdSize": 4,
    "housingType": "OWNED",
    "hasElectricity": true,
    "hasWaterSupply": true
  },
  "members": [
    {
      "firstName": "Maria",
      "lastName": "Dela Cruz",
      "relationship": "SPOUSE",
      "birthDate": "1987-08-20",
      "gender": "FEMALE"
    }
  ]
}
```

#### Response
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "householdNumber": "HH-2025-001234",
  "status": "DRAFT",
  "createdAt": "2025-06-28T10:30:00Z",
  "updatedAt": "2025-06-28T10:30:00Z",
  "headOfHousehold": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "firstName": "Juan",
    "lastName": "Dela Cruz",
    "philsysNumber": "1234-5678-9012"
  },
  "validationResults": {
    "isValid": true,
    "errors": [],
    "warnings": []
  }
}
```

#### Get Household Registration
```http
GET /api/v1/registration/households/{id}
Authorization: Bearer {token}
```

#### Update Household Registration
```http
PUT /api/v1/registration/households/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "status": "SUBMITTED",
  "notes": "All required documents submitted"
}
```

#### List Household Registrations
```http
GET /api/v1/registration/households?page=0&size=20&status=APPROVED&region=NCR
Authorization: Bearer {token}
```

#### Approve Registration
```http
POST /api/v1/registration/households/{id}/approve
Authorization: Bearer {token}
Content-Type: application/json

{
  "approvalNotes": "All documents verified and approved",
  "approvedBy": "LGU Staff ID"
}
```

## Data Management Service API

### Base URL: `/api/v1/data-management`

#### PhilSys Verification
```http
POST /api/v1/data-management/philsys/verify
Authorization: Bearer {token}
Content-Type: application/json

{
  "philsysNumber": "1234-5678-9012",
  "firstName": "Juan",
  "lastName": "Dela Cruz",
  "birthDate": "1985-05-15"
}
```

#### Response
```json
{
  "verificationId": "550e8400-e29b-41d4-a716-446655440002",
  "status": "VERIFIED",
  "matchScore": 0.98,
  "verifiedData": {
    "philsysNumber": "1234-5678-9012",
    "fullName": "Juan Santos Dela Cruz",
    "birthDate": "1985-05-15",
    "address": "123 Rizal Street, Poblacion, Quezon City"
  },
  "verificationDate": "2025-06-28T10:35:00Z"
}
```

#### Data Validation
```http
POST /api/v1/data-management/validate
Authorization: Bearer {token}
Content-Type: application/json

{
  "dataType": "HOUSEHOLD_REGISTRATION",
  "data": {
    "householdNumber": "HH-2025-001234",
    "headOfHousehold": {
      "firstName": "Juan",
      "lastName": "Dela Cruz"
    }
  }
}
```

#### Deduplication Check
```http
POST /api/v1/data-management/deduplication/check
Authorization: Bearer {token}
Content-Type: application/json

{
  "firstName": "Juan",
  "lastName": "Dela Cruz",
  "birthDate": "1985-05-15",
  "phoneNumber": "+639171234567"
}
```

## Eligibility Service API

### Base URL: `/api/v1/eligibility`

#### Conduct Eligibility Assessment
```http
POST /api/v1/eligibility/assessments
Authorization: Bearer {token}
Content-Type: application/json

{
  "householdId": "550e8400-e29b-41d4-a716-446655440000",
  "programName": "4Ps",
  "assessmentType": "FULL",
  "assessmentData": {
    "monthlyIncome": 15000,
    "householdSize": 4,
    "hasChildren": true,
    "childrenAges": [5, 8, 12],
    "hasPregnantMember": false,
    "hasDisabledMember": false,
    "housingType": "OWNED",
    "hasElectricity": true,
    "hasWaterSupply": true
  }
}
```

#### Response
```json
{
  "assessmentId": "550e8400-e29b-41d4-a716-446655440003",
  "householdId": "550e8400-e29b-41d4-a716-446655440000",
  "programName": "4Ps",
  "status": "ELIGIBLE",
  "pmtScore": 42.5,
  "eligibilityThreshold": 50.0,
  "assessmentDate": "2025-06-28T11:00:00Z",
  "validUntil": "2026-06-28T11:00:00Z",
  "conditions": [
    {
      "condition": "CHILDREN_SCHOOL_ATTENDANCE",
      "description": "All children must maintain 85% school attendance",
      "required": true
    },
    {
      "condition": "HEALTH_CHECKUPS",
      "description": "Regular health checkups for children and pregnant women",
      "required": true
    }
  ],
  "benefitAmount": 1400.00,
  "paymentFrequency": "MONTHLY"
}
```

#### Get Assessment Results
```http
GET /api/v1/eligibility/assessments/{id}
Authorization: Bearer {token}
```

#### List Assessments by Household
```http
GET /api/v1/eligibility/assessments?householdId={id}&program=4Ps
Authorization: Bearer {token}
```

## Payment Service API

### Base URL: `/api/v1/payments`

#### Create Payment Request
```http
POST /api/v1/payments
Authorization: Bearer {token}
Content-Type: application/json

{
  "householdId": "550e8400-e29b-41d4-a716-446655440000",
  "programName": "4Ps",
  "amount": 1400.00,
  "paymentMethod": "BANK_TRANSFER",
  "beneficiaryAccount": {
    "accountNumber": "1234567890123456",
    "bankCode": "LBP",
    "accountName": "Juan Dela Cruz"
  },
  "paymentPeriod": {
    "startDate": "2025-06-01",
    "endDate": "2025-06-30"
  },
  "conditions": [
    {
      "conditionId": "SCHOOL_ATTENDANCE",
      "status": "COMPLIED",
      "verificationDate": "2025-06-25"
    }
  ]
}
```

#### Response
```json
{
  "paymentId": "550e8400-e29b-41d4-a716-446655440004",
  "householdId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING",
  "amount": 1400.00,
  "paymentMethod": "BANK_TRANSFER",
  "createdAt": "2025-06-28T12:00:00Z",
  "estimatedProcessingDate": "2025-06-30T12:00:00Z",
  "referenceNumber": "PAY-2025-001234",
  "fspTransactionId": null
}
```

#### Process Payment
```http
POST /api/v1/payments/{id}/process
Authorization: Bearer {token}
Content-Type: application/json

{
  "processingNotes": "Payment approved and sent to FSP",
  "processedBy": "Payment Officer ID"
}
```

#### Get Payment Status
```http
GET /api/v1/payments/{id}
Authorization: Bearer {token}
```

#### List Payments
```http
GET /api/v1/payments?householdId={id}&status=COMPLETED&page=0&size=20
Authorization: Bearer {token}
```

## Interoperability Service API

### Base URL: `/api/v1/interoperability`

#### External System Integration
```http
POST /api/v1/interoperability/external-systems/{systemId}/sync
Authorization: Bearer {token}
Content-Type: application/json

{
  "syncType": "INCREMENTAL",
  "dataTypes": ["HOUSEHOLDS", "PAYMENTS"],
  "filters": {
    "dateFrom": "2025-06-01",
    "dateTo": "2025-06-28",
    "region": "NCR"
  }
}
```

#### Service Delivery Record
```http
POST /api/v1/interoperability/service-delivery
Authorization: Bearer {token}
Content-Type: application/json

{
  "householdId": "550e8400-e29b-41d4-a716-446655440000",
  "serviceType": "HEALTH_CHECKUP",
  "serviceProvider": "DOH",
  "serviceDate": "2025-06-28",
  "serviceDetails": {
    "facilityName": "Quezon City Health Center",
    "serviceDescription": "Routine health checkup for children",
    "beneficiaries": ["Child 1", "Child 2"]
  }
}
```

## Grievance Service API

### Base URL: `/api/v1/grievance`

#### Create Grievance Case
```http
POST /api/v1/grievance/cases
Authorization: Bearer {token}
Content-Type: application/json

{
  "householdId": "550e8400-e29b-41d4-a716-446655440000",
  "subject": "Payment Delay Issue",
  "description": "Monthly payment for June 2025 has not been received",
  "category": "PAYMENT_ISSUE",
  "priorityLevel": "MEDIUM",
  "channel": "ONLINE_PORTAL",
  "contactInformation": {
    "email": "juan.delacruz@email.com",
    "phoneNumber": "+639171234567"
  },
  "attachments": [
    {
      "fileName": "payment_receipt.pdf",
      "fileType": "application/pdf",
      "fileSize": 1024000
    }
  ]
}
```

#### Response
```json
{
  "caseId": "550e8400-e29b-41d4-a716-446655440005",
  "caseNumber": "GRV-2025-001234",
  "status": "OPEN",
  "priorityLevel": "MEDIUM",
  "createdAt": "2025-06-28T13:00:00Z",
  "estimatedResolutionDate": "2025-07-05T13:00:00Z",
  "assignedTo": null,
  "slaDeadline": "2025-07-12T13:00:00Z"
}
```

#### Update Case Status
```http
PUT /api/v1/grievance/cases/{id}/status
Authorization: Bearer {token}
Content-Type: application/json

{
  "status": "IN_PROGRESS",
  "assignedTo": "case-worker-id",
  "notes": "Case assigned to case worker for investigation"
}
```

## Analytics Service API

### Base URL: `/api/v1/analytics`

#### Generate Dashboard Data
```http
GET /api/v1/analytics/dashboard?region=NCR&dateFrom=2025-06-01&dateTo=2025-06-28
Authorization: Bearer {token}
```

#### Response
```json
{
  "summary": {
    "totalRegistrations": 15420,
    "approvedRegistrations": 12336,
    "pendingRegistrations": 2084,
    "totalPayments": 8945,
    "totalPaymentAmount": 12523000.00,
    "activeGrievances": 156
  },
  "trends": {
    "registrationTrend": [
      {"date": "2025-06-01", "count": 45},
      {"date": "2025-06-02", "count": 52}
    ],
    "paymentTrend": [
      {"date": "2025-06-01", "amount": 125000.00},
      {"date": "2025-06-02", "amount": 134000.00}
    ]
  },
  "demographics": {
    "byRegion": [
      {"region": "NCR", "count": 3456},
      {"region": "Region IV-A", "count": 2890}
    ],
    "byProgram": [
      {"program": "4Ps", "count": 8945},
      {"program": "DSWD-SLP", "count": 1234}
    ]
  }
}
```

#### Generate Custom Report
```http
POST /api/v1/analytics/reports
Authorization: Bearer {token}
Content-Type: application/json

{
  "reportType": "PAYMENT_SUMMARY",
  "parameters": {
    "dateFrom": "2025-06-01",
    "dateTo": "2025-06-28",
    "region": "NCR",
    "program": "4Ps"
  },
  "format": "PDF",
  "includeCharts": true
}
```

## Error Handling

### Standard Error Response Format
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed",
    "details": [
      {
        "field": "email",
        "message": "Invalid email format"
      }
    ],
    "timestamp": "2025-06-28T10:30:00Z",
    "path": "/api/v1/registration/households"
  }
}
```

### Common Error Codes
- `400` - Bad Request (validation errors)
- `401` - Unauthorized (authentication required)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found (resource not found)
- `409` - Conflict (duplicate resource)
- `429` - Too Many Requests (rate limit exceeded)
- `500` - Internal Server Error

## Rate Limiting

### Rate Limits
- **Standard Users:** 100 requests per minute
- **System Integration:** 1000 requests per minute
- **Bulk Operations:** 10 requests per minute

### Rate Limit Headers
```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1640995200
```

## Integration Examples

### Complete Workflow Example
```javascript
// 1. Authenticate
const authResponse = await fetch('/api/v1/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'user@dsr.gov.ph',
    password: 'password'
  })
});
const { accessToken } = await authResponse.json();

// 2. Create household registration
const householdResponse = await fetch('/api/v1/registration/households', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(householdData)
});
const household = await householdResponse.json();

// 3. Conduct eligibility assessment
const assessmentResponse = await fetch('/api/v1/eligibility/assessments', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    householdId: household.id,
    programName: '4Ps',
    assessmentType: 'FULL'
  })
});
const assessment = await assessmentResponse.json();

// 4. Create payment if eligible
if (assessment.status === 'ELIGIBLE') {
  const paymentResponse = await fetch('/api/v1/payments', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      householdId: household.id,
      programName: '4Ps',
      amount: assessment.benefitAmount
    })
  });
  const payment = await paymentResponse.json();
}
```

---

**For additional support and integration assistance:**
- **Technical Support:** api-support@dsr.gov.ph
- **Documentation:** https://docs.dsr.gov.ph
- **Status Page:** https://status.dsr.gov.ph
- **Developer Portal:** https://developers.dsr.gov.ph
