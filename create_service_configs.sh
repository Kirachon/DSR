#!/bin/bash

# Create service configurations
services=(
  "eligibility:8082"
  "interoperability:8083"
  "payment:8084"
  "grievance:8085"
  "analytics:8086"
)

for service_port in "${services[@]}"; do
  service=$(echo $service_port | cut -d: -f1)
  port=$(echo $service_port | cut -d: -f2)
  
  echo "Creating configuration for $service service on port $port"
  
  # Create resources directory
  mkdir -p "services/${service}-service/src/main/resources"
  
  # Copy base configuration
  cp "services/data-management-service/src/main/resources/application-local.yml" \
     "services/${service}-service/src/main/resources/application-local.yml"
  
  # Update service-specific values
  sed -i "s/Data Management Service/${service^} Service/g" "services/${service}-service/src/main/resources/application-local.yml"
  sed -i "s/DATA_MANAGEMENT_SERVICE_PORT:8081/${service^^}_SERVICE_PORT:${port}/g" "services/${service}-service/src/main/resources/application-local.yml"
  sed -i "s/dsr-data-management-service/dsr-${service}-service/g" "services/${service}-service/src/main/resources/application-local.yml"
  sed -i "s/data_management_service/${service}_service/g" "services/${service}-service/src/main/resources/application-local.yml"
  sed -i "s/data-management-service.log/${service}-service.log/g" "services/${service}-service/src/main/resources/application-local.yml"
done

echo "Service configurations created successfully!"
