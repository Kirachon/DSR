package ph.gov.dsr.eligibility.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ph.gov.dsr.eligibility.dto.ProgramInfo;
import ph.gov.dsr.eligibility.service.ProgramManagementService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mock implementation of ProgramManagementService for no-database mode
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@Profile("no-db")
@Slf4j
public class MockProgramManagementServiceImpl implements ProgramManagementService {

    private final Map<String, ProgramInfo> programs = new HashMap<>();
    private final Map<String, Map<String, Object>> programStatistics = new HashMap<>();

    public MockProgramManagementServiceImpl() {
        initializeMockPrograms();
    }

    @Override
    public List<ProgramInfo> getAllPrograms(boolean activeOnly) {
        log.info("Mock getting all programs, activeOnly: {}", activeOnly);
        
        return programs.values().stream()
                .filter(program -> !activeOnly || program.getStatus() == ProgramInfo.ProgramStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    @Override
    public ProgramInfo getProgramByCode(String programCode) {
        log.info("Mock getting program by code: {}", programCode);
        return programs.get(programCode);
    }

    @Override
    public List<ProgramInfo> getProgramsByAgency(String agency, boolean activeOnly) {
        log.info("Mock getting programs by agency: {}, activeOnly: {}", agency, activeOnly);
        
        return programs.values().stream()
                .filter(program -> agency.equals(program.getImplementingAgency()))
                .filter(program -> !activeOnly || program.getStatus() == ProgramInfo.ProgramStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProgramInfo> getProgramsByType(ProgramInfo.ProgramType programType, boolean activeOnly) {
        log.info("Mock getting programs by type: {}, activeOnly: {}", programType, activeOnly);
        
        return programs.values().stream()
                .filter(program -> programType.equals(program.getProgramType()))
                .filter(program -> !activeOnly || program.getStatus() == ProgramInfo.ProgramStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProgramInfo> searchPrograms(Map<String, Object> searchCriteria) {
        log.info("Mock searching programs with criteria: {}", searchCriteria);
        
        return programs.values().stream()
                .filter(program -> matchesSearchCriteria(program, searchCriteria))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProgramInfo> getProgramsByLocation(String region, String province, String cityMunicipality) {
        log.info("Mock getting programs by location: {}, {}, {}", region, province, cityMunicipality);
        
        // In mock implementation, most programs are available nationwide
        return programs.values().stream()
                .filter(program -> program.getStatus() == ProgramInfo.ProgramStatus.ACTIVE)
                .filter(program -> program.getCoverageAreas().contains("NATIONWIDE") || 
                                 program.getCoverageAreas().contains(region))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isProgramAcceptingApplications(String programCode) {
        log.info("Mock checking if program is accepting applications: {}", programCode);
        
        ProgramInfo program = programs.get(programCode);
        return program != null && 
               Boolean.TRUE.equals(program.getAcceptingApplications()) &&
               program.getStatus() == ProgramInfo.ProgramStatus.ACTIVE;
    }

    @Override
    public Map<String, Object> getProgramCapacity(String programCode) {
        log.info("Mock getting program capacity for: {}", programCode);
        
        ProgramInfo program = programs.get(programCode);
        if (program == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> capacity = new HashMap<>();
        capacity.put("targetBeneficiaries", program.getTargetBeneficiaries());
        capacity.put("currentBeneficiaries", program.getCurrentBeneficiaries());
        capacity.put("availableSlots", program.getAvailableSlots());
        capacity.put("utilizationRate", calculateUtilizationRate(program));
        
        return capacity;
    }

    @Override
    public Map<String, Object> getProgramStatistics(String programCode) {
        log.info("Mock getting program statistics for: {}", programCode);
        
        return programStatistics.computeIfAbsent(programCode, k -> {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalApplications", 15000 + new Random().nextInt(5000));
            stats.put("approvedApplications", 12000 + new Random().nextInt(2000));
            stats.put("rejectedApplications", 2000 + new Random().nextInt(1000));
            stats.put("pendingApplications", 1000 + new Random().nextInt(500));
            stats.put("averageProcessingDays", 15 + new Random().nextInt(10));
            stats.put("lastUpdated", LocalDate.now());
            return stats;
        });
    }

    @Override
    public ProgramInfo updateProgramStatus(String programCode, ProgramInfo.ProgramStatus status, 
                                          String reason, String updatedBy) {
        log.info("Mock updating program status for: {}, new status: {}", programCode, status);
        
        ProgramInfo program = programs.get(programCode);
        if (program != null) {
            program.setStatus(status);
            // In real implementation, would also log the change with reason and updatedBy
        }
        
        return program;
    }

    @Override
    public ProgramInfo updateProgramCapacity(String programCode, Integer newCapacity, 
                                           String reason, String updatedBy) {
        log.info("Mock updating program capacity for: {}, new capacity: {}", programCode, newCapacity);
        
        ProgramInfo program = programs.get(programCode);
        if (program != null) {
            Integer currentBeneficiaries = program.getCurrentBeneficiaries();
            program.setTargetBeneficiaries(newCapacity);
            program.setAvailableSlots(newCapacity - currentBeneficiaries);
            // In real implementation, would also log the change with reason and updatedBy
        }
        
        return program;
    }

    @Override
    public List<ProgramInfo> getEligiblePrograms(Double householdIncome, Integer householdSize, 
                                               Map<String, String> location, List<String> vulnerabilityFactors) {
        log.info("Mock getting eligible programs for income: {}, size: {}", householdIncome, householdSize);
        
        // Simple mock logic - return programs based on income threshold
        return programs.values().stream()
                .filter(program -> program.getStatus() == ProgramInfo.ProgramStatus.ACTIVE)
                .filter(program -> Boolean.TRUE.equals(program.getAcceptingApplications()))
                .filter(program -> isIncomeEligible(program, householdIncome, householdSize))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getProgramEnrollmentTrends(String programCode, int months) {
        log.info("Mock getting enrollment trends for: {}, months: {}", programCode, months);
        
        Map<String, Object> trends = new HashMap<>();
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        
        for (int i = months - 1; i >= 0; i--) {
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", LocalDate.now().minusMonths(i));
            monthData.put("newEnrollments", 800 + new Random().nextInt(400));
            monthData.put("totalBeneficiaries", 50000 + new Random().nextInt(10000));
            monthlyData.add(monthData);
        }
        
        trends.put("monthlyData", monthlyData);
        trends.put("averageMonthlyGrowth", 5.2 + new Random().nextDouble() * 3);
        trends.put("totalGrowthRate", 15.8 + new Random().nextDouble() * 10);
        
        return trends;
    }

    @Override
    public Map<String, Object> validateProgramConfiguration(String programCode) {
        log.info("Mock validating program configuration for: {}", programCode);
        
        Map<String, Object> validation = new HashMap<>();
        validation.put("isValid", true);
        validation.put("warnings", new ArrayList<>());
        validation.put("errors", new ArrayList<>());
        validation.put("lastValidated", LocalDate.now());
        
        return validation;
    }

    // Helper methods

    private void initializeMockPrograms() {
        // 4Ps Program
        ProgramInfo fourPs = new ProgramInfo();
        fourPs.setProgramCode("4PS_CONDITIONAL_CASH");
        fourPs.setProgramName("Pantawid Pamilyang Pilipino Program");
        fourPs.setDescription("Conditional cash transfer program for poor families with children");
        fourPs.setImplementingAgency("DSWD");
        fourPs.setProgramType(ProgramInfo.ProgramType.CONDITIONAL_CASH_TRANSFER);
        fourPs.setStatus(ProgramInfo.ProgramStatus.ACTIVE);
        fourPs.setStartDate(LocalDate.of(2008, 1, 1));
        fourPs.setTargetBeneficiaries(4000000);
        fourPs.setCurrentBeneficiaries(3200000);
        fourPs.setAvailableSlots(800000);
        fourPs.setBudgetAllocation(new BigDecimal("89000000000"));
        fourPs.setBenefitAmount(new BigDecimal("1400"));
        fourPs.setBenefitFrequency(ProgramInfo.BenefitFrequency.MONTHLY);
        fourPs.setCoverageAreas(Arrays.asList("NATIONWIDE"));
        fourPs.setAcceptingApplications(true);
        fourPs.setRequiredDocuments(Arrays.asList("Birth Certificate", "Barangay Certificate", "Income Certificate"));
        programs.put(fourPs.getProgramCode(), fourPs);

        // Senior Citizens Program
        ProgramInfo seniorCitizens = new ProgramInfo();
        seniorCitizens.setProgramCode("SENIOR_CITIZEN_PENSION");
        seniorCitizens.setProgramName("Social Pension for Indigent Senior Citizens");
        seniorCitizens.setDescription("Monthly pension for indigent senior citizens");
        seniorCitizens.setImplementingAgency("DSWD");
        seniorCitizens.setProgramType(ProgramInfo.ProgramType.SENIOR_CITIZEN_BENEFIT);
        seniorCitizens.setStatus(ProgramInfo.ProgramStatus.ACTIVE);
        seniorCitizens.setStartDate(LocalDate.of(2011, 1, 1));
        seniorCitizens.setTargetBeneficiaries(3000000);
        seniorCitizens.setCurrentBeneficiaries(2800000);
        seniorCitizens.setAvailableSlots(200000);
        seniorCitizens.setBudgetAllocation(new BigDecimal("16800000000"));
        seniorCitizens.setBenefitAmount(new BigDecimal("500"));
        seniorCitizens.setBenefitFrequency(ProgramInfo.BenefitFrequency.MONTHLY);
        seniorCitizens.setCoverageAreas(Arrays.asList("NATIONWIDE"));
        seniorCitizens.setAcceptingApplications(true);
        programs.put(seniorCitizens.getProgramCode(), seniorCitizens);

        // PWD Assistance
        ProgramInfo pwdAssistance = new ProgramInfo();
        pwdAssistance.setProgramCode("PWD_ASSISTANCE");
        pwdAssistance.setProgramName("Assistance to Individuals in Crisis Situation - PWD");
        pwdAssistance.setDescription("Financial assistance for persons with disabilities");
        pwdAssistance.setImplementingAgency("DSWD");
        pwdAssistance.setProgramType(ProgramInfo.ProgramType.PWD_ASSISTANCE);
        pwdAssistance.setStatus(ProgramInfo.ProgramStatus.ACTIVE);
        pwdAssistance.setStartDate(LocalDate.of(2015, 1, 1));
        pwdAssistance.setTargetBeneficiaries(500000);
        pwdAssistance.setCurrentBeneficiaries(350000);
        pwdAssistance.setAvailableSlots(150000);
        pwdAssistance.setBudgetAllocation(new BigDecimal("2500000000"));
        pwdAssistance.setBenefitAmount(new BigDecimal("3000"));
        pwdAssistance.setBenefitFrequency(ProgramInfo.BenefitFrequency.ONE_TIME);
        pwdAssistance.setCoverageAreas(Arrays.asList("NATIONWIDE"));
        pwdAssistance.setAcceptingApplications(true);
        programs.put(pwdAssistance.getProgramCode(), pwdAssistance);
    }

    private boolean matchesSearchCriteria(ProgramInfo program, Map<String, Object> searchCriteria) {
        for (Map.Entry<String, Object> criteria : searchCriteria.entrySet()) {
            String key = criteria.getKey();
            Object value = criteria.getValue();

            switch (key) {
                case "programName":
                    if (!program.getProgramName().toLowerCase().contains(value.toString().toLowerCase())) {
                        return false;
                    }
                    break;
                case "implementingAgency":
                    if (!program.getImplementingAgency().equals(value)) {
                        return false;
                    }
                    break;
                case "programType":
                    if (!program.getProgramType().toString().equals(value.toString())) {
                        return false;
                    }
                    break;
                case "status":
                    if (!program.getStatus().toString().equals(value.toString())) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }

    private double calculateUtilizationRate(ProgramInfo program) {
        if (program.getTargetBeneficiaries() == null || program.getTargetBeneficiaries() == 0) {
            return 0.0;
        }

        double current = program.getCurrentBeneficiaries() != null ? program.getCurrentBeneficiaries() : 0;
        return (current / program.getTargetBeneficiaries()) * 100;
    }

    private boolean isIncomeEligible(ProgramInfo program, Double householdIncome, Integer householdSize) {
        // Simple mock logic - assume programs have income thresholds
        if (householdIncome == null || householdSize == null) {
            return true; // Default to eligible if no income info
        }

        double perCapitaIncome = householdIncome / householdSize;

        // Mock income thresholds for different programs
        switch (program.getProgramCode()) {
            case "4PS_CONDITIONAL_CASH":
                return perCapitaIncome <= 3000; // 4Ps threshold
            case "SENIOR_CITIZEN_PENSION":
                return perCapitaIncome <= 2500; // Senior citizen threshold
            case "PWD_ASSISTANCE":
                return perCapitaIncome <= 4000; // PWD assistance threshold
            default:
                return perCapitaIncome <= 3500; // Default threshold
        }
    }
}
