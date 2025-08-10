# DSR Operational Runbook

**Version:** 1.0  
**Date:** June 24, 2025  
**Purpose:** Day-to-day operational procedures for DSR production system

## 🎯 Overview

This runbook provides step-by-step procedures for common operational tasks, incident response, and system maintenance for the DSR production environment.

## 📊 Daily Operations

### Morning Health Check (9:00 AM)
```bash
# 1. Check overall system status
kubectl get pods -n dsr-production
kubectl get services -n dsr-production

# 2. Verify all services are healthy
for port in 8080 8081 8082 8083 8084 8085 8086; do
    curl -f "https://api.dsr.gov.ph/actuator/health" || echo "❌ Service on port $port unhealthy"
done

# 3. Check frontend accessibility
curl -f "https://dsr.gov.ph" || echo "❌ Frontend inaccessible"

# 4. Review overnight alerts
kubectl logs -l app=alertmanager -n dsr-monitoring --since=24h
```

### Database Health Check
```bash
# Check PostgreSQL status
kubectl exec -it postgresql-0 -n dsr-production -- pg_isready -U dsr_user

# Check database connections
kubectl exec -it postgresql-0 -n dsr-production -- \
  psql -U dsr_user -d dsr_production -c "SELECT count(*) FROM pg_stat_activity;"

# Check database size
kubectl exec -it postgresql-0 -n dsr-production -- \
  psql -U dsr_user -d dsr_production -c "SELECT pg_size_pretty(pg_database_size('dsr_production'));"
```

### Performance Monitoring
```bash
# Check resource utilization
kubectl top pods -n dsr-production
kubectl top nodes

# Review response times
curl -w "@curl-format.txt" -o /dev/null -s "https://api.dsr.gov.ph/api/v1/health"
```

## 🚨 Incident Response

### Service Down Alert
```bash
# 1. Identify failed service
kubectl get pods -n dsr-production | grep -v Running

# 2. Check service logs
kubectl logs -f deployment/dsr-[SERVICE-NAME] -n dsr-production --tail=100

# 3. Check recent events
kubectl get events -n dsr-production --sort-by='.lastTimestamp' | head -20

# 4. Restart service if needed
kubectl rollout restart deployment/dsr-[SERVICE-NAME] -n dsr-production

# 5. Monitor recovery
kubectl rollout status deployment/dsr-[SERVICE-NAME] -n dsr-production
```

### Database Connection Issues
```bash
# 1. Check PostgreSQL pod status
kubectl get pods -l app=postgresql -n dsr-production

# 2. Check PostgreSQL logs
kubectl logs -f postgresql-0 -n dsr-production --tail=50

# 3. Test database connectivity
kubectl exec -it postgresql-0 -n dsr-production -- \
  psql -U dsr_user -d dsr_production -c "SELECT 1;"

# 4. Check connection pool status
kubectl exec -it deployment/dsr-registration-service -n dsr-production -- \
  curl -s http://localhost:8080/actuator/metrics/hikaricp.connections.active
```

### High CPU/Memory Usage
```bash
# 1. Identify resource-heavy pods
kubectl top pods -n dsr-production --sort-by=cpu
kubectl top pods -n dsr-production --sort-by=memory

# 2. Scale up if needed
kubectl scale deployment dsr-[SERVICE-NAME] --replicas=5 -n dsr-production

# 3. Check for memory leaks
kubectl exec -it deployment/dsr-[SERVICE-NAME] -n dsr-production -- \
  curl -s http://localhost:8080/actuator/metrics/jvm.memory.used
```

## 🔧 Maintenance Procedures

### Weekly Backup Verification
```bash
# 1. Check backup job status
kubectl get cronjobs -n dsr-production

# 2. Verify latest backup
kubectl logs -l job-name=dsr-database-backup -n dsr-production --tail=50

# 3. Test backup restoration (on staging)
kubectl exec -it postgresql-staging-0 -n dsr-staging -- \
  pg_restore -U dsr_user -d dsr_staging /backup/latest.sql
```

### Security Updates
```bash
# 1. Check for security updates
kubectl get pods -n dsr-production -o jsonpath='{.items[*].spec.containers[*].image}' | \
  xargs -n1 echo | sort -u

# 2. Update container images (rolling update)
kubectl set image deployment/dsr-registration-service \
  registration-service=dsr/registration-service:v1.2.0 -n dsr-production

# 3. Monitor rollout
kubectl rollout status deployment/dsr-registration-service -n dsr-production
```

### Log Rotation and Cleanup
```bash
# 1. Check log disk usage
kubectl exec -it postgresql-0 -n dsr-production -- df -h

# 2. Archive old logs
kubectl exec -it postgresql-0 -n dsr-production -- \
  find /var/log -name "*.log" -mtime +30 -exec gzip {} \;

# 3. Clean up old backups
kubectl exec -it postgresql-0 -n dsr-production -- \
  find /backup -name "*.sql" -mtime +90 -delete
```

## 📈 Performance Optimization

### Database Optimization
```bash
# 1. Analyze slow queries
kubectl exec -it postgresql-0 -n dsr-production -- \
  psql -U dsr_user -d dsr_production -c "SELECT query, mean_time, calls FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 10;"

# 2. Update table statistics
kubectl exec -it postgresql-0 -n dsr-production -- \
  psql -U dsr_user -d dsr_production -c "ANALYZE;"

# 3. Check index usage
kubectl exec -it postgresql-0 -n dsr-production -- \
  psql -U dsr_user -d dsr_production -c "SELECT schemaname, tablename, indexname, idx_scan FROM pg_stat_user_indexes ORDER BY idx_scan;"
```

### Application Performance Tuning
```bash
# 1. Check JVM metrics
kubectl exec -it deployment/dsr-registration-service -n dsr-production -- \
  curl -s http://localhost:8080/actuator/metrics/jvm.gc.pause

# 2. Monitor connection pools
kubectl exec -it deployment/dsr-registration-service -n dsr-production -- \
  curl -s http://localhost:8080/actuator/metrics/hikaricp.connections

# 3. Check cache hit rates
kubectl exec -it deployment/dsr-registration-service -n dsr-production -- \
  curl -s http://localhost:8080/actuator/metrics/cache.gets
```

## 🔍 Monitoring and Alerting

### Key Metrics to Monitor
- **Service Health**: All services responding to health checks
- **Response Times**: API response times < 2 seconds
- **Error Rates**: Error rate < 1%
- **Database Performance**: Query response times < 100ms
- **Resource Usage**: CPU < 70%, Memory < 80%
- **Disk Space**: Database disk usage < 80%

### Alert Escalation
1. **Level 1 (Warning)**: Automated alerts to operations team
2. **Level 2 (Critical)**: Page on-call engineer
3. **Level 3 (Emergency)**: Escalate to technical lead and management

### Grafana Dashboard URLs
- **System Overview**: https://monitoring.dsr.gov.ph/d/system-overview
- **Service Metrics**: https://monitoring.dsr.gov.ph/d/service-metrics
- **Database Performance**: https://monitoring.dsr.gov.ph/d/database-performance

## 📞 Emergency Contacts

### On-Call Rotation
- **Primary**: DevOps Engineer (+63-XXX-XXX-XXXX)
- **Secondary**: Backend Lead (+63-XXX-XXX-XXXX)
- **Escalation**: Technical Director (+63-XXX-XXX-XXXX)

### External Contacts
- **Infrastructure Provider**: support@provider.com
- **Security Team**: security@dsr.gov.ph
- **Database Administrator**: dba@dsr.gov.ph

## 📋 Runbook Checklist

### Daily Tasks
- [ ] Morning health check completed
- [ ] Performance metrics reviewed
- [ ] Alert status checked
- [ ] Backup status verified

### Weekly Tasks
- [ ] Security updates applied
- [ ] Performance optimization reviewed
- [ ] Log cleanup performed
- [ ] Capacity planning updated

### Monthly Tasks
- [ ] Disaster recovery test conducted
- [ ] Security audit completed
- [ ] Performance baseline updated
- [ ] Documentation reviewed

---

**Document Status:** ✅ Active  
**Last Updated:** June 24, 2025  
**Next Review:** July 24, 2025  
**Owner:** DevOps Team
