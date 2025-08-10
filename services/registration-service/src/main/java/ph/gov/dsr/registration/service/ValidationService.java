package ph.gov.dsr.registration.service;

import ph.gov.dsr.registration.dto.RegistrationCreateRequest;
import ph.gov.dsr.registration.dto.ValidationResult;
import ph.gov.dsr.registration.entity.Household;
import ph.gov.dsr.registration.entity.HouseholdMember;

import java.util.List;

/**
 * Service interface for validation operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-22
 */
public interface ValidationService {

    /**
     * Validate a registration request
     */
    ValidationResult validateRegistrationRequest(RegistrationCreateRequest request);

    /**
     * Validate household information
     */
    ValidationResult validateHousehold(Household household);

    /**
     * Validate household members
     */
    ValidationResult validateHouseholdMembers(List<HouseholdMember> members);

    /**
     * Validate PSN (PhilSys Number) format and uniqueness
     */
    ValidationResult validatePSN(String psn);

    /**
     * Validate household eligibility for DSR registration
     */
    ValidationResult validateEligibility(Household household, List<HouseholdMember> members);

    /**
     * Validate required documents
     */
    ValidationResult validateRequiredDocuments(RegistrationCreateRequest request);

    /**
     * Validate address information
     */
    ValidationResult validateAddress(RegistrationCreateRequest request);

    /**
     * Validate contact information
     */
    ValidationResult validateContactInformation(RegistrationCreateRequest request);

    /**
     * Comprehensive validation for registration submission
     */
    ValidationResult validateForSubmission(RegistrationCreateRequest request);

    /**
     * Validate data consistency and business rules
     */
    ValidationResult validateBusinessRules(RegistrationCreateRequest request);
}
