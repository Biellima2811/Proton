package com.mycompany.proton.util;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class HistoricoAuditoria {

    public static void registrar(String tabela, int registroId, String campo, String valorAntigo, String valorNovo) {
        String sql = "INSERT INTO historico_alteracoes (tabela, registro_id, campo, valor_antigo, valor_novo, usuario) VALUES (?, ?, ?, ?, ?, ?)";
        String usuario = com.mycompany.proton.App.getUsuarioLogado() != null ? com.mycompany.proton.App.getUsuarioLogado() : "SISTEMA";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tabela);
            stmt.setInt(2, registroId);
            stmt.setString(3, campo);
            stmt.setString(4, valorAntigo);
            stmt.setString(5, valorNovo);
            stmt.setString(6, usuario);
            stmt.executeUpdate();
        } catch (Exception e) {
            // log
        }
    }
}