-- Create a test user for API testing
INSERT INTO dsr_auth.users (
    email,
    password_hash,
    first_name,
    last_name,
    phone_number,
    role,
    status,
    email_verified,
    phone_verified,
    created_at,
    updated_at
) VALUES (
    'test@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye.Ik.KzAWR.2Y.bGdwMaMeU7H.a8/ye.',  -- BCrypt hash for 'TestPassword123!'
    'John',
    'Doe',
    '+639123456789',
    'CITIZEN',
    'ACTIVE',
    true,
    true,
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;

-- Verify the user was created
SELECT id, email, first_name, last_name, role, status FROM dsr_auth.users WHERE email = 'test@example.com';
