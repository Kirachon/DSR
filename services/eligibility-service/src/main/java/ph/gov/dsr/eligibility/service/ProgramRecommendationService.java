package ph.gov.dsr.eligibility.service;

import ph.gov.dsr.eligibility.dto.EligibilityRequest;
import ph.gov.dsr.eligibility.dto.ProgramInfo;

import java.util.List;
import java.util.Map;

/**
 * Service interface for program recommendation and matching
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
public interface ProgramRecommendationService {

    /**
     * Get program recommendations for a household
     * 
     * @param request Eligibility request containing household information
     * @return List of recommended programs with priority scores
     */
    List<ProgramRecommendation> getRecommendations(EligibilityRequest request);

    /**
     * Get program recommendations with custom criteria
     * 
     * @param householdData Household information
     * @param criteria Additional matching criteria
     * @return List of recommended programs
     */
    List<ProgramRecommendation> getRecommendations(Map<String, Object> householdData, Map<String, Object> criteria);

    /**
     * Match household to specific program
     * 
     * @param request Eligibility request
     * @param programCode Program to match against
     * @return Program matching result
     */
    ProgramMatchResult matchProgram(EligibilityRequest request, String programCode);

    /**
     * Get alternative programs if primary program is not available
     * 
     * @param request Eligibility request
     * @param primaryProgramCode Primary program that's not available
     * @return List of alternative programs
     */
    List<ProgramRecommendation> getAlternativePrograms(EligibilityRequest request, String primaryProgramCode);

    /**
     * Update program recommendation algorithms (admin function)
     * 
     * @param algorithmConfig New algorithm configuration
     */
    void updateRecommendationAlgorithm(Map<String, Object> algorithmConfig);

    /**
     * Get recommendation statistics
     * 
     * @return Statistics about program recommendations
     */
    Map<String, Object> getRecommendationStatistics();

    /**
     * Program recommendation with priority score
     */
    class ProgramRecommendation {
        private ProgramInfo program;
        private double priorityScore;
        private String matchReason;
        private Map<String, Object> matchCriteria;
        private List<String> benefits;
        private List<String> requirements;
        private String applicationProcess;
        private java.time.LocalDateTime recommendedAt;

        // Getters and setters
        public ProgramInfo getProgram() { return program; }
        public void setProgram(ProgramInfo program) { this.program = program; }
        
        public double getPriorityScore() { return priorityScore; }
        public void setPriorityScore(double priorityScore) { this.priorityScore = priorityScore; }
        
        public String getMatchReason() { return matchReason; }
        public void setMatchReason(String matchReason) { this.matchReason = matchReason; }
        
        public Map<String, Object> getMatchCriteria() { return matchCriteria; }
        public void setMatchCriteria(Map<String, Object> matchCriteria) { this.matchCriteria = matchCriteria; }
        
        public List<String> getBenefits() { return benefits; }
        public void setBenefits(List<String> benefits) { this.benefits = benefits; }
        
        public List<String> getRequirements() { return requirements; }
        public void setRequirements(List<String> requirements) { this.requirements = requirements; }
        
        public String getApplicationProcess() { return applicationProcess; }
        public void setApplicationProcess(String applicationProcess) { this.applicationProcess = applicationProcess; }
        
        public java.time.LocalDateTime getRecommendedAt() { return recommendedAt; }
        public void setRecommendedAt(java.time.LocalDateTime recommendedAt) { this.recommendedAt = recommendedAt; }
    }

    /**
     * Program matching result
     */
    class ProgramMatchResult {
        private boolean matched;
        private String programCode;
        private double matchScore;
        private Map<String, Object> matchDetails;
        private List<String> matchedCriteria;
        private List<String> unmatchedCriteria;
        private String recommendation;
        private java.time.LocalDateTime matchedAt;

        // Getters and setters
        public boolean isMatched() { return matched; }
        public void setMatched(boolean matched) { this.matched = matched; }
        
        public String getProgramCode() { return programCode; }
        public void setProgramCode(String programCode) { this.programCode = programCode; }
        
        public double getMatchScore() { return matchScore; }
        public void setMatchScore(double matchScore) { this.matchScore = matchScore; }
        
        public Map<String, Object> getMatchDetails() { return matchDetails; }
        public void setMatchDetails(Map<String, Object> matchDetails) { this.matchDetails = matchDetails; }
        
        public List<String> getMatchedCriteria() { return matchedCriteria; }
        public void setMatchedCriteria(List<String> matchedCriteria) { this.matchedCriteria = matchedCriteria; }
        
        public List<String> getUnmatchedCriteria() { return unmatchedCriteria; }
        public void setUnmatchedCriteria(List<String> unmatchedCriteria) { this.unmatchedCriteria = unmatchedCriteria; }
        
        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
        
        public java.time.LocalDateTime getMatchedAt() { return matchedAt; }
        public void setMatchedAt(java.time.LocalDateTime matchedAt) { this.matchedAt = matchedAt; }
    }
}
