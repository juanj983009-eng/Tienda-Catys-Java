package vista;

import dao.ClienteDAO;
import java.awt.*;
import javax.swing.*;
import modelo.Cliente;

public class RegistroClienteFrame extends JFrame {

    private JTextField txtDni, txtNombre, txtTelefono;
    private JButton btnGuardar, btnCancelar;
    private ClienteDAO clienteDAO;

    public RegistroClienteFrame() {
        clienteDAO = new ClienteDAO();
        initComponents();
    }

    private void initComponents() {
        setTitle("Registrar Nuevo Cliente");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 10, 10));
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Componentes
        add(new JLabel("DNI (8 dígitos):"));
        txtDni = new JTextField();
        add(txtDni);

        add(new JLabel("Nombre Completo:"));
        txtNombre = new JTextField();
        add(txtNombre);

        add(new JLabel("Teléfono (9 dígitos):"));
        txtTelefono = new JTextField();
        add(txtTelefono);

        btnGuardar = new JButton("Guardar");
        btnGuardar.setBackground(new Color(46, 204, 113));
        btnGuardar.setForeground(Color.WHITE);

        btnCancelar = new JButton("Cancelar");

        add(btnGuardar);
        add(btnCancelar);

        // EVENTO GUARDAR CON VALIDACIÓN REGEX
        btnGuardar.addActionListener(e -> accionGuardar());

        btnCancelar.addActionListener(e -> dispose());
    }

    private void accionGuardar() {
        String dni = txtDni.getText().trim();
        String nombre = txtNombre.getText().trim();
        String telefono = txtTelefono.getText().trim();

        // LLAMADA A LAS VALIDACIONES (Punto 4)
        if (validarCampos(dni, nombre, telefono)) {
            Cliente nuevoCliente = new Cliente();
            nuevoCliente.setDni(dni);
            nuevoCliente.setNombre(nombre);
            nuevoCliente.setTelefono(telefono);

            if (clienteDAO.registrarCliente(nuevoCliente)) {
                JOptionPane.showMessageDialog(this, "Cliente registrado correctamente.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error al guardar en la base de datos.");
            }
        }
    }

    /**
     * Implementación de Expresiones Regulares (Regex) para validación de datos.
     */
    private boolean validarCampos(String dni, String nombre, String telefono) {
        // 1. Validar DNI: Solo 8 dígitos numéricos
        if (!dni.matches("\\d{8}")) {
            JOptionPane.showMessageDialog(this, "Error: El DNI debe tener exactamente 8 números.");
            txtDni.requestFocus();
            return false;
        }

        // 2. Validar Nombre: Letras, tildes y espacios (3 a 100 caracteres)
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]{3,100}$")) {
            JOptionPane.showMessageDialog(this, "Error: El nombre solo puede contener letras y debe ser real.");
            txtNombre.requestFocus();
            return false;
        }

        // 3. Validar Teléfono: Empieza con 9 y tiene 9 dígitos
        if (!telefono.matches("9\\d{8}")) {
            JOptionPane.showMessageDialog(this, "Error: El teléfono debe empezar con 9 y tener 9 dígitos.");
            txtTelefono.requestFocus();
            return false;
        }

        return true;
    }
}