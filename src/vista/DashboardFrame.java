package vista;

import modelo.Usuario;
import repository.ClienteRepository;
import repository.ProductoRepository;
import repository.VentaRepository;
import repository.UsuarioRepository;
import service.ClienteService;
import service.ProductoService;
import service.UsuarioService;
import service.VentaService;
import util.AppLogger;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Panel de control legado — REEMPLAZADO por MainWindow + AppShell en v4.0.
 *
 * Esta clase se mantiene por compatibilidad de compilación.
 * El punto de entrada real de la aplicación es MainWindow.main().
 *
 * @deprecated Use MainWindow + AppShell (v4.0 Single-Window Architecture)
 */
@Deprecated
public class DashboardFrame extends JFrame {

    private final Usuario usuarioActual;

    // Servicios compartidos — se construyen una vez y se pasan a los frames hijos
    private final ProductoService productoService;
    private final VentaService    ventaService;
    private final ClienteService  clienteService;

    // --- Colores de la paleta del Dashboard ---
    private static final Color COLOR_FONDO         = new Color(245, 247, 250);
    private static final Color COLOR_HEADER        = new Color(33, 43, 54);
    private static final Color ACCENTO_NARANJA     = new Color(255, 99, 71);
    private static final Color ACCENTO_AZUL        = new Color(33, 150, 243);
    private static final Color ACCENTO_MORADO      = new Color(156, 39, 176);
    private static final Color ACCENTO_AMARILLO    = new Color(255, 193, 7);
    private static final Color ACCENTO_TEAL        = new Color(0, 150, 136);

    public DashboardFrame(Usuario usuario) {
        this.usuarioActual = usuario;

        // Composition Root del Dashboard — construir los servicios una sola vez
        ProductoRepository productoRepo = new ProductoRepository();
        VentaRepository    ventaRepo    = new VentaRepository();
        ClienteRepository  clienteRepo  = new ClienteRepository();

        this.productoService = new ProductoService(productoRepo);
        this.ventaService    = new VentaService(ventaRepo, productoRepo);
        this.clienteService  = new ClienteService(clienteRepo);

        AppLogger.info("Dashboard abierto — Usuario: " + usuario.getUsername()
                     + " | Rol: " + usuario.getRol());

        construirUI();
    }

    private void construirUI() {
        setTitle("Panel de Control - Catys Enterprise");
        setSize(1250, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_HEADER);
        header.setPreferredSize(new Dimension(1000, 70));
        header.setBorder(new EmptyBorder(0, 30, 0, 30));

        JLabel lblTitulo = new JLabel("\uD83D\uDC31 CATYS | ERP/POS");
        lblTitulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblUser = new JLabel(
            "<html><span style='font-family:Segoe UI Emoji;'>\uD83D\uDC64</span> "
            + usuarioActual.getNombreCompleto()
            + " <span style='color:#aaaaaa;'>|</span> "
            + "<span style='color:#4caf50; font-weight:bold;'>"
            + usuarioActual.getRol() + "</span></html>"
        );
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUser.setForeground(Color.WHITE);

        header.add(lblTitulo, BorderLayout.WEST);
        header.add(lblUser,   BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // --- Panel central con tarjetas de módulos ---
        JPanel panelCentral = new JPanel(new GridBagLayout());
        panelCentral.setBackground(COLOR_FONDO);

        JPanel gridBotones = new JPanel(new GridLayout(1, 5, 15, 0));
        gridBotones.setOpaque(false);
        gridBotones.setPreferredSize(new Dimension(1150, 220));

        JButton btnVentas    = new TarjetaBoton("\uD83D\uDED2", "Punto de Venta",  "Realizar nueva venta",      ACCENTO_NARANJA);
        JButton btnClientes  = new TarjetaBoton("\uD83D\uDC65", "Clientes VIP",    "Directorio de clientes",    ACCENTO_AZUL);
        JButton btnReportes  = new TarjetaBoton("\uD83D\uDCCA", "Reportes",        "Historial financiero",      ACCENTO_MORADO);
        JButton btnMenu      = new TarjetaBoton("\uD83C\uDF71", "Gestion Menu",    "Editar platos y precios",   ACCENTO_AMARILLO);
        JButton btnCocina    = new TarjetaBoton("\uD83D\uDC68\u200D\uD83C\uDF73", "Monitor Cocina", "Ver pedidos en cola", ACCENTO_TEAL);

        btnVentas.setName("btnVentas");
        btnClientes.setName("btnClientes");
        btnReportes.setName("btnReportes");
        btnMenu.setName("btnMenu");
        btnCocina.setName("btnCocina");

        gridBotones.add(btnVentas);
        gridBotones.add(btnClientes);
        gridBotones.add(btnReportes);
        gridBotones.add(btnMenu);
        gridBotones.add(btnCocina);
        panelCentral.add(gridBotones);
        add(panelCentral, BorderLayout.CENTER);

        // --- Footer con info de versión y usuario ---
        JLabel footer = new JLabel(
            "Catys ERP/POS v3.0 | Arquitectura Enterprise (Service + Repository + DTO)  ",
            SwingConstants.RIGHT
        );
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.setForeground(Color.GRAY);
        footer.setBorder(new EmptyBorder(5, 5, 5, 10));
        add(footer, BorderLayout.SOUTH);

        // --- Acciones de cada botón ---
        btnVentas.addActionListener(e -> abrirPuntoDeVenta());
        btnClientes.addActionListener(e -> abrirModulo("CLIENTES"));
        btnReportes.addActionListener(e -> abrirConPermiso("REPORTES", "ADMIN"));
        btnMenu.addActionListener(e -> abrirConPermiso("MENU", "ADMIN"));
        btnCocina.addActionListener(e -> abrirModulo("COCINA"));
    }

    // ==========================================
    //  NAVEGACIÓN CON CONTROL DE ACCESO
    // ==========================================

    /**
     * Abre el Punto de Venta instanciando TiendaGUI correctamente con DI.
     * Cierra el Dashboard — el usuario vuelve al Dashboard al cerrar sesión desde TiendaGUI.
     */
    /**
     * @deprecated DashboardFrame ya no es el punto de entrada.
     * TiendaGUI ahora es un JPanel — usar AppShell.navegarA("TIENDA") en su lugar.
     */
    @Deprecated
    private void abrirPuntoDeVenta() {
        AppLogger.warn("DashboardFrame.abrirPuntoDeVenta() está deprecated. Use MainWindow + AppShell.");
        dispose();
        // TiendaGUI ahora es un JPanel, no puede abrirse como ventana independiente.
        // Para iniciar la app correctamente, ejecuta: MainWindow.main()
    }

    /**
     * Abre un módulo sin restricción de rol.
     * @param modulo identificador del módulo a abrir
     */
    private void abrirModulo(String modulo) {
        AppLogger.info("Abriendo módulo: " + modulo + " — Usuario: " + usuarioActual.getUsername());
        switch (modulo) {
            case "CLIENTES" -> new ListaClientesFrame(clienteService).setVisible(true);
            case "COCINA"   -> new CocinaFrame(ventaService).setVisible(true);
            default         -> AppLogger.warn("Módulo desconocido: " + modulo);
        }
    }

    /**
     * Abre un módulo solo si el usuario tiene el rol requerido.
     * Muestra un diálogo de acceso denegado si no tiene permisos.
     *
     * @param modulo       identificador del módulo
     * @param rolRequerido rol mínimo necesario (ej: "ADMIN")
     */
    private void abrirConPermiso(String modulo, String rolRequerido) {
        if (!usuarioActual.getRol().equalsIgnoreCase(rolRequerido)) {
            AppLogger.warn("Acceso denegado a '" + modulo + "' para usuario: "
                         + usuarioActual.getUsername() + " (Rol: " + usuarioActual.getRol() + ")");
            JOptionPane.showMessageDialog(this,
                "\u26D4 Acceso denegado.\nSe requieren permisos de " + rolRequerido + " para acceder a este módulo.",
                "Sin permisos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AppLogger.info("Abriendo módulo restringido: " + modulo + " — Admin: " + usuarioActual.getUsername());
        switch (modulo) {
            case "REPORTES" -> new ReportesFrame(ventaService).setVisible(true);
            case "MENU"     -> new GestionProductosFrame(productoService).setVisible(true);
            default         -> AppLogger.warn("Módulo restringido desconocido: " + modulo);
        }
    }

    // ==========================================
    //  TARJETA VISUAL DEL MÓDULO
    // ==========================================

    /**
     * Componente de tarjeta del Dashboard — botón con ícono, título y descripción.
     * Su diseño es exclusivo del Dashboard (no se comparte con otros frames).
     */
    static class TarjetaBoton extends JButton {

        private final Color colorAcento;
        private boolean mouseEncima = false;

        public TarjetaBoton(String icono, String titulo, String subtitulo, Color color) {
            this.colorAcento = color;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel lblIcono   = new JLabel(icono);
            lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
            lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblTitulo  = new JLabel(titulo);
            lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblTitulo.setForeground(Color.DARK_GRAY);
            lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblSub = new JLabel(subtitulo);
            lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblSub.setForeground(Color.GRAY);
            lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

            add(Box.createVerticalStrut(20));
            add(lblIcono);
            add(Box.createVerticalStrut(15));
            add(lblTitulo);
            add(Box.createVerticalStrut(5));
            add(lblSub);
            add(Box.createVerticalStrut(20));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { mouseEncima = true;  repaint(); }
                public void mouseExited(MouseEvent e)  { mouseEncima = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (mouseEncima) {
                // Estado hover: fondo azulado + borde de color de acento
                g2.setColor(new Color(245, 245, 255));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setColor(colorAcento);
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 20, 20));
            } else {
                // Estado normal: blanco con borde gris
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setColor(new Color(230, 230, 230));
                g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));
            }

            // Franja de color en la parte superior (identifica el módulo visualmente)
            g2.setColor(colorAcento);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 6, 20, 20));
            g2.fillRect(0, 3, getWidth(), 3);

            super.paintComponent(g);
            g2.dispose();
        }
    }
}