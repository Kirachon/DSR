#!/bin/bash

# Generate Redis Cluster Configuration Files
# This script creates configuration files for all Redis cluster nodes

set -e

echo "Generating Redis cluster configuration files..."

# Base configuration template
generate_redis_config() {
    local node_type=$1
    local node_number=$2
    local port=$3
    local bus_port=$4
    local container_name=$5
    
    cat > "redis-${node_type}-${node_number}.conf" << EOF
# Redis ${node_type^} ${node_number} Configuration
# Production-optimized Redis configuration for DSR cluster

# Network Configuration
port ${port}
cluster-announce-port ${port}
cluster-announce-bus-port ${bus_port}
bind 0.0.0.0
protected-mode no

# Cluster Configuration
cluster-enabled yes
cluster-config-file nodes-${port}.conf
cluster-node-timeout 15000
cluster-announce-ip ${container_name}

# Memory Configuration
maxmemory 2gb
maxmemory-policy allkeys-lru
maxmemory-samples 5

# Persistence Configuration
save 900 1
save 300 10
save 60 10000
stop-writes-on-bgsave-error yes
rdbcompression yes
rdbchecksum yes
dbfilename dump-${port}.rdb
dir /data

# AOF Configuration
appendonly yes
appendfilename "appendonly-${port}.aof"
appendfsync everysec
no-appendfsync-on-rewrite no
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb
aof-load-truncated yes
aof-use-rdb-preamble yes

# Logging
loglevel notice
logfile /data/redis-${port}.log
syslog-enabled no

# Security
requirepass dsrrediscluster2024
masterauth dsrrediscluster2024

# Performance Tuning
tcp-keepalive 300
tcp-backlog 511
timeout 0
databases 16
hz 10

# Memory Management
hash-max-ziplist-entries 512
hash-max-ziplist-value 64
list-max-ziplist-size -2
list-compress-depth 0
set-max-intset-entries 512
zset-max-ziplist-entries 128
zset-max-ziplist-value 64
hll-sparse-max-bytes 3000

# Slow Log
slowlog-log-slower-than 10000
slowlog-max-len 128

# Latency Monitoring
latency-monitor-threshold 100

# Client Configuration
maxclients 10000

# Replication
replica-serve-stale-data yes
replica-read-only yes
repl-diskless-sync no
repl-diskless-sync-delay 5
repl-ping-replica-period 10
repl-timeout 60
repl-disable-tcp-nodelay no
repl-backlog-size 1mb
repl-backlog-ttl 3600

# Lua Scripting
lua-time-limit 5000

# Event Notification
notify-keyspace-events ""

# Advanced Configuration
client-output-buffer-limit normal 0 0 0
client-output-buffer-limit replica 256mb 64mb 60
client-output-buffer-limit pubsub 32mb 8mb 60

# TLS Configuration (disabled for internal cluster)
tls-port 0

# Cluster Configuration
cluster-require-full-coverage yes
cluster-migration-barrier 1
cluster-allow-reads-when-down no
EOF

    echo "Generated redis-${node_type}-${node_number}.conf"
}

# Generate Sentinel configuration
generate_sentinel_config() {
    local sentinel_number=$1
    local port=$2
    
    cat > "sentinel-${sentinel_number}.conf" << EOF
# Redis Sentinel ${sentinel_number} Configuration
# High availability monitoring for Redis cluster

port ${port}
bind 0.0.0.0
protected-mode no

# Sentinel Configuration
sentinel announce-ip redis-sentinel-${sentinel_number}
sentinel announce-port ${port}

# Monitor Redis Masters
sentinel monitor dsr-master-1 redis-master-1 7001 2
sentinel monitor dsr-master-2 redis-master-2 7002 2
sentinel monitor dsr-master-3 redis-master-3 7003 2

# Authentication
sentinel auth-pass dsr-master-1 dsrrediscluster2024
sentinel auth-pass dsr-master-2 dsrrediscluster2024
sentinel auth-pass dsr-master-3 dsrrediscluster2024

# Timeouts and Thresholds
sentinel down-after-milliseconds dsr-master-1 30000
sentinel down-after-milliseconds dsr-master-2 30000
sentinel down-after-milliseconds dsr-master-3 30000

sentinel parallel-syncs dsr-master-1 1
sentinel parallel-syncs dsr-master-2 1
sentinel parallel-syncs dsr-master-3 1

sentinel failover-timeout dsr-master-1 180000
sentinel failover-timeout dsr-master-2 180000
sentinel failover-timeout dsr-master-3 180000

# Logging
loglevel notice
logfile /data/sentinel-${port}.log

# Security
requirepass dsrsentinel2024

# Notification Scripts
# sentinel notification-script dsr-master-1 /path/to/notify.sh
# sentinel client-reconfig-script dsr-master-1 /path/to/reconfig.sh
EOF

    echo "Generated sentinel-${sentinel_number}.conf"
}

# Generate Redis node configurations
echo "Generating Redis master configurations..."
generate_redis_config "master" "1" "7001" "17001" "redis-master-1"
generate_redis_config "master" "2" "7002" "17002" "redis-master-2"
generate_redis_config "master" "3" "7003" "17003" "redis-master-3"

echo "Generating Redis slave configurations..."
generate_redis_config "slave" "1" "7004" "17004" "redis-slave-1"
generate_redis_config "slave" "2" "7005" "17005" "redis-slave-2"
generate_redis_config "slave" "3" "7006" "17006" "redis-slave-3"

echo "Generating Sentinel configurations..."
generate_sentinel_config "1" "26379"
generate_sentinel_config "2" "26379"
generate_sentinel_config "3" "26379"

# Create cluster management scripts
cat > "start-cluster.sh" << 'EOF'
#!/bin/bash
echo "Starting Redis cluster..."
docker-compose -f redis-cluster.yml up -d
echo "Waiting for cluster initialization..."
sleep 30
echo "Redis cluster started successfully!"
EOF

cat > "stop-cluster.sh" << 'EOF'
#!/bin/bash
echo "Stopping Redis cluster..."
docker-compose -f redis-cluster.yml down
echo "Redis cluster stopped!"
EOF

cat > "cluster-status.sh" << 'EOF'
#!/bin/bash
echo "Redis Cluster Status:"
docker exec dsr-redis-master-1 redis-cli -p 7001 -a dsrrediscluster2024 cluster nodes
echo ""
echo "Cluster Info:"
docker exec dsr-redis-master-1 redis-cli -p 7001 -a dsrrediscluster2024 cluster info
EOF

cat > "cluster-health.sh" << 'EOF'
#!/bin/bash
echo "Checking Redis cluster health..."

for port in 7001 7002 7003 7004 7005 7006; do
    container="dsr-redis-$(if [ $port -le 7003 ]; then echo "master-$((port-7000))"; else echo "slave-$((port-7003))"; fi)"
    echo -n "Node $container ($port): "
    if docker exec $container redis-cli -p $port -a dsrrediscluster2024 ping > /dev/null 2>&1; then
        echo "OK"
    else
        echo "FAILED"
    fi
done

echo ""
echo "Sentinel Status:"
for port in 26379 26380 26381; do
    container="dsr-redis-sentinel-$((port-26378))"
    echo -n "Sentinel $container ($port): "
    if docker exec $container redis-cli -p $port -a dsrsentinel2024 ping > /dev/null 2>&1; then
        echo "OK"
    else
        echo "FAILED"
    fi
done
EOF

# Make scripts executable
chmod +x start-cluster.sh stop-cluster.sh cluster-status.sh cluster-health.sh

echo ""
echo "Redis cluster configuration files generated successfully!"
echo ""
echo "Available scripts:"
echo "  ./start-cluster.sh    - Start the Redis cluster"
echo "  ./stop-cluster.sh     - Stop the Redis cluster"
echo "  ./cluster-status.sh   - Check cluster status"
echo "  ./cluster-health.sh   - Check cluster health"
echo ""
echo "To start the cluster, run: ./start-cluster.sh"
