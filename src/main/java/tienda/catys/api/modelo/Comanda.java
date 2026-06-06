package tienda.catys.api.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Comandas")
public class Comanda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comanda")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mesa")
    private Mesa mesa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @Column(name = "cliente_nombre", nullable = false, length = 200)
    private String clienteNombre = "Público General";

    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "en_cola";

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @OneToMany(mappedBy = "comanda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComandaItem> items = new ArrayList<>();

    // Constructors
    public Comanda() {}

    public Comanda(Mesa mesa, Cliente cliente, String clienteNombre, String estado) {
        this.mesa = mesa;
        this.cliente = cliente;
        this.clienteNombre = clienteNombre;
        this.estado = estado;
        this.fechaHora = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public void setMesa(Mesa mesa) {
        this.mesa = mesa;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public List<ComandaItem> getItems() {
        return items;
    }

    public void setItems(List<ComandaItem> items) {
        this.items = items;
    }

    // Helper method to add items
    public void addItem(ComandaItem item) {
        items.add(item);
        item.setComanda(this);
    }

    // Helper method to remove items
    public void removeItem(ComandaItem item) {
        items.remove(item);
        item.setComanda(null);
    }
}
