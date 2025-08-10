package ph.gov.dsr.eligibility.service;

import ph.gov.dsr.eligibility.dto.ProgramInfo;

import java.util.List;
import java.util.Map;

/**
 * Service interface for program management operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
public interface ProgramManagementService {

    /**
     * Get all available programs
     * 
     * @param activeOnly Whether to return only active programs
     * @return List of program information
     */
    List<ProgramInfo> getAllPrograms(boolean activeOnly);

    /**
     * Get program information by code
     * 
     * @param programCode Program code
     * @return Program information
     */
    ProgramInfo getProgramByCode(String programCode);

    /**
     * Get programs by implementing agency
     * 
     * @param agency Implementing agency
     * @param activeOnly Whether to return only active programs
     * @return List of program information
     */
    List<ProgramInfo> getProgramsByAgency(String agency, boolean activeOnly);

    /**
     * Get programs by type
     * 
     * @param programType Program type
     * @param activeOnly Whether to return only active programs
     * @return List of program information
     */
    List<ProgramInfo> getProgramsByType(ProgramInfo.ProgramType programType, boolean activeOnly);

    /**
     * Search programs by criteria
     * 
     * @param searchCriteria Search criteria
     * @return List of matching programs
     */
    List<ProgramInfo> searchPrograms(Map<String, Object> searchCriteria);

    /**
     * Get programs available in a specific geographic area
     * 
     * @param region Region code
     * @param province Province name
     * @param cityMunicipality City/Municipality name
     * @return List of available programs in the area
     */
    List<ProgramInfo> getProgramsByLocation(String region, String province, String cityMunicipality);

    /**
     * Check if a program is currently accepting applications
     * 
     * @param programCode Program code
     * @return True if accepting applications, false otherwise
     */
    boolean isProgramAcceptingApplications(String programCode);

    /**
     * Get program capacity information
     * 
     * @param programCode Program code
     * @return Map containing capacity information (target, current, available)
     */
    Map<String, Object> getProgramCapacity(String programCode);

    /**
     * Get program statistics
     * 
     * @param programCode Program code
     * @return Map containing program statistics
     */
    Map<String, Object> getProgramStatistics(String programCode);

    /**
     * Update program status
     * 
     * @param programCode Program code
     * @param status New program status
     * @param reason Reason for status change
     * @param updatedBy User who made the change
     * @return Updated program information
     */
    ProgramInfo updateProgramStatus(String programCode, ProgramInfo.ProgramStatus status, 
                                   String reason, String updatedBy);

    /**
     * Update program capacity
     * 
     * @param programCode Program code
     * @param newCapacity New target beneficiary capacity
     * @param reason Reason for capacity change
     * @param updatedBy User who made the change
     * @return Updated program information
     */
    ProgramInfo updateProgramCapacity(String programCode, Integer newCapacity, 
                                     String reason, String updatedBy);

    /**
     * Get programs that a beneficiary might be eligible for based on PSN and household data
     *
     * @param psn Personal Social Number
     * @param householdData Household data for eligibility assessment
     * @return List of potentially eligible programs
     */
    List<ProgramInfo> getEligiblePrograms(String psn, Map<String, Object> householdData);

    /**
     * Get programs that a beneficiary might be eligible for
     *
     * @param householdIncome Monthly household income
     * @param householdSize Number of household members
     * @param location Geographic location information
     * @param vulnerabilityFactors List of vulnerability factors
     * @return List of potentially eligible programs
     */
    List<ProgramInfo> getEligiblePrograms(Double householdIncome, Integer householdSize,
                                         Map<String, String> location, List<String> vulnerabilityFactors);

    /**
     * Search programs by search term
     *
     * @param searchTerm Search term to match against program names and descriptions
     * @param activeOnly Whether to return only active programs
     * @return List of matching programs
     */
    List<ProgramInfo> searchPrograms(String searchTerm, boolean activeOnly);

    /**
     * Check if a program is currently active
     *
     * @param programCode Program code
     * @return True if program is active, false otherwise
     */
    boolean isProgramActive(String programCode);

    /**
     * Get program budget allocation
     *
     * @param programCode Program code
     * @return Budget allocation amount
     */
    java.math.BigDecimal getProgramBudget(String programCode);

    /**
     * Get program target beneficiary categories
     *
     * @param programCode Program code
     * @return List of target beneficiary categories
     */
    List<String> getProgramBeneficiaryCategories(String programCode);

    /**
     * Get program eligibility criteria
     *
     * @param programCode Program code
     * @return Map containing eligibility criteria
     */
    Map<String, Object> getProgramEligibilityCriteria(String programCode);

    /**
     * Get program enrollment trends
     * 
     * @param programCode Program code
     * @param months Number of months to look back
     * @return Map containing enrollment trend data
     */
    Map<String, Object> getProgramEnrollmentTrends(String programCode, int months);

    /**
     * Validate program configuration
     * 
     * @param programCode Program code
     * @return Map containing validation results
     */
    Map<String, Object> validateProgramConfiguration(String programCode);
}
