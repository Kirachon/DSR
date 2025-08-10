package ph.gov.dsr.interoperability.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Beneficiary record for program roster generation
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class BeneficiaryRecord {
    
    // Basic identification
    private String psn; // Philippine Statistical Number
    private String firstName;
    private String middleName;
    private String lastName;
    private String suffix;
    private LocalDate birthDate;
    private String gender;
    private String civilStatus;
    
    // Location information
    private String region;
    private String province;
    private String municipality;
    private String barangay;
    private String address;
    private String zipCode;
    
    // Program information
    private String programCode;
    private String programName;
    private String status; // ACTIVE, INACTIVE, SUSPENDED, GRADUATED, etc.
    private LocalDateTime registrationDate;
    private LocalDateTime lastUpdateDate;
    private String enrollmentStatus;
    
    // Contact information
    private String mobileNumber;
    private String emailAddress;
    private String emergencyContact;
    private String emergencyContactNumber;
    
    // Household information
    private Integer householdSize;
    private String householdHead;
    private Double monthlyIncome;
    private String incomeSource;
    private String housingType;
    
    // Vulnerability indicators
    private boolean isPWD;
    private boolean isSeniorCitizen;
    private boolean isIndigenous;
    private boolean isSoloParent;
    private boolean isPregnant;
    private boolean isLactating;
    private String vulnerabilityCategory;
    
    // External system data (enriched)
    private Map<String, Object> philsysData;
    private Map<String, Object> sssData;
    private Map<String, Object> gsisData;
    private Map<String, Object> healthData;
    private Map<String, Object> educationData;
    
    // Program-specific data
    private Map<String, Object> programSpecificData;
    private Map<String, Object> benefitHistory;
    private Map<String, Object> complianceData;
    
    // Data quality indicators
    private Double dataCompletenessScore;
    private LocalDateTime lastVerified;
    private String verificationStatus;
    private String dataSource;
    
    // Helper methods
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        if (firstName != null) fullName.append(firstName);
        if (middleName != null) fullName.append(" ").append(middleName);
        if (lastName != null) fullName.append(" ").append(lastName);
        if (suffix != null) fullName.append(" ").append(suffix);
        return fullName.toString().trim();
    }
    
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        if (this.address != null) address.append(this.address);
        if (barangay != null) address.append(", ").append(barangay);
        if (municipality != null) address.append(", ").append(municipality);
        if (province != null) address.append(", ").append(province);
        if (region != null) address.append(", ").append(region);
        return address.toString().replaceFirst("^, ", "");
    }
    
    public int getAge() {
        if (birthDate == null) return 0;
        return LocalDate.now().getYear() - birthDate.getYear();
    }
    
    public boolean isVulnerable() {
        return isPWD || isSeniorCitizen || isIndigenous || isSoloParent || 
               isPregnant || isLactating;
    }
    
    public boolean hasCompleteBasicInfo() {
        return psn != null && firstName != null && lastName != null && 
               birthDate != null && gender != null;
    }
    
    public boolean hasCompleteLocationInfo() {
        return region != null && province != null && municipality != null && 
               barangay != null;
    }
}
