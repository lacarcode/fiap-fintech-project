import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';

// Provisório: a F02 troca por <RouterProvider />
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <p className="p-8 text-brand">Fintech — em construção</p>
  </StrictMode>,
);
