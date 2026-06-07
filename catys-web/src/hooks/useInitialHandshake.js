import { useState, useEffect } from 'react';

const SESSION_KEY = 'catys_enterprise_session';

/**
 * Custom hook to handle initial system handshake and loading state.
 * Implements an "Anti-F5" mechanism to bypass loading screens if a session already exists.
 */
export function useInitialHandshake() {
  const [loading, setLoading] = useState(() => {
    // Check if the session already exists in sessionStorage
    const session = sessionStorage.getItem(SESSION_KEY);
    return !session; // If session exists, loading starts at false. If not, starts at true.
  });
  
  const [handshakeMessage, setHandshakeMessage] = useState('Inicializando sistemas...');

  useEffect(() => {
    // If we are not in loading state initially, nothing to do
    if (!loading) return;

    const messages = [
      'Estableciendo conexión con el servidor...',
      'Verificando inventario de insumos...',
      'Sincronizando caja registradora...',
      'Cargando base de datos local...',
      '¡Bienvenido a Catys ERP!'
    ];

    let currentStep = 0;
    
    const interval = setInterval(() => {
      if (currentStep < messages.length - 1) {
        setHandshakeMessage(messages[currentStep]);
        currentStep++;
      } else {
        clearInterval(interval);
        setHandshakeMessage(messages[messages.length - 1]);
        
        // Finalize loading after a brief delay for the welcome message
        setTimeout(() => {
          sessionStorage.setItem(SESSION_KEY, 'active');
          setLoading(false);
        }, 600);
      }
    }, 400);

    return () => {
      clearInterval(interval);
    };
  }, [loading]);

  return { loading, handshakeMessage };
}
