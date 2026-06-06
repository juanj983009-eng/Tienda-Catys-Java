import React, { useState, useEffect } from 'react';
import { DollarSign, ClipboardList, Users, AlertTriangle, Activity, ChefHat, Grid } from 'lucide-react';
import { API_BASE_URL } from '../config/api';

function ResumenGeneral() {
  const [estadoEnVivo, setEstadoEnVivo] = useState({
    kpis: { ventasHoy: 0, ordenesHoy: 0, clientesVip: 0 },
    cocina: { enCola: 0, enPreparacion: 0 },
    mesas: { ocupadas: 0, disponibles: 0 },
    alertasStock: []
  });
  const [loading, setLoading] = useState(true);

  const cargarMetricas = async () => {
    try {
      const res = await fetch(`${API_BASE_URL}/dashboard/resumen`);
      if (!res.ok) throw new Error('Error al obtener métricas');
      const data = await res.json();
      setEstadoEnVivo(data);
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

  const totalMesas = estadoEnVivo.mesas.ocupadas + estadoEnVivo.mesas.disponibles;
  const ocupacionPorcentaje = totalMesas > 0 
    ? Math.round((estadoEnVivo.mesas.ocupadas / totalMesas) * 100) 
    : 0;

  const totalCocina = estadoEnVivo.cocina.enCola + estadoEnVivo.cocina.enPreparacion;

  return (
    <div className="space-y-6 h-full overflow-y-auto pb-8 pr-1 text-slate-100">
      
      {/* 1. FILA SUPERIOR: KPIs */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        
        {/* KPI Ventas */}
        <div 
          className="bg-slate-900/60 border border-slate-800 p-6 rounded-2xl transition-all duration-300 ease-out transform hover:-translate-y-1 hover:border-slate-700 shadow-md flex justify-between items-center"
        >
          <div className="space-y-1.5">
            <span className="text-[10px] text-slate-500 font-bold uppercase tracking-wider">Ventas de Hoy</span>
            <h3 className="text-2xl font-bold text-slate-100">
              S/ {estadoEnVivo.kpis.ventasHoy.toFixed(2)}
            </h3>
            <p className="text-[10px] text-emerald-400 font-semibold flex items-center gap-1">
              <span>↑ 12% vs ayer</span>
            </p>
          </div>
          <div className="w-12 h-12 bg-emerald-500/10 text-emerald-500 rounded-xl flex items-center justify-center">
            <DollarSign className="w-6 h-6" />
          </div>
        </div>

        {/* KPI Órdenes */}
        <div 
          className="bg-slate-900/60 border border-slate-800 p-6 rounded-2xl transition-all duration-300 ease-out transform hover:-translate-y-1 hover:border-slate-700 shadow-md flex justify-between items-center"
        >
          <div className="space-y-1.5">
            <span className="text-[10px] text-slate-500 font-bold uppercase tracking-wider">Órdenes Procesadas</span>
            <h3 className="text-2xl font-bold text-slate-100">
              {estadoEnVivo.kpis.ordenesHoy} Pedidos
            </h3>
            <p className="text-[10px] text-amber-500 font-semibold">
              <span>Caja en operación activa</span>
            </p>
          </div>
          <div className="w-12 h-12 bg-amber-500/10 text-amber-500 rounded-xl flex items-center justify-center">
            <ClipboardList className="w-6 h-6" />
          </div>
        </div>

        {/* KPI Clientes VIP */}
        <div 
          className="bg-slate-900/60 border border-slate-800 p-6 rounded-2xl transition-all duration-300 ease-out transform hover:-translate-y-1 hover:border-slate-700 shadow-md flex justify-between items-center"
        >
          <div className="space-y-1.5">
            <span className="text-[10px] text-slate-500 font-bold uppercase tracking-wider">Clientes VIP del Turno</span>
            <h3 className="text-2xl font-bold text-slate-100">
              {estadoEnVivo.kpis.clientesVip} Frecuentes
            </h3>
            <p className="text-[10px] text-indigo-400 font-semibold">
              <span>Descuento preferencial aplicado</span>
            </p>
          </div>
          <div className="w-12 h-12 bg-indigo-500/10 text-indigo-500 rounded-xl flex items-center justify-center">
            <Users className="w-6 h-6" />
          </div>
        </div>

      </div>

      {/* 2. BLOQUE CENTRAL: ESTADO OPERATIVO & ABASTECIMIENTO */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        
        {/* Panel Izquierdo: Estado Operativo */}
        <div 
          className="bg-slate-900/40 border border-slate-800 p-6 rounded-2xl transition-all duration-300 ease-out transform hover:-translate-y-1 hover:border-slate-700 flex flex-col justify-between"
        >
          <div className="border-b border-slate-800 pb-4 mb-6 flex items-center gap-2">
            <Activity className="w-4 h-4 text-amber-500" />
            <h3 className="text-xs font-bold text-slate-200 uppercase tracking-wider">
              Monitor de Operación Salón & Cocina
            </h3>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            
            {/* Cocina Mini-Container */}
            <div className="border border-amber-500/20 bg-amber-500/5 p-4 rounded-xl flex flex-col justify-between h-32 hover:border-amber-500/40 transition-colors">
              <div className="flex items-center justify-between">
                <span className="text-[10px] text-amber-400 font-bold uppercase tracking-wider">Cocina en Vivo</span>
                <ChefHat className="w-4 h-4 text-amber-500" />
              </div>
              <div className="mt-2 flex items-baseline gap-2">
                <span className="text-3xl font-extrabold text-slate-100">{totalCocina}</span>
                <span className="text-xs text-slate-400">pedidos</span>
              </div>
              <div className="mt-2 text-[10px] text-slate-400 flex flex-col gap-0.5 font-medium">
                <span>• {estadoEnVivo.cocina.enCola} comandas en cola</span>
                <span>• {estadoEnVivo.cocina.enPreparacion} en preparación</span>
              </div>
            </div>

            {/* Mesas Mini-Container */}
            <div className="border border-amber-500/20 bg-amber-500/5 p-4 rounded-xl flex flex-col justify-between h-32 hover:border-amber-500/40 transition-colors">
              <div className="flex items-center justify-between">
                <span className="text-[10px] text-amber-400 font-bold uppercase tracking-wider">Mesas Activas</span>
                <Grid className="w-4 h-4 text-amber-500" />
              </div>
              <div className="mt-2 flex items-baseline gap-2">
                <span className="text-3xl font-extrabold text-slate-100">{ocupacionPorcentaje}%</span>
                <span className="text-xs text-slate-400">ocupación</span>
              </div>
              <div className="mt-2 text-[10px] text-slate-400 flex flex-col gap-0.5 font-medium">
                <span>• {estadoEnVivo.mesas.ocupadas} ocupadas en salón</span>
                <span>• {estadoEnVivo.mesas.disponibles} disponibles</span>
              </div>
            </div>

          </div>

          <div className="mt-6 text-[10px] text-slate-500">
            Los datos se refrescan de forma automática con cada actualización del POS y del tablero de mesas.
          </div>
        </div>

        {/* Panel Derecho: Alertas de Abastecimiento */}
        <div 
          className="bg-slate-900/40 border border-slate-800 p-6 rounded-2xl transition-all duration-300 ease-out transform hover:-translate-y-1 hover:border-slate-700 flex flex-col"
        >
          <div className="border-b border-slate-800 pb-4 mb-4 flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 text-rose-500" />
            <h3 className="text-xs font-bold text-slate-200 uppercase tracking-wider">
              Alertas de Abastecimiento Crítico
            </h3>
          </div>

          <div className="space-y-3 flex-1 flex flex-col justify-center">
            {estadoEnVivo.alertasStock.map((alerta) => (
              <div 
                key={alerta.id}
                className="border border-rose-500/20 bg-rose-950/10 p-3 rounded-xl flex justify-between items-center transition-all duration-300 ease-out transform hover:-translate-y-1 hover:border-slate-700"
              >
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-lg bg-rose-500/10 flex items-center justify-center text-rose-500 shrink-0">
                    <AlertTriangle className="w-4 h-4 animate-pulse" />
                  </div>
                  <div>
                    <h5 className="text-xs font-bold text-slate-250 leading-tight">{alerta.insumo}</h5>
                    <span className="text-[9px] text-rose-400 font-bold uppercase tracking-wider">Stock Bajo Mínimo</span>
                  </div>
                </div>
                <div className="text-right">
                  <span className="text-xs font-bold text-rose-400 block font-mono">{alerta.actual}</span>
                  <span className="text-[9px] text-slate-500">Mínimo: {alerta.minimo}</span>
                </div>
              </div>
            ))}

            {estadoEnVivo.alertasStock.length === 0 && (
              <div className="text-center py-6 text-slate-500 text-xs">
                No hay alertas críticas de abastecimiento. Todo está en niveles óptimos.
              </div>
            )}
          </div>
        </div>

      </div>

    </div>
  );
}

export default ResumenGeneral;
