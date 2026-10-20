package modelo;

public class Plato {

    private int idPlato;
    private int idCategoria;
    private String nombre;
    private String descripcion;
    private double precio;
    private boolean disponible;
    private int tiempoPreparacion;

    // Category name for display purposes (not in DB directly)
    private String nombreCategoria;

    public Plato() {
        this.disponible = true;
        this.tiempoPreparacion = 15;
    }

    public Plato(int idPlato, int idCategoria, String nombre, String descripcion, double precio, boolean disponible, int tiempoPreparacion) {
        this.idPlato = idPlato;
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.disponible = disponible;
        this.tiempoPreparacion = tiempoPreparacion;
    }

    public Plato(int idCategoria, String nombre, String descripcion, double precio, int tiempoPreparacion) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.disponible = true;
        this.tiempoPreparacion = tiempoPreparacion;
    }

    public int getIdPlato() {
        return idPlato;
    }

    public void setIdPlato(int idPlato) {
        this.idPlato = idPlato;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public int getTiempoPreparacion() {
        return tiempoPreparacion;
    }

    public void setTiempoPreparacion(int tiempoPreparacion) {
        this.tiempoPreparacion = tiempoPreparacion;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    @Override
    public String toString() {
        return "Plato{" +
                "idPlato=" + idPlato +
                ", idCategoria=" + idCategoria +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precio=" + precio +
                ", disponible=" + disponible +
                ", tiempoPreparacion=" + tiempoPreparacion +
                ", nombreCategoria='" + nombreCategoria + '\'' +
                '}';
    }
}