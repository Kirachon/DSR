#!/usr/bin/env node

/**
 * DSR Design Token Build Script
 * Builds design tokens for Tailwind CSS integration
 */

const { buildTokens } = require('./tailwind-config-generator');
const fs = require('fs');
const path = require('path');

// Ensure output directories exist
const ensureDirectories = () => {
  const dirs = [
    'src/styles',
    'src/components/ui',
    'src/types'
  ];

  dirs.forEach(dir => {
    if (!fs.existsSync(dir)) {
      fs.mkdirSync(dir, { recursive: true });
      console.log(`📁 Created directory: ${dir}`);
    }
  });
};

// Generate TypeScript types for design tokens
const generateTokenTypes = () => {
  const tokenTypes = `/**
 * DSR Design Token Types
 * Auto-generated from design tokens
 */

export interface DSRColors {
  'philippine-government': {
    primary: Record<string, string>;
    secondary: Record<string, string>;
    accent: Record<string, string>;
  };
  semantic: {
    success: Record<string, string>;
    warning: Record<string, string>;
    error: Record<string, string>;
    info: Record<string, string>;
  };
  neutral: Record<string, string>;
}

export interface DSRSpacing {
  0: string;
  1: string;
  2: string;
  3: string;
  4: string;
  5: string;
  6: string;
  8: string;
  10: string;
  12: string;
  16: string;
  20: string;
  24: string;
  32: string;
  40: string;
  48: string;
  56: string;
  64: string;
}

export interface DSRTypography {
  'font-families': {
    primary: string;
    secondary: string;
  };
  'font-sizes': {
    xs: string;
    sm: string;
    base: string;
    lg: string;
    xl: string;
    '2xl': string;
    '3xl': string;
    '4xl': string;
    '5xl': string;
    '6xl': string;
  };
  'font-weights': {
    light: string;
    normal: string;
    medium: string;
    semibold: string;
    bold: string;
    extrabold: string;
  };
}

export interface DSRBreakpoints {
  xs: string;
  sm: string;
  md: string;
  lg: string;
  xl: string;
  '2xl': string;
  '3xl': string;
}

export type UserRole = 'CITIZEN' | 'LGU_STAFF' | 'DSWD_STAFF' | 'ADMIN';

export interface ComponentVariant {
  colors: Partial<DSRColors>;
  spacing: Partial<DSRSpacing>;
  typography: Partial<DSRTypography>;
}

export interface RoleBasedTheme {
  citizen: ComponentVariant;
  'dswd-staff': ComponentVariant;
  'lgu-staff': ComponentVariant;
}
`;

  fs.writeFileSync('src/types/design-tokens.ts', tokenTypes);
  console.log('📝 Generated TypeScript types: src/types/design-tokens.ts');
};

// Generate CSS custom properties
const generateCSSVariables = () => {
  const cssVariables = `/**
 * DSR Design Token CSS Variables
 * Auto-generated from design tokens
 */

:root {
  /* Philippine Government Colors */
  --dsr-ph-gov-primary-50: #eff6ff;
  --dsr-ph-gov-primary-100: #dbeafe;
  --dsr-ph-gov-primary-500: #1e3a8a;
  --dsr-ph-gov-primary-600: #1d4ed8;
  --dsr-ph-gov-primary-900: #172554;

  --dsr-ph-gov-secondary-50: #fef2f2;
  --dsr-ph-gov-secondary-500: #dc2626;
  --dsr-ph-gov-secondary-600: #b91c1c;
  --dsr-ph-gov-secondary-900: #450a0a;

  --dsr-ph-gov-accent-50: #fffbeb;
  --dsr-ph-gov-accent-500: #fbbf24;
  --dsr-ph-gov-accent-600: #d97706;
  --dsr-ph-gov-accent-900: #78350f;

  /* Semantic Colors */
  --dsr-success-50: #f0fdf4;
  --dsr-success-500: #16a34a;
  --dsr-success-600: #15803d;
  --dsr-success-900: #052e16;

  --dsr-warning-50: #fffbeb;
  --dsr-warning-500: #f59e0b;
  --dsr-warning-600: #d97706;
  --dsr-warning-900: #78350f;

  --dsr-error-50: #fef2f2;
  --dsr-error-500: #ef4444;
  --dsr-error-600: #dc2626;
  --dsr-error-900: #7f1d1d;

  --dsr-info-50: #f0f9ff;
  --dsr-info-500: #0ea5e9;
  --dsr-info-600: #0284c7;
  --dsr-info-900: #0c4a6e;

  /* Neutral Colors */
  --dsr-neutral-50: #f8fafc;
  --dsr-neutral-100: #f1f5f9;
  --dsr-neutral-200: #e2e8f0;
  --dsr-neutral-300: #cbd5e1;
  --dsr-neutral-400: #94a3b8;
  --dsr-neutral-500: #64748b;
  --dsr-neutral-600: #475569;
  --dsr-neutral-700: #334155;
  --dsr-neutral-800: #1e293b;
  --dsr-neutral-900: #0f172a;

  /* Typography */
  --dsr-font-family-primary: Inter, system-ui, -apple-system, sans-serif;
  --dsr-font-family-secondary: JetBrains Mono, Consolas, monospace;

  /* Spacing */
  --dsr-spacing-0: 0px;
  --dsr-spacing-1: 0.25rem;
  --dsr-spacing-2: 0.5rem;
  --dsr-spacing-3: 0.75rem;
  --dsr-spacing-4: 1rem;
  --dsr-spacing-5: 1.25rem;
  --dsr-spacing-6: 1.5rem;
  --dsr-spacing-8: 2rem;
  --dsr-spacing-10: 2.5rem;
  --dsr-spacing-12: 3rem;
  --dsr-spacing-16: 4rem;
  --dsr-spacing-20: 5rem;
  --dsr-spacing-24: 6rem;
  --dsr-spacing-32: 8rem;
  --dsr-spacing-40: 10rem;
  --dsr-spacing-48: 12rem;
  --dsr-spacing-56: 14rem;
  --dsr-spacing-64: 16rem;

  /* Border Radius */
  --dsr-radius-none: 0px;
  --dsr-radius-sm: 0.125rem;
  --dsr-radius-base: 0.25rem;
  --dsr-radius-md: 0.375rem;
  --dsr-radius-lg: 0.5rem;
  --dsr-radius-xl: 0.75rem;
  --dsr-radius-2xl: 1rem;
  --dsr-radius-3xl: 1.5rem;
  --dsr-radius-full: 9999px;

  /* Shadows */
  --dsr-shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
  --dsr-shadow-base: 0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1);
  --dsr-shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
  --dsr-shadow-lg: 0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1);
  --dsr-shadow-xl: 0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1);
  --dsr-shadow-2xl: 0 25px 50px -12px rgb(0 0 0 / 0.25);
  --dsr-shadow-inner: inset 0 2px 4px 0 rgb(0 0 0 / 0.05);
}

/* Role-based theme variations */
[data-theme="citizen"] {
  --dsr-primary: var(--dsr-ph-gov-primary-500);
  --dsr-primary-hover: var(--dsr-ph-gov-primary-600);
  --dsr-background: var(--dsr-neutral-50);
  --dsr-surface: var(--dsr-neutral-100);
}

[data-theme="dswd-staff"] {
  --dsr-primary: var(--dsr-ph-gov-primary-500);
  --dsr-accent: var(--dsr-ph-gov-accent-500);
  --dsr-background: var(--dsr-neutral-50);
  --dsr-surface: var(--dsr-neutral-100);
}

[data-theme="lgu-staff"] {
  --dsr-primary: var(--dsr-ph-gov-secondary-500);
  --dsr-accent: var(--dsr-ph-gov-accent-500);
  --dsr-background: var(--dsr-neutral-50);
  --dsr-surface: var(--dsr-neutral-100);
}

/* Accessibility enhancements */
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

/* High contrast mode support */
@media (prefers-contrast: high) {
  :root {
    --dsr-neutral-200: #000000;
    --dsr-neutral-800: #ffffff;
  }
}

/* Dark mode support (future enhancement) */
@media (prefers-color-scheme: dark) {
  :root {
    --dsr-background: var(--dsr-neutral-900);
    --dsr-surface: var(--dsr-neutral-800);
    --dsr-text-primary: var(--dsr-neutral-100);
    --dsr-text-secondary: var(--dsr-neutral-300);
  }
}
`;

  fs.writeFileSync('src/styles/design-tokens.css', cssVariables);
  console.log('🎨 Generated CSS variables: src/styles/design-tokens.css');
};

// Main build function
const main = async () => {
  console.log('🚀 Starting DSR design token build...\n');

  try {
    // Ensure directories exist
    ensureDirectories();

    // Generate TypeScript types
    generateTokenTypes();

    // Generate CSS variables
    generateCSSVariables();

    // Build tokens using Style Dictionary
    await buildTokens();

    console.log('\n✅ DSR design token build completed successfully!');
    console.log('\n📋 Next steps:');
    console.log('   1. Import design tokens in your Tailwind config');
    console.log('   2. Use CSS variables in your components');
    console.log('   3. Apply role-based themes with data-theme attributes');
    console.log('   4. Test accessibility compliance with generated tokens\n');

  } catch (error) {
    console.error('❌ Build failed:', error);
    process.exit(1);
  }
};

// Run if called directly
if (require.main === module) {
  main();
}

module.exports = { main };
