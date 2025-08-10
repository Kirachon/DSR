-- DSR Database Manual Setup Script
-- Philippine Dynamic Social Registry (DSR) System
-- Version: 3.0.0
-- 
-- INSTRUCTIONS:
-- 1. Connect to PostgreSQL as superuser (postgres)
-- 2. Run this script to create database, user, and initial schema
-- 3. Update your application.yml files to use database mode

-- Create the DSR local database
CREATE DATABASE dsr_local
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    TEMPLATE = template0;

-- Create the main DSR user
CREATE USER dsr_user WITH
    LOGIN
    NOSUPERUSER
    CREATEDB
    NOCREATEROLE
    INHERIT
    NOREPLICATION
    CONNECTION LIMIT -1
    PASSWORD 'dsr_local_password';

-- Grant privileges to dsr_user on the database
GRANT ALL PRIVILEGES ON DATABASE dsr_local TO dsr_user;

-- Connect to the new database
\c dsr_local;

-- Grant schema creation privileges to dsr_user
GRANT CREATE ON DATABASE dsr_local TO dsr_user;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS btree_gin;

-- Grant usage on extensions
GRANT USAGE ON SCHEMA public TO dsr_user;

-- Create schemas for different services
CREATE SCHEMA IF NOT EXISTS dsr_core;
CREATE SCHEMA IF NOT EXISTS dsr_auth;
CREATE SCHEMA IF NOT EXISTS dsr_analytics;
CREATE SCHEMA IF NOT EXISTS dsr_audit;

-- Grant schema privileges to dsr_user
GRANT ALL PRIVILEGES ON SCHEMA dsr_core TO dsr_user;
GRANT ALL PRIVILEGES ON SCHEMA dsr_auth TO dsr_user;
GRANT ALL PRIVILEGES ON SCHEMA dsr_analytics TO dsr_user;
GRANT ALL PRIVILEGES ON SCHEMA dsr_audit TO dsr_user;

-- Set search path
SET search_path TO dsr_core, dsr_auth, dsr_analytics, dsr_audit, public;

-- Create custom types
CREATE TYPE dsr_core.registration_status AS ENUM (
    'DRAFT',
    'PENDING_VERIFICATION',
    'PENDING_APPROVAL',
    'APPROVED',
    'REJECTED',
    'CANCELLED',
    'EXPIRED'
);

CREATE TYPE dsr_core.registration_channel AS ENUM (
    'WEB_PORTAL',
    'MOBILE_APP',
    'FIELD_REGISTRATION',
    'CALL_CENTER',
    'WALK_IN'
);

CREATE TYPE dsr_core.verification_status AS ENUM (
    'PENDING',
    'VERIFIED',
    'FAILED',
    'EXPIRED'
);

CREATE TYPE dsr_core.life_event_type AS ENUM (
    'BIRTH',
    'DEATH',
    'MARRIAGE',
    'SEPARATION',
    'EMPLOYMENT_CHANGE',
    'INCOME_CHANGE',
    'ADDRESS_CHANGE',
    'EDUCATION_CHANGE',
    'HEALTH_CHANGE',
    'OTHER'
);

CREATE TYPE dsr_core.gender AS ENUM (
    'MALE',
    'FEMALE',
    'OTHER',
    'PREFER_NOT_TO_SAY'
);

CREATE TYPE dsr_core.civil_status AS ENUM (
    'SINGLE',
    'MARRIED',
    'WIDOWED',
    'SEPARATED',
    'DIVORCED',
    'LIVE_IN'
);

CREATE TYPE dsr_core.relationship_type AS ENUM (
    'HEAD',
    'SPOUSE',
    'CHILD',
    'PARENT',
    'SIBLING',
    'GRANDPARENT',
    'GRANDCHILD',
    'OTHER_RELATIVE',
    'NON_RELATIVE'
);

CREATE TYPE dsr_core.employment_status AS ENUM (
    'EMPLOYED',
    'UNEMPLOYED',
    'SELF_EMPLOYED',
    'RETIRED',
    'STUDENT',
    'HOMEMAKER',
    'DISABLED',
    'OTHER'
);

CREATE TYPE dsr_core.education_level AS ENUM (
    'NO_FORMAL_EDUCATION',
    'ELEMENTARY_UNDERGRADUATE',
    'ELEMENTARY_GRADUATE',
    'HIGH_SCHOOL_UNDERGRADUATE',
    'HIGH_SCHOOL_GRADUATE',
    'VOCATIONAL',
    'COLLEGE_UNDERGRADUATE',
    'COLLEGE_GRADUATE',
    'POST_GRADUATE'
);

CREATE TYPE dsr_auth.user_role AS ENUM (
    'CITIZEN',
    'LGU_STAFF',
    'DSWD_STAFF',
    'SYSTEM_ADMIN',
    'FIELD_WORKER',
    'CALL_CENTER_AGENT'
);

CREATE TYPE dsr_auth.user_status AS ENUM (
    'ACTIVE',
    'INACTIVE',
    'SUSPENDED',
    'PENDING_VERIFICATION'
);

-- Comment on schemas
COMMENT ON SCHEMA dsr_core IS 'Core DSR entities and business data';
COMMENT ON SCHEMA dsr_auth IS 'Authentication and authorization data';
COMMENT ON SCHEMA dsr_analytics IS 'Analytics and reporting data';
COMMENT ON SCHEMA dsr_audit IS 'Audit trail and logging data';

COMMENT ON DATABASE dsr_local IS 'DSR Local Development Database';

-- Display success message
SELECT 'DSR Database Setup Complete!' as message;
SELECT 'Database: dsr_local' as info;
SELECT 'User: dsr_user' as info;
SELECT 'Password: dsr_local_password' as info;
SELECT 'Next: Run the table creation scripts' as next_step;
