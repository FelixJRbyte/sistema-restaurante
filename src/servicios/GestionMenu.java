package servicios;

import dao.CategoriaPlatoDAO;
import dao.ConexionDB;
import dao.PlatoDAO;
import modelo.CategoriaPlato;
import modelo.Plato;

import java.util.List;
import java.util.Optional;

public class GestionMenu {

    private final PlatoDAO platoDAO = new PlatoDAO();
    private final CategoriaPlatoDAO categoriaDAO = new CategoriaPlatoDAO();

    // Plato operations
    public Optional<Plato> buscarPlatoPorId(int idPlato) {
        return platoDAO.buscarPorId(idPlato);
    }

    public List<Plato> listarPlatosPorCategoria(int idCategoria) {
        return platoDAO.listarPorCategoria(idCategoria);
    }

    public List<Plato> listarPlatosDisponibles() {
        return platoDAO.listarTodosDisponibles();
    }

    public List<Plato> listarTodosLosPlatos() {
        return platoDAO.listarTodos();
    }

    public boolean registrarPlato(Plato plato) {
        return platoDAO.insertar(plato);
    }

    public boolean actualizarPlato(Plato plato) {
        return platoDAO.actualizar(plato);
    }

    public boolean cambiarDisponibilidadPlato(int idPlato, boolean disponible) {
        return platoDAO.cambiarDisponibilidad(idPlato, disponible);
    }

    public boolean eliminarPlato(int idPlato) {
        // Soft delete: mark as unavailable
        return platoDAO.cambiarDisponibilidad(idPlato, false);
    }

    // Categoria operations
    public List<CategoriaPlato> listarCategorias() {
        return categoriaDAO.listarTodas();
    }

    public Optional<CategoriaPlato> buscarCategoriaPorId(int idCategoria) {
        return categoriaDAO.buscarPorId(idCategoria);
    }

    public boolean registrarCategoria(CategoriaPlato categoria) {
        return categoriaDAO.insertar(categoria);
    }

    public boolean actualizarCategoria(CategoriaPlato categoria) {
        return categoriaDAO.actualizar(categoria);
    }

    public void cerrarConexion() {
        ConexionDB.closeConnection();
    }
}