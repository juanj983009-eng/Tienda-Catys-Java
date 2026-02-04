package vista;

import dao.ClienteDAO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import modelo.Cliente;

public class RegistroClienteFrame extends JFrame {

    private JTextField txtDni, txtNombre, txtTelefono;

    // --- COLORES CORPORATIVOS ---
    private static final Color COLOR_PRIMARIO = new Color(255, 99, 71); // Naranja
    private static final Color COLOR_SECUNDARIO = new Color(33, 150, 243); // Azul para iconos
    private static final Color COLOR_FONDO = new Color(255, 255, 255);
    private static final Color COLOR_TEXTO = new Color(51, 51, 51);
    private static final Color COLOR_VERDE = new Color(72, 187, 120);

    public RegistroClienteFrame() {
        setTitle("Gestión de Clientes - Catys");
        setSize(850, 500); // Más ancho para el diseño dividido
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. PANEL IZQUIERDO (DECORATIVO)
        JPanel panelIzquierdo = new JPanel();
        panelIzquierdo.setBackground(COLOR_PRIMARIO);
        panelIzquierdo.setPreferredSize(new Dimension(320, 500));
        panelIzquierdo.setLayout(new GridBagLayout());

        // Contenido del panel izquierdo
        JLabel lblIcono = new JLabel("👥");
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 100)); // Emoji Gigante
        
        JLabel lblTituloIzq = new JLabel("<html><div style='text-align: center;'>Comunidad<br>Catys VIP</div></html>");
        lblTituloIzq.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTituloIzq.setForeground(Color.WHITE);
        
        JLabel lblDesc = new JLabel("<html><div style='text-align: center;'>Registra a tus clientes<br>frecuentes para brindarles<br>una mejor atención.</div></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDesc.setForeground(new Color(255, 230, 230)); // Blanco suave

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        panelIzquierdo.add(lblIcono, gbc);
        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 10, 0);
        panelIzquierdo.add(lblTituloIzq, gbc);
        gbc.gridy++;
        panelIzquierdo.add(lblDesc, gbc);


        // 2. PANEL DERECHO (FORMULARIO)
        JPanel panelDerecho = new JPanel(new GridBagLayout());
        panelDerecho.setBackground(Color.WHITE);

        JPanel formBox = new JPanel();
        formBox.setLayout(new BoxLayout(formBox, BoxLayout.Y_AXIS));
        formBox.setBackground(Color.WHITE);
        formBox.setPreferredSize(new Dimension(350, 400));

        // Título del Formulario
        JLabel lblFormTitulo = new JLabel("Nuevo Registro");
        lblFormTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblFormTitulo.setForeground(COLOR_TEXTO);
        lblFormTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- CAMPOS ---
        
        // DNI
        formBox.add(lblFormTitulo);
        formBox.add(Box.createVerticalStrut(30));
        formBox.add(crearEtiqueta("🪪 DNI / Documento:"));
        txtDni = crearCampoTexto();
        formBox.add(txtDni);
        formBox.add(Box.createVerticalStrut(15));

        // Nombre
        formBox.add(crearEtiqueta("👤 Nombre Completo:"));
        txtNombre = crearCampoTexto();
        formBox.add(txtNombre);
        formBox.add(Box.createVerticalStrut(15));

        // Teléfono
        formBox.add(crearEtiqueta("📞 Teléfono / Celular:"));
        txtTelefono = crearCampoTexto();
        formBox.add(txtTelefono);
        formBox.add(Box.createVerticalStrut(30));

        // Botón
        BotonRedondeado btnGuardar = new BotonRedondeado("GUARDAR CLIENTE", COLOR_VERDE, new Color(50, 150, 90), Color.WHITE);
        btnGuardar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGuardar.setMaximumSize(new Dimension(350, 45));
        
        btnGuardar.addActionListener(e -> guardar());
        
        formBox.add(btnGuardar);

        panelDerecho.add(formBox);

        // Agregar paneles a la ventana
        add(panelIzquierdo, BorderLayout.WEST);
        add(panelDerecho, BorderLayout.CENTER);
    }

    // --- UTILIDADES DE DISEÑO ---

    private JLabel crearEtiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12)); // Fuente Emoji activada
        lbl.setForeground(new Color(100, 100, 100));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 5, 0));
        return lbl;
    }

    private JTextField crearCampoTexto() {
        JTextField campo = new JTextField();
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        return campo;
    }

    // Lógica de guardado (Idéntica a la anterior, solo cambia la UI)
    private void guardar() {
        String dni = txtDni.getText();
        String nombre = txtNombre.getText();
        String fono = txtTelefono.getText();

        if (dni.isEmpty() || nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor complete DNI y Nombre.", "Datos Faltantes", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Cliente c = new Cliente(dni, nombre, fono);
        ClienteDAO dao = new ClienteDAO();
        
        if (dao.registrarCliente(c)) {
            JOptionPane.showMessageDialog(this, "¡Cliente registrado exitosamente!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            txtDni.setText(""); txtNombre.setText(""); txtTelefono.setText("");
            txtDni.requestFocus();
        } else {
            JOptionPane.showMessageDialog(this, "Error: El DNI ya existe o hubo un fallo de conexión.", "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Clase botón
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
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10)); // Bordes menos curvos
            super.paintComponent(g); g2.dispose();
        }
    }
}