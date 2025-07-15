'use client';

// DSR Data Table Component
// Advanced table with filtering, sorting, and bulk actions for staff interfaces

import { cva, type VariantProps } from 'class-variance-authority';
import React, { forwardRef, useState, useMemo } from 'react';

import { Button } from './button';
import { Input } from './input';
import { StatusBadge } from './status-badge';
import { cn } from '@/utils';

// Data table variants
const dataTableVariants = cva(
  'w-full border-collapse border border-gray-200 rounded-lg overflow-hidden',
  {
    variants: {
      variant: {
        default: 'bg-white',
        compact: 'bg-white text-sm',
        detailed: 'bg-white',
        striped: 'bg-white [&_tbody_tr:nth-child(even)]:bg-gray-50',
      },
      size: {
        sm: 'text-xs',
        md: 'text-sm',
        lg: 'text-base',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'md',
    },
  }
);

// Column definition interface
export interface Column<T = any> {
  key: string;
  header: string;
  accessor?: keyof T | ((row: T) => any);
  cell?: (value: any, row: T, index: number) => React.ReactNode;
  sortable?: boolean;
  filterable?: boolean;
  width?: string;
  align?: 'left' | 'center' | 'right';
  sticky?: boolean;
}

// Sort configuration
export interface SortConfig {
  key: string;
  direction: 'asc' | 'desc';
}

// Filter configuration
export interface FilterConfig {
  [key: string]: string | string[];
}

// Data table props
export interface DataTableProps<T = any>
  extends React.HTMLAttributes<HTMLTableElement>,
    VariantProps<typeof dataTableVariants> {
  data: T[];
  columns: Column<T>[];
  loading?: boolean;
  selectable?: boolean;
  selectedIds?: string[];
  onSelectionChange?: (selectedIds: string[]) => void;
  sortable?: boolean;
  sortConfig?: SortConfig;
  onSortChange?: (sortConfig: SortConfig) => void;
  filterable?: boolean;
  filterConfig?: FilterConfig;
  onFilterChange?: (filterConfig: FilterConfig) => void;
  searchable?: boolean;
  searchQuery?: string;
  onSearchChange?: (query: string) => void;
  pagination?: {
    page: number;
    pageSize: number;
    total: number;
    onPageChange: (page: number) => void;
    onPageSizeChange: (pageSize: number) => void;
  };
  bulkActions?: {
    label: string;
    action: (selectedIds: string[]) => void;
    variant?: 'primary' | 'secondary' | 'destructive';
    disabled?: boolean;
  }[];
  emptyState?: React.ReactNode;
  getRowId?: (row: T, index: number) => string;
  onRowClick?: (row: T, index: number) => void;
  rowClassName?: (row: T, index: number) => string;
}

// Loading skeleton component
const TableSkeleton: React.FC<{ columns: number; rows: number }> = ({ columns, rows }) => (
  <tbody>
    {Array.from({ length: rows }).map((_, rowIndex) => (
      <tr key={rowIndex}>
        {Array.from({ length: columns }).map((_, colIndex) => (
          <td key={colIndex} className="px-4 py-3">
            <div className="h-4 bg-gray-200 rounded animate-pulse" />
          </td>
        ))}
      </tr>
    ))}
  </tbody>
);

// Empty state component
const EmptyState: React.FC<{ message?: string }> = ({ message = 'No data available' }) => (
  <tbody>
    <tr>
      <td colSpan={100} className="px-4 py-12 text-center text-gray-500">
        <div className="flex flex-col items-center space-y-2">
          <div className="w-12 h-12 bg-gray-200 rounded-full flex items-center justify-center">
            <span className="text-gray-400 text-xl">📄</span>
          </div>
          <p>{message}</p>
        </div>
      </td>
    </tr>
  </tbody>
);

// Sort icon component
const SortIcon: React.FC<{ direction?: 'asc' | 'desc' }> = ({ direction }) => (
  <span className="ml-1 inline-flex flex-col">
    <span className={cn('text-xs leading-none', direction === 'asc' ? 'text-primary-600' : 'text-gray-400')}>▲</span>
    <span className={cn('text-xs leading-none', direction === 'desc' ? 'text-primary-600' : 'text-gray-400')}>▼</span>
  </span>
);

// Main data table component
const DataTable = forwardRef<HTMLTableElement, DataTableProps>(
  (
    {
      className,
      variant,
      size,
      data,
      columns,
      loading = false,
      selectable = false,
      selectedIds = [],
      onSelectionChange,
      sortable = false,
      sortConfig,
      onSortChange,
      filterable = false,
      filterConfig = {},
      onFilterChange,
      searchable = false,
      searchQuery = '',
      onSearchChange,
      pagination,
      bulkActions = [],
      emptyState,
      getRowId = (row, index) => index.toString(),
      onRowClick,
      rowClassName,
      ...props
    },
    ref
  ) => {
    const [localSortConfig, setLocalSortConfig] = useState<SortConfig | undefined>(sortConfig);
    const [localFilterConfig, setLocalFilterConfig] = useState<FilterConfig>(filterConfig);
    const [localSearchQuery, setLocalSearchQuery] = useState(searchQuery);

    // Handle sorting
    const handleSort = (key: string) => {
      if (!sortable) return;

      const newDirection = 
        localSortConfig?.key === key && localSortConfig.direction === 'asc' 
          ? 'desc' 
          : 'asc';
      
      const newSortConfig = { key, direction: newDirection };
      setLocalSortConfig(newSortConfig);
      onSortChange?.(newSortConfig);
    };

    // Handle selection
    const handleSelectAll = (checked: boolean) => {
      if (!selectable || !onSelectionChange) return;
      
      if (checked) {
        const allIds = data.map((row, index) => getRowId(row, index));
        onSelectionChange(allIds);
      } else {
        onSelectionChange([]);
      }
    };

    const handleSelectRow = (rowId: string, checked: boolean) => {
      if (!selectable || !onSelectionChange) return;
      
      if (checked) {
        onSelectionChange([...selectedIds, rowId]);
      } else {
        onSelectionChange(selectedIds.filter(id => id !== rowId));
      }
    };

    // Process data (sorting, filtering, searching)
    const processedData = useMemo(() => {
      let result = [...data];

      // Apply search
      if (localSearchQuery && searchable) {
        result = result.filter(row =>
          columns.some(column => {
            const value = column.accessor 
              ? typeof column.accessor === 'function'
                ? column.accessor(row)
                : row[column.accessor]
              : row[column.key];
            return String(value).toLowerCase().includes(localSearchQuery.toLowerCase());
          })
        );
      }

      // Apply filters
      if (filterable) {
        Object.entries(localFilterConfig).forEach(([key, filterValue]) => {
          if (filterValue && filterValue !== '') {
            result = result.filter(row => {
              const column = columns.find(col => col.key === key);
              if (!column) return true;
              
              const value = column.accessor 
                ? typeof column.accessor === 'function'
                  ? column.accessor(row)
                  : row[column.accessor]
                : row[column.key];
              
              if (Array.isArray(filterValue)) {
                return filterValue.includes(String(value));
              }
              return String(value).toLowerCase().includes(String(filterValue).toLowerCase());
            });
          }
        });
      }

      // Apply sorting
      if (localSortConfig && sortable) {
        result.sort((a, b) => {
          const column = columns.find(col => col.key === localSortConfig.key);
          if (!column) return 0;
          
          const aValue = column.accessor 
            ? typeof column.accessor === 'function'
              ? column.accessor(a)
              : a[column.accessor]
            : a[column.key];
          
          const bValue = column.accessor 
            ? typeof column.accessor === 'function'
              ? column.accessor(b)
              : b[column.accessor]
            : b[column.key];
          
          if (aValue < bValue) return localSortConfig.direction === 'asc' ? -1 : 1;
          if (aValue > bValue) return localSortConfig.direction === 'asc' ? 1 : -1;
          return 0;
        });
      }

      return result;
    }, [data, columns, localSearchQuery, localFilterConfig, localSortConfig, searchable, filterable, sortable]);

    const isAllSelected = selectedIds.length > 0 && selectedIds.length === processedData.length;
    const isIndeterminate = selectedIds.length > 0 && selectedIds.length < processedData.length;

    return (
      <div className="space-y-4">
        {/* Search and Bulk Actions */}
        {(searchable || bulkActions.length > 0) && (
          <div className="flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center">
            {searchable && (
              <div className="flex-1 max-w-md">
                <Input
                  placeholder="Search..."
                  value={localSearchQuery}
                  onChange={(e) => {
                    setLocalSearchQuery(e.target.value);
                    onSearchChange?.(e.target.value);
                  }}
                  className="w-full"
                />
              </div>
            )}
            
            {bulkActions.length > 0 && selectedIds.length > 0 && (
              <div className="flex gap-2">
                {bulkActions.map((action, index) => (
                  <Button
                    key={index}
                    variant={action.variant || 'secondary'}
                    size="sm"
                    disabled={action.disabled}
                    onClick={() => action.action(selectedIds)}
                  >
                    {action.label} ({selectedIds.length})
                  </Button>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Table */}
        <div className="overflow-x-auto border border-gray-200 rounded-lg">
          <table
            ref={ref}
            className={cn(dataTableVariants({ variant, size }), className)}
            {...props}
          >
            {/* Header */}
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                {selectable && (
                  <th className="px-4 py-3 text-left">
                    <input
                      type="checkbox"
                      checked={isAllSelected}
                      ref={(el) => {
                        if (el) el.indeterminate = isIndeterminate;
                      }}
                      onChange={(e) => handleSelectAll(e.target.checked)}
                      className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    />
                  </th>
                )}
                {columns.map((column) => (
                  <th
                    key={column.key}
                    className={cn(
                      'px-4 py-3 font-medium text-gray-900',
                      column.align === 'center' && 'text-center',
                      column.align === 'right' && 'text-right',
                      column.sortable && sortable && 'cursor-pointer hover:bg-gray-100 select-none',
                      column.sticky && 'sticky left-0 bg-gray-50 z-10'
                    )}
                    style={{ width: column.width }}
                    onClick={() => column.sortable && handleSort(column.key)}
                  >
                    <div className="flex items-center">
                      {column.header}
                      {column.sortable && sortable && (
                        <SortIcon 
                          direction={localSortConfig?.key === column.key ? localSortConfig.direction : undefined}
                        />
                      )}
                    </div>
                  </th>
                ))}
              </tr>
            </thead>

            {/* Body */}
            {loading ? (
              <TableSkeleton columns={columns.length + (selectable ? 1 : 0)} rows={5} />
            ) : processedData.length === 0 ? (
              emptyState || <EmptyState />
            ) : (
              <tbody>
                {processedData.map((row, index) => {
                  const rowId = getRowId(row, index);
                  const isSelected = selectedIds.includes(rowId);
                  
                  return (
                    <tr
                      key={rowId}
                      className={cn(
                        'border-b border-gray-200 hover:bg-gray-50 transition-colors',
                        onRowClick && 'cursor-pointer',
                        isSelected && 'bg-primary-50',
                        rowClassName?.(row, index)
                      )}
                      onClick={() => onRowClick?.(row, index)}
                    >
                      {selectable && (
                        <td className="px-4 py-3">
                          <input
                            type="checkbox"
                            checked={isSelected}
                            onChange={(e) => handleSelectRow(rowId, e.target.checked)}
                            onClick={(e) => e.stopPropagation()}
                            className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                          />
                        </td>
                      )}
                      {columns.map((column) => {
                        const value = column.accessor 
                          ? typeof column.accessor === 'function'
                            ? column.accessor(row)
                            : row[column.accessor]
                          : row[column.key];
                        
                        return (
                          <td
                            key={column.key}
                            className={cn(
                              'px-4 py-3',
                              column.align === 'center' && 'text-center',
                              column.align === 'right' && 'text-right',
                              column.sticky && 'sticky left-0 bg-white z-10'
                            )}
                          >
                            {column.cell ? column.cell(value, row, index) : String(value || '')}
                          </td>
                        );
                      })}
                    </tr>
                  );
                })}
              </tbody>
            )}
          </table>
        </div>

        {/* Pagination */}
        {pagination && (
          <div className="flex items-center justify-between">
            <div className="text-sm text-gray-700">
              Showing {((pagination.page - 1) * pagination.pageSize) + 1} to{' '}
              {Math.min(pagination.page * pagination.pageSize, pagination.total)} of{' '}
              {pagination.total} results
            </div>
            <div className="flex items-center space-x-2">
              <Button
                variant="outline"
                size="sm"
                disabled={pagination.page <= 1}
                onClick={() => pagination.onPageChange(pagination.page - 1)}
              >
                Previous
              </Button>
              <span className="text-sm text-gray-700">
                Page {pagination.page} of {Math.ceil(pagination.total / pagination.pageSize)}
              </span>
              <Button
                variant="outline"
                size="sm"
                disabled={pagination.page >= Math.ceil(pagination.total / pagination.pageSize)}
                onClick={() => pagination.onPageChange(pagination.page + 1)}
              >
                Next
              </Button>
            </div>
          </div>
        )}
      </div>
    );
  }
);

DataTable.displayName = 'DataTable';

export { DataTable, dataTableVariants };
export type { DataTableProps, Column, SortConfig, FilterConfig };
