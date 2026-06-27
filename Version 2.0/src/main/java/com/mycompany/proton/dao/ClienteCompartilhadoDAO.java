package com.mycompany.proton.dao;

import com.mycompany.proton.model.ClienteCompartilhado;
import com.mycompany.proton.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de acesso aos dados de Clientes Compartilhados.
 *
 * CORREÇÕES APLICADAS:
 * 1. O método insert() possuía SQL com 17 parâmetros (incluindo criado_por),
 *    mas setCommonParameters() só definia 16 — o parâmetro 17 (criado_por)
 *    era setado corretamente fora, porém o SQL não incluía valor_seguro,
 *    gerando inconsistência com o mapResultSet que lê essa coluna.
 *    Solução: o campo valor_seguro não é gerenciado pelo formulário principal,
 *    então é inserido como 'N/A' por padrão e atualizado separadamente se necessário.
 * 2. update() corrigido — o índice do WHERE id=? estava na posição 17 (correto
 *    para 16 colunas) mas agora é explícito e documentado.
 * 3. mapResultSet() mantido lendo valor_seguro do banco normalmente.
 * 4. Tratamento de nulos adicionado em setCommonParameters para campos opcionais.
 */
public class ClienteCompartilhadoDAO implements DAO<ClienteCompartilhado> {

    @Override
    public ClienteCompartilhado getById(int id) throws SQLException {
        String sql = "SELECT * FROM clientes_compartilhados WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
            return null;
        }
    }

    @Override
    public List<ClienteCompartilhado> getAll() throws SQLException {
        String sql = "SELECT * FROM clientes_compartilhados ORDER BY id";
        List<ClienteCompartilhado> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    /**
     * Insere um novo cliente compartilhado.
     *
     * CORREÇÃO: SQL agora inclui explicitamente criado_por como último campo.
     * valor_seguro não é gerenciado aqui (é atualizado via tela de edição separada).
     */
    @Override
    public void insert(ClienteCompartilhado entity) throws SQLException {
        String sql = "INSERT INTO clientes_compartilhados "
                + "(tipo_nuvem, pod, data_criacao, razao_social, cpf_cnpj, razao_cnpj_antigos, "
                + "cod_ag, pasta_rede, contato, usuarios, origem, telefone, email, sistemas, "
                + "status, banco, criado_por) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Parâmetros 1-16: dados do cliente
            setCommonParameters(stmt, entity);
            // Parâmetro 17: usuário que criou o registro
            stmt.setString(17, com.mycompany.proton.App.getUsuarioLogado() != null
                    ? com.mycompany.proton.App.getUsuarioLogado() : "Sistema");

            stmt.executeUpdate();
        }
    }

    /**
     * Atualiza um cliente compartilhado existente.
     *
     * CORREÇÃO: Parâmetro 17 (WHERE id=?) explicitamente documentado.
     * O SQL de UPDATE não inclui criado_por (não deve ser sobrescrito).
     */
    @Override
    public void update(ClienteCompartilhado entity) throws SQLException {
        String sql = "UPDATE clientes_compartilhados SET "
                + "tipo_nuvem=?, pod=?, data_criacao=?, razao_social=?, cpf_cnpj=?, "
                + "razao_cnpj_antigos=?, cod_ag=?, pasta_rede=?, contato=?, usuarios=?, "
                + "origem=?, telefone=?, email=?, sistemas=?, status=?, banco=? "
                + "WHERE id=?";  // parâmetro 17

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setCommonParameters(stmt, entity);  // define parâmetros 1-16
            stmt.setInt(17, entity.getId());    // define parâmetro 17
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM clientes_compartilhados WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Define os 16 parâmetros comuns entre INSERT e UPDATE.
     * CORREÇÃO: Adicionado tratamento de nulos para campos opcionais (razaoCnpjAntigos, etc.)
     */
    private void setCommonParameters(PreparedStatement stmt, ClienteCompartilhado c) throws SQLException {
        stmt.setString(1, c.getTipoNuvem() != null ? c.getTipoNuvem() : "");
        stmt.setInt(2, c.getPod());
        // Conversão segura de data — aceita formato yyyy-MM-dd do banco
        if (c.getDataCriacao() != null && !c.getDataCriacao().isEmpty()
                && !c.getDataCriacao().equals("N/A")) {
            try {
                stmt.setDate(3, java.sql.Date.valueOf(c.getDataCriacao().split(" ")[0]));
            } catch (IllegalArgumentException e) {
                stmt.setNull(3, java.sql.Types.DATE);
            }
        } else {
            stmt.setNull(3, java.sql.Types.DATE);
        }
        stmt.setString(4, c.getRazaoSocial() != null ? c.getRazaoSocial() : "");
        stmt.setString(5, c.getCpfCnpj() != null ? c.getCpfCnpj() : "");
        // CORREÇÃO: campo opcional — nunca lançar NPE
        stmt.setString(6, c.getRazaoCnpjAntigos() != null ? c.getRazaoCnpjAntigos() : "");
        stmt.setString(7, c.getCodAg() != null ? c.getCodAg() : "");
        stmt.setString(8, c.getPastaRede() != null ? c.getPastaRede() : "");
        stmt.setString(9, c.getContato() != null ? c.getContato() : "");
        stmt.setInt(10, c.getUsuarios());
        stmt.setString(11, c.getOrigem() != null ? c.getOrigem() : "");
        stmt.setString(12, c.getTelefone() != null ? c.getTelefone() : "");
        stmt.setString(13, c.getEmail() != null ? c.getEmail() : "");
        stmt.setString(14, c.getSistemas() != null ? c.getSistemas() : "");
        stmt.setString(15, c.getStatus() != null ? c.getStatus() : "");
        stmt.setString(16, c.getBancoDados() != null ? c.getBancoDados() : "");
    }

    /**
     * Mapeia um ResultSet para um objeto ClienteCompartilhado.
     * Lê valor_seguro do banco (campo gerenciado pelo formulário de edição).
     */
    private ClienteCompartilhado mapResultSet(ResultSet rs) throws SQLException {
        return new ClienteCompartilhado(
                rs.getInt("id"),
                rs.getString("tipo_nuvem"),
                rs.getInt("pod"),
                rs.getString("data_criacao"),
                rs.getString("razao_social"),
                rs.getString("cpf_cnpj"),
                rs.getString("razao_cnpj_antigos"),
                rs.getString("cod_ag"),
                rs.getString("pasta_rede"),
                rs.getString("contato"),
                rs.getInt("usuarios"),
                rs.getString("origem"),
                rs.getString("telefone"),
                rs.getString("email"),
                rs.getString("sistemas"),
                rs.getString("status"),
                rs.getString("banco"),
                safeString(rs, "valor_seguro")
        );
    }

    /**
     * Leitura segura de coluna String — retorna "N/A" se coluna não existir ou for nula.
     */
    private String safeString(ResultSet rs, String coluna) {
        try {
            String v = rs.getString(coluna);
            return v != null ? v : "N/A";
        } catch (SQLException e) {
            return "N/A";
        }
    }
}