package modelo;

import java.time.LocalDateTime;

public class Factura {

    public enum MetodoPago {
        EFECTIVO,
        TARJETA_CREDITO,
        TARJETA_DEBITO,
        TRANSFERENCIA,
        QR,
        MIXTO
    }

    public enum EstadoPago {
        PENDIENTE,
        PAGADO,
        ANULADO,
        PARCIAL
    }

    private int idFactura;
    private int idPedido;
    private LocalDateTime fechaEmision;
    private double subtotal;
    private double impuestos;
    private double total;
    private MetodoPago metodoPago;
    private EstadoPago estadoPago;
    private String numeroFactura;
    private String observaciones;

    // Related objects for display
    private String numeroMesa;
    private String nombreCliente;

    public Factura() {
        this.fechaEmision = LocalDateTime.now();
        this.estadoPago = EstadoPago.PENDIENTE;
        this.impuestos = 0.0;
    }

    public Factura(int idPedido, double subtotal, double impuestos, double total, MetodoPago metodoPago, String numeroFactura) {
        this.idPedido = idPedido;
        this.subtotal = subtotal;
        this.impuestos = impuestos;
        this.total = total;
        this.metodoPago = metodoPago;
        this.numeroFactura = numeroFactura;
        this.fechaEmision = LocalDateTime.now();
        this.estadoPago = (metodoPago == MetodoPago.EFECTIVO) ? EstadoPago.PAGADO : EstadoPago.PENDIENTE;
    }

    public Factura(int idFactura, int idPedido, LocalDateTime fechaEmision, double subtotal, double impuestos, double total, MetodoPago metodoPago, EstadoPago estadoPago, String numeroFactura, String observaciones) {
        this.idFactura = idFactura;
        this.idPedido = idPedido;
        this.fechaEmision = fechaEmision;
        this.subtotal = subtotal;
        this.impuestos = impuestos;
        this.total = total;
        this.metodoPago = metodoPago;
        this.estadoPago = estadoPago;
        this.numeroFactura = numeroFactura;
        this.observaciones = observaciones;
    }

    public int getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(int idFactura) {
        this.idFactura = idFactura;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(double impuestos) {
        this.impuestos = impuestos;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public EstadoPago getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(EstadoPago estadoPago) {
        this.estadoPago = estadoPago;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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
    public String toString() {
        return "Factura{" +
                "idFactura=" + idFactura +
                ", idPedido=" + idPedido +
                ", fechaEmision=" + fechaEmision +
                ", subtotal=" + subtotal +
                ", impuestos=" + impuestos +
                ", total=" + total +
                ", metodoPago=" + metodoPago +
                ", estadoPago=" + estadoPago +
                ", numeroFactura='" + numeroFactura + '\'' +
                ", observaciones='" + observaciones + '\'' +
                ", numeroMesa='" + numeroMesa + '\'' +
                ", nombreCliente='" + nombreCliente + '\'' +
                '}';
    }
}