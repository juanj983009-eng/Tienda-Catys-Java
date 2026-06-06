package tienda.catys.api.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Integer id;

    @Column(name = "cliente_nombre", nullable = false, length = 200)
    private String clienteNombre;

    @Column(name = "metodo_pago", nullable = false, length = 30)
    private String metodoPago;

    @Column(name = "detalle_compra", columnDefinition = "NVARCHAR(MAX)")
    private String detalleCompra;

    @Column(name = "total", nullable = false)
    private Double total;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    // Constructors
    public Venta() {}

    public Venta(String clienteNombre, String metodoPago, String detalleCompra, Double total) {
        this.clienteNombre = clienteNombre;
        this.metodoPago = metodoPago;
        this.detalleCompra = detalleCompra;
        this.total = total;
        this.fecha = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getDetalleCompra() {
        return detalleCompra;
    }

    public void setDetalleCompra(String detalleCompra) {
        this.detalleCompra = detalleCompra;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
