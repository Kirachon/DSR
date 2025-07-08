package ph.gov.dsr.analytics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.analytics.entity.Report;
import ph.gov.dsr.analytics.repository.ReportRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for building custom reports dynamically
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-27
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomReportBuilderService {

    private final ReportRepository reportRepository;

    /**
     * Build a custom report based on specifications
     */
    @Transactional
    public Map<String, Object> buildCustomReport(Map<String, Object> reportSpec) {
        log.info("Building custom report: {}", reportSpec.get("name"));
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Validate report specification
            validateReportSpecification(reportSpec);
            
            // Generate report data
            Map<String, Object> reportData = generateReportData(reportSpec);
            
            // Apply formatting
            Map<String, Object> formattedReport = applyFormatting(reportData, reportSpec);
            
            // Save report if requested
            Report savedReport = null;
            if (Boolean.TRUE.equals(reportSpec.get("saveReport"))) {
                savedReport = saveReport(reportSpec, formattedReport);
            }
            
            result.put("reportName", reportSpec.get("name"));
            result.put("reportType", reportSpec.get("type"));
            result.put("data", formattedReport);
            result.put("metadata", generateReportMetadata(reportSpec, reportData));
            if (savedReport != null) {
                result.put("savedReport", Map.of("id", savedReport.getId(), "code", savedReport.getReportCode()));
            }
            result.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Error building custom report", e);
            result.put("error", "Failed to build custom report: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Create a report template for reuse
     */
    @Transactional
    public Map<String, Object> createReportTemplate(Map<String, Object> templateSpec) {
        log.info("Creating report template: {}", templateSpec.get("name"));
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            validateTemplateSpecification(templateSpec);
            
            Report template = new Report();
            template.setReportCode(generateReportCode((String) templateSpec.get("name")));
            template.setReportName((String) templateSpec.get("name"));
            template.setDescription((String) templateSpec.get("description"));
            template.setReportType(Report.ReportType.CUSTOM);
            template.setCategory(Report.ReportCategory.valueOf(((String) templateSpec.get("category")).toUpperCase()));
            template.setIsTemplate(true);
            template.setQueryConfig(convertToJson(templateSpec.get("queryConfig")));
            template.setFormatConfig(convertToJson(templateSpec.get("formatConfig")));
            template.setFilterConfig(convertToJson(templateSpec.get("filterConfig")));
            template.setCreatedBy((String) templateSpec.get("createdBy"));
            
            Report savedTemplate = reportRepository.save(template);
            
            result.put("templateId", savedTemplate.getId());
            result.put("templateCode", savedTemplate.getReportCode());
            result.put("templateName", savedTemplate.getReportName());
            result.put("status", "CREATED");
            result.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Error creating report template", e);
            result.put("error", "Failed to create report template: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Generate report from existing template
     */
    @Transactional
    public Map<String, Object> generateFromTemplate(UUID templateId, Map<String, Object> parameters) {
        log.info("Generating report from template: {}", templateId);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<Report> templateOpt = reportRepository.findById(templateId);
            if (templateOpt.isEmpty()) {
                throw new IllegalArgumentException("Template not found: " + templateId);
            }
            
            Report template = templateOpt.get();
            if (!Boolean.TRUE.equals(template.getIsTemplate())) {
                throw new IllegalArgumentException("Report is not a template: " + templateId);
            }
            
            // Merge template configuration with parameters
            Map<String, Object> reportSpec = mergeTemplateWithParameters(template, parameters);
            
            // Build report using merged specification
            Map<String, Object> reportResult = buildCustomReport(reportSpec);
            
            result.put("templateId", templateId);
            result.put("templateName", template.getReportName());
            result.put("reportResult", reportResult);
            result.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Error generating report from template", e);
            result.put("error", "Failed to generate report from template: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Get available report templates
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAvailableTemplates(String category, String targetRole) {
        log.info("Getting available report templates for category: {}, role: {}", category, targetRole);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Report> templates = reportRepository.findReportTemplates();
            
            // Filter by category and role if specified
            List<Map<String, Object>> filteredTemplates = templates.stream()
                    .filter(t -> category == null || t.getCategory().name().equalsIgnoreCase(category))
                    .filter(t -> targetRole == null || targetRole.equals(t.getTargetRole()) || t.getTargetRole() == null)
                    .map(this::convertTemplateToSummary)
                    .toList();
            
            Map<String, Long> categoryCount = templates.stream()
                    .collect(Collectors.groupingBy(
                            template -> template.getCategory().name(),
                            Collectors.counting()));
            
            result.put("templates", filteredTemplates);
            result.put("totalTemplates", filteredTemplates.size());
            result.put("categoryBreakdown", categoryCount);
            result.put("filters", Map.of("category", category, "targetRole", targetRole));
            result.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Error getting available templates", e);
            result.put("error", "Failed to get available templates: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Validate report data and structure
     */
    public Map<String, Object> validateReportData(Map<String, Object> reportData) {
        log.info("Validating report data structure");
        
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try {
            // Check required fields
            if (!reportData.containsKey("data") || reportData.get("data") == null) {
                errors.add("Report data is missing");
            }
            
            if (!reportData.containsKey("headers") || reportData.get("headers") == null) {
                warnings.add("Report headers are missing");
            }
            
            // Validate data structure
            if (reportData.containsKey("data")) {
                Object data = reportData.get("data");
                if (data instanceof List) {
                    List<?> dataList = (List<?>) data;
                    if (dataList.isEmpty()) {
                        warnings.add("Report contains no data rows");
                    } else {
                        validateDataConsistency(dataList, warnings);
                    }
                } else {
                    errors.add("Report data must be a list");
                }
            }
            
            // Check data types
            validateDataTypes(reportData, warnings);
            
            boolean isValid = errors.isEmpty();
            
            result.put("valid", isValid);
            result.put("errors", errors);
            result.put("warnings", warnings);
            result.put("errorCount", errors.size());
            result.put("warningCount", warnings.size());
            result.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Error validating report data", e);
            result.put("error", "Failed to validate report data: " + e.getMessage());
        }
        
        return result;
    }

    // Private helper methods
    
    private void validateReportSpecification(Map<String, Object> reportSpec) {
        if (!reportSpec.containsKey("name") || reportSpec.get("name") == null) {
            throw new IllegalArgumentException("Report name is required");
        }
        
        if (!reportSpec.containsKey("type") || reportSpec.get("type") == null) {
            throw new IllegalArgumentException("Report type is required");
        }
        
        if (!reportSpec.containsKey("dataSource") || reportSpec.get("dataSource") == null) {
            throw new IllegalArgumentException("Data source is required");
        }
    }

    private void validateTemplateSpecification(Map<String, Object> templateSpec) {
        if (!templateSpec.containsKey("name") || templateSpec.get("name") == null) {
            throw new IllegalArgumentException("Template name is required");
        }
        
        if (!templateSpec.containsKey("category") || templateSpec.get("category") == null) {
            throw new IllegalArgumentException("Template category is required");
        }
    }

    private Map<String, Object> generateReportData(Map<String, Object> reportSpec) {
        Map<String, Object> reportData = new HashMap<>();
        
        String dataSource = (String) reportSpec.get("dataSource");
        @SuppressWarnings("unchecked")
        Map<String, Object> filters = (Map<String, Object>) reportSpec.get("filters");
        
        // Generate mock data based on data source
        switch (dataSource.toLowerCase()) {
            case "registration":
                reportData = generateRegistrationData(filters);
                break;
            case "payment":
                reportData = generatePaymentData(filters);
                break;
            case "eligibility":
                reportData = generateEligibilityData(filters);
                break;
            case "grievance":
                reportData = generateGrievanceData(filters);
                break;
            default:
                reportData = generateGenericData(filters);
                break;
        }
        
        return reportData;
    }

    private Map<String, Object> applyFormatting(Map<String, Object> reportData, Map<String, Object> reportSpec) {
        Map<String, Object> formatted = new HashMap<>(reportData);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> formatConfig = (Map<String, Object>) reportSpec.get("formatConfig");
        if (formatConfig != null) {
            // Apply number formatting
            if (formatConfig.containsKey("numberFormat")) {
                formatted = applyNumberFormatting(formatted, (String) formatConfig.get("numberFormat"));
            }
            
            // Apply date formatting
            if (formatConfig.containsKey("dateFormat")) {
                formatted = applyDateFormatting(formatted, (String) formatConfig.get("dateFormat"));
            }
            
            // Apply column ordering
            if (formatConfig.containsKey("columnOrder")) {
                @SuppressWarnings("unchecked")
                List<String> columnOrder = (List<String>) formatConfig.get("columnOrder");
                formatted = applyColumnOrdering(formatted, columnOrder);
            }
        }
        
        return formatted;
    }

    private Report saveReport(Map<String, Object> reportSpec, Map<String, Object> reportData) {
        Report report = new Report();
        
        report.setReportCode(generateReportCode((String) reportSpec.get("name")));
        report.setReportName((String) reportSpec.get("name"));
        report.setDescription((String) reportSpec.get("description"));
        report.setReportType(Report.ReportType.CUSTOM);
        report.setCategory(Report.ReportCategory.valueOf(((String) reportSpec.get("category")).toUpperCase()));
        report.setDataSource((String) reportSpec.get("dataSource"));
        report.setQueryConfig(convertToJson(reportSpec.get("queryConfig")));
        report.setFormatConfig(convertToJson(reportSpec.get("formatConfig")));
        report.setFilterConfig(convertToJson(reportSpec.get("filterConfig")));
        report.setOutputFormat((String) reportSpec.getOrDefault("outputFormat", "JSON"));
        report.setCreatedBy((String) reportSpec.get("createdBy"));
        report.setStatus(Report.ReportStatus.COMPLETED);
        
        // Set report metrics
        List<?> data = (List<?>) reportData.get("data");
        if (data != null) {
            report.setRowCount(data.size());
            if (!data.isEmpty() && data.get(0) instanceof Map) {
                report.setColumnCount(((Map<?, ?>) data.get(0)).size());
            }
        }
        
        return reportRepository.save(report);
    }

    private Map<String, Object> generateReportMetadata(Map<String, Object> reportSpec, Map<String, Object> reportData) {
        Map<String, Object> metadata = new HashMap<>();
        
        metadata.put("generatedAt", LocalDateTime.now());
        metadata.put("dataSource", reportSpec.get("dataSource"));
        metadata.put("filters", reportSpec.get("filters"));
        metadata.put("generatedBy", reportSpec.get("createdBy"));
        
        // Data statistics
        List<?> data = (List<?>) reportData.get("data");
        if (data != null) {
            metadata.put("rowCount", data.size());
            if (!data.isEmpty() && data.get(0) instanceof Map) {
                metadata.put("columnCount", ((Map<?, ?>) data.get(0)).size());
            }
        }
        
        return metadata;
    }

    private Map<String, Object> mergeTemplateWithParameters(Report template, Map<String, Object> parameters) {
        Map<String, Object> reportSpec = new HashMap<>();
        
        reportSpec.put("name", template.getReportName() + " - " + LocalDateTime.now().toString());
        reportSpec.put("type", template.getReportType().name());
        reportSpec.put("category", template.getCategory().name());
        reportSpec.put("dataSource", template.getDataSource());
        reportSpec.put("description", template.getDescription());
        
        // Merge template configurations with parameters
        reportSpec.put("queryConfig", parseJson(template.getQueryConfig()));
        reportSpec.put("formatConfig", parseJson(template.getFormatConfig()));
        reportSpec.put("filterConfig", parseJson(template.getFilterConfig()));
        
        // Override with provided parameters
        if (parameters != null) {
            reportSpec.putAll(parameters);
        }
        
        return reportSpec;
    }

    private Map<String, Object> convertTemplateToSummary(Report template) {
        Map<String, Object> summary = new HashMap<>();
        
        summary.put("id", template.getId());
        summary.put("code", template.getReportCode());
        summary.put("name", template.getReportName());
        summary.put("description", template.getDescription());
        summary.put("category", template.getCategory().name());
        summary.put("type", template.getReportType().name());
        summary.put("dataSource", template.getDataSource());
        summary.put("targetRole", template.getTargetRole());
        summary.put("createdAt", template.getCreatedAt());
        summary.put("createdBy", template.getCreatedBy());
        
        return summary;
    }

    private void validateDataConsistency(List<?> dataList, List<String> warnings) {
        if (dataList.isEmpty()) return;
        
        Object firstRow = dataList.get(0);
        if (!(firstRow instanceof Map)) {
            warnings.add("Data rows should be objects/maps");
            return;
        }
        
        Set<String> expectedKeys = ((Map<?, ?>) firstRow).keySet().stream()
                .map(Object::toString)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
        
        for (int i = 1; i < dataList.size(); i++) {
            Object row = dataList.get(i);
            if (row instanceof Map) {
                Set<String> rowKeys = ((Map<?, ?>) row).keySet().stream()
                        .map(Object::toString)
                        .collect(HashSet::new, HashSet::add, HashSet::addAll);
                
                if (!rowKeys.equals(expectedKeys)) {
                    warnings.add("Inconsistent column structure at row " + (i + 1));
                    break;
                }
            }
        }
    }

    private void validateDataTypes(Map<String, Object> reportData, List<String> warnings) {
        // Check for mixed data types in columns
        List<?> data = (List<?>) reportData.get("data");
        if (data == null || data.isEmpty()) return;
        
        if (data.get(0) instanceof Map) {
            Map<?, ?> firstRow = (Map<?, ?>) data.get(0);
            
            for (Object key : firstRow.keySet()) {
                String columnName = key.toString();
                Class<?> expectedType = firstRow.get(key) != null ? firstRow.get(key).getClass() : null;
                
                if (expectedType != null) {
                    for (Object row : data) {
                        if (row instanceof Map) {
                            Object value = ((Map<?, ?>) row).get(key);
                            if (value != null && !expectedType.isAssignableFrom(value.getClass())) {
                                warnings.add("Mixed data types in column: " + columnName);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    // Mock data generation methods
    
    private Map<String, Object> generateRegistrationData(Map<String, Object> filters) {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        
        for (int i = 1; i <= 50; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", "REG-" + String.format("%05d", i));
            row.put("householdHead", "Household Head " + i);
            row.put("region", "Region " + (i % 17 + 1));
            row.put("program", i % 3 == 0 ? "4Ps" : i % 3 == 1 ? "SLP" : "SCP");
            row.put("status", i % 4 == 0 ? "PENDING" : i % 4 == 1 ? "APPROVED" : i % 4 == 2 ? "REJECTED" : "UNDER_REVIEW");
            row.put("registrationDate", LocalDateTime.now().minusDays(i));
            row.put("householdSize", 3 + new Random().nextInt(5));
            rows.add(row);
        }
        
        data.put("data", rows);
        data.put("headers", List.of("ID", "Household Head", "Region", "Program", "Status", "Registration Date", "Household Size"));
        
        return data;
    }

    private Map<String, Object> generatePaymentData(Map<String, Object> filters) {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        
        for (int i = 1; i <= 30; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("paymentId", "PAY-" + String.format("%05d", i));
            row.put("beneficiaryId", "BEN-" + String.format("%05d", i));
            row.put("amount", 1500 + new Random().nextInt(3000));
            row.put("paymentDate", LocalDateTime.now().minusDays(i));
            row.put("status", i % 3 == 0 ? "COMPLETED" : i % 3 == 1 ? "PENDING" : "FAILED");
            row.put("method", i % 2 == 0 ? "BANK_TRANSFER" : "CASH_CARD");
            rows.add(row);
        }
        
        data.put("data", rows);
        data.put("headers", List.of("Payment ID", "Beneficiary ID", "Amount", "Payment Date", "Status", "Method"));
        
        return data;
    }

    private Map<String, Object> generateEligibilityData(Map<String, Object> filters) {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        
        for (int i = 1; i <= 40; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("assessmentId", "ELIG-" + String.format("%05d", i));
            row.put("householdId", "HH-" + String.format("%05d", i));
            row.put("program", i % 3 == 0 ? "4Ps" : i % 3 == 1 ? "SLP" : "SCP");
            row.put("result", i % 4 == 0 ? "ELIGIBLE" : i % 4 == 1 ? "NOT_ELIGIBLE" : i % 4 == 2 ? "CONDITIONAL" : "PENDING");
            row.put("score", 60 + new Random().nextInt(40));
            row.put("assessmentDate", LocalDateTime.now().minusDays(i));
            rows.add(row);
        }
        
        data.put("data", rows);
        data.put("headers", List.of("Assessment ID", "Household ID", "Program", "Result", "Score", "Assessment Date"));
        
        return data;
    }

    private Map<String, Object> generateGrievanceData(Map<String, Object> filters) {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        
        for (int i = 1; i <= 25; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("grievanceId", "GRIEV-" + String.format("%05d", i));
            row.put("complainantId", "COMP-" + String.format("%05d", i));
            row.put("category", i % 4 == 0 ? "PAYMENT_ISSUE" : i % 4 == 1 ? "ELIGIBILITY_DISPUTE" : i % 4 == 2 ? "SERVICE_QUALITY" : "OTHER");
            row.put("priority", i % 3 == 0 ? "HIGH" : i % 3 == 1 ? "MEDIUM" : "LOW");
            row.put("status", i % 5 == 0 ? "RESOLVED" : i % 5 == 1 ? "IN_PROGRESS" : i % 5 == 2 ? "PENDING" : i % 5 == 3 ? "ESCALATED" : "CLOSED");
            row.put("submissionDate", LocalDateTime.now().minusDays(i));
            rows.add(row);
        }
        
        data.put("data", rows);
        data.put("headers", List.of("Grievance ID", "Complainant ID", "Category", "Priority", "Status", "Submission Date"));
        
        return data;
    }

    private Map<String, Object> generateGenericData(Map<String, Object> filters) {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        
        for (int i = 1; i <= 20; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", i);
            row.put("name", "Item " + i);
            row.put("value", new Random().nextInt(1000));
            row.put("date", LocalDateTime.now().minusDays(i));
            rows.add(row);
        }
        
        data.put("data", rows);
        data.put("headers", List.of("ID", "Name", "Value", "Date"));
        
        return data;
    }

    // Formatting helper methods
    
    private Map<String, Object> applyNumberFormatting(Map<String, Object> data, String format) {
        // Simple number formatting implementation
        return data; // Placeholder
    }

    private Map<String, Object> applyDateFormatting(Map<String, Object> data, String format) {
        // Simple date formatting implementation
        return data; // Placeholder
    }

    private Map<String, Object> applyColumnOrdering(Map<String, Object> data, List<String> columnOrder) {
        // Simple column ordering implementation
        return data; // Placeholder
    }

    // Utility methods
    
    private String generateReportCode(String name) {
        return "RPT-" + name.replaceAll("[^A-Za-z0-9]", "").toUpperCase() + "-" + 
               System.currentTimeMillis() % 100000;
    }

    private String convertToJson(Object obj) {
        // Simple JSON conversion - in real implementation would use Jackson
        return obj != null ? obj.toString() : "{}";
    }

    private Map<String, Object> parseJson(String json) {
        // Simple JSON parsing - in real implementation would use Jackson
        return json != null ? new HashMap<>() : new HashMap<>();
    }
}
