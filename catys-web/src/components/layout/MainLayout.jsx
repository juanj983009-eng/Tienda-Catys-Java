import React, { useState } from 'react';
import { ShoppingCart, Cat, ChefHat, Users, TrendingUp, LogOut, Grid, Boxes, LayoutDashboard, Menu, X, Wifi, ShieldAlert } from 'lucide-react';

export function MainLayout({ children, vistaActual, setVistaActual }) {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const menuItems = [
    {
      group: 'Operaciones',
      items: [
        { id: 'pos', name: 'Punto de Venta', icon: ShoppingCart },
        { id: 'cocina', name: 'Pedidos Cocina', icon: ChefHat },
        { id: 'mesas', name: 'Control de Mesas', icon: Grid },
      ],
    },
    {
      group: 'Análisis',
      items: [
        { id: 'resumen', name: 'Resumen General', icon: LayoutDashboard },
        { id: 'reportes', name: 'Reportes Financieros', icon: TrendingUp },
      ],
    },
    {
      group: 'Gestión',
      items: [
        { id: 'clientes', name: 'Clientes VIP', icon: Users },
        { id: 'inventario', name: 'Inventario & Insumos', icon: Boxes },
      ],
    },
  ];

  const handleNavClick = (id) => {
    setVistaActual(id);
    setMobileMenuOpen(false);
  };

  const getHeaderTitle = () => {
    switch (vistaActual) {
      case 'resumen': return 'Resumen de Operaciones';
      case 'pos': return 'Catálogo Visual & POS';
      case 'cocina': return 'Pedidos en Cocina';
      case 'mesas': return 'Control de Mesas';
      case 'reportes': return 'Reportes Financieros';
      case 'clientes': return 'Gestión de Clientes VIP';
      case 'inventario': return 'Inventario & Insumos';
      default: return 'Catys ERP';
    }
  };

  const getHeaderSubtitle = () => {
    switch (vistaActual) {
      case 'resumen': return 'Monitoreo en tiempo real de Tienda Catys';
      case 'pos': return 'Registra pedidos y descuenta stock al instante';
      case 'cocina': return 'Monitoreo de comandas y preparación de platos';
      case 'mesas': return 'Monitoreo y asignación de mesas en salón';
      case 'reportes': return 'Visualiza ingresos, egresos y auditorías de caja';
      case 'clientes': return 'Fidelización de clientes, historial de compras y puntos';
      case 'inventario': return 'Control de stock de ingredientes e insumos críticos';
      default: return 'Panel Administrativo';
    }
  };

  return (
    <div className="flex flex-col md:flex-row bg-slate-950 text-slate-100 min-h-screen font-sans antialiased overflow-hidden">
      
      {/* HEADER MÓVIL (Solo visible en pantallas pequeñas) */}
      <header className="md:hidden flex items-center justify-between px-5 py-4 bg-slate-900 border-b border-slate-800 shrink-0 z-20">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 bg-gradient-to-tr from-orange-500 to-amber-500 rounded-xl flex items-center justify-center shadow-md shadow-orange-500/10">
            <Cat className="w-5.5 h-5.5 text-white" />
          </div>
          <div>
            <h1 className="font-bold text-sm leading-tight text-slate-100 tracking-wide">Catys ERP</h1>
            <span className="text-[9px] text-orange-500 font-semibold tracking-widest uppercase">Mobile Panel</span>
          </div>
        </div>
        
        <button 
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)} 
          className="p-2 text-slate-400 hover:text-slate-200 transition-colors focus:outline-none"
          aria-label="Toggle menu"
        >
          {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
        </button>
      </header>

      {/* MENÚ LATERAL (SIDEBAR) */}
      {/* Escritorio: Fijo a la izquierda con scroll interno. Móvil: Superpuesto absolute/fixed */}
      <aside className={`
        fixed inset-y-0 left-0 z-30 w-72 bg-slate-900 border-r border-slate-800 flex flex-col transform transition-transform duration-300 ease-in-out shrink-0
        md:translate-x-0 md:static md:h-screen
        ${mobileMenuOpen ? 'translate-x-0' : '-translate-x-full'}
      `}>
        {/* Cabecera Sidebar (Escritorio) */}
        <div className="hidden md:flex p-6 border-b border-slate-800 items-center gap-3">
          <div className="w-10 h-10 bg-gradient-to-tr from-orange-500 to-amber-500 rounded-xl flex items-center justify-center shadow-md shadow-orange-500/10 shrink-0">
            <Cat className="w-6 h-6 text-white" />
          </div>
          <div>
            <h1 className="font-bold text-md leading-tight text-slate-100 tracking-wide">Catys ERP</h1>
            <span className="text-[10px] text-orange-500 font-semibold tracking-widest uppercase">Admin Panel</span>
          </div>
        </div>

        {/* Menú de Navegación */}
        <nav className="flex-1 p-5 space-y-6 overflow-y-auto custom-scrollbar">
          {menuItems.map((group) => (
            <div key={group.group} className="flex flex-col gap-1.5">
              <span className="text-[10px] font-bold tracking-wider text-slate-500 mb-1 px-3 uppercase">
                {group.group}
              </span>
              {group.items.map((item) => {
                const Icon = item.icon;
                const active = vistaActual === item.id;
                return (
                  <button
                    key={item.id}
                    onClick={() => handleNavClick(item.id)}
                    className={`w-full px-3.5 py-2.5 text-left text-sm font-semibold tracking-wide flex items-center gap-3 transition-all duration-200 border-l-4 ${
                      active
                        ? 'bg-slate-800/60 text-amber-400 border-amber-500 rounded-r-xl rounded-l-none'
                        : 'bg-transparent text-slate-400 border-transparent hover:bg-slate-800/40 hover:text-slate-200 rounded-xl'
                    }`}
                  >
                    <Icon className={`w-5 h-5 ${active ? 'text-amber-400' : 'text-slate-400'}`} />
                    {item.name}
                  </button>
                );
              })}
            </div>
          ))}
        </nav>

        {/* Pie Sidebar */}
        <div className="mt-auto border-t border-slate-800/60 bg-slate-900/80 backdrop-blur-sm p-4">
          <div className="flex items-center justify-between mb-3 px-1">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-full bg-gradient-to-tr from-amber-500 to-orange-600 flex items-center justify-center text-sm font-bold text-white shrink-0 shadow-md">
                JP
              </div>
              <div className="min-w-0">
                <p className="text-sm font-medium text-slate-200 leading-tight truncate">Juan José</p>
                <p className="text-xs text-slate-500 truncate">Administrador</p>
              </div>
            </div>
            <button 
              onClick={() => alert('Cerrando sesión...')}
              className="p-1.5 text-slate-500 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-all"
              title="Cerrar sesión"
            >
              <LogOut className="w-4.5 h-4.5" />
            </button>
          </div>
          
          <div className="text-[10px] text-slate-500 text-center font-medium border-t border-slate-850 pt-2 flex items-center justify-center gap-1.5">
            <div className="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse"></div>
            <span>ERP Web v5.0 — Mobile-First</span>
          </div>
        </div>
      </aside>

      {/* Overlay para cerrar el menú en móvil cuando se hace tap fuera */}
      {mobileMenuOpen && (
        <div 
          onClick={() => setMobileMenuOpen(false)} 
          className="fixed inset-0 bg-black/60 z-25 md:hidden backdrop-blur-xs"
        />
      )}

      {/* CONTENEDOR PRINCIPAL */}
      {/* Se adapta con scroll independiente en escritorio */}
      <div className="flex-1 flex flex-col h-[calc(100vh-69px)] md:h-screen overflow-hidden">
        
        {/* Encabezado Principal */}
        <header className="bg-slate-900/30 border-b border-slate-900/80 px-6 md:px-8 py-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 shrink-0">
          <div>
            <h2 className="text-xl md:text-2xl font-bold text-slate-100 tracking-wide">
              {getHeaderTitle()}
            </h2>
            <p className="text-xs text-slate-400 mt-1">
              {getHeaderSubtitle()}
            </p>
          </div>
          
          {/* Status indicators */}
          <div className="hidden sm:flex items-center gap-3 text-xs">
            <div className="flex items-center gap-1.5 px-3 py-1.5 bg-slate-900 rounded-lg border border-slate-800">
              <Wifi className="w-3.5 h-3.5 text-green-500" />
              <span className="text-slate-300 font-medium">En línea</span>
            </div>
            <div className="flex items-center gap-1.5 px-3 py-1.5 bg-amber-500/10 text-amber-400 rounded-lg border border-amber-500/20">
              <ShieldAlert className="w-3.5 h-3.5" />
              <span className="font-semibold">Modo POS</span>
            </div>
          </div>
        </header>

        {/* CONTENIDO PRINCIPAL (Workspace con scroll independiente) */}
        <main className="flex-1 overflow-y-auto p-6 md:p-8 bg-slate-950 custom-scrollbar">
          {children}
        </main>
      </div>
    </div>
  );
}
