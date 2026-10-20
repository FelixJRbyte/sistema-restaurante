package dao;

import modelo.Plato;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlatoDAO {

    public Optional<Plato> buscarPorId(int idPlato) {
        String sql = """
            SELECT p.*, c.name AS category_name
            FROM dish p
            LEFT JOIN dish_category c ON p.id_category = c.id_category
            WHERE p.id_dish = ?
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPlato);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToPlato(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error buscando plato por ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Plato> listarPorCategoria(int idCategoria) {
        List<Plato> platos = new ArrayList<>();
        String sql = """
            SELECT p.*, c.name AS category_name
            FROM dish p
            LEFT JOIN dish_category c ON p.id_category = c.id_category
            WHERE p.id_category = ? AND p.available = TRUE
            ORDER BY p.name
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCategoria);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                platos.add(mapResultSetToPlato(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listando platos por categoría: " + e.getMessage());
        }
        return platos;
    }

    public List<Plato> listarTodosDisponibles() {
        List<Plato> platos = new ArrayList<>();
        String sql = """
            SELECT p.*, c.name AS category_name
            FROM dish p
            LEFT JOIN dish_category c ON p.id_category = c.id_category
            WHERE p.available = TRUE
            ORDER BY c.display_order, p.name
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                platos.add(mapResultSetToPlato(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listando platos disponibles: " + e.getMessage());
        }
        return platos;
    }

    public List<Plato> listarTodos() {
        List<Plato> platos = new ArrayList<>();
        String sql = """
            SELECT p.*, c.name AS category_name
            FROM dish p
            LEFT JOIN dish_category c ON p.id_category = c.id_category
            ORDER BY c.display_order, p.name
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                platos.add(mapResultSetToPlato(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listando todos los platos: " + e.getMessage());
        }
        return platos;
    }

    public boolean insertar(Plato plato) {
        String sql = "INSERT INTO dish (id_category, name, description, price, available, prep_time_minutes) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, plato.getIdCategoria());
            stmt.setString(2, plato.getNombre());
            stmt.setString(3, plato.getDescripcion());
            stmt.setDouble(4, plato.getPrecio());
            stmt.setBoolean(5, plato.isDisponible());
            stmt.setInt(6, plato.getTiempoPreparacion());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    plato.setIdPlato(keys.getInt(1));
                }
                ConexionDB.commit();
                return true;
            }
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error insertando plato: " + e.getMessage());
        }
        return false;
    }

    public boolean actualizar(Plato plato) {
        String sql = "UPDATE dish SET id_category = ?, name = ?, description = ?, price = ?, available = ?, prep_time_minutes = ? WHERE id_dish = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, plato.getIdCategoria());
            stmt.setString(2, plato.getNombre());
            stmt.setString(3, plato.getDescripcion());
            stmt.setDouble(4, plato.getPrecio());
            stmt.setBoolean(5, plato.isDisponible());
            stmt.setInt(6, plato.getTiempoPreparacion());
            stmt.setInt(7, plato.getIdPlato());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                ConexionDB.commit();
                return true;
            }
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error actualizando plato: " + e.getMessage());
        }
        return false;
    }

    public boolean cambiarDisponibilidad(int idPlato, boolean disponible) {
        String sql = "UPDATE dish SET available = ? WHERE id_dish = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, disponible);
            stmt.setInt(2, idPlato);

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                ConexionDB.commit();
                return true;
            }
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error cambiando disponibilidad: " + e.getMessage());
        }
        return false;
    }

    private Plato mapResultSetToPlato(ResultSet rs) throws SQLException {
        Plato plato = new Plato();
        plato.setIdPlato(rs.getInt("id_dish"));
        plato.setIdCategoria(rs.getInt("id_category"));
        plato.setNombre(rs.getString("name"));
        plato.setDescripcion(rs.getString("description"));
        plato.setPrecio(rs.getDouble("price"));
        plato.setDisponible(rs.getBoolean("available"));
        plato.setTiempoPreparacion(rs.getInt("prep_time_minutes"));
        plato.setNombreCategoria(rs.getString("category_name"));
        return plato;
    }
}