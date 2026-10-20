package dao;

import modelo.Factura;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FacturaDAO {

    public Optional<Factura> buscarPorId(int idFactura) {
        String sql = """
            SELECT f.*, o.id_table, t.number AS table_number, c.name AS customer_name
            FROM invoice f
            JOIN `order` o ON f.id_order = o.id_order
            LEFT JOIN table_restaurant t ON o.id_table = t.id_table
            LEFT JOIN customer c ON o.id_customer = c.id_customer
            WHERE f.id_invoice = ?
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFactura);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToFactura(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error buscando factura por ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Factura> buscarPorPedido(int idPedido) {
        String sql = """
            SELECT f.*, o.id_table, t.number AS table_number, c.name AS customer_name
            FROM invoice f
            JOIN `order` o ON f.id_order = o.id_order
            LEFT JOIN table_restaurant t ON o.id_table = t.id_table
            LEFT JOIN customer c ON o.id_customer = c.id_customer
            WHERE f.id_order = ?
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPedido);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToFactura(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error buscando factura por pedido: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Factura> buscarPorNumero(String numeroFactura) {
        String sql = """
            SELECT f.*, o.id_table, t.number AS table_number, c.name AS customer_name
            FROM invoice f
            JOIN `order` o ON f.id_order = o.id_order
            LEFT JOIN table_restaurant t ON o.id_table = t.id_table
            LEFT JOIN customer c ON o.id_customer = c.id_customer
            WHERE f.invoice_number = ?
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numeroFactura);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToFactura(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error buscando factura por número: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Factura> listarPorEstadoPago(Factura.EstadoPago estado) {
        List<Factura> facturas = new ArrayList<>();
        String sql = """
            SELECT f.*, o.id_table, t.number AS table_number, c.name AS customer_name
            FROM invoice f
            JOIN `order` o ON f.id_order = o.id_order
            LEFT JOIN table_restaurant t ON o.id_table = t.id_table
            LEFT JOIN customer c ON o.id_customer = c.id_customer
            WHERE f.payment_status = ?
            ORDER BY f.issue_date DESC
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estado.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                facturas.add(mapResultSetToFactura(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listando facturas por estado: " + e.getMessage());
        }
        return facturas;
    }

    public List<Factura> listarPendientes() {
        // Uses the view v_pending_invoices
        List<Factura> facturas = new ArrayList<>();
        String sql = "SELECT * FROM v_pending_invoices";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Factura factura = new Factura();
                factura.setIdFactura(rs.getInt("id_invoice"));
                factura.setNumeroFactura(rs.getString("invoice_number"));
                Timestamp fecha = rs.getTimestamp("issue_date");
                if (fecha != null) factura.setFechaEmision(fecha.toLocalDateTime());
                factura.setTotal(rs.getDouble("total"));
                factura.setEstadoPago(Factura.EstadoPago.valueOf(rs.getString("payment_status")));
                factura.setNumeroMesa(rs.getString("table_number"));
                factura.setNombreCliente(rs.getString("customer"));
                facturas.add(factura);
            }
        } catch (SQLException e) {
            System.err.println("Error listando facturas pendientes: " + e.getMessage());
        }
        return facturas;
    }

    public List<Factura> listarVentasDiarias(LocalDateTime fecha) {
        // Uses the view v_daily_sales
        List<Factura> facturas = new ArrayList<>();
        String sql = "SELECT * FROM v_daily_sales WHERE date = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(fecha.toLocalDate()));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // This returns aggregated data, not individual invoices
                // Use for reports only
            }
        } catch (SQLException e) {
            System.err.println("Error listando ventas diarias: " + e.getMessage());
        }
        return facturas;
    }

    // Generate invoice using stored procedure
    public Optional<Integer> generarFactura(int idPedido, Factura.MetodoPago metodoPago, String numeroFactura) {
        String sql = "{CALL sp_generate_invoice(?, ?, ?)}";
        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, idPedido);
            stmt.setString(2, metodoPago.name());
            stmt.setString(3, numeroFactura);

            boolean hasResults = stmt.execute();
            if (hasResults) {
                ResultSet rs = stmt.getResultSet();
                if (rs.next()) {
                    int idFactura = rs.getInt("id_invoice");
                    ConexionDB.commit();
                    return Optional.of(idFactura);
                }
            }
            ConexionDB.commit();
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error generando factura (SP): " + e.getMessage());
        }
        return Optional.empty();
    }

    // Register payment using stored procedure
    public boolean registrarPago(int idFactura, double monto, Factura.MetodoPago metodoPago, String referencia) {
        String sql = "{CALL sp_register_payment(?, ?, ?, ?)}";
        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, idFactura);
            stmt.setDouble(2, monto);
            stmt.setString(3, metodoPago.name());
            stmt.setString(4, referencia);

            stmt.execute();
            ConexionDB.commit();
            return true;
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error registrando pago (SP): " + e.getMessage());
        }
        return false;
    }

    public boolean anularFactura(int idFactura) {
        String sql = "UPDATE invoice SET payment_status = 'VOIDED' WHERE id_invoice = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFactura);
            int filas = stmt.executeUpdate();
            if (filas > 0) {
                ConexionDB.commit();
                return true;
            }
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error anulando factura: " + e.getMessage());
        }
        return false;
    }

    private Factura mapResultSetToFactura(ResultSet rs) throws SQLException {
        Factura factura = new Factura();
        factura.setIdFactura(rs.getInt("id_invoice"));
        factura.setIdPedido(rs.getInt("id_order"));
        Timestamp fecha = rs.getTimestamp("issue_date");
        if (fecha != null) {
            factura.setFechaEmision(fecha.toLocalDateTime());
        }
        factura.setSubtotal(rs.getDouble("subtotal"));
        factura.setImpuestos(rs.getDouble("taxes"));
        factura.setTotal(rs.getDouble("total"));
        factura.setMetodoPago(Factura.MetodoPago.valueOf(rs.getString("payment_method")));
        factura.setEstadoPago(Factura.EstadoPago.valueOf(rs.getString("payment_status")));
        factura.setNumeroFactura(rs.getString("invoice_number"));
        factura.setObservaciones(rs.getString("notes"));
        factura.setNumeroMesa(rs.getString("table_number"));
        factura.setNombreCliente(rs.getString("customer_name"));
        return factura;
    }
}