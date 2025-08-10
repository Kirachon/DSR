-- DSR Database Initial Schema Migration
-- Philippine Dynamic Social Registry (DSR) System
-- Version: 3.0.0
-- Flyway Migration: V1__Initial_Schema.sql

-- Create schemas for different services
CREATE SCHEMA IF NOT EXISTS dsr_core;
CREATE SCHEMA IF NOT EXISTS dsr_auth;
CREATE SCHEMA IF NOT EXISTS dsr_analytics;
CREATE SCHEMA IF NOT EXISTS dsr_audit;

-- Set search path
SET search_path TO dsr_core, dsr_auth, dsr_analytics, dsr_audit, public;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable pgcrypto for password hashing
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Enable full-text search
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Enable GIN indexes for better performance
CREATE EXTENSION IF NOT EXISTS btree_gin;

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
