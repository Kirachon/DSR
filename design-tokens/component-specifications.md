# DSR Component Specifications for Figma Integration

## Overview
This document defines the component specifications for the DSR (Department of Social Welfare and Development) frontend redesign, optimized for Figma design token integration and Tailwind CSS implementation.

## Design Token Integration

### Color System
- **Philippine Government Primary**: Blue scale (50-950) for official government branding
- **Philippine Government Secondary**: Red scale (50-950) for alerts and important actions  
- **Philippine Government Accent**: Yellow scale (50-950) for highlights and CTAs
- **Semantic Colors**: Success (green), Warning (orange), Error (red), Info (blue)
- **Neutral Scale**: Gray scale (50-950) for text, backgrounds, and borders

### Typography Scale
- **Font Family**: Inter (primary), JetBrains Mono (monospace)
- **Font Sizes**: xs (12px) to 6xl (60px) with consistent scaling
- **Font Weights**: Light (300) to Extrabold (800)
- **Line Heights**: Tight (1.25) to Loose (2.0)

### Spacing System
- **Base Unit**: 0.25rem (4px)
- **Scale**: 0, 1, 2, 3, 4, 5, 6, 8, 10, 12, 16, 20, 24, 32, 40, 48, 56, 64
- **Usage**: Consistent spacing for margins, padding, and gaps

## Component Categories

### 1. Foundation Components

#### Button Component
```typescript
interface ButtonProps {
  variant: 'primary' | 'secondary' | 'outline' | 'ghost' | 'destructive';
  size: 'sm' | 'md' | 'lg' | 'xl';
  disabled?: boolean;
  loading?: boolean;
  icon?: ReactNode;
  children: ReactNode;
}
```

**Design Tokens:**
- Primary: `--dsr-philippine-government-primary-500`
- Secondary: `--dsr-neutral-100`
- Padding: `--dsr-spacing-3` (sm), `--dsr-spacing-4` (md), `--dsr-spacing-5` (lg)
- Border Radius: `--dsr-border-radius-md`
- Font Weight: `--dsr-font-weight-medium`

#### Card Component
```typescript
interface CardProps {
  variant: 'default' | 'elevated' | 'outlined';
  padding: 'sm' | 'md' | 'lg';
  children: ReactNode;
  className?: string;
}
```

**Design Tokens:**
- Background: `--dsr-neutral-50`
- Border: `--dsr-neutral-200`
- Shadow: `--dsr-shadow-md`
- Border Radius: `--dsr-border-radius-lg`

### 2. Form Components

#### Input Component
```typescript
interface InputProps {
  type: 'text' | 'email' | 'password' | 'number' | 'tel';
  label: string;
  placeholder?: string;
  error?: string;
  required?: boolean;
  disabled?: boolean;
  value: string;
  onChange: (value: string) => void;
}
```

**Design Tokens:**
- Border: `--dsr-neutral-300`
- Focus Border: `--dsr-philippine-government-primary-500`
- Error Border: `--dsr-semantic-error-500`
- Background: `--dsr-neutral-50`
- Text: `--dsr-neutral-900`

#### Select Component
```typescript
interface SelectProps {
  label: string;
  options: Array<{ value: string; label: string }>;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  error?: string;
  required?: boolean;
  disabled?: boolean;
}
```

### 3. Navigation Components

#### Role-Based Navigation
```typescript
interface NavigationProps {
  userRole: 'CITIZEN' | 'LGU_STAFF' | 'DSWD_STAFF' | 'ADMIN';
  currentPath: string;
  onNavigate: (path: string) => void;
}
```

**Design Tokens:**
- Background: `--dsr-philippine-government-primary-800`
- Active Item: `--dsr-philippine-government-primary-600`
- Text: `--dsr-neutral-50`
- Hover: `--dsr-philippine-government-primary-700`

#### Breadcrumb Component
```typescript
interface BreadcrumbProps {
  items: Array<{
    label: string;
    href?: string;
    current?: boolean;
  }>;
}
```

### 4. Data Display Components

#### Data Table Component
```typescript
interface DataTableProps<T> {
  data: T[];
  columns: Array<{
    key: keyof T;
    label: string;
    sortable?: boolean;
    render?: (value: any, record: T) => ReactNode;
  }>;
  onSort?: (field: keyof T, direction: 'asc' | 'desc') => void;
  pagination?: {
    current: number;
    total: number;
    pageSize: number;
    onChange: (page: number) => void;
  };
}
```

**Design Tokens:**
- Header Background: `--dsr-neutral-100`
- Border: `--dsr-neutral-200`
- Hover Row: `--dsr-neutral-50`
- Text: `--dsr-neutral-900`

#### Status Badge Component
```typescript
interface StatusBadgeProps {
  status: 'draft' | 'submitted' | 'review' | 'approved' | 'rejected';
  size?: 'sm' | 'md' | 'lg';
}
```

**Design Tokens:**
- Draft: `--dsr-neutral-500`
- Submitted: `--dsr-semantic-info-500`
- Review: `--dsr-semantic-warning-500`
- Approved: `--dsr-semantic-success-500`
- Rejected: `--dsr-semantic-error-500`

### 5. Workflow Components

#### Progress Indicator Component
```typescript
interface ProgressIndicatorProps {
  steps: Array<{
    label: string;
    status: 'completed' | 'current' | 'upcoming';
    description?: string;
  }>;
  orientation?: 'horizontal' | 'vertical';
}
```

#### Workflow Timeline Component
```typescript
interface WorkflowTimelineProps {
  events: Array<{
    id: string;
    title: string;
    description: string;
    timestamp: Date;
    status: 'completed' | 'current' | 'upcoming';
    user?: string;
  }>;
}
```

### 6. Dashboard Components

#### Metric Card Component
```typescript
interface MetricCardProps {
  title: string;
  value: string | number;
  change?: {
    value: number;
    type: 'increase' | 'decrease';
    period: string;
  };
  icon?: ReactNode;
  trend?: Array<number>;
}
```

#### Quick Actions Card Component
```typescript
interface QuickActionsProps {
  actions: Array<{
    label: string;
    icon: ReactNode;
    onClick: () => void;
    disabled?: boolean;
    badge?: number;
  }>;
  userRole: 'CITIZEN' | 'LGU_STAFF' | 'DSWD_STAFF' | 'ADMIN';
}
```

## Responsive Design Specifications

### Breakpoint System
- **xs**: 475px (Small mobile)
- **sm**: 640px (Mobile)
- **md**: 768px (Tablet)
- **lg**: 1024px (Desktop)
- **xl**: 1280px (Large desktop)
- **2xl**: 1536px (Extra large)
- **3xl**: 1600px (Ultra wide)

### Mobile-First Approach
All components must be designed mobile-first with progressive enhancement:

1. **Base (Mobile)**: Single column, stacked layout
2. **Tablet (md+)**: Two-column layouts, expanded navigation
3. **Desktop (lg+)**: Multi-column layouts, sidebar navigation
4. **Large (xl+)**: Enhanced spacing, larger content areas

## Accessibility Specifications

### WCAG 2.0 AA Compliance
- **Color Contrast**: Minimum 4.5:1 for normal text, 3:1 for large text
- **Keyboard Navigation**: Full keyboard accessibility with visible focus indicators
- **Screen Reader Support**: Proper ARIA labels, roles, and descriptions
- **Touch Targets**: Minimum 44px for interactive elements

### Focus Management
- **Focus Indicators**: 2px solid outline with `--dsr-philippine-government-primary-500`
- **Tab Order**: Logical tab sequence following visual layout
- **Skip Links**: "Skip to main content" for keyboard users

## Animation Specifications

### Micro-Interactions
- **Duration**: 200-300ms for UI feedback, 400-600ms for transitions
- **Easing**: `ease-out` for entrances, `ease-in` for exits
- **Reduced Motion**: Respect `prefers-reduced-motion` setting

### Custom Animations
- **fade-in**: 300ms ease-in-out
- **slide-up**: 300ms ease-out
- **scale-in**: 200ms ease-out
- **bounce-gentle**: 600ms ease-out
- **pulse-slow**: 2s infinite for loading states

## Implementation Guidelines

### Figma to Code Workflow
1. **Design Tokens**: Use Tokens Studio plugin to maintain design tokens
2. **Component Library**: Create master components in Figma with variants
3. **Auto-Layout**: Use Figma auto-layout for responsive behavior
4. **Export**: Generate design tokens and component specifications
5. **Development**: Transform tokens to Tailwind CSS configuration

### Code Generation
- **Token Transformer**: Use `@tokens-studio/sd-transforms` for token processing
- **Style Dictionary**: Generate CSS custom properties and Tailwind config
- **Component Templates**: Create TypeScript component templates with proper typing

This specification ensures consistent, accessible, and maintainable components across the DSR system while supporting the three user roles (citizens, DSWD staff, LGU staff) with appropriate design variations.
