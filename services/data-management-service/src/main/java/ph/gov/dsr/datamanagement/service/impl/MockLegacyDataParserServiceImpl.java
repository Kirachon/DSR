package ph.gov.dsr.datamanagement.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ph.gov.dsr.datamanagement.dto.DataIngestionRequest;
import ph.gov.dsr.datamanagement.service.LegacyDataParserService;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock implementation of LegacyDataParserService for no-database mode
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@Profile("no-db")
@Slf4j
public class MockLegacyDataParserServiceImpl implements LegacyDataParserService {

    @Override
    public List<DataIngestionRequest> parseFile(String sourceSystem, String filePath, String dataType) {
        log.info("Mock parsing file from {}: {}", sourceSystem, filePath);
        
        List<DataIngestionRequest> requests = new ArrayList<>();
        
        // Create mock data based on source system
        switch (sourceSystem.toUpperCase()) {
            case "LISTAHANAN":
                requests.addAll(createMockListahananData(dataType));
                break;
            case "I_REGISTRO":
                requests.addAll(createMockIRegistroData(dataType));
                break;
            default:
                requests.addAll(createMockGenericData(dataType));
        }
        
        return requests;
    }

    @Override
    public boolean validateFileFormat(String sourceSystem, String filePath) {
        log.info("Mock validating file format for {}: {}", sourceSystem, filePath);
        
        // Mock validation - check if file exists and has expected extension
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        
        String fileName = file.getName().toLowerCase();
        
        return switch (sourceSystem.toUpperCase()) {
            case "LISTAHANAN" -> fileName.endsWith(".csv") || fileName.endsWith(".xlsx");
            case "I_REGISTRO" -> fileName.endsWith(".xml") || fileName.endsWith(".json");
            default -> true; // Accept any format for unknown systems
        };
    }

    @Override
    public List<String> getSupportedFormats(String sourceSystem) {
        log.info("Mock getting supported formats for: {}", sourceSystem);
        
        return switch (sourceSystem.toUpperCase()) {
            case "LISTAHANAN" -> List.of("CSV", "XLSX");
            case "I_REGISTRO" -> List.of("XML", "JSON");
            default -> List.of("CSV", "JSON", "XML", "XLSX");
        };
    }

    @Override
    public FileMetadata getFileMetadata(String filePath) {
        log.info("Mock getting file metadata for: {}", filePath);
        
        FileMetadata metadata = new FileMetadata();
        File file = new File(filePath);
        
        if (file.exists()) {
            metadata.setValid(true);
            metadata.setFileSizeBytes(file.length());
            metadata.setFileFormat(getFileExtension(filePath));
            metadata.setEstimatedRecordCount(100); // Mock estimate
            metadata.setEncoding("UTF-8");
        } else {
            metadata.setValid(false);
            metadata.setErrorMessage("File not found: " + filePath);
        }
        
        return metadata;
    }

    @Override
    public List<DataIngestionRequest> parseListahananFile(String filePath, String dataType) {
        log.info("Mock parsing Listahanan file: {}", filePath);
        return createMockListahananData(dataType);
    }

    @Override
    public List<DataIngestionRequest> parseIRegistroFile(String filePath, String dataType) {
        log.info("Mock parsing i-Registro file: {}", filePath);
        return createMockIRegistroData(dataType);
    }

    private List<DataIngestionRequest> createMockListahananData(String dataType) {
        List<DataIngestionRequest> requests = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            DataIngestionRequest request = new DataIngestionRequest();
            request.setSourceSystem("LISTAHANAN");
            request.setDataType(dataType);
            
            Map<String, Object> data = new HashMap<>();
            if ("HOUSEHOLD".equals(dataType)) {
                data.put("householdNumber", "LH-2024-" + String.format("%06d", i));
                data.put("headOfHouseholdName", "Juan Dela Cruz " + i);
                data.put("totalMembers", 4 + (i % 3));
                data.put("monthlyIncome", 15000 + (i * 1000));
                data.put("address", "Barangay " + i + ", Municipality, Province");
                data.put("isIndigenous", i % 4 == 0);
                data.put("isPwdHousehold", i % 5 == 0);
            } else if ("INDIVIDUAL".equals(dataType)) {
                data.put("psn", "1234-5678-" + String.format("%04d", i));
                data.put("firstName", "Juan");
                data.put("lastName", "Dela Cruz");
                data.put("middleName", "Santos");
                data.put("dateOfBirth", "1990-01-" + String.format("%02d", i));
                data.put("sex", i % 2 == 0 ? "M" : "F");
                data.put("civilStatus", "SINGLE");
            }
            
            request.setDataPayload(data);
            requests.add(request);
        }
        
        return requests;
    }

    private List<DataIngestionRequest> createMockIRegistroData(String dataType) {
        List<DataIngestionRequest> requests = new ArrayList<>();
        
        for (int i = 1; i <= 3; i++) {
            DataIngestionRequest request = new DataIngestionRequest();
            request.setSourceSystem("I_REGISTRO");
            request.setDataType(dataType);
            
            Map<String, Object> data = new HashMap<>();
            if ("HOUSEHOLD".equals(dataType)) {
                data.put("householdNumber", "IR-2024-" + String.format("%06d", i));
                data.put("headOfHouseholdName", "Maria Garcia " + i);
                data.put("totalMembers", 3 + (i % 2));
                data.put("monthlyIncome", 20000 + (i * 2000));
                data.put("address", "Street " + i + ", City, Province");
                data.put("registrationDate", "2024-01-" + String.format("%02d", i));
            } else if ("INDIVIDUAL".equals(dataType)) {
                data.put("psn", "5678-9012-" + String.format("%04d", i));
                data.put("firstName", "Maria");
                data.put("lastName", "Garcia");
                data.put("middleName", "Reyes");
                data.put("dateOfBirth", "1985-02-" + String.format("%02d", i));
                data.put("sex", "F");
                data.put("civilStatus", "MARRIED");
            }
            
            request.setDataPayload(data);
            requests.add(request);
        }
        
        return requests;
    }

    private List<DataIngestionRequest> createMockGenericData(String dataType) {
        List<DataIngestionRequest> requests = new ArrayList<>();
        
        DataIngestionRequest request = new DataIngestionRequest();
        request.setSourceSystem("MANUAL_ENTRY");
        request.setDataType(dataType);
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", "MOCK-001");
        data.put("name", "Mock Data Entry");
        data.put("type", dataType);
        
        request.setDataPayload(data);
        requests.add(request);
        
        return requests;
    }

    private String getFileExtension(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filePath.length() - 1) {
            return filePath.substring(lastDotIndex + 1).toUpperCase();
        }
        return "UNKNOWN";
    }
}
