package ph.gov.dsr.compliance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ph.gov.dsr.compliance.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Compliance Automation Management Controller
 */
@RestController
@RequestMapping("/api/v1/admin/compliance")
@RequiredArgsConstructor
@Tag(name = "Compliance Automation", description = "Automated compliance monitoring and reporting")
public class ComplianceController {

    private final ComplianceAutomationService complianceService;
    private final ComplianceFrameworkRegistry frameworkRegistry;
    private final ComplianceRuleEngine ruleEngine;
    private final ComplianceReportingService reportingService;

    @GetMapping("/health")
    @Operation(summary = "Get compliance system health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getComplianceHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            ComplianceStatistics stats = complianceService.getComplianceStatistics();
            
            health.put("compliance", Map.of(
                "automationEnabled", stats.isAutomationEnabled(),
                "continuousMonitoring", stats.isContinuousMonitoring(),
                "autoRemediationEnabled", stats.isAutoRemediationEnabled(),
                "activeFrameworks", stats.getActiveFrameworks(),
                "totalChecks", stats.getTotalComplianceChecks(),
                "totalViolations", stats.getTotalViolations(),
                "complianceRate", stats.getComplianceRate()
            ));
            
            health.put("frameworks", Map.of(
                "total", frameworkRegistry.getAllFrameworks().size(),
                "dataPrivacy", frameworkRegistry.getFrameworksByCategory(FrameworkCategory.DATA_PRIVACY).size(),
                "security", frameworkRegistry.getFrameworksByCategory(FrameworkCategory.INFORMATION_SECURITY).size(),
                "government", frameworkRegistry.getFrameworksByCategory(FrameworkCategory.GOVERNMENT_STANDARDS).size()
            ));
            
            health.put("status", "OPERATIONAL");
            health.put("timestamp", stats.getTimestamp());
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            health.put("status", "ERROR");
            health.put("error", e.getMessage());
            return ResponseEntity.status(500).body(health);
        }
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get compliance dashboard data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplianceDashboardData> getComplianceDashboard() {
        try {
            ComplianceDashboardData dashboard = complianceService.getComplianceDashboard();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/assessment")
    @Operation(summary = "Perform compliance assessment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplianceAssessmentResult> performAssessment(@RequestBody ComplianceAssessmentRequest request) {
        try {
            ComplianceAssessmentResult result = complianceService.performComplianceAssessment(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/check/trigger")
    @Operation(summary = "Trigger manual compliance check")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> triggerComplianceCheck() {
        try {
            complianceService.performAutomatedComplianceCheck();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "completed");
            response.put("message", "Manual compliance check performed");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/frameworks")
    @Operation(summary = "Get all compliance frameworks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ComplianceFramework>> getAllFrameworks() {
        try {
            List<ComplianceFramework> frameworks = frameworkRegistry.getAllFrameworks().stream().toList();
            return ResponseEntity.ok(frameworks);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/frameworks/{frameworkId}")
    @Operation(summary = "Get specific compliance framework")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplianceFramework> getFramework(@PathVariable String frameworkId) {
        try {
            ComplianceFramework framework = frameworkRegistry.getFramework(frameworkId);
            if (framework != null) {
                return ResponseEntity.ok(framework);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/frameworks/category/{category}")
    @Operation(summary = "Get frameworks by category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ComplianceFramework>> getFrameworksByCategory(@PathVariable FrameworkCategory category) {
        try {
            List<ComplianceFramework> frameworks = frameworkRegistry.getFrameworksByCategory(category);
            return ResponseEntity.ok(frameworks);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/frameworks")
    @Operation(summary = "Register custom compliance framework")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> registerFramework(@RequestBody ComplianceFramework framework) {
        try {
            complianceService.registerComplianceFramework(framework);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "registered");
            response.put("frameworkId", framework.getId());
            response.put("frameworkName", framework.getName());
            response.put("message", "Compliance framework registered successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/rules/validate")
    @Operation(summary = "Validate compliance rule")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RuleValidationResult> validateRule(@RequestBody ComplianceRule rule) {
        try {
            RuleValidationResult result = ruleEngine.validateRule(rule);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/rules/execute")
    @Operation(summary = "Execute single compliance rule")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RuleExecutionResult> executeRule(
            @RequestBody ComplianceRule rule,
            @RequestParam String frameworkId) {
        try {
            ComplianceFramework framework = frameworkRegistry.getFramework(frameworkId);
            if (framework == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Build context and execute rule
            ComplianceContext context = new ComplianceContextBuilder().buildContext(framework);
            RuleExecutionResult result = ruleEngine.executeRule(rule, context);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/reports/generate")
    @Operation(summary = "Generate compliance report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplianceReport> generateReport(@RequestBody ComplianceReportRequest request) {
        try {
            ComplianceReport report = complianceService.generateComplianceReport(request);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/reports/{reportId}")
    @Operation(summary = "Get compliance report by ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplianceReport> getReport(@PathVariable String reportId) {
        try {
            ComplianceReport report = reportingService.getReport(reportId);
            if (report != null) {
                return ResponseEntity.ok(report);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/violations")
    @Operation(summary = "Get active compliance violations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ComplianceViolation>> getActiveViolations() {
        try {
            List<ComplianceViolation> violations = complianceService.getActiveViolations();
            return ResponseEntity.ok(violations);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/violations/recent/{hours}")
    @Operation(summary = "Get recent compliance violations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ComplianceViolation>> getRecentViolations(@PathVariable int hours) {
        try {
            List<ComplianceViolation> violations = complianceService.getRecentViolations(hours);
            return ResponseEntity.ok(violations);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/violations/{violationId}/remediate")
    @Operation(summary = "Trigger violation remediation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RemediationResult> remediateViolation(
            @PathVariable String violationId,
            @RequestParam RemediationType type) {
        try {
            RemediationResult result = complianceService.triggerRemediation(violationId, type);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get compliance statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplianceStatistics> getComplianceStatistics() {
        try {
            ComplianceStatistics stats = complianceService.getComplianceStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/trends/{timeRange}")
    @Operation(summary = "Get compliance trends")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplianceTrends> getComplianceTrends(@PathVariable String timeRange) {
        try {
            ComplianceTrends trends = complianceService.getComplianceTrends(timeRange);
            return ResponseEntity.ok(trends);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/audit/export")
    @Operation(summary = "Export compliance audit trail")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplianceAuditExport> exportAuditTrail(@RequestBody ComplianceAuditExportRequest request) {
        try {
            ComplianceAuditExport export = complianceService.exportAuditTrail(request);
            return ResponseEntity.ok(export);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/frameworks/{frameworkId}/status")
    @Operation(summary = "Get framework compliance status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplianceStatus> getFrameworkStatus(@PathVariable String frameworkId) {
        try {
            ComplianceStatus status = complianceService.getFrameworkStatus(frameworkId);
            if (status != null) {
                return ResponseEntity.ok(status);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/frameworks/{frameworkId}/check")
    @Operation(summary = "Check specific framework compliance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplianceCheckResult> checkFrameworkCompliance(@PathVariable String frameworkId) {
        try {
            ComplianceFramework framework = frameworkRegistry.getFramework(frameworkId);
            if (framework == null) {
                return ResponseEntity.notFound().build();
            }
            
            ComplianceCheckResult result = ruleEngine.executeFrameworkRules(framework);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/recommendations")
    @Operation(summary = "Get compliance recommendations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ComplianceRecommendation>> getComplianceRecommendations() {
        try {
            List<ComplianceRecommendation> recommendations = complianceService.getComplianceRecommendations();
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/settings/update")
    @Operation(summary = "Update compliance settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updateComplianceSettings(@RequestBody ComplianceSettings settings) {
        try {
            complianceService.updateComplianceSettings(settings);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "updated");
            response.put("message", "Compliance settings updated successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}
