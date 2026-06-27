package com.mycompany.proton.service;

import com.mycompany.proton.dao.FortesRHDAO;
import com.mycompany.proton.model.FortesRH;
import com.mycompany.proton.util.LoggerAuditoria;
import java.sql.SQLException;

public class FortesRHService {
    private FortesRHDAO dao = new FortesRHDAO();

    public void salvar(FortesRH rh, boolean isEdicao) throws SQLException {
        dao.salvar(rh, isEdicao);
        
        String acaoLog = isEdicao ? "EDIÇÃO" : "INCLUSÃO";
        LoggerAuditoria.registrar(acaoLog, "Ambiente FortesRH: " + rh.getCliente() + " foi " + (isEdicao ? "alterado." : "criado."));
    }
}