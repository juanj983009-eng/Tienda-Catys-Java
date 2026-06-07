import React, { useState, useEffect, useCallback } from 'react';
import {
  Clock, Flame, CheckCircle, ChefHat, Play, Check,
  AlertTriangle, Package2, UtensilsCrossed, Timer
} from 'lucide-react';
import { API_BASE_URL } from '../config/api';

/* ─── Helpers ──────────────────────────────────────────────────────────────── */
const urgente = (minutos) => minutos >= 12;

function TimerBadge({ minutos, estilo }) {
  const esUrgente = urgente(minutos);

  if (estilo === 'cola') {
    return (
      <span className={`text-[10px] flex items-center gap-1 px-2 py-1 rounded-lg border font-semibold transition-all duration-300 ${
        esUrgente
          ? 'bg-red-500/10 border-red-500/30 text-red-400 animate-pulse'
          : 'bg-slate-800/80 border-slate-700/50 text-slate-400'
      }`}>
        <Clock className={`w-3 h-3 ${esUrgente ? 'text-red-400' : 'text-orange-500'}`} />
        {minutos} min
      </span>
    );
  }

  if (estilo === 'preparacion') {
    return (
      <span className={`text-[10px] flex items-center gap-1 px-2 py-1 rounded-lg border font-semibold ${
        esUrgente
          ? 'bg-red-500/10 border-red-500/30 text-red-400 animate-pulse'
          : 'bg-orange-500/10 border-orange-500/20 text-orange-400'
      }`}>
        <Flame className={`w-3 h-3 ${esUrgente ? 'text-red-400' : 'text-orange-400'}`} />
        {minutos} min
      </span>
    );
  }

  return (
    <span className="text-[10px] flex items-center gap-1 px-2 py-1 rounded-lg border font-semibold bg-emerald-500/10 border-emerald-500/20 text-emerald-400">
      <CheckCircle className="w-3 h-3 text-emerald-400" />
      Listo
    </span>
  );
}

/* ─── Skeleton de comanda ───────────────────────────────────────────────────── */
function SkeletonComanda() {
  return (
    <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-4 animate-pulse space-y-3">
      <div className="flex justify-between items-start">
        <div className="space-y-1.5">
          <div className="h-3 bg-slate-700/50 rounded w-20" />
          <div className="h-4 bg-slate-700/40 rounded w-28" />
        </div>
        <div className="h-6 bg-slate-700/40 rounded-lg w-16" />
      </div>
      <div className="border-t border-slate-800 pt-2 space-y-2">
        <div className="flex items-center gap-2">
          <div className="w-5 h-5 bg-slate-700/40 rounded" />
          <div className="h-3 bg-slate-700/40 rounded w-32" />
        </div>
        <div className="flex items-center gap-2">
          <div className="w-5 h-5 bg-slate-700/40 rounded" />
          <div className="h-3 bg-slate-700/40 rounded w-24" />
        </div>
      </div>
      <div className="h-8 bg-slate-700/40 rounded-xl w-full" />
    </div>
  );
}

/* ─── Empty state de columna ────────────────────────────────────────────────── */
function EmptyColum({ icon: Icon, label, color }) {
  return (
    <div className="flex flex-col items-center justify-center h-48 border border-dashed border-slate-800 rounded-xl text-slate-600 gap-2 select-none">
      <Icon className={`w-7 h-7 ${color} opacity-30 animate-pulse`} />
      <p className="text-xs font-medium tracking-wide">{label}</p>
    </div>
  );
}

/* ─── Tarjeta de comanda ────────────────────────────────────────────────────── */
function ComandaCard({ pedido, evaporandoId, onAvanzar, variante }) {
  const esUrgente = urgente(pedido.haceMinutos);

  const cardBase =
    'flex flex-col justify-between h-auto min-h-[250px] bg-slate-900/60 backdrop-blur-md border rounded-xl p-5 shadow-lg mb-4 transition-all duration-300 hover:-translate-y-1 relative';

  const borderClass = {
    cola:       esUrgente ? 'border-red-500/30 hover:border-red-500/50 shadow-red-500/5' : 'border-slate-800 hover:border-orange-500/30',
    preparacion: esUrgente ? 'border-red-500/30 hover:border-red-500/50'                 : 'border-orange-500/20 hover:border-orange-500/40 shadow-orange-500/5',
    listo:       'border-emerald-500/15 hover:border-emerald-500/35',
  }[variante];

  const idColor = {
    cola: 'text-orange-500',
    preparacion: 'bg-gradient-to-r from-orange-500 to-amber-500 bg-clip-text text-transparent',
    listo: 'text-emerald-400',
  }[variante];

  const evaporando = pedido.id === evaporandoId;

  return (
    <div className={`${cardBase} ${borderClass} ${evaporando ? 'opacity-0 scale-95 pointer-events-none' : ''}`}>
      {/* Barra de acento lateral */}
      <div className={`absolute left-0 top-0 bottom-0 w-0.5 rounded-l-xl ${
        variante === 'cola'        ? (esUrgente ? 'bg-red-500/60' : 'bg-orange-500/30')
        : variante === 'preparacion'? (esUrgente ? 'bg-red-500/60' : 'bg-gradient-to-b from-orange-500 to-amber-500')
        : 'bg-emerald-500/40'
      }`} />

      {/* Cabecera */}
      <div className="flex justify-between items-start mb-3 pl-1">
        <div>
          <span className={`text-[10px] font-extrabold tracking-widest uppercase ${idColor}`}>
            Orden #{pedido.id}
          </span>
          <h4 className="font-bold text-sm text-slate-100 mt-0.5 leading-tight">{pedido.cliente}</h4>
          {pedido.mesa && (
            <span className="text-[10px] text-slate-500 font-medium">Mesa {pedido.mesa}</span>
          )}
        </div>
        <TimerBadge minutos={pedido.haceMinutos} estilo={variante} />
      </div>

      {/* Badge URGENTE */}
      {esUrgente && (
        <div className="flex items-center gap-1.5 bg-red-500/10 border border-red-500/20 rounded-lg px-2.5 py-1 w-fit animate-pulse pl-1 ml-1 mb-3">
          <AlertTriangle className="w-3 h-3 text-red-400" />
          <span className="text-[10px] font-bold text-red-400 uppercase tracking-wider">URGENTE</span>
        </div>
      )}

      {/* Ítems de la comanda */}
      <div className="flex-1 w-full mb-5 border-t border-slate-800/70 pt-2.5 pl-1 text-slate-300">
        <ul className="space-y-2">
          {pedido.items.map((item, i) => (
            <li key={i} className="text-sm font-semibold text-slate-200 flex justify-between items-center gap-2">
              <span className="flex items-center gap-2">
                <span className={`w-5 h-5 rounded text-[10px] font-bold flex items-center justify-center shrink-0 ${
                  variante === 'listo'
                    ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                    : 'bg-orange-500/10 text-orange-400 border border-orange-500/15'
                }`}>
                  {item.cant}
                </span>
                <span>{item.nombre}</span>
              </span>
            </li>
          ))}
        </ul>
      </div>

      {/* Botón de acción */}
      {variante === 'cola' && (
        <button
          onClick={() => onAvanzar(pedido.id, pedido.estado)}
          className="flex-shrink-0 w-full py-2.5 bg-gradient-to-r from-orange-500 to-amber-500 hover:from-orange-600 hover:to-amber-600 text-white text-xs font-bold rounded-lg active:scale-95 transition-all duration-200 flex items-center justify-center gap-1.5 uppercase tracking-wider shadow-[0_4px_12px_rgba(249,115,22,0.2)] cursor-pointer mt-2"
        >
          <Play className="w-3.5 h-3.5" />
          Comenzar Preparación
        </button>
      )}

      {variante === 'preparacion' && (
        <button
          onClick={() => onAvanzar(pedido.id, pedido.estado)}
          className="flex-shrink-0 w-full py-2.5 bg-orange-500/15 hover:bg-orange-500 text-orange-400 hover:text-white border border-orange-500/30 hover:border-transparent text-xs font-bold rounded-lg active:scale-95 transition-all duration-200 flex items-center justify-center gap-1.5 uppercase tracking-wider cursor-pointer mt-2"
        >
          <Check className="w-3.5 h-3.5" />
          Marcar como Listo
        </button>
      )}

      {variante === 'listo' && (
        <button
          onClick={() => onAvanzar(pedido.id, pedido.estado)}
          className="flex-shrink-0 w-full py-2.5 bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-400 hover:to-teal-400 text-white text-xs font-semibold rounded-lg active:scale-95 transition-all duration-200 flex items-center justify-center gap-1.5 uppercase tracking-wider shadow-[0_4px_12px_rgba(16,185,129,0.15)] cursor-pointer mt-2"
        >
          <CheckCircle className="w-3.5 h-3.5" />
          Entregar / Despachar
        </button>
      )}
    </div>
  );
}

/* ─── Componente principal ──────────────────────────────────────────────────── */
function PedidosCocina() {
  const [evaporandoId, setEvaporandoId] = useState(null);
  const [comandas, setComandas]         = useState([]);
  const [loading, setLoading]           = useState(true);
  const [error, setError]               = useState(null);

  const cargarComandas = useCallback(async () => {
    try {
      const res = await fetch(`${API_BASE_URL}/comandas/activas`);
      if (!res.ok) throw new Error('Error al obtener comandas');
      const data = await res.json();
      setComandas(data);
      setError(null);
    } catch (err) {
      console.error('Error al cargar comandas de cocina:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarComandas();
    const interval = setInterval(cargarComandas, 5000);
    return () => clearInterval(interval);
  }, [cargarComandas]);

  const avanzarEstado = async (id, estadoActual) => {
    const mapa = { en_cola: 'preparacion', preparacion: 'listo', listo: 'entregado' };
    const nuevoEstado = mapa[estadoActual];
    if (!nuevoEstado) return;

    try {
      const res = await fetch(`${API_BASE_URL}/comandas/${id}/estado`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ estado: nuevoEstado }),
      });
      if (!res.ok) throw new Error('Error al actualizar el estado de la comanda');

      if (nuevoEstado === 'entregado') {
        setComandas(prev => prev.filter(c => c.id !== id));
      } else {
        cargarComandas();
      }
    } catch (err) {
      console.error('Error al avanzar el estado de la comanda:', err);
    }
  };

  const handleAvanzar = (id, estadoActual) => {
    if (estadoActual === 'preparacion') {
      setEvaporandoId(id);
      setTimeout(() => {
        avanzarEstado(id, estadoActual);
        setEvaporandoId(null);
      }, 250);
    } else {
      avanzarEstado(id, estadoActual);
    }
  };

  const cola        = comandas.filter(c => c.estado === 'en_cola');
  const preparacion = comandas.filter(c => c.estado === 'preparacion');
  const listos      = comandas.filter(c => c.estado === 'listo');
  const totalActivas = cola.length + preparacion.length + listos.length;

  /* ── Skeleton inicial ───────────────────────────────────────────────────── */
  if (loading && comandas.length === 0) {
    return (
      <div className="h-full flex flex-col overflow-hidden gap-4">
        {/* Header */}
        <div className="shrink-0 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 bg-slate-800 rounded-xl animate-pulse" />
            <div className="h-5 bg-slate-800 rounded w-48 animate-pulse" />
          </div>
          <div className="h-6 bg-slate-800 rounded-full w-28 animate-pulse" />
        </div>
        {/* Kanban grid skeletons */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 flex-1">
          {[0, 1, 2].map(col => (
            <div key={col} className="bg-slate-900/40 border border-slate-800 rounded-2xl p-5 flex flex-col gap-4">
              <div className="h-4 bg-slate-800 rounded w-32 animate-pulse shrink-0" />
              <div className="space-y-4">
                {[0, 1, 2].map(i => <SkeletonComanda key={i} />)}
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  /* ── Error state ─────────────────────────────────────────────────────────── */
  if (error && comandas.length === 0) {
    return (
      <div className="h-full flex flex-col items-center justify-center gap-4 text-red-400">
        <AlertTriangle className="w-10 h-10 opacity-60" />
        <p className="text-sm font-semibold">Error al cargar el tablero de cocina</p>
        <button
          onClick={cargarComandas}
          className="px-4 py-2 bg-slate-800 text-slate-200 rounded-xl hover:bg-slate-700 text-xs transition cursor-pointer"
        >
          Reintentar
        </button>
      </div>
    );
  }

  return (
    <div className="h-full flex flex-col overflow-hidden gap-5">

      {/* ── Page Header ─────────────────────────────────────────────────────── */}
      <div className="shrink-0 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 bg-gradient-to-tr from-orange-500 to-amber-500 rounded-xl flex items-center justify-center shadow-lg shadow-orange-500/20">
            <ChefHat className="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 className="text-base font-bold text-slate-100 leading-tight">Tablero de Cocina</h1>
            <p className="text-[10px] text-slate-500 font-medium uppercase tracking-widest">Catys Enterprise — Módulo de Comandas</p>
          </div>
        </div>

        {/* Total activas badge */}
        <div className="flex items-center gap-2 bg-slate-900/60 border border-slate-800 rounded-xl px-3 py-1.5">
          <div className={`w-2 h-2 rounded-full ${totalActivas > 0 ? 'bg-orange-500 animate-pulse' : 'bg-slate-600'}`} />
          <span className="text-xs text-slate-400 font-medium">
            {totalActivas} comanda{totalActivas !== 1 ? 's' : ''} activa{totalActivas !== 1 ? 's' : ''}
          </span>
        </div>
      </div>

      {/* ── Kanban Board ─────────────────────────────────────────────────────── */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 flex-1 lg:overflow-hidden overflow-y-auto pr-1 pb-2">

        {/* ── COLUMNA 1: RECIBIDOS ──────────────────────────────────────────── */}
        <div className="bg-slate-900/40 border border-orange-500/10 rounded-2xl p-5 flex flex-col h-[calc(100vh-220px)] overflow-hidden">
          {/* Column header */}
          <div className="flex justify-between items-center pb-4 border-b border-slate-800/80 shrink-0 mb-4">
            <h3 className="font-bold text-sm tracking-widest text-slate-400 uppercase flex items-center gap-2">
              <Clock className="w-4 h-4 text-amber-500/70 animate-pulse" />
              Recibidos
            </h3>
            <span className="bg-orange-500/10 border border-orange-500/20 text-orange-400 text-[10px] px-2.5 py-0.5 rounded-full font-bold">
              {cola.length}
            </span>
          </div>

          <div className="flex-1 overflow-y-auto space-y-3 pr-1 [&::-webkit-scrollbar]:w-1 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:bg-slate-700/50 hover:[&::-webkit-scrollbar-thumb]:bg-orange-500/40 [&::-webkit-scrollbar-thumb]:rounded-full">
            {cola.length === 0
              ? <EmptyColum icon={Package2} label="Sin pedidos en cola" color="text-amber-500" />
              : cola.map(pedido => (
                  <ComandaCard
                    key={pedido.id}
                    pedido={pedido}
                    evaporandoId={evaporandoId}
                    onAvanzar={handleAvanzar}
                    variante="cola"
                  />
                ))
            }
          </div>
        </div>

        {/* ── COLUMNA 2: EN PREPARACIÓN ────────────────────────────────────── */}
        <div className="bg-slate-900/40 border border-orange-500/20 rounded-2xl p-5 flex flex-col h-[calc(100vh-220px)] overflow-hidden shadow-[inset_0_0_60px_rgba(249,115,22,0.03)]">
          {/* Column header */}
          <div className="flex justify-between items-center pb-4 border-b border-orange-500/15 shrink-0 mb-4">
            <h3 className="font-bold text-sm tracking-widest uppercase flex items-center gap-2">
              <Flame className="w-4 h-4 text-orange-500 animate-bounce" />
              <span className="bg-gradient-to-r from-orange-400 to-amber-400 bg-clip-text text-transparent">
                En Preparación
              </span>
            </h3>
            <span className="bg-gradient-to-r from-orange-500 to-amber-500 text-white text-[10px] px-2.5 py-0.5 rounded-full font-bold shadow-md shadow-orange-500/20">
              {preparacion.length}
            </span>
          </div>

          <div className="flex-1 overflow-y-auto space-y-3 pr-1 [&::-webkit-scrollbar]:w-1 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:bg-slate-700/50 hover:[&::-webkit-scrollbar-thumb]:bg-orange-500/40 [&::-webkit-scrollbar-thumb]:rounded-full">
            {preparacion.length === 0
              ? <EmptyColum icon={ChefHat} label="Sin platos en preparación" color="text-orange-500" />
              : preparacion.map(pedido => (
                  <ComandaCard
                    key={pedido.id}
                    pedido={pedido}
                    evaporandoId={evaporandoId}
                    onAvanzar={handleAvanzar}
                    variante="preparacion"
                  />
                ))
            }
          </div>
        </div>

        {/* ── COLUMNA 3: LISTOS PARA ENTREGAR ─────────────────────────────── */}
        <div className="bg-slate-900/40 border border-emerald-500/10 rounded-2xl p-5 flex flex-col h-[calc(100vh-220px)] overflow-hidden">
          {/* Column header */}
          <div className="flex justify-between items-center pb-4 border-b border-emerald-500/15 shrink-0 mb-4">
            <h3 className="font-bold text-sm tracking-widest text-slate-400 uppercase flex items-center gap-2">
              <CheckCircle className="w-4 h-4 text-emerald-500/80" />
              Listos para Entregar
            </h3>
            <span className="bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-[10px] px-2.5 py-0.5 rounded-full font-bold">
              {listos.length}
            </span>
          </div>

          <div className="flex-1 overflow-y-auto space-y-3 pr-1 [&::-webkit-scrollbar]:w-1 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:bg-slate-700/50 hover:[&::-webkit-scrollbar-thumb]:bg-orange-500/40 [&::-webkit-scrollbar-thumb]:rounded-full flex flex-col">
            {listos.length === 0 ? (
              <>
                <EmptyColum icon={UtensilsCrossed} label="Ningún pedido por entregar" color="text-emerald-500" />
                {/* Estadísticas del turno */}
                <div className="mt-auto bg-slate-900/50 border border-slate-800/80 rounded-xl p-4 text-center shrink-0">
                  <div className="flex items-center justify-center gap-1.5 mb-1">
                    <Timer className="w-3.5 h-3.5 text-orange-500/60" />
                    <p className="text-xs font-semibold text-slate-400">Eficiencia del Turno</p>
                  </div>
                  <p className="text-xl font-black text-transparent bg-gradient-to-r from-orange-400 to-amber-400 bg-clip-text">94%</p>
                  <p className="text-[10px] text-slate-500 mt-1">Tiempo prom. de despacho: 12 min</p>
                </div>
              </>
            ) : (
              listos.map(pedido => (
                <ComandaCard
                  key={pedido.id}
                  pedido={pedido}
                  evaporandoId={evaporandoId}
                  onAvanzar={handleAvanzar}
                  variante="listo"
                />
              ))
            )}
          </div>
        </div>

      </div>
    </div>
  );
}

export default PedidosCocina;
