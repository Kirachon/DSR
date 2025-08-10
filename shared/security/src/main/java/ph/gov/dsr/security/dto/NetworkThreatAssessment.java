package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Network Threat Assessment DTO
 * Contains comprehensive network threat assessment results
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkThreatAssessment {

    /**
     * Assessment ID
     */
    private String assessmentId;

    /**
     * Network being assessed
     */
    private String networkId;

    /**
     * Assessment timestamp
     */
    private LocalDateTime assessmentTime;

    /**
     * Overall threat level
     */
    private String threatLevel;

    /**
     * Risk score (0-100)
     */
    private Integer riskScore;

    /**
     * Confidence level (0-100)
     */
    private Integer confidence;

    /**
     * Detected threats
     */
    private List<String> detectedThreats;

    /**
     * Network vulnerabilities
     */
    private List<String> vulnerabilities;

    /**
     * Suspicious activities
     */
    private List<String> suspiciousActivities;

    /**
     * Assessment details
     */
    private Map<String, Object> assessmentDetails;

    /**
     * Recommendations
     */
    private List<String> recommendations;

    /**
     * Check if high-risk threats exist
     */
    public boolean hasHighRiskThreats() {
        return riskScore != null && riskScore >= 80;
    }

    /**
     * Get threat count
     */
    public int getThreatCount() {
        return detectedThreats != null ? detectedThreats.size() : 0;
    }

    /**
     * Create high-risk assessment
     */
    public static NetworkThreatAssessment highRisk(String networkId, List<String> threats) {
        return NetworkThreatAssessment.builder()
                .assessmentId(java.util.UUID.randomUUID().toString())
                .networkId(networkId)
                .threatLevel("HIGH")
                .riskScore(85)
                .confidence(90)
                .detectedThreats(threats)
                .assessmentTime(LocalDateTime.now())
                .build();
    }

    /**
     * Create low-risk assessment
     */
    public static NetworkThreatAssessment lowRisk(String networkId) {
        return NetworkThreatAssessment.builder()
                .assessmentId(java.util.UUID.randomUUID().toString())
                .networkId(networkId)
                .threatLevel("LOW")
                .riskScore(20)
                .confidence(85)
                .assessmentTime(LocalDateTime.now())
                .build();
    }
}
