package vista;

import dao.ClienteDAO;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import modelo.Cliente;

public class RegistroClienteFrame extends JFrame {

    // Componentes de la interfaz
    private JTextField txtDni, txtNombre, txtTelefono;
    private JTable tablaClientes;
    private DefaultTableModel modeloTabla;
    private ClienteDAO clienteDAO;

    public RegistroClienteFrame() {
        clienteDAO = new ClienteDAO(); // Inicializamos el DAO
        initComponents();
        actualizarTablaClientes(); // Cargamos los datos al iniciar
    }

    private void initComponents() {
        setTitle("Gestión de Clientes - Tienda Catys");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // --- PANEL DE FORMULARIO (IZQUIERDA) ---
        JPanel panelForm = new JPanel(new GridLayout(4, 2, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Datos del Cliente"));

        panelForm.add(new JLabel("DNI:"));
        txtDni = new JTextField();
        panelForm.add(txtDni);

        panelForm.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        panelForm.add(txtNombre);

        panelForm.add(new JLabel("Teléfono:"));
        txtTelefono = new JTextField();
        panelForm.add(txtTelefono);

        JButton btnGuardar = new JButton("Guardar Cliente");
        btnGuardar.addActionListener(e -> accionGuardar());
        panelForm.add(btnGuardar);

        add(panelForm, BorderLayout.WEST);

        // --- PANEL DE TABLA (CENTRO) ---
        String[] columnas = {"ID", "DNI", "Nombre Completo", "Teléfono"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaClientes = new JTable(modeloTabla);
        add(new JScrollPane(tablaClientes), BorderLayout.CENTER);
    }

    // MÉTODO CLAVE: Actualiza la tabla usando List<Cliente>
    private void actualizarTablaClientes() {
        List<Cliente> lista = clienteDAO.obtenerTodos(); // Obtenemos datos puros
        modeloTabla.setRowCount(0); // Limpiamos la tabla visual

        for (Cliente c : lista) {
            Object[] fila = {
                c.getId(),
                c.getDni(),
                c.getNombre(),
                c.getTelefono()
            };
            modeloTabla.addRow(fila);
        }
    }

    private void accionGuardar() {
        // 1. Validaciones básicas
        if (txtDni.getText().isEmpty() || txtNombre.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "DNI y Nombre son obligatorios");
            return;
        }

        // 2. Creamos el objeto modelo
        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setDni(txtDni.getText());
        nuevoCliente.setNombre(txtNombre.getText());
        nuevoCliente.setTelefono(txtTelefono.getText());

        // 3. Llamamos al DAO
        if (clienteDAO.registrarCliente(nuevoCliente)) {
            JOptionPane.showMessageDialog(this, "¡Cliente registrado con éxito!");
            limpiarCampos();
            actualizarTablaClientes(); // Refrescamos la tabla automáticamente
        } else {
            JOptionPane.showMessageDialog(this, "Error al registrar. Verifique si el DNI ya existe.");
        }
    }

    private void limpiarCampos() {
        txtDni.setText("");
        txtNombre.setText("");
        txtTelefono.setText("");
        txtDni.requestFocus();
    }
}