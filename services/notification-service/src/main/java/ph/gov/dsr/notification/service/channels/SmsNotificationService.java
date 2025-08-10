package ph.gov.dsr.notification.service.channels;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ph.gov.dsr.notification.dto.SmsRequest;
import ph.gov.dsr.notification.dto.ChannelResponse;
import ph.gov.dsr.notification.dto.NotificationChannel;
import ph.gov.dsr.notification.integration.TwilioSmsClient;
import ph.gov.dsr.notification.integration.GlobeSmsClient;
import ph.gov.dsr.notification.integration.SmartSmsClient;
import ph.gov.dsr.notification.integration.DitoSmsClient;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * SMS notification service with multiple provider support and failover
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmsNotificationService {

    private final TwilioSmsClient twilioClient;
    private final GlobeSmsClient globeClient;
    private final SmartSmsClient smartClient;
    private final DitoSmsClient ditoClient;

    @Value("${dsr.notification.sms.primary-provider:twilio}")
    private String primaryProvider;

    @Value("${dsr.notification.sms.failover-enabled:true}")
    private boolean failoverEnabled;

    @Value("${dsr.notification.sms.rate-limit:100}")
    private int rateLimit;

    /**
     * Send SMS with automatic provider selection and failover
     */
    public ChannelResponse sendSms(SmsRequest request) {
        log.info("Sending SMS to: {} via provider: {}", 
                maskPhoneNumber(request.getPhoneNumber()), primaryProvider);
        
        try {
            // Validate phone number format
            validatePhoneNumber(request.getPhoneNumber());
            
            // Check rate limiting
            if (!checkRateLimit(request.getPhoneNumber())) {
                return ChannelResponse.builder()
                    .channel(NotificationChannel.SMS)
                    .success(false)
                    .error("Rate limit exceeded")
                    .timestamp(LocalDateTime.now())
                    .build();
            }
            
            // Determine best provider based on phone number
            String provider = selectProvider(request.getPhoneNumber());
            
            // Send SMS with failover
            return sendWithFailover(request, provider);
            
        } catch (Exception e) {
            log.error("Failed to send SMS", e);
            return ChannelResponse.builder()
                .channel(NotificationChannel.SMS)
                .success(false)
                .error(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        }
    }

    /**
     * Send bulk SMS messages with optimization
     */
    public CompletableFuture<Map<String, ChannelResponse>> sendBulkSms(
            Map<String, SmsRequest> requests) {
        
        log.info("Sending bulk SMS to {} recipients", requests.size());
        
        return CompletableFuture.supplyAsync(() -> {
            Map<String, ChannelResponse> responses = new HashMap<>();
            
            // Group by provider for batch optimization
            Map<String, List<SmsRequest>> providerGroups = groupByProvider(requests);
            
            for (Map.Entry<String, List<SmsRequest>> group : providerGroups.entrySet()) {
                String provider = group.getKey();
                List<SmsRequest> providerRequests = group.getValue();
                
                try {
                    Map<String, ChannelResponse> providerResponses = 
                        sendBulkToProvider(provider, providerRequests);
                    responses.putAll(providerResponses);
                } catch (Exception e) {
                    log.error("Bulk SMS failed for provider: {}", provider, e);
                    // Mark all requests as failed
                    for (SmsRequest req : providerRequests) {
                        responses.put(req.getPhoneNumber(), ChannelResponse.builder()
                            .channel(NotificationChannel.SMS)
                            .success(false)
                            .error("Provider error: " + e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
                    }
                }
            }
            
            return responses;
        });
    }

    private ChannelResponse sendWithFailover(SmsRequest request, String provider) {
        ChannelResponse response = sendToProvider(request, provider);
        
        if (!response.isSuccess() && failoverEnabled) {
            log.warn("Primary provider {} failed, attempting failover", provider);
            
            // Try other providers
            String[] providers = {"twilio", "globe", "smart", "dito"};
            for (String fallbackProvider : providers) {
                if (!fallbackProvider.equals(provider)) {
                    response = sendToProvider(request, fallbackProvider);
                    if (response.isSuccess()) {
                        log.info("Failover successful with provider: {}", fallbackProvider);
                        break;
                    }
                }
            }
        }
        
        return response;
    }

    private ChannelResponse sendToProvider(SmsRequest request, String provider) {
        try {
            switch (provider.toLowerCase()) {
                case "twilio":
                    return twilioClient.sendSms(request);
                case "globe":
                    return globeClient.sendSms(request);
                case "smart":
                    return smartClient.sendSms(request);
                case "dito":
                    return ditoClient.sendSms(request);
                default:
                    throw new IllegalArgumentException("Unknown SMS provider: " + provider);
            }
        } catch (Exception e) {
            log.error("Failed to send SMS via provider: {}", provider, e);
            return ChannelResponse.builder()
                .channel(NotificationChannel.SMS)
                .success(false)
                .error("Provider error: " + e.getMessage())
                .provider(provider)
                .timestamp(LocalDateTime.now())
                .build();
        }
    }

    private String selectProvider(String phoneNumber) {
        // Smart provider selection based on phone number prefix
        if (phoneNumber.startsWith("+639")) {
            String prefix = phoneNumber.substring(4, 7);
            
            // Globe/TM prefixes
            if (prefix.matches("^(817|905|906|915|916|917|926|927|935|936|937|945|953|954|955|956|965|966|967|975|976|977|994|995|996|997).*")) {
                return "globe";
            }
            // Smart/TNT prefixes
            else if (prefix.matches("^(813|907|908|909|910|912|918|919|920|921|928|929|930|938|939|946|947|948|949|989|998|999).*")) {
                return "smart";
            }
            // DITO prefixes
            else if (prefix.matches("^(895|896|897|898).*")) {
                return "dito";
            }
        }
        
        // Default to primary provider
        return primaryProvider;
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        
        // Philippine mobile number format validation
        if (!phoneNumber.matches("^(\\+63|0)[0-9]{10}$")) {
            throw new IllegalArgumentException("Invalid Philippine mobile number format");
        }
    }

    private boolean checkRateLimit(String phoneNumber) {
        // Implement rate limiting logic
        // This could use Redis or in-memory cache
        return true; // Simplified for now
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber.length() > 4) {
            return phoneNumber.substring(0, 4) + "****" + 
                   phoneNumber.substring(phoneNumber.length() - 2);
        }
        return "****";
    }

    private Map<String, List<SmsRequest>> groupByProvider(Map<String, SmsRequest> requests) {
        Map<String, List<SmsRequest>> groups = new HashMap<>();
        
        for (SmsRequest request : requests.values()) {
            String provider = selectProvider(request.getPhoneNumber());
            groups.computeIfAbsent(provider, k -> new ArrayList<>()).add(request);
        }
        
        return groups;
    }

    private Map<String, ChannelResponse> sendBulkToProvider(
            String provider, List<SmsRequest> requests) {
        
        Map<String, ChannelResponse> responses = new HashMap<>();
        
        // Check if provider supports bulk sending
        if (supportsBulkSending(provider)) {
            try {
                Map<String, ChannelResponse> bulkResponses = sendBulkToProviderNative(provider, requests);
                responses.putAll(bulkResponses);
            } catch (Exception e) {
                log.error("Bulk sending failed for provider: {}, falling back to individual sends", provider, e);
                // Fallback to individual sends
                for (SmsRequest request : requests) {
                    ChannelResponse response = sendToProvider(request, provider);
                    responses.put(request.getPhoneNumber(), response);
                }
            }
        } else {
            // Send individually
            for (SmsRequest request : requests) {
                ChannelResponse response = sendToProvider(request, provider);
                responses.put(request.getPhoneNumber(), response);
            }
        }
        
        return responses;
    }

    private boolean supportsBulkSending(String provider) {
        return "twilio".equals(provider) || "globe".equals(provider);
    }

    private Map<String, ChannelResponse> sendBulkToProviderNative(
            String provider, List<SmsRequest> requests) {
        
        switch (provider.toLowerCase()) {
            case "twilio":
                return twilioClient.sendBulkSms(requests);
            case "globe":
                return globeClient.sendBulkSms(requests);
            default:
                throw new UnsupportedOperationException("Bulk sending not supported for provider: " + provider);
        }
    }

    /**
     * Get SMS delivery status
     */
    public SmsDeliveryStatus getDeliveryStatus(String messageId, String provider) {
        try {
            switch (provider.toLowerCase()) {
                case "twilio":
                    return twilioClient.getDeliveryStatus(messageId);
                case "globe":
                    return globeClient.getDeliveryStatus(messageId);
                case "smart":
                    return smartClient.getDeliveryStatus(messageId);
                case "dito":
                    return ditoClient.getDeliveryStatus(messageId);
                default:
                    throw new IllegalArgumentException("Unknown SMS provider: " + provider);
            }
        } catch (Exception e) {
            log.error("Failed to get delivery status from provider: {}", provider, e);
            return SmsDeliveryStatus.UNKNOWN;
        }
    }

    /**
     * Get SMS analytics for a time period
     */
    public SmsAnalytics getAnalytics(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            SmsAnalytics analytics = new SmsAnalytics();
            
            // Aggregate analytics from all providers
            SmsAnalytics twilioAnalytics = twilioClient.getAnalytics(startTime, endTime);
            SmsAnalytics globeAnalytics = globeClient.getAnalytics(startTime, endTime);
            SmsAnalytics smartAnalytics = smartClient.getAnalytics(startTime, endTime);
            SmsAnalytics ditoAnalytics = ditoClient.getAnalytics(startTime, endTime);
            
            analytics.aggregate(twilioAnalytics, globeAnalytics, smartAnalytics, ditoAnalytics);
            
            return analytics;
        } catch (Exception e) {
            log.error("Failed to get SMS analytics", e);
            return new SmsAnalytics(); // Return empty analytics
        }
    }
}
