package modelo;

public class CategoriaPlato extends EntidadBase {

    private int idCategoria;
    private String nombre;
    private String descripcion;
    private int orden;

    public CategoriaPlato() {
        super();
        this.orden = 0;
    }

    public CategoriaPlato(int idCategoria, String nombre, String descripcion, int orden) {
        super();
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.orden = orden;
    }

    public CategoriaPlato(String nombre, String descripcion, int orden) {
        super();
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
    public int getId() {
        return idCategoria;
    }

    @Override
    public String obtenerResumen() {
        return String.format("Category #%d: %s (%s)", idCategoria, nombre, descripcion != null ? descripcion : "");
    }

    @Override
    public boolean esValido() {
        return nombre != null && !nombre.isBlank() && orden >= 0;
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
