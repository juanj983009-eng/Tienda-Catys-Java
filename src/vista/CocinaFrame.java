package vista;

import dao.VentaDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class CocinaFrame extends JFrame {

    private JTable tabla;
    private VentaDAO dao = new VentaDAO();
    private JLabel lblContador;

    // --- COLORES KDS (MODO OSCURO PROFESIONAL) ---
    private static final Color COLOR_FONDO = new Color(30, 30, 30);      // Gris muy oscuro
    private static final Color COLOR_HEADER = new Color(0, 0, 0);        // Negro puro
    private static final Color COLOR_TABLA_BG = new Color(45, 45, 45);   // Gris para filas
    private static final Color COLOR_TEXTO_BLANCO = new Color(240, 240, 240);
    private static final Color COLOR_ACENTO_VERDE = new Color(0, 200, 83); // Verde Neón
    private static final Color COLOR_ACENTO_NARANJA = new Color(255, 110, 64);
    private static final Color COLOR_SELECCION = new Color(70, 70, 70);

    public CocinaFrame() {
        setTitle("KDS - Monitor de Cocina");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_FONDO);

        // 1. HEADER (Barra Superior)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_HEADER);
        header.setPreferredSize(new Dimension(1200, 80));
        header.setBorder(new EmptyBorder(0, 30, 0, 30));

        JLabel lblTitulo = new JLabel("👨‍🍳 COCINA | PEDIDOS EN COLA");
        lblTitulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 26)); // Fuente Emoji para el chef
        lblTitulo.setForeground(COLOR_TEXTO_BLANCO);

        // Contador de pendientes
        lblContador = new JLabel("Cargando...");
        lblContador.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblContador.setForeground(COLOR_ACENTO_NARANJA);

        header.add(lblTitulo, BorderLayout.WEST);
        header.add(lblContador, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // 2. PANEL CENTRAL (TABLA)
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBackground(COLOR_FONDO);
        panelCentral.setBorder(new EmptyBorder(20, 20, 20, 20));

        tabla = new JTable();
        estilizarTablaKDS(tabla);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(COLOR_TABLA_BG); // Fondo de la tabla oscuro
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
        
        panelCentral.add(scroll, BorderLayout.CENTER);
        add(panelCentral, BorderLayout.CENTER);

        // 3. FOOTER (Botón Actualizar)
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        footer.setBackground(COLOR_FONDO);

        // Botón con fuente EMOJI para arreglar el cuadrado
        BotonNeon btnRefrescar = new BotonNeon("🔄 ACTUALIZAR MONITOR", COLOR_ACENTO_VERDE);
        btnRefrescar.setPreferredSize(new Dimension(300, 55));
        
        btnRefrescar.addActionListener(e -> cargarPedidos());

        footer.add(btnRefrescar);
        add(footer, BorderLayout.SOUTH);

        cargarPedidos();
    }

    private void cargarPedidos() {
        DefaultTableModel modeloCompleto = dao.obtenerHistorialVentas();
        
        // Creamos modelo específico para cocina
        DefaultTableModel modeloCocina = new DefaultTableModel();
        modeloCocina.addColumn("TICKET");
        modeloCocina.addColumn("HORA");
        modeloCocina.addColumn("DETALLE (PLATOS)");
        modeloCocina.addColumn("ESTADO"); // Nueva columna visual

        for (int i = 0; i < modeloCompleto.getRowCount(); i++) {
            Object id = modeloCompleto.getValueAt(i, 0);
            
            // Extraer hora limpia
            String fecha = modeloCompleto.getValueAt(i, 1).toString();
            String hora = fecha.length() > 10 ? fecha.substring(11, 16) : fecha; // Solo HH:mm
            
            Object detalle = modeloCompleto.getValueAt(i, 5);

            // Agregamos estado "PENDIENTE" (Simulado)
            modeloCocina.addRow(new Object[]{ "#" + id, hora, detalle, "PENDIENTE" });
        }

        tabla.setModel(modeloCocina);
        
        // Ajustar anchos
        tabla.getColumnModel().getColumn(0).setMaxWidth(100); // Ticket
        tabla.getColumnModel().getColumn(1).setMaxWidth(100); // Hora
        tabla.getColumnModel().getColumn(3).setMaxWidth(150); // Estado
        
        lblContador.setText("PENDIENTES: " + modeloCocina.getRowCount());
    }

    private void estilizarTablaKDS(JTable tabla) {
        tabla.setRowHeight(60); // Filas muy altas para leer rápido
        tabla.setBackground(COLOR_TABLA_BG);
        tabla.setForeground(COLOR_TEXTO_BLANCO);
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(new Color(80, 80, 80)); // Líneas sutiles

        // Renderizador de Celdas (Aquí damos el estilo a cada columna)
        DefaultTableCellRenderer renderizador = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Color base
                if (isSelected) {
                    c.setBackground(COLOR_SELECCION);
                } else {
                    c.setBackground(COLOR_TABLA_BG);
                }
                c.setForeground(COLOR_TEXTO_BLANCO);

                // Lógica por columna
                switch (column) {
                    case 0: // TICKET
                        setFont(new Font("Segoe UI", Font.BOLD, 18));
                        setHorizontalAlignment(JLabel.CENTER);
                        setForeground(COLOR_ACENTO_NARANJA); // Naranja para el número
                        break;
                    case 1: // HORA
                        setFont(new Font("Segoe UI", Font.PLAIN, 18));
                        setHorizontalAlignment(JLabel.CENTER);
                        setForeground(Color.CYAN); // Cian para la hora
                        break;
                    case 2: // DETALLE
                        // Usamos fuente monoespaciada para que parezca ticket real
                        setFont(new Font("Consolas", Font.PLAIN, 16)); 
                        setHorizontalAlignment(JLabel.LEFT);
                        setBorder(new EmptyBorder(0, 15, 0, 0));
                        break;
                    case 3: // ESTADO
                        setFont(new Font("Segoe UI", Font.BOLD, 14));
                        setHorizontalAlignment(JLabel.CENTER);
                        setForeground(Color.YELLOW); // Amarillo para "Pendiente"
                        break;
                }
                return c;
            }
        };

        tabla.setDefaultRenderer(Object.class, renderizador);

        // Header
        JTableHeader header = tabla.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setBackground(new Color(20, 20, 20)); // Casi negro
        header.setForeground(Color.GRAY);
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(null);
    }

    // Botón estilo Neón
    static class BotonNeon extends JButton {
        private Color colorBase;
        public BotonNeon(String texto, Color color) {
            super(texto);
            this.colorBase = color;
            // AQUI ESTA EL ARREGLO DEL CUADRADO: Usamos Segoe UI Emoji
            setFont(new Font("Segoe UI Emoji", Font.BOLD, 16)); 
            setForeground(Color.BLACK);
            setBackground(colorBase);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Efecto Hover (Brillo)
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(colorBase.brighter()); }
                public void mouseExited(MouseEvent e) { setBackground(colorBase); }
            });
        }
        
        // Bordes redondeados
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
            super.paintComponent(g);
            g2.dispose();
        }
    }
}