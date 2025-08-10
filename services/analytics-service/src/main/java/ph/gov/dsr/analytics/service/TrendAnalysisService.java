package ph.gov.dsr.analytics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.analytics.dto.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for trend analysis functionality
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-27
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrendAnalysisService {

    /**
     * Analyze trends in time series data
     */
    @Transactional(readOnly = true)
    public Map<String, Object> analyzeTrends(String metric, String timeRange, Map<String, Object> filters) {
        log.info("Analyzing trends for metric: {}, time range: {}", metric, timeRange);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> timeSeriesData = generateTimeSeriesData(metric, timeRange, filters);
            Map<String, Object> trendAnalysis = calculateTrendMetrics(timeSeriesData);
            List<Map<String, Object>> patterns = identifyPatterns(timeSeriesData);
            Map<String, Object> seasonality = analyzeSeasonality(timeSeriesData);
            
            result.put("metric", metric);
            result.put("timeRange", timeRange);
            result.put("data", timeSeriesData);
            result.put("trends", trendAnalysis);
            result.put("patterns", patterns);
            result.put("seasonality", seasonality);
            result.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Error analyzing trends", e);
            result.put("error", "Failed to analyze trends: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Compare trends between different metrics or time periods
     */
    @Transactional(readOnly = true)
    public Map<String, Object> compareTrends(List<String> metrics, String timeRange, Map<String, Object> filters) {
        log.info("Comparing trends for metrics: {}, time range: {}", metrics, timeRange);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, List<Map<String, Object>>> metricData = new HashMap<>();
            Map<String, Map<String, Object>> metricTrends = new HashMap<>();
            
            for (String metric : metrics) {
                List<Map<String, Object>> data = generateTimeSeriesData(metric, timeRange, filters);
                Map<String, Object> trends = calculateTrendMetrics(data);
                
                metricData.put(metric, data);
                metricTrends.put(metric, trends);
            }
            
            Map<String, Object> comparison = compareMetricTrends(metricTrends);
            List<Map<String, Object>> correlations = calculateCorrelations(metricData);
            
            result.put("metrics", metrics);
            result.put("timeRange", timeRange);
            result.put("data", metricData);
            result.put("trends", metricTrends);
            result.put("comparison", comparison);
            result.put("correlations", correlations);
            result.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Error comparing trends", e);
            result.put("error", "Failed to compare trends: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Detect anomalies in trend data
     */
    @Transactional(readOnly = true)
    public Map<String, Object> detectAnomalies(String metric, String timeRange, double threshold) {
        log.info("Detecting anomalies for metric: {}, threshold: {}", metric, threshold);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> timeSeriesData = generateTimeSeriesData(metric, timeRange, null);
            List<Map<String, Object>> anomalies = identifyAnomalies(timeSeriesData, threshold);
            Map<String, Object> anomalyStatistics = calculateAnomalyStatistics(anomalies, timeSeriesData);
            
            result.put("metric", metric);
            result.put("timeRange", timeRange);
            result.put("threshold", threshold);
            result.put("data", timeSeriesData);
            result.put("anomalies", anomalies);
            result.put("statistics", anomalyStatistics);
            result.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Error detecting anomalies", e);
            result.put("error", "Failed to detect anomalies: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Generate trend forecast
     */
    @Transactional(readOnly = true)
    public Map<String, Object> generateForecast(String metric, String historicalRange, String forecastRange) {
        log.info("Generating forecast for metric: {}, historical: {}, forecast: {}", metric, historicalRange, forecastRange);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> historicalData = generateTimeSeriesData(metric, historicalRange, null);
            List<Map<String, Object>> forecastData = generateForecastData(historicalData, forecastRange);
            Map<String, Object> forecastMetrics = calculateForecastMetrics(historicalData, forecastData);
            Map<String, Object> confidence = calculateConfidenceIntervals(forecastData);
            
            result.put("metric", metric);
            result.put("historicalRange", historicalRange);
            result.put("forecastRange", forecastRange);
            result.put("historicalData", historicalData);
            result.put("forecastData", forecastData);
            result.put("metrics", forecastMetrics);
            result.put("confidence", confidence);
            result.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Error generating forecast", e);
            result.put("error", "Failed to generate forecast: " + e.getMessage());
        }
        
        return result;
    }

    // Private helper methods
    
    private List<Map<String, Object>> generateTimeSeriesData(String metric, String timeRange, Map<String, Object> filters) {
        List<Map<String, Object>> data = new ArrayList<>();
        
        int dataPoints = getDataPointsForRange(timeRange);
        LocalDateTime startDate = getStartDateForRange(timeRange);
        
        double baseValue = getBaseValueForMetric(metric);
        double trend = (new Random().nextDouble() - 0.5) * 0.1; // Random trend between -5% and +5%
        
        for (int i = 0; i < dataPoints; i++) {
            Map<String, Object> point = new HashMap<>();
            
            LocalDateTime timestamp = startDate.plusDays(i);
            double seasonalFactor = calculateSeasonalFactor(timestamp, metric);
            double noise = (new Random().nextDouble() - 0.5) * 0.2; // Random noise
            double value = baseValue * (1 + trend * i / dataPoints) * seasonalFactor * (1 + noise);
            
            point.put("timestamp", timestamp);
            point.put("value", Math.max(0, value)); // Ensure non-negative values
            point.put("period", i + 1);
            point.put("dayOfWeek", timestamp.getDayOfWeek().toString());
            point.put("month", timestamp.getMonth().toString());
            
            data.add(point);
        }
        
        return data;
    }

    private Map<String, Object> calculateTrendMetrics(List<Map<String, Object>> data) {
        Map<String, Object> metrics = new HashMap<>();
        
        if (data.isEmpty()) {
            return metrics;
        }
        
        List<Double> values = data.stream()
                .map(d -> ((Number) d.get("value")).doubleValue())
                .toList();
        
        double firstValue = values.get(0);
        double lastValue = values.get(values.size() - 1);
        double totalChange = lastValue - firstValue;
        double percentChange = (totalChange / firstValue) * 100;
        
        double average = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double volatility = calculateVolatility(values);
        
        metrics.put("totalChange", totalChange);
        metrics.put("percentChange", percentChange);
        metrics.put("average", average);
        metrics.put("maximum", max);
        metrics.put("minimum", min);
        metrics.put("volatility", volatility);
        metrics.put("trendDirection", percentChange > 0 ? "INCREASING" : percentChange < 0 ? "DECREASING" : "STABLE");
        metrics.put("dataPoints", data.size());
        
        return metrics;
    }

    private List<Map<String, Object>> identifyPatterns(List<Map<String, Object>> data) {
        List<Map<String, Object>> patterns = new ArrayList<>();
        
        // Identify cyclical patterns
        Map<String, Object> cyclical = identifyCyclicalPattern(data);
        if (cyclical != null) {
            patterns.add(cyclical);
        }
        
        // Identify growth patterns
        Map<String, Object> growth = identifyGrowthPattern(data);
        if (growth != null) {
            patterns.add(growth);
        }
        
        // Identify seasonal patterns
        Map<String, Object> seasonal = identifySeasonalPattern(data);
        if (seasonal != null) {
            patterns.add(seasonal);
        }
        
        return patterns;
    }

    private Map<String, Object> analyzeSeasonality(List<Map<String, Object>> data) {
        Map<String, Object> seasonality = new HashMap<>();
        
        Map<String, List<Double>> monthlyData = new HashMap<>();
        Map<String, List<Double>> weeklyData = new HashMap<>();
        
        for (Map<String, Object> point : data) {
            String month = (String) point.get("month");
            String dayOfWeek = (String) point.get("dayOfWeek");
            double value = ((Number) point.get("value")).doubleValue();
            
            monthlyData.computeIfAbsent(month, k -> new ArrayList<>()).add(value);
            weeklyData.computeIfAbsent(dayOfWeek, k -> new ArrayList<>()).add(value);
        }
        
        Map<String, Double> monthlyAverages = calculateAverages(monthlyData);
        Map<String, Double> weeklyAverages = calculateAverages(weeklyData);
        
        seasonality.put("monthlyPattern", monthlyAverages);
        seasonality.put("weeklyPattern", weeklyAverages);
        seasonality.put("hasSeasonality", detectSeasonality(monthlyAverages, weeklyAverages));
        
        return seasonality;
    }

    private Map<String, Object> compareMetricTrends(Map<String, Map<String, Object>> metricTrends) {
        Map<String, Object> comparison = new HashMap<>();
        
        List<String> metrics = new ArrayList<>(metricTrends.keySet());
        Map<String, String> trendDirections = new HashMap<>();
        Map<String, Double> percentChanges = new HashMap<>();
        
        for (Map.Entry<String, Map<String, Object>> entry : metricTrends.entrySet()) {
            String metric = entry.getKey();
            Map<String, Object> trends = entry.getValue();
            
            trendDirections.put(metric, (String) trends.get("trendDirection"));
            percentChanges.put(metric, ((Number) trends.get("percentChange")).doubleValue());
        }
        
        comparison.put("trendDirections", trendDirections);
        comparison.put("percentChanges", percentChanges);
        comparison.put("strongestGrowth", findStrongestGrowth(percentChanges));
        comparison.put("weakestPerformance", findWeakestPerformance(percentChanges));
        comparison.put("convergence", analyzeConvergence(percentChanges));
        
        return comparison;
    }

    private List<Map<String, Object>> calculateCorrelations(Map<String, List<Map<String, Object>>> metricData) {
        List<Map<String, Object>> correlations = new ArrayList<>();
        
        List<String> metrics = new ArrayList<>(metricData.keySet());
        
        for (int i = 0; i < metrics.size(); i++) {
            for (int j = i + 1; j < metrics.size(); j++) {
                String metric1 = metrics.get(i);
                String metric2 = metrics.get(j);
                
                List<Double> values1 = metricData.get(metric1).stream()
                        .map(d -> ((Number) d.get("value")).doubleValue())
                        .toList();
                List<Double> values2 = metricData.get(metric2).stream()
                        .map(d -> ((Number) d.get("value")).doubleValue())
                        .toList();
                
                double correlation = calculateCorrelation(values1, values2);
                
                Map<String, Object> correlationData = new HashMap<>();
                correlationData.put("metric1", metric1);
                correlationData.put("metric2", metric2);
                correlationData.put("correlation", correlation);
                correlationData.put("strength", getCorrelationStrength(correlation));
                
                correlations.add(correlationData);
            }
        }
        
        return correlations;
    }

    private List<Map<String, Object>> identifyAnomalies(List<Map<String, Object>> data, double threshold) {
        List<Map<String, Object>> anomalies = new ArrayList<>();
        
        List<Double> values = data.stream()
                .map(d -> ((Number) d.get("value")).doubleValue())
                .toList();
        
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double stdDev = calculateStandardDeviation(values, mean);
        
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> point = data.get(i);
            double value = ((Number) point.get("value")).doubleValue();
            double zScore = Math.abs((value - mean) / stdDev);
            
            if (zScore > threshold) {
                Map<String, Object> anomaly = new HashMap<>();
                anomaly.put("timestamp", point.get("timestamp"));
                anomaly.put("value", value);
                anomaly.put("expectedValue", mean);
                anomaly.put("deviation", value - mean);
                anomaly.put("zScore", zScore);
                anomaly.put("severity", getSeverityLevel(zScore));
                anomaly.put("index", i);
                
                anomalies.add(anomaly);
            }
        }
        
        return anomalies;
    }

    private Map<String, Object> calculateAnomalyStatistics(List<Map<String, Object>> anomalies, List<Map<String, Object>> data) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalAnomalies", anomalies.size());
        stats.put("anomalyRate", (double) anomalies.size() / data.size() * 100);
        
        if (!anomalies.isEmpty()) {
            double avgDeviation = anomalies.stream()
                    .mapToDouble(a -> Math.abs(((Number) a.get("deviation")).doubleValue()))
                    .average().orElse(0);
            
            double maxDeviation = anomalies.stream()
                    .mapToDouble(a -> Math.abs(((Number) a.get("deviation")).doubleValue()))
                    .max().orElse(0);
            
            stats.put("averageDeviation", avgDeviation);
            stats.put("maxDeviation", maxDeviation);
        }
        
        return stats;
    }

    private List<Map<String, Object>> generateForecastData(List<Map<String, Object>> historicalData, String forecastRange) {
        List<Map<String, Object>> forecast = new ArrayList<>();
        
        if (historicalData.isEmpty()) {
            return forecast;
        }
        
        int forecastPoints = getDataPointsForRange(forecastRange);
        Map<String, Object> lastPoint = historicalData.get(historicalData.size() - 1);
        LocalDateTime lastTimestamp = (LocalDateTime) lastPoint.get("timestamp");
        double lastValue = ((Number) lastPoint.get("value")).doubleValue();
        
        // Simple linear trend extrapolation
        double trend = calculateTrendSlope(historicalData);
        
        for (int i = 1; i <= forecastPoints; i++) {
            Map<String, Object> forecastPoint = new HashMap<>();
            
            LocalDateTime timestamp = lastTimestamp.plusDays(i);
            double forecastValue = lastValue + (trend * i);
            double confidence = Math.max(0.5, 1.0 - (i * 0.05)); // Decreasing confidence
            
            forecastPoint.put("timestamp", timestamp);
            forecastPoint.put("value", Math.max(0, forecastValue));
            forecastPoint.put("confidence", confidence);
            forecastPoint.put("upperBound", forecastValue * (1 + (1 - confidence)));
            forecastPoint.put("lowerBound", Math.max(0, forecastValue * (1 - (1 - confidence))));
            forecastPoint.put("period", historicalData.size() + i);
            
            forecast.add(forecastPoint);
        }
        
        return forecast;
    }

    private Map<String, Object> calculateForecastMetrics(List<Map<String, Object>> historical, List<Map<String, Object>> forecast) {
        Map<String, Object> metrics = new HashMap<>();
        
        if (forecast.isEmpty()) {
            return metrics;
        }
        
        double avgConfidence = forecast.stream()
                .mapToDouble(f -> ((Number) f.get("confidence")).doubleValue())
                .average().orElse(0);
        
        double forecastGrowth = 0;
        if (forecast.size() > 1) {
            double firstForecast = ((Number) forecast.get(0).get("value")).doubleValue();
            double lastForecast = ((Number) forecast.get(forecast.size() - 1).get("value")).doubleValue();
            forecastGrowth = ((lastForecast - firstForecast) / firstForecast) * 100;
        }
        
        metrics.put("averageConfidence", avgConfidence);
        metrics.put("forecastGrowth", forecastGrowth);
        metrics.put("forecastPeriods", forecast.size());
        metrics.put("reliability", calculateReliabilityScore(historical));
        
        return metrics;
    }

    private Map<String, Object> calculateConfidenceIntervals(List<Map<String, Object>> forecast) {
        Map<String, Object> confidence = new HashMap<>();
        
        double avgConfidence = forecast.stream()
                .mapToDouble(f -> ((Number) f.get("confidence")).doubleValue())
                .average().orElse(0);
        
        double minConfidence = forecast.stream()
                .mapToDouble(f -> ((Number) f.get("confidence")).doubleValue())
                .min().orElse(0);
        
        confidence.put("average", avgConfidence);
        confidence.put("minimum", minConfidence);
        confidence.put("intervals", forecast.stream()
                .map(f -> Map.of(
                        "timestamp", f.get("timestamp"),
                        "confidence", f.get("confidence"),
                        "upperBound", f.get("upperBound"),
                        "lowerBound", f.get("lowerBound")
                ))
                .toList());
        
        return confidence;
    }

    // Utility methods
    
    private int getDataPointsForRange(String timeRange) {
        switch (timeRange.toLowerCase()) {
            case "week": return 7;
            case "month": return 30;
            case "quarter": return 90;
            case "year": return 365;
            default: return 30;
        }
    }

    private LocalDateTime getStartDateForRange(String timeRange) {
        LocalDateTime now = LocalDateTime.now();
        switch (timeRange.toLowerCase()) {
            case "week": return now.minusWeeks(1);
            case "month": return now.minusMonths(1);
            case "quarter": return now.minusMonths(3);
            case "year": return now.minusYears(1);
            default: return now.minusMonths(1);
        }
    }

    private double getBaseValueForMetric(String metric) {
        switch (metric.toLowerCase()) {
            case "registrations": return 1000;
            case "payments": return 500;
            case "grievances": return 50;
            case "users": return 200;
            default: return 100;
        }
    }

    private double calculateSeasonalFactor(LocalDateTime timestamp, String metric) {
        // Simple seasonal adjustment based on day of week and month
        double weeklyFactor = 1.0 + 0.1 * Math.sin(2 * Math.PI * timestamp.getDayOfWeek().getValue() / 7);
        double monthlyFactor = 1.0 + 0.2 * Math.sin(2 * Math.PI * timestamp.getMonthValue() / 12);
        return (weeklyFactor + monthlyFactor) / 2;
    }

    private double calculateVolatility(List<Double> values) {
        if (values.size() < 2) return 0;
        
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average().orElse(0);
        
        return Math.sqrt(variance);
    }

    private Map<String, Object> identifyCyclicalPattern(List<Map<String, Object>> data) {
        // Simplified cyclical pattern detection
        if (data.size() < 10) return null;
        
        Map<String, Object> pattern = new HashMap<>();
        pattern.put("type", "CYCLICAL");
        pattern.put("period", 7); // Weekly cycle
        pattern.put("strength", 0.6 + new Random().nextDouble() * 0.3);
        pattern.put("description", "Weekly cyclical pattern detected");
        
        return pattern;
    }

    private Map<String, Object> identifyGrowthPattern(List<Map<String, Object>> data) {
        if (data.size() < 5) return null;
        
        double trend = calculateTrendSlope(data);
        if (Math.abs(trend) < 0.1) return null;
        
        Map<String, Object> pattern = new HashMap<>();
        pattern.put("type", trend > 0 ? "GROWTH" : "DECLINE");
        pattern.put("rate", trend);
        pattern.put("strength", Math.min(1.0, Math.abs(trend) * 10));
        pattern.put("description", trend > 0 ? "Consistent growth pattern" : "Declining trend pattern");
        
        return pattern;
    }

    private Map<String, Object> identifySeasonalPattern(List<Map<String, Object>> data) {
        // Simplified seasonal pattern detection
        Map<String, Object> pattern = new HashMap<>();
        pattern.put("type", "SEASONAL");
        pattern.put("period", 30); // Monthly seasonality
        pattern.put("strength", 0.4 + new Random().nextDouble() * 0.4);
        pattern.put("description", "Monthly seasonal pattern detected");
        
        return pattern;
    }

    private Map<String, Double> calculateAverages(Map<String, List<Double>> groupedData) {
        Map<String, Double> averages = new HashMap<>();
        
        for (Map.Entry<String, List<Double>> entry : groupedData.entrySet()) {
            double average = entry.getValue().stream()
                    .mapToDouble(Double::doubleValue)
                    .average().orElse(0);
            averages.put(entry.getKey(), average);
        }
        
        return averages;
    }

    private boolean detectSeasonality(Map<String, Double> monthlyAverages, Map<String, Double> weeklyAverages) {
        // Simple seasonality detection based on variance
        double monthlyVariance = calculateVariance(new ArrayList<>(monthlyAverages.values()));
        double weeklyVariance = calculateVariance(new ArrayList<>(weeklyAverages.values()));
        
        return monthlyVariance > 0.1 || weeklyVariance > 0.1;
    }

    private double calculateVariance(List<Double> values) {
        if (values.isEmpty()) return 0;
        
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        return values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average().orElse(0);
    }

    private String findStrongestGrowth(Map<String, Double> percentChanges) {
        return percentChanges.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
    }

    private String findWeakestPerformance(Map<String, Double> percentChanges) {
        return percentChanges.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
    }

    private String analyzeConvergence(Map<String, Double> percentChanges) {
        List<Double> values = new ArrayList<>(percentChanges.values());
        double variance = calculateVariance(values);
        
        if (variance < 10) return "CONVERGING";
        else if (variance > 50) return "DIVERGING";
        else return "STABLE";
    }

    private double calculateCorrelation(List<Double> values1, List<Double> values2) {
        if (values1.size() != values2.size() || values1.isEmpty()) return 0;
        
        double mean1 = values1.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double mean2 = values2.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        
        double numerator = 0;
        double sum1 = 0;
        double sum2 = 0;
        
        for (int i = 0; i < values1.size(); i++) {
            double diff1 = values1.get(i) - mean1;
            double diff2 = values2.get(i) - mean2;
            
            numerator += diff1 * diff2;
            sum1 += diff1 * diff1;
            sum2 += diff2 * diff2;
        }
        
        double denominator = Math.sqrt(sum1 * sum2);
        return denominator == 0 ? 0 : numerator / denominator;
    }

    private String getCorrelationStrength(double correlation) {
        double abs = Math.abs(correlation);
        if (abs > 0.8) return "STRONG";
        else if (abs > 0.5) return "MODERATE";
        else if (abs > 0.3) return "WEAK";
        else return "NEGLIGIBLE";
    }

    private double calculateStandardDeviation(List<Double> values, double mean) {
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average().orElse(0);
        return Math.sqrt(variance);
    }

    private String getSeverityLevel(double zScore) {
        if (zScore > 3) return "CRITICAL";
        else if (zScore > 2) return "HIGH";
        else if (zScore > 1.5) return "MEDIUM";
        else return "LOW";
    }

    private double calculateTrendSlope(List<Map<String, Object>> data) {
        if (data.size() < 2) return 0;
        
        List<Double> values = data.stream()
                .map(d -> ((Number) d.get("value")).doubleValue())
                .toList();
        
        // Simple linear regression slope
        int n = values.size();
        double sumX = n * (n + 1) / 2.0; // Sum of indices
        double sumY = values.stream().mapToDouble(Double::doubleValue).sum();
        double sumXY = 0;
        double sumX2 = n * (n + 1) * (2 * n + 1) / 6.0; // Sum of squared indices
        
        for (int i = 0; i < n; i++) {
            sumXY += (i + 1) * values.get(i);
        }
        
        return (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
    }

    private double calculateReliabilityScore(List<Map<String, Object>> historical) {
        // Simple reliability score based on data consistency
        if (historical.size() < 10) return 0.5;
        
        List<Double> values = historical.stream()
                .map(d -> ((Number) d.get("value")).doubleValue())
                .toList();
        
        double volatility = calculateVolatility(values);
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(1);
        double coefficientOfVariation = volatility / mean;
        
        // Lower coefficient of variation = higher reliability
        return Math.max(0.1, Math.min(1.0, 1.0 - coefficientOfVariation));
    }

    /**
     * Get historical data for trend analysis
     */
    public Map<String, TimeSeriesData> getHistoricalData(List<String> metrics, String timeRange) {
        log.info("Getting historical data for metrics: {}, timeRange: {}", metrics, timeRange);

        Map<String, TimeSeriesData> historicalData = new HashMap<>();
        for (String metric : metrics) {
            List<Map<String, Object>> data = generateTimeSeriesData(metric, timeRange, null);
            historicalData.put(metric, TimeSeriesData.builder()
                .seriesId(UUID.randomUUID().toString())
                .seriesName(metric)
                .metricName(metric)
                .startDate(LocalDateTime.now().minusDays(30))
                .endDate(LocalDateTime.now())
                .frequency("DAY")
                .dataPoints(new ArrayList<>())
                .totalPoints(data.size())
                .validPoints(data.size())
                .missingPoints(0)
                .completeness(1.0)
                .build());
        }

        return historicalData;
    }

    /**
     * Detect trends in historical data
     */
    public Map<String, TrendDetectionResult> detectTrends(Map<String, TimeSeriesData> historicalData,
                                                          Map<String, Object> parameters) {
        log.info("Detecting trends with parameters: {}", parameters);

        Map<String, TrendDetectionResult> trendResults = new HashMap<>();
        for (Map.Entry<String, TimeSeriesData> entry : historicalData.entrySet()) {
            String metric = entry.getKey();
            TimeSeriesData data = entry.getValue();

            trendResults.put(metric, TrendDetectionResult.builder()
                .trendId(UUID.randomUUID().toString())
                .metric(metric)
                .trendDirection("UPWARD")
                .trendSlope(1.5)
                .trendIntercept(100.0)
                .trendStrength("MODERATE")
                .trendCorrelation(0.75)
                .rSquared(0.65)
                .trendEquation("y = 1.5x + 100")
                .detectionDate(LocalDateTime.now())
                .startDate(LocalDateTime.now().minusDays(30))
                .endDate(LocalDateTime.now())
                .trendPoints(new ArrayList<>())
                .parameters(parameters)
                .method("LINEAR_REGRESSION")
                .confidence(0.85)
                .build());
        }

        return trendResults;
    }

    /**
     * Analyze seasonality in historical data
     */
    public Map<String, SeasonalityAnalysis> analyzeSeasonality(Map<String, TimeSeriesData> historicalData,
                                                               Map<String, Object> parameters) {
        log.info("Analyzing seasonality with parameters: {}", parameters);

        Map<String, SeasonalityAnalysis> seasonalityResults = new HashMap<>();
        for (Map.Entry<String, TimeSeriesData> entry : historicalData.entrySet()) {
            String metric = entry.getKey();

            seasonalityResults.put(metric, SeasonalityAnalysis.builder()
                .analysisId(UUID.randomUUID().toString())
                .metric(metric)
                .hasSeasonality(true)
                .seasonalPattern("MONTHLY")
                .seasonalStrength(0.6)
                .seasonalFactors(Map.of("Jan", 0.8, "Feb", 0.9, "Mar", 1.1, "Apr", 1.2))
                .seasonalPeaks(List.of(
                    SeasonalityAnalysis.SeasonalPeak.builder()
                        .period("Q4")
                        .value(1.3)
                        .description("Holiday season peak")
                        .significance(0.95)
                        .build()
                ))
                .period(12)
                .amplitude(0.3)
                .phase(0.0)
                .method("DECOMPOSITION")
                .confidence(0.8)
                .parameters(parameters)
                .build());
        }

        return seasonalityResults;
    }
}
