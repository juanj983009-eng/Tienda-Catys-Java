package vista;

import util.AppLogger;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

/**
 * Utilidad de imágenes extraída de TiendaGUI.
 * Centraliza la carga, redimensionado y generación de placeholders de imágenes de productos.
 */
public final class ImagenUtil {

    private ImagenUtil() {}

    /**
     * Carga y redimensiona una imagen de producto.
     * Si la imagen no existe o falla, devuelve un placeholder gris con el nombre del producto.
     *
     * @param path   ruta relativa o classpath de la imagen
     * @param ancho  ancho destino en px
     * @param alto   alto destino en px
     * @return ImageIcon lista para usar en un JLabel
     */
    public static ImageIcon cargar(String nombreImagen, int ancho, int alto) {
        // Estrategia 1: ruta absoluta desde la raíz del proyecto (user.dir/imagenes/)
        File archivoDisco = new File(
            System.getProperty("user.dir") + File.separator + "imagenes" + File.separator + nombreImagen
        );

        try {
            ImageIcon original = null;

            if (archivoDisco.exists()) {
                original = new ImageIcon(archivoDisco.getAbsolutePath());
                AppLogger.info("Imagen cargada desde disco: " + archivoDisco.getAbsolutePath());
            } else {
                // Estrategia 2: classpath (útil si las imágenes están empaquetadas en el JAR)
                URL imgURL = ImagenUtil.class.getClassLoader().getResource("imagenes/" + nombreImagen);
                if (imgURL != null) {
                    original = new ImageIcon(imgURL);
                }
            }

            if (original == null || original.getIconWidth() <= 0) {
                AppLogger.warn("Imagen no encontrada: " + archivoDisco.getAbsolutePath());
                return crearPlaceholder(nombreImagen, ancho, alto);
            }

            Image img = original.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
            return new ImageIcon(img);

        } catch (Exception e) {
            AppLogger.warn("No se pudo cargar la imagen: " + nombreImagen + " — " + e.getMessage());
            return crearPlaceholder(nombreImagen, ancho, alto);
        }
    }

    private static ImageIcon crearPlaceholder(String path, int ancho, int alto) {
        String nombre = new File(path).getName()
            .replace(".jpg", "").replace(".png", "").replace("imagenes/", "");
        BufferedImage img = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(new Color(230, 230, 230));
        g2.fillRect(0, 0, ancho, alto);
        g2.setColor(new Color(120, 120, 120));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();
        int x = (ancho - fm.stringWidth(nombre)) / 2;
        g2.drawString(nombre, Math.max(x, 5), alto / 2);
        g2.dispose();
        return new ImageIcon(img);
    }
}
