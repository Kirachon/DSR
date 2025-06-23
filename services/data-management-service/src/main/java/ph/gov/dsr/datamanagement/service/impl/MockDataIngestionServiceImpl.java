package ph.gov.dsr.datamanagement.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ph.gov.dsr.datamanagement.dto.DataIngestionRequest;
import ph.gov.dsr.datamanagement.dto.DataIngestionResponse;
import ph.gov.dsr.datamanagement.service.DataIngestionService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Mock implementation of DataIngestionService for no-database mode
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@Profile("no-db")
@Slf4j
public class MockDataIngestionServiceImpl implements DataIngestionService {

    @Override
    public DataIngestionResponse ingestData(DataIngestionRequest request) {
        log.info("Mock ingesting data from source: {}", request.getSourceSystem());
        
        DataIngestionResponse response = new DataIngestionResponse();
        response.setIngestionId(UUID.randomUUID());
        response.setStatus("SUCCESS");
        response.setMessage("Mock data ingestion completed successfully");
        response.setProcessedAt(LocalDateTime.now());
        response.setBatchId(request.getBatchId());
        response.setTotalRecords(1);
        response.setSuccessfulRecords(1);
        response.setFailedRecords(0);
        response.setDuplicateRecords(0);
        response.setValidationErrors(new ArrayList<>());
        response.setWarnings(new ArrayList<>());
        response.setProcessingTimeMs("150");
        
        return response;
    }

    @Override
    public DataIngestionResponse ingestBatch(List<DataIngestionRequest> requests, String batchId) {
        log.info("Mock ingesting batch with {} records, batchId: {}", requests.size(), batchId);
        
        DataIngestionResponse response = new DataIngestionResponse();
        response.setIngestionId(UUID.randomUUID());
        response.setStatus("SUCCESS");
        response.setMessage("Mock batch ingestion completed successfully");
        response.setProcessedAt(LocalDateTime.now());
        response.setBatchId(batchId);
        response.setTotalRecords(requests.size());
        response.setSuccessfulRecords(requests.size());
        response.setFailedRecords(0);
        response.setDuplicateRecords(0);
        response.setValidationErrors(new ArrayList<>());
        response.setWarnings(new ArrayList<>());
        response.setProcessingTimeMs(String.valueOf(requests.size() * 100));
        
        return response;
    }

    @Override
    public DataIngestionResponse getIngestionStatus(UUID ingestionId) {
        log.info("Mock getting ingestion status for ID: {}", ingestionId);
        
        DataIngestionResponse response = new DataIngestionResponse();
        response.setIngestionId(ingestionId);
        response.setStatus("COMPLETED");
        response.setMessage("Mock ingestion completed");
        response.setProcessedAt(LocalDateTime.now().minusMinutes(5));
        
        return response;
    }

    @Override
    public DataIngestionResponse processLegacyDataFile(String sourceSystem, String filePath, String dataType) {
        log.info("Mock processing legacy data file from {}: {}", sourceSystem, filePath);
        
        DataIngestionResponse response = new DataIngestionResponse();
        response.setIngestionId(UUID.randomUUID());
        response.setStatus("SUCCESS");
        response.setMessage("Mock legacy data file processed successfully");
        response.setProcessedAt(LocalDateTime.now());
        response.setTotalRecords(100);
        response.setSuccessfulRecords(95);
        response.setFailedRecords(3);
        response.setDuplicateRecords(2);
        response.setValidationErrors(new ArrayList<>());
        response.setWarnings(List.of("Some records had minor data quality issues"));
        response.setProcessingTimeMs("5000");
        
        return response;
    }

    @Override
    public DataIngestionResponse validateData(DataIngestionRequest request) {
        log.info("Mock validating data from source: {}", request.getSourceSystem());
        
        DataIngestionResponse response = new DataIngestionResponse();
        response.setIngestionId(UUID.randomUUID());
        response.setStatus("VALID");
        response.setMessage("Mock data validation completed - data is valid");
        response.setProcessedAt(LocalDateTime.now());
        response.setTotalRecords(1);
        response.setSuccessfulRecords(1);
        response.setFailedRecords(0);
        response.setValidationErrors(new ArrayList<>());
        response.setWarnings(new ArrayList<>());
        response.setProcessingTimeMs("50");
        
        return response;
    }

    @Override
    public DataIngestionResponse getIngestionStatistics(String batchId) {
        log.info("Mock getting ingestion statistics for batch: {}", batchId);
        
        DataIngestionResponse response = new DataIngestionResponse();
        response.setBatchId(batchId);
        response.setStatus("COMPLETED");
        response.setMessage("Mock batch statistics");
        response.setTotalRecords(1000);
        response.setSuccessfulRecords(950);
        response.setFailedRecords(30);
        response.setDuplicateRecords(20);
        response.setProcessedAt(LocalDateTime.now().minusHours(1));
        
        return response;
    }
}
