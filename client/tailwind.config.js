/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#000000',
        tertiary: '#666666',
        neutral: '#777777',
      },
      fontFamily: {
        sans: ['Geist', 'system-ui', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
