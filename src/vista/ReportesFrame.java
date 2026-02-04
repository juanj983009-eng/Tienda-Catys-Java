package vista;

import dao.VentaDAO;
// Importaciones JFreeChart
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.RingPlot; 
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.title.TextTitle; // Importante para el título
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.Map;
import java.text.DecimalFormat;
import java.awt.geom.RoundRectangle2D;

public class ReportesFrame extends JFrame {

    // --- COLORES CORPORATIVOS ---
    private static final Color COLOR_NARANJA_BRAND = new Color(255, 99, 71);   
    private static final Color COLOR_NARANJA_HEADER = new Color(230, 74, 25);  
    private static final Color COLOR_FONDO_CLARO = new Color(255, 248, 245);   
    private static final Color COLOR_TEXTO = new Color(51, 51, 51);
    private static final Color COLOR_SELECCION = new Color(255, 204, 188);     

    public ReportesFrame() {
        setTitle("Reporte de Ventas - Catys Enterprise");
        setSize(1250, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        VentaDAO dao = new VentaDAO();
        DefaultTableModel modeloDatos = dao.obtenerHistorialVentas();
        
        double totalDinero = calcularTotalVentas(modeloDatos);
        int totalTransacciones = modeloDatos.getRowCount();

        // 1. PANEL IZQUIERDO
        JPanel panelIzquierdo = new JPanel();
        panelIzquierdo.setBackground(COLOR_NARANJA_BRAND);
        panelIzquierdo.setPreferredSize(new Dimension(380, 700));
        panelIzquierdo.setLayout(new GridBagLayout());

        JPanel panelGrafico = crearPanelGrafico(dao.obtenerVentasPorMetodo());

        JPanel cardDinero = crearTarjetaKPI("Ingresos Totales", "S/ " + String.format("%.2f", totalDinero));
        JPanel cardVentas = crearTarjetaKPI("Transacciones", totalTransacciones + " Pedidos");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 10, 0);
        
        // CORRECCIÓN EMOJI TÍTULO
        JLabel lblTitulo = new JLabel("<html><div style='text-align: center;'>Resumen<br>de Negocio</div></html>");
        lblTitulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 30)); // Fuente Emoji
        lblTitulo.setForeground(Color.WHITE);
        panelIzquierdo.add(lblTitulo, gbc);
        
        gbc.gridy++; gbc.insets = new Insets(0, 0, 10, 0);
        panelIzquierdo.add(cardDinero, gbc);
        
        gbc.gridy++; 
        panelIzquierdo.add(cardVentas, gbc);

        gbc.gridy++; gbc.fill = GridBagConstraints.BOTH; 
        gbc.weightx = 1.0; gbc.weighty = 1.0; 
        gbc.insets = new Insets(10, 10, 10, 10);
        panelIzquierdo.add(panelGrafico, gbc);

        // 2. PANEL DERECHO
        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.setBackground(Color.WHITE);
        panelDerecho.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel lblTablaTitulo = new JLabel("Detalle de Movimientos");
        lblTablaTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTablaTitulo.setForeground(COLOR_TEXTO);
        lblTablaTitulo.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JTable tabla = new JTable(modeloDatos);
        estilizarTablaProfesional(tabla);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoton.setBackground(Color.WHITE);
        
        BotonRedondeado btnCerrar = new BotonRedondeado("Volver al Menú", new Color(55, 65, 81), new Color(30, 30, 30), Color.WHITE);
        btnCerrar.setPreferredSize(new Dimension(160, 45));
        btnCerrar.addActionListener(e -> dispose());
        panelBoton.add(btnCerrar);

        panelDerecho.add(lblTablaTitulo, BorderLayout.NORTH);
        panelDerecho.add(scroll, BorderLayout.CENTER);
        panelDerecho.add(panelBoton, BorderLayout.SOUTH);

        add(panelIzquierdo, BorderLayout.WEST);
        add(panelDerecho, BorderLayout.CENTER);
    }

    // --- MÉTODO DEL GRÁFICO DE DONA CORREGIDO ---
    private JPanel crearPanelGrafico(Map<String, Double> datos) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<String, Double> entry : datos.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        JFreeChart chart = ChartFactory.createRingChart(
                "Ventas por Método", 
                dataset,
                true, // Leyenda activada
                true,
                false
        );

        chart.setBackgroundPaint(null); 
        
        // 1. FUENTE DEL TÍTULO (Para que soporte emojis)
        TextTitle title = chart.getTitle();
        title.setPaint(Color.WHITE);
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18)); // <--- CLAVE

        // 2. FUENTE DE LA LEYENDA (Los cuadritos de abajo)
        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(new Font("Segoe UI Emoji", Font.PLAIN, 12)); // <--- CLAVE
            chart.getLegend().setBackgroundPaint(new Color(255, 255, 255, 200));
        }

        RingPlot plot = (RingPlot) chart.getPlot();
        plot.setBackgroundPaint(null); 
        plot.setOutlineVisible(false); 
        plot.setSectionDepth(0.35); 
        plot.setShadowPaint(null); 
        
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}", new DecimalFormat("0"), new DecimalFormat("0%")));
        plot.setLabelBackgroundPaint(null); 
        plot.setLabelOutlinePaint(null);
        
        // 3. FUENTE DE LAS ETIQUETAS (El texto flotante)
        plot.setLabelFont(new Font("Segoe UI Emoji", Font.BOLD, 11)); // <--- CLAVE
        plot.setLabelPaint(Color.WHITE); 
        plot.setSimpleLabels(true); 
        plot.setLabelLinkPaint(new Color(255,255,255, 150)); 

        // Colores
        plot.setSectionPaint("Efectivo", new Color(255, 255, 255));
        plot.setSectionPaint("Yape", new Color(179, 157, 219));
        plot.setSectionPaint("Plin", new Color(129, 212, 250));
        plot.setSectionPaint("Tarjeta", new Color(255, 224, 130));
        plot.setSectionPaint("Tarjeta de Crédito", new Color(255, 224, 130));

        plot.setSeparatorPaint(COLOR_NARANJA_BRAND); 
        plot.setSeparatorStroke(new BasicStroke(2.0f));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(false);
        chartPanel.setPreferredSize(new Dimension(300, 250));
        return chartPanel;
    }

    private void estilizarTablaProfesional(JTable tabla) {
        tabla.setRowHeight(40);
        // FUENTE EMOJI EN LA TABLA
        tabla.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14)); 
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(new Color(230, 230, 230));
        
        DefaultTableCellRenderer renderizador = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                // FUENTE EMOJI EN LAS CELDAS
                c.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
                if (isSelected) {
                    c.setBackground(COLOR_SELECCION);
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : COLOR_FONDO_CLARO);
                    c.setForeground(COLOR_TEXTO);
                }
                if (column != 5) setHorizontalAlignment(JLabel.CENTER);
                else setHorizontalAlignment(JLabel.LEFT);
                return c;
            }
        };

        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderizador);
        }

        JTableHeader header = tabla.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(COLOR_NARANJA_HEADER);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setPreferredSize(new Dimension(0, 45));
    }

    private JPanel crearTarjetaKPI(String titulo, String valor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(255, 255, 255, 40));
        card.setBorder(new LineBorder(new Color(255, 255, 255, 100), 1, true));
        card.setPreferredSize(new Dimension(260, 90));
        
        JLabel lblTit = new JLabel(titulo, SwingConstants.CENTER);
        lblTit.setForeground(new Color(245, 245, 245));
        lblTit.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTit.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JLabel lblVal = new JLabel(valor, SwingConstants.CENTER);
        lblVal.setForeground(Color.WHITE);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 28));
        
        card.add(lblTit, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        return card;
    }

    private double calcularTotalVentas(DefaultTableModel modelo) {
        double total = 0;
        for (int i = 0; i < modelo.getRowCount(); i++) {
            try {
                String valor = modelo.getValueAt(i, 4).toString().replace("S/ ", "").replace(",", "");
                total += Double.parseDouble(valor);
            } catch (Exception e) {}
        }
        return total;
    }

    static class BotonRedondeado extends JButton {
        private Color cN, cH;
        public BotonRedondeado(String t, Color n, Color h, Color f) {
            super(t); cN=n; cH=h; setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false);
            setForeground(f); setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR)); setBackground(cN);
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { setBackground(cH); repaint(); }
                public void mouseExited(java.awt.event.MouseEvent e) { setBackground(cN); repaint(); }
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground()); g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            super.paintComponent(g); g2.dispose();
        }
    }
}