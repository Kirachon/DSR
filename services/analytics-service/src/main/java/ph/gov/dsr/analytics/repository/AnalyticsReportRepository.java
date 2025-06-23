package ph.gov.dsr.analytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.analytics.entity.AnalyticsReport;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for AnalyticsReport entity
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Repository
public interface AnalyticsReportRepository extends JpaRepository<AnalyticsReport, UUID> {
    
    List<AnalyticsReport> findByReportCodeOrderByGenerationDateDesc(String reportCode);
    
    List<AnalyticsReport> findByCategory(AnalyticsReport.ReportCategory category);
    
    List<AnalyticsReport> findByStatus(AnalyticsReport.ReportStatus status);
}
