#!/usr/bin/env node

/**
 * DSR Design Token Build Script
 * Builds design tokens for Tailwind CSS integration
 */

const fs = require('fs');
const path = require('path');

// Simple token processor
function processTokens() {
  console.log('🎨 Building DSR design tokens...');

  try {
    // Read the design tokens
    const tokensPath = path.join(__dirname, 'dsr-design-tokens.json');
    const tokens = JSON.parse(fs.readFileSync(tokensPath, 'utf8'));

    // Generate CSS variables
    generateCSSVariables(tokens);

    // Generate Tailwind config
    generateTailwindConfig(tokens);

    console.log('✅ Design tokens built successfully!');
    console.log('📁 Generated files:');
    console.log('   - src/styles/design-tokens.css');
    console.log('   - tailwind.config.tokens.js');
  } catch (error) {
    console.error('❌ Build failed:', error);
    process.exit(1);
  }
}

function generateCSSVariables(tokens) {
  const cssVars = [];

  // Process global colors
  if (tokens.global?.colors) {
    Object.entries(tokens.global.colors).forEach(([category, colors]) => {
      Object.entries(colors).forEach(([name, shades]) => {
        if (typeof shades === 'object') {
          Object.entries(shades).forEach(([shade, value]) => {
            cssVars.push(`  --dsr-${category}-${name}-${shade}: ${value};`);
          });
        }
      });
    });
  }

  // Process spacing
  if (tokens.global?.spacing) {
    Object.entries(tokens.global.spacing).forEach(([key, value]) => {
      cssVars.push(`  --dsr-spacing-${key}: ${value};`);
    });
  }

  // Process typography
  if (tokens.global?.typography) {
    Object.entries(tokens.global.typography).forEach(([category, values]) => {
      Object.entries(values).forEach(([key, value]) => {
        cssVars.push(`  --dsr-${category.replace('-', '')}-${key}: ${value};`);
      });
    });
  }

  const cssContent = `/**
 * DSR Design Token CSS Variables
 * Auto-generated from design tokens
 */

:root {
${cssVars.join('\n')}
}

/* Role-based theme variations */
[data-theme="citizen"] {
  --dsr-primary: var(--dsr-philippine-government-primary-500);
  --dsr-background: var(--dsr-neutral-50);
  --dsr-surface: var(--dsr-neutral-100);
}

[data-theme="dswd-staff"] {
  --dsr-primary: var(--dsr-philippine-government-primary-500);
  --dsr-accent: var(--dsr-philippine-government-accent-500);
  --dsr-background: var(--dsr-neutral-50);
  --dsr-surface: var(--dsr-neutral-100);
}

[data-theme="lgu-staff"] {
  --dsr-primary: var(--dsr-philippine-government-secondary-500);
  --dsr-accent: var(--dsr-philippine-government-accent-500);
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
`;

  // Ensure src/styles directory exists
  const stylesDir = path.join(__dirname, '..', 'src', 'styles');
  if (!fs.existsSync(stylesDir)) {
    fs.mkdirSync(stylesDir, { recursive: true });
  }

  fs.writeFileSync(path.join(stylesDir, 'design-tokens.css'), cssContent);
}

function generateTailwindConfig(tokens) {
  const colors = {};
  const spacing = {};
  const fontSize = {};
  const fontFamily = {};

  // Process colors
  if (tokens.global?.colors) {
    Object.entries(tokens.global.colors).forEach(([category, colorGroups]) => {
      colors[category] = {};
      Object.entries(colorGroups).forEach(([name, shades]) => {
        if (typeof shades === 'object') {
          colors[category][name] = shades;
        }
      });
    });
  }

  // Process spacing
  if (tokens.global?.spacing) {
    Object.assign(spacing, tokens.global.spacing);
  }

  // Process typography
  if (tokens.global?.typography?.['font-sizes']) {
    Object.assign(fontSize, tokens.global.typography['font-sizes']);
  }

  if (tokens.global?.typography?.['font-families']) {
    Object.entries(tokens.global.typography['font-families']).forEach(([key, value]) => {
      fontFamily[key] = value.split(', ');
    });
  }

  const tailwindConfig = `/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/pages/**/*.{js,ts,jsx,tsx,mdx}',
    './src/components/**/*.{js,ts,jsx,tsx,mdx}',
    './src/app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: ${JSON.stringify(colors, null, 6)},
      spacing: ${JSON.stringify(spacing, null, 6)},
      fontSize: ${JSON.stringify(fontSize, null, 6)},
      fontFamily: ${JSON.stringify(fontFamily, null, 6)},
      animation: {
        'fade-in': 'fadeIn 0.3s ease-in-out',
        'slide-up': 'slideUp 0.3s ease-out',
        'slide-down': 'slideDown 0.3s ease-out',
        'scale-in': 'scaleIn 0.2s ease-out',
        'bounce-gentle': 'bounceGentle 0.6s ease-out',
        'pulse-slow': 'pulseSlow 2s ease-in-out infinite',
        'progress-fill': 'progressFill 1s ease-out',
        'status-change': 'statusChange 0.4s ease-in-out',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { transform: 'translateY(10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        slideDown: {
          '0%': { transform: 'translateY(-10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        scaleIn: {
          '0%': { transform: 'scale(0.95)', opacity: '0' },
          '100%': { transform: 'scale(1)', opacity: '1' },
        },
        bounceGentle: {
          '0%, 20%, 53%, 80%, 100%': { transform: 'translate3d(0,0,0)' },
          '40%, 43%': { transform: 'translate3d(0, -8px, 0)' },
          '70%': { transform: 'translate3d(0, -4px, 0)' },
          '90%': { transform: 'translate3d(0, -2px, 0)' },
        },
        pulseSlow: {
          '0%, 100%': { opacity: '1' },
          '50%': { opacity: '0.7' },
        },
        progressFill: {
          '0%': { width: '0%' },
          '100%': { width: 'var(--progress-width, 100%)' },
        },
        statusChange: {
          '0%': { transform: 'scale(1)', backgroundColor: 'currentColor' },
          '50%': { transform: 'scale(1.05)' },
          '100%': { transform: 'scale(1)', backgroundColor: 'var(--status-color)' },
        },
      },
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
    require('@tailwindcss/typography'),
    require('@tailwindcss/aspect-ratio'),
  ],
};`;

  fs.writeFileSync(path.join(__dirname, '..', 'tailwind.config.tokens.js'), tailwindConfig);
}

// Run if called directly
if (require.main === module) {
  processTokens();
}

module.exports = { processTokens };
    const colors = {};
    const spacing = {};
    const fontSize = {};
    const fontFamily = {};
    const borderRadius = {};
    const boxShadow = {};
    const screens = {};

    dictionary.allTokens.forEach(token => {
      const path = token.path;
      const value = token.value;

      if (path.includes('colors')) {
        const colorPath = path.slice(path.indexOf('colors') + 1);
        setNestedValue(colors, colorPath, value);
      } else if (path.includes('spacing')) {
        const spacingKey = path[path.length - 1];
        spacing[spacingKey] = value;
      } else if (path.includes('font-sizes')) {
        const sizeKey = path[path.length - 1];
        fontSize[sizeKey] = value;
      } else if (path.includes('font-families')) {
        const familyKey = path[path.length - 1];
        fontFamily[familyKey] = value.split(', ');
      } else if (path.includes('border-radius')) {
        const radiusKey = path[path.length - 1];
        borderRadius[radiusKey] = value;
      } else if (path.includes('shadows')) {
        const shadowKey = path[path.length - 1];
        boxShadow[shadowKey] = value;
      } else if (path.includes('breakpoints')) {
        const screenKey = path[path.length - 1];
        screens[screenKey] = value;
      }
    });

    return `/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/pages/**/*.{js,ts,jsx,tsx,mdx}',
    './src/components/**/*.{js,ts,jsx,tsx,mdx}',
    './src/app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: ${JSON.stringify(colors, null, 6)},
      spacing: ${JSON.stringify(spacing, null, 6)},
      fontSize: ${JSON.stringify(fontSize, null, 6)},
      fontFamily: ${JSON.stringify(fontFamily, null, 6)},
      borderRadius: ${JSON.stringify(borderRadius, null, 6)},
      boxShadow: ${JSON.stringify(boxShadow, null, 6)},
      screens: ${JSON.stringify(screens, null, 6)},
      animation: {
        'fade-in': 'fadeIn 0.3s ease-in-out',
        'slide-up': 'slideUp 0.3s ease-out',
        'slide-down': 'slideDown 0.3s ease-out',
        'scale-in': 'scaleIn 0.2s ease-out',
        'bounce-gentle': 'bounceGentle 0.6s ease-out',
        'pulse-slow': 'pulseSlow 2s ease-in-out infinite',
        'progress-fill': 'progressFill 1s ease-out',
        'status-change': 'statusChange 0.4s ease-in-out',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { transform: 'translateY(10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        slideDown: {
          '0%': { transform: 'translateY(-10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        scaleIn: {
          '0%': { transform: 'scale(0.95)', opacity: '0' },
          '100%': { transform: 'scale(1)', opacity: '1' },
        },
        bounceGentle: {
          '0%, 20%, 53%, 80%, 100%': { transform: 'translate3d(0,0,0)' },
          '40%, 43%': { transform: 'translate3d(0, -8px, 0)' },
          '70%': { transform: 'translate3d(0, -4px, 0)' },
          '90%': { transform: 'translate3d(0, -2px, 0)' },
        },
        pulseSlow: {
          '0%, 100%': { opacity: '1' },
          '50%': { opacity: '0.7' },
        },
        progressFill: {
          '0%': { width: '0%' },
          '100%': { width: 'var(--progress-width, 100%)' },
        },
        statusChange: {
          '0%': { transform: 'scale(1)', backgroundColor: 'currentColor' },
          '50%': { transform: 'scale(1.05)' },
          '100%': { transform: 'scale(1)', backgroundColor: 'var(--status-color)' },
        },
      },
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
    require('@tailwindcss/typography'),
    require('@tailwindcss/aspect-ratio'),
  ],
};`;
  }
});

// Helper function to set nested object values
function setNestedValue(obj, path, value) {
  const keys = Array.isArray(path) ? path : path.split('.');
  let current = obj;
  
  for (let i = 0; i < keys.length - 1; i++) {
    const key = keys[i];
    if (!(key in current)) {
      current[key] = {};
    }
    current = current[key];
  }
  
  current[keys[keys.length - 1]] = value;
}

// Style Dictionary configuration
const dsrConfig = {
  source: ['design-tokens/dsr-design-tokens.json'],
  platforms: {
    css: {
      transformGroup: 'tokens-studio',
      transforms: ['dsr/tailwind/color', 'dsr/tailwind/spacing', 'name/kebab'],
      buildPath: 'src/styles/',
      files: [
        {
          destination: 'design-tokens.css',
          format: 'css/variables',
          options: {
            outputReferences: true,
          },
        },
      ],
    },
    tailwind: {
      transforms: ['dsr/tailwind/color', 'dsr/tailwind/spacing', 'name/kebab'],
      buildPath: './',
      files: [
        {
          destination: 'tailwind.config.tokens.js',
          format: 'dsr/tailwind/config',
        },
      ],
    },
  },
};

// Build tokens
async function buildTokens() {
  console.log('🎨 Building DSR design tokens...');
  
  try {
    const sd = new StyleDictionary(dsrConfig);
    await sd.cleanAllPlatforms();
    await sd.buildAllPlatforms();
    
    console.log('✅ Design tokens built successfully!');
    console.log('📁 Generated files:');
    console.log('   - src/styles/design-tokens.css');
    console.log('   - tailwind.config.tokens.js');
  } catch (error) {
    console.error('❌ Build failed:', error);
    process.exit(1);
  }
}

// Run if called directly
if (require.main === module) {
  buildTokens();
}

module.exports = { buildTokens };
