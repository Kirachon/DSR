package ph.gov.dsr.datamanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an individual data ingestion record
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Entity
@Table(name = "data_ingestion_records", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = false)
public class DataIngestionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private DataIngestionBatch batch;

    @Column(name = "record_index")
    private Integer recordIndex;

    @NotBlank
    @Column(name = "status", nullable = false, length = 20)
    private String status; // SUCCESS, FAILED, DUPLICATE, SKIPPED

    @Column(name = "entity_id", columnDefinition = "UUID")
    private UUID entityId; // ID of the created/updated entity

    @Column(name = "entity_type", length = 50)
    private String entityType; // HOUSEHOLD, INDIVIDUAL, ECONOMIC_PROFILE

    @Column(name = "source_record_id", length = 100)
    private String sourceRecordId; // Original ID from source system

    @Column(name = "raw_data", columnDefinition = "JSONB")
    private String rawData; // Original data as received

    @Column(name = "processed_data", columnDefinition = "JSONB")
    private String processedData; // Cleaned and normalized data

    @Column(name = "validation_errors", columnDefinition = "JSONB")
    private String validationErrors; // JSON array of validation errors

    @Column(name = "warnings", columnDefinition = "JSONB")
    private String warnings; // JSON array of warnings

    @Column(name = "duplicate_of", columnDefinition = "UUID")
    private UUID duplicateOf; // ID of the entity this is a duplicate of

    @Column(name = "similarity_score")
    private Double similarityScore; // Similarity score for duplicates

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;

    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * Mark record as successfully processed
     */
    public void markAsSuccess(UUID entityId, String entityType) {
        this.status = "SUCCESS";
        this.entityId = entityId;
        this.entityType = entityType;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Mark record as failed
     */
    public void markAsFailed(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Mark record as duplicate
     */
    public void markAsDuplicate(UUID duplicateOf, double similarityScore) {
        this.status = "DUPLICATE";
        this.duplicateOf = duplicateOf;
        this.similarityScore = similarityScore;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Mark record as skipped
     */
    public void markAsSkipped(String reason) {
        this.status = "SKIPPED";
        this.errorMessage = reason;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Increment retry count
     */
    public void incrementRetryCount() {
        this.retryCount = (this.retryCount != null ? this.retryCount : 0) + 1;
        this.lastRetryAt = LocalDateTime.now();
    }

    /**
     * Check if record was successfully processed
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }

    /**
     * Check if record failed processing
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    /**
     * Check if record is a duplicate
     */
    public boolean isDuplicate() {
        return "DUPLICATE".equals(status);
    }

    /**
     * Check if record was skipped
     */
    public boolean isSkipped() {
        return "SKIPPED".equals(status);
    }

    /**
     * Check if record can be retried
     */
    public boolean canRetry(int maxRetries) {
        return isFailed() && (retryCount == null || retryCount < maxRetries);
    }
}
