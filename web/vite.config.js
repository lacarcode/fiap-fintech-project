import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
  plugins: [react(), tailwindcss()],
  // strictPort: se a 5173 estiver ocupada, falha em vez de trocar de porta (o CORS da API só libera a 5173)
  server: { port: 5173, strictPort: true },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/setupTests.js',
  },
});
