package ph.gov.dsr.datamanagement.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ph.gov.dsr.datamanagement.dto.DataIngestionRequest;
import ph.gov.dsr.datamanagement.service.LegacyDataParserService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Production implementation of LegacyDataParserService
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@Profile("!no-db")
@Slf4j
public class LegacyDataParserServiceImpl implements LegacyDataParserService {

    // CSV parsing patterns
    private static final Pattern CSV_PATTERN = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    private static final String CSV_QUOTE = "\"";
    
    // Supported file formats by source system
    private static final Map<String, List<String>> SUPPORTED_FORMATS = Map.of(
        "LISTAHANAN", List.of("CSV", "XLSX"),
        "I_REGISTRO", List.of("XML", "JSON"),
        "MANUAL_ENTRY", List.of("CSV", "JSON", "XML", "XLSX")
    );

    @Override
    public List<DataIngestionRequest> parseFile(String sourceSystem, String filePath, String dataType) {
        log.info("Parsing file from {}: {} for data type: {}", sourceSystem, filePath, dataType);
        
        try {
            // Validate file exists and format
            if (!validateFileFormat(sourceSystem, filePath)) {
                throw new IllegalArgumentException("Invalid file format for source system: " + sourceSystem);
            }
            
            // Route to appropriate parser based on source system
            return switch (sourceSystem.toUpperCase()) {
                case "LISTAHANAN" -> parseListahananFile(filePath, dataType);
                case "I_REGISTRO" -> parseIRegistroFile(filePath, dataType);
                default -> parseGenericFile(filePath, dataType, sourceSystem);
            };
            
        } catch (Exception e) {
            log.error("Error parsing file: {}", filePath, e);
            throw new RuntimeException("Failed to parse file: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validateFileFormat(String sourceSystem, String filePath) {
        log.debug("Validating file format for {}: {}", sourceSystem, filePath);
        
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.warn("File does not exist: {}", filePath);
                return false;
            }
            
            String fileName = path.getFileName().toString().toLowerCase();
            String extension = getFileExtension(filePath).toUpperCase();
            
            List<String> supportedFormats = SUPPORTED_FORMATS.getOrDefault(
                sourceSystem.toUpperCase(), List.of("CSV", "JSON", "XML", "XLSX"));
            
            boolean isSupported = supportedFormats.contains(extension);
            
            if (!isSupported) {
                log.warn("Unsupported file format '{}' for source system '{}'", extension, sourceSystem);
            }
            
            return isSupported;
            
        } catch (Exception e) {
            log.error("Error validating file format", e);
            return false;
        }
    }

    @Override
    public List<String> getSupportedFormats(String sourceSystem) {
        return SUPPORTED_FORMATS.getOrDefault(sourceSystem.toUpperCase(), 
                List.of("CSV", "JSON", "XML", "XLSX"));
    }

    @Override
    public FileMetadata getFileMetadata(String filePath) {
        log.debug("Getting file metadata for: {}", filePath);
        
        FileMetadata metadata = new FileMetadata();
        
        try {
            Path path = Paths.get(filePath);
            
            if (!Files.exists(path)) {
                metadata.setValid(false);
                metadata.setErrorMessage("File not found: " + filePath);
                return metadata;
            }
            
            metadata.setValid(true);
            metadata.setFileSizeBytes(Files.size(path));
            metadata.setFileFormat(getFileExtension(filePath));
            metadata.setEncoding(detectEncoding(path));
            metadata.setEstimatedRecordCount(estimateRecordCount(path));
            
        } catch (Exception e) {
            log.error("Error getting file metadata", e);
            metadata.setValid(false);
            metadata.setErrorMessage("Error reading file: " + e.getMessage());
        }
        
        return metadata;
    }

    @Override
    public List<DataIngestionRequest> parseListahananFile(String filePath, String dataType) {
        log.info("Parsing Listahanan file: {} for data type: {}", filePath, dataType);
        
        String extension = getFileExtension(filePath).toUpperCase();
        
        return switch (extension) {
            case "CSV" -> parseListahananCSV(filePath, dataType);
            case "XLSX" -> parseListahananExcel(filePath, dataType);
            default -> throw new IllegalArgumentException("Unsupported Listahanan file format: " + extension);
        };
    }

    @Override
    public List<DataIngestionRequest> parseIRegistroFile(String filePath, String dataType) {
        log.info("Parsing i-Registro file: {} for data type: {}", filePath, dataType);
        
        String extension = getFileExtension(filePath).toUpperCase();
        
        return switch (extension) {
            case "XML" -> parseIRegistroXML(filePath, dataType);
            case "JSON" -> parseIRegistroJSON(filePath, dataType);
            default -> throw new IllegalArgumentException("Unsupported i-Registro file format: " + extension);
        };
    }

    /**
     * Parse generic file format
     */
    private List<DataIngestionRequest> parseGenericFile(String filePath, String dataType, String sourceSystem) {
        String extension = getFileExtension(filePath).toUpperCase();
        
        return switch (extension) {
            case "CSV" -> parseGenericCSV(filePath, dataType, sourceSystem);
            case "JSON" -> parseGenericJSON(filePath, dataType, sourceSystem);
            default -> throw new IllegalArgumentException("Unsupported file format: " + extension);
        };
    }

    /**
     * Parse Listahanan CSV file
     */
    private List<DataIngestionRequest> parseListahananCSV(String filePath, String dataType) {
        log.debug("Parsing Listahanan CSV file: {}", filePath);
        
        List<DataIngestionRequest> requests = new ArrayList<>();
        
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("Empty CSV file");
            }
            
            String[] headers = parseCSVLine(headerLine);
            Map<String, Integer> headerMap = createHeaderMap(headers);
            
            String line;
            int lineNumber = 1;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }
                
                try {
                    String[] values = parseCSVLine(line);
                    Map<String, Object> data = mapListahananData(headerMap, values, dataType);
                    
                    if (!data.isEmpty()) {
                        DataIngestionRequest request = createIngestionRequest("LISTAHANAN", dataType, data);
                        requests.add(request);
                    }
                    
                } catch (Exception e) {
                    log.warn("Error parsing line {} in file {}: {}", lineNumber, filePath, e.getMessage());
                }
            }
            
        } catch (IOException e) {
            log.error("Error reading CSV file: {}", filePath, e);
            throw new RuntimeException("Failed to read CSV file", e);
        }
        
        log.info("Parsed {} records from Listahanan CSV file", requests.size());
        return requests;
    }

    /**
     * Parse Listahanan Excel file (simplified - would need Apache POI in production)
     */
    private List<DataIngestionRequest> parseListahananExcel(String filePath, String dataType) {
        log.warn("Excel parsing not fully implemented - returning empty list");
        // TODO: Implement Excel parsing using Apache POI
        // This would require adding Apache POI dependency to pom.xml
        return new ArrayList<>();
    }

    /**
     * Parse i-Registro XML file
     */
    private List<DataIngestionRequest> parseIRegistroXML(String filePath, String dataType) {
        log.warn("XML parsing not fully implemented - returning empty list");
        // TODO: Implement XML parsing using JAXB or DOM parser
        return new ArrayList<>();
    }

    /**
     * Parse i-Registro JSON file
     */
    private List<DataIngestionRequest> parseIRegistroJSON(String filePath, String dataType) {
        log.warn("JSON parsing not fully implemented - returning empty list");
        // TODO: Implement JSON parsing using Jackson ObjectMapper
        return new ArrayList<>();
    }

    /**
     * Parse generic CSV file
     */
    private List<DataIngestionRequest> parseGenericCSV(String filePath, String dataType, String sourceSystem) {
        log.debug("Parsing generic CSV file: {}", filePath);
        
        List<DataIngestionRequest> requests = new ArrayList<>();
        
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("Empty CSV file");
            }
            
            String[] headers = parseCSVLine(headerLine);
            
            String line;
            int lineNumber = 1;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    String[] values = parseCSVLine(line);
                    Map<String, Object> data = new HashMap<>();
                    
                    for (int i = 0; i < Math.min(headers.length, values.length); i++) {
                        data.put(headers[i].trim(), values[i].trim());
                    }
                    
                    if (!data.isEmpty()) {
                        DataIngestionRequest request = createIngestionRequest(sourceSystem, dataType, data);
                        requests.add(request);
                    }
                    
                } catch (Exception e) {
                    log.warn("Error parsing line {} in file {}: {}", lineNumber, filePath, e.getMessage());
                }
            }
            
        } catch (IOException e) {
            log.error("Error reading CSV file: {}", filePath, e);
            throw new RuntimeException("Failed to read CSV file", e);
        }
        
        log.info("Parsed {} records from generic CSV file", requests.size());
        return requests;
    }

    /**
     * Parse generic JSON file
     */
    private List<DataIngestionRequest> parseGenericJSON(String filePath, String dataType, String sourceSystem) {
        log.warn("Generic JSON parsing not fully implemented - returning empty list");
        // TODO: Implement JSON parsing
        return new ArrayList<>();
    }

    /**
     * Parse CSV line handling quoted values
     */
    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote
                    currentField.append('"');
                    i++; // Skip next quote
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // Field separator
                result.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        // Add last field
        result.add(currentField.toString());

        return result.toArray(new String[0]);
    }

    /**
     * Create header map for CSV parsing
     */
    private Map<String, Integer> createHeaderMap(String[] headers) {
        Map<String, Integer> headerMap = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            headerMap.put(headers[i].trim().toLowerCase(), i);
        }
        return headerMap;
    }

    /**
     * Map Listahanan data based on data type
     */
    private Map<String, Object> mapListahananData(Map<String, Integer> headerMap, String[] values, String dataType) {
        Map<String, Object> data = new HashMap<>();

        switch (dataType.toUpperCase()) {
            case "HOUSEHOLD":
                mapListahananHouseholdData(headerMap, values, data);
                break;
            case "INDIVIDUAL":
                mapListahananIndividualData(headerMap, values, data);
                break;
            case "ECONOMIC_PROFILE":
                mapListahananEconomicData(headerMap, values, data);
                break;
            default:
                // Generic mapping
                for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
                    int index = entry.getValue();
                    if (index < values.length) {
                        data.put(entry.getKey(), values[index]);
                    }
                }
        }

        return data;
    }

    /**
     * Map Listahanan household data
     */
    private void mapListahananHouseholdData(Map<String, Integer> headerMap, String[] values, Map<String, Object> data) {
        // Map common Listahanan household fields
        mapField(headerMap, values, data, "household_number", "householdNumber");
        mapField(headerMap, values, data, "head_name", "headOfHouseholdName");
        mapField(headerMap, values, data, "total_members", "totalMembers");
        mapField(headerMap, values, data, "monthly_income", "monthlyIncome");
        mapField(headerMap, values, data, "address", "address");
        mapField(headerMap, values, data, "barangay", "barangay");
        mapField(headerMap, values, data, "municipality", "municipality");
        mapField(headerMap, values, data, "province", "province");
        mapField(headerMap, values, data, "is_indigenous", "isIndigenous");
        mapField(headerMap, values, data, "is_pwd_household", "isPwdHousehold");
    }

    /**
     * Map Listahanan individual data
     */
    private void mapListahananIndividualData(Map<String, Integer> headerMap, String[] values, Map<String, Object> data) {
        // Map common Listahanan individual fields
        mapField(headerMap, values, data, "psn", "psn");
        mapField(headerMap, values, data, "first_name", "firstName");
        mapField(headerMap, values, data, "last_name", "lastName");
        mapField(headerMap, values, data, "middle_name", "middleName");
        mapField(headerMap, values, data, "date_of_birth", "dateOfBirth");
        mapField(headerMap, values, data, "sex", "sex");
        mapField(headerMap, values, data, "civil_status", "civilStatus");
        mapField(headerMap, values, data, "relationship_to_head", "relationshipToHead");
        mapField(headerMap, values, data, "is_pwd", "isPwd");
        mapField(headerMap, values, data, "education_level", "educationLevel");
    }

    /**
     * Map Listahanan economic data
     */
    private void mapListahananEconomicData(Map<String, Integer> headerMap, String[] values, Map<String, Object> data) {
        // Map common Listahanan economic fields
        mapField(headerMap, values, data, "household_id", "householdId");
        mapField(headerMap, values, data, "total_assets", "totalAssets");
        mapField(headerMap, values, data, "monthly_expenses", "monthlyExpenses");
        mapField(headerMap, values, data, "income_sources", "incomeSources");
        mapField(headerMap, values, data, "livelihood", "livelihood");
        mapField(headerMap, values, data, "house_type", "houseType");
        mapField(headerMap, values, data, "water_source", "waterSource");
        mapField(headerMap, values, data, "toilet_facility", "toiletFacility");
    }

    /**
     * Map field from CSV to data object
     */
    private void mapField(Map<String, Integer> headerMap, String[] values, Map<String, Object> data,
                         String csvField, String dataField) {
        Integer index = headerMap.get(csvField.toLowerCase());
        if (index != null && index < values.length) {
            String value = values[index].trim();
            if (!value.isEmpty()) {
                data.put(dataField, value);
            }
        }
    }

    /**
     * Create DataIngestionRequest
     */
    private DataIngestionRequest createIngestionRequest(String sourceSystem, String dataType, Map<String, Object> data) {
        DataIngestionRequest request = new DataIngestionRequest();
        request.setSourceSystem(sourceSystem);
        request.setDataType(dataType);
        request.setDataPayload(data);
        request.setSubmissionDate(LocalDateTime.now());
        request.setSubmittedBy("SYSTEM");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("parsedAt", LocalDateTime.now().toString());
        metadata.put("parser", "LegacyDataParserServiceImpl");
        request.setMetadata(metadata);

        return request;
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filePath.length() - 1) {
            return filePath.substring(lastDotIndex + 1).toUpperCase();
        }
        return "UNKNOWN";
    }

    /**
     * Detect file encoding
     */
    private String detectEncoding(Path path) {
        try {
            // Simple encoding detection - in production, use a library like ICU4J
            byte[] bytes = Files.readAllBytes(path);
            if (bytes.length >= 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
                return "UTF-8";
            }
            return "UTF-8"; // Default assumption
        } catch (Exception e) {
            return "UTF-8";
        }
    }

    /**
     * Estimate record count in file
     */
    private int estimateRecordCount(Path path) {
        try {
            long fileSize = Files.size(path);
            if (fileSize == 0) return 0;

            // Sample first 1KB to estimate average line length
            byte[] sample = new byte[(int) Math.min(1024, fileSize)];
            try (InputStream is = Files.newInputStream(path)) {
                int bytesRead = is.read(sample);
                String sampleText = new String(sample, 0, bytesRead, StandardCharsets.UTF_8);

                long lineCount = sampleText.chars().mapToObj(c -> (char) c).mapToLong(c -> c == '\n' ? 1 : 0).sum();
                if (lineCount > 0) {
                    double avgLineLength = (double) bytesRead / lineCount;
                    return (int) (fileSize / avgLineLength) - 1; // Subtract 1 for header
                }
            }

            // Fallback estimation
            return (int) (fileSize / 100); // Assume ~100 bytes per record

        } catch (Exception e) {
            log.warn("Error estimating record count for file: {}", path, e);
            return 0;
        }
    }
}
