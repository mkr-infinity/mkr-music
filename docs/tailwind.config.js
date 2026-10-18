/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        graphite: {
          950: '#080809', // Deep Nordic charcoal base (zero blue)
          900: '#0e0f10', // Dark earth tone
          850: '#141416', // Surface card
          800: '#1c1c1f', // Elevated surface / border
          750: '#242428',
          700: '#2f2f35',
          600: '#46464e',
          500: '#64646e',
        },
        anime: {
          green: {
            DEFAULT: '#22c55e',
            moss: '#16a34a',
            forest: '#15803d',
            light: '#4ade80',
            glow: 'rgba(34, 197, 94, 0.35)',
          },
          yellow: {
            DEFAULT: '#facc15',
            amber: '#fbbf24',
            gold: '#eab308',
            wheat: '#fde047',
            glow: 'rgba(250, 204, 21, 0.35)',
          },
          cream: {
            DEFAULT: '#fbf8ee',
            parchment: '#f6f1df',
            pure: '#fdfbf7',
            soft: '#ede5cf',
            warm: '#dfd4b7',
            muted: '#b5aa8d',
            dark: '#7e7356',
          },
        },
      },
      fontFamily: {
        sans: ['Manrope', 'Inter', 'system-ui', '-apple-system', 'sans-serif'],
        display: ['Manrope', 'Inter', 'system-ui', 'sans-serif'],
        mono: ['DM Mono', 'JetBrains Mono', 'monospace'],
      },
      boxShadow: {
        'neo-glass': '0 12px 36px 0 rgba(0, 0, 0, 0.75)',
        'neo-glow-green': '0 0 35px -5px rgba(34, 197, 94, 0.4)',
        'neo-glow-yellow': '0 0 35px -5px rgba(250, 204, 21, 0.35)',
        'neo-glow-cream': '0 0 35px -5px rgba(251, 248, 238, 0.25)',
        'neo-card': '0 16px 40px -10px rgba(0, 0, 0, 0.85), inset 0 1px 0 0 rgba(255, 255, 255, 0.12)',
        'glass-inner': 'inset 0 1px 1px 0 rgba(255, 255, 255, 0.14)',
      },
      animation: {
        'pulse-slow': 'pulse 4s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'float': 'float 5s ease-in-out infinite',
        'float-alt': 'float-alt 6s ease-in-out infinite',
        'spin-slow': 'spin 22s linear infinite',
      },
      keyframes: {
        float: {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(-10px)' },
        },
        'float-alt': {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(10px)' },
        },
      },
    },
  },
  plugins: [],
};
