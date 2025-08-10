package ph.gov.dsr.datamanagement.service;

import ph.gov.dsr.datamanagement.dto.DataIngestionRequest;

import java.util.List;

/**
 * Service interface for parsing legacy system data files
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
public interface LegacyDataParserService {

    /**
     * Parse legacy data file into ingestion requests
     */
    List<DataIngestionRequest> parseFile(String sourceSystem, String filePath, String dataType);

    /**
     * Validate file format and structure
     */
    boolean validateFileFormat(String sourceSystem, String filePath);

    /**
     * Get supported file formats for source system
     */
    List<String> getSupportedFormats(String sourceSystem);

    /**
     * Get file metadata (size, record count estimate, etc.)
     */
    FileMetadata getFileMetadata(String filePath);

    /**
     * Parse Listahanan CSV/Excel files
     */
    List<DataIngestionRequest> parseListahananFile(String filePath, String dataType);

    /**
     * Parse i-Registro XML/JSON files
     */
    List<DataIngestionRequest> parseIRegistroFile(String filePath, String dataType);

    /**
     * File metadata information
     */
    class FileMetadata {
        private long fileSizeBytes;
        private String fileFormat;
        private int estimatedRecordCount;
        private String encoding;
        private boolean isValid;
        private String errorMessage;

        // Getters and setters
        public long getFileSizeBytes() { return fileSizeBytes; }
        public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

        public String getFileFormat() { return fileFormat; }
        public void setFileFormat(String fileFormat) { this.fileFormat = fileFormat; }

        public int getEstimatedRecordCount() { return estimatedRecordCount; }
        public void setEstimatedRecordCount(int estimatedRecordCount) { this.estimatedRecordCount = estimatedRecordCount; }

        public String getEncoding() { return encoding; }
        public void setEncoding(String encoding) { this.encoding = encoding; }

        public boolean isValid() { return isValid; }
        public void setValid(boolean valid) { isValid = valid; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}
