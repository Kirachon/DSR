package ph.gov.dsr.datamanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing archived data records
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-25
 */
@Entity
@Table(name = "archived_data", schema = "dsr_core")
@Data
@EqualsAndHashCode(callSuper = false)
public class ArchivedData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "archive_id")
    private UUID archiveId;

    @Column(name = "original_entity_id", nullable = false)
    private UUID originalEntityId;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "archived_data", columnDefinition = "TEXT")
    private String archivedData;

    @Column(name = "archive_reason", length = 500)
    private String archiveReason;

    @Column(name = "archived_by", length = 100)
    private String archivedBy;

    @Column(name = "original_created_at")
    private LocalDateTime originalCreatedAt;

    @Column(name = "original_updated_at")
    private LocalDateTime originalUpdatedAt;

    @CreationTimestamp
    @Column(name = "archived_at", nullable = false)
    private LocalDateTime archivedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "retention_until")
    private LocalDateTime retentionUntil;

    @Column(name = "is_encrypted", nullable = false)
    private Boolean isEncrypted = false;

    @Column(name = "encryption_key_id")
    private String encryptionKeyId;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "compression_type", length = 20)
    private String compressionType;

    @Column(name = "storage_location")
    private String storageLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "archive_status", nullable = false)
    private ArchiveStatus archiveStatus = ArchiveStatus.ACTIVE;

    @Column(name = "restored_at")
    private LocalDateTime restoredAt;

    @Column(name = "restored_by")
    private String restoredBy;

    @Column(name = "restore_reason")
    private String restoreReason;

    public enum ArchiveStatus {
        ACTIVE,
        RESTORED,
        EXPIRED,
        DELETED
    }

    // Helper methods
    public boolean isExpired() {
        return retentionUntil != null && LocalDateTime.now().isAfter(retentionUntil);
    }

    public boolean isRestored() {
        return archiveStatus == ArchiveStatus.RESTORED;
    }

    public void markAsRestored(String restoredBy, String reason) {
        this.archiveStatus = ArchiveStatus.RESTORED;
        this.restoredAt = LocalDateTime.now();
        this.restoredBy = restoredBy;
        this.restoreReason = reason;
    }

    public void markAsExpired() {
        this.archiveStatus = ArchiveStatus.EXPIRED;
    }
}
