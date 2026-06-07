import React, { useState, useEffect } from 'react';
import { DollarSign, ClipboardList, Users, AlertTriangle, Activity, ChefHat, Grid, ShoppingCart, TrendingUp, TrendingDown } from 'lucide-react';
import { API_BASE_URL } from '../config/api';

function MainDashboard() {
  const [estadoEnVivo, setEstadoEnVivo] = useState({
    kpis: { ventasHoy: 0, ordenesHoy: 0, clientesVip: 0 },
    cocina: { enCola: 0, enPreparacion: 0 },
    mesas: { ocupadas: 0, disponibles: 0 },
    alertasStock: []
  });
  const [mesas, setMesas] = useState([]);
  const [loading, setLoading] = useState(true);

  const cargarMetricas = async () => {
    try {
      const [resResumen, resMesas] = await Promise.all([
        fetch(`${API_BASE_URL}/dashboard/resumen`),
        fetch(`${API_BASE_URL}/mesas`)
      ]);
      
      if (!resResumen.ok) throw new Error('Error al obtener métricas');
      if (!resMesas.ok) throw new Error('Error al obtener mesas');
      
      const dataResumen = await resResumen.json();
      const dataMesas = await resMesas.json();
      
      setEstadoEnVivo(dataResumen);
      setMesas(dataMesas);
    } catch (err) {
      console.error("Error al cargar métricas del dashboard:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarMetricas();
    const interval = setInterval(cargarMetricas, 5000);
    return () => clearInterval(interval);
  }, []);

  const safeMesas = Array.isArray(mesas) ? mesas : [];
  const ocupadasCount = safeMesas.filter(m => m && m.estado === 'ocupada').length;
  const checkoutsCount = safeMesas.filter(m => m && m.estado === 'cuenta').length;
  const disponiblesCount = safeMesas.filter(m => m && m.estado === 'disponible').length;
  const totalMesasCount = safeMesas.length;
  const ocupacionPorcentaje = totalMesasCount > 0 
    ? Math.round(((ocupadasCount + checkoutsCount) / totalMesasCount) * 100) 
    : 0;

  const totalCocina = (estadoEnVivo?.cocina?.enCola || 0) + (estadoEnVivo?.cocina?.enPreparacion || 0);

  // ── KEYFRAMES Y DISEÑO DE INTERACCIONES FLUIDAS ──
  const dashboardStyles = `
    @keyframes dash {
      to {
        stroke-dashoffset: 0;
      }
    }
    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(10px); }
      to { opacity: 1; transform: translateY(0); }
    }
  `;

  // ── SKELETON LOADING STATE ──
  if (loading) {
    return (
      <div className="space-y-6 h-full overflow-hidden pb-8 pr-1 animate-pulse text-slate-100">
        <style>{dashboardStyles}</style>
        {/* 1. KPIs Skeletons */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {[1, 2, 3].map((i) => (
            <div key={i} className="bg-slate-900/40 border border-slate-800/80 p-6 rounded-2xl flex justify-between items-center h-28">
              <div className="space-y-2.5 w-2/3">
                <div className="h-2.5 bg-slate-800/60 rounded w-1/2" />
                <div className="h-6 bg-slate-800/60 rounded w-3/4" />
                <div className="h-2 bg-slate-800/60 rounded w-1/3" />
              </div>
              <div className="w-12 h-12 bg-slate-800/50 rounded-full" />
            </div>
          ))}
        </div>

        {/* 2. Block Skeletons */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Left Panel Skeleton */}
          <div className="bg-slate-900/40 border border-slate-800/80 p-6 rounded-2xl h-80 flex flex-col justify-between">
            <div>
              <div className="h-4 bg-slate-800/60 rounded w-1/3 mb-6" />
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="bg-slate-800/30 rounded-xl p-4 h-32 flex flex-col justify-between">
                  <div className="h-3 bg-slate-800/50 rounded w-1/2" />
                  <div className="h-8 bg-slate-800/50 rounded w-1/3" />
                  <div className="h-2 bg-slate-800/50 rounded w-2/3" />
                </div>
                <div className="bg-slate-800/30 rounded-xl p-4 h-32 flex flex-col justify-between">
                  <div className="h-3 bg-slate-800/50 rounded w-1/2" />
                  <div className="h-8 bg-slate-800/50 rounded w-1/3" />
                  <div className="h-2 bg-slate-800/50 rounded w-2/3" />
                </div>
              </div>
            </div>
            <div className="h-2.5 bg-slate-800/40 rounded w-2/3 mt-4" />
          </div>

          {/* Right Panel Skeleton */}
          <div className="bg-slate-900/40 border border-slate-800/80 p-6 rounded-2xl h-80 flex flex-col">
            <div className="h-4 bg-slate-800/60 rounded w-1/3 mb-6" />
            <div className="space-y-3 flex-1 flex flex-col justify-center items-center">
              <div className="w-16 h-16 bg-slate-800/40 rounded-full mb-3" />
              <div className="h-3 bg-slate-800/40 rounded w-1/4 mb-1" />
              <div className="h-2 bg-slate-800/40 rounded w-1/3" />
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 h-full overflow-y-auto pb-8 pr-1 text-slate-100 animate-[fadeIn_0.5s_ease-out]">
      <style>{dashboardStyles}</style>
      
      {/* 1. FILA SUPERIOR: KPIs */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        
        {/* KPI Ventas */}
        <div 
          className="bg-slate-900/40 backdrop-blur-md border border-slate-800 p-6 rounded-2xl transition-all duration-300 hover:border-orange-500/20 hover:shadow-[0_0_20px_rgba(249,115,22,0.08)] flex justify-between items-center group"
        >
          <div className="space-y-1.5 flex-1">
            <span className="text-[10px] text-slate-500 font-bold uppercase tracking-wider block">Ventas de Hoy</span>
            
            <div className="flex items-center gap-3">
              <h3 className="text-2xl font-bold text-slate-100 font-mono tracking-tight">
                S/ {Number(estadoEnVivo?.kpis?.ventasHoy || 0).toFixed(2)}
              </h3>
              
              {/* Mini-sparkline SVG de tendencia */}
              <div className="h-6 flex items-end">
                <svg className="w-14 h-5 text-amber-500 stroke-current drop-shadow-[0_0_6px_rgba(245,158,11,0.4)]" viewBox="0 0 100 30" fill="none" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
                  <path 
                    d="M5 25 Q 25 10, 45 22 T 85 8 T 95 12" 
                    strokeDasharray="120"
                    strokeDashoffset="120"
                    className="animate-[dash_1.5s_ease-in-out_forwards]"
                  />
                </svg>
              </div>
            </div>

            <p className="text-[10px] text-emerald-400 font-bold flex items-center gap-1 group-hover:-translate-y-0.5 transition-transform duration-300">
              <TrendingUp className="w-3.5 h-3.5 text-emerald-400" />
              <span>+12.4% vs ayer</span>
            </p>
          </div>
          <div className="bg-gradient-to-br from-orange-500/10 to-transparent p-3 rounded-full border border-orange-500/10 text-orange-500 shrink-0">
            <DollarSign className="w-6 h-6" />
          </div>
        </div>

        {/* KPI Órdenes */}
        <div 
          className="bg-slate-900/40 backdrop-blur-md border border-slate-800 p-6 rounded-2xl transition-all duration-300 hover:border-orange-500/20 hover:shadow-[0_0_20px_rgba(249,115,22,0.08)] flex justify-between items-center group"
        >
          <div className="space-y-1.5 flex-1">
            <span className="text-[10px] text-slate-500 font-bold uppercase tracking-wider block">Órdenes Procesadas</span>
            <h3 className="text-2xl font-bold text-slate-100">
              {estadoEnVivo?.kpis?.ordenesHoy || 0} Pedidos
            </h3>
            <p className="text-[10px] text-emerald-400 font-bold flex items-center gap-1 group-hover:-translate-y-0.5 transition-transform duration-300">
              <TrendingUp className="w-3.5 h-3.5 text-emerald-400" />
              <span>+8.2% vs ayer</span>
            </p>
          </div>
          <div className="bg-gradient-to-br from-orange-500/10 to-transparent p-3 rounded-full border border-orange-500/10 text-orange-500 shrink-0">
            <ClipboardList className="w-6 h-6" />
          </div>
        </div>

        {/* KPI Clientes VIP */}
        <div 
          className="bg-slate-900/40 backdrop-blur-md border border-slate-800 p-6 rounded-2xl transition-all duration-300 hover:border-orange-500/20 hover:shadow-[0_0_20px_rgba(249,115,22,0.08)] flex justify-between items-center group"
        >
          <div className="space-y-1.5 flex-1">
            <span className="text-[10px] text-slate-500 font-bold uppercase tracking-wider block">Clientes VIP del Turno</span>
            <h3 className="text-2xl font-bold text-slate-100">
              {estadoEnVivo?.kpis?.clientesVip || 0} Frecuentes
            </h3>
            <p className="text-[10px] text-red-500 font-bold flex items-center gap-1 group-hover:-translate-y-0.5 transition-transform duration-300">
              <TrendingDown className="w-3.5 h-3.5 text-red-500" />
              <span>-2.1% vs ayer</span>
            </p>
          </div>
          <div className="bg-gradient-to-br from-orange-500/10 to-transparent p-3 rounded-full border border-orange-500/10 text-orange-500 shrink-0">
            <Users className="w-6 h-6" />
          </div>
        </div>

      </div>

      {/* 2. BLOQUE CENTRAL: ESTADO OPERATIVO & ABASTECIMIENTO */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        
        {/* Panel Izquierdo: Estado Operativo */}
        <div 
          className="bg-slate-900/40 backdrop-blur-md border border-slate-800 p-6 rounded-2xl transition-all duration-300 hover:border-orange-500/20 hover:shadow-[0_0_20px_rgba(249,115,22,0.08)] flex flex-col justify-between"
        >
          <div className="border-b border-slate-800 pb-4 mb-6 flex items-center gap-2">
            <Activity className="w-4 h-4 text-orange-500 animate-pulse" />
            <h3 className="text-xs font-bold text-slate-200 uppercase tracking-wider">
              Monitor de Operación Salón & Cocina
            </h3>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            
            {/* Cocina Mini-Container */}
            <div className="border border-orange-500/15 bg-slate-950/45 p-4 rounded-xl flex flex-col justify-between h-32 hover:border-orange-500/35 transition-colors duration-300">
              <div className="flex items-center justify-between">
                <span className="text-[10px] text-orange-400 font-bold uppercase tracking-wider">Cocina en Vivo</span>
                <ChefHat className="w-4 h-4 text-orange-500" />
              </div>
              <div className="mt-2 flex items-center gap-2">
                <span className="text-3xl font-extrabold text-slate-100">{totalCocina}</span>
                <span className="text-xs text-slate-400 font-medium">pedidos</span>
                {/* Radar Pulse Indicator */}
                <span className="relative flex h-2 w-2">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-orange-500 opacity-75" />
                  <span className="relative inline-flex rounded-full h-2 w-2 bg-orange-600" />
                </span>
              </div>
              <div className="mt-2 text-[10px] text-slate-400 flex flex-col gap-0.5 font-medium">
                <span>• {estadoEnVivo?.cocina?.enCola || 0} comandas en cola</span>
                <span>• {estadoEnVivo?.cocina?.enPreparacion || 0} en preparación</span>
              </div>
            </div>

            {/* Mesas Mini-Container */}
            <div className="border border-orange-500/15 bg-slate-950/45 p-4 rounded-xl flex flex-col justify-between h-32 hover:border-orange-500/35 transition-colors duration-300">
              <div className="flex items-center justify-between">
                <span className="text-[10px] text-orange-400 font-bold uppercase tracking-wider">Mesas Activas</span>
                <Grid className="w-4 h-4 text-orange-500" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <span className="text-3xl font-extrabold text-slate-100">{ocupacionPorcentaje}%</span>
                  <span className="text-xs text-slate-400 font-medium">ocupación</span>
                  {/* Radar Pulse Indicator */}
                  <span className="relative flex h-2 w-2">
                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-orange-500 opacity-75" />
                    <span className="relative inline-flex rounded-full h-2 w-2 bg-orange-600" />
                  </span>
                </div>
                {/* Barra de progreso de ocupación */}
                <div className="w-full bg-slate-900 rounded-full h-1.5 mt-2 overflow-hidden border border-slate-800/80">
                  <div 
                    className="bg-gradient-to-r from-orange-500 to-amber-500 h-1.5 rounded-full transition-all duration-1000 ease-out" 
                    style={{ width: `${ocupacionPorcentaje}%` }} 
                  />
                </div>
              </div>
              <div className="text-[10px] text-slate-400 flex flex-col gap-0.5 font-medium">
                <span>• {ocupadasCount} ocupadas en salón</span>
                <span>• {disponiblesCount} disponibles</span>
              </div>
            </div>

          </div>

          <div className="mt-6 text-[10px] text-slate-500 font-medium">
            Los datos se refrescan de forma automática con cada actualización del POS y del tablero de mesas.
          </div>
        </div>

        {/* Panel Derecho: Alertas de Abastecimiento */}
        <div 
          className="bg-slate-900/40 backdrop-blur-md border border-slate-800 p-6 rounded-2xl transition-all duration-300 hover:border-orange-500/20 hover:shadow-[0_0_20px_rgba(249,115,22,0.08)] flex flex-col"
        >
          <div className="border-b border-slate-800 pb-4 mb-4 flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 text-orange-500" />
            <h3 className="text-xs font-bold text-slate-200 uppercase tracking-wider">
              Alertas de Abastecimiento Crítico
            </h3>
          </div>

          <div className="flex-1 flex flex-col justify-center">
            {(estadoEnVivo?.alertasStock || []).length === 0 ? (
              /* ELEGANT EMPTY STATE WITH LATENT SCANNER PULSE */
              <div className="flex flex-col items-center justify-center py-10 text-center flex-1">
                <ShoppingCart className="w-16 h-16 text-slate-500 opacity-[0.05] dark:opacity-[0.07] mb-4 animate-[pulse_4s_infinite]" />
                <h4 className="text-xs font-bold text-slate-400">Todo en Niveles Óptimos</h4>
                <p className="text-[10px] text-slate-600/50 mt-1 max-w-[240px] font-medium">
                  No hay alertas críticas de insumos en este momento.
                </p>
              </div>
            ) : (
              /* ALERT CARDS (FUTURE PROOFING) */
              <div className="space-y-3">
                {(estadoEnVivo?.alertasStock || []).map((alerta) => (
                  <div 
                    key={alerta.id}
                    className="border border-orange-500/10 bg-orange-950/5 p-3.5 rounded-xl flex justify-between items-center transition-all duration-300 ease-out transform hover:-translate-y-0.5 hover:border-orange-500/30"
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-lg bg-orange-500/10 flex items-center justify-center text-orange-500 shrink-0">
                        <AlertTriangle className="w-4 h-4 animate-pulse" />
                      </div>
                      <div>
                        <h5 className="text-xs font-bold text-slate-200 leading-tight">{alerta.insumo}</h5>
                        <span className="text-[9px] text-orange-400 font-bold uppercase tracking-wider">Stock Bajo Mínimo</span>
                      </div>
                    </div>
                    <div className="text-right">
                      <span className="text-xs font-bold text-orange-400 block font-mono">{alerta.actual}</span>
                      <span className="text-[9px] text-slate-500 font-semibold">Mínimo: {alerta.minimo}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

      </div>

    </div>
  );
}

export default MainDashboard;
