package vista;

import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import modelo.Cliente;
import repository.ClienteRepository;
import repository.exception.DataAccessException;
import service.ClienteService;
import service.exception.ValidacionException;
import util.AppLogger;

/**
 * Diálogo para registrar un nuevo cliente.
 *
 * CAMBIOS vs versión anterior:
 * - Usa ClienteService en lugar de ClienteDAO directamente
 * - Validaciones delegadas al ClienteService (ValidacionException)
 * - El Service detecta DNI duplicado antes de intentar guardar en BD
 * - accionGuardar() usa SwingWorker — no bloquea el EDT durante el INSERT
 * - Acepta ClienteService inyectado desde ListaClientesFrame (comparte la misma
 * instancia)
 */
public class RegistroClienteFrame extends JFrame {

    private JTextField txtDni, txtNombre, txtTelefono;
    private JButton btnGuardar;

    private final ClienteService clienteService;

    public RegistroClienteFrame(ClienteService clienteService) {
        this.clienteService = clienteService;
        initComponents();
    }

    /** Constructor de conveniencia */
    public RegistroClienteFrame() {
        this(new ClienteService(new ClienteRepository()));
    }

    private void initComponents() {
        setTitle("Registrar Nuevo Cliente");
        setSize(420, 320);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(new JLabel("DNI (8 dígitos):"));
        txtDni = new JTextField();
        txtDni.setName("txtDni");
        add(txtDni);

        add(new JLabel("Nombre Completo:"));
        txtNombre = new JTextField();
        txtNombre.setName("txtNombre");
        add(txtNombre);

        add(new JLabel("Telefono (9 digitos, empieza en 9):"));
        txtTelefono = new JTextField();
        txtTelefono.setName("txtTelefono");
        add(txtTelefono);

        btnGuardar = new JButton("Guardar");
        btnGuardar.setName("btnGuardar");
        btnGuardar.setBackground(new Color(46, 204, 113));
        btnGuardar.setForeground(Color.WHITE);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        add(btnGuardar);
        add(btnCancelar);

        btnGuardar.addActionListener(e -> accionGuardar());
        txtTelefono.addActionListener(e -> accionGuardar());
    }

    private void accionGuardar() {
        Cliente cliente = new Cliente();
        cliente.setDni(txtDni.getText().trim());
        cliente.setNombre(txtNombre.getText().trim());
        cliente.setTelefono(txtTelefono.getText().trim());

        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");

        new SwingWorker<Void, Void>() {
            private String mensajeError = null;
            private String campoConError = null;

            @Override
            protected Void doInBackground() {
                try {
                    clienteService.registrar(cliente);
                } catch (ValidacionException e) {
                    mensajeError = "\u26a0 " + e.getMessage();
                    campoConError = e.getCampo();
                } catch (DataAccessException e) {
                    AppLogger.error("Error de BD al registrar cliente", e);
                    mensajeError = "\u274c Error al guardar en la base de datos.\n" + e.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                btnGuardar.setEnabled(true);
                btnGuardar.setText("Guardar");
                if (mensajeError == null) {
                    JOptionPane.showMessageDialog(RegistroClienteFrame.this,
                            "\u2705 Cliente registrado correctamente.", "Exito",
                            JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(RegistroClienteFrame.this,
                            mensajeError, "Error de validacion", JOptionPane.ERROR_MESSAGE);
                    enfocarCampoConError(campoConError);
                }
            }
        }.execute();
    }

    private void enfocarCampoConError(String campo) {
        if (campo == null)
            return;
        switch (campo) {
            case "dni":
                txtDni.requestFocus();
                break;
            case "nombre":
                txtNombre.requestFocus();
                break;
            case "telefono":
                txtTelefono.requestFocus();
                break;
        }
    }
}