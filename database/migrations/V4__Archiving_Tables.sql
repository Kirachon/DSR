-- DSR Archiving Tables Migration
-- Philippine Dynamic Social Registry (DSR) System
-- Version: 3.0.0
-- Flyway Migration: V4__Archiving_Tables.sql

-- Set search path
SET search_path TO dsr_core, dsr_auth, public;

-- Create archived_data table for storing archived records
CREATE TABLE dsr_core.archived_data (
    archive_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    original_entity_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    archived_data TEXT,
    archive_reason VARCHAR(500),
    archived_by VARCHAR(100),
    original_created_at TIMESTAMP WITH TIME ZONE,
    original_updated_at TIMESTAMP WITH TIME ZONE,
    archived_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    retention_until TIMESTAMP WITH TIME ZONE,
    is_encrypted BOOLEAN NOT NULL DEFAULT FALSE,
    encryption_key_id VARCHAR(255),
    checksum VARCHAR(255),
    file_size_bytes BIGINT,
    compression_type VARCHAR(20),
    storage_location VARCHAR(500),
    archive_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    restored_at TIMESTAMP WITH TIME ZONE,
    restored_by VARCHAR(100),
    restore_reason VARCHAR(500)
);

-- Create retention_policies table for managing data retention rules
CREATE TABLE dsr_core.retention_policies (
    policy_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_type VARCHAR(50) NOT NULL UNIQUE,
    retention_days INTEGER NOT NULL,
    auto_archive_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    auto_delete_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    archive_after_days INTEGER,
    delete_after_days INTEGER,
    policy_description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    effective_from TIMESTAMP WITH TIME ZONE,
    effective_until TIMESTAMP WITH TIME ZONE
);

-- Create indexes for performance optimization
CREATE INDEX idx_archived_data_original_entity ON dsr_core.archived_data(original_entity_id, entity_type);
CREATE INDEX idx_archived_data_entity_type ON dsr_core.archived_data(entity_type);
CREATE INDEX idx_archived_data_archived_at ON dsr_core.archived_data(archived_at);
CREATE INDEX idx_archived_data_status ON dsr_core.archived_data(archive_status);
CREATE INDEX idx_archived_data_retention_until ON dsr_core.archived_data(retention_until);
CREATE INDEX idx_archived_data_archived_by ON dsr_core.archived_data(archived_by);
CREATE INDEX idx_archived_data_file_size ON dsr_core.archived_data(file_size_bytes);

CREATE INDEX idx_retention_policies_entity_type ON dsr_core.retention_policies(entity_type);
CREATE INDEX idx_retention_policies_active ON dsr_core.retention_policies(is_active);
CREATE INDEX idx_retention_policies_auto_archive ON dsr_core.retention_policies(auto_archive_enabled);
CREATE INDEX idx_retention_policies_effective_dates ON dsr_core.retention_policies(effective_from, effective_until);

-- Add constraints
ALTER TABLE dsr_core.archived_data 
ADD CONSTRAINT chk_archive_status 
CHECK (archive_status IN ('ACTIVE', 'RESTORED', 'EXPIRED', 'DELETED'));

ALTER TABLE dsr_core.archived_data 
ADD CONSTRAINT chk_file_size_positive 
CHECK (file_size_bytes IS NULL OR file_size_bytes >= 0);

ALTER TABLE dsr_core.retention_policies 
ADD CONSTRAINT chk_retention_days_positive 
CHECK (retention_days > 0);

ALTER TABLE dsr_core.retention_policies 
ADD CONSTRAINT chk_archive_days_positive 
CHECK (archive_after_days IS NULL OR archive_after_days > 0);

ALTER TABLE dsr_core.retention_policies 
ADD CONSTRAINT chk_delete_days_positive 
CHECK (delete_after_days IS NULL OR delete_after_days > 0);

-- Insert default retention policies for core entity types
INSERT INTO dsr_core.retention_policies (
    entity_type, 
    retention_days, 
    auto_archive_enabled, 
    auto_delete_enabled,
    archive_after_days,
    policy_description,
    created_by
) VALUES 
('HOUSEHOLD', 2555, true, false, 1825, 'Default retention policy for household records - 7 years retention, archive after 5 years', 'SYSTEM'),
('HOUSEHOLD_MEMBER', 2555, true, false, 1825, 'Default retention policy for household member records - 7 years retention, archive after 5 years', 'SYSTEM'),
('REGISTRATION', 3650, true, false, 2555, 'Default retention policy for registration records - 10 years retention, archive after 7 years', 'SYSTEM'),
('DOCUMENT', 3650, false, false, 2555, 'Default retention policy for document records - 10 years retention, manual archive after 7 years', 'SYSTEM'),
('LIFE_EVENT', 2555, true, false, 1825, 'Default retention policy for life event records - 7 years retention, archive after 5 years', 'SYSTEM');

-- Add comments for documentation
COMMENT ON TABLE dsr_core.archived_data IS 'Stores archived data records with metadata for restoration and compliance';
COMMENT ON TABLE dsr_core.retention_policies IS 'Defines data retention policies for different entity types';

COMMENT ON COLUMN dsr_core.archived_data.archive_id IS 'Unique identifier for the archived record';
COMMENT ON COLUMN dsr_core.archived_data.original_entity_id IS 'Original ID of the archived entity';
COMMENT ON COLUMN dsr_core.archived_data.entity_type IS 'Type of entity that was archived (HOUSEHOLD, HOUSEHOLD_MEMBER, etc.)';
COMMENT ON COLUMN dsr_core.archived_data.archived_data IS 'JSON representation of the original entity data';
COMMENT ON COLUMN dsr_core.archived_data.archive_reason IS 'Reason for archiving the data';
COMMENT ON COLUMN dsr_core.archived_data.retention_until IS 'Date until which the archived data should be retained';
COMMENT ON COLUMN dsr_core.archived_data.archive_status IS 'Current status of the archived data (ACTIVE, RESTORED, EXPIRED, DELETED)';
COMMENT ON COLUMN dsr_core.archived_data.checksum IS 'Checksum for data integrity verification';
COMMENT ON COLUMN dsr_core.archived_data.file_size_bytes IS 'Size of the archived data in bytes';

COMMENT ON COLUMN dsr_core.retention_policies.entity_type IS 'Type of entity this policy applies to';
COMMENT ON COLUMN dsr_core.retention_policies.retention_days IS 'Number of days to retain data before deletion';
COMMENT ON COLUMN dsr_core.retention_policies.auto_archive_enabled IS 'Whether automatic archiving is enabled';
COMMENT ON COLUMN dsr_core.retention_policies.archive_after_days IS 'Number of days after which data should be archived';
COMMENT ON COLUMN dsr_core.retention_policies.delete_after_days IS 'Number of days after which data should be deleted';
