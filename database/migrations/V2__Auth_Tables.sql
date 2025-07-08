-- DSR Authentication Tables Migration
-- Philippine Dynamic Social Registry (DSR) System
-- Version: 3.0.0
-- Flyway Migration: V2__Auth_Tables.sql

-- Set search path
SET search_path TO dsr_auth, dsr_core, public;

-- Users table for authentication
CREATE TABLE dsr_auth.users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    role dsr_auth.user_role NOT NULL DEFAULT 'CITIZEN',
    status dsr_auth.user_status NOT NULL DEFAULT 'PENDING_VERIFICATION',
    email_verified BOOLEAN DEFAULT FALSE,
    phone_number VARCHAR(20),
    phone_verified BOOLEAN DEFAULT FALSE,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(32),
    preferred_language VARCHAR(10) DEFAULT 'en',
    timezone VARCHAR(50) DEFAULT 'Asia/Manila',
    last_login_at TIMESTAMP WITH TIME ZONE,
    last_login_ip INET,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    password_changed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

-- User sessions table for JWT token management
CREATE TABLE dsr_auth.user_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES dsr_auth.users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    refresh_token_hash VARCHAR(255),
    device_info JSONB,
    ip_address INET,
    user_agent TEXT,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    refresh_expires_at TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- User permissions table
CREATE TABLE dsr_auth.user_permissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES dsr_auth.users(id) ON DELETE CASCADE,
    permission VARCHAR(100) NOT NULL,
    resource VARCHAR(100),
    granted_by UUID REFERENCES dsr_auth.users(id),
    granted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE
);

-- Password reset tokens
CREATE TABLE dsr_auth.password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES dsr_auth.users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Email verification tokens
CREATE TABLE dsr_auth.email_verification_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES dsr_auth.users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Security events log
CREATE TABLE dsr_auth.security_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES dsr_auth.users(id) ON DELETE SET NULL,
    event_type VARCHAR(50) NOT NULL,
    event_description TEXT,
    ip_address INET,
    user_agent TEXT,
    additional_data JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_users_email ON dsr_auth.users(email);
CREATE INDEX idx_users_role ON dsr_auth.users(role);
CREATE INDEX idx_users_status ON dsr_auth.users(status);
CREATE INDEX idx_users_created_at ON dsr_auth.users(created_at);

CREATE INDEX idx_user_sessions_user_id ON dsr_auth.user_sessions(user_id);
CREATE INDEX idx_user_sessions_token_hash ON dsr_auth.user_sessions(token_hash);
CREATE INDEX idx_user_sessions_expires_at ON dsr_auth.user_sessions(expires_at);
CREATE INDEX idx_user_sessions_is_active ON dsr_auth.user_sessions(is_active);

CREATE INDEX idx_user_permissions_user_id ON dsr_auth.user_permissions(user_id);
CREATE INDEX idx_user_permissions_permission ON dsr_auth.user_permissions(permission);
CREATE INDEX idx_user_permissions_is_active ON dsr_auth.user_permissions(is_active);

CREATE INDEX idx_password_reset_tokens_user_id ON dsr_auth.password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_tokens_expires_at ON dsr_auth.password_reset_tokens(expires_at);

CREATE INDEX idx_email_verification_tokens_user_id ON dsr_auth.email_verification_tokens(user_id);
CREATE INDEX idx_email_verification_tokens_expires_at ON dsr_auth.email_verification_tokens(expires_at);

CREATE INDEX idx_security_events_user_id ON dsr_auth.security_events(user_id);
CREATE INDEX idx_security_events_event_type ON dsr_auth.security_events(event_type);
CREATE INDEX idx_security_events_created_at ON dsr_auth.security_events(created_at);

-- Add comments
COMMENT ON TABLE dsr_auth.users IS 'System users with authentication credentials';
COMMENT ON TABLE dsr_auth.user_sessions IS 'Active user sessions and JWT tokens';
COMMENT ON TABLE dsr_auth.user_permissions IS 'User-specific permissions and access rights';
COMMENT ON TABLE dsr_auth.password_reset_tokens IS 'Password reset tokens for user account recovery';
COMMENT ON TABLE dsr_auth.email_verification_tokens IS 'Email verification tokens for account activation';
COMMENT ON TABLE dsr_auth.security_events IS 'Security-related events and audit trail';
