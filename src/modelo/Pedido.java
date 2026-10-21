package modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Pedido extends EntidadBase {

    public enum Estado {
        PENDIENTE,
        EN_PREPARACION,
        LISTO,
        ENTREGADO,
        CANCELADO,
        FACTURADO
    }

    private int idPedido;
    private int idMesa;
    private int idCliente;
    private LocalDateTime fechaHora;
    private Estado estado;
    private String observaciones;
    private double total;

    // Related objects for display
    private List<DetallePedido> detalles;
    private String numeroMesa;
    private String nombreCliente;

    public Pedido() {
        super();
        this.fechaHora = LocalDateTime.now();
        this.estado = Estado.PENDIENTE;
        this.total = 0.0;
        this.detalles = new ArrayList<>();
    }

    public Pedido(int idMesa, int idCliente, String observaciones) {
        super();
        this.idMesa = idMesa;
        this.idCliente = idCliente;
        this.observaciones = observaciones;
        this.fechaHora = LocalDateTime.now();
        this.estado = Estado.PENDIENTE;
        this.total = 0.0;
        this.detalles = new ArrayList<>();
    }

    public Pedido(int idPedido, int idMesa, int idCliente, LocalDateTime fechaHora, Estado estado, String observaciones, double total) {
        super();
        this.idPedido = idPedido;
        this.idMesa = idMesa;
        this.idCliente = idCliente;
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.observaciones = observaciones;
        this.total = total;
        this.detalles = new ArrayList<>();
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles;
        recalcularTotal();
    }

    public void agregarDetalle(DetallePedido detalle) {
        this.detalles.add(detalle);
        recalcularTotal();
    }

    public void removerDetalle(int idPlato) {
        this.detalles.removeIf(d -> d.getIdPlato() == idPlato);
        recalcularTotal();
    }

    private void recalcularTotal() {
        this.total = this.detalles.stream()
                .mapToDouble(DetallePedido::getSubtotal)
                .sum();
    }

    public String getNumeroMesa() {
        return numeroMesa;
    }

    public void setNumeroMesa(String numeroMesa) {
        this.numeroMesa = numeroMesa;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    @Override
    public int getId() {
        return idPedido;
    }

    @Override
    public String obtenerResumen() {
        return String.format("Order #%d | Table: %s | Status: %s | Items: %d | Total: $%.2f",
                idPedido, numeroMesa != null ? numeroMesa : idMesa, estado, detalles.size(), total);
    }

    @Override
    public boolean esValido() {
        return idMesa > 0 && estado != null && total >= 0;
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "idPedido=" + idPedido +
                ", idMesa=" + idMesa +
                ", idCliente=" + idCliente +
                ", fechaHora=" + fechaHora +
                ", estado=" + estado +
                ", observaciones='" + observaciones + '\'' +
                ", total=" + total +
                ", numeroMesa='" + numeroMesa + '\'' +
                ", nombreCliente='" + nombreCliente + '\'' +
                ", detalles=" + detalles.size() + " items" +
                '}';
    }
}
