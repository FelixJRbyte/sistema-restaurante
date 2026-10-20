package servicios;

import dao.ConexionDB;
import dao.FacturaDAO;
import modelo.Factura;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class GestionFacturas {

    private final FacturaDAO facturaDAO = new FacturaDAO();

    public Optional<Factura> buscarFacturaPorId(int idFactura) {
        return facturaDAO.buscarPorId(idFactura);
    }

    public Optional<Factura> buscarFacturaPorPedido(int idPedido) {
        return facturaDAO.buscarPorPedido(idPedido);
    }

    public Optional<Factura> buscarFacturaPorNumero(String numeroFactura) {
        return facturaDAO.buscarPorNumero(numeroFactura);
    }

    public List<Factura> listarFacturasPendientes() {
        return facturaDAO.listarPendientes();
    }

    public List<Factura> listarFacturasPorEstado(Factura.EstadoPago estado) {
        return facturaDAO.listarPorEstadoPago(estado);
    }

    public Optional<Integer> generarFactura(int idPedido, Factura.MetodoPago metodoPago, String numeroFactura) {
        return facturaDAO.generarFactura(idPedido, metodoPago, numeroFactura);
    }

    public boolean registrarPago(int idFactura, double monto, Factura.MetodoPago metodoPago, String referencia) {
        return facturaDAO.registrarPago(idFactura, monto, metodoPago, referencia);
    }

    public boolean anularFactura(int idFactura) {
        return facturaDAO.anularFactura(idFactura);
    }

    public List<Factura> listarVentasDiarias(LocalDateTime fecha) {
        return facturaDAO.listarVentasDiarias(fecha);
    }

    public void cerrarConexion() {
        ConexionDB.closeConnection();
    }
}