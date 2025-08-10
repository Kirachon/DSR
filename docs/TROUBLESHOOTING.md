# DSR Local Development - Troubleshooting Guide

## Common Issues and Solutions

### Infrastructure Issues

#### PostgreSQL Connection Problems

**Symptoms:**
- Services fail to start with database connection errors
- `Connection refused` errors in logs

**Solutions:**
```bash
# Check if PostgreSQL is running
podman ps | grep postgresql

# Check PostgreSQL logs
podman-compose logs postgresql

# Restart PostgreSQL
podman-compose restart postgresql

# Verify connection manually
PGPASSWORD=dsr_local_password psql -h localhost -p 5432 -U dsr_user -d dsr_local

# If database doesn't exist, recreate it
podman-compose down
podman volume rm dsr_postgres_data
podman-compose up -d postgresql
```

#### Redis Connection Issues

**Symptoms:**
- Cache-related errors in service logs
- Session management problems

**Solutions:**
```bash
# Check Redis status
redis-cli -h localhost -p 6379 ping

# Check Redis logs
podman-compose logs redis

# Restart Redis
podman-compose restart redis

# Clear Redis data if needed
redis-cli -h localhost -p 6379 FLUSHALL
```

#### Elasticsearch Not Starting

**Symptoms:**
- Elasticsearch container exits immediately
- Out of memory errors

**Solutions:**
```bash
# Increase virtual memory (Linux/macOS)
sudo sysctl -w vm.max_map_count=262144

# For Windows with WSL2
wsl -d docker-desktop
sysctl -w vm.max_map_count=262144

# Check Elasticsearch logs
podman-compose logs elasticsearch

# Restart with more memory
podman-compose down
# Edit podman-compose.yml to increase ES_JAVA_OPTS
podman-compose up -d elasticsearch
```

#### Kafka Connection Problems

**Symptoms:**
- Event streaming not working
- Kafka consumer lag

**Solutions:**
```bash
# Check Kafka and Zookeeper
podman-compose logs kafka
podman-compose logs zookeeper

# Restart Kafka stack
podman-compose restart zookeeper
sleep 10
podman-compose restart kafka

# Test Kafka connectivity
nc -z localhost 9092
```

### Service Issues

#### Service Won't Start

**Symptoms:**
- Service exits immediately
- Port binding errors

**Solutions:**
```bash
# Check if port is already in use
lsof -i :8080

# Kill process using the port
kill -9 $(lsof -t -i:8080)

# Check service logs
tail -f logs/registration-service.log

# Start with debug logging
java -Dlogging.level.root=DEBUG -jar build/registration-service.jar

# Verify JAR file exists
ls -la build/registration-service.jar
```

#### Service Health Check Failures

**Symptoms:**
- Health endpoints return 503 or timeout
- Services appear down in monitoring

**Solutions:**
```bash
# Check actuator endpoints
curl http://localhost:8080/actuator/health

# Check specific health indicators
curl http://localhost:8080/actuator/health/db
curl http://localhost:8080/actuator/health/redis

# Restart service
./scripts/run-local.sh restart

# Check dependencies
./scripts/test-local-deployment.sh infrastructure
```

#### Memory Issues

**Symptoms:**
- OutOfMemoryError in logs
- Services becoming unresponsive

**Solutions:**
```bash
# Increase JVM heap size
export JAVA_OPTS="-Xmx2g -Xms1g"

# Monitor memory usage
java -XX:+PrintGCDetails -jar build/registration-service.jar

# Check system memory
free -h  # Linux
vm_stat  # macOS

# Restart services with more memory
./scripts/run-local.sh stop
export JAVA_OPTS="-Xmx2g"
./scripts/run-local.sh start
```

### Build Issues

#### Maven Build Failures

**Symptoms:**
- Build script fails
- Compilation errors

**Solutions:**
```bash
# Clean and rebuild
./mvnw clean
./mvnw compile

# Skip tests if needed
./mvnw clean package -DskipTests

# Check Java version
java -version
./mvnw -version

# Update dependencies
./mvnw dependency:resolve
```

#### Missing Dependencies

**Symptoms:**
- ClassNotFoundException
- NoSuchMethodError

**Solutions:**
```bash
# Clean Maven cache
rm -rf ~/.m2/repository

# Rebuild with fresh dependencies
./mvnw clean install

# Check for version conflicts
./mvnw dependency:tree
```

### Network Issues

#### Port Conflicts

**Symptoms:**
- "Address already in use" errors
- Services can't bind to ports

**Solutions:**
```bash
# Find what's using the port
lsof -i :8080
netstat -tulpn | grep 8080

# Use different ports
export REGISTRATION_SERVICE_PORT=8090
export DATA_MANAGEMENT_SERVICE_PORT=8091

# Kill conflicting processes
sudo kill -9 $(lsof -t -i:8080)
```

#### DNS Resolution Issues

**Symptoms:**
- Services can't connect to each other
- Host resolution failures

**Solutions:**
```bash
# Check /etc/hosts file
cat /etc/hosts

# Add entries if needed
echo "127.0.0.1 localhost" >> /etc/hosts

# Use IP addresses instead of hostnames
export DATABASE_HOST=127.0.0.1
```

### Performance Issues

#### Slow Response Times

**Symptoms:**
- API calls taking too long
- Timeouts in logs

**Solutions:**
```bash
# Check system resources
top
htop

# Monitor database performance
PGPASSWORD=dsr_local_password psql -h localhost -p 5432 -U dsr_user -d dsr_local -c "SELECT * FROM pg_stat_activity;"

# Increase connection pool sizes
# Edit application-local.yml files

# Check for database locks
PGPASSWORD=dsr_local_password psql -h localhost -p 5432 -U dsr_user -d dsr_local -c "SELECT * FROM pg_locks;"
```

#### High CPU Usage

**Symptoms:**
- System becomes sluggish
- High CPU in monitoring

**Solutions:**
```bash
# Identify CPU-intensive processes
top -o cpu

# Reduce parallel processing
# Edit service configurations to use fewer threads

# Check for infinite loops in logs
grep -i "loop\|infinite\|stuck" logs/*.log
```

### Data Issues

#### Sample Data Not Loading

**Symptoms:**
- Empty database tables
- No test data available

**Solutions:**
```bash
# Manually load sample data
PGPASSWORD=dsr_local_password psql -h localhost -p 5432 -U dsr_user -d dsr_local -f database/sample-data/03_load_sample_data.sql

# Check if data exists
PGPASSWORD=dsr_local_password psql -h localhost -p 5432 -U dsr_user -d dsr_local -c "SELECT COUNT(*) FROM dsr_core.household_profiles;"

# Reset and reload data
PGPASSWORD=dsr_local_password psql -h localhost -p 5432 -U dsr_user -d dsr_local -c "SELECT dsr_core.reset_local_data();"
PGPASSWORD=dsr_local_password psql -h localhost -p 5432 -U dsr_user -d dsr_local -c "SELECT dsr_core.generate_comprehensive_sample_data(50, true, true);"
```

#### Database Schema Issues

**Symptoms:**
- Table doesn't exist errors
- Column not found errors

**Solutions:**
```bash
# Check if schema exists
PGPASSWORD=dsr_local_password psql -h localhost -p 5432 -U dsr_user -d dsr_local -c "\dn"

# Recreate schema
PGPASSWORD=dsr_local_password psql -h localhost -p 5432 -U dsr_user -d dsr_local -f database/schemas/01_core_schema.sql

# Check table structure
PGPASSWORD=dsr_local_password psql -h localhost -p 5432 -U dsr_user -d dsr_local -c "\dt dsr_core.*"
```

### Monitoring Issues

#### Grafana Not Accessible

**Symptoms:**
- Can't access Grafana dashboard
- Login issues

**Solutions:**
```bash
# Check Grafana container
podman ps | grep grafana

# Reset Grafana admin password
podman exec -it dsr-grafana grafana-cli admin reset-admin-password admin

# Check Grafana logs
podman-compose logs grafana

# Restart Grafana
podman-compose restart grafana
```

#### Prometheus Not Collecting Metrics

**Symptoms:**
- No data in Grafana
- Prometheus targets down

**Solutions:**
```bash
# Check Prometheus targets
curl http://localhost:9090/api/v1/targets

# Verify service metrics endpoints
curl http://localhost:8080/actuator/prometheus

# Check Prometheus configuration
podman exec -it dsr-prometheus cat /etc/prometheus/prometheus.yml

# Restart Prometheus
podman-compose restart prometheus
```

## Getting Help

### Log Analysis

```bash
# View all service logs
./scripts/run-local.sh logs

# Search for errors
grep -i error logs/*.log

# Check specific service
tail -f logs/registration-service.log
```

### System Information

```bash
# Check system resources
df -h
free -h
top

# Check Podman status
podman system info
podman-compose ps
```

### Reset Everything

```bash
# Nuclear option - reset entire environment
./scripts/run-local.sh stop
podman-compose down -v
podman system prune -a
./scripts/setup-local-env.sh
```

### Contact Support

If issues persist:
1. Collect logs: `./scripts/run-local.sh logs > debug.log`
2. Run diagnostics: `./scripts/test-local-deployment.sh > test-results.log`
3. Include system information and error messages
4. Create an issue in the project repository
