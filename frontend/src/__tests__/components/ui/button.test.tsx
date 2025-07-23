/**
 * Button Component Tests
 * Comprehensive testing for design system Button component
 */

import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { Button } from '@/components/ui/button';
import { ThemeProvider } from '@/contexts/theme-context';
import { AuthProvider } from '@/contexts/auth-context';

// Test wrapper with providers
const TestWrapper: React.FC<{ children: React.ReactNode; theme?: string }> = ({ 
  children, 
  theme = 'citizen' 
}) => (
  <AuthProvider>
    <ThemeProvider>
      <div data-theme={theme}>
        {children}
      </div>
    </ThemeProvider>
  </AuthProvider>
);

describe('Button Component', () => {
  describe('Basic Functionality', () => {
    it('should render with default props', () => {
      render(
        <TestWrapper>
          <Button>Test Button</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      expect(button).toBeInTheDocument();
      expect(button).toHaveTextContent('Test Button');
    });

    it('should handle click events', () => {
      const handleClick = jest.fn();
      
      render(
        <TestWrapper>
          <Button onClick={handleClick}>Click Me</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      fireEvent.click(button);
      
      expect(handleClick).toHaveBeenCalledTimes(1);
    });

    it('should be disabled when disabled prop is true', () => {
      render(
        <TestWrapper>
          <Button disabled>Disabled Button</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      expect(button).toBeDisabled();
    });

    it('should not call onClick when disabled', () => {
      const handleClick = jest.fn();
      
      render(
        <TestWrapper>
          <Button disabled onClick={handleClick}>Disabled Button</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      fireEvent.click(button);
      
      expect(handleClick).not.toHaveBeenCalled();
    });
  });

  describe('Variants', () => {
    it('should apply primary variant styles', () => {
      render(
        <TestWrapper>
          <Button variant="primary">Primary Button</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      expect(button).toHaveClass('bg-philippine-government-primary-500');
    });

    it('should apply secondary variant styles', () => {
      render(
        <TestWrapper>
          <Button variant="secondary">Secondary Button</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      expect(button).toHaveClass('bg-neutral-100');
    });

    it('should apply outline variant styles', () => {
      render(
        <TestWrapper>
          <Button variant="outline">Outline Button</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      expect(button).toHaveClass('border');
    });

    it('should apply ghost variant styles', () => {
      render(
        <TestWrapper>
          <Button variant="ghost">Ghost Button</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      expect(button).toHaveClass('hover:bg-accent');
    });
  });

  describe('Sizes', () => {
    it('should apply small size styles', () => {
      render(
        <TestWrapper>
          <Button size="sm">Small Button</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      expect(button).toHaveClass('h-9');
    });

    it('should apply large size styles', () => {
      render(
        <TestWrapper>
          <Button size="lg">Large Button</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      expect(button).toHaveClass('h-11');
    });

    it('should apply icon size styles', () => {
      render(
        <TestWrapper>
          <Button size="icon">Icon</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      expect(button).toHaveClass('h-10', 'w-10');
    });
  });

  describe('Theme Integration', () => {
    it('should apply citizen theme styles', () => {
      render(
        <TestWrapper theme="citizen">
          <Button variant="primary">Citizen Button</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      expect(button).toHaveClass('bg-philippine-government-primary-500');
    });

    it('should apply DSWD staff theme styles', () => {
      render(
        <TestWrapper theme="dswd-staff">
          <Button variant="primary">DSWD Button</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      expect(button).toHaveClass('bg-philippine-government-primary-500');
    });

    it('should apply LGU staff theme styles', () => {
      render(
        <TestWrapper theme="lgu-staff">
          <Button variant="primary">LGU Button</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      // LGU staff uses secondary color as primary
      expect(button).toHaveClass('bg-philippine-government-secondary-500');
    });

    it('should apply theme-specific secondary styles', () => {
      render(
        <TestWrapper theme="dswd-staff">
          <Button variant="secondary">DSWD Secondary</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      expect(button).toHaveClass('bg-philippine-government-accent-100');
    });
  });

  describe('Loading State', () => {
    it('should show loading spinner when loading', () => {
      render(
        <TestWrapper>
          <Button loading>Loading Button</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      const spinner = button.querySelector('svg');
      
      expect(spinner).toBeInTheDocument();
      expect(spinner).toHaveClass('animate-spin');
    });

    it('should be disabled when loading', () => {
      render(
        <TestWrapper>
          <Button loading>Loading Button</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      expect(button).toBeDisabled();
    });

    it('should not call onClick when loading', () => {
      const handleClick = jest.fn();
      
      render(
        <TestWrapper>
          <Button loading onClick={handleClick}>Loading Button</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      fireEvent.click(button);
      
      expect(handleClick).not.toHaveBeenCalled();
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA attributes', () => {
      render(
        <TestWrapper>
          <Button aria-label="Custom label">Button</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      expect(button).toHaveAttribute('aria-label', 'Custom label');
    });

    it('should be focusable', () => {
      render(
        <TestWrapper>
          <Button>Focusable Button</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      button.focus();
      
      expect(button).toHaveFocus();
    });

    it('should support keyboard navigation', () => {
      const handleClick = jest.fn();
      
      render(
        <TestWrapper>
          <Button onClick={handleClick}>Keyboard Button</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      button.focus();
      
      fireEvent.keyDown(button, { key: 'Enter' });
      expect(handleClick).toHaveBeenCalledTimes(1);
      
      fireEvent.keyDown(button, { key: ' ' });
      expect(handleClick).toHaveBeenCalledTimes(2);
    });
  });

  describe('Custom Props', () => {
    it('should accept custom className', () => {
      render(
        <TestWrapper>
          <Button className="custom-class">Custom Button</Button>
        </TestWrapper>
      );
      
      const button = screen.getByRole('button');
      expect(button).toHaveClass('custom-class');
    });

    it('should forward ref', () => {
      const ref = React.createRef<HTMLButtonElement>();
      
      render(
        <TestWrapper>
          <Button ref={ref}>Ref Button</Button>
        </TestWrapper>
      );
      
      expect(ref.current).toBeInstanceOf(HTMLButtonElement);
    });

    it('should accept data attributes', () => {
      render(
        <TestWrapper>
          <Button data-testid="test-button">Test Button</Button>
        </TestWrapper>
      );
      
      const button = screen.getByTestId('test-button');
      expect(button).toBeInTheDocument();
    });
  });
});
