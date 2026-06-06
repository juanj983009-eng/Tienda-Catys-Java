import React, { useState } from 'react';
import { ShoppingCart, Cat, ChefHat, Users, TrendingUp, LogOut, Grid, Boxes, LayoutDashboard } from 'lucide-react';
import PuntoVenta from './components/PuntoVenta';
import PedidosCocina from './views/PedidosCocina';
import ControlMesas from './views/ControlMesas';
import ReportesFinancieros from './views/ReportesFinancieros';
import ClientesVIP from './views/ClientesVIP';
import InventarioInsumos from './views/InventarioInsumos';
import ResumenGeneral from './views/ResumenGeneral';


function App() {
  const [vistaActual, setVistaActual] = useState('resumen');
  const logError = (msg) => {
    console.error(msg);
  };

  return (
    <div className="flex bg-slate-950 text-slate-100 min-h-screen font-sans print:bg-white print:text-black print:min-h-0 print:block">
      {/* Barra Lateral / Sidebar */}
      <aside className="print:hidden w-64 bg-slate-900 border-r border-slate-800 flex flex-col shrink-0">
        {/* Cabecera Sidebar */}
        <div className="p-6 border-b border-slate-800 flex items-center gap-3">
          <div className="w-10 h-10 bg-gradient-to-tr from-orange-500 to-amber-500 rounded-xl flex items-center justify-center shadow-md shadow-orange-500/10 shrink-0">
            <Cat className="w-6 h-6 text-white" />
          </div>
          <div>
            <h1 className="font-bold text-md leading-tight text-slate-100 tracking-wide">Catys ERP</h1>
            <span className="text-[10px] text-orange-500 font-semibold tracking-widest uppercase">Admin Panel</span>
          </div>
        </div>

        {/* Menú de Navegación */}
        <nav className="flex-1 p-4 space-y-1.5 overflow-y-auto pr-1">
          {/* Grupo 1: OPERACIONES */}
          <div className="flex flex-col">
            <span className="text-xs font-semibold tracking-wider text-slate-500 mb-2 px-4 uppercase">
              Operaciones
            </span>
            <button
              onClick={() => setVistaActual('pos')}
              className={`w-full px-4 py-3 text-left text-sm font-semibold tracking-wide flex items-center gap-3 transition-all duration-200 border-l-4 ${
                vistaActual === 'pos'
                  ? 'bg-slate-800/60 text-slate-100 border-amber-500 rounded-r-xl rounded-l-none'
                  : 'bg-transparent text-slate-400 border-transparent hover:bg-slate-800/40 hover:text-slate-200 rounded-xl'
              }`}
            >
              <ShoppingCart className="w-5 h-5" />
              Punto de Venta
            </button>
            <button
              onClick={() => setVistaActual('cocina')}
              className={`w-full px-4 py-3 text-left text-sm font-semibold tracking-wide flex items-center gap-3 transition-all duration-200 border-l-4 ${
                vistaActual === 'cocina'
                  ? 'bg-slate-800/60 text-slate-100 border-amber-500 rounded-r-xl rounded-l-none'
                  : 'bg-transparent text-slate-400 border-transparent hover:bg-slate-800/40 hover:text-slate-200 rounded-xl'
              }`}
            >
              <ChefHat className="w-5 h-5" />
              Pedidos Cocina
            </button>
            <button
              onClick={() => setVistaActual('mesas')}
              className={`w-full px-4 py-3 text-left text-sm font-semibold tracking-wide flex items-center gap-3 transition-all duration-200 border-l-4 ${
                vistaActual === 'mesas'
                  ? 'bg-slate-800/60 text-slate-100 border-amber-500 rounded-r-xl rounded-l-none'
                  : 'bg-transparent text-slate-400 border-transparent hover:bg-slate-800/40 hover:text-slate-200 rounded-xl'
              }`}
            >
              <Grid className="w-5 h-5" />
              Control de Mesas
            </button>
          </div>

          {/* Grupo 2: ANÁLISIS */}
          <div className="flex flex-col">
            <span className="text-xs font-semibold tracking-wider text-slate-500 mt-6 mb-2 px-4 uppercase">
              Análisis
            </span>
            <button
              onClick={() => setVistaActual('resumen')}
              className={`w-full px-4 py-3 text-left text-sm font-semibold tracking-wide flex items-center gap-3 transition-all duration-200 border-l-4 ${
                vistaActual === 'resumen'
                  ? 'bg-slate-800/60 text-slate-100 border-amber-500 rounded-r-xl rounded-l-none'
                  : 'bg-transparent text-slate-400 border-transparent hover:bg-slate-800/40 hover:text-slate-200 rounded-xl'
              }`}
            >
              <LayoutDashboard className="w-5 h-5" />
              Resumen General
            </button>
            <button
              onClick={() => setVistaActual('reportes')}
              className={`w-full px-4 py-3 text-left text-sm font-semibold tracking-wide flex items-center gap-3 transition-all duration-200 border-l-4 ${
                vistaActual === 'reportes'
                  ? 'bg-slate-800/60 text-slate-100 border-amber-500 rounded-r-xl rounded-l-none'
                  : 'bg-transparent text-slate-400 border-transparent hover:bg-slate-800/40 hover:text-slate-200 rounded-xl'
              }`}
            >
              <TrendingUp className="w-5 h-5" />
              Reportes Financieros
            </button>
          </div>

          {/* Grupo 3: GESTIÓN */}
          <div className="flex flex-col">
            <span className="text-xs font-semibold tracking-wider text-slate-500 mt-6 mb-2 px-4 uppercase">
              Gestión
            </span>
            <button
              onClick={() => setVistaActual('clientes')}
              className={`w-full px-4 py-3 text-left text-sm font-semibold tracking-wide flex items-center gap-3 transition-all duration-200 border-l-4 ${
                vistaActual === 'clientes'
                  ? 'bg-slate-800/60 text-slate-100 border-amber-500 rounded-r-xl rounded-l-none'
                  : 'bg-transparent text-slate-400 border-transparent hover:bg-slate-800/40 hover:text-slate-200 rounded-xl'
              }`}
            >
              <Users className="w-5 h-5" />
              Clientes VIP
            </button>
            <button
              onClick={() => setVistaActual('inventario')}
              className={`w-full px-4 py-3 text-left text-sm font-semibold tracking-wide flex items-center gap-3 transition-all duration-200 border-l-4 ${
                vistaActual === 'inventario'
                  ? 'bg-slate-800/60 text-slate-100 border-amber-500 rounded-r-xl rounded-l-none'
                  : 'bg-transparent text-slate-400 border-transparent hover:bg-slate-800/40 hover:text-slate-200 rounded-xl'
              }`}
            >
              <Boxes className="w-5 h-5" />
              Inventario &amp; Insumos
            </button>
          </div>
        </nav>

        {/* Pie Sidebar */}
        <div className="mt-auto border-t border-slate-800/60 pt-2">
          {/* Perfil de Operario */}
          <div className="flex items-center justify-between px-4 py-3 mb-2">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-full bg-gradient-to-tr from-amber-500 to-orange-600 flex items-center justify-center text-sm font-bold text-white shrink-0 shadow-md">
                JP
              </div>
              <div className="min-w-0">
                <p className="text-sm font-medium text-slate-200 leading-tight truncate">Juan José</p>
                <p className="text-xs text-slate-500 truncate">Administrador</p>
              </div>
            </div>
            <LogOut className="w-4 h-4 text-slate-500 hover:text-red-400 cursor-pointer transition-colors shrink-0" />
          </div>
          
          <div className="p-4 border-t border-slate-800/40 text-[10px] text-slate-500 text-center font-medium">
            ERP Web v5.0 — NextGen UI
          </div>
        </div>
      </aside>

      {/* Panel Principal */}
      <main className="print:p-0 print:h-auto print:overflow-visible flex-1 p-8 flex flex-col h-screen overflow-hidden">
        {/* Encabezado */}
        <header className="print:hidden flex justify-between items-center mb-8 shrink-0">
          <div>
            <h2 className="text-xl font-bold text-slate-100 tracking-wide">
              {vistaActual === 'resumen' && 'Resumen de Operaciones'}
              {vistaActual === 'pos' && 'Catálogo Visual & POS'}
              {vistaActual === 'cocina' && 'Pedidos en Cocina'}
              {vistaActual === 'mesas' && 'Control de Mesas'}
              {vistaActual === 'reportes' && 'Reportes Financieros'}
              {vistaActual === 'clientes' && 'Gestión de Clientes VIP'}
              {vistaActual === 'inventario' && 'Inventario & Insumos'}
            </h2>
            <p className="text-xs text-slate-550 mt-1">
              {vistaActual === 'resumen' && 'Monitoreo en tiempo real de Tienda Catys'}
              {vistaActual === 'pos' && 'Registra pedidos y descuenta stock al instante'}
              {vistaActual === 'cocina' && 'Monitoreo de comandas y preparación de platos'}
              {vistaActual === 'mesas' && 'Monitoreo y asignación de mesas en salón'}
              {vistaActual === 'reportes' && 'Visualiza ingresos, egresos y auditorías de caja'}
              {vistaActual === 'clientes' && 'Fidelización de clientes, historial de compras y puntos'}
              {vistaActual === 'inventario' && 'Control de stock de ingredientes e insumos críticos'}
            </p>
          </div>
        </header>

        {/* Contenido Condicional */}
        <div className="flex-1 overflow-hidden">
          {vistaActual === 'resumen' && (
            <ResumenGeneral />
          )}

          {vistaActual === 'pos' && (
            <PuntoVenta />
          )}

          {vistaActual === 'cocina' && (
            <PedidosCocina />
          )}

          {vistaActual === 'reportes' && (
            <ReportesFinancieros />
          )}

          {vistaActual === 'clientes' && (
            <ClientesVIP />
          )}

          {vistaActual === 'mesas' && (
            <ControlMesas setVistaActual={setVistaActual} />
          )}

          {vistaActual === 'inventario' && (
            <InventarioInsumos />
          )}
        </div>
      </main>
    </div>
  );
}

export default App;
