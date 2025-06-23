package ph.gov.dsr.eligibility.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.eligibility.client.DataManagementServiceClient;
import ph.gov.dsr.eligibility.dto.EligibilityRequest;
import ph.gov.dsr.eligibility.dto.EligibilityResponse;
import ph.gov.dsr.eligibility.entity.EligibilityAssessment;
import ph.gov.dsr.eligibility.repository.EligibilityAssessmentRepository;
import ph.gov.dsr.eligibility.service.EligibilityAssessmentService;
import ph.gov.dsr.eligibility.service.EligibilityRulesEngineService;
import ph.gov.dsr.eligibility.service.PmtCalculatorService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Production implementation of EligibilityAssessmentService with database persistence
 * and service integration for real business workflows
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@ConditionalOnProperty(name = "features.database-persistence", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class ProductionEligibilityAssessmentServiceImpl implements EligibilityAssessmentService {

    private final PmtCalculatorService pmtCalculatorService;
    private final EligibilityRulesEngineService rulesEngineService;
    private final EligibilityAssessmentRepository assessmentRepository;
    private final DataManagementServiceClient dataManagementClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public EligibilityResponse assessEligibility(EligibilityRequest request) {
        log.info("Assessing eligibility for PSN: {} and program: {}", 
                request.getPsn(), request.getProgramCode());
        
        try {
            // Check for existing valid assessment unless force reassessment
            if (!Boolean.TRUE.equals(request.getForceReassessment())) {
                Optional<EligibilityAssessment> existingAssessment = 
                    assessmentRepository.findLatestByPsnAndProgramCode(request.getPsn(), request.getProgramCode());
                
                if (existingAssessment.isPresent() && existingAssessment.get().isValid()) {
                    log.info("Returning existing valid assessment for PSN: {}", request.getPsn());
                    return convertToResponse(existingAssessment.get());
                }
            }

            // Enrich request with household data from Data Management Service
            EligibilityRequest enrichedRequest = enrichRequestWithHouseholdData(request);

            // Step 1: Calculate PMT score
            PmtCalculatorService.PmtCalculationResult pmtResult = 
                pmtCalculatorService.calculatePmtScore(enrichedRequest);

            // Step 2: Evaluate categorical eligibility rules
            EligibilityRulesEngineService.RuleEvaluationResult rulesResult = 
                rulesEngineService.evaluateEligibilityRules(enrichedRequest, request.getProgramCode());

            // Step 3: Determine overall eligibility
            EligibilityResponse.EligibilityStatus status = determineEligibilityStatus(pmtResult, rulesResult);

            // Step 4: Calculate eligibility score
            BigDecimal eligibilityScore = calculateOverallScore(pmtResult, rulesResult);

            // Step 5: Create and persist assessment
            EligibilityAssessment assessment = createAssessment(enrichedRequest, pmtResult, rulesResult, status, eligibilityScore);
            assessment = assessmentRepository.save(assessment);

            // Step 6: Convert to response
            EligibilityResponse response = convertToResponse(assessment);
            
            log.info("Eligibility assessment completed for PSN: {}. Status: {}, Score: {}", 
                    request.getPsn(), status, eligibilityScore);
            
            return response;

        } catch (Exception e) {
            log.error("Error assessing eligibility for PSN: {}", request.getPsn(), e);
            return createErrorResponse(request, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EligibilityResponse> getEligibilityHistory(String psn, String programCode) {
        log.info("Getting eligibility history for PSN: {} and program: {}", psn, programCode);
        
        List<EligibilityAssessment> assessments;
        if (programCode != null) {
            assessments = assessmentRepository.findByPsnAndProgramCodeOrderByAssessmentDateDesc(psn, programCode);
        } else {
            assessments = assessmentRepository.findByPsnOrderByAssessmentDateDesc(psn);
        }
        
        return assessments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAssessmentValid(String psn, String programCode) {
        return assessmentRepository.hasValidEligibleAssessment(psn, programCode, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void invalidateAssessment(String psn, String programCode, String reason) {
        log.info("Invalidating assessment for PSN: {}, program: {}, reason: {}", psn, programCode, reason);
        
        Optional<EligibilityAssessment> assessment = 
            assessmentRepository.findLatestByPsnAndProgramCode(psn, programCode);
        
        if (assessment.isPresent()) {
            EligibilityAssessment entity = assessment.get();
            entity.setStatus(EligibilityAssessment.EligibilityStatus.EXPIRED);
            entity.setNotes(reason);
            entity.setUpdatedBy(getCurrentUser());
            assessmentRepository.save(entity);
        }
    }

    @Override
    @Transactional
    public EligibilityResponse updateEligibilityStatus(String psn, String programCode, 
                                                     EligibilityResponse.EligibilityStatus status, 
                                                     String reason, String updatedBy) {
        log.info("Updating eligibility status for PSN: {}, program: {}, new status: {}", psn, programCode, status);
        
        Optional<EligibilityAssessment> assessmentOpt = 
            assessmentRepository.findLatestByPsnAndProgramCode(psn, programCode);
        
        if (assessmentOpt.isPresent()) {
            EligibilityAssessment assessment = assessmentOpt.get();
            assessment.setStatus(convertToEntityStatus(status));
            assessment.setReason(reason);
            assessment.setUpdatedBy(updatedBy);
            assessment = assessmentRepository.save(assessment);
            
            return convertToResponse(assessment);
        } else {
            throw new RuntimeException("No assessment found for PSN: " + psn + " and program: " + programCode);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getEligibilityStatistics(String programCode) {
        log.info("Getting eligibility statistics for program: {}", programCode);
        
        List<EligibilityAssessment> assessments;
        if (programCode != null) {
            assessments = assessmentRepository.findByProgramCodeOrderByAssessmentDateDesc(programCode);
        } else {
            assessments = assessmentRepository.findAll();
        }
        
        long totalAssessments = assessments.size();
        long eligibleCount = assessments.stream()
                .filter(a -> a.getStatus() == EligibilityAssessment.EligibilityStatus.ELIGIBLE)
                .count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAssessments", totalAssessments);
        stats.put("eligibleCount", eligibleCount);
        stats.put("ineligibleCount", totalAssessments - eligibleCount);
        stats.put("eligibilityRate", totalAssessments > 0 ? (double) eligibleCount / totalAssessments : 0.0);
        stats.put("generatedAt", LocalDateTime.now());
        
        return stats;
    }

    @Override
    @Transactional
    public List<EligibilityResponse> batchAssessEligibility(List<EligibilityRequest> requests) {
        log.info("Processing batch eligibility assessment for {} requests", requests.size());
        
        return requests.stream()
                .map(this::assessEligibility)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EligibilityResponse> getPendingReviews(String programCode, int limit) {
        log.info("Getting pending reviews for program: {}, limit: {}", programCode, limit);
        
        List<EligibilityAssessment> assessments = assessmentRepository.findAssessmentsNeedingReview();
        
        return assessments.stream()
                .filter(a -> programCode == null || programCode.equals(a.getProgramCode()))
                .limit(limit)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> calculateEligibilityScore(EligibilityRequest request) {
        log.info("Calculating detailed eligibility score for PSN: {}", request.getPsn());
        
        Map<String, Object> scoreBreakdown = new HashMap<>();
        
        // Enrich request with household data
        EligibilityRequest enrichedRequest = enrichRequestWithHouseholdData(request);
        
        // PMT score breakdown
        Map<String, Object> pmtBreakdown = pmtCalculatorService.getPmtScoreBreakdown(enrichedRequest);
        scoreBreakdown.put("pmtCalculation", pmtBreakdown);
        
        // Rules evaluation breakdown
        EligibilityRulesEngineService.RuleEvaluationResult rulesResult = 
            rulesEngineService.evaluateEligibilityRules(enrichedRequest, request.getProgramCode());
        scoreBreakdown.put("rulesEvaluation", rulesResult);
        
        // Overall score calculation
        PmtCalculatorService.PmtCalculationResult pmtResult = pmtCalculatorService.calculatePmtScore(enrichedRequest);
        BigDecimal overallScore = calculateOverallScore(pmtResult, rulesResult);
        
        scoreBreakdown.put("overallScore", overallScore);
        scoreBreakdown.put("calculatedAt", LocalDateTime.now());
        
        return scoreBreakdown;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, EligibilityResponse> assessMultiplePrograms(String psn, List<String> programCodes, boolean forceReassessment) {
        log.info("Assessing eligibility for PSN: {} across {} programs", psn, programCodes.size());
        
        Map<String, EligibilityResponse> results = new HashMap<>();
        
        for (String programCode : programCodes) {
            try {
                EligibilityRequest request = new EligibilityRequest();
                request.setPsn(psn);
                request.setProgramCode(programCode);
                request.setForceReassessment(forceReassessment);
                
                EligibilityResponse response = assessEligibility(request);
                results.put(programCode, response);
                
            } catch (Exception e) {
                log.error("Error assessing eligibility for PSN: {} and program: {}", psn, programCode, e);
                EligibilityRequest errorRequest = new EligibilityRequest();
                errorRequest.setPsn(psn);
                errorRequest.setProgramCode(programCode);
                results.put(programCode, createErrorResponse(errorRequest, e));
            }
        }
        
        return results;
    }

    // Helper methods

    private EligibilityRequest enrichRequestWithHouseholdData(EligibilityRequest request) {
        try {
            String authToken = getAuthToken();
            Optional<DataManagementServiceClient.HouseholdData> householdData =
                dataManagementClient.getHouseholdByPsn(request.getPsn(), authToken);

            if (householdData.isPresent()) {
                DataManagementServiceClient.HouseholdData household = householdData.get();

                // Enrich household info
                if (request.getHouseholdInfo() == null) {
                    request.setHouseholdInfo(new EligibilityRequest.HouseholdInfo());
                }

                EligibilityRequest.HouseholdInfo householdInfo = request.getHouseholdInfo();
                householdInfo.setHouseholdNumber(household.getHouseholdNumber());
                householdInfo.setMonthlyIncome(household.getMonthlyIncome());
                householdInfo.setTotalMembers(household.getTotalMembers());

                // Set location info
                if (householdInfo.getLocation() == null) {
                    householdInfo.setLocation(new EligibilityRequest.LocationInfo());
                }
                EligibilityRequest.LocationInfo location = householdInfo.getLocation();
                location.setRegion(household.getRegion());
                location.setProvince(household.getProvince());
                location.setCityMunicipality(household.getMunicipality());
                location.setBarangay(household.getBarangay());

                // Set vulnerability indicators using existing boolean fields
                householdInfo.setIsIndigenous(household.getIsIndigenous());
                householdInfo.setHasPwdMembers(household.getIsPwdHousehold());
                householdInfo.setHasSeniorCitizens(household.getIsSeniorCitizenHousehold());
                householdInfo.setIsSoloParentHousehold(household.getIsSoloParentHousehold());

                log.debug("Enriched request with household data for PSN: {}", request.getPsn());
            } else {
                log.warn("No household data found for PSN: {}", request.getPsn());
            }
        } catch (Exception e) {
            log.warn("Failed to enrich request with household data for PSN: {}: {}", request.getPsn(), e.getMessage());
        }

        return request;
    }

    private EligibilityAssessment createAssessment(EligibilityRequest request,
                                                  PmtCalculatorService.PmtCalculationResult pmtResult,
                                                  EligibilityRulesEngineService.RuleEvaluationResult rulesResult,
                                                  EligibilityResponse.EligibilityStatus status,
                                                  BigDecimal eligibilityScore) {
        EligibilityAssessment assessment = new EligibilityAssessment();
        assessment.setPsn(request.getPsn());
        assessment.setProgramCode(request.getProgramCode());
        assessment.setAssessmentDate(LocalDateTime.now());
        assessment.setStatus(convertToEntityStatus(status));

        // Set PMT data
        assessment.setPmtScore(pmtResult.getPmtScore());
        assessment.setPovertyThreshold(pmtResult.getPovertyThreshold());
        assessment.setIsPoor(pmtResult.isPoor());

        // Set scores
        assessment.setOverallScore(eligibilityScore);

        // Set validity period (6 months for most programs)
        assessment.setValidUntil(LocalDateTime.now().plusMonths(6));

        // Set assessment metadata
        assessment.setAssessmentMethod("AUTOMATED");
        assessment.setSourceSystem("ELIGIBILITY_SERVICE");
        assessment.setCreatedBy(getCurrentUser());

        // Store calculation details as JSON
        try {
            Map<String, Object> calculationDetails = new HashMap<>();
            calculationDetails.put("pmtResult", pmtResult);
            calculationDetails.put("rulesResult", rulesResult);
            assessment.setCalculationDetails(objectMapper.writeValueAsString(calculationDetails));
        } catch (Exception e) {
            log.warn("Failed to serialize calculation details: {}", e.getMessage());
        }

        // Generate reason and recommendations
        assessment.setReason(generateReason(status, pmtResult, rulesResult));
        assessment.setRecommendations(String.join("; ", generateRecommendations(status, pmtResult, rulesResult)));

        return assessment;
    }

    private EligibilityResponse convertToResponse(EligibilityAssessment assessment) {
        EligibilityResponse response = new EligibilityResponse();
        response.setPsn(assessment.getPsn());
        response.setProgramCode(assessment.getProgramCode());
        response.setStatus(convertToResponseStatus(assessment.getStatus()));
        response.setIsEligible(assessment.isEligible());
        response.setEligibilityScore(assessment.getOverallScore());
        response.setLastAssessmentDate(assessment.getAssessmentDate());
        response.setValidUntil(assessment.getValidUntil());
        response.setReason(assessment.getReason());

        if (assessment.getRecommendations() != null) {
            response.setRecommendations(Arrays.asList(assessment.getRecommendations().split("; ")));
        }

        // Add metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("assessmentId", assessment.getId());
        metadata.put("assessmentMethod", assessment.getAssessmentMethod());
        metadata.put("createdBy", assessment.getCreatedBy());
        metadata.put("pmtScore", assessment.getPmtScore());
        metadata.put("povertyThreshold", assessment.getPovertyThreshold());
        metadata.put("isPoor", assessment.getIsPoor());
        response.setMetadata(metadata);

        return response;
    }

    private EligibilityResponse.EligibilityStatus determineEligibilityStatus(
            PmtCalculatorService.PmtCalculationResult pmtResult,
            EligibilityRulesEngineService.RuleEvaluationResult rulesResult) {

        if (pmtResult.isPoor() && rulesResult.isPassed()) {
            return EligibilityResponse.EligibilityStatus.ELIGIBLE;
        } else if (!pmtResult.isPoor() && !rulesResult.isPassed()) {
            return EligibilityResponse.EligibilityStatus.INELIGIBLE;
        } else {
            return EligibilityResponse.EligibilityStatus.CONDITIONAL;
        }
    }

    private BigDecimal calculateOverallScore(PmtCalculatorService.PmtCalculationResult pmtResult,
                                           EligibilityRulesEngineService.RuleEvaluationResult rulesResult) {
        // Normalize PMT score (0-100 scale)
        double pmtWeight = 0.7;
        double rulesWeight = 0.3;

        double normalizedPmtScore = normalizePmtScore(pmtResult.getPmtScore(), pmtResult.getPovertyThreshold());
        double rulesScore = rulesResult.isPassed() ? 100.0 : 0.0;

        double overallScore = (normalizedPmtScore * pmtWeight) + (rulesScore * rulesWeight);
        return new BigDecimal(overallScore).setScale(2, RoundingMode.HALF_UP);
    }

    private double normalizePmtScore(BigDecimal pmtScore, BigDecimal threshold) {
        if (threshold.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        double ratio = pmtScore.divide(threshold, 4, RoundingMode.HALF_UP).doubleValue();
        return Math.max(0, Math.min(100, 100 - (ratio * 50)));
    }

    private String generateReason(EligibilityResponse.EligibilityStatus status,
                                PmtCalculatorService.PmtCalculationResult pmtResult,
                                EligibilityRulesEngineService.RuleEvaluationResult rulesResult) {
        switch (status) {
            case ELIGIBLE:
                return "Household meets both PMT and categorical eligibility criteria";
            case INELIGIBLE:
                List<String> reasons = new ArrayList<>();
                if (!pmtResult.isPoor()) {
                    reasons.add("PMT score above poverty threshold");
                }
                if (!rulesResult.isPassed()) {
                    reasons.add("Does not meet categorical eligibility criteria");
                }
                return String.join("; ", reasons);
            case CONDITIONAL:
                return "Meets some but not all eligibility criteria - requires manual review";
            default:
                return "Assessment completed with status: " + status;
        }
    }

    private List<String> generateRecommendations(EligibilityResponse.EligibilityStatus status,
                                               PmtCalculatorService.PmtCalculationResult pmtResult,
                                               EligibilityRulesEngineService.RuleEvaluationResult rulesResult) {
        List<String> recommendations = new ArrayList<>();

        if (status == EligibilityResponse.EligibilityStatus.ELIGIBLE) {
            recommendations.add("Proceed with program enrollment");
            recommendations.add("Ensure all required documents are submitted");
        } else if (status == EligibilityResponse.EligibilityStatus.CONDITIONAL) {
            recommendations.add("Schedule manual review");
            recommendations.add("Verify household information");
        } else {
            recommendations.add("Consider alternative programs");
            recommendations.add("Re-assess after household circumstances change");
        }

        return recommendations;
    }

    private EligibilityResponse createErrorResponse(EligibilityRequest request, Exception e) {
        EligibilityResponse errorResponse = new EligibilityResponse();
        errorResponse.setPsn(request.getPsn());
        errorResponse.setProgramCode(request.getProgramCode());
        errorResponse.setStatus(EligibilityResponse.EligibilityStatus.UNDER_REVIEW);
        errorResponse.setIsEligible(false);
        errorResponse.setReason("Assessment failed: " + e.getMessage());
        errorResponse.setLastAssessmentDate(LocalDateTime.now());
        return errorResponse;
    }

    private EligibilityAssessment.EligibilityStatus convertToEntityStatus(EligibilityResponse.EligibilityStatus status) {
        switch (status) {
            case ELIGIBLE: return EligibilityAssessment.EligibilityStatus.ELIGIBLE;
            case INELIGIBLE: return EligibilityAssessment.EligibilityStatus.NOT_ELIGIBLE;
            case CONDITIONAL: return EligibilityAssessment.EligibilityStatus.CONDITIONAL;
            case UNDER_REVIEW: return EligibilityAssessment.EligibilityStatus.UNDER_REVIEW;
            default: return EligibilityAssessment.EligibilityStatus.PENDING_REVIEW;
        }
    }

    private EligibilityResponse.EligibilityStatus convertToResponseStatus(EligibilityAssessment.EligibilityStatus status) {
        switch (status) {
            case ELIGIBLE: return EligibilityResponse.EligibilityStatus.ELIGIBLE;
            case NOT_ELIGIBLE: return EligibilityResponse.EligibilityStatus.INELIGIBLE;
            case CONDITIONAL: return EligibilityResponse.EligibilityStatus.CONDITIONAL;
            case UNDER_REVIEW: return EligibilityResponse.EligibilityStatus.UNDER_REVIEW;
            default: return EligibilityResponse.EligibilityStatus.UNDER_REVIEW;
        }
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }

    private String getAuthToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getCredentials() != null) {
            return "Bearer " + auth.getCredentials().toString();
        }
        return null;
    }
}
