import React, { useState } from 'react';
import { Boxes, AlertTriangle, Plus, Minus, Search, Sparkles, TrendingUp, CheckCircle, XCircle } from 'lucide-react';

function InventarioInsumos() {
  const [insumos, setInsumos] = useState([
    { id: 1, nombre: "Lomo Fino (Carne)", stockActual: 12, stockMinimo: 5, unidad: "kg", categoria: "Proteínas" },
    { id: 2, nombre: "Arroz Extra", stockActual: 4, stockMinimo: 10, unidad: "kg", categoria: "Abarrotes" },
    { id: 3, nombre: "Papa Amarilla", stockActual: 45, stockMinimo: 15, unidad: "kg", categoria: "Verduras" },
    { id: 4, nombre: "Chicha Morada Concentrada", stockActual: 8, stockMinimo: 3, unidad: "Litros", categoria: "Bebidas" }
  ]);

  // Form states
  const [insumoSeleccionadoId, setInsumoSeleccionadoId] = useState('');
  const [cantidad, setCantidad] = useState('');
  
  // Search & Filter states
  const [buscar, setBuscar] = useState('');
  const [categoriaActiva, setCategoriaActiva] = useState('TODOS');

  // Simple Notification Toast State
  const [notificacion, setNotificacion] = useState({ mensaje: '', tipo: '', visible: false });

  const mostrarMensaje = (mensaje, tipo) => {
    setNotificacion({ mensaje, tipo, visible: true });
    setTimeout(() => {
      setNotificacion(prev => ({ ...prev, visible: false }));
    }, 4000);
  };

  const handleAjuste = (tipo) => {
    const id = Number(insumoSeleccionadoId);
    if (!id) {
      mostrarMensaje('Seleccione un insumo para realizar el ajuste', 'error');
      return;
    }
    const cantNum = parseFloat(cantidad);
    if (isNaN(cantNum) || cantNum <= 0) {
      mostrarMensaje('Ingrese una cantidad válida mayor a 0', 'error');
      return;
    }

    setInsumos(prevInsumos => prevInsumos.map(ins => {
      if (ins.id === id) {
        let nuevoStock = ins.stockActual;
        if (tipo === 'ingreso') {
          nuevoStock += cantNum;
          mostrarMensaje(`Kardex actualizado: +${cantNum} ${ins.unidad} de ${ins.nombre}`, 'success');
        } else if (tipo === 'merma') {
          if (nuevoStock < cantNum) {
            mostrarMensaje(`Error: Merma supera el stock actual (${ins.stockActual} ${ins.unidad})`, 'error');
            return ins;
          }
          nuevoStock -= cantNum;
          mostrarMensaje(`Merma registrada: -${cantNum} ${ins.unidad} de ${ins.nombre}`, 'success');
        }
        return { ...ins, stockActual: Math.round(nuevoStock * 100) / 100 };
      }
      return ins;
    }));

    setCantidad('');
  };

  // Unique categories for filtering
  const categorias = ['TODOS', ...new Set(insumos.map(i => i.categoria))];

  // Filter list
  const insumosFiltrados = insumos.filter(ins => {
    const coincideBusqueda = ins.nombre.toLowerCase().includes(buscar.toLowerCase()) ||
                             ins.categoria.toLowerCase().includes(buscar.toLowerCase());
    const coincideCategoria = categoriaActiva === 'TODOS' || ins.categoria === categoriaActiva;
    return coincideBusqueda && coincideCategoria;
  });

  return (
    <div className="flex flex-col lg:flex-row gap-6 p-6 bg-slate-950 min-h-screen text-slate-100 w-full overflow-y-auto">
      
      {/* Toast Notification */}
      {notificacion.visible && (
        <div className={`fixed bottom-6 right-6 z-55 flex items-center gap-3 px-4 py-3 rounded-xl border backdrop-blur-md transition-all duration-300 shadow-xl ${
          notificacion.tipo === 'success' 
            ? 'bg-emerald-950/80 border-emerald-500/30 text-emerald-350' 
            : 'bg-rose-950/80 border-rose-500/30 text-rose-350'
        }`}>
          {notificacion.tipo === 'success' ? (
            <CheckCircle className="w-5 h-5 text-emerald-450 shrink-0" />
          ) : (
            <XCircle className="w-5 h-5 text-rose-450 shrink-0" />
          )}
          <span className="text-xs font-semibold">{notificacion.mensaje}</span>
        </div>
      )}

      {/* Panel Izquierdo: Monitor de Stock (2/3 de ancho) */}
      <div className="w-full lg:w-2/3 flex flex-col gap-4">
        
        {/* Filtros e Inputs */}
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center pb-4 border-b border-slate-800 gap-4">
          <div className="flex gap-2 overflow-x-auto pb-1 max-w-full">
            {categorias.map(cat => (
              <button
                key={cat}
                onClick={() => setCategoriaActiva(cat)}
                className={`px-3 py-1.5 rounded-xl text-[10px] font-bold uppercase tracking-wider transition-all whitespace-nowrap cursor-pointer ${
                  categoriaActiva === cat
                    ? 'bg-amber-500 text-slate-950 shadow-lg shadow-amber-500/10'
                    : 'bg-slate-900 border border-slate-800 text-slate-400 hover:text-slate-200'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>

          <div className="relative w-full md:w-64">
            <span className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Search className="h-4 w-4 text-slate-500" />
            </span>
            <input
              type="text"
              value={buscar}
              onChange={(e) => setBuscar(e.target.value)}
              placeholder="Buscar insumo..."
              className="w-full bg-slate-900 border border-slate-800 rounded-xl pl-9 pr-3 py-1.5 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-amber-500 transition-colors"
            />
          </div>
        </div>

        {/* Grid de Insumos */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {insumosFiltrados.map((item) => {
            const stockBajo = item.stockActual < item.stockMinimo;
            const porcentaje = Math.min(100, Math.round((item.stockActual / (item.stockMinimo * 2.5)) * 100));

            return (
              <div
                key={item.id}
                className={`transition-all duration-300 ease-out transform hover:-translate-y-0.5 hover:border-slate-700 hover:shadow-md p-5 rounded-2xl flex flex-col justify-between border relative group ${
                  stockBajo 
                    ? 'border-rose-500/30 bg-rose-950/10' 
                    : 'bg-slate-900/40 border-slate-800'
                }`}
              >
                {/* Cabecera Tarjeta */}
                <div className="flex justify-between items-start gap-2">
                  <div>
                    <h4 className="font-bold text-slate-200 text-sm tracking-wide group-hover:text-amber-400 transition-colors">
                      {item.nombre}
                    </h4>
                    <span className="text-[10px] text-slate-500 font-semibold uppercase tracking-wider block mt-0.5">
                      {item.categoria}
                    </span>
                  </div>
                  
                  {/* Badge de Stock */}
                  <span className={`text-[10px] font-bold px-2.5 py-1 rounded-md shrink-0 border ${
                    stockBajo 
                      ? 'text-rose-450 bg-rose-500/10 border-rose-500/15 animate-pulse' 
                      : 'text-emerald-400 bg-emerald-500/10 border-emerald-500/15'
                  }`}>
                    {item.stockActual} {item.unidad}
                  </span>
                </div>

                {/* Info de Stock Mínimo y Alertas */}
                <div className="mt-4 space-y-3">
                  <div className="flex justify-between items-center text-xs text-slate-400">
                    <span className="text-slate-500">Mínimo requerido:</span>
                    <span className="font-mono text-slate-350">{item.stockMinimo} {item.unidad}</span>
                  </div>

                  {/* Barra de progreso visual */}
                  <div className="space-y-1">
                    <div className="flex justify-between text-[10px] text-slate-500">
                      <span>Nivel de Seguridad</span>
                      <span>{porcentaje}%</span>
                    </div>
                    <div className="w-full h-2 bg-slate-950 rounded-full overflow-hidden border border-slate-850">
                      <div 
                        style={{ width: `${porcentaje}%` }}
                        className={`h-full rounded-full transition-all duration-500 ${
                          stockBajo ? 'bg-rose-500' : 'bg-emerald-500'
                        }`}
                      />
                    </div>
                  </div>
                </div>

                {/* Alerta inferior si el stock está por debajo del mínimo */}
                {stockBajo && (
                  <div className="mt-4 flex items-center gap-1.5 text-[10px] text-rose-400 font-bold bg-rose-500/5 p-2 rounded-lg border border-rose-500/10">
                    <AlertTriangle className="w-3.5 h-3.5 text-rose-500 animate-pulse" />
                    <span>¡Alerta! Stock crítico en almacén.</span>
                  </div>
                )}

                {/* Overlay decorativo */}
                <div className="absolute inset-0 bg-slate-100/[0.01] opacity-0 group-hover:opacity-100 rounded-2xl pointer-events-none transition-opacity duration-300" />
              </div>
            );
          })}
        </div>

        {/* Estado Vacío */}
        {insumosFiltrados.length === 0 && (
          <div className="flex flex-col items-center justify-center py-20 text-slate-500 border border-dashed border-slate-800 rounded-2xl bg-slate-900/10">
            <Boxes className="w-10 h-10 text-slate-700 animate-pulse mb-3" />
            <span className="text-xs font-semibold text-slate-400">No se encontraron insumos</span>
            <p className="text-[10px] text-slate-600 mt-1">Refine sus filtros o cargue insumos en el Kardex.</p>
          </div>
        )}

      </div>

      {/* Panel Derecho: Ajuste de Kardex (1/3 de ancho) */}
      <div className="w-full lg:w-1/3 shrink-0">
        <div 
          className="bg-slate-900/40 border border-slate-800 p-6 rounded-2xl h-fit transition-all duration-300 ease-out transform hover:-translate-y-0.5 hover:border-slate-700 hover:shadow-md"
        >
          <div className="flex items-center gap-3 border-b border-slate-800 pb-4 mb-6">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-amber-500 to-orange-600 flex items-center justify-center shadow-lg shadow-orange-500/10">
              <TrendingUp className="w-5 h-5 text-white" />
            </div>
            <div>
              <h3 className="font-bold text-sm text-slate-200 uppercase tracking-wider">
                Ajuste de Kardex
              </h3>
              <p className="text-[10px] text-slate-500">Movimiento rápido de stock</p>
            </div>
          </div>

          <div className="space-y-4">
            {/* Selector de Insumo */}
            <div>
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1.5">
                Seleccionar Insumo
              </label>
              <select
                value={insumoSeleccionadoId}
                onChange={(e) => setInsumoSeleccionadoId(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 focus:border-amber-500 rounded-xl px-4 py-2.5 text-sm text-slate-100 focus:outline-none focus:ring-1 focus:ring-amber-500 transition-colors cursor-pointer"
              >
                <option value="" className="text-slate-500">-- Seleccionar Insumo --</option>
                {insumos.map((ins) => (
                  <option key={ins.id} value={ins.id} className="text-slate-300 bg-slate-950">
                    {ins.nombre} ({ins.unidad})
                  </option>
                ))}
              </select>
            </div>

            {/* Input de Cantidad */}
            <div>
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1.5">
                Cantidad a ajustar
              </label>
              <input
                type="number"
                min="0"
                step="any"
                value={cantidad}
                onChange={(e) => setCantidad(e.target.value)}
                placeholder="Ej. 5.5"
                className="w-full bg-slate-950 border border-slate-800 focus:border-amber-500 rounded-xl px-4 py-2.5 text-sm text-slate-100 placeholder-slate-650 focus:outline-none focus:ring-1 focus:ring-amber-500 transition-colors font-mono"
              />
            </div>
          </div>

          {/* Botones de Acción de Ajuste */}
          <div className="grid grid-cols-2 gap-4 mt-6">
            <button
              type="button"
              onClick={() => handleAjuste('ingreso')}
              className="bg-emerald-650 hover:bg-emerald-550 text-white font-semibold py-2.5 rounded-lg transition-all cursor-pointer flex items-center justify-center gap-1.5 hover:shadow-lg hover:shadow-emerald-500/10 active:scale-[0.98]"
            >
              <Plus className="w-4 h-4" />
              Ingreso (+)
            </button>
            <button
              type="button"
              onClick={() => handleAjuste('merma')}
              className="bg-rose-650 hover:bg-rose-550 text-white font-semibold py-2.5 rounded-lg transition-all cursor-pointer flex items-center justify-center gap-1.5 hover:shadow-lg hover:shadow-rose-500/10 active:scale-[0.98]"
            >
              <Minus className="w-4 h-4" />
              Merma (-)
            </button>
          </div>

          {/* Caja Informativa Premium de Ayuda */}
          <div className="mt-6 border-t border-slate-800/60 pt-4 text-[10px] text-slate-500 leading-relaxed">
            <span className="font-bold text-slate-400 block mb-1">Notas de Operación:</span>
            Un <strong className="text-emerald-500">Ingreso</strong> suma directamente al stock actual de la materia prima. Una <strong className="text-rose-500">Merma</strong> descuenta del inventario (ej. insumos dañados, desechos o consumo interno).
          </div>
        </div>
      </div>

    </div>
  );
}

export default InventarioInsumos;
