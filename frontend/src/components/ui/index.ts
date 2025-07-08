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
