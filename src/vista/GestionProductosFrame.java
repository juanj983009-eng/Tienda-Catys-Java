package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import modelo.Producto;
import repository.ProductoRepository;
import repository.exception.DataAccessException;
import service.ProductoService;
import service.exception.ValidacionException;
import util.AppLogger;
import vista.components.BotonRedondeado;

/**
 * Panel CRUD de gestión de productos (v4.0 — Single Window).
 *
 * CAMBIOS vs versión anterior:
 * - Extiende JPanel en lugar de JFrame — ya NO es una ventana independiente
 * - El header de sección lo provee AppShell — eliminado aquí
 * - recargar() es public — AppShell lo llama al navegar a "INVENTARIO"
 * - Paleta adherida a MainWindow (FlatDarkLaf)
 * - Filas alternas de color en la tabla (FlatLaf + renderer personalizado)
 * - "FAST FOOD" en el combo de categorías (corregido de "FAST_FOOD")
 * - Constructor deprecado mantenido para compatibilidad de compilación
 */
public class GestionProductosFrame extends JPanel {

    private JTextField txtNombre, txtPrecio, txtStock, txtImagen, txtBuscar;
    private JComboBox<String> cmbCategoria;
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private int idSeleccionado = -1;

    private final ProductoService productoService;

    // --- Paleta desde la fuente maestra ---
    private static final Color C_FONDO    = MainWindow.C_FONDO;
    private static final Color C_SUPERF   = MainWindow.C_SUPERFICIE;
    private static final Color C_SUPERF2  = MainWindow.C_SUPERFICIE2;
    private static final Color C_PRIMARIO = MainWindow.C_PRIMARIO;
    private static final Color C_PRIM_HOV = MainWindow.C_PRIM_HOVER;
    private static final Color C_TEXTO    = MainWindow.C_TEXTO;
    private static final Color C_TEXTO_S  = MainWindow.C_TEXTO_S;
    private static final Color C_BORDE    = MainWindow.C_BORDE;
    private static final Color C_VERDE    = MainWindow.C_VERDE;
    private static final Color C_ROJO     = MainWindow.C_ROJO;
    // Filas alternas de la tabla
    private static final Color C_FILA_PAR  = MainWindow.C_SUPERFICIE;
    private static final Color C_FILA_IMP  = new Color(32, 32, 44);
    private static final Color C_SELECCION = new Color(80, 40, 20);

    public GestionProductosFrame(ProductoService productoService) {
        this.productoService = productoService;
        construirUI();
        recargar();
    }

    /** @deprecated Solo para compatibilidad de compilación con código legado. */
    @Deprecated
    public GestionProductosFrame() {
        this(new ProductoService(new ProductoRepository()));
    }

    /** Recarga la tabla desde la BD. AppShell lo llama al navegar a INVENTARIO. */
    public void recargar() {
        cargarTablaEnBackground(null);
    }

    private void construirUI() {
        setLayout(new BorderLayout());
        setBackground(C_FONDO);

        // --- Panel Izquierdo: Formulario ---
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBackground(C_SUPERF);
        panelForm.setPreferredSize(new Dimension(380, 0));
        panelForm.setBorder(new EmptyBorder(24, 28, 24, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(7, 0, 7, 0);
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel lblSub = new JLabel("Editar Producto");
        lblSub.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSub.setForeground(C_PRIMARIO);
        panelForm.add(lblSub, gbc);

        gbc.gridy++; panelForm.add(mkLblForm("🏷️  Nombre del Plato:"), gbc);
        gbc.gridy++; txtNombre = mkInput("txtNombre"); panelForm.add(txtNombre, gbc);

        gbc.gridy++;
        JPanel fila = new JPanel(new GridLayout(1, 2, 12, 0));
        fila.setBackground(C_SUPERF);
        JPanel pPrecio = panelCampo("💵  Precio (S/):", txtPrecio = mkInput("txtPrecio"));
        JPanel pStock  = panelCampo("📦  Stock:",       txtStock  = mkInput("txtStock"));
        fila.add(pPrecio); fila.add(pStock);
        panelForm.add(fila, gbc);

        gbc.gridy++; panelForm.add(mkLblForm("📂  Categoría:"), gbc);
        gbc.gridy++;
        // ✓ Corregido: "FAST FOOD" con espacio (igual que en la BD)
        cmbCategoria = new JComboBox<>(new String[]{"CRIOLLO", "CHIFA", "FAST FOOD", "BEBIDAS"});
        cmbCategoria.setName("cmbCategoria");
        cmbCategoria.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbCategoria.setBackground(C_SUPERF2);
        cmbCategoria.setForeground(C_TEXTO);
        panelForm.add(cmbCategoria, gbc);

        gbc.gridy++; panelForm.add(mkLblForm("🖼️  Nombre de imagen:"), gbc);
        gbc.gridy++;
        txtImagen = mkInput("txtImagen");
        txtImagen.setText("imagen.jpg");
        panelForm.add(txtImagen, gbc);

        gbc.gridy++; gbc.insets = new Insets(22, 0, 8, 0);
        BotonRedondeado btnGuardar = new BotonRedondeado("💾  GUARDAR / ACTUALIZAR",
            C_VERDE, new Color(55, 160, 100), Color.WHITE);
        btnGuardar.setName("btnGuardar");
        panelForm.add(btnGuardar, gbc);

        gbc.gridy++; gbc.insets = new Insets(4, 0, 8, 0);
        BotonRedondeado btnEliminar = new BotonRedondeado("🗑  ELIMINAR SELECCIÓN",
            new Color(120, 30, 30), new Color(180, 50, 50), Color.WHITE);
        btnEliminar.setName("btnEliminar");
        panelForm.add(btnEliminar, gbc);

        gbc.gridy++; gbc.insets = new Insets(4, 0, 0, 0);
        JButton btnLimpiar = new JButton("Limpiar formulario");
        btnLimpiar.setBorderPainted(false);
        btnLimpiar.setContentAreaFilled(false);
        btnLimpiar.setForeground(C_TEXTO_S);
        btnLimpiar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimpiar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panelForm.add(btnLimpiar, gbc);

        add(panelForm, BorderLayout.WEST);

        // --- Panel Derecho: Tabla + Buscador ---
        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBackground(C_FONDO);
        panelTabla.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Barra de búsqueda
        JPanel panelBuscar = new JPanel(new BorderLayout());
        panelBuscar.setBackground(C_FONDO);
        panelBuscar.setBorder(new EmptyBorder(0, 0, 16, 0));
        JLabel lblBuscar = new JLabel("🔍  Buscar:  ");
        lblBuscar.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        lblBuscar.setForeground(C_TEXTO);
        txtBuscar = new JTextField();
        txtBuscar.setName("txtBuscar");
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtBuscar.setBackground(C_SUPERF2);
        txtBuscar.setForeground(C_TEXTO);
        txtBuscar.setCaretColor(C_TEXTO);
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDE, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrarEnBackground(); }
            public void removeUpdate(DocumentEvent e) { filtrarEnBackground(); }
            public void changedUpdate(DocumentEvent e) { filtrarEnBackground(); }
        });
        panelBuscar.add(lblBuscar, BorderLayout.WEST);
        panelBuscar.add(txtBuscar, BorderLayout.CENTER);
        panelTabla.add(panelBuscar, BorderLayout.NORTH);

        // Tabla
        modeloTabla = crearModelo();
        tabla = new JTable(modeloTabla);
        tabla.setName("tablaProductos");
        estilizarTabla(tabla);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(C_SUPERF);
        scroll.setBorder(new LineBorder(C_BORDE, 1));
        panelTabla.add(scroll, BorderLayout.CENTER);
        add(panelTabla, BorderLayout.CENTER);

        // --- Eventos ---
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int f = tabla.getSelectedRow();
                if (f < 0) return;
                idSeleccionado = (int) modeloTabla.getValueAt(f, 0);
                txtNombre.setText(modeloTabla.getValueAt(f, 1).toString());
                txtPrecio.setText(modeloTabla.getValueAt(f, 2).toString());
                txtStock.setText(modeloTabla.getValueAt(f, 3).toString());
                cmbCategoria.setSelectedItem(modeloTabla.getValueAt(f, 4).toString());
                txtImagen.setText(modeloTabla.getValueAt(f, 5).toString());
            }
        });

        btnGuardar.addActionListener(e  -> accionGuardar(btnGuardar));
        btnEliminar.addActionListener(e -> accionEliminar(btnEliminar));
        btnLimpiar.addActionListener(e  -> limpiarFormulario());
    }

    // ==========================================
    //  ACCIONES CON SWINGWORKER
    // ==========================================

    private void accionGuardar(JButton btn) {
        Producto p;
        try {
            p = leerFormulario();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "⚠ Precio y Stock deben ser valores numéricos.", "Error de formato",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        btn.setEnabled(false);
        final Producto pf = p;
        final boolean esNuevo = (idSeleccionado == -1);

        new SwingWorker<Void, Void>() {
            private String ok, err;
            @Override protected Void doInBackground() {
                try {
                    if (esNuevo) { productoService.registrar(pf);  ok = "✅ Producto creado correctamente."; }
                    else         { productoService.actualizar(pf); ok = "✅ Producto actualizado correctamente."; }
                } catch (ValidacionException e) {
                    err = "⚠ Error en campo '" + e.getCampo() + "': " + e.getMessage();
                } catch (DataAccessException e) {
                    AppLogger.error("Error de BD al guardar producto", e);
                    err = "❌ Error al guardar en la base de datos.";
                }
                return null;
            }
            @Override protected void done() {
                btn.setEnabled(true);
                if (ok != null) {
                    JOptionPane.showMessageDialog(GestionProductosFrame.this, ok, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiarFormulario();
                    cargarTablaEnBackground(null);
                } else {
                    JOptionPane.showMessageDialog(GestionProductosFrame.this, err, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void accionEliminar(JButton btn) {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la tabla primero.",
                "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int conf = JOptionPane.showConfirmDialog(this,
            "¿Eliminar el producto seleccionado?\nEsta acción no se puede deshacer.",
            "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf != JOptionPane.YES_OPTION) return;

        btn.setEnabled(false);
        final int id = idSeleccionado;

        new SwingWorker<Void, Void>() {
            private String err;
            @Override protected Void doInBackground() {
                try { productoService.eliminar(id); }
                catch (DataAccessException e) {
                    AppLogger.error("Error al eliminar producto ID " + id, e);
                    err = "❌ Error al eliminar: " + e.getMessage();
                }
                return null;
            }
            @Override protected void done() {
                btn.setEnabled(true);
                if (err == null) {
                    JOptionPane.showMessageDialog(GestionProductosFrame.this,
                        "✅ Producto eliminado.", "Eliminado", JOptionPane.INFORMATION_MESSAGE);
                    limpiarFormulario();
                    cargarTablaEnBackground(null);
                } else {
                    JOptionPane.showMessageDialog(GestionProductosFrame.this, err, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void filtrarEnBackground() {
        String t = txtBuscar.getText().trim();
        cargarTablaEnBackground(t.isEmpty() ? null : t);
    }

    private void cargarTablaEnBackground(String texto) {
        new SwingWorker<List<Producto>, Void>() {
            @Override protected List<Producto> doInBackground() throws DataAccessException {
                return (texto == null) ? productoService.getTodos() : productoService.buscar(texto);
            }
            @Override protected void done() {
                try { actualizarTabla(get()); }
                catch (Exception ex) { AppLogger.error("Error al cargar productos en la tabla", ex); }
            }
        }.execute();
    }

    private void actualizarTabla(List<Producto> lista) {
        modeloTabla.setRowCount(0);
        for (Producto p : lista) {
            modeloTabla.addRow(new Object[]{
                p.getId(), p.getNombre(), p.getPrecio(),
                p.getStock(), p.getCategoria(), p.getImagen()
            });
        }
    }

    // ==========================================
    //  HELPERS
    // ==========================================

    private Producto leerFormulario() {
        return new Producto(
            idSeleccionado,
            txtNombre.getText().trim(),
            Double.parseDouble(txtPrecio.getText().trim()),
            txtImagen.getText().trim(),
            cmbCategoria.getSelectedItem().toString(),
            Integer.parseInt(txtStock.getText().trim())
        );
    }

    private void limpiarFormulario() {
        idSeleccionado = -1;
        txtNombre.setText(""); txtPrecio.setText(""); txtStock.setText("");
        txtImagen.setText("imagen.jpg");
        tabla.clearSelection();
    }

    private DefaultTableModel crearModelo() {
        return new DefaultTableModel(
            new String[]{"ID", "Nombre", "Precio", "Stock", "Categoría", "Imagen"}, 0
        ) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    }

    private JPanel panelCampo(String label, JTextField campo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_SUPERF);
        p.add(mkLblForm(label), BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        return p;
    }

    private JTextField mkInput(String name) {
        JTextField t = new JTextField();
        t.setName(name);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setBackground(C_SUPERF2);
        t.setForeground(C_TEXTO);
        t.setCaretColor(C_TEXTO);
        t.setPreferredSize(new Dimension(100, 40));
        t.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDE, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        return t;
    }

    private JLabel mkLblForm(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        l.setForeground(C_TEXTO_S);
        return l;
    }

    /**
     * Estiliza la tabla con filas alternas de color y encabezado naranja.
     * Compatible con FlatDarkLaf — no sobreescribe los colores de selección del L&F.
     */
    private void estilizarTabla(JTable t) {
        t.setRowHeight(38);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setShowVerticalLines(false);
        t.setGridColor(C_BORDE);
        t.setBackground(C_SUPERF);
        t.setForeground(C_TEXTO);
        t.setSelectionBackground(C_SELECCION);
        t.setSelectionForeground(Color.WHITE);

        // Renderer con filas alternas
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? C_FILA_PAR : C_FILA_IMP);
                    c.setForeground(C_TEXTO);
                }
                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        };
        for (int i = 0; i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setCellRenderer(renderer);

        // Encabezado
        JTableHeader h = t.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 14));
        h.setBackground(C_PRIMARIO);
        h.setForeground(Color.WHITE);
        h.setPreferredSize(new Dimension(0, 42));
        h.setOpaque(true);
        ((DefaultTableCellRenderer) h.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
    }
}