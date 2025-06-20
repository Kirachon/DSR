package ph.gov.dsr.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pagination information for paginated API responses.
 * Provides consistent pagination metadata across all DSR services.
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginationInfo {

    /**
     * Current page number (1-based).
     */
    private int page;

    /**
     * Number of items per page.
     */
    private int size;

    /**
     * Total number of items across all pages.
     */
    private long totalElements;

    /**
     * Total number of pages.
     */
    private int totalPages;

    /**
     * Number of items in the current page.
     */
    private int numberOfElements;

    /**
     * Whether this is the first page.
     */
    private boolean first;

    /**
     * Whether this is the last page.
     */
    private boolean last;

    /**
     * Whether there is a next page.
     */
    private boolean hasNext;

    /**
     * Whether there is a previous page.
     */
    private boolean hasPrevious;

    /**
     * Sort information.
     */
    private SortInfo sort;

    /**
     * Creates pagination info from Spring Data Page.
     * 
     * @param page Spring Data Page object
     * @return PaginationInfo instance
     */
    public static PaginationInfo fromPage(org.springframework.data.domain.Page<?> page) {
        return PaginationInfo.builder()
                .page(page.getNumber() + 1) // Convert to 1-based
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .numberOfElements(page.getNumberOfElements())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .sort(SortInfo.fromSort(page.getSort()))
                .build();
    }

    /**
     * Sort information structure.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SortInfo {
        /**
         * Whether the result is sorted.
         */
        private boolean sorted;

        /**
         * Whether the result is unsorted.
         */
        private boolean unsorted;

        /**
         * Whether the result is empty.
         */
        private boolean empty;

        /**
         * Creates sort info from Spring Data Sort.
         * 
         * @param sort Spring Data Sort object
         * @return SortInfo instance
         */
        public static SortInfo fromSort(org.springframework.data.domain.Sort sort) {
            return SortInfo.builder()
                    .sorted(sort.isSorted())
                    .unsorted(sort.isUnsorted())
                    .empty(sort.isEmpty())
                    .build();
        }
    }
}
