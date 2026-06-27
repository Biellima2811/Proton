package com.mycompany.proton.service;

import com.mycompany.proton.dao.ClienteCanceladoDAO;
import com.mycompany.proton.model.ClienteCancelado;
import com.mycompany.proton.util.LoggerAuditoria;
import java.sql.SQLException;

public class ClienteCanceladoService {
    private ClienteCanceladoDAO dao = new ClienteCanceladoDAO();

    public void salvar(ClienteCancelado cliente, boolean isEdicao) throws SQLException {
        dao.salvar(cliente, isEdicao);
        
        String acaoLog = isEdicao ? "EDIÇÃO" : "INCLUSÃO";
        LoggerAuditoria.registrar(acaoLog, "Registro de Cancelamento: " + cliente.getCliente_razao() + " foi " + (isEdicao ? "alterado." : "criado."));
    }
}