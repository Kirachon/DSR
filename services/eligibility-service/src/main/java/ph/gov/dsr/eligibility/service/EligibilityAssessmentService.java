package ph.gov.dsr.eligibility.service;

import ph.gov.dsr.eligibility.dto.EligibilityRequest;
import ph.gov.dsr.eligibility.dto.EligibilityResponse;

import java.util.List;
import java.util.Map;

/**
 * Service interface for eligibility assessment operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
public interface EligibilityAssessmentService {

    /**
     * Assess eligibility for a specific program
     * 
     * @param request Eligibility assessment request
     * @return Eligibility assessment response
     */
    EligibilityResponse assessEligibility(EligibilityRequest request);

    /**
     * Assess eligibility for multiple programs
     * 
     * @param psn Philippine Statistical Number
     * @param programCodes List of program codes to assess
     * @param forceReassessment Whether to force reassessment
     * @return Map of program codes to eligibility responses
     */
    Map<String, EligibilityResponse> assessMultiplePrograms(String psn, List<String> programCodes, boolean forceReassessment);

    /**
     * Get eligibility history for a beneficiary
     * 
     * @param psn Philippine Statistical Number
     * @param programCode Program code (optional, null for all programs)
     * @return List of historical eligibility assessments
     */
    List<EligibilityResponse> getEligibilityHistory(String psn, String programCode);

    /**
     * Check if eligibility assessment is still valid
     * 
     * @param psn Philippine Statistical Number
     * @param programCode Program code
     * @return True if assessment is still valid, false otherwise
     */
    boolean isAssessmentValid(String psn, String programCode);

    /**
     * Invalidate eligibility assessment (force reassessment on next request)
     * 
     * @param psn Philippine Statistical Number
     * @param programCode Program code
     * @param reason Reason for invalidation
     */
    void invalidateAssessment(String psn, String programCode, String reason);

    /**
     * Get eligibility statistics for a program
     * 
     * @param programCode Program code
     * @return Statistics about eligibility assessments
     */
    Map<String, Object> getEligibilityStatistics(String programCode);

    /**
     * Batch assess eligibility for multiple beneficiaries
     * 
     * @param requests List of eligibility assessment requests
     * @return List of eligibility assessment responses
     */
    List<EligibilityResponse> batchAssessEligibility(List<EligibilityRequest> requests);

    /**
     * Update eligibility status (for manual overrides)
     * 
     * @param psn Philippine Statistical Number
     * @param programCode Program code
     * @param status New eligibility status
     * @param reason Reason for status change
     * @param updatedBy User who made the change
     * @return Updated eligibility response
     */
    EligibilityResponse updateEligibilityStatus(String psn, String programCode, 
                                              EligibilityResponse.EligibilityStatus status, 
                                              String reason, String updatedBy);

    /**
     * Get pending eligibility reviews
     * 
     * @param programCode Program code (optional)
     * @param limit Maximum number of results
     * @return List of eligibility responses requiring review
     */
    List<EligibilityResponse> getPendingReviews(String programCode, int limit);

    /**
     * Calculate eligibility score for debugging purposes
     * 
     * @param request Eligibility assessment request
     * @return Detailed scoring breakdown
     */
    Map<String, Object> calculateEligibilityScore(EligibilityRequest request);
}
