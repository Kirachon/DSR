package ph.gov.dsr.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ph.gov.dsr.ai.dto.*;
import ph.gov.dsr.ai.model.MLModel;
import ph.gov.dsr.ai.repository.PredictionRepository;
import ph.gov.dsr.ai.integration.PythonMLClient;
import ph.gov.dsr.ai.integration.TensorFlowClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Predictive analytics service using machine learning models
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PredictiveAnalyticsService {

    private final PythonMLClient pythonMLClient;
    private final TensorFlowClient tensorFlowClient;
    private final PredictionRepository predictionRepository;
    private final ModelTrainingService modelTrainingService;
    private final FeatureEngineeringService featureEngineeringService;

    @Value("${dsr.ai.prediction.confidence-threshold:0.8}")
    private double confidenceThreshold;

    @Value("${dsr.ai.prediction.model-refresh-hours:24}")
    private int modelRefreshHours;

    /**
     * Predict household eligibility for social programs
     */
    public CompletableFuture<EligibilityPrediction> predictEligibility(
            String householdId, String programId) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Predicting eligibility for household: {} program: {}", 
                        householdId, programId);
                
                // Extract features for the household
                Map<String, Object> features = featureEngineeringService
                    .extractHouseholdFeatures(householdId);
                
                // Get the appropriate model
                MLModel model = getEligibilityModel(programId);
                
                // Make prediction
                PredictionResult result = tensorFlowClient.predict(
                    model.getModelId(), features);
                
                // Create prediction response
                EligibilityPrediction prediction = EligibilityPrediction.builder()
                    .householdId(householdId)
                    .programId(programId)
                    .eligible(result.getProbability() > confidenceThreshold)
                    .confidence(result.getProbability())
                    .factors(result.getFeatureImportance())
                    .predictedAt(LocalDateTime.now())
                    .modelVersion(model.getVersion())
                    .build();
                
                // Store prediction for audit and retraining
                predictionRepository.save(prediction);
                
                log.info("Eligibility prediction completed: {} (confidence: {})", 
                        prediction.isEligible(), prediction.getConfidence());
                
                return prediction;
                
            } catch (Exception e) {
                log.error("Failed to predict eligibility", e);
                throw new PredictionException("Eligibility prediction failed", e);
            }
        });
    }

    /**
     * Predict fraud risk for applications or transactions
     */
    public CompletableFuture<FraudRiskPrediction> predictFraudRisk(
            FraudRiskRequest request) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Predicting fraud risk for type: {} entity: {}", 
                        request.getType(), request.getEntityId());
                
                // Extract fraud detection features
                Map<String, Object> features = featureEngineeringService
                    .extractFraudFeatures(request);
                
                // Get fraud detection model
                MLModel model = getFraudDetectionModel(request.getType());
                
                // Make prediction
                PredictionResult result = pythonMLClient.predict(
                    model.getModelId(), features);
                
                // Determine risk level
                FraudRiskLevel riskLevel = determineRiskLevel(result.getProbability());
                
                // Create prediction response
                FraudRiskPrediction prediction = FraudRiskPrediction.builder()
                    .entityId(request.getEntityId())
                    .type(request.getType())
                    .riskLevel(riskLevel)
                    .riskScore(result.getProbability())
                    .riskFactors(result.getFeatureImportance())
                    .recommendedActions(getRecommendedActions(riskLevel))
                    .predictedAt(LocalDateTime.now())
                    .modelVersion(model.getVersion())
                    .build();
                
                // Store prediction
                predictionRepository.save(prediction);
                
                // Trigger alerts for high-risk cases
                if (riskLevel == FraudRiskLevel.HIGH) {
                    triggerFraudAlert(prediction);
                }
                
                log.info("Fraud risk prediction completed: {} (score: {})", 
                        riskLevel, prediction.getRiskScore());
                
                return prediction;
                
            } catch (Exception e) {
                log.error("Failed to predict fraud risk", e);
                throw new PredictionException("Fraud risk prediction failed", e);
            }
        });
    }

    /**
     * Predict optimal payment amounts and timing
     */
    public CompletableFuture<PaymentOptimizationPrediction> optimizePayments(
            PaymentOptimizationRequest request) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Optimizing payments for {} beneficiaries", 
                        request.getBeneficiaryIds().size());
                
                // Extract payment optimization features
                Map<String, Object> features = featureEngineeringService
                    .extractPaymentFeatures(request);
                
                // Get payment optimization model
                MLModel model = getPaymentOptimizationModel();
                
                // Make prediction
                PredictionResult result = pythonMLClient.predict(
                    model.getModelId(), features);
                
                // Parse optimization recommendations
                PaymentOptimizationPrediction prediction = parsePaymentOptimization(
                    result, request);
                
                // Store prediction
                predictionRepository.save(prediction);
                
                log.info("Payment optimization completed with {} recommendations", 
                        prediction.getRecommendations().size());
                
                return prediction;
                
            } catch (Exception e) {
                log.error("Failed to optimize payments", e);
                throw new PredictionException("Payment optimization failed", e);
            }
        });
    }

    /**
     * Predict program demand and resource needs
     */
    public CompletableFuture<DemandForecast> forecastDemand(
            DemandForecastRequest request) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Forecasting demand for program: {} region: {}", 
                        request.getProgramId(), request.getRegion());
                
                // Extract time series features
                Map<String, Object> features = featureEngineeringService
                    .extractTimeSeriesFeatures(request);
                
                // Get demand forecasting model
                MLModel model = getDemandForecastModel();
                
                // Make prediction
                PredictionResult result = tensorFlowClient.predict(
                    model.getModelId(), features);
                
                // Parse forecast results
                DemandForecast forecast = parseDemandForecast(result, request);
                
                // Store forecast
                predictionRepository.save(forecast);
                
                log.info("Demand forecast completed for {} periods", 
                        forecast.getForecastPeriods().size());
                
                return forecast;
                
            } catch (Exception e) {
                log.error("Failed to forecast demand", e);
                throw new PredictionException("Demand forecasting failed", e);
            }
        });
    }

    /**
     * Predict case outcomes and resolution times
     */
    public CompletableFuture<CaseOutcomePrediction> predictCaseOutcome(
            String caseId) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Predicting outcome for case: {}", caseId);
                
                // Extract case features
                Map<String, Object> features = featureEngineeringService
                    .extractCaseFeatures(caseId);
                
                // Get case outcome model
                MLModel model = getCaseOutcomeModel();
                
                // Make prediction
                PredictionResult result = pythonMLClient.predict(
                    model.getModelId(), features);
                
                // Create prediction response
                CaseOutcomePrediction prediction = CaseOutcomePrediction.builder()
                    .caseId(caseId)
                    .predictedOutcome(result.getPredictedClass())
                    .confidence(result.getProbability())
                    .estimatedResolutionDays(result.getRegressionValue().intValue())
                    .influencingFactors(result.getFeatureImportance())
                    .predictedAt(LocalDateTime.now())
                    .modelVersion(model.getVersion())
                    .build();
                
                // Store prediction
                predictionRepository.save(prediction);
                
                log.info("Case outcome prediction completed: {} (confidence: {})", 
                        prediction.getPredictedOutcome(), prediction.getConfidence());
                
                return prediction;
                
            } catch (Exception e) {
                log.error("Failed to predict case outcome", e);
                throw new PredictionException("Case outcome prediction failed", e);
            }
        });
    }

    /**
     * Get model performance metrics
     */
    public ModelPerformanceMetrics getModelPerformance(String modelId) {
        try {
            MLModel model = getModel(modelId);
            
            // Get performance metrics from the model
            Map<String, Double> metrics = tensorFlowClient.getModelMetrics(modelId);
            
            return ModelPerformanceMetrics.builder()
                .modelId(modelId)
                .modelType(model.getType())
                .accuracy(metrics.get("accuracy"))
                .precision(metrics.get("precision"))
                .recall(metrics.get("recall"))
                .f1Score(metrics.get("f1_score"))
                .auc(metrics.get("auc"))
                .lastEvaluated(LocalDateTime.now())
                .build();
                
        } catch (Exception e) {
            log.error("Failed to get model performance for: {}", modelId, e);
            throw new ModelException("Failed to get model performance", e);
        }
    }

    /**
     * Retrain models with new data
     */
    public CompletableFuture<ModelTrainingResult> retrainModel(String modelId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Starting model retraining for: {}", modelId);
                
                MLModel model = getModel(modelId);
                
                // Trigger model retraining
                ModelTrainingResult result = modelTrainingService.retrainModel(model);
                
                log.info("Model retraining completed for: {} (accuracy: {})", 
                        modelId, result.getAccuracy());
                
                return result;
                
            } catch (Exception e) {
                log.error("Failed to retrain model: {}", modelId, e);
                throw new ModelException("Model retraining failed", e);
            }
        });
    }

    private MLModel getEligibilityModel(String programId) {
        return getModel("eligibility_" + programId);
    }

    private MLModel getFraudDetectionModel(String type) {
        return getModel("fraud_detection_" + type);
    }

    private MLModel getPaymentOptimizationModel() {
        return getModel("payment_optimization");
    }

    private MLModel getDemandForecastModel() {
        return getModel("demand_forecast");
    }

    private MLModel getCaseOutcomeModel() {
        return getModel("case_outcome");
    }

    private MLModel getModel(String modelId) {
        // Implementation would retrieve model from repository
        // For now, return a mock model
        return MLModel.builder()
            .modelId(modelId)
            .version("1.0")
            .type("classification")
            .lastTrained(LocalDateTime.now().minusHours(modelRefreshHours))
            .build();
    }

    private FraudRiskLevel determineRiskLevel(double riskScore) {
        if (riskScore >= 0.8) return FraudRiskLevel.HIGH;
        if (riskScore >= 0.5) return FraudRiskLevel.MEDIUM;
        return FraudRiskLevel.LOW;
    }

    private List<String> getRecommendedActions(FraudRiskLevel riskLevel) {
        return switch (riskLevel) {
            case HIGH -> List.of(
                "Immediate manual review required",
                "Suspend processing until verification",
                "Contact applicant for additional documentation"
            );
            case MEDIUM -> List.of(
                "Enhanced verification required",
                "Additional documentation review",
                "Supervisor approval needed"
            );
            case LOW -> List.of(
                "Standard processing",
                "Routine verification"
            );
        };
    }

    private void triggerFraudAlert(FraudRiskPrediction prediction) {
        // Implementation would send alerts to fraud investigation team
        log.warn("High fraud risk detected for entity: {} (score: {})", 
                prediction.getEntityId(), prediction.getRiskScore());
    }

    private PaymentOptimizationPrediction parsePaymentOptimization(
            PredictionResult result, PaymentOptimizationRequest request) {
        // Implementation would parse the ML model output into payment recommendations
        return PaymentOptimizationPrediction.builder()
            .requestId(request.getRequestId())
            .recommendations(List.of()) // Would be populated from result
            .totalOptimizedAmount(0.0) // Would be calculated from result
            .estimatedSavings(0.0) // Would be calculated from result
            .predictedAt(LocalDateTime.now())
            .build();
    }

    private DemandForecast parseDemandForecast(
            PredictionResult result, DemandForecastRequest request) {
        // Implementation would parse the time series forecast results
        return DemandForecast.builder()
            .programId(request.getProgramId())
            .region(request.getRegion())
            .forecastPeriods(List.of()) // Would be populated from result
            .confidence(result.getProbability())
            .predictedAt(LocalDateTime.now())
            .build();
    }
}
