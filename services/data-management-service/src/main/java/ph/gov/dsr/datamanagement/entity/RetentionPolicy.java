package ph.gov.dsr.datamanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing data retention policies
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-25
 */
@Entity
@Table(name = "retention_policies", schema = "dsr_core")
@Data
@EqualsAndHashCode(callSuper = false)
public class RetentionPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "policy_id")
    private UUID policyId;

    @Column(name = "entity_type", nullable = false, length = 50, unique = true)
    private String entityType;

    @Column(name = "retention_days", nullable = false)
    private Integer retentionDays;

    @Column(name = "auto_archive_enabled", nullable = false)
    private Boolean autoArchiveEnabled = false;

    @Column(name = "auto_delete_enabled", nullable = false)
    private Boolean autoDeleteEnabled = false;

    @Column(name = "archive_after_days")
    private Integer archiveAfterDays;

    @Column(name = "delete_after_days")
    private Integer deleteAfterDays;

    @Column(name = "policy_description", length = 500)
    private String policyDescription;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_until")
    private LocalDateTime effectiveUntil;

    // Helper methods
    public boolean isCurrentlyActive() {
        if (!isActive) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        if (effectiveFrom != null && now.isBefore(effectiveFrom)) {
            return false;
        }
        
        if (effectiveUntil != null && now.isAfter(effectiveUntil)) {
            return false;
        }
        
        return true;
    }

    public LocalDateTime calculateArchiveDate(LocalDateTime entityCreatedAt) {
        if (archiveAfterDays == null) {
            return null;
        }
        return entityCreatedAt.plusDays(archiveAfterDays);
    }

    public LocalDateTime calculateDeleteDate(LocalDateTime entityCreatedAt) {
        if (deleteAfterDays == null) {
            return null;
        }
        return entityCreatedAt.plusDays(deleteAfterDays);
    }

    public LocalDateTime calculateRetentionUntil(LocalDateTime archivedAt) {
        return archivedAt.plusDays(retentionDays);
    }
}
