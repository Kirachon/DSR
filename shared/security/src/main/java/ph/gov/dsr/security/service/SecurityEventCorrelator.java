package ph.gov.dsr.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ph.gov.dsr.security.dto.CorrelationResult;
import ph.gov.dsr.security.dto.SecurityEventData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Security Event Correlator Service
 * Correlates security events to identify patterns and threats
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityEventCorrelator {

    /**
     * Correlate single security event
     */
    public CorrelationResult correlateEvent(SecurityEventData event) {
        return correlateEvents(List.of(event));
    }

    /**
     * Correlate security events
     */
    public CorrelationResult correlateEvents(List<SecurityEventData> events) {
        log.debug("Correlating {} security events", events.size());
        
        try {
            // Perform event correlation analysis
            Map<String, Object> correlationDetails = performCorrelationAnalysis(events);
            
            // Calculate correlation score
            int correlationScore = calculateCorrelationScore(events, correlationDetails);
            
            // Identify correlation patterns
            List<String> patterns = identifyCorrelationPatterns(events);
            
            // Extract event IDs
            List<String> eventIds = events.stream()
                    .map(event -> event.getEventId())
                    .toList();
            
            return CorrelationResult.builder()
                    .correlationId(java.util.UUID.randomUUID().toString())
                    .correlationType("SECURITY_EVENT_CORRELATION")
                    .correlationScore(correlationScore)
                    .confidence(calculateConfidence(correlationScore))
                    .correlatedEvents(eventIds)
                    .correlationPatterns(patterns)
                    .correlationDetails(correlationDetails)
                    .analyzedAt(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error correlating security events", e);
            return createErrorResult();
        }
    }

    /**
     * Perform correlation analysis
     */
    private Map<String, Object> performCorrelationAnalysis(List<SecurityEventData> events) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Time-based correlation
        analysis.put("timeBasedCorrelation", analyzeTimeBasedCorrelation(events));
        
        // Source-based correlation
        analysis.put("sourceBasedCorrelation", analyzeSourceBasedCorrelation(events));
        
        // Pattern-based correlation
        analysis.put("patternBasedCorrelation", analyzePatternBasedCorrelation(events));
        
        return analysis;
    }

    /**
     * Analyze time-based correlation
     */
    private Map<String, Object> analyzeTimeBasedCorrelation(List<SecurityEventData> events) {
        Map<String, Object> timeAnalysis = new HashMap<>();
        
        // Calculate time windows
        timeAnalysis.put("eventCount", events.size());
        timeAnalysis.put("timeSpan", calculateTimeSpan(events));
        timeAnalysis.put("eventFrequency", calculateEventFrequency(events));
        
        return timeAnalysis;
    }

    /**
     * Analyze source-based correlation
     */
    private Map<String, Object> analyzeSourceBasedCorrelation(List<SecurityEventData> events) {
        Map<String, Object> sourceAnalysis = new HashMap<>();
        
        // Group by source
        Map<String, Long> sourceGroups = events.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    event -> event.getSourceSystem() != null ? event.getSourceSystem() : "UNKNOWN",
                    java.util.stream.Collectors.counting()
                ));
        
        sourceAnalysis.put("sourceGroups", sourceGroups);
        sourceAnalysis.put("uniqueSources", sourceGroups.size());
        
        return sourceAnalysis;
    }

    /**
     * Analyze pattern-based correlation
     */
    private Map<String, Object> analyzePatternBasedCorrelation(List<SecurityEventData> events) {
        Map<String, Object> patternAnalysis = new HashMap<>();
        
        // Group by event type
        Map<String, Long> typeGroups = events.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    event -> event.getEventType() != null ? event.getEventType() : "UNKNOWN",
                    java.util.stream.Collectors.counting()
                ));
        
        patternAnalysis.put("typeGroups", typeGroups);
        patternAnalysis.put("uniqueTypes", typeGroups.size());
        
        return patternAnalysis;
    }

    /**
     * Calculate correlation score
     */
    private int calculateCorrelationScore(List<SecurityEventData> events, Map<String, Object> details) {
        if (events.isEmpty()) return 0;
        
        int score = 0;
        
        // Base score from event count
        score += Math.min(events.size() * 10, 40);
        
        // Time clustering bonus
        if (isTimeClusteredEvents(events)) {
            score += 30;
        }
        
        // Source correlation bonus
        if (hasSameSourceEvents(events)) {
            score += 20;
        }
        
        // Pattern correlation bonus
        if (hasSimilarPatterns(events)) {
            score += 10;
        }
        
        return Math.min(score, 100);
    }

    /**
     * Calculate confidence level
     */
    private int calculateConfidence(int correlationScore) {
        if (correlationScore >= 80) return 90;
        if (correlationScore >= 60) return 80;
        if (correlationScore >= 40) return 70;
        return 60;
    }

    /**
     * Identify correlation patterns
     */
    private List<String> identifyCorrelationPatterns(List<SecurityEventData> events) {
        List<String> patterns = new ArrayList<>();
        
        if (isTimeClusteredEvents(events)) {
            patterns.add("TIME_CLUSTERED");
        }
        
        if (hasSameSourceEvents(events)) {
            patterns.add("SAME_SOURCE");
        }
        
        if (hasSimilarPatterns(events)) {
            patterns.add("SIMILAR_PATTERNS");
        }
        
        return patterns;
    }

    /**
     * Check if events are time clustered
     */
    private boolean isTimeClusteredEvents(List<SecurityEventData> events) {
        if (events.size() < 2) return false;
        
        // Simple time clustering check - events within 1 hour
        long timeSpan = calculateTimeSpan(events);
        return timeSpan <= 3600000; // 1 hour in milliseconds
    }

    /**
     * Check if events have same source
     */
    private boolean hasSameSourceEvents(List<SecurityEventData> events) {
        if (events.isEmpty()) return false;

        String firstSource = events.get(0).getSourceSystem();
        return events.stream().allMatch(event ->
            firstSource != null && firstSource.equals(event.getSourceSystem()));
    }

    /**
     * Check if events have similar patterns
     */
    private boolean hasSimilarPatterns(List<SecurityEventData> events) {
        if (events.size() < 2) return false;
        
        // Check if majority of events have same type
        Map<String, Long> typeGroups = events.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    event -> event.getEventType() != null ? event.getEventType() : "UNKNOWN",
                    java.util.stream.Collectors.counting()
                ));
        
        long maxCount = typeGroups.values().stream().mapToLong(Long::longValue).max().orElse(0);
        return maxCount >= events.size() * 0.7; // 70% threshold
    }

    /**
     * Calculate time span of events
     */
    private long calculateTimeSpan(List<SecurityEventData> events) {
        if (events.isEmpty()) return 0;
        
        LocalDateTime earliest = events.stream()
                .map(SecurityEventData::getTimestamp)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
                
        LocalDateTime latest = events.stream()
                .map(SecurityEventData::getTimestamp)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        
        return java.time.Duration.between(earliest, latest).toMillis();
    }

    /**
     * Calculate event frequency
     */
    private double calculateEventFrequency(List<SecurityEventData> events) {
        if (events.isEmpty()) return 0.0;
        
        long timeSpan = calculateTimeSpan(events);
        if (timeSpan == 0) return events.size();
        
        return (double) events.size() / (timeSpan / 1000.0); // Events per second
    }

    /**
     * Create error result
     */
    private CorrelationResult createErrorResult() {
        return CorrelationResult.builder()
                .correlationId(java.util.UUID.randomUUID().toString())
                .correlationType("ERROR")
                .correlationScore(0)
                .confidence(0)
                .correlatedEvents(new ArrayList<>())
                .correlationPatterns(new ArrayList<>())
                .analyzedAt(LocalDateTime.now())
                .build();
    }
}
