package vista;

import dao.ClienteDAO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class ListaClientesFrame extends JFrame {

    // Colores Corporativos
    private static final Color COLOR_FONDO = new Color(245, 247, 250);
    private static final Color COLOR_HEADER = new Color(33, 43, 54); 
    private static final Color COLOR_NARANJA = new Color(255, 99, 71);
    private static final Color COLOR_VERDE = new Color(72, 187, 120);

    public ListaClientesFrame() {
        setTitle("Directorio de Clientes VIP - Catys");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. HEADER
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 20));
        header.setBackground(COLOR_HEADER);
        
        JLabel lblTitulo = new JLabel("👥 CARTERA DE CLIENTES");
        // CORRECCIÓN 1: Forzar fuente Emoji en el título
        lblTitulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24)); 
        lblTitulo.setForeground(Color.WHITE);
        
        header.add(lblTitulo);
        add(header, BorderLayout.NORTH);

        // 2. TABLA
        ClienteDAO dao = new ClienteDAO();
        DefaultTableModel modelo = dao.listarClientes();
        
        JTable tabla = new JTable(modelo);
        estilizarTabla(tabla);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        add(scroll, BorderLayout.CENTER);

        // 3. BARRA INFERIOR (BOTONES)
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        footer.setBackground(Color.WHITE);

        // Botón Nuevo
        BotonRedondeado btnNuevo = new BotonRedondeado("➕ REGISTRAR NUEVO", COLOR_NARANJA, new Color(230, 80, 50), Color.WHITE);
        btnNuevo.setPreferredSize(new Dimension(220, 45));
        
        // Botón Cerrar
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setPreferredSize(new Dimension(100, 45));
        btnCerrar.setBackground(Color.WHITE);
        btnCerrar.setForeground(Color.GRAY);
        btnCerrar.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        btnCerrar.setFocusPainted(false);
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // ACCIONES
        btnNuevo.addActionListener(e -> {
            this.dispose(); // Cerramos la lista
            new RegistroClienteFrame().setVisible(true); // Abrimos el registro
        });
        
        btnCerrar.addActionListener(e -> dispose());

        footer.add(btnCerrar);
        footer.add(btnNuevo);
        add(footer, BorderLayout.SOUTH);
    }

    private void estilizarTabla(JTable tabla) {
        tabla.setRowHeight(35);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(new Color(230, 230, 230));
        
        JTableHeader header = tabla.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(255, 99, 71)); // Encabezado Naranja
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tabla.setDefaultRenderer(Object.class, centerRenderer);
    }

    // Botón bonito
    static class BotonRedondeado extends JButton {
        private Color cN, cH;
        public BotonRedondeado(String t, Color n, Color h, Color f) {
            super(t); cN=n; cH=h; setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false);
            setForeground(f); 
            // CORRECCIÓN 2: Forzar fuente Emoji en el botón para que se vea el "➕"
            setFont(new Font("Segoe UI Emoji", Font.BOLD, 14)); 
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