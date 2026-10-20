package modelo;

public class CategoriaPlato {

    private int idCategoria;
    private String nombre;
    private String descripcion;
    private int orden;

    public CategoriaPlato() {
        this.orden = 0;
    }

    public CategoriaPlato(int idCategoria, String nombre, String descripcion, int orden) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.orden = orden;
    }

    public CategoriaPlato(String nombre, String descripcion, int orden) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.orden = orden;
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

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    @Override
    public String toString() {
        return "CategoriaPlato{" +
                "idCategoria=" + idCategoria +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", orden=" + orden +
                '}';
    }
}