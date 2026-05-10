package vista;

import modelo.Usuario;
import service.ProductoService;
import service.VentaService;
import util.AppLogger;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * Shell de la aplicación tras el login.
 *
 * Contiene:
 *   - Sidebar fijo a la izquierda (logo, info usuario, botones de nav)
 *   - Header fijo arriba (sección actual + usuario logueado)
 *   - ContentArea (CardLayout): DASHBOARD | TIENDA | INVENTARIO | REPORTES
 *
 * Botones de Inventario y Reportes SOLO aparecen si el rol es ADMIN.
 */
public final class AppShell extends JPanel {

    private final MainWindow      mainWindow;
    private final Usuario         usuario;
    private final ProductoService productoService;
    private final VentaService    ventaService;

    // Sub-paneles de contenido
    private TiendaGUI             tiendaPanel;
    private GestionProductosFrame gestionPanel;
    private ReportesFrame         reportesPanel;

    // Navegación del contenido
    private final CardLayout contentLayout = new CardLayout();
    private final JPanel     contentArea   = new JPanel(contentLayout);

    // Header dinámico
    private final JLabel lblSeccion = new JLabel("🏠  Panel de Control");

    // Sidebar — tracking botón activo
    private JButton btnActivo;

    // Alias de colores
    private static final Color C_SIDEBAR  = MainWindow.C_SIDEBAR;
    private static final Color C_SIDEBAR2 = new Color(16, 16, 26);  // más oscuro para profundidad
    private static final Color C_FONDO    = MainWindow.C_FONDO;
    private static final Color C_PRIM     = MainWindow.C_PRIMARIO;
    private static final Color C_TEXTO    = MainWindow.C_TEXTO;
    private static final Color C_TEXTO_S  = MainWindow.C_TEXTO_S;
    private static final Color C_BORDE    = MainWindow.C_BORDE;
    private static final Color C_ACTIVO   = MainWindow.C_SIDEBAR_ACT;
    private static final Color C_SUPERF   = MainWindow.C_SUPERFICIE;

    public AppShell(MainWindow mainWindow, Usuario usuario,
                    ProductoService productoService, VentaService ventaService) {
        this.mainWindow      = mainWindow;
        this.usuario         = usuario;
        this.productoService = productoService;
        this.ventaService    = ventaService;
        setLayout(new BorderLayout());
        setBackground(C_FONDO);
    }

    /**
     * Construye toda la UI y arranca la carga de datos inicial.
     * Llamado desde MainWindow.onLoginExitoso().
     */
    public void inicializar() {
        tiendaPanel   = new TiendaGUI(mainWindow, productoService, ventaService);
        gestionPanel  = new GestionProductosFrame(productoService);
        reportesPanel = new ReportesFrame(ventaService);

        contentArea.setBackground(C_FONDO);
        contentArea.add(crearDashboardPanel(), "DASHBOARD");
        contentArea.add(tiendaPanel,           "TIENDA");
        contentArea.add(gestionPanel,          "INVENTARIO");
        contentArea.add(reportesPanel,         "REPORTES");

        add(crearSidebar(),  BorderLayout.WEST);
        add(crearHeader(),   BorderLayout.NORTH);
        add(contentArea,     BorderLayout.CENTER);

        navegarA("DASHBOARD");
        tiendaPanel.cargarProductos();   // Precarga el catálogo en background
        revalidate();
        repaint();
    }

    /** Cambia la vista activa del ContentArea y actualiza el header. */
    public void navegarA(String card) {
        contentLayout.show(contentArea, card);
        switch (card) {
            case "TIENDA"     -> lblSeccion.setText("🛒  Punto de Venta");
            case "INVENTARIO" -> { lblSeccion.setText("🍱  Inventario"); gestionPanel.recargar(); }
            case "REPORTES"   -> { lblSeccion.setText("📊  Reportes");   reportesPanel.recargarDatos(); }
            default           -> lblSeccion.setText("🏠  Panel de Control");
        }
        AppLogger.info("Navegación → " + card + " | Usuario: " + usuario.getUsername());
    }

    // ==========================================
    //  SIDEBAR
    // ==========================================

    private JPanel crearSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(C_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, C_BORDE));

        // ── Logo Section ──────────────────────────────────
        JPanel logoBox = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradiente sutil de fondo para el área del logo
                java.awt.GradientPaint gp = new java.awt.GradientPaint(
                    0, 0, new Color(30, 14, 6), 0, getHeight(), C_SIDEBAR);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        logoBox.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 20, 0));
        logoBox.setOpaque(false);
        logoBox.setPreferredSize(new Dimension(260, 80));
        logoBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // Icono circular del logo
        JLabel lIcono = new JLabel("🐱") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_PRIM);
                g2.fillOval(0, 0, 38, 38);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        lIcono.setForeground(Color.WHITE);
        lIcono.setOpaque(false);
        lIcono.setHorizontalAlignment(SwingConstants.CENTER);
        lIcono.setPreferredSize(new Dimension(38, 38));

        JPanel txtLogo = new JPanel();
        txtLogo.setLayout(new BoxLayout(txtLogo, BoxLayout.Y_AXIS));
        txtLogo.setOpaque(false);
        JLabel lNombreLogo = new JLabel("Catys ERP");
        lNombreLogo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lNombreLogo.setForeground(Color.WHITE);
        JLabel lVersionLogo = new JLabel("v4.0 · Professional");
        lVersionLogo.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lVersionLogo.setForeground(C_PRIM);
        txtLogo.add(lNombreLogo);
        txtLogo.add(lVersionLogo);

        logoBox.add(lIcono);
        logoBox.add(txtLogo);
        sidebar.add(logoBox);
        sidebar.add(mkSep());

        // ── Info de usuario ───────────────────────────────
        JPanel userBox = new JPanel();
        userBox.setLayout(new BoxLayout(userBox, BoxLayout.Y_AXIS));
        userBox.setBackground(C_SIDEBAR);
        userBox.setBorder(new EmptyBorder(18, 22, 18, 22));
        userBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // Avatar circular con inicial
        String inicial = usuario.getNombreCompleto().isEmpty() ? "?"
            : String.valueOf(usuario.getNombreCompleto().charAt(0)).toUpperCase();
        JLabel lAvatar = new JLabel(inicial, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 87, 34, 60));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(C_PRIM);
                g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.drawOval(1, 1, getWidth()-2, getHeight()-2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lAvatar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lAvatar.setForeground(C_PRIM);
        lAvatar.setOpaque(false);
        lAvatar.setPreferredSize(new Dimension(36, 36));
        lAvatar.setMinimumSize(new Dimension(36, 36));
        lAvatar.setMaximumSize(new Dimension(36, 36));
        lAvatar.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lNom = new JLabel(usuario.getNombreCompleto());
        lNom.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lNom.setForeground(C_TEXTO);
        JLabel lRol = new JLabel("●  " + usuario.getRol().toUpperCase());
        lRol.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lRol.setForeground(C_PRIM);
        userBox.add(lAvatar);
        userBox.add(Box.createVerticalStrut(8));
        userBox.add(lNom);
        userBox.add(Box.createVerticalStrut(2));
        userBox.add(lRol);
        sidebar.add(userBox);
        sidebar.add(mkSep());
        sidebar.add(Box.createVerticalStrut(8));

        // ── Etiqueta de sección ──────────────────────────
        JLabel lSeccion = new JLabel("  NAVEGACIÓN");
        lSeccion.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lSeccion.setForeground(new Color(100, 100, 130));
        lSeccion.setBorder(new EmptyBorder(4, 22, 6, 0));
        lSeccion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        sidebar.add(lSeccion);

        // ── Botones de navegación ────────────────────────
        JButton btnHome   = mkNavBtn("🏠  Inicio",         "DASHBOARD");
        JButton btnTienda = mkNavBtn("🛒  Punto de Venta", "TIENDA");
        sidebar.add(btnHome);
        sidebar.add(btnTienda);

        // Solo ADMIN ve Inventario y Reportes
        boolean esAdmin = "ADMIN".equalsIgnoreCase(usuario.getRol());
        if (esAdmin) {
            JLabel lAdmin = new JLabel("  ADMINISTRACIÓN");
            lAdmin.setFont(new Font("Segoe UI", Font.BOLD, 9));
            lAdmin.setForeground(new Color(100, 100, 130));
            lAdmin.setBorder(new EmptyBorder(14, 22, 6, 0));
            lAdmin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
            sidebar.add(lAdmin);
            sidebar.add(mkNavBtn("📦  Inventario", "INVENTARIO"));
            sidebar.add(mkNavBtn("📊  Reportes",   "REPORTES"));
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(mkSep());

        // ── Cerrar Sesión ────────────────────────────────
        JButton btnSalir = mkRawBtn("🚪  Cerrar Sesión", MainWindow.C_ROJO, true);
        btnSalir.addActionListener(e -> confirmarSalida());
        sidebar.add(btnSalir);
        sidebar.add(Box.createVerticalStrut(14));

        activar(btnHome);
        return sidebar;
    }

    private JButton mkNavBtn(String texto, String card) {
        JButton btn = mkRawBtn(texto, C_TEXTO_S, false);
        btn.addActionListener(e -> { activar(btn); navegarA(card); });
        return btn;
    }

    private JButton mkRawBtn(String texto, Color colorTexto, boolean esSalir) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Dibuja fondo redondeado con un leve relleno interior
                g2.setColor(getBackground());
                g2.fillRoundRect(8, 3, getWidth() - 16, getHeight() - 6, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        btn.setForeground(colorTexto);
        btn.setBackground(C_SIDEBAR);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(13, 22, 13, 20));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        if (!esSalir) {
            btn.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    if (btn != btnActivo) btn.setBackground(new Color(32, 32, 50));
                }
                @Override public void mouseExited(MouseEvent e) {
                    if (btn != btnActivo) btn.setBackground(C_SIDEBAR);
                }
            });
        } else {
            btn.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(70, 20, 20)); }
                @Override public void mouseExited(MouseEvent e)  { btn.setBackground(C_SIDEBAR); }
            });
        }
        return btn;
    }

    private void activar(JButton btn) {
        if (btnActivo != null) {
            btnActivo.setBackground(C_SIDEBAR);
            btnActivo.setForeground(C_TEXTO_S);
        }
        btn.setBackground(C_ACTIVO);
        btn.setForeground(C_PRIM);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));  // negrita al activar
        btnActivo = btn;
    }

    private JSeparator mkSep() {
        JSeparator sep = new JSeparator();
        sep.setForeground(C_BORDE);
        sep.setBackground(C_SIDEBAR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    // ==========================================
    //  HEADER
    // ==========================================

    private JPanel crearHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(new Color(18, 18, 28));
        h.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, C_BORDE),
            new EmptyBorder(16, 32, 16, 32)
        ));

        lblSeccion.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblSeccion.setForeground(C_TEXTO);

        JLabel lblUser = new JLabel("\uD83D\uDC64  " + usuario.getNombreCompleto()
            + "     \uD83D\uDD11  " + usuario.getRol());
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUser.setForeground(C_TEXTO_S);

        h.add(lblSeccion, BorderLayout.WEST);
        h.add(lblUser,    BorderLayout.EAST);
        return h;
    }

    // ==========================================
    //  PANEL DASHBOARD (bienvenida)
    // ==========================================

    private JPanel crearDashboardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(C_FONDO);

        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(C_FONDO);

        JLabel lEmoji = new JLabel("🐱🍜", SwingConstants.CENTER);
        lEmoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        lEmoji.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lHola = new JLabel("¡Bienvenido, " + usuario.getNombreCompleto() + "!");
        lHola.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lHola.setForeground(C_TEXTO);
        lHola.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lSub = new JLabel("Usa el menú lateral para navegar entre módulos.");
        lSub.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lSub.setForeground(C_TEXTO_S);
        lSub.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lRol = new JLabel("Sesión activa   •   Rol: " + usuario.getRol().toUpperCase());
        lRol.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lRol.setForeground(C_PRIM);
        lRol.setAlignmentX(CENTER_ALIGNMENT);

        col.add(lEmoji);
        col.add(Box.createVerticalStrut(22));
        col.add(lHola);
        col.add(Box.createVerticalStrut(10));
        col.add(lSub);
        col.add(Box.createVerticalStrut(8));
        col.add(lRol);
        panel.add(col);
        return panel;
    }

    // ==========================================
    //  CERRAR SESIÓN
    // ==========================================

    private void confirmarSalida() {
        int ok = JOptionPane.showConfirmDialog(mainWindow,
            "¿Deseas cerrar sesión?", "Cerrar Sesión", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            AppLogger.info("Cierre de sesión — usuario: " + usuario.getUsername());
            mainWindow.mostrarLogin();
        }
    }
}
