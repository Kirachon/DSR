#!/usr/bin/env python3
"""
Generate BCrypt hashes for DSR demo user passwords
"""

import bcrypt

def generate_hash(password):
    """Generate BCrypt hash for a password"""
    # Generate salt and hash the password
    salt = bcrypt.gensalt()
    hashed = bcrypt.hashpw(password.encode('utf-8'), salt)
    return hashed.decode('utf-8')

def main():
    passwords = {
        'admin123': 'admin@dsr.gov.ph',
        'staff123': 'staff@dsr.gov.ph', 
        'citizen123': 'citizen@dsr.gov.ph'
    }
    
    print("Generating BCrypt hashes for DSR demo passwords:")
    print("=" * 50)
    
    for password, email in passwords.items():
        hash_value = generate_hash(password)
        print(f"Password: {password}")
        print(f"Email: {email}")
        print(f"BCrypt Hash: {hash_value}")
        print("-" * 50)

if __name__ == "__main__":
    main()
