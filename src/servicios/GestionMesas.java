package servicios;

import dao.ConexionDB;
import dao.MesaDAO;
import modelo.Mesa;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class GestionMesas {

    private final MesaDAO mesaDAO = new MesaDAO();

    public Optional<Mesa> buscarMesaPorId(int idMesa) {
        return mesaDAO.buscarPorId(idMesa);
    }

    public Optional<Mesa> buscarMesaPorNumero(int numero) {
        return mesaDAO.buscarPorNumero(numero);
    }

    public List<Mesa> listarTodasLasMesas() {
        return mesaDAO.listarTodas();
    }

    public List<Mesa> listarMesasDisponibles(int capacidadMinima) {
        return mesaDAO.listarDisponibles(capacidadMinima);
    }

    public List<Mesa> listarMesasOcupadas() {
        return mesaDAO.listarOcupadas();
    }

    public boolean registrarMesa(Mesa mesa) {
        return mesaDAO.insertar(mesa);
    }

    public boolean actualizarMesa(Mesa mesa) {
        return mesaDAO.actualizar(mesa);
    }

    public boolean cambiarEstadoMesa(int idMesa, String nuevoEstado) {
        // Use stored procedure for validation
        return mesaDAO.cambiarEstadoConValidacion(idMesa, nuevoEstado);
    }

    public boolean liberarMesa(int idMesa) {
        return mesaDAO.cambiarEstadoConValidacion(idMesa, "FREE");
    }

    public boolean eliminarMesa(int idMesa) {
        // Soft delete: change status to MAINTENANCE
        return mesaDAO.cambiarEstadoConValidacion(idMesa, "MAINTENANCE");
    }

    public void cerrarConexion() {
        ConexionDB.closeConnection();
    }
}