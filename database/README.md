# DSR Database Setup Guide

This guide explains how to set up the PostgreSQL database for the DSR (Dynamic Social Registry) system.

## Prerequisites

1. **PostgreSQL 12+** installed and running
2. **PostgreSQL client tools** (psql) available
3. **Superuser access** to PostgreSQL (usually `postgres` user)

## Quick Setup Options

### Option 1: Automated Setup (Recommended)

If you have PostgreSQL client tools installed:

**Windows:**
```cmd
cd database
init-database.bat
```

**Linux/Mac:**
```bash
cd database
chmod +x init-database.sh
./init-database.sh
```

### Option 2: Manual Setup

If you don't have PostgreSQL client tools or prefer manual setup:

1. **Connect to PostgreSQL** as superuser:
   ```sql
   -- Using pgAdmin, DBeaver, or any PostgreSQL client
   -- Connect as user: postgres
   ```

2. **Run the setup script:**
   ```sql
   -- Copy and paste the contents of manual-setup.sql
   -- Or use: \i /path/to/manual-setup.sql
   ```

3. **Run the table creation scripts** in order:
   ```sql
   \c dsr_local dsr_user
   \i migrations/V1__Initial_Schema.sql
   \i migrations/V2__Auth_Tables.sql
   \i migrations/V3__Core_Tables.sql
   ```

### Option 3: Using Maven Flyway Plugin

If you have Maven installed:

```bash
# From the project root directory
mvn flyway:migrate -Dflyway.configFiles=database/flyway.conf
```

## Database Configuration

After setup, your database will have:

- **Database Name:** `dsr_local`
- **Username:** `dsr_user`
- **Password:** `dsr_local_password`
- **Host:** `localhost`
- **Port:** `5432`
- **JDBC URL:** `jdbc:postgresql://localhost:5432/dsr_local`

## Schemas Created

The setup creates four schemas:

1. **dsr_core** - Core business entities (households, members, registrations)
2. **dsr_auth** - Authentication and authorization (users, sessions, permissions)
3. **dsr_analytics** - Analytics and reporting data
4. **dsr_audit** - Audit trail and logging

## Tables Created

### Authentication Schema (dsr_auth)
- `users` - System users and authentication
- `user_sessions` - JWT token management
- `user_permissions` - User-specific permissions
- `password_reset_tokens` - Password recovery
- `email_verification_tokens` - Email verification
- `security_events` - Security audit log

### Core Schema (dsr_core)
- `households` - Household information
- `household_members` - Individual household members
- `household_addresses` - Address information
- `contact_information` - Contact details
- `registrations` - Registration applications
- `verification_info` - Verification details
- `documents` - Document uploads
- `life_events` - Life event tracking

## Updating Service Configuration

After database setup, update your service configurations:

1. **Remove no-db profile** from startup commands:
   ```bash
   # Change from:
   java -jar target/registration-service-3.0.0.jar --spring.profiles.active=no-db
   
   # To:
   java -jar target/registration-service-3.0.0.jar --spring.profiles.active=local
   ```

2. **Verify application-local.yml** has correct database settings:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/dsr_local
       username: dsr_user
       password: dsr_local_password
   ```

## Verification

To verify the setup:

1. **Check database connection:**
   ```sql
   SELECT version();
   ```

2. **List schemas:**
   ```sql
   SELECT schema_name FROM information_schema.schemata 
   WHERE schema_name LIKE 'dsr_%';
   ```

3. **Count tables:**
   ```sql
   SELECT schemaname, COUNT(*) as table_count 
   FROM pg_tables 
   WHERE schemaname LIKE 'dsr_%' 
   GROUP BY schemaname;
   ```

## Troubleshooting

### Common Issues

1. **PostgreSQL not running:**
   ```bash
   # Check if PostgreSQL is running
   pg_isready -h localhost -p 5432
   ```

2. **Permission denied:**
   - Ensure you're running as PostgreSQL superuser
   - Check pg_hba.conf for authentication settings

3. **Database already exists:**
   - The scripts handle existing databases gracefully
   - Drop and recreate if needed: `DROP DATABASE dsr_local;`

4. **Connection refused:**
   - Check PostgreSQL is listening on localhost:5432
   - Verify firewall settings

### Reset Database

To completely reset the database:

```sql
-- Connect as postgres superuser
DROP DATABASE IF EXISTS dsr_local;
DROP USER IF EXISTS dsr_user;

-- Then re-run the setup scripts
```

## Next Steps

After successful database setup:

1. **Start services** without the `no-db` profile
2. **Test API endpoints** to verify database connectivity
3. **Check logs** for any database-related errors
4. **Run integration tests** to validate functionality

## Support

For issues with database setup:

1. Check PostgreSQL logs for detailed error messages
2. Verify all prerequisites are met
3. Ensure proper permissions and network connectivity
4. Consult PostgreSQL documentation for specific errors
