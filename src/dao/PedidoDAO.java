package dao;

import modelo.DetallePedido;
import modelo.Pedido;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PedidoDAO {

    private final DetallePedidoDAO detalleDAO = new DetallePedidoDAO();

    // Mapeo entre enums Java (español) y BD (inglés)
    private static String estadoToDb(Pedido.Estado estado) {
        return switch (estado) {
            case PENDIENTE -> "PENDING";
            case EN_PREPARACION -> "IN_PREPARATION";
            case LISTO -> "READY";
            case ENTREGADO -> "DELIVERED";
            case CANCELADO -> "CANCELLED";
            case FACTURADO -> "INVOICED";
        };
    }

    private static Pedido.Estado estadoFromDb(String dbValue) {
        return switch (dbValue) {
            case "PENDING" -> Pedido.Estado.PENDIENTE;
            case "IN_PREPARATION" -> Pedido.Estado.EN_PREPARACION;
            case "READY" -> Pedido.Estado.LISTO;
            case "DELIVERED" -> Pedido.Estado.ENTREGADO;
            case "CANCELLED" -> Pedido.Estado.CANCELADO;
            case "INVOICED" -> Pedido.Estado.FACTURADO;
            default -> Pedido.Estado.PENDIENTE;
        };
    }

    public Optional<Pedido> buscarPorId(int idPedido) {
        String sql = """
            SELECT p.*, t.number AS table_number, c.name AS customer_name
            FROM `order_restaurant` p
            LEFT JOIN table_restaurant t ON p.id_table = t.id_table
            LEFT JOIN customer c ON p.id_customer = c.id_customer
            WHERE p.id_order = ?
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPedido);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Pedido pedido = mapResultSetToPedido(rs);
                pedido.setDetalles(detalleDAO.buscarPorPedido(idPedido));
                return Optional.of(pedido);
            }
        } catch (SQLException e) {
            System.err.println("Error buscando pedido por ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Pedido> listarPorMesa(int idMesa) {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = """
            SELECT p.*, t.number AS table_number, c.name AS customer_name
            FROM `order_restaurant` p
            LEFT JOIN table_restaurant t ON p.id_table = t.id_table
            LEFT JOIN customer c ON p.id_customer = c.id_customer
            WHERE p.id_table = ?
            ORDER BY p.order_datetime DESC
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMesa);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Pedido pedido = mapResultSetToPedido(rs);
                pedido.setDetalles(detalleDAO.buscarPorPedido(pedido.getIdPedido()));
                pedidos.add(pedido);
            }
        } catch (SQLException e) {
            System.err.println("Error listando pedidos por mesa: " + e.getMessage());
        }
        return pedidos;
    }

    public List<Pedido> listarPorCliente(int idCliente) {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = """
            SELECT p.*, t.number AS table_number, c.name AS customer_name
            FROM `order_restaurant` p
            LEFT JOIN table_restaurant t ON p.id_table = t.id_table
            LEFT JOIN customer c ON p.id_customer = c.id_customer
            WHERE p.id_customer = ?
            ORDER BY p.order_datetime DESC
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCliente);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Pedido pedido = mapResultSetToPedido(rs);
                pedido.setDetalles(detalleDAO.buscarPorPedido(pedido.getIdPedido()));
                pedidos.add(pedido);
            }
        } catch (SQLException e) {
            System.err.println("Error listando pedidos por cliente: " + e.getMessage());
        }
        return pedidos;
    }

    public List<Pedido> listarPorEstado(Pedido.Estado estado) {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = """
            SELECT p.*, t.number AS table_number, c.name AS customer_name
            FROM `order_restaurant` p
            LEFT JOIN table_restaurant t ON p.id_table = t.id_table
            LEFT JOIN customer c ON p.id_customer = c.id_customer
            WHERE p.status = ?
            ORDER BY p.order_datetime
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estadoToDb(estado));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Pedido pedido = mapResultSetToPedido(rs);
                pedido.setDetalles(detalleDAO.buscarPorPedido(pedido.getIdPedido()));
                pedidos.add(pedido);
            }
        } catch (SQLException e) {
            System.err.println("Error listando pedidos por estado: " + e.getMessage());
        }
        return pedidos;
    }

    public List<Pedido> listarPedidosCocina() {
        // Uses the view v_kitchen_orders (already uses correct table name)
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT * FROM v_kitchen_orders";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Pedido pedido = new Pedido();
                pedido.setIdPedido(rs.getInt("id_order"));
                pedido.setIdMesa(rs.getInt("id_table"));
                pedido.setNumeroMesa(rs.getString("table_number"));
                Timestamp fecha = rs.getTimestamp("order_datetime");
                if (fecha != null) pedido.setFechaHora(fecha.toLocalDateTime());
                pedido.setEstado(estadoFromDb(rs.getString("status")));
                pedido.setObservaciones(rs.getString("notes"));
                pedido.setDetalles(detalleDAO.buscarPorPedido(pedido.getIdPedido()));
                pedidos.add(pedido);
            }
        } catch (SQLException e) {
            System.err.println("Error listando pedidos de cocina: " + e.getMessage());
        }
        return pedidos;
    }

    // Create order using stored procedure
    public Optional<Integer> crearPedido(int idMesa, int idCliente, String observaciones) {
        String sql = "{CALL sp_create_order(?, ?, ?)}";
        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, idMesa);
            if (idCliente > 0) {
                stmt.setInt(2, idCliente);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, observaciones);

            boolean hasResults = stmt.execute();
            if (hasResults) {
                ResultSet rs = stmt.getResultSet();
                if (rs.next()) {
                    int idPedido = rs.getInt("id_order");
                    ConexionDB.commit();
                    return Optional.of(idPedido);
                }
            }
            ConexionDB.commit();
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error creando pedido (SP): " + e.getMessage());
        }
        return Optional.empty();
    }

    // Add dish to order using stored procedure
    public boolean agregarPlatoAPedido(int idPedido, int idPlato, int cantidad) {
        String sql = "{CALL sp_add_dish_to_order(?, ?, ?)}";
        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, idPedido);
            stmt.setInt(2, idPlato);
            stmt.setInt(3, cantidad);

            stmt.execute();
            ConexionDB.commit();
            return true;
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error agregando plato a pedido (SP): " + e.getMessage());
        }
        return false;
    }

    public boolean actualizarEstado(int idPedido, Pedido.Estado nuevoEstado) {
        String sql = "UPDATE `order_restaurant` SET status = ? WHERE id_order = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estadoToDb(nuevoEstado));
            stmt.setInt(2, idPedido);

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                ConexionDB.commit();
                return true;
            }
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error actualizando estado de pedido: " + e.getMessage());
        }
        return false;
    }

    public boolean cancelarPedido(int idPedido) {
        return actualizarEstado(idPedido, Pedido.Estado.CANCELADO);
    }

    private Pedido mapResultSetToPedido(ResultSet rs) throws SQLException {
        Pedido pedido = new Pedido();
        pedido.setIdPedido(rs.getInt("id_order"));
        pedido.setIdMesa(rs.getInt("id_table"));
        int idCliente = rs.getInt("id_customer");
        if (!rs.wasNull()) {
            pedido.setIdCliente(idCliente);
        }
        Timestamp fecha = rs.getTimestamp("order_datetime");
        if (fecha != null) {
            pedido.setFechaHora(fecha.toLocalDateTime());
        }
        pedido.setEstado(estadoFromDb(rs.getString("status")));
        pedido.setObservaciones(rs.getString("notes"));
        pedido.setTotal(rs.getDouble("total"));
        pedido.setNumeroMesa(rs.getString("table_number"));
        pedido.setNombreCliente(rs.getString("customer_name"));
        return pedido;
    }
}