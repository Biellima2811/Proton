package com.mycompany.proton.dao;

import com.mycompany.proton.model.FortesRH;
import com.mycompany.proton.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * DAO de acesso aos dados de ambientes Fortes RH.
 *
 * CORREÇÕES APLICADAS: 1. Chamadas a f.getStatus(), f.getData_criacao(),
 * f.getIp_publico(), f.getIp_privado(), f.getVersao(), f.getWeb_aplication()
 * agora funcionam porque esses campos foram adicionados ao modelo
 * FortesRH.java. 2. Adicionado setDateSafe() para converter String de data com
 * segurança. 3. Garantido que campos nulos não causem NPE nos setString().
 */
public class FortesRHDAO {

    /**
     * Salva (INSERT) ou atualiza (UPDATE) um registro FortesRH.
     */
    public void salvar(FortesRH f, boolean isEdicao) throws SQLException {
        String sql = isEdicao
                ? "UPDATE fortesrh SET tipo_ambiente=?, cliente=?, cnpj_cpf=?, url_acesso=?, "
                + "servidor_app=?, banco_dados=?, pasta_web=?, usuario_db=?, senha_db=?, "
                + "load_balance=?, ip_load_balance=?, status=?, data_criacao=?, ip_publico=?, "
                + "ip_privado=?, versao=?, web_aplication=? WHERE id=?"
                : "INSERT INTO fortesrh (tipo_ambiente, cliente, cnpj_cpf, url_acesso, "
                + "servidor_app, banco_dados, pasta_web, usuario_db, senha_db, load_balance, "
                + "ip_load_balance, status, data_criacao, ip_publico, ip_privado, versao, "
                + "web_aplication, criado_por) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Parâmetros 1-11: campos originais do modelo
            stmt.setString(1, safe(f.getTipo_ambiente()));
            stmt.setString(2, safe(f.getCliente()));
            stmt.setString(3, safe(f.getCnpj_cpf()));
            stmt.setString(4, safe(f.getUrl_acesso()));
            stmt.setString(5, safe(f.getServidor_app()));
            stmt.setString(6, safe(f.getBanco_dados()));
            stmt.setString(7, safe(f.getPasta_web()));
            stmt.setString(8, safe(f.getUsuario_db()));
            stmt.setString(9, safe(f.getSenha_db()));
            stmt.setString(10, safe(f.getLoad_balance()));
            stmt.setString(11, safe(f.getIp_load_balance()));

            // Parâmetros 12-17: campos adicionados ao modelo FortesRH (CORREÇÃO)
            stmt.setString(12, safe(f.getStatus()));
            setDateSafe(stmt, 13, f.getData_criacao());
            stmt.setString(14, safe(f.getIp_publico()));
            stmt.setString(15, safe(f.getIp_privado()));
            stmt.setString(16, safe(f.getVersao()));
            stmt.setString(17, safe(f.getWeb_aplication()));

            // Parâmetro 18: ID (UPDATE) ou criado_por (INSERT)
            if (isEdicao) {
                stmt.setInt(18, f.getId());
            } else {
                String usuarioLogado = com.mycompany.proton.App.getUsuarioLogado();
                stmt.setString(18, usuarioLogado != null ? usuarioLogado : "Sistema");
            }

            stmt.executeUpdate();
        }
    }

    /**
     * Garante que um valor String nunca seja null ao setar no
     * PreparedStatement.
     */
    private String safe(String valor) {
        return valor != null ? valor : "N/A";
    }

    /**
     * Define uma data no PreparedStatement de forma segura. Se o valor for
     * nulo, vazio ou inválido, insere NULL no banco.
     */
    private void setDateSafe(PreparedStatement stmt, int index, String dataStr) throws SQLException {
        if (dataStr != null && !dataStr.isEmpty() && !dataStr.equals("N/A")) {
            try {
                stmt.setDate(index, java.sql.Date.valueOf(dataStr.split(" ")[0]));
            } catch (IllegalArgumentException e) {
                stmt.setNull(index, Types.DATE);
            }
        } else {
            stmt.setNull(index, Types.DATE);
        }
    }
}
