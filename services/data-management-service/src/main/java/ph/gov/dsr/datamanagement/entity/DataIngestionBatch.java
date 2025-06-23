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
 * Entity representing a data ingestion batch operation
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Entity
@Table(name = "data_ingestion_batches", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = false)
public class DataIngestionBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(name = "batch_id", unique = true, nullable = false, length = 100)
    private String batchId;

    @NotBlank
    @Column(name = "source_system", nullable = false, length = 50)
    private String sourceSystem; // LISTAHANAN, I_REGISTRO, MANUAL_ENTRY

    @NotBlank
    @Column(name = "data_type", nullable = false, length = 50)
    private String dataType; // HOUSEHOLD, INDIVIDUAL, ECONOMIC_PROFILE

    @NotBlank
    @Column(name = "status", nullable = false, length = 20)
    private String status; // PROCESSING, SUCCESS, FAILED, PARTIAL

    @Column(name = "total_records")
    private Integer totalRecords = 0;

    @Column(name = "successful_records")
    private Integer successfulRecords = 0;

    @Column(name = "failed_records")
    private Integer failedRecords = 0;

    @Column(name = "duplicate_records")
    private Integer duplicateRecords = 0;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "submitted_by", length = 100)
    private String submittedBy;

    @Column(name = "processing_priority", length = 10)
    private String processingPriority = "NORMAL"; // HIGH, NORMAL, LOW

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "warnings", columnDefinition = "TEXT")
    private String warnings;

    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata;

    @NotNull
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Mark batch as started
     */
    public void markAsStarted() {
        this.status = "PROCESSING";
        this.startedAt = LocalDateTime.now();
    }

    /**
     * Mark batch as completed with success
     */
    public void markAsCompleted() {
        this.status = "SUCCESS";
        this.completedAt = LocalDateTime.now();
        calculateProcessingTime();
    }

    /**
     * Mark batch as failed
     */
    public void markAsFailed(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
        calculateProcessingTime();
    }

    /**
     * Mark batch as partially completed
     */
    public void markAsPartial() {
        this.status = "PARTIAL";
        this.completedAt = LocalDateTime.now();
        calculateProcessingTime();
    }

    /**
     * Calculate processing time
     */
    private void calculateProcessingTime() {
        if (startedAt != null && completedAt != null) {
            this.processingTimeMs = java.time.Duration.between(startedAt, completedAt).toMillis();
        }
    }

    /**
     * Get success rate as percentage
     */
    public double getSuccessRate() {
        if (totalRecords == null || totalRecords == 0) {
            return 0.0;
        }
        return (double) (successfulRecords != null ? successfulRecords : 0) / totalRecords * 100.0;
    }

    /**
     * Check if batch is completed
     */
    public boolean isCompleted() {
        return "SUCCESS".equals(status) || "FAILED".equals(status) || "PARTIAL".equals(status);
    }

    /**
     * Check if batch is in progress
     */
    public boolean isInProgress() {
        return "PROCESSING".equals(status);
    }
}
