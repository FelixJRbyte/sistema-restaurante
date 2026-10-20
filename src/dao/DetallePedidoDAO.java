package dao;

import modelo.DetallePedido;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DetallePedidoDAO {

    public List<DetallePedido> buscarPorPedido(int idPedido) {
        List<DetallePedido> detalles = new ArrayList<>();
        String sql = """
            SELECT od.*, d.name AS dish_name
            FROM order_detail od
            LEFT JOIN dish d ON od.id_dish = d.id_dish
            WHERE od.id_order = ?
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPedido);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                detalles.add(mapResultSetToDetalle(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error buscando detalles de pedido: " + e.getMessage());
        }
        return detalles;
    }

    public Optional<DetallePedido> buscarPorId(int idDetalle) {
        String sql = """
            SELECT od.*, d.name AS dish_name
            FROM order_detail od
            LEFT JOIN dish d ON od.id_dish = d.id_dish
            WHERE od.id_detail = ?
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idDetalle);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToDetalle(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error buscando detalle por ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean insertar(DetallePedido detalle) {
        String sql = "INSERT INTO order_detail (id_order, id_dish, quantity, unit_price, notes) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, detalle.getIdPedido());
            stmt.setInt(2, detalle.getIdPlato());
            stmt.setInt(3, detalle.getCantidad());
            stmt.setDouble(4, detalle.getPrecioUnitario());
            stmt.setString(5, detalle.getObservaciones());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    detalle.setIdDetalle(keys.getInt(1));
                }
                ConexionDB.commit();
                return true;
            }
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error insertando detalle: " + e.getMessage());
        }
        return false;
    }

    public boolean actualizar(DetallePedido detalle) {
        String sql = "UPDATE order_detail SET quantity = ?, unit_price = ?, notes = ? WHERE id_detail = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, detalle.getCantidad());
            stmt.setDouble(2, detalle.getPrecioUnitario());
            stmt.setString(3, detalle.getObservaciones());
            stmt.setInt(4, detalle.getIdDetalle());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                ConexionDB.commit();
                return true;
            }
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error actualizando detalle: " + e.getMessage());
        }
        return false;
    }

    public boolean eliminar(int idDetalle) {
        String sql = "DELETE FROM order_detail WHERE id_detail = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idDetalle);
            int filas = stmt.executeUpdate();
            if (filas > 0) {
                ConexionDB.commit();
                return true;
            }
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error eliminando detalle: " + e.getMessage());
        }
        return false;
    }

    public boolean eliminarPorPedido(int idPedido) {
        String sql = "DELETE FROM order_detail WHERE id_order = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPedido);
            int filas = stmt.executeUpdate();
            if (filas > 0) {
                ConexionDB.commit();
                return true;
            }
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error eliminando detalles de pedido: " + e.getMessage());
        }
        return false;
    }

    private DetallePedido mapResultSetToDetalle(ResultSet rs) throws SQLException {
        DetallePedido detalle = new DetallePedido();
        detalle.setIdDetalle(rs.getInt("id_detail"));
        detalle.setIdPedido(rs.getInt("id_order"));
        detalle.setIdPlato(rs.getInt("id_dish"));
        detalle.setCantidad(rs.getInt("quantity"));
        detalle.setPrecioUnitario(rs.getDouble("unit_price"));
        detalle.setObservaciones(rs.getString("notes"));
        detalle.setNombrePlato(rs.getString("dish_name"));
        return detalle;
    }
}