package com.mycompany.proton.service;

import com.mycompany.proton.dao.ClienteCompartilhadoDAO;
import com.mycompany.proton.model.ClienteCompartilhado;
import java.sql.SQLException;
import java.util.List;

public class ClienteCompartilhadoService {
    private ClienteCompartilhadoDAO dao = new ClienteCompartilhadoDAO();

    public List<ClienteCompartilhado> listarTodos() throws SQLException {
        return dao.getAll();
    }

    public ClienteCompartilhado buscarPorId(int id) throws SQLException {
        return dao.getById(id);
    }

    public void salvar(ClienteCompartilhado cliente) throws SQLException {
        if (cliente.getId() == 0) dao.insert(cliente);
        else dao.update(cliente);
    }

    public void excluir(int id) throws SQLException {
        dao.delete(id);
    }
}