# Payment Service API Documentation

## Overview

The Payment Service API provides comprehensive payment processing capabilities for the Philippine Dynamic Social Registry (DSR) system. This RESTful API supports individual payments, batch processing, and Financial Service Provider (FSP) integrations.

## Base URL
- **Development**: `http://localhost:8080/api/v1`
- **Staging**: `https://staging-api.dsr.gov.ph/api/v1`
- **Production**: `https://api.dsr.gov.ph/api/v1`

## Authentication

All API endpoints require JWT authentication. Include the token in the Authorization header:

```http
Authorization: Bearer <your-jwt-token>
```

### Getting an Access Token

```http
POST /auth/login
Content-Type: application/json

{
  "username": "your_username",
  "password": "your_password"
}
```

## Payment Operations

### Create Payment

Creates a new payment record in the system.

```http
POST /payments
Content-Type: application/json
Authorization: Bearer <token>

{
  "householdId": "HH-001",
  "programName": "4Ps",
  "amount": 1400.00,
  "currency": "PHP",
  "paymentMethod": "BANK_TRANSFER",
  "beneficiaryAccount": {
    "accountNumber": "1234567890",
    "bankCode": "LBP",
    "accountName": "Juan Dela Cruz"
  },
  "description": "Monthly 4Ps payment",
  "metadata": {
    "region": "NCR",
    "municipality": "Quezon City"
  }
}
```

**Response (201 Created):**
```json
{
  "paymentId": "550e8400-e29b-41d4-a716-446655440000",
  "householdId": "HH-001",
  "programName": "4Ps",
  "amount": 1400.00,
  "currency": "PHP",
  "paymentMethod": "BANK_TRANSFER",
  "status": "PENDING",
  "internalReferenceNumber": "PAY-2024-001",
  "recipientAccountNumber": "1234567890",
  "recipientAccountName": "Juan Dela Cruz",
  "createdBy": "user123",
  "createdDate": "2024-01-15T10:30:00Z"
}
```

### Get Payment by ID

Retrieves a specific payment by its ID.

```http
GET /payments/{paymentId}
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "paymentId": "550e8400-e29b-41d4-a716-446655440000",
  "householdId": "HH-001",
  "programName": "4Ps",
  "amount": 1400.00,
  "status": "COMPLETED",
  "fspCode": "LBP",
  "fspReferenceNumber": "LBP-REF-123456",
  "completedDate": "2024-01-15T11:45:00Z"
}
```

### Get Payments by Household

Retrieves all payments for a specific household with pagination.

```http
GET /payments/household/{householdId}?page=0&size=20&sort=createdDate,desc
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "paymentId": "550e8400-e29b-41d4-a716-446655440000",
      "amount": 1400.00,
      "status": "COMPLETED",
      "createdDate": "2024-01-15T10:30:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### Process Payment

Submits a payment to the FSP for processing.

```http
POST /payments/{paymentId}/process?processedBy=user123
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "paymentId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PROCESSING",
  "fspCode": "LBP",
  "fspReferenceNumber": "LBP-REF-123456",
  "processedDate": "2024-01-15T11:00:00Z"
}
```

### Update Payment Status

Updates the status of a payment with audit trail.

```http
PUT /payments/{paymentId}/status
Content-Type: application/x-www-form-urlencoded
Authorization: Bearer <token>

status=COMPLETED&reason=Payment completed successfully&updatedBy=user123
```

### Cancel Payment

Cancels a pending payment.

```http
POST /payments/{paymentId}/cancel
Content-Type: application/x-www-form-urlencoded
Authorization: Bearer <token>

reason=User requested cancellation&cancelledBy=user123
```

### Retry Payment

Retries a failed payment.

```http
POST /payments/{paymentId}/retry?retriedBy=user123
Authorization: Bearer <token>
```

## Batch Operations

### Create Payment Batch

Creates a new payment batch for bulk processing.

```http
POST /payment-batches
Content-Type: application/json
Authorization: Bearer <token>

{
  "programId": "550e8400-e29b-41d4-a716-446655440001",
  "programName": "4Ps",
  "paymentRequests": [
    {
      "householdId": "HH-001",
      "amount": 1400.00,
      "paymentMethod": "BANK_TRANSFER",
      "beneficiaryAccount": {
        "accountNumber": "1234567890",
        "bankCode": "LBP",
        "accountName": "Juan Dela Cruz"
      }
    }
  ],
  "scheduledDate": "2024-01-16T09:00:00Z",
  "metadata": {
    "region": "NCR",
    "batchType": "MONTHLY"
  }
}
```

**Response (201 Created):**
```json
{
  "batchId": "550e8400-e29b-41d4-a716-446655440002",
  "batchNumber": "BATCH-2024-001",
  "programId": "550e8400-e29b-41d4-a716-446655440001",
  "programName": "4Ps",
  "totalPayments": 1,
  "totalAmount": 1400.00,
  "status": "PENDING",
  "createdDate": "2024-01-15T10:30:00Z"
}
```

### Start Batch Processing

Initiates processing of a payment batch.

```http
POST /payment-batches/{batchId}/start?startedBy=user123
Authorization: Bearer <token>
```

### Monitor Batch Progress

Retrieves real-time progress of batch processing.

```http
GET /payment-batches/{batchId}/progress
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "batchId": "550e8400-e29b-41d4-a716-446655440002",
  "status": "PROCESSING",
  "totalPayments": 100,
  "processedPayments": 75,
  "successfulPayments": 70,
  "failedPayments": 5,
  "progressPercentage": 75.0,
  "estimatedCompletionTime": "2024-01-15T12:30:00Z"
}
```

### Get Batch Statistics

Retrieves comprehensive batch statistics.

```http
GET /payment-batches/statistics
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "PENDING": {
    "count": 5,
    "totalAmount": 7000.00
  },
  "PROCESSING": {
    "count": 2,
    "totalAmount": 2800.00
  },
  "COMPLETED": {
    "count": 10,
    "totalAmount": 14000.00
  }
}
```

## FSP Management

### List FSP Services

Retrieves all available FSP services.

```http
GET /fsp/services
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
[
  {
    "fspCode": "LBP",
    "fspName": "Land Bank of the Philippines",
    "supportedMethods": ["BANK_TRANSFER", "CHECK"],
    "isHealthy": true,
    "lastHealthCheck": "2024-01-15T11:00:00Z"
  }
]
```

### Check FSP Health

Retrieves health status of all FSP services.

```http
GET /fsp/health
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "LBP": true,
  "BPI": true,
  "BDO": false
}
```

## Search and Filtering

### Search Payments

Search payments with multiple criteria.

```http
GET /payments/search?householdId=HH-001&status=COMPLETED&startDate=2024-01-01T00:00:00Z&endDate=2024-01-31T23:59:59Z&page=0&size=20
Authorization: Bearer <token>
```

### Search Batches

Search payment batches with criteria.

```http
GET /payment-batches/search?programId=550e8400-e29b-41d4-a716-446655440001&status=COMPLETED&page=0&size=20
Authorization: Bearer <token>
```

## Statistics and Reporting

### Payment Statistics

Get comprehensive payment statistics.

```http
GET /payments/statistics
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "PENDING": {
    "count": 150,
    "totalAmount": 210000.00
  },
  "COMPLETED": {
    "count": 1000,
    "totalAmount": 1400000.00
  },
  "FAILED": {
    "count": 25,
    "totalAmount": 35000.00
  }
}
```

### Daily Payment Volume

Get daily payment volume for a date range.

```http
GET /payments/daily-volume?startDate=2024-01-01T00:00:00Z&endDate=2024-01-31T23:59:59Z
Authorization: Bearer <token>
```

## Error Handling

### Error Response Format

All error responses follow this format:

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid payment amount",
  "path": "/api/v1/payments"
}
```

### Common Error Codes

- **400 Bad Request**: Invalid request data
- **401 Unauthorized**: Missing or invalid authentication
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **409 Conflict**: Resource conflict (e.g., duplicate payment)
- **422 Unprocessable Entity**: Validation errors
- **500 Internal Server Error**: Server error

## Rate Limiting

API requests are rate-limited to prevent abuse:

- **Standard Users**: 100 requests per minute
- **System Administrators**: 1000 requests per minute
- **Batch Operations**: 10 requests per minute

Rate limit headers are included in responses:
```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1642248600
```

## Webhooks

The Payment Service supports webhooks for real-time notifications:

### Payment Status Updates

```json
{
  "eventType": "PAYMENT_STATUS_CHANGED",
  "paymentId": "550e8400-e29b-41d4-a716-446655440000",
  "oldStatus": "PROCESSING",
  "newStatus": "COMPLETED",
  "timestamp": "2024-01-15T11:45:00Z"
}
```

### Batch Completion

```json
{
  "eventType": "BATCH_COMPLETED",
  "batchId": "550e8400-e29b-41d4-a716-446655440002",
  "totalPayments": 100,
  "successfulPayments": 95,
  "failedPayments": 5,
  "timestamp": "2024-01-15T12:30:00Z"
}
```

## SDK and Client Libraries

Official client libraries are available for:
- **Java**: `dsr-payment-client-java`
- **JavaScript/Node.js**: `@dsr/payment-client`
- **Python**: `dsr-payment-client`
- **PHP**: `dsr/payment-client`

## Support

- **API Documentation**: https://docs.dsr.gov.ph/payment-service
- **Swagger UI**: https://api.dsr.gov.ph/swagger-ui
- **Technical Support**: api-support@dsr.gov.ph
- **Issues**: Create an issue in the repository
