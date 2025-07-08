package ph.gov.dsr.disaster.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ph.gov.dsr.disaster.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Disaster Recovery Management Controller
 */
@RestController
@RequestMapping("/api/v1/admin/disaster-recovery")
@RequiredArgsConstructor
@Tag(name = "Disaster Recovery", description = "Comprehensive disaster recovery and business continuity")
public class DisasterRecoveryController {

    private final DisasterRecoveryService disasterRecoveryService;
    private final BackupService backupService;
    private final FailoverService failoverService;
    private final RecoveryService recoveryService;

    @GetMapping("/health")
    @Operation(summary = "Get disaster recovery system health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDisasterRecoveryHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            DisasterRecoveryStatus drStatus = disasterRecoveryService.getDisasterRecoveryStatus();
            BackupStatus backupStatus = backupService.getBackupStatus();
            FailoverServiceStatus failoverStatus = failoverService.getFailoverStatus();
            
            health.put("disasterRecovery", Map.of(
                "enabled", drStatus.isEnabled(),
                "autoFailoverEnabled", drStatus.isAutoFailoverEnabled(),
                "rtoMinutes", drStatus.getRtoMinutes(),
                "rpoMinutes", drStatus.getRpoMinutes(),
                "monitoredComponents", drStatus.getMonitoredComponents(),
                "activeDisasters", drStatus.getActiveDisasters().size()
            ));
            
            health.put("backup", Map.of(
                "lastBackupTime", backupStatus.getLastBackupTime(),
                "integrityVerified", backupStatus.isIntegrityVerified(),
                "remoteStorageStatus", backupStatus.getRemoteStorageStatus(),
                "activeBackups", backupStatus.getActiveBackups().size()
            ));
            
            health.put("failover", Map.of(
                "enabled", failoverStatus.isEnabled(),
                "automaticEnabled", failoverStatus.isAutomaticFailoverEnabled(),
                "primarySite", failoverStatus.getPrimarySite(),
                "secondarySites", failoverStatus.getSecondarySites().size(),
                "failoverReady", failoverStatus.getFailoverReadiness().isReady()
            ));
            
            health.put("status", "OPERATIONAL");
            health.put("timestamp", drStatus.getTimestamp());
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            health.put("status", "ERROR");
            health.put("error", e.getMessage());
            return ResponseEntity.status(500).body(health);
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Get comprehensive disaster recovery status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DisasterRecoveryStatus> getDisasterRecoveryStatus() {
        try {
            DisasterRecoveryStatus status = disasterRecoveryService.getDisasterRecoveryStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/initiate")
    @Operation(summary = "Initiate disaster recovery")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DisasterRecoveryResult> initiateDisasterRecovery(@RequestBody DisasterRecoveryRequest request) {
        try {
            DisasterRecoveryResult result = disasterRecoveryService.initiateDisasterRecovery(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/test")
    @Operation(summary = "Test disaster recovery procedures")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DisasterRecoveryTestResult> testDisasterRecovery(@RequestBody DisasterRecoveryTestRequest request) {
        try {
            DisasterRecoveryTestResult result = disasterRecoveryService.testDisasterRecovery(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/backup/execute")
    @Operation(summary = "Execute manual backup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BackupResult> executeBackup(@RequestBody BackupPlan plan) {
        try {
            BackupResult result = backupService.executeBackup(plan);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/backup/status")
    @Operation(summary = "Get backup status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BackupStatus> getBackupStatus() {
        try {
            BackupStatus status = backupService.getBackupStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/backup/{backupId}/verify")
    @Operation(summary = "Verify backup integrity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BackupIntegrityResult> verifyBackupIntegrity(@PathVariable String backupId) {
        try {
            BackupIntegrityResult result = backupService.verifyBackupIntegrity(backupId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/restore")
    @Operation(summary = "Restore from backup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestoreResult> restoreFromBackup(@RequestBody RestoreRequest request) {
        try {
            RestoreResult result = backupService.restoreFromBackup(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/failover/execute")
    @Operation(summary = "Execute manual failover")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FailoverResult> executeFailover(@RequestBody FailoverRequest request) {
        try {
            FailoverResult result = disasterRecoveryService.performFailover(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/failover/readiness")
    @Operation(summary = "Get failover readiness status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FailoverReadiness> getFailoverReadiness() {
        try {
            FailoverReadiness readiness = failoverService.getFailoverReadiness();
            return ResponseEntity.ok(readiness);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/failover/status")
    @Operation(summary = "Get failover service status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FailoverServiceStatus> getFailoverStatus() {
        try {
            FailoverServiceStatus status = failoverService.getFailoverStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/failover/check")
    @Operation(summary = "Trigger manual failover check")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> triggerFailoverCheck() {
        try {
            failoverService.performAutomaticFailoverCheck();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "completed");
            response.put("message", "Manual failover check performed");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/monitoring/trigger")
    @Operation(summary = "Trigger manual disaster recovery monitoring")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> triggerMonitoring() {
        try {
            disasterRecoveryService.performContinuousMonitoring();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "completed");
            response.put("message", "Manual disaster recovery monitoring performed");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/backup/trigger")
    @Operation(summary = "Trigger manual backup process")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> triggerBackup() {
        try {
            disasterRecoveryService.performAutomatedBackup();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "initiated");
            response.put("message", "Manual backup process initiated");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/recovery-plans")
    @Operation(summary = "Get available recovery plans")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RecoveryPlan>> getRecoveryPlans() {
        try {
            List<RecoveryPlan> plans = recoveryService.getAvailableRecoveryPlans();
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/recovery-plans")
    @Operation(summary = "Create custom recovery plan")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> createRecoveryPlan(@RequestBody RecoveryPlan plan) {
        try {
            recoveryService.createRecoveryPlan(plan);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "created");
            response.put("planId", plan.getId());
            response.put("planName", plan.getName());
            response.put("message", "Recovery plan created successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/disasters/active")
    @Operation(summary = "Get active disaster events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DisasterEvent>> getActiveDisasters() {
        try {
            List<DisasterEvent> disasters = disasterRecoveryService.getActiveDisasters();
            return ResponseEntity.ok(disasters);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/disasters/history")
    @Operation(summary = "Get disaster recovery history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DisasterEvent>> getDisasterHistory(@RequestParam(defaultValue = "30") int days) {
        try {
            List<DisasterEvent> history = disasterRecoveryService.getDisasterHistory(days);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get disaster recovery metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DisasterRecoveryMetrics> getDisasterRecoveryMetrics() {
        try {
            DisasterRecoveryMetrics metrics = disasterRecoveryService.getDisasterRecoveryMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/rto-rpo")
    @Operation(summary = "Get current RTO/RPO status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRtoRpoStatus() {
        try {
            DisasterRecoveryStatus status = disasterRecoveryService.getDisasterRecoveryStatus();
            
            Map<String, Object> rtoRpo = new HashMap<>();
            rtoRpo.put("rto", Map.of(
                "targetMinutes", status.getRtoMinutes(),
                "currentMinutes", status.getRecoveryMetrics().getCurrentRto(),
                "status", status.getRecoveryMetrics().getCurrentRto() <= status.getRtoMinutes() ? "COMPLIANT" : "NON_COMPLIANT"
            ));
            
            rtoRpo.put("rpo", Map.of(
                "targetMinutes", status.getRpoMinutes(),
                "currentMinutes", status.getRecoveryMetrics().getCurrentRpo(),
                "status", status.getRecoveryMetrics().getCurrentRpo() <= status.getRpoMinutes() ? "COMPLIANT" : "NON_COMPLIANT"
            ));
            
            rtoRpo.put("timestamp", status.getTimestamp());
            
            return ResponseEntity.ok(rtoRpo);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/backup/cleanup")
    @Operation(summary = "Cleanup old backups")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> cleanupOldBackups(@RequestParam(defaultValue = "30") int retentionDays) {
        try {
            java.time.LocalDateTime cutoffDate = java.time.LocalDateTime.now().minusDays(retentionDays);
            backupService.cleanupBackupsOlderThan(cutoffDate);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "completed");
            response.put("message", "Old backups cleaned up successfully");
            response.put("retentionDays", String.valueOf(retentionDays));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/components/status")
    @Operation(summary = "Get monitored components status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SystemComponent>> getMonitoredComponentsStatus() {
        try {
            List<SystemComponent> components = disasterRecoveryService.getMonitoredComponents();
            return ResponseEntity.ok(components);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/components/register")
    @Operation(summary = "Register component for monitoring")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> registerComponent(@RequestBody SystemComponent component) {
        try {
            disasterRecoveryService.registerComponent(component);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "registered");
            response.put("componentId", component.getId());
            response.put("componentName", component.getName());
            response.put("message", "Component registered for monitoring");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/settings/update")
    @Operation(summary = "Update disaster recovery settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updateDisasterRecoverySettings(@RequestBody DisasterRecoverySettings settings) {
        try {
            disasterRecoveryService.updateSettings(settings);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "updated");
            response.put("message", "Disaster recovery settings updated successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}
