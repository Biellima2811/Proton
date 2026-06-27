package com.mycompany.proton.dao;

import com.mycompany.proton.model.Cliente;
import com.mycompany.proton.model.ServidorDedicado;
import com.mycompany.proton.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de acesso aos dados de Clientes Dedicados.
 *
 * CORREÇÕES APLICADAS:
 * 1. Adicionado "implements DAO<Cliente>" — a classe usava @Override nos métodos
 *    mas não declarava a implementação da interface, causando erro de compilação.
 * 2. Adicionado import de ServidorDedicado — estava sendo usado em salvarComServidores
 *    sem o import correspondente.
 * 3. Método getById() agora usa o nome correto (consistente com a interface DAO corrigida).
 * 4. Todos os métodos da interface implementados corretamente.
 */
public class ClienteDedicadoDAO implements DAO<Cliente> {

    /**
     * Salva o cliente e seus servidores em uma única transação atômica.
     * Se qualquer operação falhar, tudo é revertido (rollback).
     */
    public void salvarComServidores(Cliente cliente, List<ServidorDedicado> servidores) throws SQLException {
        String sqlCliente = (cliente.getId() == 0)
                ? "INSERT INTO clientes_dedicados (cliente, cnpj_cpf, qnt_de_servs, ad, ambiente, vpn, criado_por) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id"
                : "UPDATE clientes_dedicados SET cliente=?, cnpj_cpf=?, qnt_de_servs=?, ad=?, ambiente=?, vpn=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int idCliente = cliente.getId();

                try (PreparedStatement cmd = conn.prepareStatement(sqlCliente,
                        (cliente.getId() == 0) ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS)) {

                    cmd.setString(1, cliente.getNome());
                    cmd.setString(2, cliente.getCnpj());
                    cmd.setInt(3, servidores.size());
                    cmd.setString(4, cliente.getActive_directory());
                    cmd.setString(5, cliente.getAmbiente());
                    cmd.setBoolean(6, cliente.isVpn());

                    if (cliente.getId() != 0) {
                        cmd.setInt(7, cliente.getId());
                        cmd.executeUpdate();
                        // Remove servidores antigos para regravar a lista atualizada
                        try (PreparedStatement cmdDel = conn.prepareStatement(
                                "DELETE FROM servidores_clientes_dedicados WHERE cliente_id = ?")) {
                            cmdDel.setInt(1, idCliente);
                            cmdDel.executeUpdate();
                        }
                    } else {
                        cmd.setString(7, com.mycompany.proton.App.getUsuarioLogado() != null
                                ? com.mycompany.proton.App.getUsuarioLogado() : "Sistema");
                        cmd.executeUpdate();
                        try (ResultSet rs = cmd.getGeneratedKeys()) {
                            if (rs.next()) {
                                idCliente = rs.getInt(1);
                            }
                        }
                    }
                }

                // Insere os servidores em lote
                String sqlServ = "INSERT INTO servidores_clientes_dedicados "
                        + "(cliente_id, tipo_servidor, ip_servidor, usuario, senha) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement cmdServ = conn.prepareStatement(sqlServ)) {
                    for (ServidorDedicado serv : servidores) {
                        cmdServ.setInt(1, idCliente);
                        cmdServ.setString(2, serv.getTipo());
                        cmdServ.setString(3, serv.getIp());
                        cmdServ.setString(4, serv.getUsuario());
                        cmdServ.setString(5, serv.getSenha());
                        cmdServ.addBatch();
                    }
                    cmdServ.executeBatch();
                }

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Busca um cliente dedicado pelo ID.
     * CORREÇÃO: @Override agora funciona corretamente com a interface DAO corrigida.
     */
    @Override
    public Cliente getById(int id) throws SQLException {
        String sql = "SELECT * FROM clientes_dedicados WHERE id = ?";
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
    public List<Cliente> getAll() throws SQLException {
        String sql = "SELECT * FROM clientes_dedicados ORDER BY id";
        List<Cliente> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    @Override
    public void insert(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO clientes_dedicados "
                + "(cliente, cnpj_cpf, qnt_de_servs, ad, ambiente, vpn, criado_por) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getCnpj());
            stmt.setInt(3, cliente.getQnt_server());
            stmt.setString(4, cliente.getActive_directory());
            stmt.setString(5, cliente.getAmbiente());
            stmt.setBoolean(6, cliente.isVpn());
            stmt.setString(7, com.mycompany.proton.App.getUsuarioLogado() != null
                    ? com.mycompany.proton.App.getUsuarioLogado() : "Sistema");
            stmt.executeUpdate();
        }
    }

    @Override
    public void update(Cliente cliente) throws SQLException {
        String sql = "UPDATE clientes_dedicados "
                + "SET cliente=?, cnpj_cpf=?, qnt_de_servs=?, ad=?, ambiente=?, vpn=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getCnpj());
            stmt.setInt(3, cliente.getQnt_server());
            stmt.setString(4, cliente.getActive_directory());
            stmt.setString(5, cliente.getAmbiente());
            stmt.setBoolean(6, cliente.isVpn());
            stmt.setInt(7, cliente.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM clientes_dedicados WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Mapeia um ResultSet para um objeto Cliente.
     */
    private Cliente mapResultSet(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id"),
                rs.getString("cliente"),
                rs.getString("cnpj_cpf"),
                rs.getInt("qnt_de_servs"),
                rs.getString("ad"),
                rs.getString("ambiente"),
                rs.getBoolean("vpn"),
                "Ver Servidores"
        );
    }
}