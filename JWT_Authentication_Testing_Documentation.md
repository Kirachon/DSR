# JWT Authentication Testing Documentation
## DSR Registration Service - Phase 2 Implementation

### Overview
This document provides comprehensive testing results and examples for the JWT authentication system implemented in the DSR Registration Service. All tests were conducted in no-database mode with real JWT token generation and validation.

### Service Information
- **Service**: DSR Registration Service
- **Base URL**: http://localhost:8080
- **Profile**: no-db (with real JWT authentication)
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html

### Demo Accounts
- **Admin**: admin@dsr.gov.ph / admin123 (SYSTEM_ADMIN)
- **Citizen**: citizen@dsr.gov.ph / citizen123 (CITIZEN)

## Authentication Endpoints

### 1. User Login
**Endpoint**: `POST /api/v1/auth/login`

**Request Example**:
```json
{
  "email": "admin@dsr.gov.ph",
  "password": "admin123"
}
```

**Response Example**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": "46d96906-6c3b-47fb-9e12-202647d2fb4e",
    "email": "admin@dsr.gov.ph",
    "firstName": "System",
    "lastName": "Administrator",
    "role": "SYSTEM_ADMIN",
    "status": "ACTIVE",
    "emailVerified": true,
    "fullName": "System Administrator"
  }
}
```

### 2. User Registration
**Endpoint**: `POST /api/v1/auth/register`

**Request Example**:
```json
{
  "email": "test.citizen@dsr.gov.ph",
  "password": "TestPassword123!",
  "firstName": "Test",
  "lastName": "Citizen",
  "middleName": "Demo",
  "phoneNumber": "+639123456789",
  "role": "CITIZEN"
}
```

**Response**: Same format as login response with JWT tokens automatically generated.

### 3. Token Refresh
**Endpoint**: `POST /api/v1/auth/refresh`

**Request Example**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response**: New access and refresh tokens with updated expiration times.

## Protected Endpoints Testing

### Profile Access (All Authenticated Users)
**Endpoint**: `GET /api/v1/auth/profile`
**Authorization**: Bearer token required

**PowerShell Test Command**:
```powershell
Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/auth/profile' -Method GET -Headers @{Authorization='Bearer YOUR_ACCESS_TOKEN'}
```

### Registration Management (LGU_STAFF, DSWD_STAFF, SYSTEM_ADMIN only)
**Endpoint**: `GET /api/v1/registrations`
**Authorization**: Bearer token with appropriate role required

**Test Results**:
- ✅ CITIZEN users: 403 Forbidden (correctly denied)
- ✅ LGU_STAFF users: 200 OK (access granted)
- ✅ SYSTEM_ADMIN users: 200 OK (full access)

## Swagger UI JWT Integration

### Setup Instructions
1. Navigate to http://localhost:8080/swagger-ui/index.html
2. Click the "Authorize" button (lock icon)
3. Enter: `Bearer YOUR_ACCESS_TOKEN`
4. Click "Authorize" to apply the token
5. All subsequent API requests will include the JWT token

### Features Verified
- ✅ JWT Bearer authentication scheme configured
- ✅ Authorization button functional
- ✅ Token input accepts JWT tokens
- ✅ Authenticated requests include proper Authorization headers
- ✅ API documentation includes JWT usage instructions

## Security Validation Results

### JWT Token Security
- **Algorithm**: HMAC256 (secure signing algorithm)
- **Issuer**: dsr-registration-service
- **Audience**: dsr-users
- **Access Token Expiration**: 24 hours (86400 seconds)
- **Refresh Token Expiration**: 7 days (604800 seconds)
- **Unique JWT ID**: Each token has UUID-based JTI for tracking

### Password Security
- **Hashing Algorithm**: BCrypt (industry standard)
- **Salt Rounds**: Default BCrypt configuration
- **Password Validation**: Enforced during registration

### Security Headers
- **X-Frame-Options**: DENY
- **X-Content-Type-Options**: nosniff
- **Strict-Transport-Security**: max-age=31536000; includeSubDomains; preload
- **Session Management**: Stateless (JWT-based)

### CORS Configuration
- **Allowed Origins**: localhost:3000, localhost:3001, *.dsr.gov.ph, dsr.gov.ph
- **Allowed Methods**: GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD
- **Allowed Headers**: Authorization, Content-Type, Accept, Origin, etc.
- **Credentials**: Allowed for JWT authentication

## Role-Based Access Control

### User Roles and Permissions
| Role | Profile Access | Registration Management | Admin Functions |
|------|---------------|------------------------|-----------------|
| CITIZEN | ✅ | ❌ | ❌ |
| LGU_STAFF | ✅ | ✅ | ❌ |
| DSWD_STAFF | ✅ | ✅ | ❌ |
| SYSTEM_ADMIN | ✅ | ✅ | ✅ |

### Testing Commands

**Test with CITIZEN token**:
```powershell
# Should succeed (200 OK)
Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/auth/profile' -Method GET -Headers @{Authorization='Bearer CITIZEN_TOKEN'}

# Should fail (403 Forbidden)
Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/registrations' -Method GET -Headers @{Authorization='Bearer CITIZEN_TOKEN'}
```

**Test with LGU_STAFF token**:
```powershell
# Should succeed (200 OK)
Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/registrations' -Method GET -Headers @{Authorization='Bearer LGU_STAFF_TOKEN'}
```

## Error Handling

### Common Error Responses
- **401 Unauthorized**: Missing or invalid JWT token
- **403 Forbidden**: Valid token but insufficient permissions
- **400 Bad Request**: Invalid request format or validation errors

### Token Validation Errors
- Invalid token format: 403 Forbidden
- Expired token: 401 Unauthorized
- Wrong token type (refresh used as access): 403 Forbidden

## Production Deployment Checklist

### Security Requirements
- [ ] Change JWT secret from default value
- [ ] Configure proper CORS origins for production domains
- [ ] Enable HTTPS in production
- [ ] Set up proper logging and monitoring
- [ ] Configure rate limiting for authentication endpoints
- [ ] Set up token blacklisting for logout functionality
- [ ] Enable audit logging for security events

### Environment Variables
```bash
JWT_SECRET=your_production_secret_key_here
JWT_EXPIRATION=86400
JWT_REFRESH_EXPIRATION=604800
```

## Troubleshooting Guide

### Common Issues

**Issue**: Swagger UI returns 401 Unauthorized
**Solution**: Ensure JWT token is properly formatted with "Bearer " prefix

**Issue**: Token refresh fails with 403 Forbidden
**Solution**: Verify refresh token is valid and not expired

**Issue**: Role-based access not working
**Solution**: Check JWT token contains correct role claim and user has appropriate permissions

**Issue**: CORS errors in browser
**Solution**: Verify frontend origin is included in CORS configuration

### Debug Commands
```powershell
# Test service health
Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health'

# Check OpenAPI specification
Invoke-RestMethod -Uri 'http://localhost:8080/v3/api-docs'

# Test without authentication (should fail)
Invoke-WebRequest -Uri 'http://localhost:8080/api/v1/auth/profile'
```

## Next Steps

### Phase 3: Next.js Frontend Integration
- Configure frontend authentication context
- Implement JWT token storage and management
- Create login/logout components
- Set up protected routes
- Integrate with backend API endpoints

### Additional Security Enhancements
- Implement token blacklisting for logout
- Add rate limiting for authentication endpoints
- Set up audit logging for security events
- Configure session management for concurrent logins
- Implement two-factor authentication (2FA)
