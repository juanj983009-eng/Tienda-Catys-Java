package tienda.catys.api.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "Mesas")
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mesa")
    private Integer id;

    @Column(name = "numero", nullable = false, unique = true, length = 50)
    private String numero;

    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "disponible";

    @Column(name = "zona", nullable = false, length = 80)
    private String zona;

    // Constructors
    public Mesa() {}

    public Mesa(String numero, Integer capacidad, String estado, String zona) {
        this.numero = numero;
        this.capacidad = capacidad;
        this.estado = estado;
        this.zona = zona;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }
}
