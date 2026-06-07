import React, { useState } from 'react';
import { useInitialHandshake } from './hooks/useInitialHandshake';
import { MainLayout } from './components/layout/MainLayout';
import { ProductGrid } from './components/pos/ProductGrid';
import { Cat, Sparkles, AlertCircle, ShoppingCart } from 'lucide-react';

function App() {
  const { loading, handshakeMessage } = useInitialHandshake();
  const [vistaActual, setVistaActual] = useState('pos');
  const [cartCount, setCartCount] = useState(0);
  const [lastAddedProduct, setLastAddedProduct] = useState(null);

  // Mock de datos para POS
  const mockProducts = [
    { id: 1, nombre: 'Burger Triple Catys', categoria: 'Hamburguesas', precio: 14.90, stock: 12, imagen: '🍔', calificacion: 4.9, popular: true },
    { id: 2, nombre: 'Papas Cheddar & Bacon', categoria: 'Acompañamientos', precio: 6.50, stock: 25, imagen: '🍟', calificacion: 4.7, popular: true },
    { id: 3, nombre: 'Malteada Oreo Exclusiva', categoria: 'Bebidas', precio: 7.00, stock: 15, imagen: '🥤', calificacion: 4.8, popular: false },
    { id: 4, nombre: 'Pizza Catys Suprema', categoria: 'Pizzas', precio: 19.90, stock: 5, imagen: '🍕', calificacion: 5.0, popular: true },
    { id: 5, nombre: 'Alitas BBQ Crujientes', categoria: 'Entradas', precio: 10.50, stock: 18, imagen: '🍗', calificacion: 4.6, popular: false },
    { id: 6, nombre: 'Ensalada Fresca de la Casa', categoria: 'Saludable', precio: 8.00, stock: 20, imagen: '🥗', calificacion: 4.3, popular: false },
    { id: 7, nombre: 'Tarta de Fresa y Limón', categoria: 'Postres', precio: 5.00, stock: 8, imagen: '🍰', calificacion: 4.7, popular: false },
    { id: 8, nombre: 'Café Espresso Doble', categoria: 'Bebidas', precio: 3.50, stock: 50, imagen: '☕', calificacion: 4.9, popular: false }
  ];

  const handleAddToCart = (product) => {
    setCartCount(prev => prev + 1);
    setLastAddedProduct(product.nombre);
    setTimeout(() => setLastAddedProduct(null), 3000);
  };

  // 1. PANTALLA DE CARGA INICIAL (Handshake persistido)
  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen bg-slate-950 text-slate-100 font-sans p-6 select-none">
        <div className="relative flex flex-col items-center max-w-sm w-full text-center">
          {/* Logo animado */}
          <div className="relative mb-8">
            <div className="absolute inset-0 bg-amber-500/20 rounded-3xl filter blur-xl animate-pulse"></div>
            <div className="w-20 h-20 bg-gradient-to-tr from-orange-500 to-amber-500 rounded-3xl flex items-center justify-center shadow-lg shadow-orange-500/20 relative z-10 border border-amber-400/20">
              <Cat className="w-12 h-12 text-white animate-bounce" />
            </div>
          </div>

          {/* Información del Sistema */}
          <h1 className="text-2xl font-black tracking-wide text-slate-100 mb-2">
            Catys Enterprise <span className="text-amber-500">ERP</span>
          </h1>
          <p className="text-xs text-slate-400 font-semibold uppercase tracking-widest mb-6">
            Iniciando Handshake de Seguridad
          </p>

          {/* Barra de progreso animada */}
          <div className="w-full h-1.5 bg-slate-900 rounded-full overflow-hidden mb-4 border border-slate-800">
            <div className="h-full bg-gradient-to-r from-orange-500 to-amber-500 rounded-full animate-[loading_1.5s_ease-in-out_infinite] w-2/3"></div>
          </div>

          {/* Mensaje de estado dinámico */}
          <div className="flex items-center justify-center gap-2 text-sm text-slate-300 font-medium">
            <Sparkles className="w-4 h-4 text-amber-500 animate-spin" />
            <span>{handshakeMessage}</span>
          </div>

          {/* Detalle inferior */}
          <div className="absolute bottom-[-100px] text-[10px] text-slate-600 font-mono">
            SECURE HANDSHAKE // PORT: 443 // CATYS-SYS-v5
          </div>
        </div>
      </div>
    );
  }

  // Estilo para simular animación de barra
  const keyframeStyle = `
    @keyframes loading {
      0% { transform: translateX(-100%); }
      100% { transform: translateX(200%); }
    }
  `;

  // 2. APLICACIÓN PRINCIPAL (Layout + Rutas/Vistas)
  return (
    <>
      <style>{keyframeStyle}</style>
      <MainLayout vistaActual={vistaActual} setVistaActual={setVistaActual}>
        
        {/* Notificación flotante de adición de producto */}
        {lastAddedProduct && (
          <div className="fixed bottom-4 right-4 bg-slate-900 border border-green-500/30 text-green-400 px-4 py-3 rounded-xl shadow-lg z-50 animate-bounce flex items-center gap-2 text-sm font-semibold">
            <Sparkles className="w-4 h-4 text-amber-400" />
            <span>¡{lastAddedProduct} añadido al carrito!</span>
          </div>
        )}

        {/* CONTENIDO DENTRO DE VISTAS */}
        {vistaActual === 'pos' ? (
          <div className="space-y-6">
            {/* Cabecera / Acciones rápidas del POS */}
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-slate-900/40 p-5 rounded-2xl border border-slate-900">
              <div>
                <h3 className="font-bold text-lg text-slate-100 flex items-center gap-2">
                  <span>Productos Disponibles</span>
                  <span className="text-xs bg-amber-500/15 text-amber-400 px-2 py-0.5 rounded-full border border-amber-500/20 font-medium">
                    {mockProducts.length} items
                  </span>
                </h3>
                <p className="text-xs text-slate-400 mt-0.5">Selecciona productos para armar el pedido actual</p>
              </div>

              {/* Botón rápido del carrito */}
              <div className="flex items-center gap-3 w-full sm:w-auto justify-end">
                <div className="flex items-center gap-2 bg-slate-900 border border-slate-800 px-4 py-2.5 rounded-xl">
                  <ShoppingCart className="w-4 h-4 text-amber-500" />
                  <span className="text-sm font-bold text-slate-200">Carrito ({cartCount})</span>
                </div>
                {cartCount > 0 && (
                  <button 
                    onClick={() => setCartCount(0)}
                    className="text-xs text-red-400 hover:text-red-300 font-semibold px-2 py-1 transition-colors"
                  >
                    Vaciar
                  </button>
                )}
              </div>
            </div>

            {/* Render de la Grilla de Productos */}
            <ProductGrid products={mockProducts} onAddToCart={handleAddToCart} />
          </div>
        ) : (
          /* Vista genérica para otras opciones del menú */
          <div className="flex flex-col items-center justify-center py-16 text-center max-w-md mx-auto">
            <div className="w-14 h-14 bg-slate-900 border border-slate-800 rounded-2xl flex items-center justify-center mb-4 text-slate-400">
              <AlertCircle className="w-7 h-7" />
            </div>
            <h3 className="font-bold text-lg text-slate-200">Módulo en Desarrollo</h3>
            <p className="text-sm text-slate-400 mt-2">
              El módulo de <span className="font-semibold text-amber-500">"{vistaActual.toUpperCase()}"</span> está siendo refactorizado por completo en la arquitectura limpia. Usa el menú para volver al Punto de Venta.
            </p>
            <button
              onClick={() => setVistaActual('pos')}
              className="mt-6 px-4 py-2.5 bg-slate-900 hover:bg-slate-850 text-amber-400 font-bold rounded-xl border border-slate-800 transition-all text-sm"
            >
              Volver al Punto de Venta
            </button>
          </div>
        )}
      </MainLayout>
    </>
  );
}

export default App;
