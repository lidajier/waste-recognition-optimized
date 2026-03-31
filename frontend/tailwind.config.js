/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{vue,js,ts,jsx,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        display: ["Space Grotesk", "Noto Sans SC", "sans-serif"],
        body: ["Manrope", "Noto Sans SC", "sans-serif"]
      },
      boxShadow: {
        panel: "0 18px 40px rgba(0,0,0,0.22)"
      }
    }
  },
  plugins: []
};
