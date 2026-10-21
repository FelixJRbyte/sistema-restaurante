package modelo;

import java.time.LocalDateTime;

public class Cliente extends EntidadBase {

    private int idCliente;
    private String nombre;
    private String telefono;
    private String email;
    private String direccion;
    private LocalDateTime fechaRegistro;
    private boolean activo;

    public Cliente() {
        super();
        this.activo = true;
        this.fechaRegistro = LocalDateTime.now();
    }

    public Cliente(int idCliente, String nombre, String telefono, String email, String direccion) {
        super();
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.activo = true;
        this.fechaRegistro = LocalDateTime.now();
    }

    public Cliente(String nombre, String telefono, String email, String direccion) {
        super();
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.activo = true;
        this.fechaRegistro = LocalDateTime.now();
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public int getId() {
        return idCliente;
    }

    @Override
    public String obtenerResumen() {
        return String.format("Customer #%d: %s | Phone: %s | Email: %s",
                idCliente, nombre, telefono != null ? telefono : "N/A", email != null ? email : "N/A");
    }

    @Override
    public boolean esValido() {
        return nombre != null && !nombre.isBlank();
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "idCliente=" + idCliente +
                ", nombre='" + nombre + '\'' +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                ", direccion='" + direccion + '\'' +
                ", fechaRegistro=" + fechaRegistro +
                ", activo=" + activo +
                '}';
    }
}
