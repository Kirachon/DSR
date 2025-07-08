'use client';

// Pagination Component
// Comprehensive pagination component with various display options

import { cva, type VariantProps } from 'class-variance-authority';
import React, { useMemo } from 'react';

import { cn } from '@/utils';

// Pagination variants
export const paginationVariants = cva(
  'flex items-center justify-center space-x-1',
  {
    variants: {
      size: {
        sm: 'text-sm',
        default: 'text-base',
        lg: 'text-lg',
      },
      variant: {
        default: '',
        compact: 'space-x-0',
        spaced: 'space-x-2',
      },
    },
    defaultVariants: {
      size: 'default',
      variant: 'default',
    },
  }
);

// Pagination button variants
export const paginationButtonVariants = cva(
  'inline-flex items-center justify-center rounded-md border border-gray-300 bg-white px-3 py-2 text-sm font-medium text-gray-500 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
  {
    variants: {
      variant: {
        default: '',
        current:
          'border-primary-500 bg-primary-50 text-primary-600 hover:bg-primary-100',
        ghost:
          'border-transparent bg-transparent hover:bg-gray-100 hover:text-gray-900',
      },
      size: {
        sm: 'px-2 py-1 text-xs',
        default: 'px-3 py-2 text-sm',
        lg: 'px-4 py-3 text-base',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'default',
    },
  }
);

// Pagination props interface
export interface PaginationProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof paginationVariants> {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  showFirstLast?: boolean;
  showPrevNext?: boolean;
  showPageNumbers?: boolean;
  maxVisiblePages?: number;
  disabled?: boolean;
  showInfo?: boolean;
  totalItems?: number;
  itemsPerPage?: number;
}

// Generate page numbers to display
const generatePageNumbers = (
  currentPage: number,
  totalPages: number,
  maxVisible: number
): (number | string)[] => {
  if (totalPages <= maxVisible) {
    return Array.from({ length: totalPages }, (_, i) => i + 1);
  }

  const pages: (number | string)[] = [];
  const halfVisible = Math.floor(maxVisible / 2);

  // Always show first page
  pages.push(1);

  let startPage = Math.max(2, currentPage - halfVisible);
  let endPage = Math.min(totalPages - 1, currentPage + halfVisible);

  // Adjust if we're near the beginning
  if (currentPage <= halfVisible + 1) {
    endPage = Math.min(totalPages - 1, maxVisible - 1);
  }

  // Adjust if we're near the end
  if (currentPage >= totalPages - halfVisible) {
    startPage = Math.max(2, totalPages - maxVisible + 2);
  }

  // Add ellipsis if needed
  if (startPage > 2) {
    pages.push('...');
  }

  // Add middle pages
  for (let i = startPage; i <= endPage; i++) {
    pages.push(i);
  }

  // Add ellipsis if needed
  if (endPage < totalPages - 1) {
    pages.push('...');
  }

  // Always show last page (if not already included)
  if (totalPages > 1) {
    pages.push(totalPages);
  }

  return pages;
};

// Pagination component
export const Pagination: React.FC<PaginationProps> = ({
  currentPage,
  totalPages,
  onPageChange,
  showFirstLast = true,
  showPrevNext = true,
  showPageNumbers = true,
  maxVisiblePages = 7,
  disabled = false,
  showInfo = false,
  totalItems,
  itemsPerPage,
  size,
  variant,
  className,
  ...props
}) => {
  const pageNumbers = useMemo(
    () => generatePageNumbers(currentPage, totalPages, maxVisiblePages),
    [currentPage, totalPages, maxVisiblePages]
  );

  const handlePageChange = (page: number) => {
    if (disabled || page < 1 || page > totalPages || page === currentPage) {
      return;
    }
    onPageChange(page);
  };

  // Calculate info text
  const getInfoText = () => {
    if (!totalItems || !itemsPerPage) return null;
    
    const startItem = (currentPage - 1) * itemsPerPage + 1;
    const endItem = Math.min(currentPage * itemsPerPage, totalItems);
    
    return `Showing ${startItem} to ${endItem} of ${totalItems} results`;
  };

  if (totalPages <= 1) {
    return showInfo ? (
      <div className="text-sm text-gray-700">{getInfoText()}</div>
    ) : null;
  }

  return (
    <div className="flex flex-col items-center space-y-3">
      {/* Info text */}
      {showInfo && (
        <div className="text-sm text-gray-700">{getInfoText()}</div>
      )}

      {/* Pagination controls */}
      <nav
        className={cn(paginationVariants({ size, variant, className }))}
        aria-label="Pagination"
        {...props}
      >
        {/* First page button */}
        {showFirstLast && (
          <button
            type="button"
            onClick={() => handlePageChange(1)}
            disabled={disabled || currentPage === 1}
            className={cn(
              paginationButtonVariants({ size }),
              'rounded-l-md'
            )}
            aria-label="Go to first page"
          >
            <svg
              className="h-4 w-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M11 19l-7-7 7-7m8 14l-7-7 7-7"
              />
            </svg>
          </button>
        )}

        {/* Previous page button */}
        {showPrevNext && (
          <button
            type="button"
            onClick={() => handlePageChange(currentPage - 1)}
            disabled={disabled || currentPage === 1}
            className={cn(
              paginationButtonVariants({ size }),
              !showFirstLast && 'rounded-l-md'
            )}
            aria-label="Go to previous page"
          >
            <svg
              className="h-4 w-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 19l-7-7 7-7"
              />
            </svg>
          </button>
        )}

        {/* Page numbers */}
        {showPageNumbers &&
          pageNumbers.map((page, index) => {
            if (page === '...') {
              return (
                <span
                  key={`ellipsis-${index}`}
                  className="inline-flex items-center px-2 py-2 text-sm font-medium text-gray-500"
                >
                  ...
                </span>
              );
            }

            const pageNumber = page as number;
            const isCurrent = pageNumber === currentPage;

            return (
              <button
                key={pageNumber}
                type="button"
                onClick={() => handlePageChange(pageNumber)}
                disabled={disabled}
                className={cn(
                  paginationButtonVariants({
                    size,
                    variant: isCurrent ? 'current' : 'default',
                  })
                )}
                aria-label={`Go to page ${pageNumber}`}
                aria-current={isCurrent ? 'page' : undefined}
              >
                {pageNumber}
              </button>
            );
          })}

        {/* Next page button */}
        {showPrevNext && (
          <button
            type="button"
            onClick={() => handlePageChange(currentPage + 1)}
            disabled={disabled || currentPage === totalPages}
            className={cn(
              paginationButtonVariants({ size }),
              !showFirstLast && 'rounded-r-md'
            )}
            aria-label="Go to next page"
          >
            <svg
              className="h-4 w-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 5l7 7-7 7"
              />
            </svg>
          </button>
        )}

        {/* Last page button */}
        {showFirstLast && (
          <button
            type="button"
            onClick={() => handlePageChange(totalPages)}
            disabled={disabled || currentPage === totalPages}
            className={cn(
              paginationButtonVariants({ size }),
              'rounded-r-md'
            )}
            aria-label="Go to last page"
          >
            <svg
              className="h-4 w-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M13 5l7 7-7 7M5 5l7 7-7 7"
              />
            </svg>
          </button>
        )}
      </nav>
    </div>
  );
};

// Simple pagination component for basic use cases
export const SimplePagination: React.FC<{
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  disabled?: boolean;
  className?: string;
}> = ({ currentPage, totalPages, onPageChange, disabled, className }) => {
  return (
    <Pagination
      currentPage={currentPage}
      totalPages={totalPages}
      onPageChange={onPageChange}
      disabled={disabled}
      showFirstLast={false}
      maxVisiblePages={5}
      className={className}
    />
  );
};

export default Pagination;
