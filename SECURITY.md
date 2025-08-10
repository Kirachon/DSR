# DSR Security Guidelines

## Overview
This document outlines security best practices for the Philippine Dynamic Social Registry (DSR) system development and deployment.

## Environment Configuration Security

### ❌ NEVER Commit These Files:
- `.env.local` - Contains local development secrets
- `.env.production` - Contains production secrets
- `config.local.*` - Local configuration with sensitive data
- `secrets.json` - API keys and credentials
- `credentials.*` - Authentication credentials

### ✅ Safe to Commit:
- `.env.example` - Template files without real values
- `.env.template` - Configuration templates
- `config.example.*` - Example configurations

### Environment Variables Security
All sensitive configuration must use environment variables or external secret management:

```yaml
# ❌ WRONG - Hardcoded secrets
database:
  password: "mypassword123"
  
# ✅ CORRECT - Environment variable
database:
  password: ${DATABASE_PASSWORD}
```

## Secrets Management

### Development Environment
- Use `.env.local` for local development (never commit)
- Use mock/dummy values for external services
- Use local databases with non-production credentials

### Production Environment
- Use external secret management (AWS Secrets Manager, Azure Key Vault, etc.)
- Use environment variables injected at runtime
- Rotate secrets regularly

## CI/CD Security

### GitLab CI Variables
Use GitLab CI/CD variables for sensitive data:
```yaml
variables:
  POSTGRES_PASSWORD: $CI_POSTGRES_PASSWORD  # ✅ CORRECT
  # POSTGRES_PASSWORD: test_password        # ❌ WRONG
```

### GitHub Actions Secrets
Use GitHub repository secrets:
```yaml
env:
  SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}   # ✅ CORRECT
  # SONAR_TOKEN: abc123                     # ❌ WRONG
```

## File Security Checklist

Before committing, ensure:
- [ ] No `.env*` files (except `.env.example`)
- [ ] No hardcoded passwords in configuration files
- [ ] No API keys in source code
- [ ] No database connection strings with credentials
- [ ] No SSL certificates or private keys
- [ ] No service account files

## Security Scanning

The CI/CD pipeline includes:
- SonarCloud code quality and security analysis
- Snyk vulnerability scanning
- Container image security scanning
- Dependency vulnerability checks

## Incident Response

If sensitive data is accidentally committed:
1. **Immediately** rotate all exposed credentials
2. Remove the sensitive data from the repository
3. Contact the security team
4. Consider the data compromised and take appropriate action

## Contact

For security concerns, contact:
- Security Team: security@dsr.gov.ph
- DevOps Team: devops@dsr.gov.ph
