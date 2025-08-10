package ph.gov.dsr.compliance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

/**
 * Compliance Rule Engine
 * Executes compliance rules and evaluates compliance status
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ComplianceRuleEngine {

    private final ComplianceDataProvider dataProvider;
    private final RuleExpressionEvaluator expressionEvaluator;
    private final ComplianceContextBuilder contextBuilder;

    // Rule execution cache
    private final Map<String, RuleExecutionResult> executionCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> cacheTimestamps = new ConcurrentHashMap<>();
    private static final int CACHE_DURATION_MINUTES = 5;

    /**
     * Execute all rules for a compliance framework
     */
    public ComplianceCheckResult executeFrameworkRules(ComplianceFramework framework) {
        try {
            log.debug("Executing compliance rules for framework: {}", framework.getId());
            
            List<RuleExecutionResult> ruleResults = new ArrayList<>();
            List<ComplianceViolation> violations = new ArrayList<>();
            
            // Build compliance context
            ComplianceContext context = contextBuilder.buildContext(framework);
            
            // Execute each rule
            for (ComplianceRule rule : framework.getRules()) {
                RuleExecutionResult result = executeRule(rule, context);
                ruleResults.add(result);
                
                if (!result.isPassed()) {
                    violations.add(createViolation(rule, result, framework));
                }
            }
            
            // Calculate overall compliance score
            double complianceScore = calculateComplianceScore(ruleResults);
            
            return ComplianceCheckResult.builder()
                .frameworkId(framework.getId())
                .ruleResults(ruleResults)
                .violations(violations)
                .complianceScore(complianceScore)
                .compliant(violations.isEmpty())
                .executionTime(LocalDateTime.now())
                .build();
                
        } catch (Exception e) {
            log.error("Error executing framework rules: {}", framework.getId(), e);
            return ComplianceCheckResult.error(framework.getId(), e.getMessage());
        }
    }

    /**
     * Execute comprehensive compliance check
     */
    public ComplianceCheckResult executeComprehensiveCheck(ComplianceFramework framework, 
                                                         ComplianceAssessmentRequest request) {
        try {
            log.debug("Executing comprehensive compliance check for framework: {}", framework.getId());
            
            // Build enhanced context with assessment parameters
            ComplianceContext context = contextBuilder.buildEnhancedContext(framework, request);
            
            List<RuleExecutionResult> ruleResults = new ArrayList<>();
            List<ComplianceViolation> violations = new ArrayList<>();
            
            // Execute rules with enhanced data collection
            for (ComplianceRule rule : framework.getRules()) {
                RuleExecutionResult result = executeRuleWithEnhancedContext(rule, context, request);
                ruleResults.add(result);
                
                if (!result.isPassed()) {
                    ComplianceViolation violation = createDetailedViolation(rule, result, framework, request);
                    violations.add(violation);
                }
            }
            
            // Perform cross-rule analysis
            List<ComplianceViolation> crossRuleViolations = performCrossRuleAnalysis(ruleResults, framework, context);
            violations.addAll(crossRuleViolations);
            
            // Calculate comprehensive compliance score
            double complianceScore = calculateComprehensiveScore(ruleResults, crossRuleViolations);
            
            return ComplianceCheckResult.builder()
                .frameworkId(framework.getId())
                .ruleResults(ruleResults)
                .violations(violations)
                .complianceScore(complianceScore)
                .compliant(violations.isEmpty())
                .executionTime(LocalDateTime.now())
                .comprehensive(true)
                .build();
                
        } catch (Exception e) {
            log.error("Error executing comprehensive compliance check: {}", framework.getId(), e);
            return ComplianceCheckResult.error(framework.getId(), e.getMessage());
        }
    }

    /**
     * Execute single compliance rule
     */
    public RuleExecutionResult executeRule(ComplianceRule rule, ComplianceContext context) {
        try {
            log.debug("Executing compliance rule: {}", rule.getId());
            
            // Check cache first
            String cacheKey = generateCacheKey(rule, context);
            if (isCacheValid(cacheKey)) {
                return executionCache.get(cacheKey);
            }
            
            // Prepare rule execution context
            RuleExecutionContext executionContext = prepareExecutionContext(rule, context);
            
            // Evaluate rule condition
            boolean passed = expressionEvaluator.evaluate(rule.getCondition(), executionContext);
            
            // Collect evidence
            Map<String, Object> evidence = collectEvidence(rule, executionContext);
            
            // Calculate rule score
            double ruleScore = calculateRuleScore(rule, passed, evidence);
            
            RuleExecutionResult result = RuleExecutionResult.builder()
                .ruleId(rule.getId())
                .ruleName(rule.getName())
                .passed(passed)
                .score(ruleScore)
                .evidence(evidence)
                .executionTime(LocalDateTime.now())
                .evaluationDetails(createEvaluationDetails(rule, executionContext, passed))
                .build();
            
            // Cache result
            cacheResult(cacheKey, result);
            
            return result;
            
        } catch (Exception e) {
            log.error("Error executing compliance rule: {}", rule.getId(), e);
            return RuleExecutionResult.error(rule.getId(), e.getMessage());
        }
    }

    /**
     * Validate compliance rule
     */
    public RuleValidationResult validateRule(ComplianceRule rule) {
        try {
            log.debug("Validating compliance rule: {}", rule.getId());
            
            List<String> validationErrors = new ArrayList<>();
            
            // Validate rule structure
            if (rule.getId() == null || rule.getId().trim().isEmpty()) {
                validationErrors.add("Rule ID cannot be null or empty");
            }
            
            if (rule.getName() == null || rule.getName().trim().isEmpty()) {
                validationErrors.add("Rule name cannot be null or empty");
            }
            
            if (rule.getCondition() == null || rule.getCondition().trim().isEmpty()) {
                validationErrors.add("Rule condition cannot be null or empty");
            }
            
            // Validate rule condition syntax
            if (rule.getCondition() != null) {
                try {
                    expressionEvaluator.validateExpression(rule.getCondition());
                } catch (Exception e) {
                    validationErrors.add("Invalid rule condition syntax: " + e.getMessage());
                }
            }
            
            // Validate rule category
            if (rule.getCategory() == null || rule.getCategory().trim().isEmpty()) {
                validationErrors.add("Rule category cannot be null or empty");
            }
            
            // Validate severity
            if (rule.getSeverity() == null) {
                validationErrors.add("Rule severity cannot be null");
            }
            
            boolean isValid = validationErrors.isEmpty();
            
            return RuleValidationResult.builder()
                .ruleId(rule.getId())
                .valid(isValid)
                .validationErrors(validationErrors)
                .validatedAt(LocalDateTime.now())
                .build();
                
        } catch (Exception e) {
            log.error("Error validating compliance rule: {}", rule.getId(), e);
            return RuleValidationResult.error(rule.getId(), e.getMessage());
        }
    }

    /**
     * Register framework with rule engine
     */
    public void registerFramework(ComplianceFramework framework) {
        try {
            log.debug("Registering framework with rule engine: {}", framework.getId());
            
            // Validate all rules in the framework
            for (ComplianceRule rule : framework.getRules()) {
                RuleValidationResult validation = validateRule(rule);
                if (!validation.isValid()) {
                    throw new ComplianceException("Invalid rule in framework: " + rule.getId() + 
                        " - " + String.join(", ", validation.getValidationErrors()));
                }
            }
            
            // Pre-compile rule expressions for performance
            precompileRuleExpressions(framework);
            
            log.info("Successfully registered framework: {}", framework.getId());
            
        } catch (Exception e) {
            log.error("Error registering framework: {}", framework.getId(), e);
            throw new ComplianceException("Failed to register framework", e);
        }
    }

    // Private helper methods

    private RuleExecutionResult executeRuleWithEnhancedContext(ComplianceRule rule, 
                                                              ComplianceContext context,
                                                              ComplianceAssessmentRequest request) {
        try {
            // Enhanced execution with additional data collection
            RuleExecutionContext executionContext = prepareEnhancedExecutionContext(rule, context, request);
            
            // Evaluate rule with enhanced context
            boolean passed = expressionEvaluator.evaluate(rule.getCondition(), executionContext);
            
            // Collect comprehensive evidence
            Map<String, Object> evidence = collectComprehensiveEvidence(rule, executionContext, request);
            
            // Calculate enhanced rule score
            double ruleScore = calculateEnhancedRuleScore(rule, passed, evidence, request);
            
            return RuleExecutionResult.builder()
                .ruleId(rule.getId())
                .ruleName(rule.getName())
                .passed(passed)
                .score(ruleScore)
                .evidence(evidence)
                .executionTime(LocalDateTime.now())
                .evaluationDetails(createEnhancedEvaluationDetails(rule, executionContext, passed, request))
                .enhanced(true)
                .build();
                
        } catch (Exception e) {
            log.error("Error executing rule with enhanced context: {}", rule.getId(), e);
            return RuleExecutionResult.error(rule.getId(), e.getMessage());
        }
    }

    private ComplianceViolation createViolation(ComplianceRule rule, RuleExecutionResult result, 
                                              ComplianceFramework framework) {
        return ComplianceViolation.builder()
            .id(UUID.randomUUID().toString())
            .frameworkId(framework.getId())
            .ruleId(rule.getId())
            .ruleName(rule.getName())
            .description(rule.getDescription())
            .severity(rule.getSeverity())
            .category(rule.getCategory())
            .status(ViolationStatus.ACTIVE)
            .detectedAt(LocalDateTime.now())
            .evidence(result.getEvidence())
            .autoRemediable(rule.isAutoRemediable())
            .build();
    }

    private ComplianceViolation createDetailedViolation(ComplianceRule rule, RuleExecutionResult result,
                                                       ComplianceFramework framework,
                                                       ComplianceAssessmentRequest request) {
        ComplianceViolation violation = createViolation(rule, result, framework);
        
        // Add detailed assessment information
        violation.setAssessmentId(request.getAssessmentId());
        violation.setAssessmentScope(request.getScope());
        violation.setDetailedEvidence(result.getEvaluationDetails());
        
        return violation;
    }

    private List<ComplianceViolation> performCrossRuleAnalysis(List<RuleExecutionResult> ruleResults,
                                                             ComplianceFramework framework,
                                                             ComplianceContext context) {
        List<ComplianceViolation> crossRuleViolations = new ArrayList<>();
        
        try {
            // Analyze patterns across multiple rule failures
            Map<String, List<RuleExecutionResult>> failuresByCategory = groupFailuresByCategory(ruleResults);
            
            for (Map.Entry<String, List<RuleExecutionResult>> entry : failuresByCategory.entrySet()) {
                String category = entry.getKey();
                List<RuleExecutionResult> failures = entry.getValue();
                
                if (failures.size() >= 2) { // Multiple failures in same category
                    ComplianceViolation crossRuleViolation = ComplianceViolation.builder()
                        .id(UUID.randomUUID().toString())
                        .frameworkId(framework.getId())
                        .ruleId("CROSS_RULE_" + category.toUpperCase())
                        .ruleName("Multiple " + category + " Violations")
                        .description("Multiple compliance violations detected in " + category + " category")
                        .severity(ViolationSeverity.HIGH)
                        .category("Cross-Rule Analysis")
                        .status(ViolationStatus.ACTIVE)
                        .detectedAt(LocalDateTime.now())
                        .crossRuleViolation(true)
                        .relatedRules(failures.stream().map(RuleExecutionResult::getRuleId).toList())
                        .build();
                    
                    crossRuleViolations.add(crossRuleViolation);
                }
            }
            
        } catch (Exception e) {
            log.error("Error performing cross-rule analysis", e);
        }
        
        return crossRuleViolations;
    }

    private double calculateComplianceScore(List<RuleExecutionResult> ruleResults) {
        if (ruleResults.isEmpty()) {
            return 0.0;
        }
        
        double totalScore = ruleResults.stream()
            .mapToDouble(RuleExecutionResult::getScore)
            .sum();
        
        return totalScore / ruleResults.size();
    }

    private double calculateComprehensiveScore(List<RuleExecutionResult> ruleResults, 
                                             List<ComplianceViolation> crossRuleViolations) {
        double baseScore = calculateComplianceScore(ruleResults);
        
        // Apply penalty for cross-rule violations
        double penalty = crossRuleViolations.size() * 5.0; // 5% penalty per cross-rule violation
        
        return Math.max(0.0, baseScore - penalty);
    }

    private RuleExecutionContext prepareExecutionContext(ComplianceRule rule, ComplianceContext context) {
        return RuleExecutionContext.builder()
            .rule(rule)
            .complianceContext(context)
            .dataProvider(dataProvider)
            .executionTime(LocalDateTime.now())
            .build();
    }

    private RuleExecutionContext prepareEnhancedExecutionContext(ComplianceRule rule, 
                                                               ComplianceContext context,
                                                               ComplianceAssessmentRequest request) {
        RuleExecutionContext baseContext = prepareExecutionContext(rule, context);
        
        // Add assessment-specific data
        baseContext.setAssessmentRequest(request);
        baseContext.setEnhancedDataCollection(true);
        
        return baseContext;
    }

    private Map<String, Object> collectEvidence(ComplianceRule rule, RuleExecutionContext context) {
        Map<String, Object> evidence = new HashMap<>();
        
        try {
            // Collect relevant data points that influenced the rule evaluation
            evidence.put("rule_condition", rule.getCondition());
            evidence.put("evaluation_time", context.getExecutionTime());
            evidence.put("data_sources", context.getDataSources());
            
            // Add rule-specific evidence
            if (rule.getEvidenceCollectors() != null) {
                for (String collector : rule.getEvidenceCollectors()) {
                    Object evidenceData = dataProvider.collectEvidence(collector, context);
                    evidence.put(collector, evidenceData);
                }
            }
            
        } catch (Exception e) {
            log.error("Error collecting evidence for rule: {}", rule.getId(), e);
            evidence.put("evidence_collection_error", e.getMessage());
        }
        
        return evidence;
    }

    private Map<String, Object> collectComprehensiveEvidence(ComplianceRule rule, 
                                                           RuleExecutionContext context,
                                                           ComplianceAssessmentRequest request) {
        Map<String, Object> evidence = collectEvidence(rule, context);
        
        // Add comprehensive assessment evidence
        evidence.put("assessment_scope", request.getScope());
        evidence.put("assessment_parameters", request.getParameters());
        
        return evidence;
    }

    private double calculateRuleScore(ComplianceRule rule, boolean passed, Map<String, Object> evidence) {
        if (passed) {
            return 100.0;
        }
        
        // Calculate partial score based on evidence and rule severity
        double partialScore = 0.0;
        
        switch (rule.getSeverity()) {
            case CRITICAL -> partialScore = 0.0;  // No partial credit for critical rules
            case HIGH -> partialScore = 10.0;     // Minimal partial credit
            case MEDIUM -> partialScore = 25.0;   // Some partial credit
            case LOW -> partialScore = 50.0;      // Significant partial credit
        }
        
        return partialScore;
    }

    private double calculateEnhancedRuleScore(ComplianceRule rule, boolean passed, 
                                            Map<String, Object> evidence,
                                            ComplianceAssessmentRequest request) {
        double baseScore = calculateRuleScore(rule, passed, evidence);
        
        // Apply assessment-specific scoring adjustments
        if (request.getStrictMode() != null && request.getStrictMode()) {
            baseScore *= 0.8; // Reduce partial scores in strict mode
        }
        
        return baseScore;
    }

    private String generateCacheKey(ComplianceRule rule, ComplianceContext context) {
        return rule.getId() + "_" + context.getContextHash();
    }

    private boolean isCacheValid(String cacheKey) {
        LocalDateTime cacheTime = cacheTimestamps.get(cacheKey);
        return cacheTime != null && 
               cacheTime.isAfter(LocalDateTime.now().minusMinutes(CACHE_DURATION_MINUTES));
    }

    private void cacheResult(String cacheKey, RuleExecutionResult result) {
        executionCache.put(cacheKey, result);
        cacheTimestamps.put(cacheKey, LocalDateTime.now());
    }

    private void precompileRuleExpressions(ComplianceFramework framework) {
        for (ComplianceRule rule : framework.getRules()) {
            try {
                expressionEvaluator.precompile(rule.getCondition());
            } catch (Exception e) {
                log.warn("Failed to precompile expression for rule: {}", rule.getId(), e);
            }
        }
    }

    private Map<String, List<RuleExecutionResult>> groupFailuresByCategory(List<RuleExecutionResult> ruleResults) {
        Map<String, List<RuleExecutionResult>> failuresByCategory = new HashMap<>();
        
        for (RuleExecutionResult result : ruleResults) {
            if (!result.isPassed()) {
                String category = result.getCategory() != null ? result.getCategory() : "Unknown";
                failuresByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(result);
            }
        }
        
        return failuresByCategory;
    }

    private Map<String, Object> createEvaluationDetails(ComplianceRule rule, 
                                                       RuleExecutionContext context, 
                                                       boolean passed) {
        Map<String, Object> details = new HashMap<>();
        details.put("rule_id", rule.getId());
        details.put("condition", rule.getCondition());
        details.put("passed", passed);
        details.put("evaluation_time", context.getExecutionTime());
        return details;
    }

    private Map<String, Object> createEnhancedEvaluationDetails(ComplianceRule rule,
                                                              RuleExecutionContext context,
                                                              boolean passed,
                                                              ComplianceAssessmentRequest request) {
        Map<String, Object> details = createEvaluationDetails(rule, context, passed);
        details.put("assessment_id", request.getAssessmentId());
        details.put("enhanced_evaluation", true);
        return details;
    }
}
