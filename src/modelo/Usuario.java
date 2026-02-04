package modelo;

public class Usuario {
    private int id;
    private String nombreCompleto;
    private String username;
    private String password;
    private String rol;

    public Usuario() {}

    public Usuario(int id, String nombre, String user, String pass, String rol) {
        this.id = id;
        this.nombreCompleto = nombre;
        this.username = user;
        this.password = pass;
        this.rol = rol;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombre) { this.nombreCompleto = nombre; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}