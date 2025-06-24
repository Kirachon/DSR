package ph.gov.dsr.payment.performance;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for load testing
 */
@TestConfiguration
@Profile("performance")
public class LoadTestConfiguration {

    @Bean("loadTestExecutor")
    public Executor loadTestExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("LoadTest-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    @Bean("batchProcessingExecutor")
    public Executor batchProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("BatchProcessing-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.initialize();
        return executor;
    }

    /**
     * Performance test metrics collector
     */
    public static class PerformanceMetrics {
        private long totalRequests = 0;
        private long successfulRequests = 0;
        private long failedRequests = 0;
        private long totalResponseTime = 0;
        private long minResponseTime = Long.MAX_VALUE;
        private long maxResponseTime = 0;
        private final Object lock = new Object();

        public void recordRequest(long responseTime, boolean success) {
            synchronized (lock) {
                totalRequests++;
                totalResponseTime += responseTime;
                
                if (success) {
                    successfulRequests++;
                } else {
                    failedRequests++;
                }
                
                minResponseTime = Math.min(minResponseTime, responseTime);
                maxResponseTime = Math.max(maxResponseTime, responseTime);
            }
        }

        public double getAverageResponseTime() {
            return totalRequests > 0 ? (double) totalResponseTime / totalRequests : 0;
        }

        public double getSuccessRate() {
            return totalRequests > 0 ? (double) successfulRequests / totalRequests * 100 : 0;
        }

        public long getTotalRequests() { return totalRequests; }
        public long getSuccessfulRequests() { return successfulRequests; }
        public long getFailedRequests() { return failedRequests; }
        public long getMinResponseTime() { return minResponseTime == Long.MAX_VALUE ? 0 : minResponseTime; }
        public long getMaxResponseTime() { return maxResponseTime; }

        public void reset() {
            synchronized (lock) {
                totalRequests = 0;
                successfulRequests = 0;
                failedRequests = 0;
                totalResponseTime = 0;
                minResponseTime = Long.MAX_VALUE;
                maxResponseTime = 0;
            }
        }

        @Override
        public String toString() {
            return String.format(
                "PerformanceMetrics{" +
                "totalRequests=%d, " +
                "successfulRequests=%d, " +
                "failedRequests=%d, " +
                "avgResponseTime=%.2fms, " +
                "minResponseTime=%dms, " +
                "maxResponseTime=%dms, " +
                "successRate=%.2f%%}",
                totalRequests, successfulRequests, failedRequests,
                getAverageResponseTime(), getMinResponseTime(), getMaxResponseTime(),
                getSuccessRate()
            );
        }
    }

    /**
     * Load test scenario configuration
     */
    public static class LoadTestScenario {
        private final String name;
        private final int concurrentUsers;
        private final int requestsPerUser;
        private final long rampUpTimeMs;
        private final long thinkTimeMs;

        public LoadTestScenario(String name, int concurrentUsers, int requestsPerUser, 
                               long rampUpTimeMs, long thinkTimeMs) {
            this.name = name;
            this.concurrentUsers = concurrentUsers;
            this.requestsPerUser = requestsPerUser;
            this.rampUpTimeMs = rampUpTimeMs;
            this.thinkTimeMs = thinkTimeMs;
        }

        // Getters
        public String getName() { return name; }
        public int getConcurrentUsers() { return concurrentUsers; }
        public int getRequestsPerUser() { return requestsPerUser; }
        public long getRampUpTimeMs() { return rampUpTimeMs; }
        public long getThinkTimeMs() { return thinkTimeMs; }

        // Predefined scenarios
        public static LoadTestScenario lightLoad() {
            return new LoadTestScenario("Light Load", 5, 10, 5000, 1000);
        }

        public static LoadTestScenario moderateLoad() {
            return new LoadTestScenario("Moderate Load", 20, 25, 10000, 500);
        }

        public static LoadTestScenario heavyLoad() {
            return new LoadTestScenario("Heavy Load", 50, 50, 20000, 100);
        }

        public static LoadTestScenario stressTest() {
            return new LoadTestScenario("Stress Test", 100, 100, 30000, 50);
        }
    }

    /**
     * Performance thresholds for different operations
     */
    public static class PerformanceThresholds {
        // Response time thresholds (in milliseconds)
        public static final long PAYMENT_CREATION_THRESHOLD = 500;
        public static final long PAYMENT_PROCESSING_THRESHOLD = 1000;
        public static final long BATCH_CREATION_THRESHOLD = 2000;
        public static final long BATCH_PROCESSING_THRESHOLD = 5000;
        public static final long STATISTICS_GENERATION_THRESHOLD = 3000;
        public static final long DATABASE_QUERY_THRESHOLD = 1000;

        // Throughput thresholds (requests per second)
        public static final double MIN_PAYMENT_CREATION_TPS = 10.0;
        public static final double MIN_PAYMENT_PROCESSING_TPS = 5.0;
        public static final double MIN_BATCH_CREATION_TPS = 2.0;

        // Success rate threshold (percentage)
        public static final double MIN_SUCCESS_RATE = 95.0;

        // Memory usage thresholds
        public static final long MAX_MEMORY_INCREASE_MB = 200;
        public static final long MAX_MEMORY_PER_PAYMENT_KB = 10;

        // Database connection thresholds
        public static final int MAX_DB_CONNECTIONS = 20;
        public static final long MAX_DB_CONNECTION_WAIT_MS = 5000;
    }

    /**
     * Test data generator for performance tests
     */
    public static class TestDataGenerator {
        private static int counter = 0;

        public static String generateHouseholdId() {
            return "HH-PERF-" + System.currentTimeMillis() + "-" + (++counter);
        }

        public static String generateProgramName() {
            String[] programs = {"4Ps", "DSWD-SLP", "KALAHI-CIDSS", "PANTAWID"};
            return programs[counter % programs.length];
        }

        public static String generateBankCode() {
            String[] banks = {"LBP", "BPI", "BDO", "METROBANK", "PNB"};
            return banks[counter % banks.length];
        }

        public static String generateAccountNumber() {
            return String.format("%010d", System.currentTimeMillis() % 10000000000L);
        }

        public static String generateBeneficiaryName() {
            String[] firstNames = {"Juan", "Maria", "Jose", "Ana", "Pedro", "Carmen", "Luis", "Rosa"};
            String[] lastNames = {"Santos", "Cruz", "Reyes", "Garcia", "Mendoza", "Torres", "Flores", "Ramos"};
            return firstNames[counter % firstNames.length] + " " + lastNames[(counter + 1) % lastNames.length];
        }
    }

    /**
     * Performance test utilities
     */
    public static class PerformanceTestUtils {
        
        public static void warmUp(Runnable operation, int iterations) {
            System.out.println("Warming up with " + iterations + " iterations...");
            for (int i = 0; i < iterations; i++) {
                try {
                    operation.run();
                } catch (Exception e) {
                    // Ignore warmup failures
                }
            }
            System.out.println("Warmup completed.");
        }

        public static void printMemoryUsage(String label) {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            long maxMemory = runtime.maxMemory();

            System.out.printf("%s - Memory Usage: Used=%dMB, Free=%dMB, Total=%dMB, Max=%dMB%n",
                label,
                usedMemory / 1024 / 1024,
                freeMemory / 1024 / 1024,
                totalMemory / 1024 / 1024,
                maxMemory / 1024 / 1024
            );
        }

        public static void forceGarbageCollection() {
            System.gc();
            try {
                Thread.sleep(100); // Give GC time to run
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public static double calculateThroughput(long totalRequests, long durationMs) {
            return durationMs > 0 ? (double) totalRequests / (durationMs / 1000.0) : 0;
        }

        public static boolean isWithinThreshold(long actualValue, long threshold, String operation) {
            boolean withinThreshold = actualValue <= threshold;
            if (!withinThreshold) {
                System.err.printf("Performance threshold exceeded for %s: %dms > %dms%n", 
                    operation, actualValue, threshold);
            }
            return withinThreshold;
        }
    }
}
