package servicios;

import dao.ConexionDB;
import dao.FacturaDAO;
import dao.PedidoDAO;
import modelo.Factura;
import modelo.Pedido;

import java.util.List;
import java.util.Optional;

public class GestionPedidos {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final FacturaDAO facturaDAO = new FacturaDAO();

    public Optional<Pedido> buscarPedidoPorId(int idPedido) {
        return pedidoDAO.buscarPorId(idPedido);
    }

    public List<Pedido> listarPedidosPorMesa(int idMesa) {
        return pedidoDAO.listarPorMesa(idMesa);
    }

    public List<Pedido> listarPedidosPorCliente(int idCliente) {
        return pedidoDAO.listarPorCliente(idCliente);
    }

    public List<Pedido> listarPedidosPorEstado(Pedido.Estado estado) {
        return pedidoDAO.listarPorEstado(estado);
    }

    public List<Pedido> listarPedidosCocina() {
        return pedidoDAO.listarPedidosCocina();
    }

    public Optional<Integer> crearPedido(int idMesa, int idCliente, String observaciones) {
        return pedidoDAO.crearPedido(idMesa, idCliente, observaciones);
    }

    public boolean agregarPlatoAPedido(int idPedido, int idPlato, int cantidad) {
        return pedidoDAO.agregarPlatoAPedido(idPedido, idPlato, cantidad);
    }

    public boolean eliminarPlatoDePedido(int idPedido, int idPlato) {
        return pedidoDAO.eliminarPlatoDePedido(idPedido, idPlato);
    }

    public boolean actualizarEstadoPedido(int idPedido, Pedido.Estado nuevoEstado) {
        return pedidoDAO.actualizarEstado(idPedido, nuevoEstado);
    }

    public boolean cancelarPedido(int idPedido) {
        return pedidoDAO.cancelarPedido(idPedido);
    }

    // Invoice operations
    public Optional<Integer> generarFactura(int idPedido, Factura.MetodoPago metodoPago, String numeroFactura) {
        return facturaDAO.generarFactura(idPedido, metodoPago, numeroFactura);
    }

    public boolean registrarPagoFactura(int idFactura, double monto, Factura.MetodoPago metodoPago, String referencia) {
        return facturaDAO.registrarPago(idFactura, monto, metodoPago, referencia);
    }

    public Optional<Factura> buscarFacturaPorId(int idFactura) {
        return facturaDAO.buscarPorId(idFactura);
    }

    public Optional<Factura> buscarFacturaPorPedido(int idPedido) {
        return facturaDAO.buscarPorPedido(idPedido);
    }

    public List<Factura> listarFacturasPendientes() {
        return facturaDAO.listarPendientes();
    }

    public List<Factura> listarFacturasPorEstado(Factura.EstadoPago estado) {
        return facturaDAO.listarPorEstadoPago(estado);
    }

    public boolean anularFactura(int idFactura) {
        return facturaDAO.anularFactura(idFactura);
    }

    public void cerrarConexion() {
        ConexionDB.closeConnection();
    }
}
