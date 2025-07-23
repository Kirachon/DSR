# DSR Design System Guide
## Department of Social Welfare and Development - Frontend Redesign

### Table of Contents
1. [Overview](#overview)
2. [Design Principles](#design-principles)
3. [User Role Interfaces](#user-role-interfaces)
4. [Component Library](#component-library)
5. [Accessibility Guidelines](#accessibility-guidelines)
6. [Implementation Guidelines](#implementation-guidelines)

---

## Overview

The DSR Design System provides a comprehensive framework for creating consistent, accessible, and user-friendly interfaces across all Department of Social Welfare and Development digital services. This system supports three distinct user roles while maintaining visual coherence and Philippine government design standards.

### Design System Goals
- **Consistency**: Unified experience across all DSR services
- **Accessibility**: WCAG 2.0 AA compliance for inclusive design
- **Efficiency**: Reusable components and patterns
- **Scalability**: Flexible system supporting future growth
- **Government Standards**: Adherence to Philippine government design guidelines

### Technical Foundation
- **Framework**: Next.js 14+ with App Router
- **Styling**: Tailwind CSS with custom design tokens
- **Typography**: Inter font family for optimal readability
- **Icons**: Lucide React for consistent iconography
- **State Management**: React Context with React Query/SWR

---

## Design Principles

### 1. Citizen-Centric Design
- **Simplicity First**: Clear, intuitive interfaces for all education levels
- **Progressive Disclosure**: Show essential information first, details on demand
- **Plain Language**: Jargon-free communication in Filipino and English
- **Cultural Sensitivity**: Respectful of Filipino values and context

### 2. Accessibility by Default
- **Universal Design**: Usable by people with diverse abilities
- **Keyboard Navigation**: Full functionality without mouse
- **Screen Reader Support**: Proper semantic markup and ARIA labels
- **Color Independence**: Information conveyed through multiple channels

### 3. Mobile-First Approach
- **Responsive Design**: Optimal experience on all devices
- **Touch-Friendly**: Minimum 44px touch targets
- **Performance**: Fast loading on slow connections
- **Offline Capability**: Essential functions work offline

### 4. Trust and Transparency
- **Clear Communication**: Honest, straightforward messaging
- **Status Visibility**: Always show system status and progress
- **Error Prevention**: Help users avoid mistakes
- **Data Privacy**: Transparent data handling practices

---

## User Role Interfaces

### 1. Citizen Interface

#### Visual Identity
- **Primary Color**: Philippine Government Blue (#1e3a8a)
- **Background**: Clean white (#f8fafc) with subtle gray accents
- **Typography**: Inter Regular for body text, Inter Medium for headings
- **Iconography**: Outline style icons for approachable feel

#### Layout Patterns
```
┌─────────────────────────────────────┐
│ Header: Logo + Navigation + Profile │
├─────────────────────────────────────┤
│ Breadcrumb Navigation               │
├─────────────────────────────────────┤
│ Main Content Area                   │
│ ┌─────────────┐ ┌─────────────────┐ │
│ │ Quick       │ │ Primary Content │ │
│ │ Actions     │ │                 │ │
│ │ Card        │ │                 │ │
│ └─────────────┘ └─────────────────┘ │
├─────────────────────────────────────┤
│ Footer: Links + Contact Info        │
└─────────────────────────────────────┘
```

#### Key Components
- **Service Cards**: Large, visual cards for service discovery
- **Application Forms**: Multi-step forms with progress indicators
- **Status Dashboard**: Clear status updates for applications
- **Document Upload**: Drag-and-drop file upload with preview
- **Help System**: Contextual help and FAQ integration

#### Interaction Patterns
- **Guided Workflows**: Step-by-step process guidance
- **Smart Defaults**: Pre-filled forms where possible
- **Confirmation Dialogs**: Clear confirmation for important actions
- **Success Feedback**: Positive reinforcement for completed actions

### 2. DSWD Staff Interface

#### Visual Identity
- **Primary Color**: Philippine Government Blue (#1e3a8a)
- **Accent Color**: Philippine Government Yellow (#fbbf24)
- **Background**: Professional gray (#f1f5f9) with white content areas
- **Typography**: Inter Medium for headings, Inter Regular for content
- **Iconography**: Filled icons for professional appearance

#### Layout Patterns
```
┌─────────────────────────────────────┐
│ Header: Logo + Search + Notifications + Profile │
├─────────────────────────────────────┤
│ ┌─────────┐ ┌─────────────────────┐ │
│ │ Sidebar │ │ Main Dashboard      │ │
│ │ Nav     │ │ ┌─────┐ ┌─────┐     │ │
│ │         │ │ │Metric│ │Metric│    │ │
│ │ • Cases │ │ │Card │ │Card │     │ │
│ │ • Reports│ │ └─────┘ └─────┘     │ │
│ │ • Users │ │                     │ │
│ │ • Settings│ │ Recent Activity    │ │
│ └─────────┘ └─────────────────────┘ │
└─────────────────────────────────────┘
```

#### Key Components
- **Case Management Dashboard**: Overview of assigned cases
- **Data Tables**: Sortable, filterable tables for case lists
- **Workflow Timeline**: Visual representation of case progress
- **Bulk Actions**: Efficient processing of multiple items
- **Reporting Tools**: Charts and analytics for performance tracking

#### Interaction Patterns
- **Keyboard Shortcuts**: Efficient navigation for power users
- **Batch Operations**: Select multiple items for bulk actions
- **Advanced Filters**: Complex filtering for data discovery
- **Export Functions**: Data export in multiple formats

### 3. LGU Staff Interface

#### Visual Identity
- **Primary Color**: Philippine Government Red (#dc2626)
- **Accent Color**: Philippine Government Yellow (#fbbf24)
- **Background**: Warm gray (#f8fafc) with red accent highlights
- **Typography**: Inter SemiBold for headings, Inter Regular for content
- **Iconography**: Mixed outline/filled icons for balanced feel

#### Layout Patterns
```
┌─────────────────────────────────────┐
│ Header: LGU Logo + DSR Integration + Profile │
├─────────────────────────────────────┤
│ ┌─────────────────────────────────┐ │
│ │ Integration Status Dashboard    │ │
│ │ ┌─────────┐ ┌─────────────────┐ │ │
│ │ │ Sync    │ │ Local Cases     │ │ │
│ │ │ Status  │ │ Pending Review  │ │ │
│ │ └─────────┘ └─────────────────┘ │ │
│ └─────────────────────────────────┘ │
│ ┌─────────────────────────────────┐ │
│ │ Local Beneficiary Management   │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

#### Key Components
- **Integration Dashboard**: Status of DSWD system synchronization
- **Local Case Management**: LGU-specific case handling
- **Beneficiary Registry**: Local beneficiary database management
- **Compliance Reporting**: Automated compliance report generation
- **Communication Hub**: Direct communication with DSWD staff

#### Interaction Patterns
- **Sync Indicators**: Clear status of data synchronization
- **Conflict Resolution**: Tools for handling data conflicts
- **Local Overrides**: Ability to override central system data
- **Escalation Workflows**: Clear escalation paths to DSWD

---

## Component Library

### Foundation Components

#### Button Component
```typescript
interface ButtonProps {
  variant: 'primary' | 'secondary' | 'outline' | 'ghost' | 'destructive';
  size: 'sm' | 'md' | 'lg' | 'xl';
  userRole?: 'CITIZEN' | 'DSWD_STAFF' | 'LGU_STAFF';
  disabled?: boolean;
  loading?: boolean;
  icon?: ReactNode;
  children: ReactNode;
}
```

**Role Variations:**
- **Citizen**: Rounded corners, friendly blue primary
- **DSWD Staff**: Sharp corners, professional blue with yellow accent
- **LGU Staff**: Medium corners, red primary with yellow accent

#### Card Component
```typescript
interface CardProps {
  variant: 'default' | 'elevated' | 'outlined' | 'interactive';
  userRole?: 'CITIZEN' | 'DSWD_STAFF' | 'LGU_STAFF';
  padding: 'sm' | 'md' | 'lg';
  children: ReactNode;
}
```

**Role Variations:**
- **Citizen**: Soft shadows, rounded corners, white background
- **DSWD Staff**: Medium shadows, subtle borders, gray background
- **LGU Staff**: Strong shadows, red accent borders, warm background

### Navigation Components

#### Role-Based Navigation
```typescript
interface NavigationProps {
  userRole: 'CITIZEN' | 'DSWD_STAFF' | 'LGU_STAFF';
  currentPath: string;
  notifications?: number;
  onNavigate: (path: string) => void;
}
```

**Navigation Structures:**
- **Citizen**: Horizontal navigation with service categories
- **DSWD Staff**: Vertical sidebar with collapsible sections
- **LGU Staff**: Hybrid navigation with integration status

### Form Components

#### Multi-Step Form
```typescript
interface MultiStepFormProps {
  steps: Array<{
    id: string;
    title: string;
    description?: string;
    component: ReactNode;
    validation?: ValidationSchema;
  }>;
  userRole?: 'CITIZEN' | 'DSWD_STAFF' | 'LGU_STAFF';
  onComplete: (data: any) => void;
}
```

**Role Adaptations:**
- **Citizen**: Large step indicators, helpful descriptions
- **DSWD Staff**: Compact indicators, validation summaries
- **LGU Staff**: Status synchronization indicators

### Data Display Components

#### Status Badge
```typescript
interface StatusBadgeProps {
  status: 'draft' | 'submitted' | 'review' | 'approved' | 'rejected';
  userRole?: 'CITIZEN' | 'DSWD_STAFF' | 'LGU_STAFF';
  size?: 'sm' | 'md' | 'lg';
}
```

**Role Variations:**
- **Citizen**: Friendly language, encouraging colors
- **DSWD Staff**: Professional terminology, standard colors
- **LGU Staff**: Local context, integration status colors

---

## Accessibility Guidelines

### WCAG 2.0 AA Compliance

#### Color and Contrast
- **Minimum Contrast**: 4.5:1 for normal text, 3:1 for large text
- **Color Independence**: Never use color alone to convey information
- **High Contrast Mode**: Support for Windows high contrast mode
- **Color Blindness**: Test with color blindness simulators

#### Keyboard Navigation
- **Tab Order**: Logical tab sequence following visual layout
- **Focus Indicators**: Visible 2px outline with primary color
- **Skip Links**: "Skip to main content" for screen reader users
- **Keyboard Shortcuts**: Document all keyboard shortcuts

#### Screen Reader Support
- **Semantic HTML**: Use proper heading hierarchy (h1-h6)
- **ARIA Labels**: Descriptive labels for interactive elements
- **Live Regions**: Announce dynamic content changes
- **Alternative Text**: Descriptive alt text for all images

#### Touch and Motor
- **Touch Targets**: Minimum 44px for interactive elements
- **Spacing**: Adequate spacing between interactive elements
- **Gesture Alternatives**: Provide alternatives to complex gestures
- **Timeout Extensions**: Allow users to extend session timeouts

### Testing Procedures
1. **Automated Testing**: Use axe-core for automated accessibility testing
2. **Keyboard Testing**: Navigate entire interface using only keyboard
3. **Screen Reader Testing**: Test with NVDA, JAWS, and VoiceOver
4. **Color Testing**: Verify contrast ratios and color independence
5. **User Testing**: Include users with disabilities in testing process

---

## Implementation Guidelines

### Development Workflow
1. **Design Tokens**: Use Tokens Studio for Figma integration
2. **Component Development**: Build components with accessibility first
3. **Testing**: Automated and manual accessibility testing
4. **Documentation**: Maintain component documentation
5. **Review**: Accessibility review for all new components

### Code Standards
- **TypeScript**: Strict typing for all components
- **ESLint**: Accessibility-focused linting rules
- **Testing**: 80%+ test coverage including accessibility tests
- **Documentation**: Storybook for component documentation

### Performance Guidelines
- **Bundle Size**: Monitor and optimize bundle size
- **Loading States**: Provide loading indicators for all async operations
- **Error Boundaries**: Graceful error handling
- **Caching**: Implement appropriate caching strategies

This design system ensures consistent, accessible, and user-friendly interfaces across all DSR services while respecting the unique needs of each user role.

---

## Role-Specific Interface Patterns

### Citizen Interface Detailed Specifications

#### Dashboard Layout
The citizen dashboard prioritizes service discovery and application status tracking:

```
┌─────────────────────────────────────────────────────┐
│ Welcome Header: "Kumusta, [Name]!" + Quick Stats   │
├─────────────────────────────────────────────────────┤
│ ┌─────────────────┐ ┌─────────────────────────────┐ │
│ │ Quick Actions   │ │ Application Status          │ │
│ │ • Apply for Aid │ │ ┌─────────────────────────┐ │ │
│ │ • Check Status  │ │ │ [Progress Bar] 60%      │ │ │
│ │ • Upload Docs   │ │ │ Educational Assistance  │ │ │
│ │ • Contact Help  │ │ │ Next: Document Review   │ │ │
│ └─────────────────┘ │ └─────────────────────────┘ │ │
│                     │ ┌─────────────────────────┐ │ │
│                     │ │ Recent Notifications    │ │ │
│                     │ │ • Payment processed     │ │ │
│                     │ │ • Document approved     │ │ │
│                     │ └─────────────────────────┘ │ │
│                     └─────────────────────────────┘ │
├─────────────────────────────────────────────────────┤
│ Available Services (Card Grid)                      │
│ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐   │
│ │[Icon]   │ │[Icon]   │ │[Icon]   │ │[Icon]   │   │
│ │Medical  │ │Educ.    │ │Housing  │ │Senior   │   │
│ │Assist.  │ │Assist.  │ │Assist.  │ │Citizen  │   │
│ └─────────┘ └─────────┘ └─────────┘ └─────────┘   │
└─────────────────────────────────────────────────────┘
```

#### Service Application Flow
1. **Service Discovery**: Visual service cards with clear descriptions
2. **Eligibility Check**: Interactive eligibility questionnaire
3. **Application Form**: Multi-step form with progress indicator
4. **Document Upload**: Drag-and-drop with preview and validation
5. **Review & Submit**: Summary page with edit capabilities
6. **Confirmation**: Clear confirmation with next steps

#### Accessibility Features
- **Large Touch Targets**: 48px minimum for mobile users
- **High Contrast**: 7:1 contrast ratio for better visibility
- **Simple Language**: Grade 8 reading level in Filipino/English
- **Voice Navigation**: Support for voice commands
- **Offline Mode**: Core functions work without internet

### DSWD Staff Interface Detailed Specifications

#### Case Management Dashboard
The DSWD staff interface focuses on efficient case processing and oversight:

```
┌─────────────────────────────────────────────────────┐
│ Header: Search + Notifications(5) + Profile        │
├─────────────────────────────────────────────────────┤
│ ┌─────────┐ ┌─────────────────────────────────────┐ │
│ │ Sidebar │ │ Dashboard Metrics                   │ │
│ │         │ │ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐   │ │
│ │ Cases   │ │ │ 45  │ │ 12  │ │ 8   │ │ 2   │   │ │
│ │ • New   │ │ │Pend.│ │Rev. │ │App. │ │Rej. │   │ │
│ │ • Review│ │ └─────┘ └─────┘ └─────┘ └─────┘   │ │
│ │ • Apprvd│ │                                     │ │
│ │         │ │ Priority Cases (Table)              │ │
│ │ Reports │ │ ┌─────────────────────────────────┐ │ │
│ │ • Daily │ │ │Name │Type │Date │Status │Action│ │ │
│ │ • Weekly│ │ │─────┼─────┼─────┼───────┼──────│ │ │
│ │ • Custom│ │ │Juan │Med  │3/15 │Review │[Btn] │ │ │
│ │         │ │ │Maria│Edu  │3/14 │New    │[Btn] │ │ │
│ │ Admin   │ │ └─────────────────────────────────┘ │ │
│ │ • Users │ │                                     │ │
│ │ • Config│ │ Recent Activity Feed                │ │
│ └─────────┘ └─────────────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
```

#### Workflow Management
- **Case Assignment**: Automatic and manual case assignment
- **Bulk Operations**: Process multiple cases simultaneously
- **Approval Workflows**: Multi-level approval processes
- **Deadline Tracking**: Visual indicators for approaching deadlines
- **Performance Metrics**: Individual and team performance tracking

#### Advanced Features
- **Smart Filters**: AI-powered case categorization
- **Predictive Analytics**: Risk assessment for applications
- **Integration APIs**: Connect with external government systems
- **Audit Trail**: Complete history of all case actions
- **Mobile App**: Companion mobile app for field work

### LGU Staff Interface Detailed Specifications

#### Integration Dashboard
The LGU staff interface emphasizes local management with central coordination:

```
┌─────────────────────────────────────────────────────┐
│ Header: [LGU Logo] + DSWD Integration + Profile     │
├─────────────────────────────────────────────────────┤
│ Integration Status Panel                            │
│ ┌─────────────────────────────────────────────────┐ │
│ │ ● Connected to DSWD Central (Last sync: 2 min)  │ │
│ │ ↑ 15 cases uploaded ↓ 3 updates received        │ │
│ │ ⚠ 2 conflicts require resolution                 │ │
│ └─────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────┤
│ ┌─────────────────┐ ┌─────────────────────────────┐ │
│ │ Local Cases     │ │ Beneficiary Registry        │ │
│ │ • Pending: 23   │ │ • Total: 1,247              │ │
│ │ • Approved: 156 │ │ • Active: 892               │ │
│ │ • Rejected: 12  │ │ • Inactive: 355             │ │
│ │                 │ │ • Duplicates: 3 (resolve)   │ │
│ │ [View All]      │ │ [Manage Registry]           │ │
│ └─────────────────┘ └─────────────────────────────┘ │
├─────────────────────────────────────────────────────┤
│ Local Actions Required                              │
│ ┌─────────────────────────────────────────────────┐ │
│ │ • Review 5 field verification reports           │ │
│ │ • Resolve 2 data conflicts with central system  │ │
│ │ • Submit monthly compliance report (Due: 3/20)  │ │
│ │ • Update 12 beneficiary status changes          │ │
│ └─────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
```

#### Local Management Features
- **Field Verification**: Mobile tools for field staff
- **Community Mapping**: Geographic visualization of beneficiaries
- **Local Reporting**: Customizable reports for LGU leadership
- **Conflict Resolution**: Tools for resolving data discrepancies
- **Offline Capability**: Full functionality during connectivity issues

#### Synchronization Management
- **Real-time Sync**: Automatic synchronization with central system
- **Conflict Detection**: Identify and resolve data conflicts
- **Version Control**: Track changes and maintain data integrity
- **Backup Systems**: Local data backup and recovery
- **Audit Compliance**: Maintain compliance with national standards

---

## Component Interaction Patterns

### Cross-Role Communication
- **Notification System**: Role-appropriate notifications
- **Message Center**: Secure communication between roles
- **Status Updates**: Real-time status updates across roles
- **Escalation Paths**: Clear escalation procedures

### Data Sharing Protocols
- **Privacy Controls**: Role-based data access controls
- **Consent Management**: Citizen consent for data sharing
- **Audit Logging**: Complete audit trail for all data access
- **Encryption**: End-to-end encryption for sensitive data

### Performance Optimization
- **Lazy Loading**: Load components as needed
- **Caching Strategy**: Intelligent caching for better performance
- **Progressive Enhancement**: Core functionality first, enhancements second
- **Bundle Splitting**: Role-specific code bundles

This comprehensive design system provides the foundation for a cohesive, accessible, and efficient DSR frontend that serves all user roles effectively while maintaining Philippine government design standards.
