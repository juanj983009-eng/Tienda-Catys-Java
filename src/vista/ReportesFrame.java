package vista;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultPieDataset;

import dto.ResumenVentasDTO;
import dto.VentaDTO;
import repository.ProductoRepository;
import repository.VentaRepository;
import repository.exception.DataAccessException;
import service.VentaService;
import util.AppLogger;

/**
 * Panel de reportes y estadísticas de ventas (v4.0 — Single Window).
 *
 * CAMBIOS vs versión anterior:
 * - Extiende JPanel en lugar de JFrame
 * - recargarDatos() es public — AppShell lo llama al navegar a "REPORTES"
 * - Botón "Volver al Menú" eliminado — el Sidebar maneja la navegación
 * - Paleta adherida a MainWindow (FlatDarkLaf dark)
 * - Constructor deprecado para compatibilidad de compilación
 */
public class ReportesFrame extends JPanel {

    // Colores desde la fuente maestra
    private static final Color C_FONDO = MainWindow.C_FONDO;
    private static final Color C_SUPERF = MainWindow.C_SUPERFICIE;
    private static final Color C_PRIMARIO = MainWindow.C_PRIMARIO;
    private static final Color C_TEXTO = MainWindow.C_TEXTO;
    private static final Color C_TEXTO_S = MainWindow.C_TEXTO_S;
    private static final Color C_BORDE = MainWindow.C_BORDE;
    // Tabla
    private static final Color C_FILA_PAR = MainWindow.C_SUPERFICIE;
    private static final Color C_FILA_IMP = new Color(32, 32, 44);
    private static final Color C_SELECCION = new Color(80, 40, 20);

    private final VentaService ventaService;

    // Referencias para actualizar con datos reales
    private JPanel panelIzquierdo;
    private JPanel panelDerecho;
    private JLabel lblTotalDinero;
    private JLabel lblTotalTx;

    public ReportesFrame(VentaService ventaService) {
        this.ventaService = ventaService;
        construirEsqueleto();
        // No cargamos datos aquí — AppShell llama recargarDatos() al navegar
    }

    /** @deprecated Solo para compatibilidad de compilación con código legado. */
    @Deprecated
    public ReportesFrame() {
        this(new VentaService(new VentaRepository(), new ProductoRepository()));
    }

    /**
     * Recarga los KPIs, gráfico y tabla desde la BD.
     * AppShell lo llama cada vez que el usuario navega a "REPORTES".
     */
    public void recargarDatos() {
        // Resetear KPIs a "cargando..."
        lblTotalDinero.setText("Cargando...");
        lblTotalTx.setText("Cargando...");
        cargarEnBackground();
    }

    // ==========================================
    // CONSTRUCCIÓN DE LA UI (esqueleto)
    // ==========================================

    private void construirEsqueleto() {
        setLayout(new BorderLayout());
        setBackground(C_FONDO);

        // --- Panel izquierdo: KPIs + gráfico ---
        panelIzquierdo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, C_PRIMARIO, 0, getHeight(), new Color(180, 60, 20)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panelIzquierdo.setPreferredSize(new Dimension(360, 0));
        panelIzquierdo.setLayout(new GridBagLayout());
        panelIzquierdo.setOpaque(false);

        JPanel leftWrap = new JPanel(new BorderLayout());
        leftWrap.setBackground(C_PRIMARIO);
        leftWrap.add(panelIzquierdo, BorderLayout.CENTER);

        JLabel lblTitle = new JLabel(
                "<html><div style='text-align:center;'>Resumen<br>de Negocio</div></html>",
                SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(Color.WHITE);

        lblTotalDinero = mkKpiValor("Cargando...");
        lblTotalTx = mkKpiValor("Cargando...");

        JPanel cardDinero = mkTarjetaKPI("Ingresos Totales", lblTotalDinero);
        JPanel cardTx = mkTarjetaKPI("Transacciones", lblTotalTx);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 14, 0);
        panelIzquierdo.add(lblTitle, gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        panelIzquierdo.add(cardDinero, gbc);
        gbc.gridy++;
        panelIzquierdo.add(cardTx, gbc);

        // --- Panel derecho: tabla ---
        panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.setBackground(C_FONDO);
        panelDerecho.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel lblTabla = new JLabel("Historial de Movimientos");
        lblTabla.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTabla.setForeground(C_TEXTO);
        lblTabla.setBorder(new EmptyBorder(0, 0, 18, 0));

        JTable tabla = new JTable(new DefaultTableModel(
                new String[] { "ID", "Fecha", "Cliente", "Pago", "Total", "Detalle" }, 0));
        tabla.setName("tablaHistorial");
        estilizarTabla(tabla);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(C_SUPERF);
        scroll.setBorder(new LineBorder(C_BORDE, 1));

        panelDerecho.add(lblTabla, BorderLayout.NORTH);
        panelDerecho.add(scroll, BorderLayout.CENTER);

        add(leftWrap, BorderLayout.WEST);
        add(panelDerecho, BorderLayout.CENTER);
    }

    // ==========================================
    // CARGA DE DATOS (SwingWorker)
    // ==========================================

    private void cargarEnBackground() {
        new SwingWorker<DatosReporte, Void>() {
            @Override
            protected DatosReporte doInBackground() throws DataAccessException {
                ResumenVentasDTO resumen = ventaService.obtenerResumen();
                List<VentaDTO> historial = ventaService.obtenerHistorial();
                return new DatosReporte(resumen, historial);
            }

            @Override
            protected void done() {
                try {
                    DatosReporte datos = get();
                    actualizarKPIs(datos.resumen());
                    actualizarGrafico(datos.resumen().ventasPorMetodo());
                    actualizarTabla(datos.historial());
                } catch (Exception ex) {
                    AppLogger.error("Error al cargar datos del reporte", ex);
                    JOptionPane.showMessageDialog(ReportesFrame.this,
                            "❌ No se pudieron cargar los datos del reporte.\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void actualizarKPIs(ResumenVentasDTO r) {
        lblTotalDinero.setText(r.getTotalIngresosFormateado());
        lblTotalTx.setText(r.totalTransacciones() + " Pedidos");
    }

    private void actualizarGrafico(Map<String, Double> datos) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(10, 10, 10, 10);
        panelIzquierdo.add(crearGrafico(datos), gbc);
        panelIzquierdo.revalidate();
        panelIzquierdo.repaint();
    }

    private void actualizarTabla(List<VentaDTO> historial) {
        DefaultTableModel modelo = new DefaultTableModel(
                new String[] { "ID", "Fecha", "Cliente", "Pago", "Total", "Detalle" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (VentaDTO v : historial) {
            modelo.addRow(new Object[] {
                    v.id(),
                    v.fecha() != null ? v.fecha().format(fmt) : "-",
                    v.clienteNombre(),
                    v.metodoPago().getDisplayName(),
                    String.format("S/ %.2f", v.total()),
                    v.detalle()
            });
        }

        JScrollPane scroll = (JScrollPane) ((BorderLayout) panelDerecho.getLayout())
                .getLayoutComponent(BorderLayout.CENTER);
        JTable tabla = (JTable) scroll.getViewport().getView();
        tabla.setModel(modelo);
        estilizarTabla(tabla);
    }

    // ==========================================
    // COMPONENTES UI
    // ==========================================

    private JPanel crearGrafico(Map<String, Double> datos) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        datos.forEach(dataset::setValue);

        JFreeChart chart = ChartFactory.createRingChart("Ventas por Método", dataset, true, true, false);
        chart.setBackgroundPaint(null);

        TextTitle title = chart.getTitle();
        title.setPaint(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));

        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
            chart.getLegend().setBackgroundPaint(new Color(255, 255, 255, 30));
        }

        RingPlot plot = (RingPlot) chart.getPlot();
        plot.setBackgroundPaint(null);
        plot.setOutlineVisible(false);
        plot.setSectionDepth(0.35);
        plot.setShadowPaint(null);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}: {2}", new DecimalFormat("0"), new DecimalFormat("0%")));
        plot.setLabelBackgroundPaint(null);
        plot.setLabelOutlinePaint(null);
        plot.setLabelFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        plot.setLabelPaint(Color.WHITE);
        plot.setSimpleLabels(true);
        plot.setSeparatorPaint(C_PRIMARIO);
        plot.setSeparatorStroke(new BasicStroke(2.0f));

        ChartPanel cp = new ChartPanel(chart);
        cp.setOpaque(false);
        cp.setPreferredSize(new Dimension(300, 240));
        return cp;
    }

    private JPanel mkTarjetaKPI(String titulo, JLabel lblValor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(255, 255, 255, 30));
        card.setBorder(new LineBorder(new Color(255, 255, 255, 80), 1, true));
        card.setPreferredSize(new Dimension(260, 88));

        JLabel lblTit = new JLabel(titulo, SwingConstants.CENTER);
        lblTit.setForeground(new Color(245, 245, 245));
        lblTit.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTit.setBorder(new EmptyBorder(8, 0, 0, 0));

        card.add(lblTit, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);
        return card;
    }

    private JLabel mkKpiValor(String texto) {
        JLabel l = new JLabel(texto, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 26));
        l.setForeground(Color.WHITE);
        return l;
    }

    private void estilizarTabla(JTable t) {
        t.setRowHeight(40);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setShowVerticalLines(false);
        t.setGridColor(C_BORDE);
        t.setBackground(C_SUPERF);
        t.setForeground(C_TEXTO);
        t.setSelectionBackground(C_SELECCION);
        t.setSelectionForeground(Color.WHITE);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                c.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? C_FILA_PAR : C_FILA_IMP);
                    c.setForeground(C_TEXTO);
                }
                setHorizontalAlignment(col != 5 ? JLabel.CENTER : JLabel.LEFT);
                return c;
            }
        };
        for (int i = 0; i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setCellRenderer(renderer);

        JTableHeader h = t.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 14));
        h.setBackground(C_PRIMARIO);
        h.setForeground(Color.WHITE);
        h.setOpaque(true);
        h.setPreferredSize(new Dimension(0, 44));
        ((DefaultTableCellRenderer) h.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
    }

    private record DatosReporte(ResumenVentasDTO resumen, List<VentaDTO> historial) {
    }
}