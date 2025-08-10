package ph.gov.dsr.grievance.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

/**
 * Notification preferences for citizens
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class NotificationPreferences {
    
    // Channel preferences
    private boolean smsEnabled;
    private boolean emailEnabled;
    private boolean pushEnabled;
    private boolean voiceCallEnabled;
    
    // Timing preferences
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
    private boolean respectQuietHours;
    
    // Content preferences
    private String language; // en, fil, etc.
    private String timezone;
    private boolean includeDetails;
    private boolean includeSurveyLinks;
    
    // Frequency preferences
    private boolean immediateNotifications;
    private boolean dailyDigest;
    private boolean weeklyDigest;
    private List<String> notificationTypes; // CASE_UPDATE, RESOLUTION, etc.
    
    // Contact information
    private String preferredPhone;
    private String preferredEmail;
    private String alternatePhone;
    private String alternateEmail;
    
    // Accessibility preferences
    private boolean largeText;
    private boolean highContrast;
    private boolean screenReaderFriendly;
    
    // Default preferences
    public static NotificationPreferences getDefault() {
        return NotificationPreferences.builder()
            .smsEnabled(true)
            .emailEnabled(true)
            .pushEnabled(true)
            .voiceCallEnabled(false)
            .quietHoursStart(LocalTime.of(22, 0))
            .quietHoursEnd(LocalTime.of(7, 0))
            .respectQuietHours(true)
            .language("en")
            .timezone("Asia/Manila")
            .includeDetails(true)
            .includeSurveyLinks(true)
            .immediateNotifications(true)
            .dailyDigest(false)
            .weeklyDigest(false)
            .largeText(false)
            .highContrast(false)
            .screenReaderFriendly(false)
            .build();
    }
}
