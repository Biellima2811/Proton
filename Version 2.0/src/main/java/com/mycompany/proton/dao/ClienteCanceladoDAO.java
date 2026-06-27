package com.mycompany.proton.dao;

import com.mycompany.proton.model.ClienteCancelado;
import com.mycompany.proton.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * DAO de acesso aos dados de Clientes Cancelados.
 *
 * CORREÇÃO: Nenhum erro de compilação nesta classe, mas foi revisada para:
 * 1. Usar DatabaseConnection.getConnection() de forma consistente.
 * 2. Garantir que o usuário logado não cause NPE se for null.
 * 3. Tratamento seguro de datas — datas inválidas viram NULL no banco.
 */
public class ClienteCanceladoDAO {

    /**
     * Salva (INSERT) ou atualiza (UPDATE) um ClienteCancelado no banco.
     */
    public void salvar(ClienteCancelado c, boolean isEdicao) throws SQLException {
        String sql = isEdicao
                ? "UPDATE clientes_cancelados SET tipo_nuvem=?, pod=?, data_criacao=?, "
                + "cliente_razao=?, status_antigo=?, inicio_cancelamento=?, "
                + "final_cancelamento=?, chamado=?, tecnico_responsavel=? WHERE id=?"
                : "INSERT INTO clientes_cancelados (tipo_nuvem, pod, data_criacao, cliente_razao, "
                + "status_antigo, inicio_cancelamento, final_cancelamento, chamado, "
                + "tecnico_responsavel, criado_por) VALUES (?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement cmd = conn.prepareStatement(sql)) {

            cmd.setString(1, c.getTipo_nuvem() != null ? c.getTipo_nuvem() : "N/A");
            cmd.setInt(2, c.getPod());

            // Conversão segura de String para Date — datas inválidas viram NULL
            setDateSafe(cmd, 3, c.getData_criacao());

            cmd.setString(4, c.getCliente_razao() != null ? c.getCliente_razao() : "");
            cmd.setString(5, c.getStatus_antigo() != null ? c.getStatus_antigo() : "");

            setDateSafe(cmd, 6, c.getInicio_cancelamento());
            setDateSafe(cmd, 7, c.getFinal_cancelamento());

            cmd.setString(8, c.getChamado() != null ? c.getChamado() : "");
            cmd.setString(9, c.getTecnico_responsavel() != null ? c.getTecnico_responsavel() : "");

            if (isEdicao) {
                // Parâmetro 10 = WHERE id=?
                cmd.setInt(10, c.getId());
            } else {
                // Parâmetro 10 = criado_por
                String usuarioLogado = com.mycompany.proton.App.getUsuarioLogado();
                cmd.setString(10, usuarioLogado != null ? usuarioLogado : "Sistema");
            }

            cmd.executeUpdate();
        }
    }

    /**
     * Define uma data no PreparedStatement de forma segura.
     * Se o valor for nulo, vazio ou inválido, insere NULL no banco.
     */
    private void setDateSafe(PreparedStatement cmd, int index, String dataStr) throws SQLException {
        if (dataStr != null && !dataStr.isEmpty() && !dataStr.equals("N/A")) {
            try {
                cmd.setDate(index, java.sql.Date.valueOf(dataStr.split(" ")[0]));
            } catch (IllegalArgumentException e) {
                cmd.setNull(index, Types.DATE);
            }
        } else {
            cmd.setNull(index, Types.DATE);
        }
    }
}