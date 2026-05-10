package vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import modelo.Cliente;
import repository.ClienteRepository;
import repository.exception.DataAccessException;
import service.ClienteService;
import util.AppLogger;

/**
 * Frame de gestión de clientes (lista y eliminación).
 *
 * CAMBIOS vs versión anterior:
 * - Usa ClienteService en lugar de ClienteDAO directamente
 * - cargarDatos() usa SwingWorker — no bloquea el EDT al abrir
 * - accionEliminar() también asíncrona
 * - Errores de negocio (DataAccessException) se muestran al usuario con mensaje legible
 */
public class ListaClientesFrame extends JFrame {

    private JTable tablaClientes;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private TableRowSorter<DefaultTableModel> sorter;

    private final ClienteService clienteService;

    public ListaClientesFrame(ClienteService clienteService) {
        this.clienteService = clienteService;
        initComponents();
        cargarDatosEnBackground();
    }

    /** Constructor de conveniencia */
    public ListaClientesFrame() {
        this(new ClienteService(new ClienteRepository()));
    }

    private void initComponents() {
        setTitle("Gestión de Clientes - Tienda Catys");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));

        // --- Panel superior: título y buscador ---
        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));

        JLabel lblTitulo = new JLabel("MANTENIMIENTO DE CLIENTES", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBusqueda.add(new JLabel("Buscar por Nombre:"));
        txtBuscar = new JTextField(20);
        txtBuscar.setName("txtBuscar");
        txtBuscar.setPreferredSize(new Dimension(200, 30));
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { filtrar(); }
        });
        panelBusqueda.add(txtBuscar);

        panelSuperior.add(lblTitulo,    BorderLayout.WEST);
        panelSuperior.add(panelBusqueda, BorderLayout.EAST);
        add(panelSuperior, BorderLayout.NORTH);

        // --- Tabla ---
        modeloTabla = new DefaultTableModel(
            new String[]{"ID", "DNI", "Nombre Completo", "Teléfono"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tablaClientes = new JTable(modeloTabla);
        tablaClientes.setName("tablaClientes");
        tablaClientes.setRowHeight(30);

        sorter = new TableRowSorter<>(modeloTabla);
        tablaClientes.setRowSorter(sorter);

        add(new JScrollPane(tablaClientes), BorderLayout.CENTER);

        // --- Botones ---
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        JButton btnNuevo     = new JButton("Agregar Nuevo");
        JButton btnEliminar  = new JButton("Eliminar");
        JButton btnRefrescar = new JButton("Actualizar");
        btnNuevo.setName("btnNuevoCliente");
        btnEliminar.setName("btnEliminarCliente");

        btnNuevo.addActionListener(e -> new RegistroClienteFrame(clienteService).setVisible(true));
        btnEliminar.addActionListener(e -> accionEliminar(btnEliminar));
        btnRefrescar.addActionListener(e -> {
            txtBuscar.setText("");
            sorter.setRowFilter(null);
            cargarDatosEnBackground();
        });

        panelAcciones.add(btnRefrescar);
        panelAcciones.add(btnNuevo);
        panelAcciones.add(btnEliminar);
        add(panelAcciones, BorderLayout.SOUTH);
    }

    private void filtrar() {
        String texto = txtBuscar.getText().trim();
        sorter.setRowFilter(texto.isEmpty() ? null : RowFilter.regexFilter("(?i)" + texto, 2));
    }

    private void cargarDatosEnBackground() {
        new SwingWorker<List<Cliente>, Void>() {
            @Override
            protected List<Cliente> doInBackground() throws DataAccessException {
                return clienteService.getTodos();
            }

            @Override
            protected void done() {
                try {
                    List<Cliente> lista = get();
                    modeloTabla.setRowCount(0);
                    for (Cliente c : lista) {
                        modeloTabla.addRow(new Object[]{c.getId(), c.getDni(), c.getNombre(), c.getTelefono()});
                    }
                } catch (Exception ex) {
                    AppLogger.error("Error al cargar lista de clientes", ex);
                    JOptionPane.showMessageDialog(ListaClientesFrame.this,
                        "❌ Error al cargar clientes: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void accionEliminar(JButton btnEliminar) {
        int fila = tablaClientes.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente de la lista primero.",
                "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int filaReal = tablaClientes.convertRowIndexToModel(fila);
        int id       = (int) modeloTabla.getValueAt(filaReal, 0);
        String nombre = modeloTabla.getValueAt(filaReal, 2).toString();

        int confirma = JOptionPane.showConfirmDialog(this,
            "¿Eliminar al cliente \"" + nombre + "\"?\nEsta acción no se puede deshacer.",
            "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirma != JOptionPane.YES_OPTION) return;

        btnEliminar.setEnabled(false);

        new SwingWorker<Void, Void>() {
            private String mensajeError;

            @Override
            protected Void doInBackground() {
                try {
                    clienteService.eliminar(id);
                } catch (DataAccessException e) {
                    AppLogger.error("Error al eliminar cliente ID " + id, e);
                    mensajeError = "Error al eliminar: " + e.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                btnEliminar.setEnabled(true);
                if (mensajeError == null) {
                    JOptionPane.showMessageDialog(ListaClientesFrame.this,
                        "✅ Cliente eliminado correctamente.", "Eliminado", JOptionPane.INFORMATION_MESSAGE);
                    cargarDatosEnBackground();
                } else {
                    JOptionPane.showMessageDialog(ListaClientesFrame.this, mensajeError,
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}