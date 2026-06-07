import React, { useState } from 'react';
import { useInitialHandshake } from './hooks/useInitialHandshake';
import { MainLayout } from './components/layout/MainLayout';

// ── Módulo POS (dos paneles: catálogo + ticket de venta) ──────────────────────
import PuntoVenta from './components/PuntoVenta';

// ── Módulos de operaciones / análisis / gestión ───────────────────────────────
import PedidosCocina       from './views/PedidosCocina';
import ControlMesas        from './views/ControlMesas';
import MainDashboard       from './views/MainDashboard';
import ReportesFinancieros from './views/ReportesFinancieros';
import ClientesVIP         from './views/ClientesVIP';
import InventarioInsumos   from './views/InventarioInsumos';

import { Cat, Sparkles } from 'lucide-react';

function App() {
  const { loading, handshakeMessage } = useInitialHandshake();
  const [vistaActual, setVistaActual] = useState('pos');

  // ── Keyframe para la barra de progreso del handshake ─────────────────────
  const keyframeStyle = `
    @keyframes loading {
      0%   { transform: translateX(-100%); }
      100% { transform: translateX(200%); }
    }
  `;

  // ── 1. PANTALLA DE CARGA INICIAL (Hook Anti-F5 persistido en sessionStorage) ─
  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen bg-slate-950 text-slate-100 font-sans p-6 select-none">
        <style>{keyframeStyle}</style>
        <div className="relative flex flex-col items-center max-w-sm w-full text-center">

          {/* Logo animado */}
          <div className="relative mb-8">
            <div className="absolute inset-0 bg-amber-500/20 rounded-3xl filter blur-xl animate-pulse" />
            <div className="w-20 h-20 bg-gradient-to-tr from-orange-500 to-amber-500 rounded-3xl flex items-center justify-center shadow-lg shadow-orange-500/20 relative z-10 border border-amber-400/20">
              <Cat className="w-12 h-12 text-white animate-bounce" />
            </div>
          </div>

          {/* Título del sistema */}
          <h1 className="text-2xl font-black tracking-wide text-slate-100 mb-2">
            Catys Enterprise <span className="text-amber-500">ERP</span>
          </h1>
          <p className="text-xs text-slate-400 font-semibold uppercase tracking-widest mb-6">
            Iniciando Handshake de Seguridad
          </p>

          {/* Barra de progreso */}
          <div className="w-full h-1.5 bg-slate-900 rounded-full overflow-hidden mb-4 border border-slate-800">
            <div className="h-full bg-gradient-to-r from-orange-500 to-amber-500 rounded-full animate-[loading_1.5s_ease-in-out_infinite] w-2/3" />
          </div>

          {/* Mensaje dinámico de estado */}
          <div className="flex items-center justify-center gap-2 text-sm text-slate-300 font-medium">
            <Sparkles className="w-4 h-4 text-amber-500 animate-spin" />
            <span>{handshakeMessage}</span>
          </div>

          {/* Firma técnica */}
          <div className="absolute bottom-[-100px] text-[10px] text-slate-600 font-mono">
            SECURE HANDSHAKE // PORT: 443 // CATYS-SYS-v5
          </div>
        </div>
      </div>
    );
  }

  // ── 2. ROUTER DE VISTAS ───────────────────────────────────────────────────
  // Cada clave coincide con el id definido en MainLayout › menuItems.
  const renderVista = () => {
    switch (vistaActual) {
      case 'pos':        return <PuntoVenta />;
      case 'cocina':     return <PedidosCocina />;
      case 'mesas':      return <ControlMesas setVistaActual={setVistaActual} />;
      case 'resumen':    return <MainDashboard />;
      case 'reportes':   return <ReportesFinancieros />;
      case 'clientes':   return <ClientesVIP />;
      case 'inventario': return <InventarioInsumos />;
      default:           return <PuntoVenta />;
    }
  };

  // ── 3. APLICACIÓN PRINCIPAL ───────────────────────────────────────────────
  return (
    <MainLayout vistaActual={vistaActual} setVistaActual={setVistaActual}>
      {renderVista()}
    </MainLayout>
  );
}

export default App;
