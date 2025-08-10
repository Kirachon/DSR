package ph.gov.dsr.interoperability.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for program roster generation
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class ProgramRosterResponse {
    
    private String rosterId;
    private String programCode;
    private String programName;
    private String region;
    private String province;
    private String municipality;
    private String barangay;
    
    // Generation metadata
    private LocalDateTime generationDate;
    private String generatedBy;
    private String dataSource; // PRODUCTION, MOCK, CACHED
    private String version;
    
    // Beneficiary data
    private List<BeneficiaryRecord> beneficiaries;
    private int totalBeneficiaries;
    private int filteredBeneficiaries;
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    
    // Statistics and analytics
    private Map<String, Object> statistics;
    private Map<String, Long> regionDistribution;
    private Map<String, Long> statusDistribution;
    private Map<String, Long> genderDistribution;
    private Map<String, Long> ageGroupDistribution;
    
    // Data quality indicators
    private DataQualityMetrics dataQuality;
    private List<String> warnings;
    private List<String> errors;
    
    // Export information
    private String downloadUrl;
    private String reportUrl;
    private long fileSizeBytes;
    private String checksum;
    
    // Processing information
    private long processingTimeMs;
    private int systemsQueried;
    private int successfulQueries;
    private int failedQueries;
    
    @Data
    @Builder
    public static class DataQualityMetrics {
        private double completenessScore; // 0-1
        private double accuracyScore; // 0-1
        private double consistencyScore; // 0-1
        private int missingFields;
        private int duplicateRecords;
        private int invalidRecords;
        private LocalDateTime lastValidated;
    }
    
    // Helper methods
    public boolean isSuccessful() {
        return errors == null || errors.isEmpty();
    }
    
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }
    
    public double getDataCompletenessPercentage() {
        if (dataQuality == null) return 0.0;
        return dataQuality.getCompletenessScore() * 100;
    }
    
    public String getGenerationSummary() {
        return String.format("Generated %d beneficiaries for program %s in %dms", 
                           totalBeneficiaries, programCode, processingTimeMs);
    }
}
