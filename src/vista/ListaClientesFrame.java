package vista;

import dao.ClienteDAO;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import modelo.Cliente;

public class ListaClientesFrame extends JFrame {

    private JTable tablaClientes;
    private DefaultTableModel modeloTabla;
    private ClienteDAO clienteDAO;
    private JTextField txtBuscar; // Nuevo componente para el buscador
    private TableRowSorter<DefaultTableModel> sorter; // El encargado de filtrar

    public ListaClientesFrame() {
        clienteDAO = new ClienteDAO();
        initComponents();
        cargarDatosTabla();
    }

    private void initComponents() {
        setTitle("Gestión de Clientes - Tienda Catys");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));

        // --- PANEL SUPERIOR: TÍTULO Y BUSCADOR ---
        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));

        JLabel lblTitulo = new JLabel("MANTENIMIENTO DE CLIENTES", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));

        // Configuración del buscador
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBusqueda.add(new JLabel("Buscar por Nombre:"));
        txtBuscar = new JTextField(20);
        txtBuscar.setPreferredSize(new Dimension(200, 30));

        // EVENTO DE BÚSQUEDA EN TIEMPO REAL
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrar();
            }
        });

        panelBusqueda.add(txtBuscar);
        panelSuperior.add(lblTitulo, BorderLayout.WEST);
        panelSuperior.add(panelBusqueda, BorderLayout.EAST);
        add(panelSuperior, BorderLayout.NORTH);

        // --- TABLA ---
        String[] columnas = {"ID", "DNI", "Nombre Completo", "Teléfono"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaClientes = new JTable(modeloTabla);
        tablaClientes.setRowHeight(30);

        // Inicializar el Sorter
        sorter = new TableRowSorter<>(modeloTabla);
        tablaClientes.setRowSorter(sorter);

        add(new JScrollPane(tablaClientes), BorderLayout.CENTER);

        // --- PANEL DE BOTONES (SUR) ---
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        JButton btnNuevo = new JButton("Agregar Nuevo");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnRefrescar = new JButton("Actualizar");

        btnNuevo.addActionListener(e -> new RegistroClienteFrame().setVisible(true));
        btnEliminar.addActionListener(e -> accionEliminar());
        btnRefrescar.addActionListener(e -> {
            txtBuscar.setText(""); // Limpiar buscador al refrescar
            cargarDatosTabla();
        });

        panelAcciones.add(btnRefrescar);
        panelAcciones.add(btnNuevo);
        panelAcciones.add(btnEliminar);
        add(panelAcciones, BorderLayout.SOUTH);
    }

    // Método que realiza el filtrado visual
    private void filtrar() {
        String texto = txtBuscar.getText();
        if (texto.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            // Filtra ignorando mayúsculas/minúsculas en la columna 2 (Nombre)
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 2));
        }
    }

    private void cargarDatosTabla() {
        modeloTabla.setRowCount(0);
        List<Cliente> lista = clienteDAO.obtenerTodos();
        for (Cliente c : lista) {
            modeloTabla.addRow(new Object[]{c.getId(), c.getDni(), c.getNombre(), c.getTelefono()});
        }
    }

    private void accionEliminar() {
        int fila = tablaClientes.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente.");
            return;
        }
        // Cuando hay filtro activo, debemos convertir el índice visual al índice del modelo
        int filaReal = tablaClientes.convertRowIndexToModel(fila);
        int id = (int) modeloTabla.getValueAt(filaReal, 0);

        if (JOptionPane.showConfirmDialog(this, "¿Eliminar?") == JOptionPane.YES_OPTION) {
            if (clienteDAO.eliminarCliente(id)) {
                cargarDatosTabla();
            }
        }
    }
}