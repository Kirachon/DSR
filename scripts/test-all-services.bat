@echo off
echo === DSR Services Health Check ===
echo Testing all 7 microservices...
echo.

echo Testing Registration Service (Port 8080)...
curl -s http://localhost:8080/api/v1/health
echo.

echo Testing Data Management Service (Port 8081)...
curl -s http://localhost:8081/actuator/health
echo.

echo Testing Eligibility Service (Port 8082)...
curl -s http://localhost:8082/actuator/health
echo.

echo Testing Interoperability Service (Port 8083)...
curl -s http://localhost:8083/actuator/health
echo.

echo Testing Payment Service (Port 8084)...
curl -s http://localhost:8084/actuator/health
echo.

echo Testing Grievance Service (Port 8085)...
curl -s http://localhost:8085/actuator/health
echo.

echo Testing Analytics Service (Port 8086)...
curl -s http://localhost:8086/actuator/health
echo.

echo === Health Check Complete ===
