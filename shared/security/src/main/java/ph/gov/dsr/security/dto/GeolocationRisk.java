package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Geolocation Risk DTO
 * Contains geolocation-based risk assessment information
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeolocationRisk {

    /**
     * Risk assessment ID
     */
    private String riskId;

    /**
     * IP address being assessed
     */
    private String ipAddress;

    /**
     * Country code
     */
    private String countryCode;

    /**
     * Country name
     */
    private String countryName;

    /**
     * Region/state
     */
    private String region;

    /**
     * City
     */
    private String city;

    /**
     * Latitude
     */
    private Double latitude;

    /**
     * Longitude
     */
    private Double longitude;

    /**
     * Risk score (0-100)
     */
    private Integer riskScore;

    /**
     * Risk level
     */
    private String riskLevel;

    /**
     * Whether country is high-risk
     */
    private Boolean highRiskCountry;

    /**
     * Whether country is sanctioned
     */
    private Boolean sanctionedCountry;

    /**
     * Whether location is known VPN/proxy
     */
    private Boolean knownVpnProxy;

    /**
     * Whether location is known Tor exit node
     */
    private Boolean torExitNode;

    /**
     * Distance from expected location (in km)
     */
    private Double distanceFromExpected;

    /**
     * Whether location change is impossible travel
     */
    private Boolean impossibleTravel;

    /**
     * Assessment timestamp
     */
    private LocalDateTime assessedAt;

    /**
     * Additional geolocation data
     */
    private Map<String, Object> geolocationData;

    /**
     * Check if location is high risk
     */
    public boolean isHighRisk() {
        return riskScore != null && riskScore >= 70;
    }

    /**
     * Check if location should be blocked
     */
    public boolean shouldBlock() {
        return Boolean.TRUE.equals(sanctionedCountry) || 
               Boolean.TRUE.equals(impossibleTravel) ||
               (riskScore != null && riskScore >= 90);
    }

    /**
     * Check if enhanced monitoring is needed
     */
    public boolean needsEnhancedMonitoring() {
        return Boolean.TRUE.equals(highRiskCountry) ||
               Boolean.TRUE.equals(knownVpnProxy) ||
               Boolean.TRUE.equals(torExitNode) ||
               isHighRisk();
    }

    /**
     * Get risk category
     */
    public String getRiskCategory() {
        if (riskScore == null) return "UNKNOWN";
        if (riskScore >= 80) return "CRITICAL";
        if (riskScore >= 60) return "HIGH";
        if (riskScore >= 40) return "MEDIUM";
        if (riskScore >= 20) return "LOW";
        return "MINIMAL";
    }

    /**
     * Create high-risk geolocation assessment
     */
    public static GeolocationRisk highRisk(String ipAddress, String countryCode, int riskScore) {
        return GeolocationRisk.builder()
                .riskId(java.util.UUID.randomUUID().toString())
                .ipAddress(ipAddress)
                .countryCode(countryCode)
                .riskScore(riskScore)
                .riskLevel("HIGH")
                .highRiskCountry(true)
                .assessedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create low-risk geolocation assessment
     */
    public static GeolocationRisk lowRisk(String ipAddress, String countryCode) {
        return GeolocationRisk.builder()
                .riskId(java.util.UUID.randomUUID().toString())
                .ipAddress(ipAddress)
                .countryCode(countryCode)
                .riskScore(25)
                .riskLevel("LOW")
                .highRiskCountry(false)
                .sanctionedCountry(false)
                .assessedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Validate geolocation risk consistency
     */
    public boolean isValid() {
        return riskId != null && !riskId.trim().isEmpty() &&
               ipAddress != null && !ipAddress.trim().isEmpty() &&
               assessedAt != null &&
               (riskScore == null || (riskScore >= 0 && riskScore <= 100)) &&
               (latitude == null || (latitude >= -90 && latitude <= 90)) &&
               (longitude == null || (longitude >= -180 && longitude <= 180)) &&
               (distanceFromExpected == null || distanceFromExpected >= 0);
    }
}
