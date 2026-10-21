package modelo;

public class DetallePedido extends EntidadBase {

    private int idDetalle;
    private int idPedido;
    private int idPlato;
    private int cantidad;
    private double precioUnitario;
    private String observaciones;

    // For display purposes
    private String nombrePlato;

    public DetallePedido() {
        super();
        this.cantidad = 1;
    }

    public DetallePedido(int idDetalle, int idPedido, int idPlato, int cantidad, double precioUnitario, String observaciones) {
        super();
        this.idDetalle = idDetalle;
        this.idPedido = idPedido;
        this.idPlato = idPlato;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.observaciones = observaciones;
    }

    public DetallePedido(int idPedido, int idPlato, int cantidad, double precioUnitario) {
        super();
        this.idPedido = idPedido;
        this.idPlato = idPlato;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdPlato() {
        return idPlato;
    }

    public void setIdPlato(int idPlato) {
        this.idPlato = idPlato;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public double getSubtotal() {
        return cantidad * precioUnitario;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getNombrePlato() {
        return nombrePlato;
    }

    public void setNombrePlato(String nombrePlato) {
        this.nombrePlato = nombrePlato;
    }

    @Override
    public int getId() {
        return idDetalle;
    }

    @Override
    public String obtenerResumen() {
        return String.format("%dx %s ($%.2f each = $%.2f)",
                cantidad, nombrePlato != null ? nombrePlato : "Dish #" + idPlato, precioUnitario, getSubtotal());
    }

    @Override
    public boolean esValido() {
        return idPlato > 0 && cantidad > 0 && precioUnitario >= 0;
    }

    @Override
    public String toString() {
        return "DetallePedido{" +
                "idDetalle=" + idDetalle +
                ", idPedido=" + idPedido +
                ", idPlato=" + idPlato +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", subtotal=" + getSubtotal() +
                ", observaciones='" + observaciones + '\'' +
                ", nombrePlato='" + nombrePlato + '\'' +
                '}';
    }
}
