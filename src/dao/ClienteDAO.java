package dao;

import modelo.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClienteDAO {

    public Optional<Cliente> buscarPorId(int idCliente) {
        String sql = "SELECT * FROM customer WHERE id_customer = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCliente);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToCliente(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error buscando cliente por ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Cliente> buscarPorEmail(String email) {
        String sql = "SELECT * FROM customer WHERE email = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToCliente(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error buscando cliente por email: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Cliente> listarTodos() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM customer WHERE active = TRUE ORDER BY name";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                clientes.add(mapResultSetToCliente(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listando clientes: " + e.getMessage());
        }
        return clientes;
    }

    public boolean insertar(Cliente cliente) {
        String sql = "INSERT INTO customer (name, phone, email, address) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, cliente.getNombre());
            stmt.setString(2, cliente.getTelefono());
            stmt.setString(3, cliente.getEmail());
            stmt.setString(4, cliente.getDireccion());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    cliente.setIdCliente(keys.getInt(1));
                }
                ConexionDB.commit();
                return true;
            }
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error insertando cliente: " + e.getMessage());
        }
        return false;
    }

    public boolean actualizar(Cliente cliente) {
        String sql = "UPDATE customer SET name = ?, phone = ?, email = ?, address = ?, active = ? WHERE id_customer = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cliente.getNombre());
            stmt.setString(2, cliente.getTelefono());
            stmt.setString(3, cliente.getEmail());
            stmt.setString(4, cliente.getDireccion());
            stmt.setBoolean(5, cliente.isActivo());
            stmt.setInt(6, cliente.getIdCliente());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                ConexionDB.commit();
                return true;
            }
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error actualizando cliente: " + e.getMessage());
        }
        return false;
    }

    public boolean eliminar(int idCliente) {
        // Soft delete - set active to false
        String sql = "UPDATE customer SET active = FALSE WHERE id_customer = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCliente);
            int filas = stmt.executeUpdate();
            if (filas > 0) {
                ConexionDB.commit();
                return true;
            }
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error eliminando cliente: " + e.getMessage());
        }
        return false;
    }

    private Cliente mapResultSetToCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getInt("id_customer"));
        cliente.setNombre(rs.getString("name"));
        cliente.setTelefono(rs.getString("phone"));
        cliente.setEmail(rs.getString("email"));
        cliente.setDireccion(rs.getString("address"));
        Timestamp fechaReg = rs.getTimestamp("registration_date");
        if (fechaReg != null) {
            cliente.setFechaRegistro(fechaReg.toLocalDateTime());
        }
        cliente.setActivo(rs.getBoolean("active"));
        return cliente;
    }
}