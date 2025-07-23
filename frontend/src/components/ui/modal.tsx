'use client';

// Modal Component
// Accessible modal dialog with backdrop, animations, and keyboard navigation

import { cva, type VariantProps } from 'class-variance-authority';
import React, { useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';

import { cn } from '@/utils';

// Modal variants
const modalVariants = cva(
  // Base styles - enhanced with modern design patterns
  'relative bg-white rounded-xl shadow-2xl transform transition-all duration-300 ease-out border border-gray-200',
  {
    variants: {
      size: {
        xs: 'max-w-xs w-full mx-4',
        sm: 'max-w-sm w-full mx-4',
        md: 'max-w-md w-full mx-4',
        lg: 'max-w-lg w-full mx-4',
        xl: 'max-w-xl w-full mx-4',
        '2xl': 'max-w-2xl w-full mx-4',
        '3xl': 'max-w-3xl w-full mx-4',
        '4xl': 'max-w-4xl w-full mx-4',
        '5xl': 'max-w-5xl w-full mx-4',
        '6xl': 'max-w-6xl w-full mx-4',
        '7xl': 'max-w-7xl w-full mx-4',
        full: 'max-w-full w-full mx-4',
      },
      centered: {
        true: 'mx-auto my-auto',
        false: 'mx-auto mt-20',
      },
    },
    defaultVariants: {
      size: 'md',
      centered: true,
    },
  }
);

// Modal props interface
export interface ModalProps extends VariantProps<typeof modalVariants> {
  isOpen: boolean;
  onClose: () => void;
  children: React.ReactNode;
  title?: string;
  description?: string;
  closeOnOverlayClick?: boolean;
  closeOnEscape?: boolean;
  showCloseButton?: boolean;
  className?: string;
  overlayClassName?: string;
  contentClassName?: string;
  preventScroll?: boolean;
  initialFocus?: React.RefObject<HTMLElement>;
  finalFocus?: React.RefObject<HTMLElement>;
}

// Close icon component
const CloseIcon: React.FC = () => (
  <svg
    className='h-6 w-6'
    fill='none'
    stroke='currentColor'
    viewBox='0 0 24 24'
    xmlns='http://www.w3.org/2000/svg'
  >
    <path
      strokeLinecap='round'
      strokeLinejoin='round'
      strokeWidth={2}
      d='M6 18L18 6M6 6l12 12'
    />
  </svg>
);

// Modal component
const Modal: React.FC<ModalProps> = ({
  isOpen,
  onClose,
  children,
  title,
  description,
  size,
  centered,
  closeOnOverlayClick = true,
  closeOnEscape = true,
  showCloseButton = true,
  className,
  overlayClassName,
  contentClassName,
  preventScroll = true,
  initialFocus,
  finalFocus,
}) => {
  const modalRef = useRef<HTMLDivElement>(null);
  const previousActiveElement = useRef<HTMLElement | null>(null);

  // Handle escape key
  useEffect(() => {
    if (!isOpen || !closeOnEscape) return undefined;

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onClose();
      }
    };

    document.addEventListener('keydown', handleEscape);
    return () => document.removeEventListener('keydown', handleEscape);
  }, [isOpen, closeOnEscape, onClose]);

  // Handle focus management
  useEffect(() => {
    if (!isOpen) return undefined;

    // Store the currently focused element
    previousActiveElement.current = document.activeElement as HTMLElement;

    // Focus the modal or initial focus element
    const focusElement = initialFocus?.current || modalRef.current;
    if (focusElement) {
      focusElement.focus();
    }

    // Trap focus within modal
    const handleTabKey = (event: KeyboardEvent) => {
      if (event.key !== 'Tab' || !modalRef.current) return;

      const focusableElements = modalRef.current.querySelectorAll(
        'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
      );
      const firstElement = focusableElements[0] as HTMLElement;
      const lastElement = focusableElements[
        focusableElements.length - 1
      ] as HTMLElement;

      if (event.shiftKey) {
        if (document.activeElement === firstElement) {
          lastElement.focus();
          event.preventDefault();
        }
      } else {
        if (document.activeElement === lastElement) {
          firstElement.focus();
          event.preventDefault();
        }
      }
    };

    document.addEventListener('keydown', handleTabKey);

    return () => {
      document.removeEventListener('keydown', handleTabKey);

      // Restore focus to the previously focused element
      if (finalFocus?.current) {
        finalFocus.current.focus();
      } else if (previousActiveElement.current) {
        previousActiveElement.current.focus();
      }
    };
  }, [isOpen, initialFocus, finalFocus]);

  // Handle body scroll
  useEffect(() => {
    if (!preventScroll) return undefined;

    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }

    return () => {
      document.body.style.overflow = '';
    };
  }, [isOpen, preventScroll]);

  // Handle overlay click
  const handleOverlayClick = (event: React.MouseEvent<HTMLDivElement>) => {
    if (closeOnOverlayClick && event.target === event.currentTarget) {
      onClose();
    }
  };

  if (!isOpen) return null;

  const modalContent = (
    <div
      className={cn(
        'fixed inset-0 z-50 flex items-center justify-center p-4',
        overlayClassName
      )}
      onClick={handleOverlayClick}
    >
      {/* Backdrop */}
      <div
        className='absolute inset-0 bg-black bg-opacity-50 transition-opacity'
        aria-hidden='true'
      />

      {/* Modal */}
      <div
        ref={modalRef}
        className={cn(
          modalVariants({ size, centered }),
          'relative z-10 w-full',
          className
        )}
        role='dialog'
        aria-modal='true'
        aria-labelledby={title ? 'modal-title' : undefined}
        aria-describedby={description ? 'modal-description' : undefined}
      >
        <div className={cn('p-6', contentClassName)}>
          {/* Header */}
          {(title || showCloseButton) && (
            <div className='flex items-center justify-between mb-4'>
              {title && (
                <h2
                  id='modal-title'
                  className='text-lg font-semibold text-gray-900'
                >
                  {title}
                </h2>
              )}
              {showCloseButton && (
                <button
                  type='button'
                  className='text-gray-400 hover:text-gray-600 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 rounded-md p-1'
                  onClick={onClose}
                  aria-label='Close modal'
                >
                  <CloseIcon />
                </button>
              )}
            </div>
          )}

          {/* Description */}
          {description && (
            <p id='modal-description' className='text-sm text-gray-500 mb-4'>
              {description}
            </p>
          )}

          {/* Content */}
          {children}
        </div>
      </div>
    </div>
  );

  // Render modal in portal
  return typeof window !== 'undefined'
    ? createPortal(modalContent, document.body)
    : null;
};

// Modal Header component
export const ModalHeader: React.FC<{
  children: React.ReactNode;
  className?: string;
}> = ({ children, className }) => (
  <div className={cn('mb-4', className)}>{children}</div>
);

// Modal Body component
export const ModalBody: React.FC<{
  children: React.ReactNode;
  className?: string;
}> = ({ children, className }) => (
  <div className={cn('mb-4', className)}>{children}</div>
);

// Modal Footer component
export const ModalFooter: React.FC<{
  children: React.ReactNode;
  className?: string;
}> = ({ children, className }) => (
  <div
    className={cn(
      'flex justify-end space-x-2 pt-4 border-t border-gray-200',
      className
    )}
  >
    {children}
  </div>
);

// Compound Modal component
const CompoundModal = Object.assign(Modal, {
  Header: ModalHeader,
  Body: ModalBody,
  Footer: ModalFooter,
});

export { Modal, modalVariants, CompoundModal };
export default CompoundModal;
