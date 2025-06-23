#!/bin/bash

# Create basic service structure for all DSR microservices
set -euo pipefail

SERVICES=(
    "data-management-service:DataManagementServiceApplication"
    "eligibility-service:EligibilityServiceApplication"
    "interoperability-service:InteroperabilityServiceApplication"
    "payment-service:PaymentServiceApplication"
    "grievance-service:GrievanceServiceApplication"
    "analytics-service:AnalyticsServiceApplication"
)

for service_info in "${SERVICES[@]}"; do
    IFS=':' read -r service_name class_name <<< "$service_info"
    
    # Create directory structure
    mkdir -p "services/$service_name/src/main/java/ph/gov/dsr/${service_name//-/}"
    mkdir -p "services/$service_name/src/main/resources"
    
    # Create main application class if it doesn't exist
    app_file="services/$service_name/src/main/java/ph/gov/dsr/${service_name//-/}/$class_name.java"
    if [[ ! -f "$app_file" ]]; then
        cat > "$app_file" << EOF
package ph.gov.dsr.${service_name//-/};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class $class_name {

    public static void main(String[] args) {
        SpringApplication.run($class_name.class, args);
    }

    @GetMapping("/")
    public String home() {
        return "DSR ${service_name^} is running!";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
EOF
        echo "Created $app_file"
    fi
    
    # Create basic application.yml if it doesn't exist
    app_yml="services/$service_name/src/main/resources/application.yml"
    if [[ ! -f "$app_yml" ]]; then
        cat > "$app_yml" << EOF
server:
  port: 8080

spring:
  application:
    name: dsr-$service_name
  profiles:
    active: local

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
EOF
        echo "Created $app_yml"
    fi
done

echo "Service structure creation completed!"
