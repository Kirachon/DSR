package ph.gov.dsr.datamanagement.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import ph.gov.dsr.datamanagement.dto.DataIngestionRequest;
import ph.gov.dsr.datamanagement.service.LegacyDataParserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LegacyDataParserServiceImpl
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@ExtendWith(MockitoExtension.class)
class LegacyDataParserServiceImplTest {

    private LegacyDataParserServiceImpl legacyDataParserService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        legacyDataParserService = new LegacyDataParserServiceImpl();
    }

    @Test
    void testValidateFileFormat_ValidListahananCSV() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("test.csv");
        Files.createFile(csvFile);

        // Act
        boolean result = legacyDataParserService.validateFileFormat("LISTAHANAN", csvFile.toString());

        // Assert
        assertTrue(result);
    }

    @Test
    void testValidateFileFormat_ValidIRegistroXML() throws IOException {
        // Arrange
        Path xmlFile = tempDir.resolve("test.xml");
        Files.createFile(xmlFile);

        // Act
        boolean result = legacyDataParserService.validateFileFormat("I_REGISTRO", xmlFile.toString());

        // Assert
        assertTrue(result);
    }

    @Test
    void testValidateFileFormat_InvalidFormat() throws IOException {
        // Arrange
        Path txtFile = tempDir.resolve("test.txt");
        Files.createFile(txtFile);

        // Act
        boolean result = legacyDataParserService.validateFileFormat("LISTAHANAN", txtFile.toString());

        // Assert
        assertFalse(result);
    }

    @Test
    void testValidateFileFormat_FileNotExists() {
        // Arrange
        String nonExistentFile = tempDir.resolve("nonexistent.csv").toString();

        // Act
        boolean result = legacyDataParserService.validateFileFormat("LISTAHANAN", nonExistentFile);

        // Assert
        assertFalse(result);
    }

    @Test
    void testGetSupportedFormats_Listahanan() {
        // Act
        List<String> formats = legacyDataParserService.getSupportedFormats("LISTAHANAN");

        // Assert
        assertNotNull(formats);
        assertTrue(formats.contains("CSV"));
        assertTrue(formats.contains("XLSX"));
        assertEquals(2, formats.size());
    }

    @Test
    void testGetSupportedFormats_IRegistro() {
        // Act
        List<String> formats = legacyDataParserService.getSupportedFormats("I_REGISTRO");

        // Assert
        assertNotNull(formats);
        assertTrue(formats.contains("XML"));
        assertTrue(formats.contains("JSON"));
        assertEquals(2, formats.size());
    }

    @Test
    void testGetSupportedFormats_Unknown() {
        // Act
        List<String> formats = legacyDataParserService.getSupportedFormats("UNKNOWN_SYSTEM");

        // Assert
        assertNotNull(formats);
        assertTrue(formats.contains("CSV"));
        assertTrue(formats.contains("JSON"));
        assertTrue(formats.contains("XML"));
        assertTrue(formats.contains("XLSX"));
    }

    @Test
    void testGetFileMetadata_ValidFile() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("test.csv");
        String content = "header1,header2,header3\nvalue1,value2,value3\nvalue4,value5,value6\n";
        Files.write(csvFile, content.getBytes());

        // Act
        LegacyDataParserService.FileMetadata metadata = legacyDataParserService.getFileMetadata(csvFile.toString());

        // Assert
        assertNotNull(metadata);
        assertTrue(metadata.isValid());
        assertEquals("CSV", metadata.getFileFormat());
        assertEquals("UTF-8", metadata.getEncoding());
        assertTrue(metadata.getFileSizeBytes() > 0);
        assertTrue(metadata.getEstimatedRecordCount() > 0);
    }

    @Test
    void testGetFileMetadata_FileNotExists() {
        // Arrange
        String nonExistentFile = tempDir.resolve("nonexistent.csv").toString();

        // Act
        LegacyDataParserService.FileMetadata metadata = legacyDataParserService.getFileMetadata(nonExistentFile);

        // Assert
        assertNotNull(metadata);
        assertFalse(metadata.isValid());
        assertTrue(metadata.getErrorMessage().contains("File not found"));
    }

    @Test
    void testParseListahananCSV() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("listahanan.csv");
        String content = "household_number,head_name,total_members,monthly_income,address\n" +
                        "HH-001,Juan Dela Cruz,5,15000,123 Main St\n" +
                        "HH-002,Maria Garcia,3,12000,456 Oak Ave\n";
        Files.write(csvFile, content.getBytes());

        // Act
        List<DataIngestionRequest> requests = legacyDataParserService.parseListahananFile(csvFile.toString(), "HOUSEHOLD");

        // Assert
        assertNotNull(requests);
        assertEquals(2, requests.size());

        DataIngestionRequest firstRequest = requests.get(0);
        assertEquals("LISTAHANAN", firstRequest.getSourceSystem());
        assertEquals("HOUSEHOLD", firstRequest.getDataType());
        assertNotNull(firstRequest.getDataPayload());
        assertTrue(firstRequest.getDataPayload().containsKey("householdNumber"));
        assertEquals("HH-001", firstRequest.getDataPayload().get("householdNumber"));
    }

    @Test
    void testParseListahananCSV_EmptyFile() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("empty.csv");
        Files.createFile(csvFile);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            legacyDataParserService.parseListahananFile(csvFile.toString(), "HOUSEHOLD");
        });
    }

    @Test
    void testParseListahananCSV_OnlyHeader() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("header_only.csv");
        String content = "household_number,head_name,total_members\n";
        Files.write(csvFile, content.getBytes());

        // Act
        List<DataIngestionRequest> requests = legacyDataParserService.parseListahananFile(csvFile.toString(), "HOUSEHOLD");

        // Assert
        assertNotNull(requests);
        assertTrue(requests.isEmpty());
    }

    @Test
    void testParseListahananCSV_WithQuotedValues() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("quoted.csv");
        String content = "household_number,head_name,address\n" +
                        "\"HH-001\",\"Juan \"\"Jr\"\" Dela Cruz\",\"123 Main St, Apt 2\"\n";
        Files.write(csvFile, content.getBytes());

        // Act
        List<DataIngestionRequest> requests = legacyDataParserService.parseListahananFile(csvFile.toString(), "HOUSEHOLD");

        // Assert
        assertNotNull(requests);
        assertEquals(1, requests.size());

        DataIngestionRequest request = requests.get(0);
        assertEquals("HH-001", request.getDataPayload().get("householdNumber"));
        assertEquals("Juan \"Jr\" Dela Cruz", request.getDataPayload().get("headOfHouseholdName"));
        assertEquals("123 Main St, Apt 2", request.getDataPayload().get("address"));
    }

    @Test
    void testParseGenericCSV() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("generic.csv");
        String content = "field1,field2,field3\n" +
                        "value1,value2,value3\n" +
                        "value4,value5,value6\n";
        Files.write(csvFile, content.getBytes());

        // Act
        List<DataIngestionRequest> requests = legacyDataParserService.parseFile("MANUAL_ENTRY", csvFile.toString(), "GENERIC");

        // Assert
        assertNotNull(requests);
        assertEquals(2, requests.size());

        DataIngestionRequest firstRequest = requests.get(0);
        assertEquals("MANUAL_ENTRY", firstRequest.getSourceSystem());
        assertEquals("GENERIC", firstRequest.getDataType());
        assertNotNull(firstRequest.getDataPayload());
        assertEquals("value1", firstRequest.getDataPayload().get("field1"));
        assertEquals("value2", firstRequest.getDataPayload().get("field2"));
        assertEquals("value3", firstRequest.getDataPayload().get("field3"));
    }

    @Test
    void testParseFile_UnsupportedFormat() throws IOException {
        // Arrange
        Path txtFile = tempDir.resolve("test.txt");
        Files.createFile(txtFile);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            legacyDataParserService.parseFile("LISTAHANAN", txtFile.toString(), "HOUSEHOLD");
        });
    }

    @Test
    void testParseFile_UnsupportedSourceSystem() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("test.csv");
        String content = "field1,field2\nvalue1,value2\n";
        Files.write(csvFile, content.getBytes());

        // Act
        List<DataIngestionRequest> requests = legacyDataParserService.parseFile("UNKNOWN_SYSTEM", csvFile.toString(), "GENERIC");

        // Assert
        assertNotNull(requests);
        assertEquals(1, requests.size());
        assertEquals("UNKNOWN_SYSTEM", requests.get(0).getSourceSystem());
    }

    @Test
    void testParseListahananIndividualData() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("individuals.csv");
        String content = "psn,first_name,last_name,date_of_birth,sex,civil_status\n" +
                        "1234-5678-9012,Juan,Dela Cruz,1990-01-01,M,Single\n" +
                        "2345-6789-0123,Maria,Garcia,1985-05-15,F,Married\n";
        Files.write(csvFile, content.getBytes());

        // Act
        List<DataIngestionRequest> requests = legacyDataParserService.parseListahananFile(csvFile.toString(), "INDIVIDUAL");

        // Assert
        assertNotNull(requests);
        assertEquals(2, requests.size());

        DataIngestionRequest firstRequest = requests.get(0);
        assertEquals("INDIVIDUAL", firstRequest.getDataType());
        assertEquals("1234-5678-9012", firstRequest.getDataPayload().get("psn"));
        assertEquals("Juan", firstRequest.getDataPayload().get("firstName"));
        assertEquals("Dela Cruz", firstRequest.getDataPayload().get("lastName"));
        assertEquals("1990-01-01", firstRequest.getDataPayload().get("dateOfBirth"));
        assertEquals("M", firstRequest.getDataPayload().get("sex"));
        assertEquals("Single", firstRequest.getDataPayload().get("civilStatus"));
    }

    @Test
    void testParseListahananEconomicData() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("economic.csv");
        String content = "household_id,total_assets,monthly_expenses,income_sources,livelihood\n" +
                        "HH-001,50000,8000,Employment,Farming\n" +
                        "HH-002,75000,12000,Business,Trading\n";
        Files.write(csvFile, content.getBytes());

        // Act
        List<DataIngestionRequest> requests = legacyDataParserService.parseListahananFile(csvFile.toString(), "ECONOMIC_PROFILE");

        // Assert
        assertNotNull(requests);
        assertEquals(2, requests.size());

        DataIngestionRequest firstRequest = requests.get(0);
        assertEquals("ECONOMIC_PROFILE", firstRequest.getDataType());
        assertEquals("HH-001", firstRequest.getDataPayload().get("householdId"));
        assertEquals("50000", firstRequest.getDataPayload().get("totalAssets"));
        assertEquals("8000", firstRequest.getDataPayload().get("monthlyExpenses"));
        assertEquals("Employment", firstRequest.getDataPayload().get("incomeSources"));
        assertEquals("Farming", firstRequest.getDataPayload().get("livelihood"));
    }

    @Test
    void testParseListahananExcel_NotImplemented() throws IOException {
        // Arrange
        Path xlsxFile = tempDir.resolve("test.xlsx");
        Files.createFile(xlsxFile);

        // Act
        List<DataIngestionRequest> requests = legacyDataParserService.parseListahananFile(xlsxFile.toString(), "HOUSEHOLD");

        // Assert
        assertNotNull(requests);
        assertTrue(requests.isEmpty()); // Excel parsing not fully implemented
    }

    @Test
    void testParseIRegistroXML_NotImplemented() throws IOException {
        // Arrange
        Path xmlFile = tempDir.resolve("test.xml");
        Files.createFile(xmlFile);

        // Act
        List<DataIngestionRequest> requests = legacyDataParserService.parseIRegistroFile(xmlFile.toString(), "INDIVIDUAL");

        // Assert
        assertNotNull(requests);
        assertTrue(requests.isEmpty()); // XML parsing not fully implemented
    }

    @Test
    void testParseIRegistroJSON_NotImplemented() throws IOException {
        // Arrange
        Path jsonFile = tempDir.resolve("test.json");
        Files.createFile(jsonFile);

        // Act
        List<DataIngestionRequest> requests = legacyDataParserService.parseIRegistroFile(jsonFile.toString(), "INDIVIDUAL");

        // Assert
        assertNotNull(requests);
        assertTrue(requests.isEmpty()); // JSON parsing not fully implemented
    }

    @Test
    void testParseCSV_SkipEmptyLines() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("with_empty_lines.csv");
        String content = "field1,field2\n" +
                        "value1,value2\n" +
                        "\n" +  // Empty line
                        "value3,value4\n" +
                        "   \n" +  // Line with only spaces
                        "value5,value6\n";
        Files.write(csvFile, content.getBytes());

        // Act
        List<DataIngestionRequest> requests = legacyDataParserService.parseFile("MANUAL_ENTRY", csvFile.toString(), "GENERIC");

        // Assert
        assertNotNull(requests);
        assertEquals(3, requests.size()); // Should skip empty lines
    }

    @Test
    void testParseCSV_HandleMalformedLines() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("malformed.csv");
        String content = "field1,field2,field3\n" +
                        "value1,value2,value3\n" +
                        "incomplete_line,missing_field\n" +  // Missing third field
                        "value4,value5,value6\n";
        Files.write(csvFile, content.getBytes());

        // Act
        List<DataIngestionRequest> requests = legacyDataParserService.parseFile("MANUAL_ENTRY", csvFile.toString(), "GENERIC");

        // Assert
        assertNotNull(requests);
        assertEquals(3, requests.size()); // Should handle malformed lines gracefully
        
        // Check that the malformed line is still processed with available fields
        DataIngestionRequest malformedRequest = requests.get(1);
        assertEquals("incomplete_line", malformedRequest.getDataPayload().get("field1"));
        assertEquals("missing_field", malformedRequest.getDataPayload().get("field2"));
        assertNull(malformedRequest.getDataPayload().get("field3")); // Missing field should be null
    }
}
