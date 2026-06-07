import React from 'react';
import { ShoppingCart, Heart, Info, Star } from 'lucide-react';

export function ProductGrid({ products = [], onAddToCart }) {
  // Default mock products if none provided
  const defaultProducts = [
    {
      id: 1,
      nombre: 'Hamburguesa Catys Especial',
      categoria: 'Hamburguesas',
      precio: 12.50,
      stock: 15,
      imagen: '🍔',
      calificacion: 4.8,
      popular: true,
    },
    {
      id: 2,
      nombre: 'Papas Fritas Crujientes',
      categoria: 'Acompañamientos',
      precio: 4.50,
      stock: 40,
      imagen: '🍟',
      calificacion: 4.5,
      popular: false,
    },
    {
      id: 3,
      nombre: 'Malteada de Fresa Premium',
      categoria: 'Bebidas',
      precio: 6.00,
      stock: 12,
      imagen: '🥤',
      calificacion: 4.9,
      popular: true,
    },
    {
      id: 4,
      nombre: 'Pizza Peperoni Familiar',
      categoria: 'Pizzas',
      precio: 18.90,
      stock: 8,
      imagen: '🍕',
      calificacion: 4.7,
      popular: false,
    },
    {
      id: 5,
      nombre: 'Club Sándwich Tres Pisos',
      categoria: 'Sándwiches',
      precio: 9.50,
      stock: 20,
      imagen: '🥪',
      calificacion: 4.4,
      popular: false,
    },
    {
      id: 6,
      nombre: 'Tarta de Chocolate Belga',
      categoria: 'Postres',
      precio: 5.50,
      stock: 10,
      imagen: '🍰',
      calificacion: 4.9,
      popular: true,
    },
  ];

  const itemsToRender = products.length > 0 ? products : defaultProducts;

  return (
    <div className="space-y-6">
      {/* Grid container */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {itemsToRender.map((product) => (
          <div
            key={product.id}
            className="group relative bg-slate-900/60 border border-slate-800 hover:border-amber-500/40 rounded-2xl overflow-hidden transition-all duration-300 hover:-translate-y-1 hover:shadow-xl hover:shadow-amber-500/5 flex flex-col justify-between"
          >
            {/* Tag Popular */}
            {product.popular && (
              <span className="absolute top-3 left-3 bg-amber-500/95 text-slate-950 font-bold text-[10px] tracking-wider uppercase px-2 py-0.5 rounded-md shadow-sm z-10">
                Popular
              </span>
            )}

            {/* Area de Imagen / Emoji */}
            <div className="h-44 bg-slate-950 flex items-center justify-center text-6xl relative select-none overflow-hidden transition-colors group-hover:bg-slate-900">
              <span className="transform transition-transform duration-300 group-hover:scale-110">
                {product.imagen}
              </span>
              
              {/* Overlay con detalles rápidos */}
              <div className="absolute inset-0 bg-slate-950/70 opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-center justify-center gap-3">
                <button 
                  className="w-10 h-10 rounded-full bg-slate-800 hover:bg-slate-700 text-slate-200 flex items-center justify-center transition-colors"
                  title="Ver Detalles"
                >
                  <Info className="w-5 h-5" />
                </button>
                <button 
                  className="w-10 h-10 rounded-full bg-slate-800 hover:bg-slate-700 text-slate-200 flex items-center justify-center transition-colors"
                  title="Añadir a Favoritos"
                >
                  <Heart className="w-5 h-5" />
                </button>
              </div>
            </div>

            {/* Contenido / Info del producto */}
            <div className="p-5 flex-1 flex flex-col justify-between gap-4">
              <div>
                <div className="flex items-center justify-between gap-2 mb-1.5">
                  <span className="text-[10px] font-bold text-amber-500/90 uppercase tracking-widest">
                    {product.categoria}
                  </span>
                  <div className="flex items-center gap-1 text-[11px] text-slate-400 font-semibold">
                    <Star className="w-3.5 h-3.5 text-amber-400 fill-amber-400" />
                    <span>{product.calificacion}</span>
                  </div>
                </div>

                <h3 className="font-bold text-slate-200 group-hover:text-slate-100 transition-colors line-clamp-2">
                  {product.nombre}
                </h3>
              </div>

              {/* Fila de precio e interacción */}
              <div className="flex items-center justify-between mt-auto">
                <div className="flex flex-col">
                  <span className="text-[10px] text-slate-500 font-medium">Precio</span>
                  <span className="text-lg font-black text-slate-100">
                    ${product.precio.toFixed(2)}
                  </span>
                </div>

                <button
                  onClick={() => onAddToCart && onAddToCart(product)}
                  className="px-3 py-2 bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold rounded-xl flex items-center gap-1.5 transition-all duration-200 active:scale-95 text-xs shadow-md shadow-amber-500/10 cursor-pointer"
                >
                  <ShoppingCart className="w-4 h-4" />
                  <span>Añadir</span>
                </button>
              </div>
            </div>
            
            {/* Barra de Stock */}
            <div className="px-5 py-2.5 bg-slate-900/30 border-t border-slate-800/40 text-[10px] text-slate-400 flex items-center justify-between">
              <span>Stock disponible</span>
              <span className={`font-bold ${product.stock <= 10 ? 'text-red-400' : 'text-slate-300'}`}>
                {product.stock} u.
              </span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
export default ProductGrid;
