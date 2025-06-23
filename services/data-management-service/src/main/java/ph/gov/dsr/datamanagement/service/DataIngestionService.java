package ph.gov.dsr.datamanagement.service;

import ph.gov.dsr.datamanagement.dto.DataIngestionRequest;
import ph.gov.dsr.datamanagement.dto.DataIngestionResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for data ingestion operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
public interface DataIngestionService {

    /**
     * Ingest single record
     */
    DataIngestionResponse ingestData(DataIngestionRequest request);

    /**
     * Ingest batch of records
     */
    DataIngestionResponse ingestBatch(List<DataIngestionRequest> requests, String batchId);

    /**
     * Get ingestion status
     */
    DataIngestionResponse getIngestionStatus(UUID ingestionId);

    /**
     * Process legacy system data file
     */
    DataIngestionResponse processLegacyDataFile(String sourceSystem, String filePath, String dataType);

    /**
     * Validate data without persisting
     */
    DataIngestionResponse validateData(DataIngestionRequest request);

    /**
     * Get ingestion statistics
     */
    DataIngestionResponse getIngestionStatistics(String batchId);
}
