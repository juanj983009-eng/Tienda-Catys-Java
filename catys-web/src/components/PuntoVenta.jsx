import React, { useState, useEffect } from 'react';
import { ShoppingCart, X, Banknote, CreditCard, Smartphone, ChevronDown, Check } from 'lucide-react';
import { API_BASE_URL, API_HOST_URL } from '../config/api';
import SkeletonCard from './ui/SkeletonCard';

function PuntoVenta() {
  const productosIniciales = [];

  const [productos, setProductos] = useState(productosIniciales);
  const [categoriaActiva, setCategoriaActiva] = useState('TODOS');
  const [carrito, setCarrito] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [notificacion, setNotificacion] = useState({ mensaje: '', tipo: 'success', visible: false });
  const [clienteSeleccionado, setClienteSeleccionado] = useState(null);

  const clientesVIP = [
    { id: 1, nombre: "Juan José Parra", dni: "12345678", porcentajeDescuento: 0.05 },
    { id: 2, nombre: "Aníbal Torres", dni: "87654321", porcentajeDescuento: 0.05 }
  ];

  // Campos adicionales de la Venta/Ticket
  const [tipoComprobante, setTipoComprobante] = useState('BOLETA');
  const [metodoPago, setMetodoPago] = useState('EFECTIVO');
  const [nombreCliente, setNombreCliente] = useState('Público General');
  const [docCliente, setDocCliente] = useState('');

  // Snapshot para el ticket de impresión
  const [ticketImpresion, setTicketImpresion] = useState(null);

  // Control de apertura del Drawer en Móviles
  const [drawerOpen, setDrawerOpen] = useState(false);

  // Estado para la animación reactiva del carrito
  const [cartBadgeActive, setCartBadgeActive] = useState(false);

  // Estados de control para Dropdowns personalizados
  const [comprobanteOpen, setComprobanteOpen] = useState(false);
  const [pagoOpen, setPagoOpen] = useState(false);
  const [clienteOpen, setClienteOpen] = useState(false);

  const triggerCartBadgeAnimation = () => {
    setCartBadgeActive(true);
    setTimeout(() => {
      setCartBadgeActive(false);
    }, 600);
  };

  // Cargar productos al iniciar
  const cargarProductos = async () => {
    try {
      setLoading(true);
      const res = await fetch(`${API_BASE_URL}/productos/buscar`);
      if (!res.ok) throw new Error('Error al cargar catálogo de productos');
      const data = await res.json();
      
      // Normalize category names (CRIOLLO -> CRIOLLA) and generate initials
      const normalizedData = data.map(p => {
        let cat = p.categoria ? p.categoria.toUpperCase() : '';
        if (cat === 'CRIOLLO') cat = 'CRIOLLA';

        // Match initial mapping
        let initials = p.iniciales || '';
        const matchedMock = productosIniciales.find(mock => mock.nombre.toLowerCase() === p.nombre.toLowerCase());
        if (matchedMock) {
          cat = matchedMock.categoria;
          initials = matchedMock.iniciales;
        } else if (!initials && p.nombre) {
          const parts = p.nombre.trim().split(/\s+/);
          initials = parts.length >= 2 
            ? (parts[0][0] + parts[1][0]).toUpperCase() 
            : p.nombre.substring(0, 2).toUpperCase();
        }

        const id = p.id || p.idProducto || (matchedMock ? matchedMock.id : null);

        return {
          ...p,
          id,
          categoria: cat,
          iniciales: initials
        };
      });

      // Merge: if present in normalizedData (by name), override. If not, add.
      const finalProducts = [...normalizedData];
      productosIniciales.forEach(plato => {
        const idx = finalProducts.findIndex(p => p.nombre.toLowerCase() === plato.nombre.toLowerCase());
        if (idx >= 0) {
          finalProducts[idx] = { ...plato, ...finalProducts[idx] };
        } else {
          finalProducts.push(plato);
        }
      });

      setProductos(finalProducts);
      setError(null);
    } catch (err) {
      logError(err.message);
      setError(err.message);
      // Fallback in case of server failure
      setProductos(productosIniciales);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarProductos();
  }, []);

  const logError = (msg) => {
    console.error(msg);
  };

  // Agregar producto al carrito
  const agregarAlCarrito = (producto) => {
    if (producto.stock <= 0) {
      mostrarMensaje('Producto sin stock disponible', 'error');
      return;
    }

    triggerCartBadgeAnimation();

    setCarrito((prevCarrito) => {
      const existente = prevCarrito.find((item) => item.idProducto === producto.id);
      if (existente) {
        if (existente.cantidad >= producto.stock) {
          mostrarMensaje(`Solo quedan ${producto.stock} unidades en stock`, 'error');
          return prevCarrito;
        }
        return prevCarrito.map((item) =>
          item.idProducto === producto.id
            ? { ...item, cantidad: item.cantidad + 1 }
            : item
        );
      }
      return [
        ...prevCarrito,
        {
          idProducto: producto.id,
          nombre: producto.nombre,
          cantidad: 1,
          precioUnitario: producto.precio,
          stockMax: producto.stock,
        },
      ];
    });
  };

  // Modificar cantidad en la ticketera
  const cambiarCantidad = (idProducto, delta) => {
    if (delta > 0) {
      triggerCartBadgeAnimation();
    }
    setCarrito((prevCarrito) => {
      return prevCarrito
        .map((item) => {
          if (item.idProducto === idProducto) {
            const nuevaCantidad = item.cantidad + delta;
            if (nuevaCantidad > item.stockMax) {
              mostrarMensaje(`Límite de stock alcanzado (${item.stockMax} disponibles)`, 'error');
              return item;
            }
            return { ...item, cantidad: nuevaCantidad };
          }
          return item;
        })
        .filter((item) => item.cantidad > 0);
    });
  };

  // Mostrar alertas temporales (Toast)
  const mostrarMensaje = (texto, tipo = 'success') => {
    setNotificacion({ mensaje: texto, tipo, visible: true });
  };

  // Efecto para ocultar la notificación automáticamente tras 4 segundos
  useEffect(() => {
    if (notificacion.visible) {
      const timer = setTimeout(() => {
        setNotificacion((prev) => ({ ...prev, visible: false }));
      }, 4000);
      return () => clearTimeout(timer);
    }
  }, [notificacion.visible]);

  // Confirmar la venta e imprimir ticket
  const handleConfirmarVenta = () => {
    if (carrito.length === 0) {
      mostrarMensaje('El carrito está vacío', 'error');
      return;
    }

    const payload = {
      cliente: clienteSeleccionado ? clienteSeleccionado.nombre : 'Público General',
      clienteId: clienteSeleccionado ? clienteSeleccionado.id : null,
      idMesa: null,
      metodoPago: metodoPago,
      items: carrito.map(({ idProducto, nombre, cantidad, precioUnitario }) => ({
        idProducto,
        nombre,
        cantidad,
        precioUnitario: clienteSeleccionado ? precioUnitario * (1 - clienteSeleccionado.porcentajeDescuento) : precioUnitario,
      })),
    };

    fetch(`${API_BASE_URL}/ventas/confirmar`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })
      .then((res) => {
        if (!res.ok) {
          return res.json().then((data) => {
            throw new Error(data.error || 'Error al procesar la venta');
          });
        }
        return res.json();
      })
      .then((data) => {
        mostrarMensaje('¡Pedido procesado con éxito!', 'success');

        // Crear snapshot para la impresión
        const ticketData = {
          fecha: new Date().toLocaleString(),
          tipoComprobante,
          cliente: nombreCliente.trim() || 'Público General',
          docCliente: docCliente.trim(),
          metodoPago,
          items: [...carrito],
          subtotal,
          descuento,
          total: totalFinal,
        };
        setTicketImpresion(ticketData);

        // Disparar flujo de impresión nativo del navegador tras un leve retraso de renderizado
        setTimeout(() => {
          window.print();
          // Resetear estados tras impresión
          setCarrito([]);
          setTicketImpresion(null);
          setNombreCliente('Público General');
          setDocCliente('');
          setClienteSeleccionado(null);
          setTipoComprobante('BOLETA');
          setMetodoPago('EFECTIVO');
          setDrawerOpen(false); // Cerrar drawer en móvil
          cargarProductos(); // Recargar stock del catálogo
        }, 250);
      })
      .catch((err) => {
        console.error("Error al registrar venta:", err);
        mostrarMensaje('Error interno: No se pudo registrar la venta', 'error');
      });
  };

  const handleSelectCliente = (e) => {
    const idStr = e.target.value;
    if (!idStr) {
      setClienteSeleccionado(null);
      setNombreCliente('Público General');
      setDocCliente('');
    } else {
      const cliente = clientesVIP.find((c) => c.id === parseInt(idStr, 10));
      if (cliente) {
        setClienteSeleccionado(cliente);
        setNombreCliente(cliente.nombre);
        setDocCliente(cliente.dni);
      }
    }
  };

  // Filtrar productos
  const categorias = ['TODOS', 'ENTRADAS', 'CRIOLLA', 'BEBIDAS'];
  const productosFiltrados = categoriaActiva === 'TODOS'
    ? productos
    : productos.filter((p) => p.categoria?.toUpperCase() === categoriaActiva);

  // Calcular total, subtotal y descuento dinámicamente
  const subtotal = carrito.reduce((sum, item) => sum + item.cantidad * item.precioUnitario, 0);
  const descuento = clienteSeleccionado ? subtotal * clienteSeleccionado.porcentajeDescuento : 0;
  const totalFinal = subtotal - descuento;

  // Helpers para Iconografía y Labels de Método de Pago
  const getPagoIcon = (metodo) => {
    switch (metodo) {
      case 'EFECTIVO':
        return <Banknote className="w-4 h-4 text-emerald-400 shrink-0" />;
      case 'TARJETA_CREDITO':
        return <CreditCard className="w-4 h-4 text-blue-400 shrink-0" />;
      case 'YAPE_PLIN':
        return <Smartphone className="w-4 h-4 text-fuchsia-400 shrink-0" />;
      default:
        return null;
    }
  };

  const getPagoLabel = (metodo) => {
    switch (metodo) {
      case 'EFECTIVO':
        return 'EFECTIVO';
      case 'TARJETA_CREDITO':
        return 'TARJETA';
      case 'YAPE_PLIN':
        return 'YAPE/PLIN';
      default:
        return metodo;
    }
  };

  const getClienteLabel = () => {
    return clienteSeleccionado ? clienteSeleccionado.nombre : 'Público General';
  };

  // Renderizador unificado para el Ticket de Venta (Desktop y Móvil)
  const renderTicketContent = (onClose) => {
    return (
      <div className="flex flex-col h-full justify-between">
        <div className="shrink-0 border-b border-slate-800 pb-3 flex justify-between items-center">
          <h2 className="text-md font-bold text-slate-200 flex items-center gap-2">
            <span className={`transition-transform duration-300 inline-block ${cartBadgeActive ? 'scale-125 text-orange-400 rotate-6' : ''}`}>🎫</span>
            Ticket de Venta
            {carrito.length > 0 && (
              <span className={`ml-2 bg-orange-500 text-white text-[10px] font-bold px-2 py-0.5 rounded-full transition-all duration-300 ${cartBadgeActive ? 'animate-bounce scale-110' : ''}`}>
                {carrito.reduce((sum, item) => sum + item.cantidad, 0)}
              </span>
            )}
          </h2>
          {onClose && (
            <button
              onClick={onClose}
              className="text-slate-400 hover:text-slate-200 p-1.5 rounded-lg md:hidden transition-all duration-200 hover:bg-slate-800 cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>
          )}
        </div>

        {/* CONTENEDOR CON SCROLL INDEPENDIENTE (Inputs + Carrito) */}
        <div className="flex-1 overflow-y-auto pr-1 space-y-6 custom-scrollbar">
          {/* Formulario del Comprobante (Campos Interactivos con Dropdowns Personalizados) */}
          <div className="space-y-3 p-3 bg-slate-950/40 border border-slate-800 rounded-xl mt-4">
            <div className="grid grid-cols-2 gap-2">
              
              {/* Tipo de Comprobante (Custom Dropdown) */}
              <div className="relative" onBlur={() => setTimeout(() => setComprobanteOpen(false), 200)}>
                <label className="text-[10px] text-slate-500 font-bold uppercase block mb-1">Comprobante</label>
                <button
                  type="button"
                  onClick={() => setComprobanteOpen(!comprobanteOpen)}
                  className="w-full bg-slate-950/50 border border-slate-800 text-slate-200 text-xs rounded-lg px-2.5 py-1.5 focus:outline-none focus:border-orange-500/50 focus:ring-1 focus:ring-orange-500/20 transition-all flex items-center justify-between cursor-pointer"
                >
                  <span>{tipoComprobante}</span>
                  <ChevronDown className={`w-3.5 h-3.5 text-slate-500 transition-transform duration-200 ${comprobanteOpen ? 'rotate-180' : ''}`} />
                </button>
                {comprobanteOpen && (
                  <div className="absolute left-0 right-0 z-50 mt-1 bg-slate-900/95 backdrop-blur-md border border-slate-800 rounded-xl shadow-2xl p-1 animate-[fadeIn_0.15s_ease-out]">
                    {['BOLETA', 'FACTURA'].map((op) => (
                      <div
                        key={op}
                        onClick={() => {
                          setTipoComprobante(op);
                          setComprobanteOpen(false);
                        }}
                        className="px-2.5 py-1.5 text-xs text-slate-350 hover:bg-orange-500/10 hover:text-orange-400 transition-colors rounded-lg cursor-pointer flex items-center justify-between"
                      >
                        <span>{op}</span>
                        {tipoComprobante === op && <Check className="w-3.5 h-3.5 text-orange-500" />}
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Método de Pago (Custom Dropdown con Lucide Icons) */}
              <div className="relative" onBlur={() => setTimeout(() => setPagoOpen(false), 200)}>
                <label className="text-[10px] text-slate-500 font-bold uppercase block mb-1">Pago</label>
                <button
                  type="button"
                  onClick={() => setPagoOpen(!pagoOpen)}
                  className="w-full bg-slate-950/50 border border-slate-800 text-slate-200 text-xs rounded-lg px-2.5 py-1.5 focus:outline-none focus:border-orange-500/50 focus:ring-1 focus:ring-orange-500/20 transition-all flex items-center justify-between cursor-pointer"
                >
                  <div className="flex items-center gap-2">
                    {getPagoIcon(metodoPago)}
                    <span>{getPagoLabel(metodoPago)}</span>
                  </div>
                  <ChevronDown className={`w-3.5 h-3.5 text-slate-500 transition-transform duration-200 ${pagoOpen ? 'rotate-180' : ''}`} />
                </button>
                {pagoOpen && (
                  <div className="absolute left-0 right-0 z-50 mt-1 bg-slate-900/95 backdrop-blur-md border border-slate-800 rounded-xl shadow-2xl p-1 animate-[fadeIn_0.15s_ease-out]">
                    {[
                      { value: 'EFECTIVO', label: 'EFECTIVO' },
                      { value: 'TARJETA_CREDITO', label: 'TARJETA' },
                      { value: 'YAPE_PLIN', label: 'YAPE/PLIN' }
                    ].map((op) => (
                      <div
                        key={op.value}
                        onClick={() => {
                          setMetodoPago(op.value);
                          setPagoOpen(false);
                        }}
                        className="px-2.5 py-1.5 text-xs text-slate-350 hover:bg-orange-500/10 hover:text-orange-400 transition-colors rounded-lg cursor-pointer flex items-center justify-between"
                      >
                        <div className="flex items-center gap-2">
                          {getPagoIcon(op.value)}
                          <span>{op.label}</span>
                        </div>
                        {metodoPago === op.value && <Check className="w-3.5 h-3.5 text-orange-500" />}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>

            <div className="grid grid-cols-2 gap-2">
              {/* Nombre Cliente Selector (Custom Dropdown) */}
              <div className="relative" onBlur={() => setTimeout(() => setClienteOpen(false), 200)}>
                <label className="text-[10px] text-slate-500 font-bold uppercase block mb-1">Cliente</label>
                <button
                  type="button"
                  onClick={() => setClienteOpen(!clienteOpen)}
                  className="w-full bg-slate-950/50 border border-slate-800 text-slate-200 text-xs rounded-lg px-2.5 py-1.5 focus:outline-none focus:border-orange-500/50 focus:ring-1 focus:ring-orange-500/20 transition-all flex items-center justify-between cursor-pointer"
                >
                  <span className="truncate pr-1">{getClienteLabel()}</span>
                  <ChevronDown className={`w-3.5 h-3.5 text-slate-500 transition-transform duration-200 ${clienteOpen ? 'rotate-180' : ''}`} />
                </button>
                {clienteOpen && (
                  <div className="absolute left-0 right-0 z-50 mt-1 bg-slate-900/95 backdrop-blur-md border border-slate-800 rounded-xl shadow-2xl p-1 animate-[fadeIn_0.15s_ease-out] max-h-48 overflow-y-auto custom-scrollbar">
                    <div
                      onClick={() => {
                        setClienteSeleccionado(null);
                        setNombreCliente('Público General');
                        setDocCliente('');
                        setClienteOpen(false);
                      }}
                      className="px-2.5 py-1.5 text-xs text-slate-350 hover:bg-orange-500/10 hover:text-orange-400 transition-colors rounded-lg cursor-pointer flex items-center justify-between"
                    >
                      <span>Público General</span>
                      {!clienteSeleccionado && <Check className="w-3.5 h-3.5 text-orange-500" />}
                    </div>
                    {clientesVIP.map((cli) => (
                      <div
                        key={cli.id}
                        onClick={() => {
                          setClienteSeleccionado(cli);
                          setNombreCliente(cli.nombre);
                          setDocCliente(cli.dni);
                          setClienteOpen(false);
                        }}
                        className="px-2.5 py-1.5 text-xs text-slate-350 hover:bg-orange-500/10 hover:text-orange-400 transition-colors rounded-lg cursor-pointer flex items-center justify-between"
                      >
                        <span>{cli.nombre}</span>
                        {clienteSeleccionado?.id === cli.id && <Check className="w-3.5 h-3.5 text-orange-500" />}
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Documento Cliente (DNI/RUC) */}
              <div>
                <label className="text-[10px] text-slate-500 font-bold uppercase block mb-1">DNI / RUC</label>
                <input
                  type="text"
                  placeholder="Opcional"
                  value={docCliente}
                  onChange={(e) => setDocCliente(e.target.value)}
                  className="w-full bg-slate-950/50 border border-slate-800 text-slate-200 text-xs rounded-lg px-2.5 py-1.5 focus:outline-none focus:border-orange-500/50 focus:ring-1 focus:ring-orange-500/20 transition-all placeholder-slate-650"
                />
              </div>
            </div>
          </div>

          {/* Lista del Carrito */}
          <div className="space-y-2">
            {carrito.length === 0 ? (
              <div className="flex flex-col items-center justify-center min-h-[350px] p-6 text-center select-none">
                {/* Halo ring + icon container */}
                <div className="relative mb-6">
                  {/* Pulsing outer ring */}
                  <div className="absolute inset-0 rounded-full bg-orange-500/5 animate-pulse scale-150 blur-md" />
                  {/* Subtle inner ring */}
                  <div className="w-20 h-20 rounded-2xl bg-slate-800/60 border border-slate-700/50 flex items-center justify-center shadow-inner relative z-10">
                    <ShoppingCart className="w-8 h-8 text-slate-600/60 animate-[pulse_3s_ease-in-out_infinite]" />
                  </div>
                </div>

                {/* Text block */}
                <p className="text-base font-semibold text-slate-300 mb-1.5 tracking-wide">
                  Terminal de Venta Lista
                </p>
                <p className="text-sm text-slate-500 max-w-[220px] mx-auto leading-relaxed">
                  Selecciona productos del catálogo para estructurar una nueva comanda.
                </p>

                {/* Decorative divider */}
                <div className="mt-6 flex items-center gap-2 opacity-30">
                  <div className="w-8 h-px bg-slate-600" />
                  <div className="w-1.5 h-1.5 rounded-full bg-orange-500/50" />
                  <div className="w-8 h-px bg-slate-600" />
                </div>
              </div>
            ) : (
              carrito.map((item) => (
                <div
                  key={item.idProducto}
                  className="bg-slate-800/40 border border-slate-800/80 rounded-xl p-3 flex items-center justify-between animate-[fadeIn_0.25s_ease-out]"
                >
                  <div className="flex-1 min-w-0 pr-2">
                    <h4 className="font-semibold text-slate-350 text-xs truncate">
                      {item.nombre}
                    </h4>
                    <span className="text-slate-500 text-[10px]">
                      S/ {item.precioUnitario.toFixed(2)} c/u
                    </span>
                  </div>

                  {/* Controles de Cantidad */}
                  <div className="flex items-center gap-2 shrink-0">
                    <button
                      onClick={() => cambiarCantidad(item.idProducto, -1)}
                      className="w-5 h-5 rounded bg-slate-850 hover:bg-slate-750 text-slate-400 hover:text-slate-200 flex items-center justify-center text-xs transition cursor-pointer"
                    >
                      -
                    </button>
                    <span className="text-slate-200 font-bold text-xs w-4 text-center">
                      {item.cantidad}
                    </span>
                    <button
                      onClick={() => cambiarCantidad(item.idProducto, 1)}
                      className="w-5 h-5 rounded bg-slate-850 hover:bg-slate-750 text-slate-400 hover:text-slate-200 flex items-center justify-center text-xs transition cursor-pointer"
                    >
                      +
                    </button>
                  </div>

                  {/* Subtotal Ítem */}
                  <span className="font-semibold text-slate-200 text-xs w-16 text-right shrink-0">
                    S/ {(item.cantidad * item.precioUnitario).toFixed(2)}
                  </span>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Sección de Pago (Fijo abajo, fuera de scroll) */}
        <div className="shrink-0 border-t border-slate-800 pt-4 mt-4 space-y-4">
          {/* Desglose de Precios */}
          <div className="space-y-1.5">
            <div className="flex justify-between items-center text-xs">
              <span className="text-slate-450">Subtotal</span>
              <span className="text-slate-300 font-medium">S/ {subtotal.toFixed(2)}</span>
            </div>
            {clienteSeleccionado && (
              <div className="flex justify-between items-center text-xs">
                <span className="text-red-400 font-medium">Descuento VIP (5%)</span>
                <span className="text-red-400 font-medium">-S/ {descuento.toFixed(2)}</span>
              </div>
            )}
            <div className="flex justify-between items-center border-t border-slate-850 pt-1.5">
              <span className="text-slate-450 text-sm font-semibold">Total a Pagar</span>
              <span className="text-orange-400 text-xl font-bold tracking-tight">
                S/ {totalFinal.toFixed(2)}
              </span>
            </div>
          </div>

          <button
            onClick={() => {
              handleConfirmarVenta();
              if (onClose) onClose();
            }}
            disabled={carrito.length === 0}
            className={`w-full py-3 font-bold text-sm rounded-xl uppercase tracking-wider transition-all duration-200 flex items-center justify-center gap-1.5 ${
              carrito.length === 0
                ? 'bg-slate-800/40 text-slate-500 border border-slate-800/80 cursor-not-allowed font-semibold shadow-none'
                : 'bg-orange-500 hover:bg-orange-600 text-white shadow-lg shadow-orange-500/15 active:scale-95 cursor-pointer'
            }`}
          >
            Confirmar Pedido &amp; Imprimir
          </button>
        </div>
      </div>
    );
  };

  return (
    <div className="h-[calc(100vh-80px)] overflow-hidden">
      {/* 1. LAYOUT DE PANTALLA (Oculto al imprimir) */}
      <div className="flex flex-col md:flex-row gap-6 h-full print:hidden overflow-hidden">
        {/* Alerta flotante */}
        {notificacion.visible && (
          <div className={`fixed top-4 right-4 z-50 px-5 py-4 rounded-2xl shadow-2xl backdrop-blur-md border flex items-center gap-3.5 transition-all duration-500 ease-out transform translate-y-0 opacity-100 ${
            notificacion.tipo === 'error'
              ? 'bg-red-950/80 border-red-500/30 text-red-200'
              : 'bg-emerald-950/80 border-emerald-500/30 text-emerald-200'
          }`}>
            <div className={`w-8 h-8 rounded-xl flex items-center justify-center text-sm shadow-md shrink-0 ${
              notificacion.tipo === 'error'
                ? 'bg-red-500/20 text-red-400'
                : 'bg-emerald-500/20 text-emerald-400'
            }`}>
              {notificacion.tipo === 'error' ? '✕' : '✓'}
            </div>
            <div>
              <p className="font-bold text-[10px] uppercase tracking-widest opacity-60">
                {notificacion.tipo === 'error' ? 'Error' : 'Éxito'}
              </p>
              <p className="text-xs font-semibold mt-0.5 text-slate-100">{notificacion.mensaje}</p>
            </div>
          </div>
        )}

        {/* Columna Izquierda: Catálogo de Productos */}
        <div className="w-full md:w-2/3 flex flex-col h-full bg-slate-900/40 border border-slate-800 rounded-2xl p-5 overflow-hidden">
          {/* Categorías */}
          <div className="flex gap-2 pb-4 overflow-x-auto shrink-0">
            {categorias.map((cat) => (
              <button
                key={cat}
                onClick={() => setCategoriaActiva(cat)}
                className={`px-4 py-2 rounded-xl text-xs font-semibold uppercase tracking-wider transition-all whitespace-nowrap cursor-pointer ${
                  categoriaActiva === cat
                    ? 'bg-orange-500 text-white shadow-lg shadow-orange-500/20'
                    : 'bg-slate-800 text-slate-400 hover:text-slate-200 hover:bg-slate-750'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>

          {/* Productos Grid */}
          <div className="flex-1 overflow-y-auto scrollbar-thin scrollbar-thumb-slate-800 scrollbar-track-transparent [&::-webkit-scrollbar]:w-2 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:bg-slate-800/80 [&::-webkit-scrollbar-thumb]:rounded-full hover:[&::-webkit-scrollbar-thumb]:bg-slate-700 pr-1 pb-4">
            {loading ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
                {[...Array(6)].map((_, i) => (
                  <SkeletonCard key={i} />
                ))}
              </div>
            ) : error ? (
              <div className="flex flex-col items-center justify-center h-64 text-red-400 gap-3">
                <span>Error: {error}</span>
                <button
                  onClick={cargarProductos}
                  className="px-4 py-2 bg-slate-800 text-slate-200 rounded-xl hover:bg-slate-700 transition cursor-pointer"
                >
                  Reintentar
                </button>
              </div>
            ) : productosFiltrados.length === 0 ? (
              <div className="text-center py-20 text-slate-500">
                No hay productos disponibles en esta categoría.
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
                {productosFiltrados.map((p) => {
                  const esStockBajo = p.stock <= 5;
                  const esSinStock = p.stock === 0;
                  const mostrarAlertaStock = p.stock > 0 && p.stock < 5;

                  return (
                    <div
                      key={p.id}
                      className="bg-slate-800/65 border border-slate-750/70 rounded-2xl p-4 flex flex-col justify-between transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_0_20px_rgba(249,115,22,0.15)] hover:border-orange-500/40 relative group"
                    >
                      {/* Badge parpadeante si el stock es bajo (< 5 y > 0) */}
                      {mostrarAlertaStock && (
                        <div className="absolute -top-2 -right-2 z-10 bg-red-500 text-white text-[9px] font-extrabold px-2.5 py-1 rounded-lg shadow-lg animate-pulse uppercase tracking-wider border border-red-400">
                          ¡Últimas unidades!
                        </div>
                      )}
                      <div>
                        {/* Cargar imagen dinámica o fallback */}
                        {p.imagenUrl ? (
                          <div className="relative mb-4 shrink-0 overflow-hidden rounded-xl">
                             <img 
                               src={`${API_HOST_URL}${p.imagenUrl}`} 
                               alt={p.nombre}
                               onError={(e) => {
                                 e.target.style.display = 'none';
                                 e.target.nextSibling.style.display = 'flex';
                               }}
                               className="object-cover h-28 w-full shadow-md group-hover:scale-[1.02] transition-transform duration-300"
                             />
                            <div 
                              style={{ display: 'none' }}
                              className="bg-gradient-to-tr from-orange-500/10 to-amber-500/5 text-orange-500 border border-orange-500/10 font-bold text-2xl items-center justify-center rounded-xl h-28 w-full shadow-inner group-hover:scale-[1.02] transition-transform duration-300"
                            >
                              {p.iniciales || p.nombre.substring(0, 2).toUpperCase()}
                            </div>
                          </div>
                        ) : (
                          <div className="bg-gradient-to-tr from-orange-500/10 to-amber-500/5 text-orange-500 border border-orange-500/10 font-bold text-2xl flex items-center justify-center rounded-xl h-28 w-full shadow-inner mb-4 group-hover:scale-[1.02] transition-transform duration-300">
                            {p.iniciales || p.nombre.substring(0, 2).toUpperCase()}
                          </div>
                        )}

                        <div className="flex items-start justify-between gap-2">
                          <h3 className="font-semibold text-slate-200 text-sm tracking-wide leading-snug">
                            {p.nombre}
                          </h3>
                          <span className="text-orange-400 font-bold text-sm shrink-0">
                            S/ {p.precio.toFixed(2)}
                          </span>
                        </div>
                      </div>

                      <div className="mt-4 flex items-center justify-between">
                        {/* Indicador de Stock */}
                        <div className="flex flex-col">
                          <span className="text-[10px] text-slate-500 uppercase font-semibold">Stock</span>
                          <span className={`text-xs font-bold ${
                            esSinStock
                              ? 'text-red-500'
                              : esStockBajo
                              ? 'text-yellow-500'
                              : 'text-emerald-500'
                          }`}>
                            {esSinStock ? 'Agotado' : `${p.stock} unidades`}
                          </span>
                        </div>

                        {/* Botón Agregar */}
                        <button
                          onClick={() => agregarAlCarrito(p)}
                          disabled={esSinStock}
                          className={`px-3 py-1.5 rounded-xl text-xs font-semibold cursor-pointer active:scale-95 transition-transform ${
                            esSinStock
                              ? 'bg-slate-900 text-slate-655 cursor-not-allowed'
                              : 'bg-orange-500 hover:bg-orange-600 text-white shadow-md shadow-orange-500/10'
                          }`}
                        >
                          {esSinStock ? 'Agotado' : 'Agregar'}
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>

        {/* Columna Derecha: Formulario y Ticket de Venta (Visible en desktop, oculta en móvil) */}
        <div className="hidden md:flex md:w-1/3 bg-slate-900/40 backdrop-blur-md border border-slate-800 rounded-2xl p-4 h-[calc(100vh-120px)] flex flex-col justify-between">
          {renderTicketContent()}
        </div>
      </div>

      {/* Botón flotante para móviles (FAB) */}
      <button
        onClick={() => setDrawerOpen(true)}
        className="md:hidden fixed bottom-4 right-4 z-40 bg-gradient-to-r from-orange-500 to-amber-500 text-white p-4 rounded-full shadow-2xl shadow-orange-500/30 hover:scale-105 active:scale-95 transition-all flex items-center justify-center border border-orange-400/20 cursor-pointer"
      >
        <ShoppingCart className={`w-6 h-6 transition-transform duration-300 ${cartBadgeActive ? 'scale-125 text-amber-300 rotate-12' : 'animate-pulse'}`} />
        {carrito.reduce((sum, item) => sum + item.cantidad, 0) > 0 && (
          <span className={`absolute -top-1 -right-1 text-white text-[10px] font-black px-2 py-0.5 rounded-full shadow border border-slate-900 transition-all duration-300 ${
            cartBadgeActive 
              ? 'bg-orange-500 scale-125 animate-bounce' 
              : 'bg-red-500'
          }`}>
            {carrito.reduce((sum, item) => sum + item.cantidad, 0)}
          </span>
        )}
      </button>

      {/* Mobile Drawer (visible only on mobile) */}
      {drawerOpen && (
        <div className="md:hidden fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex justify-end">
          <div className="w-full max-w-lg bg-slate-900/40 backdrop-blur-md h-full flex flex-col p-6 shadow-2xl relative border-l border-slate-800 overflow-hidden animate-[slideLeft_0.3s_ease-out]">
            {renderTicketContent(() => setDrawerOpen(false))}
          </div>
        </div>
      )}

      {/* Estilos de Impresión Térmica POS y Scrollbar Reset */}
      <style>{`
        .custom-scrollbar::-webkit-scrollbar {
          width: 0px; /* Hides scrollbar completely */
          background: transparent;
        }
        .custom-scrollbar {
          -ms-overflow-style: none;  /* IE and Edge */
          scrollbar-width: none;  /* Firefox */
        }
        @media print {
          /* Forzar fondo blanco y texto negro, eliminando colores de administración */
          body, html, #root {
            background-color: white !important;
            color: black !important;
            min-height: 0 !important;
            height: auto !important;
          }
          /* Ocultar elementos del panel administrativo */
          aside, header, button, select, input, .print\\:hidden, .no-print {
            display: none !important;
          }
          /* Formato centrado de ticketera térmica estándar */
          .print-ticket-container {
            display: block !important;
            width: 80mm !important;
            max-width: 300px !important;
            margin: 0 auto !important;
            padding: 8px !important;
            background: white !important;
            color: black !important;
            font-family: monospace !important;
            font-size: 10px !important;
            line-height: 1.2 !important;
          }
        }
      `}</style>

      {/* 2. MAQUETA DE IMPRESIÓN (Oculta en pantalla, visible solo al imprimir) */}
      {ticketImpresion && (
        <div className="hidden print:block print-ticket-container font-mono text-black p-4 text-[10px] mx-auto bg-white border-0">
          <div className="text-center font-bold text-sm uppercase tracking-wide">TIENDA CATYS ERP</div>
          <div className="text-center text-[9px] mb-2 font-medium">TIENDA CATYS S.A.C. - LIMA, PERÚ</div>
          <div className="border-t border-dashed border-black my-2"></div>
          
          <div className="space-y-0.5 text-[10px]">
            <div><strong>Fecha:</strong> {ticketImpresion.fecha}</div>
            <div><strong>Comprobante:</strong> {ticketImpresion.tipoComprobante}</div>
            <div><strong>Cliente:</strong> {ticketImpresion.cliente}</div>
            {ticketImpresion.docCliente && <div><strong>DNI/RUC:</strong> {ticketImpresion.docCliente}</div>}
            <div><strong>Pago:</strong> {ticketImpresion.metodoPago}</div>
          </div>
          
          <div className="border-t border-dashed border-black my-2"></div>
          
          {/* Cabecera Tabla Items */}
          <div className="grid grid-cols-5 font-bold text-[9px] pb-1 border-b border-black">
            <span className="text-center col-span-1">Cant</span>
            <span className="col-span-3">Producto</span>
            <span className="text-right col-span-1">Total</span>
          </div>

          {/* Listado de Items en Ticket */}
          <div className="space-y-1 pt-1 text-[9px]">
            {ticketImpresion.items.map((item) => (
              <div key={item.idProducto} className="grid grid-cols-5">
                <span className="text-center col-span-1">x{item.cantidad}</span>
                <span className="col-span-3 truncate">{item.nombre}</span>
                <span className="text-right col-span-1">S/ {(item.cantidad * item.precioUnitario).toFixed(2)}</span>
              </div>
            ))}
          </div>

          <div className="border-t border-dashed border-black my-2"></div>
          
          {/* Pie del Ticket: Desglose claro de Subtotal, Descuento VIP y Total Final */}
          <div className="space-y-1 text-[9px] pt-1">
            <div className="flex justify-between">
              <span>SUBTOTAL:</span>
              <span>S/ {ticketImpresion.subtotal.toFixed(2)}</span>
            </div>
            {ticketImpresion.descuento > 0 && (
              <div className="flex justify-between font-bold">
                <span>DESC. VIP (5%):</span>
                <span>-S/ {ticketImpresion.descuento.toFixed(2)}</span>
              </div>
            )}
            <div className="border-t border-dashed border-black my-1"></div>
            <div className="flex justify-between items-center text-xs font-bold">
              <span>TOTAL FINAL:</span>
              <span>S/ {ticketImpresion.total.toFixed(2)}</span>
            </div>
          </div>
          
          <div className="border-t border-dashed border-black my-2"></div>
          
          <div className="text-center font-bold text-[10px] mt-4 uppercase">
            *** ¡Gracias por su consumo! ***
          </div>
          <div className="text-center text-[8px] text-slate-500 mt-1">
            Catys ERP Web — v5.0 NextGen
          </div>
        </div>
      )}
    </div>
  );
}

export default PuntoVenta;
