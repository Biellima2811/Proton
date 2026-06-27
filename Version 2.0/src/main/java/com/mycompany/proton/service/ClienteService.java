package com.mycompany.proton.service;

import com.mycompany.proton.dao.ClienteDedicadoDAO;
import com.mycompany.proton.model.Cliente;
import java.sql.SQLException;
import java.util.List;

public class ClienteService {

    private ClienteDedicadoDAO dao = new ClienteDedicadoDAO();

    public List<Cliente> listarTodos() throws SQLException {
        return dao.getAll();
    }

    public Cliente buscarPorId(int id) throws SQLException {
        return dao.getById(id);
    }

    public void salvar(Cliente cliente) throws SQLException {
        if (cliente.getId() == 0) {
            dao.insert(cliente);
        } else {
            dao.update(cliente);
        }
    }

    public void excluir(int id) throws SQLException {
        dao.delete(id);
    }
}