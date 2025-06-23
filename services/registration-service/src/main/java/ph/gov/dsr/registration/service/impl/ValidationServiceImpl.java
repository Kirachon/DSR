package ph.gov.dsr.registration.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ph.gov.dsr.registration.dto.RegistrationCreateRequest;
import ph.gov.dsr.registration.dto.ValidationResult;
import ph.gov.dsr.registration.entity.Household;
import ph.gov.dsr.registration.entity.HouseholdMember;
import ph.gov.dsr.registration.repository.HouseholdMemberRepository;
import ph.gov.dsr.registration.repository.HouseholdRepository;
import ph.gov.dsr.registration.service.ValidationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementation of ValidationService with comprehensive business rules
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-22
 */
@Service
@Profile("!no-db")
public class ValidationServiceImpl implements ValidationService {

    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;

    // Validation patterns
    private static final Pattern PSN_PATTERN = Pattern.compile("^\\d{4}-\\d{4}-\\d{4}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+63|0)\\d{10}$");

    // Business rule constants
    private static final BigDecimal POVERTY_THRESHOLD = new BigDecimal("12000.00"); // Monthly income threshold
    private static final int MIN_HOUSEHOLD_SIZE = 1;
    private static final int MAX_HOUSEHOLD_SIZE = 20;
    private static final int MIN_AGE_HEAD_OF_HOUSEHOLD = 18;

    @Autowired
    public ValidationServiceImpl(HouseholdRepository householdRepository, 
                               HouseholdMemberRepository householdMemberRepository) {
        this.householdRepository = householdRepository;
        this.householdMemberRepository = householdMemberRepository;
    }

    @Override
    public ValidationResult validateRegistrationRequest(RegistrationCreateRequest request) {
        List<ValidationResult> results = new ArrayList<>();
        
        // Basic request validation
        results.add(validateBasicRequest(request));
        
        // Household validation
        if (request.getHousehold() != null) {
            results.add(validateHouseholdFromDto(request.getHousehold()));
        }

        // Members validation
        if (request.getMembers() != null && !request.getMembers().isEmpty()) {
            results.add(validateMembersFromDto(request.getMembers()));
        }
        
        // Address validation
        results.add(validateAddress(request));
        
        // Contact validation
        results.add(validateContactInformation(request));
        
        // Business rules validation
        results.add(validateBusinessRules(request));
        
        return ValidationResult.merge(results);
    }

    @Override
    public ValidationResult validateHousehold(Household household) {
        ValidationResult result = new ValidationResult();
        result.setContext("household");
        result.setValid(true);

        if (household == null) {
            result.addError("household", "REQUIRED", "Household information is required");
            return result;
        }

        // Validate household number uniqueness
        if (household.getHouseholdNumber() != null && 
            householdRepository.existsByHouseholdNumber(household.getHouseholdNumber())) {
            result.addError("householdNumber", "DUPLICATE", "Household number already exists");
        }

        // Validate monthly income
        if (household.getMonthlyIncome() != null) {
            if (household.getMonthlyIncome().compareTo(BigDecimal.ZERO) < 0) {
                result.addError("monthlyIncome", "INVALID", "Monthly income cannot be negative");
            }
            if (household.getMonthlyIncome().compareTo(new BigDecimal("1000000")) > 0) {
                result.addWarning("monthlyIncome", "HIGH_INCOME", "Monthly income is unusually high");
            }
        }

        // Validate total members
        if (household.getTotalMembers() != null) {
            if (household.getTotalMembers() < MIN_HOUSEHOLD_SIZE) {
                result.addError("totalMembers", "TOO_SMALL", "Household must have at least " + MIN_HOUSEHOLD_SIZE + " member");
            }
            if (household.getTotalMembers() > MAX_HOUSEHOLD_SIZE) {
                result.addError("totalMembers", "TOO_LARGE", "Household cannot have more than " + MAX_HOUSEHOLD_SIZE + " members");
            }
        }

        return result;
    }

    @Override
    public ValidationResult validateHouseholdMembers(List<HouseholdMember> members) {
        ValidationResult result = new ValidationResult();
        result.setContext("members");
        result.setValid(true);

        if (members == null || members.isEmpty()) {
            result.addError("members", "REQUIRED", "At least one household member is required");
            return result;
        }

        // Check for head of household
        long headCount = members.stream()
                .filter(member -> member.getRelationshipToHead() != null && 
                        member.getRelationshipToHead().name().equals("HEAD"))
                .count();

        if (headCount == 0) {
            result.addError("members", "NO_HEAD", "Household must have a head of household");
        } else if (headCount > 1) {
            result.addError("members", "MULTIPLE_HEADS", "Household can only have one head of household");
        }

        // Validate individual members
        for (int i = 0; i < members.size(); i++) {
            HouseholdMember member = members.get(i);
            ValidationResult memberResult = validateMember(member, i);
            if (!memberResult.isValid()) {
                result.getErrors().addAll(memberResult.getErrors());
                result.setValid(false);
            }
            result.getWarnings().addAll(memberResult.getWarnings());
        }

        // Check for duplicate PSNs
        List<String> psns = members.stream()
                .map(HouseholdMember::getPsn)
                .filter(psn -> psn != null && !psn.trim().isEmpty())
                .toList();
        
        if (psns.size() != psns.stream().distinct().count()) {
            result.addError("members", "DUPLICATE_PSN", "Duplicate PSN numbers found in household members");
        }

        return result;
    }

    @Override
    public ValidationResult validatePSN(String psn) {
        ValidationResult result = new ValidationResult();
        result.setContext("psn");
        result.setValid(true);

        if (psn == null || psn.trim().isEmpty()) {
            return result; // PSN is optional
        }

        // Format validation
        if (!PSN_PATTERN.matcher(psn).matches()) {
            result.addError("psn", "INVALID_FORMAT", "PSN must be in format XXXX-XXXX-XXXX");
            return result;
        }

        // Uniqueness validation
        if (householdMemberRepository.existsByPsn(psn)) {
            result.addError("psn", "DUPLICATE", "PSN already exists in the system");
        }

        return result;
    }

    @Override
    public ValidationResult validateEligibility(Household household, List<HouseholdMember> members) {
        ValidationResult result = new ValidationResult();
        result.setContext("eligibility");
        result.setValid(true);

        // Income-based eligibility
        if (household.getMonthlyIncome() != null && 
            household.getMonthlyIncome().compareTo(POVERTY_THRESHOLD) > 0) {
            result.addWarning("eligibility", "HIGH_INCOME", 
                "Household income exceeds poverty threshold - may not be eligible for all programs");
        }

        // Vulnerable group eligibility
        boolean hasVulnerableMembers = members.stream().anyMatch(member ->
            Boolean.TRUE.equals(member.getIsPwd()));

        if (hasVulnerableMembers) {
            result.addWarning("eligibility", "VULNERABLE_GROUP",
                "Household has vulnerable members - may qualify for additional programs");
        }

        // Indigenous peoples eligibility
        if (household.getIsIndigenous()) {
            result.addWarning("eligibility", "INDIGENOUS", 
                "Indigenous household - may qualify for special programs");
        }

        return result;
    }

    @Override
    public ValidationResult validateRequiredDocuments(RegistrationCreateRequest request) {
        ValidationResult result = new ValidationResult();
        result.setContext("documents");
        result.setValid(true);

        // For now, just add a warning since documents are not yet implemented in the DTO
        result.addWarning("documents", "NO_DOCUMENTS",
            "Document validation not yet implemented - may be required for verification");

        return result;
    }

    @Override
    public ValidationResult validateAddress(RegistrationCreateRequest request) {
        ValidationResult result = new ValidationResult();
        result.setContext("address");
        result.setValid(true);

        if (request.getAddress() == null) {
            result.addError("address", "REQUIRED", "Address information is required");
            return result;
        }

        // Validate required address fields
        if (request.getAddress().getBarangay() == null || request.getAddress().getBarangay().trim().isEmpty()) {
            result.addError("address.barangay", "REQUIRED", "Barangay is required");
        }
        if (request.getAddress().getMunicipality() == null || request.getAddress().getMunicipality().trim().isEmpty()) {
            result.addError("address.municipality", "REQUIRED", "Municipality is required");
        }
        if (request.getAddress().getProvince() == null || request.getAddress().getProvince().trim().isEmpty()) {
            result.addError("address.province", "REQUIRED", "Province is required");
        }
        if (request.getAddress().getRegion() == null || request.getAddress().getRegion().trim().isEmpty()) {
            result.addError("address.region", "REQUIRED", "Region is required");
        }

        return result;
    }

    @Override
    public ValidationResult validateContactInformation(RegistrationCreateRequest request) {
        ValidationResult result = new ValidationResult();
        result.setContext("contact");
        result.setValid(true);

        if (request.getContactInfo() == null) {
            result.addWarning("contact", "NO_CONTACT", "No contact information provided");
            return result;
        }

        // For now, just validate that contact info is provided
        // Full validation will be implemented when contact info structure is finalized
        result.addWarning("contact", "VALIDATION_PENDING", "Contact information validation not fully implemented");

        return result;
    }

    @Override
    public ValidationResult validateForSubmission(RegistrationCreateRequest request) {
        ValidationResult result = validateRegistrationRequest(request);
        
        // Additional submission-specific validations
        if (request.getConsentGiven() == null || !request.getConsentGiven()) {
            result.addError("consent", "REQUIRED", "Consent must be given before submission");
        }

        return result;
    }

    @Override
    public ValidationResult validateBusinessRules(RegistrationCreateRequest request) {
        ValidationResult result = new ValidationResult();
        result.setContext("business_rules");
        result.setValid(true);

        if (request.getMembers() != null && !request.getMembers().isEmpty()) {
            // Validate head of household age
            request.getMembers().stream()
                    .filter(member -> member.getRelationshipToHead() != null &&
                            "HEAD".equals(member.getRelationshipToHead()))
                    .findFirst()
                    .ifPresent(head -> {
                        if (head.getBirthDate() != null) {
                            int age = Period.between(head.getBirthDate(), LocalDate.now()).getYears();
                            if (age < MIN_AGE_HEAD_OF_HOUSEHOLD) {
                                result.addError("head.age", "TOO_YOUNG",
                                    "Head of household must be at least " + MIN_AGE_HEAD_OF_HOUSEHOLD + " years old");
                            }
                        }
                    });

            // Basic family relationship validation
            validateBasicFamilyStructure(request.getMembers(), result);
        }

        return result;
    }

    // Helper methods
    private ValidationResult validateBasicRequest(RegistrationCreateRequest request) {
        ValidationResult result = new ValidationResult();
        result.setContext("basic");
        result.setValid(true);

        if (request == null) {
            result.addError("request", "NULL", "Registration request cannot be null");
            return result;
        }

        if (request.getRegistrationChannel() == null) {
            result.addError("registrationChannel", "REQUIRED", "Registration channel is required");
        }

        return result;
    }

    private ValidationResult validateMember(HouseholdMember member, int index) {
        ValidationResult result = new ValidationResult();
        result.setContext("member_" + index);
        result.setValid(true);

        if (member.getFirstName() == null || member.getFirstName().trim().isEmpty()) {
            result.addError("firstName", "REQUIRED", "First name is required");
        }
        if (member.getLastName() == null || member.getLastName().trim().isEmpty()) {
            result.addError("lastName", "REQUIRED", "Last name is required");
        }
        if (member.getBirthDate() == null) {
            result.addError("birthDate", "REQUIRED", "Birth date is required");
        } else if (member.getBirthDate().isAfter(LocalDate.now())) {
            result.addError("birthDate", "FUTURE_DATE", "Birth date cannot be in the future");
        }
        if (member.getGender() == null) {
            result.addError("gender", "REQUIRED", "Gender is required");
        }
        if (member.getRelationshipToHead() == null) {
            result.addError("relationshipToHead", "REQUIRED", "Relationship to head is required");
        }

        // Validate PSN if provided
        if (member.getPsn() != null && !member.getPsn().trim().isEmpty()) {
            ValidationResult psnResult = validatePSN(member.getPsn());
            if (!psnResult.isValid()) {
                result.getErrors().addAll(psnResult.getErrors());
                result.setValid(false);
            }
        }

        return result;
    }

    // Helper methods for DTO validation
    private ValidationResult validateHouseholdFromDto(RegistrationCreateRequest.HouseholdCreateDto householdDto) {
        ValidationResult result = new ValidationResult();
        result.setContext("household");
        result.setValid(true);

        if (householdDto == null) {
            result.addError("household", "REQUIRED", "Household information is required");
            return result;
        }

        // Validate monthly income
        if (householdDto.getMonthlyIncome() != null) {
            if (householdDto.getMonthlyIncome().compareTo(BigDecimal.ZERO) < 0) {
                result.addError("monthlyIncome", "INVALID", "Monthly income cannot be negative");
            }
            if (householdDto.getMonthlyIncome().compareTo(new BigDecimal("1000000")) > 0) {
                result.addWarning("monthlyIncome", "HIGH_INCOME", "Monthly income is unusually high");
            }
        }

        return result;
    }

    private ValidationResult validateMembersFromDto(List<RegistrationCreateRequest.HouseholdMemberCreateDto> memberDtos) {
        ValidationResult result = new ValidationResult();
        result.setContext("members");
        result.setValid(true);

        if (memberDtos == null || memberDtos.isEmpty()) {
            result.addError("members", "REQUIRED", "At least one household member is required");
            return result;
        }

        // Check for head of household
        long headCount = memberDtos.stream()
                .filter(member -> member.getRelationshipToHead() != null &&
                        "HEAD".equals(member.getRelationshipToHead()))
                .count();

        if (headCount == 0) {
            result.addError("members", "NO_HEAD", "Household must have a head of household");
        } else if (headCount > 1) {
            result.addError("members", "MULTIPLE_HEADS", "Household can only have one head of household");
        }

        // Validate individual members
        for (int i = 0; i < memberDtos.size(); i++) {
            RegistrationCreateRequest.HouseholdMemberCreateDto member = memberDtos.get(i);
            ValidationResult memberResult = validateMemberDto(member, i);
            if (!memberResult.isValid()) {
                result.getErrors().addAll(memberResult.getErrors());
                result.setValid(false);
            }
            result.getWarnings().addAll(memberResult.getWarnings());
        }

        return result;
    }

    private ValidationResult validateMemberDto(RegistrationCreateRequest.HouseholdMemberCreateDto member, int index) {
        ValidationResult result = new ValidationResult();
        result.setContext("member_" + index);
        result.setValid(true);

        if (member.getFirstName() == null || member.getFirstName().trim().isEmpty()) {
            result.addError("firstName", "REQUIRED", "First name is required");
        }
        if (member.getLastName() == null || member.getLastName().trim().isEmpty()) {
            result.addError("lastName", "REQUIRED", "Last name is required");
        }
        if (member.getBirthDate() == null) {
            result.addError("birthDate", "REQUIRED", "Birth date is required");
        } else if (member.getBirthDate().isAfter(LocalDate.now())) {
            result.addError("birthDate", "FUTURE_DATE", "Birth date cannot be in the future");
        }
        if (member.getGender() == null) {
            result.addError("gender", "REQUIRED", "Gender is required");
        }
        if (member.getRelationshipToHead() == null) {
            result.addError("relationshipToHead", "REQUIRED", "Relationship to head is required");
        }

        // Validate PSN if provided
        if (member.getPsn() != null && !member.getPsn().trim().isEmpty()) {
            ValidationResult psnResult = validatePSN(member.getPsn());
            if (!psnResult.isValid()) {
                result.getErrors().addAll(psnResult.getErrors());
                result.setValid(false);
            }
        }

        return result;
    }

    private void validateBasicFamilyStructure(List<RegistrationCreateRequest.HouseholdMemberCreateDto> members, ValidationResult result) {
        // Count spouses
        long spouseCount = members.stream()
                .filter(member -> member.getRelationshipToHead() != null &&
                        "SPOUSE".equals(member.getRelationshipToHead()))
                .count();

        if (spouseCount > 1) {
            result.addWarning("relationships", "MULTIPLE_SPOUSES",
                "Multiple spouses detected - please verify family structure");
        }

        // Basic age validation for head of household
        members.stream()
                .filter(member -> member.getRelationshipToHead() != null &&
                        "HEAD".equals(member.getRelationshipToHead()))
                .findFirst()
                .ifPresent(head -> {
                    if (head.getBirthDate() != null) {
                        int age = Period.between(head.getBirthDate(), LocalDate.now()).getYears();
                        if (age < MIN_AGE_HEAD_OF_HOUSEHOLD) {
                            result.addError("head.age", "TOO_YOUNG",
                                "Head of household must be at least " + MIN_AGE_HEAD_OF_HOUSEHOLD + " years old");
                        }
                    }
                });
    }
}
