package ph.gov.dsr.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ph.gov.dsr.security.dto.MLAnalysisResult;
import ph.gov.dsr.security.dto.SecurityEventData;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Machine Learning Service
 * Provides ML-based security analysis and threat detection
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MachineLearningService {

    private static final String DEFAULT_MODEL = "DSR_SECURITY_ML_MODEL_V3";
    private static final String MODEL_VERSION = "3.0.0";

    /**
     * Analyze security event using ML (alias method)
     */
    public MLAnalysisResult analyzeEvent(SecurityEventData event) {
        return analyzeSecurityEvent(event);
    }

    /**
     * Analyze security event using ML
     */
    public MLAnalysisResult analyzeSecurityEvent(SecurityEventData event) {
        log.debug("Analyzing security event with ML: {}", event.getEventId());
        
        try {
            long startTime = System.currentTimeMillis();
            
            // Extract features from event
            Map<String, Object> features = extractFeatures(event);
            
            // Run ML prediction
            MLPrediction prediction = runMLPrediction(features);
            
            // Calculate confidence
            int confidence = calculateConfidence(prediction);
            
            long duration = System.currentTimeMillis() - startTime;
            
            return MLAnalysisResult.builder()
                    .analysisId(java.util.UUID.randomUUID().toString())
                    .modelName(DEFAULT_MODEL)
                    .modelVersion(MODEL_VERSION)
                    .predictionScore(prediction.getScore())
                    .confidence(confidence)
                    .predictedClass(prediction.getPredictedClass())
                    .classProbabilities(prediction.getClassProbabilities())
                    .featureImportance(prediction.getFeatureImportance())
                    .features(features)
                    .analyzedAt(LocalDateTime.now())
                    .analysisDurationMs(duration)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error analyzing security event with ML: {}", event.getEventId(), e);
            return createErrorResult();
        }
    }

    /**
     * Analyze multiple events for pattern detection
     */
    public MLAnalysisResult analyzeEventPattern(List<SecurityEventData> events) {
        log.debug("Analyzing event pattern with ML: {} events", events.size());
        
        try {
            long startTime = System.currentTimeMillis();
            
            // Extract pattern features
            Map<String, Object> patternFeatures = extractPatternFeatures(events);
            
            // Run pattern analysis
            MLPrediction prediction = runPatternAnalysis(patternFeatures);
            
            // Calculate confidence
            int confidence = calculateConfidence(prediction);
            
            long duration = System.currentTimeMillis() - startTime;
            
            return MLAnalysisResult.builder()
                    .analysisId(java.util.UUID.randomUUID().toString())
                    .modelName("DSR_PATTERN_ANALYSIS_MODEL_V3")
                    .modelVersion(MODEL_VERSION)
                    .predictionScore(prediction.getScore())
                    .confidence(confidence)
                    .predictedClass(prediction.getPredictedClass())
                    .classProbabilities(prediction.getClassProbabilities())
                    .features(patternFeatures)
                    .analyzedAt(LocalDateTime.now())
                    .analysisDurationMs(duration)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error analyzing event pattern with ML", e);
            return createErrorResult();
        }
    }

    /**
     * Extract features from security event
     */
    private Map<String, Object> extractFeatures(SecurityEventData event) {
        Map<String, Object> features = new HashMap<>();
        
        // Basic event features
        features.put("eventType", event.getEventType());
        features.put("severity", event.getSeverityLevel());
        features.put("source", event.getSourceSystem());
        features.put("hasUserId", event.getUserId() != null);
        features.put("hasSessionId", event.getSessionId() != null);
        
        // Time-based features
        LocalDateTime eventTime = event.getTimestamp();
        if (eventTime != null) {
            features.put("hourOfDay", eventTime.getHour());
            features.put("dayOfWeek", eventTime.getDayOfWeek().getValue());
            features.put("isWeekend", eventTime.getDayOfWeek().getValue() >= 6);
        }
        
        // Additional features from event data
        if (event.getEventMetadata() != null) {
            features.putAll(extractEventDataFeatures(event.getEventMetadata()));
        }
        
        return features;
    }

    /**
     * Extract pattern features from multiple events
     */
    private Map<String, Object> extractPatternFeatures(List<SecurityEventData> events) {
        Map<String, Object> features = new HashMap<>();
        
        // Count-based features
        features.put("eventCount", events.size());
        features.put("uniqueEventTypes", events.stream()
                .map(SecurityEventData::getEventType)
                .distinct()
                .count());
        features.put("uniqueSources", events.stream()
                .map(SecurityEventData::getSourceSystem)
                .distinct()
                .count());
        
        // Time-based pattern features
        if (!events.isEmpty()) {
            LocalDateTime earliest = events.stream()
                    .map(SecurityEventData::getTimestamp)
                    .min(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now());
            LocalDateTime latest = events.stream()
                    .map(SecurityEventData::getTimestamp)
                    .max(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now());
            
            long timeSpanMinutes = java.time.Duration.between(earliest, latest).toMinutes();
            features.put("timeSpanMinutes", timeSpanMinutes);
            features.put("eventFrequency", timeSpanMinutes > 0 ? events.size() / (double) timeSpanMinutes : events.size());
        }
        
        return features;
    }

    /**
     * Extract features from event data
     */
    private Map<String, Object> extractEventDataFeatures(Map<String, Object> eventData) {
        Map<String, Object> features = new HashMap<>();
        
        // Extract relevant features from event data
        features.put("dataFieldCount", eventData.size());
        features.put("hasIpAddress", eventData.containsKey("ipAddress") || eventData.containsKey("sourceIp"));
        features.put("hasUserAgent", eventData.containsKey("userAgent"));
        features.put("hasLocation", eventData.containsKey("location") || eventData.containsKey("geolocation"));
        
        return features;
    }

    /**
     * Run ML prediction
     */
    private MLPrediction runMLPrediction(Map<String, Object> features) {
        // Simulate ML model prediction
        // In real implementation, this would call actual ML model
        
        int score = calculateSimulatedScore(features);
        String predictedClass = determinePredictedClass(score);
        Map<String, Double> classProbabilities = calculateClassProbabilities(score);
        Map<String, Double> featureImportance = calculateFeatureImportance(features);
        
        return new MLPrediction(score, predictedClass, classProbabilities, featureImportance);
    }

    /**
     * Run pattern analysis
     */
    private MLPrediction runPatternAnalysis(Map<String, Object> features) {
        // Simulate pattern analysis
        int score = calculatePatternScore(features);
        String predictedClass = determinePatternClass(score);
        Map<String, Double> classProbabilities = calculateClassProbabilities(score);
        Map<String, Double> featureImportance = calculateFeatureImportance(features);
        
        return new MLPrediction(score, predictedClass, classProbabilities, featureImportance);
    }

    /**
     * Calculate simulated score
     */
    private int calculateSimulatedScore(Map<String, Object> features) {
        int score = 30; // Base score
        
        // Adjust based on features
        if ("SECURITY_VIOLATION".equals(features.get("eventType"))) {
            score += 40;
        }
        if ("HIGH".equals(features.get("severity"))) {
            score += 30;
        }
        if (Boolean.TRUE.equals(features.get("isWeekend"))) {
            score += 10;
        }
        
        return Math.min(score, 100);
    }

    /**
     * Calculate pattern score
     */
    private int calculatePatternScore(Map<String, Object> features) {
        int score = 20; // Base score
        
        Integer eventCount = (Integer) features.get("eventCount");
        if (eventCount != null && eventCount > 10) {
            score += 30;
        }
        
        Double frequency = (Double) features.get("eventFrequency");
        if (frequency != null && frequency > 1.0) {
            score += 25;
        }
        
        return Math.min(score, 100);
    }

    /**
     * Determine predicted class
     */
    private String determinePredictedClass(int score) {
        if (score >= 80) return "HIGH_THREAT";
        if (score >= 60) return "MEDIUM_THREAT";
        if (score >= 40) return "LOW_THREAT";
        return "BENIGN";
    }

    /**
     * Determine pattern class
     */
    private String determinePatternClass(int score) {
        if (score >= 70) return "ATTACK_PATTERN";
        if (score >= 50) return "SUSPICIOUS_PATTERN";
        return "NORMAL_PATTERN";
    }

    /**
     * Calculate class probabilities
     */
    private Map<String, Double> calculateClassProbabilities(int score) {
        Map<String, Double> probabilities = new HashMap<>();
        
        double threatProb = score / 100.0;
        double benignProb = 1.0 - threatProb;
        
        probabilities.put("THREAT", threatProb);
        probabilities.put("BENIGN", benignProb);
        
        return probabilities;
    }

    /**
     * Calculate feature importance
     */
    private Map<String, Double> calculateFeatureImportance(Map<String, Object> features) {
        Map<String, Double> importance = new HashMap<>();
        
        // Simulate feature importance scores
        importance.put("eventType", 0.3);
        importance.put("severity", 0.25);
        importance.put("source", 0.2);
        importance.put("timeFeatures", 0.15);
        importance.put("other", 0.1);
        
        return importance;
    }

    /**
     * Calculate confidence
     */
    private int calculateConfidence(MLPrediction prediction) {
        // Base confidence on prediction score and class probabilities
        int score = prediction.getScore();
        
        if (score >= 80 || score <= 20) {
            return 90; // High confidence for extreme scores
        } else if (score >= 70 || score <= 30) {
            return 80;
        } else {
            return 70;
        }
    }

    /**
     * Create error result
     */
    private MLAnalysisResult createErrorResult() {
        return MLAnalysisResult.builder()
                .analysisId(java.util.UUID.randomUUID().toString())
                .modelName("ERROR")
                .modelVersion("N/A")
                .predictionScore(0)
                .confidence(0)
                .predictedClass("ERROR")
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Internal ML Prediction class
     */
    private static class MLPrediction {
        private final int score;
        private final String predictedClass;
        private final Map<String, Double> classProbabilities;
        private final Map<String, Double> featureImportance;

        public MLPrediction(int score, String predictedClass, 
                          Map<String, Double> classProbabilities, 
                          Map<String, Double> featureImportance) {
            this.score = score;
            this.predictedClass = predictedClass;
            this.classProbabilities = classProbabilities;
            this.featureImportance = featureImportance;
        }

        public int getScore() { return score; }
        public String getPredictedClass() { return predictedClass; }
        public Map<String, Double> getClassProbabilities() { return classProbabilities; }
        public Map<String, Double> getFeatureImportance() { return featureImportance; }
    }
}
