package dao;

import modelo.Factura;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FacturaDAO {

    // Mapping between Java enums (Spanish) and DB ENUMs (English)
    private static String metodoPagoToDb(Factura.MetodoPago metodo) {
        return switch (metodo) {
            case EFECTIVO -> "CASH";
            case TARJETA_CREDITO -> "CREDIT_CARD";
            case TARJETA_DEBITO -> "DEBIT_CARD";
            case TRANSFERENCIA -> "TRANSFER";
            case QR -> "QR";
            case MIXTO -> "MIXED";
        };
    }

    private static Factura.MetodoPago metodoPagoFromDb(String dbValue) {
        return switch (dbValue) {
            case "CASH" -> Factura.MetodoPago.EFECTIVO;
            case "CREDIT_CARD" -> Factura.MetodoPago.TARJETA_CREDITO;
            case "DEBIT_CARD" -> Factura.MetodoPago.TARJETA_DEBITO;
            case "TRANSFER" -> Factura.MetodoPago.TRANSFERENCIA;
            case "QR" -> Factura.MetodoPago.QR;
            case "MIXED" -> Factura.MetodoPago.MIXTO;
            default -> Factura.MetodoPago.EFECTIVO;
        };
    }

    private static String estadoPagoToDb(Factura.EstadoPago estado) {
        return switch (estado) {
            case PENDIENTE -> "PENDING";
            case PAGADO -> "PAID";
            case ANULADO -> "VOIDED";
            case PARCIAL -> "PARTIAL";
        };
    }

    private static Factura.EstadoPago estadoPagoFromDb(String dbValue) {
        return switch (dbValue) {
            case "PENDING" -> Factura.EstadoPago.PENDIENTE;
            case "PAID" -> Factura.EstadoPago.PAGADO;
            case "VOIDED" -> Factura.EstadoPago.ANULADO;
            case "PARTIAL" -> Factura.EstadoPago.PARCIAL;
            default -> Factura.EstadoPago.PENDIENTE;
        };
    }

    public Optional<Factura> buscarPorId(int idFactura) {
        String sql = """
            SELECT f.*, o.id_table, t.number AS table_number, c.name AS customer_name
            FROM invoice f
            JOIN `order_restaurant` o ON f.id_order = o.id_order
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
            System.err.println("Error searching invoice by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Factura> buscarPorPedido(int idPedido) {
        String sql = """
            SELECT f.*, o.id_table, t.number AS table_number, c.name AS customer_name
            FROM invoice f
            JOIN `order_restaurant` o ON f.id_order = o.id_order
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
            System.err.println("Error searching invoice by order: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Factura> buscarPorNumero(String numeroFactura) {
        String sql = """
            SELECT f.*, o.id_table, t.number AS table_number, c.name AS customer_name
            FROM invoice f
            JOIN `order_restaurant` o ON f.id_order = o.id_order
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
            System.err.println("Error searching invoice by number: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Factura> listarPorEstadoPago(Factura.EstadoPago estado) {
        List<Factura> facturas = new ArrayList<>();
        String sql = """
            SELECT f.*, o.id_table, t.number AS table_number, c.name AS customer_name
            FROM invoice f
            JOIN `order_restaurant` o ON f.id_order = o.id_order
            LEFT JOIN table_restaurant t ON o.id_table = t.id_table
            LEFT JOIN customer c ON o.id_customer = c.id_customer
            WHERE f.payment_status = ?
            ORDER BY f.issue_date DESC
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estadoPagoToDb(estado));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                facturas.add(mapResultSetToFactura(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listing invoices by status: " + e.getMessage());
        }
        return facturas;
    }

    public List<Factura> listarPendientes() {
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
                factura.setEstadoPago(estadoPagoFromDb(rs.getString("payment_status")));
                factura.setNumeroMesa(rs.getString("table_number"));
                factura.setNombreCliente(rs.getString("customer"));
                facturas.add(factura);
            }
        } catch (SQLException e) {
            System.err.println("Error listing pending invoices: " + e.getMessage());
        }
        return facturas;
    }

    public List<Factura> listarVentasDiarias(LocalDateTime fecha) {
        List<Factura> facturas = new ArrayList<>();
        String sql = "SELECT * FROM v_daily_sales WHERE date = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(fecha.toLocalDate()));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Factura f = new Factura();
                f.setFechaEmision(fecha);
                f.setTotal(rs.getDouble("total_sales"));
                f.setObservaciones(String.format(
                        "Invoices: %d | Cash: $%.2f | Card: $%.2f | Transfer: $%.2f | QR: $%.2f",
                        rs.getInt("total_invoices"),
                        rs.getDouble("cash_sales"),
                        rs.getDouble("card_sales"),
                        rs.getDouble("transfer_sales"),
                        rs.getDouble("qr_sales")
                ));
                facturas.add(f);
            }
        } catch (SQLException e) {
            System.err.println("Error listing daily sales: " + e.getMessage());
        }
        return facturas;
    }

    public double obtenerTotalPagado(int idFactura) {
        String sql = "SELECT COALESCE(SUM(amount), 0.00) AS total_paid FROM invoice_payment WHERE id_invoice = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFactura);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total_paid");
            }
        } catch (SQLException e) {
            System.err.println("Error getting paid amount for invoice: " + e.getMessage());
        }
        return 0.0;
    }

    public Optional<Integer> generarFactura(int idPedido, Factura.MetodoPago metodoPago, String numeroFactura) {
        String sql = "{CALL sp_generate_invoice(?, ?, ?)}";
        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, idPedido);
            stmt.setString(2, metodoPagoToDb(metodoPago));
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
            System.err.println("Error generating invoice (SP): " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean registrarPago(int idFactura, double monto, Factura.MetodoPago metodoPago, String referencia) {
        String sql = "{CALL sp_register_payment(?, ?, ?, ?)}";
        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, idFactura);
            stmt.setDouble(2, monto);
            stmt.setString(3, metodoPagoToDb(metodoPago));
            stmt.setString(4, referencia);

            stmt.execute();
            ConexionDB.commit();
            return true;
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error registering payment (SP): " + e.getMessage());
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
            System.err.println("Error voiding invoice: " + e.getMessage());
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
        factura.setMetodoPago(metodoPagoFromDb(rs.getString("payment_method")));
        factura.setEstadoPago(estadoPagoFromDb(rs.getString("payment_status")));
        factura.setNumeroFactura(rs.getString("invoice_number"));
        factura.setObservaciones(rs.getString("notes"));
        factura.setNumeroMesa(rs.getString("table_number"));
        factura.setNombreCliente(rs.getString("customer_name"));
        return factura;
    }
}
