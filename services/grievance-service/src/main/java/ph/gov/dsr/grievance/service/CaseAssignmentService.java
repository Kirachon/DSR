package ph.gov.dsr.grievance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ph.gov.dsr.grievance.entity.GrievanceCase;

import java.util.*;

/**
 * Service for intelligent case assignment based on workload and expertise
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseAssignmentService {

    // Staff expertise mapping
    private static final Map<GrievanceCase.GrievanceCategory, List<String>> CATEGORY_EXPERTS = createCategoryExpertsMap();

    // Escalation hierarchy
    private static final Map<String, String> ESCALATION_HIERARCHY = createEscalationHierarchyMap();

    // Simulated workload tracking (in production, this would be from database)
    private final Map<String, Integer> currentWorkload = new HashMap<>();

    /**
     * Find best assignee based on category and current workload
     */
    public String findBestAssignee(GrievanceCase.GrievanceCategory category, GrievanceCase.Priority priority) {
        log.debug("Finding best assignee for category: {} with priority: {}", category, priority);
        
        List<String> experts = CATEGORY_EXPERTS.get(category);
        if (experts == null || experts.isEmpty()) {
            experts = CATEGORY_EXPERTS.get(GrievanceCase.GrievanceCategory.OTHER);
        }
        
        // Find expert with lowest workload
        String bestAssignee = experts.stream()
            .min(Comparator.comparingInt(this::getCurrentWorkload))
            .orElse(experts.get(0));
        
        // For critical cases, prefer senior staff
        if (priority == GrievanceCase.Priority.CRITICAL) {
            bestAssignee = findSeniorStaff(experts);
        }
        
        // Update workload
        incrementWorkload(bestAssignee);
        
        log.debug("Assigned case to: {} (current workload: {})", bestAssignee, getCurrentWorkload(bestAssignee));
        return bestAssignee;
    }

    /**
     * Find escalation target for a staff member
     */
    public String findEscalationTarget(String currentAssignee, GrievanceCase.GrievanceCategory category) {
        log.debug("Finding escalation target for: {} in category: {}", currentAssignee, category);
        
        // First try direct hierarchy escalation
        String escalationTarget = ESCALATION_HIERARCHY.get(currentAssignee);
        
        // If no direct escalation, find category manager
        if (escalationTarget == null) {
            List<String> experts = CATEGORY_EXPERTS.get(category);
            if (experts != null && experts.size() > 1) {
                // Find the most senior person in the category
                escalationTarget = findSeniorStaff(experts);
            }
        }
        
        // Default to regional director
        if (escalationTarget == null) {
            escalationTarget = "regional.director@dswd.gov.ph";
        }
        
        log.debug("Escalation target found: {}", escalationTarget);
        return escalationTarget;
    }

    /**
     * Get current workload for a staff member
     */
    public int getCurrentWorkload(String staffEmail) {
        return currentWorkload.getOrDefault(staffEmail, 0);
    }

    /**
     * Increment workload for a staff member
     */
    public void incrementWorkload(String staffEmail) {
        currentWorkload.merge(staffEmail, 1, Integer::sum);
    }

    /**
     * Decrement workload for a staff member (when case is resolved/closed)
     */
    public void decrementWorkload(String staffEmail) {
        currentWorkload.merge(staffEmail, -1, (current, decrement) -> Math.max(0, current + decrement));
    }

    /**
     * Get workload distribution across all staff
     */
    public Map<String, Integer> getWorkloadDistribution() {
        return new HashMap<>(currentWorkload);
    }

    /**
     * Find staff members with capacity for new cases
     */
    public List<String> findAvailableStaff(GrievanceCase.GrievanceCategory category) {
        List<String> experts = CATEGORY_EXPERTS.get(category);
        if (experts == null) {
            experts = CATEGORY_EXPERTS.get(GrievanceCase.GrievanceCategory.OTHER);
        }
        
        return experts.stream()
            .filter(staff -> getCurrentWorkload(staff) < getMaxWorkloadCapacity(staff))
            .sorted(Comparator.comparingInt(this::getCurrentWorkload))
            .toList();
    }

    /**
     * Reassign case to different staff member
     */
    public String reassignCase(String currentAssignee, GrievanceCase.GrievanceCategory category, 
                             GrievanceCase.Priority priority, String reason) {
        log.info("Reassigning case from {} due to: {}", currentAssignee, reason);
        
        // Decrement current assignee's workload
        if (currentAssignee != null) {
            decrementWorkload(currentAssignee);
        }
        
        // Find new assignee
        String newAssignee = findBestAssignee(category, priority);
        
        log.info("Case reassigned from {} to {}", currentAssignee, newAssignee);
        return newAssignee;
    }

    /**
     * Balance workload across team members
     */
    public Map<String, String> balanceWorkload(GrievanceCase.GrievanceCategory category) {
        log.info("Balancing workload for category: {}", category);
        
        List<String> experts = CATEGORY_EXPERTS.get(category);
        if (experts == null || experts.size() < 2) {
            return Collections.emptyMap();
        }
        
        Map<String, String> reassignments = new HashMap<>();
        
        // Find overloaded and underloaded staff
        int avgWorkload = experts.stream()
            .mapToInt(this::getCurrentWorkload)
            .sum() / experts.size();
        
        List<String> overloaded = experts.stream()
            .filter(staff -> getCurrentWorkload(staff) > avgWorkload + 2)
            .toList();
        
        List<String> underloaded = experts.stream()
            .filter(staff -> getCurrentWorkload(staff) < avgWorkload - 1)
            .sorted(Comparator.comparingInt(this::getCurrentWorkload))
            .toList();
        
        // Suggest reassignments
        for (String overloadedStaff : overloaded) {
            if (!underloaded.isEmpty()) {
                String targetStaff = underloaded.get(0);
                reassignments.put(overloadedStaff, targetStaff);
                
                // Update workload for simulation
                decrementWorkload(overloadedStaff);
                incrementWorkload(targetStaff);
                
                // Remove from underloaded if they reach average
                if (getCurrentWorkload(targetStaff) >= avgWorkload) {
                    underloaded.remove(0);
                }
            }
        }
        
        log.info("Workload balancing completed. {} reassignments suggested", reassignments.size());
        return reassignments;
    }

    // Helper methods
    
    private String findSeniorStaff(List<String> experts) {
        // Simple heuristic: staff with "manager" or "director" in email are senior
        return experts.stream()
            .filter(email -> email.contains("manager") || email.contains("director"))
            .findFirst()
            .orElse(experts.get(experts.size() - 1)); // Last in list as fallback
    }

    private int getMaxWorkloadCapacity(String staffEmail) {
        // Senior staff can handle more cases
        if (staffEmail.contains("manager") || staffEmail.contains("director")) {
            return 15;
        } else if (staffEmail.contains("specialist") || staffEmail.contains("officer")) {
            return 10;
        } else {
            return 8;
        }
    }

    /**
     * Get staff expertise areas
     */
    public List<GrievanceCase.GrievanceCategory> getStaffExpertise(String staffEmail) {
        return CATEGORY_EXPERTS.entrySet().stream()
            .filter(entry -> entry.getValue().contains(staffEmail))
            .map(Map.Entry::getKey)
            .toList();
    }

    /**
     * Check if staff member is available for assignment
     */
    public boolean isStaffAvailable(String staffEmail) {
        return getCurrentWorkload(staffEmail) < getMaxWorkloadCapacity(staffEmail);
    }

    /**
     * Get recommended assignment based on multiple factors
     */
    public String getRecommendedAssignment(GrievanceCase.GrievanceCategory category, 
                                         GrievanceCase.Priority priority, 
                                         String preferredStaff) {
        // If preferred staff is available and has expertise, assign to them
        if (preferredStaff != null && isStaffAvailable(preferredStaff) && 
            getStaffExpertise(preferredStaff).contains(category)) {
            incrementWorkload(preferredStaff);
            return preferredStaff;
        }
        
        // Otherwise, use standard assignment logic
        return findBestAssignee(category, priority);
    }

    // Static initialization methods

    private static Map<GrievanceCase.GrievanceCategory, List<String>> createCategoryExpertsMap() {
        Map<GrievanceCase.GrievanceCategory, List<String>> map = new HashMap<>();
        map.put(GrievanceCase.GrievanceCategory.SERVICE_DELIVERY, Arrays.asList("service.specialist@dswd.gov.ph", "operations.manager@dswd.gov.ph"));
        map.put(GrievanceCase.GrievanceCategory.PAYMENT_ISSUE, Arrays.asList("payment.specialist@dswd.gov.ph", "finance.officer@dswd.gov.ph"));
        map.put(GrievanceCase.GrievanceCategory.ELIGIBILITY_DISPUTE, Arrays.asList("eligibility.officer@dswd.gov.ph", "assessment.specialist@dswd.gov.ph"));
        map.put(GrievanceCase.GrievanceCategory.STAFF_CONDUCT, Arrays.asList("hr.manager@dswd.gov.ph", "ethics.officer@dswd.gov.ph"));
        map.put(GrievanceCase.GrievanceCategory.SYSTEM_ERROR, Arrays.asList("it.support@dswd.gov.ph", "system.admin@dswd.gov.ph"));
        map.put(GrievanceCase.GrievanceCategory.DATA_PRIVACY, Arrays.asList("privacy.officer@dswd.gov.ph", "legal.counsel@dswd.gov.ph"));
        map.put(GrievanceCase.GrievanceCategory.DISCRIMINATION, Arrays.asList("legal.counsel@dswd.gov.ph", "ethics.officer@dswd.gov.ph"));
        map.put(GrievanceCase.GrievanceCategory.CORRUPTION, Arrays.asList("integrity.officer@dswd.gov.ph", "legal.counsel@dswd.gov.ph"));
        map.put(GrievanceCase.GrievanceCategory.ACCESS_ISSUE, Arrays.asList("accessibility.officer@dswd.gov.ph", "service.specialist@dswd.gov.ph"));
        map.put(GrievanceCase.GrievanceCategory.QUALITY_CONCERN, Arrays.asList("quality.assurance@dswd.gov.ph", "operations.manager@dswd.gov.ph"));
        map.put(GrievanceCase.GrievanceCategory.OTHER, Arrays.asList("general.officer@dswd.gov.ph", "case.manager@dswd.gov.ph"));
        return map;
    }

    private static Map<String, String> createEscalationHierarchyMap() {
        Map<String, String> map = new HashMap<>();
        map.put("service.specialist@dswd.gov.ph", "operations.manager@dswd.gov.ph");
        map.put("payment.specialist@dswd.gov.ph", "finance.manager@dswd.gov.ph");
        map.put("eligibility.officer@dswd.gov.ph", "assessment.manager@dswd.gov.ph");
        map.put("it.support@dswd.gov.ph", "it.manager@dswd.gov.ph");
        map.put("general.officer@dswd.gov.ph", "case.manager@dswd.gov.ph");
        map.put("operations.manager@dswd.gov.ph", "regional.director@dswd.gov.ph");
        map.put("finance.manager@dswd.gov.ph", "regional.director@dswd.gov.ph");
        map.put("case.manager@dswd.gov.ph", "regional.director@dswd.gov.ph");
        return map;
    }
}
