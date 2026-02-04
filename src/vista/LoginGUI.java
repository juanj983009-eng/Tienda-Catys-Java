package vista;

import dao.UsuarioDAO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import modelo.Usuario;

public class LoginGUI extends JFrame {

    private JTextField txtUser;
    private JPasswordField txtPass;

    // --- Colores de la Marca ---
    private static final Color COLOR_PRIMARIO = new Color(255, 99, 71); // Naranja Catys
    private static final Color COLOR_FONDO_OSCURO = new Color(45, 55, 72); // Gris azulado
    private static final Color COLOR_TEXTO_GRIS = new Color(100, 116, 139);

    public LoginGUI() {
        setTitle("Sistema de Seguridad - Catys");
        setSize(900, 600); // Ventana más grande para lucir el diseño
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. PANEL IZQUIERDO (Imagen / Decoración)
        JPanel panelIzquierdo = new JPanel();
        panelIzquierdo.setBackground(COLOR_PRIMARIO);
        panelIzquierdo.setPreferredSize(new Dimension(400, 600));
        panelIzquierdo.setLayout(new GridBagLayout());

        JLabel lblLogoGrande = new JLabel("🐱🍜");
        lblLogoGrande.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 120));

        JLabel lblSlogan = new JLabel("<html><div style='text-align: center;'>Gestión Inteligente<br>para tu Restaurante</div></html>");
        lblSlogan.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblSlogan.setForeground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx=0; gbc.gridy=0;
        panelIzquierdo.add(lblLogoGrande, gbc);
        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 0, 0); // Espacio
        panelIzquierdo.add(lblSlogan, gbc);


        // 2. PANEL DERECHO (Formulario de Login)
        JPanel panelDerecho = new JPanel(new GridBagLayout());
        panelDerecho.setBackground(Color.WHITE);

        // Contenedor del Formulario
        JPanel formBox = new JPanel();
        formBox.setLayout(new BoxLayout(formBox, BoxLayout.Y_AXIS));
        formBox.setBackground(Color.WHITE);
        formBox.setPreferredSize(new Dimension(320, 400));

        // Título del Formulario
        JLabel lblBienvenida = new JLabel("Iniciar Sesión");
        lblBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblBienvenida.setForeground(COLOR_FONDO_OSCURO);
        lblBienvenida.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("Ingresa tus credenciales para acceder");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitulo.setForeground(COLOR_TEXTO_GRIS);
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Campos de Texto con Estilo
        JLabel lblUser = new JLabel("Usuario");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(COLOR_FONDO_OSCURO);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUser = new JTextField();
        estilizarCampo(txtUser);

        JLabel lblPass = new JLabel("Contraseña");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(COLOR_FONDO_OSCURO);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPass = new JPasswordField();
        estilizarCampo(txtPass);

        // Botón Personalizado
        BotonRedondeado btnIngresar = new BotonRedondeado("ENTRAR AL SISTEMA", COLOR_PRIMARIO, new Color(220, 80, 60), Color.WHITE);
        btnIngresar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnIngresar.setMaximumSize(new Dimension(320, 45));

        // Acción del Botón
        btnIngresar.addActionListener(e -> validar());

        // Armado del Formulario (Añadiendo espaciadores)
        formBox.add(lblBienvenida);
        formBox.add(lblSubtitulo);
        formBox.add(Box.createVerticalStrut(40));
        formBox.add(lblUser);
        formBox.add(Box.createVerticalStrut(5));
        formBox.add(txtUser);
        formBox.add(Box.createVerticalStrut(20));
        formBox.add(lblPass);
        formBox.add(Box.createVerticalStrut(5));
        formBox.add(txtPass);
        formBox.add(Box.createVerticalStrut(40));
        formBox.add(btnIngresar);

        panelDerecho.add(formBox);

        // Agregar paneles a la ventana
        add(panelIzquierdo, BorderLayout.WEST);
        add(panelDerecho, BorderLayout.CENTER);
    }

    private void estilizarCampo(JTextField campo) {
        campo.setMaximumSize(new Dimension(320, 40));
        campo.setPreferredSize(new Dimension(320, 40));
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1, true), // Borde redondeado
            new EmptyBorder(5, 10, 5, 10) // Padding interno
        ));
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void validar() {
        String u = txtUser.getText();
        String p = new String(txtPass.getPassword());

        if(u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese usuario y contraseña.");
            return;
        }

        UsuarioDAO dao = new UsuarioDAO();
        Usuario usuarioEncontrado = dao.validarLogin(u, p);

        if (usuarioEncontrado != null) {
            this.dispose(); 
            // Abre el Dashboard
            new DashboardFrame(usuarioEncontrado).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Credenciales incorrectas", "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Clase botón (Igual que en las otras ventanas)
    static class BotonRedondeado extends JButton {
        private Color colorNormal, colorHover;
        public BotonRedondeado(String texto, Color bgNormal, Color bgHover, Color textoColor) {
            super(texto);
            this.colorNormal = bgNormal; this.colorHover = bgHover;
            setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false);
            setForeground(textoColor); setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR)); setBackground(colorNormal);
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(colorHover); repaint(); }
                public void mouseExited(MouseEvent e) { setBackground(colorNormal); repaint(); }
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground()); g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            super.paintComponent(g); g2.dispose();
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
    }
}