import React, { useState } from 'react';
import { Users, Clock, DollarSign, ArrowRight, Layers } from 'lucide-react';

function ControlMesas({ setVistaActual }) {
  const [filtroZona, setFiltroZona] = useState('TODAS');
  const [mesas, setMesas] = useState([
    { id: 1, numero: "Mesa 1", capacidad: 4, estado: "disponible", zona: "Salón Principal" },
    { id: 2, numero: "Mesa 2", capacidad: 2, estado: "ocupada", tiempo: "32 min", consumo: 45.00, zona: "Salón Principal" },
    { id: 3, numero: "Mesa 3", capacidad: 6, estado: "disponible", zona: "Salón Principal" },
    { id: 4, numero: "Mesa 4", capacidad: 4, estado: "ocupada", tiempo: "45 min", consumo: 128.50, zona: "Salón Principal" },
    { id: 5, numero: "Barra 1", capacidad: 1, estado: "cuenta", tiempo: "15 min", consumo: 18.00, zona: "Barra" },
    { id: 6, numero: "Barra 2", capacidad: 1, estado: "disponible", zona: "Barra" },
    { id: 7, numero: "Mesa T1", capacidad: 4, estado: "disponible", zona: "Terraza" },
    { id: 8, numero: "Mesa T2", capacidad: 4, estado: "ocupada", tiempo: "12 min", consumo: 62.00, zona: "Terraza" },
    { id: 9, numero: "Mesa T3", capacidad: 2, estado: "disponible", zona: "Terraza" }
  ]);

  // Cicla el estado de la mesa: Disponible -> Ocupada -> Cuenta -> Disponible
  const cambiarEstadoMesa = (id) => {
    setMesas((prevMesas) =>
      prevMesas.map((m) => {
        if (m.id === id) {
          if (m.estado === 'disponible') {
            return { ...m, estado: 'ocupada', tiempo: '5 min', consumo: 35.00 };
          }
          if (m.estado === 'ocupada') {
            return { ...m, estado: 'cuenta', tiempo: '15 min' };
          }
          if (m.estado === 'cuenta') {
            return { ...m, estado: 'disponible', tiempo: undefined, consumo: undefined };
          }
        }
        return m;
      })
    );
  };

  const zonas = ['TODAS', 'Salón Principal', 'Terraza', 'Barra'];

  const mesasFiltradas = filtroZona === 'TODAS'
    ? mesas
    : mesas.filter((m) => m.zona.toLowerCase() === filtroZona.toLowerCase());

  // Contadores para resúmenes
  const totalMesas = mesas.length;
  const disponibles = mesas.filter((m) => m.estado === 'disponible').length;
  const ocupadas = mesas.filter((m) => m.estado === 'ocupada').length;
  const cuentas = mesas.filter((m) => m.estado === 'cuenta').length;

  return (
    <div className="h-full flex flex-col overflow-hidden">
      {/* 1. MÁSCARA SUPERIOR DE MÉTRICAS */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6 shrink-0">
        <div className="bg-gradient-to-b from-slate-500/5 to-slate-900/40 border border-slate-800/80 rounded-2xl p-4 flex flex-col shadow-sm">
          <span className="text-[10px] text-slate-500 font-bold uppercase tracking-wider">Total Salón</span>
          <span className="text-xl font-bold text-slate-200 mt-1">{totalMesas} Mesas</span>
        </div>
        <div className="bg-gradient-to-b from-emerald-500/5 to-slate-900/40 border border-slate-800/80 rounded-2xl p-4 flex flex-col shadow-sm">
          <span className="text-[10px] text-emerald-500/80 font-bold uppercase tracking-wider">Disponibles</span>
          <span className="text-xl font-bold text-emerald-400 mt-1">{disponibles} Libres</span>
        </div>
        <div className="bg-gradient-to-b from-orange-500/5 to-slate-900/40 border border-slate-800/80 rounded-2xl p-4 flex flex-col shadow-sm">
          <span className="text-[10px] text-red-500/80 font-bold uppercase tracking-wider">Ocupadas</span>
          <span className="text-xl font-bold text-red-400 mt-1">{ocupadas} Mesas</span>
        </div>
        <div className="bg-gradient-to-b from-amber-500/5 to-slate-900/40 border border-slate-800/80 rounded-2xl p-4 flex flex-col shadow-sm">
          <span className="text-[10px] text-amber-500/80 font-bold uppercase tracking-wider">Pidiendo Cuenta</span>
          <span className="text-xl font-bold text-amber-400 mt-1">{cuentas} Checkouts</span>
        </div>
      </div>

      {/* 2. PESTAÑAS DE FILTRADO */}
      <div className="flex justify-between items-center pb-4 border-b border-slate-800 shrink-0 mb-6 gap-4 overflow-x-auto">
        <div className="flex gap-2">
          {zonas.map((z) => (
            <button
              key={z}
              onClick={() => setFiltroZona(z)}
              className={`px-4 py-2 rounded-xl text-xs font-semibold uppercase tracking-wider transition-all whitespace-nowrap ${
                filtroZona === z
                  ? 'bg-amber-500 text-slate-950 shadow-lg shadow-amber-500/20'
                  : 'bg-slate-900 border border-slate-800 text-slate-400 hover:text-slate-200 hover:border-slate-700'
              }`}
            >
              {z}
            </button>
          ))}
        </div>

        {/* Acceso rápido a comanda */}
        <button
          onClick={() => setVistaActual('pos')}
          className="px-4 py-2 bg-slate-900 hover:bg-slate-850 border border-slate-800 hover:border-slate-700 rounded-xl text-xs font-bold text-slate-300 transition-all flex items-center gap-1.5 whitespace-nowrap hover:text-amber-500 group shrink-0 active:scale-95 transition-transform"
        >
          Nueva Venta
          <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-0.5 transition-transform" />
        </button>
      </div>

      {/* 3. REJILLA DE MESAS */}
      <div className="flex-1 overflow-y-auto pr-1 pb-6">
        <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
          {mesasFiltradas.map((mesa) => {
            const esDisponible = mesa.estado === 'disponible';
            const esOcupada = mesa.estado === 'ocupada';
            const esCuenta = mesa.estado === 'cuenta';

            // Clasificación dinámica de estilos de tarjeta
            let cardClasses = "bg-slate-900/40 backdrop-blur-md border border-slate-800 rounded-2xl p-5 transition-all duration-300 flex flex-col justify-between h-36 cursor-pointer relative group overflow-hidden";
            if (esDisponible) {
              cardClasses += " hover:border-emerald-500/30 hover:shadow-[0_0_15px_rgba(16,185,129,0.05)]";
            } else if (esOcupada || esCuenta) {
              cardClasses += " hover:-translate-y-1 hover:border-orange-500/40 hover:shadow-[0_0_20px_rgba(249,115,22,0.12)]";
            }

            return (
              <div
                key={mesa.id}
                onClick={() => cambiarEstadoMesa(mesa.id)}
                className={cardClasses}
              >
                {/* Cabecera Tarjeta: Número e Icono Capacidad */}
                <div className="flex justify-between items-start z-10">
                  <div>
                    <span className="font-bold text-slate-100 text-sm tracking-wide block">{mesa.numero}</span>
                    <span className="text-[10px] text-slate-500 font-semibold tracking-widest uppercase block mt-0.5">
                      {mesa.zona}
                    </span>
                  </div>
                  <div className="flex items-center gap-1 bg-slate-950/60 border border-slate-850 px-1.5 py-0.5 rounded-md shrink-0">
                    <Users className="w-3 h-3 text-slate-500" />
                    <span className="text-[10px] font-bold text-slate-400">{mesa.capacidad}</span>
                  </div>
                </div>

                {/* Pie Tarjeta: Badges Operativos de Estado */}
                <div className="mt-4 z-10">
                  {esDisponible && (
                    <span className="inline-flex items-center justify-center px-2.5 py-1 text-[9px] font-bold uppercase tracking-wider text-emerald-400 bg-emerald-500/10 border border-emerald-500/15 rounded-md">
                      Disponible
                    </span>
                  )}
                  {esOcupada && (
                    <div className="space-y-2">
                      <div className="flex items-center gap-1.5 text-[9px] text-slate-400 font-medium">
                        <Clock className="w-3 h-3 text-red-400" />
                        <span>Hace {mesa.tiempo}</span>
                        <span className="flex h-2 w-2 relative ml-0.5">
                          <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75" />
                          <span className="relative inline-flex rounded-full h-2 w-2 bg-amber-500" />
                        </span>
                      </div>
                      <span className="inline-flex items-center justify-center px-2.5 py-1 text-[9px] font-bold uppercase tracking-wider text-orange-500 bg-slate-950 border border-slate-800/80 rounded-full shadow-inner">
                        Consumo: S/ {mesa.consumo.toFixed(2)}
                      </span>
                    </div>
                  )}
                  {esCuenta && (
                    <div className="space-y-2">
                      <div className="flex items-center gap-1.5 text-[9px] text-slate-400 font-medium">
                        <Clock className="w-3 h-3 text-amber-400 animate-pulse" />
                        <span>Solicitado</span>
                      </div>
                      <span className="inline-flex items-center justify-center px-2.5 py-1 text-[9px] font-bold uppercase tracking-wider text-amber-400 bg-slate-950 border border-slate-800 rounded-full shadow-inner animate-pulse">
                        Cuenta: S/ {mesa.consumo.toFixed(2)}
                      </span>
                    </div>
                  )}
                </div>

                {/* Overlay de Ayuda visual rápida (Hover) */}
                <div className={`absolute inset-0 opacity-0 group-hover:opacity-100 rounded-2xl pointer-events-none transition-opacity duration-300 ${
                  esDisponible ? 'bg-emerald-500/5' : 'bg-orange-500/5'
                }`} />
              </div>
            );
          })}
        </div>

        {mesasFiltradas.length === 0 && (
          <div className="text-center py-20 text-slate-500 text-xs border border-dashed border-slate-800 rounded-2xl mt-4">
            No hay mesas registradas en esta zona.
          </div>
        )}
      </div>
    </div>
  );
}

export default ControlMesas;
