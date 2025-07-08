package ph.gov.dsr.interoperability.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for program roster generation
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class ProgramRosterRequest {
    
    private String programCode;
    private String region;
    private String province;
    private String municipality;
    private String barangay;
    
    // Filtering options
    private String status; // ACTIVE, INACTIVE, SUSPENDED, etc.
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private List<String> includeFields;
    private List<String> excludeFields;
    
    // Data enrichment options
    private boolean includePhilSysData;
    private boolean includeSSSData;
    private boolean includeGSISData;
    private boolean includeHealthData;
    private boolean includeEducationData;
    
    // Output options
    private String format; // JSON, CSV, EXCEL, PDF
    private String sortBy; // psn, lastName, registrationDate, etc.
    private String sortOrder; // ASC, DESC
    private Integer maxResults;
    private Integer pageSize;
    private Integer pageNumber;
    
    // Security and audit
    private String requestedBy;
    private String purpose;
    private String organization;
    private boolean includePersonalData;
    private boolean includeSensitiveData;
    
    // Additional filters
    private String genderFilter;
    private String ageGroupFilter;
    private String vulnerabilityFilter;
    private String incomeRangeFilter;
    private String householdSizeFilter;
    
    // Export options
    private boolean generateReport;
    private boolean includeStatistics;
    private boolean includeCharts;
    private String reportTemplate;
    
    // Validation
    public boolean isValid() {
        return programCode != null && !programCode.trim().isEmpty() &&
               requestedBy != null && !requestedBy.trim().isEmpty();
    }
    
    public String getValidationError() {
        if (programCode == null || programCode.trim().isEmpty()) {
            return "Program code is required";
        }
        if (requestedBy == null || requestedBy.trim().isEmpty()) {
            return "Requested by is required";
        }
        if (maxResults != null && maxResults > 50000) {
            return "Maximum results cannot exceed 50,000";
        }
        return null;
    }
}
