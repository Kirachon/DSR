-- DSR Local Database Setup Script
-- Philippine Dynamic Social Registry (DSR) System
-- Version: 3.0.0
-- Purpose: Create database, user, and initial setup for local development

-- Connect as postgres superuser to create database and user
-- This script should be run as: psql -U postgres -f setup_local_database.sql

-- Create the DSR local database
CREATE DATABASE dsr_local
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'English_Philippines.1252'
    LC_CTYPE = 'English_Philippines.1252'
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

COMMENT ON DATABASE dsr_local IS 'DSR Local Development Database';

-- Display success message
\echo 'DSR Local Database Setup Complete!'
\echo 'Database: dsr_local'
\echo 'User: dsr_user'
\echo 'Password: dsr_local_password'
\echo 'Next: Run schema creation scripts'
