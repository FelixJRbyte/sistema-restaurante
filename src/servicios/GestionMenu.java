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

    // Dish operations
    public Optional<Plato> buscarPlatoPorId(int idPlato) {
        return platoDAO.buscarPorId(idPlato);
    }

    public List<Plato> buscarPlatosPorNombre(String termino) {
        return platoDAO.buscarPorNombre(termino);
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

    public List<Plato> listarPlatosMasVendidos(int limite) {
        return platoDAO.listarMasVendidos(limite);
    }

    public boolean registrarPlato(Plato plato) {
        if (!plato.esValido()) {
            return false;
        }
        return platoDAO.insertar(plato);
    }

    public boolean actualizarPlato(Plato plato) {
        if (!plato.esValido()) {
            return false;
        }
        return platoDAO.actualizar(plato);
    }

    public boolean cambiarDisponibilidadPlato(int idPlato, boolean disponible) {
        return platoDAO.cambiarDisponibilidad(idPlato, disponible);
    }

    public boolean eliminarPlato(int idPlato) {
        // Soft delete: mark as unavailable
        return platoDAO.cambiarDisponibilidad(idPlato, false);
    }

    // Category operations
    public List<CategoriaPlato> listarCategorias() {
        return categoriaDAO.listarTodas();
    }

    public Optional<CategoriaPlato> buscarCategoriaPorId(int idCategoria) {
        return categoriaDAO.buscarPorId(idCategoria);
    }

    public boolean registrarCategoria(CategoriaPlato categoria) {
        if (!categoria.esValido()) {
            return false;
        }
        return categoriaDAO.insertar(categoria);
    }

    public boolean actualizarCategoria(CategoriaPlato categoria) {
        if (!categoria.esValido()) {
            return false;
        }
        return categoriaDAO.actualizar(categoria);
    }

    public void cerrarConexion() {
        ConexionDB.closeConnection();
    }
}
