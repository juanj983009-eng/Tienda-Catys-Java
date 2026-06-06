import React, { useState } from 'react';
import { Users, Phone, Award, Search, Trash2, Edit2, Plus, X, CreditCard } from 'lucide-react';

function ClientesVIP() {
  const [clientes, setClientes] = useState([
    { id: 1, nombre: "Juan José Parra", dni: "12345678", telefono: "987654321", visitas: 14 },
    { id: 2, nombre: "Aníbal Torres", dni: "87654321", telefono: "912345678", visitas: 8 }
  ]);

  // Form states
  const [nombre, setNombre] = useState('');
  const [dni, setDni] = useState('');
  const [telefono, setTelefono] = useState('');
  
  // Edit mode state
  const [editId, setEditId] = useState(null);

  // Search state
  const [buscar, setBuscar] = useState('');

  // Get initials for avatar
  const obtenerIniciales = (nombreCompleto) => {
    if (!nombreCompleto) return '??';
    const partes = nombreCompleto.trim().split(/\s+/);
    if (partes.length >= 2) {
      return (partes[0][0] + partes[1][0]).toUpperCase();
    }
    return partes[0].substring(0, 2).toUpperCase();
  };

  // Create or Update Client
  const handleSubmit = (e) => {
    e.preventDefault();
    if (!nombre.trim() || !dni.trim() || !telefono.trim()) return;

    if (editId !== null) {
      // Edit / Update
      setClientes(clientes.map(c => 
        c.id === editId ? { ...c, nombre: nombre.trim(), dni: dni.trim(), telefono: telefono.trim() } : c
      ));
      setEditId(null);
    } else {
      // Add / Create
      const nuevoCliente = {
        id: Date.now(),
        nombre: nombre.trim(),
        dni: dni.trim(),
        telefono: telefono.trim(),
        visitas: 0
      };
      setClientes([...clientes, nuevoCliente]);
    }

    // Reset Form
    setNombre('');
    setDni('');
    setTelefono('');
  };

  // Start edit flow
  const handleEdit = (cliente) => {
    setEditId(cliente.id);
    setNombre(cliente.nombre);
    setDni(cliente.dni);
    setTelefono(cliente.telefono);
  };

  // Cancel edit flow
  const handleCancel = () => {
    setEditId(null);
    setNombre('');
    setDni('');
    setTelefono('');
  };

  // Delete flow
  const handleDelete = (id) => {
    setClientes(clientes.filter(c => c.id !== id));
    if (editId === id) {
      handleCancel();
    }
  };

  // Filter list
  const clientesFiltrados = clientes.filter(c => 
    c.nombre.toLowerCase().includes(buscar.toLowerCase()) || 
    c.dni.includes(buscar)
  );

  return (
    <div className="flex flex-col lg:flex-row gap-6 p-6 bg-slate-950 min-h-screen w-full overflow-y-auto text-slate-100">
      
      {/* Panel Izquierdo: Formulario */}
      <div className="w-full lg:w-1/3 shrink-0">
        <form 
          onSubmit={handleSubmit}
          className="bg-slate-900/40 border border-slate-800 p-6 rounded-2xl h-fit transition-all duration-300 ease-out transform hover:-translate-y-1 hover:border-amber-500/30 hover:shadow-lg hover:shadow-amber-500/5"
        >
          <div className="flex items-center gap-3 border-b border-slate-800 pb-4 mb-6">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-amber-500 to-orange-600 flex items-center justify-center shadow-lg shadow-orange-500/10">
              <Plus className="w-5 h-5 text-white" />
            </div>
            <div>
              <h3 className="font-bold text-sm text-slate-200 uppercase tracking-wider">
                {editId !== null ? 'Modificar Cliente' : 'Fidelizar Cliente'}
              </h3>
              <p className="text-[10px] text-slate-500">Módulo de Clientes Preferenciales</p>
            </div>
          </div>

          <div className="space-y-4">
            {/* Input DNI */}
            <div>
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1.5">
                DNI / Identificación
              </label>
              <input
                type="text"
                required
                maxLength={8}
                value={dni}
                onChange={(e) => setDni(e.target.value.replace(/\D/g, ''))}
                placeholder="Ej. 12345678"
                className="w-full bg-slate-950 border border-slate-800 focus:border-amber-500 rounded-xl px-4 py-2.5 text-sm text-slate-100 placeholder-slate-650 focus:outline-none focus:ring-1 focus:ring-amber-500 transition-colors font-mono"
              />
            </div>

            {/* Input Nombre */}
            <div>
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1.5">
                Nombre Completo
              </label>
              <input
                type="text"
                required
                value={nombre}
                onChange={(e) => setNombre(e.target.value)}
                placeholder="Ej. Juan José Parra"
                className="w-full bg-slate-950 border border-slate-800 focus:border-amber-500 rounded-xl px-4 py-2.5 text-sm text-slate-100 placeholder-slate-650 focus:outline-none focus:ring-1 focus:ring-amber-500 transition-colors"
              />
            </div>

            {/* Input Teléfono */}
            <div>
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1.5">
                Teléfono Celular
              </label>
              <input
                type="text"
                required
                value={telefono}
                onChange={(e) => setTelefono(e.target.value.replace(/\D/g, ''))}
                placeholder="Ej. 987654321"
                className="w-full bg-slate-950 border border-slate-800 focus:border-amber-500 rounded-xl px-4 py-2.5 text-sm text-slate-100 placeholder-slate-650 focus:outline-none focus:ring-1 focus:ring-amber-500 transition-colors"
              />
            </div>
          </div>

          <div className="flex flex-col gap-2 mt-6">
            <button
              type="submit"
              className="w-full bg-gradient-to-r from-amber-500 to-orange-600 text-white font-medium py-2 rounded-lg hover:brightness-110 transition-all cursor-pointer flex items-center justify-center gap-2"
            >
              {editId !== null ? 'Actualizar Cliente' : 'Registrar Favorito'}
            </button>
            {editId !== null && (
              <button
                type="button"
                onClick={handleCancel}
                className="w-full bg-slate-800 border border-slate-700 text-slate-350 hover:text-slate-100 font-medium py-2 rounded-lg hover:bg-slate-700 transition-all cursor-pointer"
              >
                Cancelar Edición
              </button>
            )}
          </div>
        </form>
      </div>

      {/* Panel Derecho: Lista de Favoritos */}
      <div className="w-full lg:w-2/3 flex flex-col gap-4">
        
        {/* Barra de Búsqueda y Título */}
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center pb-4 border-b border-slate-800 gap-4">
          <div className="flex items-center gap-2">
            <Award className="w-5 h-5 text-amber-500" />
            <h3 className="text-sm font-bold text-slate-200 uppercase tracking-wider">
              Clientes VIP Registrados
            </h3>
          </div>
          <div className="relative w-full md:w-64">
            <span className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Search className="h-4 w-4 text-slate-500" />
            </span>
            <input
              type="text"
              value={buscar}
              onChange={(e) => setBuscar(e.target.value)}
              placeholder="Buscar por DNI o Nombre..."
              className="w-full bg-slate-900 border border-slate-800 rounded-xl pl-9 pr-3 py-1.5 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-amber-500 transition-colors"
            />
          </div>
        </div>

        {/* Grid de Clientes */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {clientesFiltrados.map((cliente) => (
            <div
              key={cliente.id}
              className="bg-slate-900/40 border border-slate-800 rounded-2xl transition-all duration-300 ease-out transform hover:-translate-y-1 hover:border-amber-500/30 hover:shadow-lg hover:shadow-amber-500/5 p-5 flex flex-col justify-between relative group"
            >
              {/* Header de la Tarjeta */}
              <div className="flex justify-between items-start gap-2">
                <div className="flex items-center gap-3">
                  {/* Avatar */}
                  <div className="w-10 h-10 rounded-full bg-amber-500/10 text-amber-500 flex items-center justify-center font-bold text-sm shrink-0 border border-amber-500/10">
                    {obtenerIniciales(cliente.nombre)}
                  </div>
                  <div>
                    <h4 className="font-bold text-slate-200 text-sm tracking-wide leading-tight group-hover:text-amber-400 transition-colors">
                      {cliente.nombre}
                    </h4>
                    <span className="text-[10px] text-slate-500 font-mono">DNI: {cliente.dni}</span>
                  </div>
                </div>
                
                {/* Badge de Beneficio */}
                <span className="text-xs font-semibold text-amber-400 bg-amber-500/10 px-2 py-1 rounded-md shrink-0">
                  VIP 5% Descuento
                </span>
              </div>

              {/* Detalles de Contacto / Visitas */}
              <div className="mt-4 space-y-2 border-t border-slate-800/60 pt-4 text-xs text-slate-400">
                <div className="flex justify-between items-center">
                  <span className="text-slate-500 flex items-center gap-1.5">
                    <Phone className="w-3.5 h-3.5 text-slate-650" />
                    Contacto:
                  </span>
                  <span className="font-mono text-slate-350">{cliente.telefono}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-slate-500 flex items-center gap-1.5">
                    <CreditCard className="w-3.5 h-3.5 text-slate-650" />
                    Visitas POS:
                  </span>
                  <span className="font-bold text-amber-500 bg-amber-500/5 px-2 py-0.5 rounded border border-amber-500/5">
                    {cliente.visitas}
                  </span>
                </div>
              </div>

              {/* Botones de Accion */}
              <div className="mt-5 flex gap-2 border-t border-slate-800/40 pt-4 justify-end">
                <button
                  onClick={() => handleEdit(cliente)}
                  className="p-1.5 px-3 text-slate-400 hover:text-amber-400 hover:bg-amber-500/5 rounded-lg transition-colors flex items-center gap-1.5 text-[11px] font-semibold cursor-pointer border border-transparent hover:border-amber-500/10"
                >
                  <Edit2 className="w-3 h-3" />
                  Editar
                </button>
                <button
                  onClick={() => handleDelete(cliente.id)}
                  className="p-1.5 px-3 text-slate-400 hover:text-red-400 hover:bg-red-500/5 rounded-lg transition-colors flex items-center gap-1.5 text-[11px] font-semibold cursor-pointer border border-transparent hover:border-red-500/10"
                >
                  <Trash2 className="w-3 h-3" />
                  Eliminar
                </button>
              </div>

              {/* Overlay Decorativo */}
              <div className="absolute inset-0 bg-amber-500/1 opacity-0 group-hover:opacity-100 rounded-2xl pointer-events-none transition-opacity duration-300" />
            </div>
          ))}
        </div>

        {/* Estado Vacío */}
        {clientesFiltrados.length === 0 && (
          <div className="flex flex-col items-center justify-center py-20 text-slate-500 border border-dashed border-slate-800 rounded-2xl bg-slate-900/10">
            <Users className="w-10 h-10 text-slate-700 animate-pulse mb-3" />
            <span className="text-xs font-semibold text-slate-400">No se encontraron clientes VIP</span>
            <p className="text-[10px] text-slate-600 mt-1">Registra un cliente VIP a la izquierda para comenzar</p>
          </div>
        )}

      </div>
    </div>
  );
}

export default ClientesVIP;
