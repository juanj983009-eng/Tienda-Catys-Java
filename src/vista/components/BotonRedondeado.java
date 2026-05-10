package vista.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Botón redondeado reutilizable — componente único compartido por toda la UI.
 *
 * ANTES: definido dos veces (TiendaGUI y ReportesFrame) con código duplicado.
 * AHORA: una sola definición en vista.components — cualquier frame lo importa.
 *
 * Uso: new BotonRedondeado("Confirmar", COLOR_VERDE, COLOR_VERDE_HOVER, Color.WHITE);
 */
public class BotonRedondeado extends JButton {

    private Color colorNormal;
    private Color colorHover;

    /**
     * @param texto        texto que mostrará el botón
     * @param colorNormal  color de fondo normal
     * @param colorHover   color de fondo al pasar el mouse
     * @param colorTexto   color del texto
     */
    public BotonRedondeado(String texto, Color colorNormal, Color colorHover, Color colorTexto) {
        super(texto);
        this.colorNormal = colorNormal;
        this.colorHover = colorHover;

        // Sin el fondo por defecto de Swing — lo pintamos nosotros
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(colorTexto);
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBackground(colorNormal);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(colorHover);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(colorNormal);
                repaint();
            }
        });
    }

    /**
     * Actualiza los colores del botón en tiempo de ejecución.
     * Útil para implementar highlighting de botón activo sin recrear el componente.
     *
     * @param normalColor  nuevo color normal
     * @param hoverColor   nuevo color hover
     * @param textColor    nuevo color del texto
     */
    public void setNormalColor(Color normalColor, Color hoverColor, Color textColor) {
        this.colorNormal = normalColor;
        this.colorHover  = hoverColor;
        setForeground(textColor);
        setBackground(normalColor);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (!isEnabled()) {
            g2.setColor(new Color(45, 45, 60));
        } else {
            g2.setColor(getBackground());
        }
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
        super.paintComponent(g);
        g2.dispose();
    }
}
