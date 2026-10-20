package dao;

import modelo.CategoriaPlato;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoriaPlatoDAO {

    public Optional<CategoriaPlato> buscarPorId(int idCategoria) {
        String sql = "SELECT * FROM dish_category WHERE id_category = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCategoria);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToCategoria(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error buscando categoría por ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<CategoriaPlato> listarTodas() {
        List<CategoriaPlato> categorias = new ArrayList<>();
        String sql = "SELECT * FROM dish_category ORDER BY display_order";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categorias.add(mapResultSetToCategoria(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listando categorías: " + e.getMessage());
        }
        return categorias;
    }

    public boolean insertar(CategoriaPlato categoria) {
        String sql = "INSERT INTO dish_category (name, description, display_order) VALUES (?, ?, ?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, categoria.getNombre());
            stmt.setString(2, categoria.getDescripcion());
            stmt.setInt(3, categoria.getOrden());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    categoria.setIdCategoria(keys.getInt(1));
                }
                ConexionDB.commit();
                return true;
            }
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error insertando categoría: " + e.getMessage());
        }
        return false;
    }

    public boolean actualizar(CategoriaPlato categoria) {
        String sql = "UPDATE dish_category SET name = ?, description = ?, display_order = ? WHERE id_category = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoria.getNombre());
            stmt.setString(2, categoria.getDescripcion());
            stmt.setInt(3, categoria.getOrden());
            stmt.setInt(4, categoria.getIdCategoria());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                ConexionDB.commit();
                return true;
            }
        } catch (SQLException e) {
            ConexionDB.rollback();
            System.err.println("Error actualizando categoría: " + e.getMessage());
        }
        return false;
    }

    private CategoriaPlato mapResultSetToCategoria(ResultSet rs) throws SQLException {
        CategoriaPlato categoria = new CategoriaPlato();
        categoria.setIdCategoria(rs.getInt("id_category"));
        categoria.setNombre(rs.getString("name"));
        categoria.setDescripcion(rs.getString("description"));
        categoria.setOrden(rs.getInt("display_order"));
        return categoria;
    }
}