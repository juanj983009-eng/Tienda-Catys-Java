package vista;

import dao.UsuarioDAO;
import modelo.Usuario;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField txtUser;
    private JPasswordField txtPass;

    public LoginFrame() {
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

        // Logo (Texto o Imagen)
        JLabel lblLogo = new JLabel("🐱🔒");
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        add(lblLogo, gbc);

        gbc.gridy++;
        JLabel lblTitulo = new JLabel("LOGIN EMPLEADOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(lblTitulo, gbc);

        // Campos
        gbc.gridy++;
        txtUser = new JTextField(15);
        txtUser.setBorder(BorderFactory.createTitledBorder("Usuario"));
        add(txtUser, gbc);

        gbc.gridy++;
        txtPass = new JPasswordField(15);
        txtPass.setBorder(BorderFactory.createTitledBorder("Contraseña"));
        add(txtPass, gbc);

        // Botón
        gbc.gridy++;
        JButton btnIngresar = new JButton("Ingresar al Sistema");
        btnIngresar.setBackground(new Color(255, 99, 71));
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setPreferredSize(new Dimension(200, 40));

        btnIngresar.addActionListener(e -> validarLogin());
        add(btnIngresar, gbc);
    }

    private void validarLogin() {
        String user = txtUser.getText();
        String pass = new String(txtPass.getPassword());

        UsuarioDAO dao = new UsuarioDAO();
        Usuario usuarioEncontrado = dao.login(user, pass);

        if (usuarioEncontrado != null) {
            JOptionPane.showMessageDialog(this, "¡Bienvenido, " + usuarioEncontrado.getNombre() + "!");
            this.dispose(); // Cerrar Login

            // AQUÍ ABRIMOS LA TIENDA O EL DASHBOARD
            // Por ahora, instanciamos la tienda antigua como ejemplo
            // new TiendaGUI().setVisible(true); // (Tendrás que adaptar TiendaGUI para que sea una clase instanciable)

            // O abrir un Dashboard nuevo
            new DashboardFrame(usuarioEncontrado).setVisible(true);

        } else {
            JOptionPane.showMessageDialog(this, "Credenciales incorrectas", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new LoginFrame().setVisible(true);
    }
}