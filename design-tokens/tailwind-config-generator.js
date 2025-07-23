/**
 * DSR Design Token to Tailwind CSS Configuration Generator
 * Transforms design tokens into Tailwind-compatible configuration
 */

const { register } = require('@tokens-studio/sd-transforms');
const StyleDictionary = require('style-dictionary');
const fs = require('fs');
const path = require('path');

// Register Tokens Studio transforms
register(StyleDictionary, {
  excludeParentKeys: true,
  platform: 'css',
  name: 'dsr-tokens',
});

// DSR-specific transform for Tailwind CSS
StyleDictionary.registerTransform({
  name: 'dsr/tailwind/color',
  type: 'value',
  matcher: (token) => token.type === 'color',
  transformer: (token) => {
    // Convert color values to CSS custom properties for Tailwind
    return `var(--dsr-${token.name.replace(/\./g, '-')})`;
  }
});

StyleDictionary.registerTransform({
  name: 'dsr/tailwind/spacing',
  type: 'value',
  matcher: (token) => token.type === 'spacing' || token.type === 'dimension',
  transformer: (token) => {
    // Ensure spacing values have units
    const value = token.value;
    return typeof value === 'number' ? `${value}px` : value;
  }
});

// Custom format for Tailwind config generation
StyleDictionary.registerFormat({
  name: 'dsr/tailwind/config',
  formatter: ({ dictionary }) => {
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
  preprocessors: ['dsr-tokens'],
  platforms: {
    css: {
      transformGroup: 'dsr-tokens',
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
  
  const sd = new StyleDictionary(dsrConfig);
  await sd.cleanAllPlatforms();
  await sd.buildAllPlatforms();
  
  console.log('✅ Design tokens built successfully!');
  console.log('📁 Generated files:');
  console.log('   - src/styles/design-tokens.css');
  console.log('   - tailwind.config.tokens.js');
}

// Export for use in build scripts
module.exports = { buildTokens, dsrConfig };

// Run if called directly
if (require.main === module) {
  buildTokens().catch(console.error);
}
