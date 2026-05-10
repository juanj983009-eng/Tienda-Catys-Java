package vista;

import modelo.Usuario;
import repository.UsuarioRepository;
import repository.exception.DataAccessException;
import service.UsuarioService;
import util.AppLogger;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

/**
 * Frame de login simplificado (versión compacta — para compatibilidad).
 * El login principal con diseño completo es LoginGUI.java.
 *
 * CAMBIOS: migrado a UsuarioService — ya no usa dao.UsuarioDAO.
 */
public class LoginFrame extends JFrame {

    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnIngresar;

    private final UsuarioService usuarioService;

    public LoginFrame() {
        this(new UsuarioService(new UsuarioRepository()));
    }

    public LoginFrame(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
        setTitle("Acceso al Sistema - Catys");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(245, 247, 250));
        iniciarComponentes();
    }

    private void iniciarComponentes() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel lblLogo = new JLabel("\uD83D\uDC31\uD83D\uDD12");
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        add(lblLogo, gbc);

        gbc.gridy++;
        JLabel lblTitulo = new JLabel("LOGIN EMPLEADOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(lblTitulo, gbc);

        gbc.gridy++;
        txtUser = new JTextField(15);
        txtUser.setName("txtUser");
        txtUser.setBorder(BorderFactory.createTitledBorder("Usuario"));
        add(txtUser, gbc);

        gbc.gridy++;
        txtPass = new JPasswordField(15);
        txtPass.setName("txtPass");
        txtPass.setBorder(BorderFactory.createTitledBorder("Contrasena"));
        add(txtPass, gbc);

        gbc.gridy++;
        btnIngresar = new JButton("Ingresar al Sistema");
        btnIngresar.setName("btnIngresar");
        btnIngresar.setBackground(new Color(255, 99, 71));
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setPreferredSize(new Dimension(200, 40));
        btnIngresar.addActionListener(e -> ejecutarLogin());
        txtPass.addActionListener(e -> ejecutarLogin());
        add(btnIngresar, gbc);
    }

    private void ejecutarLogin() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa usuario y contrasena.", "Campos requeridos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnIngresar.setEnabled(false);
        btnIngresar.setText("Verificando...");

        new SwingWorker<Optional<Usuario>, Void>() {
            @Override
            protected Optional<Usuario> doInBackground() throws DataAccessException {
                return usuarioService.autenticar(user, pass);
            }

            @Override
            protected void done() {
                btnIngresar.setEnabled(true);
                btnIngresar.setText("Ingresar al Sistema");
                try {
                    Optional<Usuario> resultado = get();
                    if (resultado.isPresent()) {
                        dispose();
                        new DashboardFrame(resultado.get()).setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this,
                            "Credenciales incorrectas.", "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
                        txtPass.setText("");
                        txtPass.requestFocus();
                    }
                } catch (Exception ex) {
                    AppLogger.error("Error en login simplificado", ex);
                    JOptionPane.showMessageDialog(LoginFrame.this,
                        "Error al conectar con el sistema.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        AppLogger.init();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}