package tienda.catys.api.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "Productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "precio", nullable = false)
    private Double precio;

    @Column(name = "imagen", length = 300)
    private String imagen;

    @Column(name = "categoria", nullable = false, length = 80)
    private String categoria;

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Transient
    private Boolean disponible;

    public Producto() {}

    public Producto(Integer id, String nombre, Double precio, String imagen, String categoria, Integer stock) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.imagen = imagen;
        this.categoria = categoria;
        this.stock = stock;
        this.disponible = (stock > 0);
    }

    public Integer getId() { return id; }
    
    public Integer getIdProducto() { return id; }
    
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Double getPrecio() { return precio; }
    
    public void setPrecio(Double precio) { this.precio = precio; }

    public String getImagen() { return imagen; }
    
    public void setImagen(String imagen) { this.imagen = imagen; }

    public String getImagenUrl() {
        if (this.imagen == null || this.imagen.trim().isEmpty()) {
            return "/imagenes/default.jpg";
        }
        return "/imagenes/" + this.imagen.trim().toLowerCase();
    }

    public String getCategoria() { return categoria; }
    
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Integer getStock() { return stock; }
    
    public void setStock(Integer stock) { this.stock = stock; }

    public boolean isDisponible() {
        return stock != null && stock > 0;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }
}
