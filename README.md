# Philippine Dynamic Social Registry (DSR) - Production Implementation

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2+-green.svg)](https://spring.io/projects/spring-boot)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.30+-blue.svg)](https://kubernetes.io/)
[![Podman](https://img.shields.io/badge/Podman-4.8+-purple.svg)](https://podman.io/)

## Overview

This repository contains the complete production-ready implementation of the Philippine Dynamic Social Registry (DSR) system, a modern cloud-native platform for delivering social protection services to Filipino citizens.

## Architecture

The DSR system is built using a microservices architecture with the following core services:

- **Registration Service** - Citizen engagement and household registration
- **Data Management Service** - Unified data management and master data
- **Eligibility Service** - Program eligibility assessment and recommendations
- **Interoperability Service** - Agency APIs and service delivery tracking
- **Payment Service** - Financial service provider integrations
- **Grievance Service** - Multi-channel grievance management
- **Analytics Service** - Real-time reporting and dashboards

## Technology Stack

### Core Technologies
- **Runtime**: Java 17+ with Spring Boot 3.2+
- **Databases**: PostgreSQL 16+, Redis 7.2+, Elasticsearch 8.11+
- **Messaging**: Apache Kafka 3.6+
- **Containerization**: Podman 4.8+ (rootless containers)
- **Orchestration**: Kubernetes 1.30+ with Istio 1.20+ service mesh

### Frontend & Mobile
- **Web**: Next.js 14+ with React
- **Mobile**: React Native 0.73+
- **API Gateway**: Kong Gateway 3.5+

### DevOps & Monitoring
- **CI/CD**: GitLab CI/CD 16+
- **IaC**: Terraform, Helm 3.13+
- **Monitoring**: Prometheus 2.48+, Grafana 10.2+, Jaeger 1.52+
- **Logging**: ELK Stack 8.11+

## Quick Start

### Prerequisites

- Podman 4.8+
- Kubernetes 1.30+ cluster
- Helm 3.13+
- Java 17+
- Node.js 18+

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/Kirachon/DSR.git
   cd DSR
   ```

2. **Start local infrastructure**
   ```bash
   # Start PostgreSQL, Redis, Elasticsearch, and Kafka
   podman-compose -f docker-compose.dev.yml up -d
   ```

3. **Build and run services**
   ```bash
   # Build all services
   ./scripts/build-all.sh

   # Run services locally
   ./scripts/run-local.sh
   ```

4. **Access the application**
   - Web Portal: http://localhost:3000
   - API Gateway: http://localhost:8080
   - API Documentation: http://localhost:8080/swagger-ui

### Production Deployment

1. **Deploy to Kubernetes**
   ```bash
   # Deploy infrastructure
   helm install dsr-infrastructure ./helm/infrastructure

   # Deploy services
   helm install dsr-services ./helm/services
   ```

2. **Configure Istio service mesh**
   ```bash
   kubectl apply -f k8s/istio/
   ```

3. **Set up monitoring**
   ```bash
   helm install monitoring ./helm/monitoring
   ```

## Project Structure

```
DSR/
├── services/                    # Microservices
│   ├── registration-service/    # Citizen registration and engagement
│   ├── data-management-service/ # Unified data management
│   ├── eligibility-service/     # Program eligibility assessment
│   ├── interoperability-service/ # Agency interoperability
│   ├── payment-service/         # Payment and disbursement
│   ├── grievance-service/       # Grievance management
│   └── analytics-service/       # Analytics and reporting
├── shared/                      # Shared libraries and utilities
│   ├── common/                  # Common utilities
│   ├── security/               # Security components
│   └── messaging/              # Event handling
├── frontend/                    # Frontend applications
│   ├── web-portal/             # Citizen web portal
│   ├── admin-portal/           # Administrative portal
│   └── mobile-app/             # Mobile application
├── infrastructure/              # Infrastructure as Code
│   ├── terraform/              # Terraform configurations
│   ├── helm/                   # Helm charts
│   └── k8s/                    # Kubernetes manifests
├── database/                    # Database schemas and migrations
├── monitoring/                  # Monitoring and observability
├── security/                    # Security policies and configurations
├── scripts/                     # Build and deployment scripts
└── docs/                       # Additional documentation
```

## Security

The DSR system implements a comprehensive zero-trust security architecture:

- **Authentication**: OAuth 2.1 with PKCE
- **Authorization**: Role-based access control (RBAC)
- **Encryption**: TLS 1.3 for data in transit, AES-256 for data at rest
- **Network Security**: Istio service mesh with mTLS
- **Container Security**: Rootless Podman containers with security policies
- **Compliance**: Philippine Data Privacy Act (R.A. 10173) compliant

## API Documentation

Complete API documentation is available at:
- **Development**: http://localhost:8080/swagger-ui
- **Production**: https://api.dsr.gov.ph/swagger-ui

All APIs follow OpenAPI 3.1 specifications and include:
- Comprehensive request/response examples
- Error handling documentation
- Security requirements
- Rate limiting information

## Monitoring and Observability

The system includes comprehensive monitoring:

- **Metrics**: Prometheus with custom business metrics
- **Dashboards**: Grafana dashboards for all services
- **Tracing**: Jaeger distributed tracing
- **Logging**: Centralized logging with ELK stack
- **Alerting**: PagerDuty integration for critical alerts

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## Support

- **Technical Support**: api-support@dsr.gov.ph
- **Documentation**: docs@dsr.gov.ph
- **Issues**: Create an issue in this repository
- **Security**: security@dsr.gov.ph

## Acknowledgments

- Department of Social Welfare and Development (DSWD)
- Philippine Statistics Authority (PSA)
- Department of Information and Communications Technology (DICT)
- All contributing government agencies and technical teams

---

**Note**: This is a production-ready implementation of the Philippine Dynamic Social Registry system. Please ensure proper security configurations and compliance with local regulations before deployment.