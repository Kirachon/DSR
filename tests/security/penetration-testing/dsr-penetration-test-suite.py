#!/usr/bin/env python3
"""
DSR Penetration Testing Suite
Comprehensive penetration testing framework for all 7 DSR services
Automated security testing with OWASP methodology
"""

import requests
import json
import time
import sys
import argparse
from datetime import datetime
from typing import List, Dict, Any
import urllib.parse
import base64
import hashlib

class DSRPenetrationTester:
    def __init__(self, base_url: str = "http://localhost", auth_token: str = None):
        self.base_url = base_url
        self.auth_token = auth_token
        self.services = {
            'registration': f'{base_url}:8080',
            'data_management': f'{base_url}:8081',
            'eligibility': f'{base_url}:8082',
            'interoperability': f'{base_url}:8083',
            'payment': f'{base_url}:8084',
            'grievance': f'{base_url}:8085',
            'analytics': f'{base_url}:8086'
        }
        self.vulnerabilities = []
        self.test_results = {}
        
    def authenticate(self, email: str = "security.tester@dsr.gov.ph", password: str = "SecureTestPassword123!"):
        """Authenticate with DSR system for testing"""
        try:
            auth_url = f"{self.services['registration']}/api/v1/auth/login"
            response = requests.post(auth_url, json={
                'email': email,
                'password': password
            }, timeout=30)
            
            if response.status_code == 200:
                auth_data = response.json()
                self.auth_token = auth_data.get('accessToken')
                print(f"✅ Authentication successful")
                return True
            else:
                print(f"❌ Authentication failed: {response.status_code}")
                return False
        except Exception as e:
            print(f"❌ Authentication error: {str(e)}")
            return False
    
    def get_headers(self, include_auth: bool = True) -> Dict[str, str]:
        """Get standard headers for requests"""
        headers = {
            'Content-Type': 'application/json',
            'User-Agent': 'DSR-PenTest-Suite/1.0'
        }
        if include_auth and self.auth_token:
            headers['Authorization'] = f'Bearer {self.auth_token}'
        return headers
    
    def log_vulnerability(self, service: str, vuln_type: str, severity: str, 
                         description: str, evidence: str, recommendation: str):
        """Log discovered vulnerability"""
        vulnerability = {
            'timestamp': datetime.now().isoformat(),
            'service': service,
            'type': vuln_type,
            'severity': severity,
            'description': description,
            'evidence': evidence,
            'recommendation': recommendation
        }
        self.vulnerabilities.append(vulnerability)
        print(f"🚨 {severity} vulnerability found in {service}: {vuln_type}")
    
    def test_authentication_bypass(self) -> Dict[str, Any]:
        """Test for authentication bypass vulnerabilities"""
        print("\n🔍 Testing Authentication Bypass...")
        results = {}
        
        for service_name, service_url in self.services.items():
            print(f"  Testing {service_name}...")
            
            # Test endpoints without authentication
            test_endpoints = [
                '/api/v1/health',
                '/api/v1/admin/users',
                '/api/v1/config',
                '/actuator/env',
                '/actuator/configprops'
            ]
            
            bypass_attempts = 0
            for endpoint in test_endpoints:
                try:
                    response = requests.get(f"{service_url}{endpoint}", timeout=10)
                    
                    # Check for sensitive data exposure
                    if response.status_code == 200:
                        content = response.text.lower()
                        if any(keyword in content for keyword in ['password', 'secret', 'key', 'token']):
                            self.log_vulnerability(
                                service_name,
                                'Sensitive Data Exposure',
                                'HIGH',
                                f'Endpoint {endpoint} exposes sensitive information without authentication',
                                f'Response contains sensitive keywords: {response.status_code}',
                                'Implement proper authentication and data filtering'
                            )
                            bypass_attempts += 1
                            
                except Exception as e:
                    continue
            
            results[service_name] = {
                'bypass_attempts': bypass_attempts,
                'status': 'VULNERABLE' if bypass_attempts > 0 else 'SECURE'
            }
        
        return results
    
    def test_sql_injection(self) -> Dict[str, Any]:
        """Test for SQL injection vulnerabilities"""
        print("\n🔍 Testing SQL Injection...")
        results = {}
        
        sql_payloads = [
            "' OR '1'='1",
            "'; DROP TABLE users; --",
            "' UNION SELECT version() --",
            "1' OR 1=1#",
            "admin'/**/OR/**/1=1#"
        ]
        
        for service_name, service_url in self.services.items():
            print(f"  Testing {service_name}...")
            vulnerable_endpoints = 0
            
            # Test common vulnerable endpoints
            test_cases = [
                {'endpoint': '/api/v1/search', 'param': 'query'},
                {'endpoint': '/api/v1/households/search', 'param': 'name'},
                {'endpoint': '/api/v1/users', 'param': 'filter'}
            ]
            
            for test_case in test_cases:
                for payload in sql_payloads:
                    try:
                        params = {test_case['param']: payload}
                        response = requests.get(
                            f"{service_url}{test_case['endpoint']}", 
                            params=params,
                            headers=self.get_headers(),
                            timeout=10
                        )
                        
                        # Check for SQL error messages
                        content = response.text.lower()
                        sql_errors = ['sql syntax', 'mysql', 'postgresql', 'ora-', 'sqlite', 'syntax error']
                        
                        if any(error in content for error in sql_errors):
                            self.log_vulnerability(
                                service_name,
                                'SQL Injection',
                                'CRITICAL',
                                f'SQL injection vulnerability in {test_case["endpoint"]}',
                                f'Payload: {payload}, Response contains SQL errors',
                                'Use parameterized queries and input validation'
                            )
                            vulnerable_endpoints += 1
                            break
                            
                    except Exception as e:
                        continue
            
            results[service_name] = {
                'vulnerable_endpoints': vulnerable_endpoints,
                'status': 'VULNERABLE' if vulnerable_endpoints > 0 else 'SECURE'
            }
        
        return results
    
    def test_xss_vulnerabilities(self) -> Dict[str, Any]:
        """Test for Cross-Site Scripting vulnerabilities"""
        print("\n🔍 Testing XSS Vulnerabilities...")
        results = {}
        
        xss_payloads = [
            '<script>alert("XSS")</script>',
            '<img src="x" onerror="alert(1)">',
            'javascript:alert("XSS")',
            '<svg onload="alert(1)">',
            '"><script>alert("XSS")</script>'
        ]
        
        for service_name, service_url in self.services.items():
            print(f"  Testing {service_name}...")
            vulnerable_fields = 0
            
            # Test form submissions with XSS payloads
            test_endpoints = [
                {'url': '/api/v1/grievances', 'method': 'POST', 'fields': ['complainantName', 'description']},
                {'url': '/api/v1/households', 'method': 'POST', 'fields': ['firstName', 'lastName']},
                {'url': '/api/v1/comments', 'method': 'POST', 'fields': ['content', 'title']}
            ]
            
            for endpoint in test_endpoints:
                for payload in xss_payloads:
                    try:
                        data = {}
                        for field in endpoint['fields']:
                            data[field] = payload
                        
                        if endpoint['method'] == 'POST':
                            response = requests.post(
                                f"{service_url}{endpoint['url']}", 
                                json=data,
                                headers=self.get_headers(),
                                timeout=10
                            )
                        
                        # Check if payload is reflected without encoding
                        if response.status_code in [200, 201]:
                            content = response.text
                            if payload in content and '<script>' in content:
                                self.log_vulnerability(
                                    service_name,
                                    'Cross-Site Scripting (XSS)',
                                    'HIGH',
                                    f'XSS vulnerability in {endpoint["url"]}',
                                    f'Payload reflected without encoding: {payload}',
                                    'Implement proper input validation and output encoding'
                                )
                                vulnerable_fields += 1
                                
                    except Exception as e:
                        continue
            
            results[service_name] = {
                'vulnerable_fields': vulnerable_fields,
                'status': 'VULNERABLE' if vulnerable_fields > 0 else 'SECURE'
            }
        
        return results
    
    def test_business_logic_flaws(self) -> Dict[str, Any]:
        """Test for business logic vulnerabilities"""
        print("\n🔍 Testing Business Logic Flaws...")
        results = {}
        
        for service_name, service_url in self.services.items():
            print(f"  Testing {service_name}...")
            logic_flaws = 0
            
            # Test service-specific business logic
            if service_name == 'eligibility':
                # Test negative income values
                try:
                    response = requests.post(
                        f"{service_url}/api/v1/eligibility/assessments",
                        json={
                            'householdId': 'HH-TEST-123',
                            'monthlyIncome': -50000,  # Negative income
                            'assessmentType': 'FULL_ASSESSMENT'
                        },
                        headers=self.get_headers(),
                        timeout=10
                    )
                    
                    if response.status_code in [200, 201]:
                        self.log_vulnerability(
                            service_name,
                            'Business Logic Bypass',
                            'HIGH',
                            'Negative income values accepted in eligibility assessment',
                            f'Negative income (-50000) was accepted: {response.status_code}',
                            'Implement server-side business rule validation'
                        )
                        logic_flaws += 1
                        
                except Exception as e:
                    pass
            
            elif service_name == 'payment':
                # Test payment amount manipulation
                try:
                    response = requests.post(
                        f"{service_url}/api/v1/payments",
                        json={
                            'beneficiaryId': 'BEN-TEST-123',
                            'amount': 999999999,  # Extremely large amount
                            'programId': 'PANTAWID'
                        },
                        headers=self.get_headers(),
                        timeout=10
                    )
                    
                    if response.status_code in [200, 201]:
                        self.log_vulnerability(
                            service_name,
                            'Business Logic Bypass',
                            'CRITICAL',
                            'Unrealistic payment amounts accepted',
                            f'Large payment amount (999999999) was accepted: {response.status_code}',
                            'Implement payment amount limits and validation'
                        )
                        logic_flaws += 1
                        
                except Exception as e:
                    pass
            
            results[service_name] = {
                'logic_flaws': logic_flaws,
                'status': 'VULNERABLE' if logic_flaws > 0 else 'SECURE'
            }
        
        return results
    
    def test_information_disclosure(self) -> Dict[str, Any]:
        """Test for information disclosure vulnerabilities"""
        print("\n🔍 Testing Information Disclosure...")
        results = {}
        
        for service_name, service_url in self.services.items():
            print(f"  Testing {service_name}...")
            disclosure_issues = 0
            
            # Test error message information disclosure
            test_endpoints = [
                '/api/v1/nonexistent',
                '/api/v1/users/999999',
                '/api/v1/admin/config',
                '/actuator/env',
                '/actuator/configprops'
            ]
            
            for endpoint in test_endpoints:
                try:
                    response = requests.get(f"{service_url}{endpoint}", timeout=10)
                    content = response.text.lower()
                    
                    # Check for sensitive information in responses
                    sensitive_keywords = [
                        'password', 'secret', 'key', 'token', 'database',
                        'connection', 'jdbc', 'username', 'config'
                    ]
                    
                    found_keywords = [kw for kw in sensitive_keywords if kw in content]
                    
                    if found_keywords and response.status_code != 404:
                        self.log_vulnerability(
                            service_name,
                            'Information Disclosure',
                            'MEDIUM',
                            f'Sensitive information exposed in {endpoint}',
                            f'Found keywords: {", ".join(found_keywords)}',
                            'Remove sensitive information from error messages and responses'
                        )
                        disclosure_issues += 1
                        
                except Exception as e:
                    continue
            
            results[service_name] = {
                'disclosure_issues': disclosure_issues,
                'status': 'VULNERABLE' if disclosure_issues > 0 else 'SECURE'
            }
        
        return results
    
    def run_comprehensive_test(self) -> Dict[str, Any]:
        """Run comprehensive penetration testing suite"""
        print("🚀 Starting DSR Penetration Testing Suite...")
        print(f"Target services: {len(self.services)}")
        print(f"Authentication: {'✅ Enabled' if self.auth_token else '❌ Disabled'}")
        
        start_time = datetime.now()
        
        # Run all test categories
        test_results = {
            'authentication_bypass': self.test_authentication_bypass(),
            'sql_injection': self.test_sql_injection(),
            'xss_vulnerabilities': self.test_xss_vulnerabilities(),
            'business_logic_flaws': self.test_business_logic_flaws(),
            'information_disclosure': self.test_information_disclosure()
        }
        
        end_time = datetime.now()
        duration = (end_time - start_time).total_seconds()
        
        # Generate summary
        total_vulnerabilities = len(self.vulnerabilities)
        critical_vulns = len([v for v in self.vulnerabilities if v['severity'] == 'CRITICAL'])
        high_vulns = len([v for v in self.vulnerabilities if v['severity'] == 'HIGH'])
        medium_vulns = len([v for v in self.vulnerabilities if v['severity'] == 'MEDIUM'])
        low_vulns = len([v for v in self.vulnerabilities if v['severity'] == 'LOW'])
        
        summary = {
            'test_duration': duration,
            'services_tested': len(self.services),
            'total_vulnerabilities': total_vulnerabilities,
            'vulnerability_breakdown': {
                'critical': critical_vulns,
                'high': high_vulns,
                'medium': medium_vulns,
                'low': low_vulns
            },
            'test_results': test_results,
            'vulnerabilities': self.vulnerabilities,
            'overall_security_status': self.get_overall_security_status()
        }
        
        return summary
    
    def get_overall_security_status(self) -> str:
        """Determine overall security status"""
        critical_vulns = len([v for v in self.vulnerabilities if v['severity'] == 'CRITICAL'])
        high_vulns = len([v for v in self.vulnerabilities if v['severity'] == 'HIGH'])
        
        if critical_vulns > 0:
            return 'CRITICAL_RISK'
        elif high_vulns > 3:
            return 'HIGH_RISK'
        elif high_vulns > 0:
            return 'MEDIUM_RISK'
        else:
            return 'LOW_RISK'
    
    def generate_report(self, output_file: str = None):
        """Generate penetration testing report"""
        summary = self.run_comprehensive_test()
        
        report = {
            'report_metadata': {
                'generated_at': datetime.now().isoformat(),
                'tester': 'DSR Penetration Testing Suite',
                'version': '1.0.0',
                'target_system': 'Dynamic Social Registry (DSR) v3.0.0'
            },
            'executive_summary': {
                'test_duration_seconds': summary['test_duration'],
                'services_tested': summary['services_tested'],
                'total_vulnerabilities': summary['total_vulnerabilities'],
                'overall_security_status': summary['overall_security_status'],
                'recommendation': self.get_security_recommendation(summary['overall_security_status'])
            },
            'detailed_results': summary
        }
        
        if output_file:
            with open(output_file, 'w') as f:
                json.dump(report, f, indent=2)
            print(f"📄 Report saved to: {output_file}")
        
        # Print summary to console
        self.print_summary(summary)
        
        return report
    
    def get_security_recommendation(self, status: str) -> str:
        """Get security recommendation based on status"""
        recommendations = {
            'CRITICAL_RISK': 'IMMEDIATE ACTION REQUIRED - Do not deploy to production',
            'HIGH_RISK': 'Address high-severity vulnerabilities before production deployment',
            'MEDIUM_RISK': 'Review and address vulnerabilities, acceptable for production with monitoring',
            'LOW_RISK': 'System is secure for production deployment with standard monitoring'
        }
        return recommendations.get(status, 'Review security findings')
    
    def print_summary(self, summary: Dict[str, Any]):
        """Print test summary to console"""
        print("\n" + "="*60)
        print("🛡️  DSR PENETRATION TESTING SUMMARY")
        print("="*60)
        print(f"⏱️  Test Duration: {summary['test_duration']:.2f} seconds")
        print(f"🎯 Services Tested: {summary['services_tested']}")
        print(f"🚨 Total Vulnerabilities: {summary['total_vulnerabilities']}")
        print(f"📊 Breakdown:")
        print(f"   🔴 Critical: {summary['vulnerability_breakdown']['critical']}")
        print(f"   🟠 High: {summary['vulnerability_breakdown']['high']}")
        print(f"   🟡 Medium: {summary['vulnerability_breakdown']['medium']}")
        print(f"   🟢 Low: {summary['vulnerability_breakdown']['low']}")
        print(f"🏆 Overall Status: {summary['overall_security_status']}")
        print("="*60)

def main():
    parser = argparse.ArgumentParser(description='DSR Penetration Testing Suite')
    parser.add_argument('--base-url', default='http://localhost', help='Base URL for DSR services')
    parser.add_argument('--output', help='Output file for report')
    parser.add_argument('--auth-email', default='security.tester@dsr.gov.ph', help='Authentication email')
    parser.add_argument('--auth-password', default='SecureTestPassword123!', help='Authentication password')
    
    args = parser.parse_args()
    
    # Initialize tester
    tester = DSRPenetrationTester(base_url=args.base_url)
    
    # Authenticate
    if not tester.authenticate(args.auth_email, args.auth_password):
        print("❌ Authentication failed. Some tests may not run properly.")
    
    # Generate report
    output_file = args.output or f"dsr_pentest_report_{datetime.now().strftime('%Y%m%d_%H%M%S')}.json"
    tester.generate_report(output_file)

if __name__ == "__main__":
    main()
