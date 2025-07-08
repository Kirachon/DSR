package ph.gov.dsr.eligibility.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ph.gov.dsr.eligibility.dto.EligibilityRequest;
import ph.gov.dsr.eligibility.dto.EligibilityResponse;
import ph.gov.dsr.eligibility.service.EligibilityAssessmentService;
import ph.gov.dsr.eligibility.service.PmtCalculatorService;
import ph.gov.dsr.eligibility.service.EligibilityRulesEngineService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Production implementation of EligibilityAssessmentService
 * Integrates PMT calculator and rules engine for comprehensive eligibility assessment
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@Profile("!no-db")
@RequiredArgsConstructor
@Slf4j
public class EligibilityAssessmentServiceImpl implements EligibilityAssessmentService {

    private final PmtCalculatorService pmtCalculatorService;
    private final EligibilityRulesEngineService rulesEngineService;

    // Cache for recent assessments (in production, this would be Redis or database)
    private final Map<String, EligibilityResponse> assessmentCache = new HashMap<>();

    @Override
    public EligibilityResponse assessEligibility(EligibilityRequest request) {
        log.info("Assessing eligibility for PSN: {} and program: {}", 
                request.getPsn(), request.getProgramCode());
        
        try {
            // Check cache first (unless force reassessment)
            String cacheKey = generateCacheKey(request.getPsn(), request.getProgramCode());
            if (!Boolean.TRUE.equals(request.getForceReassessment()) && assessmentCache.containsKey(cacheKey)) {
                EligibilityResponse cached = assessmentCache.get(cacheKey);
                if (isAssessmentValid(request.getPsn(), request.getProgramCode())) {
                    log.info("Returning cached eligibility assessment for PSN: {}", request.getPsn());
                    return cached;
                }
            }

            EligibilityResponse response = new EligibilityResponse();
            response.setPsn(request.getPsn());
            response.setProgramCode(request.getProgramCode());
            response.setLastAssessmentDate(LocalDateTime.now());

            // Step 1: Calculate PMT score
            PmtCalculatorService.PmtCalculationResult pmtResult = pmtCalculatorService.calculatePmtScore(request);

            // Step 2: Evaluate categorical eligibility rules
            EligibilityRulesEngineService.RuleEvaluationResult rulesResult = 
                rulesEngineService.evaluateEligibilityRules(request, request.getProgramCode());

            // Step 3: Determine overall eligibility
            EligibilityResponse.EligibilityStatus status = determineEligibilityStatus(pmtResult, rulesResult);
            response.setStatus(status);
            response.setIsEligible(status == EligibilityResponse.EligibilityStatus.ELIGIBLE);

            // Step 4: Calculate eligibility score
            BigDecimal eligibilityScore = calculateOverallScore(pmtResult, rulesResult);
            response.setEligibilityScore(eligibilityScore);

            // Step 5: Generate assessment details
            EligibilityResponse.AssessmentDetails details = createAssessmentDetails(pmtResult, rulesResult, request);
            response.setAssessmentDetails(details);

            // Step 6: Set reason and recommendations
            response.setReason(generateReason(status, pmtResult, rulesResult));
            response.setRecommendations(generateRecommendations(status, pmtResult, rulesResult));

            // Step 7: Set validity period
            response.setValidUntil(LocalDateTime.now().plusDays(30));

            // Step 8: Cache the result
            assessmentCache.put(cacheKey, response);

            log.info("Eligibility assessment completed for PSN: {}. Status: {}", request.getPsn(), status);
            return response;

        } catch (Exception e) {
            log.error("Error assessing eligibility for PSN: {}", request.getPsn(), e);
            return createErrorResponse(request, e);
        }
    }

    @Override
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

    @Override
    public List<EligibilityResponse> getEligibilityHistory(String psn, String programCode) {
        log.info("Getting eligibility history for PSN: {} and program: {}", psn, programCode);
        
        return assessmentCache.values().stream()
                .filter(response -> psn.equals(response.getPsn()))
                .filter(response -> programCode == null || programCode.equals(response.getProgramCode()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAssessmentValid(String psn, String programCode) {
        String cacheKey = generateCacheKey(psn, programCode);
        if (!assessmentCache.containsKey(cacheKey)) {
            return false;
        }
        
        EligibilityResponse assessment = assessmentCache.get(cacheKey);
        LocalDateTime assessedAt = assessment.getLastAssessmentDate();
        
        // Assessment is valid for 30 days
        return assessedAt != null && assessedAt.isAfter(LocalDateTime.now().minusDays(30));
    }

    @Override
    public void invalidateAssessment(String psn, String programCode, String reason) {
        log.info("Invalidating assessment for PSN: {}, program: {}, reason: {}", psn, programCode, reason);
        
        String cacheKey = generateCacheKey(psn, programCode);
        assessmentCache.remove(cacheKey);
    }

    @Override
    public Map<String, Object> getEligibilityStatistics(String programCode) {
        log.info("Getting eligibility statistics for program: {}", programCode);
        
        List<EligibilityResponse> programAssessments = assessmentCache.values().stream()
                .filter(a -> programCode.equals(a.getProgramCode()))
                .collect(Collectors.toList());
        
        long totalAssessments = programAssessments.size();
        long eligibleCount = programAssessments.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsEligible()))
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
    public List<EligibilityResponse> batchAssessEligibility(List<EligibilityRequest> requests) {
        log.info("Processing batch eligibility assessment for {} requests", requests.size());
        
        return requests.stream()
                .map(this::assessEligibility)
                .collect(Collectors.toList());
    }

    @Override
    public EligibilityResponse updateEligibilityStatus(String psn, String programCode, 
                                                     EligibilityResponse.EligibilityStatus status, 
                                                     String reason, String updatedBy) {
        log.info("Updating eligibility status for PSN: {}, program: {}, new status: {}", psn, programCode, status);
        
        String cacheKey = generateCacheKey(psn, programCode);
        EligibilityResponse response = assessmentCache.getOrDefault(cacheKey, new EligibilityResponse());
        
        response.setPsn(psn);
        response.setProgramCode(programCode);
        response.setStatus(status);
        response.setIsEligible(status == EligibilityResponse.EligibilityStatus.ELIGIBLE);
        response.setReason(reason);
        response.setLastAssessmentDate(LocalDateTime.now());
        
        // Add metadata about the update
        Map<String, Object> metadata = response.getMetadata() != null ? response.getMetadata() : new HashMap<>();
        metadata.put("lastUpdatedBy", updatedBy);
        metadata.put("lastUpdatedAt", LocalDateTime.now());
        metadata.put("updateReason", reason);
        response.setMetadata(metadata);
        
        assessmentCache.put(cacheKey, response);
        return response;
    }

    @Override
    public List<EligibilityResponse> getPendingReviews(String programCode, int limit) {
        log.info("Getting pending reviews for program: {}, limit: {}", programCode, limit);
        
        return assessmentCache.values().stream()
                .filter(response -> programCode == null || programCode.equals(response.getProgramCode()))
                .filter(response -> response.getStatus() == EligibilityResponse.EligibilityStatus.UNDER_REVIEW)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> calculateEligibilityScore(EligibilityRequest request) {
        log.info("Calculating detailed eligibility score for PSN: {}", request.getPsn());
        
        Map<String, Object> scoreBreakdown = new HashMap<>();
        
        // PMT score breakdown
        Map<String, Object> pmtBreakdown = pmtCalculatorService.getPmtScoreBreakdown(request);
        scoreBreakdown.put("pmtCalculation", pmtBreakdown);
        
        // Rules evaluation breakdown
        EligibilityRulesEngineService.RuleEvaluationResult rulesResult = 
            rulesEngineService.evaluateEligibilityRules(request, request.getProgramCode());
        scoreBreakdown.put("rulesEvaluation", rulesResult);
        
        // Overall score calculation
        PmtCalculatorService.PmtCalculationResult pmtResult = pmtCalculatorService.calculatePmtScore(request);
        BigDecimal overallScore = calculateOverallScore(pmtResult, rulesResult);
        
        scoreBreakdown.put("overallScore", overallScore);
        scoreBreakdown.put("calculatedAt", LocalDateTime.now());
        
        return scoreBreakdown;
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

    private String generateCacheKey(String psn, String programCode) {
        return psn + ":" + programCode;
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

    private EligibilityResponse.AssessmentDetails createAssessmentDetails(
            PmtCalculatorService.PmtCalculationResult pmtResult,
            EligibilityRulesEngineService.RuleEvaluationResult rulesResult,
            EligibilityRequest request) {

        EligibilityResponse.AssessmentDetails details = new EligibilityResponse.AssessmentDetails();
        details.setSummary("Comprehensive eligibility assessment completed");

        // Create income assessment
        EligibilityResponse.IncomeAssessment incomeAssessment = new EligibilityResponse.IncomeAssessment();
        if (request.getHouseholdInfo() != null) {
            incomeAssessment.setMonthlyIncome(request.getHouseholdInfo().getMonthlyIncome());
            if (request.getHouseholdInfo().getTotalMembers() != null && request.getHouseholdInfo().getTotalMembers() > 0) {
                BigDecimal perCapita = request.getHouseholdInfo().getMonthlyIncome()
                        .divide(new BigDecimal(request.getHouseholdInfo().getTotalMembers()), 2, RoundingMode.HALF_UP);
                incomeAssessment.setPerCapitaIncome(perCapita);
            }
        }
        incomeAssessment.setPovertyThreshold(pmtResult.getPovertyThreshold());
        incomeAssessment.setMeetsIncomeCriteria(pmtResult.isPoor());
        details.setIncomeAssessment(incomeAssessment);

        return details;
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

    @Override
    public void processLifeEvent(String psn, String eventType, Map<String, Object> eventData) {
        log.info("Processing life event for PSN: {} (in-memory implementation)", psn);
        // In-memory implementation - just log the event
        log.debug("Life event processed: type={}, data={}", eventType, eventData);
    }
}
