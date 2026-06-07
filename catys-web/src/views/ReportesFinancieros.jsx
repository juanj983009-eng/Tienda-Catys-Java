import React, { useEffect, useState } from 'react';
import { TrendingUp, DollarSign, Wallet, CreditCard, Activity, Banknote } from 'lucide-react';
import { API_BASE_URL } from '../config/api';

function ReportesFinancieros() {
  const [animar, setAnimar] = useState(false);
  const [dataFinanciera, setDataFinanciera] = useState({
    kpis: { dia: 0, semana: 0, ano: 0 },
    metodosPago: [],
    progressionMensual: []
  });
  const [loading, setLoading] = useState(true);

  const cargarReporte = async () => {
    try {
      const res = await fetch(`${API_BASE_URL}/financiero/reporte`);
      if (!res.ok) throw new Error('Error al obtener reporte financiero');
      const data = await res.json();
      setDataFinanciera(data);
    } catch (err) {
      console.error("Error al cargar reporte financiero:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const timer = setTimeout(() => setAnimar(true), 50);
    
    cargarReporte();
    const interval = setInterval(cargarReporte, 5000);
    
    return () => {
      clearTimeout(timer);
      clearInterval(interval);
    };
  }, []);

  const getColor = (name) => {
    const lowercaseName = name.toLowerCase();
    if (lowercaseName.includes('yape') || lowercaseName.includes('plin')) {
      return 'bg-gradient-to-r from-indigo-600 to-violet-500';
    }
    if (lowercaseName.includes('efectivo')) {
      return 'bg-gradient-to-r from-emerald-600 to-teal-400';
    }
    if (lowercaseName.includes('tarjeta')) {
      return 'bg-gradient-to-r from-blue-600 to-cyan-400';
    }
    return 'bg-gradient-to-r from-slate-600 to-slate-500';
  };

  return (
    <div className={`h-full flex flex-col overflow-y-auto pb-6 pr-1 transition-all duration-500 ease-out transform ${
      animar ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-4'
    }`}>
      {/* 1. FILA SUPERIOR: METRICAS RAPIDAS */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6 shrink-0">
        
        {/* KPI: HOY */}
        <div className="bg-slate-900/40 backdrop-blur-md border border-slate-800 rounded-2xl p-6 flex flex-col justify-between h-32 hover:-translate-y-1 hover:border-orange-500/20 hover:shadow-[0_0_20px_rgba(249,115,22,0.08)] transition-all duration-300 shadow-md">
          <div className="flex justify-between items-center">
            <span className="text-[10px] text-slate-500 font-bold uppercase tracking-widest">Ingresos Hoy</span>
            <div className="flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold text-emerald-400 bg-emerald-500/10 border border-emerald-500/15">
              <TrendingUp className="w-3 h-3" />
              En vivo
            </div>
          </div>
          <div className="mt-4 flex items-baseline justify-between">
            <span className="text-2xl font-bold text-slate-100">S/ {dataFinanciera.kpis.dia.toFixed(2)}</span>
            <span className="text-slate-500 text-xs font-medium tracking-wide">Transacciones reales</span>
          </div>
        </div>

        {/* KPI: ESTA SEMANA */}
        <div className="bg-slate-900/40 backdrop-blur-md border border-slate-800 rounded-2xl p-6 flex flex-col justify-between h-32 hover:-translate-y-1 hover:border-orange-500/20 hover:shadow-[0_0_20px_rgba(249,115,22,0.08)] transition-all duration-300 shadow-md">
          <div className="flex justify-between items-center">
            <span className="text-[10px] text-slate-500 font-bold uppercase tracking-widest">Esta Semana</span>
            <div className="flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold text-emerald-400 bg-emerald-500/10 border border-emerald-500/15">
              <TrendingUp className="w-3 h-3" />
              Últimos 7 días
            </div>
          </div>
          <div className="mt-4 flex items-baseline justify-between">
            <span className="text-2xl font-bold text-slate-100">S/ {dataFinanciera.kpis.semana.toFixed(2)}</span>
            <span className="text-slate-500 text-xs font-medium tracking-wide">Acumulado</span>
          </div>
        </div>

        {/* KPI: ESTE AÑO */}
        <div className="bg-slate-900/40 backdrop-blur-md border border-slate-800 rounded-2xl p-6 flex flex-col justify-between h-32 hover:-translate-y-1 hover:border-orange-500/20 hover:shadow-[0_0_20px_rgba(249,115,22,0.08)] transition-all duration-300 shadow-md">
          <div className="flex justify-between items-center">
            <span className="text-[10px] text-slate-500 font-bold uppercase tracking-widest">Este Año</span>
            <div className="flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold text-emerald-400 bg-emerald-500/10 border border-emerald-500/15">
              <TrendingUp className="w-3 h-3" />
              Anual 2026
            </div>
          </div>
          <div className="mt-4 flex items-baseline justify-between">
            <span className="text-2xl font-bold text-slate-100">S/ {dataFinanciera.kpis.ano.toLocaleString('es-PE', { minimumFractionDigits: 2 })}</span>
            <span className="text-slate-500 text-xs font-medium tracking-wide">Ejecución anual</span>
          </div>
        </div>

      </div>

      {/* 2. BLOQUE CENTRAL: COMPARATIVAS */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 flex-1">
        
        {/* PANEL IZQUIERDO: METODOS DE PAGO */}
        <div className="bg-slate-900/40 backdrop-blur-md border border-slate-800 rounded-2xl p-6 flex flex-col h-96 hover:border-orange-500/20 hover:shadow-[0_0_20px_rgba(249,115,22,0.08)] transition-all duration-300">
          <div className="border-b border-slate-800 pb-4 mb-6 shrink-0 flex items-center justify-between">
            <h3 className="font-bold text-sm text-slate-200 uppercase tracking-wider flex items-center gap-2">
              <Activity className="w-4 h-4 text-orange-500" />
              Distribución de Pagos
            </h3>
            <span className="text-[10px] text-slate-500 font-bold uppercase">Mes Actual</span>
          </div>

          <div className="flex-1 flex flex-col justify-around">
            {loading && dataFinanciera.metodosPago.length === 0 ? (
              <div className="text-center text-slate-500 text-xs py-4">Cargando distribución...</div>
            ) : dataFinanciera.metodosPago.length === 0 ? (
              <div className="text-center text-slate-500 text-xs py-4">Sin datos de métodos de pago.</div>
            ) : (
              dataFinanciera.metodosPago.map((pago, idx) => (
                <div key={idx} className="space-y-2">
                  <div className="flex justify-between text-xs font-semibold">
                    <span className="text-slate-300 flex items-center gap-2 font-medium">
                      {pago.name.toLowerCase().includes('yape') && <Wallet className="w-4 h-4 text-indigo-400/80" />}
                      {pago.name.toLowerCase().includes('efectivo') && <Banknote className="w-4 h-4 text-emerald-400/80" />}
                      {pago.name.toLowerCase().includes('tarjeta') && <CreditCard className="w-4 h-4 text-cyan-400/80" />}
                      {pago.name}
                    </span>
                    <span className="text-slate-100">
                      S/ {pago.monto.toFixed(2)} <span className="text-slate-500 text-[10px] font-normal">({pago.porcentaje}%)</span>
                    </span>
                  </div>

                  {/* Barra de progreso */}
                  <div className="w-full bg-slate-950/60 h-2.5 rounded-full overflow-hidden border border-slate-800/80">
                    <div 
                      style={{ width: `${pago.porcentaje}%` }} 
                      className={`h-full ${getColor(pago.name)} rounded-full transition-all duration-1000 ease-out`}
                    />
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* PANEL DERECHO: PROGRESO MENSUAL */}
        <div className="bg-slate-900/40 backdrop-blur-md border border-slate-800 rounded-2xl p-6 flex flex-col h-96 hover:border-orange-500/20 hover:shadow-[0_0_20px_rgba(249,115,22,0.08)] transition-all duration-300">
          <div className="border-b border-slate-800 pb-4 mb-6 shrink-0 flex items-center justify-between">
            <h3 className="font-bold text-sm text-slate-200 uppercase tracking-wider flex items-center gap-2">
              <TrendingUp className="w-4 h-4 text-amber-500" />
              Progresión de Ingresos
            </h3>
            <span className="text-[10px] text-slate-500 font-bold uppercase">Año 2026</span>
          </div>

          {/* Gráfico de Barras Verticales */}
          <div className="flex-1 flex items-end justify-between border-b border-l border-slate-800/60 pb-3 pl-4 relative h-64">
            
            {/* Líneas de cuadrícula horizontal */}
            <div className="absolute left-0 right-0 top-0 border-t border-slate-800/60 w-full" />
            <div className="absolute left-0 right-0 top-1/4 border-t border-slate-800/60 w-full" />
            <div className="absolute left-0 right-0 top-2/4 border-t border-slate-800/60 w-full" />
            <div className="absolute left-0 right-0 top-3/4 border-t border-slate-800/60 w-full" />

            {loading && dataFinanciera.progressionMensual.length === 0 ? (
              <div className="absolute inset-0 flex items-center justify-center text-slate-500 text-xs">Cargando progresión...</div>
            ) : dataFinanciera.progressionMensual.length === 0 ? (
              <div className="absolute inset-0 flex items-center justify-center text-slate-500 text-xs">Sin registros mensuales.</div>
            ) : (
              dataFinanciera.progressionMensual.map((prog, idx) => (
                <div key={idx} className="flex flex-col items-center gap-2 group h-full justify-end relative z-10">
                  {/* Popover de monto en hover */}
                  <span className="absolute -top-6 text-[9px] bg-slate-950 border border-slate-800 text-amber-400 font-bold px-1.5 py-0.5 rounded opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none whitespace-nowrap shadow-md">
                    S/ {prog.monto.toLocaleString()}
                  </span>
                  
                  {/* Barra vertical animada */}
                  <div 
                    style={{ height: `${prog.porcentaje}%` }}
                    className="w-8 bg-gradient-to-t from-orange-600/80 to-amber-400 rounded-t-lg hover:from-orange-500 hover:to-amber-300 transition-all duration-500 ease-out shadow-[0_0_12px_rgba(249,115,22,0.15)]"
                  />
                  
                  {/* Nombre del mes */}
                  <span className="text-[10px] text-slate-400 font-semibold">{prog.mes}</span>
                </div>
              ))
            )}
          </div>
        </div>

      </div>
    </div>
  );
}

export default ReportesFinancieros;
