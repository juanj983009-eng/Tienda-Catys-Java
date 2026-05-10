package vista;

import dto.ItemVentaDTO;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import modelo.Carrito;
import modelo.MetodoPago;
import modelo.Producto;
import repository.exception.DataAccessException;
import service.ProductoService;
import service.VentaService;
import service.exception.VentaException;
import util.AppLogger;
import util.GeneradorPDF;
import vista.components.BotonRedondeado;

/**
 * Panel del Punto de Venta (v4.0 — Single Window).
 *
 * CAMBIOS vs versión anterior:
 * - Extiende JPanel en lugar de JFrame
 * - Flujo interno manejado por CardLayout propio: CARGA → NOMBRE → PAGO →
 * CATALOGO
 * - cargarProductos() es public — lo invoca AppShell.inicializar()
 * - "Cerrar Sesión" eliminado del catálogo — el Sidebar del AppShell lo maneja
 * - Constructor deprecado añadido para compatibilidad de compilación con
 * DashboardFrame
 */
public class TiendaGUI extends JPanel {

    private final MainWindow mainWindow;
    private final VentaService ventaService;
    private final ProductoService productoService;

    // Estado de la sesión del cliente
    private final Carrito carrito = new Carrito();
    private String nombreCliente = "";
    private MetodoPago metodoPago = MetodoPago.EFECTIVO;

    // Listas de productos por categoría
    private List<Producto> listaCriollo;
    private List<Producto> listaChifa;
    private List<Producto> listaFastFood;
    private List<Producto> listaBebidas;

    // Navegación interna del flujo de cliente
    private final CardLayout innerLayout = new CardLayout();

    // Navegación del catálogo (pestañas de categoría)
    private JPanel panelContenido;
    private CardLayout cardLayout;
    private BotonRedondeado btnCatActivo;

    // Paleta — desde la fuente maestra
    private static final Color C_FONDO = MainWindow.C_FONDO;
    private static final Color C_SUPERF = MainWindow.C_SUPERFICIE;
    private static final Color C_SUPERF2 = MainWindow.C_SUPERFICIE2;
    private static final Color C_PRIMARIO = MainWindow.C_PRIMARIO;
    private static final Color C_PRIM_HOV = MainWindow.C_PRIM_HOVER;
    private static final Color C_ACENTO = MainWindow.C_ACENTO;
    private static final Color C_TEXTO = MainWindow.C_TEXTO;
    private static final Color C_TEXTO_S = MainWindow.C_TEXTO_S;
    private static final Color C_BORDE = MainWindow.C_BORDE;
    private static final Color C_VERDE = MainWindow.C_VERDE;
    private static final Color C_ROJO = MainWindow.C_ROJO;

    // ==========================================
    // CONSTRUCTORES
    // ==========================================

    public TiendaGUI(MainWindow mainWindow, ProductoService productoService, VentaService ventaService) {
        this.mainWindow = mainWindow;
        this.productoService = productoService;
        this.ventaService = ventaService;
        setLayout(innerLayout);
        setBackground(C_FONDO);
        add(buildCarga(), "CARGA");
        innerLayout.show(this, "CARGA");
    }

    /** @deprecated Solo para compatibilidad de compilación con código legado. */
    @Deprecated
    public TiendaGUI(VentaService ventaService, ProductoService productoService) {
        this(null, productoService, ventaService);
    }

    // ==========================================
    // CARGA DE PRODUCTOS (llamado desde AppShell)
    // ==========================================

    /**
     * Dispara la carga de productos en background.
     * Muestra la pantalla de carga hasta que el SwingWorker termina.
     */
    public void cargarProductos() {
        innerLayout.show(this, "CARGA");

        new SwingWorker<Map<String, List<Producto>>, Void>() {
            @Override
            protected Map<String, List<Producto>> doInBackground() throws Exception {
                return productoService.getProductosPorCategoria();
            }

            @Override
            protected void done() {
                try {
                    Map<String, List<Producto>> m = get();
                    listaCriollo = m.getOrDefault("CRIOLLO", List.of());
                    listaChifa = m.getOrDefault("CHIFA", List.of());
                    listaFastFood = m.getOrDefault("FAST FOOD", List.of());
                    listaBebidas = m.getOrDefault("BEBIDAS", List.of());

                    AppLogger.info(String.format(
                            "Productos cargados — Criollo:%d | Chifa:%d | FastFood:%d | Bebidas:%d",
                            listaCriollo.size(), listaChifa.size(), listaFastFood.size(), listaBebidas.size()));

                    // Construir paneles restantes y mostrar el flujo del cliente
                    add(buildNombre(), "NOMBRE");
                    add(buildPago(), "PAGO");
                    add(buildCatalogo(), "CATALOGO");
                    revalidate();
                    innerLayout.show(TiendaGUI.this, "NOMBRE");

                } catch (Exception ex) {
                    AppLogger.error("Error crítico al cargar productos", ex);
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(mainWindow,
                            "❌ No se pudieron cargar los productos.\nDetalle: " + ex.getMessage(),
                            "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // ==========================================
    // PANTALLA: CARGA
    // ==========================================

    private JPanel buildCarga() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(C_FONDO);
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(C_FONDO);
        JLabel icono = mkLbl("🐱🍜", "Segoe UI Emoji", Font.PLAIN, 90, C_TEXTO);
        icono.setAlignmentX(CENTER_ALIGNMENT);
        JLabel msg = mkLbl("Conectando con la cocina...", "Segoe UI", Font.PLAIN, 16, C_TEXTO_S);
        msg.setAlignmentX(CENTER_ALIGNMENT);
        col.add(icono);
        col.add(Box.createVerticalStrut(18));
        col.add(msg);
        p.add(col);
        return p;
    }

    // ==========================================
    // PANTALLA: NOMBRE DEL CLIENTE
    // ==========================================

    private JPanel buildNombre() {
        JPanel fondo = new JPanel(new BorderLayout());
        fondo.setBackground(C_FONDO);

        // Lateral decorativo
        JPanel lateral = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, C_PRIMARIO, 0, getHeight(), C_PRIM_HOV));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        lateral.setPreferredSize(new Dimension(380, 0));
        lateral.setOpaque(false);

        JPanel ltCon = new JPanel();
        ltCon.setLayout(new BoxLayout(ltCon, BoxLayout.Y_AXIS));
        ltCon.setOpaque(false);
        JLabel eL = mkLbl("🐱🍜", "Segoe UI Emoji", Font.PLAIN, 100, Color.WHITE);
        eL.setAlignmentX(CENTER_ALIGNMENT);
        JLabel mL = mkLbl("Restaurante Catys", "Segoe UI", Font.BOLD, 26, Color.WHITE);
        mL.setAlignmentX(CENTER_ALIGNMENT);
        JLabel sL = new JLabel("<html><center>Comida con alma,<br>servida con amor 🧡</center></html>");
        sL.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sL.setForeground(new Color(255, 220, 200));
        sL.setHorizontalAlignment(SwingConstants.CENTER);
        sL.setAlignmentX(CENTER_ALIGNMENT);
        ltCon.add(eL);
        ltCon.add(Box.createVerticalStrut(18));
        ltCon.add(mL);
        ltCon.add(Box.createVerticalStrut(10));
        ltCon.add(sL);
        lateral.add(ltCon);

        // Formulario de nombre
        JPanel derecho = new JPanel(new GridBagLayout());
        derecho.setBackground(C_FONDO);
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(C_SUPERF);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(C_BORDE, 1, true), new EmptyBorder(50, 55, 50, 55)));

        JLabel lTit = mkLbl("¡Bienvenido!", "Segoe UI", Font.BOLD, 26, C_TEXTO);
        JLabel lSub = mkLbl("Dinos tu nombre para empezar", "Segoe UI", Font.PLAIN, 14, C_TEXTO_S);
        JLabel lLbl = mkLbl("Tu nombre", "Segoe UI", Font.BOLD, 12, C_TEXTO_S);
        lTit.setAlignmentX(LEFT_ALIGNMENT);
        lSub.setAlignmentX(LEFT_ALIGNMENT);
        lLbl.setAlignmentX(LEFT_ALIGNMENT);

        JTextField campo = new JTextField();
        campo.setName("campoNombre");
        campo.setMaximumSize(new Dimension(300, 46));
        campo.setPreferredSize(new Dimension(300, 46));
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        campo.setBackground(C_SUPERF2);
        campo.setForeground(C_TEXTO);
        campo.setCaretColor(C_TEXTO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(C_BORDE, 1, true), new EmptyBorder(6, 12, 6, 12)));
        campo.setAlignmentX(LEFT_ALIGNMENT);

        BotonRedondeado btnIng = new BotonRedondeado("Ingresar →", C_PRIMARIO, C_PRIM_HOV, Color.WHITE);
        btnIng.setName("btnIngresar");
        btnIng.setAlignmentX(LEFT_ALIGNMENT);
        btnIng.setMaximumSize(new Dimension(300, 48));
        btnIng.setFont(new Font("Segoe UI", Font.BOLD, 15));

        Runnable ingresar = () -> {
            String nombre = campo.getText().trim();
            if (!nombre.isEmpty()) {
                this.nombreCliente = nombre;
                innerLayout.show(this, "PAGO");
            } else {
                JOptionPane.showMessageDialog(mainWindow, "Por favor, dinos cómo te llamas 😊",
                        "Campo requerido", JOptionPane.WARNING_MESSAGE);
            }
        };
        btnIng.addActionListener(e -> ingresar.run());
        campo.addActionListener(e -> ingresar.run());

        tarjeta.add(lTit);
        tarjeta.add(Box.createVerticalStrut(6));
        tarjeta.add(lSub);
        tarjeta.add(Box.createVerticalStrut(36));
        tarjeta.add(lLbl);
        tarjeta.add(Box.createVerticalStrut(6));
        tarjeta.add(campo);
        tarjeta.add(Box.createVerticalStrut(28));
        tarjeta.add(btnIng);
        derecho.add(tarjeta);

        fondo.add(lateral, BorderLayout.WEST);
        fondo.add(derecho, BorderLayout.CENTER);
        return fondo;
    }

    // ==========================================
    // PANTALLA: MÉTODO DE PAGO
    // ==========================================

    private JPanel buildPago() {
        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(C_FONDO);

        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(C_SUPERF);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(C_BORDE, 1, true), new EmptyBorder(45, 55, 45, 55)));

        JLabel lTit = mkLbl("¿Cómo deseas pagar?", "Segoe UI", Font.BOLD, 24, C_PRIMARIO);
        JLabel lSub = mkLbl("Selecciona tu método de pago", "Segoe UI", Font.PLAIN, 14, C_TEXTO_S);
        JLabel lLbl = mkLbl("Método de pago", "Segoe UI", Font.BOLD, 12, C_TEXTO_S);
        lTit.setAlignmentX(LEFT_ALIGNMENT);
        lSub.setAlignmentX(LEFT_ALIGNMENT);
        lLbl.setAlignmentX(LEFT_ALIGNMENT);

        JComboBox<MetodoPago> combo = new JComboBox<>(MetodoPago.values());
        combo.setName("comboPago");
        combo.setMaximumSize(new Dimension(280, 44));
        combo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        combo.setBackground(C_SUPERF2);
        combo.setForeground(C_TEXTO);
        combo.setAlignmentX(LEFT_ALIGNMENT);
        combo.setRenderer((list, val, idx, sel, foc) -> {
            JLabel l = new JLabel(val != null ? val.getDisplayName() : "");
            l.setOpaque(true);
            l.setBorder(new EmptyBorder(6, 12, 6, 12));
            l.setBackground(sel ? C_PRIMARIO : C_SUPERF2);
            l.setForeground(C_TEXTO);
            return l;
        });

        BotonRedondeado btnMenu = new BotonRedondeado("Ver el Menú →", C_PRIMARIO, C_PRIM_HOV, Color.WHITE);
        btnMenu.setName("btnVerMenu");
        btnMenu.setAlignmentX(LEFT_ALIGNMENT);
        btnMenu.setMaximumSize(new Dimension(280, 48));
        btnMenu.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnMenu.addActionListener(e -> {
            this.metodoPago = (MetodoPago) combo.getSelectedItem();
            innerLayout.show(this, "CATALOGO");
        });

        tarjeta.add(lTit);
        tarjeta.add(Box.createVerticalStrut(6));
        tarjeta.add(lSub);
        tarjeta.add(Box.createVerticalStrut(30));
        tarjeta.add(lLbl);
        tarjeta.add(Box.createVerticalStrut(8));
        tarjeta.add(combo);
        tarjeta.add(Box.createVerticalStrut(30));
        tarjeta.add(btnMenu);
        fondo.add(tarjeta);
        return fondo;
    }

    // ==========================================
    // PANTALLA: CATÁLOGO DE PRODUCTOS
    // ==========================================

    private JPanel buildCatalogo() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_FONDO);

        // Pestañas de categoría
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        navPanel.setBackground(C_FONDO);
        navPanel.setBorder(new EmptyBorder(0, 18, 0, 18));

        cardLayout = new CardLayout();
        panelContenido = new JPanel(cardLayout);
        panelContenido.setBackground(C_FONDO);

        panelContenido.add(buildGrid(listaCriollo), "CRIOLLO");
        panelContenido.add(buildGrid(listaChifa), "CHIFA");
        panelContenido.add(buildGrid(listaFastFood), "FAST FOOD");
        panelContenido.add(buildGrid(listaBebidas), "BEBIDAS");

        BotonRedondeado[] cats = {
                mkBtnCat("🥘 Criollo", "CRIOLLO", navPanel),
                mkBtnCat("🍜 Chifa", "CHIFA", navPanel),
                mkBtnCat("🍔 Fast Food", "FAST FOOD", navPanel),
                mkBtnCat("🥤 Bebidas", "BEBIDAS", navPanel),
        };
        activarCat(cats[0], "CRIOLLO");

        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(C_FONDO);
        centro.add(navPanel, BorderLayout.NORTH);
        centro.add(panelContenido, BorderLayout.CENTER);

        // Barra inferior (sin botón "Cerrar Sesión" — está en el Sidebar)
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        barra.setBackground(MainWindow.C_SUPERFICIE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, C_BORDE),
                new EmptyBorder(4, 16, 4, 16)));

        JButton btnVer = mkAccion("📋 Ver Pedido", C_SUPERF2, C_SUPERF2);
        JButton btnVac = mkAccion("🗑 Vaciar", C_SUPERF2, C_SUPERF2);
        JButton btnConf = mkAccion("✅ Confirmar", new Color(30, 90, 55), C_VERDE);

        btnVer.addActionListener(e -> mostrarResumen());
        btnVac.addActionListener(e -> {
            carrito.vaciar();
            JOptionPane.showMessageDialog(mainWindow, "Carrito vaciado.", "Info", JOptionPane.INFORMATION_MESSAGE);
        });
        btnConf.addActionListener(e -> {
            if (carrito.estaVacio()) {
                JOptionPane.showMessageDialog(mainWindow, "El carrito está vacío. Agrega productos primero.",
                        "Carrito vacío", JOptionPane.WARNING_MESSAGE);
            } else {
                procesarVenta(btnConf);
            }
        });

        barra.add(btnVer);
        barra.add(btnVac);
        barra.add(btnConf);

        root.add(centro, BorderLayout.CENTER);
        root.add(barra, BorderLayout.SOUTH);
        return root;
    }

    // ==========================================
    // PROCESAMIENTO DE VENTA (SwingWorker)
    // ==========================================

    private void procesarVenta(JButton btnConf) {
        List<ItemVentaDTO> items = ventaService.agruparItems(carrito.getLista());
        String cliente = this.nombreCliente;
        MetodoPago pago = this.metodoPago;
        String resumen = ventaService.generarResumenCarrito(items);
        double total = ventaService.calcularTotal(items);

        btnConf.setEnabled(false);
        btnConf.setText("Procesando...");

        new SwingWorker<Void, Void>() {
            private boolean ok = false;
            private String err = null;

            @Override
            protected Void doInBackground() {
                try {
                    ventaService.procesarVenta(cliente, pago, items);
                    ok = true;
                    GeneradorPDF.crearTicket(cliente, pago.getDisplayName(), carrito.getLista(), total);
                } catch (VentaException e) {
                    err = "⚠ " + e.getMessage();
                } catch (DataAccessException e) {
                    AppLogger.error("Error de BD al procesar venta", e);
                    err = "❌ Error al guardar en la base de datos.\n" + e.getMessage();
                } catch (Exception e) {
                    AppLogger.error("Error inesperado en venta", e);
                    err = "❌ Error inesperado: " + e.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                btnConf.setEnabled(true);
                btnConf.setText("✅ Confirmar");
                if (ok) {
                    mostrarTicket(resumen, cliente, pago);
                    carrito.vaciar();
                    nombreCliente = "";
                    recargarBackground();
                    innerLayout.show(TiendaGUI.this, "NOMBRE");
                } else {
                    JOptionPane.showMessageDialog(mainWindow, err,
                            "Error en la venta", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void recargarBackground() {
        new SwingWorker<Map<String, List<Producto>>, Void>() {
            @Override
            protected Map<String, List<Producto>> doInBackground() throws Exception {
                return productoService.getProductosPorCategoria();
            }

            @Override
            protected void done() {
                try {
                    Map<String, List<Producto>> m = get();
                    listaCriollo = m.getOrDefault("CRIOLLO", List.of());
                    listaChifa = m.getOrDefault("CHIFA", List.of());
                    listaFastFood = m.getOrDefault("FAST FOOD", List.of());
                    listaBebidas = m.getOrDefault("BEBIDAS", List.of());
                    AppLogger.debug("Catálogo recargado con stock actualizado.");
                } catch (Exception ex) {
                    AppLogger.error("Error al recargar el catálogo", ex);
                }
            }
        }.execute();
    }

    private void mostrarTicket(String resumen, String cliente, MetodoPago pago) {
        StringBuilder sb = new StringBuilder();
        sb.append("       RESTAURANTE CATYS\n");
        sb.append("─────────────────────────────────\n");
        sb.append("Cliente : ").append(cliente).append("\n");
        sb.append("Pago    : ").append(pago.getDisplayName()).append("\n");
        sb.append("─────────────────────────────────\n");
        sb.append(resumen);
        sb.append("\n─────────────────────────────────\n");
        sb.append("      ¡Gracias por tu compra! 🧡\n");
        sb.append("  (✔ Guardado en BD y Stock)\n");
        sb.append("  (📄 PDF Generado)\n");
        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBackground(C_SUPERF);
        area.setForeground(C_TEXTO);
        JOptionPane.showMessageDialog(mainWindow, new JScrollPane(area), "Ticket de venta", JOptionPane.PLAIN_MESSAGE);
    }

    private void mostrarResumen() {
        List<ItemVentaDTO> items = ventaService.agruparItems(carrito.getLista());
        String res = ventaService.generarResumenCarrito(items);
        JTextArea area = new JTextArea(res.isEmpty() ? "El carrito está vacío." : res);
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        area.setEditable(false);
        area.setBackground(C_SUPERF);
        area.setForeground(C_TEXTO);
        JOptionPane.showMessageDialog(mainWindow, new JScrollPane(area), "Detalle del Pedido",
                JOptionPane.PLAIN_MESSAGE);
    }

    // ==========================================
    // COMPONENTES DE UI
    // ==========================================

    private JScrollPane buildGrid(List<Producto> lista) {
        JPanel grid = new JPanel(new GridLayout(0, 3, 24, 24));
        grid.setBackground(C_FONDO);
        grid.setBorder(new EmptyBorder(28, 28, 28, 28));

        if (lista != null && !lista.isEmpty()) {
            for (Producto p : lista)
                grid.add(crearTarjeta(p));
        } else {
            grid.setLayout(new GridBagLayout());
            JLabel empty = new JLabel("Sin productos en esta categoría", SwingConstants.CENTER);
            empty.setForeground(C_TEXTO_S);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            grid.add(empty);
        }

        JScrollPane sc = new JScrollPane(grid);
        sc.setBorder(null);
        sc.setBackground(C_FONDO);
        sc.getViewport().setBackground(C_FONDO);
        sc.getVerticalScrollBar().setUnitIncrement(30);
        sc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return sc;
    }

    private JPanel crearTarjeta(Producto p) {
        // Panel exterior para efecto de sombra (un rectángulo ligeramente más grande
        // y más oscuro simula sombra en Swing sin librerías externas)
        JPanel sombra = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Sombra difuminada: 3 capas semi-transparentes desplazadas
                for (int i = 4; i > 0; i--) {
                    g2.setColor(new Color(0, 0, 0, 20 * i));
                    g2.fill(new RoundRectangle2D.Float(i, i + 2, getWidth() - i * 2, getHeight() - i * 2, 18, 18));
                }
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        sombra.setOpaque(false);

        // Panel principal de la tarjeta
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));
                // Borde sutil
                g2.setColor(new Color(60, 60, 85));
                g2.setStroke(new java.awt.BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 18, 18));
                g2.dispose();
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        card.setBackground(C_SUPERF);
        card.setOpaque(false);

        // ── Micro-interacción: hover eleva la tarjeta (cambia el color a más claro)
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(C_SUPERF2);
                card.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(C_SUPERF);
                card.repaint();
            }
        });

        // Imagen
        JPanel imgP = new JPanel(new GridBagLayout());
        imgP.setOpaque(false);
        imgP.setBorder(new EmptyBorder(18, 18, 10, 18));
        imgP.add(new JLabel(ImagenUtil.cargar(p.getImagen(), 160, 110)));
        card.add(imgP, BorderLayout.NORTH);

        // Info textual
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.setBorder(new EmptyBorder(4, 18, 18, 18));

        // Nombre del plato — más grande y en negrita
        String nm = p.getNombre().length() > 22 ? p.getNombre().substring(0, 19) + "…" : p.getNombre();
        JLabel lNom = mkLbl(nm, "Segoe UI", Font.BOLD, 16, C_TEXTO);
        lNom.setAlignmentX(LEFT_ALIGNMENT);

        // Precio — naranja vibrante #FF5722, tamaño grande, lo primero que ve el ojo
        JLabel lPr = mkLbl("S/ " + String.format("%.2f", p.getPrecio()),
                "Segoe UI", Font.BOLD, 20, C_PRIMARIO);
        lPr.setAlignmentX(LEFT_ALIGNMENT);

        // Stock — badge con fondo
        JLabel lSt = new JLabel(
                p.isDisponible() ? "●  Stock: " + p.getStock() : "●  AGOTADO") {
            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        lSt.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lSt.setForeground(p.isDisponible() ? C_VERDE : C_ROJO);
        lSt.setAlignmentX(LEFT_ALIGNMENT);

        BotonRedondeado btnAg = new BotonRedondeado(
                p.isDisponible() ? "＋ Agregar al pedido" : "Agotado",
                p.isDisponible() ? C_PRIMARIO : new Color(50, 50, 65),
                p.isDisponible() ? C_PRIM_HOV : new Color(50, 50, 65),
                Color.WHITE);
        btnAg.setName("btnAgregar_" + p.getId());
        btnAg.setAlignmentX(LEFT_ALIGNMENT);
        btnAg.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnAg.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAg.setEnabled(p.isDisponible());

        if (p.isDisponible()) {
            btnAg.addActionListener(e -> {
                JSpinner sp = new JSpinner(new SpinnerNumberModel(1, 1, p.getStock(), 1));
                int op = JOptionPane.showOptionDialog(mainWindow,
                        new Object[] { "¿Cuántas unidades?", sp }, "Cantidad",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (op == JOptionPane.OK_OPTION) {
                    int cant = (Integer) sp.getValue();
                    for (int i = 0; i < cant; i++)
                        carrito.agregarProducto(p);
                    JOptionPane.showMessageDialog(mainWindow,
                            cant + " × " + p.getNombre() + " añadido al pedido ✓",
                            "Producto Agregado", JOptionPane.INFORMATION_MESSAGE);
                }
            });
        }

        info.add(lNom);
        info.add(Box.createVerticalStrut(6));
        info.add(lPr);
        info.add(Box.createVerticalStrut(6));
        info.add(lSt);
        info.add(Box.createVerticalStrut(16));
        info.add(btnAg);
        card.add(info, BorderLayout.CENTER);

        sombra.add(card, BorderLayout.CENTER);
        sombra.setBorder(new EmptyBorder(0, 0, 6, 6)); // espacio para la sombra
        return sombra;
    }

    private BotonRedondeado mkBtnCat(String texto, String card, JPanel nav) {
        BotonRedondeado btn = new BotonRedondeado(texto, C_SUPERF, C_SUPERF2, C_TEXTO_S);
        btn.setPreferredSize(new Dimension(150, 44));
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(C_BORDE, 1, true), new EmptyBorder(0, 10, 0, 10)));
        btn.addActionListener(e -> activarCat(btn, card));
        nav.add(btn);
        return btn;
    }

    private void activarCat(BotonRedondeado btn, String card) {
        if (btnCatActivo != null)
            btnCatActivo.setNormalColor(C_SUPERF, C_SUPERF2, C_TEXTO_S);
        btn.setNormalColor(C_PRIMARIO, C_PRIM_HOV, Color.WHITE);
        btnCatActivo = btn;
        if (cardLayout != null)
            cardLayout.show(panelContenido, card);
    }

    private JButton mkAccion(String txt, Color bg, Color hover) {
        JButton btn = new JButton(txt);
        btn.setBackground(bg);
        btn.setForeground(C_TEXTO);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(11, 24, 11, 24));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(hover);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    private JLabel mkLbl(String t, String f, int s, int sz, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(new Font(f, s, sz));
        l.setForeground(c);
        return l;
    }
}