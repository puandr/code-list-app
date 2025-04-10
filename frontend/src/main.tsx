import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App'; 
import './index.css'; 
import { AuthProvider } from "react-oidc-context";
import { oidcConfig } from './config/oidcConfig'; 
import { BrowserRouter } from 'react-router-dom';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    {/* Provide OIDC configuration and wrap the app */}
    <AuthProvider {...oidcConfig}>
      {/* BrowserRouter needed for routing */}
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </AuthProvider>
  </React.StrictMode>,
);