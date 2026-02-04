package vista;

import dao.ClienteDAO;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import modelo.Cliente;

public class ListaClientesFrame extends JFrame {

    private JTable tablaClientes;
    private DefaultTableModel modeloTabla;
    private ClienteDAO clienteDAO;

    public ListaClientesFrame() {
        clienteDAO = new ClienteDAO();
        initComponents();
        cargarDatosTabla();
    }

    private void initComponents() {
        setTitle("Gestión de Clientes - Tienda Catys");
        setSize(900, 550); // Un poco más ancho para mejor lectura
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(245, 245, 245)); // Color de fondo suave

        // --- TÍTULO SUPERIOR ---
        JLabel lblTitulo = new JLabel("MANTENIMIENTO DE CLIENTES", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // --- TABLA ESTILIZADA ---
        String[] columnas = {"ID", "DNI", "Nombre Completo", "Teléfono"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaClientes = new JTable(modeloTabla);
        tablaClientes.setRowHeight(30); // Filas más altas
        tablaClientes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tablaClientes.setSelectionBackground(new Color(173, 216, 230));
        tablaClientes.setShowVerticalLines(false); // Diseño más moderno sin líneas verticales

        // Estilo a la cabecera
        JTableHeader header = tablaClientes.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(new Color(52, 73, 94));
        header.setForeground(Color.WHITE);

        // Centrar columnas específicas (ID y DNI)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tablaClientes.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tablaClientes.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        // IMPORTANTE: El JScrollPane permite que la tabla sea "infinita" y scrollee
        JScrollPane scrollPane = new JScrollPane(tablaClientes);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(scrollPane, BorderLayout.CENTER);

        // --- PANEL DE BOTONES ---
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panelAcciones.setBackground(new Color(245, 245, 245));

        JButton btnNuevo = crearBoton("Agregar Nuevo", new Color(46, 204, 113));
        JButton btnEliminar = crearBoton("Eliminar", new Color(231, 76, 60));
        JButton btnRefrescar = crearBoton("Actualizar", new Color(52, 152, 219));

        btnNuevo.addActionListener(e -> {
            new RegistroClienteFrame().setVisible(true);
        });

        btnEliminar.addActionListener(e -> accionEliminar());
        btnRefrescar.addActionListener(e -> cargarDatosTabla());

        panelAcciones.add(btnRefrescar);
        panelAcciones.add(btnNuevo);
        panelAcciones.add(btnEliminar);

        add(panelAcciones, BorderLayout.SOUTH);
    }

    // Método auxiliar para crear botones con estilo uniforme
    private JButton crearBoton(String texto, Color colorFondo) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(colorFondo);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 40));
        return btn;
    }

    private void cargarDatosTabla() {
        modeloTabla.setRowCount(0);
        List<Cliente> lista = clienteDAO.obtenerTodos();
        
        // El problema de que "no se ven" suele ser porque la lista llega vacía o 
        // el scrollPane no está bien configurado. Aquí los imprimimos todos.
        for (Cliente c : lista) {
            modeloTabla.addRow(new Object[]{
                c.getId(),
                c.getDni(),
                c.getNombre(),
                c.getTelefono()
            });
        }
    }

    private void accionEliminar() {
        int fila = tablaClientes.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente para eliminar.");
            return;
        }

        int id = (int) tablaClientes.getValueAt(fila, 0);
        int confirmar = JOptionPane.showConfirmDialog(this, "¿Eliminar cliente seleccionado?");
        
        if (confirmar == JOptionPane.YES_OPTION) {
            if (clienteDAO.eliminarCliente(id)) {
                cargarDatosTabla();
                JOptionPane.showMessageDialog(this, "Eliminado con éxito.");
            }
        }
    }
}