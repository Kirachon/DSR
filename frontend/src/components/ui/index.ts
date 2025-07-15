// Central export for all UI components
// DSR Frontend UI Component Library

// Button components
export { Button, buttonVariants } from './button';
export type { ButtonProps } from './button';

// Input components
export { Input, inputVariants } from './input';
export type { InputProps } from './input';

// Card components
export {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  CardFooter,
  CompoundCard,
  cardVariants,
} from './card';
export type {
  CardProps,
  CardHeaderProps,
  CardTitleProps,
  CardDescriptionProps,
  CardContentProps,
  CardFooterProps,
} from './card';

// Modal components
export {
  Modal,
  ModalHeader,
  ModalBody,
  ModalFooter,
  CompoundModal,
  modalVariants,
} from './modal';
export type { ModalProps } from './modal';

// Loading components
export {
  Loading,
  Spinner,
  Skeleton,
  CardSkeleton,
  TableSkeleton,
  PageLoading,
  ButtonLoading,
  loadingVariants,
} from './loading';
export type { LoadingProps } from './loading';

// Alert components
export {
  Alert,
  AlertTitle,
  AlertDescription,
  Toast,
  Banner,
  InlineAlert,
  alertVariants,
} from './alert';
export type { AlertProps } from './alert';

// Badge components
export {
  Badge,
  StatusBadge,
  PriorityBadge,
  CountBadge,
  badgeVariants,
} from './badge';
export type { BadgeProps } from './badge';

// Pagination components
export {
  Pagination,
  SimplePagination,
  paginationVariants,
  paginationButtonVariants,
} from './pagination';
export type { PaginationProps } from './pagination';

// Enhanced DSR Components
// Status Badge components (enhanced version)
export { StatusBadge as DSRStatusBadge, statusBadgeVariants, getStatusText, getStatusColor } from './status-badge';
export type { StatusBadgeProps as DSRStatusBadgeProps } from './status-badge';

// Progress Indicator components
export { ProgressIndicator, progressIndicatorVariants } from './progress-indicator';
export type { ProgressIndicatorProps, Step, StepStatus } from './progress-indicator';

// Data Table components
export { DataTable, dataTableVariants } from './data-table';
export type { DataTableProps, Column, SortConfig, FilterConfig } from './data-table';

// Role-Based Navigation components
export { RoleBasedNavigation } from './role-based-navigation';
export { DSR_NAVIGATION_CONFIG } from './role-based-navigation';
export type { RoleBasedNavigationProps, NavigationItem, NavigationSection } from './role-based-navigation';

// Workflow Timeline components
export { WorkflowTimeline, timelineVariants } from './workflow-timeline';
export type { WorkflowTimelineProps, TimelineEvent } from './workflow-timeline';
