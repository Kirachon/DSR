package ph.gov.dsr.performance.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

/**
 * Comprehensive Alerting Service
 * Multi-channel alerting with escalation and notification management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertingService {

    private final JavaMailSender mailSender;
    private final SlackNotificationService slackService;
    private final SMSNotificationService smsService;
    private final WebhookNotificationService webhookService;

    @Value("${dsr.alerting.enabled:true}")
    private boolean alertingEnabled;

    @Value("${dsr.alerting.email.enabled:true}")
    private boolean emailAlertsEnabled;

    @Value("${dsr.alerting.slack.enabled:false}")
    private boolean slackAlertsEnabled;

    @Value("${dsr.alerting.sms.enabled:false}")
    private boolean smsAlertsEnabled;

    @Value("${dsr.alerting.webhook.enabled:false}")
    private boolean webhookAlertsEnabled;

    @Value("${dsr.alerting.escalation.enabled:true}")
    private boolean escalationEnabled;

    @Value("${dsr.alerting.escalation.timeout-minutes:30}")
    private int escalationTimeoutMinutes;

    // Alert tracking
    private final Map<String, Alert> activeAlerts = new ConcurrentHashMap<>();
    private final Map<String, AlertEscalation> escalations = new ConcurrentHashMap<>();
    private final List<Alert> alertHistory = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, NotificationChannel> notificationChannels = new ConcurrentHashMap<>();

    /**
     * Send alert through configured channels
     */
    public void sendAlert(Alert alert) {
        if (!alertingEnabled) {
            log.debug("Alerting is disabled, skipping alert: {}", alert.getId());
            return;
        }

        try {
            log.info("Sending alert: {} - {}", alert.getRuleName(), alert.getMessage());
            
            // Store alert
            activeAlerts.put(alert.getId(), alert);
            alertHistory.add(alert);
            
            // Send through configured channels
            List<CompletableFuture<Void>> notificationTasks = new ArrayList<>();
            
            if (emailAlertsEnabled) {
                notificationTasks.add(sendEmailAlert(alert));
            }
            
            if (slackAlertsEnabled) {
                notificationTasks.add(sendSlackAlert(alert));
            }
            
            if (smsAlertsEnabled && alert.getSeverity() == AlertSeverity.CRITICAL) {
                notificationTasks.add(sendSMSAlert(alert));
            }
            
            if (webhookAlertsEnabled) {
                notificationTasks.add(sendWebhookAlert(alert));
            }
            
            // Wait for all notifications to complete
            CompletableFuture.allOf(notificationTasks.toArray(new CompletableFuture[0]))
                .thenRun(() -> log.debug("All notifications sent for alert: {}", alert.getId()))
                .exceptionally(throwable -> {
                    log.error("Error sending notifications for alert: {}", alert.getId(), throwable);
                    return null;
                });
            
            // Set up escalation if enabled
            if (escalationEnabled && alert.getSeverity().ordinal() >= AlertSeverity.HIGH.ordinal()) {
                setupEscalation(alert);
            }
            
        } catch (Exception e) {
            log.error("Error sending alert: {}", alert.getId(), e);
        }
    }

    /**
     * Resolve alert
     */
    public void resolveAlert(String alertId, String resolvedBy, String resolution) {
        try {
            Alert alert = activeAlerts.get(alertId);
            if (alert == null) {
                log.warn("Attempted to resolve non-existent alert: {}", alertId);
                return;
            }
            
            // Update alert status
            alert.setResolved(true);
            alert.setResolvedAt(LocalDateTime.now());
            alert.setResolvedBy(resolvedBy);
            alert.setResolution(resolution);
            
            // Remove from active alerts
            activeAlerts.remove(alertId);
            
            // Cancel escalation if exists
            AlertEscalation escalation = escalations.remove(alertId);
            if (escalation != null) {
                escalation.cancel();
            }
            
            // Send resolution notification
            sendResolutionNotification(alert);
            
            log.info("Alert resolved: {} by {}", alertId, resolvedBy);
            
        } catch (Exception e) {
            log.error("Error resolving alert: {}", alertId, e);
        }
    }

    /**
     * Get recent alerts
     */
    public List<Alert> getRecentAlerts(int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        
        return alertHistory.stream()
            .filter(alert -> alert.getTimestamp().isAfter(cutoff))
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .toList();
    }

    /**
     * Get active alerts
     */
    public List<Alert> getActiveAlerts() {
        return new ArrayList<>(activeAlerts.values());
    }

    /**
     * Get alerting statistics
     */
    public AlertingStats getAlertingStats() {
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        
        long alertsLast24Hours = alertHistory.stream()
            .filter(alert -> alert.getTimestamp().isAfter(last24Hours))
            .count();
        
        long criticalAlertsLast24Hours = alertHistory.stream()
            .filter(alert -> alert.getTimestamp().isAfter(last24Hours))
            .filter(alert -> alert.getSeverity() == AlertSeverity.CRITICAL)
            .count();
        
        return AlertingStats.builder()
            .alertingEnabled(alertingEnabled)
            .activeAlerts(activeAlerts.size())
            .totalAlerts(alertHistory.size())
            .alertsLast24Hours(alertsLast24Hours)
            .criticalAlertsLast24Hours(criticalAlertsLast24Hours)
            .escalationsActive(escalations.size())
            .notificationChannels(notificationChannels.size())
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Configure notification channel
     */
    public void configureNotificationChannel(NotificationChannel channel) {
        try {
            notificationChannels.put(channel.getId(), channel);
            log.info("Configured notification channel: {} - {}", channel.getId(), channel.getType());
            
        } catch (Exception e) {
            log.error("Error configuring notification channel", e);
        }
    }

    /**
     * Test notification channel
     */
    public NotificationTestResult testNotificationChannel(String channelId) {
        try {
            NotificationChannel channel = notificationChannels.get(channelId);
            if (channel == null) {
                return NotificationTestResult.failed("Channel not found: " + channelId);
            }
            
            // Create test alert
            Alert testAlert = Alert.builder()
                .id("test-" + UUID.randomUUID().toString())
                .ruleName("Test Alert")
                .severity(AlertSeverity.LOW)
                .message("This is a test alert to verify notification channel configuration")
                .timestamp(LocalDateTime.now())
                .build();
            
            // Send test notification
            boolean success = sendTestNotification(channel, testAlert);
            
            return success ? 
                NotificationTestResult.success(channelId) :
                NotificationTestResult.failed("Test notification failed");
                
        } catch (Exception e) {
            log.error("Error testing notification channel: {}", channelId, e);
            return NotificationTestResult.failed("Test failed: " + e.getMessage());
        }
    }

    // Private helper methods

    private CompletableFuture<Void> sendEmailAlert(Alert alert) {
        return CompletableFuture.runAsync(() -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(getEmailRecipients(alert.getSeverity()));
                message.setSubject(formatEmailSubject(alert));
                message.setText(formatEmailBody(alert));
                
                mailSender.send(message);
                log.debug("Email alert sent: {}", alert.getId());
                
            } catch (Exception e) {
                log.error("Error sending email alert: {}", alert.getId(), e);
            }
        });
    }

    private CompletableFuture<Void> sendSlackAlert(Alert alert) {
        return CompletableFuture.runAsync(() -> {
            try {
                SlackMessage slackMessage = SlackMessage.builder()
                    .channel(getSlackChannel(alert.getSeverity()))
                    .text(formatSlackMessage(alert))
                    .color(getSlackColor(alert.getSeverity()))
                    .build();
                
                slackService.sendMessage(slackMessage);
                log.debug("Slack alert sent: {}", alert.getId());
                
            } catch (Exception e) {
                log.error("Error sending Slack alert: {}", alert.getId(), e);
            }
        });
    }

    private CompletableFuture<Void> sendSMSAlert(Alert alert) {
        return CompletableFuture.runAsync(() -> {
            try {
                SMSMessage smsMessage = SMSMessage.builder()
                    .recipients(getSMSRecipients(alert.getSeverity()))
                    .message(formatSMSMessage(alert))
                    .build();
                
                smsService.sendMessage(smsMessage);
                log.debug("SMS alert sent: {}", alert.getId());
                
            } catch (Exception e) {
                log.error("Error sending SMS alert: {}", alert.getId(), e);
            }
        });
    }

    private CompletableFuture<Void> sendWebhookAlert(Alert alert) {
        return CompletableFuture.runAsync(() -> {
            try {
                WebhookPayload payload = WebhookPayload.builder()
                    .alert(alert)
                    .timestamp(LocalDateTime.now())
                    .build();
                
                webhookService.sendWebhook(payload);
                log.debug("Webhook alert sent: {}", alert.getId());
                
            } catch (Exception e) {
                log.error("Error sending webhook alert: {}", alert.getId(), e);
            }
        });
    }

    private void setupEscalation(Alert alert) {
        try {
            AlertEscalation escalation = AlertEscalation.builder()
                .alertId(alert.getId())
                .escalationTime(LocalDateTime.now().plusMinutes(escalationTimeoutMinutes))
                .escalated(false)
                .build();
            
            escalations.put(alert.getId(), escalation);
            
            // Schedule escalation check
            CompletableFuture.delayedExecutor(escalationTimeoutMinutes, java.util.concurrent.TimeUnit.MINUTES)
                .execute(() -> checkEscalation(alert.getId()));
            
            log.debug("Escalation setup for alert: {}", alert.getId());
            
        } catch (Exception e) {
            log.error("Error setting up escalation for alert: {}", alert.getId(), e);
        }
    }

    private void checkEscalation(String alertId) {
        try {
            AlertEscalation escalation = escalations.get(alertId);
            Alert alert = activeAlerts.get(alertId);
            
            if (escalation != null && alert != null && !alert.isResolved()) {
                // Escalate alert
                escalateAlert(alert, escalation);
            }
            
        } catch (Exception e) {
            log.error("Error checking escalation for alert: {}", alertId, e);
        }
    }

    private void escalateAlert(Alert alert, AlertEscalation escalation) {
        try {
            log.warn("Escalating unresolved alert: {} - {}", alert.getId(), alert.getRuleName());
            
            // Mark as escalated
            escalation.setEscalated(true);
            escalation.setEscalatedAt(LocalDateTime.now());
            
            // Send escalation notifications
            sendEscalationNotifications(alert);
            
        } catch (Exception e) {
            log.error("Error escalating alert: {}", alert.getId(), e);
        }
    }

    private void sendEscalationNotifications(Alert alert) {
        // Send to escalation contacts (managers, on-call engineers, etc.)
        // Implementation depends on escalation policy
    }

    private void sendResolutionNotification(Alert alert) {
        try {
            // Send resolution notification to relevant channels
            if (emailAlertsEnabled) {
                sendResolutionEmail(alert);
            }
            
            if (slackAlertsEnabled) {
                sendResolutionSlack(alert);
            }
            
        } catch (Exception e) {
            log.error("Error sending resolution notification: {}", alert.getId(), e);
        }
    }

    private boolean sendTestNotification(NotificationChannel channel, Alert testAlert) {
        try {
            switch (channel.getType()) {
                case EMAIL -> {
                    return sendTestEmail(channel, testAlert);
                }
                case SLACK -> {
                    return sendTestSlack(channel, testAlert);
                }
                case SMS -> {
                    return sendTestSMS(channel, testAlert);
                }
                case WEBHOOK -> {
                    return sendTestWebhook(channel, testAlert);
                }
                default -> {
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("Error sending test notification", e);
            return false;
        }
    }

    // Formatting methods

    private String[] getEmailRecipients(AlertSeverity severity) {
        // Return email recipients based on severity
        return new String[]{"admin@dsr.gov.ph"}; // Placeholder
    }

    private String formatEmailSubject(Alert alert) {
        return String.format("[%s] DSR Alert: %s", alert.getSeverity(), alert.getRuleName());
    }

    private String formatEmailBody(Alert alert) {
        return String.format(
            "Alert Details:\n" +
            "Rule: %s\n" +
            "Severity: %s\n" +
            "Message: %s\n" +
            "Time: %s\n" +
            "Alert ID: %s",
            alert.getRuleName(),
            alert.getSeverity(),
            alert.getMessage(),
            alert.getTimestamp(),
            alert.getId()
        );
    }

    private String getSlackChannel(AlertSeverity severity) {
        return switch (severity) {
            case CRITICAL -> "#alerts-critical";
            case HIGH -> "#alerts-high";
            case MEDIUM -> "#alerts-medium";
            case LOW -> "#alerts-low";
        };
    }

    private String formatSlackMessage(Alert alert) {
        return String.format("🚨 *%s*\n%s\n_Time: %s_", 
            alert.getRuleName(), alert.getMessage(), alert.getTimestamp());
    }

    private String getSlackColor(AlertSeverity severity) {
        return switch (severity) {
            case CRITICAL -> "danger";
            case HIGH -> "warning";
            case MEDIUM -> "good";
            case LOW -> "#36a64f";
        };
    }

    private String[] getSMSRecipients(AlertSeverity severity) {
        // Return SMS recipients for critical alerts
        return new String[]{"+63XXXXXXXXXX"}; // Placeholder
    }

    private String formatSMSMessage(Alert alert) {
        return String.format("DSR ALERT [%s]: %s", alert.getSeverity(), alert.getMessage());
    }

    private boolean sendTestEmail(NotificationChannel channel, Alert testAlert) {
        // Implement test email sending
        return true; // Placeholder
    }

    private boolean sendTestSlack(NotificationChannel channel, Alert testAlert) {
        // Implement test Slack sending
        return true; // Placeholder
    }

    private boolean sendTestSMS(NotificationChannel channel, Alert testAlert) {
        // Implement test SMS sending
        return true; // Placeholder
    }

    private boolean sendTestWebhook(NotificationChannel channel, Alert testAlert) {
        // Implement test webhook sending
        return true; // Placeholder
    }

    private void sendResolutionEmail(Alert alert) {
        // Implement resolution email sending
    }

    private void sendResolutionSlack(Alert alert) {
        // Implement resolution Slack sending
    }
}
