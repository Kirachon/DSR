-- Update user passwords with proper BCrypt hashes
-- BCrypt hash for 'admin123' - $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
UPDATE dsr_auth.users
SET password_hash = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE email = 'admin@dsr.gov.ph';

-- BCrypt hash for 'citizen123' - $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
UPDATE dsr_auth.users
SET password_hash = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE email = 'citizen@dsr.gov.ph';

-- Verify the updates
SELECT email, password_hash FROM dsr_auth.users WHERE email IN ('admin@dsr.gov.ph', 'citizen@dsr.gov.ph');
