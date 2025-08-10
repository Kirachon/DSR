/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/pages/**/*.{js,ts,jsx,tsx,mdx}',
    './src/components/**/*.{js,ts,jsx,tsx,mdx}',
    './src/app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
      "philippine-government": {
            "primary": {
                  "50": "#eff6ff",
                  "100": "#dbeafe",
                  "200": "#bfdbfe",
                  "300": "#93c5fd",
                  "400": "#60a5fa",
                  "500": "#1e3a8a",
                  "600": "#1d4ed8",
                  "700": "#1e40af",
                  "800": "#1e3a8a",
                  "900": "#172554",
                  "950": "#0f172a"
            },
            "secondary": {
                  "50": "#fef2f2",
                  "100": "#fee2e2",
                  "200": "#fecaca",
                  "300": "#fca5a5",
                  "400": "#f87171",
                  "500": "#dc2626",
                  "600": "#b91c1c",
                  "700": "#991b1b",
                  "800": "#7f1d1d",
                  "900": "#450a0a",
                  "950": "#2d0a0a"
            },
            "accent": {
                  "50": "#fffbeb",
                  "100": "#fef3c7",
                  "200": "#fde68a",
                  "300": "#fcd34d",
                  "400": "#fbbf24",
                  "500": "#f59e0b",
                  "600": "#d97706",
                  "700": "#b45309",
                  "800": "#92400e",
                  "900": "#78350f",
                  "950": "#451a03"
            }
      },
      "semantic": {
            "success": {
                  "50": "#f0fdf4",
                  "100": "#dcfce7",
                  "200": "#bbf7d0",
                  "300": "#86efac",
                  "400": "#4ade80",
                  "500": "#16a34a",
                  "600": "#15803d",
                  "700": "#166534",
                  "800": "#14532d",
                  "900": "#052e16",
                  "950": "#021f0c"
            },
            "warning": {
                  "50": "#fffbeb",
                  "100": "#fef3c7",
                  "200": "#fde68a",
                  "300": "#fcd34d",
                  "400": "#fbbf24",
                  "500": "#f59e0b",
                  "600": "#d97706",
                  "700": "#b45309",
                  "800": "#92400e",
                  "900": "#78350f",
                  "950": "#451a03"
            },
            "error": {
                  "50": "#fef2f2",
                  "100": "#fee2e2",
                  "200": "#fecaca",
                  "300": "#fca5a5",
                  "400": "#f87171",
                  "500": "#ef4444",
                  "600": "#dc2626",
                  "700": "#b91c1c",
                  "800": "#991b1b",
                  "900": "#7f1d1d",
                  "950": "#450a0a"
            },
            "info": {
                  "50": "#f0f9ff",
                  "100": "#e0f2fe",
                  "200": "#bae6fd",
                  "300": "#7dd3fc",
                  "400": "#38bdf8",
                  "500": "#0ea5e9",
                  "600": "#0284c7",
                  "700": "#0369a1",
                  "800": "#075985",
                  "900": "#0c4a6e",
                  "950": "#082f49"
            }
      },
      "neutral": {}
},
      spacing: {
      "0": "0px",
      "1": "0.25rem",
      "2": "0.5rem",
      "3": "0.75rem",
      "4": "1rem",
      "5": "1.25rem",
      "6": "1.5rem",
      "8": "2rem",
      "10": "2.5rem",
      "12": "3rem",
      "16": "4rem",
      "20": "5rem",
      "24": "6rem",
      "32": "8rem",
      "40": "10rem",
      "48": "12rem",
      "56": "14rem",
      "64": "16rem"
},
      fontSize: {
      "xs": "0.75rem",
      "sm": "0.875rem",
      "base": "1rem",
      "lg": "1.125rem",
      "xl": "1.25rem",
      "2xl": "1.5rem",
      "3xl": "1.875rem",
      "4xl": "2.25rem",
      "5xl": "3rem",
      "6xl": "3.75rem"
},
      fontFamily: {
      "primary": [
            "Inter",
            "system-ui",
            "-apple-system",
            "sans-serif"
      ],
      "secondary": [
            "JetBrains Mono",
            "Consolas",
            "monospace"
      ]
},
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
};