package vista;

import dto.VentaDTO;
import repository.ProductoRepository;
import repository.VentaRepository;
import repository.exception.DataAccessException;
import service.VentaService;
import util.AppLogger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * KDS (Kitchen Display System) — Monitor de pedidos para la cocina.
 *
 * CAMBIOS vs versión anterior:
 * - Usa VentaService en lugar de VentaDAO directamente
 * - cargarPedidos() usa SwingWorker — el monitor no se congela al actualizar
 * - Recibe List<VentaDTO> del servicio — construye el modelo de cocina en la UI
 * - BotonNeon como clase de componente reutilizable (se mantiene local porque
 *   es específica del estilo KDS oscuro, no del sistema general)
 */
public class CocinaFrame extends JFrame {

    private JTable tabla;
    private JLabel lblContador;

    private final VentaService ventaService;
    private JButton btnRefrescar;

    // --- Colores KDS (modo oscuro) ---
    private static final Color COLOR_FONDO          = new Color(30, 30, 30);
    private static final Color COLOR_HEADER         = new Color(0, 0, 0);
    private static final Color COLOR_TABLA_BG       = new Color(45, 45, 45);
    private static final Color COLOR_TEXTO_BLANCO   = new Color(240, 240, 240);
    private static final Color COLOR_ACENTO_VERDE   = new Color(0, 200, 83);
    private static final Color COLOR_ACENTO_NARANJA = new Color(255, 110, 64);
    private static final Color COLOR_SELECCION      = new Color(70, 70, 70);

    public CocinaFrame(VentaService ventaService) {
        this.ventaService = ventaService;
        construirUI();
        cargarPedidosEnBackground(); // Carga inicial asíncrona
    }

    /** Constructor de conveniencia */
    public CocinaFrame() {
        this(new VentaService(new VentaRepository(), new ProductoRepository()));
    }

    private void construirUI() {
        setTitle("KDS - Monitor de Cocina");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_FONDO);

        // --- Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_HEADER);
        header.setPreferredSize(new Dimension(1200, 80));
        header.setBorder(new EmptyBorder(0, 30, 0, 30));

        JLabel lblTitulo = new JLabel("👨‍🍳 COCINA | PEDIDOS EN COLA");
        lblTitulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 26));
        lblTitulo.setForeground(COLOR_TEXTO_BLANCO);

        lblContador = new JLabel("Cargando...");
        lblContador.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblContador.setForeground(COLOR_ACENTO_NARANJA);

        header.add(lblTitulo,   BorderLayout.WEST);
        header.add(lblContador, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // --- Panel central (tabla) ---
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBackground(COLOR_FONDO);
        panelCentral.setBorder(new EmptyBorder(20, 20, 20, 20));

        tabla = new JTable();
        tabla.setName("tablaKDS");
        estilizarTablaKDS(tabla);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(COLOR_TABLA_BG);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));

        panelCentral.add(scroll, BorderLayout.CENTER);
        add(panelCentral, BorderLayout.CENTER);

        // --- Footer (botón actualizar) ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        footer.setBackground(COLOR_FONDO);

        btnRefrescar = new BotonNeon("🔄 ACTUALIZAR MONITOR", COLOR_ACENTO_VERDE);
        btnRefrescar.setPreferredSize(new Dimension(300, 55));
        btnRefrescar.addActionListener(e -> cargarPedidosEnBackground());
        footer.add(btnRefrescar);
        add(footer, BorderLayout.SOUTH);
    }

    /**
     * Carga los pedidos en un hilo de fondo.
     * Deshabilita el botón durante la carga para evitar requests duplicados.
     */
    private void cargarPedidosEnBackground() {
        btnRefrescar.setEnabled(false);
        lblContador.setText("Actualizando...");

        new SwingWorker<List<VentaDTO>, Void>() {
            @Override
            protected List<VentaDTO> doInBackground() throws DataAccessException {
                return ventaService.obtenerHistorial();
            }

            @Override
            protected void done() {
                btnRefrescar.setEnabled(true);
                try {
                    List<VentaDTO> ventas = get();
                    actualizarTablaKDS(ventas);
                    lblContador.setText("PEDIDOS: " + ventas.size());
                } catch (Exception ex) {
                    AppLogger.error("Error al cargar pedidos para el KDS", ex);
                    lblContador.setText("❌ Error al cargar");
                    JOptionPane.showMessageDialog(CocinaFrame.this,
                        "No se pudieron cargar los pedidos.\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void actualizarTablaKDS(List<VentaDTO> ventas) {
        DefaultTableModel modeloCocina = new DefaultTableModel(
            new String[]{"TICKET", "HORA", "DETALLE (PLATOS)", "ESTADO"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        DateTimeFormatter fmtHora = DateTimeFormatter.ofPattern("HH:mm");

        for (VentaDTO v : ventas) {
            String hora = (v.fecha() != null) ? v.fecha().format(fmtHora) : "--:--";
            modeloCocina.addRow(new Object[]{
                "#" + v.id(),
                hora,
                v.detalle(),
                "PENDIENTE"
            });
        }

        tabla.setModel(modeloCocina);

        // Ajustar anchos de columnas
        if (tabla.getColumnCount() >= 4) {
            tabla.getColumnModel().getColumn(0).setMaxWidth(100); // Ticket
            tabla.getColumnModel().getColumn(1).setMaxWidth(100); // Hora
            tabla.getColumnModel().getColumn(3).setMaxWidth(150); // Estado
        }
    }

    private void estilizarTablaKDS(JTable tabla) {
        tabla.setRowHeight(60);
        tabla.setBackground(COLOR_TABLA_BG);
        tabla.setForeground(COLOR_TEXTO_BLANCO);
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(new Color(80, 80, 80));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                c.setBackground(isSelected ? COLOR_SELECCION : COLOR_TABLA_BG);
                c.setForeground(COLOR_TEXTO_BLANCO);
                setBorder(new EmptyBorder(0, 0, 0, 0));
                switch (col) {
                    case 0 -> { setFont(new Font("Segoe UI", Font.BOLD, 18)); setHorizontalAlignment(JLabel.CENTER); setForeground(COLOR_ACENTO_NARANJA); }
                    case 1 -> { setFont(new Font("Segoe UI", Font.PLAIN, 18)); setHorizontalAlignment(JLabel.CENTER); setForeground(Color.CYAN); }
                    case 2 -> { setFont(new Font("Consolas", Font.PLAIN, 14)); setHorizontalAlignment(JLabel.LEFT); setBorder(new EmptyBorder(0, 15, 0, 0)); }
                    case 3 -> { setFont(new Font("Segoe UI", Font.BOLD, 14)); setHorizontalAlignment(JLabel.CENTER); setForeground(Color.YELLOW); }
                }
                return c;
            }
        };
        tabla.setDefaultRenderer(Object.class, renderer);

        JTableHeader h = tabla.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 16));
        h.setBackground(new Color(20, 20, 20));
        h.setForeground(Color.GRAY);
        h.setPreferredSize(new Dimension(0, 45));
        h.setBorder(null);
    }

    /** Botón de estilo neón, específico del tema oscuro de cocina */
    private static class BotonNeon extends JButton {
        private final Color colorBase;

        public BotonNeon(String texto, Color color) {
            super(texto);
            this.colorBase = color;
            setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
            setForeground(Color.BLACK);
            setBackground(colorBase);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(colorBase.brighter()); }
                public void mouseExited(MouseEvent e)  { setBackground(colorBase); }
            });
        }

        @Override
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