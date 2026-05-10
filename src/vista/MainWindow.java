package vista;

import com.formdev.flatlaf.FlatDarkLaf;
import modelo.Usuario;
import repository.ClienteRepository;
import repository.ProductoRepository;
import repository.VentaRepository;
import service.ClienteService;
import service.ProductoService;
import service.VentaService;
import util.AppLogger;

import java.awt.*;
import javax.swing.*;

/**
 * Ventana única de la aplicación — punto de entrada del sistema.
 *
 * v4.0 — Arquitectura Single-Window:
 * - Un solo JFrame con CardLayout raíz (LOGIN / APP)
 * - Composition Root: todos los servicios se crean aquí una vez
 * - FlatDarkLaf configurado globalmente antes de cualquier componente Swing
 * - onLoginExitoso() construye el AppShell y lo inyecta en el CardLayout
 * - mostrarLogin() destruye el AppShell y regresa al login (mismo JFrame)
 */
public final class MainWindow extends JFrame {

    // ==========================================
    // PALETA MAESTRA — compartida por toda la app
    // ==========================================
    public static final Color C_FONDO = new Color(18, 18, 24);
    public static final Color C_SIDEBAR = new Color(12, 12, 20);
    public static final Color C_SUPERFICIE = new Color(28, 28, 40);
    public static final Color C_SUPERFICIE2 = new Color(38, 38, 54);
    public static final Color C_PRIMARIO = new Color(255, 87, 34); // #FF5722
    public static final Color C_PRIM_HOVER = new Color(230, 74, 25);
    public static final Color C_ACENTO = new Color(255, 171, 64);
    public static final Color C_TEXTO = new Color(240, 240, 245);
    public static final Color C_TEXTO_S = new Color(155, 155, 175);
    public static final Color C_BORDE = new Color(50, 50, 72);
    public static final Color C_VERDE = new Color(72, 199, 142);
    public static final Color C_ROJO = new Color(245, 101, 101);
    public static final Color C_SIDEBAR_ACT = new Color(60, 28, 14);
    public static final Color C_SOMBRA = new Color(0, 0, 0, 120); // sombra semitransparente

    // --- Composition Root ---
    private final ProductoService productoService;
    private final VentaService ventaService;
    @SuppressWarnings("unused")
    private final ClienteService clienteService;

    // --- Navegación raíz ---
    private final CardLayout rootLayout = new CardLayout();
    private final JPanel rootPanel = new JPanel(rootLayout);

    private AppShell currentShell;

    public MainWindow() {
        // Construir servicios una sola vez para toda la sesión
        ProductoRepository pr = new ProductoRepository();
        VentaRepository vr = new VentaRepository();
        ClienteRepository cr = new ClienteRepository();

        this.productoService = new ProductoService(pr);
        this.ventaService = new VentaService(vr, pr);
        this.clienteService = new ClienteService(cr);

        setTitle("Catys ERP/POS v4.0");
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 660));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        rootPanel.setBackground(C_FONDO);
        rootPanel.add(new LoginGUI(this), "LOGIN");
        setContentPane(rootPanel);
        rootLayout.show(rootPanel, "LOGIN");
    }

    /**
     * Llamado por LoginGUI cuando las credenciales son correctas.
     * Construye el AppShell con todos los sub-paneles y lo muestra.
     */
    public void onLoginExitoso(Usuario usuario) {
        AppLogger.info("onLoginExitoso → construyendo AppShell para: " + usuario.getUsername());
        currentShell = new AppShell(this, usuario, productoService, ventaService);
        rootPanel.add(currentShell, "APP");
        rootLayout.show(rootPanel, "APP");
        currentShell.inicializar();
        revalidate();
        repaint();
    }

    /**
     * Destruye el AppShell y vuelve al Login (Cerrar Sesión).
     */
    public void mostrarLogin() {
        if (currentShell != null) {
            rootPanel.remove(currentShell);
            currentShell = null;
        }
        LoginGUI nuevoLogin = new LoginGUI(this);
        rootPanel.add(nuevoLogin, "LOGIN");
        rootLayout.show(rootPanel, "LOGIN");
        revalidate();
        repaint();
    }

    // ==========================================
    // PUNTO DE ENTRADA ÚNICO
    // ==========================================

    public static void main(String[] args) {
        AppLogger.init();
        AppLogger.info("Iniciando Catys ERP/POS v4.0...");

        // FlatDarkLaf ANTES de cualquier componente Swing
        try {
            FlatDarkLaf.setup();

            // Arcs redondeados globales
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("CheckBox.arc", 6);

            // Tipografía premium — Segoe UI en toda la app
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));

            // Tabla
            UIManager.put("Table.alternateRowColor", new Color(30, 30, 44));
            UIManager.put("Table.selectionBackground", new Color(255, 87, 34, 180));
            UIManager.put("Table.rowHeight", 34);

            // Scrollbar minimalista
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 8);
            UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));

            // Separadores y bordes
            UIManager.put("Separator.foreground", new Color(50, 50, 72));

            // Padding en campos de texto
            UIManager.put("TextField.margin", new java.awt.Insets(6, 10, 6, 10));
            UIManager.put("PasswordField.margin", new java.awt.Insets(6, 10, 6, 10));

            // Botones con más padding
            UIManager.put("Button.margin", new java.awt.Insets(8, 16, 8, 16));

            // ComboBox
            UIManager.put("ComboBox.arc", 10);

        } catch (Exception e) {
            AppLogger.warn("FlatDarkLaf no disponible: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
