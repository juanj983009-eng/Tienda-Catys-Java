import React from 'react';

function SkeletonCard() {
  return (
    <div className="bg-slate-800/65 border border-slate-750/70 rounded-2xl p-4 flex flex-col justify-between animate-pulse h-full min-h-[260px]">
      <div>
        {/* Contenedor redondeado de la imagen */}
        <div className="bg-slate-700/30 rounded-xl h-28 w-full mb-4"></div>
        
        {/* Nombre y Precio (Barras) */}
        <div className="flex justify-between items-start gap-4">
          {/* Barra gruesa para el nombre */}
          <div className="h-5 bg-slate-700/40 rounded-lg w-2/3"></div>
          {/* Barra para el precio */}
          <div className="h-5 bg-slate-700/40 rounded-lg w-1/4"></div>
        </div>
      </div>

      <div className="mt-4 flex items-center justify-between">
        {/* Indicador de stock */}
        <div className="flex flex-col gap-1.5">
          <div className="h-2 bg-slate-700/30 rounded w-8"></div>
          <div className="h-3 bg-slate-700/40 rounded w-16"></div>
        </div>

        {/* Botón de agregar */}
        <div className="h-8 bg-slate-700/40 rounded-xl w-16"></div>
      </div>
    </div>
  );
}

export default SkeletonCard;
