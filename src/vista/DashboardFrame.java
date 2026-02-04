package vista;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import modelo.Usuario;

public class DashboardFrame extends JFrame {

    private Usuario usuarioActual;

    // --- COLORES MODERNOS ---
    private static final Color COLOR_FONDO = new Color(245, 247, 250); 
    private static final Color COLOR_HEADER = new Color(33, 43, 54);   
    private static final Color ACCENTO_NARANJA = new Color(255, 99, 71);
    private static final Color ACCENTO_AZUL = new Color(33, 150, 243);
    private static final Color ACCENTO_MORADO = new Color(156, 39, 176);
    private static final Color ACCENTO_AMARILLO = new Color(255, 193, 7);
    private static final Color ACCENTO_TEAL = new Color(0, 150, 136); 

    public DashboardFrame(Usuario usuario) {
        this.usuarioActual = usuario;
        
        setTitle("Panel de Control - Catys Enterprise");
        setSize(1250, 680); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_HEADER);
        header.setPreferredSize(new Dimension(1000, 70));
        header.setBorder(new EmptyBorder(0, 30, 0, 30));

        JLabel lblTitulo = new JLabel("🐱 CATYS | POS");
        lblTitulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22)); 
        lblTitulo.setForeground(Color.WHITE);
        
        JLabel lblUser = new JLabel("<html><span style='font-family:Segoe UI Emoji;'>👤</span> " + usuario.getNombreCompleto() + " <span style='color:#aaaaaa;'>|</span> <span style='color:#4caf50; font-weight:bold;'>" + usuario.getRol() + "</span></html>");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUser.setForeground(Color.WHITE);

        header.add(lblTitulo, BorderLayout.WEST);
        header.add(lblUser, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // PANEL CENTRAL
        JPanel panelCentral = new JPanel(new GridBagLayout());
        panelCentral.setBackground(COLOR_FONDO);
        
        JPanel gridBotones = new JPanel(new GridLayout(1, 5, 15, 0)); 
        gridBotones.setOpaque(false); 
        gridBotones.setPreferredSize(new Dimension(1150, 220)); 

        JButton btnVentas = new TarjetaBoton("🛒", "Punto de Venta", "Realizar nueva venta", ACCENTO_NARANJA);
        JButton btnClientes = new TarjetaBoton("👥", "Clientes VIP", "Directorio de clientes", ACCENTO_AZUL);
        JButton btnReportes = new TarjetaBoton("📊", "Reportes", "Historial financiero", ACCENTO_MORADO);
        JButton btnMenu = new TarjetaBoton("🍱", "Gestión Menú", "Editar platos y precios", ACCENTO_AMARILLO);
        JButton btnCocina = new TarjetaBoton("👨‍🍳", "Monitor Cocina", "Ver pedidos en cola", ACCENTO_TEAL);

        gridBotones.add(btnVentas);
        gridBotones.add(btnClientes);
        gridBotones.add(btnReportes);
        gridBotones.add(btnMenu);
        gridBotones.add(btnCocina);

        panelCentral.add(gridBotones);
        add(panelCentral, BorderLayout.CENTER);

        // FOOTER
        JLabel footer = new JLabel("Sistema de Gestión v2.6 - SQL Server Conectado  ", SwingConstants.RIGHT);
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.setForeground(Color.GRAY);
        footer.setBorder(new EmptyBorder(5, 5, 5, 10));
        add(footer, BorderLayout.SOUTH);

        // --- ACCIONES ---
        
        btnVentas.addActionListener(e -> {
            this.dispose(); 
            TiendaGUI.main(new String[]{}); 
        });

        // CORRECCIÓN AQUÍ: AHORA ABRE LA LISTA
        btnClientes.addActionListener(e -> {
            new ListaClientesFrame().setVisible(true); 
        });

        btnReportes.addActionListener(e -> {
            if (usuario.getRol().equalsIgnoreCase("ADMIN")) {
                new ReportesFrame().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "⛔ Acceso denegado: Se requieren permisos de Administrador.");
            }
        });

        btnMenu.addActionListener(e -> {
            if (usuario.getRol().equalsIgnoreCase("ADMIN")) {
                new GestionProductosFrame().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "⛔ Acceso denegado: Se requieren permisos de Administrador.");
            }
        });

        btnCocina.addActionListener(e -> new CocinaFrame().setVisible(true));
    }

    // CLASE BOTÓN INTERNA
    static class TarjetaBoton extends JButton {
        private Color colorAcento;
        private boolean mouseEncima = false;

        public TarjetaBoton(String icono, String titulo, String subtitulo, Color color) {
            this.colorAcento = color;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            JLabel lblIcono = new JLabel(icono);
            lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
            lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel lblTitulo = new JLabel(titulo);
            lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblTitulo.setForeground(Color.DARK_GRAY);
            lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel lblSub = new JLabel(subtitulo);
            lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblSub.setForeground(Color.GRAY);
            lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

            add(Box.createVerticalStrut(20));
            add(lblIcono);
            add(Box.createVerticalStrut(15));
            add(lblTitulo);
            add(Box.createVerticalStrut(5));
            add(lblSub);
            add(Box.createVerticalStrut(20));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    mouseEncima = true;
                    repaint();
                }
                public void mouseExited(MouseEvent e) {
                    mouseEncima = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (mouseEncima) {
                g2.setColor(new Color(245, 245, 255));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setColor(colorAcento);
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 20, 20));
            } else {
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setColor(new Color(230, 230, 230));
                g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 20, 20));
            }
            
            g2.setColor(colorAcento);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 6, 20, 20));
            g2.fillRect(0, 3, getWidth(), 3); 

            super.paintComponent(g);
            g2.dispose();
        }
    }
}