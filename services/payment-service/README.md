# DSR Payment Service

The Payment Service is a critical component of the Philippine Dynamic Social Registry (DSR) system, responsible for managing payment processing, disbursement tracking, and Financial Service Provider (FSP) integrations.

## Overview

The Payment Service handles:
- Individual payment processing and tracking
- Batch payment operations for large-scale disbursements
- Integration with multiple Financial Service Providers (FSPs)
- Payment reconciliation and audit trails
- Real-time payment status monitoring
- Comprehensive reporting and analytics

## Features

### Core Payment Operations
- **Payment Creation**: Create individual payments with validation
- **Payment Processing**: Submit payments to FSPs for execution
- **Status Tracking**: Real-time payment status monitoring
- **Payment Cancellation**: Cancel pending payments with audit trail
- **Payment Retry**: Retry failed payments with configurable limits

### Batch Processing
- **Batch Creation**: Create payment batches for bulk processing
- **Batch Management**: Start, pause, resume, and cancel batch operations
- **Progress Monitoring**: Real-time batch processing progress tracking
- **Batch Statistics**: Comprehensive batch performance metrics
- **Batch Reporting**: Detailed batch execution reports

### FSP Integration
- **Multi-FSP Support**: Integration with multiple financial service providers
- **FSP Registry**: Dynamic FSP service registration and management
- **Health Monitoring**: Continuous FSP service health checks
- **Load Balancing**: Intelligent FSP selection based on availability and performance
- **Failover Support**: Automatic failover to backup FSPs

### Security & Compliance
- **Role-Based Access Control**: Granular permissions for different user roles
- **Data Encryption**: Sensitive data encryption at rest and in transit
- **Audit Logging**: Comprehensive audit trails for all operations
- **Compliance**: Philippine Data Privacy Act (R.A. 10173) compliant

## Architecture

### Service Components
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Payment        │    │  Payment Batch  │    │  FSP Service    │
│  Controller     │    │  Controller     │    │  Registry       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Payment        │    │  Payment Batch  │    │  FSP Service    │
│  Service        │    │  Service        │    │  Implementations│
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 ▼
                    ┌─────────────────┐
                    │  Data Layer     │
                    │  (PostgreSQL)   │
                    └─────────────────┘
```

### Database Schema
- **payments**: Individual payment records
- **payment_batches**: Batch processing records
- **payment_audit_logs**: Comprehensive audit trail
- **fsp_configurations**: FSP connection configurations

## API Documentation

### Authentication
All endpoints require JWT authentication. Include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

### Core Endpoints

#### Payment Operations
- `POST /api/v1/payments` - Create a new payment
- `GET /api/v1/payments/{paymentId}` - Get payment by ID
- `GET /api/v1/payments/household/{householdId}` - Get payments by household
- `POST /api/v1/payments/{paymentId}/process` - Process payment
- `PUT /api/v1/payments/{paymentId}/status` - Update payment status
- `POST /api/v1/payments/{paymentId}/cancel` - Cancel payment
- `POST /api/v1/payments/{paymentId}/retry` - Retry failed payment

#### Batch Operations
- `POST /api/v1/payment-batches` - Create payment batch
- `GET /api/v1/payment-batches/{batchId}` - Get batch by ID
- `POST /api/v1/payment-batches/{batchId}/start` - Start batch processing
- `GET /api/v1/payment-batches/{batchId}/progress` - Monitor batch progress
- `GET /api/v1/payment-batches/statistics` - Get batch statistics

#### FSP Management
- `GET /api/v1/fsp/services` - List available FSP services
- `GET /api/v1/fsp/health` - Check FSP health status
- `POST /api/v1/fsp/register` - Register new FSP service

### Request/Response Examples

#### Create Payment
```json
POST /api/v1/payments
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
  }
}
```

#### Response
```json
{
  "paymentId": "550e8400-e29b-41d4-a716-446655440000",
  "householdId": "HH-001",
  "programName": "4Ps",
  "amount": 1400.00,
  "status": "PENDING",
  "internalReferenceNumber": "PAY-2024-001",
  "createdDate": "2024-01-15T10:30:00Z"
}
```

## Configuration

### Environment Variables
```bash
# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/dsr_payment
DATABASE_USERNAME=payment_user
DATABASE_PASSWORD=secure_password

# Security Configuration
JWT_SECRET=your_jwt_secret_key
ENCRYPTION_KEY=your_encryption_key_32_chars

# FSP Configuration
FSP_TIMEOUT=30000
FSP_RETRY_ATTEMPTS=3
FSP_RETRY_DELAY=5000

# Batch Processing
BATCH_MAX_SIZE=1000
BATCH_PROCESSING_TIMEOUT=300000
```

### Application Profiles
- `local` - Local development with H2 database
- `test` - Testing environment with mock FSPs
- `staging` - Staging environment with limited FSPs
- `production` - Production environment with all FSPs

## Deployment

### Prerequisites
- Java 17 or higher
- PostgreSQL 13 or higher
- Redis (for caching)
- Kafka (for event streaming)

### Using Podman
```bash
# Build the service
podman build -t dsr-payment-service .

# Run with environment variables
podman run -d \
  --name payment-service \
  -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host:5432/dsr_payment \
  -e JWT_SECRET=your_secret \
  dsr-payment-service
```

### Using Docker Compose
```yaml
version: '3.8'
services:
  payment-service:
    image: dsr-payment-service:latest
    ports:
      - "8080:8080"
    environment:
      - DATABASE_URL=jdbc:postgresql://db:5432/dsr_payment
      - JWT_SECRET=${JWT_SECRET}
    depends_on:
      - db
      - redis
```

## Monitoring

### Health Checks
- `/actuator/health` - Service health status
- `/actuator/health/db` - Database connectivity
- `/actuator/health/fsp` - FSP service health

### Metrics
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics endpoint

### Key Metrics
- `payment.created.total` - Total payments created
- `payment.processed.total` - Total payments processed
- `payment.failed.total` - Total payment failures
- `batch.processing.duration` - Batch processing time
- `fsp.response.time` - FSP response times

## Security

### Role-Based Access Control
- **DSWD_STAFF**: Create, process, and manage payments
- **LGU_STAFF**: View payments for their jurisdiction
- **SYSTEM_ADMIN**: Full administrative access
- **BENEFICIARY**: View own payment status

### Data Protection
- Sensitive data encrypted using AES-256
- PII data masked in logs
- Audit trails for all operations
- Secure FSP communication with TLS 1.3

## Troubleshooting

### Common Issues

#### Payment Processing Failures
1. Check FSP service health: `GET /api/v1/fsp/health`
2. Verify payment validation: Check audit logs
3. Review FSP configuration: Validate credentials

#### Batch Processing Delays
1. Monitor batch progress: `GET /api/v1/payment-batches/{batchId}/progress`
2. Check system resources: CPU, memory usage
3. Review FSP performance: Response times

#### Database Connection Issues
1. Check database health: `/actuator/health/db`
2. Verify connection pool settings
3. Review database logs

### Support
- **Technical Issues**: Create an issue in the repository
- **Security Concerns**: security@dsr.gov.ph
- **API Questions**: api-support@dsr.gov.ph

## Contributing

Please read [CONTRIBUTING.md](../../CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](../../LICENSE) file for details.
