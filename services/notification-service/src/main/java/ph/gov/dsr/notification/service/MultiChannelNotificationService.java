package ph.gov.dsr.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.notification.dto.*;
import ph.gov.dsr.notification.entity.NotificationTemplate;
import ph.gov.dsr.notification.entity.NotificationLog;
import ph.gov.dsr.notification.entity.UserPreferences;
import ph.gov.dsr.notification.repository.NotificationLogRepository;
import ph.gov.dsr.notification.repository.UserPreferencesRepository;
import ph.gov.dsr.notification.service.channels.*;
import ph.gov.dsr.notification.service.template.TemplateEngine;
import ph.gov.dsr.notification.service.analytics.NotificationAnalyticsService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Multi-channel notification service that supports SMS, Email, Push, Voice, and WhatsApp
 * with template engine, A/B testing, and analytics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MultiChannelNotificationService {

    private final SmsNotificationService smsService;
    private final EmailNotificationService emailService;
    private final PushNotificationService pushService;
    private final VoiceNotificationService voiceService;
    private final WhatsAppNotificationService whatsAppService;
    
    private final TemplateEngine templateEngine;
    private final NotificationAnalyticsService analyticsService;
    
    private final NotificationLogRepository notificationLogRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * Send notification through multiple channels based on user preferences
     */
    @Transactional
    public NotificationResponse sendMultiChannelNotification(MultiChannelNotificationRequest request) {
        log.info("Sending multi-channel notification to user: {}", request.getUserId());
        
        try {
            // Get user preferences
            UserPreferences preferences = getUserPreferences(request.getUserId());
            
            // Determine channels to use
            List<NotificationChannel> channels = determineChannels(request, preferences);
            
            // Process template if provided
            NotificationContent content = processTemplate(request);
            
            // Check quiet hours
            if (isQuietHours(preferences) && !request.isUrgent()) {
                return scheduleForLater(request, content, channels);
            }
            
            // Send through each channel asynchronously
            List<CompletableFuture<ChannelResponse>> futures = new ArrayList<>();
            
            for (NotificationChannel channel : channels) {
                CompletableFuture<ChannelResponse> future = sendThroughChannel(
                    channel, content, request, preferences);
                futures.add(future);
            }
            
            // Wait for all channels to complete
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));
            
            List<ChannelResponse> responses = allFutures
                .thenApply(v -> futures.stream()
                    .map(CompletableFuture::join)
                    .toList())
                .get();
            
            // Log notification
            NotificationLog log = createNotificationLog(request, content, responses);
            notificationLogRepository.save(log);
            
            // Update analytics
            analyticsService.recordNotification(request, responses);
            
            return buildResponse(responses, log);
            
        } catch (Exception e) {
            log.error("Failed to send multi-channel notification", e);
            throw new NotificationException("Multi-channel notification failed", e);
        }
    }

    /**
     * Send bulk notifications with rate limiting and optimization
     */
    @Transactional
    public BulkNotificationResponse sendBulkNotifications(BulkNotificationRequest request) {
        log.info("Sending bulk notifications to {} recipients", request.getRecipients().size());
        
        try {
            List<CompletableFuture<NotificationResponse>> futures = new ArrayList<>();
            
            // Process in batches to avoid overwhelming the system
            int batchSize = 100;
            List<List<String>> batches = partitionList(request.getRecipients(), batchSize);
            
            for (List<String> batch : batches) {
                CompletableFuture<NotificationResponse> future = CompletableFuture.supplyAsync(() -> {
                    return processBatch(batch, request);
                }, executorService);
                
                futures.add(future);
            }
            
            // Wait for all batches to complete
            List<NotificationResponse> responses = futures.stream()
                .map(CompletableFuture::join)
                .toList();
            
            return aggregateBulkResponses(responses);
            
        } catch (Exception e) {
            log.error("Failed to send bulk notifications", e);
            throw new NotificationException("Bulk notification failed", e);
        }
    }

    /**
     * Schedule notification for future delivery
     */
    @Transactional
    public ScheduledNotificationResponse scheduleNotification(ScheduledNotificationRequest request) {
        log.info("Scheduling notification for delivery at: {}", request.getScheduledTime());
        
        try {
            // Validate schedule time
            if (request.getScheduledTime().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Scheduled time must be in the future");
            }
            
            // Process template
            NotificationContent content = processTemplate(request);
            
            // Create scheduled notification record
            ScheduledNotification scheduled = new ScheduledNotification();
            scheduled.setUserId(request.getUserId());
            scheduled.setContent(content);
            scheduled.setScheduledTime(request.getScheduledTime());
            scheduled.setChannels(request.getChannels());
            scheduled.setStatus(ScheduledNotificationStatus.PENDING);
            
            scheduledNotificationRepository.save(scheduled);
            
            return ScheduledNotificationResponse.builder()
                .scheduledId(scheduled.getId())
                .scheduledTime(scheduled.getScheduledTime())
                .status("SCHEDULED")
                .build();
                
        } catch (Exception e) {
            log.error("Failed to schedule notification", e);
            throw new NotificationException("Notification scheduling failed", e);
        }
    }

    /**
     * Send A/B test notifications
     */
    @Transactional
    public ABTestResponse sendABTestNotification(ABTestRequest request) {
        log.info("Starting A/B test notification: {}", request.getTestName());
        
        try {
            // Split recipients into test groups
            Map<String, List<String>> testGroups = splitIntoTestGroups(
                request.getRecipients(), request.getVariants());
            
            List<CompletableFuture<VariantResult>> futures = new ArrayList<>();
            
            for (Map.Entry<String, List<String>> group : testGroups.entrySet()) {
                String variantId = group.getKey();
                List<String> recipients = group.getValue();
                NotificationVariant variant = request.getVariants().get(variantId);
                
                CompletableFuture<VariantResult> future = CompletableFuture.supplyAsync(() -> {
                    return processVariant(variantId, variant, recipients, request);
                }, executorService);
                
                futures.add(future);
            }
            
            // Wait for all variants to complete
            List<VariantResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();
            
            // Record A/B test results
            ABTestResult testResult = new ABTestResult();
            testResult.setTestName(request.getTestName());
            testResult.setVariantResults(results);
            testResult.setStartTime(LocalDateTime.now());
            
            abTestResultRepository.save(testResult);
            
            return ABTestResponse.builder()
                .testId(testResult.getId())
                .testName(request.getTestName())
                .variantResults(results)
                .build();
                
        } catch (Exception e) {
            log.error("Failed to send A/B test notification", e);
            throw new NotificationException("A/B test notification failed", e);
        }
    }

    private CompletableFuture<ChannelResponse> sendThroughChannel(
            NotificationChannel channel, 
            NotificationContent content, 
            MultiChannelNotificationRequest request,
            UserPreferences preferences) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                switch (channel) {
                    case SMS:
                        return smsService.sendSms(SmsRequest.builder()
                            .phoneNumber(preferences.getPhoneNumber())
                            .message(content.getSmsContent())
                            .priority(request.getPriority())
                            .build());
                            
                    case EMAIL:
                        return emailService.sendEmail(EmailRequest.builder()
                            .toEmail(preferences.getEmail())
                            .subject(content.getEmailSubject())
                            .htmlContent(content.getEmailContent())
                            .priority(request.getPriority())
                            .build());
                            
                    case PUSH:
                        return pushService.sendPushNotification(PushRequest.builder()
                            .deviceToken(preferences.getDeviceToken())
                            .title(content.getPushTitle())
                            .body(content.getPushBody())
                            .data(request.getData())
                            .build());
                            
                    case VOICE:
                        return voiceService.makeVoiceCall(VoiceRequest.builder()
                            .phoneNumber(preferences.getPhoneNumber())
                            .message(content.getVoiceContent())
                            .language(preferences.getLanguage())
                            .build());
                            
                    case WHATSAPP:
                        return whatsAppService.sendWhatsAppMessage(WhatsAppRequest.builder()
                            .phoneNumber(preferences.getWhatsAppNumber())
                            .message(content.getWhatsAppContent())
                            .templateId(content.getWhatsAppTemplateId())
                            .build());
                            
                    default:
                        throw new UnsupportedOperationException("Channel not supported: " + channel);
                }
            } catch (Exception e) {
                log.error("Failed to send through channel: {}", channel, e);
                return ChannelResponse.builder()
                    .channel(channel)
                    .success(false)
                    .error(e.getMessage())
                    .build();
            }
        }, executorService);
    }

    private NotificationContent processTemplate(NotificationRequest request) {
        if (request.getTemplateId() != null) {
            NotificationTemplate template = templateService.getTemplate(request.getTemplateId());
            return templateEngine.processTemplate(template, request.getTemplateData());
        } else {
            return NotificationContent.fromRequest(request);
        }
    }

    private List<NotificationChannel> determineChannels(
            MultiChannelNotificationRequest request, 
            UserPreferences preferences) {
        
        List<NotificationChannel> channels = new ArrayList<>();
        
        // Use requested channels if specified
        if (request.getChannels() != null && !request.getChannels().isEmpty()) {
            channels.addAll(request.getChannels());
        } else {
            // Use user preferences
            if (preferences.isSmsEnabled()) channels.add(NotificationChannel.SMS);
            if (preferences.isEmailEnabled()) channels.add(NotificationChannel.EMAIL);
            if (preferences.isPushEnabled()) channels.add(NotificationChannel.PUSH);
            if (preferences.isVoiceEnabled() && request.isUrgent()) {
                channels.add(NotificationChannel.VOICE);
            }
            if (preferences.isWhatsAppEnabled()) channels.add(NotificationChannel.WHATSAPP);
        }
        
        // Filter based on notification type preferences
        return channels.stream()
            .filter(channel -> isChannelAllowedForType(channel, request.getType(), preferences))
            .toList();
    }

    private boolean isQuietHours(UserPreferences preferences) {
        if (!preferences.isQuietHoursEnabled()) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        int currentHour = now.getHour();
        
        return currentHour >= preferences.getQuietHoursStart() || 
               currentHour < preferences.getQuietHoursEnd();
    }

    private UserPreferences getUserPreferences(String userId) {
        return userPreferencesRepository.findByUserId(userId)
            .orElse(createDefaultPreferences(userId));
    }

    private UserPreferences createDefaultPreferences(String userId) {
        UserPreferences preferences = new UserPreferences();
        preferences.setUserId(userId);
        preferences.setSmsEnabled(true);
        preferences.setEmailEnabled(true);
        preferences.setPushEnabled(true);
        preferences.setVoiceEnabled(false);
        preferences.setWhatsAppEnabled(false);
        preferences.setQuietHoursEnabled(false);
        preferences.setQuietHoursStart(22);
        preferences.setQuietHoursEnd(7);
        
        return userPreferencesRepository.save(preferences);
    }
}
