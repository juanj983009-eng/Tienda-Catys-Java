package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import modelo.Usuario;
import repository.UsuarioRepository;
import repository.exception.DataAccessException;
import service.UsuarioService;
import util.AppLogger;
import vista.components.BotonRedondeado;

/**
 * Panel de autenticación del sistema (v4.0 — Single Window).
 *
 * CAMBIOS vs versión anterior:
 * - Extiende JPanel en lugar de JFrame — ya NO es una ventana independiente
 * - Recibe MainWindow por constructor para llamar onLoginExitoso(usuario)
 * - En éxito: mainWindow.onLoginExitoso(usuario) en lugar de dispose() + new DashboardFrame()
 * - Paleta de colores referenciada desde MainWindow para consistencia global
 */
public class LoginGUI extends JPanel {

    private JTextField txtUser;
    private JPasswordField txtPass;
    private BotonRedondeado btnIngresar;

    private final UsuarioService usuarioService;
    private final MainWindow     mainWindow;

    // Colores desde la paleta maestra
    private static final Color C_PRIMARIO  = MainWindow.C_PRIMARIO;
    private static final Color C_PRIM_HOV  = MainWindow.C_PRIM_HOVER;
    private static final Color C_FONDO     = MainWindow.C_FONDO;
    private static final Color C_SUPERF    = MainWindow.C_SUPERFICIE;
    private static final Color C_SUPERF2   = MainWindow.C_SUPERFICIE2;
    private static final Color C_TEXTO     = MainWindow.C_TEXTO;
    private static final Color C_TEXTO_S   = MainWindow.C_TEXTO_S;
    private static final Color C_BORDE     = MainWindow.C_BORDE;

    public LoginGUI(MainWindow mainWindow) {
        this(mainWindow, new UsuarioService(new UsuarioRepository()));
    }

    public LoginGUI(MainWindow mainWindow, UsuarioService usuarioService) {
        this.mainWindow      = mainWindow;
        this.usuarioService  = usuarioService;
        construirUI();
    }

    private void construirUI() {
        setLayout(new BorderLayout());
        setBackground(C_FONDO);

        // --- Panel izquierdo decorativo con gradiente ---
        JPanel lateral = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, C_PRIMARIO, 0, getHeight(), C_PRIM_HOV));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        lateral.setPreferredSize(new Dimension(420, 0));
        lateral.setOpaque(false);

        JPanel ltCon = new JPanel();
        ltCon.setLayout(new BoxLayout(ltCon, BoxLayout.Y_AXIS));
        ltCon.setOpaque(false);

        JLabel emoji   = mkLbl("🐱🍜", "Segoe UI Emoji", Font.PLAIN, 110, Color.WHITE);
        JLabel marca   = mkLbl("Restaurante Catys", "Segoe UI", Font.BOLD, 28, Color.WHITE);
        JLabel slogan  = new JLabel(
            "<html><center>Sistema ERP/POS Profesional<br>Gestiona tu restaurante con precisión</center></html>");
        slogan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        slogan.setForeground(new Color(255, 220, 200));
        slogan.setHorizontalAlignment(SwingConstants.CENTER);
        slogan.setAlignmentX(Component.CENTER_ALIGNMENT);
        emoji.setAlignmentX(Component.CENTER_ALIGNMENT);
        marca.setAlignmentX(Component.CENTER_ALIGNMENT);

        ltCon.add(emoji);
        ltCon.add(Box.createVerticalStrut(20));
        ltCon.add(marca);
        ltCon.add(Box.createVerticalStrut(10));
        ltCon.add(slogan);
        lateral.add(ltCon);

        // --- Panel derecho — formulario ---
        JPanel derecho = new JPanel(new GridBagLayout());
        derecho.setBackground(C_FONDO);

        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(C_SUPERF);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDE, 1, true),
            new EmptyBorder(50, 55, 50, 55)
        ));

        JLabel lTit = mkLbl("Iniciar Sesión",                        "Segoe UI", Font.BOLD,  28, C_TEXTO);
        JLabel lSub = mkLbl("Accede al sistema con tus credenciales", "Segoe UI", Font.PLAIN, 14, C_TEXTO_S);
        lTit.setAlignmentX(Component.LEFT_ALIGNMENT);
        lSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUser = crearCampo("txtUsuario");
        txtPass = new JPasswordField();
        txtPass.setName("txtPassword");
        estilizarCampo(txtPass);

        btnIngresar = new BotonRedondeado("ENTRAR AL SISTEMA", C_PRIMARIO, C_PRIM_HOV, Color.WHITE);
        btnIngresar.setName("btnIngresar");
        btnIngresar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnIngresar.setMaximumSize(new Dimension(320, 48));
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 15));

        txtPass.addActionListener(e -> ejecutarLogin());
        btnIngresar.addActionListener(e -> ejecutarLogin());

        JLabel lLblU = mkLbl("Usuario",    "Segoe UI", Font.BOLD, 12, C_TEXTO_S);
        JLabel lLblP = mkLbl("Contraseña", "Segoe UI", Font.BOLD, 12, C_TEXTO_S);
        lLblU.setAlignmentX(Component.LEFT_ALIGNMENT);
        lLblP.setAlignmentX(Component.LEFT_ALIGNMENT);

        tarjeta.add(lTit);             tarjeta.add(Box.createVerticalStrut(6));
        tarjeta.add(lSub);             tarjeta.add(Box.createVerticalStrut(36));
        tarjeta.add(lLblU);            tarjeta.add(Box.createVerticalStrut(6));
        tarjeta.add(txtUser);          tarjeta.add(Box.createVerticalStrut(20));
        tarjeta.add(lLblP);            tarjeta.add(Box.createVerticalStrut(6));
        tarjeta.add(txtPass);          tarjeta.add(Box.createVerticalStrut(36));
        tarjeta.add(btnIngresar);

        derecho.add(tarjeta);

        add(lateral, BorderLayout.WEST);
        add(derecho, BorderLayout.CENTER);
    }

    private void ejecutarLogin() {
        String username = txtUser.getText().trim();
        String password = new String(txtPass.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingresa usuario y contraseña.",
                "Campos requeridos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnIngresar.setEnabled(false);
        btnIngresar.setText("Verificando...");

        new SwingWorker<Optional<Usuario>, Void>() {
            @Override
            protected Optional<Usuario> doInBackground() throws DataAccessException {
                return usuarioService.autenticar(username, password);
            }

            @Override
            protected void done() {
                btnIngresar.setEnabled(true);
                btnIngresar.setText("ENTRAR AL SISTEMA");
                try {
                    Optional<Usuario> resultado = get();
                    if (resultado.isPresent()) {
                        mainWindow.onLoginExitoso(resultado.get());
                    } else {
                        JOptionPane.showMessageDialog(LoginGUI.this,
                            "Usuario o contraseña incorrectos.", "Acceso Denegado",
                            JOptionPane.ERROR_MESSAGE);
                        txtPass.setText("");
                        txtPass.requestFocus();
                    }
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    AppLogger.error("Error inesperado durante el login", causa);
                    causa.printStackTrace();
                    JOptionPane.showMessageDialog(LoginGUI.this,
                        "❌ Error al conectar con el sistema.\n" + causa.getMessage(),
                        "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    AppLogger.error("Error inesperado durante el login", ex);
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(LoginGUI.this,
                        "❌ Error al conectar con el sistema.\n" + ex.getMessage(),
                        "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private JTextField crearCampo(String name) {
        JTextField t = new JTextField();
        t.setName(name);
        estilizarCampo(t);
        return t;
    }

    private void estilizarCampo(JTextField campo) {
        campo.setMaximumSize(new Dimension(320, 46));
        campo.setPreferredSize(new Dimension(320, 46));
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        campo.setBackground(C_SUPERF2);
        campo.setForeground(C_TEXTO);
        campo.setCaretColor(C_TEXTO);
        campo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDE, 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JLabel mkLbl(String t, String fuente, int estilo, int size, Color color) {
        JLabel l = new JLabel(t);
        l.setFont(new Font(fuente, estilo, size));
        l.setForeground(color);
        return l;
    }
}