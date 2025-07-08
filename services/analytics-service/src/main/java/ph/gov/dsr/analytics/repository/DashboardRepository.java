package ph.gov.dsr.analytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.analytics.entity.Dashboard;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Dashboard entity
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-27
 */
@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, UUID> {

    /**
     * Find dashboard by code
     */
    Optional<Dashboard> findByDashboardCode(String dashboardCode);

    /**
     * Find dashboards by type
     */
    List<Dashboard> findByDashboardTypeOrderBySortOrder(Dashboard.DashboardType dashboardType);

    /**
     * Find dashboards by category
     */
    List<Dashboard> findByCategoryOrderBySortOrder(Dashboard.DashboardCategory category);

    /**
     * Find dashboards by status
     */
    List<Dashboard> findByStatusOrderBySortOrder(Dashboard.DashboardStatus status);

    /**
     * Find active dashboards
     */
    @Query("SELECT d FROM Dashboard d WHERE d.status = 'ACTIVE' ORDER BY d.sortOrder, d.dashboardName")
    List<Dashboard> findActiveDashboards();

    /**
     * Find dashboards by target role
     */
    List<Dashboard> findByTargetRoleOrderBySortOrder(String targetRole);

    /**
     * Find public dashboards
     */
    @Query("SELECT d FROM Dashboard d WHERE d.isPublic = true AND d.status = 'ACTIVE' ORDER BY d.sortOrder")
    List<Dashboard> findPublicDashboards();

    /**
     * Find default dashboards
     */
    @Query("SELECT d FROM Dashboard d WHERE d.isDefault = true AND d.status = 'ACTIVE' ORDER BY d.sortOrder")
    List<Dashboard> findDefaultDashboards();

    /**
     * Find dashboards by access level
     */
    List<Dashboard> findByAccessLevelAndStatusOrderBySortOrder(String accessLevel, Dashboard.DashboardStatus status);

    /**
     * Find dashboards accessible by role
     */
    @Query("SELECT d FROM Dashboard d WHERE d.status = 'ACTIVE' AND " +
           "(d.targetRole = :role OR d.targetRole IS NULL OR d.allowedRoles LIKE %:role%) " +
           "ORDER BY d.sortOrder")
    List<Dashboard> findDashboardsAccessibleByRole(@Param("role") String role);

    /**
     * Find dashboards that need refresh
     */
    @Query("SELECT d FROM Dashboard d WHERE d.status = 'ACTIVE' AND d.autoRefreshEnabled = true AND " +
           "(d.lastRefreshed IS NULL OR d.lastRefreshed < :refreshThreshold)")
    List<Dashboard> findDashboardsNeedingRefresh(@Param("refreshThreshold") LocalDateTime refreshThreshold);

    /**
     * Find recently viewed dashboards
     */
    @Query("SELECT d FROM Dashboard d WHERE d.lastViewed >= :since ORDER BY d.lastViewed DESC")
    List<Dashboard> findRecentlyViewedDashboards(@Param("since") LocalDateTime since);

    /**
     * Find most popular dashboards
     */
    @Query("SELECT d FROM Dashboard d WHERE d.status = 'ACTIVE' ORDER BY d.viewCount DESC")
    List<Dashboard> findMostPopularDashboards();

    /**
     * Find dashboards by name pattern
     */
    @Query("SELECT d FROM Dashboard d WHERE LOWER(d.dashboardName) LIKE LOWER(CONCAT('%', :namePattern, '%')) " +
           "AND d.status = 'ACTIVE' ORDER BY d.dashboardName")
    List<Dashboard> findByNamePattern(@Param("namePattern") String namePattern);

    /**
     * Find dashboards created by user
     */
    List<Dashboard> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    /**
     * Find dashboards updated by user
     */
    List<Dashboard> findByUpdatedByOrderByUpdatedAtDesc(String updatedBy);

    /**
     * Find dashboards created within date range
     */
    @Query("SELECT d FROM Dashboard d WHERE d.createdAt BETWEEN :startDate AND :endDate ORDER BY d.createdAt DESC")
    List<Dashboard> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Find dashboards with real-time enabled
     */
    @Query("SELECT d FROM Dashboard d WHERE d.realTimeEnabled = true AND d.status = 'ACTIVE' ORDER BY d.sortOrder")
    List<Dashboard> findRealTimeDashboards();

    /**
     * Find dashboards with alerts enabled
     */
    @Query("SELECT d FROM Dashboard d WHERE d.alertEnabled = true AND d.status = 'ACTIVE' ORDER BY d.sortOrder")
    List<Dashboard> findDashboardsWithAlerts();

    /**
     * Find dashboards with drill-down enabled
     */
    @Query("SELECT d FROM Dashboard d WHERE d.drillDownEnabled = true AND d.status = 'ACTIVE' ORDER BY d.sortOrder")
    List<Dashboard> findDrillDownDashboards();

    /**
     * Find child dashboards
     */
    List<Dashboard> findByParentDashboardIdOrderBySortOrder(UUID parentDashboardId);

    /**
     * Find root dashboards (no parent)
     */
    @Query("SELECT d FROM Dashboard d WHERE d.parentDashboardId IS NULL AND d.status = 'ACTIVE' ORDER BY d.sortOrder")
    List<Dashboard> findRootDashboards();

    /**
     * Count dashboards by type
     */
    @Query("SELECT d.dashboardType, COUNT(d) FROM Dashboard d GROUP BY d.dashboardType")
    List<Object[]> countDashboardsByType();

    /**
     * Count dashboards by category
     */
    @Query("SELECT d.category, COUNT(d) FROM Dashboard d GROUP BY d.category")
    List<Object[]> countDashboardsByCategory();

    /**
     * Count dashboards by status
     */
    @Query("SELECT d.status, COUNT(d) FROM Dashboard d GROUP BY d.status")
    List<Object[]> countDashboardsByStatus();

    /**
     * Get dashboard usage statistics
     */
    @Query("SELECT COUNT(d), SUM(d.viewCount), AVG(d.viewCount), MAX(d.viewCount) FROM Dashboard d WHERE d.status = 'ACTIVE'")
    Object[] getDashboardUsageStatistics();

    /**
     * Find unused dashboards (never viewed or low view count)
     */
    @Query("SELECT d FROM Dashboard d WHERE d.status = 'ACTIVE' AND (d.viewCount IS NULL OR d.viewCount < :minViews) " +
           "ORDER BY d.createdAt DESC")
    List<Dashboard> findUnusedDashboards(@Param("minViews") Integer minViews);

    /**
     * Find stale dashboards (not viewed recently)
     */
    @Query("SELECT d FROM Dashboard d WHERE d.status = 'ACTIVE' AND " +
           "(d.lastViewed IS NULL OR d.lastViewed < :staleThreshold) " +
           "ORDER BY d.lastViewed ASC")
    List<Dashboard> findStaleDashboards(@Param("staleThreshold") LocalDateTime staleThreshold);

    /**
     * Find dashboards by multiple criteria
     */
    @Query("SELECT d FROM Dashboard d WHERE " +
           "(:dashboardType IS NULL OR d.dashboardType = :dashboardType) AND " +
           "(:category IS NULL OR d.category = :category) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:targetRole IS NULL OR d.targetRole = :targetRole) AND " +
           "(:accessLevel IS NULL OR d.accessLevel = :accessLevel) " +
           "ORDER BY d.sortOrder, d.dashboardName")
    List<Dashboard> findByCriteria(@Param("dashboardType") Dashboard.DashboardType dashboardType,
                                  @Param("category") Dashboard.DashboardCategory category,
                                  @Param("status") Dashboard.DashboardStatus status,
                                  @Param("targetRole") String targetRole,
                                  @Param("accessLevel") String accessLevel);

    /**
     * Search dashboards by text
     */
    @Query("SELECT d FROM Dashboard d WHERE " +
           "(LOWER(d.dashboardName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(d.description) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(d.dashboardCode) LIKE LOWER(CONCAT('%', :searchText, '%'))) AND " +
           "d.status = 'ACTIVE' " +
           "ORDER BY d.dashboardName")
    List<Dashboard> searchDashboards(@Param("searchText") String searchText);

    /**
     * Find dashboards with export enabled
     */
    @Query("SELECT d FROM Dashboard d WHERE d.exportEnabled = true AND d.status = 'ACTIVE' ORDER BY d.sortOrder")
    List<Dashboard> findExportableDashboards();

    /**
     * Find dashboards with caching enabled
     */
    @Query("SELECT d FROM Dashboard d WHERE d.cacheEnabled = true AND d.status = 'ACTIVE' ORDER BY d.sortOrder")
    List<Dashboard> findCacheableDashboards();

    /**
     * Find dashboards with invalid cache
     */
    @Query("SELECT d FROM Dashboard d WHERE d.cacheEnabled = true AND d.status = 'ACTIVE' AND " +
           "(d.lastRefreshed IS NULL OR d.lastRefreshed < :cacheExpiry)")
    List<Dashboard> findDashboardsWithInvalidCache(@Param("cacheExpiry") LocalDateTime cacheExpiry);

    /**
     * Get dashboard performance metrics
     */
    @Query("SELECT d.dashboardCode, d.viewCount, d.lastViewed, " +
           "EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - d.lastRefreshed)) as secondsSinceRefresh " +
           "FROM Dashboard d WHERE d.status = 'ACTIVE' ORDER BY d.viewCount DESC")
    List<Object[]> getDashboardPerformanceMetrics();

    /**
     * Find dashboards by tag
     */
    @Query("SELECT d FROM Dashboard d WHERE d.tags LIKE %:tag% AND d.status = 'ACTIVE' ORDER BY d.sortOrder")
    List<Dashboard> findByTag(@Param("tag") String tag);

    /**
     * Update view count
     */
    @Query("UPDATE Dashboard d SET d.viewCount = d.viewCount + 1, d.lastViewed = CURRENT_TIMESTAMP WHERE d.id = :id")
    void incrementViewCount(@Param("id") UUID id);

    /**
     * Update last refreshed timestamp
     */
    @Query("UPDATE Dashboard d SET d.lastRefreshed = CURRENT_TIMESTAMP WHERE d.id = :id")
    void updateLastRefreshed(@Param("id") UUID id);

    /**
     * Bulk update status
     */
    @Query("UPDATE Dashboard d SET d.status = :newStatus WHERE d.id IN :ids")
    void bulkUpdateStatus(@Param("ids") List<UUID> ids, @Param("newStatus") Dashboard.DashboardStatus newStatus);

    /**
     * Delete old dashboards
     */
    @Query("DELETE FROM Dashboard d WHERE d.status = 'ARCHIVED' AND d.updatedAt < :cutoffDate")
    void deleteOldArchivedDashboards(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find duplicate dashboard codes
     */
    @Query("SELECT d.dashboardCode FROM Dashboard d GROUP BY d.dashboardCode HAVING COUNT(d) > 1")
    List<String> findDuplicateDashboardCodes();

    /**
     * Get dashboard hierarchy
     */
    @Query("SELECT d FROM Dashboard d WHERE d.parentDashboardId = :parentId OR " +
           "(d.parentDashboardId IS NULL AND :parentId IS NULL) " +
           "ORDER BY d.sortOrder")
    List<Dashboard> findDashboardHierarchy(@Param("parentId") UUID parentId);

    /**
     * Find dashboards requiring maintenance
     */
    @Query("SELECT d FROM Dashboard d WHERE d.status = 'MAINTENANCE' OR " +
           "(d.autoRefreshEnabled = true AND d.lastRefreshed < :maintenanceThreshold)")
    List<Dashboard> findDashboardsRequiringMaintenance(@Param("maintenanceThreshold") LocalDateTime maintenanceThreshold);
}
