import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/modules/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      fontSize: {
        '2xs': '10px',
        'xs': '12px',
        'sm': '13px', // Estándar para tablas
      },
      colors: {
        'grid-header': '#f8f9fa',
        'grid-border': '#e2e8f0',
        'grid-selected': '#e3f2fd',
      },
      lineHeight: {
        'tight': '1.2',
        'snug': '1.3',
        'normal': '1.4',
      },
    },
  },
  plugins: [],
};

export default config;
