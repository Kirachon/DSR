package ph.gov.dsr.eligibility.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
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
@Profile("!no-db")
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

            // Fetch household data with retry mechanism
            Optional<DataManagementServiceClient.HouseholdData> householdData =
                fetchHouseholdDataWithRetry(request.getPsn(), authToken);

            if (householdData.isPresent()) {
                // Enrich request with comprehensive household data
                return enrichRequestWithComprehensiveData(request, householdData.get(), authToken);
            } else {
                log.warn("No household data found for PSN: {}, proceeding with basic assessment", request.getPsn());
                return request;
            }
        } catch (Exception e) {
            log.error("Error enriching request with household data for PSN: {}", request.getPsn(), e);
            return request; // Proceed with basic assessment if enrichment fails
        }
    }

    private Optional<DataManagementServiceClient.HouseholdData> fetchHouseholdDataWithRetry(String psn, String authToken) {
        int maxRetries = 3;
        int retryDelay = 1000; // 1 second

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Optional<DataManagementServiceClient.HouseholdData> householdData =
                    dataManagementClient.getHouseholdByPsn(psn, authToken);

                if (householdData.isPresent()) {
                    return householdData;
                }

            } catch (Exception e) {
                log.warn("Attempt {} failed to fetch household data for PSN: {}: {}", attempt, psn, e.getMessage());

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelay * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        return Optional.empty();
    }

    private EligibilityRequest enrichRequestWithComprehensiveData(EligibilityRequest request,
                                                                DataManagementServiceClient.HouseholdData householdData,
                                                                String authToken) {
        try {
            // Create enriched request copy
            EligibilityRequest enrichedRequest = new EligibilityRequest();
            enrichedRequest.setPsn(request.getPsn());
            enrichedRequest.setProgramCode(request.getProgramCode());
            enrichedRequest.setForceReassessment(request.getForceReassessment());

            // Enrich with household data
            if (enrichedRequest.getHouseholdInfo() == null) {
                enrichedRequest.setHouseholdInfo(new EligibilityRequest.HouseholdInfo());
            }

            EligibilityRequest.HouseholdInfo householdInfo = enrichedRequest.getHouseholdInfo();
            householdInfo.setHouseholdNumber(householdData.getHouseholdNumber());
            householdInfo.setTotalMembers(householdData.getTotalMembers());
            householdInfo.setMonthlyIncome(householdData.getMonthlyIncome());
            householdInfo.setIsIndigenous(householdData.getIsIndigenous());
            householdInfo.setHasPwdMembers(householdData.getIsPwdHousehold());
            householdInfo.setHasSeniorCitizens(householdData.getIsSeniorCitizenHousehold());
            householdInfo.setIsSoloParentHousehold(householdData.getIsSoloParentHousehold());

            // Set location information
            if (householdInfo.getLocation() == null) {
                householdInfo.setLocation(new EligibilityRequest.LocationInfo());
            }
            EligibilityRequest.LocationInfo location = householdInfo.getLocation();
            location.setRegion(householdData.getRegion());
            location.setProvince(householdData.getProvince());
            location.setCityMunicipality(householdData.getMunicipality());
            location.setBarangay(householdData.getBarangay());

            // Fetch and enrich with economic profile
            Optional<DataManagementServiceClient.EconomicProfileData> economicProfile =
                dataManagementClient.getEconomicProfile(householdData.getId(), authToken);

            if (economicProfile.isPresent()) {
                enrichRequestWithEconomicData(enrichedRequest, economicProfile.get());
            }

            // Enrich with housing and utility data from household data
            enrichRequestWithHousingData(enrichedRequest, householdData);

            return enrichedRequest;

        } catch (Exception e) {
            log.error("Error creating comprehensive enriched request for PSN: {}", request.getPsn(), e);
            return request; // Return original request if enrichment fails
        }
    }

    private void enrichRequestWithEconomicData(EligibilityRequest request,
                                             DataManagementServiceClient.EconomicProfileData economicProfile) {
        // Enrich household info with economic data
        if (request.getHouseholdInfo() == null) {
            request.setHouseholdInfo(new EligibilityRequest.HouseholdInfo());
        }

        EligibilityRequest.HouseholdInfo householdInfo = request.getHouseholdInfo();

        // Update monthly income if available from economic profile
        if (economicProfile.getTotalHouseholdIncome() != null) {
            householdInfo.setMonthlyIncome(economicProfile.getTotalHouseholdIncome());
        }

        // Add economic indicators to additional parameters
        if (request.getAdditionalParameters() == null) {
            request.setAdditionalParameters(new HashMap<>());
        }

        Map<String, Object> additionalParams = request.getAdditionalParameters();
        additionalParams.put("perCapitaIncome", economicProfile.getPerCapitaIncome());
        additionalParams.put("totalAssetsValue", economicProfile.getTotalAssetsValue());
        additionalParams.put("pmtScore", economicProfile.getPmtScore());
        additionalParams.put("povertyThreshold", economicProfile.getPovertyThreshold());
        additionalParams.put("isPoor", economicProfile.getIsPoor());
        additionalParams.put("hasSalaryIncome", economicProfile.getHasSalaryIncome());
        additionalParams.put("hasBusinessIncome", economicProfile.getHasBusinessIncome());
        additionalParams.put("hasAgriculturalIncome", economicProfile.getHasAgriculturalIncome());
        additionalParams.put("hasRemittanceIncome", economicProfile.getHasRemittanceIncome());
        additionalParams.put("ownsHouse", economicProfile.getOwnsHouse());
        additionalParams.put("ownsLand", economicProfile.getOwnsLand());
        additionalParams.put("ownsVehicle", economicProfile.getOwnsVehicle());
        additionalParams.put("hasSavings", economicProfile.getHasSavings());
    }

    private void enrichRequestWithHousingData(EligibilityRequest request,
                                            DataManagementServiceClient.HouseholdData householdData) {
        if (request.getAdditionalParameters() == null) {
            request.setAdditionalParameters(new HashMap<>());
        }

        Map<String, Object> additionalParams = request.getAdditionalParameters();
        additionalParams.put("housingType", householdData.getHousingType());
        additionalParams.put("housingTenure", householdData.getHousingTenure());
        additionalParams.put("waterSource", householdData.getWaterSource());
        additionalParams.put("toiletFacility", householdData.getToiletFacility());
        additionalParams.put("electricitySource", householdData.getElectricitySource());
        additionalParams.put("cookingFuel", householdData.getCookingFuel());
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

    /**
     * Process life events that may trigger eligibility reassessment
     */
    @Override
    @Transactional
    public void processLifeEvent(String psn, String eventType, Map<String, Object> eventData) {
        log.info("Processing life event for PSN: {}, event type: {}", psn, eventType);

        try {
            // Determine if this life event requires eligibility reassessment
            boolean requiresReassessment = shouldTriggerReassessment(eventType, eventData);

            if (requiresReassessment) {
                // Get all active program enrollments for this PSN
                List<EligibilityAssessment> activeAssessments =
                    assessmentRepository.findActiveProgramsByPsn(psn);

                for (EligibilityAssessment assessment : activeAssessments) {
                    // Create reassessment request
                    EligibilityRequest reassessmentRequest = createReassessmentRequest(psn,
                        assessment.getProgramCode(), eventType, eventData);

                    // Perform reassessment
                    EligibilityResponse reassessmentResult = assessEligibility(reassessmentRequest);

                    // Log reassessment result
                    log.info("Life event reassessment completed for PSN: {}, program: {}, new status: {}",
                            psn, assessment.getProgramCode(), reassessmentResult.getStatus());

                    // Update assessment with life event trigger information
                    updateAssessmentWithLifeEvent(assessment, eventType, eventData, reassessmentResult);
                }
            } else {
                log.debug("Life event {} for PSN {} does not require eligibility reassessment", eventType, psn);
            }

        } catch (Exception e) {
            log.error("Error processing life event for PSN: {}, event type: {}", psn, eventType, e);
            throw new RuntimeException("Failed to process life event", e);
        }
    }

    private boolean shouldTriggerReassessment(String eventType, Map<String, Object> eventData) {
        // Define life events that trigger reassessment
        Set<String> reassessmentTriggers = Set.of(
            "BIRTH", "DEATH", "EMPLOYMENT_CHANGE", "INCOME_CHANGE",
            "ADDRESS_CHANGE", "MARRIAGE", "SEPARATION", "DISABILITY_STATUS_CHANGE",
            "EDUCATION_COMPLETION", "HOUSEHOLD_COMPOSITION_CHANGE"
        );

        return reassessmentTriggers.contains(eventType);
    }

    private EligibilityRequest createReassessmentRequest(String psn, String programCode,
                                                       String eventType, Map<String, Object> eventData) {
        EligibilityRequest request = new EligibilityRequest();
        request.setPsn(psn);
        request.setProgramCode(programCode);
        request.setForceReassessment(true);

        // Add life event context to additional parameters
        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put("lifeEventType", eventType);
        additionalParams.put("lifeEventData", eventData);
        additionalParams.put("reassessmentTrigger", "LIFE_EVENT");
        additionalParams.put("reassessmentDate", LocalDateTime.now());

        request.setAdditionalParameters(additionalParams);

        return request;
    }

    private void updateAssessmentWithLifeEvent(EligibilityAssessment assessment, String eventType,
                                             Map<String, Object> eventData, EligibilityResponse reassessmentResult) {
        // Update assessment with life event information
        Map<String, Object> lifeEventInfo = new HashMap<>();
        lifeEventInfo.put("eventType", eventType);
        lifeEventInfo.put("eventData", eventData);
        lifeEventInfo.put("reassessmentDate", LocalDateTime.now());
        lifeEventInfo.put("previousStatus", assessment.getStatus());
        lifeEventInfo.put("newStatus", reassessmentResult.getStatus());

        // Store life event information in assessment notes or additional data
        String currentNotes = assessment.getNotes() != null ? assessment.getNotes() : "";
        String lifeEventNote = String.format("Life event processed: %s at %s. Status changed from %s to %s.",
                eventType, LocalDateTime.now(), assessment.getStatus(), reassessmentResult.getStatus());

        assessment.setNotes(currentNotes + "\n" + lifeEventNote);
        assessment.setUpdatedAt(LocalDateTime.now());

        assessmentRepository.save(assessment);
    }

    /**
     * Enhanced recommendation generator with program-specific logic
     */
    private List<String> generateAdvancedRecommendations(EligibilityResponse.EligibilityStatus status,
                                                        PmtCalculatorService.PmtCalculationResult pmtResult,
                                                        EligibilityRulesEngineService.RuleEvaluationResult rulesResult,
                                                        String programCode) {
        List<String> recommendations = new ArrayList<>();

        switch (status) {
            case ELIGIBLE:
                recommendations.addAll(generateEligibleRecommendations(programCode, pmtResult));
                break;
            case CONDITIONAL:
                recommendations.addAll(generateConditionalRecommendations(programCode, rulesResult));
                break;
            case INELIGIBLE:
                recommendations.addAll(generateIneligibleRecommendations(programCode, pmtResult, rulesResult));
                break;
            default:
                recommendations.add("Contact your local DSWD office for assistance");
        }

        return recommendations;
    }

    private List<String> generateEligibleRecommendations(String programCode,
                                                       PmtCalculatorService.PmtCalculationResult pmtResult) {
        List<String> recommendations = new ArrayList<>();

        recommendations.add("Proceed with program enrollment immediately");
        recommendations.add("Prepare all required documents for submission");

        if ("4PS_CONDITIONAL_CASH".equals(programCode)) {
            recommendations.add("Ensure children are enrolled in school and attend regularly");
            recommendations.add("Schedule regular health check-ups for pregnant women and children");
            recommendations.add("Participate in Family Development Sessions (FDS)");
        } else if ("SOCIAL_PENSION".equals(programCode)) {
            recommendations.add("Bring valid ID and proof of age for enrollment");
            recommendations.add("Designate an authorized representative if needed");
        }

        return recommendations;
    }

    private List<String> generateConditionalRecommendations(String programCode,
                                                          EligibilityRulesEngineService.RuleEvaluationResult rulesResult) {
        List<String> recommendations = new ArrayList<>();

        recommendations.add("Schedule manual review with DSWD staff");
        recommendations.add("Provide additional documentation to verify eligibility");

        // Add specific recommendations based on failed rules
        if (rulesResult.getRuleResults() != null) {
            for (EligibilityRulesEngineService.RuleResult ruleResult : rulesResult.getRuleResults()) {
                if (!ruleResult.isPassed()) {
                    recommendations.add("Address requirement: " + ruleResult.getFailureMessage());
                }
            }
        }

        return recommendations;
    }

    private List<String> generateIneligibleRecommendations(String programCode,
                                                         PmtCalculatorService.PmtCalculationResult pmtResult,
                                                         EligibilityRulesEngineService.RuleEvaluationResult rulesResult) {
        List<String> recommendations = new ArrayList<>();

        if (!pmtResult.isPoor()) {
            recommendations.add("Household income exceeds poverty threshold for this program");
            recommendations.add("Consider other social protection programs with higher income limits");
        }

        if (!rulesResult.isPassed()) {
            recommendations.add("Review program eligibility criteria and address gaps");
            recommendations.add("Consider reapplying after 6 months if circumstances change");
        }

        recommendations.add("Explore alternative livelihood and employment opportunities");
        recommendations.add("Contact local government units for other available assistance programs");

        return recommendations;
    }
}
