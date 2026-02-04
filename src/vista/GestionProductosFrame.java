package vista;

import dao.ProductoDAO;
import modelo.Producto;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class GestionProductosFrame extends JFrame {

    private JTextField txtNombre, txtPrecio, txtStock, txtImagen, txtBuscar; // txtBuscar NUEVO
    private JComboBox<String> cmbCategoria;
    private JTable tabla;
    private ProductoDAO dao = new ProductoDAO();
    private int idSeleccionado = -1;

    private static final Color COLOR_SIDEBAR = new Color(255, 255, 255); 
    private static final Color COLOR_FONDO_TABLA = new Color(240, 242, 245);
    private static final Color COLOR_NARANJA = new Color(255, 99, 71);
    private static final Color COLOR_VERDE = new Color(72, 187, 120);
    private static final Color COLOR_ROJO = new Color(220, 53, 69);
    private static final Color COLOR_TEXTO = new Color(51, 51, 51);
    private static final Color COLOR_HEADER_TABLA = new Color(230, 74, 25);

    public GestionProductosFrame() {
        setTitle("Administrador de Menú - Catys");
        setSize(1250, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. HEADER
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 20));
        header.setBackground(new Color(33, 43, 54));
        JLabel lblTitulo = new JLabel("🍱 GESTIÓN DE INVENTARIO Y MENÚ");
        lblTitulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        header.add(lblTitulo);
        add(header, BorderLayout.NORTH);

        // 2. PANEL IZQUIERDO (FORMULARIO)
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBackground(COLOR_SIDEBAR);
        panelForm.setPreferredSize(new Dimension(400, 0));
        panelForm.setBorder(new EmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0); 
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel lblSub = new JLabel("Editar Producto");
        lblSub.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSub.setForeground(COLOR_NARANJA);
        panelForm.add(lblSub, gbc);

        gbc.gridy++; panelForm.add(crearLabel("🏷️ Nombre del Plato:"), gbc);
        gbc.gridy++; txtNombre = crearInput(); panelForm.add(txtNombre, gbc);

        gbc.gridy++;
        JPanel panelFila = new JPanel(new GridLayout(1, 2, 15, 0));
        panelFila.setBackground(COLOR_SIDEBAR);
        
        JPanel pPrecio = new JPanel(new BorderLayout()); pPrecio.setBackground(COLOR_SIDEBAR);
        pPrecio.add(crearLabel("💵 Precio (S/):"), BorderLayout.NORTH);
        txtPrecio = crearInput(); pPrecio.add(txtPrecio, BorderLayout.CENTER);

        JPanel pStock = new JPanel(new BorderLayout()); pStock.setBackground(COLOR_SIDEBAR);
        pStock.add(crearLabel("📦 Stock:"), BorderLayout.NORTH);
        txtStock = crearInput(); pStock.add(txtStock, BorderLayout.CENTER);

        panelFila.add(pPrecio); panelFila.add(pStock);
        panelForm.add(panelFila, gbc);

        gbc.gridy++; panelForm.add(crearLabel("📂 Categoría:"), gbc);
        gbc.gridy++;
        cmbCategoria = new JComboBox<>(new String[]{"CRIOLLO", "CHIFA", "FAST_FOOD", "BEBIDAS"});
        estilizarCombo(cmbCategoria);
        panelForm.add(cmbCategoria, gbc);

        gbc.gridy++; panelForm.add(crearLabel("🖼️ Nombre Imagen:"), gbc);
        gbc.gridy++; txtImagen = crearInput(); txtImagen.setText("imagenes/");
        panelForm.add(txtImagen, gbc);

        gbc.gridy++; gbc.insets = new Insets(25, 0, 10, 0);
        BotonRedondeado btnGuardar = new BotonRedondeado("GUARDAR / ACTUALIZAR", COLOR_VERDE, new Color(60, 160, 100), Color.WHITE);
        panelForm.add(btnGuardar, gbc);

        gbc.gridy++; gbc.insets = new Insets(5, 0, 10, 0);
        BotonRedondeado btnEliminar = new BotonRedondeado("ELIMINAR SELECCIÓN", COLOR_ROJO, new Color(200, 50, 50), Color.WHITE);
        panelForm.add(btnEliminar, gbc);

        gbc.gridy++;
        JButton btnLimpiar = new JButton("Limpiar formulario");
        btnLimpiar.setBorderPainted(false); btnLimpiar.setContentAreaFilled(false);
        btnLimpiar.setForeground(Color.GRAY); btnLimpiar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelForm.add(btnLimpiar, gbc);

        add(panelForm, BorderLayout.WEST);

        // 3. PANEL DERECHO (TABLA + BUSCADOR)
        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBackground(COLOR_FONDO_TABLA);
        panelTabla.setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- BARRA DE BÚSQUEDA NUEVA ---
        JPanel panelBuscar = new JPanel(new BorderLayout());
        panelBuscar.setBackground(COLOR_FONDO_TABLA);
        panelBuscar.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JLabel lblBuscar = new JLabel("🔍 Buscar Plato:  ");
        lblBuscar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        txtBuscar = new JTextField();
        txtBuscar.setPreferredSize(new Dimension(200, 35));
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.GRAY, 1, true), new EmptyBorder(5, 10, 5, 10)
        ));
        
        // Listener para buscar mientras escribes
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        panelBuscar.add(lblBuscar, BorderLayout.WEST);
        panelBuscar.add(txtBuscar, BorderLayout.CENTER);
        
        panelTabla.add(panelBuscar, BorderLayout.NORTH);
        // -------------------------------

        tabla = new JTable();
        cargarTabla();
        estilizarTabla(tabla);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        panelTabla.add(scroll, BorderLayout.CENTER);

        add(panelTabla, BorderLayout.CENTER);

        // --- LÓGICA ---
        tabla.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int f = tabla.getSelectedRow();
                idSeleccionado = (int) tabla.getValueAt(f, 0);
                txtNombre.setText(tabla.getValueAt(f, 1).toString());
                txtPrecio.setText(tabla.getValueAt(f, 2).toString().replace("S/ ", ""));
                txtStock.setText(tabla.getValueAt(f, 3).toString());
                cmbCategoria.setSelectedItem(tabla.getValueAt(f, 4).toString());
                txtImagen.setText(tabla.getValueAt(f, 5).toString());
            }
        });

        btnGuardar.addActionListener(e -> {
            try {
                Producto p = new Producto(idSeleccionado, txtNombre.getText(), 
                    Double.parseDouble(txtPrecio.getText()), txtImagen.getText(), 
                    cmbCategoria.getSelectedItem().toString(), Integer.parseInt(txtStock.getText()));
                
                if(idSeleccionado == -1) {
                    if(dao.registrar(p)) JOptionPane.showMessageDialog(this, "¡Creado!");
                } else {
                    if(dao.actualizar(p)) JOptionPane.showMessageDialog(this, "¡Actualizado!");
                }
                limpiar(); cargarTabla();
            } catch(Exception ex) { JOptionPane.showMessageDialog(this, "Verifique los datos."); }
        });

        btnEliminar.addActionListener(e -> {
            if(idSeleccionado != -1 && JOptionPane.showConfirmDialog(this, "¿Borrar?", "Confirma", JOptionPane.YES_NO_OPTION) == 0) {
                dao.eliminar(idSeleccionado); limpiar(); cargarTabla();
            }
        });

        btnLimpiar.addActionListener(e -> limpiar());
    }

    private void filtrar() {
        String texto = txtBuscar.getText();
        tabla.setModel(dao.filtrar(texto));
    }

    private void estilizarCombo(JComboBox box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14)); box.setBackground(Color.WHITE);
        box.setPreferredSize(new Dimension(100, 40)); ((JComponent) box.getRenderer()).setBorder(new EmptyBorder(0, 5, 0, 0));
    }
    private JTextField crearInput() {
        JTextField t = new JTextField(); t.setPreferredSize(new Dimension(100, 40)); t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 200, 200), 1, true), new EmptyBorder(5, 10, 5, 10))); return t;
    }
    private JLabel crearLabel(String t) { JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12)); l.setForeground(new Color(100, 100, 100)); return l; }
    
    private void estilizarTabla(JTable t) {
        t.setRowHeight(35); t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setShowVerticalLines(false); t.setGridColor(new Color(230, 230, 230));
        JTableHeader h = t.getTableHeader(); h.setFont(new Font("Segoe UI", Font.BOLD, 14));
        h.setBackground(COLOR_HEADER_TABLA); h.setForeground(Color.WHITE); h.setPreferredSize(new Dimension(0, 40));
        DefaultTableCellRenderer center = new DefaultTableCellRenderer(); center.setHorizontalAlignment(JLabel.CENTER);
        t.setDefaultRenderer(Object.class, center);
    }
    private void cargarTabla() { tabla.setModel(dao.listarTodos()); }
    private void limpiar() { idSeleccionado = -1; txtNombre.setText(""); txtPrecio.setText(""); txtStock.setText(""); txtImagen.setText("imagenes/"); tabla.clearSelection(); }

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
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground()); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10));
            super.paintComponent(g); g2.dispose();
        }
    }
}