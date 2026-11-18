/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        border: "hsl(var(--border, 217 19% 27%))",
        background: "hsl(var(--background, 222 47% 11%))",
        foreground: "hsl(var(--foreground, 210 40% 98%))",
        card: "hsl(var(--card, 224 71% 4%))",
        "card-foreground": "hsl(var(--card-foreground, 210 40% 98%))",
        primary: {
          DEFAULT: "hsl(var(--primary, 217 91% 60%))",
          foreground: "hsl(var(--primary-foreground, 222 47% 11%))",
        },
        muted: {
          DEFAULT: "hsl(var(--muted, 217 19% 27%))",
          foreground: "hsl(var(--muted-foreground, 215 20.2% 65.1%))",
        },
        accent: {
          DEFAULT: "hsl(var(--accent, 217 19% 27%))",
          foreground: "hsl(var(--accent-foreground, 210 40% 98%))",
        },
      },
      fontFamily: {
        sans: ["Inter", "-apple-system", "BlinkMacSystemFont", "Segoe UI", "Roboto", "sans-serif"],
        mono: ["JetBrains Mono", "Fira Code", "Courier New", "monospace"],
      },
    },
  },
  plugins: [],
}
