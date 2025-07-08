package ph.gov.dsr.eligibility.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ph.gov.dsr.eligibility.dto.EligibilityRequest;
import ph.gov.dsr.eligibility.dto.EligibilityResponse;
import ph.gov.dsr.eligibility.service.EligibilityAssessmentService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Mock implementation of EligibilityAssessmentService for no-database mode
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@Profile("no-db")
@Slf4j
public class MockEligibilityAssessmentServiceImpl implements EligibilityAssessmentService {

    // Mock data storage
    private final Map<String, List<EligibilityResponse>> assessmentHistory = new HashMap<>();
    private final Map<String, Map<String, Object>> programStatistics = new HashMap<>();

    // Poverty thresholds by region (mock data)
    private final Map<String, BigDecimal> povertyThresholds = Map.of(
        "NCR", new BigDecimal("12000"),
        "CAR", new BigDecimal("10000"),
        "REGION_I", new BigDecimal("9500"),
        "REGION_II", new BigDecimal("9000"),
        "REGION_III", new BigDecimal("10500"),
        "DEFAULT", new BigDecimal("9000")
    );

    @Override
    public EligibilityResponse assessEligibility(EligibilityRequest request) {
        log.info("Mock assessing eligibility for PSN: {} and program: {}", 
                request.getPsn(), request.getProgramCode());

        EligibilityResponse response = new EligibilityResponse();
        response.setPsn(request.getPsn());
        response.setProgramCode(request.getProgramCode());
        response.setLastAssessmentDate(LocalDateTime.now());

        // Mock assessment logic
        EligibilityResponse.AssessmentDetails details = performMockAssessment(request);
        response.setAssessmentDetails(details);

        // Calculate overall eligibility
        BigDecimal overallScore = calculateOverallScore(details);
        response.setEligibilityScore(overallScore);

        // Determine eligibility status
        EligibilityResponse.EligibilityStatus status = determineEligibilityStatus(overallScore, request.getProgramCode());
        response.setStatus(status);
        response.setIsEligible(status == EligibilityResponse.EligibilityStatus.ELIGIBLE || 
                              status == EligibilityResponse.EligibilityStatus.CONDITIONAL);

        // Set validity period (6 months for most programs)
        response.setValidUntil(LocalDateTime.now().plusMonths(6));

        // Generate reason and recommendations
        response.setReason(generateAssessmentReason(details, status));
        response.setRecommendations(generateRecommendations(details, status));
        response.setConditions(generateConditions(request.getProgramCode(), status));

        // Store in mock history
        storeAssessmentHistory(response);

        log.info("Mock assessment completed. Status: {}, Score: {}", status, overallScore);
        return response;
    }

    @Override
    public Map<String, EligibilityResponse> assessMultiplePrograms(String psn, List<String> programCodes, boolean forceReassessment) {
        log.info("Mock assessing multiple programs for PSN: {}, Programs: {}", psn, programCodes);
        
        Map<String, EligibilityResponse> results = new HashMap<>();
        
        for (String programCode : programCodes) {
            // Create a basic request for each program
            EligibilityRequest request = createBasicRequest(psn, programCode);
            EligibilityResponse response = assessEligibility(request);
            results.put(programCode, response);
        }
        
        return results;
    }

    @Override
    public List<EligibilityResponse> getEligibilityHistory(String psn, String programCode) {
        log.info("Mock getting eligibility history for PSN: {}, Program: {}", psn, programCode);
        
        String key = psn + (programCode != null ? ":" + programCode : "");
        return assessmentHistory.getOrDefault(key, new ArrayList<>());
    }

    @Override
    public boolean isAssessmentValid(String psn, String programCode) {
        log.info("Mock checking assessment validity for PSN: {}, Program: {}", psn, programCode);
        
        List<EligibilityResponse> history = getEligibilityHistory(psn, programCode);
        if (history.isEmpty()) {
            return false;
        }
        
        EligibilityResponse latest = history.get(history.size() - 1);
        return latest.getValidUntil().isAfter(LocalDateTime.now());
    }

    @Override
    public void invalidateAssessment(String psn, String programCode, String reason) {
        log.info("Mock invalidating assessment for PSN: {}, Program: {}, Reason: {}", 
                psn, programCode, reason);
        
        // In mock implementation, we just log the invalidation
        // In real implementation, this would update database records
    }

    @Override
    public Map<String, Object> getEligibilityStatistics(String programCode) {
        log.info("Mock getting eligibility statistics for program: {}", programCode);
        
        return programStatistics.computeIfAbsent(programCode, k -> {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalAssessments", 1500 + new Random().nextInt(500));
            stats.put("eligibleCount", 1200 + new Random().nextInt(200));
            stats.put("ineligibleCount", 200 + new Random().nextInt(100));
            stats.put("conditionalCount", 100 + new Random().nextInt(50));
            stats.put("averageScore", 75.5 + new Random().nextDouble() * 20);
            stats.put("lastUpdated", LocalDateTime.now());
            return stats;
        });
    }

    @Override
    public List<EligibilityResponse> batchAssessEligibility(List<EligibilityRequest> requests) {
        log.info("Mock batch assessing eligibility for {} requests", requests.size());
        
        List<EligibilityResponse> responses = new ArrayList<>();
        for (EligibilityRequest request : requests) {
            responses.add(assessEligibility(request));
        }
        
        return responses;
    }

    @Override
    public EligibilityResponse updateEligibilityStatus(String psn, String programCode, 
                                                      EligibilityResponse.EligibilityStatus status, 
                                                      String reason, String updatedBy) {
        log.info("Mock updating eligibility status for PSN: {}, Program: {}, Status: {}", 
                psn, programCode, status);
        
        // Create a mock updated response
        EligibilityResponse response = new EligibilityResponse();
        response.setPsn(psn);
        response.setProgramCode(programCode);
        response.setStatus(status);
        response.setReason(reason);
        response.setLastAssessmentDate(LocalDateTime.now());
        response.setIsEligible(status == EligibilityResponse.EligibilityStatus.ELIGIBLE);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("updatedBy", updatedBy);
        metadata.put("updateReason", reason);
        metadata.put("updateTimestamp", LocalDateTime.now());
        response.setMetadata(metadata);
        
        return response;
    }

    @Override
    public List<EligibilityResponse> getPendingReviews(String programCode, int limit) {
        log.info("Mock getting pending reviews for program: {}, limit: {}", programCode, limit);
        
        List<EligibilityResponse> pendingReviews = new ArrayList<>();
        
        // Generate mock pending reviews
        for (int i = 0; i < Math.min(limit, 5); i++) {
            EligibilityResponse response = new EligibilityResponse();
            response.setPsn(String.format("1234-5678-%04d", 9000 + i));
            response.setProgramCode(programCode != null ? programCode : "4PS_CONDITIONAL_CASH");
            response.setStatus(EligibilityResponse.EligibilityStatus.UNDER_REVIEW);
            response.setLastAssessmentDate(LocalDateTime.now().minusDays(i + 1));
            response.setReason("Requires manual review due to conflicting information");
            pendingReviews.add(response);
        }
        
        return pendingReviews;
    }

    @Override
    public Map<String, Object> calculateEligibilityScore(EligibilityRequest request) {
        log.info("Mock calculating eligibility score breakdown for PSN: {}", request.getPsn());
        
        EligibilityResponse.AssessmentDetails details = performMockAssessment(request);
        
        Map<String, Object> scoreBreakdown = new HashMap<>();
        scoreBreakdown.put("incomeScore", details.getIncomeAssessment().getScore());
        scoreBreakdown.put("demographicScore", details.getDemographicAssessment().getScore());
        scoreBreakdown.put("geographicScore", details.getGeographicAssessment().getScore());
        scoreBreakdown.put("vulnerabilityScore", details.getVulnerabilityAssessment().getScore());
        scoreBreakdown.put("overallScore", calculateOverallScore(details));
        scoreBreakdown.put("assessmentDetails", details);
        
        return scoreBreakdown;
    }

    // Helper methods for mock assessment logic

    private EligibilityResponse.AssessmentDetails performMockAssessment(EligibilityRequest request) {
        EligibilityResponse.AssessmentDetails details = new EligibilityResponse.AssessmentDetails();
        
        // Income assessment
        details.setIncomeAssessment(performIncomeAssessment(request));
        
        // Demographic assessment
        details.setDemographicAssessment(performDemographicAssessment(request));
        
        // Geographic assessment
        details.setGeographicAssessment(performGeographicAssessment(request));
        
        // Vulnerability assessment
        details.setVulnerabilityAssessment(performVulnerabilityAssessment(request));
        
        // Program-specific criteria
        details.setProgramCriteria(performProgramCriteriaAssessment(request));
        
        details.setSummary("Mock assessment completed successfully");
        
        return details;
    }

    private EligibilityResponse.IncomeAssessment performIncomeAssessment(EligibilityRequest request) {
        EligibilityResponse.IncomeAssessment assessment = new EligibilityResponse.IncomeAssessment();
        
        if (request.getHouseholdInfo() != null) {
            BigDecimal monthlyIncome = request.getHouseholdInfo().getMonthlyIncome();
            Integer totalMembers = request.getHouseholdInfo().getTotalMembers();
            
            assessment.setMonthlyIncome(monthlyIncome);
            
            if (totalMembers != null && totalMembers > 0) {
                BigDecimal perCapitaIncome = monthlyIncome.divide(new BigDecimal(totalMembers), 2, RoundingMode.HALF_UP);
                assessment.setPerCapitaIncome(perCapitaIncome);
            }
            
            // Get poverty threshold for region
            String region = request.getHouseholdInfo().getLocation() != null ? 
                           request.getHouseholdInfo().getLocation().getRegion() : "DEFAULT";
            BigDecimal threshold = povertyThresholds.getOrDefault(region, povertyThresholds.get("DEFAULT"));
            assessment.setPovertyThreshold(threshold);
            
            // Calculate income ratio
            BigDecimal ratio = monthlyIncome.divide(threshold, 2, RoundingMode.HALF_UP);
            assessment.setIncomeRatio(ratio);
            
            // Determine if meets criteria (income below 1.5x poverty line for most programs)
            boolean meetsCriteria = ratio.compareTo(new BigDecimal("1.5")) <= 0;
            assessment.setMeetsIncomeCriteria(meetsCriteria);
            
            // Calculate score (higher score for lower income relative to poverty line)
            BigDecimal score = meetsCriteria ? 
                new BigDecimal("100").subtract(ratio.multiply(new BigDecimal("30"))) :
                new BigDecimal("30");
            assessment.setScore(score.max(BigDecimal.ZERO));
        }
        
        return assessment;
    }

    private EligibilityResponse.DemographicAssessment performDemographicAssessment(EligibilityRequest request) {
        EligibilityResponse.DemographicAssessment assessment = new EligibilityResponse.DemographicAssessment();

        if (request.getMembers() != null) {
            int childrenCount = 0;
            int schoolAgeChildren = 0;
            int pregnantLactatingWomen = 0;
            int seniorCitizens = 0;
            int pwdCount = 0;

            for (EligibilityRequest.HouseholdMemberInfo member : request.getMembers()) {
                if (member.getAge() != null) {
                    if (member.getAge() < 18) {
                        childrenCount++;
                        if (member.getAge() >= 5 && member.getAge() <= 17) {
                            schoolAgeChildren++;
                        }
                    }
                    if (member.getAge() >= 60) {
                        seniorCitizens++;
                    }
                }

                if (Boolean.TRUE.equals(member.getIsPregnant()) || Boolean.TRUE.equals(member.getIsLactating())) {
                    pregnantLactatingWomen++;
                }

                if (Boolean.TRUE.equals(member.getIsPwd())) {
                    pwdCount++;
                }
            }

            assessment.setChildrenCount(childrenCount);
            assessment.setSchoolAgeChildren(schoolAgeChildren);
            assessment.setPregnantLactatingWomen(pregnantLactatingWomen);
            assessment.setSeniorCitizens(seniorCitizens);
            assessment.setPwdCount(pwdCount);

            // For 4Ps, having children is a key criteria
            boolean meetsCriteria = childrenCount > 0 || pregnantLactatingWomen > 0;
            assessment.setMeetsDemographicCriteria(meetsCriteria);

            // Calculate score based on vulnerability indicators
            BigDecimal score = new BigDecimal("50");
            if (childrenCount > 0) score = score.add(new BigDecimal("20"));
            if (schoolAgeChildren > 0) score = score.add(new BigDecimal("15"));
            if (pregnantLactatingWomen > 0) score = score.add(new BigDecimal("10"));
            if (pwdCount > 0) score = score.add(new BigDecimal("5"));

            assessment.setScore(score.min(new BigDecimal("100")));
        }

        return assessment;
    }

    private EligibilityResponse.GeographicAssessment performGeographicAssessment(EligibilityRequest request) {
        EligibilityResponse.GeographicAssessment assessment = new EligibilityResponse.GeographicAssessment();

        if (request.getHouseholdInfo() != null && request.getHouseholdInfo().getLocation() != null) {
            EligibilityRequest.LocationInfo location = request.getHouseholdInfo().getLocation();

            assessment.setLocationType(location.getLocationType());
            assessment.setIsGida(location.getIsGida());

            // Determine region priority (mock logic)
            String regionPriority = "MEDIUM";
            if (Boolean.TRUE.equals(location.getIsGida()) || "RURAL".equals(location.getLocationType())) {
                regionPriority = "HIGH";
            }
            assessment.setRegionPriority(regionPriority);

            // Most programs are available nationwide
            boolean meetsCriteria = true;
            assessment.setMeetsGeographicCriteria(meetsCriteria);

            // Calculate score (higher for rural/GIDA areas)
            BigDecimal score = new BigDecimal("70");
            if ("RURAL".equals(location.getLocationType())) score = score.add(new BigDecimal("15"));
            if (Boolean.TRUE.equals(location.getIsGida())) score = score.add(new BigDecimal("15"));

            assessment.setScore(score.min(new BigDecimal("100")));
        }

        return assessment;
    }

    private EligibilityResponse.VulnerabilityAssessment performVulnerabilityAssessment(EligibilityRequest request) {
        EligibilityResponse.VulnerabilityAssessment assessment = new EligibilityResponse.VulnerabilityAssessment();

        List<String> vulnerabilityFactors = new ArrayList<>();

        if (request.getHouseholdInfo() != null) {
            if (Boolean.TRUE.equals(request.getHouseholdInfo().getIsIndigenous())) {
                vulnerabilityFactors.add("Indigenous Community");
            }
            if (Boolean.TRUE.equals(request.getHouseholdInfo().getHasPwdMembers())) {
                vulnerabilityFactors.add("Has PWD Members");
            }
            if (Boolean.TRUE.equals(request.getHouseholdInfo().getIsSoloParentHousehold())) {
                vulnerabilityFactors.add("Solo Parent Household");
            }
            if (Boolean.TRUE.equals(request.getHouseholdInfo().getHasSeniorCitizens())) {
                vulnerabilityFactors.add("Has Senior Citizens");
            }
        }

        assessment.setVulnerabilityFactors(vulnerabilityFactors);

        // Determine vulnerability level
        String vulnerabilityLevel = "LOW";
        if (vulnerabilityFactors.size() >= 3) {
            vulnerabilityLevel = "HIGH";
        } else if (vulnerabilityFactors.size() >= 1) {
            vulnerabilityLevel = "MEDIUM";
        }
        assessment.setVulnerabilityLevel(vulnerabilityLevel);

        boolean meetsCriteria = !vulnerabilityFactors.isEmpty();
        assessment.setMeetsVulnerabilityCriteria(meetsCriteria);

        // Calculate score based on number of vulnerability factors
        BigDecimal score = new BigDecimal("60").add(new BigDecimal(vulnerabilityFactors.size() * 10));
        assessment.setScore(score.min(new BigDecimal("100")));

        return assessment;
    }

    private List<EligibilityResponse.CriteriaAssessment> performProgramCriteriaAssessment(EligibilityRequest request) {
        List<EligibilityResponse.CriteriaAssessment> criteriaList = new ArrayList<>();

        // Mock program-specific criteria based on program code
        String programCode = request.getProgramCode();

        if ("4PS_CONDITIONAL_CASH".equals(programCode)) {
            // 4Ps specific criteria
            criteriaList.add(createCriteriaAssessment("CHILD_SCHOOL_ATTENDANCE",
                "Children must maintain school attendance", true, 0.3, new BigDecimal("100")));
            criteriaList.add(createCriteriaAssessment("HEALTH_CENTER_VISITS",
                "Regular health center visits required", true, 0.2, new BigDecimal("100")));
            criteriaList.add(createCriteriaAssessment("FAMILY_DEVELOPMENT_SESSIONS",
                "Participation in family development sessions", true, 0.1, new BigDecimal("90")));
        }

        return criteriaList;
    }

    private EligibilityResponse.CriteriaAssessment createCriteriaAssessment(String name, String description,
                                                                           boolean isMet, double weight, BigDecimal score) {
        EligibilityResponse.CriteriaAssessment criteria = new EligibilityResponse.CriteriaAssessment();
        criteria.setCriteriaName(name);
        criteria.setDescription(description);
        criteria.setIsMet(isMet);
        criteria.setWeight(new BigDecimal(weight));
        criteria.setScore(score);
        criteria.setDetails(isMet ? "Criteria satisfied" : "Criteria not met");
        return criteria;
    }

    private BigDecimal calculateOverallScore(EligibilityResponse.AssessmentDetails details) {
        BigDecimal totalScore = BigDecimal.ZERO;
        int componentCount = 0;

        if (details.getIncomeAssessment() != null && details.getIncomeAssessment().getScore() != null) {
            totalScore = totalScore.add(details.getIncomeAssessment().getScore().multiply(new BigDecimal("0.4")));
            componentCount++;
        }

        if (details.getDemographicAssessment() != null && details.getDemographicAssessment().getScore() != null) {
            totalScore = totalScore.add(details.getDemographicAssessment().getScore().multiply(new BigDecimal("0.3")));
            componentCount++;
        }

        if (details.getGeographicAssessment() != null && details.getGeographicAssessment().getScore() != null) {
            totalScore = totalScore.add(details.getGeographicAssessment().getScore().multiply(new BigDecimal("0.15")));
            componentCount++;
        }

        if (details.getVulnerabilityAssessment() != null && details.getVulnerabilityAssessment().getScore() != null) {
            totalScore = totalScore.add(details.getVulnerabilityAssessment().getScore().multiply(new BigDecimal("0.15")));
            componentCount++;
        }

        return componentCount > 0 ? totalScore : BigDecimal.ZERO;
    }

    private EligibilityResponse.EligibilityStatus determineEligibilityStatus(BigDecimal score, String programCode) {
        if (score.compareTo(new BigDecimal("80")) >= 0) {
            return EligibilityResponse.EligibilityStatus.ELIGIBLE;
        } else if (score.compareTo(new BigDecimal("60")) >= 0) {
            return EligibilityResponse.EligibilityStatus.CONDITIONAL;
        } else if (score.compareTo(new BigDecimal("40")) >= 0) {
            return EligibilityResponse.EligibilityStatus.WAITLISTED;
        } else {
            return EligibilityResponse.EligibilityStatus.INELIGIBLE;
        }
    }

    private String generateAssessmentReason(EligibilityResponse.AssessmentDetails details,
                                          EligibilityResponse.EligibilityStatus status) {
        switch (status) {
            case ELIGIBLE:
                return "Household meets all eligibility criteria for the program";
            case CONDITIONAL:
                return "Household meets basic criteria but must fulfill additional conditions";
            case WAITLISTED:
                return "Household is eligible but placed on waiting list due to program capacity";
            case INELIGIBLE:
                return "Household does not meet minimum eligibility requirements";
            default:
                return "Assessment completed with status: " + status;
        }
    }

    private List<String> generateRecommendations(EligibilityResponse.AssessmentDetails details,
                                                EligibilityResponse.EligibilityStatus status) {
        List<String> recommendations = new ArrayList<>();

        switch (status) {
            case ELIGIBLE:
                recommendations.add("Proceed with program enrollment");
                recommendations.add("Ensure all required documents are prepared");
                break;
            case CONDITIONAL:
                recommendations.add("Complete additional requirements to secure eligibility");
                recommendations.add("Schedule follow-up assessment in 3 months");
                break;
            case WAITLISTED:
                recommendations.add("Monitor program capacity for available slots");
                recommendations.add("Consider applying for alternative programs");
                break;
            case INELIGIBLE:
                recommendations.add("Review eligibility criteria and address gaps");
                recommendations.add("Consider reapplying after 6 months if circumstances change");
                break;
            case UNDER_REVIEW:
                recommendations.add("Wait for manual review to complete");
                recommendations.add("Provide additional documentation if requested");
                break;
            case SUSPENDED:
                recommendations.add("Address compliance issues to restore eligibility");
                recommendations.add("Contact program administrator for guidance");
                break;
            case EXPIRED:
                recommendations.add("Submit new application for reassessment");
                recommendations.add("Update household information and documentation");
                break;
            case ERROR:
                recommendations.add("Contact system administrator for technical support");
                recommendations.add("Retry assessment after system issues are resolved");
                break;
        }

        return recommendations;
    }

    private List<String> generateConditions(String programCode, EligibilityResponse.EligibilityStatus status) {
        List<String> conditions = new ArrayList<>();

        if (status == EligibilityResponse.EligibilityStatus.CONDITIONAL ||
            status == EligibilityResponse.EligibilityStatus.ELIGIBLE) {

            if ("4PS_CONDITIONAL_CASH".equals(programCode)) {
                conditions.add("Children must maintain 85% school attendance");
                conditions.add("Regular health center visits for pregnant women and children under 5");
                conditions.add("Participation in family development sessions");
            }
        }

        return conditions;
    }

    private void storeAssessmentHistory(EligibilityResponse response) {
        String key = response.getPsn() + ":" + response.getProgramCode();
        assessmentHistory.computeIfAbsent(key, k -> new ArrayList<>()).add(response);

        // Also store under PSN only for general history
        assessmentHistory.computeIfAbsent(response.getPsn(), k -> new ArrayList<>()).add(response);
    }

    private EligibilityRequest createBasicRequest(String psn, String programCode) {
        EligibilityRequest request = new EligibilityRequest();
        request.setPsn(psn);
        request.setProgramCode(programCode);

        // Create basic household info for assessment
        EligibilityRequest.HouseholdInfo householdInfo = new EligibilityRequest.HouseholdInfo();
        householdInfo.setHouseholdNumber("HH-MOCK-" + psn.replace("-", ""));
        householdInfo.setMonthlyIncome(new BigDecimal("12000")); // Mock income
        householdInfo.setTotalMembers(4); // Mock family size

        request.setHouseholdInfo(householdInfo);

        return request;
    }

    @Override
    public void processLifeEvent(String psn, String eventType, Map<String, Object> eventData) {
        log.info("Processing life event for PSN: {} (mock implementation)", psn);
        // Mock implementation - just log the event
        log.debug("Mock life event processed: type={}, data={}", eventType, eventData);
    }
}
