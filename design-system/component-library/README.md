# DSR Component Library

## Overview
The DSR Component Library provides a comprehensive set of reusable components designed specifically for the Department of Social Welfare and Development's digital services. Each component is built with accessibility, performance, and role-based customization in mind.

## Component Categories

### 1. Foundation Components
- **Button**: Primary, secondary, outline, ghost, and destructive variants
- **Card**: Default, elevated, outlined, and interactive variants
- **Badge**: Status indicators with semantic colors
- **Avatar**: User profile images with fallbacks
- **Divider**: Visual separation elements

### 2. Form Components
- **Input**: Text, email, password, number, and tel inputs
- **Textarea**: Multi-line text input with auto-resize
- **Select**: Dropdown selection with search capability
- **Checkbox**: Single and group checkbox controls
- **Radio**: Radio button groups with proper labeling
- **Switch**: Toggle switches for boolean values
- **DatePicker**: Accessible date selection component
- **FileUpload**: Drag-and-drop file upload with preview

### 3. Navigation Components
- **Header**: Role-based header with navigation and user menu
- **Sidebar**: Collapsible sidebar navigation
- **Breadcrumb**: Hierarchical navigation trail
- **Tabs**: Horizontal and vertical tab navigation
- **Pagination**: Page navigation for large datasets
- **Steps**: Multi-step process indicator

### 4. Data Display Components
- **Table**: Sortable, filterable data tables
- **List**: Ordered and unordered lists with custom styling
- **Timeline**: Chronological event display
- **Progress**: Linear and circular progress indicators
- **Metric**: Dashboard metric display with trends
- **Chart**: Basic chart components for data visualization

### 5. Feedback Components
- **Alert**: Success, warning, error, and info alerts
- **Toast**: Temporary notification messages
- **Modal**: Overlay dialogs for focused interactions
- **Tooltip**: Contextual help and information
- **Loading**: Loading states and skeleton screens
- **Empty**: Empty state illustrations and messaging

### 6. Layout Components
- **Container**: Responsive content containers
- **Grid**: Flexible grid system
- **Stack**: Vertical and horizontal stacking
- **Flex**: Flexbox utility components
- **Spacer**: Consistent spacing elements

## Role-Based Theming

### Citizen Theme
```css
[data-theme="citizen"] {
  --primary-color: #1e3a8a;
  --background-color: #f8fafc;
  --card-radius: 0.5rem;
  --button-style: rounded;
}
```

### DSWD Staff Theme
```css
[data-theme="dswd-staff"] {
  --primary-color: #1e3a8a;
  --accent-color: #fbbf24;
  --background-color: #f1f5f9;
  --card-radius: 0.375rem;
  --button-style: professional;
}
```

### LGU Staff Theme
```css
[data-theme="lgu-staff"] {
  --primary-color: #dc2626;
  --accent-color: #fbbf24;
  --background-color: #f8fafc;
  --card-radius: 0.25rem;
  --button-style: sharp;
}
```

## Usage Guidelines

### Component Import
```typescript
import { Button, Card, Input } from '@/components/ui';
import { DataTable, StatusBadge } from '@/components/data-display';
import { Header, Sidebar } from '@/components/navigation';
```

### Role-Based Usage
```typescript
// Automatic role detection from context
const MyComponent = () => {
  const { userRole } = useAuth();
  
  return (
    <div data-theme={userRole.toLowerCase().replace('_', '-')}>
      <Button variant="primary">
        {userRole === 'CITIZEN' ? 'Apply Now' : 'Process Application'}
      </Button>
    </div>
  );
};
```

### Accessibility Features
- All components include proper ARIA labels and roles
- Keyboard navigation support with visible focus indicators
- Screen reader compatibility with semantic markup
- High contrast mode support
- Reduced motion respect for animations

## Development Guidelines

### Component Structure
```typescript
interface ComponentProps {
  // Required props
  children: ReactNode;
  
  // Optional props with defaults
  variant?: 'default' | 'primary' | 'secondary';
  size?: 'sm' | 'md' | 'lg';
  disabled?: boolean;
  
  // Role-based customization
  userRole?: 'CITIZEN' | 'DSWD_STAFF' | 'LGU_STAFF';
  
  // Accessibility props
  'aria-label'?: string;
  'aria-describedby'?: string;
  
  // Event handlers
  onClick?: () => void;
  onChange?: (value: any) => void;
}
```

### Testing Requirements
- Unit tests with React Testing Library
- Accessibility tests with jest-axe
- Visual regression tests with Chromatic
- Cross-browser compatibility testing
- Mobile responsiveness testing

### Documentation Standards
- Storybook stories for all components
- TypeScript interfaces for all props
- Usage examples and best practices
- Accessibility guidelines and testing
- Performance considerations

## Performance Optimization

### Bundle Splitting
Components are organized for optimal tree-shaking:
```typescript
// Good: Import only what you need
import { Button } from '@/components/ui/Button';

// Avoid: Importing entire library
import * as UI from '@/components/ui';
```

### Lazy Loading
Large components support lazy loading:
```typescript
const DataTable = lazy(() => import('@/components/data-display/DataTable'));
const Chart = lazy(() => import('@/components/data-display/Chart'));
```

### Memoization
Components use React.memo for performance:
```typescript
export const Button = React.memo<ButtonProps>(({ children, ...props }) => {
  // Component implementation
});
```

## Contributing

### Adding New Components
1. Create component in appropriate category folder
2. Include TypeScript interfaces and props
3. Add Storybook stories with all variants
4. Write comprehensive tests
5. Update documentation and examples
6. Ensure accessibility compliance

### Component Checklist
- [ ] TypeScript interfaces defined
- [ ] All variants implemented
- [ ] Role-based theming support
- [ ] Accessibility features included
- [ ] Keyboard navigation working
- [ ] Screen reader compatible
- [ ] Mobile responsive
- [ ] Tests written and passing
- [ ] Storybook stories created
- [ ] Documentation updated

This component library ensures consistent, accessible, and maintainable UI components across all DSR applications while supporting the unique needs of each user role.
