package ph.gov.dsr.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Base entity class providing common fields for all DSR entities.
 * Includes audit fields, versioning, and standard identification.
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-20
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /**
     * Unique identifier for the entity.
     * Uses UUID for better distribution and security.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    /**
     * Timestamp when the entity was created.
     * Automatically set by JPA auditing.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Instant createdAt;

    /**
     * Timestamp when the entity was last updated.
     * Automatically updated by JPA auditing.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Instant updatedAt;

    /**
     * Version field for optimistic locking.
     * Prevents concurrent modification issues.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    /**
     * Soft delete flag.
     * When true, the entity is considered deleted but remains in the database.
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    /**
     * User ID who created this entity.
     * Links to the authentication system.
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    /**
     * User ID who last modified this entity.
     * Links to the authentication system.
     */
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /**
     * Pre-persist callback to set default values.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
        if (deleted == null) {
            deleted = false;
        }
        if (version == null) {
            version = 0L;
        }
    }

    /**
     * Pre-update callback to update modification timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Marks the entity as deleted (soft delete).
     * The entity remains in the database but is considered deleted.
     */
    public void markAsDeleted() {
        this.deleted = true;
        this.updatedAt = Instant.now();
    }

    /**
     * Restores a soft-deleted entity.
     */
    public void restore() {
        this.deleted = false;
        this.updatedAt = Instant.now();
    }

    /**
     * Checks if the entity is soft-deleted.
     * 
     * @return true if the entity is deleted, false otherwise
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }

    /**
     * Checks if the entity is new (not yet persisted).
     * 
     * @return true if the entity is new, false otherwise
     */
    public boolean isNew() {
        return id == null;
    }
}
