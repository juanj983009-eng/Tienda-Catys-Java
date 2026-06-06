import React, { useState, useEffect } from 'react';
import { Clock, Flame, CheckCircle, ChefHat, Play, Check } from 'lucide-react';
import { API_BASE_URL } from '../config/api';

function PedidosCocina() {
  const [evaporandoId, setEvaporandoId] = useState(null);
  const [comandas, setComandas] = useState([]);
  const [loading, setLoading] = useState(true);

  // Cargar comandas activas desde el backend
  const cargarComandas = async () => {
    try {
      const res = await fetch(`${API_BASE_URL}/comandas/activas`);
      if (!res.ok) throw new Error('Error al obtener comandas');
      const data = await res.json();
      setComandas(data);
    } catch (err) {
      console.error("Error al cargar comandas de cocina:", err);
    } finally {
      setLoading(false);
    }
  };

  // Polling cada 5 segundos para mantener la pantalla actualizada
  useEffect(() => {
    cargarComandas();
    const interval = setInterval(cargarComandas, 5000);
    return () => clearInterval(interval);
  }, []);

  // Avanza el pedido de estado en el backend y refresca localmente
  const avanzarEstado = async (id, estadoActual) => {
    let nuevoEstado = '';
    if (estadoActual === 'en_cola') nuevoEstado = 'preparacion';
    else if (estadoActual === 'preparacion') nuevoEstado = 'listo';
    else if (estadoActual === 'listo') nuevoEstado = 'entregado';

    try {
      const res = await fetch(`${API_BASE_URL}/comandas/${id}/estado`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ estado: nuevoEstado }),
      });

      if (!res.ok) throw new Error('Error al actualizar el estado de la comanda');
      
      // Si el nuevo estado es entregado, desaparece. De lo contrario, actualizamos la lista.
      if (nuevoEstado === 'entregado') {
        setComandas(prev => prev.filter(c => c.id !== id));
      } else {
        cargarComandas();
      }
    } catch (err) {
      console.error("Error al avanzar el estado de la comanda:", err);
    }
  };

  // Maneja el avance con efecto de evaporación de 200ms
  const handleAvanzar = (id, estadoActual) => {
    if (estadoActual === 'preparacion') {
      setEvaporandoId(id);
      setTimeout(() => {
        avanzarEstado(id, estadoActual);
        setEvaporandoId(null);
      }, 200);
    } else {
      avanzarEstado(id, estadoActual);
    }
  };

  const getColumnaFiltro = (estado) => {
    return comandas.filter((c) => c.estado === estado);
  };

  return (
    <div className="h-full flex flex-col overflow-hidden">
      {loading && comandas.length === 0 ? (
        <div className="flex-1 flex items-center justify-center text-slate-400 text-xs">
          Cargando pedidos de cocina...
        </div>
      ) : (
        /* Tablero Kanban de Cocina */
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 flex-1 overflow-y-auto pr-1 pb-6">
          
          {/* Columna: EN COLA */}
          <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-5 flex flex-col h-[calc(100vh-210px)] overflow-hidden">
            <div className="flex justify-between items-center pb-4 border-b border-slate-800 shrink-0 mb-4">
              <h3 className="font-bold text-sm tracking-wider text-slate-400 uppercase flex items-center gap-2">
                <Clock className="w-4 h-4 text-amber-500 animate-pulse" />
                En Cola
              </h3>
              <span className="bg-slate-800 text-amber-500 text-xs px-2.5 py-0.5 rounded-full font-bold">
                {getColumnaFiltro('en_cola').length}
              </span>
            </div>
            
            <div className="flex-1 overflow-y-auto space-y-4 pr-1">
              {getColumnaFiltro('en_cola').length === 0 ? (
                <div className="flex flex-col items-center justify-center h-40 border border-dashed border-slate-800 rounded-xl text-slate-600 text-xs gap-2">
                  <Clock className="w-6 h-6 animate-pulse text-slate-600" />
                  Sin pedidos en cola
                </div>
              ) : (
                getColumnaFiltro('en_cola').map((pedido) => (
                  <div 
                    key={pedido.id} 
                    className={`bg-slate-900 border border-slate-800 rounded-xl p-5 hover:border-slate-700/50 flex flex-col justify-between gap-4 transition-all duration-300 ease-out transform ${
                      pedido.id === evaporandoId 
                        ? 'opacity-0 scale-95' 
                        : 'hover:-translate-y-1 hover:shadow-lg hover:shadow-slate-900/50'
                    }`}
                  >
                    <div className="space-y-3">
                      <div className="flex justify-between items-start">
                        <div>
                          <span className="text-xs text-orange-500 font-extrabold tracking-wider">PEDIDO #{pedido.id}</span>
                          <h4 className="font-bold text-sm text-slate-100 mt-0.5">{pedido.cliente}</h4>
                        </div>
                        <span className="text-[10px] text-slate-500 flex items-center gap-1.5 bg-slate-950 px-2 py-1 rounded-md border border-slate-850">
                          <Clock className="w-3 h-3 text-slate-400" />
                          Hace {pedido.haceMinutos} min
                        </span>
                      </div>

                      <div className="border-t border-slate-800/60 pt-2">
                        <ul className="space-y-1">
                          {pedido.items.map((item, i) => (
                            <li key={i} className="text-base font-semibold text-slate-200 flex items-center gap-2">
                              <span className="w-5 h-5 rounded bg-slate-800 text-orange-400 text-xs font-bold flex items-center justify-center shrink-0">
                                {item.cant}
                              </span>
                              {item.nombre}
                            </li>
                          ))}
                        </ul>
                      </div>
                    </div>

                    <button
                      onClick={() => handleAvanzar(pedido.id, pedido.estado)}
                      className="w-full py-2.5 bg-amber-500/10 hover:bg-amber-500 text-amber-500 hover:text-white border border-amber-500/20 hover:border-transparent text-xs font-bold rounded-xl transition flex items-center justify-center gap-1.5 uppercase tracking-wider"
                    >
                      <Play className="w-3.5 h-3.5" />
                      Empezar Cocina
                    </button>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Columna: EN PREPARACIÓN */}
          <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-5 flex flex-col h-[calc(100vh-210px)] overflow-hidden">
            <div className="flex justify-between items-center pb-4 border-b border-slate-800 shrink-0 mb-4">
              <h3 className="font-bold text-sm tracking-wider text-slate-400 uppercase flex items-center gap-2">
                <Flame className="w-4 h-4 text-orange-500 animate-bounce" />
                En Preparación
              </h3>
              <span className="bg-slate-800 text-orange-500 text-xs px-2.5 py-0.5 rounded-full font-bold">
                {getColumnaFiltro('preparacion').length}
              </span>
            </div>

            <div className="flex-1 overflow-y-auto space-y-4 pr-1">
              {getColumnaFiltro('preparacion').length === 0 ? (
                <div className="flex flex-col items-center justify-center h-40 border border-dashed border-slate-800 rounded-xl text-slate-600 text-xs gap-2">
                  <ChefHat className="w-6 h-6 opacity-40 animate-pulse" />
                  Sin platos en preparación
                </div>
              ) : (
                getColumnaFiltro('preparacion').map((pedido) => (
                  <div 
                    key={pedido.id} 
                    className={`bg-slate-900 border border-slate-800 rounded-xl p-5 hover:border-slate-700/50 flex flex-col justify-between gap-4 transition-all duration-300 ease-out transform ${
                      pedido.id === evaporandoId 
                        ? 'opacity-0 scale-95' 
                        : 'hover:-translate-y-1 hover:shadow-lg hover:shadow-slate-900/50'
                    }`}
                  >
                    <div className="space-y-3">
                      <div className="flex justify-between items-start">
                        <div>
                          <span className="text-xs text-orange-500 font-extrabold tracking-wider">PEDIDO #{pedido.id}</span>
                          <h4 className="font-bold text-sm text-slate-100 mt-0.5">{pedido.cliente}</h4>
                        </div>
                        <span className="text-[10px] text-orange-400 flex items-center gap-1.5 bg-orange-50/5 px-2 py-1 rounded-md border border-orange-500/10">
                          <Flame className="w-3 h-3 text-orange-400" />
                          Hace {pedido.haceMinutos} min
                        </span>
                      </div>

                      <div className="border-t border-slate-800/60 pt-2">
                        <ul className="space-y-1">
                          {pedido.items.map((item, i) => (
                            <li key={i} className="text-base font-semibold text-slate-200 flex items-center gap-2">
                              <span className="w-5 h-5 rounded bg-slate-800 text-orange-400 text-xs font-bold flex items-center justify-center shrink-0">
                                {item.cant}
                              </span>
                              {item.nombre}
                            </li>
                          ))}
                        </ul>
                      </div>
                    </div>

                    <button
                      onClick={() => handleAvanzar(pedido.id, pedido.estado)}
                      className="w-full py-2.5 bg-orange-500 hover:bg-orange-600 text-white text-xs font-bold rounded-xl transition flex items-center justify-center gap-1.5 uppercase tracking-wider shadow-lg shadow-orange-500/10 active:scale-95"
                    >
                      <Check className="w-3.5 h-3.5" />
                      Marcar como Listo
                    </button>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Columna: LISTO */}
          <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-5 flex flex-col h-[calc(100vh-210px)] overflow-hidden">
            <div className="flex justify-between items-center pb-4 border-b border-slate-800 shrink-0 mb-4">
              <h3 className="font-bold text-sm tracking-wider text-slate-400 uppercase flex items-center gap-2">
                <CheckCircle className="w-4 h-4 text-emerald-500" />
                Listo
              </h3>
              <span className="bg-slate-800 text-emerald-500 text-xs px-2.5 py-0.5 rounded-full font-bold">
                {getColumnaFiltro('listo').length}
              </span>
            </div>

            <div className="flex-1 overflow-y-auto space-y-4 pr-1 flex flex-col justify-between">
              {getColumnaFiltro('listo').length === 0 ? (
                <>
                  <div className="flex flex-col items-center justify-center h-40 border border-dashed border-slate-800 rounded-xl text-slate-600 text-xs gap-2">
                    <CheckCircle className="w-6 h-6 opacity-40" />
                    Ningún pedido por entregar
                  </div>
                  
                  {/* Estadísticas del turno (Anti-Vacío) */}
                  <div className="bg-slate-900/40 border border-slate-800/80 rounded-xl p-4 text-center mt-auto">
                    <p className="text-xs font-semibold text-slate-400">Eficiencia del Turno: 94%</p>
                    <p className="text-[10px] text-slate-500 mt-1">Tiempo prom. de despacho: 12 min</p>
                  </div>
                </>
              ) : (
                getColumnaFiltro('listo').map((pedido) => (
                  <div 
                    key={pedido.id} 
                    className={`bg-slate-900 border border-slate-800 rounded-xl p-5 hover:border-slate-700/50 flex flex-col justify-between gap-4 border-emerald-500/10 hover:border-emerald-500/30 transition-all duration-300 ease-out transform ${
                      pedido.id === evaporandoId 
                        ? 'opacity-0 scale-95' 
                        : 'hover:-translate-y-1 hover:shadow-lg hover:shadow-slate-900/50'
                    }`}
                  >
                    <div className="space-y-3">
                      <div className="flex justify-between items-start">
                        <div>
                          <span className="text-xs text-emerald-500 font-extrabold tracking-wider">PEDIDO #{pedido.id}</span>
                          <h4 className="font-bold text-sm text-slate-100 mt-0.5">{pedido.cliente}</h4>
                        </div>
                        <span className="text-[10px] text-emerald-400 flex items-center gap-1.5 bg-emerald-50/5 px-2 py-1 rounded-md border border-emerald-500/10">
                          <CheckCircle className="w-3 h-3 text-emerald-400" />
                          Despachado
                        </span>
                      </div>

                      <div className="border-t border-slate-800/60 pt-2">
                        <ul className="space-y-1">
                          {pedido.items.map((item, i) => (
                            <li key={i} className="text-base font-semibold text-slate-200 flex items-center gap-2">
                              <span className="w-5 h-5 rounded bg-slate-800 text-orange-400 text-xs font-bold flex items-center justify-center shrink-0">
                                {item.cant}
                              </span>
                              {item.nombre}
                            </li>
                          ))}
                        </ul>
                      </div>
                    </div>

                    <button
                      onClick={() => handleAvanzar(pedido.id, pedido.estado)}
                      className="w-full py-2.5 bg-emerald-500 hover:bg-emerald-600 text-white text-xs font-bold rounded-xl transition flex items-center justify-center gap-1.5 uppercase tracking-wider shadow-lg shadow-emerald-500/10 active:scale-95"
                    >
                      <CheckCircle className="w-3.5 h-3.5" />
                      Entregar / Despachar
                    </button>
                  </div>
                ))
              )}
            </div>
          </div>

        </div>
      )}
    </div>
  );
}

export default PedidosCocina;
