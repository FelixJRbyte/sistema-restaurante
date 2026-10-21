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
            System.err.println("Error searching dish by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Plato> buscarPorNombre(String termino) {
        List<Plato> platos = new ArrayList<>();
        String sql = """
            SELECT p.*, c.name AS category_name
            FROM dish p
            LEFT JOIN dish_category c ON p.id_category = c.id_category
            WHERE p.name LIKE ?
            ORDER BY p.name
            """;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + termino + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                platos.add(mapResultSetToPlato(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching dishes by name: " + e.getMessage());
        }
        return platos;
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
            System.err.println("Error listing dishes by category: " + e.getMessage());
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
            System.err.println("Error listing available dishes: " + e.getMessage());
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
            System.err.println("Error listing all dishes: " + e.getMessage());
        }
        return platos;
    }

    public List<Plato> listarMasVendidos(int limite) {
        List<Plato> platos = new ArrayList<>();
        String sql = "SELECT * FROM v_top_dishes LIMIT ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limite);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Plato plato = new Plato();
                plato.setIdPlato(rs.getInt("id_dish"));
                plato.setNombre(rs.getString("name"));
                plato.setIdCategoria(rs.getInt("id_category"));
                plato.setNombreCategoria(rs.getString("category_name"));
                plato.setPrecio(rs.getDouble("price"));
                plato.setCantidadVendida(rs.getInt("total_sold"));
                plato.setIngresosGenerados(rs.getDouble("total_revenue"));
                platos.add(plato);
            }
        } catch (SQLException e) {
            System.err.println("Error listing top selling dishes: " + e.getMessage());
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
            System.err.println("Error inserting dish: " + e.getMessage());
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
            System.err.println("Error updating dish: " + e.getMessage());
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
            System.err.println("Error changing dish availability: " + e.getMessage());
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
