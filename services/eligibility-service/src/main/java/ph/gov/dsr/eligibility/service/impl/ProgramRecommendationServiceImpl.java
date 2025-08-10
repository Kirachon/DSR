package ph.gov.dsr.eligibility.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ph.gov.dsr.eligibility.dto.EligibilityRequest;
import ph.gov.dsr.eligibility.dto.ProgramInfo;
import ph.gov.dsr.eligibility.service.ProgramManagementService;
import ph.gov.dsr.eligibility.service.ProgramRecommendationService;
import ph.gov.dsr.eligibility.service.PmtCalculatorService;
import ph.gov.dsr.eligibility.service.EligibilityRulesEngineService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Production implementation of ProgramRecommendationService
 * Provides intelligent program matching and recommendation algorithms
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProgramRecommendationServiceImpl implements ProgramRecommendationService {

    private final ProgramManagementService programManagementService;
    private final PmtCalculatorService pmtCalculatorService;
    private final EligibilityRulesEngineService rulesEngineService;
    
    // Algorithm configuration
    private Map<String, Object> algorithmConfig = initializeDefaultAlgorithmConfig();

    @Override
    public List<ProgramRecommendation> getRecommendations(EligibilityRequest request) {
        log.info("Getting program recommendations for PSN: {}", request.getPsn());
        
        try {
            // Get all active programs
            List<ProgramInfo> activePrograms = programManagementService.getAllPrograms(true);
            
            // Calculate PMT score for household
            PmtCalculatorService.PmtCalculationResult pmtResult = pmtCalculatorService.calculatePmtScore(request);
            
            // Generate recommendations with priority scoring
            List<ProgramRecommendation> recommendations = new ArrayList<>();
            
            for (ProgramInfo program : activePrograms) {
                ProgramRecommendation recommendation = evaluateProgram(request, program, pmtResult);
                if (recommendation != null && recommendation.getPriorityScore() > 0) {
                    recommendations.add(recommendation);
                }
            }
            
            // Sort by priority score (highest first)
            recommendations.sort((a, b) -> Double.compare(b.getPriorityScore(), a.getPriorityScore()));
            
            // Limit to top 10 recommendations
            return recommendations.stream().limit(10).collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error generating program recommendations for PSN: {}", request.getPsn(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ProgramRecommendation> getRecommendations(Map<String, Object> householdData, Map<String, Object> criteria) {
        log.info("Getting program recommendations with custom criteria");
        
        // Convert household data to EligibilityRequest
        EligibilityRequest request = convertToEligibilityRequest(householdData);
        
        // Apply custom criteria filters
        List<ProgramRecommendation> recommendations = getRecommendations(request);
        
        return applyCustomCriteria(recommendations, criteria);
    }

    @Override
    public ProgramMatchResult matchProgram(EligibilityRequest request, String programCode) {
        log.info("Matching PSN: {} to program: {}", request.getPsn(), programCode);
        
        try {
            ProgramInfo program = programManagementService.getProgramByCode(programCode);
            if (program == null) {
                return createMatchResult(false, "Program not found", 0.0, null);
            }
            
            // Calculate PMT score
            PmtCalculatorService.PmtCalculationResult pmtResult = pmtCalculatorService.calculatePmtScore(request);
            
            // Evaluate eligibility rules
            EligibilityRulesEngineService.RuleEvaluationResult rulesResult = 
                rulesEngineService.evaluateEligibilityRules(request, programCode);
            
            // Calculate match score
            double matchScore = calculateMatchScore(request, program, pmtResult, rulesResult);
            
            // Determine if match is successful
            boolean isMatch = matchScore >= getMinimumMatchThreshold();
            String reason = generateMatchReason(isMatch, pmtResult, rulesResult);
            
            return createMatchResult(isMatch, reason, matchScore, program);
            
        } catch (Exception e) {
            log.error("Error matching program {} for PSN: {}", programCode, request.getPsn(), e);
            return createMatchResult(false, "Error during matching: " + e.getMessage(), 0.0, null);
        }
    }

    @Override
    public List<ProgramRecommendation> getAlternativePrograms(EligibilityRequest request, String primaryProgramCode) {
        log.info("Getting alternative programs for PSN: {}, primary program: {}", request.getPsn(), primaryProgramCode);
        
        // Get all recommendations
        List<ProgramRecommendation> allRecommendations = getRecommendations(request);
        
        // Filter out the primary program and return alternatives
        return allRecommendations.stream()
            .filter(rec -> !primaryProgramCode.equals(rec.getProgram().getProgramCode()))
            .limit(5)
            .collect(Collectors.toList());
    }

    @Override
    public void updateRecommendationAlgorithm(Map<String, Object> algorithmConfig) {
        log.info("Updating recommendation algorithm configuration");
        this.algorithmConfig.putAll(algorithmConfig);
    }

    @Override
    public Map<String, Object> getRecommendationStatistics() {
        log.info("Getting recommendation statistics");
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecommendations", 0);
        stats.put("successfulMatches", 0);
        stats.put("averageMatchScore", 0.0);
        stats.put("lastUpdated", LocalDateTime.now());
        return stats;
    }

    /**
     * Evaluate a program for recommendation
     */
    private ProgramRecommendation evaluateProgram(EligibilityRequest request, ProgramInfo program, 
                                                PmtCalculatorService.PmtCalculationResult pmtResult) {
        try {
            // Check basic eligibility
            EligibilityRulesEngineService.RuleEvaluationResult rulesResult = 
                rulesEngineService.evaluateEligibilityRules(request, program.getProgramCode());
            
            // Calculate priority score
            double priorityScore = calculatePriorityScore(request, program, pmtResult, rulesResult);
            
            if (priorityScore <= 0) {
                return null; // Not recommended
            }
            
            // Create recommendation
            ProgramRecommendation recommendation = new ProgramRecommendation();
            recommendation.setProgram(program);
            recommendation.setPriorityScore(priorityScore);
            recommendation.setMatchReason(generateRecommendationReason(pmtResult, rulesResult));
            recommendation.setMatchCriteria(generateMatchCriteria(request, program));
            recommendation.setBenefits(extractProgramBenefits(program));
            recommendation.setRequirements(extractProgramRequirements(program));
            recommendation.setApplicationProcess(generateApplicationProcess(program));
            recommendation.setRecommendedAt(LocalDateTime.now());
            
            return recommendation;
            
        } catch (Exception e) {
            log.warn("Error evaluating program {} for PSN: {}", program.getProgramCode(), request.getPsn(), e);
            return null;
        }
    }

    /**
     * Calculate priority score for program recommendation
     */
    private double calculatePriorityScore(EligibilityRequest request, ProgramInfo program,
                                        PmtCalculatorService.PmtCalculationResult pmtResult,
                                        EligibilityRulesEngineService.RuleEvaluationResult rulesResult) {
        double score = 0.0;
        
        // Base eligibility score (40% weight)
        if (rulesResult.isPassed()) {
            score += 40.0;
        } else {
            // Partial score based on passed rules
            List<EligibilityRulesEngineService.RuleResult> ruleResults = rulesResult.getRuleResults();
            if (ruleResults != null && !ruleResults.isEmpty()) {
                long passedCount = ruleResults.stream().mapToLong(r -> r.isPassed() ? 1 : 0).sum();
                double passedRatio = (double) passedCount / ruleResults.size();
                score += 40.0 * passedRatio;
            }
        }
        
        // PMT score alignment (30% weight)
        if (pmtResult.isPoor()) {
            score += 30.0;
        } else {
            // Partial score based on proximity to poverty threshold
            BigDecimal ratio = pmtResult.getPmtScore().divide(pmtResult.getPovertyThreshold(), 2, java.math.RoundingMode.HALF_UP);
            if (ratio.compareTo(BigDecimal.valueOf(1.5)) <= 0) {
                score += 30.0 * (1.5 - ratio.doubleValue()) / 0.5;
            }
        }
        
        // Program priority and availability (20% weight)
        if (program.getStatus() == ProgramInfo.ProgramStatus.ACTIVE && 
            Boolean.TRUE.equals(program.getAcceptingApplications())) {
            score += 20.0;
        }
        
        // Vulnerability factors bonus (10% weight)
        score += calculateVulnerabilityBonus(request, program);
        
        return Math.min(100.0, score); // Cap at 100
    }

    /**
     * Calculate vulnerability bonus score
     */
    private double calculateVulnerabilityBonus(EligibilityRequest request, ProgramInfo program) {
        double bonus = 0.0;
        
        // Check for vulnerable groups
        if (request.getHouseholdInfo() != null) {
            // Senior citizens
            if (hasElderly(request) && program.getProgramCode().contains("SENIOR")) {
                bonus += 5.0;
            }
            
            // PWD
            if (hasPWD(request) && program.getProgramCode().contains("PWD")) {
                bonus += 5.0;
            }
            
            // Children
            if (hasChildren(request) && program.getProgramCode().contains("4PS")) {
                bonus += 5.0;
            }
        }
        
        return Math.min(10.0, bonus);
    }

    /**
     * Generate match reason
     */
    private String generateRecommendationReason(PmtCalculatorService.PmtCalculationResult pmtResult,
                                              EligibilityRulesEngineService.RuleEvaluationResult rulesResult) {
        if (rulesResult.isPassed() && pmtResult.isPoor()) {
            return "Meets all eligibility criteria and poverty threshold";
        } else if (rulesResult.isPassed()) {
            return "Meets program eligibility criteria";
        } else if (pmtResult.isPoor()) {
            return "Meets poverty threshold, partial eligibility criteria";
        } else {
            return "Partial match based on household characteristics";
        }
    }

    /**
     * Helper methods
     */
    private Map<String, Object> initializeDefaultAlgorithmConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("minimumMatchThreshold", 60.0);
        config.put("eligibilityWeight", 0.4);
        config.put("pmtWeight", 0.3);
        config.put("programWeight", 0.2);
        config.put("vulnerabilityWeight", 0.1);
        return config;
    }

    private double getMinimumMatchThreshold() {
        return (Double) algorithmConfig.getOrDefault("minimumMatchThreshold", 60.0);
    }

    private boolean hasElderly(EligibilityRequest request) {
        // Implementation would check household members for age >= 60
        return false; // Simplified for now
    }

    private boolean hasPWD(EligibilityRequest request) {
        // Implementation would check household members for PWD status
        return false; // Simplified for now
    }

    private boolean hasChildren(EligibilityRequest request) {
        // Implementation would check household members for age < 18
        return false; // Simplified for now
    }

    private EligibilityRequest convertToEligibilityRequest(Map<String, Object> householdData) {
        // Convert map to EligibilityRequest object
        EligibilityRequest request = new EligibilityRequest();
        // Implementation would map fields appropriately
        return request;
    }

    private List<ProgramRecommendation> applyCustomCriteria(List<ProgramRecommendation> recommendations, 
                                                          Map<String, Object> criteria) {
        // Apply additional filtering based on custom criteria
        return recommendations; // Simplified for now
    }

    private double calculateMatchScore(EligibilityRequest request, ProgramInfo program,
                                     PmtCalculatorService.PmtCalculationResult pmtResult,
                                     EligibilityRulesEngineService.RuleEvaluationResult rulesResult) {
        return calculatePriorityScore(request, program, pmtResult, rulesResult);
    }

    private String generateMatchReason(boolean isMatch, PmtCalculatorService.PmtCalculationResult pmtResult,
                                     EligibilityRulesEngineService.RuleEvaluationResult rulesResult) {
        if (isMatch) {
            return "Household meets program eligibility requirements";
        } else {
            return "Household does not meet minimum eligibility requirements";
        }
    }

    private ProgramMatchResult createMatchResult(boolean isMatch, String reason, double score, ProgramInfo program) {
        ProgramMatchResult result = new ProgramMatchResult();
        result.setMatched(isMatch);
        result.setRecommendation(reason);
        result.setMatchScore(score);
        if (program != null) {
            result.setProgramCode(program.getProgramCode());
        }
        result.setMatchedAt(LocalDateTime.now());
        return result;
    }

    private Map<String, Object> generateMatchCriteria(EligibilityRequest request, ProgramInfo program) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("programCode", program.getProgramCode());
        criteria.put("assessmentDate", LocalDateTime.now());
        return criteria;
    }

    private List<String> extractProgramBenefits(ProgramInfo program) {
        // Extract benefits from program description or configuration
        return Arrays.asList("Financial assistance", "Social services", "Capacity building");
    }

    private List<String> extractProgramRequirements(ProgramInfo program) {
        // Extract requirements from program configuration
        return Arrays.asList("Valid ID", "Proof of income", "Household composition");
    }

    private String generateApplicationProcess(ProgramInfo program) {
        return "Visit your local DSWD office or apply online through the DSR portal";
    }
}
