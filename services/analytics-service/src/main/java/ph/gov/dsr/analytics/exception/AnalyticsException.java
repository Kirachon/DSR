package ph.gov.dsr.analytics.exception;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Analytics Exception for handling analytics service errors
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Getter
public class AnalyticsException extends RuntimeException {
    
    private final String errorCode;
    private final String errorType;
    private final LocalDateTime timestamp;
    private final Map<String, Object> context;
    private final String component;
    private final String operation;
    
    public AnalyticsException(String message) {
        super(message);
        this.errorCode = "ANALYTICS_ERROR";
        this.errorType = "GENERAL";
        this.timestamp = LocalDateTime.now();
        this.context = null;
        this.component = null;
        this.operation = null;
    }
    
    public AnalyticsException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "ANALYTICS_ERROR";
        this.errorType = "GENERAL";
        this.timestamp = LocalDateTime.now();
        this.context = null;
        this.component = null;
        this.operation = null;
    }
    
    public AnalyticsException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = "GENERAL";
        this.timestamp = LocalDateTime.now();
        this.context = null;
        this.component = null;
        this.operation = null;
    }
    
    public AnalyticsException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorType = "GENERAL";
        this.timestamp = LocalDateTime.now();
        this.context = null;
        this.component = null;
        this.operation = null;
    }
    
    public AnalyticsException(String errorCode, String errorType, String message, String component, String operation) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.timestamp = LocalDateTime.now();
        this.context = null;
        this.component = component;
        this.operation = operation;
    }
    
    public AnalyticsException(String errorCode, String errorType, String message, String component, String operation, Map<String, Object> context) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.timestamp = LocalDateTime.now();
        this.context = context;
        this.component = component;
        this.operation = operation;
    }
    
    public AnalyticsException(String errorCode, String errorType, String message, Throwable cause, String component, String operation, Map<String, Object> context) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.timestamp = LocalDateTime.now();
        this.context = context;
        this.component = component;
        this.operation = operation;
    }
    
    // Common error types
    public static class DataException extends AnalyticsException {
        public DataException(String message) {
            super("DATA_ERROR", "DATA", message, "DataService", "DataProcessing");
        }
        
        public DataException(String message, Throwable cause) {
            super("DATA_ERROR", "DATA", message, cause, "DataService", "DataProcessing", null);
        }
    }
    
    public static class CalculationException extends AnalyticsException {
        public CalculationException(String message) {
            super("CALCULATION_ERROR", "CALCULATION", message, "CalculationService", "MetricCalculation");
        }
        
        public CalculationException(String message, Throwable cause) {
            super("CALCULATION_ERROR", "CALCULATION", message, cause, "CalculationService", "MetricCalculation", null);
        }
    }
    
    public static class DashboardException extends AnalyticsException {
        public DashboardException(String message) {
            super("DASHBOARD_ERROR", "DASHBOARD", message, "DashboardService", "DashboardOperation");
        }
        
        public DashboardException(String message, Throwable cause) {
            super("DASHBOARD_ERROR", "DASHBOARD", message, cause, "DashboardService", "DashboardOperation", null);
        }
    }
    
    public static class ReportException extends AnalyticsException {
        public ReportException(String message) {
            super("REPORT_ERROR", "REPORT", message, "ReportService", "ReportGeneration");
        }
        
        public ReportException(String message, Throwable cause) {
            super("REPORT_ERROR", "REPORT", message, cause, "ReportService", "ReportGeneration", null);
        }
    }
    
    public static class ValidationException extends AnalyticsException {
        public ValidationException(String message) {
            super("VALIDATION_ERROR", "VALIDATION", message, "ValidationService", "DataValidation");
        }
        
        public ValidationException(String message, Map<String, Object> context) {
            super("VALIDATION_ERROR", "VALIDATION", message, "ValidationService", "DataValidation", context);
        }
    }
    
    public static class ConfigurationException extends AnalyticsException {
        public ConfigurationException(String message) {
            super("CONFIG_ERROR", "CONFIGURATION", message, "ConfigurationService", "ConfigurationLoad");
        }
        
        public ConfigurationException(String message, Throwable cause) {
            super("CONFIG_ERROR", "CONFIGURATION", message, cause, "ConfigurationService", "ConfigurationLoad", null);
        }
    }
    
    public static class SecurityException extends AnalyticsException {
        public SecurityException(String message) {
            super("SECURITY_ERROR", "SECURITY", message, "SecurityService", "AccessControl");
        }
        
        public SecurityException(String message, Map<String, Object> context) {
            super("SECURITY_ERROR", "SECURITY", message, "SecurityService", "AccessControl", context);
        }
    }
    
    public static class PerformanceException extends AnalyticsException {
        public PerformanceException(String message) {
            super("PERFORMANCE_ERROR", "PERFORMANCE", message, "PerformanceService", "PerformanceMonitoring");
        }
        
        public PerformanceException(String message, Map<String, Object> context) {
            super("PERFORMANCE_ERROR", "PERFORMANCE", message, "PerformanceService", "PerformanceMonitoring", context);
        }
    }
}
