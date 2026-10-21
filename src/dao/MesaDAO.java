package dao;

import modelo.Mesa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MesaDAO {

    public Optional<Mesa> buscarPorId(int idMesa) {
        String sql = "SELECT * FROM table_restaurant WHERE id_table = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMesa);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToMesa(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching table by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Mesa> buscarPorNumero(int numero) {
        String sql = "SELECT * FROM table_restaurant WHERE number = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, numero);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToMesa(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching table by number: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Mesa> listarTodas() {
        List<Mesa> mesas = new ArrayList<>();
        String sql = "SELECT * FROM table_restaurant ORDER BY number";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                mesas.add(mapResultSetToMesa(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listing tables: " + e.getMessage());
        }
        return mesas;
    }

    public List<Mesa> listarDisponibles(int capacidadMinima) {
        List<Mesa> mesas = new ArrayList<>();
        String sql = "SELECT * FROM table_restaurant WHERE status = 'FREE' AND capacity >= ? ORDER BY capacity, number";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, capacidadMinima);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                mesas.add(mapResultSetToMesa(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listing available tables: " + e.getMessage());
        }
        return mesas;
    }

    public List<Mesa> listarOcupadas() {
        List<Mesa> mesas = new ArrayList<>();
        String sql = "SELECT * FROM table_restaurant WHERE status = 'OCCUPIED' ORDER BY number";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                mesas.add(mapResultSetToMesa(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listing occupied tables: " + e.getMessage());
        }
        return mesas;
    }

    public boolean insertar(Mesa mesa) {
        String sql = "INSERT INTO table_restaurant (number, capacity, status, location) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, mesa.getNumero());
            stmt.setInt(2, mesa.getCapacidad());
            stmt.setString(3, mesa.getEstado());
            stmt.setString(4, mesa.getUbicacion());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    mesa.setIdMesa(keys.getInt(1));
                }
                ConexionDB.commit();
                return true;
            }
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error inserting table: " + e.getMessage());
        }
        return false;
    }

    public boolean actualizar(Mesa mesa) {
        String sql = "UPDATE table_restaurant SET number = ?, capacity = ?, status = ?, location = ? WHERE id_table = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mesa.getNumero());
            stmt.setInt(2, mesa.getCapacidad());
            stmt.setString(3, mesa.getEstado());
            stmt.setString(4, mesa.getUbicacion());
            stmt.setInt(5, mesa.getIdMesa());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                ConexionDB.commit();
                return true;
            }
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error updating table: " + e.getMessage());
        }
        return false;
    }

    public boolean cambiarEstado(int idMesa, String nuevoEstado) {
        String sql = "UPDATE table_restaurant SET status = ? WHERE id_table = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevoEstado);
            stmt.setInt(2, idMesa);

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                ConexionDB.commit();
                return true;
            }
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error changing table status: " + e.getMessage());
        }
        return false;
    }

    // Call stored procedure to change table status with validation
    public boolean cambiarEstadoConValidacion(int idMesa, String nuevoEstado) {
        String sql = "{CALL sp_change_table_status(?, ?)}";
        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, idMesa);
            stmt.setString(2, nuevoEstado);

            stmt.execute();
            ConexionDB.commit();
            return true;
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error in SP sp_change_table_status: " + e.getMessage());
        }
        return false;
    }

    private Mesa mapResultSetToMesa(ResultSet rs) throws SQLException {
        Mesa mesa = new Mesa();
        mesa.setIdMesa(rs.getInt("id_table"));
        mesa.setNumero(rs.getInt("number"));
        mesa.setCapacidad(rs.getInt("capacity"));
        mesa.setEstado(rs.getString("status"));
        mesa.setUbicacion(rs.getString("location"));
        return mesa;
    }
}
