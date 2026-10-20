package servicios;

import dao.ClienteDAO;
import dao.ConexionDB;
import modelo.Cliente;

import java.util.List;
import java.util.Optional;

public class GestionClientes {

    private final ClienteDAO clienteDAO = new ClienteDAO();

    public Optional<Cliente> buscarClientePorId(int idCliente) {
        return clienteDAO.buscarPorId(idCliente);
    }

    public Optional<Cliente> buscarClientePorEmail(String email) {
        return clienteDAO.buscarPorEmail(email);
    }

    public List<Cliente> listarTodosLosClientes() {
        return clienteDAO.listarTodos();
    }

    public boolean registrarCliente(Cliente cliente) {
        return clienteDAO.insertar(cliente);
    }

    public boolean actualizarCliente(Cliente cliente) {
        return clienteDAO.actualizar(cliente);
    }

    public boolean eliminarCliente(int idCliente) {
        return clienteDAO.eliminar(idCliente);
    }

    public void cerrarConexion() {
        ConexionDB.closeConnection();
    }
}