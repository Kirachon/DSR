-- Create demo users for DSR E2E testing
-- These users are for development and testing purposes only

-- Insert demo users with BCrypt hashed passwords
-- Password for all users: admin123, staff123, citizen123 respectively
-- BCrypt hash for 'admin123': $2b$12$4.Mtbo5TDEW3IiPwr0PGCONMi/bH7/maBxnlU7Z1GDXFwNUqaa9oi
-- BCrypt hash for 'staff123': $2b$12$WxM1SwIf7i1c4cst428jmu.x4JWHCvCsmVC9QxLxikBnX8nFS5YCS
-- BCrypt hash for 'citizen123': $2b$12$7izSdFw/FERSmMlNDKVx7.znt55RxUKylnhTW7bL7pFvZtjYRrpku

INSERT INTO dsr_auth.users (
    id, 
    email, 
    password_hash, 
    first_name, 
    last_name, 
    middle_name, 
    phone_number, 
    role, 
    status, 
    email_verified, 
    two_factor_enabled, 
    failed_login_attempts, 
    account_locked_until, 
    last_login_at, 
    password_changed_at, 
    created_at, 
    updated_at
) VALUES 
-- Admin User
(
    gen_random_uuid(),
    'admin@dsr.gov.ph',
    '$2b$12$4.Mtbo5TDEW3IiPwr0PGCONMi/bH7/maBxnlU7Z1GDXFwNUqaa9oi',
    'System',
    'Administrator',
    'DSR',
    '+639171234567',
    'SYSTEM_ADMIN',
    'ACTIVE',
    true,
    false,
    0,
    null,
    null,
    NOW(),
    NOW(),
    NOW()
),
-- Staff User  
(
    gen_random_uuid(),
    'staff@dsr.gov.ph',
    '$2b$12$WxM1SwIf7i1c4cst428jmu.x4JWHCvCsmVC9QxLxikBnX8nFS5YCS',
    'DSR',
    'Staff',
    'Member',
    '+639181234567',
    'LGU_STAFF',
    'ACTIVE',
    true,
    false,
    0,
    null,
    null,
    NOW(),
    NOW(),
    NOW()
),
-- Citizen User
(
    gen_random_uuid(),
    'citizen@dsr.gov.ph',
    '$2b$12$7izSdFw/FERSmMlNDKVx7.znt55RxUKylnhTW7bL7pFvZtjYRrpku',
    'Juan',
    'Dela Cruz',
    'Santos',
    '+639191234567',
    'CITIZEN',
    'ACTIVE',
    true,
    false,
    0,
    null,
    null,
    NOW(),
    NOW(),
    NOW()
);

-- Verify the users were created
SELECT email, role, status, email_verified, created_at 
FROM dsr_auth.users 
WHERE email IN ('admin@dsr.gov.ph', 'staff@dsr.gov.ph', 'citizen@dsr.gov.ph')
ORDER BY email;
