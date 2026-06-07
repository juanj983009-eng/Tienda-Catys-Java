import React from 'react';
import { ShoppingCart, Heart, Info, Star, RefreshCw, PackageX } from 'lucide-react';
import { API_BASE_URL, API_HOST_URL } from '../../config/api';

/**
 * API_HOST — base del servidor Spring Boot (sin el path /api).
 * Permite construir URLs absolutas de imagen:
 */
const API_HOST = API_HOST_URL;

/** Genera iniciales de fallback cuando la imagen no carga. */
function getInitials(nombre = '') {
  const parts = nombre.trim().split(/\s+/);
  return parts.length >= 2
    ? (parts[0][0] + parts[1][0]).toUpperCase()
    : nombre.substring(0, 2).toUpperCase();
}

/** Skeleton animado que se muestra mientras se carga el catálogo. */
function SkeletonCard() {
  return (
    <div className="bg-slate-900/60 border border-slate-800 rounded-2xl overflow-hidden animate-pulse flex flex-col">
      <div className="h-44 bg-slate-800/80" />
      <div className="p-5 space-y-3">
        <div className="h-2 bg-slate-800 rounded-full w-1/3" />
        <div className="h-3.5 bg-slate-800 rounded-full w-3/4" />
        <div className="h-3 bg-slate-800 rounded-full w-1/2" />
        <div className="flex justify-between items-center pt-2">
          <div className="h-5 bg-slate-800 rounded-full w-16" />
          <div className="h-8 bg-slate-800 rounded-xl w-20" />
        </div>
      </div>
      <div className="px-5 py-2.5 bg-slate-900/30 border-t border-slate-800/40 flex justify-between">
        <div className="h-2 bg-slate-800 rounded-full w-24" />
        <div className="h-2 bg-slate-800 rounded-full w-8" />
      </div>
    </div>
  );
}

/**
 * ProductGrid — Grilla responsiva de productos que consume el JSON del backend.
 *
 * Props:
 *   products   — Array normalizado desde la API Spring Boot
 *                { id, nombre, precio, categoria, stock, disponible, imagenUrl, imagen }
 *   loading    — Boolean: muestra skeletons durante la carga inicial
 *   error      — String | null: mensaje de error de red
 *   onRetry    — Función para reintentar el fetch
 *   onAddToCart — Callback(product) al pulsar "Añadir"
 */
export function ProductGrid({ products = [], loading = false, error = null, onRetry, onAddToCart }) {

  // Estado de carga → 8 skeletons
  if (loading) {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {[...Array(8)].map((_, i) => <SkeletonCard key={i} />)}
      </div>
    );
  }

  // Error de red → panel con botón Reintentar
  if (error) {
    return (
      <div className="flex flex-col items-center justify-center py-20 text-center gap-4">
        <div className="w-16 h-16 bg-red-500/10 border border-red-500/20 rounded-2xl flex items-center justify-center">
          <PackageX className="w-8 h-8 text-red-400" />
        </div>
        <div>
          <p className="font-bold text-slate-200 text-sm">No se pudo cargar el catálogo</p>
          <p className="text-xs text-slate-500 mt-1 max-w-xs">{error}</p>
        </div>
        {onRetry && (
          <button
            onClick={onRetry}
            className="flex items-center gap-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl border border-slate-700 transition-all"
          >
            <RefreshCw className="w-4 h-4" />
            Reintentar
          </button>
        )}
      </div>
    );
  }

  // Sin productos
  if (products.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-20 text-center gap-3">
        <div className="w-14 h-14 bg-slate-900 border border-slate-800 rounded-2xl flex items-center justify-center">
          <PackageX className="w-7 h-7 text-slate-500" />
        </div>
        <p className="text-sm text-slate-400 font-medium">Sin productos disponibles en este momento.</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Grid 1-col móvil / 2-col tablet / 4-col desktop */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {products.map((product) => {
          const sinStock  = product.stock <= 0;
          const stockBajo = product.stock > 0 && product.stock <= 5;
          const fullImageUrl = product.imagenUrl ? `${API_HOST}${product.imagenUrl}` : null;

          return (
            <div
              key={product.id}
              className={`group relative bg-slate-900/60 border rounded-2xl overflow-hidden transition-all duration-300 hover:-translate-y-1 hover:shadow-xl flex flex-col justify-between ${
                sinStock
                  ? 'border-slate-800/50 opacity-60'
                  : 'border-slate-800 hover:border-amber-500/40 hover:shadow-amber-500/5'
              }`}
            >
              {/* Badge stock bajo */}
              {stockBajo && (
                <span className="absolute top-3 left-3 bg-red-500 text-white font-bold text-[9px] tracking-wider uppercase px-2 py-0.5 rounded-md shadow-sm z-10 animate-pulse border border-red-400">
                  ¡Últimas unidades!
                </span>
              )}
              {/* Badge agotado */}
              {sinStock && (
                <span className="absolute top-3 left-3 bg-slate-700 text-slate-300 font-bold text-[9px] tracking-wider uppercase px-2 py-0.5 rounded-md z-10">
                  Agotado
                </span>
              )}

              {/* Área de imagen */}
              <div className="h-44 bg-slate-950 flex items-center justify-center relative select-none overflow-hidden transition-colors group-hover:bg-slate-900">
                {fullImageUrl ? (
                  <>
                    <img
                      src={fullImageUrl}
                      alt={product.nombre}
                      className="object-cover h-full w-full transform transition-transform duration-300 group-hover:scale-110"
                      onError={(e) => {
                        e.currentTarget.style.display = 'none';
                        const fb = e.currentTarget.nextSibling;
                        if (fb) fb.style.display = 'flex';
                      }}
                    />
                    {/* Fallback de iniciales */}
                    <div
                      style={{ display: 'none' }}
                      className="absolute inset-0 bg-gradient-to-tr from-orange-500/10 to-amber-500/5 text-orange-400 font-black text-4xl items-center justify-center"
                    >
                      {getInitials(product.nombre)}
                    </div>
                  </>
                ) : (
                  <span className="text-orange-400 font-black text-4xl transform transition-transform duration-300 group-hover:scale-110">
                    {getInitials(product.nombre)}
                  </span>
                )}

                {/* Acciones rápidas en hover */}
                <div className="absolute inset-0 bg-slate-950/70 opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-center justify-center gap-3">
                  <button className="w-10 h-10 rounded-full bg-slate-800 hover:bg-slate-700 text-slate-200 flex items-center justify-center transition-colors" title="Ver Detalles">
                    <Info className="w-5 h-5" />
                  </button>
                  <button className="w-10 h-10 rounded-full bg-slate-800 hover:bg-slate-700 text-slate-200 flex items-center justify-center transition-colors" title="Favoritos">
                    <Heart className="w-5 h-5" />
                  </button>
                </div>
              </div>

              {/* Información */}
              <div className="p-5 flex-1 flex flex-col justify-between gap-4">
                <div>
                  <div className="flex items-center justify-between gap-2 mb-1.5">
                    <span className="text-[10px] font-bold text-amber-500/90 uppercase tracking-widest">
                      {product.categoria}
                    </span>
                    <div className="flex items-center gap-1 text-[11px] text-slate-400 font-semibold">
                      <Star className="w-3.5 h-3.5 text-amber-400 fill-amber-400" />
                      <span>5.0</span>
                    </div>
                  </div>
                  <h3 className="font-bold text-slate-200 group-hover:text-slate-100 transition-colors line-clamp-2">
                    {product.nombre}
                  </h3>
                </div>

                {/* Precio en Soles + botón Añadir */}
                <div className="flex items-center justify-between mt-auto">
                  <div className="flex flex-col">
                    <span className="text-[10px] text-slate-500 font-medium">Precio</span>
                    <span className="text-lg font-black text-slate-100">
                      S/ {product.precio.toFixed(2)}
                    </span>
                  </div>
                  <button
                    onClick={() => !sinStock && onAddToCart && onAddToCart(product)}
                    disabled={sinStock}
                    className={`px-3 py-2 font-bold rounded-xl flex items-center gap-1.5 transition-all duration-200 text-xs shadow-md ${
                      sinStock
                        ? 'bg-slate-800 text-slate-600 cursor-not-allowed'
                        : 'bg-amber-500 hover:bg-amber-600 text-slate-950 active:scale-95 shadow-amber-500/10 cursor-pointer'
                    }`}
                  >
                    <ShoppingCart className="w-4 h-4" />
                    <span>{sinStock ? 'Agotado' : 'Añadir'}</span>
                  </button>
                </div>
              </div>

              {/* Barra de stock */}
              <div className="px-5 py-2.5 bg-slate-900/30 border-t border-slate-800/40 text-[10px] text-slate-400 flex items-center justify-between">
                <span>Stock disponible</span>
                <span className={`font-bold ${sinStock ? 'text-red-500' : stockBajo ? 'text-yellow-400' : 'text-slate-300'}`}>
                  {sinStock ? 'Agotado' : `${product.stock} u.`}
                </span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default ProductGrid;
