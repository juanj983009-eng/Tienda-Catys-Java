package vista;

import dao.ProductoDAO;
import dao.VentaDAO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import modelo.Carrito;
import modelo.Producto;
import util.GeneradorPDF; // <--- ¡IMPORTANTE PARA EL TICKET!

public class TiendaGUI {

    private static Carrito carrito = new Carrito();
    
    // Listas de productos
    private static ArrayList<Producto> listaCriollo = new ArrayList<>();
    private static ArrayList<Producto> listaChifa = new ArrayList<>();
    private static ArrayList<Producto> listaFastFood = new ArrayList<>();
    private static ArrayList<Producto> listaBebidas = new ArrayList<>();

    // Navegación
    private static JPanel panelContenido;
    private static CardLayout cardLayout;

    // --- COLORES ---
    private static final Color COLOR_FONDO_APP = new Color(240, 242, 245);
    private static final Color COLOR_TARJETA = Color.WHITE;
    private static final Color COLOR_PRIMARIO = new Color(255, 99, 71); 
    private static final Color COLOR_SECUNDARIO = new Color(55, 65, 81);
    private static final Color COLOR_TEXTO = new Color(51, 51, 51);
    private static final Color COLOR_TEXTO_SUAVE = new Color(100, 116, 139);
    private static final Color COLOR_VERDE = new Color(72, 187, 120);
    private static final Color COLOR_ROJO = new Color(245, 101, 101); 

    private static String nombreUsuario = "";
    private static JTextField campoNombre;
    private static JComboBox<String> metodoPagoComboBox;

    // --- CLASE BOTÓN REDONDEADO ---
    static class BotonRedondeado extends JButton {
        private Color colorNormal, colorHover;
        public BotonRedondeado(String texto, Color bgNormal, Color bgHover, Color textoColor) {
            super(texto);
            this.colorNormal = bgNormal; this.colorHover = bgHover;
            setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false);
            setForeground(textoColor); setFont(new Font("Segoe UI", Font.BOLD, 13));
            setCursor(new Cursor(Cursor.HAND_CURSOR)); setBackground(colorNormal);
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(colorHover); repaint(); }
                public void mouseExited(MouseEvent e) { setBackground(colorNormal); repaint(); }
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 25, 25));
            super.paintComponent(g); g2.dispose();
        }
    }

    // --- CARGAR DATOS DESDE SQL SERVER ---
    public static void cargarProductosDesdeBD() {
        ProductoDAO dao = new ProductoDAO();
        listaCriollo = dao.listarPorCategoria("CRIOLLO");
        listaChifa = dao.listarPorCategoria("CHIFA");
        listaFastFood = dao.listarPorCategoria("FAST_FOOD");
        listaBebidas = dao.listarPorCategoria("BEBIDAS");
    }

    // --- IMÁGENES ---
    public static ImageIcon redimensionarImagen(String path) {
        try {
            java.net.URL imgURL = TiendaGUI.class.getClassLoader().getResource(path);
            if (imgURL == null && !new java.io.File(path).exists()) return crearImagenGris(path);
            ImageIcon original = (imgURL != null) ? new ImageIcon(imgURL) : new ImageIcon(path);
            if (original.getIconWidth() <= 0) return crearImagenGris(path);
            Image img = original.getImage().getScaledInstance(160, 110, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) { return crearImagenGris(path); }
    }

    private static ImageIcon crearImagenGris(String path) {
        String nombre = new java.io.File(path).getName().replace(".jpg", "").replace("imagenes/", "");
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(160, 110, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(new Color(230,230,230)); g2.fillRect(0, 0, 160, 110);
        g2.setColor(Color.GRAY); g2.drawString(nombre, 20, 60); g2.dispose();
        return new ImageIcon(img);
    }

    // --- TARJETA DE PRODUCTO ---
    public static JPanel crearPanelProducto(Producto producto) {
        JPanel panelTarjeta = new JPanel(new BorderLayout());
        panelTarjeta.setBackground(COLOR_TARJETA);
        panelTarjeta.setBorder(new LineBorder(new Color(230, 230, 230), 1));

        JPanel panelImagen = new JPanel(new GridBagLayout());
        panelImagen.setBackground(COLOR_TARJETA);
        panelImagen.setBorder(new EmptyBorder(15, 15, 5, 15));
        panelImagen.add(new JLabel(redimensionarImagen(producto.getImagen())));
        panelTarjeta.add(panelImagen, BorderLayout.NORTH);

        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.setBackground(COLOR_TARJETA);
        panelInfo.setBorder(new EmptyBorder(5, 15, 15, 15));

        String nombreDisplay = producto.getNombre().length() > 22 ? producto.getNombre().substring(0, 19) + "..." : producto.getNombre();
        JLabel nombreLabel = new JLabel(nombreDisplay);
        nombreLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JLabel precioLabel = new JLabel("S/ " + String.format("%.2f", producto.getPrecio()));
        precioLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        precioLabel.setForeground(COLOR_PRIMARIO);
        
        // Stock visual
        String textoStock = producto.isDisponible() ? "Stock: " + producto.getStock() : "AGOTADO";
        JLabel estadoLabel = new JLabel(textoStock);
        estadoLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        estadoLabel.setForeground(producto.isDisponible() ? COLOR_VERDE : COLOR_ROJO);

        BotonRedondeado botonAgregar = new BotonRedondeado("Agregar", COLOR_PRIMARIO, new Color(220, 80, 60), Color.WHITE);
        botonAgregar.setAlignmentX(Component.LEFT_ALIGNMENT);
        botonAgregar.setMaximumSize(new Dimension(180, 35));

        // Deshabilitar si no hay stock
        if (!producto.isDisponible()) {
            botonAgregar.setEnabled(false);
            botonAgregar.setBackground(Color.GRAY);
            botonAgregar.setText("Sin Stock");
        }

        botonAgregar.addActionListener(e -> {
            if (producto.isDisponible()) {
                JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, producto.getStock(), 1));
                int opcion = JOptionPane.showOptionDialog(null, 
                    new Object[] {"¿Cuántas unidades?", spinner}, 
                    "Cantidad", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

                if (opcion == JOptionPane.OK_OPTION) {
                    int cantidad = (Integer) spinner.getValue();
                    if (cantidad <= producto.getStock()) {
                        for (int i = 0; i < cantidad; i++) carrito.agregarProducto(producto);
                        JOptionPane.showMessageDialog(null, "¡Agregado al carrito!");
                    } else {
                        JOptionPane.showMessageDialog(null, "No hay suficiente stock.");
                    }
                }
            }
        });

        panelInfo.add(nombreLabel); panelInfo.add(Box.createVerticalStrut(5));
        panelInfo.add(precioLabel); panelInfo.add(Box.createVerticalStrut(5));
        panelInfo.add(estadoLabel); panelInfo.add(Box.createVerticalStrut(15));
        panelInfo.add(botonAgregar);
        panelTarjeta.add(panelInfo, BorderLayout.CENTER);
        return panelTarjeta;
    }

    // --- PANTALLA 1: LOGIN ---
    public static void mostrarCampoNombre(JFrame ventana) {
        ventana.getContentPane().removeAll();
        JPanel panelCentral = new JPanel(new GridBagLayout());
        panelCentral.setBackground(COLOR_FONDO_APP);
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(COLOR_TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(220, 220, 230), 1), new EmptyBorder(50, 60, 50, 60)));

        JLabel icono = new JLabel("🐱🍜"); icono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 70)); icono.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel titulo = new JLabel("Restaurante Catys"); titulo.setFont(new Font("Segoe UI", Font.BOLD, 28)); titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel subtitulo = new JLabel("¡Qué alegría verte! ¿Listo para comer rico?"); subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14)); subtitulo.setForeground(COLOR_TEXTO_SUAVE); subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        campoNombre = new JTextField(); campoNombre.setMaximumSize(new Dimension(280, 45)); campoNombre.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campoNombre.setHorizontalAlignment(JTextField.CENTER);
        campoNombre.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 200, 200), 1), new EmptyBorder(5, 10, 5, 10)));

        JLabel lblInput = new JLabel("Ingresa tu nombre:", SwingConstants.CENTER);
        lblInput.setFont(new Font("Segoe UI", Font.BOLD, 12)); lblInput.setForeground(COLOR_TEXTO_SUAVE);
        lblInput.setAlignmentX(Component.CENTER_ALIGNMENT); lblInput.setMaximumSize(new Dimension(280, 20)); 
        
        BotonRedondeado btn = new BotonRedondeado("Ingresar ahora", COLOR_PRIMARIO, new Color(220, 80, 60), Color.WHITE);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT); btn.setMaximumSize(new Dimension(280, 50));

        btn.addActionListener(e -> {
            nombreUsuario = campoNombre.getText();
            if (!nombreUsuario.trim().isEmpty()) mostrarMetodoPago(ventana);
            else JOptionPane.showMessageDialog(ventana, "Por favor, dinos cómo te llamas 😊");
        });

        tarjeta.add(icono); tarjeta.add(Box.createVerticalStrut(10)); tarjeta.add(titulo); tarjeta.add(Box.createVerticalStrut(5));
        tarjeta.add(subtitulo); tarjeta.add(Box.createVerticalStrut(35)); tarjeta.add(lblInput); tarjeta.add(Box.createVerticalStrut(5));
        tarjeta.add(campoNombre); tarjeta.add(Box.createVerticalStrut(25)); tarjeta.add(btn);
        panelCentral.add(tarjeta); ventana.add(panelCentral); ventana.revalidate(); ventana.repaint();
    }

    // --- PAGO ---
    public static void mostrarMetodoPago(JFrame ventana) {
        ventana.getContentPane().removeAll();
        JPanel panelCentral = new JPanel(new GridBagLayout());
        panelCentral.setBackground(COLOR_FONDO_APP);
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(COLOR_TARJETA);
        tarjeta.setBorder(new EmptyBorder(40, 50, 40, 50));

        JLabel label = new JLabel("¡Hola, " + nombreUsuario + "!"); label.setFont(new Font("Segoe UI", Font.BOLD, 22)); label.setForeground(COLOR_PRIMARIO); label.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel label2 = new JLabel("¿Cómo deseas pagar hoy?"); label2.setFont(new Font("Segoe UI", Font.PLAIN, 14)); label2.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        metodoPagoComboBox = new JComboBox<>(new String[]{"💳 Tarjeta de Crédito", "💵 Efectivo", "📱 Yape / Plin"});
        metodoPagoComboBox.setMaximumSize(new Dimension(250, 40)); metodoPagoComboBox.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

        BotonRedondeado btn = new BotonRedondeado("Ver el Menú", COLOR_PRIMARIO, new Color(220, 80, 60), Color.WHITE);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT); btn.setMaximumSize(new Dimension(250, 45));
        
        btn.addActionListener(e -> mostrarProductos(ventana));

        tarjeta.add(label); tarjeta.add(label2); tarjeta.add(Box.createVerticalStrut(25)); tarjeta.add(metodoPagoComboBox); 
        tarjeta.add(Box.createVerticalStrut(25)); tarjeta.add(btn);
        panelCentral.add(tarjeta); ventana.add(panelCentral); ventana.revalidate(); ventana.repaint();
    }

    // --- TIENDA (CENTRADA) ---
    public static void mostrarProductos(JFrame ventana) {
        ventana.getContentPane().removeAll();
        ventana.setLayout(new BorderLayout());

        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        header.setBackground(COLOR_PRIMARIO);
        JLabel titulo = new JLabel("CATYS | Menú Digital");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22)); titulo.setForeground(Color.WHITE); header.add(titulo);

        JPanel panelNavegacion = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelNavegacion.setBackground(COLOR_FONDO_APP);

        cardLayout = new CardLayout();
        panelContenido = new JPanel(cardLayout);
        panelContenido.setBackground(COLOR_FONDO_APP);

        panelContenido.add(crearGrid(listaCriollo), "CRIOLLO");
        panelContenido.add(crearGrid(listaChifa), "CHIFA");
        panelContenido.add(crearGrid(listaFastFood), "FAST_FOOD");
        panelContenido.add(crearGrid(listaBebidas), "BEBIDAS");

        crearBotonMenu("CRIOLLO", "CRIOLLO", panelNavegacion);
        crearBotonMenu("CHIFA", "CHIFA", panelNavegacion);
        crearBotonMenu("FAST FOOD", "FAST_FOOD", panelNavegacion);
        crearBotonMenu("BEBIDAS", "BEBIDAS", panelNavegacion);

        JPanel bloqueCentral = new JPanel(new BorderLayout());
        bloqueCentral.add(panelNavegacion, BorderLayout.NORTH);
        bloqueCentral.add(panelContenido, BorderLayout.CENTER);

        JPanel panelCarrito = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panelCarrito.setBackground(Color.WHITE);
        panelCarrito.setBorder(new LineBorder(new Color(230,230,230), 1));

        JButton btnVer = new JButton("Ver Pedido"); btnVer.setBackground(Color.DARK_GRAY); btnVer.setForeground(Color.WHITE);
        JButton btnVaciar = new JButton("Vaciar"); btnVaciar.setBackground(Color.LIGHT_GRAY); btnVaciar.setForeground(Color.BLACK);
        JButton btnFin = new JButton("Confirmar"); btnFin.setBackground(COLOR_VERDE); btnFin.setForeground(Color.WHITE);
        JButton btnSalir = new JButton("Cerrar Sesión"); btnSalir.setBackground(new Color(220, 53, 69)); btnSalir.setForeground(Color.WHITE); btnSalir.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnVer.addActionListener(e -> mostrarCarritoDetalle(ventana));
        btnVaciar.addActionListener(e -> { carrito.vaciar(); JOptionPane.showMessageDialog(ventana, "Carrito vaciado."); });
        btnFin.addActionListener(e -> {
            if(carrito.estaVacio()) JOptionPane.showMessageDialog(ventana, "Carrito vacío.");
            else generarRecibo(ventana);
        });
        btnSalir.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(ventana, "¿Seguro que desea salir al Login?", "Cerrar Sesión", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                ventana.dispose();
                new LoginGUI().setVisible(true);
            }
        });

        panelCarrito.add(btnSalir); panelCarrito.add(btnVer); panelCarrito.add(btnVaciar); panelCarrito.add(btnFin);

        ventana.add(header, BorderLayout.NORTH);
        ventana.add(bloqueCentral, BorderLayout.CENTER);
        ventana.add(panelCarrito, BorderLayout.SOUTH);
        ventana.revalidate(); ventana.repaint();
    }

    private static void crearBotonMenu(String texto, String cardName, JPanel panel) {
        BotonRedondeado btn = new BotonRedondeado(texto, Color.WHITE, new Color(245,245,245), COLOR_SECUNDARIO);
        btn.setPreferredSize(new Dimension(120, 40));
        btn.setBorder(new LineBorder(new Color(200,200,200), 1));
        btn.addActionListener(e -> cardLayout.show(panelContenido, cardName));
        panel.add(btn);
    }

    private static JScrollPane crearGrid(ArrayList<Producto> lista) {
        JPanel panelGrid = new JPanel(new GridLayout(0, 4, 15, 15));
        panelGrid.setBackground(COLOR_FONDO_APP);
        panelGrid.setBorder(new EmptyBorder(10, 25, 20, 25)); 
        for (Producto p : lista) panelGrid.add(crearPanelProducto(p));
        JScrollPane scroll = new JScrollPane(panelGrid);
        scroll.setBorder(null); scroll.getVerticalScrollBar().setUnitIncrement(20);
        return scroll;
    }

    private static void mostrarCarritoDetalle(JFrame ventana) {
         JTextArea area = new JTextArea(carrito.generarResumen()); 
         area.setFont(new Font("Monospaced", Font.PLAIN, 14));
         area.setEditable(false);
         JOptionPane.showMessageDialog(ventana, new JScrollPane(area), "Detalle", JOptionPane.PLAIN_MESSAGE);
    }

    // --- AQUÍ ESTÁ EL CAMBIO PARA GENERAR EL PDF ---
    public static void generarRecibo(JFrame ventana) {
        String resumenProductos = carrito.generarResumen();
        double totalVenta = carrito.obtenerTotalNumerico();
        String pago = metodoPagoComboBox.getSelectedItem().toString();

        // 1. Guardar en SQL Server
        VentaDAO dao = new VentaDAO();
        boolean exitoSQL = dao.registrarVenta(nombreUsuario, pago, resumenProductos, totalVenta, carrito);

        // 2. Generar PDF (Llamada a la clase utilitaria)
        GeneradorPDF.crearTicket(nombreUsuario, pago, carrito.getLista(), totalVenta);

        // 3. Mensaje
        StringBuilder sb = new StringBuilder();
        sb.append("         RESTAURANTE CATYS        \n");
        sb.append("------------------------------------\n");
        sb.append("Cliente: ").append(nombreUsuario).append("\n");
        sb.append("Pago:    ").append(pago).append("\n");
        sb.append("------------------------------------\n");
        sb.append(resumenProductos);
        sb.append("\n------------------------------------\n");
        sb.append("      ¡Gracias por tu compra!     \n");
        
        if (exitoSQL) sb.append("   (✔ Guardado en BD y Stock)    \n");
        else          sb.append("   (⚠ Error al guardar en BD)    \n");
        sb.append("   (📄 PDF Generado)             \n");

        JTextArea areaRecibo = new JTextArea(sb.toString());
        areaRecibo.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaRecibo.setEditable(false);
        JOptionPane.showMessageDialog(ventana, new JScrollPane(areaRecibo), "Ticket", JOptionPane.PLAIN_MESSAGE);

        // 4. Recargar productos (para actualizar el stock en pantalla)
        cargarProductosDesdeBD();

        // 5. Reiniciar
        carrito.vaciar();
        nombreUsuario = "";
        mostrarCampoNombre(ventana);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } catch (Exception e) {}
        
        cargarProductosDesdeBD();

        JFrame ventana = new JFrame("Catys App");
        ventana.setSize(1150, 750); 
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setLocationRelativeTo(null);
        mostrarCampoNombre(ventana);
        ventana.setVisible(true);
    }
}