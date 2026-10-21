package modelo;

public class Mesa extends EntidadBase {

    private int idMesa;
    private int numero;
    private int capacidad;
    private String estado;
    private String ubicacion;

    public Mesa() {
        super();
        this.estado = "FREE";
    }

    public Mesa(int numero, int capacidad, String estado) {
        super();
        this.numero = numero;
        this.capacidad = capacidad;
        this.estado = estado;
    }

    public Mesa(int idMesa, int numero, int capacidad, String estado, String ubicacion) {
        super();
        this.idMesa = idMesa;
        this.numero = numero;
        this.capacidad = capacidad;
        this.estado = estado;
        this.ubicacion = ubicacion;
    }

    public int getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    @Override
    public int getId() {
        return idMesa;
    }

    @Override
    public String obtenerResumen() {
        return String.format("Table #%d (ID: %d) | Capacity: %d | Status: %s | Location: %s",
                numero, idMesa, capacidad, estado, ubicacion != null ? ubicacion : "N/A");
    }

    @Override
    public boolean esValido() {
        return numero > 0 && capacidad > 0 && capacidad <= 20 && estado != null && !estado.isBlank();
    }

    @Override
    public String toString() {
        return "Mesa{" +
                "idMesa=" + idMesa +
                ", numero=" + numero +
                ", capacidad=" + capacidad +
                ", estado='" + estado + '\'' +
                ", ubicacion='" + ubicacion + '\'' +
                '}';
    }
}
